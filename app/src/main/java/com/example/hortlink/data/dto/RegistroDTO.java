package com.example.hortlink.data.dto;

public class RegistroDTO {
    private String nome;
    private String email;
    private String senha;
    private String role;
    private String telefone;

    public RegistroDTO(String nome, String email, String senha, String role, String telefone) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.role = role;
        this.telefone = telefone;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }
}
