package com.family.dao;

import com.family.model.Contact;
import com.family.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ContactDAO {


    public boolean addContact(Contact c) throws SQLException {
        String sql = """
            INSERT INTO contacts
              (first_name, last_name, relationship, phone, email, address, birthday, notes, photo_path)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getFirstName());
            ps.setString(2, c.getLastName());
            ps.setString(3, c.getRelationship());
            ps.setString(4, c.getPhone());
            ps.setString(5, c.getEmail());
            ps.setString(6, c.getAddress());
            ps.setObject(7, c.getBirthday());
            ps.setString(8, c.getNotes());
            ps.setString(9, c.getPhotoPath());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) c.setId(rs.getInt(1));
                }
                return true;
            }
            return false;
        }
    }


    public List<Contact> getAllContacts() throws SQLException {
        List<Contact> list = new ArrayList<>();
        String sql = "SELECT * FROM contacts ORDER BY last_name, first_name";
        try (Connection conn = DBConnection.getConnection();
             Statement  stmt = conn.createStatement();
             ResultSet  rs   = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }


    public Contact getContactById(int id) throws SQLException {
        String sql = "SELECT * FROM contacts WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }


    public List<Contact> searchContacts(String keyword) throws SQLException {
        List<Contact> list = new ArrayList<>();
        String sql = """
            SELECT * FROM contacts
            WHERE first_name  LIKE ? OR last_name   LIKE ?
               OR phone       LIKE ? OR email       LIKE ?
               OR relationship LIKE ?
            ORDER BY last_name, first_name
        """;
        String kw = "%" + keyword + "%";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 1; i <= 5; i++) ps.setString(i, kw);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }


    public List<Contact> getContactsByGroup(int groupId) throws SQLException {
        List<Contact> list = new ArrayList<>();
        String sql = """
            SELECT c.* FROM contacts c
            JOIN contact_group_map m ON c.id = m.contact_id
            WHERE m.group_id = ?
            ORDER BY c.last_name, c.first_name
        """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }


    public boolean updateContact(Contact c) throws SQLException {
        String sql = """
            UPDATE contacts SET
              first_name=?, last_name=?, relationship=?, phone=?,
              email=?, address=?, birthday=?, notes=?, photo_path=?
            WHERE id=?
        """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getFirstName());
            ps.setString(2, c.getLastName());
            ps.setString(3, c.getRelationship());
            ps.setString(4, c.getPhone());
            ps.setString(5, c.getEmail());
            ps.setString(6, c.getAddress());
            ps.setObject(7, c.getBirthday());
            ps.setString(8, c.getNotes());
            ps.setString(9, c.getPhotoPath());
            ps.setInt(10, c.getId());
            return ps.executeUpdate() > 0;
        }
    }


    public boolean deleteContact(int id) throws SQLException {
        String sql = "DELETE FROM contacts WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }


    public List<Contact> getUpcomingBirthdays(int days) throws SQLException {
        List<Contact> list = new ArrayList<>();
        String sql = """
            SELECT * FROM contacts
            WHERE birthday IS NOT NULL
              AND DAYOFYEAR(
                    DATE(CONCAT(YEAR(CURDATE()),'-',MONTH(birthday),'-',DAY(birthday)))
                  ) BETWEEN DAYOFYEAR(CURDATE()) AND DAYOFYEAR(CURDATE()) + ?
            ORDER BY DAYOFYEAR(DATE(CONCAT(YEAR(CURDATE()),'-',MONTH(birthday),'-',DAY(birthday))))
        """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, days);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }


    public void assignGroup(int contactId, int groupId) throws SQLException {
        String sql = "INSERT IGNORE INTO contact_group_map (contact_id, group_id) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, contactId);
            ps.setInt(2, groupId);
            ps.executeUpdate();
        }
    }

    public void removeGroup(int contactId, int groupId) throws SQLException {
        String sql = "DELETE FROM contact_group_map WHERE contact_id=? AND group_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, contactId);
            ps.setInt(2, groupId);
            ps.executeUpdate();
        }
    }


    private Contact mapRow(ResultSet rs) throws SQLException {
        Date bd = rs.getDate("birthday");
        return new Contact(
            rs.getInt("id"),
            rs.getString("first_name"),
            rs.getString("last_name"),
            rs.getString("relationship"),
            rs.getString("phone"),
            rs.getString("email"),
            rs.getString("address"),
            bd != null ? bd.toLocalDate() : null,
            rs.getString("notes"),
            rs.getString("photo_path")
        );
    }
}
