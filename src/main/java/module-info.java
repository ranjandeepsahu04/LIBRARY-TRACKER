module com.manage.lms {
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive javafx.base;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    // requires eu.hansolo.tilesfx;

    opens com.manage.lms to javafx.fxml;

    exports com.manage.lms;
}