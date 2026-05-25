package com.example.hortlink.data.model;

public class Usuario {

    private Long id;
    private String nome;
    private String email;
    private String role;
    private Long comercioProfileId;
    private Boolean cadastroIncompleto;
    private String urlFotoPerfil;

    // Getters
    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public Long getComercioProfileId() { return comercioProfileId; }
    public Boolean getCadastroIncompleto() { return cadastroIncompleto; }
    public String getUrlFotoPerfil() { return urlFotoPerfil; }
}
