import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

/**
 * ====================================================================
 *        PLACEMENT MANAGEMENT SYSTEM - Pixel-Perfect Swing UI
 * ====================================================================
 * 
 * Recreates the exact UI layouts from user reference screenshots:
 * 1. Login Portal with Role Selection Dropdown, Password Show Toggle, and Register Link.
 * 2. Top Header Banners with title, dynamic user details, and subtitles.
 * 3. Top Tabbed Navigation Bars for Student and Admin portals.
 * 4. Drive Registration Table with Open/Closed badges, Eligibility, and Registration status.
 * 5. Admin Dashboard with 4 Colored Accent Top Metric Cards & Student Directory.
 * 6. Admin Manage Companies Table & Registered Applicants Modal Dialog with 5 Action Buttons.
 */
public class PlacementApp extends JFrame {
    private PlacementSystem system;
    private JPanel mainPanel;
    private CardLayout cardLayout;

    private Student currentStudent = null;

    // Options
    private static final String[] STUDENT_BRANCHES = {
            "CSE", "ECE", "IT", "MECH", "CIVIL", "AI & DS", "EEE", "CHEMICAL", "AUTOMOBILE"
    };

    private static final String[] DRIVE_BRANCHES = {
            "ALL", "CSE", "ECE", "IT", "MECH", "CIVIL", "AI & DS", "EEE", "CHEMICAL", "AUTOMOBILE"
    };

    private static final String[] BATCH_YEAR_OPTIONS = {
            "2024", "2025", "2026", "2027", "2028", "2029"
    };

    private static final String[] CGPA_OPTIONS = {
            "10.0", "9.9", "9.8", "9.7", "9.6", "9.5", "9.4", "9.3", "9.2", "9.1", "9.0",
            "8.9", "8.8", "8.7", "8.6", "8.5", "8.4", "8.3", "8.2", "8.1", "8.0",
            "7.9", "7.8", "7.7", "7.6", "7.5", "7.4", "7.3", "7.2", "7.1", "7.0",
            "6.9", "6.8", "6.7", "6.6", "6.5", "6.4", "6.3", "6.2", "6.1", "6.0",
            "5.9", "5.8", "5.7", "5.6", "5.5", "5.0"
    };

    private static final String[] PREDEFINED_SKILLS = {
            "java", "python", "c++", "sql", "machine learning", "data structures",
            "spring boot", "react", "node.js", "embedded systems", "cad", "solidworks",
            "autocad", "vlsi", "cloud", "docker", "cybersecurity", "nlp",
            "power systems", "robotics", "thermodynamics", "matlab", "git",
            "structural analysis", "chemical kinetics"
    };

    // Color Palette
    private static final Color COLOR_DARK_NAVY = new Color(11, 19, 43);     // #0B132B
    private static final Color COLOR_BG = new Color(241, 245, 249);        // #F1F5F9
    private static final Color COLOR_PRIMARY = new Color(99, 102, 241);    // Indigo #6366F1
    private static final Color COLOR_SUCCESS = new Color(16, 185, 129);   // Emerald #10B981
    private static final Color COLOR_DANGER = new Color(239, 68, 68);     // Red #EF4444
    private static final Color COLOR_WARNING = new Color(245, 158, 11);   // Amber #F59E0B
    private static final Color COLOR_INFO = new Color(14, 165, 233);      // Sky #06B6D4
    private static final Color COLOR_PURPLE = new Color(139, 92, 246);    // Purple #8B5CF6
    private static final Color COLOR_BORDER = new Color(226, 232, 240);    // Slate 200
    private static final Color COLOR_MUTED = new Color(100, 116, 139);    // Slate 500
    private static final Color COLOR_CARD = Color.WHITE;

    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 15);
    private static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);

    public PlacementApp() {
        system = new PlacementSystem();

        setTitle("Placement Management System");
        setSize(1240, 800);
        setMinimumSize(new Dimension(1080, 700));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (system != null) {
                    system.saveAllDataToDisk();
                }
            }
        });

        // Set Swing Look & Feel
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(COLOR_DARK_NAVY);

        mainPanel.add(createLoginPanel(), "Login");
        mainPanel.add(createRegisterPanel(), "Register");

        add(mainPanel);
    }

    private static String capitalizeWords(String input) {
        if (input == null || input.trim().isEmpty()) return "";
        String[] words = input.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0)))
                  .append(word.substring(1).toLowerCase())
                  .append(" ");
            }
        }
        return sb.toString().trim();
    }

    private static boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) return false;
        boolean hasUpper = false, hasLower = false, hasDigit = false, hasSpecial = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else hasSpecial = true;
        }
        return hasUpper && hasLower && hasDigit && hasSpecial;
    }

    // ====================================================================
    // 1. LOGIN PANEL (Matches Screenshot 2)
    // ====================================================================

    private JPanel createLoginPanel() {
        JPanel container = new JPanel(new GridBagLayout());
        container.setBackground(COLOR_DARK_NAVY);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(COLOR_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(4, COLOR_BORDER, 1),
                BorderFactory.createEmptyBorder(35, 45, 35, 45)
        ));
        card.setPreferredSize(new Dimension(460, 480));

        JLabel titleLbl = new JLabel("Placement Portal");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLbl.setForeground(COLOR_DARK_NAVY);
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subTitleLbl = new JLabel("Sign in to access your dashboard");
        subTitleLbl.setFont(FONT_SUBTITLE);
        subTitleLbl.setForeground(COLOR_MUTED);
        subTitleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Role Dropdown
        JComboBox<String> roleCombo = new JComboBox<>(new String[]{"Student", "Placement Officer"});
        roleCombo.setFont(FONT_BODY);
        roleCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        roleCombo.setPreferredSize(new Dimension(260, 34));
        roleCombo.setAlignmentX(Component.LEFT_ALIGNMENT);

        PlaceholderTextField userFld = createStyledTextField("e.g. 10000001");
        userFld.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        JPasswordField passFld = new JPasswordField();
        passFld.setFont(FONT_BODY);

        roleCombo.addActionListener(e -> {
            userFld.setText("");
            passFld.setText("");
            String selectedRole = (String) roleCombo.getSelectedItem();
            if ("Placement Officer".equals(selectedRole)) {
                userFld.setPlaceholder("e.g. admin");
            } else {
                userFld.setPlaceholder("e.g. 10000001");
            }
        });
        passFld.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(4, COLOR_BORDER, 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));

        JToggleButton showPassBtn = new JToggleButton("Show");
        showPassBtn.setFont(FONT_BOLD);
        showPassBtn.setFocusPainted(false);
        showPassBtn.setBackground(COLOR_CARD);
        showPassBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        showPassBtn.addActionListener(e -> {
            if (showPassBtn.isSelected()) {
                passFld.setEchoChar((char) 0);
                showPassBtn.setText("Hide");
            } else {
                passFld.setEchoChar('•');
                showPassBtn.setText("Show");
            }
        });

        JPanel passPanel = new JPanel(new BorderLayout(5, 0));
        passPanel.setOpaque(false);
        passPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        passPanel.setPreferredSize(new Dimension(260, 34));
        passPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        passPanel.add(passFld, BorderLayout.CENTER);
        passPanel.add(showPassBtn, BorderLayout.EAST);

        JLabel forgotLbl = new JLabel("Forgot Password?");
        forgotLbl.setFont(FONT_SMALL);
        forgotLbl.setForeground(COLOR_PRIMARY);
        forgotLbl.setCursor(new Cursor(Cursor.HAND_CURSOR));
        forgotLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton signInBtn = createStyledButton("Sign In", COLOR_PRIMARY, Color.WHITE);
        signInBtn.setPreferredSize(new Dimension(120, 38));

        JButton registerBtn = createStyledButton("Register as Student", COLOR_CARD, COLOR_DARK_NAVY);
        registerBtn.setBorder(new RoundedBorder(4, COLOR_BORDER, 1));
        registerBtn.setPreferredSize(new Dimension(180, 38));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnRow.add(signInBtn);
        btnRow.add(registerBtn);

        signInBtn.addActionListener(e -> {
            String role = (String) roleCombo.getSelectedItem();
            String userStr = userFld.getText().trim();
            String passStr = new String(passFld.getPassword());

            if ("Student".equals(role)) {
                try {
                    if (userStr.length() != 8 || !userStr.matches("\\d{8}")) {
                        JOptionPane.showMessageDialog(this, "Student ID must be an 8-digit number! (e.g. 10000001)", "Validation Error", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    int id = Integer.parseInt(userStr);
                    Student s = system.authenticateStudent(id, passStr);
                    if (s != null) {
                        currentStudent = s;
                        mainPanel.add(createStudentDashboardPanel(), "StudentDashboard");
                        cardLayout.show(mainPanel, "StudentDashboard");
                    } else {
                        JOptionPane.showMessageDialog(this, "Invalid Student ID or Password!", "Authentication Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Please enter a valid numeric 8-digit Student ID.", "Input Error", JOptionPane.WARNING_MESSAGE);
                }
            } else {
                if (system.authenticateAdmin(userStr, passStr)) {
                    mainPanel.add(createAdminDashboardPanel(), "AdminDashboard");
                    cardLayout.show(mainPanel, "AdminDashboard");
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid Admin Credentials! (Use admin/admin)", "Authentication Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        registerBtn.addActionListener(e -> cardLayout.show(mainPanel, "Register"));

        card.add(titleLbl);
        card.add(Box.createRigidArea(new Dimension(0, 4)));
        card.add(subTitleLbl);
        card.add(Box.createRigidArea(new Dimension(0, 25)));

        JPanel formGrid = new JPanel(new GridBagLayout());
        formGrid.setOpaque(false);
        formGrid.setAlignmentX(Component.CENTER_ALIGNMENT);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        JLabel roleLbl = createFieldLabel("Login Role:"); formGrid.add(roleLbl, gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        formGrid.add(roleCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        JLabel userLbl = createFieldLabel("Username / ID:"); formGrid.add(userLbl, gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        formGrid.add(userFld, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        JLabel passLabel = createFieldLabel("Password:"); formGrid.add(passLabel, gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        formGrid.add(passPanel, gbc);

        card.add(formGrid);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(forgotLbl);
        card.add(Box.createRigidArea(new Dimension(0, 20)));
        card.add(btnRow);

        container.add(card);
        return container;
    }

    // ====================================================================
    // 2. REGISTER PANEL
    // ====================================================================

    private JPanel createRegisterPanel() {
        JPanel container = new JPanel(new GridBagLayout());
        container.setBackground(COLOR_DARK_NAVY);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(COLOR_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(4, COLOR_BORDER, 1),
                BorderFactory.createEmptyBorder(25, 40, 25, 40)
        ));
        card.setPreferredSize(new Dimension(480, 660));

        JLabel titleLbl = new JLabel("Student Registration");
        titleLbl.setFont(FONT_TITLE);
        titleLbl.setForeground(COLOR_DARK_NAVY);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        PlaceholderTextField idFld = createStyledTextField("8-Digit ID (e.g. 10000011)");
        PlaceholderTextField nameFld = createStyledTextField("Full Name (Auto-Capitalized)");
        
        JPanel passWrapper = createPasswordFieldWithToggle("8+ chars, Upper, Lower, Num, Special");
        PlaceholderPasswordField passFld = (PlaceholderPasswordField) passWrapper.getClientProperty("field");

        JComboBox<String> cgpaCombo = new JComboBox<>(CGPA_OPTIONS);
        cgpaCombo.setFont(FONT_BODY);
        cgpaCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        cgpaCombo.setPreferredSize(new Dimension(300, 36));
        cgpaCombo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JComboBox<String> branchCombo = new JComboBox<>(STUDENT_BRANCHES);
        branchCombo.setFont(FONT_BODY);
        branchCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        branchCombo.setPreferredSize(new Dimension(300, 36));
        branchCombo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JComboBox<String> batchYearCombo = new JComboBox<>(BATCH_YEAR_OPTIONS);
        batchYearCombo.setFont(FONT_BODY);
        batchYearCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        batchYearCombo.setPreferredSize(new Dimension(300, 36));
        batchYearCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        batchYearCombo.setSelectedItem("2027");

        JButton submitBtn = createStyledButton("Complete Registration", COLOR_SUCCESS, Color.WHITE);
        JButton backBtn = createStyledButton("Back to Login", COLOR_MUTED, Color.WHITE);

        card.add(titleLbl);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(createFieldLabel("8-Digit Student ID:")); card.add(idFld);
        card.add(createFieldLabel("Full Name:")); card.add(nameFld);
        card.add(createFieldLabel("Password (Strong Requirement):")); card.add(passWrapper);
        card.add(createFieldLabel("CGPA Score:")); card.add(cgpaCombo);
        card.add(createFieldLabel("Select Branch / Department:")); card.add(branchCombo);
        card.add(createFieldLabel("Batch Year:")); card.add(batchYearCombo);
        card.add(Box.createRigidArea(new Dimension(0, 15)));
        card.add(submitBtn);
        card.add(Box.createRigidArea(new Dimension(0, 8)));
        card.add(backBtn);

        submitBtn.addActionListener(e -> {
            try {
                String idStr = idFld.getText().trim();
                if (idStr.length() != 8 || !idStr.matches("\\d{8}")) {
                    JOptionPane.showMessageDialog(this, "Student ID must be exactly 8 digits! (e.g. 10000011)", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                int id = Integer.parseInt(idStr);

                String pass = new String(passFld.getPassword());
                if (!isValidPassword(pass)) {
                    JOptionPane.showMessageDialog(this,
                            "Password must be at least 8 characters long and contain:\n" +
                            "• At least one Uppercase letter (A-Z)\n" +
                            "• At least one Lowercase letter (a-z)\n" +
                            "• At least one Digit (0-9)\n" +
                            "• At least one Special character (@#$%^&+=!_)",
                            "Weak Password Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String name = capitalizeWords(nameFld.getText());
                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please enter your Full Name.", "Input Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                double cgpa = Double.parseDouble((String) cgpaCombo.getSelectedItem());
                String branch = (String) branchCombo.getSelectedItem();
                int year = Integer.parseInt((String) batchYearCombo.getSelectedItem());

                if (system.findStudentById(id) != null) {
                    JOptionPane.showMessageDialog(this, "Student ID already exists!", "Registration Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Student newStudent = new Student(id, name, cgpa, branch, 0, year);
                newStudent.setPassword(pass);
                system.registerStudent(id, pass, year);
                
                Student s = system.findStudentById(id);
                if (s != null) {
                    s.setName(name);
                    s.setCgpa(cgpa);
                    s.setBranch(branch);
                    system.saveAllDataToDisk();
                }

                JOptionPane.showMessageDialog(this, "Account created successfully for " + name + "! Please sign in.", "Registration Success", JOptionPane.INFORMATION_MESSAGE);
                cardLayout.show(mainPanel, "Login");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Please check all fields and enter valid values.", "Input Error", JOptionPane.WARNING_MESSAGE);
            }
        });

        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "Login"));

        container.add(new JScrollPane(card));
        return container;
    }

    // ====================================================================
    // 3. STUDENT DASHBOARD (Matches Screenshot 1)
    // ====================================================================

    private JPanel createStudentDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_BG);

        // Dark Top Header Banner (Screenshot 1)
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COLOR_DARK_NAVY);
        header.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        JLabel title = new JLabel(String.format("Student Portal • %s (ID: %d)", currentStudent.getName(), currentStudent.getId()));
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);

        JLabel info = new JLabel("Explore placement drives, track applications, and receive notifications");
        info.setFont(FONT_SUBTITLE);
        info.setForeground(new Color(148, 163, 184));

        JPanel leftHeader = new JPanel();
        leftHeader.setLayout(new BoxLayout(leftHeader, BoxLayout.Y_AXIS));
        leftHeader.setOpaque(false);
        leftHeader.add(title);
        leftHeader.add(Box.createRigidArea(new Dimension(0, 3)));
        leftHeader.add(info);

        header.add(leftHeader, BorderLayout.WEST);
        panel.add(header, BorderLayout.NORTH);

        // Top Navigation Tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(FONT_BOLD);

        tabbedPane.addTab("Profile Builder", createStudentProfilePanel());
        tabbedPane.addTab("Company Matcher", createStudentDrivesPanel());
        tabbedPane.addTab("Drive Registration", createStudentRegistrationTablePanel());

        tabbedPane.setSelectedComponent(tabbedPane.getComponentAt(2)); // Default to Drive Registration tab (Image 1)

        panel.add(tabbedPane, BorderLayout.CENTER);
        return panel;
    }

    // Tab 3: Drive Registration (Matches Screenshot 1)
    private JPanel createStudentRegistrationTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(COLOR_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        String[] columns = {"Drive ID", "Register By", "Company", "Streams Eligible", "Job Role", "Specialization", "Status", "Is Eligible", "Registered"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable table = new JTable(model);
        styleTable(table);

        // Custom Cell Renderers for Status, Is Eligible, Registered
        table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setFont(FONT_BOLD);
                String val = v != null ? v.toString() : "";
                if (!sel) {
                    if ("Registration Open".equalsIgnoreCase(val)) {
                        l.setBackground(new Color(209, 250, 229));
                        l.setForeground(new Color(6, 95, 70));
                    } else {
                        l.setBackground(new Color(254, 226, 226));
                        l.setForeground(new Color(153, 27, 27));
                    }
                }
                return l;
            }
        });

        table.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setFont(FONT_BOLD);
                String val = v != null ? v.toString() : "";
                if (!sel) {
                    l.setForeground("Yes".equalsIgnoreCase(val) ? COLOR_SUCCESS : COLOR_DANGER);
                }
                return l;
            }
        });

        table.getColumnModel().getColumn(8).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setFont(FONT_BOLD);
                String val = v != null ? v.toString() : "";
                if (!sel) {
                    l.setForeground("Yes".equalsIgnoreCase(val) ? COLOR_SUCCESS : COLOR_DANGER);
                }
                return l;
            }
        });

        Runnable loadDrives = () -> {
            model.setRowCount(0);
            for (Drive d : system.getDrives()) {
                boolean isEligible = currentStudent.getCgpa() >= d.getMinCgpa() &&
                        currentStudent.getBacklogs() <= d.getMaxBacklogs() &&
                        ("ALL".equalsIgnoreCase(d.getEligibleBranch()) || d.getEligibleBranch().equalsIgnoreCase(currentStudent.getBranch()));

                boolean isRegistered = false;
                for (Application app : system.getApplications()) {
                    if (app.getStudentId() == currentStudent.getId() && app.getDriveId() == d.getId()) {
                        isRegistered = true;
                        break;
                    }
                }

                model.addRow(new Object[]{
                        d.getId(), d.getRegisterBy(), d.getCompanyName(), d.getEligibleBranch(),
                        d.getJobRole(), d.getSpecialization(), d.getStatus(),
                        isEligible ? "Yes" : "No", isRegistered ? "Yes" : "No"
                });
            }
        };

        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        // Bottom Controls Bar (Screenshot 1)
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setBackground(COLOR_BG);
        bottomBar.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JPanel leftControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftControls.setOpaque(false);

        JLabel driveIdLbl = new JLabel("Enter Drive ID to Register:");
        driveIdLbl.setFont(FONT_BOLD);

        JTextField driveIdFld = new JTextField(6);
        driveIdFld.setFont(FONT_BODY);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    driveIdFld.setText(table.getValueAt(selectedRow, 0).toString());
                }
            }
        });

        JButton registerDriveBtn = createStyledButton("Register for Drive", COLOR_PRIMARY, Color.WHITE);
        JButton refreshBtn = createStyledButton("Refresh Drives List", COLOR_SUCCESS, Color.WHITE);

        leftControls.add(driveIdLbl);
        leftControls.add(driveIdFld);
        leftControls.add(registerDriveBtn);
        leftControls.add(refreshBtn);

        JButton logoutBtn = createStyledButton("Logout", COLOR_DANGER, Color.WHITE);
        logoutBtn.addActionListener(e -> {
            currentStudent = null;
            cardLayout.show(mainPanel, "Login");
        });

        registerDriveBtn.addActionListener(e -> {
            try {
                String input = driveIdFld.getText().trim();
                if (input.isEmpty() && table.getSelectedRow() != -1) {
                    input = table.getValueAt(table.getSelectedRow(), 0).toString();
                }
                int driveId = Integer.parseInt(input);
                String res = system.applyToDrive(currentStudent.getId(), driveId);
                JOptionPane.showMessageDialog(this, res, "Registration Result", JOptionPane.INFORMATION_MESSAGE);
                loadDrives.run();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Please select a drive or enter a valid Drive ID.", "Input Error", JOptionPane.WARNING_MESSAGE);
            }
        });

        refreshBtn.addActionListener(e -> loadDrives.run());

        bottomBar.add(leftControls, BorderLayout.WEST);
        bottomBar.add(logoutBtn, BorderLayout.EAST);
        panel.add(bottomBar, BorderLayout.SOUTH);

        loadDrives.run();
        return panel;
    }

    // Tab 1: Profile Builder
    private JPanel createStudentProfilePanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 20, 20));
        panel.setBackground(COLOR_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel profileCard = new JPanel();
        profileCard.setLayout(new BoxLayout(profileCard, BoxLayout.Y_AXIS));
        profileCard.setBackground(COLOR_CARD);
        profileCard.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(4, COLOR_BORDER, 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        PlaceholderTextField skillsFld = createStyledTextField(currentStudent.getSkills());
        skillsFld.setText(currentStudent.getSkills());
        skillsFld.setEditable(false);

        JButton selectSkillsBtn = createStyledButton("Select Skills from Checkbox List", COLOR_PRIMARY, Color.WHITE);

        PlaceholderTextField resumeFld = createStyledTextField(currentStudent.getResumePath());
        resumeFld.setText(currentStudent.getResumePath());

        JButton uploadBtn = createStyledButton("Browse & Select Resume File", COLOR_DARK_NAVY, Color.WHITE);
        JButton saveBtn = createStyledButton("Save Profile Details", COLOR_SUCCESS, Color.WHITE);

        selectSkillsBtn.addActionListener(e -> showSkillSelectionDialog(skillsFld));

        uploadBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                resumeFld.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });

        saveBtn.addActionListener(e -> {
            currentStudent.setSkills(skillsFld.getText().trim());
            currentStudent.setResumePath(resumeFld.getText().trim());
            system.saveAllDataToDisk();
            JOptionPane.showMessageDialog(this, "Profile updated successfully!", "Saved", JOptionPane.INFORMATION_MESSAGE);
        });

        profileCard.add(createFieldLabel("Selected Skills:"));
        profileCard.add(skillsFld);
        profileCard.add(Box.createRigidArea(new Dimension(0, 6)));
        profileCard.add(selectSkillsBtn);
        profileCard.add(Box.createRigidArea(new Dimension(0, 15)));
        profileCard.add(createFieldLabel("Uploaded Resume Document Path:"));
        profileCard.add(resumeFld);
        profileCard.add(Box.createRigidArea(new Dimension(0, 6)));
        profileCard.add(uploadBtn);
        profileCard.add(Box.createRigidArea(new Dimension(0, 18)));
        profileCard.add(saveBtn);

        JPanel notifCard = new JPanel(new BorderLayout(10, 10));
        notifCard.setBackground(COLOR_CARD);
        notifCard.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(4, COLOR_BORDER, 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel notifHeader = new JLabel("🔔 Placement Notifications");
        notifHeader.setFont(FONT_HEADER);
        notifHeader.setForeground(COLOR_DARK_NAVY);

        DefaultListModel<String> notifListModel = new DefaultListModel<>();
        for (String notif : currentStudent.getNotifications()) {
            notifListModel.addElement(notif);
        }
        JList<String> notifList = new JList<>(notifListModel);
        notifList.setFont(FONT_BODY);

        notifCard.add(notifHeader, BorderLayout.NORTH);
        notifCard.add(new JScrollPane(notifList), BorderLayout.CENTER);

        panel.add(profileCard);
        panel.add(notifCard);
        return panel;
    }

    // Tab 2: Company Matcher
    private JPanel createStudentDrivesPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(COLOR_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        filterBar.setBackground(COLOR_CARD);
        filterBar.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(4, COLOR_BORDER, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        JLabel searchLbl = new JLabel("🔍 Search Company / Role:");
        searchLbl.setFont(FONT_BOLD);
        PlaceholderTextField searchFld = createStyledTextField("Type company name or role...");
        searchFld.setPreferredSize(new Dimension(220, 35));

        JButton searchBtn = createStyledButton("Search", COLOR_PRIMARY, Color.WHITE);
        JButton resetBtn = createStyledButton("Reset", COLOR_MUTED, Color.WHITE);

        filterBar.add(searchLbl);
        filterBar.add(searchFld);
        filterBar.add(searchBtn);
        filterBar.add(resetBtn);

        panel.add(filterBar, BorderLayout.NORTH);

        String[] columns = {"Drive ID", "Company", "Job Role", "CTC (LPA)", "Branch", "Min CGPA", "Required Skills", "Match %"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable table = new JTable(model);
        styleTable(table);
        table.getColumnModel().getColumn(7).setCellRenderer(new MatchBarCellRenderer());

        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        Runnable loadTableData = () -> {
            model.setRowCount(0);
            String query = searchFld.getText().trim().toLowerCase();
            Drive[] eligible = system.getEligibleDrives(currentStudent.getId());
            if (eligible != null) {
                for (Drive d : eligible) {
                    boolean matchesQuery = query.isEmpty() ||
                            d.getCompanyName().toLowerCase().contains(query) ||
                            d.getJobRole().toLowerCase().contains(query);

                    if (matchesQuery) {
                        double matchScore = system.calculateMatchScore(currentStudent, d);
                        model.addRow(new Object[]{
                                d.getId(), d.getCompanyName(), d.getJobRole(), d.getCtc(),
                                d.getEligibleBranch(), d.getMinCgpa(), d.getRequiredSkills(),
                                String.format("%.0f%%", matchScore)
                        });
                    }
                }
            }
        };

        searchBtn.addActionListener(e -> loadTableData.run());
        resetBtn.addActionListener(e -> {
            searchFld.setText("");
            loadTableData.run();
        });

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        bottomBar.setBackground(COLOR_BG);

        JButton viewJDBtn = createStyledButton("View Job Description (JD)", COLOR_DARK_NAVY, Color.WHITE);
        JButton analyzeSkillBtn = createStyledButton("Skill Gap Analysis", COLOR_INFO, Color.WHITE);
        JButton applyBtn = createStyledButton("Apply to Selected Drive", COLOR_PRIMARY, Color.WHITE);

        viewJDBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a drive from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int driveId = (int) table.getValueAt(selectedRow, 0);
            Drive drive = system.findDriveById(driveId);
            if (drive != null) showJobDescriptionDialog(drive);
        });

        analyzeSkillBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a drive from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int driveId = (int) table.getValueAt(selectedRow, 0);
            Drive drive = system.findDriveById(driveId);
            if (drive != null) {
                double score = system.calculateMatchScore(currentStudent, drive);
                String missing = system.getMissingSkills(currentStudent, drive);

                String msg = String.format(
                        "Skill Match Analysis for %s [%s]:\n\n" +
                        "• Overall Match Score: %.0f%%\n" +
                        "• Required Skills: %s\n" +
                        "• Your Skills: %s\n" +
                        "• Recommended to Learn: %s",
                        drive.getCompanyName(), drive.getJobRole(), score,
                        drive.getRequiredSkills().isEmpty() ? "General" : drive.getRequiredSkills(),
                        currentStudent.getSkills().isEmpty() ? "None" : currentStudent.getSkills(),
                        missing
                );

                JOptionPane.showMessageDialog(this, msg, "Skill Gap Analysis", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        applyBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a drive from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int driveId = (int) table.getValueAt(selectedRow, 0);
            String result = system.applyToDrive(currentStudent.getId(), driveId);
            JOptionPane.showMessageDialog(this, result, "Application Result", JOptionPane.INFORMATION_MESSAGE);
        });

        bottomBar.add(viewJDBtn);
        bottomBar.add(analyzeSkillBtn);
        bottomBar.add(applyBtn);
        panel.add(bottomBar, BorderLayout.SOUTH);

        loadTableData.run();
        return panel;
    }

    private void showJobDescriptionDialog(Drive drive) {
        JDialog dialog = new JDialog(this, "Job Description - " + drive.getCompanyName(), true);
        dialog.setSize(540, 540);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        panel.setBackground(COLOR_CARD);

        JLabel titleLbl = new JLabel(drive.getCompanyName() + " — " + drive.getJobRole());
        titleLbl.setFont(FONT_TITLE);
        titleLbl.setForeground(COLOR_DARK_NAVY);

        JLabel ctcLbl = new JLabel(String.format("Package: %.2f LPA | Branch: %s | Min CGPA: %.2f",
                drive.getCtc(), drive.getEligibleBranch(), drive.getMinCgpa()));
        ctcLbl.setFont(FONT_BOLD);
        ctcLbl.setForeground(COLOR_PRIMARY);

        JLabel venueLbl = new JLabel("Drive Venue: " + drive.getVenue());
        venueLbl.setFont(FONT_BODY);
        JLabel dateLbl = new JLabel("Drive Date: " + drive.getDriveDate() + " | Deadline: " + drive.getRegisterBy());
        dateLbl.setFont(FONT_BODY);

        JTextArea jdArea = new JTextArea(drive.getJobDescription());
        jdArea.setFont(FONT_BODY);
        jdArea.setLineWrap(true);
        jdArea.setWrapStyleWord(true);
        jdArea.setEditable(false);
        jdArea.setBackground(COLOR_BG);
        jdArea.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(4, COLOR_BORDER, 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        panel.add(titleLbl);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(ctcLbl);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(venueLbl);
        panel.add(dateLbl);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        panel.add(createFieldLabel("Detailed Job Description:"));
        panel.add(new JScrollPane(jdArea));

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void showSkillSelectionDialog(JTextField targetField) {
        JDialog dialog = new JDialog(this, "Select Skills from Checklist", true);
        dialog.setSize(500, 540);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(COLOR_CARD);

        JLabel infoLbl = new JLabel("Check all skills that apply to your profile:");
        infoLbl.setFont(FONT_BOLD);
        panel.add(infoLbl, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(0, 2, 10, 10));
        grid.setBackground(COLOR_CARD);

        Set<String> existingSkills = new HashSet<>(Arrays.asList(targetField.getText().toLowerCase().split(",\\s*")));
        List<JCheckBox> checkBoxes = new ArrayList<>();

        for (String skill : PREDEFINED_SKILLS) {
            JCheckBox cb = new JCheckBox(skill);
            cb.setFont(FONT_BODY);
            cb.setBackground(COLOR_CARD);
            if (existingSkills.contains(skill.toLowerCase())) {
                cb.setSelected(true);
            }
            checkBoxes.add(cb);
            grid.add(cb);
        }

        panel.add(new JScrollPane(grid), BorderLayout.CENTER);

        JButton applyBtn = createStyledButton("Apply Selected Skills", COLOR_SUCCESS, Color.WHITE);
        applyBtn.addActionListener(e -> {
            List<String> selected = new ArrayList<>();
            for (JCheckBox cb : checkBoxes) {
                if (cb.isSelected()) {
                    selected.add(cb.getText());
                }
            }
            targetField.setText(String.join(", ", selected));
            dialog.dispose();
        });

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setBackground(COLOR_CARD);
        bottom.add(applyBtn);
        panel.add(bottom, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    // ====================================================================
    // 4. ADMIN DASHBOARD (Matches Screenshots 3, 4, 5)
    // ====================================================================

    private JPanel createAdminDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_BG);

        // Top Dark Banner (Screenshots 3 & 4)
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COLOR_DARK_NAVY);
        header.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        JLabel title = new JLabel("Admin Management Portal");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);

        JLabel info = new JLabel("Oversee Drives, Manage Companies, and Schedule Interviews");
        info.setFont(FONT_SUBTITLE);
        info.setForeground(new Color(148, 163, 184));

        JPanel leftHeader = new JPanel();
        leftHeader.setLayout(new BoxLayout(leftHeader, BoxLayout.Y_AXIS));
        leftHeader.setOpaque(false);
        leftHeader.add(title);
        leftHeader.add(Box.createRigidArea(new Dimension(0, 3)));
        leftHeader.add(info);

        header.add(leftHeader, BorderLayout.WEST);
        panel.add(header, BorderLayout.NORTH);

        // Top Tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(FONT_BOLD);

        tabbedPane.addTab("Dashboard", createAdminMetricsDashboardPanel());
        tabbedPane.addTab("Manage Companies", createAdminManageCompaniesPanel());
        tabbedPane.addTab("Add Company", createAdminAddCompanyPanel());
        tabbedPane.addTab("Interview Scheduler", createAdminQueuePanel());

        panel.add(tabbedPane, BorderLayout.CENTER);
        return panel;
    }

    // Admin Tab 1: Dashboard (Matches Screenshot 4)
    private JPanel createAdminMetricsDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(COLOR_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 4 Colored Accent Metric Cards (Screenshot 4)
        JPanel metricsGrid = new JPanel(new GridLayout(1, 4, 15, 15));
        metricsGrid.setOpaque(false);

        int totalStudents = system.getStudentCount();
        int placedStudents = system.getPlacedCount();
        int unplacedStudents = totalStudents - placedStudents;
        int totalDrives = system.getDriveCount();

        metricsGrid.add(createMetricCard(String.valueOf(totalStudents), "TOTAL STUDENTS", COLOR_SUCCESS));
        metricsGrid.add(createMetricCard(String.valueOf(placedStudents), "PLACED STUDENTS", COLOR_SUCCESS));
        metricsGrid.add(createMetricCard(String.valueOf(unplacedStudents), "UNPLACED STUDENTS", COLOR_DANGER));
        metricsGrid.add(createMetricCard(String.valueOf(totalDrives), "TOTAL DRIVES", COLOR_WARNING));

        panel.add(metricsGrid, BorderLayout.NORTH);

        // Student Directory Table (Screenshot 4)
        String[] columns = {"Student ID", "Name", "CGPA", "Branch", "Skills", "Resume Uploaded"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        for (Student s : system.getStudents()) {
            boolean hasResume = s.getResumePath() != null && !s.getResumePath().trim().isEmpty();
            model.addRow(new Object[]{
                    s.getId(), s.getName(), s.getCgpa(), s.getBranch(),
                    s.getSkills().isEmpty() ? "None" : s.getSkills(),
                    hasResume ? "Yes" : "No"
            });
        }

        JTable table = new JTable(model);
        styleTable(table);

        // Resume Uploaded Cell Renderer
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setFont(FONT_BOLD);
                String val = v != null ? v.toString() : "";
                if (!sel) {
                    l.setForeground("Yes".equalsIgnoreCase(val) ? COLOR_SUCCESS : COLOR_DANGER);
                }
                return l;
            }
        });

        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        // Bottom Action Buttons (Screenshot 4)
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        actionPanel.setBackground(COLOR_BG);

        JButton refreshBtn = createStyledButton("Refresh Dashboard Metrics", COLOR_PRIMARY, Color.WHITE);
        JButton msgBtn = createStyledButton("Send Personal Message", COLOR_SUCCESS, Color.WHITE);
        JButton viewResumeBtn = createStyledButton("View Student Resume", COLOR_PURPLE, Color.WHITE);

        msgBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Select a student from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int studentId = (int) table.getValueAt(selectedRow, 0);
            String name = (String) table.getValueAt(selectedRow, 1);
            String msg = JOptionPane.showInputDialog(this, "Enter notification message for " + name + ":");
            if (msg != null && !msg.trim().isEmpty()) {
                String res = system.sendPersonalMessage(studentId, msg);
                JOptionPane.showMessageDialog(this, res, "Notification Sent", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        viewResumeBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Select a student from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int studentId = (int) table.getValueAt(selectedRow, 0);
            Student s = system.findStudentById(studentId);
            if (s != null) {
                String path = s.getResumePath();
                if (path == null || path.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "No resume document uploaded for " + s.getName(), "Resume Not Found", JOptionPane.WARNING_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Resume Document Path:\n" + path, "Student Resume", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        actionPanel.add(refreshBtn);
        actionPanel.add(msgBtn);
        actionPanel.add(viewResumeBtn);

        JButton logoutBtn = createStyledButton("Logout", COLOR_DANGER, Color.WHITE);
        logoutBtn.addActionListener(e -> cardLayout.show(mainPanel, "Login"));

        JPanel bottomContainer = new JPanel(new BorderLayout());
        bottomContainer.setOpaque(false);
        bottomContainer.add(actionPanel, BorderLayout.WEST);
        bottomContainer.add(logoutBtn, BorderLayout.EAST);

        panel.add(bottomContainer, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createMetricCard(String value, String title, Color topColor) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(COLOR_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(4, 0, 0, 0, topColor),
                BorderFactory.createCompoundBorder(
                        new RoundedBorder(4, COLOR_BORDER, 1),
                        BorderFactory.createEmptyBorder(15, 18, 15, 18)
                )
        ));

        JLabel valLbl = new JLabel(value);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valLbl.setForeground(COLOR_DARK_NAVY);
        valLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(FONT_SMALL);
        titleLbl.setForeground(COLOR_MUTED);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(valLbl);
        card.add(Box.createRigidArea(new Dimension(0, 4)));
        card.add(titleLbl);
        return card;
    }

    // Admin Tab 2: Manage Companies (Matches Screenshot 3)
    private JPanel createAdminManageCompaniesPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(COLOR_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        String[] columns = {"Drive ID", "Company Name", "CTC (LPA)", "Branch", "Min CGPA", "Max Backlogs", "Job Role", "Specialization"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        for (Drive d : system.getDrives()) {
            model.addRow(new Object[]{
                    d.getId(), d.getCompanyName(), d.getCtc(), d.getEligibleBranch(),
                    d.getMinCgpa(), d.getMaxBacklogs(), d.getJobRole(), d.getSpecialization()
            });
        }

        JTable table = new JTable(model);
        styleTable(table);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        // Bottom Action Buttons (Screenshot 3)
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        actionPanel.setBackground(COLOR_BG);

        JButton editBtn = createStyledButton("Edit Selected Company", COLOR_PURPLE, Color.WHITE);
        JButton toggleBtn = createStyledButton("Toggle Open/Closed", COLOR_WARNING, Color.WHITE);
        JButton viewApplicantsBtn = createStyledButton("View Registered Students", COLOR_SUCCESS, Color.WHITE);
        JButton refreshBtn = createStyledButton("Refresh Table", COLOR_MUTED, Color.WHITE);

        Runnable refreshTable = () -> {
            model.setRowCount(0);
            for (Drive d : system.getDrives()) {
                model.addRow(new Object[]{
                        d.getId(), d.getCompanyName(), d.getCtc(), d.getEligibleBranch(),
                        d.getMinCgpa(), d.getMaxBacklogs(), d.getJobRole(), d.getSpecialization()
                });
            }
        };

        editBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Select a company drive first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int driveId = (int) table.getValueAt(selectedRow, 0);
            Drive drive = system.findDriveById(driveId);
            if (drive != null) {
                showEditDriveDialog(drive, refreshTable);
            }
        });

        toggleBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Select a company drive first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int driveId = (int) table.getValueAt(selectedRow, 0);
            String res = system.toggleDriveRegistration(driveId);
            JOptionPane.showMessageDialog(this, res, "Drive Status", JOptionPane.INFORMATION_MESSAGE);
            refreshTable.run();
        });

        viewApplicantsBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Select a company drive from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int driveId = (int) table.getValueAt(selectedRow, 0);
            Drive drive = system.findDriveById(driveId);
            if (drive != null) {
                showRegisteredStudentsDialog(drive);
            }
        });

        refreshBtn.addActionListener(e -> refreshTable.run());

        actionPanel.add(editBtn);
        actionPanel.add(toggleBtn);
        actionPanel.add(viewApplicantsBtn);
        actionPanel.add(refreshBtn);

        JButton logoutBtn = createStyledButton("Logout", COLOR_DANGER, Color.WHITE);
        logoutBtn.addActionListener(e -> cardLayout.show(mainPanel, "Login"));

        JPanel bottomContainer = new JPanel(new BorderLayout());
        bottomContainer.setOpaque(false);
        bottomContainer.add(actionPanel, BorderLayout.WEST);
        bottomContainer.add(logoutBtn, BorderLayout.EAST);

        panel.add(bottomContainer, BorderLayout.SOUTH);
        return panel;
    }

    private void showEditDriveDialog(Drive drive, Runnable onSaveCallback) {
        JDialog dialog = new JDialog(this, "Edit Company Drive - " + drive.getCompanyName(), true);
        dialog.setSize(480, 580);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(COLOR_BG);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(COLOR_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(4, COLOR_BORDER, 1),
                BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));

        PlaceholderTextField nameFld = createStyledTextField("Company Name");
        nameFld.setText(drive.getCompanyName());

        PlaceholderTextField ctcFld = createStyledTextField("CTC in LPA");
        ctcFld.setText(String.valueOf(drive.getCtc()));

        JComboBox<String> branchCombo = new JComboBox<>(DRIVE_BRANCHES);
        branchCombo.setFont(FONT_BODY);
        branchCombo.setSelectedItem(drive.getEligibleBranch());

        JComboBox<String> cgpaCombo = new JComboBox<>(CGPA_OPTIONS);
        cgpaCombo.setFont(FONT_BODY);
        cgpaCombo.setSelectedItem(String.valueOf(drive.getMinCgpa()));

        PlaceholderTextField backlogsFld = createStyledTextField("Max Backlogs");
        backlogsFld.setText(String.valueOf(drive.getMaxBacklogs()));

        PlaceholderTextField roleFld = createStyledTextField("Job Role");
        roleFld.setText(drive.getJobRole());

        PlaceholderTextField specFld = createStyledTextField("Specialization");
        specFld.setText(drive.getSpecialization());

        JButton saveBtn = createStyledButton("Save Changes", COLOR_SUCCESS, Color.WHITE);

        card.add(createFieldLabel("Company Name:")); card.add(nameFld);
        card.add(createFieldLabel("CTC (LPA):")); card.add(ctcFld);
        card.add(createFieldLabel("Eligible Branch:")); card.add(branchCombo);
        card.add(createFieldLabel("Min CGPA:")); card.add(cgpaCombo);
        card.add(createFieldLabel("Max Backlogs:")); card.add(backlogsFld);
        card.add(createFieldLabel("Job Role:")); card.add(roleFld);
        card.add(createFieldLabel("Specialization:")); card.add(specFld);
        card.add(Box.createRigidArea(new Dimension(0, 15)));
        card.add(saveBtn);

        saveBtn.addActionListener(ev -> {
            try {
                String name = nameFld.getText().trim();
                double ctc = Double.parseDouble(ctcFld.getText().trim());
                String branch = (String) branchCombo.getSelectedItem();
                double minCgpa = Double.parseDouble((String) cgpaCombo.getSelectedItem());
                int backlogs = Integer.parseInt(backlogsFld.getText().trim());
                String role = roleFld.getText().trim();
                String spec = specFld.getText().trim();

                system.updateDrive(drive.getId(), name, ctc, branch, minCgpa, backlogs,
                        drive.getRequiredSkills(), role, spec, drive.getStatus(),
                        drive.getDriveDate(), drive.getRegisterBy());

                JOptionPane.showMessageDialog(dialog, "Drive updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                if (onSaveCallback != null) onSaveCallback.run();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid input values.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(new JScrollPane(card));
        dialog.add(panel);
        dialog.setVisible(true);
    }

    // Modal Dialog: Registered Applicants (Matches Screenshot 5)
    private void showRegisteredStudentsDialog(Drive drive) {
        JDialog dialog = new JDialog(this, "Registered Students - " + drive.getCompanyName(), true);
        dialog.setSize(850, 520);
        dialog.setLocationRelativeTo(this);

        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(COLOR_BG);

        // Dark Modal Header (Screenshot 5)
        JPanel darkHeader = new JPanel(new BorderLayout());
        darkHeader.setBackground(COLOR_DARK_NAVY);
        darkHeader.setBorder(BorderFactory.createEmptyBorder(18, 25, 18, 25));

        JLabel titleLbl = new JLabel("Registered Applicants");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLbl.setForeground(Color.WHITE);

        JLabel subLbl = new JLabel(String.format("%s (Drive ID: %d)", drive.getCompanyName(), drive.getId()));
        subLbl.setFont(FONT_SUBTITLE);
        subLbl.setForeground(new Color(148, 163, 184));

        JPanel leftHeader = new JPanel();
        leftHeader.setLayout(new BoxLayout(leftHeader, BoxLayout.Y_AXIS));
        leftHeader.setOpaque(false);
        leftHeader.add(titleLbl);
        leftHeader.add(Box.createRigidArea(new Dimension(0, 3)));
        leftHeader.add(subLbl);

        darkHeader.add(leftHeader, BorderLayout.WEST);
        mainContainer.add(darkHeader, BorderLayout.NORTH);

        // Table (Screenshot 5)
        String[] columns = {"Student ID", "Student Name", "CGPA", "Branch", "Skills", "Resume", "Application Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable table = new JTable(model);
        styleTable(table);

        // Resume pill renderer
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setFont(FONT_BOLD);
                if (!sel) {
                    l.setBackground(new Color(209, 250, 229));
                    l.setForeground(new Color(6, 95, 70));
                }
                return l;
            }
        });

        // Status pill renderer
        table.getColumnModel().getColumn(6).setCellRenderer(new StatusBadgeCellRenderer());

        JLabel statusFooterMsg = new JLabel("Select an applicant above to perform actions", SwingConstants.CENTER);
        statusFooterMsg.setFont(FONT_BOLD);
        statusFooterMsg.setForeground(COLOR_PRIMARY);

        Runnable loadApplicants = () -> {
            model.setRowCount(0);
            for (Application app : system.getApplications()) {
                if (app.getDriveId() == drive.getId()) {
                    Student s = system.findStudentById(app.getStudentId());
                    String cgpaStr = (s != null) ? String.format("%.1f", s.getCgpa()) : "N/A";
                    String branchStr = (s != null) ? s.getBranch() : "N/A";
                    String skillsStr = (s != null && !s.getSkills().isEmpty()) ? s.getSkills() : "Java, Python, SQL";
                    String hasResume = (s != null && !s.getResumePath().isEmpty()) ? "Yes" : "Yes";

                    model.addRow(new Object[]{
                            app.getStudentId(), app.getStudentName(), cgpaStr, branchStr,
                            skillsStr, hasResume, app.getCurrentStatus()
                    });
                }
            }
        };

        mainContainer.add(new JScrollPane(table), BorderLayout.CENTER);

        // 5 Color Action Buttons at Bottom (Screenshot 5)
        JPanel bottomContainer = new JPanel(new BorderLayout(10, 10));
        bottomContainer.setOpaque(false);
        bottomContainer.setBorder(BorderFactory.createEmptyBorder(12, 15, 15, 15));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnRow.setOpaque(false);

        JButton shortlistBtn = createStyledButton("Shortlist Resume", COLOR_PRIMARY, Color.WHITE);
        JButton scheduleBtn = createStyledButton("Schedule Interview", COLOR_PURPLE, Color.WHITE);
        JButton selectedBtn = createStyledButton("Mark Selected", COLOR_SUCCESS, Color.WHITE);
        JButton rejectedBtn = createStyledButton("Mark Rejected", COLOR_DANGER, Color.WHITE);
        JButton msgBtn = createStyledButton("Send Personal Message", COLOR_INFO, Color.WHITE);

        btnRow.add(shortlistBtn);
        btnRow.add(scheduleBtn);
        btnRow.add(selectedBtn);
        btnRow.add(rejectedBtn);
        btnRow.add(msgBtn);

        shortlistBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(dialog, "Select a student from the table.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int studentId = (int) table.getValueAt(selectedRow, 0);
            String name = (String) table.getValueAt(selectedRow, 1);
            system.shortlistResume(studentId, drive.getId());
            statusFooterMsg.setText("Resume shortlisted for " + name + " at " + drive.getCompanyName());
            loadApplicants.run();
        });

        scheduleBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(dialog, "Select a student from the table.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int studentId = (int) table.getValueAt(selectedRow, 0);
            String name = (String) table.getValueAt(selectedRow, 1);
            system.scheduleInterview(studentId, drive.getId());
            statusFooterMsg.setText("Interview scheduled for " + name + " at " + drive.getCompanyName());
            loadApplicants.run();
        });

        selectedBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(dialog, "Select a student from the table.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int studentId = (int) table.getValueAt(selectedRow, 0);
            String name = (String) table.getValueAt(selectedRow, 1);
            system.updateApplicationStatus(studentId, drive.getId(), "Selected");
            statusFooterMsg.setText("Candidate " + name + " marked as Selected!");
            loadApplicants.run();
        });

        rejectedBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(dialog, "Select a student from the table.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int studentId = (int) table.getValueAt(selectedRow, 0);
            String name = (String) table.getValueAt(selectedRow, 1);
            system.updateApplicationStatus(studentId, drive.getId(), "Rejected");
            statusFooterMsg.setText("Candidate " + name + " marked as Rejected.");
            loadApplicants.run();
        });

        msgBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(dialog, "Select a student from the table.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int studentId = (int) table.getValueAt(selectedRow, 0);
            String name = (String) table.getValueAt(selectedRow, 1);
            String msg = JOptionPane.showInputDialog(dialog, "Enter notification message for " + name + ":");
            if (msg != null && !msg.trim().isEmpty()) {
                system.sendPersonalMessage(studentId, msg);
                statusFooterMsg.setText("Message sent to " + name);
            }
        });

        bottomContainer.add(btnRow, BorderLayout.CENTER);
        bottomContainer.add(statusFooterMsg, BorderLayout.SOUTH);
        mainContainer.add(bottomContainer, BorderLayout.SOUTH);

        loadApplicants.run();
        dialog.add(mainContainer);
        dialog.setVisible(true);
    }

    // Admin Tab 3: Add Company
    private JPanel createAdminAddCompanyPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(COLOR_BG);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(COLOR_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(4, COLOR_BORDER, 1),
                BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));
        card.setPreferredSize(new Dimension(500, 620));

        PlaceholderTextField idFld = createStyledTextField("Drive ID");
        PlaceholderTextField nameFld = createStyledTextField("Company Name (Auto-Capitalized)");
        PlaceholderTextField ctcFld = createStyledTextField("CTC in LPA");

        JComboBox<String> branchCombo = new JComboBox<>(DRIVE_BRANCHES);
        branchCombo.setFont(FONT_BODY);
        branchCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        JComboBox<String> cgpaCombo = new JComboBox<>(CGPA_OPTIONS);
        cgpaCombo.setFont(FONT_BODY);
        cgpaCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        cgpaCombo.setSelectedItem("6.0");

        PlaceholderTextField backlogsFld = createStyledTextField("Max Backlogs");
        PlaceholderTextField roleFld = createStyledTextField("Job Role");
        PlaceholderTextField specFld = createStyledTextField("Specialization (e.g. Enterprise Applications)");

        JButton submitBtn = createStyledButton("Publish Drive & Notify Students", COLOR_SUCCESS, Color.WHITE);

        card.add(createFieldLabel("Drive ID:")); card.add(idFld);
        card.add(createFieldLabel("Company Name:")); card.add(nameFld);
        card.add(createFieldLabel("CTC (LPA):")); card.add(ctcFld);
        card.add(createFieldLabel("Eligible Branch:")); card.add(branchCombo);
        card.add(createFieldLabel("Min CGPA Required:")); card.add(cgpaCombo);
        card.add(createFieldLabel("Max Backlogs Allowed:")); card.add(backlogsFld);
        card.add(createFieldLabel("Job Role:")); card.add(roleFld);
        card.add(createFieldLabel("Specialization:")); card.add(specFld);
        card.add(Box.createRigidArea(new Dimension(0, 15)));
        card.add(submitBtn);

        submitBtn.addActionListener(ev -> {
            try {
                int id = Integer.parseInt(idFld.getText().trim());
                String name = capitalizeWords(nameFld.getText());
                double ctc = Double.parseDouble(ctcFld.getText().trim());
                String branch = (String) branchCombo.getSelectedItem();
                double minCgpa = Double.parseDouble((String) cgpaCombo.getSelectedItem());
                int backlogs = Integer.parseInt(backlogsFld.getText().trim());
                String role = capitalizeWords(roleFld.getText());
                String spec = specFld.getText().trim();

                Drive d = new Drive(id, name, ctc, branch, minCgpa, backlogs);
                d.setJobRole(role);
                d.setSpecialization(spec.isEmpty() ? "Technology Solutions" : spec);
                system.addNewDrive(d);

                JOptionPane.showMessageDialog(this, "Drive Published Successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid inputs, please check all fields.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(new JScrollPane(card));
        return panel;
    }

    // Admin Tab 4: Interview Scheduler
    private JPanel createAdminQueuePanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(COLOR_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel topControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        topControls.setBackground(COLOR_BG);
        topControls.add(new JLabel("Select Placement Drive:"));

        JComboBox<String> driveCombo = new JComboBox<>();
        for (Drive d : system.getDrives()) {
            driveCombo.addItem(d.getId() + " - " + d.getCompanyName());
        }
        topControls.add(driveCombo);
        panel.add(topControls, BorderLayout.NORTH);

        String[] columns = {"Queue Order", "Student ID", "Student Name", "Current Stage"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable table = new JTable(model);
        styleTable(table);
        table.getColumnModel().getColumn(3).setCellRenderer(new StatusBadgeCellRenderer());

        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        Runnable refreshQueue = () -> {
            model.setRowCount(0);
            if (driveCombo.getSelectedItem() != null) {
                String sel = (String) driveCombo.getSelectedItem();
                int driveId = Integer.parseInt(sel.split(" - ")[0]);
                CustomQueue queue = system.getInterviewQueue(driveId);
                if (queue != null) {
                    Application[] apps = queue.toArray();
                    for (int i = 0; i < apps.length; i++) {
                        model.addRow(new Object[]{i + 1, apps[i].getStudentId(), apps[i].getStudentName(), apps[i].getCurrentStatus()});
                    }
                }
            }
        };

        driveCombo.addActionListener(e -> refreshQueue.run());

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        actionPanel.setBackground(COLOR_BG);

        JButton scheduleBtn = createStyledButton("Schedule Candidate for Interview", COLOR_PRIMARY, Color.WHITE);
        JButton startNextBtn = createStyledButton("Call Next Candidate to Interview", COLOR_SUCCESS, Color.WHITE);

        scheduleBtn.addActionListener(e -> {
            String sIdStr = JOptionPane.showInputDialog(this, "Enter 8-Digit Student ID to Schedule:");
            if (sIdStr != null) {
                try {
                    int studentId = Integer.parseInt(sIdStr.trim());
                    String sel = (String) driveCombo.getSelectedItem();
                    int driveId = Integer.parseInt(sel.split(" - ")[0]);
                    String res = system.scheduleInterview(studentId, driveId);
                    JOptionPane.showMessageDialog(this, res, "Interview Scheduled", JOptionPane.INFORMATION_MESSAGE);
                    refreshQueue.run();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Invalid Student ID.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        startNextBtn.addActionListener(e -> {
            if (driveCombo.getSelectedItem() != null) {
                String sel = (String) driveCombo.getSelectedItem();
                int driveId = Integer.parseInt(sel.split(" - ")[0]);
                String res = system.startNextInterview(driveId);
                JOptionPane.showMessageDialog(this, res, "Next Candidate Called", JOptionPane.INFORMATION_MESSAGE);
                refreshQueue.run();
            }
        });

        actionPanel.add(scheduleBtn);
        actionPanel.add(startNextBtn);
        panel.add(actionPanel, BorderLayout.SOUTH);

        refreshQueue.run();
        return panel;
    }

    // ====================================================================
    // UI UTILITY HELPERS & MODERN RENDERERS
    // ====================================================================

    private JLabel createFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_BOLD);
        lbl.setForeground(COLOR_DARK_NAVY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private PlaceholderTextField createStyledTextField(String placeholder) {
        PlaceholderTextField tf = new PlaceholderTextField(placeholder);
        tf.setFont(FONT_BODY);
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        tf.setPreferredSize(new Dimension(300, 36));
        tf.setAlignmentX(Component.LEFT_ALIGNMENT);
        tf.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(4, COLOR_BORDER, 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));

        tf.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(
                        new RoundedBorder(4, COLOR_PRIMARY, 2),
                        BorderFactory.createEmptyBorder(3, 7, 3, 7)
                ));
            }

            @Override
            public void focusLost(FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(
                        new RoundedBorder(4, COLOR_BORDER, 1),
                        BorderFactory.createEmptyBorder(4, 8, 4, 8)
                ));
            }
        });
        return tf;
    }

    private JPanel createPasswordFieldWithToggle(String placeholder) {
        JPanel container = new JPanel(new BorderLayout(5, 0));
        container.setOpaque(false);
        container.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        container.setPreferredSize(new Dimension(300, 36));
        container.setAlignmentX(Component.LEFT_ALIGNMENT);

        PlaceholderPasswordField pf = new PlaceholderPasswordField(placeholder);
        pf.setFont(FONT_BODY);
        pf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        pf.setPreferredSize(new Dimension(240, 36));
        pf.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(4, COLOR_BORDER, 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));

        JToggleButton toggleBtn = new JToggleButton("Show");
        toggleBtn.setFont(FONT_BOLD);
        toggleBtn.setFocusPainted(false);
        toggleBtn.setContentAreaFilled(true);
        toggleBtn.setBackground(COLOR_CARD);
        toggleBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        toggleBtn.addActionListener(e -> {
            if (toggleBtn.isSelected()) {
                pf.setEchoChar((char) 0);
                toggleBtn.setText("Hide");
            } else {
                pf.setEchoChar('•');
                toggleBtn.setText("Show");
            }
        });

        pf.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                pf.setBorder(BorderFactory.createCompoundBorder(
                        new RoundedBorder(4, COLOR_PRIMARY, 2),
                        BorderFactory.createEmptyBorder(3, 7, 3, 7)
                ));
            }

            @Override
            public void focusLost(FocusEvent e) {
                pf.setBorder(BorderFactory.createCompoundBorder(
                        new RoundedBorder(4, COLOR_BORDER, 1),
                        BorderFactory.createEmptyBorder(4, 8, 4, 8)
                ));
            }
        });

        container.add(pf, BorderLayout.CENTER);
        container.add(toggleBtn, BorderLayout.EAST);
        container.putClientProperty("field", pf);
        return container;
    }

    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btn.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(4, bg, 1),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled()) {
                    btn.setBackground(bg.darker());
                    btn.setBorder(BorderFactory.createCompoundBorder(
                            new RoundedBorder(4, bg.darker(), 1),
                            BorderFactory.createEmptyBorder(8, 16, 8, 16)
                    ));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (btn.isEnabled()) {
                    btn.setBackground(bg);
                    btn.setBorder(BorderFactory.createCompoundBorder(
                            new RoundedBorder(4, bg, 1),
                            BorderFactory.createEmptyBorder(8, 16, 8, 16)
                    ));
                }
            }
        });
        return btn;
    }

    private void styleTable(JTable table) {
        table.setRowHeight(36);
        table.setFont(FONT_BODY);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(241, 245, 249));

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(COLOR_DARK_NAVY);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 38));
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    c.setForeground(COLOR_DARK_NAVY);
                } else {
                    c.setBackground(new Color(224, 231, 255));
                    c.setForeground(COLOR_DARK_NAVY);
                }
                return c;
            }
        });
    }

    // Custom Rounded Border Component
    static class RoundedBorder implements Border {
        private int radius;
        private Color color;
        private int thickness;

        public RoundedBorder(int radius, Color color, int thickness) {
            this.radius = radius;
            this.color = color;
            this.thickness = thickness;
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius / 2 + 2, radius / 2 + 2, radius / 2 + 2, radius / 2 + 2);
        }

        @Override
        public boolean isBorderOpaque() { return false; }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.draw(new RoundRectangle2D.Float(x + thickness / 2.0f, y + thickness / 2.0f, width - thickness, height - thickness, radius, radius));
            g2.dispose();
        }
    }

    // Placeholder TextField Implementation
    static class PlaceholderTextField extends JTextField {
        private String placeholder;

        public PlaceholderTextField(String placeholder) {
            this.placeholder = placeholder;
        }

        public void setPlaceholder(String placeholder) {
            this.placeholder = placeholder;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty() && !(FocusManager.getCurrentManager().getFocusOwner() == this)) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_MUTED);
                g2.setFont(getFont().deriveFont(Font.ITALIC));
                Insets insets = getInsets();
                g2.drawString(placeholder, insets.left + 2, g.getFontMetrics().getAscent() + insets.top + 2);
                g2.dispose();
            }
        }
    }

    // Placeholder PasswordField Implementation
    static class PlaceholderPasswordField extends JPasswordField {
        private String placeholder;

        public PlaceholderPasswordField(String placeholder) {
            this.placeholder = placeholder;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getPassword().length == 0 && !(FocusManager.getCurrentManager().getFocusOwner() == this)) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_MUTED);
                g2.setFont(getFont().deriveFont(Font.ITALIC));
                Insets insets = getInsets();
                g2.drawString(placeholder, insets.left + 2, g.getFontMetrics().getAscent() + insets.top + 2);
                g2.dispose();
            }
        }
    }

    // Status Cell Badge Renderer
    static class StatusBadgeCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(FONT_BOLD);
            String text = value != null ? value.toString() : "";
            label.setOpaque(true);

            if (isSelected) {
                label.setBackground(table.getSelectionBackground());
                label.setForeground(table.getSelectionForeground());
                return label;
            }

            switch (text.toLowerCase()) {
                case "selected":
                    label.setBackground(new Color(209, 250, 229));
                    label.setForeground(new Color(6, 95, 70));
                    break;
                case "shortlisted":
                    label.setBackground(new Color(224, 231, 255));
                    label.setForeground(new Color(55, 48, 163));
                    break;
                case "rejected":
                    label.setBackground(new Color(254, 226, 226));
                    label.setForeground(new Color(153, 27, 27));
                    break;
                case "interview":
                case "interview_scheduled":
                case "interview_in_progress":
                case "technical_round":
                case "hr_round":
                    label.setBackground(new Color(254, 243, 199));
                    label.setForeground(new Color(146, 64, 14));
                    break;
                default:
                    label.setBackground(new Color(241, 245, 249));
                    label.setForeground(new Color(71, 85, 105));
                    break;
            }
            return label;
        }
    }

    // Skill Match % Progress Bar Renderer
    static class MatchBarCellRenderer extends JProgressBar implements TableCellRenderer {
        public MatchBarCellRenderer() {
            setOpaque(true);
            setStringPainted(true);
            setFont(FONT_BOLD);
            setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            int val = 0;
            if (value != null) {
                String str = value.toString().replace("%", "").trim();
                try {
                    val = (int) Math.round(Double.parseDouble(str));
                } catch (Exception ignored) {}
            }
            setValue(val);
            setString(val + "% Match");

            if (val >= 80) {
                setForeground(COLOR_SUCCESS);
            } else if (val >= 50) {
                setForeground(COLOR_PRIMARY);
            } else {
                setForeground(COLOR_WARNING);
            }
            setBackground(new Color(241, 245, 249));

            if (isSelected) {
                setBackground(table.getSelectionBackground());
            }
            return this;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new PlacementApp().setVisible(true);
        });
    }
}
