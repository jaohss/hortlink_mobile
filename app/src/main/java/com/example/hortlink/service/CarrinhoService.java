package com.example.hortlink.service;

import com.example.hortlink.data.dto.CheckoutRequestDTO;
import com.example.hortlink.data.model.CarrinhoResponse;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface CarrinhoService {

    @GET("carrinho")
    Call<CarrinhoResponse> obterCarrinho();

    @POST("carrinho/itens")
    Call<CarrinhoResponse> adicionarItem(@Body JsonObject request);

    @PUT("carrinho/itens/{idItem}")
    Call<CarrinhoResponse> atualizarQuantidade(
            @Path("idItem") Long idItem,
            @Body JsonObject request
    );

    @DELETE("carrinho/itens/{idItem}")
    Call<CarrinhoResponse> removerItem( @Path("idItem") Long idItem);

    @DELETE("carrinho")
    Call<Void> limparCarrinho();

    @POST("carrinho/checkout")
    Call<Void> realizarCheckout(@Body CheckoutRequestDTO requestDTO);
}
