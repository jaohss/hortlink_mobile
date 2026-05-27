package com.example.hortlink.data.dto;

import com.example.hortlink.data.model.Usuario;
import com.google.gson.annotations.SerializedName;

public class AuthResponse {
    private String token;

    @SerializedName("usuario")
    private Usuario usuario;

    public String getToken() { return token; }
    public Usuario getUsuario() { return usuario; }
}
