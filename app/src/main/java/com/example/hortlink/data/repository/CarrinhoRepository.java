package com.example.hortlink.data.repository;

import com.example.hortlink.data.remote.SupabaseClient;

import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class CarrinhoRepository {

    private final SupabaseClient client = SupabaseClient.getInstance();

    public interface Callback {
        void onSuccess(String resultado);
        void onError(String erro);
    }

    // Carrega todos os itens do carrinho com dados do produto (join)
    public void carregarCarrinho(String usuarioId, Callback callback) {
        new Thread(() -> {
            try {
                String path = "/rest/v1/carrinho"
                        + "?select=id,quantidade,produtos(id,nome,preco,foto_url,unidade,produtor_id)"
                        + "&usuario_id=eq." + usuarioId;

                Request request = client.baseRequest(path)
                        .addHeader("Accept", "application/json")  // ← essencial
                        .get()
                        .build();

                Response response = client.getHttp().newCall(request).execute();
                String body = lerBody(response);

                if (response.isSuccessful()) callback.onSuccess(body);
                else callback.onError("Erro " + response.code() + ": " + body);

            } catch (Exception e) { callback.onError(e.getMessage()); }
        }).start();
    }

    // Verifica se produto já está no carrinho do usuário
    public void buscarItemExistente(String usuarioId, String produtoId, Callback callback) {
        new Thread(() -> {
            try {
                String path = "/rest/v1/carrinho"
                        + "?usuario_id=eq." + usuarioId
                        + "&produto_id=eq." + produtoId
                        + "&select=id,quantidade";

                Request request = client.baseRequest(path)
                        .addHeader("Accept", "application/json")
                        .get()
                        .build();

                Response response = client.getHttp().newCall(request).execute();
                String body = lerBody(response);

                if (response.isSuccessful()) callback.onSuccess(body);
                else callback.onError("Erro " + response.code() + ": " + body);

            } catch (Exception e) { callback.onError(e.getMessage()); }
        }).start();
    }

    // Insere novo item no carrinho
    public void inserirItem(String usuarioId, String produtoId, Callback callback) {
        new Thread(() -> {
            try {
                JSONObject json = new JSONObject();
                json.put("usuario_id", usuarioId);
                json.put("produto_id", produtoId);
                json.put("quantidade", 1);

                RequestBody body = RequestBody.create(
                        json.toString(), MediaType.parse("application/json"));

                Request request = client.baseRequest("/rest/v1/carrinho")
                        .addHeader("Prefer", "return=minimal")
                        .post(body)
                        .build();

                Response response = client.getHttp().newCall(request).execute();
                if (response.isSuccessful()) callback.onSuccess("ok");
                else callback.onError("Erro " + response.code() + ": " + lerBody(response));

            } catch (Exception e) { callback.onError(e.getMessage()); }
        }).start();
    }

    // Atualiza quantidade de um item
    public void atualizarQuantidade(String carrinhoId, int novaQtd, Callback callback) {
        new Thread(() -> {
            try {
                JSONObject json = new JSONObject();
                json.put("quantidade", novaQtd);

                RequestBody body = RequestBody.create(
                        json.toString(), MediaType.parse("application/json"));

                Request request = client
                        .baseRequest("/rest/v1/carrinho?id=eq." + carrinhoId)
                        .addHeader("Prefer", "return=minimal")
                        .patch(body)
                        .build();

                Response response = client.getHttp().newCall(request).execute();
                if (response.isSuccessful()) callback.onSuccess("ok");
                else callback.onError("Erro " + response.code() + ": " + lerBody(response));

            } catch (Exception e) { callback.onError(e.getMessage()); }
        }).start();
    }

    // Remove um item específico pelo id do carrinho
    public void removerItem(String carrinhoId, Callback callback) {
        new Thread(() -> {
            try {
                Request request = client
                        .baseRequest("/rest/v1/carrinho?id=eq." + carrinhoId)
                        .delete()
                        .build();

                Response response = client.getHttp().newCall(request).execute();
                if (response.isSuccessful()) callback.onSuccess("ok");
                else callback.onError("Erro " + response.code());

            } catch (Exception e) { callback.onError(e.getMessage()); }
        }).start();
    }

    // Limpa todo o carrinho do usuário (pós-checkout)
    public void limparCarrinho(String usuarioId, Callback callback) {
        new Thread(() -> {
            try {
                Request request = client
                        .baseRequest("/rest/v1/carrinho?usuario_id=eq." + usuarioId)
                        .delete()
                        .build();

                Response response = client.getHttp().newCall(request).execute();
                if (response.isSuccessful()) callback.onSuccess("ok");
                else callback.onError("Erro " + response.code());

            } catch (Exception e) { callback.onError(e.getMessage()); }
        }).start();
    }

    private String lerBody(Response response) {
        ResponseBody body = response.body();
        if (body == null) return "[]";
        try { return body.string(); }
        catch (Exception e) { return "[]"; }
    }
}