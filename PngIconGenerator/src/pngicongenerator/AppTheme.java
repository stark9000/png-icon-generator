package pngicongenerator;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * AppTheme
 *
 * A tiny, dependency-free light theme for Java 8 Swing apps: white/light
 * surfaces with magenta as the single accent color, and buttons rendered with a
 * real 3D gradient + rounded corners (not just a flat fill).
 *
 * Call AppTheme.apply() once at startup (before creating any JFrame), then call
 * AppTheme.styleButton(button) on each JButton you want themed.
 */
public final class AppTheme {

    // ---- palette: white surfaces, magenta accent ---------------------------
    public static final Color APP_BG = new Color(0xFFFFFF);
    public static final Color CARD_BG = new Color(0xFFFFFF);
    public static final Color CARD_BORDER = new Color(0xE0, 0xE0, 0xE6);

    public static final Color ACCENT = new Color(0xFF, 0x00, 0x8A); // mid tone (idle base)
    public static final Color ACCENT_LIGHT = new Color(0xFD, 0x00, 0xF9); // gradient top: magenta
    public static final Color ACCENT_DARK = new Color(0xFF, 0x00, 0x00); // gradient bottom: red
    public static final Color ACCENT_BORDER = new Color(0xB8, 0x00, 0x00); // border: deep red

    public static final Color TEXT_PRIMARY = new Color(0x1E, 0x23, 0x2B);
    public static final Color TEXT_MUTED = new Color(0x74, 0x7B, 0x8A);
    public static final Color LABEL_MUTED = new Color(0x8C, 0x96, 0xA5);
    public static final Color LOG_MUTED = new Color(0x60, 0x66, 0x70);

    public static final Color SUCCESS = new Color(0x1E, 0x8E, 0x3E);
    public static final Color ERROR = new Color(0xD3, 0x2F, 0x2F);

    public static final Color BUTTON_TEXT = Color.WHITE;
    public static final int BUTTON_ARC = 5; // higher = rounder corners (try ~h for a pill shape)

    public static final Font FONT_UI = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_MONO = new Font(Font.MONOSPACED, Font.PLAIN, 12);

    private AppTheme() {
    }

    /**
     * Apply the theme to UIManager. Call before creating any Swing components.
     */
    public static void apply() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // fall back to default L&F if system one isn't available
        }

        UIManager.put("control", ui(APP_BG));

        UIManager.put("Panel.background", ui(APP_BG));
        UIManager.put("OptionPane.background", ui(APP_BG));
        UIManager.put("OptionPane.messageForeground", ui(TEXT_PRIMARY));

        UIManager.put("Label.foreground", ui(TEXT_PRIMARY));
        UIManager.put("Label.font", FONT_UI);

        UIManager.put("TextField.background", ui(CARD_BG));
        UIManager.put("TextField.foreground", ui(TEXT_PRIMARY));
        UIManager.put("TextField.caretForeground", ui(TEXT_PRIMARY));
        UIManager.put("TextField.font", FONT_UI);
        UIManager.put("TextField.border", BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)));

        UIManager.put("TextArea.background", ui(CARD_BG));
        UIManager.put("TextArea.foreground", ui(TEXT_PRIMARY));
        UIManager.put("TextArea.caretForeground", ui(TEXT_PRIMARY));
        UIManager.put("TextArea.font", FONT_MONO);

        UIManager.put("ScrollPane.background", ui(APP_BG));
        UIManager.put("Viewport.background", ui(CARD_BG));

        UIManager.put("TitledBorder.titleColor", ui(LABEL_MUTED));
        UIManager.put("TitledBorder.font", FONT_UI);

        UIManager.put("FileChooser.background", ui(APP_BG));
        UIManager.put("ToolTip.background", ui(CARD_BG));
        UIManager.put("ToolTip.foreground", ui(TEXT_PRIMARY));

        UIManager.put("ProgressBar.background", ui(CARD_BG));
        UIManager.put("ProgressBar.foreground", ui(ACCENT));
        UIManager.put("ProgressBar.selectionForeground", ui(TEXT_PRIMARY));
        UIManager.put("ProgressBar.selectionBackground", ui(CARD_BG));
    }

    /**
     * Style a JFrame's content pane background to match the theme.
     */
    public static void styleFrame(JFrame frame) {
        frame.getContentPane().setBackground(APP_BG);
    }

    /**
     * Apply a magenta, 3D-gradient, rounded-corner look to a button. Uses a
     * custom ButtonUI so the gradient/rounding actually renders, instead of
     * being overridden by the platform's native button chrome.
     */
    public static void styleButton(JButton button) {
        button.setUI(new GradientButtonUI());
        button.setForeground(BUTTON_TEXT);
        button.setFont(FONT_UI.deriveFont(Font.BOLD));
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private static ColorUIResource ui(Color c) {
        return new ColorUIResource(c);
    }

    /**
     * Custom ButtonUI: paints a rounded, vertically-gradiented magenta button
     * with a glossy highlight on top and a subtle border, giving a 3D look.
     * Falls back to BasicButtonUI's text/icon layout so we don't have to
     * reimplement label positioning by hand.
     */
    private static class GradientButtonUI extends BasicButtonUI {

        @Override
        public void installUI(JComponent c) {
            super.installUI(c);
            AbstractButton b = (AbstractButton) c;
            b.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    b.repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    b.repaint();
                }
            });
        }

        @Override
        public void paint(Graphics g, JComponent c) {
            AbstractButton b = (AbstractButton) c;
            ButtonModel model = b.getModel();
            int w = c.getWidth();
            int h = c.getHeight();
            int arc = Math.min(BUTTON_ARC, h);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color top, bottom;
            int yOffset = 0;
            if (!b.isEnabled()) {
                top = new Color(0xCC, 0xCC, 0xCC);
                bottom = new Color(0xAA, 0xAA, 0xAA);
            } else if (model.isPressed()) {
                // pressed = inverted gradient + slight downward shift, feels "pushed in"
                top = ACCENT_DARK;
                bottom = ACCENT_LIGHT;
                yOffset = 1;
            } else if (model.isRollover()) {
                top = lighten(ACCENT_LIGHT, 0.25f);
                bottom = lighten(ACCENT_DARK, 0.25f);
            } else {
                top = ACCENT_LIGHT;
                bottom = ACCENT_DARK;
            }

            // drop shadow (subtle) beneath the button, skipped while pressed
            if (b.isEnabled() && !model.isPressed()) {
                g2.setColor(new Color(0, 0, 0, 35));
                g2.fillRoundRect(1, 3, w - 2, h - 3, arc, arc);
            }

            // main gradient fill
            g2.setPaint(new GradientPaint(0, yOffset, top, 0, h + yOffset, bottom));
            g2.fillRoundRect(0, yOffset, w - 1, h - 1 - yOffset, arc, arc);

            // glossy highlight across the top half for a rounded, 3D feel
            if (b.isEnabled() && !model.isPressed()) {
                g2.setPaint(new GradientPaint(0, yOffset, new Color(255, 255, 255, 100),
                        0, (h / 2f) + yOffset, new Color(255, 255, 255, 0)));
                g2.fillRoundRect(1, 1 + yOffset, w - 3, (h / 2) - 1, arc, arc);
            }

            // crisp border
            g2.setColor(b.isEnabled() ? ACCENT_BORDER : new Color(0x99, 0x99, 0x99));
            g2.drawRoundRect(0, yOffset, w - 2, h - 2 - yOffset, arc, arc);

            g2.dispose();

            // let BasicButtonUI position/paint the text and icon
            super.paint(g, c);
        }

        /**
         * Blend a color toward white by the given amount (0-1). Unlike
         * Color.brighter(), this still lightens pure-saturated colors like
         * (255,0,0).
         */
        private static Color lighten(Color c, float amount) {
            int r = (int) (c.getRed() + (255 - c.getRed()) * amount);
            int g = (int) (c.getGreen() + (255 - c.getGreen()) * amount);
            int b = (int) (c.getBlue() + (255 - c.getBlue()) * amount);
            return new Color(r, g, b);
        }
    }
}
