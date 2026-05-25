package com.example.hortlink.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hortlink.R;
import com.example.hortlink.data.model.Pedido;

import java.util.List;

public class PedidoAdapter extends RecyclerView.Adapter<PedidoAdapter.ViewHolder> {

    public interface OnPedidoClickListener {
        void onClick(Pedido pedido);
    }



    private final List<Pedido> lista;
    private OnPedidoClickListener clickListener;

    // Substitui os dois construtores anteriores por este único:
    public PedidoAdapter(List<Pedido> lista, OnPedidoClickListener clickListener) {
        this.lista = lista;
        this.clickListener = clickListener;
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

        // Resumo dos itens
        StringBuilder resumo = new StringBuilder();
        for (Pedido.ItemPedido item : pedido.getItens()) {
            if (resumo.length() > 0) resumo.append(" • ");
            resumo.append(item.quantidade).append("x ").append(item.nomeProduto);
        }
        holder.txtItens.setText(resumo.length() > 0 ? resumo : "Carregando itens…");
        holder.txtValor.setText(String.format("R$ %.2f", pedido.getValorTotal()));
        holder.txtStatus.setText(pedido.getStatus() != null
                ? pedido.getStatus().toUpperCase() : "");

        aplicarCorStatus(holder.txtStatus, pedido.getStatus());
        holder.txtData.setText(formatarData(pedido.getCriadoEm()));

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onClick(pedido);
        });
    }

    private void aplicarCorStatus(TextView view, String status) {
        if (status == null) return;
        int cor;
        switch (status) {
            case "pendente":      cor = 0xFFFFA000; break; // laranja
            case "aceito":   cor = 0xFF1565C0; break; // azul
            case "entregue":  cor = 0xFF2E7D32; break; // verde
            case "cancelado": cor = 0xFFC62828; break; // vermelho
            default:          cor = 0xFF616161; break; // cinza
        }
        view.getBackground().setTint(cor);
    }

    // Converte "2026-05-25T14:30:00" → "25/05/2026"
    private String formatarData(String iso) {
        if (iso == null || iso.length() < 10) return "";
        try {
            String[] partes = iso.substring(0, 10).split("-");
            return partes[2] + "/" + partes[1] + "/" + partes[0];
        } catch (Exception e) {
            return "";
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
        TextView txtItens, txtValor, txtStatus, txtData;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtItens  = itemView.findViewById(R.id.txtItens);
            txtValor  = itemView.findViewById(R.id.txtValor);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            txtData   = itemView.findViewById(R.id.txtData);
        }
    }
}