module com.gestionstock {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jakarta.persistence;
    requires org.hibernate.orm.core;
    requires jbcrypt; // module automatique (jbcrypt-0.4.jar n'a pas de module-info)

    opens com.gestionstock to javafx.fxml, javafx.graphics;
    opens com.gestionstock.controller to javafx.fxml;
    opens com.gestionstock.model to javafx.fxml, javafx.base, org.hibernate.orm.core;
    opens com.gestionstock.model.enums to org.hibernate.orm.core;
}