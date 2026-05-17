package com.example.hortlink.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hortlink.R;
import com.example.hortlink.adapters.CarrinhoAdapter;
import com.example.hortlink.bd.SupabaseHelper;
import com.example.hortlink.data.model.CartItem;
import com.example.hortlink.entidades.CartManager;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CarrinhoFragment extends Fragment {
    private List<CartItem> cartItems = new ArrayList<>();
    private CarrinhoAdapter adapter;

    private View layoutEmpty;
    private View layoutFooter;
    private TextView tvTotal;
    private Button btnCheckout;
    private RecyclerView rvCart;

    private String usuarioId;
    private SupabaseHelper supabase;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_carrinho, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        usuarioId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        supabase  = new SupabaseHelper(requireContext());

        // Views — IDs do fragment_carrinho.xml
        layoutEmpty  = view.findViewById(R.id.layout_empty);
        layoutFooter = view.findViewById(R.id.layout_footer);
        tvTotal      = view.findViewById(R.id.tv_total);
        btnCheckout  = view.findViewById(R.id.btn_checkout);
        rvCart       = view.findViewById(R.id.rv_cart);

        rvCart.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CarrinhoAdapter(
                cartItems,
                this::removerItem,
                this::alterarQuantidade
        );
        rvCart.setAdapter(adapter);

        btnCheckout.setOnClickListener(v ->
                startActivity(new Intent(requireActivity(), CheckoutActivity.class)));

        carregarCarrinho();
    }

    @Override
    public void onResume() {
        super.onResume();
        cartItems.clear();             // limpa memória antes de recarregar
        adapter.notifyDataSetChanged();
        alternarEstadoVazio();         // mostra vazio enquanto a requisição vai
        carregarCarrinho();            // busca o estado real do Supabase
    }

    // ─── Carrega do Supabase ─────────────────────────────────────────
    private void carregarCarrinho() {
        String url = "/rest/v1/carrinho"
                + "?select=id,quantidade,produtos(id,nome,preco,foto_url,unidade,produtor_id)"
                + "&usuario_id=eq." + usuarioId;

        supabase.get(url, new SupabaseHelper.SupabaseCallback() {
            @Override
            public void onSuccess(String resultado) {
                try {
                    JSONArray rows = new JSONArray(resultado);
                    cartItems.clear();

                    for (int i = 0; i < rows.length(); i++) {
                        JSONObject row     = rows.getJSONObject(i);
                        JSONObject produto = row.getJSONObject("produtos");

                        CartItem item = new CartItem();
                        item.setCarrinhoId(row.getString("id"));
                        item.setQuantidade(row.getInt("quantidade"));
                        item.setProdutoId(produto.getString("id"));
                        item.setNomeProduto(produto.getString("nome"));
                        item.setPreco(produto.getDouble("preco"));
                        item.setFotoUrl(produto.getString("foto_url"));
                        item.setUnidade(produto.getString("unidade"));
                        item.setProducerId(produto.getString("produtor_id"));
                        cartItems.add(item);
                    }

                    // Sincroniza CartManager local
                    CartManager cm = CartManager.getInstance(requireContext());
                    cm.clearCart();
                    for (CartItem ci : cartItems) cm.addItem(ci);

                    atualizarUi();

                } catch (Exception e) {
                    mostrarErro("Erro ao processar carrinho");
                }
            }

            @Override
            public void onError(String erro) {
                mostrarErro("Erro ao carregar carrinho: " + erro);
            }
        });
    }

    // ─── Remove item ─────────────────────────────────────────────────
    private void removerItem(CartItem item) {
        supabase.delete(
                "/rest/v1/carrinho?id=eq." + item.getCarrinhoId(),
                new SupabaseHelper.SupabaseCallback() {
                    @Override
                    public void onSuccess(String resultado) {
                        cartItems.remove(item);
                        CartManager.getInstance(requireContext())
                                .removeItem(item.getProdutoId());
                        atualizarUi();
                    }

                    @Override
                    public void onError(String erro) {
                        mostrarErro("Erro ao remover item");
                    }
                });
    }

    // ─── Altera quantidade ───────────────────────────────────────────
    private void alterarQuantidade(CartItem item, int novaQtd) {
        if (novaQtd <= 0) {
            removerItem(item);
            return;
        }

        try {
            JSONObject body = new JSONObject();
            body.put("quantidade", novaQtd);

            supabase.patch(
                    "/rest/v1/carrinho?id=eq." + item.getCarrinhoId(),
                    body,
                    new SupabaseHelper.SupabaseCallback() {
                        @Override
                        public void onSuccess(String resultado) {
                            item.setQuantidade(novaQtd);
                            atualizarUi();
                        }

                        @Override
                        public void onError(String erro) {
                            mostrarErro("Erro ao atualizar quantidade");
                        }
                    });

        } catch (Exception e) {
            mostrarErro("Erro interno");
        }
    }

    // ─── UI helpers ──────────────────────────────────────────────────
    private void atualizarUi() {
        if (!isAdded() || getActivity() == null) return;
        getActivity().runOnUiThread(() -> {
            adapter.notifyDataSetChanged();
            atualizarTotal();
            alternarEstadoVazio();
        });
    }

    private void atualizarTotal() {
        double total = 0;
        for (CartItem i : cartItems) total += i.getSubtotal();
        tvTotal.setText(String.format("Total: R$ %.2f", total));
    }

    private void alternarEstadoVazio() {
        boolean vazio = cartItems.isEmpty();
        // Carrinho vazio: mostra ilustração, esconde lista e footer
        layoutEmpty.setVisibility(vazio ? View.VISIBLE : View.GONE);
        rvCart.setVisibility(vazio ? View.GONE : View.VISIBLE);
        layoutFooter.setVisibility(vazio ? View.GONE : View.VISIBLE);
    }

    private void mostrarErro(String msg) {
        if (!isAdded() || getActivity() == null) return;
        getActivity().runOnUiThread(() ->
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show());
    }
}