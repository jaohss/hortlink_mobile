package com.example.hortlink.data.repository;

import com.example.hortlink.data.model.Pedido;
import com.example.hortlink.service.BaseCallback;
import com.example.hortlink.service.PedidoService;
import com.example.hortlink.util.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PedidoRepository {

    private final PedidoService api;

    public PedidoRepository() { this.api = RetrofitClient.getPedidoService(); }

    public void obterPedidosPorComercio(BaseCallback<List<Pedido>> callback) {
        api.obterPedidosPorComercio().enqueue(new Callback<List<Pedido>>() {
            @Override
            public void onResponse(Call<List<Pedido>> call, Response<List<Pedido>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Erro ao carregar pedidos: " + response.code());
                }
            }
            @Override
            public void onFailure(Call<List<Pedido>> call, Throwable t) {
                callback.onError("Falha na rede: " + t.getMessage());
            }
        });

    }

    public void atualizarStatus(String id, String novoStatus, BaseCallback<Void> callback) {
        api.atualizarStatus(id, novoStatus.toUpperCase()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError("Erro ao atualizar status: " + response.code());
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Falha na rede: " + t.getMessage());
            }
        });
    }
}