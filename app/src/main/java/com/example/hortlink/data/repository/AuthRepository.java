package com.example.hortlink.data.repository;

import com.example.hortlink.data.dto.AuthRequest;
import com.example.hortlink.data.dto.AuthResponse;
import com.example.hortlink.data.dto.RegistroDTO;
import com.example.hortlink.data.model.Usuario;
import com.example.hortlink.service.AuthService;
import com.example.hortlink.service.BaseCallback;
import com.example.hortlink.util.RetrofitClient;
import com.example.hortlink.util.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {

    private final AuthService api;

    // Pedimos o Context no construtor para poder usar as classes de Sessão
    public AuthRepository() {
        this.api = RetrofitClient.getAuthService();
    }

    // ─── LOGIN ──────────────────────────────────────────────────────────
    // Note que agora o Callback retorna o UsuarioDTO
    public void login(String email, String senha, BaseCallback<Usuario> callback) { // <-- Use UsuarioDTO
        AuthRequest request = new AuthRequest(email, senha);

        api.login(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {

                    AuthResponse authResponse = response.body();
                    Usuario usuarioLogado = authResponse.getUsuario(); // <-- Use UsuarioDTO
                    String tokenJwt = authResponse.getToken();

                    // MÁGICA AQUI: Não precisa mais passar 'context'
                    SessionManager.getInstance().init(usuarioLogado, tokenJwt);

                    callback.onSuccess(usuarioLogado);

                } else if (response.code() == 401) {
                    callback.onError("E-mail ou senha incorretos.");
                } else {
                    callback.onError("Erro no servidor: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                callback.onError("Falha na conexão: " + t.getMessage());
            }
        });
    }

    // ─── REGISTRO ───────────────────────────────────────────────────────
    // É recomendado usar o UsuarioDTO aqui também, para o mobile não precisar conhecer a Entidade do banco
    public void register(RegistroDTO novoUsuario, BaseCallback<Void> callback) {
        api.register(novoUsuario).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError("Erro ao registrar: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Falha na conexão: " + t.getMessage());
            }
        });
    }

    // ─── VERIFICAR E-MAIL ───────────────────────────────────────────────
    public void verificarEmail(String email, BaseCallback<Boolean> callback) {
        api.verificarEmail(email).enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Erro ao verificar email");
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                callback.onError("Falha na rede: " + t.getMessage());
            }
        });
    }
}