package com.example.hortlink.data.model;

public class Produtor {
    public int id;
    public String nome;
    public String contato;
    public String cidade;
    public String documento;
    public double avaliacao;

    // ✅ URIs do banco (substituem os int de drawable)
    public String fotoPerfilUri;
    public String fotoCapaUri;

    // mantém os int pra não quebrar código que ainda usa drawable
    public int fotoPerfil;
    public int fotoCapa;

    public Produtor(){}

    public Produtor(String nome, String contato, String cidade, int fotoPerfil, double avaliacao, int fotoCapa){
        this.nome = nome;
        this.contato = contato;
        this.cidade = cidade;
        this.fotoPerfil = fotoPerfil;
        this.avaliacao = avaliacao;
        this.fotoCapa = fotoCapa;
    }
}
