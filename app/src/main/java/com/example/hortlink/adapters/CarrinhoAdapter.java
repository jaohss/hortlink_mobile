package com.example.hortlink.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.hortlink.R;
import com.example.hortlink.entidades.CartItem;

import java.util.List;

public class CarrinhoAdapter extends RecyclerView.Adapter<CarrinhoAdapter.ViewHolder> {

    public interface OnRemoverListener{
        void onRemover(CartItem item);
    }
    public interface OnQuantidadeListener{
        void onAlterarQtd(CartItem item, int novaQtd);
    }

    private final List<CartItem> items;
    private final OnRemoverListener    onRemover;
    private final OnQuantidadeListener onQuantidade;

    public CarrinhoAdapter(List<CartItem> items, OnRemoverListener onRemover, OnQuantidadeListener onQuantidade) {
        this.items        = items;
        this.onRemover    = onRemover;
        this.onQuantidade = onQuantidade;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        CartItem item = items.get(position);

        // ── Textos ───────────────────────────────────────────────────
        h.tvNome.setText(item.getNomeProduto());

        h.tvPrecoUnitario.setText(String.format("R$ %.2f / %s", item.getPreco(), item.getUnidade()));

        h.tvQuantidade.setText(String.valueOf(item.getQuantidade()));

        h.tvSubtotal.setText(String.format("R$ %.2f", item.getSubtotal()));

        // ── Imagem com Glide ─────────────────────────────────────────
        Glide.with(h.ivProduto.getContext())
                .load(item.getFotoUrl())
                .apply(new RequestOptions()
                        .placeholder(R.drawable.cart)   // ← troque por um drawable de placeholder se tiver
                        .error(R.drawable.cart)
                        .transform(new RoundedCorners(16)))
                .into(h.ivProduto);

        // ── Botões de quantidade ──────────────────────────────────────
        h.btnAumentar.setOnClickListener(v ->
                onQuantidade.onAlterarQtd(item, item.getQuantidade() + 1));

        h.btnDiminuir.setOnClickListener(v ->
                onQuantidade.onAlterarQtd(item, item.getQuantidade() - 1));

        // ── Botão remover ────────────────────────────────────────────
        h.btnRemover.setOnClickListener(v -> onRemover.onRemover(item));
    }

    @Override
    public int getItemCount() { return items.size(); }

    // ── ViewHolder ───────────────────────────────────────────────────
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProduto;
        TextView tvNome, tvPrecoUnitario, tvQuantidade, tvSubtotal;
        ImageButton btnAumentar, btnDiminuir, btnRemover;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProduto        = itemView.findViewById(R.id.iv_produto);
            tvNome           = itemView.findViewById(R.id.tv_nome);
            tvPrecoUnitario  = itemView.findViewById(R.id.tv_preco_unitario);
            tvQuantidade     = itemView.findViewById(R.id.tv_quantidade);
            tvSubtotal       = itemView.findViewById(R.id.tv_subtotal);
            btnAumentar      = itemView.findViewById(R.id.btn_aumentar);
            btnDiminuir      = itemView.findViewById(R.id.btn_diminuir);
            btnRemover       = itemView.findViewById(R.id.btn_remover);
        }
    }
}
