package com.example.hortlink.data.model;

/**
 * Mapeamento 1:1 com a tabela `usuarios` no Supabase.
 * É um POJO puro — sem lógica de UI, sem dependência de Android.
 * Produtor.java faz wrap deste objeto quando tipo = "produtor".
 */
public class Usuario {

/* 
    public String id;
    public String nome;
    public String email;
    public String tipo;
    public String fotoUrl;
    public String cidade;
    public String estado;
    public String descricao;
    public double avaliacao;
    public String telefone;
    public String genero;
    public String criadoEm;
    public String cep;
*/
    
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

