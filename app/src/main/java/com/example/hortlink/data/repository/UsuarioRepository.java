package com.example.hortlink.data.repository;

import com.example.hortlink.data.dto.PerfilCompradorDTO;
import com.example.hortlink.service.BaseCallback;
import com.example.hortlink.service.UsuarioService;
import com.example.hortlink.util.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;

public class UsuarioRepository {

    private final UsuarioService api;

    public UsuarioRepository() {
        this.api = RetrofitClient.getUsuarioService();
    }

    // ─── Buscar por ID ────────────────────────────────────────────────
    public void obterPerfil(BaseCallback<PerfilCompradorDTO> callback) {
        api.obterPerfil().enqueue(new Callback<PerfilCompradorDTO>() {
            @Override
            public void onResponse(Call<PerfilCompradorDTO> call, retrofit2.Response<PerfilCompradorDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Erro ao carregar seu perfil: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<PerfilCompradorDTO> call, Throwable t) {
                callback.onError("Falha na rede: " + t.getMessage());
            }
        });
    }

    public void obterDetalhesCliente(Long clienteId, BaseCallback<PerfilCompradorDTO> callback) {
        api.obterDetalhesCliente(clienteId).enqueue(new Callback<PerfilCompradorDTO>() {
            @Override
            public void onResponse(Call<PerfilCompradorDTO> call, retrofit2.Response<PerfilCompradorDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Erro ao carregar os detalhes do cliente: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<PerfilCompradorDTO> call, Throwable t) {
                callback.onError("Falha na rede: " + t.getMessage());
            }
        });
    }

    public void atualizarPerfil(PerfilCompradorDTO dto, BaseCallback<Void> callback) {
        api.atualizarPerfil(dto).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, retrofit2.Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError("Erro ao atualizar seu perfil: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Falha na rede: " + t.getMessage());
            }
        });
    }
}