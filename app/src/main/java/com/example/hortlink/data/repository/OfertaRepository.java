package com.example.hortlink.data.repository;

import com.example.hortlink.RetrofitClient;
import com.example.hortlink.data.dto.DetalheOfertaDTO;
import com.example.hortlink.data.dto.NovaOfertaDTO;
import com.example.hortlink.data.model.OfertaDTO;
import com.example.hortlink.data.remote.SupabaseClient;
import com.example.hortlink.entidades.BaseCallback;
import com.example.hortlink.service.OfertaService;

import org.json.JSONObject;

import java.util.List;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OfertaRepository {

    private final SupabaseClient client = SupabaseClient.getInstance();

    private final OfertaService api = RetrofitClient.getOfertaService();

    public interface OldCallback {
        void onSuccess(String response);
        void onError(String erro);
    }

    // ─── LISTAR todas as ofertas da API Spring ────────────────
    public void listarOfertas(BaseCallback<List<OfertaDTO>> callback) {
        api.getOfertas().enqueue(new Callback<List<OfertaDTO>>() {
            @Override
            public void onResponse(Call<List<OfertaDTO>> call, Response<List<OfertaDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Erro no servidor: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<OfertaDTO>> call, Throwable t) {
                callback.onError("Falha na rede: " + t.getMessage());
            }
        });
    }

    // CRIAR A OFERTA
    public void criarOferta(NovaOfertaDTO novaOferta, BaseCallback<OfertaDTO> callback) {
        api.criarOferta(novaOferta).enqueue(new Callback<OfertaDTO>() {
            @Override
            public void onResponse(Call<OfertaDTO> call, Response<OfertaDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // API retornou 201 Created (ou 200 OK) com os dados salvos
                    callback.onSuccess(response.body());
                } else {
                    // Tratar erros (ex: 400 Bad Request, 500 Server Error)
                    callback.onError("Erro ao salvar oferta: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<OfertaDTO> call, Throwable t) {
                callback.onError("Falha na rede: " + t.getMessage());
            }
        });

    }

    // EXCLUIR OFERTA PELO ID
    public void deletarOferta(Long idOferta, BaseCallback<Void> callback) {
        api.deletarOferta(idOferta).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if(response.isSuccessful()){
                    callback.onSuccess(null);
                } else {
                    callback.onError("Erro ao salvar oferta: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Falha na rede: " + t.getMessage());
            }
        });
    }

    public void buscarPorId(Long idOferta, BaseCallback<OfertaDTO> callback) {
        api.buscarPorId(idOferta).enqueue(new Callback<OfertaDTO>() {
            @Override
            public void onResponse(Call<OfertaDTO> call, Response<OfertaDTO> response){
                if(response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Erro ao buscar oferta: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<OfertaDTO> call, Throwable t) {
                callback.onError("Falha na rede: " + t.getMessage());
            }
        });
    }

    public void buscarOfertaDetalhadaPorId(Long idOferta, BaseCallback<DetalheOfertaDTO> callback) {
        api.buscarOfertaDetalhadaPorId(idOferta).enqueue(new Callback<DetalheOfertaDTO>() {
            @Override
            public void onResponse(Call<DetalheOfertaDTO> call, Response<DetalheOfertaDTO> response){
                if(response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Erro ao buscar oferta: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<DetalheOfertaDTO> call, Throwable t) {
                callback.onError("Falha na rede: " + t.getMessage());
            }
        });
    }

    public void buscarOfertasPorComercioId(Long comercioId, BaseCallback<List<OfertaDTO>> callback) {
        api.buscarOfertasDoComercio(comercioId).enqueue(new Callback<List<OfertaDTO>>() {
            @Override
            public void onResponse(Call<List<OfertaDTO>> call, Response<List<OfertaDTO>> response){
                if(response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Erro ao buscar oferta: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<OfertaDTO>> call, Throwable t) {
                callback.onError("Falha na rede: " + t.getMessage());
            }
        });
    }






















    // ─── INSERIR produto (Mantido) ─────────────────────────────────────────
    public void inserirOferta(String nome, String categoria, double preco,
                               String unidade, String descricao, String fotoUrl,
                               String produtorUid, BaseCallback callback) {
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
    public void listarProdutos(BaseCallback callback) {
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
    public void buscarProdutoPorId(String id, BaseCallback callback) {
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
                                 double preco, String fotoUrl, BaseCallback callback) {
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
    public void listarProdutosPorProdutor(String produtorUid, BaseCallback callback) {
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
    public void atualizarStatus(String id, boolean status, BaseCallback callback) {
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