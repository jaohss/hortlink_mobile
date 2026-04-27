package com.example.hortlink.activities;

import android.app.AlertDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hortlink.BancoHelper;
import com.example.hortlink.R;
import com.example.hortlink.adapters.GerenciarAdapter;
import com.example.hortlink.entidades.Produto;

import java.util.ArrayList;
import java.util.List;

public class GerenciarProdutosFragment extends Fragment {

    private RecyclerView recyclerGerenciar;
    private TextView txtListaVazia;
    private BancoHelper database;
    private List<Produto> listaProdutos;
    private GerenciarAdapter adapter;

    public GerenciarProdutosFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gerenciar_produtos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        database = new BancoHelper(getContext());
        recyclerGerenciar = view.findViewById(R.id.recyclerGerenciar);
        txtListaVazia = view.findViewById(R.id.txtListaVazia);

        recyclerGerenciar.setLayoutManager(new LinearLayoutManager(getContext()));

        carregarProdutos();
    }

    @Override
    public void onResume() {
        super.onResume();
        carregarProdutos();
    }

    private void carregarProdutos() {
        listaProdutos = new ArrayList<>();

        SQLiteDatabase db = database.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM produtos", null);

        if (cursor.moveToFirst()) {
            do {
                int id           = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String nome      = cursor.getString(cursor.getColumnIndexOrThrow("nome"));
                String categoria      = cursor.getString(cursor.getColumnIndexOrThrow("categoria"));
                double preco     = cursor.getDouble(cursor.getColumnIndexOrThrow("preco"));
                String descricao = cursor.getString(cursor.getColumnIndexOrThrow("descricao"));
                String foto      = cursor.getString(cursor.getColumnIndexOrThrow("foto"));

                Produto p = new Produto(nome, preco, categoria, foto, descricao);
                p.id = id;
                listaProdutos.add(p);
            } while (cursor.moveToNext());
        }
        cursor.close();

        if (listaProdutos.isEmpty()) {
            txtListaVazia.setVisibility(View.VISIBLE);
            recyclerGerenciar.setVisibility(View.GONE);
        } else {
            txtListaVazia.setVisibility(View.GONE);
            recyclerGerenciar.setVisibility(View.VISIBLE);
        }

        // ✅ Adapter separado com as duas ações
        adapter = new GerenciarAdapter(
                listaProdutos,

                // Editar
                produto -> {
                    EditarProdutosFragment fragment = EditarProdutosFragment.newInstance(produto.id);
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.container, fragment)
                            .addToBackStack(null)
                            .commit();
                },

                // Deletar
                (produto, position) -> {
                    new AlertDialog.Builder(getContext())
                            .setTitle("Deletar produto")
                            .setMessage("Tem certeza que quer deletar \"" + produto.nome + "\"?")
                            .setPositiveButton("Deletar", (dialog, which) -> {
                                database.excluirProduto(produto.id);
                                listaProdutos.remove(position);
                                adapter.notifyItemRemoved(position);
                                adapter.notifyItemRangeChanged(position, listaProdutos.size());
                                Toast.makeText(getContext(), "Produto deletado!", Toast.LENGTH_SHORT).show();

                                if (listaProdutos.isEmpty()) {
                                    txtListaVazia.setVisibility(View.VISIBLE);
                                    recyclerGerenciar.setVisibility(View.GONE);
                                }
                            })
                            .setNegativeButton("Cancelar", null)
                            .show();
                }
        );

        recyclerGerenciar.setAdapter(adapter);
    }
}