package com.manage.lms;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javafx.scene.control.ComboBox;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import java.net.URL;
import java.util.ResourceBundle;

public class NewBookController implements Initializable {
    public Button newBookCancel;
    public Button newBookAdd;
    public Label newBookError;
    public TextField newBookStocks;
    public TextField newBookYear;
    public TextField newBookAuthor;
    public TextField newBookName;
    @FXML
    public ComboBox<String> newBookCategory;
    public Scene panelScene;
    public static Connection connection;
    public static TableView<Books> booksTable;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (newBookCategory != null) {
            newBookCategory.getItems().addAll("Software Engineering", "Computer Science", "Programming Languages",
                    "DevOps", "Database", "AI / ML", "Frameworks", "Data Science", "Career", "General");
            newBookCategory.getSelectionModel().selectFirst();
        }
    }

    // loads scene for adding book
    void addBook(Connection conn, TableView<Books> adminBooksTable) {
        booksTable = adminBooksTable;
        connection = conn;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("newBook.fxml"));
            panelScene = Main.getMainStage().getScene();
            Main.mainStage.setScene(new Scene(loader.load()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // updating DB with new book detail
    public void Book2DB() {
        try {
            if (newBookName.getText().isEmpty() || newBookAuthor.getText().isEmpty() ||
                    newBookYear.getText().isEmpty() || newBookStocks.getText().isEmpty()) {
                newBookError.setText("All fields are mandatory.");
                return;
            }

            int year = Integer.parseInt(newBookYear.getText().trim());
            int stocks = Integer.parseInt(newBookStocks.getText().trim());

            if (stocks < 0) {
                newBookError.setText("Available stocks cannot be negative.");
                return;
            }

            PreparedStatement newBook = connection.prepareStatement("INSERT INTO Books VALUES(?,?,?,?,?)");
            newBook.setString(1, newBookName.getText().trim());
            newBook.setString(2, newBookAuthor.getText().trim());
            newBook.setInt(3, year);
            newBook.setInt(4, stocks);
            newBook.setString(5, newBookCategory.getValue() != null ? newBookCategory.getValue() : "General");
            newBook.execute();

            BooksAdminController.loadBooks(connection);

            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Book '" + newBookName.getText().trim() + "' has been added successfully.");
            alert.showAndWait();

            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("adminPanel.fxml"));
            Main.mainStage.setScene(new Scene(fxmlLoader.load()));
        } catch (NumberFormatException e) {
            newBookError.setText("Year and Stocks must be valid numbers.");
        } catch (SQLException e) {
            newBookError.setText("Database error. Book may already exist.");
        } catch (Exception e) {
            newBookError.setText("An unexpected error occurred.");
            e.printStackTrace();
        }
    }

    // cancel button back to admin panel
    public void cancelNewBook() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("adminPanel.fxml"));
        Main.mainStage.setScene(new Scene(fxmlLoader.load()));
    }
}
