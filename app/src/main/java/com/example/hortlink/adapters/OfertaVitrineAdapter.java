package com.example.hortlink.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hortlink.R;
import com.example.hortlink.data.model.OfertaDTO;

import java.util.List;

// ATENÇÃO: Mudamos a herança genérica para RecyclerView.ViewHolder
// porque agora temos dois tipos diferentes de holders!
public class OfertaVitrineAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // 1. Definimos os "Tipos" de visualização
    private static final int TIPO_BOTAO_ADICIONAR = 0;
    private static final int TIPO_OFERTA_NORMAL = 1;

    private final List<OfertaDTO> listaOfertas;
    private final OnOfertaActionListener listener;

    // Interface para enviar o clique lá para o Fragment
    public interface OnOfertaActionListener {
        void onAddOfertaClick();
        // void onOfertaClick(OfertaDTO oferta); // Você pode usar depois para abrir/editar a oferta!
    }

    public OfertaVitrineAdapter(List<OfertaDTO> listaOfertas, OnOfertaActionListener listener) {
        this.listaOfertas = listaOfertas;
        this.listener = listener;
    }

    // 2. A Mágica: Qual é o tipo de layout dessa posição?
    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TIPO_BOTAO_ADICIONAR; // O primeiro quadrado é sempre o botão
        } else {
            return TIPO_OFERTA_NORMAL;   // O resto é produto
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Infla o layout correto dependendo do tipo que o getItemViewType retornou
        if (viewType == TIPO_BOTAO_ADICIONAR) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_adicionar_oferta, parent, false);
            return new AddViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_oferta_vitrine, parent, false);
            return new OfertaViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        // Se for o botão de adicionar, só configuramos o clique
        if (holder.getItemViewType() == TIPO_BOTAO_ADICIONAR) {
            AddViewHolder addHolder = (AddViewHolder) holder;
            addHolder.btnCardInteiro.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAddOfertaClick(); // Grita pro Fragment: "Clicaram no botão!"
                }
            });
        }
        // Se for um produto normal, carregamos os dados
        else {
            OfertaViewHolder ofertaHolder = (OfertaViewHolder) holder;

            // ATENÇÃO MATEMÁTICA: Como a posição 0 é o botão, o primeiro item da
            // lista real está na posição 1 da tela. Então puxamos position - 1.
            OfertaDTO oferta = listaOfertas.get(position - 1);

            ofertaHolder.txtNome.setText(oferta.getNomeProduto() != null ? oferta.getNomeProduto() : "Produto sem nome");
            ofertaHolder.txtPreco.setText(String.format("R$ %.2f / %s", oferta.getPreco(), oferta.getUnidade()));
            ofertaHolder.txtEstoque.setText("Disponível: " + oferta.getId());

            Glide.with(ofertaHolder.itemView.getContext())
                    .load(oferta.getImagemUrl())
                    .placeholder(R.drawable.hortlink_logo)
                    .centerCrop()
                    .into(ofertaHolder.imgOferta);
        }
    }

    // 3. O tamanho da lista é a quantidade de ofertas MAIS UM (que é o botão vazio)
    @Override
    public int getItemCount() {
        return listaOfertas.size() + 1;
    }

    // --- CLASSES VIEWHOLDER ---

    // Novo Holder apenas para o Card do Botão
    public static class AddViewHolder extends RecyclerView.ViewHolder {
        LinearLayout btnCardInteiro;

        public AddViewHolder(@NonNull View itemView) {
            super(itemView);
            btnCardInteiro = itemView.findViewById(R.id.btnCardInteiro);
        }
    }

    // Seu Holder antigo, agora renomeado para ficar claro
    public static class OfertaViewHolder extends RecyclerView.ViewHolder {
        ImageView imgOferta;
        TextView txtNome, txtPreco, txtEstoque;

        public OfertaViewHolder(@NonNull View itemView) {
            super(itemView);
            imgOferta  = itemView.findViewById(R.id.imgOferta);
            txtNome    = itemView.findViewById(R.id.txtNomeOferta);
            txtPreco   = itemView.findViewById(R.id.txtPrecoOferta);
            txtEstoque = itemView.findViewById(R.id.txtEstoqueOferta);
        }
    }
}