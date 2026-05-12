module com.lab2.javalab3 {
    requires java.desktop;
    requires java.naming;
    requires java.sql;
    requires jakarta.persistence;
    requires javafx.controls;
    requires javafx.fxml;
    requires org.hibernate.orm.community.dialects;
    requires org.hibernate.orm.core;
    requires org.slf4j;
    requires org.xerial.sqlitejdbc;

    exports com.lab2.javalab3;
    exports com.lab2.javalab3.client;
    exports com.lab2.javalab3.common;
    exports com.lab2.javalab3.common.model;
    exports com.lab2.javalab3.server;

    opens com.lab2.javalab3 to javafx.fxml;
    opens com.lab2.javalab3.client to javafx.fxml;
    opens com.lab2.javalab3.server.db to org.hibernate.orm.core;
}
