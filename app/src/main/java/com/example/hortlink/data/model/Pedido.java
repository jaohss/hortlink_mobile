package com.example.hortlink.data.model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Pedido {

    private String id;
    private String compradorId;
    private String produtorId;
    private double valorTotal;
    private String status;
    private String criadoEm;
    private List<ItemPedido> itens = new ArrayList<>();

    public Pedido() {}

    // ─── Getters ──────────────────────────────────────────────────────
    public String getId()              { return id; }
    public String getCompradorId()     { return compradorId; }
    public String getProdutorId()      { return produtorId; }
    public double getValorTotal()      { return valorTotal; }
    public String getStatus()          { return status; }
    public String getCriadoEm()        { return criadoEm; }
    public List<ItemPedido> getItens() { return itens; }

    // ─── Setters ──────────────────────────────────────────────────────
    public void setId(String v)              { id = v; }
    public void setCompradorId(String v)     { compradorId = v; }
    public void setProdutorId(String v)      { produtorId = v; }
    public void setValorTotal(double v)      { valorTotal = v; }
    public void setStatus(String v)          { status = v; }
    public void setCriadoEm(String v)        { criadoEm = v; }
    public void setItens(List<ItemPedido> v) { itens = v; }

    // ─── Parse do JSON do Supabase ────────────────────────────────────
    public static Pedido fromJson(JSONObject obj) {
        Pedido p = new Pedido();
        p.id          = obj.optString("id");
        p.compradorId = obj.optString("comprador_id");
        p.produtorId  = obj.optString("produtor_id");
        p.valorTotal  = obj.optDouble("valor_total", 0.0);
        p.status      = obj.optString("status");
        p.criadoEm    = obj.optString("criado_em");
        return p;
    }

    // ─── Item do pedido ───────────────────────────────────────────────
    public static class ItemPedido {
        public String produtoId;
        public String nomeProduto;
        public int quantidade;
        public double precoUnitario;

        public double getSubtotal() { return precoUnitario * quantidade; }

        public static ItemPedido fromJson(JSONObject obj) {
            ItemPedido item = new ItemPedido();
            item.produtoId      = obj.optString("produto_id");
            item.quantidade     = obj.optInt("quantidade", 1);
            item.precoUnitario  = obj.optDouble("preco_unitario", 0.0);

            // Nome vem via join com produtos
            JSONObject produto = obj.optJSONObject("produtos");
            if (produto != null) {
                item.nomeProduto = produto.optString("nome");
            }
            return item;
        }
    }
}