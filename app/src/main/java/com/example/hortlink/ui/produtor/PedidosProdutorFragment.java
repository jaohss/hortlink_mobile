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
import com.example.hortlink.util.SessionManager;

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

        adapter = new PedidoAdapter(listaPedidos, new PedidoAdapter.OnStatusChangeListener() {
            @Override
            public void onAceitar(Pedido pedido) {
                atualizarStatus(pedido, "enviado");
            }

            @Override
            public void onRecusar(Pedido pedido) {
                atualizarStatus(pedido, "cancelado");
            }
        });

        recyclerPedidos.setAdapter(adapter);
        carregarPedidos();
    }

    // ─── Carregar ─────────────────────────────────────────────────────

    private void carregarPedidos() {
        String uid = SessionManager.getInstance().getUid();
        if (uid == null) return;

        setCarregando(true);

        pedidoRepository.listarPorProdutor(uid, new PedidoRepository.CallbackLista() {
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
                    Toast.makeText(getContext(),
                            "Erro ao carregar pedidos", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    // ─── Atualizar status ─────────────────────────────────────────────

    private void atualizarStatus(Pedido pedido, String novoStatus) {
        pedidoRepository.atualizarStatus(pedido.getId(), novoStatus,
                new PedidoRepository.Callback() {
                    @Override
                    public void onSuccess(String resultado) {
                        if (!isAdded()) return;
                        requireActivity().runOnUiThread(() -> {
                            pedido.setStatus(novoStatus);
                            // Atualiza só o item alterado, sem recarregar tudo
                            int pos = listaPedidos.indexOf(pedido);
                            if (pos >= 0) adapter.notifyItemChanged(pos);

                            String msg = "enviado".equals(novoStatus)
                                    ? "Pedido marcado como enviado ✓"
                                    : "Pedido cancelado";
                            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onError(String erro) {
                        if (!isAdded()) return;
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(),
                                        "Erro ao atualizar status", Toast.LENGTH_SHORT).show());
                    }
                });
    }

    // ─── Helper ───────────────────────────────────────────────────────

    private void setCarregando(boolean carregando) {
        progressBar.setVisibility(carregando ? View.VISIBLE : View.GONE);
        recyclerPedidos.setVisibility(carregando ? View.GONE : View.VISIBLE);
    }
}