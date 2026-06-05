import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

final class AuthUi {

    static final Dimension FRAME_SIZE = new Dimension(960, 600);
    static final Dimension CARD_SIZE = new Dimension(360, 440);
    static final Dimension LOGIN_CARD_SIZE = new Dimension(360, 430);
    static final Dimension FIELD_SIZE = new Dimension(260, 38);
    static final Dimension BUTTON_SIZE = new Dimension(86, 40);
    static final Color GRADIENT_LEFT = new Color(0xF08A52);
    static final Color GRADIENT_RIGHT = new Color(0xC6AE8C);
    static final Color CARD = Color.WHITE;
    static final Color BUTTON = new Color(0xDF9160);
    static final Color BUTTON_DARK = new Color(0xC97A4D);
    static final Color ACCENT = new Color(0xF06F3F);
    static final Color TEXT = new Color(0x050505);
    static final Color MUTED = new Color(0x9B9B9B);
    static final Color FIELD = Color.WHITE;

    private AuthUi() {
    }

    static void prepareFrame(javax.swing.JFrame frame, String title) {
        frame.setTitle(title);
        frame.setMinimumSize(FRAME_SIZE);
        frame.setPreferredSize(FRAME_SIZE);
        frame.setSize(FRAME_SIZE);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
    }

    static JLabel label(String text, int style, int size, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", style, size));
        label.setForeground(color);
        return label;
    }

    static void styleField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        field.setForeground(TEXT);
        field.setBackground(FIELD);
        field.setCaretColor(TEXT);
        field.setBorder(new CompoundBorder(
                new ShadowBorder(),
                new EmptyBorder(8, 12, 8, 12)
        ));
    }

    static void installPlaceholder(JTextField field, String placeholder) {
        final char echoChar = field instanceof JPasswordField ? ((JPasswordField) field).getEchoChar() : 0;
        field.setText(placeholder);
        field.setForeground(MUTED);
        if (field instanceof JPasswordField) {
            ((JPasswordField) field).setEchoChar((char) 0);
        }
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent event) {
                if (placeholder.equals(field.getText())) {
                    field.setText("");
                    field.setForeground(TEXT);
                    field.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    if (field instanceof JPasswordField) {
                        ((JPasswordField) field).setEchoChar(echoChar);
                    }
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent event) {
                if (field.getText().isBlank()) {
                    field.setText(placeholder);
                    field.setForeground(MUTED);
                    field.setFont(new Font("Segoe UI", Font.ITALIC, 12));
                    if (field instanceof JPasswordField) {
                        ((JPasswordField) field).setEchoChar((char) 0);
                    }
                }
            }
        });
    }

    static String valueOf(JTextField field) {
        String value = field.getText().trim();
        return "Ketik Di Sini".equals(value) ? "" : value;
    }

    static void stylePrimaryButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(BUTTON);
        button.setBorder(new CompoundBorder(new ShadowBorder(), new EmptyBorder(8, 16, 8, 16)));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent event) {
                button.setBackground(BUTTON_DARK);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent event) {
                button.setBackground(BUTTON);
            }
        });
    }

    static void styleLink(JLabel label) {
        label.setForeground(ACCENT);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    static JPanel card(Dimension size) {
        JPanel panel = new RoundedPanel(12);
        panel.setBackground(CARD);
        panel.setBorder(new EmptyBorder(30, 48, 28, 48));
        panel.setMinimumSize(size);
        panel.setPreferredSize(size);
        panel.setMaximumSize(size);
        return panel;
    }

    static void setFixedSize(JComponent component, Dimension size) {
        component.setMinimumSize(size);
        component.setPreferredSize(size);
        component.setMaximumSize(size);
    }

    static JLabel centeredLabel(String text, int style, int size, Color color) {
        JLabel label = label(text, style, size, color);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }

    static final class GradientPanel extends JPanel {
        GradientPanel() {
            super(new java.awt.GridBagLayout());
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setPaint(new GradientPaint(0, 0, GRADIENT_LEFT, getWidth(), 0, GRADIENT_RIGHT));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
            super.paintComponent(graphics);
        }
    }

    static final class RoundedPanel extends JPanel {
        private final int arc;

        RoundedPanel(int arc) {
            this.arc = arc;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            g2.dispose();
            super.paintComponent(graphics);
        }
    }

    static final class ShadowBorder extends EmptyBorder {
        ShadowBorder() {
            super(0, 0, 5, 0);
        }

        @Override
        public void paintBorder(java.awt.Component component, Graphics graphics, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0, 0, 0, 42));
            g2.fillRoundRect(x + 2, y + height - 7, width - 4, 6, 10, 10);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(x, y, width, height - 5, 10, 10);
            g2.dispose();
        }
    }
}
