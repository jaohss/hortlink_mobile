package com.example.hortlink.ui.produtor;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.hortlink.R;
import com.example.hortlink.data.dto.PerfilCompradorDTO;
import com.example.hortlink.data.model.Pedido;
import com.example.hortlink.data.repository.PedidoRepository;
import com.example.hortlink.data.repository.UsuarioRepository;
import com.example.hortlink.service.BaseCallback;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.Locale;

public class DetalhePedidoFragment extends BottomSheetDialogFragment {

    private static final String ARG_PEDIDO_ID = "pedido_id";
    private static final String ARG_COMPRADOR_ID = "comprador_id";
    private static final String ARG_STATUS = "status";
    private static final String ARG_TOTAL = "total";

    public interface OnStatusAtualizadoListener {
        void onStatusAtualizado(String pedidoId, String novoStatus);
    }

    private OnStatusAtualizadoListener statusListener;

    private String pedidoId;
    private Long compradorId;
    private String statusAtual;
    private double valorTotal;
    private Pedido pedidoCompleto;

    private final UsuarioRepository usuarioRepo = new UsuarioRepository();
    private final PedidoRepository  pedidoRepo  = new PedidoRepository();

    public static DetalhePedidoFragment newInstance(Pedido pedido) {
        DetalhePedidoFragment f = new DetalhePedidoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PEDIDO_ID,    pedido.getId());
        // Ajuste aqui se o seu modelo atualizou para Long
        args.putString(ARG_COMPRADOR_ID, String.valueOf(pedido.getClienteId()));
        args.putString(ARG_STATUS,       pedido.getStatus());
        // Proteção caso o valor seja nulo
        double total = pedido.getValorTotal() != null ? pedido.getValorTotal().doubleValue() : 0.0;
        args.putDouble(ARG_TOTAL,        total);
        f.setArguments(args);
        f.pedidoCompleto = pedido;
        return f;
    }

    public void setOnStatusAtualizadoListener(OnStatusAtualizadoListener l) {
        this.statusListener = l;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detalhe_pedido, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        if (args == null) { dismiss(); return; }

        pedidoId    = args.getString(ARG_PEDIDO_ID);
        compradorId = args.getLong(ARG_COMPRADOR_ID);
        statusAtual = args.getString(ARG_STATUS);
        valorTotal  = args.getDouble(ARG_TOTAL);

        TextView txtPedidoId       = view.findViewById(R.id.txtPedidoId);
        TextView txtStatusDetalhe  = view.findViewById(R.id.txtStatusDetalhe);
        TextView txtNomeComprador  = view.findViewById(R.id.txtNomeComprador);
        TextView txtTelefone       = view.findViewById(R.id.txtTelefone);
        TextView txtEmailComprador = view.findViewById(R.id.txtEmailComprador);
        TextView txtEndereco       = view.findViewById(R.id.txtEndereco);
        TextView txtFormaPagamento = view.findViewById(R.id.txtFormaPagamento);
        TextView txtTotalDetalhe   = view.findViewById(R.id.txtTotalDetalhe);
        LinearLayout containerItens      = view.findViewById(R.id.containerItens);
        LinearLayout layoutInfoComprador = view.findViewById(R.id.layoutInfoComprador);
        LinearLayout layoutEndereco      = view.findViewById(R.id.layoutEndereco);
        View progressDetalhe       = view.findViewById(R.id.progressDetalhe);
        MaterialButton btnAceitar  = view.findViewById(R.id.btnAceitarDetalhe);
        MaterialButton btnMapa     = view.findViewById(R.id.btnVerMapa);

        int[] toShow = {
                R.id.labelComprador, R.id.divider1,
                R.id.labelItens,     R.id.divider2,
                R.id.layoutPagamento, R.id.layoutTotal
        };

        NumberFormat formatoMoeda = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

        txtPedidoId.setText(pedidoId != null ? "#" + pedidoId.toUpperCase() : "");
        txtStatusDetalhe.setText(statusAtual != null ? statusAtual.toUpperCase() : "");
        aplicarCorStatus(txtStatusDetalhe, statusAtual);

        // Uso do NumberFormat para o total
        txtTotalDetalhe.setText(formatoMoeda.format(valorTotal));
        txtFormaPagamento.setText("Pix");

        if (pedidoCompleto != null && pedidoCompleto.getItens() != null) {
            for (Pedido.ItemPedido item : pedidoCompleto.getItens()) {
                View linha = LayoutInflater.from(getContext()).inflate(R.layout.item_detalhe_linha, containerItens, false);
                ((TextView) linha.findViewById(R.id.txtLinhaQtd)).setText(item.quantidade + "x");
                ((TextView) linha.findViewById(R.id.txtLinhaNome)).setText(item.nomeProduto);

                // Uso do NumberFormat para o subtotal do item
                double subtotalItem = item.subtotal != null ? item.subtotal.doubleValue() : 0.0;
                ((TextView) linha.findViewById(R.id.txtLinhaSubtotal)).setText(formatoMoeda.format(subtotalItem));

                containerItens.addView(linha);
            }
        }

        if ("pendente".equalsIgnoreCase(statusAtual)) {
            btnAceitar.setVisibility(View.VISIBLE);
            btnAceitar.setOnClickListener(v -> aceitarPedido(btnAceitar, txtStatusDetalhe));
        }

        // CORREÇÃO 1: Adicionado o "new" e ajustado o tipo esperado
        usuarioRepo.obterDetalhesCliente(compradorId, new BaseCallback<PerfilCompradorDTO>() {
            @Override
            public void onSuccess(PerfilCompradorDTO compradorDTO) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    progressDetalhe.setVisibility(View.GONE);

                    for (int id : toShow) view.findViewById(id).setVisibility(View.VISIBLE);
                    layoutInfoComprador.setVisibility(View.VISIBLE);

                    // CORREÇÃO 2: Uso dos Getters (Certifique-se de adicionar nome e email no DTO!)
                    String nome = compradorDTO.getNome();
                    String email = compradorDTO.getEmail();
                    String telefone = compradorDTO.getTelefone();
                    String cidade = compradorDTO.getCidade();
                    String estado = compradorDTO.getEstado();
                    String cep = compradorDTO.getCep();

                    txtNomeComprador.setText(nome != null && !nome.isEmpty() ? nome : "Não informado");
                    txtTelefone.setText(telefone != null && !telefone.isEmpty() ? telefone : "Não informado");
                    txtEmailComprador.setText(email != null && !email.isEmpty() ? email : "Não informado");

                    if ((cidade != null && !cidade.isEmpty()) || (estado != null && !estado.isEmpty())) {
                        String endFormatado = (cidade != null ? cidade : "") +
                                ((cidade != null && estado != null && !cidade.isEmpty() && !estado.isEmpty()) ? ", " : "") +
                                (estado != null ? estado : "");

                        txtEndereco.setText(endFormatado);
                        btnMapa.setVisibility(View.VISIBLE);

                        btnMapa.setOnClickListener(v -> {
                            String queryCep = cep != null ? cep : "";
                            String query = Uri.encode(endFormatado + " " + queryCep);
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + query));
                            intent.setPackage("com.google.android.apps.maps");
                            if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
                                startActivity(intent);
                            } else {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?q=" + query)));
                            }
                        });
                    } else {
                        layoutEndereco.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onError(String erro) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    progressDetalhe.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Não foi possível carregar dados do comprador", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void aceitarPedido(MaterialButton btnAceitar, TextView txtStatus) {
        btnAceitar.setEnabled(false);
        btnAceitar.setText("Enviando…");

        // Verifique se o seu PedidoRepository usa BaseCallback ou uma Callback própria. Ajuste se necessário.
        pedidoRepo.atualizarStatus(pedidoId, "aceito", new BaseCallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    txtStatus.setText("ACEITO");
                    aplicarCorStatus(txtStatus, "aceito");
                    btnAceitar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Pedido marcado como aceito ✓", Toast.LENGTH_SHORT).show();

                    if (statusListener != null) {
                        statusListener.onStatusAtualizado(pedidoId, "aceito");
                    }
                });
            }

            @Override
            public void onError(String erro) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    btnAceitar.setEnabled(true);
                    btnAceitar.setText("✓  Aceitar Pedido");
                    Toast.makeText(getContext(), "Erro ao atualizar pedido", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void aplicarCorStatus(TextView view, String status) {
        if (status == null || view.getBackground() == null) return;
        int cor;
        // CORREÇÃO 4: toLowerCase para segurança
        switch (status.toLowerCase()) {
            case "pendente":      cor = 0xFFFFA000; break;
            case "aceito":        cor = 0xFF1565C0; break;
            case "entregue":      cor = 0xFF2E7D32; break;
            case "cancelado":     cor = 0xFFC62828; break;
            default:              cor = 0xFF616161; break;
        }
        view.getBackground().setTint(cor);
    }
}