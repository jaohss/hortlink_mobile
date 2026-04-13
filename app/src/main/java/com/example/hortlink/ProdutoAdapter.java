package com.example.hortlink;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

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
            descricao = itemView.findViewById(R.id.txtDescricao);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_produto,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position){
        Produto p = lista.get(position);
        holder.nome.setText(p.nome);
        holder.preco.setText("R$ "+ p.preco);
        holder.imagem.setImageResource(p.imagem);
        holder.descricao.setText("Descrição: "+ p.descricao);

        holder.imagem.setImageResource(p.imagem);
        holder.itemView.setOnClickListener(v ->{
            //Toast.makeText(v.getContext(), "Item selecionado:"+p.nome, Toast.LENGTH_SHORT).show();

            produtoList.onClick(p);

        });
    }
    @Override
    public int getItemCount(){
        return lista.size();
    }

    public interface OnProdutoClick {
        void onClick(Produto p);
    }
}
