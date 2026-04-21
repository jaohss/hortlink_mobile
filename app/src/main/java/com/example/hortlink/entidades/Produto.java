package com.example.hortlink.entidades;

//Molde do produto
//Isso representa um item do app (tipo tomate, alface, etc).
public class Produto {
    public String nome;
    public double preco;
    public String categoria;
    public int imagem;
    public String descricao;
    public Produtor produtor;

    public Produto(String nome, double preco, String categoria, int imagem, String descricao, Produtor produtor){
        this.nome=nome;
        this.preco=preco;
        this.categoria=categoria;
        this.imagem=imagem;
        this.descricao=descricao;
        this.produtor=produtor;
    }
}
