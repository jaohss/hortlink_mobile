package com.example.hortlink.data.repository;

import com.example.hortlink.util.RetrofitClient;
import com.example.hortlink.data.dto.ComercioDTO;
import com.example.hortlink.service.BaseCallback;
import com.example.hortlink.service.ComercioService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ComercioRepository {

    private final ComercioService api = RetrofitClient.getComercioService();

    public void buscarPorId(Long comercioId, BaseCallback<ComercioDTO> callback) {
        api.buscarPorId(comercioId).enqueue(new Callback<ComercioDTO>() {
            @Override
            public void onResponse(Call<ComercioDTO> call, Response<ComercioDTO> response) {
                if(response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Erro ao buscar o comércio: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ComercioDTO> call, Throwable t) {
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
                    callback.onError("Erro ao salvar oferta: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Falha na rede: " + t.getMessage());
            }
        });
    }
}
