package com.example.hortlink.data.dto;

public class ComercioDTO {
    private String nome;
    private Double avaliacao;
    private String cidade;
    private String telefone;
    private String img_url;

    public ComercioDTO(String nome, Double avaliacao, String cidade, String telefone, String img_url) {
        this.nome = nome;
        this.avaliacao = avaliacao;
        this.cidade = cidade;
        this.telefone = telefone;
        this.img_url = img_url;
    }

    public ComercioDTO() {}

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Double getAvaliacao() {
        return avaliacao;
    }

    public void setAvaliacao(Double avaliacao) {
        this.avaliacao = avaliacao;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }
}
