package visao.menus;

import javax.swing.*;
import java.awt.*;

/**
 * Botão grande de cantos arredondados usado nos menus.
 * Desenhado à mão para ter a mesma aparência em qualquer Look and Feel,
 * com cor diferente ao passar o mouse e ao clicar.
 */
public class BotaoMenu extends JButton {

    public static final Color MADEIRA = new Color(176, 112, 52);
    public static final Color VERMELHO = new Color(158, 58, 52);

    private final Color corBase;
    private final boolean comSeta;

    public BotaoMenu(String texto) {
        this(texto, MADEIRA, false);
    }

    /**
     * @param comSeta true para mostrar uma setinha indicando que o botão abre uma lista de opções
     */
    public BotaoMenu(String texto, Color cor, boolean comSeta) {
        super(texto);
        this.corBase = cor;
        this.comSeta = comSeta;

        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setForeground(Color.WHITE);
        setFont(getFont().deriveFont(Font.BOLD, 15f));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /** A largura acompanha o texto (mais folga para a setinha), com mínimo de 180 e altura fixa de 54. */
    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        int largura = d.width + 24 + (comSeta ? 28 : 0);
        return new Dimension(Math.max(180, largura), 54);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        ButtonModel modelo = getModel();
        Color cor = corBase;
        if (modelo.isPressed()) {
            cor = ajustar(corBase, 0.8);
        } else if (modelo.isRollover()) {
            cor = ajustar(corBase, 1.15);
        }

        g2.setColor(cor);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
        g2.setColor(new Color(255, 255, 255, 60));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);

        if (comSeta) {
            int cx = getWidth() - 20;
            int cy = getHeight() / 2;
            g2.setColor(Color.WHITE);
            g2.fillPolygon(new int[]{cx - 5, cx + 5, cx}, new int[]{cy - 2, cy - 2, cy + 4}, 3);
        }
        g2.dispose();

        super.paintComponent(g); // desenha o texto por cima
    }

    private static Color ajustar(Color c, double fator) {
        return new Color(
                Math.min(255, (int) (c.getRed() * fator)),
                Math.min(255, (int) (c.getGreen() * fator)),
                Math.min(255, (int) (c.getBlue() * fator)));
    }
}
