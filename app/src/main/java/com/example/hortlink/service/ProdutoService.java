package com.example.hortlink.service;

import com.example.hortlink.data.dto.ProdutoListaDTO;
import com.example.hortlink.data.model.Produto;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ProdutoService {

    @Multipart
    @POST("produto/salvar")
    Call<Produto> cadastrarProduto(
            @Part("produto") RequestBody produtoData,
            @Part MultipartBody.Part imagem
    );

    @DELETE("produto/{id}")
    Call<Void> deletarProduto(@Path("id") Long idProduto);

    @GET("produto/{id}")
    Call<Produto> buscarPorId(@Path("id") Long idProduto);

    @Multipart
    @PUT("produto/{id}")
    Call<Produto> atualizarProduto(
            @Path("id") Long id,
            @Part("produto") RequestBody produtoData,
            @Part MultipartBody.Part imagem
    );

    @GET("produto")
    Call<List<ProdutoListaDTO>> listarPorComercio();

    @GET("produto/sem-oferta")
    Call<List<ProdutoListaDTO>> obterProdutosSemOfertaAtiva();
}
