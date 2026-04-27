package com.example.hortlink.activities;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.hortlink.BancoHelper;
import com.example.hortlink.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EditarProdutosFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditarProdutosFragment extends Fragment {

    private static final String ARG_PRODUTO_ID = "produto_id";

    private EditText edtNome, edtDescricao, edtPreco;
    private ImageView imgProduto;
    private BancoHelper database;
    private int produtoId;
    private Uri imagemSelecionada;

    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    imagemSelecionada = uri;
                    imgProduto.setImageURI(uri);
                }
            });

    // ✅ Método pra criar o fragment já passando o ID do produto
    public static EditarProdutosFragment newInstance(int produtoId) {
        EditarProdutosFragment fragment = new EditarProdutosFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PRODUTO_ID, produtoId);
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

        database = new BancoHelper(getContext());

        edtNome      = view.findViewById(R.id.edtNome);
        edtDescricao = view.findViewById(R.id.edtDescricao);
        edtPreco     = view.findViewById(R.id.edtPreco);
        imgProduto   = view.findViewById(R.id.imgProduto);

        // Pega o ID do produto passado
        if (getArguments() != null) {
            produtoId = getArguments().getInt(ARG_PRODUTO_ID, -1);
        }

        // Carrega os dados do produto nos campos
        carregarProduto();

        // Troca de foto
        imgProduto.setOnClickListener(v -> pickImage.launch("image/*"));

        // Botão atualizar
        view.findViewById(R.id.btnAtualizar).setOnClickListener(v -> atualizarProduto());
    }

    private void carregarProduto() {
        SQLiteDatabase db = database.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM produtos WHERE id = ?",
                new String[]{String.valueOf(produtoId)});

        if (cursor.moveToFirst()) {
            edtNome.setText(cursor.getString(cursor.getColumnIndexOrThrow("nome")));
            edtDescricao.setText(cursor.getString(cursor.getColumnIndexOrThrow("descricao")));
            edtPreco.setText(String.valueOf(cursor.getDouble(cursor.getColumnIndexOrThrow("preco"))));

            String foto = cursor.getString(cursor.getColumnIndexOrThrow("foto"));
            if (foto != null && !foto.isEmpty()) {
                imgProduto.setImageURI(Uri.parse(foto));
            } else {
                imgProduto.setImageResource(R.drawable.hortlink_logo);
            }
        }
        cursor.close();
    }

    private void atualizarProduto() {
        String nome      = edtNome.getText().toString().trim();
        String descricao = edtDescricao.getText().toString().trim();
        String precoStr  = edtPreco.getText().toString().trim();

        if (nome.isEmpty()) { edtNome.setError("Informe o nome"); return; }
        if (precoStr.isEmpty()) { edtPreco.setError("Informe o preço"); return; }

        double preco = Double.parseDouble(precoStr.replace(",", "."));
        String foto  = imagemSelecionada != null ? imagemSelecionada.toString() : "";

        int resultado = database.atualizarProduto(produtoId, nome, descricao, preco, foto);

        if (resultado > 0) {
            Toast.makeText(getContext(), "Produto atualizado!", Toast.LENGTH_SHORT).show();
            // Volta para o fragment anterior
            requireActivity().getSupportFragmentManager().popBackStack();
        } else {
            Toast.makeText(getContext(), "Erro ao atualizar!", Toast.LENGTH_SHORT).show();
        }
    }
}