package com.example.hortlink.ui.consumidor;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hortlink.R;
import com.example.hortlink.adapters.CarrinhoAdapter;

import com.example.hortlink.data.model.CarrinhoResponse;
import com.example.hortlink.data.model.ItemCarrinhoResponse;
import com.example.hortlink.data.repository.CarrinhoRepository;
import com.example.hortlink.entidades.CartManager;
import com.example.hortlink.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class CarrinhoFragment extends Fragment {

    // Substituímos o modelo antigo CartItem pelo DTO gerado pelo backend
    private List<ItemCarrinhoResponse> cartItems = new ArrayList<>();
    private CarrinhoAdapter adapter;
    private static final int QUANTIDADE_MAXIMA = 99;

    private View layoutEmpty;
    private View layoutFooter;
    private TextView tvTotal;
    private Button btnCheckout;
    private RecyclerView rvCart;


    // Novo Repository substituindo o SupabaseHelper
    private CarrinhoRepository repository;
    private Long compradorId;
    private Double valorTotalCarrinho = 0.0;

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

        // Inicializa o Repository
        repository = new CarrinhoRepository();

        // Pega o ID numérico diretamente do SessionManager
        compradorId = SessionManager.getInstance().getUsuarioId();

        // Trava de segurança: Se retornar -1, a sessão é inválida
        if (compradorId == -1L) {
            mostrarErro("Sessão expirada. Por favor, faça login novamente.");
            // Opcional: Redirecionar para a tela de Login aqui
            return;
        }


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
        cartItems.clear();
        adapter.notifyDataSetChanged();
        alternarEstadoVazio();
        carregarCarrinho();
    }

    // ─── Carrega da API Spring Boot ─────────────────────────────────────────
    private void carregarCarrinho() {
        repository.obterCarrinho(compradorId, new CarrinhoRepository.CarrinhoCallback() {
            @Override
            public void onSuccess(CarrinhoResponse carrinho) {
                processarSucessoCarrinho(carrinho);
            }

            @Override
            public void onError(String erro) {
                mostrarErro(erro);
            }
        });
    }

    // ─── Remove item ─────────────────────────────────────────────────
    private void removerItem(ItemCarrinhoResponse item) {
        // Agora passamos o ID do item diretamente para a API REST
        repository.removerItem(compradorId, item.getId(), new CarrinhoRepository.CarrinhoCallback() {
            @Override
            public void onSuccess(CarrinhoResponse carrinhoAtualizado) {
                // A API já nos devolve o carrinho inteiro atualizado, recarregamos a tela!
                CartManager.getInstance(requireContext()).removeItem(String.valueOf(item.getOfertaId()));
                processarSucessoCarrinho(carrinhoAtualizado);
            }

            @Override
            public void onError(String erro) {
                mostrarErro(erro);
            }
        });
    }

    // ─── Altera quantidade ───────────────────────────────────────────
    private void alterarQuantidade(ItemCarrinhoResponse item, int novaQtd) {
        if (novaQtd <= 0) {
            removerItem(item);
            return;
        }

        repository.alterarQuantidade(compradorId, item.getId(), novaQtd, new CarrinhoRepository.CarrinhoCallback() {
            @Override
            public void onSuccess(CarrinhoResponse carrinhoAtualizado) {
                // Ao invés de alterar só localmente, deixamos o backend ser a fonte da verdade
                processarSucessoCarrinho(carrinhoAtualizado);
            }

            @Override
            public void onError(String erro) {
                mostrarErro(erro);
            }
        });
    }

    // ─── Método auxiliar para extrair a lógica repetida ────────────────
    private void processarSucessoCarrinho(CarrinhoResponse carrinho) {
        cartItems.clear();

        if (carrinho.getItens() != null) {
            cartItems.addAll(carrinho.getItens());
        }

        // Puxa o valor total já calculado pelo Spring Boot
        valorTotalCarrinho = carrinho.getValorTotal() != null ? carrinho.getValorTotal() : 0.0;

        // Opcional: Sincroniza o CartManager local se ainda for usar
        CartManager cm = CartManager.getInstance(requireContext());
        cm.clearCart();
        for (ItemCarrinhoResponse ci : cartItems) {
            // Adapte o seu CartManager para aceitar o novo DTO se necessário
            // cm.addItem(ci);
        }

        atualizarUi();
    }

    // ─── UI helpers ──────────────────────────────────────────────────
    private void atualizarUi() {
        if (!isAdded()) return;
        adapter.notifyDataSetChanged();
        atualizarTotal();
        alternarEstadoVazio();
    }

    private void atualizarTotal() {
        // Não precisa mais fazer o 'for' somando na mão!
        tvTotal.setText(String.format("Total: R$ %.2f", valorTotalCarrinho));
    }

    private void alternarEstadoVazio() {
        boolean vazio = cartItems.isEmpty();
        layoutEmpty.setVisibility(vazio ? View.VISIBLE : View.GONE);
        rvCart.setVisibility(vazio ? View.GONE : View.VISIBLE);
        layoutFooter.setVisibility(vazio ? View.GONE : View.VISIBLE);
    }

    private void mostrarErro(String msg) {
        if (!isAdded() || getContext() == null) return;
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }
}