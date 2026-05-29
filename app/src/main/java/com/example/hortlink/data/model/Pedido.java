package com.example.hortlink.data.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Pedido {

    private String id;
    private Long clienteId;
    private Long comercioId;
    private BigDecimal valorTotal;
    private String status;
    private String observacoes;
    private String criadoEm;
    private List<ItemPedido> itens = new ArrayList<>();

    public Pedido() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public Long getComercioId() {
        return comercioId;
    }

    public void setComercioId(Long comercioId) {
        this.comercioId = comercioId;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public String getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(String criadoEm) {
        this.criadoEm = criadoEm;
    }

    public List<ItemPedido> getItens() {
        return itens;
    }

    public void setItens(List<ItemPedido> itens) {
        this.itens = itens;
    }

    // ─── Item do pedido ───────────────────────────────────────────────
    public static class ItemPedido {
        public Long id;
        public String nomeProduto;
        public Integer quantidade;
        public BigDecimal precoUnitario;
        public BigDecimal subtotal;

    }
}