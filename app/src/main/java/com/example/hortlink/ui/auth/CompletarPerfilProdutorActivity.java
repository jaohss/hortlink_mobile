package com.example.hortlink.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.hortlink.R;
import com.example.hortlink.data.dto.CompletarPerfilComercioDTO;
import com.example.hortlink.data.dto.ViaCepResponse;
import com.example.hortlink.data.repository.ComercioRepository;
import com.example.hortlink.data.repository.GeoRepository;
import com.example.hortlink.service.BaseCallback;

public class CompletarPerfilProdutorActivity extends AppCompatActivity {

    EditText edtCidade, edtTelefone, edtDescricao, edtEstado, edtCep, edtBairro;
    Button btnConcluir, btnPular;
    ProgressBar progressBar;

    private GeoRepository geoRepository;
    private ComercioRepository comercioRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_completar_perfil_produtor);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bindViews();
        configurarViaCep();
        preencherSeEdicao();
        configurarBotoes();
    }

    private void bindViews(){
        edtCep = findViewById(R.id.edtCep);
        edtEstado = findViewById(R.id.edtEstado);
        edtCidade = findViewById(R.id.edtCidade);
        edtTelefone = findViewById(R.id.edtTelefone);
        edtDescricao = findViewById(R.id.edtDescricaoProd);
        edtBairro = findViewById(R.id.edtBairro);
        btnConcluir = findViewById(R.id.btnConcluir);
        btnPular = findViewById(R.id.btnPular);
        progressBar = findViewById(R.id.progressBar);

        progressBar.setVisibility(View.GONE);

        // Cidade e estado são preenchidos pelo ViaCEP, mas o usuário
        // pode editar manualmente caso a API falhe ou o dado venha errado.
        edtCidade.setEnabled(true);
        edtEstado.setEnabled(true);
    }

    private void configurarViaCep(){
        edtCep.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                // Remove tudo que não for dígito para comparar o tamanho real
                String cepLimpo = s.toString().replaceAll("[^0-9]", "");
                if (cepLimpo.length() == 8) {
                    buscarCep(cepLimpo);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

        });
    }

    private void buscarCep(String cep){
        setCarregando(true);

        geoRepository.buscarEnderecoPorCep(cep, new BaseCallback<ViaCepResponse>() {
            @Override
            public void onSuccess(ViaCepResponse viaCep) {
                runOnUiThread(() -> {
                    setCarregando(false);

                    // 1. Extraímos os dados do DTO retornado pela
                    // API
                    // (Os nomes dos métodos dependem de como você mapeou no ViaCepResponse)
                    String cidade = viaCep.getLocalidade(); // O ViaCEP chama cidade de "localidade"
                    String estado = viaCep.getUf();
                    String bairro = viaCep.getBairro();
                    String rua = viaCep.getLogradouro(); // Se você tiver um campo de rua, pode usar também!

                    // 2. Populamos os campos (com proteção contra null, pois em
                    // cidades pequenas do interior o ViaCEP não retorna bairro/rua)
                    if (cidade != null) edtCidade.setText(cidade);
                    if (estado != null) edtEstado.setText(estado);

                    if (bairro != null && !bairro.trim().isEmpty()) {
                        edtBairro.setText(bairro);
                        edtTelefone.requestFocus();
                    } else {
                        edtBairro.setText(""); // Limpa se vier lixo
                        edtBairro.requestFocus();
                    }
                });
            }

            @Override
            public void onError(String motivo) {
                runOnUiThread(() -> {
                    setCarregando(false);
                    // Avisa o usuário mas não bloqueia — ele pode preencher manualmente
                    Toast.makeText(
                            CompletarPerfilProdutorActivity.this,
                            motivo + "\nPreencha cidade e estado manualmente.",
                            Toast.LENGTH_LONG
                    ).show();
                    edtCidade.requestFocus();
                });
            }
        });
    }

    private void configurarBotoes() {
        btnConcluir.setOnClickListener(v -> tentarSalvar());

        btnPular.setOnClickListener(v -> irParaHome());
    }

    private void tentarSalvar() {
        String bairro = edtBairro.getText().toString().trim();
        String cep = edtCep.getText().toString().trim();
        String cidade = edtCidade.getText().toString().trim();
        String estado = edtEstado.getText().toString().trim();
        String telefone = edtTelefone.getText().toString().trim();
        String descricao = edtDescricao.getText().toString().trim();

        // Campos obrigatórios: telefone, cidade e estado.
        // CEP e descrição são complementares — não bloqueiam o cadastro.
        if (telefone.isEmpty()) {
            edtTelefone.setError("Informe seu telefone");
            edtTelefone.requestFocus();
            return;
        }

        if (cidade.isEmpty()) {
            edtCidade.setError("Informe sua cidade");
            edtCidade.requestFocus();
            return;
        }

        if (estado.isEmpty()) {
            edtEstado.setError("Informe seu estado");
            edtEstado.requestFocus();
            return;
        }

        setCarregando(true);

        // 1. Monta o DTO com os dados da tela
        CompletarPerfilComercioDTO dto = new CompletarPerfilComercioDTO();
        dto.setTelefone(telefone);
        dto.setCep(cep);
        dto.setBairro(bairro);
        dto.setCidade(cidade);
        dto.setEstado(estado);
        dto.setDescricao(descricao);
        // (Ajuste os setters acima caso a sua classe DTO use nomes diferentes)

        // 2. Chama o repository passando o DTO e o novo BaseCallback<Void>
        comercioRepository.completarPerfil(dto, new BaseCallback<Void>() {
            @Override
            public void onSuccess(Void unused) { // Mudança para receber Void
                runOnUiThread(() -> {
                    setCarregando(false);
                    Toast.makeText(
                            CompletarPerfilProdutorActivity.this,
                            "Perfil completo! Bem-vindo ao Hortlink 🌱",
                            Toast.LENGTH_SHORT
                    ).show();
                    irParaHome();
                });
            }

            @Override
            public void onError(String erro) {
                runOnUiThread(() -> {
                    setCarregando(false);
                    Toast.makeText(
                            CompletarPerfilProdutorActivity.this,
                            "Erro ao salvar perfil: " + erro,
                            Toast.LENGTH_LONG
                    ).show();
                });
            }
        });
    }

    private void preencherSeEdicao() {
        boolean modoEdicao = getIntent().getBooleanExtra("modo_edicao", false);
        if (!modoEdicao) return;

        setCarregando(true);

        // O seu Retrofit vai esperar receber um CompletarPerfilComercioDTO
        comercioRepository.buscarPerfil(new BaseCallback<CompletarPerfilComercioDTO>() {
            @Override
            public void onSuccess(CompletarPerfilComercioDTO perfil) {
                runOnUiThread(() -> {
                    setCarregando(false);

                    // Preenchemos todos os campos perfeitamente!
                    if (perfil.getTelefone() != null) edtTelefone.setText(perfil.getTelefone());
                    if (perfil.getCep() != null) edtCep.setText(perfil.getCep());
                    if (perfil.getEstado() != null) edtEstado.setText(perfil.getEstado());
                    if (perfil.getCidade() != null) edtCidade.setText(perfil.getCidade());
                    if (perfil.getBairro() != null) edtBairro.setText(perfil.getBairro());
                    if (perfil.getDescricao() != null) edtDescricao.setText(perfil.getDescricao());
                });
            }

            @Override
            public void onError(String erro) {
                runOnUiThread(() -> {
                    setCarregando(false);
                    Toast.makeText(CompletarPerfilProdutorActivity.this,
                            "Erro ao carregar dados antigos", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }


    // ─── Helpers de UI ────────────────────────────────────────────────

    /**
     * Alterna entre estado de carregamento e estado interativo.
     * Centralizado aqui para não repetir em cada callback.
     */
    private void setCarregando(boolean carregando) {
        progressBar.setVisibility(carregando ? View.VISIBLE : View.GONE);
        btnConcluir.setEnabled(!carregando);
        btnPular.setEnabled(!carregando);
        edtCep.setEnabled(!carregando);
    }

    private void irParaHome() {
        startActivity(new Intent(this, RoleRouterActivity.class));
        finish();
    }

}