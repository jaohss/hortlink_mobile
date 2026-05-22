package com.example.hortlink.data.dto;

import java.math.BigDecimal;

public class DetalheOfertaDTO {
    private Long id;
    private String nome;
    private BigDecimal valor;
    private String descricao;
    private String nomeProdutor;
    private String cidadeUf;
    private String telefone;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getNomeProdutor() {
        return nomeProdutor;
    }

    public void setNomeProdutor(String nomeProdutor) {
        this.nomeProdutor = nomeProdutor;
    }

    public String getCidadeUf() {
        return cidadeUf;
    }

    public void setCidadeUf(String cidadeUf) {
        this.cidadeUf = cidadeUf;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }
}
