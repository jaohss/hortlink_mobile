package com.example.hortlink.entidades;

public class CartItem {

    private String carrinhoId;   // id da linha na tabela carrinho
    private String produtoId;
    private String nomeProduto;
    private String producerId;
    private double preco;
    private int quantidade;
    private String fotoUrl;
    private String unidade;


    public CartItem() {}

    // Getters
    public String getCarrinhoId()   { return carrinhoId; }
    public String getProdutoId()    { return produtoId; }
    public String getNomeProduto()  { return nomeProduto; }
    public String getProducerId()   { return producerId; }
    public double getPreco()        { return preco; }
    public int    getQuantidade()   { return quantidade; }
    public String getFotoUrl()      { return fotoUrl; }
    public String getUnidade()      { return unidade; }

    // Setters
    public void setCarrinhoId(String v)  { carrinhoId = v; }
    public void setProdutoId(String v)   { produtoId = v; }
    public void setNomeProduto(String v) { nomeProduto = v; }
    public void setProducerId(String v)  { producerId = v; }
    public void setPreco(double v)       { preco = v; }
    public void setQuantidade(int v)     { quantidade = v; }
    public void setFotoUrl(String v)     { fotoUrl = v; }
    public void setUnidade(String v)     { unidade = v; }

    public double getSubtotal() { return preco * quantidade; }
}
