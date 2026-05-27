package com.example.hortlink.service;

import com.example.hortlink.data.dto.ComercioDTO;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ComercioService {

    @GET("comercio/{id}")
    Call<ComercioDTO> buscarPorId(@Path("id") Long comercioId);

    @DELETE("comercio/{id}")
    Call<Void> deletarComercio(@Path("id") Long idComercio);


}
