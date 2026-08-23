package com.gestionstock.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

/*
    -@FXML: Annotation qui connecte un attribut Java à un composant déclaré dans le fichier XML via son fx:id
    -initialize(): méthode spéciale appelée automatiquement par JavaFx après le chargement du FXML
 */
public class MainController {
    @FXML
    private StackPane contenuPrincipale;

    @FXML
    public void initialize() { afficherDashboard();}

    @FXML
    private void afficherDashboard() {
        contenuPrincipale.getChildren().clear();
        contenuPrincipale.getChildren().add(new Label("Dashboard"));
    }

    @FXML
    private void afficherProduits() {
        chargerVue("/com/gestionstock/produits.fxml");
    }

    @FXML
    private void afficherCategories() {
        chargerVue("/com/gestionstock/categories.fxml");
    }

    @FXML
    private void afficherFournisseurs() {
        chargerVue("/com/gestionstock/fournisseurs.fxml");
    }

    private void chargerVue(String cheminFxml) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(cheminFxml)
            );
            Node vue = loader.load();
            contenuPrincipale.getChildren().clear();
            contenuPrincipale.getChildren().add(vue);
        } catch(Exception e){
            //correction
            e.printStackTrace();
            Alert alerte = new Alert(Alert.AlertType.ERROR);
            alerte.setTitle("Erreur de chargement");
            alerte.setHeaderText(null);
            alerte.setContentText("Impossible d'afficher cet écran pour le moment. Il n'est peut-être pas encore disponible.");
            alerte.showAndWait();
        }
    }
}
