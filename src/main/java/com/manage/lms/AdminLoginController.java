package com.manage.lms;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.sql.*;

public class AdminLoginController {
    public TextField adminUsername;
    public TextField adminPasword;
    public Label signinError;
    public static Connection conn;

    boolean connected = false;
    public static FXMLLoader fxmlLoader;

    // reset button handler
    public void resetFields() {
        adminUsername.clear();
        adminPasword.clear();
        signinError.setText("");
    }

    // admin login button handler
    public void adminLoginButton() throws IOException {
        try {
            conn = Database.connect();
            try (PreparedStatement pstmt = conn
                    .prepareStatement("SELECT * FROM Admins WHERE Username=? AND Password=?")) {
                pstmt.setString(1, adminUsername.getText());
                pstmt.setString(2, Database.hashPassword(adminPasword.getText()));
                try (ResultSet rs = pstmt.executeQuery()) {
                    connected = rs.next();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connected) {
                signinError.setText("");
                BooksAdminController.loadBooks(conn);
                fxmlLoader = new FXMLLoader(Main.class.getResource("adminPanel.fxml"));
                Main.getMainStage().setScene(new Scene(fxmlLoader.load()));

            } else {
                signinError.setText("Invalid Credentials or DB Inaccessible");
            }

        }

    }

    public void homepage() throws IOException {
        fxmlLoader = new FXMLLoader(Main.class.getResource("lms.fxml"));
        Main.getMainStage().setScene(new Scene(fxmlLoader.load()));
    }
}
