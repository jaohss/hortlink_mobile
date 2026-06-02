package com.example.hortlink.service;

import com.example.hortlink.data.dto.ComercioDTO;
import com.example.hortlink.data.dto.CompletarPerfilComercioDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ComercioService {

    @GET("comercio")
    Call<ComercioDTO> obterPerfilBase();

    @GET("comercio/{id}")
    Call<ComercioDTO> obterDadosComercio(@Path("id") Long idComercio);

    @GET("comercio/detalhes")
    Call<CompletarPerfilComercioDTO> buscarPerfilForm();

    @DELETE("comercio/{id}")
    Call<Void> deletarComercio(@Path("id") Long idComercio);

    @POST("comercio/completar-perfil")
    Call<Void> completarPerfilComercio(@Body CompletarPerfilComercioDTO dto);

    @GET("comercio/{cidade}")
    Call<List<ComercioDTO>> listarPorCidade(@Path("cidade") String cidade);


}
