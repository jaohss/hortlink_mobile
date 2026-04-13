package com.example.hortlink;

//Molde do produto
//Isso representa um item do app (tipo tomate, alface, etc).
public class Produto {
    String nome;
    double preco;
    String categoria;
    int imagem;
    String descricao;

    public Produto(String nome, double preco, String categoria, int imagem, String descricao){
        this.nome=nome;
        this.preco=preco;
        this.categoria=categoria;
        this.imagem=imagem;
        this.descricao=descricao;
    }
}
