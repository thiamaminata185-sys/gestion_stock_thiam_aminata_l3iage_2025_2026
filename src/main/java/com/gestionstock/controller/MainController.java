package com.gestionstock.controller;

import com.gestionstock.model.Utilisateur;
import com.gestionstock.util.SessionUtilisateur;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/*
    -@FXML: Annotation qui connecte un attribut Java à un composant déclaré dans le fichier XML via son fx:id
    -initialize(): méthode spéciale appelée automatiquement par JavaFx après le chargement du FXML
 */
public class MainController {
    @FXML
    private StackPane contenuPrincipale;
    @FXML
    private Label labelUtilisateurConnecte;
    @FXML
    private Button boutonDeconnexion;
    @FXML
    private Button btnUtilisateurs;


    @FXML
    public void initialize() {
        afficherUtilisateurConnecte();
        afficherDashboard();
        btnUtilisateurs.setVisible(SessionUtilisateur.estAdmin());
        btnUtilisateurs.setManaged(SessionUtilisateur.estAdmin());

    }

    private void afficherUtilisateurConnecte() {
        Utilisateur utilisateur = SessionUtilisateur.getUtilisateurConnecte();
        if (utilisateur != null) {
            labelUtilisateurConnecte.setText(utilisateur.getNom() + " (" + utilisateur.getRole() + ")");
        }
    }

    @FXML
    private void seDeconnecter() {
        SessionUtilisateur.deconnecter();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/gestionstock/LoginView.fxml"));
            Parent racine = loader.load();

            Stage stage = (Stage) boutonDeconnexion.getScene().getWindow();
            Scene scene = new Scene(racine);
            scene.getStylesheets().add(getClass().getResource("/com/gestionstock/style.css").toExternalForm());
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void afficherDashboard() {
        chargerVue("/com/gestionstock/dashboard.fxml");
    }

    @FXML
    private void afficherMouvements() {
        chargerVue("/com/gestionstock/mouvements.fxml");
    }

    @FXML
    private void afficherStatistiques() {
        chargerVue("/com/gestionstock/statistiques.fxml");
    }

    @FXML
    private void afficherUtilisateurs() {
        if (!com.gestionstock.util.SessionUtilisateur.estAdmin()) {
            Alert alerte = new Alert(Alert.AlertType.WARNING);
            alerte.setTitle("Accès refusé");
            alerte.setHeaderText(null);
            alerte.setContentText("Cette section est réservée aux administrateurs.");
            alerte.showAndWait();
            return;
        }
        chargerVue("/com/gestionstock/UtilisateursView.fxml");
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
