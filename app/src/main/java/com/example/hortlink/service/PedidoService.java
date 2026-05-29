package com.example.hortlink.service;

import com.example.hortlink.data.model.Pedido;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface PedidoService {

    @GET("pedidos/comercio")
    Call<List<Pedido>> obterPedidosPorComercio();

    @PATCH("pedidos/{id}/status")
    Call<Void> atualizarStatus(@Path("id") String id, @Query("status") String novoStatus);
}
