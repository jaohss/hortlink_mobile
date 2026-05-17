package com.example.hortlink.data.repository;

import android.content.Context;
import android.net.Uri;

import com.example.hortlink.bd.SupabaseHelper;
import com.example.hortlink.data.model.Usuario;
import com.example.hortlink.data.remote.SupabaseClient;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Responsabilidades:
 *  - login e cadastro (Firebase Auth + Supabase)
 *  - busca e atualização de perfil
 *  - upload de foto
 *
 * Nenhuma Activity ou Fragment deve chamar SupabaseHelper diretamente
 * para operações relacionadas a usuário — tudo passa por aqui.
 */
public class UsuarioRepository {

    private final SupabaseClient client = SupabaseClient.getInstance();
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    // ─── Callbacks específicos deste repository ───────────────────────

    public interface Callback {
        void onSuccess(Usuario usuario);
        void onError(String erro);
    }

    public interface CallbackSimples {
        void onSuccess();
        void onError(String erro);
    }

    public interface CallbackFoto {
        void onSuccess(String urlPublica);
        void onError(String erro);
    }

    // ─── Login ────────────────────────────────────────────────────────
    public void login(String email, String senha, Callback callback) {
        firebaseAuth.signInWithEmailAndPassword(email, senha)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        callback.onError(task.getException().getMessage());
                        return;
                    }
                    String uid = task.getResult().getUser().getUid();
                    buscarPorId(uid, callback);
                });
    }

    // ─── Cadastro ─────────────────────────────────────────────────────
    public void cadastrar(String nome, String email, String senha, String tipo, Callback callback) {
        firebaseAuth.createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        callback.onError(task.getException().getMessage());
                        return;
                    }
                    String uid = task.getResult().getUser().getUid();
                    salvarNoSupabase(uid, nome, email, tipo, callback);
                });
    }

    // ─── Buscar por ID ────────────────────────────────────────────────
    public void buscarPorId(String uid, Callback callback) {
        new Thread(() -> {
            try {
                Request request = client
                        .baseRequest("/rest/v1/usuarios?id=eq." + uid + "&select=*")
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
                    callback.onError("Usuário não encontrado");
                    return;
                }

                callback.onSuccess(Usuario.fromJson(array.getJSONObject(0)));

            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        }).start();
    }

    // ─── Atualizar perfil (genérico) ──────────────────────────────────
    // Recebe um JSONObject com apenas os campos a atualizar.
    // Usado tanto pelo CompletarPerfilCompradorActivity
    // quanto pelo CompletarPerfilProdutorActivity.
    public void atualizarPerfil(String uid, JSONObject campos, CallbackSimples callback) {
        new Thread(() -> {
            try {
                RequestBody body = RequestBody.create(campos.toString(), MediaType.parse("application/json"));

                Request request = client
                        .baseRequest("/rest/v1/usuarios?id=eq." + uid)
                        .addHeader("Prefer", "return=minimal")
                        .patch(body)
                        .build();

                Response response = client.getHttp().newCall(request).execute();

                if (response.isSuccessful()) callback.onSuccess();
                else callback.onError("Erro " + response.code() + ": " + response.body().string());

            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        }).start();
    }

    // ─── Upload de foto de perfil ─────────────────────────────────────
    // O upload não passa pelo baseRequest porque o Content-Type muda
    // para image/jpeg — por isso precisa de um request manual.
    // Solução limpa: adicionar getKey() no SupabaseClient (veja comentário abaixo).
//    public void uploadFotoPerfil(String uid, Uri fotoUri,
//                                 Context context, CallbackFoto callback) {
//        new Thread(() -> {
//            try {
//                InputStream is = context.getContentResolver().openInputStream(fotoUri);
//                byte[] bytes   = is.readAllBytes();
//                is.close();
//
//                String path      = "fotos/perfil_" + uid + ".jpg";
//                String uploadUrl = client.getStorageUrl() + "/object/produtos/" + path;
//
//                // TODO: quando adicionar getKey() no SupabaseClient, usar client.getKey() aqui
//                String key = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImR6ZmJ0ZXZpZG5mYXJscG5meXNkIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzgyNTcwNzMsImV4cCI6MjA5MzgzMzA3M30.79uc9zT_T-HPoUhJMMyUMKsW4qS2kiCHuuBcpmv3sDQ";
//
//                Request uploadRequest = new Request.Builder()
//                        .url(uploadUrl)
//                        .addHeader("Authorization", "Bearer " + key)
//                        .addHeader("apikey", key)
//                        .addHeader("x-upsert", "true")
//                        .post(RequestBody.create(bytes, MediaType.parse("image/jpeg")))
//                        .build();
//
//                Response uploadResponse = client.getHttp().newCall(uploadRequest).execute();
//
//                if (!uploadResponse.isSuccessful()) {
//                    callback.onError("Erro no upload: " + uploadResponse.code());
//                    return;
//                }
//
//                String urlPublica = client.getStorageUrl() + "/object/public/produtos/" + path;
//
//                // Salva a URL pública no banco
//                JSONObject json = new JSONObject();
//                json.put("foto_url", urlPublica);
//
//                RequestBody patchBody = RequestBody.create(
//                        json.toString(), MediaType.parse("application/json"));
//
//                Request patchRequest = client
//                        .baseRequest("/rest/v1/usuarios?id=eq." + uid)
//                        .addHeader("Prefer", "return=minimal")
//                        .patch(patchBody)
//                        .build();
//
//                Response patchResponse = client.getHttp().newCall(patchRequest).execute();
//
//                if (patchResponse.isSuccessful()) callback.onSuccess(urlPublica);
//                else callback.onError("Upload ok, mas erro ao salvar URL: " + patchResponse.code());
//
//            } catch (Exception e) {
//                callback.onError(e.getMessage());
//            }
//        }).start();
//    }

    // ─── Logout ───────────────────────────────────────────────────────
    public void logout() {
        firebaseAuth.signOut();
    }

    // ─── Privado: salva no Supabase após criar no Firebase ────────────
    private void salvarNoSupabase(String uid, String nome, String email, String tipo, Callback callback) {
        new Thread(() -> {
            try {
                JSONObject json = new JSONObject();
                json.put("id",       uid);
                json.put("nome",     nome);
                json.put("email",    email);
                json.put("tipo",     tipo);

                RequestBody body = RequestBody.create(
                        json.toString(), MediaType.parse("application/json"));

                Request request = client
                        .baseRequest("/rest/v1/usuarios")
                        .addHeader("Prefer", "resolution=merge-duplicates")
                        .post(body)
                        .build();

                Response response = client.getHttp().newCall(request).execute();

                if (!response.isSuccessful()) {
                    callback.onError("Erro ao salvar perfil: " + response.code());
                    return;
                }

                // Monta Usuario localmente — evita uma requisição extra de busca
                Usuario u  = new Usuario();
                u.id       = uid;
                u.nome     = nome;
                u.email    = email;
                u.tipo     = tipo;
                callback.onSuccess(u);

            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        }).start();
    }
}