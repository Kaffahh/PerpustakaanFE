import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

public class Dashboard extends JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Dashboard.class.getName());
    private final com.mycompany.perpustakaan.api.LibraryApi libraryApi;

    // === WARNA ===
    private static final Color BG_APP        = new Color(245, 245, 245);
    private static final Color WHITE         = Color.WHITE;
    private static final Color ACCENT        = new Color(232, 130, 90);   // Oranye
    private static final Color ACCENT_DARK   = new Color(196, 149, 94);   // Coklat muda
    private static final Color TEXT_DARK     = new Color(50, 50, 50);
    private static final Color TEXT_GRAY     = new Color(120, 120, 120);
    private static final Color BORDER_COLOR  = new Color(224, 224, 224);
    private static final Color GREEN_STATUS  = new Color(0, 200, 83);
    private static final Color RED_STATUS    = new Color(255, 23, 68);

    private static final Dimension FRAME_SIZE    = new Dimension(1280, 720);
    private static final Dimension SIDEBAR_SIZE  = new Dimension(220, 720);
    private static final Dimension HEADER_SIZE   = new Dimension(1060, 80);
    private static final Dimension CARD_SIZE     = new Dimension(160, 240);

    // Menu buttons
    private SidebarButton btnDashboard, btnBookshelf, btnLoan, btnProfile, btnHistory;
    private SidebarButton activeButton;

    // Content
    private JPanel contentPanel;
    private JLabel welcomeLabel, roleLabel;

    public Dashboard() {
        this(new com.mycompany.perpustakaan.api.LibraryApi());
    }

    public Dashboard(com.mycompany.perpustakaan.api.LibraryApi libraryApi) {
        this.libraryApi = libraryApi;
        
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // fallback
        }
        
        initComponents();
        loadUserData();
    }

    private void initComponents() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle("Dashboard Perpustakaan");
        setSize(FRAME_SIZE);
        setMinimumSize(FRAME_SIZE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BG_APP);
        setContentPane(root);

        // === SIDEBAR ===
        SidebarPanel sidebar = new SidebarPanel();
        root.add(sidebar, BorderLayout.WEST);

        // === MAIN AREA ===
        JPanel mainArea = new JPanel(new BorderLayout(0, 0));
        mainArea.setOpaque(false);

        // Header
        HeaderPanel header = new HeaderPanel();
        mainArea.add(header, BorderLayout.NORTH);

        // Content
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        JScrollPane contentScroll = new JScrollPane(contentPanel);
        contentScroll.setOpaque(false);
        contentScroll.getViewport().setOpaque(false);
        contentScroll.setBorder(null);
        contentScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        contentScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        mainArea.add(contentScroll, BorderLayout.CENTER);

        root.add(mainArea, BorderLayout.CENTER);

        // Load default content
        showDashboard();
    }

    // ============================================================
    // SIDEBAR
    // ============================================================
    private class SidebarPanel extends JPanel {
        SidebarPanel() {
            setPreferredSize(SIDEBAR_SIZE);
            setBackground(WHITE);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR));

            // Logo
            JLabel logo = new JLabel();
            try {
                logo.setIcon(new ImageIcon(getClass().getResource("/logo3.png")));
            } catch (Exception e) {
                logo.setText("📚 LIBRARY HUB");
                logo.setFont(new Font("Segoe UI", Font.BOLD, 14));
                logo.setForeground(new Color(139, 90, 43));
            }
            logo.setAlignmentX(Component.CENTER_ALIGNMENT);
            logo.setBorder(new EmptyBorder(20, 0, 10, 0));
            add(logo);

            // Menu title
            JLabel menuTitle = new JLabel("Menu");
            menuTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
            menuTitle.setForeground(TEXT_DARK);
            menuTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
            menuTitle.setBorder(new EmptyBorder(10, 0, 10, 0));
            add(menuTitle);

            add(Box.createVerticalStrut(10));

            // Menu buttons
            btnDashboard = new SidebarButton("Dashboard", "\u2630"); // ☰
            btnBookshelf = new SidebarButton("Bookshelf", "\u25A6"); // ▦
            btnLoan      = new SidebarButton("Loan Page", "\u25A0"); // ■
            btnProfile   = new SidebarButton("User Profile", "\u263A"); // ☺
            btnHistory   = new SidebarButton("History", "\u25CB"); // ○

            btnDashboard.addActionListener(e -> { setActive(btnDashboard); showDashboard(); });
            btnBookshelf.addActionListener(e -> { setActive(btnBookshelf); showBookshelf(); });
            btnLoan.addActionListener(e -> { setActive(btnLoan); showLoans(); });
            btnProfile.addActionListener(e -> { setActive(btnProfile); showProfile(); });
            btnHistory.addActionListener(e -> { setActive(btnHistory); showHistory(); });

            add(btnDashboard);
            add(Box.createVerticalStrut(8));
            add(btnBookshelf);
            add(Box.createVerticalStrut(8));
            add(btnLoan);
            add(Box.createVerticalStrut(8));
            add(btnProfile);
            add(Box.createVerticalStrut(8));
            add(btnHistory);

            setActive(btnDashboard); // Default active

            add(Box.createVerticalGlue());
        }
    }

    private class SidebarButton extends JButton {
        SidebarButton(String text, String icon) {
            super(icon + "   " + text);
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setForeground(TEXT_DARK);
            setBackground(WHITE);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setMaximumSize(new Dimension(180, 45));
            setPreferredSize(new Dimension(180, 45));
            setAlignmentX(Component.CENTER_ALIGNMENT);
            setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(10, 15, 10, 15));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (SidebarButton.this != activeButton) {
                        setBackground(new Color(250, 250, 250));
                    }
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    if (SidebarButton.this != activeButton) {
                        setBackground(WHITE);
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (this == activeButton) {
                GradientPaint gp = new GradientPaint(0, 0, ACCENT, getWidth(), getHeight(), ACCENT_DARK);
                g2d.setPaint(gp);
                g2d.fillRoundRect(5, 2, getWidth() - 10, getHeight() - 4, 12, 12);
                setForeground(WHITE);
            } else {
                g2d.setColor(getBackground());
                g2d.fillRoundRect(5, 2, getWidth() - 10, getHeight() - 4, 12, 12);
                setForeground(TEXT_DARK);
            }

            g2d.dispose();
            super.paintComponent(g);
        }
    }

    private void setActive(SidebarButton button) {
        if (activeButton != null) {
            activeButton.repaint();
        }
        activeButton = button;
        activeButton.repaint();
    }

    // ============================================================
    // HEADER
    // ============================================================
    private class HeaderPanel extends JPanel {
        HeaderPanel() {
            setPreferredSize(HEADER_SIZE);
            setBackground(WHITE);
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                new EmptyBorder(15, 25, 15, 25)
            ));

            // Left: Profile + Welcome
            JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
            left.setOpaque(false);

            JLabel avatar = new JLabel();
            try {
                avatar.setIcon(new ImageIcon(getClass().getResource("/PROFILE2.png")));
            } catch (Exception e) {
                avatar.setText("👤");
                avatar.setFont(new Font("Segoe UI", Font.PLAIN, 32));
            }
            avatar.setPreferredSize(new Dimension(50, 50));
            avatar.setHorizontalAlignment(SwingConstants.CENTER);

            JPanel textPanel = new JPanel();
            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
            textPanel.setOpaque(false);

            welcomeLabel = new JLabel("WELCOME, USER");
            welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            welcomeLabel.setForeground(TEXT_DARK);

            roleLabel = new JLabel("USER");
            roleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            roleLabel.setForeground(ACCENT);

            textPanel.add(welcomeLabel);
            textPanel.add(roleLabel);

            left.add(avatar);
            left.add(textPanel);

            // Right: Notification
            JLabel notif = new JLabel();
            try {
                notif.setIcon(new ImageIcon(getClass().getResource("/LONCENG.png")));
            } catch (Exception e) {
                notif.setText("🔔");
                notif.setFont(new Font("Segoe UI", Font.PLAIN, 24));
            }
            notif.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            notif.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    JOptionPane.showMessageDialog(Dashboard.this, "Tidak ada notifikasi baru.", "Notifikasi", JOptionPane.INFORMATION_MESSAGE);
                }
            });

            add(left, BorderLayout.WEST);
            add(notif, BorderLayout.EAST);
        }
    }

    // ============================================================
    // CONTENT SECTIONS
    // ============================================================
    private void showDashboard() {
        contentPanel.removeAll();

        // Ambil data dari API
        try {
            com.mycompany.perpustakaan.api.DashboardSummary summary = libraryApi.getDashboardSummary(5);
            List<com.mycompany.perpustakaan.api.BookSummary> books = summary.getLatestBooks();

            // Section: TRENDING (mock data - pake latest books)
            contentPanel.add(createSection("TRENDING", books, true));
            contentPanel.add(Box.createVerticalStrut(25));

            // Section: AWARDS (mock - subset)
            contentPanel.add(createSection("AWARDS", books, false));
            contentPanel.add(Box.createVerticalStrut(25));

            // Section: NEW
            contentPanel.add(createSection("NEW", books, true));
            contentPanel.add(Box.createVerticalStrut(25));

            // Section: UPCOMING
            contentPanel.add(createSection("UPCOMING", books, false));

        } catch (SQLException e) {
            showError("Gagal memuat dashboard", e);
            // Fallback: kosong
            contentPanel.add(createSection("TRENDING", java.util.Collections.emptyList(), true));
        }

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private JPanel createSection(String title, List<com.mycompany.perpustakaan.api.BookSummary> books, boolean alternateStatus) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Title with arrow
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        titlePanel.setOpaque(false);
        titlePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(ACCENT);

        JLabel arrow = new JLabel("→");
        arrow.setFont(new Font("Segoe UI", Font.BOLD, 18));
        arrow.setForeground(ACCENT);

        titlePanel.add(titleLabel);
        titlePanel.add(arrow);

        section.add(titlePanel);
        section.add(Box.createVerticalStrut(15));

        // Horizontal scroll cards
        HorizontalScrollPanel scrollPanel = new HorizontalScrollPanel();
        
        // Mock cards - dalam real app, iterate books
        for (int i = 0; i < 8; i++) {
            boolean available = alternateStatus ? (i % 2 == 0) : (i % 2 != 0);
            String bookTitle = i < books.size() ? books.get(i).getJudul() : "TITLE " + (i + 1);
            String author = i < books.size() ? books.get(i).getPenulis() : "AUTHOR";
            scrollPanel.addCard(bookTitle, author, available);
        }

        section.add(scrollPanel);
        return section;
    }

    // ============================================================
    // HORIZONTAL SCROLL CARDS
    // ============================================================
    private class HorizontalScrollPanel extends JPanel {
        private JPanel cardsContainer;

        HorizontalScrollPanel() {
            setLayout(new BorderLayout());
            setOpaque(false);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));

            cardsContainer = new JPanel();
            cardsContainer.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 0));
            cardsContainer.setOpaque(false);

            JScrollPane scroll = new JScrollPane(cardsContainer);
            scroll.setOpaque(false);
            scroll.getViewport().setOpaque(false);
            scroll.setBorder(null);
            scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
            scroll.setPreferredSize(new Dimension(1000, 260));

            // Hide scrollbar tapi tetap bisa scroll
            scroll.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 0));

            // Arrow buttons
            JPanel arrowPanel = new JPanel(new BorderLayout());
            arrowPanel.setOpaque(false);
            arrowPanel.setPreferredSize(new Dimension(40, 260));

            JLabel rightArrow = new JLabel("▶");
            rightArrow.setFont(new Font("Segoe UI", Font.BOLD, 24));
            rightArrow.setForeground(TEXT_DARK);
            rightArrow.setHorizontalAlignment(SwingConstants.CENTER);
            rightArrow.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            rightArrow.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    scroll.getHorizontalScrollBar().setValue(
                        scroll.getHorizontalScrollBar().getValue() + 200
                    );
                }
            });

            arrowPanel.add(rightArrow, BorderLayout.CENTER);

            add(scroll, BorderLayout.CENTER);
            add(arrowPanel, BorderLayout.EAST);
        }

        void addCard(String title, String author, boolean available) {
            cardsContainer.add(new BookCard(title, author, available));
        }
    }

    // ============================================================
    // BOOK CARD
    // ============================================================
    private class BookCard extends JPanel {
        BookCard(String title, String author, boolean available) {
            setPreferredSize(CARD_SIZE);
            setBackground(WHITE);
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(0, 0, 10, 0)
            ));

            // Cover placeholder
            JLabel cover = new JLabel();
            try {
                cover.setIcon(new ImageIcon(getClass().getResource("/book_cover.png")));
            } catch (Exception e) {
                cover.setText("🖼");
                cover.setFont(new Font("Segoe UI", Font.PLAIN, 40));
                cover.setForeground(TEXT_GRAY);
            }
            cover.setHorizontalAlignment(SwingConstants.CENTER);
            cover.setPreferredSize(new Dimension(160, 100));
            cover.setOpaque(true);
            cover.setBackground(new Color(250, 250, 250));

            // Info panel
            JPanel info = new JPanel();
            info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
            info.setOpaque(false);
            info.setBorder(new EmptyBorder(10, 12, 5, 12));

            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            titleLabel.setForeground(TEXT_DARK);
            titleLabel.setMaximumSize(new Dimension(140, 20));

            JLabel authorLabel = new JLabel(author);
            authorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            authorLabel.setForeground(TEXT_GRAY);
            authorLabel.setMaximumSize(new Dimension(140, 16));

            // Status badge
            JLabel statusLabel = new JLabel(available ? "Tersedia" : "Tidak Tersedia");
            statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 9));
            statusLabel.setForeground(WHITE);
            statusLabel.setOpaque(true);
            statusLabel.setBackground(available ? GREEN_STATUS : RED_STATUS);
            statusLabel.setBorder(new EmptyBorder(3, 8, 3, 8));
            statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            statusLabel.setMaximumSize(new Dimension(available ? 70 : 90, 22));

            info.add(titleLabel);
            info.add(authorLabel);
            info.add(Box.createVerticalStrut(6));
            info.add(statusLabel);
            info.add(Box.createVerticalGlue());

            // Link
            JLabel link = new JLabel("Lihat Selengkapnya →");
            link.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            link.setForeground(ACCENT);
            link.setBorder(new EmptyBorder(5, 12, 0, 12));
            link.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

            add(cover, BorderLayout.NORTH);
            add(info, BorderLayout.CENTER);
            add(link, BorderLayout.SOUTH);
        }
    }

    // ============================================================
    // DATA LOADING
    // ============================================================
    private void loadUserData() {
        try {
            com.mycompany.perpustakaan.api.DashboardSummary summary = libraryApi.getDashboardSummary(5);
            com.mycompany.perpustakaan.api.UserSummary profile = summary.getProfile();

            if (profile != null) {
                welcomeLabel.setText("WELCOME, " + profile.getNama().toUpperCase());
                roleLabel.setText(profile.getRole() == null ? "USER" : profile.getRole().toUpperCase());
                setTitle("Dashboard - " + profile.getUsername());
            }
        } catch (SQLException e) {
            logger.log(java.util.logging.Level.SEVERE, "Gagal load user", e);
        }
    }

    // ============================================================
    // MENU ACTIONS (placeholder - sesuaikan dengan logic lo)
    // ============================================================
    private void showBookshelf() {
        contentPanel.removeAll();
        contentPanel.add(new JLabel("Bookshelf Content", SwingConstants.CENTER));
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showLoans() {
        contentPanel.removeAll();
        contentPanel.add(new JLabel("Loan Page Content", SwingConstants.CENTER));
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showProfile() {
        com.mycompany.perpustakaan.api.UserSummary profile = libraryApi.getCurrentUser();
        if (profile == null) {
            JOptionPane.showMessageDialog(this, "Belum ada user yang login.", "Profil", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String message = "Nama: " + profile.getNama()
                + "\nUsername: " + profile.getUsername()
                + "\nEmail: " + profile.getEmail()
                + "\nRole: " + profile.getRole();
        Object[] options = {"Tutup", "Logout"};
        int choice = JOptionPane.showOptionDialog(this, message, "Profil", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
        if (choice == 1) {
            libraryApi.logout();
            new LoginForm(libraryApi).setVisible(true);
            dispose();
        }
    }

    private void showHistory() {
        contentPanel.removeAll();
        contentPanel.add(new JLabel("History Content", SwingConstants.CENTER));
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showError(String message, Exception exception) {
        JOptionPane.showMessageDialog(this, message + ": " + exception.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        logger.log(java.util.logging.Level.SEVERE, message, exception);
    }

    // ============================================================
    // MAIN
    // ============================================================
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> new Dashboard().setVisible(true));
    }
}