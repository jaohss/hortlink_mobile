package com.example.hortlink.service;

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

    @GET("{compradorId}/carrinho")
    Call<CarrinhoResponse> obterCarrinho(@Path("compradorId") Long compradorId);

    @POST("{compradorId}/carrinho/itens")
    Call<CarrinhoResponse> adicionarItem(
            @Path("compradorId") Long compradorId,
            @Body JsonObject request
    );

    @PUT("{compradorId}/carrinho/itens/{idItem}")
    Call<CarrinhoResponse> atualizarQuantidade(
            @Path("compradorId") Long compradorId,
            @Path("idItem") Long idItem,
            @Body JsonObject request
    );

    @DELETE("{compradorId}/carrinho/itens/{idItem}")
    Call<CarrinhoResponse> removerItem(
            @Path("compradorId") Long compradorId,
            @Path("idItem") Long idItem
    );

    @DELETE("{compradorId}/carrinho")
    Call<Void> limparCarrinho(@Path("compradorId") Long compradorId);
}
