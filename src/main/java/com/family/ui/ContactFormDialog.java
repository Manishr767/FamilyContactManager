package com.family.ui;

import com.family.dao.ContactDAO;
import com.family.dao.GroupDAO;
import com.family.model.Contact;
import com.family.model.ContactGroup;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class ContactFormDialog extends JDialog {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final Color BG       = new Color(245, 247, 252);
    private static final Color ACCENT   = new Color(99, 102, 241);
    private static final Color CARD     = Color.WHITE;
    private static final Color LABEL_FG = new Color(71, 85, 105);

    private final ContactDAO  contactDAO = new ContactDAO();
    private final GroupDAO    groupDAO   = new GroupDAO();
    private Contact           contact;
    private boolean           saved      = false;

    // Form fields
    private JTextField  tfFirst, tfLast, tfRelation, tfPhone, tfEmail, tfAddress, tfBirthday, tfPhoto;
    private JTextArea   taaNotes;
    private JLabel      lblPhoto;
    private JList<ContactGroup> groupList;
    private DefaultListModel<ContactGroup> groupModel;

    public ContactFormDialog(Frame owner, Contact contact) {
        super(owner, contact == null ? "Add Contact" : "Edit Contact", true);
        this.contact = contact;
        initUI();
        if (contact != null) populateFields();
        pack();
        setMinimumSize(new Dimension(560, 640));
        setLocationRelativeTo(owner);
    }

    private void initUI() {
        setBackground(BG);
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BG);

        // ── Header ──────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ACCENT);
        header.setBorder(new EmptyBorder(16, 20, 16, 20));
        JLabel title = new JLabel(contact == null ? "➕  New Contact" : "✏️  Edit Contact");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        header.add(title, BorderLayout.WEST);
        root.add(header, BorderLayout.NORTH);

        // ── Form ─────────────────────────────────────────────────────────────
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(CARD);
        form.setBorder(new EmptyBorder(20, 24, 10, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 4, 5, 4);

        tfFirst    = field(); tfLast     = field();
        tfRelation = field(); tfPhone    = field();
        tfEmail    = field(); tfAddress  = field();
        tfBirthday = field(); tfPhoto    = field();
        taaNotes   = new JTextArea(3, 20);
        taaNotes.setLineWrap(true); taaNotes.setWrapStyleWord(true);
        taaNotes.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        taaNotes.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));

        // Row helper
        int row = 0;
        row = addRow(form, gbc, row, "First Name *", tfFirst,    "Last Name *", tfLast);
        row = addRow(form, gbc, row, "Relationship", tfRelation, "Phone",       tfPhone);
        row = addRow(form, gbc, row, "Email",        tfEmail,    "Birthday (yyyy-MM-dd)", tfBirthday);
        row = addFullRow(form, gbc, row, "Address",  tfAddress);

        // Photo row
        JButton btnPhoto = styledBtn("Browse…", ACCENT);
        btnPhoto.addActionListener(e -> browsePhoto());
        lblPhoto = new JLabel("No photo selected");
        lblPhoto.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblPhoto.setForeground(new Color(148, 163, 184));
        JPanel photoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        photoPanel.setBackground(CARD);
        photoPanel.add(tfPhoto); tfPhoto.setPreferredSize(new Dimension(240, 28));
        photoPanel.add(Box.createHorizontalStrut(8));
        photoPanel.add(btnPhoto);
        row = addFullRow(form, gbc, row, "Photo Path", photoPanel);

        row = addFullRow(form, gbc, row, "Notes", new JScrollPane(taaNotes));

        // Groups
        try {
            List<ContactGroup> groups = groupDAO.getAllGroups();
            groupModel = new DefaultListModel<>();
            groups.forEach(groupModel::addElement);
            groupList = new JList<>(groupModel);
            groupList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            groupList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            if (contact != null) {
                List<ContactGroup> assigned = groupDAO.getGroupsForContact(contact.getId());
                for (int i = 0; i < groupModel.size(); i++) {
                    ContactGroup g = groupModel.get(i);
                    if (assigned.stream().anyMatch(a -> a.getId() == g.getId()))
                        groupList.addSelectionInterval(i, i);
                }
            }
            row = addFullRow(form, gbc, row, "Groups (Ctrl+click)", new JScrollPane(groupList) {{
                setPreferredSize(new Dimension(0, 80));
            }});
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        JScrollPane formScroll = new JScrollPane(form);
        formScroll.setBorder(null);
        root.add(formScroll, BorderLayout.CENTER);

        // ── Buttons ───────────────────────────────────────────────────────────
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        btnPanel.setBackground(BG);
        btnPanel.setBorder(new EmptyBorder(0, 0, 8, 8));

        JButton btnCancel = styledBtn("Cancel", new Color(148, 163, 184));
        JButton btnSave   = styledBtn("Save Contact", ACCENT);

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> save());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        root.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(root);
    }

    // ── Layout helpers ────────────────────────────────────────────────────────
    private int addRow(JPanel p, GridBagConstraints g, int row,
                       String l1, Component c1, String l2, Component c2) {
        g.gridy = row; g.weightx = 0;
        g.gridx = 0; g.gridwidth = 1; p.add(lbl(l1), g);
        g.gridx = 1; g.weightx = 1;  p.add(c1, g);
        g.gridx = 2; g.weightx = 0;  p.add(lbl(l2), g);
        g.gridx = 3; g.weightx = 1;  p.add(c2, g);
        return row + 1;
    }

    private int addFullRow(JPanel p, GridBagConstraints g, int row, String label, Component c) {
        g.gridy = row; g.gridx = 0; g.gridwidth = 1; g.weightx = 0;
        p.add(lbl(label), g);
        g.gridx = 1; g.gridwidth = 3; g.weightx = 1;
        p.add(c, g);
        g.gridwidth = 1;
        return row + 1;
    }

    private JLabel lbl(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(LABEL_FG);
        return l;
    }

    private JTextField field() {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225)),
            new EmptyBorder(4, 8, 4, 8)));
        return tf;
    }

    private JButton styledBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.black);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(140, 34));
        return b;
    }

    // ── Actions ───────────────────────────────────────────────────────────────
    private void browsePhoto() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Images", "jpg", "jpeg", "png", "gif", "bmp"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            tfPhoto.setText(f.getAbsolutePath());
        }
    }

    private void save() {
        if (tfFirst.getText().isBlank() || tfLast.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "First and Last name are required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDate birthday = null;
        if (!tfBirthday.getText().isBlank()) {
            try { birthday = LocalDate.parse(tfBirthday.getText().trim(), DATE_FMT); }
            catch (DateTimeParseException e) {
                JOptionPane.showMessageDialog(this, "Birthday must be yyyy-MM-dd.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        if (contact == null) contact = new Contact();
        contact.setFirstName(tfFirst.getText().trim());
        contact.setLastName(tfLast.getText().trim());
        contact.setRelationship(tfRelation.getText().trim());
        contact.setPhone(tfPhone.getText().trim());
        contact.setEmail(tfEmail.getText().trim());
        contact.setAddress(tfAddress.getText().trim());
        contact.setBirthday(birthday);
        contact.setNotes(taaNotes.getText().trim());
        contact.setPhotoPath(tfPhoto.getText().trim());

        try {
            if (contact.getId() == 0) contactDAO.addContact(contact);
            else                       contactDAO.updateContact(contact);

            // Sync groups
            List<ContactGroup> allGroups = groupDAO.getAllGroups();
            List<ContactGroup> selected  = groupList.getSelectedValuesList();
            for (ContactGroup g : allGroups) {
                boolean sel = selected.stream().anyMatch(s -> s.getId() == g.getId());
                if (sel) contactDAO.assignGroup(contact.getId(), g.getId());
                else     contactDAO.removeGroup(contact.getId(), g.getId());
            }

            saved = true;
            dispose();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error saving: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateFields() {
        tfFirst.setText(contact.getFirstName());
        tfLast.setText(contact.getLastName());
        tfRelation.setText(contact.getRelationship() != null ? contact.getRelationship() : "");
        tfPhone.setText(contact.getPhone() != null ? contact.getPhone() : "");
        tfEmail.setText(contact.getEmail() != null ? contact.getEmail() : "");
        tfAddress.setText(contact.getAddress() != null ? contact.getAddress() : "");
        if (contact.getBirthday() != null) tfBirthday.setText(contact.getBirthday().format(DATE_FMT));
        taaNotes.setText(contact.getNotes() != null ? contact.getNotes() : "");
        tfPhoto.setText(contact.getPhotoPath() != null ? contact.getPhotoPath() : "");
    }

    public boolean isSaved()    { return saved; }
    public Contact getContact() { return contact; }
}
