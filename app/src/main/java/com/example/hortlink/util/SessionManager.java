package com.example.hortlink.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.hortlink.MyApplication;
import com.example.hortlink.data.model.Usuario;

public class SessionManager {

    private static SessionManager instance;
    private SharedPreferences prefs;

    // Chaves do SharedPreferences
    private static final String PREF_NAME = "HortiLinkSessao";
    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_USUARIO_ID = "usuario_id";
    private static final String KEY_NOME = "nome_usuario";
    private static final String KEY_ROLE = "usuario_role";
    private static final String KEY_COMERCIO_ID = "comercio_id";
    private static final String KEY_CADASTRO_INCOMPLETO = "cadastro_incompleto";
    private static final String KEY_URL_FOTO = "url_foto";

    private SessionManager() {
        prefs = MyApplication.getAppContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    // ─── VERIFICAÇÃO DE LOGIN ───
    public boolean estaLogado() {
        String token = getToken();
        return token != null && !token.isEmpty();
    }

    // ─── SALVA OS DADOS APÓS O LOGIN ───
    public void init(Usuario usuario, String tokenJwt) {
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(KEY_TOKEN, tokenJwt);
        editor.putLong(KEY_USUARIO_ID, usuario.getId());
        editor.putString(KEY_NOME, usuario.getNome());
        editor.putString(KEY_ROLE, usuario.getRole());

        // Salva a flag de cadastro incompleto (se vier nulo, assume false)
        editor.putBoolean(KEY_CADASTRO_INCOMPLETO,
                usuario.getCadastroIncompleto() != null && usuario.getCadastroIncompleto());

        // Salva URL da Foto
        if (usuario.getUrlFotoPerfil() != null) {
            editor.putString(KEY_URL_FOTO, usuario.getUrlFotoPerfil());
        } else {
            editor.remove(KEY_URL_FOTO);
        }

        // Salva ID do Comércio (se houver)
        if (usuario.getComercioId() != null) {
            editor.putLong(KEY_COMERCIO_ID, usuario.getComercioId());
        } else {
            editor.remove(KEY_COMERCIO_ID);
        }

        editor.apply();
    }

    // ─── REGRAS DE NEGÓCIO ───
    public boolean isProdutor() {
        String role = getRole();
        return "PRODUTOR".equalsIgnoreCase(role) || "COMERCIO".equalsIgnoreCase(role);
    }

    public boolean isConsumidor() {
        return "CONSUMIDOR".equalsIgnoreCase(getRole());
    }

    // ─── GETTERS ───
    public String getToken() { return prefs.getString(KEY_TOKEN, null); }
    public Long getUsuarioId() { return prefs.getLong(KEY_USUARIO_ID, -1); }
    public String getNomeUsuario() { return prefs.getString(KEY_NOME, null); }
    public String getRole() { return prefs.getString(KEY_ROLE, null); }
    public Long getComercioProfileId() { return prefs.getLong(KEY_COMERCIO_ID, -1); }
    public boolean isCadastroIncompleto() { return prefs.getBoolean(KEY_CADASTRO_INCOMPLETO, false); }
    public String getUrlFoto() { return prefs.getString(KEY_URL_FOTO, null); }

    // ─── LOGOUT ───
    public void clear() {
        prefs.edit().clear().apply();
    }
}
