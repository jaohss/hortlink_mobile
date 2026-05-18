package com.example.hortlink.data.remote;

import android.content.Context;
import android.net.Uri;

import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class StorageHelper {

    private static final String BUCKET = "produtos";
    private final SupabaseClient client = SupabaseClient.getInstance();
    private final Context context;

    public interface Callback {
        void onSuccess(String urlPublica);
        void onError(String erro);
    }

    public StorageHelper(Context context) {
        this.context = context;
    }

    public void uploadImagem(Uri uri, String nomeArquivo, Callback callback) {
        new Thread(() -> {
            try {
                InputStream inputStream = context.getContentResolver().openInputStream(uri);
                byte[] bytes = inputStream.readAllBytes();
                inputStream.close();

                String path = "fotos/" + nomeArquivo;
                String uploadPath = "/storage/v1/object/" + BUCKET + "/" + path;

                RequestBody body = RequestBody.create(bytes, MediaType.parse("image/jpeg"));

                Request request = new Request.Builder()
                        .url(client.getStorageUrl().replace("/storage/v1", "") + uploadPath)
                        .addHeader("Authorization", "Bearer " + getKey())
                        .addHeader("apikey", getKey())
                        .addHeader("Content-Type", "image/jpeg")
                        .addHeader("x-upsert", "true")
                        .post(body)
                        .build();

                Response response = client.getHttp().newCall(request).execute();

                if (response.isSuccessful()) {
                    String urlPublica = client.getStorageUrl().replace("/storage/v1", "")
                            + "/storage/v1/object/public/" + BUCKET + "/" + path;
                    callback.onSuccess(urlPublica);
                } else {
                    String erro = response.body() != null ? response.body().string() : "sem detalhes";
                    callback.onError("Erro " + response.code() + ": " + erro);
                }

            } catch (Exception e) {
                callback.onError("Exceção: " + e.getMessage());
            }
        }).start();
    }

    // Pega a key do SupabaseClient via reflexão seria ruim — melhor expor direto
    private String getKey() {
        // SupabaseClient já monta o header Authorization internamente no baseRequest,
        // mas o storage não usa baseRequest pois o Content-Type é image/jpeg, não json.
        // Por isso precisamos da key aqui. Exponha um getter no SupabaseClient:
        return SupabaseClient.getInstance().getKey();
    }
}