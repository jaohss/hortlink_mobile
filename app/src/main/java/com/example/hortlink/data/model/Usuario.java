package com.example.hortlink.data.model;

import org.json.JSONObject;

/**
 * Mapeamento 1:1 com a tabela `usuarios` no Supabase.
 * É um POJO puro — sem lógica de UI, sem dependência de Android.
 * Produtor.java faz wrap deste objeto quando tipo = "produtor".
 */
public class Usuario {

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

    public static final String TIPO_COMPRADOR = "comprador";
    public static final String TIPO_PRODUTOR  = "produtor";

    public static final String GENERO_MASCULINO    = "Masculino";
    public static final String GENERO_FEMININO     = "Feminino";
    public static final String GENERO_NAO_INFORMAR = "Prefiro não informar";

    public Usuario() {}

    public boolean isProdutor() {
        return TIPO_PRODUTOR.equals(tipo);
    }

    public boolean isComprador() {
        return TIPO_COMPRADOR.equals(tipo);
    }

    public static Usuario fromJson(JSONObject obj) {
        Usuario u   = new Usuario();
        u.id        = obj.optString("id");
        u.nome      = obj.optString("nome");
        u.email     = obj.optString("email");
        u.tipo      = obj.optString("tipo");
        u.fotoUrl   = obj.optString("foto_url");
        u.cidade    = obj.optString("cidade");
        u.estado    = obj.optString("estado");
        u.descricao = obj.optString("descricao");
        u.avaliacao = obj.optDouble("avaliacao", 0.0);
        u.telefone  = obj.optString("telefone");
        u.genero    = obj.optString("genero");
        u.criadoEm  = obj.optString("criado_em");
        return u;
    }
}