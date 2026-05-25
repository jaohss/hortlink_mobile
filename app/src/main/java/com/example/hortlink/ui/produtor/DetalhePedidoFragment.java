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
import com.example.hortlink.data.model.Pedido;
import com.example.hortlink.data.model.Usuario;
import com.example.hortlink.data.repository.PedidoRepository;
import com.example.hortlink.data.repository.UsuarioRepository;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;


public class DetalhePedidoFragment extends BottomSheetDialogFragment {

    private static final String ARG_PEDIDO_ID = "pedido_id";
    private static final String ARG_COMPRADOR_ID = "comprador_id";
    private static final String ARG_STATUS = "status";
    private static final String ARG_TOTAL = "total";
    private static final String ARG_ITENS_RESUMO = "itens_resumo";

    // Callback para avisar o Fragment pai quando o status mudar
    public interface OnStatusAtualizadoListener {
        void onStatusAtualizado(String pedidoId, String novoStatus);
    }

    private OnStatusAtualizadoListener statusListener;

    // Dados do pedido passados via args (sem serializar o objeto inteiro)
    private String pedidoId;
    private String compradorId;
    private String statusAtual;
    private double valorTotal;
    private Pedido pedidoCompleto; // referência para inflar itens

    private final UsuarioRepository usuarioRepo = new UsuarioRepository();
    private final PedidoRepository  pedidoRepo  = new PedidoRepository();

    // ─── Factory ──────────────────────────────────────────────────────

    public static DetalhePedidoFragment newInstance(Pedido pedido) {
        DetalhePedidoFragment f = new DetalhePedidoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PEDIDO_ID,    pedido.getId());
        args.putString(ARG_COMPRADOR_ID, pedido.getCompradorId());
        args.putString(ARG_STATUS,       pedido.getStatus());
        args.putDouble(ARG_TOTAL,        pedido.getValorTotal());
        f.setArguments(args);
        // Guarda referência ao objeto para acessar itens
        f.pedidoCompleto = pedido;
        return f;
    }

    public void setOnStatusAtualizadoListener(OnStatusAtualizadoListener l) {
        this.statusListener = l;
    }

    // ─── Ciclo de vida ────────────────────────────────────────────────

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

        pedidoId   = args.getString(ARG_PEDIDO_ID);
        compradorId = args.getString(ARG_COMPRADOR_ID);
        statusAtual = args.getString(ARG_STATUS);
        valorTotal  = args.getDouble(ARG_TOTAL);

        // Views
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

        // IDs dos grupos que ficam ocultos até carregar
        int[] toShow = {
                R.id.labelComprador, R.id.divider1,
                R.id.labelItens,     R.id.divider2,
                R.id.layoutPagamento, R.id.layoutTotal
        };

        // Dados estáticos — já disponíveis
        txtPedidoId.setText("#" + pedidoId.substring(0, 8).toUpperCase());
        txtStatusDetalhe.setText(statusAtual != null ? statusAtual.toUpperCase() : "");
        aplicarCorStatus(txtStatusDetalhe, statusAtual);
        txtTotalDetalhe.setText(String.format("R$ %.2f", valorTotal));
        txtFormaPagamento.setText("Pix"); // fixo por enquanto — adapte quando tiver coluna

        // Itens já vêm no objeto
        if (pedidoCompleto != null && pedidoCompleto.getItens() != null) {
            for (Pedido.ItemPedido item : pedidoCompleto.getItens()) {
                View linha = LayoutInflater.from(getContext()).inflate(R.layout.item_detalhe_linha, containerItens, false);
                ((TextView) linha.findViewById(R.id.txtLinhaQtd)).setText(item.quantidade + "x");
                ((TextView) linha.findViewById(R.id.txtLinhaNome)).setText(item.nomeProduto);
                ((TextView) linha.findViewById(R.id.txtLinhaSubtotal)).setText(String.format("R$ %.2f", item.getSubtotal()));
                containerItens.addView(linha);
            }
        }

        // Botão aceitar — só mostra se ainda está como "pendente"
        if ("pendente".equals(statusAtual)) {
            btnAceitar.setVisibility(View.VISIBLE);
            btnAceitar.setOnClickListener(v -> aceitarPedido(btnAceitar, txtStatusDetalhe));
        }

        // Busca dados do comprador
        usuarioRepo.buscarPorId(compradorId, new UsuarioRepository.Callback() {
            @Override
            public void onSuccess(Usuario usuario) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    progressDetalhe.setVisibility(View.GONE);

                    // Mostra seções
                    for (int id : toShow) view.findViewById(id).setVisibility(View.VISIBLE);
                    layoutInfoComprador.setVisibility(View.VISIBLE);

                    txtNomeComprador.setText(
                            usuario.nome != null && !usuario.nome.isEmpty()
                                    ? usuario.nome : "Não informado");

                    txtTelefone.setText(
                            usuario.telefone != null && !usuario.telefone.isEmpty()
                                    ? usuario.telefone : "Não informado");

                    txtEmailComprador.setText(
                            usuario.email != null && !usuario.email.isEmpty()
                                    ? usuario.email : "Não informado");

                    // Endereço: cidade + estado
                    String cidade = usuario.cidade != null ? usuario.cidade : "";
                    String estado = usuario.estado != null ? usuario.estado : "";
                    if (!cidade.isEmpty() || !estado.isEmpty()) {
                        txtEndereco.setText(cidade + ((!cidade.isEmpty() && !estado.isEmpty()) ? ", " : "") + estado);
                        btnMapa.setVisibility(View.VISIBLE);

                        // Abre Google Maps com busca pelo endereço
                        btnMapa.setOnClickListener(v -> {
                            String query = Uri.encode(cidade + " " + estado + " " + usuario.cep);
                            Intent intent = new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("geo:0,0?q=" + query));
                            intent.setPackage("com.google.android.apps.maps");
                            if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
                                startActivity(intent);
                            } else {
                                // Fallback: abre no browser se Maps não estiver instalado
                                startActivity(new Intent(Intent.ACTION_VIEW,
                                        Uri.parse("https://maps.google.com/?q=" + query)));
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
                    Toast.makeText(getContext(),
                            "Não foi possível carregar dados do comprador", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    // ─── Aceitar pedido ───────────────────────────────────────────────

    private void aceitarPedido(MaterialButton btnAceitar, TextView txtStatus) {
        btnAceitar.setEnabled(false);
        btnAceitar.setText("Enviando…");

        pedidoRepo.atualizarStatus(pedidoId, "aceito", new PedidoRepository.Callback() {
            @Override
            public void onSuccess(String resultado) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    txtStatus.setText("ACEITO");
                    aplicarCorStatus(txtStatus, "aceito");
                    btnAceitar.setVisibility(View.GONE);
                    Toast.makeText(getContext(),
                            "Pedido marcado como aceito ✓", Toast.LENGTH_SHORT).show();

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
                    Toast.makeText(getContext(),
                            "Erro ao atualizar pedido", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    // ─── Cor do badge de status ───────────────────────────────────────

    private void aplicarCorStatus(TextView view, String status) {
        if (status == null || view.getBackground() == null) return;
        int cor;
        switch (status) {
            case "pendente":      cor = 0xFFFFA000; break;
            case "aceito":   cor = 0xFF1565C0; break;
            case "entregue":  cor = 0xFF2E7D32; break;
            case "cancelado": cor = 0xFFC62828; break;
            default:          cor = 0xFF616161; break;
        }
        view.getBackground().setTint(cor);
    }
}