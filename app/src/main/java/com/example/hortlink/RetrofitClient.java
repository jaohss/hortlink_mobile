package com.example.hortlink;

import com.example.hortlink.service.ComercioService;
import com.example.hortlink.service.OfertaService;
import com.example.hortlink.service.ProdutoService;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "http://10.0.2.2:8081/";
    private static Retrofit retrofit = null;

    private static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl("URL_DA_SUA_API_SPRING/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    // ─── Métodos para instanciar cada serviço específico ────────────────

    public static OfertaService getOfertaService() {
        return getClient().create(OfertaService.class);
    }

    public static ProdutoService getProdutoService() {
        return getClient().create(ProdutoService.class);
    }

    public static ComercioService getComercioService() {
        return getClient().create(ComercioService.class);
    }
}
