package com.example.hortlink.data.repository;

import com.example.hortlink.data.dto.ComercioDTO;
import com.example.hortlink.data.dto.CompletarPerfilComercioDTO;
import com.example.hortlink.service.BaseCallback;
import com.example.hortlink.service.ComercioService;
import com.example.hortlink.util.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ComercioRepository {

    private final ComercioService api = RetrofitClient.getComercioService();

    public void obterPerfilBase(BaseCallback<ComercioDTO> callback) {
        api.obterPerfilBase().enqueue(new Callback<ComercioDTO>() {
            @Override
            public void onResponse(Call<ComercioDTO> call, Response<ComercioDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Erro ao buscar o perfil: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ComercioDTO> call, Throwable t) {
                callback.onError("Falha na rede: " + t.getMessage());
            }
        });
    }

    public void obterDadosComercio(Long comercioId, BaseCallback<ComercioDTO> callback) {
        api.obterDadosComercio(comercioId).enqueue(new Callback<ComercioDTO>() {
            @Override
            public void onResponse(Call<ComercioDTO> call, Response<ComercioDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Erro ao buscar os dados do comércio: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ComercioDTO> call, Throwable t) {
                callback.onError("Falha na rede: " + t.getMessage());
            }
        });
    }
    public void buscarPerfil(BaseCallback<CompletarPerfilComercioDTO> callback) {
        api.buscarPerfilForm().enqueue(new Callback<CompletarPerfilComercioDTO>() {
            @Override
            public void onResponse(Call<CompletarPerfilComercioDTO> call, Response<CompletarPerfilComercioDTO> response) {
                if(response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Erro ao buscar o comércio: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<CompletarPerfilComercioDTO> call, Throwable t) {
                callback.onError("Falha na rede: " + t.getMessage());
            }
        });
    }

    public void deletarComercio(Long comercioId, BaseCallback<Void> callback) {
        api.deletarComercio(comercioId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if(response.isSuccessful()){
                    callback.onSuccess(null);
                } else {
                    callback.onError("Erro ao deletar comércio: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Falha na rede: " + t.getMessage());
            }
        });
    }

    public void completarPerfil(CompletarPerfilComercioDTO dto, BaseCallback<Void> callback) {
        api.completarPerfilComercio(dto).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError("Erro ao completar o perfil: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Falha na rede: " + t.getMessage());
            }
        });
    }

    public void listarPorCidade(String cidade, BaseCallback<List<ComercioDTO>> callback) {
        api.listarPorCidade(cidade).enqueue(new Callback<List<ComercioDTO>>() {
            @Override
            public void onResponse(Call<List<ComercioDTO>> call, Response<List<ComercioDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Erro ao listar comércios: " + response.code());
                }

            }

            @Override
            public void onFailure(Call<List<ComercioDTO>> call, Throwable t) {
                callback.onError("Falha na rede: " + t.getMessage());
            }
        });
    }
}
