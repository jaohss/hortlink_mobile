package com.example.hortlink.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hortlink.R;
import com.example.hortlink.data.model.Produto;
import com.example.hortlink.data.repository.ProdutoRepository;

import java.util.List;

public class GerenciarAdapter extends RecyclerView.Adapter<GerenciarAdapter.ViewHolder> {


    private final List<Produto> lista;
    private final OnEditarClick onEditar;
    private final ProdutoRepository produtoRepository = new ProdutoRepository();

    // ── OnDeletarClick REMOVIDO ──────────────────────────────────────
    // Produtos nunca são deletados fisicamente (FK em pedido_itens).
    // O controle de visibilidade é feito pelo campo "ativo" via soft delete.

    public interface OnEditarClick {
        void onClick(Produto produto);
    }

    // Construtor sem onDeletar
    public GerenciarAdapter(List<Produto> lista, OnEditarClick onEditar) {
        this.lista    = lista;
        this.onEditar = onEditar;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_gerenciar_produto, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Produto p = lista.get(position);

        holder.txtNome.setText(p.nome);
        holder.txtTipo.setText(p.categoria);
        holder.txtPreco.setText(String.format("R$ %.2f", p.preco));

        if (p.imagemUri != null && !p.imagemUri.isEmpty()) {
            holder.imgProduto.setImageURI(Uri.parse(p.imagemUri));
        } else {
            holder.imgProduto.setImageResource(R.drawable.hortlink_logo);
        }

        // Botão editar — sem mudança de comportamento
        holder.btnEditar.setOnClickListener(v -> onEditar.onClick(p));

        // Botão de status — reflete o estado atual e permite alternar
        aplicarEstadoBotao(holder.btnStatus, p.status);

        holder.btnStatus.setOnClickListener(v -> {
            boolean novoStatus = !p.status;

            produtoRepository.atualizarStatus(p.id, novoStatus, new ProdutoRepository.Callback() {
                @Override
                public void onSuccess(String r) {
                    // Atualiza o model em memória
                    p.status = novoStatus;

                    // Atualiza a UI na thread principal
                    holder.itemView.post(() ->
                            aplicarEstadoBotao(holder.btnStatus, novoStatus));
                }

                @Override
                public void onError(String erro) {
                    holder.itemView.post(() ->
                            Toast.makeText(
                                    holder.itemView.getContext(),
                                    "Erro ao atualizar status",
                                    Toast.LENGTH_SHORT
                            ).show());
                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    // ── Aplica visual do botão de acordo com o estado ativo ─────────
    // Verde + "● Ativo" quando visível para consumidores.
    // Vermelho + "○ Inativo" quando oculto.
    private void aplicarEstadoBotao(Button btn, boolean ativo) {
        if (ativo) {
            btn.setText("Ativo");
            btn.setBackgroundTintList(
                    ColorStateList.valueOf(
                            btn.getContext().getColor(R.color.green)));
        } else {
            btn.setText("Inativo");
            btn.setBackgroundTintList(ColorStateList.valueOf(btn.getContext().getColor(R.color.vermelho_status)));
        }
    }

    // ── ViewHolder — btnDeletar REMOVIDO ────────────────────────────
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduto;
        TextView txtNome, txtTipo, txtPreco;
        Button btnEditar, btnStatus;
        // btnDeletar removido — não existe mais nem no layout nem aqui

        public ViewHolder(View itemView) {
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