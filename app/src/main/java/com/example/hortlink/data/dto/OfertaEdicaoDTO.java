package com.example.hortlink.data.dto;

import java.math.BigDecimal;

public class OfertaEdicaoDTO {
    private Long idOferta;
    private BigDecimal preco;
    private Integer estoqueAtual;
    private String dataColheita;
    private Boolean disponivelParaVenda;

    private Long produtoId;
    private String nomeProduto;
    private String unidadeSimbolo;
    private String imagemUrl;

    public OfertaEdicaoDTO(Long idOferta, BigDecimal preco, Integer estoqueAtual, String dataColheita, Boolean disponivelParaVenda, Long produtoId, String nomeProduto, String unidadeSimbolo, String imagemUrl) {
        this.idOferta = idOferta;
        this.preco = preco;
        this.estoqueAtual = estoqueAtual;
        this.dataColheita = dataColheita;
        this.disponivelParaVenda = disponivelParaVenda;
        this.produtoId = produtoId;
        this.nomeProduto = nomeProduto;
        this.unidadeSimbolo = unidadeSimbolo;
        this.imagemUrl = imagemUrl;
    }

    public OfertaEdicaoDTO() {
    }

    public Long getIdOferta() {
        return idOferta;
    }

    public void setIdOferta(Long idOferta) {
        this.idOferta = idOferta;
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

    public String getNomeProduto() {
        return nomeProduto;
    }

    public void setNomeProduto(String nomeProduto) {
        this.nomeProduto = nomeProduto;
    }

    public String getUnidadeSimbolo() {
        return unidadeSimbolo;
    }

    public void setUnidadeSimbolo(String unidadeSimbolo) {
        this.unidadeSimbolo = unidadeSimbolo;
    }

    public String getImagemUrl() {
        return imagemUrl;
    }

    public void setImagemUrl(String imagemUrl) {
        this.imagemUrl = imagemUrl;
    }
}
