package com.example.hortlink.data.dto;

public class PerfilCompradorDTO {
    private String telefone;
    private String cep;
    private String cidade;
    private String estado;
    private String bairro;
    private String complemento;
    private String genero;

    // Construtor completo para a hora de enviar (salvar)
    public PerfilCompradorDTO(String telefone, String cep, String cidade, String estado, String bairro, String complemento, String genero) {
        this.telefone = telefone;
        this.cep = cep;
        this.cidade = cidade;
        this.estado = estado;
        this.bairro = bairro;
        this.complemento = complemento;
        this.genero = genero;
    }

    // Getters para a hora de receber (editar)
    public String getTelefone() { return telefone; }
    public String getCep() { return cep; }
    public String getCidade() { return cidade; }
    public String getEstado() { return estado; }
    public String getBairro() { return bairro; }
    public String getComplemento() { return complemento; }
    public String getGenero() { return genero; }
}
