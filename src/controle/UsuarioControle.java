package controle;

import modelo.Emprestimo;
import modelo.Reserva;
import modelo.StatusReserva;
import modelo.Usuario;
import util.ManipuladorArquivos;

import javax.swing.*;
import java.util.List;
import java.util.stream.Collectors;

public class UsuarioControle {

    public static void cadastrarUsuario(String nome, String email, JFrame tela, int idBibliotecaria) {
        if (nome.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(tela, "Preencha todos os campos.");
            return;
        }

        if (!email.contains("@")) {
            JOptionPane.showMessageDialog(tela, "Email inválido.");
            return;
        }

        int id = ManipuladorArquivos.proximoId("Usuario.csv");
        Usuario usuario = new Usuario(id, nome, email);
        BibliotecariaControle.obterBibliotecaria(idBibliotecaria).cadastrarUsuario(usuario);

        JOptionPane.showMessageDialog(tela, "Usuário cadastrado com sucesso!");
        tela.dispose();
        new visao.menus.MenuBibliotecaria(idBibliotecaria);
    }

    public static List<Usuario> listarTodosUsuarios() {
        return ManipuladorArquivos.lerUsuarios();
    }

    public static void editarUsuario(Integer idUsuario, String novoNome, String novoEmail, JFrame tela, int idBibliotecaria) {
        if (idUsuario == null) {
            JOptionPane.showMessageDialog(tela, "Selecione um usuário.");
            return;
        }

        if (novoNome.isEmpty() || novoEmail.isEmpty()) {
            JOptionPane.showMessageDialog(tela, "Preencha todos os campos.");
            return;
        }

        if (!novoEmail.contains("@")) {
            JOptionPane.showMessageDialog(tela, "Email inválido.");
            return;
        }

        List<Usuario> usuarios = ManipuladorArquivos.lerUsuarios();
        Usuario encontrado = null;

        for (Usuario u : usuarios) {
            if (u.getIdUsuario() == idUsuario) {
                u.setNome(novoNome);
                u.setEmail(novoEmail);
                encontrado = u;
                break;
            }
        }

        if (encontrado == null) {
            JOptionPane.showMessageDialog(tela, "Usuário não encontrado.");
            return;
        }

        ManipuladorArquivos.reescreverArquivoUsuarios(usuarios);

        JOptionPane.showMessageDialog(tela, "Usuário atualizado com sucesso!");
        tela.dispose();
        new visao.menus.MenuBibliotecaria(idBibliotecaria);
    }

    public static Usuario obterUsuario(int idUsuario) {
        List<Usuario> usuarios = ManipuladorArquivos.lerUsuarios();
        return usuarios.stream()
                .filter(u -> u.getIdUsuario() == idUsuario)
                .findFirst()
                .orElse(null);
    }

    public static void excluirUsuario(Integer idUsuario, JFrame tela, int idBibliotecaria) {
        if (idUsuario == null) {
            JOptionPane.showMessageDialog(tela, "Selecione um usuário.");
            return;
        }

        boolean possuiEmprestimoAtivo = EmprestimoControle.listarEmprestimosAtivos().stream()
                .anyMatch(e -> e.getUsuario() != null && e.getUsuario().getIdUsuario() == idUsuario);

        if (possuiEmprestimoAtivo) {
            JOptionPane.showMessageDialog(tela, "Não é possível excluir: usuário possui empréstimo ativo.");
            return;
        }

        boolean possuiReservaAtiva = ManipuladorArquivos.lerReservas().stream()
                .anyMatch(r -> r.getStatusReserva() == StatusReserva.ATIVA
                        && r.getUsuario().getIdUsuario() == idUsuario);

        if (possuiReservaAtiva) {
            JOptionPane.showMessageDialog(tela, "Não é possível excluir: usuário possui reserva ativa.");
            return;
        }

        // Remove o histórico do usuário para não deixar registros órfãos
        // apontando para um ID que pode ser reaproveitado.
        List<Emprestimo> emprestimos = ManipuladorArquivos.lerEmprestimos();
        emprestimos.removeIf(e -> e.getUsuario().getIdUsuario() == idUsuario);
        ManipuladorArquivos.reescreverArquivoEmprestimos(emprestimos);

        List<Reserva> reservas = ManipuladorArquivos.lerReservas();
        reservas.removeIf(r -> r.getUsuario().getIdUsuario() == idUsuario);
        ManipuladorArquivos.reescreverArquivoReservas(reservas);

        ManipuladorArquivos.removerUsuario(idUsuario);

        JOptionPane.showMessageDialog(tela, "Usuário excluído com sucesso!");
        tela.dispose();
        new visao.menus.MenuBibliotecaria(idBibliotecaria);
    }

    public static List<Emprestimo> obterHistoricoLeitura(int idUsuario) {
        List<Emprestimo> todosEmprestimos = ManipuladorArquivos.lerEmprestimos();

        return todosEmprestimos.stream()
                .filter(e -> e.getUsuario() != null && e.getUsuario().getIdUsuario() == idUsuario)
                .filter(e -> !e.getAtivo())
                .collect(Collectors.toList());
    }
}