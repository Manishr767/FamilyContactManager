package com.family.ui;

import com.family.dao.ContactDAO;
import com.family.dao.GroupDAO;
import com.family.model.Contact;
import com.family.model.ContactGroup;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MainWindow extends JFrame {

    // ── Palette ───────────────────────────────────────────────────────────────
    private static final Color BG_DARK    = new Color(30,  41,  59);
    private static final Color BG_SIDEBAR = new Color(15,  23,  42);
    private static final Color ACCENT     = new Color(99,  102, 241);
    private static final Color ACCENT_HOV = new Color(79,  70,  229);
    private static final Color BG_MAIN    = new Color(248, 250, 252);
    private static final Color CARD_BG    = Color.WHITE;
    private static final Color TEXT_MAIN  = new Color(15,  23,  42);
    private static final Color TEXT_SUB   = new Color(100, 116, 139);
    private static final Color ROW_ODD    = new Color(249, 250, 251);
    private static final Color ROW_EVEN   = Color.WHITE;
    private static final Color DANGER     = new Color(239, 68,  68);

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    // ── State ─────────────────────────────────────────────────────────────────
    private final ContactDAO contactDAO = new ContactDAO();
    private final GroupDAO   groupDAO   = new GroupDAO();

    private DefaultTableModel tableModel;
    private JTable            contactTable;
    private JTextField        searchField;
    private JLabel            lblStatus;
    private JLabel            lblAvatar;
    private JLabel            lblDetailName, lblDetailRelation, lblDetailPhone;
    private JLabel            lblDetailEmail, lblDetailBirthday, lblDetailAddress;
    private JTextArea         taDetailNotes;
    private JPanel            detailPanel;
    private JList<ContactGroup> groupFilterList;
    private DefaultListModel<ContactGroup> groupFilterModel;

    private List<Contact>      currentContacts;
    private Contact            selectedContact;

    public MainWindow() {
        super("👨‍👩‍👧‍👦  Family Contact Manager");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 700);
        setMinimumSize(new Dimension(900, 580));
        setLocationRelativeTo(null);
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        initUI();
        loadContacts();
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  UI CONSTRUCTION
    // ═════════════════════════════════════════════════════════════════════════
    private void initUI() {
        setLayout(new BorderLayout());

        add(buildSidebar(), BorderLayout.WEST);

        JPanel center = new JPanel(new BorderLayout(0, 0));
        center.setBackground(BG_MAIN);
        center.add(buildToolbar(), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildTablePanel(), buildDetailPanel());
        split.setDividerLocation(520);
        split.setDividerSize(2);
        split.setBorder(null);
        center.add(split, BorderLayout.CENTER);
        center.add(buildStatusBar(), BorderLayout.SOUTH);

        add(center, BorderLayout.CENTER);
    }

    // ── Sidebar ───────────────────────────────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(BG_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(200, 0));

        // Brand
        JPanel brand = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 16));
        brand.setBackground(BG_SIDEBAR);
        JLabel icon  = new JLabel("👨‍👩‍👧‍👦");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        JLabel name = new JLabel("<html><b>Family</b><br>Contacts</html>");
        name.setForeground(Color.WHITE);
        name.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        brand.add(icon); brand.add(name);
        sidebar.add(brand, BorderLayout.NORTH);

        // Groups list
        JPanel groupBox = new JPanel(new BorderLayout());
        groupBox.setBackground(BG_SIDEBAR);
        groupBox.setBorder(new EmptyBorder(10, 12, 10, 12));

        JLabel glbl = new JLabel("GROUPS");
        glbl.setForeground(new Color(100, 116, 139));
        glbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        groupBox.add(glbl, BorderLayout.NORTH);

        groupFilterModel = new DefaultListModel<>();
        groupFilterModel.addElement(new ContactGroup(-1, "All Contacts"));
        groupFilterList  = new JList<>(groupFilterModel);
        groupFilterList.setBackground(BG_SIDEBAR);
        groupFilterList.setForeground(Color.WHITE);
        groupFilterList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        groupFilterList.setSelectionBackground(ACCENT);
        groupFilterList.setSelectionForeground(Color.WHITE);
        groupFilterList.setFixedCellHeight(32);
        groupFilterList.setSelectedIndex(0);
        groupFilterList.setCellRenderer((list, val, idx, sel, foc) -> {
            JLabel l = new JLabel((idx == 0 ? "🏠 " : "👥 ") + val.getGroupName());
            l.setOpaque(true);
            l.setForeground(sel ? Color.WHITE : new Color(203, 213, 225));
            l.setBackground(sel ? ACCENT : BG_SIDEBAR);
            l.setBorder(new EmptyBorder(4, 12, 4, 12));
            l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            return l;
        });
        groupFilterList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) filterByGroup();
        });

        JScrollPane gScroll = new JScrollPane(groupFilterList);
        gScroll.setBorder(null);
        gScroll.setBackground(BG_SIDEBAR);
        groupBox.add(gScroll, BorderLayout.CENTER);

        // Manage groups btn
        JButton btnManageGroups = sidebarBtn("+ Manage Groups");
        btnManageGroups.addActionListener(e -> manageGroups());
        JPanel gbtnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 6));
        gbtnPanel.setBackground(BG_SIDEBAR);
        gbtnPanel.add(btnManageGroups);
        groupBox.add(gbtnPanel, BorderLayout.SOUTH);

        sidebar.add(groupBox, BorderLayout.CENTER);

        // Birthday reminder
        JButton btnBirthdays = sidebarBtn("🎂 Upcoming Birthdays");
        btnBirthdays.addActionListener(e -> showUpcomingBirthdays());
        JPanel botPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
        botPanel.setBackground(BG_SIDEBAR);
        botPanel.add(btnBirthdays);
        sidebar.add(botPanel, BorderLayout.SOUTH);

        return sidebar;
    }

    private JButton sidebarBtn(String text) {
        JButton b = new JButton(text);
        b.setBackground(new Color(51, 65, 85));
        b.setForeground(new Color(203, 213, 225));
        b.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(170, 32));
        return b;
    }

    // ── Toolbar ───────────────────────────────────────────────────────────────
    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setBackground(CARD_BG);
        toolbar.setBorder(new EmptyBorder(12, 16, 12, 16));

        // Left: search
        JPanel searchPanel = new JPanel(new BorderLayout(6, 0));
        searchPanel.setBackground(CARD_BG);
        JLabel searchIcon = new JLabel("🔍");
        searchIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225)),
            new EmptyBorder(6, 10, 6, 10)));
        searchField.setPreferredSize(new Dimension(260, 36));
        searchField.putClientProperty("JTextField.placeholderText", "Search contacts…");
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { searchContacts(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { searchContacts(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { searchContacts(); }
        });
        searchPanel.add(searchIcon, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        toolbar.add(searchPanel, BorderLayout.WEST);

        // Right: action buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setBackground(CARD_BG);

        JButton btnAdd    = actionBtn("➕ Add Contact",    ACCENT);
        JButton btnEdit   = actionBtn("✏️ Edit",           new Color(59, 130, 246));
        JButton btnDelete = actionBtn("🗑️ Delete",         DANGER);
        JButton btnRefresh = actionBtn("🔄 Refresh",       new Color(16, 185, 129));

        btnAdd.addActionListener(e    -> addContact());
        btnEdit.addActionListener(e   -> editContact());
        btnDelete.addActionListener(e -> deleteContact());
        btnRefresh.addActionListener(e -> loadContacts());

        btnPanel.add(btnRefresh); btnPanel.add(btnDelete);
        btnPanel.add(btnEdit);    btnPanel.add(btnAdd);
        toolbar.add(btnPanel, BorderLayout.EAST);

        return toolbar;
    }

    private JButton actionBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(130, 34));
        b.addMouseListener(new MouseAdapter() {
            Color orig = bg;
            public void mouseEntered(MouseEvent e)  { b.setBackground(orig.darker()); }
            public void mouseExited(MouseEvent e)   { b.setBackground(orig); }
        });
        return b;
    }

    // ── Table panel ───────────────────────────────────────────────────────────
    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_MAIN);

        String[] cols = {"Name", "Relationship", "Phone", "Email", "Birthday"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        contactTable = new JTable(tableModel);
        contactTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        contactTable.setRowHeight(36);
        contactTable.setShowVerticalLines(false);
        contactTable.setGridColor(new Color(241, 245, 249));
        contactTable.setSelectionBackground(new Color(224, 231, 255));
        contactTable.setSelectionForeground(TEXT_MAIN);
        contactTable.setFillsViewportHeight(true);

        // Header
        JTableHeader header = contactTable.getTableHeader();
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(TEXT_SUB);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));

        // Alternating rows
        contactTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                if (!sel) comp.setBackground(r % 2 == 0 ? ROW_EVEN : ROW_ODD);
                setBorder(new EmptyBorder(0, 12, 0, 8));
                return comp;
            }
        });

        // Column widths
        contactTable.getColumnModel().getColumn(0).setPreferredWidth(160);
        contactTable.getColumnModel().getColumn(1).setPreferredWidth(110);
        contactTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        contactTable.getColumnModel().getColumn(3).setPreferredWidth(170);
        contactTable.getColumnModel().getColumn(4).setPreferredWidth(100);

        contactTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) showDetail();
        });
        contactTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) editContact();
            }
        });

        JScrollPane scroll = new JScrollPane(contactTable);
        scroll.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // ── Detail panel ──────────────────────────────────────────────────────────
    private JPanel buildDetailPanel() {
        detailPanel = new JPanel(new BorderLayout());
        detailPanel.setBackground(CARD_BG);
        detailPanel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(226, 232, 240)));

        // Avatar area
        JPanel topArea = new JPanel();
        topArea.setBackground(new Color(238, 242, 255));
        topArea.setLayout(new BoxLayout(topArea, BoxLayout.Y_AXIS));
        topArea.setBorder(new EmptyBorder(20, 20, 16, 20));

        lblAvatar = new JLabel("👤");
        lblAvatar.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 56));
        lblAvatar.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblDetailName     = detailLabel("Select a contact", 18, Font.BOLD);
        lblDetailRelation = detailLabel("",  13, Font.ITALIC);

        topArea.add(lblAvatar);
        topArea.add(Box.createVerticalStrut(8));
        topArea.add(lblDetailName);
        topArea.add(Box.createVerticalStrut(2));
        topArea.add(lblDetailRelation);
        detailPanel.add(topArea, BorderLayout.NORTH);

        // Info cards
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(CARD_BG);
        infoPanel.setBorder(new EmptyBorder(12, 16, 12, 16));

        lblDetailPhone   = infoRow(infoPanel, "📞 Phone");
        lblDetailEmail   = infoRow(infoPanel, "📧 Email");
        lblDetailBirthday = infoRow(infoPanel, "🎂 Birthday");
        lblDetailAddress = infoRow(infoPanel, "📍 Address");

        infoPanel.add(Box.createVerticalStrut(10));
        JLabel notesLbl = new JLabel("📝 Notes");
        notesLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        notesLbl.setForeground(TEXT_SUB);
        infoPanel.add(notesLbl);
        infoPanel.add(Box.createVerticalStrut(4));
        taDetailNotes = new JTextArea(4, 0);
        taDetailNotes.setEditable(false);
        taDetailNotes.setLineWrap(true);
        taDetailNotes.setWrapStyleWord(true);
        taDetailNotes.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        taDetailNotes.setBackground(new Color(248, 250, 252));
        taDetailNotes.setBorder(new EmptyBorder(6, 8, 6, 8));
        infoPanel.add(new JScrollPane(taDetailNotes) {{ setBorder(null); }});

        JScrollPane infoScroll = new JScrollPane(infoPanel);
        infoScroll.setBorder(null);
        detailPanel.add(infoScroll, BorderLayout.CENTER);

        return detailPanel;
    }

    private JLabel detailLabel(String text, int size, int style) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(new Font("Segoe UI", style, size));
        l.setForeground(TEXT_MAIN);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    private JLabel infoRow(JPanel parent, String icon) {
        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        iconLbl.setForeground(TEXT_SUB);
        JLabel valLbl = new JLabel("—");
        valLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        valLbl.setForeground(TEXT_MAIN);
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setBackground(CARD_BG);
        row.setBorder(new EmptyBorder(4, 0, 4, 0));
        row.add(iconLbl, BorderLayout.WEST);
        row.add(valLbl,  BorderLayout.CENTER);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        parent.add(row);
        return valLbl;
    }

    // ── Status bar ────────────────────────────────────────────────────────────
    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(241, 245, 249));
        bar.setBorder(new EmptyBorder(4, 12, 4, 12));
        lblStatus = new JLabel("Ready");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblStatus.setForeground(TEXT_SUB);
        bar.add(lblStatus, BorderLayout.WEST);
        return bar;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  DATA OPERATIONS
    // ═════════════════════════════════════════════════════════════════════════
    private void loadContacts() {
        try {
            currentContacts = contactDAO.getAllContacts();
            refreshTable(currentContacts);
            refreshGroupSidebar();
            status("Loaded " + currentContacts.size() + " contacts.");
        } catch (SQLException ex) {
            showError("Failed to load contacts: " + ex.getMessage());
        }
    }

    private void refreshTable(List<Contact> contacts) {
        tableModel.setRowCount(0);
        for (Contact c : contacts) {
            tableModel.addRow(new Object[]{
                c.getFullName(),
                c.getRelationship() != null ? c.getRelationship() : "",
                c.getPhone()        != null ? c.getPhone()        : "",
                c.getEmail()        != null ? c.getEmail()        : "",
                c.getBirthday()     != null ? c.getBirthday().format(DATE_FMT) : ""
            });
        }
    }

    private void refreshGroupSidebar() throws SQLException {
        ContactGroup selected = groupFilterList.getSelectedValue();
        groupFilterModel.clear();
        groupFilterModel.addElement(new ContactGroup(-1, "All Contacts"));
        groupDAO.getAllGroups().forEach(groupFilterModel::addElement);
        // Restore selection
        if (selected != null && selected.getId() != -1) {
            for (int i = 0; i < groupFilterModel.size(); i++) {
                if (groupFilterModel.get(i).getId() == selected.getId()) {
                    groupFilterList.setSelectedIndex(i);
                    return;
                }
            }
        }
        groupFilterList.setSelectedIndex(0);
    }

    private void searchContacts() {
        String kw = searchField.getText().trim();
        try {
            if (kw.isEmpty()) {
                currentContacts = contactDAO.getAllContacts();
            } else {
                currentContacts = contactDAO.searchContacts(kw);
            }
            refreshTable(currentContacts);
            status("Found " + currentContacts.size() + " contact(s).");
        } catch (SQLException ex) {
            showError("Search failed: " + ex.getMessage());
        }
    }

    private void filterByGroup() {
        ContactGroup g = groupFilterList.getSelectedValue();
        if (g == null) return;
        try {
            if (g.getId() == -1) {
                currentContacts = contactDAO.getAllContacts();
            } else {
                currentContacts = contactDAO.getContactsByGroup(g.getId());
            }
            refreshTable(currentContacts);
            status("Showing " + currentContacts.size() + " contact(s) in \"" + g.getGroupName() + "\".");
        } catch (SQLException ex) {
            showError("Filter failed: " + ex.getMessage());
        }
    }

    private void showDetail() {
        int row = contactTable.getSelectedRow();
        if (row < 0 || row >= currentContacts.size()) {
            clearDetail();
            return;
        }
        selectedContact = currentContacts.get(row);
        lblDetailName.setText(selectedContact.getFullName());
        lblDetailRelation.setText(selectedContact.getRelationship() != null ? selectedContact.getRelationship() : "");
        lblDetailPhone.setText(orDash(selectedContact.getPhone()));
        lblDetailEmail.setText(orDash(selectedContact.getEmail()));
        lblDetailBirthday.setText(selectedContact.getBirthday() != null
            ? selectedContact.getBirthday().format(DATE_FMT) : "—");
        lblDetailAddress.setText(orDash(selectedContact.getAddress()));
        taDetailNotes.setText(selectedContact.getNotes() != null ? selectedContact.getNotes() : "");

        // Try to load photo
        if (selectedContact.getPhotoPath() != null && !selectedContact.getPhotoPath().isBlank()) {
            File f = new File(selectedContact.getPhotoPath());
            if (f.exists()) {
                ImageIcon img = new ImageIcon(new ImageIcon(f.getAbsolutePath())
                    .getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH));
                lblAvatar.setIcon(img);
                lblAvatar.setText("");
            } else {
                lblAvatar.setIcon(null);
                lblAvatar.setText("👤");
            }
        } else {
            lblAvatar.setIcon(null);
            lblAvatar.setText("👤");
        }
    }

    private void clearDetail() {
        selectedContact = null;
        lblDetailName.setText("Select a contact");
        lblDetailRelation.setText("");
        lblDetailPhone.setText("—");
        lblDetailEmail.setText("—");
        lblDetailBirthday.setText("—");
        lblDetailAddress.setText("—");
        taDetailNotes.setText("");
        lblAvatar.setIcon(null);
        lblAvatar.setText("👤");
    }

    private void addContact() {
        ContactFormDialog dlg = new ContactFormDialog(this, null);
        dlg.setVisible(true);
        if (dlg.isSaved()) { loadContacts(); status("Contact added."); }
    }

    private void editContact() {
        int row = contactTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a contact to edit."); return; }
        Contact c = currentContacts.get(row);
        ContactFormDialog dlg = new ContactFormDialog(this, c);
        dlg.setVisible(true);
        if (dlg.isSaved()) { loadContacts(); status("Contact updated."); }
    }

    private void deleteContact() {
        int row = contactTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a contact to delete."); return; }
        Contact c = currentContacts.get(row);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete \"" + c.getFullName() + "\"? This cannot be undone.",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            contactDAO.deleteContact(c.getId());
            loadContacts();
            clearDetail();
            status("Contact deleted.");
        } catch (SQLException ex) {
            showError("Delete failed: " + ex.getMessage());
        }
    }

    private void manageGroups() {
        try {
            List<ContactGroup> groups = groupDAO.getAllGroups();
            String[] options = {"Add Group", "Delete Group", "Close"};
            int choice = JOptionPane.showOptionDialog(this, buildGroupPanel(groups),
                "Manage Groups", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, options, options[2]);
            if (choice == 0) {
                String name = JOptionPane.showInputDialog(this, "New group name:");
                if (name != null && !name.isBlank()) {
                    groupDAO.addGroup(name.trim());
                    loadContacts();
                }
            } else if (choice == 1) {
                groups = groupDAO.getAllGroups();
                if (groups.isEmpty()) { JOptionPane.showMessageDialog(this, "No groups to delete."); return; }
                ContactGroup sel = (ContactGroup) JOptionPane.showInputDialog(this,
                    "Select group to delete:", "Delete Group",
                    JOptionPane.PLAIN_MESSAGE, null,
                    groups.toArray(), groups.get(0));
                if (sel != null) { groupDAO.deleteGroup(sel.getId()); loadContacts(); }
            }
        } catch (SQLException ex) { showError(ex.getMessage()); }
    }

    private JPanel buildGroupPanel(List<ContactGroup> groups) {
        JPanel p = new JPanel(new BorderLayout());
        DefaultListModel<ContactGroup> m = new DefaultListModel<>();
        groups.forEach(m::addElement);
        JList<ContactGroup> l = new JList<>(m);
        l.setPreferredSize(new Dimension(200, 120));
        p.add(new JScrollPane(l));
        return p;
    }

    private void showUpcomingBirthdays() {
        try {
            List<Contact> upcoming = contactDAO.getUpcomingBirthdays(30);
            if (upcoming.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No birthdays in the next 30 days.", "Upcoming Birthdays", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            StringBuilder sb = new StringBuilder("<html><b>Birthdays in the next 30 days:</b><br><br>");
            for (Contact c : upcoming) {
                sb.append("🎂 ").append(c.getFullName()).append("  —  ")
                  .append(c.getBirthday().format(DateTimeFormatter.ofPattern("MMM dd")))
                  .append("<br>");
            }
            sb.append("</html>");
            JOptionPane.showMessageDialog(this, new JLabel(sb.toString()), "Upcoming Birthdays", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) { showError(ex.getMessage()); }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private String orDash(String s) { return (s != null && !s.isBlank()) ? s : "—"; }
    private void status(String msg) { lblStatus.setText(msg); }
    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
