package com.family.dao;

import com.family.model.ContactGroup;
import com.family.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GroupDAO {

    public List<ContactGroup> getAllGroups() throws SQLException {
        List<ContactGroup> list = new ArrayList<>();
        String sql = "SELECT * FROM contact_groups ORDER BY group_name";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            while (rs.next())
                list.add(new ContactGroup(rs.getInt("id"), rs.getString("group_name")));
        }
        return list;
    }

    public boolean addGroup(String name) throws SQLException {
        String sql = "INSERT INTO contact_groups (group_name) VALUES (?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteGroup(int id) throws SQLException {
        String sql = "DELETE FROM contact_groups WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public List<ContactGroup> getGroupsForContact(int contactId) throws SQLException {
        List<ContactGroup> list = new ArrayList<>();
        String sql = """
            SELECT g.* FROM contact_groups g
            JOIN contact_group_map m ON g.id = m.group_id
            WHERE m.contact_id = ?
        """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, contactId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(new ContactGroup(rs.getInt("id"), rs.getString("group_name")));
            }
        }
        return list;
    }
}
