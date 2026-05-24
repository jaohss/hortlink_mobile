package com.example.hortlink.data.repository;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GeoRepository {
    // Cache em memória: CEP → [lat, lng]
    // Evita chamar a API duas vezes pro mesmo produtor
    private static final Map<String, double[]> cache = new HashMap<>();

    private final OkHttpClient http = new OkHttpClient();

    public interface CallbackCoordenadas {
        void onSuccess(double lat, double lng);
        void onError(String erro);
    }

    /**
     * Converte CEP em coordenadas geográficas.
     * Fluxo: ViaCEP (pega endereço) → Nominatim/OpenStreetMap (pega lat/lng)
     * Ambas as APIs são gratuitas e sem chave.
     */
    public void buscarCoordenadas(String cep, CallbackCoordenadas callback) {
        // CEP inválido ou vazio — não tenta
        if (cep == null || cep.replaceAll("[^0-9]", "").length() != 8) {
            callback.onError("CEP inválido: " + cep);
            return;
        }

        String cepLimpo = cep.replaceAll("[^0-9]", "");

        // Retorna do cache se já foi consultado antes
        if (cache.containsKey(cepLimpo)) {
            double[] coords = cache.get(cepLimpo);
            callback.onSuccess(coords[0], coords[1]);
            return;
        }

        new Thread(() -> {
            try {
                // 1. ViaCEP → logradouro para montar a query
                String viaCepUrl = "https://viacep.com.br/ws/" + cepLimpo + "/json/";
                Request viaCepReq = new Request.Builder().url(viaCepUrl).build();
                Response viaCepResp = http.newCall(viaCepReq).execute();

                if (!viaCepResp.isSuccessful()) {
                    callback.onError("ViaCEP falhou: " + viaCepResp.code());
                    return;
                }

                JSONObject viacep = new JSONObject(viaCepResp.body().string());
                if (viacep.optBoolean("erro", false)) {
                    callback.onError("CEP não encontrado");
                    return;
                }

                String logradouro = viacep.optString("logradouro", "");
                String bairro     = viacep.optString("bairro", "");
                String cidade     = viacep.optString("localidade", "");
                String estado     = viacep.optString("uf", "");

                // Monta query de busca para o Nominatim
                String query = (logradouro.isEmpty() ? bairro : logradouro)
                        + ", " + cidade + ", " + estado + ", Brasil";
                String encodedQuery = query.replace(" ", "+").replace(",", "%2C");

                // 2. Nominatim (OpenStreetMap) → lat/lng
                String nominatimUrl = "https://nominatim.openstreetmap.org/search"
                        + "?q=" + encodedQuery
                        + "&format=json&limit=1&countrycodes=br";

                Request nominatimReq = new Request.Builder()
                        .url(nominatimUrl)
                        .addHeader("User-Agent", "HortlinkApp/1.0") // obrigatório pelo Nominatim
                        .build();

                Response nominatimResp = http.newCall(nominatimReq).execute();

                if (!nominatimResp.isSuccessful()) {
                    callback.onError("Nominatim falhou: " + nominatimResp.code());
                    return;
                }

                JSONArray results = new JSONArray(nominatimResp.body().string());
                if (results.length() == 0) {
                    callback.onError("Nenhuma coordenada encontrada para: " + query);
                    return;
                }

                JSONObject lugar = results.getJSONObject(0);
                double lat = Double.parseDouble(lugar.getString("lat"));
                double lng = Double.parseDouble(lugar.getString("lon"));

                // Salva no cache
                cache.put(cepLimpo, new double[]{lat, lng});

                callback.onSuccess(lat, lng);

            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        }).start();
    }
}
