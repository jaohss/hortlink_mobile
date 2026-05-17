package com.example.hortlink.data.repository;

import com.example.hortlink.data.model.Produtor;
import com.example.hortlink.data.model.Usuario;
import com.example.hortlink.data.remote.SupabaseClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ProdutorRepository {

    private final SupabaseClient client = SupabaseClient.getInstance();

    public interface Callback {
        void onSuccess(List<Produtor> produtores);
        void onError(String erro);
    }

    public interface CallbackUnico {
        void onSuccess(Produtor produtor);
        void onError(String erro);
    }

    public interface CallbackSimples {
        void onSuccess();
        void onError(String erro);
    }

    // ─── Completar perfil do produtor (pós-cadastro) ──────────────────
    //
    // Chamado pela CompletarPerfilProdutorActivity após o cadastro inicial.
    // Persiste apenas os campos complementares — nome, email e tipo já
    // foram salvos pelo UsuarioRepository.cadastrar().
    //
    // Campos enviados:
    //   telefone  → contato principal do produtor
    //   cep       → armazenado para futuras consultas/filtros
    //   cidade    → preenchido via ViaCEP ou manualmente
    //   estado    → preenchido via ViaCEP ou manualmente
    //   descricao → texto livre de apresentação do produtor
    //
    // @param uid       Firebase UID do produtor (PK na tabela usuarios)
    // @param telefone  número de contato
    // @param cep       CEP do produtor (8 dígitos, sem máscara)
    // @param cidade    cidade resolvida pelo ViaCEP
    // @param estado    UF resolvida pelo ViaCEP
    // @param descricao apresentação do produtor (pode ser vazia)
    // @param callback  onSuccess() ou onError(String)
    public void completarPerfil(
            String uid,
            String telefone,
            String cep,
            String bairro,
            String cidade,
            String estado,
            String descricao,
            CallbackSimples callback) {

        new Thread(() -> {
            try {
                JSONObject json = new JSONObject();
                json.put("telefone", telefone);
                json.put("cep", cep);
                json.put("bairro", bairro);
                json.put("cidade", cidade);
                json.put("estado", estado);
                json.put("descricao", descricao);

                RequestBody body = RequestBody.create(
                        json.toString(), MediaType.parse("application/json"));

                Request request = client
                        .baseRequest("/rest/v1/usuarios?id=eq." + uid)
                        .addHeader("Prefer", "return=minimal")
                        .patch(body)
                        .build();

                Response response = client.getHttp().newCall(request).execute();

                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    String erro = response.body() != null ? response.body().string() : "sem detalhes";
                    callback.onError("Erro " + response.code() + ": " + erro);
                }

            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        }).start();
    }

    // ─── Listar todos os produtores ───────────────────────────────────
    // tipo=eq.produtor é o WHERE tipo = 'produtor' — nunca retorna compradores
    public void listarProdutores(Callback callback) {
        new Thread(() -> {
            try {
                Request request = client
                        .baseRequest("/rest/v1/usuarios?tipo=eq.produtor&select=*&order=criado_em.desc")
                        .get()
                        .build();

                Response response = client.getHttp().newCall(request).execute();
                String body = response.body().string();

                if (!response.isSuccessful()) {
                    callback.onError("Erro " + response.code() + ": " + body);
                    return;
                }

                callback.onSuccess(parseProdutores(body));

            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        }).start();
    }

    // ─── Buscar produtor por ID ───────────────────────────────────────
    // Duplo filtro: id E tipo — evita retornar comprador com o id errado
    public void buscarPorId(String produtorId, CallbackUnico callback) {
        new Thread(() -> {
            try {
                Request request = client
                        .baseRequest("/rest/v1/usuarios?id=eq." + produtorId
                                + "&tipo=eq.produtor&select=*")
                        .get()
                        .build();

                Response response = client.getHttp().newCall(request).execute();
                String body = response.body().string();

                if (!response.isSuccessful()) {
                    callback.onError("Erro " + response.code() + ": " + body);
                    return;
                }

                JSONArray array = new JSONArray(body);
                if (array.length() == 0) {
                    callback.onError("Produtor não encontrado");
                    return;
                }

                callback.onSuccess(Produtor.fromJson(array.getJSONObject(0)));

            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        }).start();
    }

    // ─── Buscar produtores por cidade ─────────────────────────────────
    public void listarPorCidade(String cidade, Callback callback) {
        new Thread(() -> {
            try {
                Request request = client
                        .baseRequest("/rest/v1/usuarios?tipo=eq.produtor&cidade=eq."
                                + cidade + "&select=*")
                        .get()
                        .build();

                Response response = client.getHttp().newCall(request).execute();
                String body = response.body().string();

                if (!response.isSuccessful()) {
                    callback.onError("Erro " + response.code() + ": " + body);
                    return;
                }

                callback.onSuccess(parseProdutores(body));

            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        }).start();
    }

    // ─── Parse privado ────────────────────────────────────────────────
    private List<Produtor> parseProdutores(String json) throws Exception {
        JSONArray array = new JSONArray(json);
        List<Produtor> lista = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            Usuario u = Usuario.fromJson(array.getJSONObject(i));
            if (u.isProdutor()) lista.add(new Produtor(u));
        }
        return lista;
    }
}
