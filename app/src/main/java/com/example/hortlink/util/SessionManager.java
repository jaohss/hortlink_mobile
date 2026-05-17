package com.example.hortlink.util;

import com.example.hortlink.data.model.Usuario;

/**
 * Guarda o Usuario logado em memória durante a sessão.
 *
 * Resolve dois problemas do código atual:
 *  1. Fragments chamando FirebaseAuth.getInstance().getCurrentUser()
 *     só para pegar o UID — agora pegam diretamente do SessionManager.
 *  2. Cada tela fazendo buscarProdutorPorId() logo ao abrir para
 *     descobrir o tipo do usuário — agora o tipo já está disponível.
 *
 * Uso:
 *   SessionManager.getInstance().setUsuario(u);   // após login
 *   Usuario u = SessionManager.getInstance().getUsuario();
 *   boolean isProdutor = SessionManager.getInstance().isProdutor();
 */
public class SessionManager {

    private static SessionManager instance;
    private Usuario usuarioAtual;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    // ─── Escrita ──────────────────────────────────────────────────────

    public void setUsuario(Usuario usuario) {
        this.usuarioAtual = usuario;
    }

    public void limpar() {
        this.usuarioAtual = null;
    }

    // ─── Leitura ──────────────────────────────────────────────────────

    public Usuario getUsuario() {
        return usuarioAtual;
    }

    public boolean estaLogado() {
        return usuarioAtual != null;
    }

    public boolean isProdutor() {
        return usuarioAtual != null && usuarioAtual.isProdutor();
    }

    public boolean isComprador() {
        return usuarioAtual != null && usuarioAtual.isComprador();
    }

    public String getUid() {
        return usuarioAtual != null ? usuarioAtual.id : null;
    }

    public String getNome() {
        return usuarioAtual != null ? usuarioAtual.nome : "";
    }
}
