package com.example.hortlink.entidades;

public class Produto {
    public String id;        // era int (SQLite), agora String (UUID do Supabase)
    public String nome;
    public double preco;
    public String categoria;
    public String descricao;
    public String imagemUri; // mantido — agora recebe a URL pública do Supabase Storage
    public String unidade;
    public String vendedorUid;

    // campos legados — mantidos para não quebrar outras telas
    public int imagem = 0;
    public Produtor produtor = null;
    public String produtorId;

    public Produto() {}

    // Construtor do Supabase
    public Produto(String id, String nome, double preco, String categoria,
                   String imagemUri, String descricao, String unidade) {
        this.id        = id;
        this.nome      = nome;
        this.preco     = preco;
        this.categoria = categoria;
        this.imagemUri = imagemUri;
        this.descricao = descricao;
        this.unidade   = unidade;
    }

    // Construtor legado — mantido para não quebrar nada que já usa
    public Produto(String nome, double preco, String categoria,
                   String imagemUri, String descricao) {
        this.nome      = nome;
        this.preco     = preco;
        this.categoria = categoria;
        this.imagemUri = imagemUri;
        this.descricao = descricao;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public double getPreco() {
        return preco;
    }

    public void setPreco(double preco) {
        this.preco = preco;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getImagemUri() {
        return imagemUri;
    }

    public void setImagemUri(String imagemUri) {
        this.imagemUri = imagemUri;
    }

    public String getUnidade() {
        return unidade;
    }

    public void setUnidade(String unidade) {
        this.unidade = unidade;
    }

    public String getVendedorUid() {
        return vendedorUid;
    }

    public void setVendedorUid(String vendedorUid) {
        this.vendedorUid = vendedorUid;
    }

    public int getImagem() {
        return imagem;
    }

    public void setImagem(int imagem) {
        this.imagem = imagem;
    }

    public Produtor getProdutor() {
        return produtor;
    }

    public void setProdutor(Produtor produtor) {
        this.produtor = produtor;
    }

    public String getProdutorId() {
        return produtorId;
    }

    public void setProdutorId(String produtorId) {
        this.produtorId = produtorId;
    }
}