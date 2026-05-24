package com.example.hortlink.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hortlink.R;
import com.example.hortlink.data.model.Produto;

import java.util.List;

public class ProdutoAdapter extends RecyclerView.Adapter<ProdutoAdapter.ViewHolder> {

    List<Produto> lista;
    OnProdutoClick produtoList;

    public ProdutoAdapter(List<Produto> lista, OnProdutoClick produtoList) {
        this.lista       = lista;
        this.produtoList = produtoList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView  nome, preco, distancia;
        ImageView imagem;

        public ViewHolder(View itemView) {
            super(itemView);
            nome   = itemView.findViewById(R.id.txtNome);
            preco  = itemView.findViewById(R.id.txtPreco);
            imagem = itemView.findViewById(R.id.imgProduto);
            distancia = itemView.findViewById(R.id.txtDistancia);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_produto, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Produto p = lista.get(position);

        holder.nome.setText(p.nome);
        holder.preco.setText(String.format("R$ %.2f", p.preco));

        // Exibe distância se foi calculada, esconde se não tem localização
        if (p.distanciaKm < Double.MAX_VALUE) {
            holder.distancia.setVisibility(View.VISIBLE);
            if (p.distanciaKm < 1.0) {
                holder.distancia.setText("📍 menos de 1 km");
            } else {
                holder.distancia.setText(String.format("📍 %.1f km", p.distanciaKm));
            }
        } else {
            holder.distancia.setVisibility(View.GONE); // sem localização → esconde
        }

        // URL do Supabase Storage → Glide carrega
        // URI local (legado) → Glide também suporta
        if (p.imagemUri != null && !p.imagemUri.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(p.imagemUri)                          // funciona com URL http e URI local
                    .placeholder(R.drawable.hortlink_logo)      // enquanto carrega
                    .error(R.drawable.hortlink_logo)            // se falhar
                    .centerCrop()
                    .into(holder.imagem);
        } else {
            holder.imagem.setImageResource(R.drawable.hortlink_logo);
        }

        holder.itemView.setOnClickListener(v -> produtoList.onClick(p));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public interface OnProdutoClick {
        void onClick(Produto p);
    }
}