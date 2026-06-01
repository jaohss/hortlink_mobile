package com.example.hortlink.adapters;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hortlink.R;
import com.example.hortlink.data.dto.ProdutoListaDTO;
import com.example.hortlink.data.model.Produto;

import java.util.List;

public class ProdutoAdapter extends RecyclerView.Adapter<ProdutoAdapter.ViewHolder> {

    // Interface para capturar os cliques nos botões específicos
    public interface OnProdutoActionListener {
        void onEditarClick(ProdutoListaDTO produto);
        void onStatusClick(ProdutoListaDTO produto, int position);
    }

    private final List<ProdutoListaDTO> lista;
    private final OnProdutoActionListener listener;

    public ProdutoAdapter(List<ProdutoListaDTO> lista, OnProdutoActionListener listener) {
        this.lista = lista;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_gerenciar_produto, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Produto produto = lista.get(position);

        holder.txtNome.setText(produto.getNome());

        String tipo = produto.getCategoria() != null ? produto.getCategoria().toString() : "Sem categoria";
        holder.txtTipo.setText(tipo);

        // Substituindo o preço pela Unidade de Medida
        String unidade = produto.getUnidadeMedida() != null ? produto.getUnidadeMedida().toString() : "";
        holder.txtPreco.setText("Vendido por: " + unidade);

        // Carrega a imagem com Glide
        Glide.with(holder.itemView.getContext())
                .load(produto.getImagemUrl())
                .placeholder(R.drawable.hortlink_logo) // Fica na tela enquanto a imagem baixa
                .centerCrop()
                .into(holder.imgProduto);

        // Lógica visual do botão Status
        boolean isAtivo = produto.isAtivo();
        holder.btnStatus.setText(isAtivo ? "Ativo" : "Inativo");

        // Altera a cor de fundo dinamicamente usando ColorStateList (Padrão Material)
        int corStatus = isAtivo ? 0xFF2E7D32 : 0xFF9E9E9E; // Verde se ativo, Cinza se inativo
        holder.btnStatus.setBackgroundTintList(ColorStateList.valueOf(corStatus));

        // Listeners dos botões
        holder.btnEditar.setOnClickListener(v -> listener.onEditarClick(produto));
        holder.btnStatus.setOnClickListener(v -> listener.onStatusClick(produto, position));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduto;
        TextView txtNome, txtTipo, txtPreco;
        Button btnEditar, btnStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduto = itemView.findViewById(R.id.imgProduto);
            txtNome    = itemView.findViewById(R.id.txtNomeProduto);
            txtTipo    = itemView.findViewById(R.id.txtTipoProduto);
            txtPreco   = itemView.findViewById(R.id.txtPrecoProduto); // Usado para unidade
            btnEditar  = itemView.findViewById(R.id.btnEditar);
            btnStatus  = itemView.findViewById(R.id.btnStatus);
        }
    }
}