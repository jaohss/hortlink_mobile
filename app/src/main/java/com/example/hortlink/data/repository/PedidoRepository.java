package com.example.hortlink.data.repository;

import com.example.hortlink.data.model.Pedido;
import com.example.hortlink.data.remote.SupabaseClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class PedidoRepository {

    private final SupabaseClient client = SupabaseClient.getInstance();

    public interface Callback {
        void onSuccess(String resultado);
        void onError(String erro);
    }

    public interface CallbackLista {
        void onSuccess(List<Pedido> pedidos);
        void onError(String erro);
    }

    // ─── Criar pedido — retorna JSON com id gerado ────────────────────
    public void inserirPedido(String compradorId, String produtorId,
                              double total, Callback callback) {
        new Thread(() -> {
            try {
                JSONObject json = new JSONObject();
                json.put("comprador_id", compradorId);
                json.put("produtor_id",  produtorId);
                json.put("valor_total",  total);
                json.put("status",       "pago");

                RequestBody body = RequestBody.create(
                        json.toString(), MediaType.parse("application/json"));

                Request request = client.baseRequest("/rest/v1/pedidos")
                        .addHeader("Prefer", "return=representation")
                        .post(body)
                        .build();

                Response response = client.getHttp().newCall(request).execute();
                String respBody = lerBody(response);

                if (response.isSuccessful()) callback.onSuccess(respBody);
                else callback.onError("Erro " + response.code() + ": " + respBody);

            } catch (Exception e) { callback.onError(e.getMessage()); }
        }).start();
    }

    // ─── Inserir itens do pedido em batch ─────────────────────────────
    public void inserirItensPedido(JSONArray itens, Callback callback) {
        new Thread(() -> {
            try {
                RequestBody body = RequestBody.create(
                        itens.toString(), MediaType.parse("application/json"));

                Request request = client.baseRequest("/rest/v1/pedido_itens")
                        .addHeader("Prefer", "return=minimal")
                        .post(body)
                        .build();

                Response response = client.getHttp().newCall(request).execute();

                if (response.isSuccessful()) callback.onSuccess("ok");
                else callback.onError("Erro " + response.code() + ": " + lerBody(response));

            } catch (Exception e) { callback.onError(e.getMessage()); }
        }).start();
    }

    // ─── Pedidos do COMPRADOR (com itens via join) ────────────────────
    // Usado pelo PedidosCompradorFragment
    public void listarPorComprador(String compradorId, CallbackLista callback) {
        new Thread(() -> {
            try {
                // Join: pedidos → pedido_itens → produtos (só nome)
                String path = "/rest/v1/pedidos"
                        + "?comprador_id=eq." + compradorId
                        + "&select=*,pedido_itens(quantidade,preco_unitario,produtos(nome))"
                        + "&order=criado_em.desc";

                Request request = client.baseRequest(path)
                        .addHeader("Accept", "application/json")
                        .get()
                        .build();

                Response response = client.getHttp().newCall(request).execute();
                String body = lerBody(response);

                if (response.isSuccessful()) callback.onSuccess(parsePedidos(body));
                else callback.onError("Erro " + response.code() + ": " + body);

            } catch (Exception e) { callback.onError(e.getMessage()); }
        }).start();
    }

    // ─── Pedidos do PRODUTOR (com itens via join) ─────────────────────
    // Usado pelo PedidosProdutorFragment
    public void listarPorProdutor(String produtorId, CallbackLista callback) {
        new Thread(() -> {
            try {
                String path = "/rest/v1/pedidos"
                        + "?produtor_id=eq." + produtorId
                        + "&select=*,pedido_itens(quantidade,preco_unitario,produtos(nome))"
                        + "&order=criado_em.desc";

                Request request = client.baseRequest(path)
                        .addHeader("Accept", "application/json")
                        .get()
                        .build();

                Response response = client.getHttp().newCall(request).execute();
                String body = lerBody(response);

                if (response.isSuccessful()) callback.onSuccess(parsePedidos(body));
                else callback.onError("Erro " + response.code() + ": " + body);

            } catch (Exception e) { callback.onError(e.getMessage()); }
        }).start();
    }

    // ─── Atualizar status (produtor marca como enviado/entregue) ──────
    public void atualizarStatus(String pedidoId, String novoStatus, Callback callback) {
        new Thread(() -> {
            try {
                JSONObject json = new JSONObject();
                json.put("status", novoStatus);

                RequestBody body = RequestBody.create(
                        json.toString(), MediaType.parse("application/json"));

                Request request = client
                        .baseRequest("/rest/v1/pedidos?id=eq." + pedidoId)
                        .addHeader("Prefer", "return=minimal")
                        .patch(body)
                        .build();

                Response response = client.getHttp().newCall(request).execute();

                if (response.isSuccessful()) callback.onSuccess("ok");
                else callback.onError("Erro " + response.code() + ": " + lerBody(response));

            } catch (Exception e) { callback.onError(e.getMessage()); }
        }).start();
    }

    // ─── Parse ────────────────────────────────────────────────────────
    private List<Pedido> parsePedidos(String json) throws Exception {
        JSONArray array = new JSONArray(json);
        List<Pedido> lista = new ArrayList<>();

        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            Pedido pedido = Pedido.fromJson(obj);

            // Popula itens se o join veio junto
            JSONArray itensJson = obj.optJSONArray("pedido_itens");
            if (itensJson != null) {
                List<Pedido.ItemPedido> itens = new ArrayList<>();
                for (int j = 0; j < itensJson.length(); j++) {
                    itens.add(Pedido.ItemPedido.fromJson(itensJson.getJSONObject(j)));
                }
                pedido.setItens(itens);
            }

            lista.add(pedido);
        }

        return lista;
    }

    private String lerBody(Response response) {
        ResponseBody body = response.body();
        if (body == null) return "[]";
        try { return body.string(); }
        catch (Exception e) { return "[]"; }
    }
}