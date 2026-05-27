package com.example.hortlink.data.enums;

public enum Categoria {

    FRUTA("Fruta"),
    VERDURA("Verdura"),
    LEGUME("Legume"),
    TEMPERO("Tempero");

    private final String nome;

    Categoria(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

    @Override
    public String toString() {
        return nome;
    }
}

