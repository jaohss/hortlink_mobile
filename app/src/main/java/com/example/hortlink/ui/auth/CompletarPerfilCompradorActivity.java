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
import com.example.hortlink.data.dto.PerfilCompradorDTO;
import com.example.hortlink.data.dto.ViaCepResponse;
import com.example.hortlink.data.repository.GeoRepository;
import com.example.hortlink.data.repository.UsuarioRepository;
import com.example.hortlink.service.BaseCallback;

public class CompletarPerfilCompradorActivity extends AppCompatActivity {

    // ─── Views ────────────────────────────────────────────────────────
    private EditText edtCep, edtCidade, edtEstado ,edtBairro, edtComplemento, edtTelefone;
    private Button btnConcluir;
    private Button btnPular;
    private ProgressBar progressBar;

    // ─── Dependências ─────────────────────────────────────────────────
    private final UsuarioRepository usuarioRepository = new UsuarioRepository();
    private final GeoRepository geoRepository = new GeoRepository();

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

        bindViews();
        configurarViaCep();
        preencherSeEdicao();
        configurarBotoes();
    }

    private void bindViews() {
        edtCep         = findViewById(R.id.edtCep);
        edtCidade      = findViewById(R.id.edtCidade);
        edtEstado      = findViewById(R.id.edtEstado);
        edtBairro      = findViewById(R.id.edtBairro);
        edtComplemento = findViewById(R.id.edtComplemento);
        edtTelefone    = findViewById(R.id.edtTelefone);
        btnConcluir    = findViewById(R.id.btnConcluir);
        btnPular       = findViewById(R.id.btnPular);
        progressBar    = findViewById(R.id.progressBar);

        progressBar.setVisibility(View.GONE);

        // Cidade e estado preenchidos pelo ViaCEP, mas editáveis para fallback manual
        edtCidade.setEnabled(true);
        edtEstado.setEnabled(true);
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

        geoRepository.buscarEnderecoPorCep(cep, new BaseCallback<ViaCepResponse>() {
            @Override
            public void onSuccess(ViaCepResponse endereco) {
                runOnUiThread(() -> {
                    setCarregando(false);
                    edtCidade.setText(endereco.getLocalidade());
                    edtEstado.setText(endereco.getUf());
                    edtBairro.setText(endereco.getBairro());
                    edtComplemento.requestFocus(); // Foco vai para o complemento após o auto-preenchimento
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

    // ─── Botões e Salvamento ──────────────────────────────────────────

    private void configurarBotoes() {
        btnConcluir.setOnClickListener(v -> tentarSalvar());
        btnPular.setOnClickListener(v -> irParaHome());
    }

    private void tentarSalvar() {
        String cep         = edtCep.getText().toString().trim();
        String cidade      = edtCidade.getText().toString().trim();
        String estado      = edtEstado.getText().toString().trim();
        String bairro      = edtBairro.getText().toString().trim();
        String complemento = edtComplemento.getText().toString().trim();
        String telefone    = edtTelefone.getText().toString().trim();

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

        setCarregando(true);

        // Substituição do JSONObject pelo DTO tipado
        PerfilCompradorDTO dto = new PerfilCompradorDTO();
        dto.setTelefone(telefone);
        dto.setCep(cep);
        dto.setCidade(cidade);
        dto.setEstado(estado);
        dto.setBairro(bairro);
        dto.setComplemento(complemento.isEmpty() ? null : complemento); // Atualizado aqui

        // Chamada atualizada do novo Repositório
        usuarioRepository.atualizarPerfil(dto, new BaseCallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                runOnUiThread(() -> {
                    setCarregando(false);
                    Toast.makeText(
                            CompletarPerfilCompradorActivity.this,
                            "Perfil atualizado com sucesso!",
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
    }

    // ─── Preenchimento Automático via API ─────────────────────────────

    private void preencherSeEdicao() {
        boolean modoEdicao = getIntent().getBooleanExtra("modo_edicao", false);
        if (!modoEdicao) return;

        setCarregando(true);
        btnPular.setVisibility(View.GONE); // No modo edição, geralmente não faz sentido "Pular"

        // Busca os dados reais e atualizados do backend
        usuarioRepository.obterPerfil(new BaseCallback<PerfilCompradorDTO>() {
            @Override
            public void onSuccess(PerfilCompradorDTO perfil) {
                runOnUiThread(() -> {
                    setCarregando(false);

                    if (perfil.getTelefone() != null) edtTelefone.setText(perfil.getTelefone());
                    if (perfil.getCep() != null) edtCep.setText(perfil.getCep());
                    if (perfil.getCidade() != null) edtCidade.setText(perfil.getCidade());
                    if (perfil.getEstado() != null) edtEstado.setText(perfil.getEstado());
                    if (perfil.getBairro() != null) edtBairro.setText(perfil.getBairro());
                    if (perfil.getComplemento() != null) edtComplemento.setText(perfil.getComplemento()); // Atualizado aqui
                });
            }

            @Override
            public void onError(String erro) {
                runOnUiThread(() -> {
                    setCarregando(false);
                    Toast.makeText(
                            CompletarPerfilCompradorActivity.this,
                            "Erro ao carregar seus dados: " + erro,
                            Toast.LENGTH_SHORT
                    ).show();
                });
            }
        });
    }

    // ─── Helpers de UI ────────────────────────────────────────────────

    private void setCarregando(boolean carregando) {
        progressBar.setVisibility(carregando ? View.VISIBLE : View.GONE);
        btnConcluir.setEnabled(!carregando);
        btnPular.setEnabled(!carregando);
        edtCep.setEnabled(!carregando);
        edtTelefone.setEnabled(!carregando);
        edtCidade.setEnabled(!carregando);
        edtEstado.setEnabled(!carregando);
        edtBairro.setEnabled(!carregando);
        edtComplemento.setEnabled(!carregando);
    }

    private void irParaHome() {
        startActivity(new Intent(this, RoleRouterActivity.class));
        finish();
    }
}