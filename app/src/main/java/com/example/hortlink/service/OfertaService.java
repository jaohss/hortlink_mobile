package com.example.hortlink.service;

import com.example.hortlink.data.dto.DetalheOfertaDTO;
import com.example.hortlink.data.dto.NovaOfertaDTO;
import com.example.hortlink.data.model.OfertaDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface OfertaService {
    @GET("oferta")
    Call<List<OfertaDTO>> getOfertas();

    @POST("oferta/salvar")
    Call<OfertaDTO> criarOferta(@Body NovaOfertaDTO novaOferta);

    @DELETE("oferta/{id}")
    Call<Void> deletarOferta(@Path("id") Long idOferta);

    @GET("oferta/{id}")
    Call<OfertaDTO> buscarPorId(@Path("id") Long idOferta);

    @GET("oferta/detalhes/{id}")
    Call<DetalheOfertaDTO> buscarOfertaDetalhadaPorId(@Path("id") Long idOferta);
}
