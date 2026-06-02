package com.example.hortlink.data.dto;

import java.math.BigDecimal;

public class NovaOfertaDTO {
    private Long produtoId;
    private BigDecimal preco;
    private BigDecimal estoqueAtual;
    private String dataColheita;
    private Boolean disponivelParaVenda;

    public NovaOfertaDTO(Long produtoId, BigDecimal preco, BigDecimal estoqueAtual, String dataColheita, Boolean disponivelParaVenda) {
        this.produtoId = produtoId;
        this.preco = preco;
        this.estoqueAtual = estoqueAtual;
        this.dataColheita = dataColheita;
        this.disponivelParaVenda = disponivelParaVenda;
    }

    public NovaOfertaDTO() {
    }

    public Boolean getDisponivelParaVenda() {
        return disponivelParaVenda;
    }
    public void setDisponivelParaVenda(Boolean disponivelParaVenda) {
        this.disponivelParaVenda = disponivelParaVenda;
    }
    public Long getProdutoId() {
        return produtoId;
    }

    public void setProdutoId(Long produtoId) {
        this.produtoId = produtoId;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }

    public BigDecimal getEstoqueAtual() {
        return estoqueAtual;
    }

    public void setEstoqueAtual(BigDecimal estoqueAtual) {
        this.estoqueAtual = estoqueAtual;
    }

    public String getDataColheita() {
        return dataColheita;
    }

    public void setDataColheita(String dataColheita) {
        this.dataColheita = dataColheita;
    }
}
