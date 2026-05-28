package com.example.hortlink.data.dto;

public class CheckoutRequestDTO {
    private String formaPagamento;
    private Long enderecoEntregaId;
    private String observacoes;

    public CheckoutRequestDTO(String formaPagamento, Long enderecoEntregaId, String observacoes) {
        this.formaPagamento = formaPagamento;
        this.enderecoEntregaId = enderecoEntregaId;
        this.observacoes = observacoes;
    }

    public String getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(String formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public Long getEnderecoEntregaId() {
        return enderecoEntregaId;
    }

    public void setEnderecoEntregaId(Long enderecoEntregaId) {
        this.enderecoEntregaId = enderecoEntregaId;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
}
