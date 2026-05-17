package com.example.hortlink.data.model;

import org.json.JSONObject;

/**
 * Visão de produtor sobre a tabela `usuarios`.
 *
 * NÃO duplica campos — delega tudo para o Usuario interno.
 * Isso garante que quando a tabela mudar, só Usuario.java muda.
 *
 * Uso:
 *   Usuario u = Usuario.fromJson(obj);
 *   if (u.isProdutor()) {
 *       Produtor p = new Produtor(u);
 *   }
 */
public class Produtor {

    private final Usuario usuario;

    public Produtor(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("Usuario não pode ser nulo");
        }
        if (!usuario.isProdutor()) {
            throw new IllegalArgumentException(
                    "Tentativa de criar Produtor com tipo=" + usuario.tipo);
        }
        this.usuario = usuario;
    }

    // ─── Delegação limpa — zero duplicação de campos ──────────────────

    public String getId()        { return usuario.id; }
    public String getNome()      { return usuario.nome; }
    public String getEmail()     { return usuario.email; }
    public String getFotoUrl()   { return usuario.fotoUrl; }
    public String getCidade()    { return usuario.cidade; }
    public String getTelefone()   { return usuario.telefone; }
    public String getDescricao() { return usuario.descricao; }
    //public double getAvaliacao() { return usuario.avaliacao; }

    // ─── Métodos específicos de domínio do produtor ───────────────────

    /** Texto de exibição no card de lista de produtores. */
    public String getPerfilResumido() {
        String cidade = (usuario.cidade != null && !usuario.cidade.isEmpty())
                ? usuario.cidade : "Localização não informada";
        return usuario.nome + " · " + cidade;
    }
    public Usuario getUsuario() { return usuario; }

    /** Formata avaliação para exibição em estrelas. */
    //Implementação futura
//    public String getAvaliacaoFormatada() {
//        return String.format("⭐ %.1f", usuario.avaliacao);
//    }

    /** Acesso ao objeto base quando o repositório precisar persistir. */
    public Usuario toUsuario() {
        return usuario;
    }

    // ─── Parse direto do JSON (atalho para uso nos repositories) ──────
    public static Produtor fromJson(JSONObject obj) {
        Usuario u = Usuario.fromJson(obj);
        return new Produtor(u);
    }
}