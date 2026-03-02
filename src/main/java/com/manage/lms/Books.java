package com.manage.lms;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

//Books data model class
public class Books {
    SimpleStringProperty title;
    SimpleStringProperty author;
    SimpleIntegerProperty year;
    SimpleIntegerProperty stocks;

    // Additional fields for issued books
    SimpleStringProperty username;
    SimpleStringProperty issueDate;
    SimpleStringProperty returnDate;
    SimpleIntegerProperty days;
    SimpleIntegerProperty fine;
    SimpleStringProperty category;

    public Books(String title, String author, int year, int stocks, String category) {
        this.title = new SimpleStringProperty(title);
        this.author = new SimpleStringProperty(author);
        this.year = new SimpleIntegerProperty(year);
        this.stocks = new SimpleIntegerProperty(stocks);
        this.category = new SimpleStringProperty(category != null ? category : "General");
    }

    public Books(String title, String author, int year, int stocks) {
        this(title, author, year, stocks, "General");
    }

    public Books(String username, String title, String author, int year, String issueDate, String returnDate,
            int days, int fine) {
        this.username = new SimpleStringProperty(username);
        this.title = new SimpleStringProperty(title);
        this.author = new SimpleStringProperty(author);
        this.year = new SimpleIntegerProperty(year);
        this.issueDate = new SimpleStringProperty(issueDate != null ? issueDate : "");
        this.returnDate = new SimpleStringProperty(returnDate != null ? returnDate : "");
        this.days = new SimpleIntegerProperty(days);
        this.fine = new SimpleIntegerProperty(fine);
        this.stocks = new SimpleIntegerProperty(0);
        this.category = new SimpleStringProperty("General");
    }

    //////////////////////////// getters////////////////////////////////////
    // value return
    public String getAuthor() {
        return author.get();
    }

    public String getTitle() {
        return title.get();
    }

    public int getStocks() {
        return stocks.get();
    }

    public int getYear() {
        return year.get();
    }

    public String getUsername() {
        return username != null ? username.get() : "";
    }

    public String getIssueDate() {
        return issueDate != null ? issueDate.get() : "";
    }

    public String getReturnDate() {
        return returnDate != null ? returnDate.get() : "";
    }

    public int getDays() {
        return days != null ? days.get() : 0;
    }

    public int getFine() {
        return fine != null ? fine.get() : 0;
    }

    public String getCategory() {
        return category != null ? category.get() : "General";
    }

    // Properties required for JavaFX bindings and sorting
    public SimpleStringProperty titleProperty() {
        return title;
    }

    public SimpleStringProperty authorProperty() {
        return author;
    }

    public SimpleIntegerProperty yearProperty() {
        return year;
    }

    public SimpleIntegerProperty stocksProperty() {
        return stocks;
    }

    public SimpleStringProperty usernameProperty() {
        return username;
    }

    public SimpleStringProperty issueDateProperty() {
        return issueDate;
    }

    public SimpleStringProperty returnDateProperty() {
        return returnDate;
    }

    public SimpleIntegerProperty daysProperty() {
        return days;
    }

    public SimpleIntegerProperty fineProperty() {
        return fine;
    }

    public SimpleStringProperty categoryProperty() {
        return category;
    }

}
