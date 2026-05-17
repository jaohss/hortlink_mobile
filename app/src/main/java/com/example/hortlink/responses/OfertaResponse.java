package com.example.hortlink.responses;

public class OfertaResponse {
    private int id;
    private String comercio;
    private String produto;
    private double valor;
    private int promocao;
    private int estoqueAtual;
    private boolean disponivelParaVenda;

    // Getters
    public int getId() { return id; }
    public String getComercio() { return comercio; }
    public String getProduto() { return produto; }
    public double getValor() { return valor; }
    public int getPromocao() { return promocao; }
    public int getEstoqueAtual() { return estoqueAtual; }
    public boolean isDisponivelParaVenda() { return disponivelParaVenda; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setComercio(String comercio) { this.comercio = comercio; }
    public void setProduto(String produto) { this.produto = produto; }
    public void setValor(double valor) { this.valor = valor; }
    public void setPromocao(int promocao) { this.promocao = promocao; }
    public void setEstoqueAtual(int estoqueAtual) { this.estoqueAtual = estoqueAtual; }
    public void setDisponivelParaVenda(boolean disponivelParaVenda) { this.disponivelParaVenda = disponivelParaVenda; }
}