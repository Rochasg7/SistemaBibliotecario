package visao.menus;

import javax.swing.*;
import java.awt.*;

/**
 * "Cartão" central dos menus: painel de cantos arredondados e fundo escuro
 * semitransparente, com título, subtítulo e os componentes empilhados abaixo.
 */
public class PainelCartao extends JPanel {

    private final GridBagConstraints gbc = new GridBagConstraints();

    public PainelCartao(String titulo, String subtitulo) {
        super(new GridBagLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(26, 34, 30, 34));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        adicionar(rotulo(titulo, 26f, Font.BOLD, Color.WHITE), 0);
        adicionar(rotulo(subtitulo, 14f, Font.PLAIN, new Color(230, 220, 205)), 4);
    }

    /** Adiciona um componente na próxima linha, com um espaço acima dele. */
    public void adicionar(Component componente, int espacoAcima) {
        gbc.insets = new Insets(espacoAcima, 0, 0, 0);
        add(componente, gbc);
        gbc.gridy++;
    }

    /** Adiciona um título de seção (ex.: "Cadastros & Edições"). */
    public void adicionarSecao(String texto) {
        adicionar(rotulo(texto, 13f, Font.BOLD, new Color(240, 190, 110)), 22);
    }

    /** Adiciona uma linha de botões lado a lado, todos com a mesma largura. */
    public void adicionarLinhaDeBotoes(Component... botoes) {
        JPanel linha = new JPanel(new GridLayout(1, botoes.length, 12, 0));
        linha.setOpaque(false);
        for (Component b : botoes) {
            linha.add(b);
        }
        adicionar(linha, 8);
    }

    private static JLabel rotulo(String texto, float tamanho, int estilo, Color cor) {
        JLabel lbl = new JLabel(texto, SwingConstants.CENTER);
        lbl.setFont(lbl.getFont().deriveFont(estilo, tamanho));
        lbl.setForeground(cor);
        return lbl;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(28, 18, 10, 205));
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
        g2.setColor(new Color(255, 255, 255, 70));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);
        g2.dispose();
        super.paintComponent(g);
    }
}
