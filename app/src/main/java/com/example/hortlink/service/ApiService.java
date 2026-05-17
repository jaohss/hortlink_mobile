package com.example.hortlink.service;

import com.example.hortlink.responses.OfertaResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {
    @GET("ofertas")
    Call<List<OfertaResponse>> getOfertas();
}
