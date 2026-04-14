package com.example.hortlink;

public class Produtor {
    public String nome;
    public String contato;
    public String cidade;
    public String documento;
    public int fotoPerfil;
    public double avaliacao;
    public int fotoCapa;

    public Produtor(String nome, String contato, String cidade, int fotoPerfil, double avaliacao, int fotoCapa){
        this.nome=nome;
        this.contato=contato;
        this.cidade=cidade;
        this.fotoPerfil=fotoPerfil;
        this.avaliacao=avaliacao;
        this.fotoCapa=fotoCapa;
    }
}
