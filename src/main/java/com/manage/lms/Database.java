package com.manage.lms;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class Database {
    public static final String URL = "jdbc:sqlite:lms.db";

    public static Connection connect() throws Exception {
        return DriverManager.getConnection(URL);
    }

    public static void init() {
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            // Create Users table
            stmt.execute("CREATE TABLE IF NOT EXISTS Users (" +
                    "Username TEXT PRIMARY KEY, " +
                    "Password TEXT NOT NULL, " +
                    "Age INTEGER, " +
                    "Mail TEXT, " +
                    "IssueCount INTEGER)");

            // Create Admins table
            stmt.execute("CREATE TABLE IF NOT EXISTS Admins (" +
                    "Username TEXT PRIMARY KEY, " +
                    "Password TEXT NOT NULL)");

            // Create Books table
            stmt.execute("CREATE TABLE IF NOT EXISTS Books (" +
                    "\"Book Name\" TEXT PRIMARY KEY, " +
                    "Author TEXT, " +
                    "Year INTEGER, " +
                    "Stocks INTEGER, " +
                    "Category TEXT)");

            try {
                stmt.execute("ALTER TABLE Books ADD COLUMN Category TEXT");
            } catch (Exception ignored) {
            }

            // Create IssuedBooks table
            stmt.execute("CREATE TABLE IF NOT EXISTS IssuedBooks (" +
                    "Username TEXT, " +
                    "BookName TEXT, " +
                    "Author TEXT, " +
                    "Year INTEGER, " +
                    "IssueDate TEXT, " +
                    "ReturnDate TEXT, " +
                    "Days INTEGER, " +
                    "FOREIGN KEY(Username) REFERENCES Users(Username))");

            // Alter existing IssuedBooks table to add columns in case the table already
            // exists without them
            try {
                stmt.execute("ALTER TABLE IssuedBooks ADD COLUMN IssueDate TEXT");
            } catch (Exception ignored) {
            }
            try {
                stmt.execute("ALTER TABLE IssuedBooks ADD COLUMN ReturnDate TEXT");
            } catch (Exception ignored) {
            }
            try {
                stmt.execute("ALTER TABLE IssuedBooks ADD COLUMN Days INTEGER");
            } catch (Exception ignored) {
            }

            // Insert default admin if none exists
            String adminHashed = hashPassword("admin");
            stmt.execute("INSERT OR IGNORE INTO Admins (Username, Password) VALUES ('admin', '" + adminHashed + "')");
            // Upgrade existing plaintext admin passwords
            stmt.execute(
                    "UPDATE Admins SET Password='" + adminHashed + "' WHERE Username='admin' AND Password='admin'");

            // Insert 50 default books using INSERT OR IGNORE
            String[] defaultBooks = {
                    "('Design Patterns', 'Gang of Four', 1994, 7, 'Software Engineering')",
                    "('Clean Code', 'Robert C. Martin', 2008, 5, 'Software Engineering')",
                    "('The Pragmatic Programmer', 'Andrew Hunt', 1999, 4, 'Software Engineering')",
                    "('Grokking Algorithms', 'Aditya Bhargava', 2016, 9, 'Computer Science')",
                    "('Refactoring', 'Martin Fowler', 1999, 2, 'Software Engineering')",
                    "('Code Complete', 'Steve McConnell', 2004, 6, 'Software Engineering')",
                    "('Effective Java', 'Joshua Bloch', 2018, 5, 'Programming Languages')",
                    "('Head First Design Patterns', 'Eric Freeman', 2004, 8, 'Software Engineering')",
                    "('Introduction to Algorithms', 'Thomas H. Cormen', 2009, 3, 'Computer Science')",
                    "('Cracking the Coding Interview', 'Gayle Laakmann McDowell', 2015, 12, 'Career')",
                    "('Structure and Interpretation of Computer Programs', 'Harold Abelson', 1984, 4, 'Computer Science')",
                    "('Designing Data-Intensive Applications', 'Martin Kleppmann', 2017, 8, 'Database')",
                    "('Clean Architecture', 'Robert C. Martin', 2017, 6, 'Software Engineering')",
                    "('Domain-Driven Design', 'Eric Evans', 2003, 5, 'Software Engineering')",
                    "('The Mythical Man-Month', 'Frederick P. Brooks Jr.', 1975, 4, 'Software Engineering')",
                    "('Working Effectively with Legacy Code', 'Michael Feathers', 2004, 3, 'Software Engineering')",
                    "('Patterns of Enterprise Application Architecture', 'Martin Fowler', 2002, 6, 'Software Engineering')",
                    "('Test Driven Development: By Example', 'Kent Beck', 2002, 5, 'Software Engineering')",
                    "('Continuous Delivery', 'Jez Humble', 2010, 4, 'DevOps')",
                    "('Site Reliability Engineering', 'Betsy Beyer', 2016, 7, 'DevOps')",
                    "('The Phoenix Project', 'Gene Kim', 2013, 9, 'DevOps')",
                    "('Algorithms, Part I', 'Robert Sedgewick', 2011, 8, 'Computer Science')",
                    "('Java Concurrency in Practice', 'Brian Goetz', 2006, 5, 'Programming Languages')",
                    "('Spring in Action', 'Craig Walls', 2018, 4, 'Frameworks')",
                    "('Kubernetes Up & Running', 'Kelsey Hightower', 2017, 5, 'DevOps')",
                    "('Docker Deep Dive', 'Nigel Poulton', 2020, 6, 'DevOps')",
                    "('Programming Rust', 'Jim Blandy', 2017, 4, 'Programming Languages')",
                    "('The Go Programming Language', 'Alan A. A. Donovan', 2015, 5, 'Programming Languages')",
                    "('Fluent Python', 'Luciano Ramalho', 2015, 6, 'Programming Languages')",
                    "('JavaScript: The Good Parts', 'Douglas Crockford', 2008, 7, 'Programming Languages')",
                    "('You Don''t Know JS', 'Kyle Simpson', 2015, 4, 'Programming Languages')",
                    "('Eloquent JavaScript', 'Marijn Haverbeke', 2018, 5, 'Programming Languages')",
                    "('Learning React', 'Alex Banks', 2020, 6, 'Frameworks')",
                    "('C++ Primer', 'Stanley B. Lippman', 2012, 3, 'Programming Languages')",
                    "('Effective C++', 'Scott Meyers', 2005, 5, 'Programming Languages')",
                    "('The C Programming Language', 'Brian W. Kernighan', 1978, 8, 'Programming Languages')",
                    "('Operating System Concepts', 'Abraham Silberschatz', 2018, 4, 'Computer Science')",
                    "('Computer Networking', 'James F. Kurose', 2016, 5, 'Computer Science')",
                    "('Artificial Intelligence: A Modern Approach', 'Stuart Russell', 2020, 7, 'AI / ML')",
                    "('Deep Learning', 'Ian Goodfellow', 2016, 6, 'AI / ML')",
                    "('Machine Learning Yearning', 'Andrew Ng', 2018, 4, 'AI / ML')",
                    "('Hands-On Machine Learning with Scikit-Learn', 'Aurélien Géron', 2019, 5, 'AI / ML')",
                    "('Data Science for Business', 'Foster Provost', 2013, 4, 'Data Science')",
                    "('Python Crash Course', 'Eric Matthes', 2019, 8, 'Programming Languages')",
                    "('Automate the Boring Stuff with Python', 'Al Sweigart', 2019, 9, 'Programming Languages')",
                    "('SQL Performance Explained', 'Markus Winand', 2012, 3, 'Database')",
                    "('Database Internals', 'Alex Petrov', 2019, 4, 'Database')",
                    "('High Performance MySQL', 'Baron Schwartz', 2012, 5, 'Database')",
                    "('Building Microservices', 'Sam Newman', 2015, 6, 'Software Engineering')",
                    "('Release It!', 'Michael T. Nygard', 2018, 4, 'Software Engineering')"
            };

            try (java.sql.PreparedStatement updateStmt = conn
                    .prepareStatement("UPDATE Books SET Category=? WHERE \"Book Name\"=?")) {
                for (String bookValues : defaultBooks) {
                    try {
                        // Using the predefined string values. The risk is minimized as this array is
                        // hardcoded locally.
                        stmt.execute("INSERT OR IGNORE INTO Books VALUES " + bookValues);
                        String[] parts = bookValues.split("'");
                        if (parts.length >= 6) {
                            String title = parts[1].replace("''", "'");
                            String category = parts[5];
                            updateStmt.setString(1, category);
                            updateStmt.setString(2, title);
                            updateStmt.execute();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static String hashPassword(String password) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
