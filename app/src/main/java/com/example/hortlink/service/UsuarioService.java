package com.example.hortlink.service;

import com.example.hortlink.data.dto.PerfilCompradorDTO;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface UsuarioService {

    @GET("usuarios/perfil")
    Call<PerfilCompradorDTO> obterPerfil();

    @PUT("usuarios/perfil")
    Call<Void> atualizarPerfil(@Body PerfilCompradorDTO dto);

    @GET("perfil/detalhes-cliente/{clienteId}")
    Call<PerfilCompradorDTO> obterDetalhesCliente(@Path("clienteId") Long clienteId);
}
