package com.example.hortlink.ui.produtor;

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
import com.example.hortlink.service.BaseCallback;

import java.util.ArrayList;
import java.util.List;

public class PedidosProdutorFragment extends Fragment {

    private RecyclerView recyclerPedidos;
    private PedidoAdapter adapter;
    private View layoutVazio;
    private View progressBar;

    private final List<Pedido> listaPedidos = new ArrayList<>();
    private final PedidoRepository pedidoRepository = new PedidoRepository();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pedidos_produtor, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerPedidos = view.findViewById(R.id.recyclerPedidos);
        layoutVazio     = view.findViewById(R.id.layoutVazio);
        progressBar     = view.findViewById(R.id.progressBar);

        recyclerPedidos.setLayoutManager(new LinearLayoutManager(getContext()));

        // Adapter com click listener — abre o bottom sheet
        adapter = new PedidoAdapter(listaPedidos, pedido -> {
            DetalhePedidoFragment sheet = DetalhePedidoFragment.newInstance(pedido);

            // Quando o status mudar dentro do sheet, atualiza o item na lista
            sheet.setOnStatusAtualizadoListener((pedidoId, novoStatus) -> {
                for (int i = 0; i < listaPedidos.size(); i++) {
                    if (listaPedidos.get(i).getId().equals(pedidoId)) {
                        listaPedidos.get(i).setStatus(novoStatus);
                        adapter.notifyItemChanged(i);
                        break;
                    }
                }
            });

            sheet.show(getChildFragmentManager(), "detalhe_pedido");
        });

        recyclerPedidos.setAdapter(adapter);
        carregarPedidos();
    }

    private void carregarPedidos() {
        setCarregando(true);

        pedidoRepository.obterPedidosPorComercio(new BaseCallback<List<Pedido>>() {
            @Override
            public void onSuccess(List<Pedido> pedidos) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    setCarregando(false);
                    adapter.atualizarLista(pedidos);
                    layoutVazio.setVisibility(pedidos.isEmpty() ? View.VISIBLE : View.GONE);
                    recyclerPedidos.setVisibility(pedidos.isEmpty() ? View.GONE : View.VISIBLE);
                });
            }

            @Override
            public void onError(String erro) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    setCarregando(false);
                    Toast.makeText(getContext(), "Erro ao carregar pedidos", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void setCarregando(boolean carregando) {
        progressBar.setVisibility(carregando ? View.VISIBLE : View.GONE);
        recyclerPedidos.setVisibility(carregando ? View.GONE : View.VISIBLE);
    }
}