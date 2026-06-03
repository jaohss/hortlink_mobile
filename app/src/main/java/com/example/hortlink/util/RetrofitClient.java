package com.example.hortlink.util;

import com.example.hortlink.service.AuthService;
import com.example.hortlink.service.CarrinhoService;
import com.example.hortlink.service.ComercioService;
import com.example.hortlink.service.GeoService;
import com.example.hortlink.service.OfertaService;
import com.example.hortlink.service.PedidoService;
import com.example.hortlink.service.ProdutoService;
import com.example.hortlink.service.UsuarioService;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    //private static final String BASE_URL = "http://10.0.2.2:8081/";
    private static final String BASE_URL = "https://hortlink-api.ashymushroom-34804de4.brazilsouth.azurecontainerapps.io/";
    private static final String BASE_URL_VIACEP = "https://viacep.com.br/ws/"; // <-- URL do ViaCEP adicionada

    private static Retrofit retrofit = null;
    private static Retrofit retrofitViaCep = null; // <-- Instância separada para o ViaCEP

    // ─── CLIENTE DO SEU BACKEND (COM TOKEN) ───
    private static Retrofit getClient() {
        if (retrofit == null) {

            Interceptor authInterceptor = chain -> {
                Request originalRequest = chain.request();

                String token = SessionManager.getInstance().getToken();

                if (token != null && !token.isEmpty()) {
                    Request newRequest = originalRequest.newBuilder()
                            .header("Authorization", "Bearer " + token)
                            .build();
                    return chain.proceed(newRequest);
                }
                return chain.proceed(originalRequest);
            };

            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(authInterceptor)
                    .addInterceptor(loggingInterceptor)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    // ─── CLIENTE DO VIACEP (SEM TOKEN) ───
    private static Retrofit getViaCepClient() {
        if (retrofitViaCep == null) {

            // Mantemos o log para você poder debugar, mas NÃO adicionamos o authInterceptor
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient clientLimpo = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .build();

            retrofitViaCep = new Retrofit.Builder()
                    .baseUrl(BASE_URL_VIACEP)
                    .client(clientLimpo)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofitViaCep;
    }


    // ─── SERVIÇOS DO SEU BACKEND ───

    public static OfertaService getOfertaService() {
        return getClient().create(OfertaService.class);
    }

    public static ProdutoService getProdutoService() {
        return getClient().create(ProdutoService.class);
    }

    public static ComercioService getComercioService() {
        return getClient().create(ComercioService.class);
    }

    public static CarrinhoService getCarrinhoService() {
        return getClient().create(CarrinhoService.class);
    }

    public static UsuarioService getUsuarioService() {
        return getClient().create(UsuarioService.class);
    }

    public static PedidoService getPedidoService() {
        return getClient().create(PedidoService.class);
    }

    public static AuthService getAuthService() {
        return getClient().create(AuthService.class);
    }

    // ─── SERVIÇO EXTERNO (VIACEP) ───

    public static GeoService getGeoService() {
        return getViaCepClient().create(GeoService.class);
    }
}