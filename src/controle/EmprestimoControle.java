package controle;

import modelo.Bibliotecaria;
import modelo.Emprestimo;
import modelo.Livro;
import modelo.Reserva;
import modelo.StatusReserva;
import modelo.Usuario;
import util.ManipuladorArquivos;

import javax.swing.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class EmprestimoControle {

    public static final int PRAZO_DIAS = 7;
    public static final double MULTA_POR_DIA = 2.0;

    public static class ResultadoEmprestimo {
        public String erro;
        public boolean reservaBaixada;
    }

    public static class ResultadoDevolucao {
        public String erro;
        public String avisoPrazo;
        public long diasAtraso;
        public boolean temReserva;
    }

    public static LocalDate paraLocalDate(Date data) {
        return data.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static List<Emprestimo> listarEmprestimosAtivos() {
        return ManipuladorArquivos.lerEmprestimos().stream()
                .filter(Emprestimo::getAtivo)
                .filter(e -> e.getLivro() != null && e.getUsuario() != null)
                .collect(Collectors.toList());
    }

    public static List<Emprestimo> listarTodosEmprestimos() {
        return ManipuladorArquivos.lerEmprestimos();
    }

    private static Set<Integer> idsLivrosEmprestados() {
        return listarEmprestimosAtivos().stream()
                .map(e -> e.getLivro().getIdLivro())
                .collect(Collectors.toSet());
    }

    /** Livros sem empréstimo ativo e sem reserva ativa (relatório "Livros Disponíveis"). */
    public static List<Livro> listarLivrosDisponiveis() {
        Set<Integer> emprestados = idsLivrosEmprestados();
        Set<Integer> reservados = ManipuladorArquivos.lerReservas().stream()
                .filter(r -> r.getStatusReserva() == StatusReserva.ATIVA)
                .map(r -> r.getLivro().getIdLivro())
                .collect(Collectors.toSet());

        return ManipuladorArquivos.lerLivros().stream()
                .filter(l -> !emprestados.contains(l.getIdLivro()) && !reservados.contains(l.getIdLivro()))
                .collect(Collectors.toList());
    }

    /**
     * Livros que podem ser emprestados agora: sem empréstimo ativo e, quando o usuário
     * é conhecido, sem reserva de outro usuário conflitando com o prazo do empréstimo.
     */
    public static List<Livro> listarLivrosParaEmprestimo(Integer idUsuario) {
        Set<Integer> emprestados = idsLivrosEmprestados();

        return ManipuladorArquivos.lerLivros().stream()
                .filter(l -> !emprestados.contains(l.getIdLivro()))
                .filter(l -> idUsuario == null || motivoBloqueioEmprestimo(l.getIdLivro(), idUsuario) == null)
                .collect(Collectors.toList());
    }

    /** Retorna o motivo do bloqueio ou null se o empréstimo é permitido. */
    public static String motivoBloqueioEmprestimo(int idLivro, int idUsuario) {
        if (LivroControle.obterLivro(idLivro) == null) {
            return "Livro não encontrado.";
        }
        if (UsuarioControle.obterUsuario(idUsuario) == null) {
            return "Usuário não encontrado.";
        }

        if (idsLivrosEmprestados().contains(idLivro)) {
            return "Não é possível emprestar: livro já está emprestado no momento.";
        }

        LocalDate fimDoPrazo = LocalDate.now().plusDays(PRAZO_DIAS);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        Reserva conflito = ManipuladorArquivos.lerReservas().stream()
                .filter(r -> r.getStatusReserva() == StatusReserva.ATIVA)
                .filter(r -> r.getLivro().getIdLivro() == idLivro)
                .filter(r -> r.getUsuario().getIdUsuario() != idUsuario)
                .filter(r -> paraLocalDate(r.getDataReserva()).isBefore(fimDoPrazo))
                .findFirst()
                .orElse(null);

        if (conflito != null) {
            return "Não é possível emprestar: livro está reservado para outro usuário ("
                    + conflito.getUsuario().getNome() + ", reserva de "
                    + sdf.format(conflito.getDataReserva()) + ").";
        }

        return null;
    }

    public static Reserva reservaAtivaDoUsuario(int idLivro, int idUsuario) {
        return ManipuladorArquivos.lerReservas().stream()
                .filter(r -> r.getStatusReserva() == StatusReserva.ATIVA)
                .filter(r -> r.getLivro().getIdLivro() == idLivro)
                .filter(r -> r.getUsuario().getIdUsuario() == idUsuario)
                .findFirst()
                .orElse(null);
    }

    /**
     * Fluxo único de empréstimo (bibliotecária e usuário): valida, grava, baixa a reserva
     * do próprio usuário (se houver) e sincroniza o status do livro.
     */
    public static ResultadoEmprestimo processarEmprestimo(Integer idLivro, Integer idUsuario, Bibliotecaria bibliotecaria) {
        ResultadoEmprestimo resultado = new ResultadoEmprestimo();

        if (idLivro == null || idUsuario == null) {
            resultado.erro = "Selecione um livro e um usuário.";
            return resultado;
        }

        resultado.erro = motivoBloqueioEmprestimo(idLivro, idUsuario);
        if (resultado.erro != null) {
            return resultado;
        }

        Livro livro = LivroControle.obterLivro(idLivro);
        Usuario usuario = UsuarioControle.obterUsuario(idUsuario);
        Reserva reservaDoUsuario = reservaAtivaDoUsuario(idLivro, idUsuario);

        int id = ManipuladorArquivos.proximoId("Emprestimo.csv");
        Emprestimo emprestimo = new Emprestimo(id, livro, usuario, new Date());

        if (bibliotecaria != null) {
            bibliotecaria.registrarEmprestimo(emprestimo);
        } else {
            usuario.solicitarEmprestimo();
        }
        ManipuladorArquivos.salvarEmprestimo(emprestimo);

        if (reservaDoUsuario != null) {
            List<Reserva> todasReservas = ManipuladorArquivos.lerReservas();
            for (Reserva r : todasReservas) {
                if (r.getIdReserva() == reservaDoUsuario.getIdReserva()) {
                    r.setStatusReserva(StatusReserva.CONCLUIDA);
                    break;
                }
            }
            ManipuladorArquivos.reescreverArquivoReservas(todasReservas);
            resultado.reservaBaixada = true;
        }

        LivroControle.sincronizarStatus(idLivro);
        return resultado;
    }

    public static void registrarEmprestimo(Integer idLivro, Integer idUsuario, JFrame tela, int idBibliotecaria) {
        ResultadoEmprestimo resultado = processarEmprestimo(idLivro, idUsuario,
                BibliotecariaControle.obterBibliotecaria(idBibliotecaria));

        if (resultado.erro != null) {
            JOptionPane.showMessageDialog(tela, resultado.erro);
            return;
        }

        String avisoReserva = resultado.reservaBaixada
                ? "\n\nReserva referente a este livro foi baixada automaticamente."
                : "";

        JOptionPane.showMessageDialog(tela,
                "Empréstimo registrado com sucesso!\nPrazo de devolução: " + PRAZO_DIAS + " dias." + avisoReserva);
        tela.dispose();
        new visao.menus.MenuBibliotecaria(idBibliotecaria);
    }

    public static long diasDecorridos(Emprestimo emprestimo) {
        return ChronoUnit.DAYS.between(paraLocalDate(emprestimo.getDataEmprestimo()), LocalDate.now());
    }

    public static ResultadoDevolucao processarDevolucao(int idEmprestimo, int idBibliotecaria) {
        ResultadoDevolucao resultado = new ResultadoDevolucao();

        Emprestimo emprestimo = listarEmprestimosAtivos().stream()
                .filter(e -> e.getIdEmprestimo() == idEmprestimo)
                .findFirst()
                .orElse(null);

        if (emprestimo == null) {
            resultado.erro = "Empréstimo não encontrado (ou já devolvido).";
            return resultado;
        }

        long diasEmprestado = diasDecorridos(emprestimo);

        if (diasEmprestado > PRAZO_DIAS) {
            resultado.diasAtraso = diasEmprestado - PRAZO_DIAS;
            double valorMulta = resultado.diasAtraso * MULTA_POR_DIA;
            resultado.avisoPrazo = String.format(
                    "⚠️ DEVOLUÇÃO ATRASADA: %d dia(s) de atraso.\nMulta a ser cobrada: R$ %.2f",
                    resultado.diasAtraso, valorMulta);
        } else {
            long diasRestantes = PRAZO_DIAS - diasEmprestado;
            resultado.avisoPrazo = String.format(
                    "✅ Devolução DENTRO DO PRAZO (%d dia(s) de folga). Dias em atraso: 0.", diasRestantes);
        }

        Bibliotecaria bibliotecaria = BibliotecariaControle.obterBibliotecaria(idBibliotecaria);
        if (bibliotecaria != null) {
            bibliotecaria.registrarDevolucao(emprestimo);
        } else {
            emprestimo.registrarDevolucao();
        }

        // Atualiza a linha existente do empréstimo (não acrescenta uma linha nova)
        List<Emprestimo> todos = ManipuladorArquivos.lerEmprestimos();
        for (int i = 0; i < todos.size(); i++) {
            if (todos.get(i).getIdEmprestimo() == idEmprestimo) {
                todos.set(i, emprestimo);
                break;
            }
        }
        ManipuladorArquivos.reescreverArquivoEmprestimos(todos);

        int idLivro = emprestimo.getLivro().getIdLivro();
        resultado.temReserva = ManipuladorArquivos.lerReservas().stream()
                .anyMatch(r -> r.getStatusReserva() == StatusReserva.ATIVA
                        && r.getLivro().getIdLivro() == idLivro);

        LivroControle.sincronizarStatus(idLivro);
        return resultado;
    }

    public static void registrarDevolucao(Integer idEmprestimo, JFrame tela, int idBibliotecaria) {
        if (idEmprestimo == null) {
            JOptionPane.showMessageDialog(tela, "Selecione um empréstimo.");
            return;
        }

        ResultadoDevolucao resultado = processarDevolucao(idEmprestimo, idBibliotecaria);

        if (resultado.erro != null) {
            JOptionPane.showMessageDialog(tela, resultado.erro);
            return;
        }

        String mensagem = "Devolução registrada!\n\n" + resultado.avisoPrazo;
        if (resultado.temReserva) {
            mensagem += "\n\nATENÇÃO: este livro possui reserva ativa.\nStatus alterado para RESERVADO.";
        } else {
            mensagem += "\n\nLivro DISPONÍVEL.";
        }

        JOptionPane.showMessageDialog(tela, mensagem);
        tela.dispose();
        new visao.menus.MenuBibliotecaria(idBibliotecaria);
    }

    public static void solicitarEmprestimo(Integer idLivro, int idUsuario, JFrame tela) {
        ResultadoEmprestimo resultado = processarEmprestimo(idLivro, idUsuario, null);

        if (resultado.erro != null) {
            JOptionPane.showMessageDialog(tela, resultado.erro);
            return;
        }

        String avisoReserva = resultado.reservaBaixada
                ? "\n\nSua reserva deste livro foi baixada automaticamente."
                : "";

        JOptionPane.showMessageDialog(tela,
                "Empréstimo solicitado com sucesso!\nPrazo padrão: " + PRAZO_DIAS + " dias." + avisoReserva);
        tela.dispose();
        new visao.menus.MenuUsuario(idUsuario);
    }

    public static void editarEmprestimo(Integer idEmprestimo, String novaDataStr, JFrame tela, int idBibliotecaria) {
        if (idEmprestimo == null) {
            JOptionPane.showMessageDialog(tela, "Selecione um empréstimo.");
            return;
        }

        if (novaDataStr.isEmpty()) {
            JOptionPane.showMessageDialog(tela, "Preencha a data.");
            return;
        }

        Date novaData;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            sdf.setLenient(false);
            novaData = sdf.parse(novaDataStr);
        } catch (ParseException ex) {
            JOptionPane.showMessageDialog(tela, "Data inválida. Use o formato dd/MM/yyyy.");
            return;
        }

        List<Emprestimo> emprestimos = ManipuladorArquivos.lerEmprestimos();
        Emprestimo encontrado = null;

        for (Emprestimo e : emprestimos) {
            if (e.getIdEmprestimo() == idEmprestimo) {
                e.setDataEmprestimo(novaData);
                encontrado = e;
                break;
            }
        }

        if (encontrado == null) {
            JOptionPane.showMessageDialog(tela, "Empréstimo não encontrado.");
            return;
        }

        ManipuladorArquivos.reescreverArquivoEmprestimos(emprestimos);

        JOptionPane.showMessageDialog(tela, "Empréstimo atualizado com sucesso!");
        tela.dispose();
        new visao.menus.MenuBibliotecaria(idBibliotecaria);
    }

    public static void excluirEmprestimo(Integer idEmprestimo, JFrame tela, int idBibliotecaria) {
        if (idEmprestimo == null) {
            JOptionPane.showMessageDialog(tela, "Selecione um empréstimo.");
            return;
        }

        Emprestimo emprestimo = ManipuladorArquivos.lerEmprestimos().stream()
                .filter(e -> e.getIdEmprestimo() == idEmprestimo)
                .findFirst()
                .orElse(null);

        if (emprestimo == null) {
            JOptionPane.showMessageDialog(tela, "Empréstimo não encontrado.");
            return;
        }

        int idLivro = emprestimo.getLivro().getIdLivro();
        ManipuladorArquivos.removerEmprestimo(idEmprestimo);
        LivroControle.sincronizarStatus(idLivro);

        JOptionPane.showMessageDialog(tela, "Empréstimo excluído com sucesso!");
        tela.dispose();
        new visao.menus.MenuBibliotecaria(idBibliotecaria);
    }

    public static List<Emprestimo> listarEmprestimosEmAtraso() {
        return listarEmprestimosAtivos().stream()
                .filter(e -> diasDecorridos(e) > PRAZO_DIAS)
                .collect(Collectors.toList());
    }

    public static long calcularDiasAtraso(Emprestimo emprestimo) {
        return Math.max(0, diasDecorridos(emprestimo) - PRAZO_DIAS);
    }

}
