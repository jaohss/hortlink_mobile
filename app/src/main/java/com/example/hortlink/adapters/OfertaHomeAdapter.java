package com.example.hortlink.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hortlink.R;
import com.example.hortlink.data.model.OfertaDTO;

import java.util.List;

public class OfertaHomeAdapter extends RecyclerView.Adapter<OfertaHomeAdapter.ViewHolder> {

    // Interface para avisar o Fragment que o cliente clicou em uma oferta
    public interface OnOfertaClickListener {
        void onOfertaClick(OfertaDTO oferta);
    }

    private final List<OfertaDTO> listaOfertas;
    private final OnOfertaClickListener listener;

    public OfertaHomeAdapter(List<OfertaDTO> listaOfertas, OnOfertaClickListener listener) {
        this.listaOfertas = listaOfertas;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Reaproveitamos o design lindo do "caixote de feira" que já fizemos!
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_oferta_vitrine, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OfertaDTO oferta = listaOfertas.get(position);

        // Preenche Nome e Preço
        holder.txtNome.setText(oferta.getNomeProduto() != null ? oferta.getNomeProduto() : "Produto");

        // Formata o preço com a unidade (ex: R$ 5,50 / Kg)
        String unidade = oferta.getUnidade() != null ? oferta.getUnidade().getSimbolo() : "un";
        holder.txtPreco.setText(String.format("R$ %.2f / %s", oferta.getPreco(), unidade));

        // Aqui está a mágica: Trocamos o texto de "Estoque" pela "Distância"
        if (oferta.getDistanciaKm() != null && oferta.getDistanciaKm() != Double.MAX_VALUE) {
            // Formata para 1 casa decimal (ex: "A 2,5 km")
            holder.txtDistancia.setText(String.format("A %.1f km", oferta.getDistanciaKm()));
            holder.txtDistancia.setVisibility(View.VISIBLE);
        } else {
            // Se não tiver GPS, apenas esconde a informação de distância
            holder.txtDistancia.setVisibility(View.GONE);
        }

        // Carrega a imagem
        Glide.with(holder.itemView.getContext())
                .load(oferta.getImagemUrl())
                .placeholder(R.drawable.hortlink_logo)
                .centerCrop()
                .into(holder.imgOferta);

        // Dispara o clique no cartão inteiro para abrir a tela de Detalhes
        holder.itemView.setOnClickListener(v -> listener.onOfertaClick(oferta));
    }

    @Override
    public int getItemCount() {
        return listaOfertas.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgOferta;
        TextView txtNome, txtPreco, txtDistancia;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgOferta    = itemView.findViewById(R.id.imgOferta);
            txtNome      = itemView.findViewById(R.id.txtNomeOferta);
            txtPreco     = itemView.findViewById(R.id.txtPrecoOferta);

            // Reaproveitando o TextView de estoque para mostrar a distância
            txtDistancia = itemView.findViewById(R.id.txtEstoqueOferta);
        }
    }
}
