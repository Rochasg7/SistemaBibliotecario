package visao.telas;

import controle.ReservaControle;
import controle.UsuarioControle;
import modelo.Livro;
import modelo.Usuario;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class TelaCadastroReserva extends JFrame {

    public TelaCadastroReserva(int idBibliotecaria) {
        setTitle("Cadastrar Reserva");
        setSize(560, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel painel = new JPanel(new GridLayout(4, 2, 10, 10));
        painel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        List<Livro> livros = ReservaControle.listarLivrosParaReserva();
        List<Usuario> usuarios = UsuarioControle.listarTodosUsuarios();

        JLabel lblLivro = new JLabel("Livro:");
        JComboBox<String> comboLivro = new JComboBox<>(
                livros.isEmpty()
                        ? new String[]{"Nenhum livro disponível para reserva"}
                        : livros.stream()
                            .map(l -> l.getIdLivro() + " - " + l.getTitulo() + " (" + l.getStatus() + ")")
                            .toArray(String[]::new)
        );
        comboLivro.setEnabled(!livros.isEmpty());

        JLabel lblUsuario = new JLabel("Usuário:");
        JComboBox<String> comboUsuario = new JComboBox<>(
                usuarios.isEmpty()
                        ? new String[]{"Nenhum usuário cadastrado"}
                        : usuarios.stream()
                            .map(u -> u.getIdUsuario() + " - " + u.getNome())
                            .toArray(String[]::new)
        );
        comboUsuario.setEnabled(!usuarios.isEmpty());

        JLabel lblData = new JLabel("Data (dd/MM/yyyy):");
        JTextField txtData = new JTextField(new SimpleDateFormat("dd/MM/yyyy").format(new Date()));

        JButton btnSalvar = new JButton("Salvar");
        btnSalvar.addActionListener(e -> {
            Integer idLivro = livros.isEmpty() ? null : livros.get(comboLivro.getSelectedIndex()).getIdLivro();
            Integer idUsuario = usuarios.isEmpty() ? null : usuarios.get(comboUsuario.getSelectedIndex()).getIdUsuario();
            ReservaControle.cadastrarReserva(idLivro, idUsuario, txtData.getText().trim(), this, idBibliotecaria);
        });

        JButton btnVoltar = new JButton("Voltar");
        btnVoltar.addActionListener(e -> {
            dispose();
            new visao.menus.MenuBibliotecaria(idBibliotecaria);
        });

        painel.add(lblLivro); painel.add(comboLivro);
        painel.add(lblUsuario); painel.add(comboUsuario);
        painel.add(lblData); painel.add(txtData);
        painel.add(btnVoltar); painel.add(btnSalvar);

        add(painel);
        setVisible(true);
    }
}
