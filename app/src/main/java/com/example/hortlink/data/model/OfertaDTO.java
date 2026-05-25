package com.example.hortlink.data.model;

import com.google.gson.annotations.SerializedName;

public class OfertaDTO {
    public Long id;        // era int (SQLite), agora String (UUID do Supabase)
    public String nome;
    public double preco;
    public String categoria;
    public String descricao;
    @SerializedName("fotoUrl")
    public String imagemUri; // mantido — agora recebe a URL pública do Supabase Storage
    public String unidade;
    public String vendedorUid;
    public boolean status = true;

    public OfertaDTO(Long id, String nome, double preco, String categoria, String descricao, String imagemUri, String unidade, String vendedorUid, boolean status, int imagem, Produtor produtor, Long comercioId) {
        this.id = id;
        this.nome = nome;
        this.preco = preco;
        this.categoria = categoria;
        this.descricao = descricao;
        this.imagemUri = imagemUri;
        this.unidade = unidade;
        this.vendedorUid = vendedorUid;
        this.status = status;
        this.imagem = imagem;
        this.produtor = produtor;
        this.comercioId = comercioId;
    }

    // campos legados — mantidos para não quebrar outras telas
    public int imagem = 0;
    public Produtor produtor = null;
    public Long comercioId;

    public OfertaDTO() {}

    // Construtor do Supabase




    // Construtor legado — mantido para não quebrar nada que já usa
    public OfertaDTO(String nome, double preco, String categoria,
                     String imagemUri, String descricao) {
        this.nome      = nome;
        this.preco     = preco;
        this.categoria = categoria;
        this.imagemUri = imagemUri;
        this.descricao = descricao;
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
    public boolean isStatus() {
        return status;
    }
    public void setStatus(boolean status) {
        this.status = status;
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getComercioId() {
        return comercioId;
    }

    public void setComercioId(Long comercioId) {
        this.comercioId = comercioId;
    }
}