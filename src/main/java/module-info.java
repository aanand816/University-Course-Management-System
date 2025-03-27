module com.example.coursemanagementsystem {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires java.sql;
    requires com.zaxxer.hikari;         // Correct module name for HikariCP
    requires org.slf4j;
    requires com.oracle.database.jdbc;

    opens com.example.coursemanagementsystem to javafx.fxml;
    exports com.example.coursemanagementsystem;
}