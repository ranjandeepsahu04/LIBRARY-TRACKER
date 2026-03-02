package com.manage.lms;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class BooksUserController implements Initializable {
    public static Connection connection;
    public static FXMLLoader fxmlLoader;
    @FXML
    public TableColumn<Books, String> bnameUser;
    @FXML
    public TableColumn<Books, String> bauthorUser;
    @FXML
    public TableColumn<Books, Integer> byearUser;
    @FXML
    public TableColumn<Books, Integer> bstocksUser;
    @FXML
    public TableColumn<Books, String> categoryUser;
    public TableView<Books> userAllTables;
    @FXML
    public TextField searchUserBooks;
    @FXML
    public ComboBox<String> sortUserBooks;
    @FXML
    public ComboBox<String> filterCategoryUser;
    static ObservableList<Books> booksObservableList;

    private static ObservableList<Books> issuedList;
    public Button issueButton;
    public Button returnButton;

    private static Books selectedBook;
    private static Books selectedIssuedBook;
    public TableColumn<Books, String> issuedName;
    public TableColumn<Books, String> issuedIssueDate;
    public TableColumn<Books, String> issuedReturnDate;
    public TableColumn<Books, Integer> issuedDays;
    public TableColumn<Books, Integer> issuedFineUser;
    public TableView<Books> issuedTable;
    public Label usernameID;
    public Label userEmail;
    public Label userAge;
    public Label issueCountLabel;

    // loads books and set to table to show available books
    // ------------------WIP-----------------//
    public static void loadBooks(Connection conn) {
        String getDataQuery = "SELECT * FROM Books";
        String getIssuedQuery = "SELECT * FROM IssuedBooks WHERE Username=?";
        connection = conn;
        if (booksObservableList == null)
            booksObservableList = FXCollections.observableArrayList();
        if (issuedList == null)
            issuedList = FXCollections.observableArrayList();
        booksObservableList.clear();
        issuedList.clear();
        try {
            Statement all = connection.createStatement();
            PreparedStatement issued = connection.prepareStatement(getIssuedQuery);
            issued.setString(1, UserLoginSignupController.username);
            ResultSet rs_all = all.executeQuery(getDataQuery);
            ResultSet rs_issued = issued.executeQuery();
            while (rs_all.next()) {
                String cat = "General";
                try {
                    cat = rs_all.getString("Category");
                } catch (Exception ignored) {
                }
                Books book = new Books(rs_all.getString("Book Name"), rs_all.getString("Author"), rs_all.getInt("Year"),
                        rs_all.getInt("Stocks"), cat);
                booksObservableList.add(book);
            }
            while (rs_issued.next()) {
                String issueDateStr = rs_issued.getString("IssueDate");
                String returnDateStr = rs_issued.getString("ReturnDate");
                java.time.LocalDate issueDate = null;
                java.time.LocalDate returnDate = null;
                int days = 0;

                try {
                    if (issueDateStr != null && !issueDateStr.isEmpty()) {
                        issueDate = java.time.LocalDate.parse(issueDateStr);
                    }
                    if (returnDateStr != null && !returnDateStr.isEmpty()) {
                        returnDate = java.time.LocalDate.parse(returnDateStr);
                    }
                } catch (Exception e) {
                }

                if (issueDate != null) {
                    if (returnDate != null) {
                        days = (int) java.time.temporal.ChronoUnit.DAYS.between(issueDate, returnDate);
                    } else {
                        days = (int) java.time.temporal.ChronoUnit.DAYS.between(issueDate, java.time.LocalDate.now());
                    }
                }

                int fine = Math.max(0, days - 14) * 2; // Assuming 14 day grace period, $2 per day fine

                Books book = new Books(
                        rs_issued.getString("Username"),
                        rs_issued.getString("BookName"),
                        rs_issued.getString("Author"),
                        rs_issued.getInt("Year"),
                        issueDateStr,
                        returnDateStr,
                        days,
                        fine);
                issuedList.add(book);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    ////////////////////////

    // closes connection and loads home scene
    public void logoutUser() {
        try {
            connection.close();
            fxmlLoader = new FXMLLoader(Main.class.getResource("lms.fxml"));
            Main.getMainStage().setScene(new Scene(fxmlLoader.load()));
        } catch (IOException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void changePassword() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Change Password");
        dialog.setHeaderText("Secure your account with a new password");

        // Styling the Dialog Pane
        dialog.getDialogPane().setStyle("-fx-font-family: 'Segoe UI', Arial, sans-serif; -fx-background-color: white;");
        dialog.getDialogPane().lookup(".header-panel").setStyle("-fx-background-color: #3498db;");

        ButtonType changeButtonType = new ButtonType("Change Password",
                javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(changeButtonType, ButtonType.CANCEL);

        // Styling Buttons
        javafx.scene.Node changeBtn = dialog.getDialogPane().lookupButton(changeButtonType);
        changeBtn.setStyle(
                "-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("New Password (min 8 chars)");
        passwordField.setStyle(
                "-fx-pref-width: 250px; -fx-pref-height: 35px; -fx-background-radius: 5px; -fx-border-radius: 5px; -fx-border-color: #bdc3c7;");

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm New Password");
        confirmPasswordField.setStyle(
                "-fx-pref-width: 250px; -fx-pref-height: 35px; -fx-background-radius: 5px; -fx-border-radius: 5px; -fx-border-color: #bdc3c7;");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 13px; -fx-font-weight: bold;");

        javafx.scene.layout.VBox vbox = new javafx.scene.layout.VBox();
        vbox.getChildren().addAll(passwordField, confirmPasswordField, errorLabel);
        vbox.setSpacing(15);
        dialog.getDialogPane().setContent(vbox);

        // Disable 'Change' button initially
        changeBtn.setDisable(true);

        // Validate on typing
        javafx.beans.value.ChangeListener<String> validator = (obs, oldVal, newVal) -> {
            boolean isLongEnough = passwordField.getText().length() >= 8;
            boolean hasNoSpaces = !passwordField.getText().contains(" ");
            boolean passwordsMatch = passwordField.getText().equals(confirmPasswordField.getText());

            if (!isLongEnough) {
                errorLabel.setText("Password must be at least 8 characters.");
                changeBtn.setDisable(true);
            } else if (!hasNoSpaces) {
                errorLabel.setText("Password cannot contain spaces.");
                changeBtn.setDisable(true);
            } else if (!passwordsMatch) {
                errorLabel.setText("Passwords do not match.");
                changeBtn.setDisable(true);
            } else {
                errorLabel.setText("");
                changeBtn.setDisable(false);
            }
        };

        passwordField.textProperty().addListener(validator);
        confirmPasswordField.textProperty().addListener(validator);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == changeButtonType) {
                return passwordField.getText();
            }
            return null;
        });

        java.util.Optional<String> result = dialog.showAndWait();
        result.ifPresent(newPassword -> {
            try {
                PreparedStatement updateStmt = connection
                        .prepareStatement("UPDATE Users SET Password=? WHERE Username=?");
                updateStmt.setString(1, Database.hashPassword(newPassword));
                updateStmt.setString(2, UserLoginSignupController.username);
                updateStmt.executeUpdate();

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText("Password changed successfully! You can use it on your next login.");
                alert.showAndWait();
            } catch (SQLException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR, "Database connection error. Password NOT changed.");
                alert.showAndWait();
            }
        });
    }

    public void issueBook() throws SQLException {
        if (selectedBook == null)
            return;

        // Option 3: Real-Time Stock management (Check Stock)
        if (selectedBook.getStocks() <= 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Out of Stock");
            alert.setHeaderText(null);
            alert.setContentText("We're sorry, '" + selectedBook.getTitle() + "' is currently out of stock!");
            alert.showAndWait();
            return;
        }

        String issueQuery = "INSERT INTO IssuedBooks (Username, BookName, Author, Year, IssueDate) VALUES(?,?,?,?,?)";
        PreparedStatement updateIssued = connection.prepareStatement(issueQuery);
        updateIssued.setString(1, UserLoginSignupController.username);
        updateIssued.setString(2, selectedBook.getTitle());
        updateIssued.setString(3, selectedBook.getAuthor());
        updateIssued.setInt(4, selectedBook.getYear());
        updateIssued.setString(5, java.time.LocalDate.now().toString());
        int inserts = updateIssued.executeUpdate();

        if (inserts > 0) {
            // Option 3: Real-Time Stock management (Decrement)
            String stockQuery = "UPDATE Books SET Stocks = Stocks - 1 WHERE \"Book Name\"=?";
            PreparedStatement stockStmt = connection.prepareStatement(stockQuery);
            stockStmt.setString(1, selectedBook.getTitle());
            stockStmt.execute();

            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Success");
            success.setHeaderText(null);
            success.setContentText("You have successfully issued '" + selectedBook.getTitle() + "'. Happy reading!");
            success.showAndWait();
        }

        refreshUI();
    }

    public void returnBook() throws SQLException {
        if (selectedIssuedBook == null)
            return;

        String bookName = selectedIssuedBook.getTitle();
        String returnQuery = "DELETE FROM IssuedBooks WHERE Username=? AND BookName=?";
        PreparedStatement returnBook = connection.prepareStatement(returnQuery);
        returnBook.setString(1, UserLoginSignupController.username);
        returnBook.setString(2, bookName);
        int deleted = returnBook.executeUpdate();

        if (deleted > 0) {
            // Option 3: Real-Time Stock management (Increment)
            String stockQuery = "UPDATE Books SET Stocks = Stocks + 1 WHERE \"Book Name\"=?";
            PreparedStatement stockStmt = connection.prepareStatement(stockQuery);
            stockStmt.setString(1, bookName);
            stockStmt.execute();

            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Success");
            success.setHeaderText(null);
            success.setContentText("You have successfully returned '" + bookName + "'. Thank you!");
            success.showAndWait();
        }

        refreshUI();
    }

    public void refreshUI() {
        if (userAllTables != null && userAllTables.getSelectionModel() != null)
            userAllTables.getSelectionModel().clearSelection();
        if (issuedTable != null && issuedTable.getSelectionModel() != null)
            issuedTable.getSelectionModel().clearSelection();
        selectedBook = null;
        selectedIssuedBook = null;
        if (issueButton != null)
            issueButton.setDisable(true);
        if (returnButton != null)
            returnButton.setDisable(true);

        loadBooks(connection);

        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt
                    .executeQuery("SELECT * FROM Users WHERE Username='" + UserLoginSignupController.username + "'");
            if (rs.next()) {
                userEmail.setText(rs.getString("Mail"));
                userAge.setText("Age: " + rs.getInt("Age"));
            }
            issueCountLabel.setText(String.valueOf(issuedList.size()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (sortUserBooks != null) {
            sortUserBooks.getItems().addAll("Sort Books", "Title (A-Z)", "Title (Z-A)");
            sortUserBooks.getSelectionModel().selectFirst();
        }
        if (filterCategoryUser != null) {
            filterCategoryUser.getItems().addAll("All Categories", "Software Engineering", "Computer Science",
                    "Programming Languages", "DevOps", "Database", "AI / ML", "Frameworks", "Data Science", "Career",
                    "General");
            filterCategoryUser.getSelectionModel().selectFirst();
        }
        userAllTables.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            selectedBook = newValue;
            if (issueButton != null)
                issueButton.setDisable(selectedBook == null);
        });
        issuedTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            selectedIssuedBook = newValue;
            if (returnButton != null)
                returnButton.setDisable(selectedIssuedBook == null);
        });
        usernameID.setText(UserLoginSignupController.username);

        bnameUser.setCellValueFactory(new PropertyValueFactory<Books, String>("title"));
        bauthorUser.setCellValueFactory(new PropertyValueFactory<Books, String>("author"));
        categoryUser.setCellValueFactory(new PropertyValueFactory<Books, String>("category"));
        byearUser.setCellValueFactory(new PropertyValueFactory<Books, Integer>("year"));
        bstocksUser.setCellValueFactory(new PropertyValueFactory<Books, Integer>("stocks"));
        issuedName.setCellValueFactory(new PropertyValueFactory<Books, String>("title"));
        issuedIssueDate.setCellValueFactory(new PropertyValueFactory<Books, String>("issueDate"));
        issuedReturnDate.setCellValueFactory(new PropertyValueFactory<Books, String>("returnDate"));
        issuedDays.setCellValueFactory(new PropertyValueFactory<Books, Integer>("days"));
        issuedFineUser.setCellValueFactory(new PropertyValueFactory<Books, Integer>("fine"));

        refreshUI();

        javafx.collections.transformation.FilteredList<Books> filteredData = new javafx.collections.transformation.FilteredList<>(
                booksObservableList, b -> true);

        Runnable updatePredicate = () -> {
            String searchText = searchUserBooks != null ? searchUserBooks.getText() : "";
            String categoryValue = filterCategoryUser != null ? filterCategoryUser.getValue() : "All Categories";

            filteredData.setPredicate(book -> {
                boolean matchesSearch = true;
                boolean matchesCategory = true;

                if (searchText != null && !searchText.isEmpty()) {
                    String lowerCaseFilter = searchText.toLowerCase();
                    matchesSearch = book.getTitle().toLowerCase().contains(lowerCaseFilter) ||
                            book.getAuthor().toLowerCase().contains(lowerCaseFilter) ||
                            String.valueOf(book.getYear()).contains(lowerCaseFilter);
                }

                if (categoryValue != null && !categoryValue.equals("All Categories")) {
                    matchesCategory = book.getCategory().equalsIgnoreCase(categoryValue);
                }

                return matchesSearch && matchesCategory;
            });
            if (userAllTables != null)
                userAllTables.refresh();
        };

        if (searchUserBooks != null) {
            searchUserBooks.textProperty().addListener((observable, oldValue, newValue) -> updatePredicate.run());
        }
        if (filterCategoryUser != null) {
            filterCategoryUser.valueProperty().addListener((observable, oldValue, newValue) -> updatePredicate.run());
        }

        javafx.collections.transformation.SortedList<Books> sortedData = new javafx.collections.transformation.SortedList<>(
                filteredData);
        sortedData.comparatorProperty().bind(javafx.beans.binding.Bindings.createObjectBinding(() -> {
            java.util.Comparator<Books> tableComp = userAllTables.comparatorProperty().get();
            java.util.Comparator<Books> customComp = null;
            if (sortUserBooks != null && sortUserBooks.getValue() != null) {
                if (sortUserBooks.getValue().equals("Title (A-Z)")) {
                    customComp = java.util.Comparator.comparing(Books::getTitle, String.CASE_INSENSITIVE_ORDER);
                } else if (sortUserBooks.getValue().equals("Title (Z-A)")) {
                    customComp = java.util.Comparator.comparing(Books::getTitle, String.CASE_INSENSITIVE_ORDER)
                            .reversed();
                }
            }
            if (customComp != null && tableComp != null)
                return customComp.thenComparing(tableComp);
            if (customComp != null)
                return customComp;
            return tableComp;
        }, userAllTables.comparatorProperty(), sortUserBooks.valueProperty()));
        userAllTables.setItems(sortedData);
        issuedTable.setItems(issuedList);

        updatePredicate.run();
    }
}
