package com.example.hortlink.service;

import com.example.hortlink.data.dto.DetalheOfertaDTO;
import com.example.hortlink.data.dto.NovaOfertaDTO;
import com.example.hortlink.data.dto.OfertaEdicaoDTO;
import com.example.hortlink.data.model.OfertaDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface OfertaService {
    @GET("oferta")
    Call<List<OfertaDTO>> getOfertas();

    @POST("oferta/salvar")
    Call<OfertaDTO> criarOferta(@Body NovaOfertaDTO novaOferta);

    @PUT("oferta/atualizar/{id}")
    Call<OfertaDTO> atualizarOferta(@Path("id") Long idOferta, @Body NovaOfertaDTO dto);

    @DELETE("oferta/{id}")
    Call<Void> deletarOferta(@Path("id") Long idOferta);

    @GET("oferta/{id}")
    Call<OfertaDTO> buscarPorId(@Path("id") Long idOferta);

    @GET("oferta/comercio/{comercioId}")
    Call<List<OfertaDTO>> buscarOfertasDoComercio(@Path("comercioId") Long comercioId);

    @GET("oferta/detalhes/{id}")
    Call<DetalheOfertaDTO> buscarOfertaDetalhadaPorId(@Path("id") Long idOferta);

    @GET("oferta/edicao/{id}")
    Call<OfertaEdicaoDTO> buscarOfertaEdicaoPorId(@Path("id") Long idOferta);
}
