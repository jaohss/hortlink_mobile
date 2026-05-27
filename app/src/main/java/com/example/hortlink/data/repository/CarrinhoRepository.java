package com.example.hortlink.data.repository;

import com.example.hortlink.data.model.CarrinhoResponse;
import com.example.hortlink.service.CarrinhoService;
import com.example.hortlink.util.RetrofitClient;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CarrinhoRepository {

    private final CarrinhoService api;

    public CarrinhoRepository() {
        this.api = RetrofitClient.getCarrinhoService();
    }

    public interface CarrinhoCallback {
        void onSuccess(CarrinhoResponse carrinho);
        void onError(String mensagemErro);
    }

    public void obterCarrinho(Long compradorId, CarrinhoCallback callback) {
        api.obterCarrinho(compradorId).enqueue(new Callback<CarrinhoResponse>() {
            @Override
            public void onResponse(Call<CarrinhoResponse> call, Response<CarrinhoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Erro ao carregar carrinho: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<CarrinhoResponse> call, Throwable t) {
                callback.onError("Falha na rede: " + t.getMessage());
            }
        });
    }

    public void alterarQuantidade(Long compradorId, Long idItem, int novaQuantidade, CarrinhoCallback callback) {
        JsonObject request = new JsonObject();
        request.addProperty("novaQuantidade", novaQuantidade);

        api.atualizarQuantidade(compradorId, idItem, request).enqueue(new Callback<CarrinhoResponse>() {
            @Override
            public void onResponse(Call<CarrinhoResponse> call, Response<CarrinhoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Erro ao atualizar quantidade");
                }
            }

            @Override
            public void onFailure(Call<CarrinhoResponse> call, Throwable t) {
                callback.onError("Falha na rede: " + t.getMessage());
            }
        });
    }

    public void removerItem(Long compradorId, Long idItem, CarrinhoCallback callback) {
        api.removerItem(compradorId, idItem).enqueue(new Callback<CarrinhoResponse>() {
            @Override
            public void onResponse(Call<CarrinhoResponse> call, Response<CarrinhoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Erro ao remover item");
                }
            }

            @Override
            public void onFailure(Call<CarrinhoResponse> call, Throwable t) {
                callback.onError("Falha na rede: " + t.getMessage());
            }
        });
    }
}
