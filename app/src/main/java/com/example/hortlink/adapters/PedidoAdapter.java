package com.example.hortlink.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hortlink.R;
import com.example.hortlink.data.model.Pedido;

import java.util.List;

public class PedidoAdapter extends RecyclerView.Adapter<PedidoAdapter.ViewHolder> {

    public interface OnStatusChangeListener {
        void onAceitar(Pedido pedido);
        void onRecusar(Pedido pedido);
    }

    private final List<Pedido> lista;
    private final OnStatusChangeListener listener;

    // listener pode ser null — para o lado do comprador que não tem botões
    public PedidoAdapter(List<Pedido> lista, OnStatusChangeListener listener) {
        this.lista    = lista;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pedido, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Pedido pedido = lista.get(position);

        // Resumo dos itens — ex: "2x Tomate • 1x Alface"
        StringBuilder resumo = new StringBuilder();
        for (Pedido.ItemPedido item : pedido.getItens()) {
            if (resumo.length() > 0) resumo.append(" • ");
            resumo.append(item.quantidade).append("x ").append(item.nomeProduto);
        }

        holder.txtItens.setText(resumo.length() > 0 ? resumo : "Sem itens");
        holder.txtValor.setText(String.format("R$ %.2f", pedido.getValorTotal()));
        holder.txtStatus.setText(pedido.getStatus());

        aplicarCorStatus(holder.txtStatus, pedido.getStatus());

        // Botões só visíveis para o produtor (quando listener não é null)
        boolean mostrarBotoes = listener != null
                && "pago".equals(pedido.getStatus()); // só mostra se ainda não processado

        holder.btnAceitar.setVisibility(mostrarBotoes ? View.VISIBLE : View.GONE);
        holder.btnRecusar.setVisibility(mostrarBotoes ? View.VISIBLE : View.GONE);

        holder.btnAceitar.setOnClickListener(v -> {
            if (listener != null) listener.onAceitar(pedido);
        });

        holder.btnRecusar.setOnClickListener(v -> {
            if (listener != null) listener.onRecusar(pedido);
        });
    }

    private void aplicarCorStatus(TextView view, String status) {
        if (status == null) return;
        switch (status) {
            case "pago":      view.setBackgroundColor(0xFFFFA000); break; // laranja
            case "enviado":   view.setBackgroundColor(0xFF1565C0); break; // azul
            case "entregue":  view.setBackgroundColor(0xFF2E7D32); break; // verde
            case "cancelado": view.setBackgroundColor(0xFFC62828); break; // vermelho
            default:          view.setBackgroundColor(0xFF616161); break; // cinza
        }
    }

    public void atualizarLista(List<Pedido> novos) {
        lista.clear();
        lista.addAll(novos);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() { return lista.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtItens, txtValor, txtStatus;
        Button btnAceitar, btnRecusar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtItens   = itemView.findViewById(R.id.txtItens);
            txtValor   = itemView.findViewById(R.id.txtValor);
            txtStatus  = itemView.findViewById(R.id.txtStatus);
            btnAceitar = itemView.findViewById(R.id.btnAceitar);
            btnRecusar = itemView.findViewById(R.id.btnRecusar);
        }
    }
}