package com.example.hortlink.entidades;

//Molde do produto
//Isso representa um item do app (tipo tomate, alface, etc).
public class Produto {
    public int id;
    public String nome;
    public double preco;
    public String categoria;
    public int imagem;
    public String descricao;
    public String imagemUri; // ✅ URI do banco
    public Produtor produtor;
    public int produtorId; // ✅ ID vindo do banco

    public Produto(){}


    public Produto(String nome, double preco, String categoria, int imagem, String descricao, Produtor produtor, String imagemUri){
        this.nome=nome;
        this.preco=preco;
        this.categoria=categoria;
        this.imagem=imagem;
        this.descricao=descricao;
        this.produtor=produtor;
        this.imagemUri = imagemUri;
    }

    // ✅ Construtor do banco (sem drawable, sem produtor)
    public Produto(String nome, double preco, String categoria, String imagemUri, String descricao) {
        this.nome = nome;
        this.preco = preco;
        this.categoria = categoria;
        this.imagemUri = imagemUri;
        this.descricao = descricao;
        this.imagem = 0;       // sem drawable
        this.produtor = null;  // sem produtor
    }
}
