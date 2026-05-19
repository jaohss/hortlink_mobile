package com.example.hortlink.service;

import com.example.hortlink.data.model.Produto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {
    @GET("oferta")
    Call<List<Produto>> getOfertas();
}
