package com.example.hortlink.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hortlink.R;
import com.example.hortlink.entidades.Pedido;

import java.util.List;

public class PedidoAdapter extends RecyclerView.Adapter<PedidoAdapter.ViewHolder> {

    private List<Pedido> lista;

    public PedidoAdapter(List<Pedido> lista) {
        this.lista = lista;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pedido, parent, false); // ✅ layout, não drawable
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Pedido pedido = lista.get(position);

        holder.txtCliente.setText("Cliente: " + pedido.getCliente());
        holder.txtItens.setText(pedido.getItens());
        holder.txtValor.setText(pedido.getValor());
        holder.txtStatus.setText(pedido.getStatus());

        switch (pedido.getStatus()) {
            case "PENDENTE":
                holder.txtStatus.setBackgroundColor(0xFFFFA000);
                break;
            case "ACEITO":
                holder.txtStatus.setBackgroundColor(0xFF2E7D32);
                break;
            case "RECUSADO":
                holder.txtStatus.setBackgroundColor(0xFFC62828);
                break;
            case "FINALIZADO":
                holder.txtStatus.setBackgroundColor(0xFF616161);
                break;
        }

        holder.btnAceitar.setOnClickListener(v -> {
            pedido.setStatus("ACEITO");
            notifyItemChanged(holder.getAdapterPosition()); // ✅ atualiza o item
        });

        holder.btnRecusar.setOnClickListener(v -> {
            pedido.setStatus("RECUSADO");
            notifyItemChanged(holder.getAdapterPosition()); // ✅ atualiza o item
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtCliente, txtItens, txtValor, txtStatus;
        Button btnAceitar, btnRecusar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtCliente = itemView.findViewById(R.id.txtCliente);
            txtItens = itemView.findViewById(R.id.txtItens);
            txtValor = itemView.findViewById(R.id.txtValor);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            btnAceitar = itemView.findViewById(R.id.btnAceitar);
            btnRecusar = itemView.findViewById(R.id.btnRecusar);
        }
    }
}