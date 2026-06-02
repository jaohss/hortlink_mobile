package com.example.hortlink.data.dto;

public class PerfilCompradorDTO {

    private String nome;
    private String email;
    private String telefone;
    private String cep;
    private String cidade;
    private String estado;
    private String bairro;
    private String complemento;

    public PerfilCompradorDTO() {
    }
    public PerfilCompradorDTO(String telefone, String cep, String cidade, String estado, String bairro, String complemento) {
        this.telefone = telefone;
        this.cep = cep;
        this.cidade = cidade;
        this.estado = estado;
        this.bairro = bairro;
        this.complemento = complemento;
    }

    // Getters para a hora de receber (editar)
    public String getTelefone() { return telefone; }
    public String getCep() { return cep; }
    public String getCidade() { return cidade; }
    public String getEstado() { return estado; }
    public String getBairro() { return bairro; }
    public String getComplemento() { return complemento; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }
}
