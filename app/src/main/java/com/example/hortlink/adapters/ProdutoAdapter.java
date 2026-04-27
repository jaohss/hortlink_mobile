package com.example.hortlink.adapters;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.hortlink.R;
import com.example.hortlink.entidades.Produto;

import java.util.List;

public class ProdutoAdapter extends RecyclerView.Adapter<ProdutoAdapter.ViewHolder> {
    List<Produto> lista;
    OnProdutoClick produtoList;
    public ProdutoAdapter(List<Produto> lista, OnProdutoClick produtoList){
        this.lista=lista;
        this.produtoList = produtoList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView nome, preco, descricao;
        ImageView imagem;
        public ViewHolder(View itemView){
            super(itemView);
            nome = itemView.findViewById(R.id.txtNome);
            preco = itemView.findViewById(R.id.txtPreco);
            imagem = itemView.findViewById(R.id.imgProduto);
            //descricao = itemView.findViewById(R.id.txtDescricao);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_produto,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Produto p = lista.get(position);
        holder.nome.setText(p.nome);
        holder.preco.setText("R$ " + p.preco);

        // ✅ Se veio do banco usa URI, senão usa drawable
        if (p.imagemUri != null && !p.imagemUri.isEmpty()) {
            try {
                holder.imagem.setImageURI(Uri.parse(p.imagemUri));
            } catch (SecurityException e) {
                // URI expirou, mostra a logo padrão
                holder.imagem.setImageResource(R.drawable.hortlink_logo);
            }
        } else {
            holder.imagem.setImageResource(R.drawable.hortlink_logo);
        }

        holder.itemView.setOnClickListener(v -> produtoList.onClick(p));
    }
    @Override
    public int getItemCount(){
        return lista.size();
    }

    public interface OnProdutoClick {
        void onClick(Produto p);
    }
}
