import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.sql.SQLException;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

public class SignUp extends JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(SignUp.class.getName());

    private final com.mycompany.perpustakaan.api.LibraryApi libraryApi;

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;

    private static final Color BG_APP = new Color(247, 248, 250);
    private static final Color WHITE = Color.WHITE;
    private static final Color ACCENT = new Color(232, 130, 90);
    private static final Color ACCENT_DARK = new Color(196, 149, 94);
    private static final Color ACCENT_LIGHT = new Color(255, 246, 241);
    private static final Color CARD_BORDER = new Color(238, 232, 226);
    private static final Color TEXT_DARK = new Color(50, 50, 50);
    private static final Color TEXT_GRAY = new Color(120, 120, 120);
    private static final Color PLACEHOLDER = new Color(165, 165, 165);
    private static final Color GREEN_STATUS = new Color(0, 200, 83);
    private static final Color RED_STATUS = new Color(255, 23, 68);

    private static final Dimension FRAME_SIZE = new Dimension(980, 620);
    private static final Dimension CARD_SIZE = new Dimension(420, 540);
    private static final Dimension FIELD_SIZE = new Dimension(320, 42);
    private static final Dimension BUTTON_SIZE = new Dimension(320, 44);

    private static final String USERNAME_PLACEHOLDER = "Ketik username kamu";
    private static final String PASSWORD_PLACEHOLDER = "Ketik password kamu";
    private static final String CONFIRM_PLACEHOLDER = "Ulangi password kamu";

    public SignUp() {
        this(new com.mycompany.perpustakaan.api.LibraryApi());
    }

    public SignUp(com.mycompany.perpustakaan.api.LibraryApi libraryApi) {
        this.libraryApi = libraryApi;

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // fallback
        }

        initComponents();
    }

    private void initComponents() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle("Daftar Akun Perpustakaan");
        setResizable(false);

        JPanel root = new AuthBackgroundPanel();
        root.setLayout(new GridBagLayout());
        root.setBorder(new EmptyBorder(28, 28, 28, 28));
        setContentPane(root);

        JPanel layout = new JPanel(new GridBagLayout());
        layout.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;

        gbc.gridx = 0;
        gbc.insets = new Insets(0, 0, 0, 28);
        layout.add(createBrandPanel(), gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        layout.add(createRegisterCard(), gbc);

        root.add(layout, new GridBagConstraints());

        pack();
        setSize(FRAME_SIZE);
        setLocationRelativeTo(null);
    }

    private JPanel createBrandPanel() {
        JPanel panel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(ACCENT_LIGHT);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 32, 32);

                g2d.setColor(new Color(255, 226, 214));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 32, 32);

                g2d.dispose();
                super.paintComponent(g);
            }
        };

        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(360, 500));
        panel.setBorder(new EmptyBorder(34, 34, 34, 34));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel logo = new JLabel();
        ImageIcon icon = loadScaledIcon("/assets/branding/library-logo.png", 220, 150);

        if (icon != null) {
            logo.setIcon(icon);
        } else {
            logo.setText("LIBRARY HUB");
            logo.setFont(new Font("Segoe UI", Font.BOLD, 22));
            logo.setForeground(ACCENT_DARK);
        }

        logo.setHorizontalAlignment(SwingConstants.CENTER);

        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 34, 0);
        panel.add(logo, gbc);

        JLabel title = new JLabel("Library Hub");
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setForeground(TEXT_DARK);
        title.setHorizontalAlignment(SwingConstants.CENTER);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 10, 0);
        panel.add(title, gbc);

        JLabel subtitle = new JLabel("<html><div style='text-align:center; width:260px;'>"
                + "Sistem perpustakaan digital untuk mengelola buku, peminjaman, kunjungan, dan laporan."
                + "</div></html>");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(TEXT_GRAY);
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);

        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(subtitle, gbc);

        return panel;
    }

    private JPanel createRegisterCard() {
        JPanel card = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(new Color(0, 0, 0, 22));
                g2d.fillRoundRect(6, 8, getWidth() - 12, getHeight() - 12, 32, 32);

                g2d.setColor(WHITE);
                g2d.fillRoundRect(0, 0, getWidth() - 8, getHeight() - 8, 32, 32);

                g2d.setColor(CARD_BORDER);
                g2d.drawRoundRect(0, 0, getWidth() - 9, getHeight() - 9, 32, 32);

                g2d.dispose();
                super.paintComponent(g);
            }
        };

        card.setPreferredSize(CARD_SIZE);
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(42, 44, 38, 44));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;

        // Row 0: Title
        JLabel title = new JLabel("Daftar Akun");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(TEXT_DARK);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 8, 0);
        card.add(title, gbc);

        // Row 1: Subtitle
        JLabel subtitle = new JLabel("Buat akun untuk mengakses perpustakaan.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(TEXT_GRAY);
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 28, 0);
        card.add(subtitle, gbc);

        // Row 2: Username label
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 8, 0);
        card.add(createLabel("Username"), gbc);

        // Row 3: Username field
        usernameField = createStyledField(USERNAME_PLACEHOLDER);
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 0, 14, 0);
        card.add(usernameField, gbc);

        // Row 4: Password label
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 8, 0);
        card.add(createLabel("Password"), gbc);

        // Row 5: Password field
        passwordField = createStyledPasswordField(PASSWORD_PLACEHOLDER);
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 0, 4, 0);
        card.add(passwordField, gbc);

        // Row 6: Strength indicator
        JProgressBar strengthBar = new JProgressBar(0, 100);
        strengthBar.setPreferredSize(new Dimension(320, 6));
        strengthBar.setMaximumSize(new Dimension(320, 6));
        strengthBar.setMinimumSize(new Dimension(320, 6));
        strengthBar.setValue(0);
        strengthBar.setForeground(new Color(200, 200, 200));
        strengthBar.setBackground(WHITE);
        strengthBar.setBorderPainted(false);
        strengthBar.setOpaque(true);

        JLabel strengthLabel = new JLabel("Kekuatan password");
        strengthLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        strengthLabel.setForeground(TEXT_GRAY);

        JPanel strengthPanel = new JPanel(new BorderLayout(8, 0));
        strengthPanel.setOpaque(false);
        strengthPanel.setPreferredSize(new Dimension(320, 20));
        strengthPanel.setMaximumSize(new Dimension(320, 20));
        strengthPanel.add(strengthBar, BorderLayout.CENTER);
        strengthPanel.add(strengthLabel, BorderLayout.EAST);

        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 0, 14, 0);
        card.add(strengthPanel, gbc);

        passwordField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                String pwd = new String(passwordField.getPassword());
                if (pwd.equals(PASSWORD_PLACEHOLDER) || pwd.isEmpty()) {
                    strengthBar.setValue(0);
                    strengthBar.setForeground(new Color(200, 200, 200));
                    strengthLabel.setText("Kekuatan password");
                    return;
                }
                int score = calculatePasswordStrength(pwd);
                strengthBar.setValue(score);
                if (score < 30) {
                    strengthBar.setForeground(RED_STATUS);
                    strengthLabel.setText("Lemah");
                } else if (score < 60) {
                    strengthBar.setForeground(new Color(255, 183, 77));
                    strengthLabel.setText("Sedang");
                } else if (score < 85) {
                    strengthBar.setForeground(new Color(139, 195, 74));
                    strengthLabel.setText("Kuat");
                } else {
                    strengthBar.setForeground(GREEN_STATUS);
                    strengthLabel.setText("Sangat Kuat");
                }
            }
        });

        // Row 7: Confirm password label
        gbc.gridy = 7;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 8, 0);
        card.add(createLabel("Konfirmasi Password"), gbc);

        // Row 8: Confirm password field
        confirmPasswordField = createStyledPasswordField(CONFIRM_PLACEHOLDER);
        confirmPasswordField.addActionListener(this::registerActionPerformed);
        gbc.gridy = 8;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 0, 16, 0);
        card.add(confirmPasswordField, gbc);

        // Row 9: Register button
        JButton registerButton = createStyledButton("Daftar Sekarang");
        registerButton.addActionListener(this::registerActionPerformed);
        gbc.gridy = 9;
        gbc.insets = new Insets(0, 0, 12, 0);
        card.add(registerButton, gbc);

        // Row 10: Footer
        JPanel footer = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 0));
        footer.setOpaque(false);

        JLabel textLabel = new JLabel("Sudah punya akun?");
        textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        textLabel.setForeground(TEXT_GRAY);

        JLabel linkLabel = new JLabel("Masuk di sini");
        linkLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        linkLabel.setForeground(ACCENT);
        linkLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        linkLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                openLogin();
            }

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                linkLabel.setForeground(ACCENT_DARK);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                linkLabel.setForeground(ACCENT);
            }
        });

        footer.add(textLabel);
        footer.add(linkLabel);

        gbc.gridy = 10;
        gbc.insets = new Insets(0, 0, 0, 0);
        card.add(footer, gbc);

        return card;
    }

    private int calculatePasswordStrength(String password) {
        int score = 0;
        if (password == null || password.isEmpty()) return 0;
        
        // Length scoring
        if (password.length() >= 8) score += 25;
        else if (password.length() >= 6) score += 15;
        else score += 5;
        
        // Contains uppercase
        if (!password.equals(password.toLowerCase())) score += 15;
        
        // Contains lowercase
        if (!password.equals(password.toUpperCase())) score += 10;
        
        // Contains digits
        if (password.matches(".*\\d.*")) score += 20;
        
        // Contains special chars
        if (password.matches(".*[!@#$%^&*(),.?\":{}|<>_].*")) score += 20;
        
        // Mix of character types
        int types = 0;
        if (password.matches(".*[a-z].*")) types++;
        if (password.matches(".*[A-Z].*")) types++;
        if (password.matches(".*\\d.*")) types++;
        if (password.matches(".*[!@#$%^&*(),.?\":{}|<>_].*")) types++;
        if (types >= 3) score += 10;
        
        return Math.min(100, score);
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(TEXT_DARK);
        return label;
    }

    private JTextField createStyledField(String placeholder) {
        JTextField field = new JTextField();
        field.setPreferredSize(FIELD_SIZE);
        field.setMinimumSize(FIELD_SIZE);
        field.setMaximumSize(FIELD_SIZE);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setForeground(PLACEHOLDER);
        field.setBackground(WHITE);
        field.setBorder(new EmptyBorder(9, 14, 9, 14));
        field.setText(placeholder);
        field.setOpaque(true);

        field.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                new RoundedLineBorder(CARD_BORDER, 16),
                new EmptyBorder(9, 14, 9, 14)));

        field.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(TEXT_DARK);
                }
                field.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                        new RoundedLineBorder(ACCENT, 16),
                        new EmptyBorder(9, 14, 9, 14)));
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().trim().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(PLACEHOLDER);
                }
                field.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                        new RoundedLineBorder(CARD_BORDER, 16),
                        new EmptyBorder(9, 14, 9, 14)));
            }
        });

        return field;
    }

    private JPasswordField createStyledPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField();
        field.setPreferredSize(FIELD_SIZE);
        field.setMinimumSize(FIELD_SIZE);
        field.setMaximumSize(FIELD_SIZE);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setForeground(PLACEHOLDER);
        field.setBackground(WHITE);
        field.setEchoChar((char) 0);
        field.setText(placeholder);
        field.setOpaque(true);

        field.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                new RoundedLineBorder(CARD_BORDER, 16),
                new EmptyBorder(9, 14, 9, 14)));

        field.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                String current = new String(field.getPassword());
                if (current.equals(placeholder)) {
                    field.setText("");
                    field.setForeground(TEXT_DARK);
                    field.setEchoChar('\u2022');
                }
                field.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                        new RoundedLineBorder(ACCENT, 16),
                        new EmptyBorder(9, 14, 9, 14)));
            }

            @Override
            public void focusLost(FocusEvent e) {
                String current = new String(field.getPassword());
                if (current.trim().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(PLACEHOLDER);
                    field.setEchoChar((char) 0);
                }
                field.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                        new RoundedLineBorder(CARD_BORDER, 16),
                        new EmptyBorder(9, 14, 9, 14)));
            }
        });

        return field;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(new Color(0, 0, 0, 18));
                g2d.fillRoundRect(0, 4, getWidth(), getHeight() - 4, 18, 18);

                Color start = getModel().isRollover() ? ACCENT_DARK : ACCENT;
                Color end = getModel().isRollover() ? ACCENT : ACCENT_DARK;

                GradientPaint gp = new GradientPaint(0, 0, start, getWidth(), getHeight(), end);
                g2d.setPaint(gp);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight() - 4, 18, 18);

                g2d.dispose();
                super.paintComponent(g);
            }
        };

        button.setPreferredSize(BUTTON_SIZE);
        button.setMinimumSize(BUTTON_SIZE);
        button.setMaximumSize(BUTTON_SIZE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        return button;
    }

    private ImageIcon loadScaledIcon(String resourcePath, int maxWidth, int maxHeight) {
        java.net.URL resource = getClass().getResource(resourcePath);

        if (resource == null) {
            return null;
        }

        ImageIcon source = new ImageIcon(resource);
        int sourceWidth = source.getIconWidth();
        int sourceHeight = source.getIconHeight();

        if (sourceWidth <= 0 || sourceHeight <= 0) {
            return source;
        }

        double scale = Math.min((double) maxWidth / sourceWidth, (double) maxHeight / sourceHeight);
        int targetWidth = Math.max(1, (int) Math.round(sourceWidth * scale));
        int targetHeight = Math.max(1, (int) Math.round(sourceHeight * scale));

        Image scaled = source.getImage().getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    private static class RoundedLineBorder extends javax.swing.border.AbstractBorder {
        private final Color color;
        private final int radius;

        RoundedLineBorder(Color color, int radius) {
            this.color = color;
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(color);
            g2d.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2d.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(1, 1, 1, 1);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = 1;
            insets.right = 1;
            insets.top = 1;
            insets.bottom = 1;
            return insets;
        }
    }

    private static class AuthBackgroundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2d.setColor(BG_APP);
            g2d.fillRect(0, 0, getWidth(), getHeight());

            GradientPaint gp = new GradientPaint(
                    0, 0, ACCENT_LIGHT,
                    getWidth(), getHeight(), WHITE);
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, getWidth(), getHeight());

            g2d.setColor(new Color(232, 130, 90, 35));
            g2d.fillOval(-120, -120, 300, 300);

            g2d.setColor(new Color(196, 149, 94, 28));
            g2d.fillOval(getWidth() - 180, getHeight() - 180, 320, 320);

            g2d.dispose();
        }
    }

    private void registerActionPerformed(java.awt.event.ActionEvent event) {
        String username = usernameField.getText().equals(USERNAME_PLACEHOLDER) ? "" : usernameField.getText();

        String password = new String(passwordField.getPassword()).equals(PASSWORD_PLACEHOLDER)
                ? ""
                : new String(passwordField.getPassword());

        String confirmPassword = new String(confirmPasswordField.getPassword()).equals(CONFIRM_PLACEHOLDER)
                ? ""
                : new String(confirmPasswordField.getPassword());

        if (username.trim().isEmpty() || password.trim().isEmpty() || confirmPassword.trim().isEmpty()) {
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

        try {
            com.mycompany.perpustakaan.api.MemberRequest request = new com.mycompany.perpustakaan.api.MemberRequest(
                    username.trim(),
                    username.trim(),
                    null,
                    password);

            com.mycompany.perpustakaan.api.MemberResponse response = libraryApi.register(request);

            if (!response.isSuccess()) {
                showWarning(response.getMessage());
                return;
            }

            JOptionPane.showMessageDialog(
                    this,
                    response.getMessage(),
                    "Register berhasil",
                    JOptionPane.INFORMATION_MESSAGE);

            openLogin();
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(
                    this,
                    "Gagal terhubung ke backend: " + exception.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            logger.log(java.util.logging.Level.SEVERE, "Register gagal", exception);
        } catch (IllegalArgumentException exception) {
            showWarning(exception.getMessage());
        }
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Validasi", JOptionPane.WARNING_MESSAGE);
    }

    private void openLogin() {
        new LoginForm(libraryApi).setVisible(true);
        dispose();
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> new SignUp().setVisible(true));
    }
}