package com.example.hortlink.util;

public class SessionManager {
    private static SessionManager instance;
    private String usuarioId;
    private String usuarioTipo;
    private String nomeUsuario;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    public void init(String usuarioId, String usuarioTipo, String nomeUsuario) {
        this.usuarioId = usuarioId;
        this.usuarioTipo = usuarioTipo;
        this.nomeUsuario = nomeUsuario;
    }

    public boolean isProdutor() { return "produtor".equals(usuarioTipo); }
    public boolean isConsumidor() { return "consumidor".equals(usuarioTipo); }
    public String getUsuarioId() { return usuarioId; }
    public void clear() { usuarioId = null; usuarioTipo = null; nomeUsuario = null; }
}
