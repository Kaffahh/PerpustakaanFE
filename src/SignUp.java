import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class SignUp extends JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(SignUp.class.getName());
    private final com.mycompany.perpustakaan.api.LibraryApi libraryApi;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;

    // === WARNA DARI DESIGN ===
    private static final Color GRADIENT_START = new Color(232, 130, 90);
    private static final Color GRADIENT_END   = new Color(190, 160, 130);
    private static final Color CARD_BG        = Color.WHITE;
    private static final Color TEXT_COLOR     = new Color(50, 50, 50);
    private static final Color BUTTON_BG      = new Color(232, 130, 90);
    private static final Color FIELD_BORDER   = new Color(220, 220, 220);
    private static final Color PLACEHOLDER    = new Color(180, 180, 180);

    private static final Dimension FRAME_SIZE = new Dimension(900, 600);
    private static final Dimension CARD_SIZE  = new Dimension(380, 480);
    private static final Dimension FIELD_SIZE = new Dimension(280, 38);
    private static final Dimension BUTTON_SIZE = new Dimension(120, 38);

    public SignUp() {
        this(new com.mycompany.perpustakaan.api.LibraryApi());
    }

    public SignUp(com.mycompany.perpustakaan.api.LibraryApi libraryApi) {
        this.libraryApi = libraryApi;
        initComponents();
    }

    private void initComponents() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle("Daftar Akun Perpustakaan");
        setResizable(false);

        JPanel root = new GradientPanel();
        root.setLayout(new GridBagLayout());
        setContentPane(root);

        root.add(createRegisterCard(), new GridBagConstraints());

        pack();
        setSize(FRAME_SIZE);
        setLocationRelativeTo(null);
    }

    private JPanel createRegisterCard() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setPreferredSize(CARD_SIZE);
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(0, 0, 4, 4),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 0, 0, 20), 1),
                new EmptyBorder(30, 40, 40, 40)
            )
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;

        // === TITLE "Daftar" ===
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 30, 0);
        JLabel title = new JLabel("Daftar");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(TEXT_COLOR);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(title, gbc);

        // === USERNAME ===
        usernameField = addField(card, gbc, 1, "Username  :", false);

        // === PASSWORD ===
        passwordField = (JPasswordField) addField(card, gbc, 3, "Password  :", true);

        // === KONFIRMASI ===
        confirmPasswordField = (JPasswordField) addField(card, gbc, 5, "Konfirmasi :", true);

        // === BUTTON DAFTAR ===
        JButton registerButton = new JButton("Daftar");
        registerButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        registerButton.setForeground(Color.WHITE);
        registerButton.setBackground(BUTTON_BG);
        registerButton.setFocusPainted(false);
        registerButton.setBorderPainted(false);
        registerButton.setPreferredSize(BUTTON_SIZE);
        registerButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        registerButton.addActionListener(this::registerActionPerformed);
        gbc.gridy = 7;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 0, 0, 0);
        card.add(registerButton, gbc);

        return card;
    }

    private JTextField addField(JPanel card, GridBagConstraints gbc, int row, String labelText, boolean password) {
        // Label
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 6, 0);
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(TEXT_COLOR);
        card.add(label, gbc);

        // Field
        JTextField field;
        if (password) {
            field = createStyledPasswordField("Ketik Di Sini");
        } else {
            field = createStyledField("Ketik Di Sini");
        }

        gbc.gridy = row + 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 0, 16, 0);
        card.add(field, gbc);

        return field;
    }

    // === HELPER: Styled TextField ===
    private JTextField createStyledField(String placeholder) {
        JTextField field = new JTextField();
        field.setPreferredSize(FIELD_SIZE);
        field.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        field.setForeground(PLACEHOLDER);
        field.setBackground(Color.WHITE);
        field.setBorder(new RoundedBorder(8, FIELD_BORDER));
        field.setText(placeholder);

        field.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(TEXT_COLOR);
                    field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(PLACEHOLDER);
                    field.setFont(new Font("Segoe UI", Font.ITALIC, 13));
                }
            }
        });

        return field;
    }

    // === HELPER: Styled PasswordField ===
    private JPasswordField createStyledPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField();
        field.setPreferredSize(FIELD_SIZE);
        field.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        field.setForeground(PLACEHOLDER);
        field.setBackground(Color.WHITE);
        field.setBorder(new RoundedBorder(8, FIELD_BORDER));
        field.setEchoChar((char) 0);
        field.setText(placeholder);

        field.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                String current = new String(field.getPassword());
                if (current.equals(placeholder)) {
                    field.setText("");
                    field.setForeground(TEXT_COLOR);
                    field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                    field.setEchoChar('•');
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                String current = new String(field.getPassword());
                if (current.isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(PLACEHOLDER);
                    field.setFont(new Font("Segoe UI", Font.ITALIC, 13));
                    field.setEchoChar((char) 0);
                }
            }
        });

        return field;
    }

    // === CUSTOM ROUNDED BORDER ===
    private static class RoundedBorder implements javax.swing.border.Border {
        private int radius;
        private Color color;

        RoundedBorder(int radius, Color color) {
            this.radius = radius;
            this.color = color;
        }

        @Override
        public void paintBorder(java.awt.Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2.dispose();
        }

        @Override
        public java.awt.Insets getBorderInsets(java.awt.Component c) {
            return new java.awt.Insets(radius / 2 + 2, radius + 5, radius / 2 + 2, radius + 5);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }
    }

    // === GRADIENT PANEL ===
    private static class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            GradientPaint gp = new GradientPaint(0, 0, GRADIENT_START, getWidth(), getHeight(), GRADIENT_END);
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.dispose();
        }
    }

    private void registerActionPerformed(java.awt.event.ActionEvent event) {
        // Ambil value, skip placeholder
        String username = usernameField.getText().equals("Ketik Di Sini") ? "" : usernameField.getText();
        String password = new String(passwordField.getPassword()).equals("Ketik Di Sini") ? "" : new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword()).equals("Ketik Di Sini") ? "" : new String(confirmPasswordField.getPassword());

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showWarning("Semua field wajib diisi.");
            return;
        }
        if (password.length() < 6) {
            showWarning("Password minimal 6 karakter.");
            return;
        }
        if (!password.equals(confirmPassword)) {
            showWarning("Konfirmasi password tidak cocok.");
            return;
        }

        JOptionPane.showMessageDialog(this,
                "Data register sudah valid.\nUntuk menyimpan ke database, backend perlu endpoint self-register publik.",
                "Register siap", JOptionPane.INFORMATION_MESSAGE);
        openLogin();
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Validasi", JOptionPane.WARNING_MESSAGE);
    }

    private void openLogin() {
        new LoginForm(libraryApi).setVisible(true);
        dispose();
    }

    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(() -> new SignUp().setVisible(true));
    }
}