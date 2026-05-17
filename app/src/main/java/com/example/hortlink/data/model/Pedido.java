package com.example.hortlink.data.model;

public class Pedido {

    private String cliente;
    private String itens;
    private String valor;
    private String status;

    public Pedido(String cliente, String itens, String valor, String status) {
        this.cliente = cliente;
        this.itens = itens;
        this.valor = valor;
        this.status = status;
    }

    public String getCliente() { return cliente; }
    public String getItens() { return itens; }
    public String getValor() { return valor; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

}
