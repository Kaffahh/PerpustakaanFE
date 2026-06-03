/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */

/**
 *
 * @author malik abdul aziz
 */
public class Dashboard extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Dashboard.class.getName());
    private final com.mycompany.perpustakaan.api.LibraryApi libraryApi;
    private javax.swing.JPanel contentPanel;
    private javax.swing.JLabel contentTitleLabel;
    private javax.swing.JTable contentTable;
    private int bookshelfPage = 1;
    private int historyPage = 1;

    /**
     * Creates new form Dashboard
     */
    public Dashboard() {
        this(new com.mycompany.perpustakaan.api.LibraryApi());
    }

    public Dashboard(com.mycompany.perpustakaan.api.LibraryApi libraryApi) {
        this.libraryApi = libraryApi;
        initComponents();
        configureActions();
        refreshDashboard();
    }

    private void configureActions() {
        rebuildLayoutShell();
        jButton1.setText("Dashboard");
        jButton2.setText("Bookshelf");
        jButton3.setText("Loan page");
        jButton4.setText("User Profile");
        jButton5.setText("History");
    }

    private void refreshDashboard() {
        try {
            com.mycompany.perpustakaan.api.DashboardSummary dashboardSummary = libraryApi.getDashboardSummary(5);
            com.mycompany.perpustakaan.api.UserSummary profile = dashboardSummary.getProfile();

            if (profile != null) {
                jLabel4.setText("WELCOME, " + profile.getNama().toUpperCase());
                jLabel5.setText(profile.getRole() == null ? "USER" : profile.getRole().toUpperCase());
                setTitle("Dashboard Perpustakaan - " + profile.getUsername());
            } else {
                jLabel4.setText("WELCOME, USER");
                jLabel5.setText("USER");
                setTitle("Dashboard Perpustakaan");
            }
            showDashboardContent(dashboardSummary);
        } catch (java.sql.SQLException exception) {
            javax.swing.JOptionPane.showMessageDialog(this, "Gagal memuat data dashboard: " + exception.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            logger.log(java.util.logging.Level.SEVERE, "Gagal memuat dashboard", exception);
        }
    }

    private void rebuildLayoutShell() {
        contentTitleLabel = new javax.swing.JLabel("Dashboard");
        contentTitleLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 20));

        contentPanel = new javax.swing.JPanel(new java.awt.BorderLayout(12, 12));
        contentPanel.setBackground(new java.awt.Color(255, 255, 255));
        contentPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(18, 18, 18, 18));
        contentPanel.add(contentTitleLabel, java.awt.BorderLayout.NORTH);

        javax.swing.JPanel mainPanel = new javax.swing.JPanel(new java.awt.BorderLayout(0, 8));
        mainPanel.setOpaque(false);
        mainPanel.add(jPanel3, java.awt.BorderLayout.NORTH);
        mainPanel.add(contentPanel, java.awt.BorderLayout.CENTER);

        jPanel1.removeAll();
        jPanel1.setLayout(new java.awt.BorderLayout(8, 0));
        jPanel1.add(jPanel2, java.awt.BorderLayout.WEST);
        jPanel1.add(mainPanel, java.awt.BorderLayout.CENTER);
        jPanel1.revalidate();
        jPanel1.repaint();
    }

    private void setContent(String title, java.awt.Component controls, javax.swing.JTable table) {
        contentTitleLabel.setText(title);
        contentPanel.removeAll();
        contentPanel.add(contentTitleLabel, java.awt.BorderLayout.NORTH);
        if (controls != null) {
            contentPanel.add(controls, java.awt.BorderLayout.SOUTH);
        }
        contentTable = table;
        contentPanel.add(new javax.swing.JScrollPane(table), java.awt.BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private javax.swing.JTable createTable(String[] columns, Object[][] rows) {
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(rows, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        javax.swing.JTable table = new javax.swing.JTable(model);
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(24);
        table.setAutoCreateRowSorter(true);
        return table;
    }

    private void showDashboardContent(com.mycompany.perpustakaan.api.DashboardSummary dashboardSummary) {
        try {
            com.mycompany.perpustakaan.api.UserSummary profile = dashboardSummary.getProfile();
            if (profile != null && isAdmin(profile)) {
                showAdminDashboardContent();
                return;
            }

            java.util.List<com.mycompany.perpustakaan.api.BookSummary> latestBooks = dashboardSummary.getLatestBooks();
            Object[][] rows = new Object[latestBooks.size()][5];
            for (int index = 0; index < latestBooks.size(); index++) {
                com.mycompany.perpustakaan.api.BookSummary book = latestBooks.get(index);
                rows[index] = new Object[]{
                    book.getKodeBuku(),
                    book.getJudul(),
                    book.getPenulis(),
                    book.getKategori(),
                    book.getStokTersedia() + "/" + book.getStokTotal()
                };
            }

            javax.swing.JLabel summary = new javax.swing.JLabel("Total buku: " + dashboardSummary.getTotalBooks());
            setContent("Dashboard", summary, createTable(new String[]{"Kode", "Judul", "Penulis", "Kategori", "Stok"}, rows));
        } catch (java.sql.SQLException exception) {
            showError("Gagal memuat dashboard admin", exception);
        }
    }

    private void showAdminDashboardContent() throws java.sql.SQLException {
        com.mycompany.perpustakaan.api.AdminDashboardSummary summary = libraryApi.getAdminDashboardSummary();
        Object[][] rows = new Object[][]{
            {"Total Buku", summary.getTotalBuku()},
            {"Total Anggota", summary.getTotalAnggota()},
            {"Peminjaman Aktif", summary.getTotalPeminjamanAktif()},
            {"Total Denda", summary.getTotalDenda()}
        };

        javax.swing.JPanel controls = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        javax.swing.JButton exportInventoryPdfButton = new javax.swing.JButton("Export Inventory PDF");
        javax.swing.JButton exportLoanXlsxButton = new javax.swing.JButton("Export Peminjaman XLSX");
        exportInventoryPdfButton.addActionListener(event -> exportInventory("pdf"));
        exportLoanXlsxButton.addActionListener(event -> exportLoans("xlsx"));
        controls.add(exportInventoryPdfButton);
        controls.add(exportLoanXlsxButton);

        setContent("Admin Dashboard", controls, createTable(new String[]{"Metric", "Nilai"}, rows));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new java.awt.Dimension(1280, 720));

        jPanel1.setBackground(new java.awt.Color(232, 170, 120));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/logo3.png"))); // NOI18N
        jLabel1.setText("jLabel1");

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel2.setText("Menu");

        jButton1.setBackground(new java.awt.Color(232, 170, 120));
        jButton1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setText("Dashboard");
        jButton1.addActionListener(this::jButton1ActionPerformed);

        jButton2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton2.setText("Bookshelf");
        jButton2.addActionListener(this::jButton2ActionPerformed);

        jButton3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton3.setText("Loan page");
        jButton3.addActionListener(this::jButton3ActionPerformed);

        jButton4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton4.setText("User Profile");
        jButton4.addActionListener(this::jButton4ActionPerformed);

        jButton5.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton5.setText("History");
        jButton5.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jButton5.addActionListener(this::jButton5ActionPerformed);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                .addContainerGap(23, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(552, Short.MAX_VALUE))
        );

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PROFILE2.png"))); // NOI18N
        jLabel3.setText("jLabel3");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel4.setText("WELCOME, PABRIALO GUSTAVO");

        jLabel5.setBackground(new java.awt.Color(232, 170, 120));
        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(232, 170, 120));
        jLabel5.setText("USER");

        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/LONCENG.png"))); // NOI18N
        jLabel6.setText("jLabel6");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 693, Short.MAX_VALUE)
                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel5)))
                .addContainerGap(10, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel6)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 6, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        refreshDashboard();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        showBookshelf();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        showLoans();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        showProfile();
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        showHistory();
    }//GEN-LAST:event_jButton5ActionPerformed

    private void showProfile() {
        com.mycompany.perpustakaan.api.UserSummary profile = libraryApi.getCurrentUser();
        if (profile == null) {
            javax.swing.JOptionPane.showMessageDialog(this, "Belum ada user yang login.", "Profil", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        String message = "Nama: " + profile.getNama()
                + "\nUsername: " + profile.getUsername()
                + "\nEmail: " + profile.getEmail()
                + "\nRole: " + profile.getRole()
                + "\nBergabung: " + profile.getCreatedAt();
        Object[] options = {"Tutup", "Logout"};
        int choice = javax.swing.JOptionPane.showOptionDialog(this, message, "Profil Pengguna", javax.swing.JOptionPane.DEFAULT_OPTION, javax.swing.JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
        if (choice == 1) {
            libraryApi.logout();
            new LoginForm(libraryApi).setVisible(true);
            dispose();
        }
    }

    private void showBookshelf() {
        try {
            javax.swing.JTextField keywordField = new javax.swing.JTextField(18);
            javax.swing.JComboBox<String> categoryBox = new javax.swing.JComboBox<>();
            categoryBox.addItem("Semua kategori");
            for (String category : libraryApi.getBookCategories()) {
                categoryBox.addItem(category);
            }
            javax.swing.JButton searchButton = new javax.swing.JButton("Cari");
            javax.swing.JButton previousButton = new javax.swing.JButton("Prev");
            javax.swing.JButton nextButton = new javax.swing.JButton("Next");
            javax.swing.JButton borrowButton = new javax.swing.JButton("Pinjam Buku");

            javax.swing.JPanel controls = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
            controls.add(new javax.swing.JLabel("Keyword"));
            controls.add(keywordField);
            controls.add(new javax.swing.JLabel("Kategori"));
            controls.add(categoryBox);
            controls.add(searchButton);
            controls.add(previousButton);
            controls.add(nextButton);
            if (isAnggota(libraryApi.getCurrentUser())) {
                controls.add(borrowButton);
            }

            Runnable loader = () -> loadBookshelf(keywordField.getText(), selectedCategory(categoryBox), controls);
            searchButton.addActionListener(event -> {
                bookshelfPage = 1;
                loader.run();
            });
            previousButton.addActionListener(event -> {
                if (bookshelfPage > 1) {
                    bookshelfPage--;
                    loader.run();
                }
            });
            nextButton.addActionListener(event -> {
                bookshelfPage++;
                loader.run();
            });
            borrowButton.addActionListener(event -> borrowSelectedBook());

            bookshelfPage = 1;
            loadBookshelf(null, null, controls);
        } catch (java.sql.SQLException exception) {
            showError("Gagal memuat bookshelf", exception);
        }
    }

    private void loadBookshelf(String keyword, String category, javax.swing.JPanel controls) {
        try {
            com.mycompany.perpustakaan.api.BookshelfPage page = libraryApi.getBookshelfPage(keyword, category, bookshelfPage, 10);
            Object[][] rows = new Object[page.getBooks().size()][8];
            for (int index = 0; index < page.getBooks().size(); index++) {
                com.mycompany.perpustakaan.api.BookSummary book = page.getBooks().get(index);
                rows[index] = new Object[]{
                    book.getIdBuku(),
                    book.getKodeBuku(),
                    book.getJudul(),
                    book.getPenulis(),
                    book.getPenerbit(),
                    book.getKategori(),
                    book.getTahunTerbit(),
                    book.getStokTersedia() + "/" + book.getStokTotal()
                };
            }
            setContent("Bookshelf - halaman " + page.getPage() + "/" + page.getTotalPages(), controls, createTable(new String[]{"ID", "Kode", "Judul", "Penulis", "Penerbit", "Kategori", "Tahun", "Stok"}, rows));
        } catch (java.sql.SQLException exception) {
            showError("Gagal memuat buku", exception);
        }
    }

    private void borrowSelectedBook() {
        Integer idBuku = selectedInteger(0);
        if (idBuku == null) {
            javax.swing.JOptionPane.showMessageDialog(this, "Pilih buku dulu.", "Loan Page", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            com.mycompany.perpustakaan.api.LoanResponse response = libraryApi.requestLoan(idBuku, 7);
            javax.swing.JOptionPane.showMessageDialog(this, response.getMessage(), "Loan Page", response.isSuccess() ? javax.swing.JOptionPane.INFORMATION_MESSAGE : javax.swing.JOptionPane.WARNING_MESSAGE);
            showLoans();
        } catch (java.sql.SQLException exception) {
            showError("Gagal mengajukan peminjaman", exception);
        }
    }

    private void showLoans() {
        com.mycompany.perpustakaan.api.UserSummary profile = libraryApi.getCurrentUser();
        if (profile != null && (isStaff(profile) || isAdmin(profile))) {
            showLoansForManagement();
            return;
        }

        try {
            java.util.List<com.mycompany.perpustakaan.api.LoanSummary> loans = libraryApi.getCurrentLoans();
            setContent("Loan Page - pinjaman aktif", null, createTable(loanColumns(), toLoanRows(loans)));
        } catch (java.sql.SQLException exception) {
            showError("Gagal memuat loan page", exception);
        }
    }

    private void showLoansForManagement() {
        try {
            com.mycompany.perpustakaan.api.LoanManagementPage page = libraryApi.getLoansForManagement("aktif", 1, 100);
            javax.swing.JButton returnButton = new javax.swing.JButton("Proses Pengembalian");
            javax.swing.JPanel controls = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
            controls.add(returnButton);
            returnButton.addActionListener(event -> processSelectedReturn());
            setContent("Loans & Returns - aktif", controls, createTable(loanColumns(), toLoanRows(page.getLoans())));
        } catch (java.sql.SQLException exception) {
            showError("Gagal memuat loans & returns", exception);
        }
    }

    private void processSelectedReturn() {
        Integer idPeminjaman = selectedInteger(0);
        if (idPeminjaman == null) {
            javax.swing.JOptionPane.showMessageDialog(this, "Pilih data peminjaman dulu.", "Loans & Returns", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            com.mycompany.perpustakaan.api.LoanResponse response = libraryApi.processReturn(idPeminjaman);
            javax.swing.JOptionPane.showMessageDialog(this, response.getMessage(), "Loans & Returns", response.isSuccess() ? javax.swing.JOptionPane.INFORMATION_MESSAGE : javax.swing.JOptionPane.WARNING_MESSAGE);
            showLoansForManagement();
        } catch (java.sql.SQLException exception) {
            showError("Gagal memproses pengembalian", exception);
        }
    }

    private void showHistory() {
        try {
            com.mycompany.perpustakaan.api.HistoryPage page = libraryApi.getLoanHistory(null, historyPage, 100);
            setContent("History Peminjaman", null, createTable(loanColumns(), toLoanRows(page.getLoans())));
        } catch (java.sql.SQLException | IllegalStateException exception) {
            showError("Gagal memuat history", exception);
        }
    }

    private String[] loanColumns() {
        return new String[]{"ID", "ID Buku", "Kode", "Judul", "Pinjam", "Jatuh Tempo", "Kembali", "Status", "Terlambat", "Denda"};
    }

    private Object[][] toLoanRows(java.util.List<com.mycompany.perpustakaan.api.LoanSummary> loans) {
        Object[][] rows = new Object[loans.size()][10];
        for (int index = 0; index < loans.size(); index++) {
            com.mycompany.perpustakaan.api.LoanSummary loan = loans.get(index);
            rows[index] = new Object[]{
                loan.getIdPeminjaman(),
                loan.getIdBuku(),
                loan.getKodeBuku(),
                loan.getJudulBuku(),
                loan.getTanggalPinjam(),
                loan.getTanggalJatuhTempo(),
                loan.getTanggalKembali(),
                loan.getStatus(),
                loan.getHariTerlambat(),
                loan.getDendaBerjalan()
            };
        }
        return rows;
    }

    private void exportInventory(String format) {
        try {
            com.mycompany.perpustakaan.api.ReportExportResponse response = libraryApi.exportInventoryReport(format, "exports");
            javax.swing.JOptionPane.showMessageDialog(this, response.getMessage() + "\n" + response.getFilePath(), "Export Report", response.isSuccess() ? javax.swing.JOptionPane.INFORMATION_MESSAGE : javax.swing.JOptionPane.WARNING_MESSAGE);
        } catch (java.sql.SQLException exception) {
            showError("Gagal export inventory", exception);
        }
    }

    private void exportLoans(String format) {
        try {
            com.mycompany.perpustakaan.api.ReportExportResponse response = libraryApi.exportLoanReport(format, "exports", null, java.time.LocalDate.now());
            javax.swing.JOptionPane.showMessageDialog(this, response.getMessage() + "\n" + response.getFilePath(), "Export Report", response.isSuccess() ? javax.swing.JOptionPane.INFORMATION_MESSAGE : javax.swing.JOptionPane.WARNING_MESSAGE);
        } catch (java.sql.SQLException exception) {
            showError("Gagal export peminjaman", exception);
        }
    }

    private Integer selectedInteger(int modelColumn) {
        if (contentTable == null || contentTable.getSelectedRow() < 0) {
            return null;
        }
        int modelRow = contentTable.convertRowIndexToModel(contentTable.getSelectedRow());
        Object value = contentTable.getModel().getValueAt(modelRow, modelColumn);
        return value instanceof Number number ? number.intValue() : Integer.valueOf(value.toString());
    }

    private String selectedCategory(javax.swing.JComboBox<String> categoryBox) {
        Object selected = categoryBox.getSelectedItem();
        if (selected == null || "Semua kategori".equals(selected.toString())) {
            return null;
        }
        return selected.toString();
    }

    private boolean isAdmin(com.mycompany.perpustakaan.api.UserSummary profile) {
        return profile != null && profile.isAdmin();
    }

    private boolean isStaff(com.mycompany.perpustakaan.api.UserSummary profile) {
        return profile != null && profile.isStaff();
    }

    private boolean isAnggota(com.mycompany.perpustakaan.api.UserSummary profile) {
        return profile != null && profile.isAnggota();
    }

    private void showError(String message, Exception exception) {
        javax.swing.JOptionPane.showMessageDialog(this, message + ": " + exception.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        logger.log(java.util.logging.Level.SEVERE, message, exception);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
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
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new Dashboard().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    // End of variables declaration//GEN-END:variables
}
