package com.manage.lms;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import java.io.IOException;
import java.sql.*;

public class UserLoginSignupController {

    public TextField signupUsername;
    public TextField signupAge;
    public TextField signupMail;
    public TextField signupPassword;
    public Label signupError;
    public Button signupButton;
    public TextField loginUsername;
    public TextField loginPassword;
    public Label loginError;
    FXMLLoader fxmlLoader;
    static Connection conn;
    static String username;

    // leads back to home
    public void homepage() throws IOException {
        fxmlLoader = new FXMLLoader(Main.class.getResource("lms.fxml"));
        Main.mainStage.setScene(new Scene(fxmlLoader.load()));
    }

    // resets the fields
    public void resetFields() {
        signupUsername.setText("");
        signupAge.setText("");
        signupMail.setText("");
        signupPassword.setText("");
        signupError.setText("");
        loginUsername.setText("");
        loginPassword.setText("");
        loginError.setText("");
    }

    public void userLoginButton() {
        System.out.println(loginUsername.getText());
        System.out.println(loginPassword.getText());
        try {
            conn = Database.connect();
            boolean loggedIn = false;
            try (PreparedStatement pstmt = conn
                    .prepareStatement("SELECT * FROM Users WHERE Username=? AND Password=?")) {
                pstmt.setString(1, loginUsername.getText());
                pstmt.setString(2, Database.hashPassword(loginPassword.getText()));
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        loggedIn = true;
                    }
                }
            }

            if (loggedIn) {
                loginError.setText("");
                username = loginUsername.getText();
                BooksUserController.loadBooks(conn);
                fxmlLoader = new FXMLLoader(Main.class.getResource("userPanel.fxml"));
                Main.mainStage.setScene(new Scene(fxmlLoader.load()));
            } else {
                // Fallback for older users with plain-text passwords
                boolean fallbackLogin = false;
                try (PreparedStatement fallbackStmt = conn
                        .prepareStatement("SELECT * FROM Users WHERE Username=? AND Password=?")) {
                    fallbackStmt.setString(1, loginUsername.getText());
                    fallbackStmt.setString(2, loginPassword.getText());
                    try (ResultSet fallbackRs = fallbackStmt.executeQuery()) {
                        if (fallbackRs.next()) {
                            fallbackLogin = true;
                        }
                    }
                }

                if (fallbackLogin) {
                    // Update user's password to the new hashed version
                    try (PreparedStatement migrateStmt = conn
                            .prepareStatement("UPDATE Users SET Password=? WHERE Username=?")) {
                        migrateStmt.setString(1, Database.hashPassword(loginPassword.getText()));
                        migrateStmt.setString(2, loginUsername.getText());
                        migrateStmt.executeUpdate();
                    }

                    loginError.setText("");
                    username = loginUsername.getText();
                    BooksUserController.loadBooks(conn);
                    fxmlLoader = new FXMLLoader(Main.class.getResource("userPanel.fxml"));
                    Main.mainStage.setScene(new Scene(fxmlLoader.load()));
                } else {
                    loginError.setText("Invalid credentials or inaccesible database");
                }
            }
        } catch (Exception e) {
            loginError.setText("Error connecting to database");
            e.printStackTrace();
        }

    }

    public boolean validate() {
        if (signupMail.getText().isEmpty() || signupAge.getText().isEmpty() || signupUsername.getText().isEmpty()
                || signupPassword.getText().isEmpty()) {
            signupError.setText("All fields are mandatory");
            return false;
        } else if (signupUsername.getText().contains(" ") || signupPassword.getText().contains(" ")
                || signupMail.getText().contains(" ") || signupAge.getText().contains(" ")) {
            signupError.setText("Fields can't contain spaces");
            return false;
        } else if (signupPassword.getLength() < 8) {
            signupError.setText("Password should be longer than 8 chars");
            return false;
        } else if (!signupMail.getText().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            signupError.setText("Enter valid mail");
            return false;
        }
        return true;
    }

    // adds a new user to Users table
    public void signUp() {
        boolean status = false;
        try {
            if (validate()) {
                conn = Database.connect();

                // updating users table with new user
                try (PreparedStatement updateUsers = conn.prepareStatement(
                        "INSERT INTO Users (Username, Password, Age, Mail, IssueCount) VALUES(?,?,?,?,0)")) {

                    // settings prepared statement vars
                    updateUsers.setString(1, signupUsername.getText());
                    updateUsers.setString(2, Database.hashPassword(signupPassword.getText()));

                    try {
                        updateUsers.setInt(3, Integer.parseInt(signupAge.getText()));
                    } catch (NumberFormatException nfe) {
                        signupError.setText("Age must be a valid number");
                        return;
                    }

                    updateUsers.setString(4, signupMail.getText());
                    updateUsers.execute();
                    status = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            signupError.setText("Error during signup or username already exists");
            return;
        }
        if (status) {
            signupError.setText("Signup Successful! You can login now.");
        }
    }

    // leads to signup panel
    public void goToSignup() throws IOException {
        fxmlLoader = new FXMLLoader(Main.class.getResource("userSignup.fxml"));
        Main.mainStage.setScene(new Scene(fxmlLoader.load()));
    }

    // leads to signin pane;
    public void goToSignin() throws IOException {
        fxmlLoader = new FXMLLoader(Main.class.getResource("userLogin.fxml"));
        Main.mainStage.setScene(new Scene(fxmlLoader.load()));
    }

}
