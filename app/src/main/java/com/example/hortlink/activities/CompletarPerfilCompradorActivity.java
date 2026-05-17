package com.example.hortlink.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.hortlink.R;
import com.example.hortlink.data.model.Usuario;
import com.example.hortlink.data.repository.UsuarioRepository;
import com.example.hortlink.services.ViacepService;

import org.json.JSONObject;

public class CompletarPerfilCompradorActivity extends AppCompatActivity {

    // ─── Views ────────────────────────────────────────────────────────
    private EditText edtCep, edtCidade, edtEstado ,edtBairro, edtReferencia, edtTelefone;
    private Spinner spinnerGenero;
    private Button btnConcluir;
    private Button btnPular;
    private ProgressBar progressBar;

    // ─── Dependências ─────────────────────────────────────────────────
    private final UsuarioRepository usuarioRepository = new UsuarioRepository();

    // ─── Estado ───────────────────────────────────────────────────────
    private String uid;

    // ─── Lifecycle ────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_completar_perfil_comprador);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        uid = getIntent().getStringExtra("uid");

        bindViews();
        configurarSpinnerGenero();
        configurarViaCep();
        configurarBotoes();
    }

    private void bindViews() {
        edtCep        = findViewById(R.id.edtCep);
        edtCidade     = findViewById(R.id.edtCidade);
        edtEstado     = findViewById(R.id.edtEstado);
        edtBairro     = findViewById(R.id.edtBairro);
        edtReferencia = findViewById(R.id.edtReferencia);
        edtTelefone   = findViewById(R.id.edtTelefone);
        spinnerGenero = findViewById(R.id.spinnerGenero);
        btnConcluir   = findViewById(R.id.btnConcluir);
        btnPular      = findViewById(R.id.btnPular);
        progressBar   = findViewById(R.id.progressBar);

        progressBar.setVisibility(View.GONE);

        // Cidade e estado preenchidos pelo ViaCEP, mas editáveis para fallback manual
        edtCidade.setEnabled(true);
        edtEstado.setEnabled(true);
    }

    // ─── Spinner de gênero ────────────────────────────────────────────
    //
    // Usa as constantes de Usuario para não duplicar os valores em string.
    // O primeiro item é um placeholder não selecionável — força o usuário
    // a fazer uma escolha consciente sem deixar o campo vazio por engano.

    private void configurarSpinnerGenero() {
        String[] opcoes = {
                "Selecione...",
                Usuario.GENERO_MASCULINO,
                Usuario.GENERO_FEMININO,
                Usuario.GENERO_NAO_INFORMAR
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                opcoes
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGenero.setAdapter(adapter);
        spinnerGenero.setSelection(0); // começa no placeholder
    }

    // ─── ViaCEP ───────────────────────────────────────────────────────

    private void configurarViaCep() {
        edtCep.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String cepLimpo = s.toString().replaceAll("[^0-9]", "");
                if (cepLimpo.length() == 8) {
                    buscarCep(cepLimpo);
                }
            }
        });
    }

    private void buscarCep(String cep) {
        setCarregando(true);

        ViacepService.buscar(cep, new ViacepService.Callback() {

            @Override
            public void onSuccess(String bairro, String cidade, String estado) {
                runOnUiThread(() -> {
                    setCarregando(false);
                    edtCidade.setText(cidade);
                    edtEstado.setText(estado);
                    // Foca no bairro — próximo campo lógico após localização
                    edtBairro.setText(bairro);
                });
            }

            @Override
            public void onError(String motivo) {
                runOnUiThread(() -> {
                    setCarregando(false);
                    Toast.makeText(
                            CompletarPerfilCompradorActivity.this,
                            motivo + "\nPreencha cidade e estado manualmente.",
                            Toast.LENGTH_LONG
                    ).show();
                    edtCidade.requestFocus();
                });
            }
        });
    }

    // ─── Botões ───────────────────────────────────────────────────────

    private void configurarBotoes() {
        btnConcluir.setOnClickListener(v -> tentarSalvar());
        btnPular.setOnClickListener(v -> irParaHome());
    }

    private void tentarSalvar() {
        String cep       = edtCep.getText().toString().trim();
        String cidade    = edtCidade.getText().toString().trim();
        String estado    = edtEstado.getText().toString().trim();
        String bairro    = edtBairro.getText().toString().trim();
        String referencia = edtReferencia.getText().toString().trim();
        String telefone  = edtTelefone.getText().toString().trim();

        // Gênero: posição 0 é o placeholder "Selecione..."
        int posicaoGenero = spinnerGenero.getSelectedItemPosition();
        String genero = posicaoGenero > 0
                ? spinnerGenero.getSelectedItem().toString()
                : "";

        // ── Validações ────────────────────────────────────────────────
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

        if (bairro.isEmpty()) {
            edtBairro.setError("Informe seu bairro");
            edtBairro.requestFocus();
            return;
        }

        // Gênero é opcional — "Prefiro não informar" já é uma escolha válida,
        // mas o placeholder "Selecione..." indica que o usuário não tocou no campo.
        // Não bloqueamos o cadastro por isso, apenas salvamos vazio se não escolheu.

        setCarregando(true);

        try {
            JSONObject campos = new JSONObject();
            campos.put("telefone",   telefone);
            campos.put("cep",        cep);
            campos.put("cidade",     cidade);
            campos.put("estado",     estado);
            campos.put("bairro",     bairro);
            campos.put("referencia", referencia); // pode ser vazio — campo opcional
            campos.put("genero",     genero);     // vazio se não selecionou

            usuarioRepository.atualizarPerfil(uid, campos, new UsuarioRepository.CallbackSimples() {

                @Override
                public void onSuccess() {
                    runOnUiThread(() -> {
                        setCarregando(false);
                        Toast.makeText(
                                CompletarPerfilCompradorActivity.this,
                                "Perfil completo! Bem-vindo ao Hortlink 🛒",
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
                                CompletarPerfilCompradorActivity.this,
                                "Erro ao salvar perfil: " + erro,
                                Toast.LENGTH_LONG
                        ).show();
                    });
                }
            });

        } catch (Exception e) {
            setCarregando(false);
            Toast.makeText(this, "Erro inesperado: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // ─── Helpers de UI ────────────────────────────────────────────────

    private void setCarregando(boolean carregando) {
        progressBar.setVisibility(carregando ? View.VISIBLE : View.GONE);
        btnConcluir.setEnabled(!carregando);
        btnPular.setEnabled(!carregando);
        edtCep.setEnabled(!carregando);
    }

    private void irParaHome() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

}