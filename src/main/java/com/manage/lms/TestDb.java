package com.manage.lms;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestDb {
    public static void main(String[] args) throws Exception {
        Connection conn = DriverManager.getConnection("jdbc:sqlite:lms.db");
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT \"Book Name\", Category FROM Books LIMIT 10");
        while (rs.next()) {
            System.out.println(rs.getString(1) + " -> " + rs.getString(2));
        }
        conn.close();
    }
}
