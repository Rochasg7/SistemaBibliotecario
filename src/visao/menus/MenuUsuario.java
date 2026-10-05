package visao.menus;

import controle.UsuarioControle;
import modelo.Emprestimo;
import modelo.Usuario;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import visao.telas.TelaMinhasReservas;
import visao.telas.TelaReservarLivro;
import visao.telas.TelaSolicitarEmprestimo;

public class MenuUsuario extends JFrame {
    private int idUsuario;

    public MenuUsuario(int idUsuario) {
        this.idUsuario = idUsuario;

        // 1. Buscar o nome do usuário pelo ID
        Usuario usuario = UsuarioControle.obterUsuario(idUsuario);
        String nomeUsuario = (usuario != null) ? usuario.getNome() : "Desconhecido";

        setTitle("Menu Usuário - " + nomeUsuario);
        setSize(800, 600);
        setMinimumSize(new Dimension(700, 520));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 2. Botões (no lugar da antiga barra de menu no topo)
        BotaoMenu btnSolicitarEmprestimo = new BotaoMenu("Solicitar Empréstimo");
        BotaoMenu btnReservarLivro = new BotaoMenu("Reservar Livro");
        BotaoMenu btnMinhasReservas = new BotaoMenu("Minhas Reservas");
        BotaoMenu btnHistorico = new BotaoMenu("Histórico de Leitura");
        BotaoMenu btnSair = new BotaoMenu("Sair (Voltar ao Menu Inicial)", BotaoMenu.VERMELHO, false);

        // 3. Cartão central com os botões agrupados, sobre a imagem de fundo
        PainelCartao cartao = new PainelCartao("Olá, " + nomeUsuario + "!", "O que você deseja fazer?");
        cartao.adicionarSecao("Livros & Ações");
        cartao.adicionarLinhaDeBotoes(btnSolicitarEmprestimo, btnReservarLivro);
        cartao.adicionarSecao("Minha Conta");
        cartao.adicionarLinhaDeBotoes(btnMinhasReservas, btnHistorico);
        cartao.adicionar(btnSair, 26);

        PainelFundo fundo = new PainelFundo();
        fundo.add(cartao);
        setContentPane(fundo);

        // ==========================================
        // 4. Configurar as Ações (ActionListeners)
        // ==========================================

        btnSolicitarEmprestimo.addActionListener(e -> {
            dispose();
            new TelaSolicitarEmprestimo(idUsuario);
        });

        btnReservarLivro.addActionListener(e -> {
            dispose();
            new TelaReservarLivro(idUsuario);
        });

        btnMinhasReservas.addActionListener(e -> {
            dispose();
            new TelaMinhasReservas(idUsuario);
        });

        btnHistorico.addActionListener(e -> {
            Usuario usuarioLogado = UsuarioControle.obterUsuario(idUsuario);
            if (usuarioLogado != null) {
                usuarioLogado.consultarEmprestimos();
            }

            List<Emprestimo> historico = UsuarioControle.obterHistoricoLeitura(idUsuario);

            if (historico.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Você ainda não concluiu nenhuma leitura.");
            } else {
                StringBuilder sb = new StringBuilder();
                for (Emprestimo emp : historico) {
                    sb.append("📖 ").append(emp.getLivro().getTitulo())
                            .append(" - ").append(emp.getLivro().getAutor()).append("\n");
                }

                JTextArea textArea = new JTextArea(sb.toString());
                textArea.setEditable(false);
                textArea.setOpaque(false);

                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setPreferredSize(new Dimension(300, 200));
                scrollPane.setBorder(BorderFactory.createEmptyBorder());

                JOptionPane.showMessageDialog(this, scrollPane, "Meus Livros Lidos", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        btnSair.addActionListener(e -> {
            dispose();
            new MenuInicial();
        });

        setVisible(true);
    }
}
