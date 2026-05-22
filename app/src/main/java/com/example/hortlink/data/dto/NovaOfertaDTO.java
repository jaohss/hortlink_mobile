package com.example.hortlink.data.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class NovaOfertaDTO {
    private Long produtoId;
    private BigDecimal preco;
    private Integer estoqueAtual;
    private Long comercioId;
    private LocalDate dataColheita;

    public NovaOfertaDTO(Long produtoId, BigDecimal preco, Integer estoqueAtual, Long comercioId, LocalDate dataColheita) {
        this.produtoId = produtoId;
        this.preco = preco;
        this.estoqueAtual = estoqueAtual;
        this.comercioId = comercioId;
        this.dataColheita = dataColheita;
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

    public Long getComercioId() {
        return comercioId;
    }

    public void setComercioId(Long comercioId) {
        this.comercioId = comercioId;
    }

    public LocalDate getDataColheita() {
        return dataColheita;
    }

    public void setDataColheita(LocalDate dataColheita) {
        this.dataColheita = dataColheita;
    }
}
