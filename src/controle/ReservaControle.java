package controle;

import modelo.Bibliotecaria;
import modelo.Livro;
import modelo.Reserva;
import modelo.StatusReserva;
import modelo.Usuario;
import util.ManipuladorArquivos;

import javax.swing.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ReservaControle {

    public static List<Reserva> listarReservasPorUsuario(int idUsuario) {
        return ManipuladorArquivos.lerReservas().stream()
                .filter(r -> r.getUsuario() != null && r.getUsuario().getIdUsuario() == idUsuario)
                .filter(r -> r.getLivro() != null)
                .collect(Collectors.toList());
    }

    public static List<Reserva> listarTodasReservas() {
        return ManipuladorArquivos.lerReservas();
    }

    public static List<Reserva> listarReservasPendentes() {
        return ManipuladorArquivos.lerReservas().stream()
                .filter(r -> r.getStatusReserva() == StatusReserva.ATIVA)
                .collect(Collectors.toList());
    }

    /** Livros que podem receber reserva: os que não estão emprestados no momento. */
    public static List<Livro> listarLivrosParaReserva() {
        Set<Integer> emprestados = EmprestimoControle.listarEmprestimosAtivos().stream()
                .map(e -> e.getLivro().getIdLivro())
                .collect(Collectors.toSet());

        return ManipuladorArquivos.lerLivros().stream()
                .filter(l -> !emprestados.contains(l.getIdLivro()))
                .collect(Collectors.toList());
    }

    /** Converte dd/MM/yyyy (estrito). Retorna null se inválida. */
    public static Date parseData(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return null;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            sdf.setLenient(false);
            return sdf.parse(texto.trim());
        } catch (ParseException ex) {
            return null;
        }
    }

    /**
     * Retorna o motivo do bloqueio ou null se a reserva é permitida.
     * Regras: não reservar livro emprestado (só na criação) e não reservar livro já
     * reservado na data (cada reserva ocupa o prazo de empréstimo, {@value EmprestimoControle#PRAZO_DIAS} dias).
     */
    public static String motivoBloqueioReserva(int idLivro, Date data, Integer idReservaIgnorada, boolean verificarEmprestimo) {
        if (verificarEmprestimo) {
            boolean emprestado = EmprestimoControle.listarEmprestimosAtivos().stream()
                    .anyMatch(e -> e.getLivro().getIdLivro() == idLivro);
            if (emprestado) {
                return "Não é possível reservar: livro está emprestado no momento.";
            }
        }

        LocalDate dataDesejada = EmprestimoControle.paraLocalDate(data);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        Reserva conflito = ManipuladorArquivos.lerReservas().stream()
                .filter(r -> r.getStatusReserva() == StatusReserva.ATIVA)
                .filter(r -> r.getLivro().getIdLivro() == idLivro)
                .filter(r -> idReservaIgnorada == null || r.getIdReserva() != idReservaIgnorada)
                .filter(r -> Math.abs(ChronoUnit.DAYS.between(
                        EmprestimoControle.paraLocalDate(r.getDataReserva()), dataDesejada)) < EmprestimoControle.PRAZO_DIAS)
                .findFirst()
                .orElse(null);

        if (conflito != null) {
            return "Não é possível reservar: livro já está reservado em "
                    + sdf.format(conflito.getDataReserva())
                    + " (cada reserva ocupa " + EmprestimoControle.PRAZO_DIAS + " dias).";
        }

        return null;
    }

    /** Cria a reserva. bibliotecaria == null significa que o próprio usuário reservou. Retorna erro ou null. */
    public static String criarReserva(Integer idLivro, Integer idUsuario, String dataStr, Bibliotecaria bibliotecaria) {
        if (idLivro == null || idUsuario == null) {
            return "Selecione um livro e um usuário.";
        }

        if (dataStr == null || dataStr.trim().isEmpty()) {
            return "Informe a data da reserva (dd/MM/yyyy).";
        }

        Date data = parseData(dataStr);
        if (data == null) {
            return "Data inválida. Use o formato dd/MM/yyyy.";
        }

        if (EmprestimoControle.paraLocalDate(data).isBefore(LocalDate.now())) {
            return "A data da reserva não pode ser anterior a hoje.";
        }

        Livro livro = LivroControle.obterLivro(idLivro);
        Usuario usuario = UsuarioControle.obterUsuario(idUsuario);
        if (livro == null || usuario == null) {
            return "Livro ou usuário não encontrado.";
        }

        String bloqueio = motivoBloqueioReserva(idLivro, data, null, true);
        if (bloqueio != null) {
            return bloqueio;
        }

        int id = ManipuladorArquivos.proximoId("Reserva.csv");
        Reserva reserva = new Reserva(id, livro, usuario, data);

        if (bibliotecaria != null) {
            bibliotecaria.registrarReserva(reserva);
        } else {
            usuario.solicitarReserva();
        }
        ManipuladorArquivos.salvarReserva(reserva);
        LivroControle.sincronizarStatus(idLivro);

        return null;
    }

    public static void solicitarReserva(Integer idLivro, String dataStr, int idUsuario, JFrame tela) {
        String erro = criarReserva(idLivro, idUsuario, dataStr, null);

        if (erro != null) {
            JOptionPane.showMessageDialog(tela, erro);
            return;
        }

        JOptionPane.showMessageDialog(tela, "Reserva solicitada com sucesso!");
        tela.dispose();
        new visao.menus.MenuUsuario(idUsuario);
    }

    public static void cadastrarReserva(Integer idLivro, Integer idUsuario, String dataStr, JFrame tela, int idBibliotecaria) {
        String erro = criarReserva(idLivro, idUsuario, dataStr, BibliotecariaControle.obterBibliotecaria(idBibliotecaria));

        if (erro != null) {
            JOptionPane.showMessageDialog(tela, erro);
            return;
        }

        JOptionPane.showMessageDialog(tela, "Reserva cadastrada com sucesso!");
        tela.dispose();
        new visao.menus.MenuBibliotecaria(idBibliotecaria);
    }

    public static void editarReserva(Integer idReserva, String novaDataStr, JFrame tela, int idBibliotecaria) {
        if (idReserva == null) {
            JOptionPane.showMessageDialog(tela, "Selecione uma reserva.");
            return;
        }

        if (novaDataStr.isEmpty()) {
            JOptionPane.showMessageDialog(tela, "Preencha a data.");
            return;
        }

        Date novaData = parseData(novaDataStr);
        if (novaData == null) {
            JOptionPane.showMessageDialog(tela, "Data inválida. Use o formato dd/MM/yyyy.");
            return;
        }

        List<Reserva> reservas = ManipuladorArquivos.lerReservas();
        Reserva encontrada = null;

        for (Reserva r : reservas) {
            if (r.getIdReserva() == idReserva) {
                encontrada = r;
                break;
            }
        }

        if (encontrada == null) {
            JOptionPane.showMessageDialog(tela, "Reserva não encontrada.");
            return;
        }

        if (encontrada.getStatusReserva() != StatusReserva.ATIVA) {
            JOptionPane.showMessageDialog(tela, "Só é possível alterar a data de reservas ATIVAS.");
            return;
        }

        if (EmprestimoControle.paraLocalDate(novaData).isBefore(LocalDate.now())) {
            JOptionPane.showMessageDialog(tela, "A data da reserva não pode ser anterior a hoje.");
            return;
        }

        String bloqueio = motivoBloqueioReserva(encontrada.getLivro().getIdLivro(), novaData, idReserva, false);
        if (bloqueio != null) {
            JOptionPane.showMessageDialog(tela, bloqueio);
            return;
        }

        encontrada.setDataReserva(novaData);
        ManipuladorArquivos.reescreverArquivoReservas(reservas);

        JOptionPane.showMessageDialog(tela, "Reserva atualizada com sucesso!");
        tela.dispose();
        new visao.menus.MenuBibliotecaria(idBibliotecaria);
    }

    public static void excluirReserva(Integer idReserva, JFrame tela, int idBibliotecaria) {
        if (idReserva == null) {
            JOptionPane.showMessageDialog(tela, "Selecione uma reserva.");
            return;
        }

        Reserva reserva = ManipuladorArquivos.lerReservas().stream()
                .filter(r -> r.getIdReserva() == idReserva)
                .findFirst()
                .orElse(null);

        if (reserva == null) {
            JOptionPane.showMessageDialog(tela, "Reserva não encontrada.");
            return;
        }

        int idLivro = reserva.getLivro().getIdLivro();
        ManipuladorArquivos.removerReserva(idReserva);
        LivroControle.sincronizarStatus(idLivro);

        JOptionPane.showMessageDialog(tela, "Reserva excluída com sucesso!");
        tela.dispose();
        new visao.menus.MenuBibliotecaria(idBibliotecaria);
    }
}
