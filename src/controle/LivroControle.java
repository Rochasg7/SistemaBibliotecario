package controle;

import modelo.Emprestimo;
import modelo.Livro;
import modelo.Reserva;
import modelo.StatusLivro;
import modelo.StatusReserva;
import util.ManipuladorArquivos;

import javax.swing.*;
import java.util.List;

public class LivroControle {

    public static void cadastrarLivro(String titulo, String autor, JFrame tela, int idBibliotecaria) {
        if (titulo.isEmpty() || autor.isEmpty()) {
            JOptionPane.showMessageDialog(tela, "Preencha todos os campos.");
            return;
        }

        int id = ManipuladorArquivos.proximoId("Livro.csv");
        Livro livro = new Livro(id, titulo, autor);
        BibliotecariaControle.obterBibliotecaria(idBibliotecaria).cadastrarLivro(livro);

        JOptionPane.showMessageDialog(tela, "Livro cadastrado com sucesso!");
        tela.dispose();
        new visao.menus.MenuBibliotecaria(idBibliotecaria);
    }

    public static List<Livro> listarTodosLivros() {
        return ManipuladorArquivos.lerLivros();
    }

    public static void editarLivro(Integer idLivro, String novoTitulo, String novoAutor, JFrame tela, int idBibliotecaria) {
        if (idLivro == null) {
            JOptionPane.showMessageDialog(tela, "Selecione um livro.");
            return;
        }

        if (novoTitulo.isEmpty() || novoAutor.isEmpty()) {
            JOptionPane.showMessageDialog(tela, "Preencha todos os campos.");
            return;
        }

        List<Livro> livros = ManipuladorArquivos.lerLivros();
        Livro encontrado = null;

        for (Livro l : livros) {
            if (l.getIdLivro() == idLivro) {
                l.setTitulo(novoTitulo);
                l.setAutor(novoAutor);
                encontrado = l;
                break;
            }
        }

        if (encontrado == null) {
            JOptionPane.showMessageDialog(tela, "Livro não encontrado.");
            return;
        }

        ManipuladorArquivos.reescreverArquivoLivros(livros);

        JOptionPane.showMessageDialog(tela, "Livro atualizado com sucesso!");
        tela.dispose();
        new visao.menus.MenuBibliotecaria(idBibliotecaria);
    }

    public static Livro obterLivro(int idLivro) {
        List<Livro> livros = ManipuladorArquivos.lerLivros();
        return livros.stream()
                .filter(l -> l.getIdLivro() == idLivro)
                .findFirst()
                .orElse(null);
    }

    /**
     * Recalcula o status do livro a partir dos empréstimos e reservas:
     * EMPRESTADO (empréstimo ativo) > RESERVADO (reserva ativa) > DISPONIVEL.
     */
    public static void sincronizarStatus(int idLivro) {
        boolean emprestado = EmprestimoControle.listarEmprestimosAtivos().stream()
                .anyMatch(e -> e.getLivro().getIdLivro() == idLivro);

        boolean reservado = ManipuladorArquivos.lerReservas().stream()
                .anyMatch(r -> r.getStatusReserva() == StatusReserva.ATIVA && r.getLivro().getIdLivro() == idLivro);

        StatusLivro novoStatus = emprestado ? StatusLivro.EMPRESTADO
                : reservado ? StatusLivro.RESERVADO
                : StatusLivro.DISPONIVEL;

        List<Livro> livros = ManipuladorArquivos.lerLivros();
        for (Livro l : livros) {
            if (l.getIdLivro() == idLivro) {
                l.alterarStatus(novoStatus);
                break;
            }
        }
        ManipuladorArquivos.reescreverArquivoLivros(livros);
    }

    public static void excluirLivro(Integer idLivro, JFrame tela, int idBibliotecaria) {
        if (idLivro == null) {
            JOptionPane.showMessageDialog(tela, "Selecione um livro.");
            return;
        }

        boolean emprestado = EmprestimoControle.listarEmprestimosAtivos().stream()
                .anyMatch(e -> e.getLivro() != null && e.getLivro().getIdLivro() == idLivro);

        if (emprestado) {
            JOptionPane.showMessageDialog(tela, "Não é possível excluir: livro está emprestado.");
            return;
        }

        boolean reservado = ManipuladorArquivos.lerReservas().stream()
                .anyMatch(r -> r.getStatusReserva() == StatusReserva.ATIVA && r.getLivro().getIdLivro() == idLivro);

        if (reservado) {
            JOptionPane.showMessageDialog(tela, "Não é possível excluir: livro possui reserva ativa.");
            return;
        }

        // Remove o histórico do livro (empréstimos devolvidos e reservas encerradas)
        // para não deixar registros órfãos apontando para um ID que pode ser reaproveitado.
        List<Emprestimo> emprestimos = ManipuladorArquivos.lerEmprestimos();
        emprestimos.removeIf(e -> e.getLivro().getIdLivro() == idLivro);
        ManipuladorArquivos.reescreverArquivoEmprestimos(emprestimos);

        List<Reserva> reservas = ManipuladorArquivos.lerReservas();
        reservas.removeIf(r -> r.getLivro().getIdLivro() == idLivro);
        ManipuladorArquivos.reescreverArquivoReservas(reservas);

        ManipuladorArquivos.removerLivro(idLivro);

        JOptionPane.showMessageDialog(tela, "Livro excluído com sucesso!");
        tela.dispose();
        new visao.menus.MenuBibliotecaria(idBibliotecaria);
    }
}