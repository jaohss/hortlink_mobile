package com.example.hortlink.bd;

import android.content.Context;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class SupabaseHelper {

    private static final String SUPABASE_URL = "https://dzfbtevidnfarlpnfysd.supabase.co";
    private static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImR6ZmJ0ZXZpZG5mYXJscG5meXNkIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzgyNTcwNzMsImV4cCI6MjA5MzgzMzA3M30.79uc9zT_T-HPoUhJMMyUMKsW4qS2kiCHuuBcpmv3sDQ";
    private static final String BUCKET_NAME = "produtos";

    private final OkHttpClient client = new OkHttpClient();
    private final Context context;

    public SupabaseHelper(Context context) {
        this.context = context;
    }

    // ─── Interface de callback ───────────────────────────────────
    public interface SupabaseCallback {
        void onSuccess(String resultado);
        void onError(String erro);
    }

    // ─── Helper interno: lê body sem NPE ─────────────────────────────
    // response.body() pode ser null em respostas 204 ou em erros de rede.
    // Centraliza o tratamento para não repetir em cada método.
    private String lerBody(Response response) {
        ResponseBody body = response.body();
        if (body == null) return "[]"; // retorna array vazio seguro para JSON parsing
        try {
            return body.string();
        } catch (IOException e) {
            return "[]";
        }
    }

    // ─── 1. UPLOAD de imagem no Storage ─────────────────────────
    public void uploadImagem(Uri uri, String nomeArquivo, SupabaseCallback callback) {
        new Thread(() -> {
            try {
                InputStream inputStream = context.getContentResolver().openInputStream(uri);
                byte[] bytes = inputStream.readAllBytes();
                inputStream.close();

                String path = "fotos/" + nomeArquivo;
                String url  = SUPABASE_URL + "/storage/v1/object/" + BUCKET_NAME + "/" + path;

                RequestBody body = RequestBody.create(bytes, MediaType.parse("image/jpeg"));

                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                        .addHeader("apikey", SUPABASE_KEY)
                        .addHeader("Content-Type", "image/jpeg")
                        .addHeader("x-upsert", "true")   // ← adicionar esta linha
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();

                if (response.isSuccessful()) {
                    // Monta URL pública da imagem
                    String urlPublica = SUPABASE_URL + "/storage/v1/object/public/"
                            + BUCKET_NAME + "/" + path;
                    callback.onSuccess(urlPublica);
                } else {
                    callback.onError("Erro no upload: " + response.code() + " " + response.body().string());
                }

            } catch (Exception e) {
                callback.onError("Exceção: " + e.getMessage());
            }
        }).start();
    }

    // ─── 2. INSERIR produto no banco ─────────────────────────────
    public void inserirProduto(String nome, String categoria, double preco,
                               String unidade, String descricao, String fotoUrl,
                               String produtorUid, SupabaseCallback callback) {
        new Thread(() -> {
            try {
                JSONObject json = new JSONObject();
                json.put("nome",         nome);
                json.put("categoria",    categoria);
                json.put("preco",        preco);
                json.put("unidade",      unidade);
                json.put("descricao",    descricao);
                json.put("foto_url",     fotoUrl);
                json.put("produtor_id", produtorUid);

                RequestBody body = RequestBody.create(
                        json.toString(), MediaType.parse("application/json"));

                Request request = new Request.Builder()
                        .url(SUPABASE_URL + "/rest/v1/produtos")
                        .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                        .addHeader("apikey", SUPABASE_KEY)
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Prefer", "return=representation")
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();
                String respBody   = response.body().string();

                if (response.isSuccessful()) {
                    callback.onSuccess(respBody);
                } else {
                    callback.onError("Erro ao salvar: " + response.code() + " | " + respBody);
                }

            } catch (Exception e) {
                callback.onError("Exceção: " + e.getMessage());
            }
        }).start();
    }

    // ─── 3. LISTAR produtos ──────────────────────────────────────
    public void listarProdutos(SupabaseCallback callback) {
        new Thread(() -> {
            try {
                Request request = new Request.Builder()
                        .url(SUPABASE_URL + "/rest/v1/produtos?select=*&order=criado_em.desc")
                        .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                        .addHeader("apikey", SUPABASE_KEY)
                        .get()
                        .build();

                Response response = client.newCall(request).execute();
                String body       = response.body().string();

                if (response.isSuccessful()) {
                    callback.onSuccess(body); // JSON array de produtos
                } else {
                    callback.onError("Erro ao listar: " + response.code());
                }

            } catch (Exception e) {
                callback.onError("Exceção: " + e.getMessage());
            }
        }).start();
    }

    // ─── Buscar produto por ID ───────────────────────────────────────
    public void buscarProdutoPorId(String id, SupabaseCallback callback) {
        new Thread(() -> {
            try {
                Request request = new Request.Builder()
                        .url(SUPABASE_URL + "/rest/v1/produtos?id=eq." + id + "&select=*")
                        .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                        .addHeader("apikey", SUPABASE_KEY)
                        .get()
                        .build();

                Response response = client.newCall(request).execute();
                String body = response.body().string();

                if (response.isSuccessful()) callback.onSuccess(body);
                else callback.onError("Erro " + response.code() + ": " + body);

            } catch (Exception e) { callback.onError(e.getMessage()); }
        }).start();
    }

    // ─── Buscar produtor (usuário) por ID ───────────────────────────
    public void buscarProdutorPorId(String id, SupabaseCallback callback) {
        new Thread(() -> {
            try {
                Request request = new Request.Builder()
                        .url(SUPABASE_URL + "/rest/v1/usuarios?id=eq." + id + "&select=*")
                        .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                        .addHeader("apikey", SUPABASE_KEY)
                        .get()
                        .build();

                Response response = client.newCall(request).execute();
                String body = response.body().string();

                if (response.isSuccessful()) callback.onSuccess(body);
                else callback.onError("Erro " + response.code() + ": " + body);

            } catch (Exception e) { callback.onError(e.getMessage()); }
        }).start();
    }

    // ─── Criar/atualizar usuário no Supabase após login Firebase ────
    public void salvarUsuario(String uid, String nome, String email,
                              String telefone, String tipo,
                              SupabaseCallback callback) {
        new Thread(() -> {
            try {
                JSONObject json = new JSONObject();
                json.put("id",       uid);
                json.put("nome",     nome);
                json.put("email",    email);
                json.put("telefone", telefone);
                json.put("tipo",     tipo);

                RequestBody body = RequestBody.create(
                        json.toString(), MediaType.parse("application/json"));

                Request request = new Request.Builder()
                        .url(SUPABASE_URL + "/rest/v1/usuarios")
                        .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                        .addHeader("apikey", SUPABASE_KEY)
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Prefer", "resolution=merge-duplicates")
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) callback.onSuccess("ok");
                else callback.onError(response.body().string());

            } catch (Exception e) { callback.onError(e.getMessage()); }
        }).start();
    }

    // ─── Atualizar produto (PATCH) ───────────────────────────────────
    public void atualizarProduto(String id, String nome, String descricao,
                                 double preco, String fotoUrl,
                                 SupabaseCallback callback) {
        new Thread(() -> {
            try {
                JSONObject json = new JSONObject();
                json.put("nome",     nome);
                json.put("descricao", descricao);
                json.put("preco",    preco);
                // Só atualiza foto_url se uma nova imagem foi selecionada
                if (fotoUrl != null && !fotoUrl.isEmpty()) {
                    json.put("foto_url", fotoUrl);
                }

                RequestBody body = RequestBody.create(
                        json.toString(), MediaType.parse("application/json"));

                Request request = new Request.Builder()
                        .url(SUPABASE_URL + "/rest/v1/produtos?id=eq." + id)
                        .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                        .addHeader("apikey", SUPABASE_KEY)
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Prefer", "return=representation")
                        .patch(body)
                        .build();

                Response response = client.newCall(request).execute();
                String respBody = response.body().string();
                if (response.isSuccessful()) callback.onSuccess(respBody);
                else callback.onError("Erro " + response.code() + ": " + respBody);

            } catch (Exception e) { callback.onError(e.getMessage()); }
        }).start();
    }

    // ─── Deletar produto ─────────────────────────────────────────────
    public void deletarProduto(String id, SupabaseCallback callback) {
        new Thread(() -> {
            try {
                Request request = new Request.Builder()
                        .url(SUPABASE_URL + "/rest/v1/produtos?id=eq." + id)
                        .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                        .addHeader("apikey", SUPABASE_KEY)
                        .delete()
                        .build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) callback.onSuccess("ok");
                else callback.onError("Erro " + response.code());

            } catch (Exception e) { callback.onError(e.getMessage()); }
        }).start();
    }

    // ─── Listar produtos por produtor (para o painel do produtor) ────
    public void listarProdutosPorProdutor(String produtorUid, SupabaseCallback callback) {
        new Thread(() -> {
            try {
                Request request = new Request.Builder()
                        .url(SUPABASE_URL + "/rest/v1/produtos?produtor_id=eq."
                                + produtorUid + "&select=*&order=criado_em.desc")
                        .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                        .addHeader("apikey", SUPABASE_KEY)
                        .get()
                        .build();

                Response response = client.newCall(request).execute();
                String body = response.body().string();
                if (response.isSuccessful()) callback.onSuccess(body);
                else callback.onError("Erro " + response.code() + ": " + body);

            } catch (Exception e) { callback.onError(e.getMessage()); }
        }).start();
    }

    //--------------------------Métodos para o carrinho-------------------------------------------------
    // ─── INSERT em pedidos (retorna o objeto com id) ─────────────────────
    public void inserirPedido(JSONObject pedido, SupabaseCallback callback) {
        new Thread(() -> {
            try {
                // Wrap em array — Supabase aceita array ou objeto único
                RequestBody body = RequestBody.create(
                        pedido.toString(), MediaType.parse("application/json"));

                Request request = new Request.Builder()
                        .url(SUPABASE_URL + "/rest/v1/pedidos")
                        .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                        .addHeader("apikey", SUPABASE_KEY)
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Prefer", "return=representation") // devolve o id gerado
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();
                String respBody   = response.body().string();

                if (response.isSuccessful()) callback.onSuccess(respBody);
                else callback.onError("Erro " + response.code() + ": " + respBody);

            } catch (Exception e) { callback.onError(e.getMessage()); }
        }).start();
    }

    // ─── INSERT batch em pedido_itens ────────────────────────────────────
    public void inserirItensPedido(JSONArray itens, SupabaseCallback callback) {
        new Thread(() -> {
            try {
                RequestBody body = RequestBody.create(
                        itens.toString(), MediaType.parse("application/json"));

                Request request = new Request.Builder()
                        .url(SUPABASE_URL + "/rest/v1/pedido_itens")
                        .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                        .addHeader("apikey", SUPABASE_KEY)
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Prefer", "return=minimal") // não precisa do retorno
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) callback.onSuccess("ok");
                else callback.onError("Erro " + response.code() + ": " + response.body().string());

            } catch (Exception e) { callback.onError(e.getMessage()); }
        }).start();
    }

    // ─── GET genérico (para queries com filtros customizados) ────────────
    // ─── GET genérico ─────────────────────────────────────────────────
    // CORREÇÃO: adicionado Accept: application/json.
    // Sem esse header o PostgREST retorna 204 No Content em vez de
    // JSON, fazendo response.body() vir null e quebrando todo parsing.
    public void get(String path, SupabaseCallback callback) {
        new Thread(() -> {
            try {
                Request request = new Request.Builder()
                        .url(SUPABASE_URL + path)
                        .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                        .addHeader("apikey", SUPABASE_KEY)
                        .addHeader("Accept", "application/json") // ← era ausente
                        .get()
                        .build();

                Response response = client.newCall(request).execute();
                String body = lerBody(response); // ← sem NPE mesmo com body null

                if (response.isSuccessful()) callback.onSuccess(body);
                else callback.onError("Erro " + response.code() + ": " + body);

            } catch (Exception e) {
                callback.onError("Exceção: " + e.getMessage());
            }
        }).start();
    }

    // ─── PATCH genérico (para updates parciais) ──────────────────────────
    public void patch(String path, JSONObject body, SupabaseCallback callback) {
        new Thread(() -> {
            try {
                RequestBody reqBody = RequestBody.create(
                        body.toString(), MediaType.parse("application/json"));

                Request request = new Request.Builder()
                        .url(SUPABASE_URL + path)
                        .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                        .addHeader("apikey", SUPABASE_KEY)
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Prefer", "return=minimal")
                        .patch(reqBody)
                        .build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) callback.onSuccess("ok");
                else callback.onError("Erro " + response.code() + ": " + response.body().string());

            } catch (Exception e) { callback.onError(e.getMessage()); }
        }).start();
    }

    // ─── DELETE genérico ─────────────────────────────────────────────────
    public void delete(String path, SupabaseCallback callback) {
        new Thread(() -> {
            try {
                Request request = new Request.Builder()
                        .url(SUPABASE_URL + path)
                        .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                        .addHeader("apikey", SUPABASE_KEY)
                        .delete()
                        .build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) callback.onSuccess("ok");
                else callback.onError("Erro " + response.code());

            } catch (Exception e) { callback.onError(e.getMessage()); }
        }).start();
    }

    // ─── POST genérico ────────────────────────────────────────────────────
    public void post(String path, JSONObject body, SupabaseCallback callback) {
        new Thread(() -> {
            try {
                RequestBody reqBody = RequestBody.create(
                        body.toString(), MediaType.parse("application/json"));

                Request request = new Request.Builder()
                        .url(SUPABASE_URL + path)
                        .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                        .addHeader("apikey", SUPABASE_KEY)
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Prefer", "return=minimal")
                        .post(reqBody)
                        .build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) callback.onSuccess("ok");
                else callback.onError("Erro " + response.code() + ": " + response.body().string());

            } catch (Exception e) { callback.onError(e.getMessage()); }
        }).start();
    }
}