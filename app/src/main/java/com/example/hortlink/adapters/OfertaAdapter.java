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

public class OfertaAdapter extends RecyclerView.Adapter<OfertaAdapter.ViewHolder> {

    // 1. Interface de clique preservada (Essencial para abrir os Detalhes)
    public interface OnOfertaClickListener {
        void onOfertaClick(OfertaDTO oferta);
    }

    private final List<OfertaDTO> listaOfertas;
    private final OnOfertaClickListener listener;

    public OfertaAdapter(List<OfertaDTO> listaOfertas, OnOfertaClickListener listener) {
        this.listaOfertas = listaOfertas;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_oferta_vitrine, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OfertaDTO oferta = listaOfertas.get(position);

        // Preenche Nome
        holder.txtNome.setText(oferta.getNomeProduto() != null ? oferta.getNomeProduto() : "Produto");

        // Preenche Preço
        // Nota: Assumindo que getUnidade() retorna uma String ou um objeto que tem getSimbolo()
        // Ajuste conforme a sua classe real se necessário.
        String unidade = oferta.getUnidade() != null ? oferta.getUnidade().toString() : "un";
        holder.txtPreco.setText(String.format("R$ %.2f / %s", oferta.getPreco(), unidade));

        // 2. Exibição focada no Estoque (Ideal para a vitrine do Produtor)
        if (oferta.getQuantidadeEstoque() != null) {
            holder.txtDetalheSecundario.setText("Disponível: " + oferta.getQuantidadeEstoque() + " " + unidade);
            holder.txtDetalheSecundario.setVisibility(View.VISIBLE);
        } else {
            holder.txtDetalheSecundario.setVisibility(View.GONE);
        }

        // Carrega a imagem com Glide
        Glide.with(holder.itemView.getContext())
                .load(oferta.getImagemUrl())
                .placeholder(R.drawable.hortlink_logo)
                .error(R.drawable.hortlink_logo) // Tratamento de erro adicionado
                .centerCrop()
                .into(holder.imgOferta);

        // 3. O gatilho do clique
        holder.itemView.setOnClickListener(v -> listener.onOfertaClick(oferta));
    }

    @Override
    public int getItemCount() {
        return listaOfertas.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgOferta;
        TextView txtNome, txtPreco, txtDetalheSecundario;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgOferta            = itemView.findViewById(R.id.imgOferta);
            txtNome              = itemView.findViewById(R.id.txtNomeOferta);
            txtPreco             = itemView.findViewById(R.id.txtPrecoOferta);

            // Renomeei a variável do ViewHolder para ser genérica,
            // já que ela aponta para o TextView que ora é distância, ora é estoque
            txtDetalheSecundario = itemView.findViewById(R.id.txtEstoqueOferta);
        }
    }
}