package com.example.hortlink.data.repository;

import com.example.hortlink.data.dto.ProdutoListaDTO;
import com.example.hortlink.util.RetrofitClient;
import com.example.hortlink.data.dto.NovoProdutoDTO;
import com.example.hortlink.data.model.Produto;
import com.example.hortlink.service.BaseCallback;
import com.example.hortlink.service.ProdutoService;
import com.google.gson.Gson;

import java.io.File;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProdutoRepository {

    private final ProdutoService api = RetrofitClient.getProdutoService();

    public void cadastrarProduto(NovoProdutoDTO novoProduto, File arquivoImagem, BaseCallback<Produto> callback) {
        Gson gson = new Gson();
        String dtoJson = gson.toJson(novoProduto);
        RequestBody produtoData = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                dtoJson
        );

        RequestBody requestFile = RequestBody.create(
                MediaType.parse("image/*"),
                arquivoImagem
        );

        MultipartBody.Part imagem = MultipartBody.Part.createFormData(
                "imagem",
                arquivoImagem.getName(),
                requestFile
        );

        api.cadastrarProduto(produtoData, imagem).enqueue(new Callback<Produto>() {
            @Override
            public void onResponse(Call<Produto> call, Response<Produto> response) {
                if(response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Erro ao salvar Produto: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Produto> call, Throwable t) {
                callback.onError("Falha na rede: " + t.getMessage());
            }
        });
    }

    public void deletarProduto(Long idProduto, BaseCallback<Void> callback) {
        api.deletarProduto(idProduto).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if(response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError("Erro ao deletar Produto: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Falha na rede: " + t.getMessage());
            }
        });
    }

    public void buscarPorId(Long idProduto, BaseCallback<Produto> callback) {
        api.buscarPorId(idProduto).enqueue(new Callback<Produto>() {
            @Override
            public void onResponse(Call<Produto> call, Response<Produto> response){
                if(response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Erro ao buscar Produto: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Produto> call, Throwable t) {
                callback.onError("Falha na rede: " + t.getMessage());
            }
        });
    }

    public void listarMeusProdutos(BaseCallback<List<ProdutoListaDTO>> callback) {
        api.listarPorComercio().enqueue(new Callback<List<ProdutoListaDTO>>() {
            @Override
            public void onResponse(Call<List<ProdutoListaDTO>> call, Response<List<ProdutoListaDTO>> response){
                if(response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Erro ao buscar Produtos: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<ProdutoListaDTO>> call, Throwable t) {
                callback.onError("Falha na rede: " + t.getMessage());
            }
        });
    }

    public void obterMeusProdutosSemOferta(BaseCallback<List<ProdutoListaDTO>> callback) {
        api.obterProdutosSemOfertaAtiva().enqueue(new Callback<List<ProdutoListaDTO>>() {
            @Override
            public void onResponse(Call<List<ProdutoListaDTO>> call, Response<List<ProdutoListaDTO>> response){
                if(response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Erro ao buscar Produtos: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<ProdutoListaDTO>> call, Throwable t) {
                callback.onError("Falha na rede: " + t.getMessage());
            }
        });
    }
}
