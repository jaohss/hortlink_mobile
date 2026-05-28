package com.example.hortlink.data.repository;

import com.example.hortlink.data.dto.CheckoutRequestDTO;
import com.example.hortlink.data.model.CarrinhoResponse;
import com.example.hortlink.service.BaseCallback;
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

    // ─── OBTER CARRINHO ──────────────────────────────────────────────
    public void obterCarrinho(BaseCallback<CarrinhoResponse> callback) {
        api.obterCarrinho().enqueue(new Callback<CarrinhoResponse>() {
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

    public void adicionarItem(Long ofertaId, int quantidade, BaseCallback<CarrinhoResponse> callback) {
        JsonObject request = new JsonObject();
        request.addProperty("ofertaId", ofertaId);
        request.addProperty("quantidade", quantidade);

        api.adicionarItem(request).enqueue(new Callback<CarrinhoResponse>() {
            @Override
            public void onResponse(Call<CarrinhoResponse> call, Response<CarrinhoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Erro ao adicionar item: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<CarrinhoResponse> call, Throwable t) {
                callback.onError("Falha na rede: " + t.getMessage());
            }
        });
    }

    // ─── ALTERAR QUANTIDADE ──────────────────────────────────────────
    public void alterarQuantidade(Long idItem, int novaQuantidade, BaseCallback<CarrinhoResponse> callback) {
        JsonObject request = new JsonObject();
        request.addProperty("novaQuantidade", novaQuantidade);

        api.atualizarQuantidade(idItem, request).enqueue(new Callback<CarrinhoResponse>() {
            @Override
            public void onResponse(Call<CarrinhoResponse> call, Response<CarrinhoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Erro ao atualizar quantidade: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<CarrinhoResponse> call, Throwable t) {
                callback.onError("Falha na rede: " + t.getMessage());
            }
        });
    }

    // ─── REMOVER ITEM ────────────────────────────────────────────────
    public void removerItem(Long idItem, BaseCallback<CarrinhoResponse> callback) {
        api.removerItem(idItem).enqueue(new Callback<CarrinhoResponse>() {
            @Override
            public void onResponse(Call<CarrinhoResponse> call, Response<CarrinhoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Erro ao remover item: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<CarrinhoResponse> call, Throwable t) {
                callback.onError("Falha na rede: " + t.getMessage());
            }
        });
    }

    // ─── REALIZAR CHECKOUT ───────────────────────────────────────────
    public void realizarCheckout(CheckoutRequestDTO dto, BaseCallback<Void> callback) {
        api.realizarCheckout(dto).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError("Não foi possível finalizar o pedido (Erro " + response.code() + ").");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Falha na rede: " + t.getMessage());
            }
        });
    }
}