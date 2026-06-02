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
import com.example.hortlink.data.model.OfertaDTO; // Ajuste o import para o DTO que você já tem

import java.util.List;

public class OfertaVitrineAdapter extends RecyclerView.Adapter<OfertaVitrineAdapter.ViewHolder> {

    private final List<OfertaDTO> listaOfertas;

    public OfertaVitrineAdapter(List<OfertaDTO> listaOfertas) {
        this.listaOfertas = listaOfertas;
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

        // Preenche os textos (Ajuste os getters conforme o seu OfertaDTO real)
        // Se o seu DTO ainda não tem esses campos exatos, pode criar temporários!
        holder.txtNome.setText(oferta.getNomeProduto() != null ? oferta.getNomeProduto() : "Produto sem nome");
        holder.txtPreco.setText(String.format("R$ %.2f / %s", oferta.getPreco(), oferta.getUnidade()));
        holder.txtEstoque.setText("Disponível: " + oferta.getId());

        // Carrega a foto fake da internet com Glide
        Glide.with(holder.itemView.getContext())
                .load(oferta.getImagemUrl())
                .placeholder(R.drawable.hortlink_logo) // Mostra a logo enquanto carrega a imagem da net
                .centerCrop()
                .into(holder.imgOferta);
    }

    @Override
    public int getItemCount() {
        return listaOfertas.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgOferta;
        TextView txtNome, txtPreco, txtEstoque;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgOferta  = itemView.findViewById(R.id.imgOferta);
            txtNome    = itemView.findViewById(R.id.txtNomeOferta);
            txtPreco   = itemView.findViewById(R.id.txtPrecoOferta);
            txtEstoque = itemView.findViewById(R.id.txtEstoqueOferta);
        }
    }
}
