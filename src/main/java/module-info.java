module com.agenda {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;

    opens com.agenda to javafx.fxml;
    opens com.agenda.controllers to javafx.fxml;
    opens com.agenda.models to javafx.base;

    exports com.agenda;
}
