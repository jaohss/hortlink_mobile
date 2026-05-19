package com.example.hortlink.data.repository;

import com.example.hortlink.RetrofitClient;
import com.example.hortlink.data.model.Produto;
import com.example.hortlink.data.remote.SupabaseClient;

import org.json.JSONObject;

import java.util.List;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProdutoRepository {

    private final SupabaseClient client = SupabaseClient.getInstance();

    // Novo Callback focado em tipagem para a sua nova rota Spring Boot
    public interface OnlineCallback {
        void onSuccess(List<Produto> produtos);
        void onError(String erro);
    }

    // Mantido o Callback antigo com String para não quebrar nenhuma tela anterior do app
    public interface OldCallback {
        void onSuccess(String response);
        void onError(String erro);
    }

    // ─── [NOVO] LISTAR todas as ofertas da API Spring ────────────────
    public void listarOfertas(OnlineCallback callback) {
        RetrofitClient.getApiService().getOfertas().enqueue(new Callback<List<Produto>>() {
            @Override
            public void onResponse(Call<List<Produto>> call, Response<List<Produto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Erro no servidor: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Produto>> call, Throwable t) {
                callback.onError("Falha na rede: " + t.getMessage());
            }
        });
    }

    // ─── INSERIR produto (Mantido) ─────────────────────────────────────────
    public void inserirProduto(String nome, String categoria, double preco,
                               String unidade, String descricao, String fotoUrl,
                               String produtorUid, OldCallback callback) {
        new Thread(() -> {
            try {
                JSONObject json = new JSONObject();
                json.put("nome", nome);
                json.put("categoria", categoria);
                json.put("preco", preco);
                json.put("unidade", unidade);
                json.put("descricao", descricao);
                json.put("foto_url", fotoUrl);
                json.put("produtor_id", produtorUid);

                RequestBody body = RequestBody.create(
                        json.toString(), MediaType.parse("application/json"));

                Request request = client.baseRequest("/rest/v1/produtos")
                        .addHeader("Prefer", "return=representation")
                        .post(body)
                        .build();

                okhttp3.Response response = client.getHttp().newCall(request).execute();
                ResponseBody responseBody = response.body();
                String respBody = responseBody != null ? responseBody.string() : "";

                if (response.isSuccessful()) callback.onSuccess(respBody);
                else callback.onError("Erro ao salvar: " + response.code() + " | " + respBody);

            } catch (Exception e) {
                callback.onError("Exceção: " + e.getMessage());
            }
        }).start();
    }

    // ─── LISTAR todos os produtos (Mantido) ────────────────────────────────
    public void listarProdutos(OldCallback callback) {
        new Thread(() -> {
            try {
                Request request = client
                        .baseRequest("/rest/v1/produtos?select=*&status=eq.true&order=criado_em.desc")
                        .get()
                        .build();

                okhttp3.Response response = client.getHttp().newCall(request).execute();
                ResponseBody responseBody = response.body();
                String body = responseBody != null ? responseBody.string() : "";

                if (response.isSuccessful()) callback.onSuccess(body);
                else callback.onError("Erro ao listar: " + response.code());

            } catch (Exception e) {
                callback.onError("Exceção: " + e.getMessage());
            }
        }).start();
    }

    // ─── BUSCAR produto por ID (Mantido) ───────────────────────────────────
    public void buscarProdutoPorId(String id, OldCallback callback) {
        new Thread(() -> {
            try {
                Request request = client
                        .baseRequest("/rest/v1/produtos?id=eq." + id + "&select=*")
                        .get()
                        .build();

                okhttp3.Response response = client.getHttp().newCall(request).execute();
                ResponseBody responseBody = response.body();
                String body = responseBody != null ? responseBody.string() : "";

                if (response.isSuccessful()) callback.onSuccess(body);
                else callback.onError("Erro " + response.code() + ": " + body);

            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        }).start();
    }

    // ─── Atualizar produto (PATCH) (Mantido) ───────────────────────────────────
    public void atualizarProduto(String id, String nome, String descricao,
                                 double preco, String fotoUrl, OldCallback callback) {
        new Thread(() -> {
            try {
                JSONObject json = new JSONObject();
                json.put("nome", nome);
                json.put("descricao", descricao);
                json.put("preco", preco);
                if (fotoUrl != null && !fotoUrl.isEmpty()) {
                    json.put("foto_url", fotoUrl);
                }

                RequestBody body = RequestBody.create(
                        json.toString(), MediaType.parse("application/json"));

                Request request = client.baseRequest("/rest/v1/produtos?id=eq." + id)
                        .addHeader("Prefer", "return=representation")
                        .patch(body)
                        .build();

                okhttp3.Response response = client.getHttp().newCall(request).execute();
                ResponseBody responseBody = response.body();
                String respBody = responseBody != null ? responseBody.string() : "";

                if (response.isSuccessful()) callback.onSuccess(respBody);
                else callback.onError("Erro " + response.code() + ": " + respBody);

            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        }).start();
    }

    // ─── Listar produtos por produtor (Mantido) ────
    public void listarProdutosPorProdutor(String produtorUid, OldCallback callback) {
        new Thread(() -> {
            try {
                Request request = client.baseRequest("/rest/v1/produtos?produtor_id=eq." + produtorUid + "&select=*&order=criado_em.desc")
                        .get()
                        .build();

                okhttp3.Response response = client.getHttp().newCall(request).execute();
                ResponseBody responseBody = response.body();
                String body = responseBody != null ? responseBody.string() : "";

                if (response.isSuccessful()) callback.onSuccess(body);
                else callback.onError("Erro " + response.code() + ": " + body);

            } catch (Exception e) { callback.onError(e.getMessage()); }
        }).start();
    }

    // ─── ATIVAR / DESATIVAR produto (Mantido) ────────────────
    public void atualizarStatus(String id, boolean status, OldCallback callback) {
        new Thread(() -> {
            try {
                JSONObject json = new JSONObject();
                json.put("status", status);

                RequestBody body = RequestBody.create(
                        json.toString(), MediaType.parse("application/json"));

                Request request = client.baseRequest("/rest/v1/produtos?id=eq." + id)
                        .addHeader("Prefer", "return=minimal")
                        .patch(body)
                        .build();

                okhttp3.Response response = client.getHttp().newCall(request).execute();
                ResponseBody responseBody = response.body();
                String respBody = responseBody != null ? responseBody.string() : "";

                if (response.isSuccessful()) callback.onSuccess("ok");
                else callback.onError("Erro " + response.code() + ": " + respBody);

            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        }).start();
    }
}