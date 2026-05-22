package com.example.hortlink.data.dto;

import com.example.hortlink.data.enums.Categoria;
import com.example.hortlink.data.enums.UnidadeMedida;

public class NovoProdutoDTO {
    private Long id;
    private String nome;
    private String descricao;
    private Categoria categoria;
    private UnidadeMedida unidadeMedida;

    public NovoProdutoDTO(Long id, String nome, String descricao, Categoria categoria, UnidadeMedida unidadeMedida) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.categoria = categoria;
        this.unidadeMedida = unidadeMedida;
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
