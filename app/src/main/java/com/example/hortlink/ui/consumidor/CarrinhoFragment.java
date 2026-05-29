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
import com.example.hortlink.service.BaseCallback; // <-- Import corrigido
import com.example.hortlink.util.SessionManager;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CarrinhoFragment extends Fragment {

    private List<ItemCarrinhoResponse> cartItems = new ArrayList<>();
    private CarrinhoAdapter adapter;

    private View layoutEmpty;
    private View layoutFooter;
    private TextView tvTotal;
    private Button btnCheckout;
    private RecyclerView rvCart;

    private CarrinhoRepository repository;
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

        repository = new CarrinhoRepository();

        // A verificação de sessão ainda é válida para garantir que o usuário está logado no app,
        // mas não precisamos mais passar o ID para o repositório, pois o JWT cuida disso.
        if (SessionManager.getInstance().getUsuarioId() == -1L) {
            mostrarErro("Sessão expirada. Por favor, faça login novamente.");
            return;
        }

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
        // CORREÇÃO: Removido o compradorId e usado o BaseCallback
        repository.obterCarrinho(new BaseCallback<CarrinhoResponse>() {
            @Override
            public void onSuccess(CarrinhoResponse carrinho) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> processarSucessoCarrinho(carrinho));
            }

            @Override
            public void onError(String erro) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> mostrarErro(erro));
            }
        });
    }

    // ─── Remove item ─────────────────────────────────────────────────
    private void removerItem(ItemCarrinhoResponse item) {
        // CORREÇÃO: Removido o compradorId
        repository.removerItem(item.getId(), new BaseCallback<CarrinhoResponse>() {
            @Override
            public void onSuccess(CarrinhoResponse carrinhoAtualizado) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> processarSucessoCarrinho(carrinhoAtualizado));
            }

            @Override
            public void onError(String erro) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> mostrarErro(erro));
            }
        });
    }

    // ─── Altera quantidade ───────────────────────────────────────────
    private void alterarQuantidade(ItemCarrinhoResponse item, int novaQtd) {
        if (novaQtd <= 0) {
            removerItem(item);
            return;
        }

        // CORREÇÃO: Removido o compradorId
        repository.alterarQuantidade(item.getId(), novaQtd, new BaseCallback<CarrinhoResponse>() {
            @Override
            public void onSuccess(CarrinhoResponse carrinhoAtualizado) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> processarSucessoCarrinho(carrinhoAtualizado));
            }

            @Override
            public void onError(String erro) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> mostrarErro(erro));
            }
        });
    }

    // ─── Método auxiliar ───────────────────────────────────────────────────
    private void processarSucessoCarrinho(CarrinhoResponse carrinho) {
        cartItems.clear();

        if (carrinho.getItens() != null) {
            cartItems.addAll(carrinho.getItens());
        }

        valorTotalCarrinho = carrinho.getValorTotal() != null ? carrinho.getValorTotal() : 0.0;
        atualizarUi();
    }

    // ─── UI helpers ──────────────────────────────────────────────────
    private void atualizarUi() {
        adapter.notifyDataSetChanged();
        atualizarTotal();
        alternarEstadoVazio();
    }

    private void atualizarTotal() {
        // CORREÇÃO: Usando NumberFormat para garantir padrão PT-BR sem dar crash
        NumberFormat formatoMoeda = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        tvTotal.setText("Total: " + formatoMoeda.format(valorTotalCarrinho));
    }

    private void alternarEstadoVazio() {
        boolean vazio = cartItems.isEmpty();
        layoutEmpty.setVisibility(vazio ? View.VISIBLE : View.GONE);
        rvCart.setVisibility(vazio ? View.GONE : View.VISIBLE);
        layoutFooter.setVisibility(vazio ? View.GONE : View.VISIBLE);
    }

    private void mostrarErro(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }
}