package com.example.hortlink.data.model;

import com.example.hortlink.data.enums.Categoria;
import com.example.hortlink.data.enums.UnidadeMedida;

public class Produto {
    
    private Long id;
    private String nome;
    private String descricao;
    private Categoria categoria;
    private UnidadeMedida unidadeMedida;
    private String imagemUrl;
    public double distanciaKm = Double.MAX_VALUE;
    public Long vendedorId;

    public Produto(Long id, String nome, String descricao, Categoria categoria, UnidadeMedida unidadeMedida, String imagemUrl) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.categoria = categoria;
        this.unidadeMedida = unidadeMedida;
        this.imagemUrl = imagemUrl;
    }

    public String getImagemUrl() {
        return imagemUrl;
    }

    public void setImagemUrl(String imagemUrl) {
        this.imagemUrl = imagemUrl;
    }

    @Override
    public String toString() {
        return "Produto{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", descricao='" + descricao + '\'' +
                ", categoria=" + categoria +
                ", unidadeMedida=" + unidadeMedida +
                ", imagemUrl='" + imagemUrl + '\'' +
                ", distanciaKm=" + distanciaKm +
                ", vendedorId=" + vendedorId +
                '}';
    }

    public double getDistanciaKm() {
        return distanciaKm;
    }



    public void setDistanciaKm(double distanciaKm) {
        this.distanciaKm = distanciaKm;
    }



    public Long getVendedorId() {
        return vendedorId;
    }



    public void setVendedorId(Long vendedorId) {
        this.vendedorId = vendedorId;
    }



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

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public UnidadeMedida getUnidadeMedida() {
        return unidadeMedida;
    }

    public void setUnidadeMedida(UnidadeMedida unidadeMedida) {
        this.unidadeMedida = unidadeMedida;
    }

}
