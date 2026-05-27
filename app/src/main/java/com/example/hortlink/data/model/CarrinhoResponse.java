package com.example.hortlink.data.model;

import java.util.List;

public class CarrinhoResponse {
    private Long id;
    private Long compradorId;
    private List<ItemCarrinhoResponse> itens;
    private Double valorTotal; // No mobile, Double é mais prático para a UI do que BigDecimal

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCompradorId() { return compradorId; }
    public void setCompradorId(Long compradorId) { this.compradorId = compradorId; }

    public List<ItemCarrinhoResponse> getItens() { return itens; }
    public void setItens(List<ItemCarrinhoResponse> itens) { this.itens = itens; }

    public Double getValorTotal() { return valorTotal; }
    public void setValorTotal(Double valorTotal) { this.valorTotal = valorTotal; }
}
