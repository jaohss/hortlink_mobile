package com.example.hortlink.services;


import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Serviço isolado para consulta de endereço via ViaCEP.
 *
 * Responsabilidade única: consultar a API pública e devolver os dados
 * de cidade e estado. Não conhece Supabase, Firebase, nem Android.
 *
 * Uso:
 *   ViaCepService.buscar("01310100", new ViaCepService.Callback() {
 *       public void onSuccess(String cidade, String estado) { ... }
 *       public void onError(String motivo) { ... }
 *   });
 */
public class ViacepService {

    private static final String BASE_URL = "https://viacep.com.br/ws/%s/json/";

    // Sem estado — pode ser instanciado ou usado via método estático.
    private static final OkHttpClient HTTP = new OkHttpClient();

    // ─── Callback ─────────────────────────────────────────────────────

    public interface Callback {
        /** Chamado quando a API retorna cidade e estado válidos. */
        void onSuccess(String bairo, String cidade, String estado);

        /**
         * Chamado quando:
         * - CEP tem formato inválido (não numérico ou tamanho errado)
         * - API retornou o campo "erro": true
         * - Falha de rede
         */
        void onError(String motivo);
    }

    // ─── Consulta principal ───────────────────────────────────────────

    /**
     * Busca cidade e estado para o CEP informado.
     * Sempre executa em background thread — o callback é chamado
     * na thread de trabalho; quem usar deve fazer runOnUiThread se necessário.
     *
     * @param cepBruto CEP digitado pelo usuário (com ou sem máscara)
     * @param callback resultado ou erro
     */
    public static void buscar(String cepBruto, Callback callback) {
        String cep = normalizar(cepBruto);

        if (!isValido(cep)) {
            callback.onError("CEP inválido. Informe 8 dígitos numéricos.");
            return;
        }

        new Thread(() -> {
            try {
                String url = String.format(BASE_URL, cep);

                Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .build();

                Response response = HTTP.newCall(request).execute();

                if (!response.isSuccessful()) {
                    callback.onError("Falha na consulta do CEP (código " + response.code() + ").");
                    return;
                }

                String body = response.body().string();
                JSONObject json = new JSONObject(body);

                // ViaCEP retorna { "erro": true } quando o CEP não existe
                if (json.optBoolean("erro", false)) {
                    callback.onError("CEP não encontrado. Verifique e tente novamente.");
                    return;
                }

                String bairro = json.optString("bairro", "").trim();
                String cidade = json.optString("localidade", "").trim();
                String estado = json.optString("uf", "").trim();

                if (cidade.isEmpty() || estado.isEmpty()) {
                    callback.onError("CEP encontrado, mas dados incompletos.");
                    return;
                }

                callback.onSuccess(bairro, cidade, estado);

            } catch (Exception e) {
                callback.onError("Erro de rede: " + e.getMessage());
            }
        }).start();
    }

    // ─── Helpers privados ─────────────────────────────────────────────

    /** Remove hífen e espaços — aceita "01310-100" e "01310100". */
    private static String normalizar(String cep) {
        if (cep == null) return "";
        return cep.replaceAll("[^0-9]", "");
    }

    /** CEP válido tem exatamente 8 dígitos numéricos. */
    private static boolean isValido(String cep) {
        return cep != null && cep.matches("\\d{8}");
    }
}
