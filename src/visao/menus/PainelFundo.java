package visao.menus;

import javax.swing.*;
import java.awt.*;

/**
 * Painel de fundo dos menus.
 * Desenha a imagem "images/biblioteca.jpg" preenchendo toda a janela (sem
 * distorcer, cortando o excesso) e escurece levemente para os botões ficarem
 * legíveis. Como usa GridBagLayout, tudo que for adicionado fica centralizado.
 */
public class PainelFundo extends JPanel {

    private static final String CAMINHO_IMAGEM = "images/biblioteca.jpg";

    private final Image imagem;
    private final int larguraImagem;
    private final int alturaImagem;

    public PainelFundo() {
        super(new GridBagLayout());
        setBackground(new Color(60, 40, 25)); // cor de reserva se a imagem não existir

        ImageIcon icone = new ImageIcon(CAMINHO_IMAGEM);
        if (icone.getIconWidth() > 0) {
            imagem = icone.getImage();
            larguraImagem = icone.getIconWidth();
            alturaImagem = icone.getIconHeight();
        } else {
            System.out.println("Imagem " + CAMINHO_IMAGEM + " não encontrada.");
            imagem = null;
            larguraImagem = 0;
            alturaImagem = 0;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        if (imagem != null) {
            // escala "cover": preenche o painel inteiro mantendo a proporção
            double escala = Math.max((double) getWidth() / larguraImagem,
                                     (double) getHeight() / alturaImagem);
            int w = (int) Math.ceil(larguraImagem * escala);
            int h = (int) Math.ceil(alturaImagem * escala);
            int x = (getWidth() - w) / 2;
            int y = (getHeight() - h) / 2;
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(imagem, x, y, w, h, this);
        }

        // véu escuro leve por cima da imagem
        g2.setColor(new Color(0, 0, 0, 45));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
    }
}
