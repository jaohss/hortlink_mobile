package com.example.hortlink.activities;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.hortlink.R;
import com.example.hortlink.adapters.PedidoAdapter;
import com.example.hortlink.data.model.Pedido;

import java.util.ArrayList;
import java.util.List;

public class PedidosFragment extends Fragment {

    private RecyclerView recyclerPedidos;
    private PedidoAdapter adapter;
    private List<Pedido> listaPedidos;

    public PedidosFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_pedidos, container, false);

        recyclerPedidos = view.findViewById(R.id.recyclerPedidos);
        recyclerPedidos.setLayoutManager(new LinearLayoutManager(getContext()));

        listaPedidos = new ArrayList<>();
        listaPedidos.add(new Pedido("João", "2x Tomate • 1x Alface", "R$ 32,00", "PENDENTE"));
        listaPedidos.add(new Pedido("Maria", "1x Cenoura • 3x Batata", "R$ 21,00", "ACEITO"));
        listaPedidos.add(new Pedido("Carlos", "5x Laranja", "R$ 18,00", "FINALIZADO"));

        adapter = new PedidoAdapter(listaPedidos);
        recyclerPedidos.setAdapter(adapter);

        return view;
    }
}