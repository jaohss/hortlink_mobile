package com.example.hortlink.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.hortlink.R;

import java.util.List;

// Adapter responsável por conectar a lista de categorias ao RecyclerView
public class CategoriaAdapter extends RecyclerView.Adapter<CategoriaAdapter.ViewHolder> {

    // Lista de categorias (Strings) que serão exibidas
    List<String> lista;

    // Listener para capturar cliques nos itens
    OnCategoriaClick listener;

    // Guarda a posição atualmente selecionada (-1 = nenhuma selecionada)
    int posicaoSelecionada = -1;

    // Construtor que recebe a lista e o listener de clique
    public CategoriaAdapter(List<String> lista, OnCategoriaClick listener) {
        this.lista = lista;
        this.listener = listener;
    }

    // Construtor que recebe apenas a lista (sem listener)
    public CategoriaAdapter(List<String> lista) {
        this.lista = lista;
    }

    // ViewHolder representa cada item visual da lista
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nome;

        // Associa os componentes da view (layout) ao ViewHolder
        public ViewHolder(View itemView) {
            super(itemView);
            nome = itemView.findViewById(R.id.txtCategoria);
        }
    }

    // Cria a view de cada item (inflando o layout XML)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_categoria, parent, false);
        return new ViewHolder(view);
    }

    // Faz o bind dos dados (preenche cada item com informações da lista)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        // Define o texto da categoria
        holder.nome.setText(lista.get(position));

        // Define ação de clique no item
        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();

            // Verifica se a posição é válida
            if (pos != RecyclerView.NO_POSITION) {

                // Guarda a posição anterior selecionada
                int posAnterior = posicaoSelecionada;

                // Atualiza a posição selecionada
                posicaoSelecionada = pos;

                // Atualiza visualmente o item antigo e o novo
                notifyItemChanged(posAnterior);
                notifyItemChanged(posicaoSelecionada);

                // Dispara o evento de clique, se houver listener
                if (listener != null) {
                    listener.onClick(lista.get(pos));
                }
            }
        });

        // Altera o visual do item se estiver selecionado
        if (position == posicaoSelecionada) {
            holder.nome.setBackgroundResource(R.drawable.bg_categoria_selecionada);
            holder.nome.setTextColor(0xFFFFFFFF);
        } else {
            holder.nome.setBackgroundResource(R.drawable.bg_categoria_normal);
            holder.nome.setTextColor(0xFF000000);
        }

    }

    // Retorna a quantidade de itens da lista
    @Override
    public int getItemCount() {
        return lista.size();
    }

    // Interface para capturar cliques nas categorias
    public interface OnCategoriaClick {
        void onClick(String categoria);
    }
}