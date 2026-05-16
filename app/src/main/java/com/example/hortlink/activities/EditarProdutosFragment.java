package com.example.hortlink.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.hortlink.R;
import com.example.hortlink.bd.SupabaseHelper;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONObject;

public class EditarProdutosFragment extends Fragment {

    private static final String ARG_PRODUTO_ID = "produto_id";

    private EditText edtNome, edtDescricao, edtPreco;
    private ImageView imgProduto;
    private ProgressBar progressBar;
    private String produtoId;           // agora String (UUID do Supabase)
    private String fotoUrlAtual = "";   // URL já salva no Supabase
    private Uri imagemNova = null;      // nova imagem escolhida pelo usuário

    private SupabaseHelper supabase;

    // ─── Galeria ─────────────────────────────────────────────────
    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    requireContext().getContentResolver().takePersistableUriPermission(
                            uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    imagemNova = uri;

                    // Mostra a foto e esconde o placeholder
                    imgProduto.setVisibility(View.VISIBLE);
                    requireView().findViewById(R.id.layoutPlaceholder)
                            .setVisibility(View.GONE);
                    imgProduto.setImageURI(uri);
                }
            });

    // ─── Factory ─────────────────────────────────────────────────
    public static EditarProdutosFragment newInstance(String produtoId) {
        EditarProdutosFragment fragment = new EditarProdutosFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PRODUTO_ID, produtoId); // String agora
        fragment.setArguments(args);
        return fragment;
    }

    public EditarProdutosFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_editar_produtos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        supabase = new SupabaseHelper(requireContext());

        edtNome      = view.findViewById(R.id.edtNome);
        edtDescricao = view.findViewById(R.id.edtDescricao);
        edtPreco     = view.findViewById(R.id.edtPreco);
        imgProduto   = view.findViewById(R.id.imgProduto);
        View layoutPlaceholder = view.findViewById(R.id.layoutPlaceholder);
        View frameUploadFoto   = view.findViewById(R.id.frameUploadFoto);
        MaterialButton btnCancelar = view.findViewById(R.id.btnCancelar);
        // progressBar = view.findViewById(R.id.progressBar); // descomente se tiver no XML

        if (getArguments() != null) {
            produtoId = getArguments().getString(ARG_PRODUTO_ID);
        }

        if (produtoId == null) {
            Toast.makeText(getContext(), "Produto inválido", Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager().popBackStack();
            return;
        }

        frameUploadFoto.setOnClickListener(v -> pickImage.launch("image/*"));
        view.findViewById(R.id.btnAtualizar).setOnClickListener(v -> atualizarProduto());

        carregarProduto();

        btnCancelar.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
    }

    // ─── 1. Carrega dados do produto ─────────────────────────────
    private void carregarProduto() {
        setCarregando(true);

        supabase.buscarProdutoPorId(produtoId, new SupabaseHelper.SupabaseCallback() {
            @Override
            public void onSuccess(String json) {
                try {
                    JSONArray array = new JSONArray(json);
                    if (array.length() == 0) {
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(),
                                    "Produto não encontrado", Toast.LENGTH_SHORT).show();
                            requireActivity().getSupportFragmentManager().popBackStack();
                        });
                        return;
                    }

                    JSONObject obj = array.getJSONObject(0);
                    String nome      = obj.optString("nome");
                    String descricao = obj.optString("descricao");
                    double preco     = obj.optDouble("preco", 0.0);
                    fotoUrlAtual     = obj.optString("foto_url", "");

                    requireActivity().runOnUiThread(() -> {
                        setCarregando(false);
                        edtNome.setText(nome);
                        edtDescricao.setText(descricao);
                        edtPreco.setText(String.valueOf(preco));

                        // Carrega foto atual via Glide
                        if (!fotoUrlAtual.isEmpty()) {
                            imgProduto.setVisibility(View.VISIBLE);
                            requireView().findViewById(R.id.layoutPlaceholder).setVisibility(View.GONE);
                            Glide.with(requireContext())
                                    .load(fotoUrlAtual)
                                    .placeholder(R.drawable.hortlink_logo)
                                    .error(R.drawable.hortlink_logo)
                                    .centerCrop()
                                    .into(imgProduto);
                        } else {
                            imgProduto.setVisibility(View.GONE);
                            requireView().findViewById(R.id.layoutPlaceholder).setVisibility(View.VISIBLE);
                            imgProduto.setImageResource(R.drawable.hortlink_logo);
                        }
                    });

                } catch (Exception e) {
                    requireActivity().runOnUiThread(() -> {
                        setCarregando(false);
                        Toast.makeText(getContext(),
                                "Erro ao carregar produto", Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onError(String erro) {
                requireActivity().runOnUiThread(() -> {
                    setCarregando(false);
                    Toast.makeText(getContext(), "Erro: " + erro, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    // ─── 2. Atualiza — com ou sem nova imagem ────────────────────
    private void atualizarProduto() {
        String nome     = edtNome.getText().toString().trim();
        String descricao = edtDescricao.getText().toString().trim();
        String precoStr = edtPreco.getText().toString().trim();

        if (nome.isEmpty())    { edtNome.setError("Informe o nome"); return; }
        if (precoStr.isEmpty()) { edtPreco.setError("Informe o preço"); return; }

        double preco = Double.parseDouble(precoStr.replace(",", "."));

        setCarregando(true);

        if (imagemNova != null) {
            // Tem imagem nova → faz upload primeiro, depois atualiza
            String nomeArquivo = produtoId + "_" + System.currentTimeMillis() + ".jpg";
            android.util.Log.d("EDITAR", "Iniciando upload: " + nomeArquivo);

            supabase.uploadImagem(imagemNova, nomeArquivo, new SupabaseHelper.SupabaseCallback() {
                @Override
                public void onSuccess(String novaUrl) {
                    android.util.Log.d("EDITAR", "Upload ok, URL: " + novaUrl);
                    salvarNoSupabase(nome, descricao, preco, novaUrl);
                }

                @Override
                public void onError(String erro) {
                    android.util.Log.e("EDITAR", "Erro upload: " + erro);
                    requireActivity().runOnUiThread(() -> {
                        setCarregando(false);
                        Toast.makeText(getContext(),
                                "Erro no upload da imagem: " + erro, Toast.LENGTH_LONG).show();
                    });
                }
            });

        } else {
            // Sem imagem nova → mantém a URL atual
            salvarNoSupabase(nome, descricao, preco, fotoUrlAtual);
        }
    }

    // ─── 3. PATCH no Supabase ────────────────────────────────────
    private void salvarNoSupabase(String nome, String descricao,
                                  double preco, String fotoUrl) {
        android.util.Log.d("EDITAR", "Salvando no banco. foto_url: " + fotoUrl);
        supabase.atualizarProduto(produtoId, nome, descricao, preco, fotoUrl,
                new SupabaseHelper.SupabaseCallback() {
                    @Override
                    public void onSuccess(String r) {
                        android.util.Log.d("EDITAR", "Produto atualizado. Resposta: " + r);
                        requireActivity().runOnUiThread(() -> {
                            setCarregando(false);
                            Toast.makeText(getContext(),
                                    "Produto atualizado!", Toast.LENGTH_SHORT).show();
                            requireActivity().getSupportFragmentManager().popBackStack();
                        });
                    }

                    @Override
                    public void onError(String erro) {
                        android.util.Log.e("EDITAR", "Erro ao atualizar: " + erro);
                        requireActivity().runOnUiThread(() -> {
                            setCarregando(false);
                            Toast.makeText(getContext(),
                                    "Erro ao atualizar: " + erro, Toast.LENGTH_LONG).show();
                        });
                    }
                });
    }

    private void setCarregando(boolean carregando) {
        // if (progressBar != null)
        //     progressBar.setVisibility(carregando ? View.VISIBLE : View.GONE);
        if (getView() != null) {
            getView().findViewById(R.id.btnAtualizar)
                    .setEnabled(!carregando);
        }
    }
}