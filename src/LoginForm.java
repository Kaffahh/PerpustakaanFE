import java.awt.BorderLayout;
import java.awt.Color;
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
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;

public class LoginForm extends JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger
            .getLogger(LoginForm.class.getName());
    private final com.mycompany.perpustakaan.api.LibraryApi libraryApi;
    private JTextField usernameField;
    private JPasswordField passwordField;

    // === WARNA DARI DESIGN ===
    private static final Color BG_APP = new Color(247, 248, 250);
    private static final Color WHITE = Color.WHITE;
    private static final Color ACCENT = new Color(232, 130, 90);
    private static final Color ACCENT_DARK = new Color(196, 149, 94);
    private static final Color ACCENT_LIGHT = new Color(255, 246, 241);
    private static final Color CARD_BORDER = new Color(238, 232, 226);
    private static final Color TEXT_DARK = new Color(50, 50, 50);
    private static final Color TEXT_GRAY = new Color(120, 120, 120);
    private static final Color PLACEHOLDER = new Color(165, 165, 165);

    private static final Dimension FRAME_SIZE = new Dimension(980, 620);
    private static final Dimension CARD_SIZE = new Dimension(420, 500);
    private static final Dimension FIELD_SIZE = new Dimension(320, 42);
    private static final Dimension BUTTON_SIZE = new Dimension(320, 44);
    private static final Dimension LOGO_SIZE = new Dimension(150, 110);

    public LoginForm() {
        this(new com.mycompany.perpustakaan.api.LibraryApi());
    }

    public LoginForm(com.mycompany.perpustakaan.api.LibraryApi libraryApi) {
        this.libraryApi = libraryApi;

        // === FIX: Disable Nimbus interference ===
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // fallback
        }

        initComponents();
    }

    private void initComponents() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle("Login Perpustakaan");
        setResizable(false);

        JPanel root = new AuthBackgroundPanel();
        root.setLayout(new GridBagLayout());
        root.setBorder(new EmptyBorder(28, 28, 28, 28));
        setContentPane(root);

        JPanel layout = new JPanel(new GridBagLayout());
        layout.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, 28);
        layout.add(createBrandPanel(), gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        layout.add(createLoginCard(), gbc);

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
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel logo = new JLabel();
        try {
            ImageIcon icon = loadScaledIcon("/assets/branding/library-logo.png", 220, 150);
            logo.setIcon(icon);
        } catch (Exception e) {
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
                + "Sistem perpustakaan digital untuk mengelola buku, peminjaman, kunjungan, dan laporan dengan lebih rapi."
                + "</div></html>");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(TEXT_GRAY);
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);

        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 28, 0);
        panel.add(subtitle, gbc);

        JPanel info = new JPanel(new GridBagLayout());
        info.setOpaque(false);

        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(info, gbc);

        return panel;
    }

    private JPanel createBrandInfoItem(String titleText, String descText) {
        JPanel item = new JPanel(new GridBagLayout());
        item.setOpaque(false);
        item.setPreferredSize(new Dimension(260, 82));
        item.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER),
                new EmptyBorder(14, 16, 14, 16)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.WEST;

        JLabel title = new JLabel(titleText);
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(TEXT_DARK);

        JLabel desc = new JLabel("<html><div style='width:220px;'>" + descText + "</div></html>");
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        desc.setForeground(TEXT_GRAY);

        gbc.gridy = 0;
        item.add(title, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(6, 0, 0, 0);
        item.add(desc, gbc);

        return item;
    }

    private JPanel createLoginCard() {
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
        card.setBorder(new EmptyBorder(34, 44, 40, 44));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel badge = new JLabel("WELCOME BACK");
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setForeground(ACCENT_DARK);
        badge.setOpaque(true);
        badge.setBackground(ACCENT_LIGHT);
        badge.setBorder(new EmptyBorder(7, 13, 7, 13));

        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 18, 0);
        card.add(badge, gbc);

        JLabel title = new JLabel("Masuk Akun");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(TEXT_DARK);
        title.setHorizontalAlignment(SwingConstants.CENTER);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 6, 0);
        card.add(title, gbc);

        JLabel subtitle = new JLabel("Login untuk mengakses dashboard perpustakaan.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(TEXT_GRAY);
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);

        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 28, 0);
        card.add(subtitle, gbc);

        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 8, 0);
        card.add(createLabel("Username"), gbc);

        usernameField = createStyledField("Ketik username kamu");
        usernameField.addActionListener(e -> passwordField.requestFocusInWindow());

        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 0, 16, 0);
        card.add(usernameField, gbc);

        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 8, 0);
        card.add(createLabel("Password"), gbc);

        passwordField = createStyledPasswordField("Ketik password kamu");
        passwordField.addActionListener(this::loginActionPerformed);

        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 0, 26, 0);
        card.add(passwordField, gbc);

        JButton loginButton = createStyledButton("Login Sekarang");
        loginButton.addActionListener(this::loginActionPerformed);

        gbc.gridy = 7;
        gbc.insets = new Insets(0, 0, 22, 0);
        card.add(loginButton, gbc);

        JPanel footer = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 0));
        footer.setOpaque(false);

        JLabel textLabel = new JLabel("Belum punya akun?");
        textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        textLabel.setForeground(TEXT_GRAY);

        JLabel linkLabel = new JLabel("Daftar di sini");
        linkLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        linkLabel.setForeground(ACCENT);
        linkLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        linkLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                openSignUp();
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

        gbc.gridy = 8;
        gbc.insets = new Insets(0, 0, 0, 0);
        card.add(footer, gbc);

        return card;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(TEXT_DARK);
        return label;
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

    // === STYLED TEXTFIELD ===
    private JTextField createStyledField(String placeholder) {
        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(WHITE);
                g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);

                g2d.setColor(hasFocus() ? ACCENT : CARD_BORDER);
                g2d.drawRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 16, 16);

                g2d.dispose();
                super.paintComponent(g);
            }
        };

        field.setPreferredSize(FIELD_SIZE);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setForeground(PLACEHOLDER);
        field.setBackground(WHITE);
        field.setOpaque(false);
        field.setBorder(new EmptyBorder(9, 14, 9, 14));
        field.setText(placeholder);

        field.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(TEXT_DARK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().trim().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(PLACEHOLDER);
                }
            }
        });

        return field;
    }

    // === STYLED PASSWORD FIELD ===
    private JPasswordField createStyledPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(WHITE);
                g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);

                g2d.setColor(hasFocus() ? ACCENT : CARD_BORDER);
                g2d.drawRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 16, 16);

                g2d.dispose();
                super.paintComponent(g);
            }
        };

        field.setPreferredSize(FIELD_SIZE);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setForeground(PLACEHOLDER);
        field.setBackground(WHITE);
        field.setOpaque(false);
        field.setBorder(new EmptyBorder(9, 14, 9, 14));
        field.setEchoChar((char) 0);
        field.setText(placeholder);

        field.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                String current = new String(field.getPassword());
                if (current.equals(placeholder)) {
                    field.setText("");
                    field.setForeground(TEXT_DARK);
                    field.setEchoChar('\u2022');
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                String current = new String(field.getPassword());
                if (current.trim().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(PLACEHOLDER);
                    field.setEchoChar((char) 0);
                }
            }
        });

        return field;
    }

    // === STYLED BUTTON ===
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
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        return button;
    }

    // === GRADIENT BACKGROUND ===
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

    private void loginActionPerformed(java.awt.event.ActionEvent event) {
        String username = usernameField.getText().equals("Ketik username kamu") ? "" : usernameField.getText();
        String password = new String(passwordField.getPassword()).equals("Ketik password kamu") ? ""
                : new String(passwordField.getPassword());

        try {
            com.mycompany.perpustakaan.api.AuthResponse response = libraryApi.login(username, password);
            if (!response.isSuccess()) {
                JOptionPane.showMessageDialog(this, response.getMessage(), "Login gagal", JOptionPane.ERROR_MESSAGE);
                return;
            }

            new Dashboard(libraryApi).setVisible(true);
            dispose();
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this, "Gagal terhubung ke backend: " + exception.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            logger.log(java.util.logging.Level.SEVERE, "Login gagal", exception);
        } catch (IllegalArgumentException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Validasi", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void openSignUp() {
        new SignUp(libraryApi).setVisible(true);
        dispose();
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> new LoginForm().setVisible(true));
    }
}
