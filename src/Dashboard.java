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
import java.util.Collections;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class Dashboard extends JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Dashboard.class.getName());
    private final com.mycompany.perpustakaan.api.LibraryApi libraryApi;

    private static final Color BG_APP = new Color(245, 245, 245);
    private static final Color WHITE = Color.WHITE;
    private static final Color ACCENT = new Color(232, 130, 90);
    private static final Color ACCENT_DARK = new Color(196, 149, 94);
    private static final Color TEXT_DARK = new Color(50, 50, 50);
    private static final Color TEXT_GRAY = new Color(120, 120, 120);
    private static final Color BORDER_COLOR = new Color(224, 224, 224);
    private static final Color GREEN_STATUS = new Color(0, 200, 83);
    private static final Color RED_STATUS = new Color(255, 23, 68);

    private static final Dimension FRAME_SIZE = new Dimension(1280, 720);
    private static final Dimension SIDEBAR_SIZE = new Dimension(220, 720);
    private static final Dimension HEADER_SIZE = new Dimension(1060, 80);
    private static final Dimension CARD_SIZE = new Dimension(160, 240);

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
        contentPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

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
            setBackground(WHITE);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR));

            JLabel logo = new JLabel();
            try {
                logo.setIcon(new ImageIcon(getClass().getResource("/logo3.png")));
            } catch (Exception e) {
                logo.setText("LIBRARY HUB");
                logo.setFont(new Font("Segoe UI", Font.BOLD, 14));
                logo.setForeground(new Color(139, 90, 43));
            }
            logo.setAlignmentX(Component.CENTER_ALIGNMENT);
            logo.setBorder(new EmptyBorder(20, 0, 10, 0));
            add(logo);

            JLabel menuTitle = new JLabel(roleTitle());
            menuTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
            menuTitle.setForeground(TEXT_DARK);
            menuTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
            menuTitle.setBorder(new EmptyBorder(10, 0, 10, 0));
            add(menuTitle);
            add(Box.createVerticalStrut(10));

            addMenuButton("Dashboard", "/icon-dashboard.png", e -> showDashboard());

            if (isAdmin()) {
                addMenuButton("Admin Report", "/Control Panel.svg", e -> showAdminDashboard());
                addMenuButton("Inventory", "/Book.svg", e -> showInventoryReport());
                addMenuButton("Loan Report", "/Homework.svg", e -> showLoanReport());
                addMenuButton("Buku Populer", "/icon-history.png", e -> showPopularBookReport());
                addMenuButton("Tambah Buku", "/icon-loan.png", e -> showAddBookDialog());
            }

            if (isStaffOrAdmin()) {
                addMenuButton("Manajemen Buku", "/icon-bookshelf.png", e -> showBookManagement());
                addMenuButton("Loans & Returns", "/icon-loan.png", e -> showLoanManagement());
                addMenuButton("Members", "/icon-member.svg", e -> showMemberManagement());
            } else {
                addMenuButton("Bookshelf", "/icon-bookshelf.png", e -> showBookshelf());
                addMenuButton("Pinjam Buku", "/icon-loan.png", e -> showRequestLoan());
                addMenuButton("Pinjaman Aktif", "/Homework.svg", e -> showCurrentLoans());
                addMenuButton("History", "/icon-history.png", e -> showUserHistory());
                addMenuButton("Kunjungan", "/Time Machine.svg", e -> showVisitForm());
            }

            addMenuButton("User Profile", "/icon-profile.png", e -> showProfile());
            add(Box.createVerticalGlue());
            add(createLogoutButton());
            add(Box.createVerticalStrut(18));
        }

        private void addMenuButton(String text, String icon, java.awt.event.ActionListener listener) {
            SidebarButton button = new SidebarButton(text, icon);
            button.addActionListener(e -> {
                setActive(button);
                listener.actionPerformed(e);
            });
            add(button);
            add(Box.createVerticalStrut(8));
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
            setBackground(WHITE);
            setHorizontalAlignment(SwingConstants.LEFT);
            setIconTextGap(12);
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

    private class HeaderPanel extends JPanel {
        HeaderPanel() {
            setPreferredSize(HEADER_SIZE);
            setBackground(WHITE);
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                    new EmptyBorder(15, 25, 15, 25)
            ));

            JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
            left.setOpaque(false);

            JLabel avatar = new JLabel();
            try {
                avatar.setIcon(new ImageIcon(getClass().getResource("/PROFILE2.png")));
            } catch (Exception e) {
                avatar.setText("USER");
                avatar.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            }
            avatar.setPreferredSize(new Dimension(50, 50));
            avatar.setHorizontalAlignment(SwingConstants.CENTER);

            JPanel textPanel = new JPanel();
            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
            textPanel.setOpaque(false);

            welcomeLabel = new JLabel("WELCOME, USER");
            welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            welcomeLabel.setForeground(TEXT_DARK);

            roleLabel = new JLabel(currentRole.toUpperCase());
            roleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            roleLabel.setForeground(ACCENT);

            textPanel.add(welcomeLabel);
            textPanel.add(roleLabel);

            left.add(avatar);
            left.add(textPanel);

            JButton notif = createNotificationButton();

            add(left, BorderLayout.WEST);
            add(notif, BorderLayout.EAST);
        }
    }

    private void showDashboard() {
        resetContent();
        if (isAdmin()) {
            addTitle("Dashboard Admin");
            addAdminSummaryCards();
            addQuickActions(
                    new String[]{"Admin Report", "Inventory", "Loan Report", "Buku Populer", "Tambah Buku", "Manajemen Buku", "Loans & Returns", "Members"},
                    new Runnable[]{this::showAdminDashboard, this::showInventoryReport, this::showLoanReport, this::showPopularBookReport, this::showAddBookDialog, this::showBookManagement, this::showLoanManagement, this::showMemberManagement}
            );
        } else if (isStaffOrAdmin()) {
            addTitle("Dashboard Staff");
            addQuickActions(new String[]{"Tambah Buku", "Manajemen Buku", "Loans & Returns", "Members"},
                    new Runnable[]{this::showAddBookDialog, this::showBookManagement, this::showLoanManagement, this::showMemberManagement});
        } else {
            addTitle("Dashboard Anggota");
            addUserSummaryCards();
            contentPanel.add(createSearchBar());
            contentPanel.add(Box.createVerticalStrut(18));
            addQuickActions(new String[]{"Bookshelf", "Request Pinjam", "Pinjaman Aktif", "Tambah Kunjungan"},
                    new Runnable[]{
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
        addTitle("Dashboard Admin / Report");
        addAdminSummaryCards();
        addQuickActions(new String[]{"Export Inventory PDF", "Export Inventory XLSX", "Export Loan PDF", "Export Loan XLSX"},
                new Runnable[]{
                    () -> exportInventory("pdf"),
                    () -> exportInventory("xlsx"),
                    () -> exportLoan("pdf"),
                    () -> exportLoan("xlsx")
                });
        refreshContent();
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

    private void showInventoryReport() {
        if (!requireAdminView()) {
            return;
        }
        resetContent();
        addTitle("Laporan Inventory");
        addQuickActions(new String[]{"Export PDF", "Export XLSX", "Tambah Buku"},
                new Runnable[]{() -> exportInventory("pdf"), () -> exportInventory("xlsx"), this::showAddBookDialog});
        try {
            DefaultTableModel model = new DefaultTableModel(new Object[]{"ID", "Kode", "Judul", "Penulis", "Kategori", "Stok", "Status"}, 0);
            for (com.mycompany.perpustakaan.api.InventoryReportRow row : libraryApi.getInventoryReport()) {
                model.addRow(new Object[]{
                    row.getIdBuku(),
                    row.getKodeBuku(),
                    row.getJudul(),
                    row.getPenulis(),
                    row.getKategori(),
                    row.getStokTersedia() + "/" + row.getStokTotal(),
                    row.getStatusKetersediaan()
                });
            }
            contentPanel.add(createTablePanel(model, 560));
        } catch (SQLException e) {
            showError("Gagal memuat laporan inventory", e);
        }
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
                DefaultTableModel model = new DefaultTableModel(new Object[]{"ID", "User", "Buku", "Pinjam", "Jatuh Tempo", "Kembali", "Status", "Denda"}, 0);
                for (com.mycompany.perpustakaan.api.LoanReportRow row : libraryApi.getLoanReport(startDate, endDate)) {
                    model.addRow(new Object[]{
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
        try {
            DefaultTableModel model = new DefaultTableModel(new Object[]{"ID", "Kode", "Judul", "Penulis", "Kategori", "Dipinjam", "Stok"}, 0);
            for (com.mycompany.perpustakaan.api.PopularBookReportRow row : libraryApi.getPopularBookReport(25)) {
                model.addRow(new Object[]{
                    row.getIdBuku(),
                    row.getKodeBuku(),
                    row.getJudul(),
                    row.getPenulis(),
                    row.getKategori(),
                    row.getTotalDipinjam(),
                    row.getStokTersedia() + "/" + row.getStokTotal()
                });
            }
            contentPanel.add(createTablePanel(model, 560));
        } catch (SQLException e) {
            showError("Gagal memuat laporan buku populer", e);
        }
        refreshContent();
    }

    private void showBookManagement() {
        if (!requireStaffOrAdminView()) {
            return;
        }
        resetContent();
        addTitle("Manajemen Buku");
        addQuickActions(new String[]{"Tambah Buku", "Refresh"}, new Runnable[]{this::showAddBookDialog, this::showBookManagement});
        try {
            com.mycompany.perpustakaan.api.BookshelfPage page = libraryApi.getBookshelfPage("", "", 1, 50);
            DefaultTableModel model = new DefaultTableModel(new Object[]{"ID", "Kode", "Judul", "Penulis", "Kategori", "Stok", "Status"}, 0);
            for (com.mycompany.perpustakaan.api.BookSummary book : page.getBooks()) {
                model.addRow(new Object[]{
                    book.getIdBuku(),
                    book.getKodeBuku(),
                    book.getJudul(),
                    book.getPenulis(),
                    book.getKategori(),
                    book.getStokTersedia() + "/" + book.getStokTotal(),
                    book.getStatusKetersediaan()
                });
            }
            JTable table = createTable(model);
            contentPanel.add(wrapTable(table, 520));

            JPanel actions = createToolbarPanel();
            JButton detail = createActionButton("Detail");
            JButton update = createActionButton("Update");
            JButton stock = createActionButton("Update Stok");
            JButton delete = createActionButton("Hapus");
            actions.add(detail);
            actions.add(update);
            actions.add(stock);
            actions.add(delete);
            contentPanel.add(actions);

            detail.addActionListener(e -> showSelectedBookDetail(table));
            update.addActionListener(e -> updateSelectedBook(table));
            stock.addActionListener(e -> updateSelectedStock(table));
            delete.addActionListener(e -> deleteSelectedBook(table));
        } catch (SQLException e) {
            showError("Gagal memuat manajemen buku", e);
        }
        refreshContent();
    }

    private void showLoanManagement() {
        if (!requireStaffOrAdminView()) {
            return;
        }
        resetContent();
        addTitle("Loans & Returns");
        JPanel actions = createToolbarPanel();
        JButton create = createActionButton("Buat Peminjaman");
        JComboBox<String> status = new JComboBox<>(new String[]{"semua", "aktif", "dipinjam", "terlambat", "dikembalikan"});
        JButton load = createActionButton("Tampilkan");
        actions.add(create);
        actions.add(new JLabel("Status"));
        actions.add(status);
        actions.add(load);
        contentPanel.add(actions);
        contentPanel.add(Box.createVerticalStrut(15));

        JPanel tableHolder = createDynamicContentPanel();
        contentPanel.add(tableHolder);

        Runnable render = () -> {
            tableHolder.removeAll();
            try {
                com.mycompany.perpustakaan.api.LoanManagementPage page = libraryApi.getLoansForManagement((String) status.getSelectedItem(), 1, 50);
                DefaultTableModel model = new DefaultTableModel(new Object[]{"ID", "Buku", "Pinjam", "Jatuh Tempo", "Kembali", "Status", "Denda"}, 0);
                for (com.mycompany.perpustakaan.api.LoanSummary loan : page.getLoans()) {
                    model.addRow(new Object[]{
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
                footer.add(returnButton);
                panel.add(footer);
                tableHolder.add(panel);
                refreshContent();
            } catch (SQLException e) {
                showError("Gagal memuat loans & returns", e);
            }
        };
        create.addActionListener(e -> showCreateLoanDialog());
        load.addActionListener(e -> render.run());
        render.run();
    }

    private void showMemberManagement() {
        if (!requireStaffOrAdminView()) {
            return;
        }
        resetContent();
        addTitle("Member Management");
        addQuickActions(new String[]{"Tambah Anggota", "Refresh"}, new Runnable[]{this::showAddMemberDialog, this::showMemberManagement});
        try {
            com.mycompany.perpustakaan.api.MemberPage page = libraryApi.searchMembers("", "semua", 1, 50);
            DefaultTableModel model = new DefaultTableModel(new Object[]{"ID", "Nama", "Username", "Email", "Status"}, 0);
            for (com.mycompany.perpustakaan.api.MemberSummary member : page.getMembers()) {
                model.addRow(new Object[]{
                    member.getIdUser(),
                    member.getNama(),
                    member.getUsername(),
                    member.getEmail(),
                    member.getStatusAkun()
                });
            }
            JTable table = createTable(model);
            contentPanel.add(wrapTable(table, 520));
            JPanel actions = createToolbarPanel();
            JButton update = createActionButton("Update");
            JButton suspend = createActionButton("Suspend");
            JButton activate = createActionButton("Aktifkan");
            JButton delete = createActionButton("Hapus");
            actions.add(update);
            actions.add(suspend);
            actions.add(activate);
            actions.add(delete);
            contentPanel.add(actions);
            update.addActionListener(e -> updateSelectedMember(table));
            suspend.addActionListener(e -> changeSelectedMemberStatus(table, true));
            activate.addActionListener(e -> changeSelectedMemberStatus(table, false));
            delete.addActionListener(e -> deleteSelectedMember(table));
        } catch (SQLException e) {
            showError("Gagal memuat member management", e);
        }
        refreshContent();
    }

    private void showRequestLoan() {
        if (!requireMemberView()) {
            return;
        }
        resetContent();
        addTitle("Request Pinjam Buku");
        try {
            com.mycompany.perpustakaan.api.BookshelfPage page = libraryApi.getBookshelfPage("", "", 1, 50);
            DefaultTableModel model = new DefaultTableModel(new Object[]{"ID", "Kode", "Judul", "Penulis", "Kategori", "Stok"}, 0);
            for (com.mycompany.perpustakaan.api.BookSummary book : page.getBooks()) {
                model.addRow(new Object[]{book.getIdBuku(), book.getKodeBuku(), book.getJudul(), book.getPenulis(), book.getKategori(), book.getStokTersedia()});
            }
            JTable table = createTable(model);
            contentPanel.add(wrapTable(table, 380));
            JButton request = createActionButton("Request Pinjam Buku Terpilih");
            request.addActionListener(e -> requestSelectedLoan(table));
            JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 10));
            footer.setOpaque(false);
            footer.add(request);
            contentPanel.add(footer);
        } catch (SQLException e) {
            showError("Gagal memuat buku", e);
        }
        refreshContent();
    }

    private void showCurrentLoans() {
        if (!requireMemberView()) {
            return;
        }
        resetContent();
        addTitle("Pinjaman Aktif Saya");
        try {
            DefaultTableModel model = new DefaultTableModel(new Object[]{"ID", "Buku", "Pinjam", "Jatuh Tempo", "Status", "Denda"}, 0);
            for (com.mycompany.perpustakaan.api.LoanSummary loan : libraryApi.getCurrentLoans()) {
                model.addRow(new Object[]{loan.getIdPeminjaman(), loan.getJudulBuku(), loan.getTanggalPinjam(), loan.getTanggalJatuhTempo(), loan.getStatus(), formatMoney(loan.getDendaBerjalan())});
            }
            contentPanel.add(createTablePanel(model, 430));
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
        try {
            com.mycompany.perpustakaan.api.HistoryPage page = libraryApi.getLoanHistory("semua", 1, 50);
            DefaultTableModel model = new DefaultTableModel(new Object[]{"ID", "Buku", "Pinjam", "Jatuh Tempo", "Kembali", "Status", "Denda"}, 0);
            for (com.mycompany.perpustakaan.api.LoanSummary loan : page.getLoans()) {
                model.addRow(new Object[]{loan.getIdPeminjaman(), loan.getJudulBuku(), loan.getTanggalPinjam(), loan.getTanggalJatuhTempo(), loan.getTanggalKembali(), loan.getStatus(), formatMoney(loan.getDendaBerjalan())});
            }
            contentPanel.add(createTablePanel(model, 430));
        } catch (SQLException e) {
            showError("Gagal memuat history", e);
        }
        refreshContent();
    }

    private void showVisitForm() {
        if (!requireMemberView()) {
            return;
        }
        JTextField jenis = createField("mahasiswa");
        JTextField asal = createField("");
        JTextField keperluan = createField("Membaca buku");
        JPanel panel = formPanel(new String[]{"Jenis pengunjung", "Asal instansi", "Keperluan"}, new JTextField[]{jenis, asal, keperluan});
        int result = JOptionPane.showConfirmDialog(this, panel, "Tambah Kunjungan", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                com.mycompany.perpustakaan.api.VisitResponse response = libraryApi.addRegisteredUserVisit(jenis.getText(), asal.getText(), keperluan.getText());
                showResponse(response.isSuccess(), response.getMessage());
            } catch (SQLException e) {
                showError("Gagal menambah kunjungan", e);
            }
        }
    }

    private void showAddBookDialog() {
        showBookFormPage(null);
    }

    private void showBookFormPage(Integer idBuku) {
        JTextField kode = createField("");
        JTextField isbn = createField("");
        JTextField judul = createField("");
        JTextField penulis = createField("");
        JTextField penerbit = createField("");
        JTextField kategori = createField("");
        JTextField tahun = createField("");
        JTextField stokTersedia = createField("1");
        JTextField stokTotal = createField("1");

        if (idBuku != null) {
            try {
                com.mycompany.perpustakaan.api.BookSummary book = libraryApi.getBookByIdForManagement(idBuku);
                kode.setText(safe(book.getKodeBuku()));
                isbn.setText(safe(book.getIsbn()));
                judul.setText(safe(book.getJudul()));
                penulis.setText(safe(book.getPenulis()));
                penerbit.setText(safe(book.getPenerbit()));
                kategori.setText(safe(book.getKategori()));
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

        JPanel page = new JPanel(new BorderLayout(28, 0));
        page.setOpaque(false);
        page.setAlignmentX(Component.LEFT_ALIGNMENT);

        page.add(createBookCoverPanel(260, 360), BorderLayout.WEST);

        JPanel formCard = new JPanel();
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setBackground(WHITE);
        formCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(22, 24, 22, 24)
        ));

        formCard.add(formPanel(
                new String[]{"Kode Buku", "ISBN", "Judul", "Penulis", "Penerbit", "Kategori", "Tahun Terbit", "Stok Tersedia", "Stok Total"},
                new JTextField[]{kode, isbn, judul, penulis, penerbit, kategori, tahun, stokTersedia, stokTotal}
        ));
        formCard.add(Box.createVerticalStrut(18));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        JButton cancel = createNeutralButton("Kembali");
        JButton save = createActionButton(idBuku == null ? "Tambah Buku" : "Simpan Perubahan");
        cancel.addActionListener(e -> showBookManagement());
        save.addActionListener(e -> {
            try {
                Integer tahunValue = tahun.getText().trim().isEmpty() ? null : Integer.valueOf(tahun.getText().trim());
                com.mycompany.perpustakaan.api.BookRequest request = new com.mycompany.perpustakaan.api.BookRequest(
                        kode.getText(), blankToNull(isbn.getText()), judul.getText(), penulis.getText(), penerbit.getText(),
                        kategori.getText(), tahunValue, Integer.parseInt(stokTersedia.getText().trim()), Integer.parseInt(stokTotal.getText().trim()));
                com.mycompany.perpustakaan.api.BookResponse response = idBuku == null ? libraryApi.addBook(request) : libraryApi.updateBook(idBuku, request);
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
        formCard.add(actions);

        page.add(formCard, BorderLayout.CENTER);
        contentPanel.add(page);
        refreshContent();
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
        int confirm = JOptionPane.showConfirmDialog(this, "Hapus buku ID " + id + "?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
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

        JPanel page = new JPanel(new BorderLayout(34, 0));
        page.setOpaque(false);
        page.setAlignmentX(Component.LEFT_ALIGNMENT);
        page.add(createBookCoverPanel(300, 420), BorderLayout.WEST);

        JPanel detail = new JPanel();
        detail.setLayout(new BoxLayout(detail, BoxLayout.Y_AXIS));
        detail.setOpaque(false);
        detail.setBorder(new EmptyBorder(16, 0, 0, 0));

        JLabel title = new JLabel(safe(book.getJudul()));
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(TEXT_DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        detail.add(title);
        detail.add(Box.createVerticalStrut(18));

        detail.add(createDetailRow("Kode", safe(book.getKodeBuku())));
        detail.add(createDetailRow("ISBN", safeOrDash(book.getIsbn())));
        detail.add(createDetailRow("Author", safe(book.getPenulis())));
        detail.add(createDetailRow("Penerbit", safeOrDash(book.getPenerbit())));
        detail.add(createDetailRow("Published", book.getTahunTerbit() == null ? "-" : String.valueOf(book.getTahunTerbit())));
        detail.add(createDetailRow("Kategori", safeOrDash(book.getKategori())));
        detail.add(createDetailRow("Status", safeOrDash(book.getStatusKetersediaan())));
        detail.add(Box.createVerticalStrut(8));
        detail.add(createSynopsisBlock(book));
        detail.add(Box.createVerticalStrut(12));

        JPanel stockRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        stockRow.setOpaque(false);
        stockRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel stok = new JLabel("Stok: " + book.getStokTersedia() + " / " + book.getStokTotal());
        stok.setFont(new Font("Segoe UI", Font.BOLD, 18));
        stok.setForeground(TEXT_DARK);
        stockRow.add(stok);
        if (managementMode && isStaffOrAdmin()) {
            JButton updateStock = createActionButton("Update Stok");
            updateStock.addActionListener(e -> showStockPage(book.getIdBuku()));
            stockRow.add(updateStock);
        }
        detail.add(stockRow);
        detail.add(Box.createVerticalGlue());

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
            JButton edit = createActionButton("Edit Buku");
            edit.addActionListener(e -> showBookFormPage(book.getIdBuku()));
            actions.add(edit);
        } else if (!isStaffOrAdmin() && book.getStokTersedia() > 0) {
            JButton request = createActionButton("Request Pinjam");
            request.addActionListener(e -> requestLoanFromDetail(book.getIdBuku()));
            actions.add(request);
        }
        detail.add(actions);

        page.add(detail, BorderLayout.CENTER);
        contentPanel.add(page);
        refreshContent();
    }

    private void showStockPage(int idBuku) {
        try {
            com.mycompany.perpustakaan.api.BookSummary book = libraryApi.getBookByIdForManagement(idBuku);
            resetContent();
            addTitle("Update Stok Buku");

            JPanel page = new JPanel(new BorderLayout(32, 0));
            page.setOpaque(false);
            page.setAlignmentX(Component.LEFT_ALIGNMENT);
            page.add(createBookCoverPanel(280, 390), BorderLayout.WEST);

            JPanel detail = new JPanel();
            detail.setLayout(new BoxLayout(detail, BoxLayout.Y_AXIS));
            detail.setOpaque(false);
            detail.setBorder(new EmptyBorder(24, 0, 0, 0));

            JLabel title = new JLabel(safe(book.getJudul()));
            title.setFont(new Font("Segoe UI", Font.BOLD, 26));
            title.setForeground(TEXT_DARK);
            title.setAlignmentX(Component.LEFT_ALIGNMENT);
            detail.add(title);
            detail.add(Box.createVerticalStrut(16));
            detail.add(createDetailRow("Author", safe(book.getPenulis())));
            detail.add(createDetailRow("Kategori", safeOrDash(book.getKategori())));
            detail.add(createDetailRow("Kode", safe(book.getKodeBuku())));
            detail.add(Box.createVerticalStrut(20));

            JTextField tersedia = createStockField(book.getStokTersedia());
            JTextField total = createStockField(book.getStokTotal());
            detail.add(createStockControl("Stok Tersedia", tersedia));
            detail.add(Box.createVerticalStrut(12));
            detail.add(createStockControl("Stok Total", total));
            detail.add(Box.createVerticalStrut(24));

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            actions.setOpaque(false);
            actions.setAlignmentX(Component.LEFT_ALIGNMENT);
            JButton back = createNeutralButton("Kembali");
            JButton save = createActionButton("Simpan Stok");
            back.addActionListener(e -> showBookDetailPage(book, true));
            save.addActionListener(e -> {
                try {
                    com.mycompany.perpustakaan.api.BookResponse response = libraryApi.updateBookStock(
                            idBuku,
                            Integer.parseInt(tersedia.getText().trim()),
                            Integer.parseInt(total.getText().trim())
                    );
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
            detail.add(actions);

            page.add(detail, BorderLayout.CENTER);
            contentPanel.add(page);
            refreshContent();
        } catch (SQLException e) {
            showError("Gagal membuka update stok", e);
        }
    }

    private void showCreateLoanDialog() {
        JTextField idUser = createField("");
        JTextField idBuku = createField("");
        JTextField hari = createField("7");
        JPanel panel = formPanel(new String[]{"ID User", "ID Buku", "Lama pinjam (hari)"}, new JTextField[]{idUser, idBuku, hari});
        int result = JOptionPane.showConfirmDialog(this, panel, "Buat Peminjaman Untuk User", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
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
        int result = JOptionPane.showConfirmDialog(this, hari, "Lama pinjam (hari)", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                com.mycompany.perpustakaan.api.LoanResponse response = libraryApi.requestLoan(id, Integer.parseInt(hari.getText().trim()));
                showResponse(response.isSuccess(), response.getMessage());
                showCurrentLoans();
            } catch (SQLException | NumberFormatException e) {
                showError("Gagal request peminjaman", e);
            }
        }
    }

    private void requestLoanFromDetail(int idBuku) {
        JTextField hari = createField("7");
        int result = JOptionPane.showConfirmDialog(this, hari, "Lama pinjam (hari)", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                com.mycompany.perpustakaan.api.LoanResponse response = libraryApi.requestLoan(idBuku, Integer.parseInt(hari.getText().trim()));
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
                new String[]{"Username", "Nama", "Email", idUser == null ? "Password" : "Password baru"},
                new JTextField[]{username, nama, email, password}
        );
        if (idUser != null) {
            JLabel hint = new JLabel("Kosongkan password kalau tidak ingin mengganti password lama.");
            hint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            hint.setForeground(TEXT_GRAY);
            panel.add(new JLabel(""));
            panel.add(hint);
        }
        int result = JOptionPane.showConfirmDialog(this, panel, idUser == null ? "Tambah Anggota" : "Update Anggota", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                com.mycompany.perpustakaan.api.MemberRequest request = new com.mycompany.perpustakaan.api.MemberRequest(username.getText(), nama.getText(), email.getText(), password.getText());
                com.mycompany.perpustakaan.api.MemberResponse response = idUser == null ? libraryApi.addMember(request) : libraryApi.updateMember(idUser, request);
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
            com.mycompany.perpustakaan.api.MemberResponse response = suspend ? libraryApi.suspendMember(id) : libraryApi.activateMember(id);
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
        int confirm = JOptionPane.showConfirmDialog(this, "Hapus anggota ID " + id + "?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
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
            com.mycompany.perpustakaan.api.ReportExportResponse response = libraryApi.exportInventoryReport(format, dir);
            showResponse(response.isSuccess(), response.getMessage() + (response.getFilePath() == null ? "" : "\n" + response.getFilePath()));
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
            com.mycompany.perpustakaan.api.ReportExportResponse response = libraryApi.exportLoanReport(format, dir, parseDate(start), parseDate(end));
            showResponse(response.isSuccess(), response.getMessage() + (response.getFilePath() == null ? "" : "\n" + response.getFilePath()));
        } catch (SQLException | DateTimeParseException e) {
            showError("Gagal export peminjaman", e);
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

    private JPanel createSection(String title, List<com.mycompany.perpustakaan.api.BookSummary> books, boolean withArrow) {
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
            setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1), new EmptyBorder(0, 0, 10, 0)));
            setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

            JLabel cover = new JLabel();
            ImageIcon placeholder = loadBookCoverPlaceholder();
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
        JPanel row = new JPanel(new GridLayout(1, 4, 14, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        return row;
    }

    private JPanel createMetricCard(String title, String value) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER_COLOR), new EmptyBorder(14, 16, 14, 16)));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(TEXT_GRAY);
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(TEXT_DARK);
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createTablePanel(DefaultTableModel model, int height) {
        return wrapTable(createTable(model), height);
    }

    private JTable createTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(250, 250, 250));
        table.setSelectionBackground(new Color(255, 231, 222));
        table.setSelectionForeground(TEXT_DARK);
        return table;
    }

    private JPanel wrapTable(JTable table, int height) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, height + 8));
        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(1500, height));
        scroll.setMinimumSize(new Dimension(900, height));
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createToolbarPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
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
        button.setBorder(new EmptyBorder(8, 14, 8, 14));
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        return button;
    }

    private JButton createNeutralButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(TEXT_DARK);
        button.setBackground(new Color(245, 245, 245));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TEXT_GRAY, 1),
                new EmptyBorder(8, 18, 8, 18)
        ));
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
                new EmptyBorder(18, 18, 18, 18)
        ));

        JLabel image = new JLabel();
        ImageIcon placeholder = loadBookCoverPlaceholder();
        if (placeholder != null) {
            Image scaled = placeholder.getImage().getScaledInstance(Math.min(130, width - 60), Math.min(110, height - 80), Image.SCALE_SMOOTH);
            image.setIcon(new ImageIcon(scaled));
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

    private class GradientActionButton extends JButton {
        GradientActionButton(String text) {
            super(text);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color start = getModel().isRollover() ? ACCENT_DARK : ACCENT;
            Color end = getModel().isRollover() ? ACCENT : ACCENT_DARK;
            GradientPaint gp = new GradientPaint(0, 0, start, getWidth(), getHeight(), end);
            g2d.setPaint(gp);
            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            g2d.dispose();
            super.paintComponent(g);
        }
    }

    private JButton createNotificationButton() {
        JButton button = new JButton();
        try {
            button.setIcon(new ImageIcon(getClass().getResource("/LONCENG.png")));
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
        button.addActionListener(e -> JOptionPane.showMessageDialog(this, "Belum ada notifikasi baru.", "Notifikasi", JOptionPane.INFORMATION_MESSAGE));
        return button;
    }

    private JButton createLogoutButton() {
        JButton button = new JButton("Logout");
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(ACCENT);
        button.setBackground(WHITE);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setMaximumSize(new Dimension(180, 45));
        button.setPreferredSize(new Dimension(180, 45));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(10, 15, 10, 15));
        button.addActionListener(e -> performLogout());
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setForeground(ACCENT_DARK);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setForeground(ACCENT);
            }
        });
        return button;
    }

    private void performLogout() {
        libraryApi.logout();
        new LoginForm(libraryApi).setVisible(true);
        dispose();
    }

    private void addTitle(String title) {
        JLabel label = new JLabel(title);
        label.setFont(new Font("Segoe UI", Font.BOLD, 24));
        label.setForeground(TEXT_DARK);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(label);
        contentPanel.add(Box.createVerticalStrut(18));
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
        java.net.URL resource = getClass().getResource(resourcePath);
        if (resource == null) {
            return null;
        }
        if (resourcePath.toLowerCase().endsWith(".svg")) {
            return createSvgFallbackIcon(resourcePath);
        }
        ImageIcon source = new ImageIcon(resource);
        if (source.getIconWidth() <= 0 || source.getIconHeight() <= 0) {
            return createSvgFallbackIcon(resourcePath);
        }
        Image scaled = source.getImage().getScaledInstance(22, 22, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    private ImageIcon createSvgFallbackIcon(String resourcePath) {
        BufferedImage image = new BufferedImage(22, 22, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.setColor(TEXT_DARK);

        String name = resourcePath.toLowerCase();
        if (name.contains("member") || name.contains("customer")) {
            drawMemberIcon(g2d);
        } else if (name.contains("book")) {
            drawBookIcon(g2d);
        } else if (name.contains("homework") || name.contains("control") || name.contains("time")) {
            drawReportIcon(g2d);
        } else {
            g2d.fillRoundRect(4, 4, 14, 14, 3, 3);
        }

        g2d.dispose();
        return new ImageIcon(image);
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

    private ImageIcon loadBookCoverPlaceholder() {
        String[] candidates = {"/empty-img.png", "/empty-image.png"};
        for (String candidate : candidates) {
            java.net.URL resource = getClass().getResource(candidate);
            if (resource != null) {
                ImageIcon source = new ImageIcon(resource);
                Image scaled = source.getImage().getScaledInstance(160, 100, Image.SCALE_SMOOTH);
                return new ImageIcon(scaled);
            }
        }
        return null;
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
        com.mycompany.perpustakaan.api.UserSummary profile = libraryApi.getCurrentUser();
        if (profile == null) {
            JOptionPane.showMessageDialog(this, "Belum ada user yang login.", "Profil", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String message = "Nama: " + profile.getNama()
                + "\nUsername: " + profile.getUsername()
                + "\nEmail: " + profile.getEmail()
                + "\nRole: " + profile.getRole()
                + "\nStatus: " + profile.getStatusAkun();
        Object[] options = {"Tutup", "Logout"};
        int choice = JOptionPane.showOptionDialog(this, message, "Profil", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
        if (choice == 1) {
            libraryApi.logout();
            new LoginForm(libraryApi).setVisible(true);
            dispose();
        }
    }

    private void showBookshelf() {
        if (!requireMemberView()) {
            return;
        }
        resetContent();
        addTitle("Bookshelf Perpustakaan");

        // Search bar
        contentPanel.add(createSearchBar());
        contentPanel.add(Box.createVerticalStrut(15));

        try {
            // MODE PENCARIAN
            if (!currentKeyword.isBlank() || !"Semua kategori".equals(currentCategory)) {
                contentPanel.add(createSearchResultSection());
                refreshContent();
                return;
            }

            // MODE DEFAULT: per kategori
            List<String> categories = libraryApi.getBookCategories();
            if (categories == null || categories.isEmpty()) {
                categories = List.of("SCI-FI", "FANTASY", "ECONOMY", "LAW");
            }

            for (int i = 0; i < categories.size(); i++) {
                contentPanel.add(createCategorySection(categories.get(i)));
                if (i < categories.size() - 1) {
                    contentPanel.add(Box.createVerticalStrut(15));
                }
            }

        } catch (SQLException e) {
            showError("Gagal memuat bookshelf", e);
            String[] fallback = {"SCI-FI", "FANTASY", "ECONOMY", "LAW"};
            for (int i = 0; i < fallback.length; i++) {
                contentPanel.add(createCategorySection(fallback[i]));
                if (i < fallback.length - 1) {
                    contentPanel.add(Box.createVerticalStrut(15));
                }
            }
        }

        refreshContent();
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
        JLabel searchIcon = new JLabel("🔍");
        searchIcon.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        searchIcon.setForeground(TEXT_GRAY);
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

            com.mycompany.perpustakaan.api.BookshelfPage page =
                    libraryApi.getBookshelfPage(safeKeyword, safeCategory, 1, 20);

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
            com.mycompany.perpustakaan.api.BookshelfPage page =
                    libraryApi.getBookshelfPage(null, category, 1, 8);

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
        return Integer.valueOf(String.valueOf(value));
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
        JOptionPane.showMessageDialog(this, message, success ? "Berhasil" : "Gagal", success ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String message, Exception exception) {
        JOptionPane.showMessageDialog(this, message + ": " + exception.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        logger.log(java.util.logging.Level.SEVERE, message, exception);
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> new Dashboard().setVisible(true));
    }
}
