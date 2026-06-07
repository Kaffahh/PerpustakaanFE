import java.awt.BorderLayout;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.AbstractCellEditor;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class Dashboard extends JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger
            .getLogger(Dashboard.class.getName());
    private final com.mycompany.perpustakaan.api.LibraryApi libraryApi;

    private static final Color BG_APP = new Color(247, 248, 250);
    private static final Color WHITE = Color.WHITE;
    private static final Color ACCENT = new Color(232, 130, 90);
    private static final Color ACCENT_DARK = new Color(196, 149, 94);
    private static final Color BG_SIDEBAR = new Color(255, 252, 248);
    private static final Color CARD_BORDER = new Color(238, 232, 226);
    private static final Color SHADOW = new Color(0, 0, 0, 18);
    private static final Color ACCENT_LIGHT = new Color(255, 246, 241);
    private static final Color MUTED_ORANGE = new Color(214, 136, 86);
    private static final Color TEXT_DARK = new Color(50, 50, 50);
    private static final Color TEXT_GRAY = new Color(120, 120, 120);
    private static final Color BORDER_COLOR = new Color(224, 224, 224);
    private static final Color SURFACE_ALT = new Color(252, 252, 252);
    private static final Color ACCENT_SOFT = new Color(255, 239, 232);
    private static final Color GREEN_STATUS = new Color(0, 200, 83);
    private static final Color RED_STATUS = new Color(255, 23, 68);

    private static final Dimension FRAME_SIZE = new Dimension(1280, 720);
    private static final Dimension SIDEBAR_SIZE = new Dimension(260, 720);
    private static final Dimension HEADER_SIZE = new Dimension(1060, 92);
    private static final Dimension CARD_SIZE = new Dimension(160, 240);
    private static final Dimension SIDEBAR_LOGO_SIZE = new Dimension(150, 120);

    private SidebarButton activeButton;
    private JPanel contentPanel;
    private JLabel welcomeLabel;
    private JLabel roleLabel;
    private String currentRole = "anggota";

    // === BOOKSHELF FIELDS ===
    private String currentKeyword = "";
    private String currentCategory = "Semua kategori";
    private JTextField searchField;
    private JComboBox<String> categoryFilter;

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
        com.mycompany.perpustakaan.api.UserSummary user = libraryApi.getCurrentUser();
        if (user != null && user.getRole() != null) {
            currentRole = user.getRole().toLowerCase();
        }
        initComponents();
        loadUserData();
        showDashboard();
    }

    private void initComponents() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle("Dashboard Perpustakaan");
        setSize(FRAME_SIZE);
        setMinimumSize(FRAME_SIZE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BG_APP);
        setContentPane(root);

        root.add(new SidebarPanel(), BorderLayout.WEST);

        JPanel mainArea = new JPanel(new BorderLayout(0, 0));
        mainArea.setOpaque(false);
        mainArea.add(new HeaderPanel(), BorderLayout.NORTH);

        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(28, 36, 28, 36));

        JScrollPane contentScroll = new JScrollPane(contentPanel);
        contentScroll.setOpaque(false);
        contentScroll.getViewport().setOpaque(false);
        contentScroll.setBorder(null);
        contentScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        contentScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        mainArea.add(contentScroll, BorderLayout.CENTER);
        root.add(mainArea, BorderLayout.CENTER);
    }

    private class SidebarPanel extends JPanel {
        SidebarPanel() {
            setPreferredSize(SIDEBAR_SIZE);
            setBackground(BG_SIDEBAR);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, CARD_BORDER));

            add(Box.createVerticalStrut(18));
            add(createSidebarLogo());
            add(Box.createVerticalStrut(14));

            JLabel menuTitle = new JLabel(roleTitle());
            menuTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
            menuTitle.setForeground(TEXT_DARK);
            menuTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(menuTitle);

            JLabel menuHint = new JLabel("Library Management");
            menuHint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            menuHint.setForeground(TEXT_GRAY);
            menuHint.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(Box.createVerticalStrut(3));
            add(menuHint);
            add(Box.createVerticalStrut(22));

            addMenuButton("Dashboard", "/assets/icons/icon-dashboard.png", e -> showDashboard());

            if (isAdmin()) {
                addMenuButton("Report", "/assets/icons/icon-admin-report.svg", e -> showAdminDashboard());
                addMenuButton("Inventory", "/assets/icons/icon-book.svg", e -> showInventoryReport());
                addMenuButton("Loan Report", "/assets/icons/icon-loan-report.svg", e -> showLoanReport());
                addMenuButton("Visit Report", "/assets/icons/icon-visit.svg", e -> showVisitReport());
                addMenuButton("Buku Populer", "/assets/icons/icon-history.png", e -> showPopularBookReport());
                addMenuButton("Kategori", "/assets/icons/icon-book.svg", e -> showCategoryManagement());
                addMenuButton("Tambah Buku", "/assets/icons/icon-loan.png", e -> showAddBookDialog());
            }

            if (isStaffOrAdmin()) {
                addMenuButton("Pending Request", "/assets/icons/icon-loan-report.svg", e -> showPendingLoanRequests());
                addMenuButton("Manajemen Buku", "/assets/icons/icon-bookshelf.png", e -> showBookManagement());
                addMenuButton("Loans & Returns", "/assets/icons/icon-loan.png", e -> showLoanManagement());
                addMenuButton("Denda", "/assets/icons/icon-admin-report.svg", e -> showFineManagement());
                addMenuButton("Members", "/assets/icons/icon-member.svg", e -> showMemberManagement());
                addMenuButton("Kunjungan", "/assets/icons/icon-visit.svg", e -> showVisitManagement());
            } else {
                addMenuButton("Bookshelf", "/assets/icons/icon-bookshelf.png", e -> showBookshelf());
                addMenuButton("Pinjam Buku", "/assets/icons/icon-loan.png", e -> showRequestLoan());
                addMenuButton("Pinjaman Aktif", "/assets/icons/icon-loan-report.svg", e -> showCurrentLoans());
                addMenuButton("History", "/assets/icons/icon-history.png", e -> showUserHistory());
                addMenuButton("Kunjungan", "/assets/icons/icon-visit.svg", e -> showVisitForm());
            }

            addMenuButton("User Profile", "/assets/icons/icon-profile.png", e -> showProfile());

            add(Box.createVerticalGlue());
            add(createLogoutButton());
            add(Box.createVerticalStrut(18));
        }

        private JPanel createSidebarLogo() {
            JPanel wrapper = new RoundedPanel(28, new Color(245, 213, 174), new Color(245, 213, 174), 1f);
            wrapper.setLayout(new BorderLayout());
            wrapper.setPreferredSize(new Dimension(150, 104));
            wrapper.setMaximumSize(new Dimension(150, 104));
            wrapper.setAlignmentX(Component.CENTER_ALIGNMENT);
            wrapper.setBorder(new EmptyBorder(10, 12, 10, 12));

            JLabel logo = new JLabel();
            try {
                ImageIcon source = new ImageIcon(getClass().getResource("/assets/branding/library-logo.png"));
                Image scaled = scaleImageToFit(source, 126, 82);
                logo.setIcon(new ImageIcon(scaled));
            } catch (Exception e) {
                logo.setText("LIBRARY HUB");
                logo.setFont(new Font("Segoe UI", Font.BOLD, 13));
                logo.setForeground(new Color(92, 63, 38));
            }

            logo.setHorizontalAlignment(SwingConstants.CENTER);
            wrapper.add(logo, BorderLayout.CENTER);

            return wrapper;
        }

        private void addMenuButton(String text, String icon, java.awt.event.ActionListener listener) {
            SidebarButton button = new SidebarButton(text, icon);
            button.addActionListener(e -> {
                setActive(button);
                listener.actionPerformed(e);
            });

            add(button);
            add(Box.createVerticalStrut(7));

            if (activeButton == null) {
                setActive(button);
            }
        }
    }

    private class SidebarButton extends JButton {
        SidebarButton(String text, String iconResource) {
            super(text);
            setIcon(loadMenuIcon(iconResource));
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setForeground(TEXT_DARK);
            setBackground(BG_SIDEBAR);
            setHorizontalAlignment(SwingConstants.LEFT);
            setIconTextGap(14);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setMaximumSize(new Dimension(214, 46));
            setPreferredSize(new Dimension(214, 46));
            setAlignmentX(Component.CENTER_ALIGNMENT);
            setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(10, 16, 10, 16));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (SidebarButton.this != activeButton) {
                        setBackground(ACCENT_LIGHT);
                        repaint();
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (SidebarButton.this != activeButton) {
                        setBackground(BG_SIDEBAR);
                        repaint();
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
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);

                g2d.setColor(new Color(255, 255, 255, 70));
                g2d.fillRoundRect(0, 0, 5, getHeight(), 12, 12);

                setForeground(WHITE);
            } else {
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                setForeground(TEXT_DARK);
            }

            g2d.dispose();
            super.paintComponent(g);
        }
    }

    private class HeaderPanel extends JPanel {
        HeaderPanel() {
            setPreferredSize(HEADER_SIZE);
            setBackground(WHITE);
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, CARD_BORDER),
                    new EmptyBorder(14, 30, 14, 30)));

            JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
            left.setOpaque(false);

            JLabel avatar = new JLabel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    GradientPaint gp = new GradientPaint(0, 0, ACCENT, getWidth(), getHeight(), ACCENT_DARK);
                    g2d.setPaint(gp);
                    g2d.fillOval(0, 0, getWidth() - 1, getHeight() - 1);

                    g2d.dispose();
                    super.paintComponent(g);
                }
            };

            try {
                avatar.setIcon(new ImageIcon(getClass().getResource("/assets/images/profile-avatar.png")));
            } catch (Exception e) {
                avatar.setText("U");
                avatar.setForeground(WHITE);
                avatar.setFont(new Font("Segoe UI", Font.BOLD, 18));
            }

            avatar.setPreferredSize(new Dimension(48, 48));
            avatar.setHorizontalAlignment(SwingConstants.CENTER);
            avatar.setVerticalAlignment(SwingConstants.CENTER);

            JPanel textPanel = new JPanel();
            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
            textPanel.setOpaque(false);

            welcomeLabel = new JLabel("WELCOME, USER");
            welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
            welcomeLabel.setForeground(TEXT_DARK);

            roleLabel = new JLabel(currentRole.toUpperCase());
            roleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            roleLabel.setForeground(ACCENT);

            textPanel.add(Box.createVerticalStrut(5));
            textPanel.add(welcomeLabel);
            textPanel.add(Box.createVerticalStrut(3));
            textPanel.add(roleLabel);

            left.add(avatar);
            left.add(textPanel);

            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
            right.setOpaque(false);

            JLabel dateInfo = new JLabel("Library Hub");
            dateInfo.setFont(new Font("Segoe UI", Font.BOLD, 13));
            dateInfo.setForeground(TEXT_GRAY);

            JButton notif = createNotificationButton();

            right.add(dateInfo);
            right.add(notif);

            add(left, BorderLayout.WEST);
            add(right, BorderLayout.EAST);
        }
    }

    private void showDashboard() {
        resetContent();
        if (isAdmin()) {
            addTitle("Dashboard Admin");
            addDashboardHero("Selamat datang kembali, Admin",
                    "Kelola laporan, inventory, peminjaman, dan member dari satu tempat.");
            addAdminSummaryCards();
            addQuickActions(
                    new String[] { "Admin Report", "Inventory", "Loan Report", "Buku Populer", "Tambah Buku",
                            "Manajemen Buku", "Loans & Returns", "Members" },
                    new Runnable[] { this::showAdminDashboard, this::showInventoryReport, this::showLoanReport,
                            this::showPopularBookReport, this::showAddBookDialog, this::showBookManagement,
                            this::showLoanManagement, this::showMemberManagement });
        } else if (isStaffOrAdmin()) {
            addTitle("Dashboard Staff");
            addDashboardHero("Dashboard Staff", "Kelola buku, peminjaman, pengembalian, dan data member dengan cepat.");
            addQuickActions(new String[] { "Tambah Buku", "Manajemen Buku", "Loans & Returns", "Members" },
                    new Runnable[] { this::showAddBookDialog, this::showBookManagement, this::showLoanManagement,
                            this::showMemberManagement });
        } else {
            addTitle("Dashboard Anggota");
            addDashboardHero("Halo, Selamat Membaca",
                    "Cari buku favorit kamu, ajukan peminjaman, dan pantau riwayat pinjaman.");
            addUserSummaryCards();
            contentPanel.add(createSearchBar());
            contentPanel.add(Box.createVerticalStrut(18));
            addQuickActions(new String[] { "Bookshelf", "Request Pinjam", "Pinjaman Aktif", "Tambah Kunjungan" },
                    new Runnable[] {
                            this::showBookshelf,
                            this::showRequestLoan,
                            this::showCurrentLoans,
                            this::showVisitForm
                    });
            addBookSections();
        }
        refreshContent();
    }

    private void showAdminDashboard() {
        if (!requireAdminView()) {
            return;
        }

        resetContent();
        addTitle("Laporan Perpustakaan");

        addDashboardHero(
                "Ringkasan Aktivitas Perpustakaan",
                "Pantau stok buku, peminjaman, denda, dan laporan bulanan dari satu halaman.");

        addReportSummaryCards();

        JPanel chartRow = new JPanel(new GridLayout(1, 2, 18, 0));
        chartRow.setOpaque(false);
        chartRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        chartRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 360));

        chartRow.add(createMonthlyLoanChartCard());
        chartRow.add(createReportStorageCard());

        contentPanel.add(chartRow);
        contentPanel.add(Box.createVerticalStrut(22));

        addQuickActions(
                new String[] { "Export Inventory PDF", "Export Inventory XLSX", "Export Loan PDF", "Export Loan XLSX" },
                new Runnable[] {
                        () -> exportInventory("pdf"),
                        () -> exportInventory("xlsx"),
                        () -> exportLoan("pdf"),
                        () -> exportLoan("xlsx")
                });

        refreshContent();
    }

    private void addReportSummaryCards() {
        try {
            com.mycompany.perpustakaan.api.AdminDashboardSummary summary = libraryApi.getAdminDashboardSummary();

            JPanel row = metricRow();
            row.add(createMetricCard("Total Buku", String.valueOf(summary.getTotalBuku())));
            row.add(createMetricCard("Total Anggota", String.valueOf(summary.getTotalAnggota())));
            row.add(createMetricCard("Pinjaman Aktif", String.valueOf(summary.getTotalPeminjamanAktif())));
            row.add(createMetricCard("Total Denda", formatMoney(summary.getTotalDenda())));

            contentPanel.add(row);
            contentPanel.add(Box.createVerticalStrut(22));
        } catch (SQLException e) {
            showError("Gagal memuat ringkasan laporan", e);
        }
    }

    private JPanel createMonthlyLoanChartCard() {
        JPanel card = new RoundedPanel(26, WHITE, CARD_BORDER, 1f);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(22, 24, 22, 24));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Jumlah Peminjaman Bulanan");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(TEXT_DARK);

        JLabel subtitle = new JLabel("Statistik peminjaman buku dalam beberapa bulan terakhir.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(TEXT_GRAY);

        titleBox.add(title);
        titleBox.add(Box.createVerticalStrut(4));
        titleBox.add(subtitle);

        JLabel badge = new JLabel("REPORT");
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setForeground(ACCENT_DARK);
        badge.setOpaque(true);
        badge.setBackground(ACCENT_SOFT);
        badge.setBorder(new EmptyBorder(7, 12, 7, 12));

        header.add(titleBox, BorderLayout.WEST);
        header.add(badge, BorderLayout.EAST);

        card.add(header, BorderLayout.NORTH);
        card.add(new MonthlyLoanChartPanel(), BorderLayout.CENTER);

        return card;
    }

    private class MonthlyLoanChartPanel extends JPanel {
        private final String[] months = { "Jan", "Feb", "Mar", "Apr", "Mei" };
        private final int[] values = { 30, 40, 45, 30, 40 };

        MonthlyLoanChartPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(520, 260));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int left = 48;
            int right = 28;
            int top = 34;
            int bottom = 46;

            int chartW = getWidth() - left - right;
            int chartH = getHeight() - top - bottom;

            int max = 50;

            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g2d.setColor(new Color(230, 230, 230));

            for (int i = 0; i <= 5; i++) {
                int y = top + chartH - (i * chartH / 5);
                g2d.drawLine(left, y, left + chartW, y);

                g2d.setColor(TEXT_GRAY);
                String label = String.valueOf(i * 10);
                g2d.drawString(label, left - 30, y + 4);
                g2d.setColor(new Color(230, 230, 230));
            }

            int barGap = 28;
            int barW = (chartW - (barGap * (values.length + 1))) / values.length;

            for (int i = 0; i < values.length; i++) {
                int barH = values[i] * chartH / max;
                int x = left + barGap + i * (barW + barGap);
                int y = top + chartH - barH;

                GradientPaint gp = new GradientPaint(
                        x, y, ACCENT,
                        x, y + barH, ACCENT_DARK);

                g2d.setPaint(gp);
                g2d.fillRoundRect(x, y, barW, barH, 14, 14);

                g2d.setColor(new Color(0, 0, 0, 25));
                g2d.drawRoundRect(x, y, barW, barH, 14, 14);

                g2d.setColor(TEXT_DARK);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 11));
                String value = String.valueOf(values[i]);
                int valueX = x + (barW - g2d.getFontMetrics().stringWidth(value)) / 2;
                g2d.drawString(value, valueX, y - 8);

                g2d.setColor(TEXT_GRAY);
                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                String month = months[i];
                int monthX = x + (barW - g2d.getFontMetrics().stringWidth(month)) / 2;
                g2d.drawString(month, monthX, top + chartH + 24);
            }

            g2d.dispose();
        }
    }

    private JPanel createReportStorageCard() {
        JPanel card = new RoundedPanel(26, WHITE, CARD_BORDER, 1f);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(22, 24, 22, 24));

        JLabel title = new JLabel("Penyimpanan & Aktivitas");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(TEXT_DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Ringkasan kondisi data perpustakaan saat ini.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(TEXT_GRAY);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(20));

        try {
            com.mycompany.perpustakaan.api.AdminDashboardSummary summary = libraryApi.getAdminDashboardSummary();

            card.add(createReportInfoRow("Stok Buku", String.valueOf(summary.getTotalBuku()) + " Buku"));
            card.add(Box.createVerticalStrut(12));
            card.add(createReportInfoRow("Total Anggota", String.valueOf(summary.getTotalAnggota()) + " Anggota"));
            card.add(Box.createVerticalStrut(12));
            card.add(
                    createReportInfoRow("Pinjaman Aktif", String.valueOf(summary.getTotalPeminjamanAktif()) + " Buku"));
            card.add(Box.createVerticalStrut(12));
            card.add(createReportInfoRow("Total Denda", formatMoney(summary.getTotalDenda())));
        } catch (SQLException e) {
            card.add(createReportInfoRow("Status", "Gagal memuat data"));
        }

        card.add(Box.createVerticalGlue());

        return card;
    }

    private JPanel createReportInfoRow(String labelText, String valueText) {
        JPanel row = new RoundedPanel(18, SURFACE_ALT, CARD_BORDER, 1f);
        row.setLayout(new BorderLayout());
        row.setBorder(new EmptyBorder(13, 16, 13, 16));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(TEXT_DARK);

        JLabel value = new JLabel(valueText);
        value.setFont(new Font("Segoe UI", Font.BOLD, 13));
        value.setForeground(ACCENT_DARK);

        row.add(label, BorderLayout.WEST);
        row.add(value, BorderLayout.EAST);

        return row;
    }

    private void addAdminSummaryCards() {
        try {
            com.mycompany.perpustakaan.api.AdminDashboardSummary summary = libraryApi.getAdminDashboardSummary();
            JPanel row = metricRow();
            row.add(createMetricCard("Total Buku", String.valueOf(summary.getTotalBuku())));
            row.add(createMetricCard("Total Anggota", String.valueOf(summary.getTotalAnggota())));
            row.add(createMetricCard("Pinjaman Aktif", String.valueOf(summary.getTotalPeminjamanAktif())));
            row.add(createMetricCard("Total Denda", formatMoney(summary.getTotalDenda())));
            contentPanel.add(row);
            contentPanel.add(Box.createVerticalStrut(18));
        } catch (SQLException e) {
            showError("Gagal memuat dashboard admin", e);
        }
    }

    private void addUserSummaryCards() {
        try {
            com.mycompany.perpustakaan.api.DashboardSummary summary = libraryApi.getDashboardSummary(5);
            JPanel row = metricRow();
            row.add(createMetricCard("Total Buku", String.valueOf(summary.getTotalBooks())));
            row.add(createMetricCard("Buku Terbaru", String.valueOf(summary.getLatestBooks().size())));
            row.add(createMetricCard("Buku Populer", String.valueOf(summary.getPopularBooks().size())));
            row.add(createMetricCard("Role", displayRole()));
            contentPanel.add(row);
            contentPanel.add(Box.createVerticalStrut(18));
        } catch (SQLException e) {
            showError("Gagal memuat dashboard anggota", e);
        }
    }

    private void addDashboardHero(String title, String subtitle) {
        JPanel hero = new RoundedPanel(26, ACCENT_LIGHT, new Color(255, 226, 214), 1f);
        hero.setLayout(new BorderLayout(18, 0));
        hero.setBorder(new EmptyBorder(22, 26, 22, 26));
        hero.setAlignmentX(Component.LEFT_ALIGNMENT);
        hero.setMaximumSize(new Dimension(Integer.MAX_VALUE, 118));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(TEXT_DARK);

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(TEXT_GRAY);

        text.add(titleLabel);
        text.add(Box.createVerticalStrut(7));
        text.add(subtitleLabel);

        JLabel badge = new JLabel(displayRole());
        badge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        badge.setForeground(WHITE);
        badge.setOpaque(true);
        badge.setBackground(ACCENT);
        badge.setBorder(new EmptyBorder(8, 14, 8, 14));

        hero.add(text, BorderLayout.CENTER);
        hero.add(badge, BorderLayout.EAST);

        contentPanel.add(hero);
        contentPanel.add(Box.createVerticalStrut(20));
    }

    private void showInventoryReport() {
        if (!requireAdminView()) {
            return;
        }

        resetContent();
        addTitle("Laporan Inventory");

        JTextField search = createModernSearchField("Cari kode / judul / penulis / kategori...");
        JButton load = createActionButton("Tampilkan");
        JButton pdf = createActionButton("Export PDF");
        JButton xlsx = createActionButton("Export XLSX");
        JButton addBook = createActionButton("Tambah Buku");

        JPanel toolbar = createToolbarPanel();
        toolbar.add(search);
        toolbar.add(load);
        toolbar.add(pdf);
        toolbar.add(xlsx);
        toolbar.add(addBook);

        contentPanel.add(toolbar);
        contentPanel.add(Box.createVerticalStrut(15));

        JPanel tableHolder = createDynamicContentPanel();
        contentPanel.add(tableHolder);

        final int[] currentPage = { 1 };
        final int pageSize = 25;

        final Runnable[] render = new Runnable[1];

        render[0] = () -> {
            tableHolder.removeAll();

            try {
                String keyword = searchText(search, "Cari kode / judul / penulis / kategori...");

                Object page = invokeApi(
                        "getInventoryReport",
                        new Class<?>[] { String.class, int.class, int.class },
                        keyword,
                        currentPage[0],
                        pageSize);

                List<com.mycompany.perpustakaan.api.InventoryReportRow> rows = extractList(
                        page,
                        "getRows",
                        "getItems",
                        "getInventory",
                        "getData");

                if (rows == null) {
                    rows = libraryApi.getInventoryReport();
                    rows = filterInventoryRows(rows, keyword);
                    rows = slice(rows, currentPage[0], pageSize);
                }

                DefaultTableModel model = new DefaultTableModel(
                        new Object[] { "ID", "Kode", "Judul", "Penulis", "Kategori", "Stok", "Status" },
                        0);

                if (rows == null || rows.isEmpty()) {
                    model.addRow(new Object[] { "-", "Tidak ada data", "-", "-", "-", "-", "-" });
                } else {
                    for (com.mycompany.perpustakaan.api.InventoryReportRow row : rows) {
                        model.addRow(new Object[] {
                                row.getIdBuku(),
                                row.getKodeBuku(),
                                row.getJudul(),
                                row.getPenulis(),
                                row.getKategori(),
                                row.getStokTersedia() + "/" + row.getStokTotal(),
                                row.getStatusKetersediaan()
                        });
                    }
                }

                JPanel panel = createDynamicContentPanel();
                panel.add(createTablePanel(model, 520));
                panel.add(createPaginationFooter(page, currentPage, pageSize, render[0]));

                tableHolder.add(panel);
                refreshContent();

            } catch (SQLException e) {
                showError("Gagal memuat laporan inventory", e);
            }
        };

        load.addActionListener(e -> {
            currentPage[0] = 1;
            render[0].run();
        });

        search.addActionListener(e -> {
            currentPage[0] = 1;
            render[0].run();
        });

        pdf.addActionListener(e -> exportInventory("pdf"));
        xlsx.addActionListener(e -> exportInventory("xlsx"));
        addBook.addActionListener(e -> showAddBookDialog());

        render[0].run();
        refreshContent();
    }

    private void showLoanReport() {
        if (!requireAdminView()) {
            return;
        }
        resetContent();
        addTitle("Laporan Peminjaman Berdasarkan Tanggal");

        JPanel filter = createToolbarPanel();
        JTextField start = createField(LocalDate.now().minusMonths(1).toString());
        JTextField end = createField(LocalDate.now().toString());
        JButton load = createActionButton("Tampilkan");
        JButton pdf = createActionButton("Export PDF");
        JButton xlsx = createActionButton("Export XLSX");
        filter.add(new JLabel("Dari"));
        filter.add(start);
        filter.add(new JLabel("Sampai"));
        filter.add(end);
        filter.add(load);
        filter.add(pdf);
        filter.add(xlsx);
        contentPanel.add(filter);
        contentPanel.add(Box.createVerticalStrut(15));

        JPanel tableHolder = createDynamicContentPanel();
        tableHolder.setOpaque(false);
        contentPanel.add(tableHolder);

        Runnable render = () -> {
            tableHolder.removeAll();
            try {
                LocalDate startDate = parseDate(start.getText());
                LocalDate endDate = parseDate(end.getText());
                DefaultTableModel model = new DefaultTableModel(
                        new Object[] { "ID", "User", "Buku", "Pinjam", "Jatuh Tempo", "Kembali", "Status", "Denda" },
                        0);
                for (com.mycompany.perpustakaan.api.LoanReportRow row : libraryApi.getLoanReport(startDate, endDate)) {
                    model.addRow(new Object[] {
                            row.getIdPeminjaman(),
                            row.getNamaUser(),
                            row.getJudulBuku(),
                            row.getTanggalPinjam(),
                            row.getTanggalJatuhTempo(),
                            row.getTanggalKembali(),
                            row.getStatus(),
                            formatMoney(row.getDenda())
                    });
                }
                tableHolder.add(createTablePanel(model, 540));
                refreshContent();
            } catch (SQLException | DateTimeParseException e) {
                showError("Gagal memuat laporan peminjaman", e);
            }
        };
        load.addActionListener(e -> render.run());
        pdf.addActionListener(e -> exportLoan("pdf", start.getText(), end.getText()));
        xlsx.addActionListener(e -> exportLoan("xlsx", start.getText(), end.getText()));
        render.run();
    }

    private void showPopularBookReport() {
        if (!requireAdminView()) {
            return;
        }

        resetContent();
        addTitle("Laporan Buku Populer");

        JPanel tableHolder = createDynamicContentPanel();
        contentPanel.add(tableHolder);

        final int[] currentPage = { 1 };
        final int pageSize = 25;

        final Runnable[] render = new Runnable[1];

        render[0] = () -> {
            tableHolder.removeAll();

            try {
                Object page = invokeApi(
                        "getPopularBookReport",
                        new Class<?>[] { int.class, int.class },
                        currentPage[0],
                        pageSize);

                List<com.mycompany.perpustakaan.api.PopularBookReportRow> rows = extractList(
                        page,
                        "getRows",
                        "getItems",
                        "getBooks",
                        "getData");

                if (rows == null) {
                    rows = libraryApi.getPopularBookReport(pageSize);
                }

                DefaultTableModel model = new DefaultTableModel(
                        new Object[] { "ID", "Kode", "Judul", "Penulis", "Kategori", "Dipinjam", "Stok" },
                        0);

                if (rows == null || rows.isEmpty()) {
                    model.addRow(new Object[] { "-", "Tidak ada data", "-", "-", "-", "-", "-" });
                } else {
                    for (com.mycompany.perpustakaan.api.PopularBookReportRow row : rows) {
                        model.addRow(new Object[] {
                                row.getIdBuku(),
                                row.getKodeBuku(),
                                row.getJudul(),
                                row.getPenulis(),
                                row.getKategori(),
                                row.getTotalDipinjam(),
                                row.getStokTersedia() + "/" + row.getStokTotal()
                        });
                    }
                }

                JPanel panel = createDynamicContentPanel();
                panel.add(createTablePanel(model, 540));
                panel.add(createPaginationFooter(page, currentPage, pageSize, render[0]));

                tableHolder.add(panel);
                refreshContent();

            } catch (SQLException e) {
                showError("Gagal memuat laporan buku populer", e);
            }
        };

        render[0].run();
        refreshContent();
    }

    private void showVisitReport() {
        if (!requireAdminView()) {
            return;
        }

        resetContent();
        addTitle("Laporan Kunjungan");

        JTextField search = createModernSearchField("Cari pengunjung / asal / keperluan...");
        JComboBox<String> status = new JComboBox<>(new String[] { "semua", "datang", "selesai", "batal" });
        status.setPreferredSize(new Dimension(150, 38));
        status.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JButton load = createActionButton("Tampilkan");
        JButton pdf = createActionButton("Export PDF");
        JButton xlsx = createActionButton("Export XLSX");

        JPanel toolbar = createToolbarPanel();
        toolbar.add(search);
        toolbar.add(new JLabel("Status"));
        toolbar.add(status);
        toolbar.add(load);
        toolbar.add(pdf);
        toolbar.add(xlsx);

        contentPanel.add(toolbar);
        contentPanel.add(Box.createVerticalStrut(15));

        JPanel tableHolder = createDynamicContentPanel();
        contentPanel.add(tableHolder);

        final Runnable[] render = new Runnable[1];

        render[0] = () -> {
            tableHolder.removeAll();

            try {
                String keyword = searchText(search, "Cari pengunjung / asal / keperluan...");
                String selectedStatus = status.getSelectedItem() == null
                        ? "semua"
                        : status.getSelectedItem().toString();

                DefaultTableModel model = new DefaultTableModel(
                        new Object[] { "ID", "Nama", "Jenis", "Asal", "Keperluan", "Status", "Tanggal" },
                        0);

                List<com.mycompany.perpustakaan.api.VisitReportRow> rows = libraryApi.getVisitReport(keyword,
                        selectedStatus);

                if (rows.isEmpty()) {
                    model.addRow(new Object[] { "-", "Belum ada data", "-", "-", "-", "-", "-" });
                } else {
                    for (com.mycompany.perpustakaan.api.VisitReportRow row : rows) {
                        model.addRow(new Object[] {
                                row.getIdKunjungan(),
                                safeOrDash(row.getNamaPengunjung()),
                                safeOrDash(row.getJenisPengunjung()),
                                safeOrDash(row.getAsalInstansi()),
                                safeOrDash(row.getKeperluan()),
                                safeOrDash(row.getStatusKunjungan()),
                                safeOrDash(row.getTanggalKunjungan())
                        });
                    }
                }

                tableHolder.add(createTablePanel(model, 520));
                refreshContent();

            } catch (SQLException e) {
                showError("Gagal memuat laporan kunjungan", e);
            }
        };

        load.addActionListener(e -> render[0].run());
        search.addActionListener(e -> render[0].run());
        status.addActionListener(e -> render[0].run());

        pdf.addActionListener(e -> exportVisit(
                "pdf",
                searchText(search, "Cari pengunjung / asal / keperluan..."),
                status.getSelectedItem() == null ? "semua" : status.getSelectedItem().toString()));

        xlsx.addActionListener(e -> exportVisit(
                "xlsx",
                searchText(search, "Cari pengunjung / asal / keperluan..."),
                status.getSelectedItem() == null ? "semua" : status.getSelectedItem().toString()));

        render[0].run();
        refreshContent();
    }

    private void showBookManagement() {
        if (!requireStaffOrAdminView()) {
            return;
        }

        resetContent();
        addTitle("Manajemen Buku");

        addDashboardHero(
                "Kelola Koleksi Buku",
                "Tambah, edit, pantau stok, dan kelola status buku perpustakaan dari satu halaman.");

        JTextField search = createModernSearchField("Cari judul / penulis / kode buku...");
        JComboBox<String> statusFilter = new JComboBox<>(new String[] { "Semua Status", "Tersedia", "Habis" });
        statusFilter.setPreferredSize(new Dimension(150, 38));
        statusFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JPanel summaryHolder = createDynamicContentPanel();
        JPanel tableHolder = createDynamicContentPanel();
        JPanel actionHolder = createDynamicContentPanel();

        final Runnable[] render = new Runnable[1];
        render[0] = () -> {
            summaryHolder.removeAll();
            tableHolder.removeAll();
            actionHolder.removeAll();
            try {
                String keyword = searchText(search, "Cari judul / penulis / kode buku...");
                com.mycompany.perpustakaan.api.BookshelfPage page = libraryApi.getBookshelfPage(keyword, "", 1, 100);
                List<com.mycompany.perpustakaan.api.BookSummary> books = filterBooksByStatus(
                        page.getBooks(),
                        statusFilter.getSelectedItem() == null ? "Semua Status"
                                : statusFilter.getSelectedItem().toString());

                summaryHolder.add(createBookManagementSummaryPanel(books));
                summaryHolder.add(Box.createVerticalStrut(20));

                JTable table = createTable(createBookManagementTableModel(books));
                tableHolder.add(createBookManagementTableCard(table));
                tableHolder.add(Box.createVerticalStrut(14));
                actionHolder.add(createBookManagementActions(table));
                refreshContent();
            } catch (SQLException e) {
                showError("Gagal memuat manajemen buku", e);
            }
        };

        JPanel toolbar = createBookManagementToolbar(search, statusFilter, render[0]);
        contentPanel.add(summaryHolder);
        contentPanel.add(toolbar);
        contentPanel.add(Box.createVerticalStrut(16));
        contentPanel.add(tableHolder);
        contentPanel.add(actionHolder);

        search.addActionListener(e -> render[0].run());
        statusFilter.addActionListener(e -> render[0].run());
        render[0].run();

        refreshContent();
    }

    private void addBookManagementSummary(List<com.mycompany.perpustakaan.api.BookSummary> books) {
        contentPanel.add(createBookManagementSummaryPanel(books));
        contentPanel.add(Box.createVerticalStrut(20));
    }

    private JPanel createBookManagementSummaryPanel(List<com.mycompany.perpustakaan.api.BookSummary> books) {
        int total = books == null ? 0 : books.size();
        int tersedia = 0;
        int habis = 0;
        int stokTotal = 0;

        if (books != null) {
            for (com.mycompany.perpustakaan.api.BookSummary book : books) {
                stokTotal += book.getStokTotal();

                if (book.getStokTersedia() > 0) {
                    tersedia++;
                } else {
                    habis++;
                }
            }
        }

        JPanel row = new JPanel(new GridLayout(1, 4, 18, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 112));

        row.add(createMetricCard("Total Buku", String.valueOf(total)));
        row.add(createMetricCard("Buku Tersedia", String.valueOf(tersedia)));
        row.add(createMetricCard("Stok Habis", String.valueOf(habis)));
        row.add(createMetricCard("Total Stok", String.valueOf(stokTotal)));

        return row;
    }

    private JPanel createBookManagementToolbar(JTextField search, JComboBox<String> statusFilter, Runnable render) {
        JPanel toolbar = new RoundedPanel(24, WHITE, CARD_BORDER, 1f);
        toolbar.setLayout(new BorderLayout(16, 0));
        toolbar.setBorder(new EmptyBorder(16, 18, 16, 18));
        toolbar.setAlignmentX(Component.LEFT_ALIGNMENT);
        toolbar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 76));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        JButton searchButton = createActionButton("Cari");
        searchButton.addActionListener(e -> render.run());

        left.add(search);
        left.add(statusFilter);
        left.add(searchButton);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        JButton add = createActionButton("Tambah Buku");
        JButton refresh = createNeutralButton("Refresh");

        add.addActionListener(e -> showAddBookDialog());
        refresh.addActionListener(e -> {
            search.setText("Cari judul / penulis / kode buku...");
            search.setForeground(TEXT_GRAY);
            statusFilter.setSelectedItem("Semua Status");
            render.run();
        });

        right.add(refresh);
        right.add(add);

        toolbar.add(left, BorderLayout.WEST);
        toolbar.add(right, BorderLayout.EAST);

        return toolbar;
    }

    private List<com.mycompany.perpustakaan.api.BookSummary> filterBooksByStatus(
            List<com.mycompany.perpustakaan.api.BookSummary> books,
            String status) {
        if (books == null || books.isEmpty() || status == null || "Semua Status".equalsIgnoreCase(status)) {
            return books;
        }

        List<com.mycompany.perpustakaan.api.BookSummary> filtered = new ArrayList<>();
        for (com.mycompany.perpustakaan.api.BookSummary book : books) {
            boolean available = book.getStokTersedia() > 0;
            if (("Tersedia".equalsIgnoreCase(status) && available)
                    || ("Habis".equalsIgnoreCase(status) && !available)) {
                filtered.add(book);
            }
        }
        return filtered;
    }

    private JTextField createModernSearchField(String placeholder) {
        JTextField field = new JTextField(placeholder);
        field.setPreferredSize(new Dimension(310, 38));
        field.setMaximumSize(new Dimension(310, 38));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setForeground(TEXT_GRAY);
        field.setBackground(WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER),
                new EmptyBorder(8, 13, 8, 13)));

        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(TEXT_DARK);
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (field.getText().trim().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(TEXT_GRAY);
                }
            }
        });

        return field;
    }

    private String searchText(JTextField field, String placeholder) {
        if (field == null) {
            return "";
        }
        String value = field.getText() == null ? "" : field.getText().trim();
        return value.equals(placeholder) ? "" : value;
    }

    private DefaultTableModel createBookManagementTableModel(List<com.mycompany.perpustakaan.api.BookSummary> books) {
        DefaultTableModel model = new DefaultTableModel(
                new Object[] { "ID", "Kode", "Judul", "Penulis", "Kategori", "Stok", "Status" },
                0);

        if (books == null || books.isEmpty()) {
            model.addRow(new Object[] { "-", "-", "Belum ada data buku", "-", "-", "-", "-" });
            return model;
        }

        for (com.mycompany.perpustakaan.api.BookSummary book : books) {
            model.addRow(new Object[] {
                    book.getIdBuku(),
                    safeOrDash(book.getKodeBuku()),
                    safeOrDash(book.getJudul()),
                    safeOrDash(book.getPenulis()),
                    safeOrDash(book.getKategori()),
                    book.getStokTersedia() + " / " + book.getStokTotal(),
                    safeOrDash(book.getStatusKetersediaan())
            });
        }

        return model;
    }

    private JPanel createBookManagementTableCard(JTable table) {
        JPanel card = new RoundedPanel(26, WHITE, CARD_BORDER, 1f);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(20, 22, 22, 22));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 560));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Daftar Koleksi Buku");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(TEXT_DARK);

        JLabel subtitle = new JLabel(
                "Pilih salah satu buku untuk melihat detail, mengedit data, mengubah stok, atau menghapus buku.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(TEXT_GRAY);

        titleBox.add(title);
        titleBox.add(Box.createVerticalStrut(4));
        titleBox.add(subtitle);

        JLabel badge = new JLabel("BOOK DATA");
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setForeground(ACCENT_DARK);
        badge.setOpaque(true);
        badge.setBackground(ACCENT_SOFT);
        badge.setBorder(new EmptyBorder(7, 12, 7, 12));

        header.add(titleBox, BorderLayout.WEST);
        header.add(badge, BorderLayout.EAST);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(1300, 420));
        scroll.setBorder(null);
        scroll.getViewport().setBackground(WHITE);

        card.add(header, BorderLayout.NORTH);
        card.add(Box.createVerticalStrut(14), BorderLayout.CENTER);

        JPanel tableWrap = new JPanel(new BorderLayout());
        tableWrap.setOpaque(false);
        tableWrap.setBorder(new EmptyBorder(18, 0, 0, 0));
        tableWrap.add(scroll, BorderLayout.CENTER);

        card.add(tableWrap, BorderLayout.CENTER);

        return card;
    }

    private JPanel createBookManagementActions(JTable table) {
        JPanel actions = new RoundedPanel(22, WHITE, CARD_BORDER, 1f);
        actions.setLayout(new BorderLayout());
        actions.setBorder(new EmptyBorder(14, 18, 14, 18));
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);
        actions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));

        JLabel hint = new JLabel("Pilih data buku pada tabel terlebih dahulu sebelum menjalankan aksi.");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hint.setForeground(TEXT_GRAY);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);

        JButton detail = createNeutralButton("Detail");
        JButton update = createActionButton("Edit");
        JButton stock = createActionButton("Update Stok");
        JButton delete = createDangerButton("Hapus");

        detail.addActionListener(e -> showSelectedBookDetail(table));
        update.addActionListener(e -> updateSelectedBook(table));
        stock.addActionListener(e -> updateSelectedStock(table));
        delete.addActionListener(e -> deleteSelectedBook(table));

        buttons.add(detail);
        buttons.add(update);
        buttons.add(stock);
        buttons.add(delete);

        actions.add(hint, BorderLayout.WEST);
        actions.add(buttons, BorderLayout.EAST);

        return actions;
    }

    private JButton createDangerButton(String text) {
        JButton button = new GradientActionButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(WHITE);
        button.setBackground(new Color(220, 72, 72));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setOpaque(false);
        button.setBorder(new EmptyBorder(10, 17, 10, 17));
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        return button;
    }

    private void showCategoryManagement() {
        if (!requireAdminView()) {
            return;
        }

        resetContent();
        addTitle("Manajemen Kategori");

        addDashboardHero(
                "Kelola Kategori Buku",
                "Pantau jumlah buku per kategori, tambah kategori master, ubah nama kategori, atau lepas kategori dari buku.");

        try {
            JTable table = createTable(createCategoryTableModel(libraryApi.getCategorySummaries()));
            contentPanel.add(wrapTable(table, 460));

            JPanel actions = createToolbarPanel();

            JButton create = createActionButton("Tambah Kategori");
            JButton rename = createActionButton("Rename");
            JButton clear = createDangerButton("Lepas Kategori");
            JButton refresh = createNeutralButton("Refresh");

            create.addActionListener(e -> createCategoryMaster());
            rename.addActionListener(e -> renameSelectedCategory(table));
            clear.addActionListener(e -> clearSelectedCategory(table));
            refresh.addActionListener(e -> showCategoryManagement());

            actions.add(create);
            actions.add(rename);
            actions.add(clear);
            actions.add(refresh);

            contentPanel.add(actions);

        } catch (SQLException e) {
            showError("Gagal memuat kategori", e);
        }

        refreshContent();
    }

    private DefaultTableModel createCategoryTableModel(
            List<com.mycompany.perpustakaan.api.CategorySummary> categories) {
        DefaultTableModel model = new DefaultTableModel(
                new Object[] { "Kategori", "Total Buku", "Stok Tersedia", "Stok Total" }, 0);
        if (categories == null || categories.isEmpty()) {
            model.addRow(new Object[] { "Belum ada kategori", "-", "-", "-" });
            return model;
        }
        for (com.mycompany.perpustakaan.api.CategorySummary category : categories) {
            model.addRow(new Object[] {
                    category.getNamaKategori(),
                    category.getTotalBuku(),
                    category.getStokTersedia(),
                    category.getStokTotal()
            });
        }
        return model;
    }

    private void renameSelectedCategory(JTable table) {
        String category = selectedString(table, 0);
        if (category == null || "Belum ada kategori".equals(category)) {
            return;
        }
        JTextField newName = createField(category);
        int result = JOptionPane.showConfirmDialog(this, newName, "Nama kategori baru",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            com.mycompany.perpustakaan.api.BookResponse response = libraryApi.renameCategory(category,
                    newName.getText());
            showResponse(response.isSuccess(), response.getMessage());
            if (response.isSuccess()) {
                showCategoryManagement();
            }
        } catch (SQLException e) {
            showError("Gagal rename kategori", e);
        }
    }

    private void clearSelectedCategory(JTable table) {
        String category = selectedString(table, 0);
        if (category == null || "Belum ada kategori".equals(category)) {
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Lepas kategori " + category + " dari semua buku?",
                "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            com.mycompany.perpustakaan.api.BookResponse response = libraryApi.clearCategory(category);
            showResponse(response.isSuccess(), response.getMessage());
            if (response.isSuccess()) {
                showCategoryManagement();
            }
        } catch (SQLException e) {
            showError("Gagal melepas kategori", e);
        }
    }

    private void showLoanManagement() {
        if (!requireStaffOrAdminView()) {
            return;
        }
        resetContent();
        addTitle("Loans & Returns");
        JPanel actions = createToolbarPanel();
        JButton create = createActionButton("Buat Peminjaman");
        JTextField search = createModernSearchField("Cari user / buku / kode...");
        JComboBox<String> status = new JComboBox<>(
                new String[] { "semua", "aktif", "dipinjam", "terlambat", "dikembalikan" });
        JButton load = createActionButton("Tampilkan");
        actions.add(create);
        actions.add(search);
        actions.add(new JLabel("Status"));
        actions.add(status);
        actions.add(load);
        contentPanel.add(actions);
        contentPanel.add(Box.createVerticalStrut(15));

        JPanel tableHolder = createDynamicContentPanel();
        contentPanel.add(tableHolder);
        final int[] currentPage = { 1 };
        final int pageSize = 50;

        final Runnable[] render = new Runnable[1];
        render[0] = () -> {
            tableHolder.removeAll();
            try {
                String keyword = searchText(search, "Cari user / buku / kode...");
                com.mycompany.perpustakaan.api.LoanManagementPage page = libraryApi
                        .searchLoansForManagement((String) status.getSelectedItem(), keyword, currentPage[0], pageSize);
                DefaultTableModel model = new DefaultTableModel(
                        new Object[] { "ID", "Buku", "Pinjam", "Jatuh Tempo", "Kembali", "Status", "Denda" }, 0);
                for (com.mycompany.perpustakaan.api.LoanSummary loan : page.getLoans()) {
                    model.addRow(new Object[] {
                            loan.getIdPeminjaman(),
                            loan.getJudulBuku(),
                            loan.getTanggalPinjam(),
                            loan.getTanggalJatuhTempo(),
                            loan.getTanggalKembali(),
                            loan.getStatus(),
                            formatMoney(loan.getDendaBerjalan())
                    });
                }
                JTable table = createTable(model);
                JPanel panel = createDynamicContentPanel();
                panel.setOpaque(false);
                panel.add(wrapTable(table, 500));
                JButton returnButton = createActionButton("Proses Pengembalian Terpilih");
                returnButton.addActionListener(e -> processSelectedReturn(table));
                JPanel footer = createToolbarPanel();
                JButton prev = createNeutralButton("Prev");
                JButton next = createNeutralButton("Next");
                JLabel pageInfo = new JLabel("Halaman " + page.getPage() + " / " + Math.max(1, page.getTotalPages())
                        + " - Total " + page.getTotalItems() + " data");
                pageInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                pageInfo.setForeground(TEXT_GRAY);
                prev.setEnabled(page.hasPreviousPage());
                next.setEnabled(page.hasNextPage());
                prev.addActionListener(e -> {
                    currentPage[0] = Math.max(1, currentPage[0] - 1);
                    render[0].run();
                });
                next.addActionListener(e -> {
                    currentPage[0]++;
                    render[0].run();
                });
                footer.add(returnButton);
                footer.add(Box.createHorizontalStrut(16));
                footer.add(prev);
                footer.add(next);
                footer.add(pageInfo);
                panel.add(footer);
                tableHolder.add(panel);
                refreshContent();
            } catch (SQLException e) {
                showError("Gagal memuat loans & returns", e);
            }
        };
        create.addActionListener(e -> showCreateLoanDialog());
        load.addActionListener(e -> {
            currentPage[0] = 1;
            render[0].run();
        });
        search.addActionListener(e -> {
            currentPage[0] = 1;
            render[0].run();
        });
        status.addActionListener(e -> {
            currentPage[0] = 1;
            render[0].run();
        });
        render[0].run();
    }

    private void showFineManagement() {
        if (!requireStaffOrAdminView()) {
            return;
        }
        resetContent();
        addTitle("Manajemen Denda");

        JTextField search = createModernSearchField("Cari user / buku...");
        JComboBox<String> status = new JComboBox<>(new String[] { "semua", "unpaid", "paid", "waived" });
        status.setPreferredSize(new Dimension(150, 38));
        status.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JButton load = createActionButton("Tampilkan");
        JButton pdf = createActionButton("Export PDF");
        JButton xlsx = createActionButton("Export XLSX");

        JPanel toolbar = createToolbarPanel();
        toolbar.add(search);
        toolbar.add(new JLabel("Status"));
        toolbar.add(status);
        toolbar.add(load);
        toolbar.add(pdf);
        toolbar.add(xlsx);
        contentPanel.add(toolbar);
        contentPanel.add(Box.createVerticalStrut(15));

        JPanel tableHolder = createDynamicContentPanel();
        JPanel actionHolder = createDynamicContentPanel();
        contentPanel.add(tableHolder);
        contentPanel.add(actionHolder);
        final int[] currentPage = { 1 };
        final int pageSize = 50;

        final Runnable[] render = new Runnable[1];
        render[0] = () -> {
            tableHolder.removeAll();
            actionHolder.removeAll();
            try {
                String keyword = searchText(search, "Cari user / buku...");
                String selectedStatus = status.getSelectedItem() == null ? "semua"
                        : status.getSelectedItem().toString();
                int totalItems = libraryApi.countFines(keyword, selectedStatus);
                int totalPages = calculateTotalPages(totalItems, pageSize);
                if (totalPages > 0 && currentPage[0] > totalPages) {
                    currentPage[0] = totalPages;
                }
                List<com.mycompany.perpustakaan.api.FineSummary> fines = libraryApi.getFines(keyword, selectedStatus,
                        currentPage[0], pageSize);
                JTable table = createTable(createFineTableModel(fines));
                tableHolder.add(wrapTable(table, 500));

                JPanel actions = createToolbarPanel();
                JButton paid = createActionButton("Tandai Lunas");
                paid.addActionListener(e -> markSelectedFinePaid(table));
                actions.add(paid);
                if (isAdmin()) {
                    JButton waive = createDangerButton("Waive");
                    waive.addActionListener(e -> waiveSelectedFine(table));
                    actions.add(waive);
                }
                JButton prev = createNeutralButton("Prev");
                JButton next = createNeutralButton("Next");
                JLabel pageInfo = new JLabel("Halaman " + currentPage[0] + " / " + Math.max(1, totalPages)
                        + " - Total " + totalItems + " data");
                pageInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                pageInfo.setForeground(TEXT_GRAY);
                prev.setEnabled(currentPage[0] > 1);
                next.setEnabled(totalPages > 0 && currentPage[0] < totalPages);
                prev.addActionListener(e -> {
                    currentPage[0] = Math.max(1, currentPage[0] - 1);
                    render[0].run();
                });
                next.addActionListener(e -> {
                    currentPage[0]++;
                    render[0].run();
                });
                actions.add(Box.createHorizontalStrut(16));
                actions.add(prev);
                actions.add(next);
                actions.add(pageInfo);
                actionHolder.add(actions);
                refreshContent();
            } catch (SQLException e) {
                showError("Gagal memuat denda", e);
            }
        };

        load.addActionListener(e -> {
            currentPage[0] = 1;
            render[0].run();
        });
        search.addActionListener(e -> {
            currentPage[0] = 1;
            render[0].run();
        });
        status.addActionListener(e -> {
            currentPage[0] = 1;
            render[0].run();
        });
        pdf.addActionListener(e -> exportFine(
                "pdf",
                searchText(search, "Cari user / buku..."),
                status.getSelectedItem() == null ? "semua" : status.getSelectedItem().toString()));
        xlsx.addActionListener(e -> exportFine(
                "xlsx",
                searchText(search, "Cari user / buku..."),
                status.getSelectedItem() == null ? "semua" : status.getSelectedItem().toString()));
        render[0].run();
        refreshContent();
    }

    private DefaultTableModel createFineTableModel(List<com.mycompany.perpustakaan.api.FineSummary> fines) {
        DefaultTableModel model = new DefaultTableModel(
                new Object[] { "ID Peminjaman", "User", "Username", "Buku", "Jatuh Tempo", "Kembali", "Denda",
                        "Status" },
                0);
        if (fines == null || fines.isEmpty()) {
            model.addRow(new Object[] { "-", "Belum ada denda", "-", "-", "-", "-", "-", "-" });
            return model;
        }
        for (com.mycompany.perpustakaan.api.FineSummary fine : fines) {
            model.addRow(new Object[] {
                    fine.getIdPeminjaman(),
                    safeOrDash(fine.getNamaUser()),
                    safeOrDash(fine.getUsername()),
                    safeOrDash(fine.getJudulBuku()),
                    fine.getTanggalJatuhTempo(),
                    fine.getTanggalKembali(),
                    formatMoney(fine.getNominalDenda()),
                    safeOrDash(fine.getStatusPembayaran())
            });
        }
        return model;
    }

    private void markSelectedFinePaid(JTable table) {
        Integer id = selectedId(table);
        if (id == null) {
            return;
        }
        try {
            com.mycompany.perpustakaan.api.LoanResponse response = libraryApi.markFinePaid(id);
            showResponse(response.isSuccess(), response.getMessage());
            if (response.isSuccess()) {
                showFineManagement();
            }
        } catch (SQLException e) {
            showError("Gagal memproses pembayaran denda", e);
        }
    }

    private void waiveSelectedFine(JTable table) {
        Integer id = selectedId(table);
        if (id == null) {
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Waive denda peminjaman ID " + id + "?",
                "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            com.mycompany.perpustakaan.api.LoanResponse response = libraryApi.waiveFine(id);
            showResponse(response.isSuccess(), response.getMessage());
            if (response.isSuccess()) {
                showFineManagement();
            }
        } catch (SQLException e) {
            showError("Gagal waive denda", e);
        }
    }

    private void showMemberManagement() {
        if (!requireStaffOrAdminView()) {
            return;
        }

        resetContent();
        addTitle("Member Management");

        JTextField search = createModernSearchField("Cari nama / username / email...");
        JComboBox<String> status = new JComboBox<>(new String[] { "semua", "aktif", "suspend" });
        status.setPreferredSize(new Dimension(150, 38));
        status.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JPanel toolbar = createMemberManagementToolbar(search, status);
        JPanel tableHolder = createDynamicContentPanel();
        JPanel actionHolder = createDynamicContentPanel();

        final int[] currentPage = { 1 };
        final int pageSize = 25;

        final Runnable[] render = new Runnable[1];

        render[0] = () -> {
            tableHolder.removeAll();
            actionHolder.removeAll();

            try {
                String keyword = searchText(search, "Cari nama / username / email...");
                String selectedStatus = status.getSelectedItem() == null
                        ? "semua"
                        : status.getSelectedItem().toString();

                com.mycompany.perpustakaan.api.MemberPage page = libraryApi.searchMembers(keyword, selectedStatus,
                        currentPage[0], pageSize);

                DefaultTableModel model = createMemberManagementTableModel(page.getMembers());
                JTable table = createTable(model);

                JPanel tablePanel = createDynamicContentPanel();
                tablePanel.add(wrapTable(table, 500));
                tablePanel.add(createPaginationFooter(page, currentPage, pageSize, render[0]));

                tableHolder.add(tablePanel);

                JPanel actions = createToolbarPanel();

                if (isAdmin()) {
                    JButton update = createActionButton("Update");
                    JButton suspend = createActionButton("Suspend");
                    JButton activate = createActionButton("Aktifkan");
                    JButton exportPdf = createActionButton("Export PDF");
                    JButton exportXlsx = createActionButton("Export XLSX");
                    JButton delete = createDangerButton("Hapus");

                    update.addActionListener(e -> updateSelectedMember(table));
                    suspend.addActionListener(e -> changeSelectedMemberStatus(table, true));
                    activate.addActionListener(e -> changeSelectedMemberStatus(table, false));
                    exportPdf.addActionListener(e -> exportMember(
                            "pdf",
                            searchText(search, "Cari nama / username / email..."),
                            status.getSelectedItem() == null ? "semua" : status.getSelectedItem().toString()));
                    exportXlsx.addActionListener(e -> exportMember(
                            "xlsx",
                            searchText(search, "Cari nama / username / email..."),
                            status.getSelectedItem() == null ? "semua" : status.getSelectedItem().toString()));
                    delete.addActionListener(e -> deleteSelectedMember(table));

                    actions.add(update);
                    actions.add(suspend);
                    actions.add(activate);
                    actions.add(exportPdf);
                    actions.add(exportXlsx);
                    actions.add(delete);

                } else {
                    JLabel readOnlyHint = new JLabel(
                            "Mode staff: data member hanya bisa dilihat. Perubahan member khusus admin.");
                    readOnlyHint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    readOnlyHint.setForeground(TEXT_GRAY);
                    actions.add(readOnlyHint);
                }

                actionHolder.add(actions);
                refreshContent();

            } catch (SQLException e) {
                showError("Gagal memuat member management", e);
            }
        };

        JButton searchButton = (JButton) toolbar.getClientProperty("searchButton");
        JButton refreshButton = (JButton) toolbar.getClientProperty("refreshButton");
        JButton addButton = (JButton) toolbar.getClientProperty("addButton");

        searchButton.addActionListener(e -> {
            currentPage[0] = 1;
            render[0].run();
        });

        refreshButton.addActionListener(e -> {
            search.setText("Cari nama / username / email...");
            search.setForeground(TEXT_GRAY);
            status.setSelectedItem("semua");
            currentPage[0] = 1;
            render[0].run();
        });

        if (addButton != null) {
            addButton.addActionListener(e -> showAddMemberDialog());
        }

        search.addActionListener(e -> {
            currentPage[0] = 1;
            render[0].run();
        });

        status.addActionListener(e -> {
            currentPage[0] = 1;
            render[0].run();
        });

        contentPanel.add(toolbar);
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(tableHolder);
        contentPanel.add(actionHolder);

        render[0].run();
        refreshContent();
    }

    private JPanel createMemberManagementToolbar(JTextField search, JComboBox<String> status) {
        JPanel toolbar = createToolbarPanel();
        JButton searchButton = createActionButton("Cari");
        JButton refreshButton = createNeutralButton("Refresh");

        toolbar.add(search);
        toolbar.add(new JLabel("Status"));
        toolbar.add(status);
        toolbar.add(searchButton);
        toolbar.add(refreshButton);
        if (isAdmin()) {
            JButton addButton = createActionButton("Tambah Anggota");
            toolbar.add(addButton);
            toolbar.putClientProperty("addButton", addButton);
        }

        toolbar.putClientProperty("searchButton", searchButton);
        toolbar.putClientProperty("refreshButton", refreshButton);
        return toolbar;
    }

    private DefaultTableModel createMemberManagementTableModel(
            List<com.mycompany.perpustakaan.api.MemberSummary> members) {
        DefaultTableModel model = new DefaultTableModel(
                new Object[] { "ID", "Nama", "Username", "Email", "Status" }, 0);
        if (members == null || members.isEmpty()) {
            model.addRow(new Object[] { "-", "Tidak ada anggota yang cocok", "-", "-", "-" });
            return model;
        }

        for (com.mycompany.perpustakaan.api.MemberSummary member : members) {
            model.addRow(new Object[] {
                    member.getIdUser(),
                    member.getNama(),
                    member.getUsername(),
                    member.getEmail(),
                    member.getStatusAkun()
            });
        }
        return model;
    }

    private void showPendingLoanRequests() {
        if (!requireStaffOrAdminView()) {
            return;
        }

        resetContent();
        addTitle("Pending Loan Requests");

        JPanel toolbar = createToolbarPanel();
        JTextField search = createModernSearchField("Cari peminjam / username / buku...");
        JButton load = createActionButton("Tampilkan");
        JButton reset = createNeutralButton("Reset");
        toolbar.add(search);
        toolbar.add(load);
        toolbar.add(reset);
        contentPanel.add(toolbar);
        contentPanel.add(Box.createVerticalStrut(12));

        JPanel tableHolder = createDynamicContentPanel();
        contentPanel.add(tableHolder);

        final int[] currentPage = { 1 };
        final int pageSize = 25;

        final Runnable[] render = new Runnable[1];

        render[0] = () -> {
            tableHolder.removeAll();

            try {
                String keyword = searchText(search, "Cari peminjam / username / buku...");
                Object page = invokeApi(
                        "getPendingLoanRequests",
                        new Class<?>[] { String.class, int.class, int.class },
                        keyword,
                        currentPage[0],
                        pageSize);

                List<com.mycompany.perpustakaan.api.LoanSummary> pendingLoans = extractList(
                        page,
                        "getLoans",
                        "getItems",
                        "getRows",
                        "getData");

                if (pendingLoans == null) {
                    pendingLoans = libraryApi.getPendingLoanRequests();
                    pendingLoans = filterPendingLoans(pendingLoans, keyword);
                    pendingLoans = slice(pendingLoans, currentPage[0], pageSize);
                }

                DefaultTableModel model = createPendingLoanTableModel(pendingLoans);
                JTable table = createTable(model);

                installPendingLoanActionColumn(table);

                JPanel panel = createDynamicContentPanel();
                panel.add(wrapTable(table, 480));
                panel.add(createPaginationFooter(page, currentPage, pageSize, render[0]));

                tableHolder.add(panel);

                refreshContent();

            } catch (SQLException e) {
                showError("Gagal memuat pending loan requests", e);
            }
        };

        search.addActionListener(e -> {
            currentPage[0] = 1;
            render[0].run();
        });
        load.addActionListener(e -> {
            currentPage[0] = 1;
            render[0].run();
        });
        reset.addActionListener(e -> {
            search.setText("Cari peminjam / username / buku...");
            search.setForeground(TEXT_GRAY);
            currentPage[0] = 1;
            render[0].run();
        });

        render[0].run();
        refreshContent();
    }

    private List<com.mycompany.perpustakaan.api.LoanSummary> filterPendingLoans(
            List<com.mycompany.perpustakaan.api.LoanSummary> loans,
            String keyword) {
        if (loans == null || keyword == null || keyword.isBlank()) {
            return loans;
        }

        String q = keyword.trim().toLowerCase();
        java.util.ArrayList<com.mycompany.perpustakaan.api.LoanSummary> filtered = new java.util.ArrayList<>();
        for (com.mycompany.perpustakaan.api.LoanSummary loan : loans) {
            String haystack = (safeOrDash(loan.getNamaUser()) + " "
                    + safeOrDash(loan.getUsernameUser()) + " "
                    + safeOrDash(loan.getJudulBuku()) + " "
                    + safeOrDash(loan.getStatus())).toLowerCase();
            if (haystack.contains(q)) {
                filtered.add(loan);
            }
        }
        return filtered;
    }

    private DefaultTableModel createPendingLoanTableModel(
            List<com.mycompany.perpustakaan.api.LoanSummary> pendingLoans) {
        DefaultTableModel model = new DefaultTableModel(
                new Object[] { "ID", "Peminjam", "Username", "Buku", "Tanggal Request", "Jatuh Tempo", "Status",
                        "Aksi" },
                0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7 && isValidTableId(getValueAt(row, 0));
            }
        };

        if (pendingLoans == null || pendingLoans.isEmpty()) {
            model.addRow(new Object[] { "-", "Belum ada pending request", "-", "-", "-", "-", "-", "" });
            return model;
        }

        for (com.mycompany.perpustakaan.api.LoanSummary loan : pendingLoans) {
            String namaPeminjam = loan.getNamaUser();
            if (namaPeminjam == null || namaPeminjam.isBlank()) {
                namaPeminjam = "-";
            }
            String username = loan.getUsernameUser();
            if (username == null || username.isBlank()) {
                username = "-";
            }
            model.addRow(new Object[] {
                    loan.getIdPeminjaman(),
                    namaPeminjam,
                    username,
                    loan.getJudulBuku(),
                    loan.getTanggalPinjam(),
                    loan.getTanggalJatuhTempo(),
                    loan.getStatus(),
                    ""
            });
        }
        return model;
    }

    private void installPendingLoanActionColumn(JTable table) {
        int actionColumn = 7;
        table.getColumnModel().getColumn(actionColumn).setPreferredWidth(170);
        table.getColumnModel().getColumn(actionColumn).setMinWidth(150);
        table.getColumnModel().getColumn(actionColumn).setCellRenderer(new PendingLoanActionCell(false));
        table.getColumnModel().getColumn(actionColumn).setCellEditor(new PendingLoanActionCell(true));
    }

    private class PendingLoanActionCell extends AbstractCellEditor implements TableCellRenderer, TableCellEditor {
        private final JPanel panel;
        private final JButton approve;
        private final JButton reject;
        private JTable table;
        private int modelRow = -1;

        PendingLoanActionCell(boolean editable) {
            panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 3));
            panel.setOpaque(true);

            approve = createInlineDecisionButton("Setujui", GREEN_STATUS);
            reject = createInlineDecisionButton("Tolak", RED_STATUS);
            panel.add(approve);
            panel.add(reject);

            if (editable) {
                approve.addActionListener(e -> handlePendingLoanDecision(true));
                reject.addActionListener(e -> handlePendingLoanDecision(false));
            }
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            int currentModelRow = table.convertRowIndexToModel(row);
            boolean valid = isValidTableId(table.getModel().getValueAt(currentModelRow, 0));
            configurePanel(table, isSelected, valid);
            return panel;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
                int column) {
            this.table = table;
            this.modelRow = table.convertRowIndexToModel(row);
            boolean valid = isValidTableId(table.getModel().getValueAt(modelRow, 0));
            configurePanel(table, true, valid);
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }

        private void configurePanel(JTable table, boolean selected, boolean valid) {
            panel.setBackground(selected ? table.getSelectionBackground() : table.getBackground());
            approve.setVisible(valid);
            reject.setVisible(valid);
        }

        private void handlePendingLoanDecision(boolean approveRequest) {
            if (table == null || modelRow < 0) {
                return;
            }
            Object value = table.getModel().getValueAt(modelRow, 0);
            if (!isValidTableId(value)) {
                return;
            }
            int id = Integer.parseInt(String.valueOf(value));
            String borrowerName = safeOrDash(String.valueOf(table.getModel().getValueAt(modelRow, 1)));
            stopCellEditing();
            SwingUtilities.invokeLater(() -> processPendingLoanDecision(id, borrowerName, approveRequest));
        }
    }

    private JButton createInlineDecisionButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 11));
        button.setForeground(WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(72, 28));
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        return button;
    }

    private boolean isValidTableId(Object value) {
        if (value == null) {
            return false;
        }
        try {
            Integer.parseInt(String.valueOf(value));
            return true;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    private void processPendingLoanDecision(int id, String borrowerName, boolean approveRequest) {
        String action = approveRequest ? "Setujui" : "Tolak";
        int confirm = JOptionPane.showConfirmDialog(this, action + " peminjaman dari " + safeOrDash(borrowerName) + "?",
                "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            com.mycompany.perpustakaan.api.LoanResponse response = approveRequest
                    ? libraryApi.approveLoanRequest(id)
                    : libraryApi.rejectLoanRequest(id);
            showResponse(response.isSuccess(), response.getMessage());
            if (response.isSuccess()) {
                showPendingLoanRequests();
            }
        } catch (SQLException e) {
            showError(approveRequest ? "Gagal menyetujui peminjaman" : "Gagal menolak peminjaman", e);
        }
    }

    private void showRequestLoan() {
        if (!requireMemberView()) {
            return;
        }

        resetContent();
        addTitle("Request Pinjam Buku");

        JTextField search = createModernSearchField("Cari judul / penulis / kode buku...");
        JComboBox<String> category = new JComboBox<>(loadCategoryOptions());
        category.setPreferredSize(new Dimension(180, 38));
        category.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JButton cari = createActionButton("Cari");
        JButton refresh = createNeutralButton("Refresh");

        JPanel toolbar = createToolbarPanel();
        toolbar.add(search);
        toolbar.add(category);
        toolbar.add(cari);
        toolbar.add(refresh);

        contentPanel.add(toolbar);
        contentPanel.add(Box.createVerticalStrut(15));

        JPanel tableHolder = createDynamicContentPanel();
        JPanel actionHolder = createDynamicContentPanel();

        contentPanel.add(tableHolder);
        contentPanel.add(actionHolder);

        final int[] currentPage = { 1 };
        final int pageSize = 25;

        final Runnable[] render = new Runnable[1];

        render[0] = () -> {
            tableHolder.removeAll();
            actionHolder.removeAll();

            try {
                String keyword = searchText(search, "Cari judul / penulis / kode buku...");

                Object selectedCategory = category.getSelectedItem();

                String categoryValue = selectedCategory == null || "Semua kategori".equals(selectedCategory.toString())
                        ? ""
                        : selectedCategory.toString();

                com.mycompany.perpustakaan.api.BookshelfPage page = libraryApi.getBookshelfPage(keyword, categoryValue,
                        currentPage[0], pageSize);

                DefaultTableModel model = createRequestLoanTableModel(page.getBooks());
                JTable table = createTable(model);

                JPanel tablePanel = createDynamicContentPanel();
                tablePanel.add(wrapTable(table, 500));
                tablePanel.add(createPaginationFooter(page, currentPage, pageSize, render[0]));

                tableHolder.add(tablePanel);

                JButton request = createActionButton("Request Pinjam Buku Terpilih");
                request.addActionListener(e -> requestSelectedLoan(table));

                JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 10));
                footer.setOpaque(false);
                footer.add(request);

                actionHolder.add(footer);

                refreshContent();

            } catch (SQLException e) {
                showError("Gagal memuat buku", e);
            }
        };

        cari.addActionListener(e -> {
            currentPage[0] = 1;
            render[0].run();
        });

        search.addActionListener(e -> {
            currentPage[0] = 1;
            render[0].run();
        });

        category.addActionListener(e -> {
            currentPage[0] = 1;
            render[0].run();
        });

        refresh.addActionListener(e -> {
            search.setText("Cari judul / penulis / kode buku...");
            search.setForeground(TEXT_GRAY);
            category.setSelectedItem("Semua kategori");
            currentPage[0] = 1;
            render[0].run();
        });

        render[0].run();
        refreshContent();
    }

    private DefaultTableModel createRequestLoanTableModel(List<com.mycompany.perpustakaan.api.BookSummary> books) {
        DefaultTableModel model = new DefaultTableModel(
                new Object[] { "ID", "Kode", "Judul", "Penulis", "Kategori", "Stok" }, 0);
        if (books == null || books.isEmpty()) {
            model.addRow(new Object[] { "-", "-", "Tidak ada buku yang cocok", "-", "-", "-" });
            return model;
        }

        for (com.mycompany.perpustakaan.api.BookSummary book : books) {
            model.addRow(new Object[] {
                    book.getIdBuku(),
                    safeOrDash(book.getKodeBuku()),
                    safeOrDash(book.getJudul()),
                    safeOrDash(book.getPenulis()),
                    safeOrDash(book.getKategori()),
                    book.getStokTersedia()
            });
        }
        return model;
    }

    private void showCurrentLoans() {
        if (!requireMemberView()) {
            return;
        }
        resetContent();
        addTitle("Pinjaman Aktif Saya");
        try {
            DefaultTableModel model = new DefaultTableModel(
                    new Object[] { "ID", "Buku", "Pinjam", "Jatuh Tempo", "Status", "Denda" }, 0);
            for (com.mycompany.perpustakaan.api.LoanSummary loan : libraryApi.getCurrentLoans()) {
                model.addRow(new Object[] { loan.getIdPeminjaman(), loan.getJudulBuku(), loan.getTanggalPinjam(),
                        loan.getTanggalJatuhTempo(), loan.getStatus(), formatMoney(loan.getDendaBerjalan()) });
            }
            contentPanel.add(createTablePanel(model, 520));
        } catch (SQLException e) {
            showError("Gagal memuat pinjaman aktif", e);
        }
        refreshContent();
    }

    private void showUserHistory() {
        if (!requireMemberView()) {
            return;
        }
        resetContent();
        addTitle("History Peminjaman Saya");

        JPanel toolbar = createToolbarPanel();
        JComboBox<String> status = new JComboBox<>(new String[] { "semua", "dipinjam", "dikembalikan", "terlambat" });
        status.setPreferredSize(new Dimension(170, 38));
        status.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JButton load = createActionButton("Tampilkan");
        toolbar.add(new JLabel("Status"));
        toolbar.add(status);
        toolbar.add(load);
        contentPanel.add(toolbar);
        contentPanel.add(Box.createVerticalStrut(15));

        JPanel tableHolder = createDynamicContentPanel();
        contentPanel.add(tableHolder);

        Runnable render = () -> {
            tableHolder.removeAll();
            try {
                String selectedStatus = status.getSelectedItem() == null ? "semua"
                        : status.getSelectedItem().toString();
                com.mycompany.perpustakaan.api.HistoryPage page = libraryApi.getLoanHistory(selectedStatus, 1, 50);
                tableHolder.add(createTablePanel(createLoanHistoryTableModel(page.getLoans()), 520));
                refreshContent();
            } catch (SQLException e) {
                showError("Gagal memuat history", e);
            }
        };

        load.addActionListener(e -> render.run());
        status.addActionListener(e -> render.run());
        render.run();
        refreshContent();
    }

    private DefaultTableModel createLoanHistoryTableModel(List<com.mycompany.perpustakaan.api.LoanSummary> loans) {
        DefaultTableModel model = new DefaultTableModel(
                new Object[] { "ID", "Buku", "Pinjam", "Jatuh Tempo", "Kembali", "Status", "Denda" }, 0);
        if (loans == null || loans.isEmpty()) {
            model.addRow(new Object[] { "-", "Belum ada history untuk filter ini", "-", "-", "-", "-", "-" });
            return model;
        }

        for (com.mycompany.perpustakaan.api.LoanSummary loan : loans) {
            model.addRow(new Object[] {
                    loan.getIdPeminjaman(),
                    loan.getJudulBuku(),
                    loan.getTanggalPinjam(),
                    loan.getTanggalJatuhTempo(),
                    loan.getTanggalKembali(),
                    loan.getStatus(),
                    formatMoney(loan.getDendaBerjalan())
            });
        }
        return model;
    }

    private void showVisitForm() {
        resetContent();

        boolean staffMode = isStaffOrAdmin();

        addTitle(staffMode ? "Tambah Kunjungan Manual" : "Tambah Kunjungan Saya");

        addDashboardHero(
                staffMode ? "Catat Kunjungan Pengunjung" : "Check-in Kunjungan Perpustakaan",
                staffMode
                        ? "Staff dapat mencatat kunjungan untuk pengunjung umum atau tamu perpustakaan."
                        : "Isi data kunjungan kamu sebelum menggunakan layanan perpustakaan.");

        JPanel page = new JPanel(new BorderLayout(28, 0));
        page.setOpaque(false);
        page.setAlignmentX(Component.LEFT_ALIGNMENT);
        page.setMaximumSize(new Dimension(Integer.MAX_VALUE, 420));

        JPanel leftCard = createVisitInfoCard(staffMode);
        JPanel formCard = createVisitFormCard(staffMode);

        page.add(leftCard, BorderLayout.WEST);
        page.add(formCard, BorderLayout.CENTER);

        contentPanel.add(page);
        refreshContent();
    }

    private JPanel createVisitInfoCard(boolean staffMode) {
        JPanel card = new RoundedPanel(26, ACCENT_LIGHT, new Color(255, 226, 214), 1f);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(300, 380));
        card.setMaximumSize(new Dimension(300, 380));
        card.setBorder(new EmptyBorder(28, 26, 28, 26));

        JLabel icon = new JLabel("VISIT");
        icon.setFont(new Font("Segoe UI", Font.BOLD, 26));
        icon.setForeground(ACCENT);
        icon.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel(staffMode ? "Staff Mode" : "Self Check-in");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(TEXT_DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel desc = new JLabel("<html><div style='width:230px;'>"
                + (staffMode
                        ? "Gunakan form ini untuk mencatat kunjungan pengunjung yang datang langsung ke perpustakaan."
                        : "Catat kunjungan kamu agar aktivitas perpustakaan bisa tersimpan dengan rapi.")
                + "</div></html>");
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        desc.setForeground(TEXT_GRAY);
        desc.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel tips = new RoundedPanel(18, WHITE, CARD_BORDER, 1f);
        tips.setLayout(new BoxLayout(tips, BoxLayout.Y_AXIS));
        tips.setBorder(new EmptyBorder(16, 16, 16, 16));
        tips.setAlignmentX(Component.LEFT_ALIGNMENT);
        tips.setMaximumSize(new Dimension(240, 130));

        JLabel tipsTitle = new JLabel("Informasi");
        tipsTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tipsTitle.setForeground(TEXT_DARK);

        JLabel tipsText = new JLabel("<html><div style='width:200px;'>"
                + "Pastikan jenis pengunjung, asal instansi, dan keperluan diisi dengan benar."
                + "</div></html>");
        tipsText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tipsText.setForeground(TEXT_GRAY);

        tips.add(tipsTitle);
        tips.add(Box.createVerticalStrut(8));
        tips.add(tipsText);

        card.add(icon);
        card.add(Box.createVerticalStrut(22));
        card.add(title);
        card.add(Box.createVerticalStrut(10));
        card.add(desc);
        card.add(Box.createVerticalGlue());
        card.add(tips);

        return card;
    }

    private JPanel createVisitFormCard(boolean staffMode) {
        JPanel card = new RoundedPanel(26, WHITE, CARD_BORDER, 1f);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(28, 30, 28, 30));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 380));

        JLabel formTitle = new JLabel(staffMode ? "Form Kunjungan Manual" : "Form Kunjungan");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        formTitle.setForeground(TEXT_DARK);
        formTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel formDesc = new JLabel(staffMode
                ? "Masukkan data pengunjung yang datang ke perpustakaan."
                : "Data ini akan tersimpan sebagai kunjungan akun kamu.");
        formDesc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formDesc.setForeground(TEXT_GRAY);
        formDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(formTitle);
        card.add(Box.createVerticalStrut(5));
        card.add(formDesc);
        card.add(Box.createVerticalStrut(24));

        JTextField nama = createModernVisitField("");
        JComboBox<String> jenis = createModernVisitComboBox(new String[] {
                "mahasiswa",
                "pelajar",
                "dosen",
                "staff",
                "umum",
                "tamu"
        });
        JTextField asal = createModernVisitField("");
        JComboBox<String> keperluan = createModernVisitComboBox(new String[] {
                "Membaca buku",
                "Meminjam buku",
                "Mengembalikan buku",
                "Mengerjakan tugas",
                "Riset / referensi",
                "Kunjungan umum"
        });
        keperluan.setEditable(true);

        if (staffMode) {
            card.add(createModernFormRow("Nama Pengunjung", nama));
            card.add(Box.createVerticalStrut(14));
        }

        card.add(createModernFormRow("Jenis Pengunjung", jenis));
        card.add(Box.createVerticalStrut(14));
        card.add(createModernFormRow("Asal Instansi", asal));
        card.add(Box.createVerticalStrut(14));
        card.add(createModernFormRow("Keperluan", keperluan));
        card.add(Box.createVerticalStrut(26));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton cancel = createNeutralButton("Batal");
        JButton save = createActionButton("Simpan Kunjungan");

        cancel.addActionListener(e -> {
            if (staffMode) {
                showVisitManagement();
            } else {
                showDashboard();
            }
        });

        save.addActionListener(e -> {
            if (staffMode) {
                saveStaffVisit(nama, jenis, asal, keperluan);
            } else {
                saveMemberVisit(jenis, asal, keperluan);
            }
        });

        actions.add(cancel);
        actions.add(save);

        card.add(actions);

        return card;
    }

    private JTextField createModernVisitField(String value) {
        JTextField field = new JTextField(value);
        field.setPreferredSize(new Dimension(360, 38));
        field.setMaximumSize(new Dimension(360, 38));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setForeground(TEXT_DARK);
        field.setBackground(WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER),
                new EmptyBorder(8, 12, 8, 12)));
        return field;
    }

    private JComboBox<String> createModernVisitComboBox(String[] options) {
        JComboBox<String> comboBox = new JComboBox<>(options);
        comboBox.setPreferredSize(new Dimension(360, 38));
        comboBox.setMaximumSize(new Dimension(360, 38));
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        comboBox.setForeground(TEXT_DARK);
        comboBox.setBackground(WHITE);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER),
                new EmptyBorder(4, 10, 4, 10)));
        return comboBox;
    }

    private JPanel createModernFormRow(String labelText, Component field) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(620, 42));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(TEXT_DARK);
        label.setPreferredSize(new Dimension(140, 38));

        row.add(label);
        row.add(field);

        return row;
    }

    private void saveMemberVisit(JComboBox<String> jenis, JTextField asal, JComboBox<String> keperluan) {
        String selectedJenis = comboValue(jenis);
        String selectedKeperluan = comboValue(keperluan);
        if (selectedJenis.isEmpty() || selectedKeperluan.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Jenis pengunjung dan keperluan wajib diisi.", "Validasi",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            com.mycompany.perpustakaan.api.VisitResponse response = libraryApi.addRegisteredUserVisit(
                    selectedJenis,
                    asal.getText().trim(),
                    selectedKeperluan);

            showResponse(response.isSuccess(), response.getMessage());

            if (response.isSuccess()) {
                showDashboard();
            }
        } catch (SQLException e) {
            showError("Gagal menambah kunjungan", e);
        }
    }

    private void saveStaffVisit(JTextField nama, JComboBox<String> jenis, JTextField asal, JComboBox<String> keperluan) {
        String selectedJenis = comboValue(jenis);
        String selectedKeperluan = comboValue(keperluan);
        if (nama.getText().trim().isEmpty()
                || selectedJenis.isEmpty()
                || selectedKeperluan.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama, jenis pengunjung, dan keperluan wajib diisi.", "Validasi",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            com.mycompany.perpustakaan.api.VisitResponse response = libraryApi.addManualVisit(
                    nama.getText().trim(),
                    selectedJenis,
                    asal.getText().trim(),
                    selectedKeperluan);

            showResponse(response.isSuccess(), response.getMessage());

            if (response.isSuccess()) {
                showVisitManagement();
            }
        } catch (SQLException e) {
            showError("Gagal menambah kunjungan manual", e);
        }
    }

    private void showVisitManagement() {
        if (!requireStaffOrAdminView()) {
            return;
        }

        resetContent();
        addTitle("Manajemen Kunjungan");

        addDashboardHero(
                "Data Kunjungan Perpustakaan",
                "Pantau dan catat kunjungan pengunjung perpustakaan dari satu halaman.");

        JPanel actions = createToolbarPanel();
        JTextField search = createModernSearchField("Cari pengunjung / asal / keperluan...");
        JComboBox<String> status = new JComboBox<>(new String[] { "semua", "datang", "selesai", "batal" });
        status.setPreferredSize(new Dimension(150, 38));
        status.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JButton load = createActionButton("Tampilkan");
        JButton tambahKunjungan = createActionButton("Tambah Kunjungan");
        JButton refresh = createNeutralButton("Refresh");

        tambahKunjungan.addActionListener(e -> showVisitForm());
        refresh.addActionListener(e -> showVisitManagement());

        actions.add(search);
        actions.add(new JLabel("Status"));
        actions.add(status);
        actions.add(load);
        actions.add(tambahKunjungan);
        actions.add(refresh);

        contentPanel.add(actions);
        contentPanel.add(Box.createVerticalStrut(16));

        JPanel tableHolder = createDynamicContentPanel();
        JPanel actionHolder = createDynamicContentPanel();
        contentPanel.add(tableHolder);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(actionHolder);
        final int[] currentPage = { 1 };
        final int pageSize = 50;

        final Runnable[] render = new Runnable[1];
        render[0] = () -> {
            tableHolder.removeAll();
            actionHolder.removeAll();
            try {
                String keyword = searchText(search, "Cari pengunjung / asal / keperluan...");
                String selectedStatus = status.getSelectedItem() == null ? "semua"
                        : status.getSelectedItem().toString();
                int totalItems = libraryApi.countVisits(keyword, selectedStatus);
                int totalPages = calculateTotalPages(totalItems, pageSize);
                if (totalPages > 0 && currentPage[0] > totalPages) {
                    currentPage[0] = totalPages;
                }
                List<com.mycompany.perpustakaan.api.VisitSummary> visits = libraryApi.searchVisits(keyword,
                        selectedStatus, currentPage[0], pageSize);
                JTable table = createTable(createVisitManagementTableModel(visits));
                tableHolder.add(wrapTable(table, 420));
                JPanel visitActions = createVisitManagementActions(table);
                JButton prev = createNeutralButton("Prev");
                JButton next = createNeutralButton("Next");
                JLabel pageInfo = new JLabel("Halaman " + currentPage[0] + " / " + Math.max(1, totalPages)
                        + " - Total " + totalItems + " data");
                pageInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                pageInfo.setForeground(TEXT_GRAY);
                prev.setEnabled(currentPage[0] > 1);
                next.setEnabled(totalPages > 0 && currentPage[0] < totalPages);
                prev.addActionListener(e -> {
                    currentPage[0] = Math.max(1, currentPage[0] - 1);
                    render[0].run();
                });
                next.addActionListener(e -> {
                    currentPage[0]++;
                    render[0].run();
                });
                visitActions.add(Box.createHorizontalStrut(16));
                visitActions.add(prev);
                visitActions.add(next);
                visitActions.add(pageInfo);
                actionHolder.add(visitActions);
                refreshContent();
            } catch (SQLException e) {
                showError("Gagal memuat data kunjungan", e);
            }
        };

        load.addActionListener(e -> {
            currentPage[0] = 1;
            render[0].run();
        });
        search.addActionListener(e -> {
            currentPage[0] = 1;
            render[0].run();
        });
        status.addActionListener(e -> {
            currentPage[0] = 1;
            render[0].run();
        });
        render[0].run();

        refreshContent();
    }

    private DefaultTableModel createVisitManagementTableModel(
            List<com.mycompany.perpustakaan.api.VisitSummary> visits) {
        DefaultTableModel model = new DefaultTableModel(
                new Object[] { "ID", "Nama Pengunjung", "Jenis", "Asal Instansi", "Keperluan", "Status", "Tanggal" },
                0);
        if (visits == null || visits.isEmpty()) {
            model.addRow(new Object[] { "-", "Belum ada data", "-", "-", "-", "-", "-" });
            return model;
        }

        for (com.mycompany.perpustakaan.api.VisitSummary visit : visits) {
            model.addRow(new Object[] {
                    visit.getIdKunjungan(),
                    safeOrDash(visit.getNamaPengunjung()),
                    safeOrDash(visit.getJenisPengunjung()),
                    safeOrDash(visit.getAsalInstansi()),
                    safeOrDash(visit.getKeperluan()),
                    safeOrDash(visit.getStatusKunjungan()),
                    safeOrDash(visit.getTanggalKunjungan())
            });
        }
        return model;
    }

    private JPanel createVisitManagementActions(JTable table) {
        JPanel footer = createToolbarPanel();
        JButton detail = createNeutralButton("Detail");
        JButton finish = createActionButton("Selesaikan");
        JButton cancel = createDangerButton("Batalkan");

        detail.addActionListener(e -> showSelectedVisitDetail(table));
        finish.addActionListener(e -> updateSelectedVisitStatus(table, true));
        cancel.addActionListener(e -> updateSelectedVisitStatus(table, false));

        footer.add(detail);
        footer.add(finish);
        footer.add(cancel);
        return footer;
    }

    private void showSelectedVisitDetail(JTable table) {
        Integer id = selectedId(table);
        if (id == null) {
            return;
        }
        try {
            com.mycompany.perpustakaan.api.VisitSummary visit = libraryApi.getVisitById(id);
            if (visit == null) {
                JOptionPane.showMessageDialog(this, "Data kunjungan tidak ditemukan.", "Kunjungan",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            String message = "Nama: " + safeOrDash(visit.getNamaPengunjung())
                    + "\nJenis: " + safeOrDash(visit.getJenisPengunjung())
                    + "\nAsal: " + safeOrDash(visit.getAsalInstansi())
                    + "\nKeperluan: " + safeOrDash(visit.getKeperluan())
                    + "\nStatus: " + safeOrDash(visit.getStatusKunjungan())
                    + "\nTanggal: " + safeOrDash(visit.getTanggalKunjungan());
            JOptionPane.showMessageDialog(this, message, "Detail Kunjungan", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            showError("Gagal mengambil detail kunjungan", e);
        }
    }

    private void updateSelectedVisitStatus(JTable table, boolean finish) {
        Integer id = selectedId(table);
        if (id == null) {
            return;
        }

        String actionText = finish ? "selesaikan" : "batalkan";
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Yakin ingin " + actionText + " kunjungan ID " + id + "?",
                "Konfirmasi",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            com.mycompany.perpustakaan.api.VisitResponse response = finish
                    ? libraryApi.finishVisit(id)
                    : libraryApi.cancelVisit(id);
            showResponse(response.isSuccess(), response.getMessage());
            if (response.isSuccess()) {
                showVisitManagement();
            }
        } catch (SQLException e) {
            showError("Gagal mengubah status kunjungan", e);
        }
    }

    private void showAddBookDialog() {
        showBookFormPage(null);
    }

    private void showBookFormPage(Integer idBuku) {
        JTextField kode = createModernBookField("");
        JTextField judul = createModernBookField("");
        JTextField penulis = createModernBookField("");
        JTextField penerbit = createModernBookField("");
        JComboBox<String> kategori = createModernBookComboBox(loadBookCategoryOptions());
        JTextField tahun = createModernBookField("");
        JTextField stokTersedia = createModernBookField("1");
        JTextField stokTotal = createModernBookField("1");

        if (idBuku != null) {
            try {
                com.mycompany.perpustakaan.api.BookSummary book = libraryApi.getBookByIdForManagement(idBuku);

                kode.setText(safe(book.getKodeBuku()));
                judul.setText(safe(book.getJudul()));
                penulis.setText(safe(book.getPenulis()));
                penerbit.setText(safe(book.getPenerbit()));
                selectComboValue(kategori, safe(book.getKategori()));
                tahun.setText(book.getTahunTerbit() == null ? "" : String.valueOf(book.getTahunTerbit()));
                stokTersedia.setText(String.valueOf(book.getStokTersedia()));
                stokTotal.setText(String.valueOf(book.getStokTotal()));
            } catch (SQLException e) {
                showError("Gagal mengambil detail buku", e);
                return;
            }
        }

        resetContent();
        addTitle(idBuku == null ? "Tambah Buku" : "Update Buku");

        addDashboardHero(
                idBuku == null ? "Tambah Koleksi Buku Baru" : "Perbarui Data Buku",
                idBuku == null
                        ? "Lengkapi informasi buku agar koleksi perpustakaan tersimpan dengan rapi."
                        : "Edit data buku, stok, kategori, dan informasi penerbit secara lengkap.");

        JPanel page = new JPanel(new BorderLayout(30, 0));
        page.setOpaque(false);
        page.setAlignmentX(Component.LEFT_ALIGNMENT);
        page.setMaximumSize(new Dimension(Integer.MAX_VALUE, 560));

        page.add(createModernBookCoverPanel(idBuku == null ? "Buku Baru" : "Update Buku", 300, 470), BorderLayout.WEST);
        page.add(createModernBookFormCard(
                idBuku,
                kode,
                judul,
                penulis,
                penerbit,
                kategori,
                tahun,
                stokTersedia,
                stokTotal), BorderLayout.CENTER);

        contentPanel.add(page);
        refreshContent();
    }

    private JPanel createModernBookFormCard(
            Integer idBuku,
            JTextField kode,
            JTextField judul,
            JTextField penulis,
            JTextField penerbit,
            JComboBox<String> kategori,
            JTextField tahun,
            JTextField stokTersedia,
            JTextField stokTotal) {
        JPanel card = new RoundedPanel(26, WHITE, CARD_BORDER, 1f);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(26, 30, 26, 30));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 470));

        JLabel formTitle = new JLabel(idBuku == null ? "Informasi Buku" : "Edit Informasi Buku");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        formTitle.setForeground(TEXT_DARK);
        formTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel formDesc = new JLabel("Pastikan data buku sesuai dengan informasi koleksi perpustakaan.");
        formDesc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formDesc.setForeground(TEXT_GRAY);
        formDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(formTitle);
        card.add(Box.createVerticalStrut(5));
        card.add(formDesc);
        card.add(Box.createVerticalStrut(24));

        JPanel grid = new JPanel(new GridLayout(4, 2, 18, 14));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        grid.setMaximumSize(new Dimension(860, 250));

        grid.add(createModernBookInput("Kode Buku", kode));
        grid.add(createModernBookInput("Judul Buku", judul));
        grid.add(createModernBookInput("Penulis", penulis));
        grid.add(createModernBookInput("Penerbit", penerbit));
        grid.add(createModernBookInput("Kategori", kategori));
        grid.add(createModernBookInput("Tahun Terbit", tahun));
        grid.add(createModernBookInput("Stok Tersedia", stokTersedia));
        grid.add(createModernBookInput("Stok Total", stokTotal));

        card.add(grid);
        card.add(Box.createVerticalStrut(24));

        JPanel note = new RoundedPanel(18, ACCENT_LIGHT, new Color(255, 226, 214), 1f);
        note.setLayout(new BorderLayout());
        note.setBorder(new EmptyBorder(13, 16, 13, 16));
        note.setAlignmentX(Component.LEFT_ALIGNMENT);
        note.setMaximumSize(new Dimension(860, 58));

        JLabel noteText = new JLabel(
                "Tips: gunakan kode buku yang unik agar pencarian dan manajemen stok lebih mudah.");
        noteText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        noteText.setForeground(TEXT_GRAY);

        note.add(noteText, BorderLayout.CENTER);
        card.add(note);
        card.add(Box.createVerticalGlue());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton cancel = createNeutralButton("Kembali");
        JButton save = createActionButton(idBuku == null ? "Tambah Buku" : "Simpan Perubahan");

        cancel.addActionListener(e -> showBookManagement());

        save.addActionListener(e -> {
            try {
                if (kode.getText().trim().isEmpty()
                        || judul.getText().trim().isEmpty()
                        || penulis.getText().trim().isEmpty()
                        || comboValue(kategori).isEmpty()) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Kode, judul, penulis, dan kategori wajib diisi.",
                            "Validasi",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                Integer tahunValue = tahun.getText().trim().isEmpty()
                        ? null
                        : Integer.valueOf(tahun.getText().trim());

                int stokTersediaValue = Integer.parseInt(stokTersedia.getText().trim());
                int stokTotalValue = Integer.parseInt(stokTotal.getText().trim());

                if (stokTersediaValue > stokTotalValue) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Stok tersedia tidak boleh lebih besar dari stok total.",
                            "Validasi",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                com.mycompany.perpustakaan.api.BookRequest request = new com.mycompany.perpustakaan.api.BookRequest(
                        kode.getText().trim(),
                        judul.getText().trim(),
                        penulis.getText().trim(),
                        penerbit.getText().trim(),
                        comboValue(kategori),
                        tahunValue,
                        stokTersediaValue,
                        stokTotalValue);

                com.mycompany.perpustakaan.api.BookResponse response = idBuku == null
                        ? libraryApi.addBook(request)
                        : libraryApi.updateBook(idBuku, request);

                showResponse(response.isSuccess(), response.getMessage());

                if (response.isSuccess()) {
                    showBookManagement();
                }
            } catch (SQLException | NumberFormatException exception) {
                showError("Gagal menyimpan buku", exception);
            }
        });

        actions.add(cancel);
        actions.add(save);

        card.add(Box.createVerticalStrut(18));
        card.add(actions);

        return card;
    }

    private JPanel createModernBookInput(String labelText, Component field) {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(TEXT_DARK);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        if (field instanceof javax.swing.JComponent) {
            ((javax.swing.JComponent) field).setAlignmentX(Component.LEFT_ALIGNMENT);
        }

        box.add(label);
        box.add(Box.createVerticalStrut(7));
        box.add(field);

        return box;
    }

    private JTextField createModernBookField(String value) {
        JTextField field = new JTextField(value) {
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

        field.setPreferredSize(new Dimension(360, 40));
        field.setMaximumSize(new Dimension(360, 40));
        field.setMinimumSize(new Dimension(220, 40));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setForeground(TEXT_DARK);
        field.setBackground(WHITE);
        field.setOpaque(false);
        field.setBorder(new EmptyBorder(9, 13, 9, 13));

        return field;
    }

    private JComboBox<String> createModernBookComboBox(String[] options) {
        JComboBox<String> comboBox = new JComboBox<>(options);
        comboBox.setPreferredSize(new Dimension(360, 40));
        comboBox.setMaximumSize(new Dimension(360, 40));
        comboBox.setMinimumSize(new Dimension(220, 40));
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        comboBox.setForeground(TEXT_DARK);
        comboBox.setBackground(WHITE);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER),
                new EmptyBorder(4, 10, 4, 10)));
        return comboBox;
    }

    private String[] loadBookCategoryOptions() {
        java.util.ArrayList<String> options = new java.util.ArrayList<>();
        try {
            for (String category : libraryApi.getBookCategories()) {
                if (category != null && !category.isBlank() && !options.contains(category)) {
                    options.add(category);
                }
            }
        } catch (SQLException exception) {
            logger.log(java.util.logging.Level.WARNING, "Gagal memuat kategori buku", exception);
        }

        if (options.isEmpty()) {
            options.add("Teknologi Informasi");
            options.add("Ekonomi");
            options.add("Kesehatan");
            options.add("Hukum");
            options.add("Bahasa & Sastra");
            options.add("Filsafat");
            options.add("Sains & Riset");
            options.add("Manajemen");
        }

        return options.toArray(new String[0]);
    }

    private void selectComboValue(JComboBox<String> comboBox, String value) {
        if (comboBox == null || value == null || value.isBlank()) {
            return;
        }

        for (int i = 0; i < comboBox.getItemCount(); i++) {
            if (value.equalsIgnoreCase(comboBox.getItemAt(i))) {
                comboBox.setSelectedIndex(i);
                return;
            }
        }

        comboBox.addItem(value);
        comboBox.setSelectedItem(value);
    }

    private String comboValue(JComboBox<String> comboBox) {
        if (comboBox == null) {
            return "";
        }

        Object selected = comboBox.getSelectedItem();
        return selected == null ? "" : selected.toString().trim();
    }

    private JPanel createModernBookCoverPanel(String labelText, int width, int height) {
        JPanel card = new RoundedPanel(28, WHITE, CARD_BORDER, 1f);
        card.setLayout(new BorderLayout());
        card.setPreferredSize(new Dimension(width, height));
        card.setMaximumSize(new Dimension(width, height));
        card.setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel cover = new RoundedPanel(24, ACCENT_LIGHT, new Color(255, 226, 214), 1f);
        cover.setLayout(new BoxLayout(cover, BoxLayout.Y_AXIS));
        cover.setBorder(new EmptyBorder(24, 22, 24, 22));

        JLabel badge = new JLabel("BOOK COVER");
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setForeground(ACCENT_DARK);
        badge.setOpaque(true);
        badge.setBackground(WHITE);
        badge.setBorder(new EmptyBorder(7, 12, 7, 12));
        badge.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel image = new JLabel();
        ImageIcon placeholder = loadBookCoverPlaceholder(width - 70, 210, 46);

        if (placeholder != null) {
            image.setIcon(placeholder);
        } else {
            image.setText("No Image");
            image.setFont(new Font("Segoe UI", Font.BOLD, 14));
            image.setForeground(TEXT_GRAY);
        }

        image.setHorizontalAlignment(SwingConstants.CENTER);
        image.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel(labelText);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT_DARK);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel desc = new JLabel("<html><div style='text-align:center; width:210px;'>"
                + "Cover buku akan ditampilkan sebagai identitas visual koleksi perpustakaan."
                + "</div></html>");
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        desc.setForeground(TEXT_GRAY);
        desc.setAlignmentX(Component.CENTER_ALIGNMENT);

        cover.add(Box.createVerticalStrut(4));
        cover.add(badge);
        cover.add(Box.createVerticalGlue());
        cover.add(image);
        cover.add(Box.createVerticalStrut(20));
        cover.add(title);
        cover.add(Box.createVerticalStrut(8));
        cover.add(desc);
        cover.add(Box.createVerticalGlue());

        card.add(cover, BorderLayout.CENTER);

        return card;
    }

    private void showBookFormPageFromTable(JTable table) {
        Integer idBuku = selectedId(table);
        if (idBuku == null) {
            return;
        }
        showBookFormPage(idBuku);
    }

    private void updateSelectedBook(JTable table) {
        showBookFormPageFromTable(table);
    }

    private void showSelectedBookDetail(JTable table) {
        Integer id = selectedId(table);
        if (id == null) {
            return;
        }
        try {
            com.mycompany.perpustakaan.api.BookSummary book = libraryApi.getBookByIdForManagement(id);
            showBookDetailPage(book, true);
        } catch (SQLException e) {
            showError("Gagal mengambil detail buku", e);
        }
    }

    private void updateSelectedStock(JTable table) {
        Integer id = selectedId(table);
        if (id == null) {
            return;
        }
        showStockPage(id);
    }

    private void deleteSelectedBook(JTable table) {
        Integer id = selectedId(table);
        if (id == null) {
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Hapus buku ID " + id + "?", "Konfirmasi",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                com.mycompany.perpustakaan.api.BookResponse response = libraryApi.deleteBook(id);
                showResponse(response.isSuccess(), response.getMessage());
                showBookManagement();
            } catch (SQLException e) {
                showError("Gagal hapus buku", e);
            }
        }
    }

    private void showBookDetailPage(com.mycompany.perpustakaan.api.BookSummary book, boolean managementMode) {
        if (book == null) {
            return;
        }

        resetContent();
        addTitle("Detail Buku");

        addDashboardHero(
                safeOrDash(book.getJudul()),
                "Informasi lengkap buku, stok, kategori, dan status ketersediaan koleksi.");

        JPanel page = new JPanel(new BorderLayout(30, 0));
        page.setOpaque(false);
        page.setAlignmentX(Component.LEFT_ALIGNMENT);
        page.setMaximumSize(new Dimension(Integer.MAX_VALUE, 560));

        page.add(createModernBookCoverPanel("Detail Buku", 320, 480), BorderLayout.WEST);
        page.add(createModernBookDetailCard(book, managementMode), BorderLayout.CENTER);

        contentPanel.add(page);
        refreshContent();
    }

    private JPanel createModernBookDetailCard(com.mycompany.perpustakaan.api.BookSummary book, boolean managementMode) {
        JPanel card = new RoundedPanel(28, WHITE, CARD_BORDER, 1f);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(28, 30, 28, 30));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 480));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));

        JLabel title = new JLabel(safeOrDash(book.getJudul()));
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(TEXT_DARK);

        JLabel author = new JLabel("Ditulis oleh " + safeOrDash(book.getPenulis()));
        author.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        author.setForeground(TEXT_GRAY);

        titleBox.add(title);
        titleBox.add(Box.createVerticalStrut(6));
        titleBox.add(author);

        JLabel statusBadge = createBookStatusBadge(book);

        top.add(titleBox, BorderLayout.WEST);
        top.add(statusBadge, BorderLayout.EAST);

        card.add(top);
        card.add(Box.createVerticalStrut(24));

        JPanel infoGrid = new JPanel(new GridLayout(2, 3, 14, 14));
        infoGrid.setOpaque(false);
        infoGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        infoGrid.add(createBookDetailInfoItem("Kode Buku", safeOrDash(book.getKodeBuku())));
        infoGrid.add(createBookDetailInfoItem("Kategori", safeOrDash(book.getKategori())));
        infoGrid.add(createBookDetailInfoItem("Penerbit", safeOrDash(book.getPenerbit())));
        infoGrid.add(createBookDetailInfoItem("Tahun Terbit",
                book.getTahunTerbit() == null ? "-" : String.valueOf(book.getTahunTerbit())));
        infoGrid.add(createBookDetailInfoItem("Stok Tersedia", String.valueOf(book.getStokTersedia())));
        infoGrid.add(createBookDetailInfoItem("Stok Total", String.valueOf(book.getStokTotal())));

        card.add(infoGrid);
        card.add(Box.createVerticalStrut(20));

        JPanel synopsis = createModernSynopsisBlock(book);
        card.add(synopsis);
        card.add(Box.createVerticalGlue());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton back = createNeutralButton("Kembali");

        back.addActionListener(e -> {
            if (managementMode && isStaffOrAdmin()) {
                showBookManagement();
            } else {
                showDashboard();
            }
        });

        actions.add(back);

        if (managementMode && isStaffOrAdmin()) {
            JButton stock = createNeutralButton("Update Stok");
            JButton edit = createActionButton("Edit Buku");

            stock.addActionListener(e -> showStockPage(book.getIdBuku()));
            edit.addActionListener(e -> showBookFormPage(book.getIdBuku()));

            actions.add(stock);
            actions.add(edit);
        } else if (!isStaffOrAdmin() && book.getStokTersedia() > 0) {
            JButton request = createActionButton("Request Pinjam");
            request.addActionListener(e -> requestLoanFromDetail(book.getIdBuku()));
            actions.add(request);
        }

        card.add(Box.createVerticalStrut(18));
        card.add(actions);

        return card;
    }

    private JPanel createBookDetailInfoItem(String labelText, String valueText) {
        JPanel item = new RoundedPanel(20, SURFACE_ALT, CARD_BORDER, 1f);
        item.setLayout(new BorderLayout(0, 6));
        item.setBorder(new EmptyBorder(14, 16, 14, 16));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(TEXT_GRAY);

        JLabel value = new JLabel(safeOrDash(valueText));
        value.setFont(new Font("Segoe UI", Font.BOLD, 15));
        value.setForeground(TEXT_DARK);

        item.add(label, BorderLayout.NORTH);
        item.add(value, BorderLayout.CENTER);

        return item;
    }

    private JLabel createBookStatusBadge(com.mycompany.perpustakaan.api.BookSummary book) {
        boolean available = book.getStokTersedia() > 0;

        JLabel badge = new JLabel(available ? "TERSEDIA" : "STOK HABIS");
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setForeground(WHITE);
        badge.setOpaque(true);
        badge.setBackground(available ? GREEN_STATUS : RED_STATUS);
        badge.setBorder(new EmptyBorder(9, 14, 9, 14));

        return badge;
    }

    private JPanel createModernSynopsisBlock(com.mycompany.perpustakaan.api.BookSummary book) {
        JPanel block = new RoundedPanel(22, ACCENT_LIGHT, new Color(255, 226, 214), 1f);
        block.setLayout(new BoxLayout(block, BoxLayout.Y_AXIS));
        block.setBorder(new EmptyBorder(18, 20, 18, 20));
        block.setAlignmentX(Component.LEFT_ALIGNMENT);
        block.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        JLabel label = new JLabel("Ringkasan Buku");
        label.setFont(new Font("Segoe UI", Font.BOLD, 15));
        label.setForeground(TEXT_DARK);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        String text = "Buku ini berada pada kategori "
                + safeOrDash(book.getKategori())
                + ", ditulis oleh "
                + safeOrDash(book.getPenulis())
                + ", dan memiliki stok tersedia "
                + book.getStokTersedia()
                + " dari total "
                + book.getStokTotal()
                + " buku.";

        JLabel desc = new JLabel("<html><div style='width:760px;'>"
                + escapeHtml(text)
                + "</div></html>");
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        desc.setForeground(TEXT_GRAY);
        desc.setAlignmentX(Component.LEFT_ALIGNMENT);

        block.add(label);
        block.add(Box.createVerticalStrut(8));
        block.add(desc);

        return block;
    }

    private void showStockPage(int idBuku) {
        try {
            com.mycompany.perpustakaan.api.BookSummary book = libraryApi.getBookByIdForManagement(idBuku);

            resetContent();
            addTitle("Update Stok Buku");

            addDashboardHero(
                    "Kelola Stok Koleksi",
                    "Perbarui jumlah stok tersedia dan total stok buku secara cepat dan aman.");

            JPanel page = new JPanel(new BorderLayout(30, 0));
            page.setOpaque(false);
            page.setAlignmentX(Component.LEFT_ALIGNMENT);
            page.setMaximumSize(new Dimension(Integer.MAX_VALUE, 540));

            page.add(createModernBookCoverPanel("Update Stok", 320, 460), BorderLayout.WEST);
            page.add(createModernStockCard(book), BorderLayout.CENTER);

            contentPanel.add(page);
            refreshContent();
        } catch (SQLException e) {
            showError("Gagal membuka update stok", e);
        }
    }

    private JPanel createModernStockCard(com.mycompany.perpustakaan.api.BookSummary book) {
        JPanel card = new RoundedPanel(28, WHITE, CARD_BORDER, 1f);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(28, 30, 28, 30));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 460));

        JLabel title = new JLabel(safeOrDash(book.getJudul()));
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(TEXT_DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel(
                "Kode: " + safeOrDash(book.getKodeBuku()) + " • Kategori: " + safeOrDash(book.getKategori()));
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(TEXT_GRAY);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(title);
        card.add(Box.createVerticalStrut(6));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(26));

        JPanel summary = new JPanel(new GridLayout(1, 3, 14, 0));
        summary.setOpaque(false);
        summary.setAlignmentX(Component.LEFT_ALIGNMENT);
        summary.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        summary.add(createBookDetailInfoItem("Stok Tersedia", String.valueOf(book.getStokTersedia())));
        summary.add(createBookDetailInfoItem("Stok Total", String.valueOf(book.getStokTotal())));
        summary.add(createBookDetailInfoItem("Status", safeOrDash(book.getStatusKetersediaan())));

        card.add(summary);
        card.add(Box.createVerticalStrut(26));

        JTextField tersedia = createModernStockField(book.getStokTersedia());
        JTextField total = createModernStockField(book.getStokTotal());

        JPanel stockControls = new RoundedPanel(24, ACCENT_LIGHT, new Color(255, 226, 214), 1f);
        stockControls.setLayout(new BoxLayout(stockControls, BoxLayout.Y_AXIS));
        stockControls.setBorder(new EmptyBorder(22, 24, 22, 24));
        stockControls.setAlignmentX(Component.LEFT_ALIGNMENT);
        stockControls.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        stockControls.add(createModernStockControl("Stok Tersedia", tersedia));
        stockControls.add(Box.createVerticalStrut(18));
        stockControls.add(createModernStockControl("Stok Total", total));

        card.add(stockControls);
        card.add(Box.createVerticalGlue());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton back = createNeutralButton("Kembali");
        JButton save = createActionButton("Simpan Stok");

        back.addActionListener(e -> showBookDetailPage(book, true));

        save.addActionListener(e -> {
            try {
                int tersediaValue = Integer.parseInt(tersedia.getText().trim());
                int totalValue = Integer.parseInt(total.getText().trim());

                if (tersediaValue < 0 || totalValue < 0) {
                    JOptionPane.showMessageDialog(this, "Stok tidak boleh bernilai negatif.", "Validasi",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (tersediaValue > totalValue) {
                    JOptionPane.showMessageDialog(this, "Stok tersedia tidak boleh lebih besar dari stok total.",
                            "Validasi", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                com.mycompany.perpustakaan.api.BookResponse response = libraryApi.updateBookStock(
                        book.getIdBuku(),
                        tersediaValue,
                        totalValue);

                showResponse(response.isSuccess(), response.getMessage());

                if (response.isSuccess()) {
                    showBookManagement();
                }
            } catch (SQLException | NumberFormatException exception) {
                showError("Gagal update stok", exception);
            }
        });

        actions.add(back);
        actions.add(save);

        card.add(Box.createVerticalStrut(18));
        card.add(actions);

        return card;
    }

    private JTextField createModernStockField(int value) {
        JTextField field = createModernBookField(String.valueOf(value));
        field.setHorizontalAlignment(SwingConstants.CENTER);
        field.setFont(new Font("Segoe UI", Font.BOLD, 20));
        field.setPreferredSize(new Dimension(90, 42));
        field.setMaximumSize(new Dimension(90, 42));
        return field;
    }

    private JPanel createModernStockControl(String labelText, JTextField field) {
        JPanel row = new JPanel(new BorderLayout(18, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 15));
        label.setForeground(TEXT_DARK);

        JPanel control = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        control.setOpaque(false);

        JButton minus = createSmallStockButton("-");
        JButton plus = createSmallStockButton("+");

        minus.addActionListener(e -> adjustStockField(field, -1));
        plus.addActionListener(e -> adjustStockField(field, 1));

        control.add(minus);
        control.add(field);
        control.add(plus);

        row.add(label, BorderLayout.WEST);
        row.add(control, BorderLayout.EAST);

        return row;
    }

    private JButton createSmallStockButton(String text) {
        JButton button = new GradientActionButton(text);
        button.setPreferredSize(new Dimension(42, 38));
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setForeground(WHITE);
        button.setBackground(ACCENT);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setOpaque(false);
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        return button;
    }

    private void showCreateLoanDialog() {
        JTextField idUser = createField("");
        JTextField idBuku = createField("");
        JTextField hari = createField("7");
        JPanel panel = formPanel(new String[] { "ID User", "ID Buku", "Lama pinjam (hari)" },
                new JTextField[] { idUser, idBuku, hari });
        int result = JOptionPane.showConfirmDialog(this, panel, "Buat Peminjaman Untuk User",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                com.mycompany.perpustakaan.api.LoanResponse response = libraryApi.createLoanForUser(
                        Integer.parseInt(idUser.getText().trim()),
                        Integer.parseInt(idBuku.getText().trim()),
                        Integer.parseInt(hari.getText().trim()));
                showResponse(response.isSuccess(), response.getMessage());
                showLoanManagement();
            } catch (SQLException | NumberFormatException e) {
                showError("Gagal membuat peminjaman", e);
            }
        }
    }

    private void processSelectedReturn(JTable table) {
        Integer id = selectedId(table);
        if (id == null) {
            return;
        }
        try {
            com.mycompany.perpustakaan.api.LoanResponse response = libraryApi.processReturn(id);
            showResponse(response.isSuccess(), response.getMessage());
            showLoanManagement();
        } catch (SQLException e) {
            showError("Gagal proses pengembalian", e);
        }
    }

    private void requestSelectedLoan(JTable table) {
        Integer id = selectedId(table);
        if (id == null) {
            return;
        }
        JTextField hari = createField("7");
        int result = JOptionPane.showConfirmDialog(this, hari, "Lama pinjam (hari)", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                com.mycompany.perpustakaan.api.LoanResponse response = libraryApi.requestLoan(id,
                        Integer.parseInt(hari.getText().trim()));
                showResponse(response.isSuccess(), response.getMessage());
                showCurrentLoans();
            } catch (SQLException | NumberFormatException e) {
                showError("Gagal request peminjaman", e);
            }
        }
    }

    private void requestLoanFromDetail(int idBuku) {
        JTextField hari = createField("7");
        int result = JOptionPane.showConfirmDialog(this, hari, "Lama pinjam (hari)", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                com.mycompany.perpustakaan.api.LoanResponse response = libraryApi.requestLoan(idBuku,
                        Integer.parseInt(hari.getText().trim()));
                showResponse(response.isSuccess(), response.getMessage());
                if (response.isSuccess()) {
                    showCurrentLoans();
                }
            } catch (SQLException | NumberFormatException e) {
                showError("Gagal request peminjaman", e);
            }
        }
    }

    private void showAddMemberDialog() {
        saveMember(null, null);
        showMemberManagement();
    }

    private void updateSelectedMember(JTable table) {
        Integer id = selectedId(table);
        if (id != null) {
            saveMember(id, selectedMemberFromTable(table));
            showMemberManagement();
        }
    }

    private void saveMember(Integer idUser, com.mycompany.perpustakaan.api.MemberSummary initialMember) {
        JTextField username = createField("");
        JTextField nama = createField("");
        JTextField email = createField("");
        JTextField password = createField("");

        if (initialMember != null) {
            username.setText(safe(initialMember.getUsername()));
            nama.setText(safe(initialMember.getNama()));
            email.setText(safe(initialMember.getEmail()));
        }

        JPanel panel = formPanel(
                new String[] { "Username", "Nama", "Email", idUser == null ? "Password" : "Password baru" },
                new JTextField[] { username, nama, email, password });
        if (idUser != null) {
            JLabel hint = new JLabel("Kosongkan password kalau tidak ingin mengganti password lama.");
            hint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            hint.setForeground(TEXT_GRAY);
            panel.add(new JLabel(""));
            panel.add(hint);
        }
        int result = JOptionPane.showConfirmDialog(this, panel, idUser == null ? "Tambah Anggota" : "Update Anggota",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                com.mycompany.perpustakaan.api.MemberRequest request = new com.mycompany.perpustakaan.api.MemberRequest(
                        username.getText(), nama.getText(), email.getText(), password.getText());
                com.mycompany.perpustakaan.api.MemberResponse response = idUser == null ? libraryApi.addMember(request)
                        : libraryApi.updateMember(idUser, request);
                showResponse(response.isSuccess(), response.getMessage());
            } catch (SQLException e) {
                showError("Gagal menyimpan anggota", e);
            }
        }
    }

    private com.mycompany.perpustakaan.api.MemberSummary selectedMemberFromTable(JTable table) {
        int selected = table.getSelectedRow();
        if (selected < 0) {
            return null;
        }
        int id = Integer.parseInt(String.valueOf(table.getValueAt(selected, 0)));
        String nama = String.valueOf(table.getValueAt(selected, 1));
        String username = String.valueOf(table.getValueAt(selected, 2));
        String email = String.valueOf(table.getValueAt(selected, 3));
        String status = String.valueOf(table.getValueAt(selected, 4));
        return new com.mycompany.perpustakaan.api.MemberSummary(id, nama, email, username, "anggota", status, null);
    }

    private void changeSelectedMemberStatus(JTable table, boolean suspend) {
        Integer id = selectedId(table);
        if (id == null) {
            return;
        }
        try {
            com.mycompany.perpustakaan.api.MemberResponse response = suspend ? libraryApi.suspendMember(id)
                    : libraryApi.activateMember(id);
            showResponse(response.isSuccess(), response.getMessage());
            showMemberManagement();
        } catch (SQLException e) {
            showError("Gagal mengubah status anggota", e);
        }
    }

    private void deleteSelectedMember(JTable table) {
        Integer id = selectedId(table);
        if (id == null) {
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Hapus anggota ID " + id + "?", "Konfirmasi",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                com.mycompany.perpustakaan.api.MemberResponse response = libraryApi.deleteMember(id);
                showResponse(response.isSuccess(), response.getMessage());
                showMemberManagement();
            } catch (SQLException e) {
                showError("Gagal hapus anggota", e);
            }
        }
    }

    private void exportInventory(String format) {
        String dir = chooseDirectory();
        if (dir == null) {
            return;
        }
        try {
            com.mycompany.perpustakaan.api.ReportExportResponse response = libraryApi.exportInventoryReport(format,
                    dir);
            showResponse(response.isSuccess(),
                    response.getMessage() + (response.getFilePath() == null ? "" : "\n" + response.getFilePath()));
        } catch (SQLException e) {
            showError("Gagal export inventory", e);
        }
    }

    private void exportLoan(String format) {
        exportLoan(format, LocalDate.now().minusMonths(1).toString(), LocalDate.now().toString());
    }

    private void exportLoan(String format, String start, String end) {
        String dir = chooseDirectory();
        if (dir == null) {
            return;
        }
        try {
            com.mycompany.perpustakaan.api.ReportExportResponse response = libraryApi.exportLoanReport(format, dir,
                    parseDate(start), parseDate(end));
            showResponse(response.isSuccess(),
                    response.getMessage() + (response.getFilePath() == null ? "" : "\n" + response.getFilePath()));
        } catch (SQLException | DateTimeParseException e) {
            showError("Gagal export peminjaman", e);
        }
    }

    private void exportFine(String format, String keyword, String status) {
        String dir = chooseDirectory();
        if (dir == null) {
            return;
        }
        try {
            com.mycompany.perpustakaan.api.ReportExportResponse response =
                    libraryApi.exportFineReport(format, dir, keyword, status);
            showResponse(response.isSuccess(),
                    response.getMessage() + (response.getFilePath() == null ? "" : "\n" + response.getFilePath()));
        } catch (SQLException e) {
            showError("Gagal export denda", e);
        }
    }

    private void exportMember(String format, String keyword, String status) {
        String dir = chooseDirectory();
        if (dir == null) {
            return;
        }
        try {
            com.mycompany.perpustakaan.api.ReportExportResponse response =
                    libraryApi.exportMemberReport(format, dir, keyword, status);
            showResponse(response.isSuccess(),
                    response.getMessage() + (response.getFilePath() == null ? "" : "\n" + response.getFilePath()));
        } catch (SQLException e) {
            showError("Gagal export member", e);
        }
    }

    private void addBookSections() {
        try {
            com.mycompany.perpustakaan.api.DashboardSummary summary = libraryApi.getDashboardSummary(10);
            contentPanel.add(createSection("BUKU TERPOPULER", summary.getPopularBooks(), true));
            contentPanel.add(Box.createVerticalStrut(20));
            contentPanel.add(createSection("BUKU TERBARU", summary.getLatestBooks(), true));
        } catch (SQLException e) {
            showError("Gagal memuat dashboard", e);
            contentPanel.add(createSection("BUKU TERPOPULER", Collections.emptyList(), true));
        }
    }

    private JPanel createSection(String title, List<com.mycompany.perpustakaan.api.BookSummary> books,
            boolean withArrow) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titlePanel.setOpaque(false);
        titlePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(ACCENT);
        titlePanel.add(titleLabel);

        if (withArrow) {
            JLabel arrow = new JLabel(">");
            arrow.setFont(new Font("Segoe UI", Font.BOLD, 18));
            arrow.setForeground(ACCENT);
            titlePanel.add(arrow);
        }

        section.add(titlePanel);
        section.add(Box.createVerticalStrut(15));

        HorizontalScrollPanel scrollPanel = new HorizontalScrollPanel();
        if (books == null || books.isEmpty()) {
            scrollPanel.addCard(null);
        } else {
            for (com.mycompany.perpustakaan.api.BookSummary book : books) {
                scrollPanel.addCard(book);
            }
        }
        section.add(scrollPanel);
        return section;
    }

    private class HorizontalScrollPanel extends JPanel {
        private final JPanel cardsContainer;

        HorizontalScrollPanel() {
            setLayout(new BorderLayout());
            setOpaque(false);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));
            setAlignmentX(Component.LEFT_ALIGNMENT);

            cardsContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
            cardsContainer.setOpaque(false);

            JScrollPane scroll = new JScrollPane(cardsContainer);
            scroll.setOpaque(false);
            scroll.getViewport().setOpaque(false);
            scroll.setBorder(null);
            scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
            scroll.setPreferredSize(new Dimension(1000, 260));
            scroll.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 0));
            scroll.getHorizontalScrollBar().setUnitIncrement(16);

            JPanel arrowPanel = new JPanel(new BorderLayout());
            arrowPanel.setOpaque(false);
            arrowPanel.setPreferredSize(new Dimension(36, 260));

            JLabel rightArrow = new JLabel(">") {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setColor(new Color(0, 0, 0, 80));
                    g2d.fillOval(2, 2, getWidth() - 4, getHeight() - 4);
                    g2d.setColor(WHITE);
                    g2d.setFont(getFont());
                    java.awt.FontMetrics fm = g2d.getFontMetrics();
                    int textX = (getWidth() - fm.stringWidth(getText())) / 2;
                    int textY = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                    g2d.drawString(getText(), textX, textY);
                    g2d.dispose();
                }
            };
            rightArrow.setFont(new Font("Segoe UI", Font.BOLD, 16));
            rightArrow.setHorizontalAlignment(SwingConstants.CENTER);
            rightArrow.setPreferredSize(new Dimension(32, 32));
            rightArrow.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            rightArrow.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    scroll.getHorizontalScrollBar().setValue(scroll.getHorizontalScrollBar().getValue() + 340);
                }
            });

            arrowPanel.add(rightArrow, BorderLayout.CENTER);
            add(scroll, BorderLayout.CENTER);
            add(arrowPanel, BorderLayout.EAST);
        }

        void addCard(com.mycompany.perpustakaan.api.BookSummary book) {
            cardsContainer.add(new BookCard(book));
        }
    }

    private class BookCard extends JPanel {
        BookCard(com.mycompany.perpustakaan.api.BookSummary book) {
            String title = book == null ? "Belum ada buku" : book.getJudul();
            String author = book == null ? "Data belum tersedia" : book.getPenulis();
            boolean available = book != null && book.getStokTersedia() > 0;

            setPreferredSize(CARD_SIZE);
            setBackground(WHITE);
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1),
                    new EmptyBorder(0, 0, 10, 0)));
            setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

            JLabel cover = new JLabel();
            ImageIcon placeholder = loadBookCoverPlaceholder(160, 100, 18);
            if (placeholder != null) {
                cover.setIcon(placeholder);
            } else {
                cover.setText("No Image");
                cover.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                cover.setForeground(TEXT_GRAY);
            }
            cover.setHorizontalAlignment(SwingConstants.CENTER);
            cover.setPreferredSize(new Dimension(160, 100));
            cover.setOpaque(true);
            cover.setBackground(new Color(250, 250, 250));

            JPanel info = new JPanel();
            info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
            info.setOpaque(false);
            info.setBorder(new EmptyBorder(10, 12, 5, 12));

            JLabel titleLabel = new JLabel(truncate(title, 18));
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            titleLabel.setForeground(TEXT_DARK);

            JLabel authorLabel = new JLabel(truncate(author, 20));
            authorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            authorLabel.setForeground(TEXT_GRAY);

            JLabel statusLabel = new JLabel(available ? "Tersedia" : "Tidak Tersedia");
            statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 9));
            statusLabel.setForeground(WHITE);
            statusLabel.setOpaque(true);
            statusLabel.setBackground(available ? GREEN_STATUS : RED_STATUS);
            statusLabel.setBorder(new EmptyBorder(3, 8, 3, 8));
            statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            info.add(titleLabel);
            info.add(authorLabel);
            info.add(Box.createVerticalStrut(5));
            info.add(statusLabel);
            info.add(Box.createVerticalGlue());

            JLabel link = new JLabel("Lihat Selengkapnya >");
            link.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            link.setForeground(ACCENT);
            link.setBorder(new EmptyBorder(5, 12, 0, 12));
            link.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            MouseAdapter openDetail = new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (book != null) {
                        showBookDetailPage(book, false);
                    }
                }
            };
            addMouseListener(openDetail);
            link.addMouseListener(openDetail);

            add(cover, BorderLayout.NORTH);
            add(info, BorderLayout.CENTER);
            add(link, BorderLayout.SOUTH);
        }
    }

    private void addQuickActions(String[] labels, Runnable[] actions) {
        JPanel panel = createToolbarPanel();
        for (int i = 0; i < labels.length; i++) {
            JButton button = createActionButton(labels[i]);
            Runnable action = actions[i];
            button.addActionListener(e -> action.run());
            panel.add(button);
        }
        contentPanel.add(panel);
        contentPanel.add(Box.createVerticalStrut(18));
    }

    private JPanel metricRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 18, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 112));
        return row;
    }

    private JPanel createMetricCard(String title, String value) {
        JPanel card = new RoundedPanel(22, WHITE, CARD_BORDER, 1f);
        card.setLayout(new BorderLayout(12, 0));
        card.setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel iconBox = new RoundedPanel(16, ACCENT_SOFT, new Color(255, 222, 210), 1f);
        iconBox.setPreferredSize(new Dimension(54, 54));
        iconBox.setLayout(new BorderLayout());

        JLabel icon = new JLabel(getMetricIcon(title));
        icon.setFont(new Font("Segoe UI", Font.BOLD, 22));
        icon.setForeground(ACCENT);
        icon.setHorizontalAlignment(SwingConstants.CENTER);
        iconBox.add(icon, BorderLayout.CENTER);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(TEXT_GRAY);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 25));
        valueLabel.setForeground(TEXT_DARK);

        text.add(Box.createVerticalStrut(2));
        text.add(titleLabel);
        text.add(Box.createVerticalStrut(8));
        text.add(valueLabel);

        card.add(iconBox, BorderLayout.WEST);
        card.add(text, BorderLayout.CENTER);

        return card;
    }

    private String getMetricIcon(String title) {
        String lower = title == null ? "" : title.toLowerCase();

        if (lower.contains("buku")) {
            return "BK";
        }
        if (lower.contains("anggota") || lower.contains("member")) {
            return "US";
        }
        if (lower.contains("pinjaman")) {
            return "LN";
        }
        if (lower.contains("denda")) {
            return "Rp";
        }
        if (lower.contains("role")) {
            return "★";
        }

        return "•";
    }

    private JPanel createTablePanel(DefaultTableModel model, int height) {
        return wrapTable(createTable(model), height);
    }

    private JTable createTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(42);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(ACCENT_SOFT);
        table.getTableHeader().setForeground(TEXT_DARK);
        table.getTableHeader().setPreferredSize(new Dimension(0, 42));

        table.setSelectionBackground(new Color(255, 231, 222));
        table.setSelectionForeground(TEXT_DARK);
        table.setGridColor(new Color(244, 238, 234));
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setFillsViewportHeight(true);

        return table;
    }

    private JPanel wrapTable(JTable table, int height) {
        JPanel panel = new RoundedPanel(22, WHITE, CARD_BORDER, 1f);
        panel.setLayout(new BorderLayout());
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, height + 32));
        panel.setBorder(new EmptyBorder(14, 14, 14, 14));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(1500, height));
        scroll.setMinimumSize(new Dimension(900, height));
        scroll.setBorder(null);
        scroll.getViewport().setBackground(WHITE);

        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createToolbarPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        panel.setBorder(new EmptyBorder(6, 0, 10, 0));
        return panel;
    }

    private JPanel createDynamicContentPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        return panel;
    }

    private JPanel formPanel(String[] labels, JTextField[] fields) {
        JPanel panel = new JPanel(new GridLayout(labels.length, 2, 10, 8));
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));
        panel.setOpaque(false);
        for (int i = 0; i < labels.length; i++) {
            JLabel label = new JLabel(labels[i]);
            label.setFont(new Font("Segoe UI", Font.BOLD, 12));
            label.setForeground(TEXT_DARK);
            panel.add(label);
            panel.add(fields[i]);
        }
        return panel;
    }

    private JPanel createBookFormFields(String[] labels, JTextField[] fields) {
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.setMaximumSize(new Dimension(560, 360));

        for (int i = 0; i < labels.length; i++) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
            row.setOpaque(false);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            row.setMaximumSize(new Dimension(560, 40));

            JLabel label = new JLabel(labels[i]);
            label.setFont(new Font("Segoe UI", Font.BOLD, 12));
            label.setForeground(TEXT_DARK);
            label.setPreferredSize(new Dimension(120, 36));
            label.setHorizontalAlignment(SwingConstants.LEFT);

            JTextField field = fields[i];
            field.setMaximumSize(new Dimension(360, 36));
            field.setPreferredSize(new Dimension(360, 36));
            field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(210, 210, 210)),
                    new EmptyBorder(7, 10, 7, 10)));

            row.add(label);
            row.add(field);
            form.add(row);
            if (i < labels.length - 1) {
                form.add(Box.createVerticalStrut(10));
            }
        }

        return form;
    }

    private JTextField createField(String value) {
        JTextField field = new JTextField(value);
        field.setPreferredSize(new Dimension(180, 32));
        return field;
    }

    private JTextField createStockField(int value) {
        JTextField field = createField(String.valueOf(value));
        field.setHorizontalAlignment(SwingConstants.CENTER);
        field.setFont(new Font("Segoe UI", Font.BOLD, 18));
        field.setMaximumSize(new Dimension(90, 38));
        return field;
    }

    private JPanel createStockControl(String labelText, JTextField field) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel(labelText + ":");
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));
        label.setForeground(TEXT_DARK);

        JButton minus = createRoundStockButton("-");
        JButton plus = createRoundStockButton("+");
        minus.addActionListener(e -> adjustStockField(field, -1));
        plus.addActionListener(e -> adjustStockField(field, 1));

        row.add(label);
        row.add(minus);
        row.add(field);
        row.add(plus);
        return row;
    }

    private JButton createRoundStockButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(30, 30));
        button.setFont(new Font("Segoe UI", Font.BOLD, 15));
        button.setForeground(TEXT_DARK);
        button.setBackground(new Color(238, 238, 238));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(TEXT_GRAY, 1));
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        return button;
    }

    private void adjustStockField(JTextField field, int delta) {
        try {
            int current = Integer.parseInt(field.getText().trim());
            field.setText(String.valueOf(Math.max(0, current + delta)));
        } catch (NumberFormatException e) {
            field.setText("0");
        }
    }

    private JButton createActionButton(String text) {
        JButton button = new GradientActionButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(WHITE);
        button.setBackground(ACCENT);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setOpaque(false);
        button.setBorder(new EmptyBorder(10, 17, 10, 17));
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        return button;
    }

    private JButton createNeutralButton(String text) {
        JButton button = new OutlineButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(ACCENT_DARK);
        button.setBackground(WHITE);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setOpaque(false);
        button.setBorder(new EmptyBorder(9, 18, 9, 18));
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        return button;
    }

    private JPanel createBookCoverPanel(int width, int height) {
        JPanel cover = new JPanel(new BorderLayout());
        cover.setPreferredSize(new Dimension(width, height));
        cover.setMaximumSize(new Dimension(width, height));
        cover.setBackground(WHITE);
        cover.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TEXT_GRAY, 1),
                new EmptyBorder(18, 18, 18, 18)));

        JLabel image = new JLabel();
        ImageIcon placeholder = loadBookCoverPlaceholder(width, height, 48);
        if (placeholder != null) {
            image.setIcon(placeholder);
        } else {
            image.setText("No Image");
            image.setForeground(TEXT_GRAY);
            image.setFont(new Font("Segoe UI", Font.BOLD, 14));
        }
        image.setHorizontalAlignment(SwingConstants.CENTER);
        cover.add(image, BorderLayout.CENTER);
        return cover;
    }

    private JPanel createDetailRow(String labelText, String valueText) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel(labelText + " :");
        label.setFont(new Font("Segoe UI", Font.BOLD, 17));
        label.setForeground(TEXT_DARK);

        JLabel value = new JLabel(valueText == null || valueText.isBlank() ? "-" : valueText);
        value.setFont(new Font("Segoe UI", Font.PLAIN, 17));
        value.setForeground(TEXT_DARK);

        row.add(label);
        row.add(value);
        return row;
    }

    private JPanel createSynopsisBlock(com.mycompany.perpustakaan.api.BookSummary book) {
        JPanel block = new JPanel();
        block.setLayout(new BoxLayout(block, BoxLayout.Y_AXIS));
        block.setOpaque(false);
        block.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel("Sinopsis :");
        label.setFont(new Font("Segoe UI", Font.BOLD, 17));
        label.setForeground(TEXT_DARK);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        String text = "Detail sinopsis belum tersedia di data buku. Buku ini berada pada kategori "
                + safeOrDash(book.getKategori()) + ", ditulis oleh " + safeOrDash(book.getPenulis())
                + ", dan saat ini memiliki stok " + book.getStokTersedia() + " dari total " + book.getStokTotal() + ".";
        JLabel synopsis = new JLabel("<html><div style='width:560px;'>" + escapeHtml(text) + "</div></html>");
        synopsis.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        synopsis.setForeground(TEXT_DARK);
        synopsis.setAlignmentX(Component.LEFT_ALIGNMENT);

        block.add(label);
        block.add(Box.createVerticalStrut(4));
        block.add(synopsis);
        return block;
    }

    private Color darken(Color color, float amount) {
        float factor = Math.max(0f, Math.min(1f, 1f - amount));

        return new Color(
                Math.max(0, Math.round(color.getRed() * factor)),
                Math.max(0, Math.round(color.getGreen() * factor)),
                Math.max(0, Math.round(color.getBlue() * factor)),
                color.getAlpha());
    }

    private class GradientActionButton extends JButton {
        GradientActionButton(String text) {
            super(text);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color base = getBackground() == null ? ACCENT : getBackground();
            Color endBase = darken(base, 0.16f);

            if (getModel().isPressed()) {
                base = darken(base, 0.10f);
            }

            Color start = getModel().isRollover() ? endBase : base;
            Color end = getModel().isRollover() ? base : endBase;

            g2d.setColor(new Color(0, 0, 0, 18));
            g2d.fillRoundRect(0, 3, getWidth(), getHeight() - 3, 16, 16);

            GradientPaint gp = new GradientPaint(0, 0, start, getWidth(), getHeight(), end);
            g2d.setPaint(gp);
            g2d.fillRoundRect(0, 0, getWidth(), getHeight() - 3, 16, 16);

            g2d.dispose();
            super.paintComponent(g);
        }
    }

    private class RoundedPanel extends JPanel {
        private final int arc;
        private final Color fillColor;
        private final Color borderColor;
        private final float borderWidth;

        RoundedPanel(int arc, Color fillColor, Color borderColor, float borderWidth) {
            this.arc = arc;
            this.fillColor = fillColor;
            this.borderColor = borderColor;
            this.borderWidth = borderWidth;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int inset = Math.max(2, Math.round(borderWidth));
            g2d.setColor(fillColor);
            g2d.fillRoundRect(inset, inset, getWidth() - (inset * 2), getHeight() - (inset * 2), arc, arc);
            if (borderColor != null && borderWidth > 0) {
                g2d.setColor(borderColor);
                g2d.setStroke(new BasicStroke(borderWidth));
                g2d.drawRoundRect(inset, inset, getWidth() - (inset * 2), getHeight() - (inset * 2), arc, arc);
            }
            g2d.dispose();
            super.paintComponent(g);
        }
    }

    private class OutlineButton extends JButton {
        OutlineButton(String text) {
            super(text);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            ButtonModel model = getModel();
            Color borderColor = model.isPressed() ? ACCENT_DARK : ACCENT;
            Color fillColor = model.isPressed()
                    ? new Color(255, 231, 222)
                    : model.isRollover() ? new Color(255, 246, 242) : WHITE;

            g2d.setColor(fillColor);
            g2d.fillRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 12, 12);
            g2d.setColor(borderColor);
            g2d.setStroke(new BasicStroke(1.4f));
            g2d.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 12, 12);
            g2d.dispose();

            super.paintComponent(g);
        }
    }

    private class DangerButton extends JButton {
        DangerButton(String text) {
            super(text);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color fillColor;
            if (getModel().isPressed()) {
                fillColor = new Color(153, 27, 27);
            } else if (getModel().isRollover()) {
                fillColor = new Color(185, 28, 28);
            } else {
                fillColor = RED_STATUS;
            }

            g2d.setColor(new Color(0, 0, 0, 20));
            g2d.fillRoundRect(7, 4, getWidth() - 14, getHeight() - 8, 14, 14);
            g2d.setColor(fillColor);
            g2d.fillRoundRect(5, 2, getWidth() - 10, getHeight() - 4, 14, 14);
            g2d.dispose();

            setForeground(WHITE);
            super.paintComponent(g);
        }
    }

    private JButton createNotificationButton() {
        JButton button = new JButton();
        try {
            button.setIcon(new ImageIcon(getClass().getResource("/assets/icons/icon-notification.png")));
        } catch (Exception e) {
            button.setText("Notif");
            button.setFont(new Font("Segoe UI", Font.BOLD, 12));
            button.setForeground(ACCENT);
        }
        button.setPreferredSize(new Dimension(52, 52));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setBorderPainted(false);
        button.setBorder(new EmptyBorder(0, 0, 0, 0));
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        button.addActionListener(e -> showNotifications());
        return button;
    }

    private void showNotifications() {
        try {
            List<com.mycompany.perpustakaan.api.NotificationSummary> notifications = libraryApi.getNotifications(8);
            StringBuilder message = new StringBuilder();
            for (com.mycompany.perpustakaan.api.NotificationSummary notification : notifications) {
                if (message.length() > 0) {
                    message.append("\n\n");
                }
                message.append(notification.getJudul())
                        .append("\n")
                        .append(notification.getPesan());
            }
            Object[] options = { "Tutup", "Tandai Dibaca" };
            int choice = JOptionPane.showOptionDialog(
                    this,
                    message.toString(),
                    "Notifikasi",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    options,
                    options[0]);
            if (choice == 1) {
                com.mycompany.perpustakaan.api.MemberResponse response = libraryApi.markAllNotificationsRead();
                showResponse(response.isSuccess(), response.getMessage());
            }
        } catch (SQLException | IllegalStateException e) {
            showError("Gagal memuat notifikasi", e);
        }
    }

    private JButton createLogoutButton() {
        JButton button = new DangerButton("Logout");
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(WHITE);
        button.setBackground(RED_STATUS);
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setMaximumSize(new Dimension(214, 48));
        button.setPreferredSize(new Dimension(214, 48));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(10, 16, 10, 16));
        button.addActionListener(e -> performLogout());
        return button;
    }

    private void performLogout() {
        String token = AuthSessionStore.getSessionToken();

        try {
            if (token != null) {
                try {
                    libraryApi.getClass().getMethod("logout", String.class).invoke(libraryApi, token);
                } catch (NoSuchMethodException fallback) {
                    libraryApi.logout();
                }
            } else {
                libraryApi.logout();
            }

        } catch (ReflectiveOperationException exception) {
            logger.log(java.util.logging.Level.WARNING, "Gagal revoke token saat logout", exception);

        } finally {
            AuthSessionStore.clear();
            new LoginForm(new com.mycompany.perpustakaan.api.LibraryApi()).setVisible(true);
            dispose();
        }
    }

    private void addTitle(String title) {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setOpaque(false);
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));

        JLabel label = new JLabel(title);
        label.setFont(new Font("Segoe UI", Font.BOLD, 26));
        label.setForeground(TEXT_DARK);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel lineWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        lineWrap.setOpaque(false);
        lineWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        lineWrap.setPreferredSize(new Dimension(210, 2));
        lineWrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));

        JPanel accentLine = new JPanel();
        accentLine.setBackground(ACCENT);
        accentLine.setPreferredSize(new Dimension(210, 2));
        accentLine.setMaximumSize(new Dimension(210, 2));
        accentLine.setMinimumSize(new Dimension(210, 2));

        lineWrap.add(accentLine);

        wrapper.add(label);
        wrapper.add(Box.createVerticalStrut(6));
        wrapper.add(lineWrap);

        contentPanel.add(wrapper);
        contentPanel.add(Box.createVerticalStrut(22));
    }

    private void resetContent() {
        contentPanel.removeAll();
    }

    private void refreshContent() {
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void setActive(SidebarButton button) {
        if (activeButton != null) {
            activeButton.repaint();
        }
        activeButton = button;
        activeButton.repaint();
    }

    private ImageIcon loadMenuIcon(String resourcePath) {
        return loadIconResource(resourcePath, 22, 22);
    }

    private ImageIcon loadIconResource(String resourcePath, int width, int height) {
        java.net.URL resource = getClass().getResource(resourcePath);
        if (resource == null) {
            return null;
        }
        if (resourcePath.toLowerCase().endsWith(".svg")) {
            return createSvgFallbackIcon(resourcePath, width, height);
        }
        ImageIcon source = new ImageIcon(resource);
        if (source.getIconWidth() <= 0 || source.getIconHeight() <= 0) {
            return createSvgFallbackIcon(resourcePath, width, height);
        }
        Image scaled = source.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    private ImageIcon loadBrandLogoIcon(int maxWidth, int maxHeight) {
        java.net.URL resource = getClass().getResource("/assets/branding/library-logo.png");
        if (resource == null) {
            return null;
        }
        ImageIcon source = new ImageIcon(resource);
        return new ImageIcon(scaleImageToFit(source, maxWidth, maxHeight));
    }

    private ImageIcon createSvgFallbackIcon(String resourcePath) {
        return createSvgFallbackIcon(resourcePath, 22, 22);
    }

    private ImageIcon createSvgFallbackIcon(String resourcePath, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.setColor(TEXT_DARK);

        String name = resourcePath.toLowerCase();
        if (name.contains("search")) {
            drawSearchIcon(g2d, width, height);
        } else if (name.contains("member") || name.contains("customer")) {
            drawMemberIcon(g2d);
        } else if (name.contains("book")) {
            drawBookIcon(g2d);
        } else if (name.contains("homework") || name.contains("control") || name.contains("time")
                || name.contains("report") || name.contains("loan") || name.contains("visit")) {
            drawReportIcon(g2d);
        } else {
            g2d.fillRoundRect(4, 4, 14, 14, 3, 3);
        }

        g2d.dispose();
        return new ImageIcon(image);
    }

    private void drawSearchIcon(Graphics2D g2d, int width, int height) {
        int size = Math.min(width, height);
        int lens = Math.max(7, size / 2);
        int lensX = Math.max(2, size / 5);
        int lensY = Math.max(2, size / 5);
        g2d.drawOval(lensX, lensY, lens, lens);
        int handleStart = lensX + lens - 1;
        int handleY = lensY + lens - 1;
        g2d.drawLine(handleStart, handleY, size - 3, size - 3);
    }

    private void drawMemberIcon(Graphics2D g2d) {
        g2d.drawOval(8, 4, 6, 6);
        g2d.drawArc(5, 11, 12, 8, 0, 180);
        g2d.drawOval(3, 7, 4, 4);
        g2d.drawOval(15, 7, 4, 4);
        g2d.drawArc(1, 13, 8, 6, 0, 160);
        g2d.drawArc(13, 13, 8, 6, 20, 160);
    }

    private void drawBookIcon(Graphics2D g2d) {
        g2d.drawRoundRect(4, 4, 6, 14, 2, 2);
        g2d.drawRoundRect(11, 4, 7, 14, 2, 2);
        g2d.drawLine(10, 5, 10, 18);
    }

    private void drawReportIcon(Graphics2D g2d) {
        g2d.drawRoundRect(5, 3, 12, 16, 2, 2);
        g2d.drawLine(8, 8, 14, 8);
        g2d.drawLine(8, 12, 14, 12);
        g2d.drawLine(8, 16, 12, 16);
    }

    private ImageIcon loadBookCoverPlaceholder(int width, int height, int padding) {
        String[] candidates = { "/assets/images/empty-book-cover.png", "/assets/images/empty-image.png" };
        for (String candidate : candidates) {
            java.net.URL resource = getClass().getResource(candidate);
            if (resource != null) {
                ImageIcon source = new ImageIcon(resource);
                int maxWidth = Math.max(1, width - (padding * 2));
                int maxHeight = Math.max(1, height - (padding * 2));
                Image scaled = scaleImageToFit(source, maxWidth, maxHeight);
                return new ImageIcon(scaled);
            }
        }
        return null;
    }

    private Image scaleImageToFit(ImageIcon source, int maxWidth, int maxHeight) {
        int sourceWidth = source.getIconWidth();
        int sourceHeight = source.getIconHeight();
        if (sourceWidth <= 0 || sourceHeight <= 0) {
            return source.getImage().getScaledInstance(maxWidth, maxHeight, Image.SCALE_SMOOTH);
        }

        double scale = Math.min((double) maxWidth / sourceWidth, (double) maxHeight / sourceHeight);
        int targetWidth = Math.max(1, (int) Math.round(sourceWidth * scale));
        int targetHeight = Math.max(1, (int) Math.round(sourceHeight * scale));
        return source.getImage().getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
    }

    private void loadUserData() {
        com.mycompany.perpustakaan.api.UserSummary profile = libraryApi.getCurrentUser();
        if (profile != null) {
            currentRole = profile.getRole() == null ? "anggota" : profile.getRole().toLowerCase();
            welcomeLabel.setText("WELCOME, " + profile.getNama().toUpperCase());
            roleLabel.setText(displayRole());
            setTitle("Dashboard - " + profile.getUsername());
        }
    }

    private void showProfile() {
        resetContent();

        com.mycompany.perpustakaan.api.UserSummary profile = libraryApi.getCurrentUser();
        if (profile == null) {
            JOptionPane.showMessageDialog(this, "Belum ada user yang login.", "Profil", JOptionPane.WARNING_MESSAGE);
            refreshContent();
            return;
        }

        JPanel page = new JPanel();
        page.setOpaque(false);
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setAlignmentX(Component.LEFT_ALIGNMENT);
        page.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JLabel title = new JLabel("User Profile");
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setForeground(TEXT_DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Kelola informasi akun dan akses perpustakaan kamu.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(TEXT_GRAY);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        page.add(title);
        page.add(Box.createVerticalStrut(6));
        page.add(subtitle);
        page.add(Box.createVerticalStrut(24));

        JPanel mainCard = new RoundedPanel(26, WHITE, new Color(235, 235, 235), 1f);
        mainCard.setLayout(new BorderLayout(28, 0));
        mainCard.setBorder(new EmptyBorder(28, 30, 28, 30));
        mainCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 360));

        mainCard.add(createProfileLeftPanel(profile), BorderLayout.WEST);
        mainCard.add(createProfileInfoPanel(profile), BorderLayout.CENTER);
        mainCard.add(createProfileLogoPanel(), BorderLayout.EAST);

        page.add(mainCard);
        page.add(Box.createVerticalStrut(20));

        JPanel bottom = new JPanel(new GridLayout(1, 3, 16, 0));
        bottom.setOpaque(false);
        bottom.setAlignmentX(Component.LEFT_ALIGNMENT);
        bottom.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        bottom.add(createProfileQuickCard(
                "Role Aktif",
                displayRole(),
                "Hak akses yang sedang digunakan pada sistem."));

        bottom.add(createProfileQuickCard(
                "Username",
                safeOrDash(profile.getUsername()),
                "Digunakan untuk login ke aplikasi perpustakaan."));

        bottom.add(createProfileQuickCard(
                "Status",
                "Aktif",
                "Akun siap digunakan untuk mengakses fitur."));

        page.add(bottom);
        page.add(Box.createVerticalStrut(22));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton back = createNeutralButton("Kembali");
        JButton dashboard = createActionButton("Ke Dashboard");

        back.addActionListener(e -> showDashboard());
        dashboard.addActionListener(e -> showDashboard());

        actions.add(back);
        actions.add(dashboard);

        page.add(actions);

        contentPanel.add(page);
        refreshContent();
    }

    private JPanel createProfileLeftPanel(com.mycompany.perpustakaan.api.UserSummary profile) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(240, 300));

        JLabel avatar = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gp = new GradientPaint(
                        0, 0, ACCENT,
                        getWidth(), getHeight(), ACCENT_DARK);
                g2d.setPaint(gp);
                g2d.fillOval(0, 0, getWidth() - 1, getHeight() - 1);

                g2d.setColor(new Color(255, 255, 255, 70));
                g2d.setStroke(new BasicStroke(3f));
                g2d.drawOval(7, 7, getWidth() - 15, getHeight() - 15);

                g2d.dispose();
                super.paintComponent(g);
            }
        };

        avatar.setOpaque(false);
        avatar.setPreferredSize(new Dimension(112, 112));
        avatar.setMaximumSize(new Dimension(112, 112));
        avatar.setMinimumSize(new Dimension(112, 112));
        avatar.setHorizontalAlignment(SwingConstants.CENTER);

        String initial = getInitial(profile.getNama());
        avatar.setText(initial);
        avatar.setForeground(WHITE);
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 38));
        avatar.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel name = new JLabel(safeOrDash(profile.getNama()));
        name.setFont(new Font("Segoe UI", Font.BOLD, 20));
        name.setForeground(TEXT_DARK);
        name.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel username = new JLabel("@" + safeOrDash(profile.getUsername()));
        username.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        username.setForeground(TEXT_GRAY);
        username.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel roleBadge = new JLabel(displayRole());
        roleBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        roleBadge.setForeground(ACCENT_DARK);
        roleBadge.setOpaque(true);
        roleBadge.setBackground(ACCENT_SOFT);
        roleBadge.setBorder(new EmptyBorder(7, 14, 7, 14));
        roleBadge.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(Box.createVerticalGlue());
        panel.add(avatar);
        panel.add(Box.createVerticalStrut(16));
        panel.add(name);
        panel.add(Box.createVerticalStrut(4));
        panel.add(username);
        panel.add(Box.createVerticalStrut(14));
        panel.add(roleBadge);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private JPanel createProfileInfoPanel(com.mycompany.perpustakaan.api.UserSummary profile) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(10, 8, 10, 8));

        JLabel sectionTitle = new JLabel("Informasi Akun");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        sectionTitle.setForeground(TEXT_DARK);
        sectionTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sectionDesc = new JLabel("Data utama pengguna yang sedang login.");
        sectionDesc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sectionDesc.setForeground(TEXT_GRAY);
        sectionDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(sectionTitle);
        panel.add(Box.createVerticalStrut(4));
        panel.add(sectionDesc);
        panel.add(Box.createVerticalStrut(20));

        JPanel grid = new JPanel(new GridLayout(2, 2, 14, 14));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        grid.setMaximumSize(new Dimension(560, 170));

        grid.add(createProfileInfoItem("Nama Lengkap", safeOrDash(profile.getNama())));
        grid.add(createProfileInfoItem("Username", safeOrDash(profile.getUsername())));
        grid.add(createProfileInfoItem("Role", displayRole()));
        grid.add(createProfileInfoItem("Akses", isStaffOrAdmin() ? "Management Access" : "Member Access"));

        panel.add(grid);
        panel.add(Box.createVerticalStrut(18));

        JLabel note = new JLabel("<html><div style='width:520px;'>"
                + "Username, role, dan status akun tetap dikunci agar identitas login tidak berubah sembarangan."
                + "</div></html>");
        note.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        note.setForeground(TEXT_GRAY);
        note.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(note);
        panel.add(Box.createVerticalStrut(14));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton editProfile = createActionButton("Edit Profil");
        JButton changePassword = createNeutralButton("Ganti Password");
        editProfile.addActionListener(e -> showEditProfileDialog(profile));
        changePassword.addActionListener(e -> showChangePasswordDialog());

        actions.add(editProfile);
        actions.add(changePassword);
        panel.add(actions);

        return panel;
    }

    private void showEditProfileDialog(com.mycompany.perpustakaan.api.UserSummary profile) {
        JTextField nama = createField(safe(profile.getNama()));
        JTextField email = createField(safe(profile.getEmail()));
        nama.setPreferredSize(new Dimension(260, 34));
        email.setPreferredSize(new Dimension(260, 34));

        JPanel form = formPanel(new String[] { "Nama Lengkap", "Email" }, new JTextField[] { nama, email });
        int result = JOptionPane.showConfirmDialog(
                this,
                form,
                "Edit Profil",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            com.mycompany.perpustakaan.api.ProfileRequest request =
                    new com.mycompany.perpustakaan.api.ProfileRequest(nama.getText().trim(), blankToNull(email.getText()));
            com.mycompany.perpustakaan.api.MemberResponse response = libraryApi.updateCurrentProfile(request);
            showResponse(response.isSuccess(), response.getMessage());
            if (response.isSuccess()) {
                loadUserData();
                showProfile();
            }
        } catch (SQLException exception) {
            showError("Gagal update profil", exception);
        }
    }

    private void showChangePasswordDialog() {
        javax.swing.JPasswordField oldPassword = new javax.swing.JPasswordField();
        javax.swing.JPasswordField newPassword = new javax.swing.JPasswordField();
        javax.swing.JPasswordField confirmPassword = new javax.swing.JPasswordField();
        oldPassword.setPreferredSize(new Dimension(260, 34));
        newPassword.setPreferredSize(new Dimension(260, 34));
        confirmPassword.setPreferredSize(new Dimension(260, 34));

        JPanel form = new JPanel(new GridLayout(3, 2, 10, 8));
        form.setBorder(new EmptyBorder(8, 8, 8, 8));
        form.setOpaque(false);
        form.add(new JLabel("Password Lama"));
        form.add(oldPassword);
        form.add(new JLabel("Password Baru"));
        form.add(newPassword);
        form.add(new JLabel("Konfirmasi"));
        form.add(confirmPassword);

        int result = JOptionPane.showConfirmDialog(
                this,
                form,
                "Ganti Password",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String oldValue = new String(oldPassword.getPassword());
        String newValue = new String(newPassword.getPassword());
        String confirmValue = new String(confirmPassword.getPassword());

        if (oldValue.isBlank() || newValue.isBlank() || confirmValue.isBlank()) {
            JOptionPane.showMessageDialog(this, "Semua field password wajib diisi.", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!newValue.equals(confirmValue)) {
            JOptionPane.showMessageDialog(this, "Konfirmasi password baru tidak cocok.", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            com.mycompany.perpustakaan.api.MemberResponse response =
                    libraryApi.changeCurrentPassword(oldValue, newValue);
            showResponse(response.isSuccess(), response.getMessage());
        } catch (SQLException exception) {
            showError("Gagal mengganti password", exception);
        }
    }

    private JPanel createProfileInfoItem(String labelText, String valueText) {
        JPanel item = new RoundedPanel(18, SURFACE_ALT, new Color(238, 238, 238), 1f);
        item.setLayout(new BorderLayout(0, 5));
        item.setBorder(new EmptyBorder(14, 16, 14, 16));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(TEXT_GRAY);

        JLabel value = new JLabel(safeOrDash(valueText));
        value.setFont(new Font("Segoe UI", Font.BOLD, 15));
        value.setForeground(TEXT_DARK);

        item.add(label, BorderLayout.NORTH);
        item.add(value, BorderLayout.CENTER);

        return item;
    }

    private JPanel createProfileLogoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(230, 300));

        JPanel logoCard = new RoundedPanel(24, ACCENT_SOFT, new Color(255, 222, 210), 1f);
        logoCard.setLayout(new BoxLayout(logoCard, BoxLayout.Y_AXIS));
        logoCard.setBorder(new EmptyBorder(24, 24, 24, 24));

        JLabel logo = new JLabel();
        ImageIcon brandLogo = loadBrandLogoIcon(150, 110);
        if (brandLogo != null) {
            logo.setIcon(brandLogo);
        } else {
            logo.setText("LIBRARY HUB");
            logo.setFont(new Font("Segoe UI", Font.BOLD, 18));
            logo.setForeground(ACCENT_DARK);
        }

        logo.setHorizontalAlignment(SwingConstants.CENTER);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel appName = new JLabel("Library Hub");
        appName.setFont(new Font("Segoe UI", Font.BOLD, 18));
        appName.setForeground(TEXT_DARK);
        appName.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel desc = new JLabel("<html><div style='text-align:center; width:160px;'>"
                + "Sistem perpustakaan digital yang simple, rapi, dan mudah digunakan."
                + "</div></html>");
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        desc.setForeground(TEXT_GRAY);
        desc.setAlignmentX(Component.CENTER_ALIGNMENT);

        logoCard.add(Box.createVerticalGlue());
        logoCard.add(logo);
        logoCard.add(Box.createVerticalStrut(18));
        logoCard.add(appName);
        logoCard.add(Box.createVerticalStrut(8));
        logoCard.add(desc);
        logoCard.add(Box.createVerticalGlue());

        panel.add(logoCard, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createProfileQuickCard(String title, String value, String description) {
        JPanel card = new RoundedPanel(20, WHITE, new Color(235, 235, 235), 1f);
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(18, 20, 18, 20));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(TEXT_GRAY);

        JLabel valueLabel = new JLabel(safeOrDash(value));
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valueLabel.setForeground(TEXT_DARK);

        JLabel descLabel = new JLabel("<html><div style='width:260px;'>"
                + escapeHtml(description)
                + "</div></html>");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(TEXT_GRAY);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(descLabel, BorderLayout.SOUTH);

        return card;
    }

    private String getInitial(String name) {
        if (name == null || name.isBlank()) {
            return "U";
        }

        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, 1).toUpperCase();
        }

        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }

    private void showBookshelf() {
        resetContent();
        addTitle("Bookshelf");

        addDashboardHero(
                "Jelajahi Koleksi Buku",
                "Cari buku favorit kamu berdasarkan judul, penulis, kategori, dan ketersediaan stok.");

        searchField = createModernSearchField("Cari judul / penulis / kode buku...");
        categoryFilter = new JComboBox<>(loadCategoryOptions());
        categoryFilter.setPreferredSize(new Dimension(180, 38));
        categoryFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JPanel resultHolder = createDynamicContentPanel();

        final int[] currentPage = { 1 };
        final int pageSize = 12;

        final Runnable[] render = new Runnable[1];

        render[0] = () -> {
            resultHolder.removeAll();

            try {
                String keyword = searchText(searchField, "Cari judul / penulis / kode buku...");

                Object selectedCategory = categoryFilter.getSelectedItem();

                String categoryValue = selectedCategory == null || "Semua kategori".equals(selectedCategory.toString())
                        ? ""
                        : selectedCategory.toString();

                com.mycompany.perpustakaan.api.BookshelfPage page = libraryApi.getBookshelfPage(keyword, categoryValue,
                        currentPage[0], pageSize);

                resultHolder.add(createBookshelfGrid(page.getBooks()));
                resultHolder.add(createPaginationFooter(page, currentPage, pageSize, render[0]));

                refreshContent();

            } catch (SQLException e) {
                showError("Gagal memuat bookshelf", e);
            }
        };

        JPanel toolbar = createBookshelfToolbar(() -> {
            currentPage[0] = 1;
            render[0].run();
        });

        contentPanel.add(toolbar);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(resultHolder);

        searchField.addActionListener(e -> {
            currentPage[0] = 1;
            render[0].run();
        });

        categoryFilter.addActionListener(e -> {
            currentPage[0] = 1;
            render[0].run();
        });

        render[0].run();
        refreshContent();
    }

    private JPanel createBookshelfToolbar(Runnable render) {
        JPanel toolbar = new RoundedPanel(24, WHITE, CARD_BORDER, 1f);
        toolbar.setLayout(new BorderLayout(16, 0));
        toolbar.setBorder(new EmptyBorder(16, 18, 16, 18));
        toolbar.setAlignmentX(Component.LEFT_ALIGNMENT);
        toolbar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 76));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        JButton searchButton = createActionButton("Cari");
        JButton refreshButton = createNeutralButton("Refresh");

        searchButton.addActionListener(e -> render.run());
        refreshButton.addActionListener(e -> {
            searchField.setText("Cari judul / penulis / kode buku...");
            searchField.setForeground(TEXT_GRAY);
            render.run();
        });

        left.add(searchField);
        left.add(searchButton);
        left.add(refreshButton);

        JLabel badge = new JLabel("BOOK COLLECTION");
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setForeground(ACCENT_DARK);
        badge.setOpaque(true);
        badge.setBackground(ACCENT_SOFT);
        badge.setBorder(new EmptyBorder(8, 14, 8, 14));

        toolbar.add(left, BorderLayout.WEST);
        toolbar.add(badge, BorderLayout.EAST);

        return toolbar;
    }

    private JPanel createBookshelfGrid(List<com.mycompany.perpustakaan.api.BookSummary> books) {
        JPanel wrapper = new RoundedPanel(28, WHITE, CARD_BORDER, 1f);
        wrapper.setLayout(new BorderLayout());
        wrapper.setBorder(new EmptyBorder(22, 24, 24, 24));
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 620));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Daftar Buku");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(TEXT_DARK);

        JLabel count = new JLabel((books == null ? 0 : books.size()) + " buku ditemukan");
        count.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        count.setForeground(TEXT_GRAY);

        header.add(title, BorderLayout.WEST);
        header.add(count, BorderLayout.EAST);

        JPanel grid = new JPanel(new GridLayout(0, 4, 18, 18));
        grid.setOpaque(false);
        grid.setBorder(new EmptyBorder(18, 0, 0, 0));

        if (books == null || books.isEmpty()) {
            grid.add(createEmptyBookCard());
        } else {
            for (com.mycompany.perpustakaan.api.BookSummary book : books) {
                grid.add(new ModernBookCard(book));
            }
        }

        JScrollPane scroll = new JScrollPane(grid);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setPreferredSize(new Dimension(1200, 500));

        wrapper.add(header, BorderLayout.NORTH);
        wrapper.add(scroll, BorderLayout.CENTER);

        return wrapper;
    }

    private JPanel createEmptyBookCard() {
        JPanel card = new RoundedPanel(24, ACCENT_LIGHT, new Color(255, 226, 214), 1f);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(24, 24, 24, 24));
        card.setPreferredSize(new Dimension(220, 260));

        JLabel title = new JLabel("Belum ada buku");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(TEXT_DARK);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel desc = new JLabel(
                "<html><div style='text-align:center; width:160px;'>Coba gunakan kata kunci pencarian lain.</div></html>");
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        desc.setForeground(TEXT_GRAY);
        desc.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(Box.createVerticalGlue());
        card.add(title);
        card.add(Box.createVerticalStrut(8));
        card.add(desc);
        card.add(Box.createVerticalGlue());

        return card;
    }

    private class ModernBookCard extends JPanel {
        ModernBookCard(com.mycompany.perpustakaan.api.BookSummary book) {
            boolean available = book != null && book.getStokTersedia() > 0;

            setLayout(new BorderLayout());
            setBackground(WHITE);
            setPreferredSize(new Dimension(220, 300));
            setBorder(new EmptyBorder(0, 0, 0, 0));
            setOpaque(false);
            setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

            JPanel card = new RoundedPanel(24, WHITE, CARD_BORDER, 1f);
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBorder(new EmptyBorder(14, 14, 14, 14));

            JPanel cover = new RoundedPanel(20, ACCENT_LIGHT, new Color(255, 226, 214), 1f);
            cover.setLayout(new BorderLayout());
            cover.setPreferredSize(new Dimension(190, 120));
            cover.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

            JLabel coverIcon = new JLabel();
            ImageIcon placeholder = loadBookCoverPlaceholder(130, 90, 28);
            if (placeholder != null) {
                coverIcon.setIcon(placeholder);
            } else {
                coverIcon.setText("BOOK");
                coverIcon.setFont(new Font("Segoe UI", Font.BOLD, 20));
                coverIcon.setForeground(ACCENT);
            }
            coverIcon.setHorizontalAlignment(SwingConstants.CENTER);
            cover.add(coverIcon, BorderLayout.CENTER);

            JLabel title = new JLabel("<html><div style='width:170px;'>"
                    + escapeHtml(truncate(book == null ? "Belum ada buku" : book.getJudul(), 42))
                    + "</div></html>");
            title.setFont(new Font("Segoe UI", Font.BOLD, 14));
            title.setForeground(TEXT_DARK);
            title.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel author = new JLabel(truncate(book == null ? "-" : book.getPenulis(), 26));
            author.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            author.setForeground(TEXT_GRAY);
            author.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel category = new JLabel(truncate(book == null ? "-" : safeOrDash(book.getKategori()), 24));
            category.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            category.setForeground(ACCENT_DARK);
            category.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel status = new JLabel(available ? "TERSEDIA" : "HABIS");
            status.setFont(new Font("Segoe UI", Font.BOLD, 10));
            status.setForeground(WHITE);
            status.setOpaque(true);
            status.setBackground(available ? GREEN_STATUS : RED_STATUS);
            status.setBorder(new EmptyBorder(6, 10, 6, 10));
            status.setAlignmentX(Component.LEFT_ALIGNMENT);

            JPanel bottom = new JPanel(new BorderLayout());
            bottom.setOpaque(false);
            bottom.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

            JLabel stock = new JLabel(
                    book == null ? "Stok: -" : "Stok: " + book.getStokTersedia() + "/" + book.getStokTotal());
            stock.setFont(new Font("Segoe UI", Font.BOLD, 12));
            stock.setForeground(TEXT_DARK);

            JLabel detail = new JLabel("Detail >");
            detail.setFont(new Font("Segoe UI", Font.BOLD, 12));
            detail.setForeground(ACCENT);

            bottom.add(stock, BorderLayout.WEST);
            bottom.add(detail, BorderLayout.EAST);

            MouseAdapter openDetail = new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (book != null) {
                        showBookDetailPage(book, false);
                    }
                }
            };

            card.add(cover);
            card.add(Box.createVerticalStrut(14));
            card.add(title);
            card.add(Box.createVerticalStrut(6));
            card.add(author);
            card.add(Box.createVerticalStrut(4));
            card.add(category);
            card.add(Box.createVerticalStrut(10));
            card.add(status);
            card.add(Box.createVerticalGlue());
            card.add(bottom);

            card.addMouseListener(openDetail);
            addMouseListener(openDetail);

            add(card, BorderLayout.CENTER);
        }
    }

    private JPanel createSearchBar() {
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        searchPanel.setOpaque(false);
        searchPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        String placeholder = "Cari judul atau penulis...";
        String initialText = currentKeyword.isBlank() ? placeholder : currentKeyword;

        // Search field dengan rounded background
        searchField = new JTextField(initialText) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(220, 220, 220)); // SEARCH_BG
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2d.dispose();
                super.paintComponent(g);
            }
        };

        searchField.setPreferredSize(new Dimension(400, 42));
        searchField.setFont(new Font("Segoe UI", currentKeyword.isBlank() ? Font.ITALIC : Font.PLAIN, 14));
        searchField.setForeground(currentKeyword.isBlank() ? TEXT_GRAY : TEXT_DARK);
        searchField.setOpaque(false);
        searchField.setBorder(new EmptyBorder(10, 40, 10, 20));

        // Icon search di dalam field
        JLabel searchIcon = new JLabel(loadIconResource("/assets/icons/icon-search.svg", 18, 18));
        searchIcon.setBounds(12, 10, 24, 24);

        JPanel searchWrapper = new JPanel(null);
        searchWrapper.setOpaque(false);
        searchWrapper.setPreferredSize(new Dimension(410, 48));
        searchWrapper.add(searchIcon);
        searchWrapper.add(searchField);
        searchField.setBounds(0, 0, 400, 42);

        searchField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (searchField.getText().equals(placeholder)) {
                    searchField.setText("");
                    searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                    searchField.setForeground(TEXT_DARK);
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (searchField.getText().isBlank()) {
                    searchField.setText(placeholder);
                    searchField.setFont(new Font("Segoe UI", Font.ITALIC, 14));
                    searchField.setForeground(TEXT_GRAY);
                }
            }
        });
        searchField.addActionListener(e -> performSearch());

        // Category dropdown
        categoryFilter = new JComboBox<>(loadCategoryOptions());
        categoryFilter.setPreferredSize(new Dimension(180, 42));
        categoryFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        categoryFilter.setSelectedItem(currentCategory);
        categoryFilter.setBorder(BorderFactory.createLineBorder(new Color(210, 210, 210), 1, true));
        categoryFilter.addActionListener(e -> {
            Object selected = categoryFilter.getSelectedItem();
            currentCategory = selected == null ? "Semua kategori" : selected.toString();
            performSearch();
        });

        // Search button
        JButton searchButton = new JButton("Cari") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(0, 0, 0, 20));
                g2d.fillRoundRect(2, 2, getWidth() - 2, getHeight() - 2, 10, 10);

                if (getModel().isPressed()) {
                    g2d.setColor(ACCENT.darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(ACCENT.brighter());
                } else {
                    g2d.setColor(ACCENT);
                }
                g2d.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 10, 10);

                g2d.setColor(WHITE);
                g2d.setFont(getFont());
                java.awt.FontMetrics fm = g2d.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(getText())) / 2;
                int textY = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2d.drawString(getText(), textX, textY);
                g2d.dispose();
            }
        };
        searchButton.setPreferredSize(new Dimension(80, 42));
        searchButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        searchButton.setForeground(WHITE);
        searchButton.setFocusPainted(false);
        searchButton.setBorderPainted(false);
        searchButton.setContentAreaFilled(false);
        searchButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        searchButton.addActionListener(e -> performSearch());

        searchPanel.add(searchWrapper);
        searchPanel.add(categoryFilter);
        searchPanel.add(searchButton);
        return searchPanel;
    }

    private void performSearch() {
        String keyword = searchField.getText().trim();
        currentKeyword = keyword.equals("Cari judul atau penulis...") ? "" : keyword;
        Object selected = categoryFilter == null ? null : categoryFilter.getSelectedItem();
        currentCategory = selected == null ? "Semua kategori" : selected.toString();
        showBookshelf();
    }

    private String[] loadCategoryOptions() {
        java.util.ArrayList<String> options = new java.util.ArrayList<>();
        options.add("Semua kategori");
        try {
            for (String category : libraryApi.getBookCategories()) {
                if (category != null && !category.isBlank() && !options.contains(category)) {
                    options.add(category);
                }
            }
        } catch (SQLException exception) {
            logger.log(java.util.logging.Level.WARNING, "Gagal memuat kategori", exception);
        }
        return options.toArray(new String[0]);
    }

    private JPanel createSearchResultSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Title tanpa arrow
        JLabel titleLabel = new JLabel("HASIL PENCARIAN");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(ACCENT);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(titleLabel);
        section.add(Box.createVerticalStrut(15));

        // Grid cards
        JPanel grid = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);

        try {
            String safeKeyword = currentKeyword.isBlank() ? null : currentKeyword;
            String safeCategory = "Semua kategori".equals(currentCategory) ? null : currentCategory;

            com.mycompany.perpustakaan.api.BookshelfPage page = libraryApi.getBookshelfPage(safeKeyword, safeCategory,
                    1, 20);

            List<com.mycompany.perpustakaan.api.BookSummary> books = page.getBooks();

            if (books.isEmpty()) {
                JLabel empty = new JLabel("Tidak ada buku yang ditemukan.");
                empty.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                empty.setForeground(TEXT_GRAY);
                grid.add(empty);
            } else {
                for (com.mycompany.perpustakaan.api.BookSummary book : books) {
                    grid.add(new BookCard(book));
                }
            }
        } catch (SQLException e) {
            logger.log(java.util.logging.Level.WARNING, "Gagal memuat hasil pencarian", e);
            JLabel error = new JLabel("Gagal memuat data. Periksa koneksi.");
            error.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            error.setForeground(RED_STATUS);
            grid.add(error);
        }

        section.add(grid);
        return section;
    }

    private JPanel createCategorySection(String category) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Title dengan arrow
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titlePanel.setOpaque(false);
        titlePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLabel = new JLabel(category.toUpperCase());
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

        try {
            com.mycompany.perpustakaan.api.BookshelfPage page = libraryApi.getBookshelfPage(null, category, 1, 8);

            List<com.mycompany.perpustakaan.api.BookSummary> books = page.getBooks();

            if (books.isEmpty()) {
                scrollPanel.addCard(null);
            } else {
                for (com.mycompany.perpustakaan.api.BookSummary book : books) {
                    scrollPanel.addCard(book);
                }
            }
        } catch (SQLException e) {
            logger.log(java.util.logging.Level.WARNING, "Gagal memuat kategori " + category, e);
            scrollPanel.addCard(null);
        }

        section.add(scrollPanel);
        return section;
    }

    private Integer selectedId(JTable table) {
        int selected = table.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(this, "Pilih baris dulu.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        Object value = table.getValueAt(selected, 0);
        try {
            return Integer.valueOf(String.valueOf(value));
        } catch (NumberFormatException exception) {
            JOptionPane.showMessageDialog(this, "Data ini bukan baris yang bisa diproses.", "Info",
                    JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
    }

    private String selectedString(JTable table, int column) {
        int selected = table.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(this, "Pilih baris dulu.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        Object value = table.getValueAt(selected, column);
        if (value == null || String.valueOf(value).trim().isEmpty() || "-".equals(String.valueOf(value))) {
            JOptionPane.showMessageDialog(this, "Data ini bukan baris yang bisa diproses.", "Info",
                    JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        return String.valueOf(value).trim();
    }

    private Object invokeApi(String methodName, Class<?>[] parameterTypes, Object... args) throws SQLException {
        try {
            return libraryApi.getClass().getMethod(methodName, parameterTypes).invoke(libraryApi, args);

        } catch (NoSuchMethodException missing) {
            return null;

        } catch (java.lang.reflect.InvocationTargetException wrapped) {
            Throwable cause = wrapped.getCause();

            if (cause instanceof SQLException) {
                throw (SQLException) cause;
            }

            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }

            throw new IllegalStateException(cause);

        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> extractList(Object pageOrList, String... getters) {
        if (pageOrList == null) {
            return null;
        }

        if (pageOrList instanceof List<?>) {
            return (List<T>) pageOrList;
        }

        for (String getter : getters) {
            try {
                Object value = pageOrList.getClass().getMethod(getter).invoke(pageOrList);

                if (value instanceof List<?>) {
                    return (List<T>) value;
                }

            } catch (ReflectiveOperationException ignored) {
                // coba getter berikutnya
            }
        }

        return null;
    }

    private int pageInt(Object page, String getter, int fallback) {
        if (page == null) {
            return fallback;
        }

        try {
            Object value = page.getClass().getMethod(getter).invoke(page);
            return value == null ? fallback : Integer.parseInt(String.valueOf(value));

        } catch (ReflectiveOperationException | NumberFormatException ignored) {
            return fallback;
        }
    }

    private boolean pageBool(Object page, String getter, boolean fallback) {
        if (page == null) {
            return fallback;
        }

        try {
            Object value = page.getClass().getMethod(getter).invoke(page);
            return value == null ? fallback : Boolean.parseBoolean(String.valueOf(value));

        } catch (ReflectiveOperationException ignored) {
            return fallback;
        }
    }

    private JPanel createPaginationFooter(Object page, int[] currentPage, int pageSize, Runnable render) {
        JPanel footer = createToolbarPanel();

        JButton prev = createNeutralButton("Prev");
        JButton next = createNeutralButton("Next");

        int pageNumber = pageInt(page, "getPage", currentPage[0]);
        int totalPages = pageInt(page, "getTotalPages", 0);
        int totalItems = pageInt(page, "getTotalItems", 0);

        if (totalPages <= 0 && totalItems > 0) {
            totalPages = calculateTotalPages(totalItems, pageSize);
        }

        if (totalPages <= 0) {
            totalPages = currentPage[0];
        }

        boolean hasPrev = pageBool(page, "hasPreviousPage", currentPage[0] > 1);
        boolean hasNext = pageBool(page, "hasNextPage", totalPages > currentPage[0]);

        JLabel pageInfo = new JLabel(
                "Halaman " + pageNumber + " / " + Math.max(1, totalPages)
                        + (totalItems > 0 ? " - Total " + totalItems + " data" : ""));

        pageInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pageInfo.setForeground(TEXT_GRAY);

        prev.setEnabled(hasPrev);
        next.setEnabled(hasNext);

        prev.addActionListener(e -> {
            currentPage[0] = Math.max(1, currentPage[0] - 1);
            render.run();
        });

        next.addActionListener(e -> {
            currentPage[0]++;
            render.run();
        });

        footer.add(prev);
        footer.add(next);
        footer.add(pageInfo);

        return footer;
    }

    private <T> List<T> slice(List<T> rows, int page, int pageSize) {
        if (rows == null || rows.isEmpty()) {
            return rows;
        }

        int from = Math.max(0, (page - 1) * pageSize);

        if (from >= rows.size()) {
            return Collections.emptyList();
        }

        int to = Math.min(rows.size(), from + pageSize);

        return new ArrayList<>(rows.subList(from, to));
    }

    private List<com.mycompany.perpustakaan.api.InventoryReportRow> filterInventoryRows(
            List<com.mycompany.perpustakaan.api.InventoryReportRow> rows,
            String keyword) {
        if (rows == null || keyword == null || keyword.isBlank()) {
            return rows;
        }

        String q = keyword.toLowerCase();

        List<com.mycompany.perpustakaan.api.InventoryReportRow> filtered = new ArrayList<>();

        for (com.mycompany.perpustakaan.api.InventoryReportRow row : rows) {
            String haystack = (safeOrDash(row.getKodeBuku()) + " "
                    + safeOrDash(row.getJudul()) + " "
                    + safeOrDash(row.getPenulis()) + " "
                    + safeOrDash(row.getKategori())).toLowerCase();

            if (haystack.contains(q)) {
                filtered.add(row);
            }
        }

        return filtered;
    }

    private void createCategoryMaster() {
        String name = JOptionPane.showInputDialog(
                this,
                "Nama kategori baru:",
                "Tambah Kategori",
                JOptionPane.PLAIN_MESSAGE);

        if (name == null) {
            return;
        }

        name = name.trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama kategori wajib diisi", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Object response = invokeApi("createCategory", new Class<?>[] { String.class }, name);

            boolean success = response == null || pageBool(response, "isSuccess", true);

            String message = readMessage(
                    response,
                    success ? "Kategori berhasil ditambahkan" : "Gagal menambahkan kategori");

            showResponse(success, message);

            if (success) {
                showCategoryManagement();
            }

        } catch (SQLException e) {
            showError("Gagal menambahkan kategori", e);
        }
    }

    private String readMessage(Object response, String fallback) {
        if (response == null) {
            return fallback;
        }

        try {
            Object message = response.getClass().getMethod("getMessage").invoke(response);
            return message == null ? fallback : String.valueOf(message);

        } catch (ReflectiveOperationException ignored) {
            return fallback;
        }
    }

    private void exportVisit(String format, String keyword, String status) {
        String dir = chooseDirectory();

        if (dir == null) {
            return;
        }

        try {
            Object response = invokeApi(
                    "exportVisitReport",
                    new Class<?>[] { String.class, String.class, String.class, String.class },
                    format,
                    dir,
                    keyword,
                    status);

            if (response == null) {
                response = invokeApi(
                        "exportVisitReport",
                        new Class<?>[] { String.class, String.class },
                        format,
                        dir);
            }

            if (response == null) {
                JOptionPane.showMessageDialog(
                        this,
                        "Method exportVisitReport belum tersedia / cocok di LibraryApi FE.",
                        "Export Kunjungan",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            boolean success = pageBool(response, "isSuccess", true);
            String filePath = readMessageProperty(response, "getFilePath");

            showResponse(
                    success,
                    readMessage(response, success ? "Export kunjungan berhasil" : "Export kunjungan gagal")
                            + (filePath == null ? "" : "\n" + filePath));

        } catch (SQLException e) {
            showError("Gagal export laporan kunjungan", e);
        }
    }

    private String readMessageProperty(Object response, String getter) {
        if (response == null) {
            return null;
        }

        try {
            Object value = response.getClass().getMethod(getter).invoke(response);
            return value == null ? null : String.valueOf(value);

        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private String chooseDirectory() {
        JFileChooser chooser = new JFileChooser(new File(System.getProperty("user.home"), "Documents"));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int result = chooser.showSaveDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile().getAbsolutePath();
        }

        return null;
    }

    private int calculateTotalPages(int totalItems, int pageSize) {
        if (totalItems <= 0 || pageSize <= 0) {
            return 0;
        }

        return (int) Math.ceil((double) totalItems / pageSize);
    }

    private LocalDate parseDate(String value) {
        return LocalDate.parse(value.trim());
    }

    private String truncate(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        return text.length() > maxLength ? text.substring(0, maxLength - 2) + ".." : text;
    }

    private boolean isAdmin() {
        return "admin".equalsIgnoreCase(currentRole);
    }

    private boolean isStaffOrAdmin() {
        return isAdmin() || "staff".equalsIgnoreCase(currentRole);
    }

    private boolean isMember() {
        return !isStaffOrAdmin();
    }

    private boolean requireAdminView() {
        if (isAdmin()) {
            return true;
        }
        showAccessDenied("Fitur ini khusus admin.");
        return false;
    }

    private boolean requireStaffOrAdminView() {
        if (isStaffOrAdmin()) {
            return true;
        }
        showAccessDenied("Fitur ini khusus staff atau admin.");
        return false;
    }

    private boolean requireMemberView() {
        if (isMember()) {
            return true;
        }
        showAccessDenied("Fitur ini khusus anggota.");
        return false;
    }

    private void showAccessDenied(String message) {
        JOptionPane.showMessageDialog(this, message, "Akses Ditolak", JOptionPane.WARNING_MESSAGE);
        showDashboard();
    }

    private String roleTitle() {
        if (isAdmin()) {
            return "Menu Admin";
        }
        if (isStaffOrAdmin()) {
            return "Menu Staff";
        }
        return "Menu Anggota";
    }

    private String displayRole() {
        if (isAdmin()) {
            return "ADMIN";
        }
        if (isStaffOrAdmin()) {
            return "STAFF";
        }
        return "ANGGOTA";
    }

    private String formatMoney(BigDecimal value) {
        if (value == null) {
            return "Rp 0";
        }
        return "Rp " + value.toPlainString();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String safeOrDash(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value;
    }

    private String blankToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private void showResponse(boolean success, String message) {
        JOptionPane.showMessageDialog(this, message, success ? "Berhasil" : "Gagal",
                success ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String message, Exception exception) {
        JOptionPane.showMessageDialog(this, message + ": " + exception.getMessage(), "Error",
                JOptionPane.ERROR_MESSAGE);
        logger.log(java.util.logging.Level.SEVERE, message, exception);
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> new Dashboard().setVisible(true));
    }
}
