package com.example.hortlink.service;

import com.example.hortlink.data.dto.AuthRequest;
import com.example.hortlink.data.dto.AuthResponse;
import com.example.hortlink.data.dto.RegistroDTO;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface AuthService {

    @POST("auth/login")
    Call<AuthResponse> login(@Body AuthRequest request);

    @POST("auth/register")
    Call<Void> register(@Body RegistroDTO usuario);

    @GET("auth/verify")
    Call<Boolean> verificarEmail(@Query("email") String email);
}
