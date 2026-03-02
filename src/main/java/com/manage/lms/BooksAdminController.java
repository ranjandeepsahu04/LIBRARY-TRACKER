package com.manage.lms;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.cell.PropertyValueFactory;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;

public class BooksAdminController implements Initializable {

    @FXML
    private TableView<Books> adminBooksTable;
    @FXML
    public TableView<Books> adminIssuedTable;
    @FXML
    public TextField searchAdminBooks;
    @FXML
    public ComboBox<String> sortAdminBooks;
    @FXML
    public ComboBox<String> filterCategoryAdmin;
    @FXML
    public TextField searchAdminIssued;
    static ObservableList<Books> booksObservableList;
    static ObservableList<Books> issuedBooksObservableList;
    @FXML
    public TableColumn<Books, String> bname;
    @FXML
    public TableColumn<Books, String> bauthor;
    @FXML
    public TableColumn<Books, Integer> byear;
    @FXML
    public TableColumn<Books, Integer> bstocks;
    @FXML
    public TableColumn<Books, String> categoryAdmin;
    @FXML
    public TableColumn<Books, String> issuedUser;
    @FXML
    public TableColumn<Books, String> issuedName;
    @FXML
    public TableColumn<Books, String> issuedIssueDate;
    @FXML
    public TableColumn<Books, String> issuedReturnDate;
    @FXML
    public TableColumn<Books, Integer> issuedDays;
    @FXML
    public TableColumn<Books, Integer> issuedFineAdmin;
    @FXML
    public javafx.scene.chart.PieChart analyticsChart;

    public static Connection connection;
    private static ObservableList<Books> bookList;
    public static FXMLLoader fxmlLoader;

    public BooksAdminController() {
        bookList = FXCollections.observableArrayList();
    }

    // loads books from mysql db
    public static void loadBooks(Connection conn) {
        String getDataQuery = "SELECT * FROM Books";
        String getIssuedQuery = "SELECT * FROM IssuedBooks";
        connection = conn;
        if (booksObservableList == null)
            booksObservableList = FXCollections.observableArrayList();
        if (issuedBooksObservableList == null)
            issuedBooksObservableList = FXCollections.observableArrayList();

        booksObservableList.clear();
        issuedBooksObservableList.clear();

        try {
            Statement s = connection.createStatement();
            ResultSet rs = s.executeQuery(getDataQuery);
            while (rs.next()) {
                String cat = "General";
                try {
                    cat = rs.getString("Category");
                } catch (Exception ignored) {
                }
                Books book = new Books(rs.getString("Book Name"), rs.getString("Author"), rs.getInt("Year"),
                        rs.getInt("Stocks"), cat);
                booksObservableList.add(book);
            }

            Statement issuedStmt = connection.createStatement();
            ResultSet rs_issued = issuedStmt.executeQuery(getIssuedQuery);
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
                issuedBooksObservableList.add(book);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Closes the connection and returns to home
    public void logoutAdmin() {
        try {
            connection.close();
            fxmlLoader = new FXMLLoader(Main.class.getResource("lms.fxml"));
            Main.getMainStage().setScene(new Scene(fxmlLoader.load()));
        } catch (IOException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // inserts the book
    @FXML
    void insertBook() {
        NewBookController nbc = new NewBookController();
        nbc.addBook(connection, adminBooksTable);
    }

    // initializes the table
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        bname.setCellValueFactory(new PropertyValueFactory<Books, String>("title"));
        bauthor.setCellValueFactory(new PropertyValueFactory<Books, String>("author"));
        categoryAdmin.setCellValueFactory(new PropertyValueFactory<Books, String>("category"));
        byear.setCellValueFactory(new PropertyValueFactory<Books, Integer>("year"));
        bstocks.setCellValueFactory(new PropertyValueFactory<Books, Integer>("stocks"));

        if (filterCategoryAdmin != null) {
            filterCategoryAdmin.getItems().addAll("All Categories", "Software Engineering", "Computer Science",
                    "Programming Languages", "DevOps", "Database", "AI / ML", "Frameworks", "Data Science", "Career",
                    "General");
            filterCategoryAdmin.getSelectionModel().selectFirst();
        }

        javafx.collections.transformation.FilteredList<Books> filteredBooks = new javafx.collections.transformation.FilteredList<>(
                booksObservableList, b -> true);
        if (searchAdminBooks != null || filterCategoryAdmin != null) {
            Runnable updateAdminPredicate = () -> {
                String searchText = searchAdminBooks != null ? searchAdminBooks.getText() : "";
                String categoryValue = filterCategoryAdmin != null ? filterCategoryAdmin.getValue() : "All Categories";

                filteredBooks.setPredicate(book -> {
                    boolean matchesSearch = true;
                    boolean matchesCategory = true;

                    if (searchText != null && !searchText.isEmpty()) {
                        String lower = searchText.toLowerCase();
                        matchesSearch = book.getTitle().toLowerCase().contains(lower) ||
                                book.getAuthor().toLowerCase().contains(lower) ||
                                String.valueOf(book.getYear()).contains(lower);
                    }

                    if (categoryValue != null && !categoryValue.equals("All Categories")) {
                        matchesCategory = book.getCategory().equalsIgnoreCase(categoryValue);
                    }

                    return matchesSearch && matchesCategory;
                });
                if (adminBooksTable != null)
                    adminBooksTable.refresh();
            };

            if (searchAdminBooks != null) {
                searchAdminBooks.textProperty()
                        .addListener((observable, oldValue, newValue) -> updateAdminPredicate.run());
            }
            if (filterCategoryAdmin != null) {
                filterCategoryAdmin.valueProperty()
                        .addListener((observable, oldValue, newValue) -> updateAdminPredicate.run());
            }
            updateAdminPredicate.run();
        }
        if (sortAdminBooks != null) {
            sortAdminBooks.getItems().addAll("Sort Books", "Title (A-Z)", "Title (Z-A)");
            sortAdminBooks.getSelectionModel().selectFirst();
        }
        javafx.collections.transformation.SortedList<Books> sortedBooks = new javafx.collections.transformation.SortedList<>(
                filteredBooks);
        sortedBooks.comparatorProperty().bind(javafx.beans.binding.Bindings.createObjectBinding(() -> {
            java.util.Comparator<Books> tableComp = adminBooksTable.comparatorProperty().get();
            java.util.Comparator<Books> customComp = null;
            if (sortAdminBooks != null && sortAdminBooks.getValue() != null) {
                if (sortAdminBooks.getValue().equals("Title (A-Z)")) {
                    customComp = java.util.Comparator.comparing(Books::getTitle, String.CASE_INSENSITIVE_ORDER);
                } else if (sortAdminBooks.getValue().equals("Title (Z-A)")) {
                    customComp = java.util.Comparator.comparing(Books::getTitle, String.CASE_INSENSITIVE_ORDER)
                            .reversed();
                }
            }
            if (customComp != null && tableComp != null)
                return customComp.thenComparing(tableComp);
            if (customComp != null)
                return customComp;
            return tableComp;
        }, adminBooksTable.comparatorProperty(), sortAdminBooks.valueProperty()));
        adminBooksTable.setItems(sortedBooks);

        issuedUser.setCellValueFactory(new PropertyValueFactory<Books, String>("username"));
        issuedName.setCellValueFactory(new PropertyValueFactory<Books, String>("title"));
        issuedIssueDate.setCellValueFactory(new PropertyValueFactory<Books, String>("issueDate"));
        issuedReturnDate.setCellValueFactory(new PropertyValueFactory<Books, String>("returnDate"));
        issuedDays.setCellValueFactory(new PropertyValueFactory<Books, Integer>("days"));
        issuedFineAdmin.setCellValueFactory(new PropertyValueFactory<Books, Integer>("fine"));

        javafx.collections.transformation.FilteredList<Books> filteredIssued = new javafx.collections.transformation.FilteredList<>(
                issuedBooksObservableList, b -> true);
        if (searchAdminIssued != null) {
            searchAdminIssued.textProperty().addListener((observable, oldValue, newValue) -> {
                filteredIssued.setPredicate(book -> {
                    if (newValue == null || newValue.isEmpty())
                        return true;
                    String lower = newValue.toLowerCase();
                    if (book.getUsername().toLowerCase().contains(lower))
                        return true;
                    if (book.getTitle().toLowerCase().contains(lower))
                        return true;
                    return false;
                });
            });
        }
        javafx.collections.transformation.SortedList<Books> sortedIssued = new javafx.collections.transformation.SortedList<>(
                filteredIssued);
        sortedIssued.comparatorProperty().bind(adminIssuedTable.comparatorProperty());
        adminIssuedTable.setItems(sortedIssued);

        if (analyticsChart != null) {
            java.util.Map<String, Integer> categoryCounts = new java.util.HashMap<>();
            for (Books book : issuedBooksObservableList) {
                String cat = "Unknown";
                for (Books allBk : booksObservableList) {
                    if (allBk.getTitle().equals(book.getTitle())) {
                        cat = allBk.getCategory();
                        break;
                    }
                }
                categoryCounts.put(cat, categoryCounts.getOrDefault(cat, 0) + 1);
            }
            javafx.collections.ObservableList<javafx.scene.chart.PieChart.Data> pieChartData = javafx.collections.FXCollections
                    .observableArrayList();
            for (java.util.Map.Entry<String, Integer> entry : categoryCounts.entrySet()) {
                pieChartData.add(new javafx.scene.chart.PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")",
                        entry.getValue()));
            }
            analyticsChart.setData(pieChartData);
        }
    }
}
