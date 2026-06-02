package com.example.hortlink.data.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class NovaOfertaDTO {
    private Long produtoId;
    private BigDecimal preco;
    private Integer estoqueAtual;
    private String dataColheita;
    private Boolean disponivelParaVenda;

    public NovaOfertaDTO(Long produtoId, BigDecimal preco, Integer estoqueAtual, String dataColheita, Boolean disponivelParaVenda) {
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

    public Integer getEstoqueAtual() {
        return estoqueAtual;
    }

    public void setEstoqueAtual(Integer estoqueAtual) {
        this.estoqueAtual = estoqueAtual;
    }

    public String getDataColheita() {
        return dataColheita;
    }

    public void setDataColheita(String dataColheita) {
        this.dataColheita = dataColheita;
    }
}
