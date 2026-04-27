package com.example.hortlink.adapters;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hortlink.R;
import com.example.hortlink.entidades.Produto;

import java.util.List;

public class GerenciarAdapter extends RecyclerView.Adapter<GerenciarAdapter.ViewHolder> {

    private final List<Produto> lista;
    private final OnEditarClick onEditar;
    private final OnDeletarClick onDeletar;

    public interface OnEditarClick {
        void onClick(Produto produto);
    }

    public interface OnDeletarClick {
        void onClick(Produto produto, int position);
    }

    public GerenciarAdapter(List<Produto> lista, OnEditarClick onEditar, OnDeletarClick onDeletar) {
        this.lista = lista;
        this.onEditar = onEditar;
        this.onDeletar = onDeletar;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_gerenciar_produto, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Produto p = lista.get(position);

        holder.txtNome.setText(p.nome);
        holder.txtTipo.setText(p.categoria);
        holder.txtPreco.setText(String.format("R$ %.2f", p.preco));

        if (p.imagemUri != null && !p.imagemUri.isEmpty()) {
            holder.imgProduto.setImageURI(Uri.parse(p.imagemUri));
        } else {
            holder.imgProduto.setImageResource(R.drawable.hortlink_logo);
        }

        holder.btnEditar.setOnClickListener(v -> onEditar.onClick(p));
        holder.btnDeletar.setOnClickListener(v -> onDeletar.onClick(p, holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduto;
        TextView txtNome, txtTipo, txtPreco;
        Button btnEditar, btnDeletar;

        public ViewHolder(View itemView) {
            super(itemView);
            imgProduto = itemView.findViewById(R.id.imgProduto);
            txtNome    = itemView.findViewById(R.id.txtNomeProduto);
            txtTipo    = itemView.findViewById(R.id.txtTipoProduto);
            txtPreco   = itemView.findViewById(R.id.txtPrecoProduto);
            btnEditar  = itemView.findViewById(R.id.btnEditar);
            btnDeletar = itemView.findViewById(R.id.btnDeletar);
        }
    }
}