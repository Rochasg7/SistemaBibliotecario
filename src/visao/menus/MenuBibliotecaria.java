package visao.menus;

import controle.BibliotecariaControle;
import modelo.Bibliotecaria;

import javax.swing.*;
import java.awt.*;

import visao.telas.TelaCadastroLivro;
import visao.telas.TelaCadastroUsuario;
import visao.telas.TelaCadastroBibliotecaria;
import visao.telas.TelaExcluirLivro;
import visao.telas.TelaExcluirUsuario;
import visao.telas.TelaExcluirBibliotecaria;
import visao.telas.TelaExcluirEmprestimo;
import visao.telas.TelaExcluirReserva;
import visao.telas.TelaRegistrarDevolucao;
import visao.telas.TelaRegistrarEmprestimo;
import visao.telas.TelaListarLivros;
import visao.telas.TelaListarUsuarios;
import visao.telas.TelaListarEmprestimos;
import visao.telas.TelaListarReservas;
import visao.telas.TelaListarBibliotecarias;
import visao.telas.TelaEditarLivro;
import visao.telas.TelaEditarUsuario;
import visao.telas.TelaEditarBibliotecaria;
import visao.telas.TelaEditarEmprestimo;
import visao.telas.TelaEditarReserva;
import visao.telas.TelaListarLivrosAtraso;
import visao.telas.TelaListarLivrosDisponiveis;
import visao.telas.TelaCadastroReserva;
import visao.telas.TelaListarReservasPendentes;

public class MenuBibliotecaria extends JFrame {
    private int idBibliotecaria;

    public MenuBibliotecaria(int idBibliotecaria) {
        this.idBibliotecaria = idBibliotecaria;

        Bibliotecaria bibliotecaria = BibliotecariaControle.obterBibliotecaria(idBibliotecaria);
        String nome = (bibliotecaria != null) ? bibliotecaria.getNome() : "Bibliotecária";

        setTitle("Menu Bibliotecária - " + nome);
        setSize(800, 600);
        setMinimumSize(new Dimension(700, 520));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 1. Itens de cada grupo (os mesmos de antes, agora em listas que abrem ao clicar no botão)
        JPopupMenu menuLivros = new JPopupMenu();
        JMenuItem itemCadastrarLivro = new JMenuItem("Cadastrar Livro");
        JMenuItem itemEditarLivro = new JMenuItem("Editar Livro");
        JMenuItem itemExcluirLivro = new JMenuItem("Excluir Livro");
        menuLivros.add(itemCadastrarLivro);
        menuLivros.add(itemEditarLivro);
        menuLivros.add(itemExcluirLivro);

        JPopupMenu menuUsuarios = new JPopupMenu();
        JMenuItem itemCadastrarUsuario = new JMenuItem("Cadastrar Usuário");
        JMenuItem itemEditarUsuario = new JMenuItem("Editar Usuário");
        JMenuItem itemExcluirUsuario = new JMenuItem("Excluir Usuário");
        menuUsuarios.add(itemCadastrarUsuario);
        menuUsuarios.add(itemEditarUsuario);
        menuUsuarios.add(itemExcluirUsuario);

        JPopupMenu menuBibliotecarias = new JPopupMenu();
        JMenuItem itemCadastrarBiblio = new JMenuItem("Cadastrar Bibliotecária");
        JMenuItem itemEditarBiblio = new JMenuItem("Editar Bibliotecária");
        JMenuItem itemExcluirBiblio = new JMenuItem("Excluir Bibliotecária");
        menuBibliotecarias.add(itemCadastrarBiblio);
        menuBibliotecarias.add(itemEditarBiblio);
        menuBibliotecarias.add(itemExcluirBiblio);

        JPopupMenu menuEmprestimos = new JPopupMenu();
        JMenuItem itemRegEmprestimo = new JMenuItem("Registrar Empréstimo");
        JMenuItem itemRegDevolucao = new JMenuItem("Registrar Devolução");
        JMenuItem itemEditarEmprestimo = new JMenuItem("Editar Empréstimo");
        JMenuItem itemExcluirEmprestimo = new JMenuItem("Excluir Empréstimo");
        menuEmprestimos.add(itemRegEmprestimo);
        menuEmprestimos.add(itemRegDevolucao);
        menuEmprestimos.addSeparator(); // Adiciona uma linha divisória
        menuEmprestimos.add(itemEditarEmprestimo);
        menuEmprestimos.add(itemExcluirEmprestimo);

        JPopupMenu menuReservas = new JPopupMenu();
        JMenuItem itemCadastrarReserva = new JMenuItem("Cadastrar Reserva");
        JMenuItem itemEditarReserva = new JMenuItem("Editar Reserva");
        JMenuItem itemExcluirReserva = new JMenuItem("Excluir Reserva");
        menuReservas.add(itemCadastrarReserva);
        menuReservas.add(itemEditarReserva);
        menuReservas.add(itemExcluirReserva);

        JPopupMenu menuRelatorios = new JPopupMenu();
        JMenuItem itemListarLivros = new JMenuItem("Listar Livros");
        JMenuItem itemLivrosDisponiveis = new JMenuItem("Livros Disponíveis");
        JMenuItem itemLivrosAtraso = new JMenuItem("Livros em Atraso");
        JMenuItem itemListarUsuarios = new JMenuItem("Listar Usuários");
        JMenuItem itemListarBiblio = new JMenuItem("Listar Bibliotecárias");
        JMenuItem itemListarEmprestimos = new JMenuItem("Listar Empréstimos");
        JMenuItem itemListarReservas = new JMenuItem("Listar Reservas");
        JMenuItem itemResPendentes = new JMenuItem("Reservas Pendentes");
        menuRelatorios.add(itemListarLivros);
        menuRelatorios.add(itemLivrosDisponiveis);
        menuRelatorios.add(itemLivrosAtraso);
        menuRelatorios.addSeparator();
        menuRelatorios.add(itemListarUsuarios);
        menuRelatorios.add(itemListarBiblio);
        menuRelatorios.addSeparator();
        menuRelatorios.add(itemListarEmprestimos);
        menuRelatorios.add(itemListarReservas);
        menuRelatorios.add(itemResPendentes);

        // 2. Botões grandes no centro da tela (no lugar da antiga barra de menu no topo)
        BotaoMenu btnLivros = botaoComLista("Livros", menuLivros);
        BotaoMenu btnUsuarios = botaoComLista("Usuários", menuUsuarios);
        BotaoMenu btnBibliotecarias = botaoComLista("Bibliotecárias", menuBibliotecarias);
        BotaoMenu btnEmprestimos = botaoComLista("Empréstimos", menuEmprestimos);
        BotaoMenu btnReservas = botaoComLista("Reservas", menuReservas);
        BotaoMenu btnRelatorios = botaoComLista("Relatórios", menuRelatorios);
        BotaoMenu btnSair = new BotaoMenu("Sair (Voltar ao Menu Inicial)", BotaoMenu.VERMELHO, false);

        PainelCartao cartao = new PainelCartao("Painel da Bibliotecária", "Olá, " + nome + "!");
        cartao.adicionarSecao("Cadastros & Edições");
        cartao.adicionarLinhaDeBotoes(btnLivros, btnUsuarios, btnBibliotecarias);
        cartao.adicionarSecao("Operações & Relatórios");
        cartao.adicionarLinhaDeBotoes(btnEmprestimos, btnReservas, btnRelatorios);
        cartao.adicionar(btnSair, 26);

        // 3. Imagem de fundo ocupando a janela inteira, com o cartão centralizado
        PainelFundo fundo = new PainelFundo();
        fundo.add(cartao);
        setContentPane(fundo);

        // ==========================================
        // 4. Configurar as Ações (ActionListeners)
        // ==========================================

        // Ações de Livros
        itemCadastrarLivro.addActionListener(e -> {
            dispose();
            new TelaCadastroLivro(idBibliotecaria);
        });
        itemEditarLivro.addActionListener(e -> {
            dispose();
            new TelaEditarLivro(idBibliotecaria);
        });
        itemExcluirLivro.addActionListener(e -> {
            dispose();
            new TelaExcluirLivro(idBibliotecaria);
        });

        // Ações de Usuários
        itemCadastrarUsuario.addActionListener(e -> {
            dispose();
            new TelaCadastroUsuario(idBibliotecaria);
        });
        itemEditarUsuario.addActionListener(e -> {
            dispose();
            new TelaEditarUsuario(idBibliotecaria);
        });
        itemExcluirUsuario.addActionListener(e -> {
            dispose();
            new TelaExcluirUsuario(idBibliotecaria);
        });

        // Ações de Bibliotecárias
        itemCadastrarBiblio.addActionListener(e -> {
            dispose();
            new TelaCadastroBibliotecaria(idBibliotecaria);
        });
        itemEditarBiblio.addActionListener(e -> {
            dispose();
            new TelaEditarBibliotecaria(idBibliotecaria);
        });
        itemExcluirBiblio.addActionListener(e -> {
            dispose();
            new TelaExcluirBibliotecaria(idBibliotecaria);
        });

        // Ações de Empréstimos
        itemRegEmprestimo.addActionListener(e -> {
            dispose();
            new TelaRegistrarEmprestimo(idBibliotecaria);
        });
        itemRegDevolucao.addActionListener(e -> {
            dispose();
            new TelaRegistrarDevolucao(idBibliotecaria);
        });
        itemEditarEmprestimo.addActionListener(e -> {
            dispose();
            new TelaEditarEmprestimo(idBibliotecaria);
        });
        itemExcluirEmprestimo.addActionListener(e -> {
            dispose();
            new TelaExcluirEmprestimo(idBibliotecaria);
        });

        // Ações de Reservas
        itemCadastrarReserva.addActionListener(e -> {
            dispose();
            new TelaCadastroReserva(idBibliotecaria);
        });
        itemEditarReserva.addActionListener(e -> {
            dispose();
            new TelaEditarReserva(idBibliotecaria);
        });
        itemExcluirReserva.addActionListener(e -> {
            dispose();
            new TelaExcluirReserva(idBibliotecaria);
        });

        // Ações de Relatórios
        itemListarLivros.addActionListener(e -> {
            dispose();
            new TelaListarLivros(idBibliotecaria);
        });
        itemLivrosDisponiveis.addActionListener(e -> {
            dispose();
            new TelaListarLivrosDisponiveis(idBibliotecaria);
        });
        itemLivrosAtraso.addActionListener(e -> {
            dispose();
            new TelaListarLivrosAtraso(idBibliotecaria);
        });
        itemListarUsuarios.addActionListener(e -> {
            dispose();
            new TelaListarUsuarios(idBibliotecaria);
        });
        itemListarBiblio.addActionListener(e -> {
            dispose();
            new TelaListarBibliotecarias(idBibliotecaria);
        });
        itemListarEmprestimos.addActionListener(e -> {
            dispose();
            new TelaListarEmprestimos(idBibliotecaria);
        });
        itemListarReservas.addActionListener(e -> {
            dispose();
            new TelaListarReservas(idBibliotecaria);
        });
        itemResPendentes.addActionListener(e -> {
            dispose();
            new TelaListarReservasPendentes(idBibliotecaria);
        });

        // Ação de Sair
        btnSair.addActionListener(e -> {
            dispose();
            new MenuInicial();
        });

        setVisible(true);
    }

    /** Cria um botão que, ao ser clicado, abre a lista de opções logo abaixo dele. */
    private BotaoMenu botaoComLista(String texto, JPopupMenu lista) {
        BotaoMenu botao = new BotaoMenu(texto, BotaoMenu.MADEIRA, true);
        botao.addActionListener(e -> {
            Dimension pref = lista.getPreferredSize();
            lista.setPopupSize(Math.max(pref.width, botao.getWidth()), pref.height);
            lista.show(botao, 0, botao.getHeight());
        });
        return botao;
    }
}
