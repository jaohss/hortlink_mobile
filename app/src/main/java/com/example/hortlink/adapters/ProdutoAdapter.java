package com.example.hortlink.adapters;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hortlink.R;
import com.example.hortlink.data.dto.ProdutoListaDTO;

import java.util.List;

public class ProdutoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TIPO_BOTAO_ADICIONAR = 0;
    private static final int TIPO_PRODUTO_NORMAL = 1;

    public interface OnProdutoActionListener {
        void onAddProdutoClick(); // Novo evento de clique!
        void onEditarClick(ProdutoListaDTO produto);
        void onStatusClick(ProdutoListaDTO produto, int position);
    }

    private final List<ProdutoListaDTO> lista;
    private final OnProdutoActionListener listener;

    public ProdutoAdapter(List<ProdutoListaDTO> lista, OnProdutoActionListener listener) {
        this.lista = lista;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return TIPO_BOTAO_ADICIONAR;
        else return TIPO_PRODUTO_NORMAL;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TIPO_BOTAO_ADICIONAR) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_adicionar_produto_list, parent, false);
            return new AddViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_gerenciar_produto, parent, false);
            return new ProdutoViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == TIPO_BOTAO_ADICIONAR) {
            AddViewHolder addHolder = (AddViewHolder) holder;
            addHolder.btnCardInteiro.setOnClickListener(v -> {
                if (listener != null) listener.onAddProdutoClick();
            });
        } else {
            ProdutoViewHolder prodHolder = (ProdutoViewHolder) holder;
            ProdutoListaDTO produto = lista.get(position - 1); // Desconta a posição 0

            prodHolder.txtNome.setText(produto.getNome());

            String tipo = produto.getCategoria() != null ? produto.getCategoria().toString() : "Sem categoria";
            prodHolder.txtTipo.setText(tipo);

            String unidade = produto.getUnidadeMedida() != null ? produto.getUnidadeMedida().toString() : "";
            prodHolder.txtPreco.setText("Vendido por: " + unidade);

            Glide.with(prodHolder.itemView.getContext())
                    .load(produto.getImagemUrl())
                    .placeholder(R.drawable.hortlink_logo)
                    .centerCrop()
                    .into(prodHolder.imgProduto);

            boolean isAtivo = produto.isAtivo();
            prodHolder.btnStatus.setText(isAtivo ? "Ativo" : "Inativo");

            int corStatus = isAtivo ? 0xFF2E7D32 : 0xFF9E9E9E;
            prodHolder.btnStatus.setBackgroundTintList(ColorStateList.valueOf(corStatus));

            prodHolder.btnEditar.setOnClickListener(v -> listener.onEditarClick(produto));
            prodHolder.btnStatus.setOnClickListener(v -> listener.onStatusClick(produto, position));
        }
    }

    @Override
    public int getItemCount() {
        return lista.size() + 1; // +1 para o botão de adicionar
    }

    // Holders Separados
    public static class AddViewHolder extends RecyclerView.ViewHolder {
        LinearLayout btnCardInteiro;
        public AddViewHolder(@NonNull View itemView) {
            super(itemView);
            btnCardInteiro = itemView.findViewById(R.id.btnCardInteiro);
        }
    }

    public static class ProdutoViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduto;
        TextView txtNome, txtTipo, txtPreco;
        Button btnEditar, btnStatus;

        public ProdutoViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduto = itemView.findViewById(R.id.imgProduto);
            txtNome    = itemView.findViewById(R.id.txtNomeProduto);
            txtTipo    = itemView.findViewById(R.id.txtTipoProduto);
            txtPreco   = itemView.findViewById(R.id.txtPrecoProduto);
            btnEditar  = itemView.findViewById(R.id.btnEditar);
            btnStatus  = itemView.findViewById(R.id.btnStatus);
        }
    }
}