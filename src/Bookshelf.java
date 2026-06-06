import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

public class Bookshelf extends JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Bookshelf.class.getName());
    private final com.mycompany.perpustakaan.api.LibraryApi libraryApi;

    // WARNA
    private static final Color BG_APP = new Color(245, 245, 245);
    private static final Color WHITE = Color.WHITE;
    private static final Color ACCENT = new Color(232, 130, 90);
    private static final Color ACCENT_DARK = new Color(196, 149, 94);
    private static final Color TEXT_DARK = new Color(50, 50, 50);
    private static final Color TEXT_GRAY = new Color(120, 120, 120);
    private static final Color BORDER_COLOR = new Color(224, 224, 224);
    private static final Color SEARCH_BG = new Color(220, 220, 220);
    private static final Color GREEN_STATUS = new Color(0, 200, 83);
    private static final Color RED_STATUS = new Color(255, 23, 68);

    private static final Dimension FRAME_SIZE = new Dimension(1280, 720);
    private static final Dimension SIDEBAR_SIZE = new Dimension(220, 720);
    private static final Dimension HEADER_SIZE = new Dimension(1060, 80);
    private static final Dimension CARD_SIZE = new Dimension(160, 240);

    private SidebarButton activeButton;
    private JPanel contentPanel;
    private JTextField searchField;
    private JComboBox<String> categoryFilter;
    private String currentKeyword = "";
    private String currentCategory = "Semua kategori";

    public Bookshelf() {
        this(new com.mycompany.perpustakaan.api.LibraryApi());
    }

    public Bookshelf(com.mycompany.perpustakaan.api.LibraryApi libraryApi) {
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
        setTitle("Bookshelf Perpustakaan");
        setSize(FRAME_SIZE);
        setMinimumSize(FRAME_SIZE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BG_APP);
        setContentPane(root);

        // Sidebar
        SidebarPanel sidebar = new SidebarPanel();
        root.add(sidebar, BorderLayout.WEST);

        // Main area
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

        // Load content
        loadBookshelf();
    }

    // ==================== SIDEBAR ====================
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
                logo.setText("ðŸ“š LIBRARY HUB");
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

            // Buttons
            SidebarButton btnDashboard = new SidebarButton("Dashboard", "/icon-dashboard.png");
            SidebarButton btnBookshelf = new SidebarButton("Bookshelf", "/icon-bookshelf.png");
            SidebarButton btnLoan = new SidebarButton("Loan Page", "/icon-loan.png");
            SidebarButton btnProfile = new SidebarButton("User Profile", "/icon-profile.png");
            SidebarButton btnHistory = new SidebarButton("History", "/icon-history.png");

            btnDashboard.addActionListener(e -> {
                setActive(btnDashboard);
                openDashboard();
            });
            btnBookshelf.addActionListener(e -> {
                setActive(btnBookshelf);
            });
            btnLoan.addActionListener(e -> {
                setActive(btnLoan);
            });
            btnProfile.addActionListener(e -> {
                setActive(btnProfile);
            });
            btnHistory.addActionListener(e -> {
                setActive(btnHistory);
            });

            add(btnDashboard);
            add(Box.createVerticalStrut(8));
            add(btnBookshelf);
            add(Box.createVerticalStrut(8));
            add(btnLoan);
            add(Box.createVerticalStrut(8));
            add(btnProfile);
            add(Box.createVerticalStrut(8));
            add(btnHistory);

            setActive(btnBookshelf);
            add(Box.createVerticalGlue());
        }
    }

    private class SidebarButton extends JButton {
        SidebarButton(String text, String iconResource) {
            super(text);
            setIcon(loadMenuIcon(iconResource));
            setFont(new Font("Segoe UI", Font.BOLD, 14));
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

    private ImageIcon loadMenuIcon(String resourcePath) {
        java.net.URL resource = getClass().getResource(resourcePath);
        if (resource == null) {
            return null;
        }
        ImageIcon source = new ImageIcon(resource);
        Image scaled = source.getImage().getScaledInstance(22, 22, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
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

    private void setActive(SidebarButton button) {
        if (activeButton != null) {
            activeButton.repaint();
        }
        activeButton = button;
        activeButton.repaint();
    }

    // ==================== HEADER ====================
    private class HeaderPanel extends JPanel {
        HeaderPanel() {
            setPreferredSize(HEADER_SIZE);
            setBackground(WHITE);
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                    new EmptyBorder(15, 25, 15, 25)
            ));

            // Left: Profile
            JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
            left.setOpaque(false);

            JLabel avatar = new JLabel();
            try {
                avatar.setIcon(new ImageIcon(getClass().getResource("/PROFILE2.png")));
            } catch (Exception e) {
                avatar.setText("ðŸ‘¤");
                avatar.setFont(new Font("Segoe UI", Font.PLAIN, 32));
            }
            avatar.setPreferredSize(new Dimension(50, 50));
            avatar.setHorizontalAlignment(SwingConstants.CENTER);

            JPanel textPanel = new JPanel();
            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
            textPanel.setOpaque(false);

            JLabel welcomeLabel = new JLabel("WELCOME, USER");
            welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            welcomeLabel.setForeground(TEXT_DARK);

            JLabel roleLabel = new JLabel("USER");
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
                notif.setText("ðŸ””");
                notif.setFont(new Font("Segoe UI", Font.PLAIN, 24));
            }
            notif.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

            add(left, BorderLayout.WEST);
            add(notif, BorderLayout.EAST);
        }
    }

    // ==================== SEARCH BAR ====================
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
                g2d.setColor(SEARCH_BG);
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
        JLabel searchIcon = new JLabel("ðŸ”");
        searchIcon.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        searchIcon.setForeground(TEXT_GRAY);
        searchIcon.setBounds(12, 10, 24, 24);

        JPanel searchWrapper = new JPanel(null);
        searchWrapper.setOpaque(false);
        searchWrapper.setPreferredSize(new Dimension(410, 48));
        searchWrapper.add(searchIcon);
        searchWrapper.add(searchField);
        searchField.setBounds(0, 0, 400, 42);

        searchField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals(placeholder)) {
                    searchField.setText("");
                    searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                    searchField.setForeground(TEXT_DARK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
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

    private String[] loadCategoryOptions() {
        ArrayList<String> options = new ArrayList<>();
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

    // ==================== CONTENT ====================
    private void loadBookshelf() {
        contentPanel.removeAll();

        // Search bar
        contentPanel.add(createSearchBar());
        contentPanel.add(Box.createVerticalStrut(15));

        try {
            // MODE PENCARIAN
            if (!currentKeyword.isBlank() || !"Semua kategori".equals(currentCategory)) {
                contentPanel.add(createSearchResultSection());
                contentPanel.revalidate();
                contentPanel.repaint();
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

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // ==================== SEARCH RESULT ====================
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
                    boolean available = book.getStokTersedia() > 0;
                    grid.add(new BookCard(book.getJudul(), book.getPenulis(), available));
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

    // ==================== CATEGORY SECTION ====================
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

        JLabel arrow = new JLabel("â†’");
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
                scrollPanel.addCard("Tidak ada buku", "Kategori kosong", false);
            } else {
                for (com.mycompany.perpustakaan.api.BookSummary book : books) {
                    boolean available = book.getStokTersedia() > 0;
                    scrollPanel.addCard(book.getJudul(), book.getPenulis(), available);
                }
            }
        } catch (SQLException e) {
            logger.log(java.util.logging.Level.WARNING, "Gagal memuat kategori " + category, e);
            scrollPanel.addCard("Gagal memuat", "Periksa koneksi", false);
        }

        section.add(scrollPanel);
        return section;
    }

    // ==================== HORIZONTAL SCROLL ====================
    private class HorizontalScrollPanel extends JPanel {
        private JPanel cardsContainer;

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

            // Right arrow
            JPanel arrowPanel = new JPanel(new BorderLayout());
            arrowPanel.setOpaque(false);
            arrowPanel.setPreferredSize(new Dimension(36, 260));

            JLabel rightArrow = new JLabel("â–¶") {
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
            rightArrow.setForeground(WHITE);
            rightArrow.setHorizontalAlignment(SwingConstants.CENTER);
            rightArrow.setPreferredSize(new Dimension(32, 32));
            rightArrow.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            rightArrow.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    scroll.getHorizontalScrollBar().setValue(
                            scroll.getHorizontalScrollBar().getValue() + 340
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

    // ==================== BOOK CARD ====================
    private class BookCard extends JPanel {
        BookCard(String title, String author, boolean available) {
            setPreferredSize(CARD_SIZE);
            setBackground(WHITE);
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR, 1),
                    new EmptyBorder(0, 0, 10, 0)
            ));

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

            // Info
            JPanel info = new JPanel();
            info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
            info.setOpaque(false);
            info.setBorder(new EmptyBorder(10, 12, 5, 12));

            JLabel titleLabel = new JLabel(truncate(title, 18));
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            titleLabel.setForeground(TEXT_DARK);
            titleLabel.setMaximumSize(new Dimension(140, 18));

            JLabel authorLabel = new JLabel(truncate(author, 20));
            authorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            authorLabel.setForeground(TEXT_GRAY);
            authorLabel.setMaximumSize(new Dimension(140, 14));

            JLabel statusLabel = new JLabel(available ? "Tersedia" : "Tidak Tersedia");
            statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 9));
            statusLabel.setForeground(WHITE);
            statusLabel.setOpaque(true);
            statusLabel.setBackground(available ? GREEN_STATUS : RED_STATUS);
            statusLabel.setBorder(new EmptyBorder(3, 8, 3, 8));
            statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            statusLabel.setMaximumSize(new Dimension(available ? 65 : 85, 20));

            info.add(titleLabel);
            info.add(authorLabel);
            info.add(Box.createVerticalStrut(5));
            info.add(statusLabel);
            info.add(Box.createVerticalGlue());

            // Link
            JLabel link = new JLabel("Lihat Selengkapnya â†’");
            link.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            link.setForeground(ACCENT);
            link.setBorder(new EmptyBorder(5, 12, 0, 12));
            link.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

            add(cover, BorderLayout.NORTH);
            add(info, BorderLayout.CENTER);
            add(link, BorderLayout.SOUTH);
        }
    }

    private String truncate(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        return text.length() > maxLength ? text.substring(0, maxLength - 2) + ".." : text;
    }

    // ==================== ACTIONS ====================
    private void performSearch() {
        String keyword = searchField.getText().trim();
        currentKeyword = keyword.equals("Cari judul atau penulis...") ? "" : keyword;
        Object selected = categoryFilter == null ? null : categoryFilter.getSelectedItem();
        currentCategory = selected == null ? "Semua kategori" : selected.toString();
        loadBookshelf();
    }

    private void openDashboard() {
        new Dashboard(libraryApi).setVisible(true);
        dispose();
    }

    private void showError(String message, Exception exception) {
        JOptionPane.showMessageDialog(this, message + ": " + exception.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        logger.log(java.util.logging.Level.SEVERE, message, exception);
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> new Bookshelf().setVisible(true));
    }
}
