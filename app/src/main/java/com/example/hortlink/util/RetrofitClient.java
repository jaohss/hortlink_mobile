package com.example.hortlink.util;

import com.example.hortlink.service.AuthService;
import com.example.hortlink.service.CarrinhoService;
import com.example.hortlink.service.ComercioService;
import com.example.hortlink.service.OfertaService;
import com.example.hortlink.service.ProdutoService;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = "http://10.0.2.2:8081/";
    private static Retrofit retrofit = null;

    // Removemos o parâmetro Context daqui!
    private static Retrofit getClient() {
        if (retrofit == null) {

            Interceptor authInterceptor = chain -> {
                Request originalRequest = chain.request();

                // MÁGICA AQUI: O Retrofit pega o Context sozinho e pede o Token!
                String token = SessionManager.getInstance().getToken();

                if (token != null && !token.isEmpty()) {
                    Request newRequest = originalRequest.newBuilder()
                            .header("Authorization", "Bearer " + token)
                            .build();
                    return chain.proceed(newRequest);
                }
                return chain.proceed(originalRequest);
            };

            // 1. Cria o espião de logs
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            // LEVEL.BODY mostra TUDO: cabeçalhos, rotas e o JSON de resposta
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            // 2. Adiciona ele no construtor do OkHttp
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(authInterceptor) // O seu interceptor de Token que já fizemos
                    .addInterceptor(loggingInterceptor) // <--- ADICIONE ESTA LINHA AQUI
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    // ─── TUDO VOLTA A SER LIMPO E SIMPLES ───

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

    public static AuthService getAuthService() {
        return getClient().create(AuthService.class);
    }
}