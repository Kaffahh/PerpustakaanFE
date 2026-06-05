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

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(LoginForm.class.getName());
    private final com.mycompany.perpustakaan.api.LibraryApi libraryApi;
    private JTextField usernameField;
    private JPasswordField passwordField;

    // === WARNA DARI DESIGN ===
    private static final Color GRADIENT_START = new Color(232, 130, 90);
    private static final Color GRADIENT_END   = new Color(190, 160, 130);
    private static final Color CARD_BG        = Color.WHITE;
    private static final Color TEXT_COLOR     = new Color(50, 50, 50);
    private static final Color ACCENT_COLOR   = new Color(232, 130, 90);
    private static final Color BUTTON_BG      = new Color(232, 130, 90);
    private static final Color FIELD_BORDER   = new Color(220, 220, 220);
    private static final Color PLACEHOLDER    = new Color(180, 180, 180);

    private static final Dimension FRAME_SIZE  = new Dimension(900, 600);
    private static final Dimension CARD_SIZE   = new Dimension(380, 520);
    private static final Dimension FIELD_SIZE  = new Dimension(280, 40);
    private static final Dimension BUTTON_SIZE = new Dimension(120, 40);

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

        JPanel root = new GradientPanel();
        root.setLayout(new GridBagLayout());
        setContentPane(root);

        root.add(createLoginCard(), new GridBagConstraints());

        pack();
        setSize(FRAME_SIZE);
        setLocationRelativeTo(null);
    }

    private JPanel createLoginCard() {
        JPanel card = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Shadow
                g2d.setColor(new Color(0, 0, 0, 30));
                g2d.fillRoundRect(4, 4, getWidth() - 4, getHeight() - 4, 20, 20);
                
                // Card body
                g2d.setColor(CARD_BG);
                g2d.fillRoundRect(0, 0, getWidth() - 4, getHeight() - 4, 20, 20);
                
                g2d.dispose();
            }
        };
        card.setPreferredSize(CARD_SIZE);
        card.setOpaque(false); // Penting! biar paintComponent jalan
        card.setBorder(new EmptyBorder(20, 40, 30, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;

        // === LOGO ===
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 10, 0);
        JLabel logo = new JLabel();
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/logo3.png"));
            logo.setIcon(icon);
        } catch (Exception e) {
            logo.setText("📚 LIBRARY HUB");
            logo.setFont(new Font("Segoe UI", Font.BOLD, 16));
            logo.setForeground(new Color(139, 90, 43));
        }
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(logo, gbc);

        // === TITLE ===
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 25, 0);
        JLabel title = new JLabel("Masuk");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(TEXT_COLOR);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(title, gbc);

        // === USERNAME ===
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 8, 0);
        card.add(createLabel("Username  :"), gbc);

        usernameField = createStyledField("Ketik Di Sini");
        usernameField.addActionListener(e -> passwordField.requestFocusInWindow());
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 0, 18, 0);
        card.add(usernameField, gbc);

        // === PASSWORD ===
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 8, 0);
        card.add(createLabel("Password  :"), gbc);

        passwordField = createStyledPasswordField("Ketik Di Sini");
        passwordField.addActionListener(this::loginActionPerformed);
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 0, 25, 0);
        card.add(passwordField, gbc);

        // === LOGIN BUTTON ===
        JButton loginButton = createStyledButton("Login");
        loginButton.addActionListener(this::loginActionPerformed);
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 0, 20, 0);
        card.add(loginButton, gbc);

        // === FOOTER ===
        JPanel footer = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 0));
        footer.setOpaque(false);

        JLabel textLabel = new JLabel("Belum punya akun?");
        textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        textLabel.setForeground(TEXT_COLOR);

        JLabel linkLabel = new JLabel("Daftar di sini");
        linkLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        linkLabel.setForeground(ACCENT_COLOR);
        linkLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        linkLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                openSignUp();
            }
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                linkLabel.setText("<html><u>Daftar di sini</u></html>");
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                linkLabel.setText("Daftar di sini");
            }
        });

        footer.add(textLabel);
        footer.add(linkLabel);

        gbc.gridy = 7;
        gbc.insets = new Insets(0, 0, 0, 0);
        card.add(footer, gbc);

        return card;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(TEXT_COLOR);
        return label;
    }

    // === STYLED TEXTFIELD (Rounded + Shadow + Placeholder) ===
    private JTextField createStyledField(String placeholder) {
        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Shadow
                g2d.setColor(new Color(0, 0, 0, 20));
                g2d.fillRoundRect(2, 2, getWidth() - 2, getHeight() - 2, 12, 12);
                
                // Background
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 12, 12);
                
                // Border
                g2d.setColor(FIELD_BORDER);
                g2d.drawRoundRect(0, 0, getWidth() - 3, getHeight() - 3, 12, 12);
                
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        
        field.setPreferredSize(FIELD_SIZE);
        field.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        field.setForeground(PLACEHOLDER);
        field.setBackground(Color.WHITE);
        field.setOpaque(false); // Penting! biar paintComponent jalan
        field.setBorder(new EmptyBorder(8, 14, 8, 14));
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

    // === STYLED PASSWORD FIELD ===
    private JPasswordField createStyledPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Shadow
                g2d.setColor(new Color(0, 0, 0, 20));
                g2d.fillRoundRect(2, 2, getWidth() - 2, getHeight() - 2, 12, 12);
                
                // Background
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 12, 12);
                
                // Border
                g2d.setColor(FIELD_BORDER);
                g2d.drawRoundRect(0, 0, getWidth() - 3, getHeight() - 3, 12, 12);
                
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        
        field.setPreferredSize(FIELD_SIZE);
        field.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        field.setForeground(PLACEHOLDER);
        field.setBackground(Color.WHITE);
        field.setOpaque(false);
        field.setBorder(new EmptyBorder(8, 14, 8, 14));
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
                    field.setEchoChar('\u2022');
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

    // === STYLED BUTTON (Rounded + Flat) ===
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Shadow
                g2d.setColor(new Color(0, 0, 0, 30));
                g2d.fillRoundRect(2, 2, getWidth() - 2, getHeight() - 2, 12, 12);
                
                // Button body
                if (getModel().isPressed()) {
                    g2d.setColor(BUTTON_BG.darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(BUTTON_BG.brighter());
                } else {
                    g2d.setColor(BUTTON_BG);
                }
                g2d.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 12, 12);
                
                // Text
                g2d.setColor(Color.WHITE);
                g2d.setFont(getFont());
                java.awt.FontMetrics fm = g2d.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(getText())) / 2;
                int textY = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2d.drawString(getText(), textX, textY);
                
                g2d.dispose();
            }
        };
        
        button.setPreferredSize(BUTTON_SIZE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false); // Penting! disable default button paint
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        
        return button;
    }

    // === GRADIENT BACKGROUND ===
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

    private void loginActionPerformed(java.awt.event.ActionEvent event) {
        String username = usernameField.getText().equals("Ketik Di Sini") ? "" : usernameField.getText();
        String password = new String(passwordField.getPassword()).equals("Ketik Di Sini") ? "" : new String(passwordField.getPassword());

        try {
            com.mycompany.perpustakaan.api.AuthResponse response = libraryApi.login(username, password);
            if (!response.isSuccess()) {
                JOptionPane.showMessageDialog(this, response.getMessage(), "Login gagal", JOptionPane.ERROR_MESSAGE);
                return;
            }
            new Dashboard(libraryApi).setVisible(true);
            dispose();
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this, "Gagal terhubung ke backend: " + exception.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
