package com.example.hortlink.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hortlink.R;
import com.example.hortlink.adapters.PedidoAdapter;
import com.example.hortlink.data.model.Pedido;
import com.example.hortlink.data.repository.PedidoRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class PedidosFragment extends Fragment {

    private RecyclerView recyclerPedidos;
    private PedidoAdapter adapter;
    private View layoutVazio;

    private final List<Pedido> listaPedidos = new ArrayList<>();
    private final PedidoRepository pedidoRepository = new PedidoRepository();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pedidos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerPedidos = view.findViewById(R.id.recyclerPedidos);
        layoutVazio = view.findViewById(R.id.layoutVazio);
        recyclerPedidos.setLayoutManager(new LinearLayoutManager(getContext()));

        // Comprador não tem botões de aceitar/recusar → listener null
        adapter = new PedidoAdapter(listaPedidos, null);
        recyclerPedidos.setAdapter(adapter);

        carregarPedidos();
    }

    private void carregarPedidos() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        pedidoRepository.listarPorComprador(uid, new PedidoRepository.CallbackLista() {
            @Override
            public void onSuccess(List<Pedido> pedidos) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    adapter.atualizarLista(pedidos);
                    layoutVazio.setVisibility(pedidos.isEmpty() ? View.VISIBLE : View.GONE);
                    recyclerPedidos.setVisibility(pedidos.isEmpty() ? View.GONE : View.VISIBLE);
                });
            }

            @Override
            public void onError(String erro) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Erro ao carregar pedidos", Toast.LENGTH_SHORT).show());
            }
        });
    }
}