package com.example.hortlink.service;

import com.example.hortlink.data.dto.ViaCepResponse;
import com.google.gson.JsonArray;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface GeoService {

    // Rota 1: Busca o endereço pelo CEP
    @GET("{cep}/json/")
    Call<ViaCepResponse> buscarCep(@Path("cep") String cep);

    // Rota 2: Busca as coordenadas (Parâmetros fixos ficam na URL e no Header)
    @Headers("User-Agent: HortlinkApp/1.0")
    @GET("https://nominatim.openstreetmap.org/search?format=json&limit=1&countrycodes=br")
    Call<JsonArray> buscarCoordenadas(@Query("q") String query);
}
