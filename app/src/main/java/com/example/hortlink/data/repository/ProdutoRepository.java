package com.example.hortlink.data.repository;

import com.example.hortlink.bd.SupabaseHelper;
import com.example.hortlink.data.remote.SupabaseClient;

import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ProdutoRepository {

    private final SupabaseClient client = SupabaseClient.getInstance();

    public interface Callback {
        void onSuccess(String resultado);

        void onError(String erro);
    }

    // ─── INSERIR produto ─────────────────────────────────────────
    public void inserirProduto(String nome, String categoria, double preco,
                               String unidade, String descricao, String fotoUrl,
                               String produtorUid, Callback callback) {
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

                Response response = client.getHttp().newCall(request).execute();
                String respBody = response.body().string();

                if (response.isSuccessful()) callback.onSuccess(respBody);
                else callback.onError("Erro ao salvar: " + response.code() + " | " + respBody);

            } catch (Exception e) {
                callback.onError("Exceção: " + e.getMessage());
            }
        }).start();
    }

    // ─── LISTAR todos os produtos ────────────────────────────────
    public void listarProdutos(Callback callback) {
        new Thread(() -> {
            try {
                Request request = client
                        .baseRequest("/rest/v1/produtos?select=*&status=eq.true&order=criado_em.desc")
                        .get()
                        .build();

                Response response = client.getHttp().newCall(request).execute();
                String body = response.body().string();

                if (response.isSuccessful()) callback.onSuccess(body);
                else callback.onError("Erro ao listar: " + response.code());

            } catch (Exception e) {
                callback.onError("Exceção: " + e.getMessage());
            }
        }).start();
    }

    // ─── BUSCAR produto por ID ───────────────────────────────────
    public void buscarProdutoPorId(String id, Callback callback) {
        new Thread(() -> {
            try {
                Request request = client
                        .baseRequest("/rest/v1/produtos?id=eq." + id + "&select=*")
                        .get()
                        .build();

                Response response = client.getHttp().newCall(request).execute();
                String body = response.body().string();

                if (response.isSuccessful()) callback.onSuccess(body);
                else callback.onError("Erro " + response.code() + ": " + body);

            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        }).start();
    }

    // ─── Atualizar produto (PATCH) ───────────────────────────────────
    public void atualizarProduto(String id, String nome, String descricao,
                                 double preco, String fotoUrl, Callback callback) {
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

                Response response = client.getHttp().newCall(request).execute();
                String respBody = response.body().string();

                if (response.isSuccessful()) callback.onSuccess(respBody);
                else callback.onError("Erro " + response.code() + ": " + respBody);

            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        }).start();
    }

    // ─── Listar produtos por produtor (para o painel do produtor) ────
    public void listarProdutosPorProdutor(String produtorUid, Callback callback) {
        new Thread(() -> {
            try {
                Request request = client.baseRequest("/rest/v1/produtos?produtor_id=eq." + produtorUid + "&select=*&order=criado_em.desc")
                        .get()
                        .build();

                Response response = client.getHttp().newCall(request).execute();
                String body = response.body().string();

                if (response.isSuccessful()) callback.onSuccess(body);
                else callback.onError("Erro " + response.code() + ": " + body);

            } catch (Exception e) { callback.onError(e.getMessage()); }
        }).start();
    }

    // ─── ATIVAR / DESATIVAR produto (soft delete) ────────────────
    // Substitui completamente o deletarProduto().
    // Envia PATCH com {"ativo": true/false} — bate exatamente com
    // a coluna "ativo boolean" da tabela produtos no Supabase.
    //
    // CORREÇÃO CRÍTICA: o campo enviado era "status" (nome errado).
    // O Supabase ignorava silenciosamente e nunca atualizava nada.
    public void atualizarStatus(String id, boolean status, Callback callback) {
        new Thread(() -> {
            try {
                JSONObject json = new JSONObject();
                json.put("status", status); // ← era "status", agora correto

                RequestBody body = RequestBody.create(
                        json.toString(), MediaType.parse("application/json"));

                Request request = client.baseRequest("/rest/v1/produtos?id=eq." + id)
                        .addHeader("Prefer", "return=minimal")
                        .patch(body)
                        .build();

                Response response = client.getHttp().newCall(request).execute();
                String respBody = response.body().string();

                if (response.isSuccessful()) callback.onSuccess("ok");
                else callback.onError("Erro " + response.code() + ": " + respBody);

            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        }).start();
    }

    // Adicione no ProdutoRepository existente
    public void verificarStatus(String produtoId, Callback callback) {
        new Thread(() -> {
            try {
                String path = "/rest/v1/produtos"
                        + "?select=id"
                        + "&id=eq." + produtoId
                        + "&status=eq.true";

                Request request = client.baseRequest(path)
                        .addHeader("Accept", "application/json")
                        .get()
                        .build();

                Response response = client.getHttp().newCall(request).execute();
                String body = response.body().string();

                if (response.isSuccessful()) callback.onSuccess(body);
                else callback.onError("Erro " + response.code());

            } catch (Exception e) { callback.onError(e.getMessage()); }
        }).start();
    }
}