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
    public int produtorId = 0;

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
}