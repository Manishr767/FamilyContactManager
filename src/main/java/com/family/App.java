package com.family;

import com.family.ui.MainWindow;
import com.family.util.DBConnection;

import javax.swing.*;
import java.sql.SQLException;

public class App {
    public static void main(String[] args) {

        try {

            DBConnection.getConnection();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                "<html><b>Cannot connect to MySQL.</b><br><br>" +
                "Please ensure:<br>" +
                "• MySQL server is running on localhost:3306<br>" +
                "• Username / password in DBConnection.java is correct<br>" +
                "• MySQL JDBC driver is on the classpath<br><br>" +
                "Error: " + e.getMessage() + "</html>",
                "Database Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }


        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            MainWindow window = new MainWindow();
            window.setVisible(true);
        });
    }
}
