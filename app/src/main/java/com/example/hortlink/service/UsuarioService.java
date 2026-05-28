package com.example.hortlink.service;

import com.example.hortlink.data.dto.PerfilCompradorDTO;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;

public interface UsuarioService {

    @GET("perfil")
    Call<PerfilCompradorDTO> obterPerfil();

    @PUT("perfil")
    Call<Void> atualizarPerfil(@Body PerfilCompradorDTO dto);
}
