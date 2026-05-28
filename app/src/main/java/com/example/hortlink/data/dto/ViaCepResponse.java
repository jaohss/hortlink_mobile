package com.example.hortlink.data.dto;

public class ViaCepResponse {
    private String logradouro;
    private String bairro;
    private String localidade; // O ViaCEP chama a cidade de 'localidade'
    private String uf;
    private Boolean erro; // O ViaCEP retorna "erro": true se o CEP não existir

    public String getLogradouro() { return logradouro; }
    public String getBairro() { return bairro; }
    public String getLocalidade() { return localidade; }
    public String getUf() { return uf; }
    public Boolean getErro() { return erro != null && erro; }
}
