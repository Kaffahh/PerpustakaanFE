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

public class SignUp extends JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(SignUp.class.getName());
    private final com.mycompany.perpustakaan.api.LibraryApi libraryApi;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;

    private static final Color GRADIENT_START = new Color(232, 130, 90);
    private static final Color GRADIENT_END   = new Color(190, 160, 130);
    private static final Color CARD_BG        = Color.WHITE;
    private static final Color TEXT_COLOR     = new Color(50, 50, 50);
    private static final Color BUTTON_BG      = new Color(232, 130, 90);
    private static final Color FIELD_BORDER   = new Color(220, 220, 220);
    private static final Color PLACEHOLDER    = new Color(180, 180, 180);

    private static final Dimension FRAME_SIZE  = new Dimension(900, 600);
    private static final Dimension CARD_SIZE   = new Dimension(380, 480);
    private static final Dimension FIELD_SIZE  = new Dimension(280, 40);
    private static final Dimension BUTTON_SIZE = new Dimension(120, 40);

    public SignUp() {
        this(new com.mycompany.perpustakaan.api.LibraryApi());
    }

    public SignUp(com.mycompany.perpustakaan.api.LibraryApi libraryApi) {
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
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(30, 40, 40, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;

        // === TITLE ===
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 30, 0);
        JLabel title = new JLabel("Daftar");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(TEXT_COLOR);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(title, gbc);

        // === FIELDS ===
        usernameField = addField(card, gbc, 1, "Username  :", false);
        passwordField = (JPasswordField) addField(card, gbc, 3, "Password  :", true);
        confirmPasswordField = (JPasswordField) addField(card, gbc, 5, "Konfirmasi :", true);

        // === BUTTON ===
        JButton registerButton = createStyledButton("Daftar");
        registerButton.addActionListener(this::registerActionPerformed);
        gbc.gridy = 7;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 0, 0, 0);
        card.add(registerButton, gbc);

        return card;
    }

    private JTextField addField(JPanel card, GridBagConstraints gbc, int row, String labelText, boolean password) {
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 6, 0);
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(TEXT_COLOR);
        card.add(label, gbc);

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

    // === STYLED TEXTFIELD ===
    private JTextField createStyledField(String placeholder) {
        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2d.setColor(new Color(0, 0, 0, 20));
                g2d.fillRoundRect(2, 2, getWidth() - 2, getHeight() - 2, 12, 12);
                
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 12, 12);
                
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
                
                g2d.setColor(new Color(0, 0, 0, 20));
                g2d.fillRoundRect(2, 2, getWidth() - 2, getHeight() - 2, 12, 12);
                
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 12, 12);
                
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

    // === STYLED BUTTON ===
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2d.setColor(new Color(0, 0, 0, 30));
                g2d.fillRoundRect(2, 2, getWidth() - 2, getHeight() - 2, 12, 12);
                
                if (getModel().isPressed()) {
                    g2d.setColor(BUTTON_BG.darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(BUTTON_BG.brighter());
                } else {
                    g2d.setColor(BUTTON_BG);
                }
                g2d.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 12, 12);
                
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
        button.setContentAreaFilled(false);
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        
        return button;
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

        try {
            com.mycompany.perpustakaan.api.MemberRequest request =
                    new com.mycompany.perpustakaan.api.MemberRequest(username, username, null, password);
            com.mycompany.perpustakaan.api.MemberResponse response = libraryApi.register(request);
            if (!response.isSuccess()) {
                showWarning(response.getMessage());
                return;
            }

            JOptionPane.showMessageDialog(this, response.getMessage(), "Register berhasil", JOptionPane.INFORMATION_MESSAGE);
            openLogin();
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this, "Gagal terhubung ke backend: " + exception.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
