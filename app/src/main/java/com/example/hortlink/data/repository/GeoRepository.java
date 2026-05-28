package com.example.hortlink.data.repository;

import com.example.hortlink.data.dto.CoordenadasDTO;
import com.example.hortlink.data.dto.ViaCepResponse;
import com.example.hortlink.service.BaseCallback;
import com.example.hortlink.service.GeoService;
import com.example.hortlink.util.RetrofitClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GeoRepository {

    // Cache em memória para evitar chamadas repetidas na API
    private static final Map<String, CoordenadasDTO> cache = new HashMap<>();

    private final GeoService api;

    public GeoRepository() {
        this.api = RetrofitClient.getGeoService();
    }

    public void buscarCoordenadas(String cep, BaseCallback<CoordenadasDTO> callback) {
        if (cep == null || cep.replaceAll("[^0-9]", "").length() != 8) {
            callback.onError("CEP inválido.");
            return;
        }

        String cepLimpo = cep.replaceAll("[^0-9]", "");

        // 1. Retorna do cache se já existe
        if (cache.containsKey(cepLimpo)) {
            callback.onSuccess(cache.get(cepLimpo));
            return;
        }

        // 2. Chama a API do ViaCEP
        api.buscarCep(cepLimpo).enqueue(new Callback<ViaCepResponse>() {
            @Override
            public void onResponse(Call<ViaCepResponse> call, Response<ViaCepResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onError("Erro na consulta do CEP: " + response.code());
                    return;
                }

                ViaCepResponse viaCep = response.body();

                if (viaCep.getErro()) {
                    callback.onError("CEP não encontrado.");
                    return;
                }

                // Monta a frase de pesquisa
                String logradouro = viaCep.getLogradouro() != null ? viaCep.getLogradouro() : "";
                String bairro = viaCep.getBairro() != null ? viaCep.getBairro() : "";
                String cidade = viaCep.getLocalidade() != null ? viaCep.getLocalidade() : "";
                String estado = viaCep.getUf() != null ? viaCep.getUf() : "";

                String query = (logradouro.isEmpty() ? bairro : logradouro) + ", " + cidade + ", " + estado + ", Brasil";

                // 3. Chama o Nominatim usando a query montada
                buscarLatLonNoNominatim(query, cepLimpo, callback);
            }

            @Override
            public void onFailure(Call<ViaCepResponse> call, Throwable t) {
                callback.onError("Falha na rede (ViaCEP): " + t.getMessage());
            }
        });
    }

    private void buscarLatLonNoNominatim(String query, String cepLimpo, BaseCallback<CoordenadasDTO> callback) {
        api.buscarCoordenadas(query).enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onError("Erro na consulta de localização: " + response.code());
                    return;
                }

                JsonArray results = response.body();
                if (results.isEmpty()) {
                    callback.onError("Nenhuma coordenada encontrada para o endereço informado.");
                    return;
                }

                try {
                    // Lê o JSON genérico diretamente (sem precisar de DTO para o Nominatim)
                    JsonObject lugar = results.get(0).getAsJsonObject();
                    double lat = lugar.get("lat").getAsDouble();
                    double lng = lugar.get("lon").getAsDouble();

                    CoordenadasDTO coords = new CoordenadasDTO(lat, lng);
                    cache.put(cepLimpo, coords); // Salva no cache
                    callback.onSuccess(coords);

                } catch (Exception e) {
                    callback.onError("Erro ao ler as coordenadas recebidas.");
                }
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                callback.onError("Falha na rede (Nominatim): " + t.getMessage());
            }
        });
    }

    public void buscarEnderecoPorCep(String cep, BaseCallback<ViaCepResponse> callback) {
        String cepLimpo = cep.replaceAll("[^0-9]", "");

        api.buscarCep(cepLimpo).enqueue(new Callback<ViaCepResponse>() {
            @Override
            public void onResponse(Call<ViaCepResponse> call, Response<ViaCepResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ViaCepResponse viaCep = response.body();

                    if (viaCep.getErro()) {
                        callback.onError("CEP não encontrado.");
                    } else {
                        callback.onSuccess(viaCep); // Devolve o DTO com todos os textos!
                    }
                } else {
                    callback.onError("Erro na consulta do CEP: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ViaCepResponse> call, Throwable t) {
                callback.onError("Falha na rede (ViaCEP): " + t.getMessage());
            }
        });
    }
}