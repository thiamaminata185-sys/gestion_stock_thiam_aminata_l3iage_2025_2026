package com.gestionstock.controller;

import com.gestionstock.model.Utilisateur;
import com.gestionstock.service.UtilisateurService;
import com.gestionstock.service.UtilisateurServiceImpl;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.util.List;
import java.util.Optional;

public class UtilisateurController {
    @FXML
    private TableView<Utilisateur> tableUtilisateurs;
    @FXML
    private TableColumn<Utilisateur, String> colonneNom;
    @FXML
    private TableColumn<Utilisateur, String> colonneEmail;
    @FXML
    private TableColumn<Utilisateur, String> colonneRole;
    @FXML
    private TableColumn<Utilisateur, String> colonneStatut;
    @FXML
    private TableColumn<Utilisateur, Void> colonneActions;

    private final UtilisateurService utilisateurService = new UtilisateurServiceImpl();

    @FXML
    public void initialize() {
        configurerColonnes();
        chargerDonnees();
        tableUtilisateurs.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

    }

    private void configurerColonnes() {
        colonneNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colonneEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colonneRole.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getRole().toString()));
        colonneStatut.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().isActif() ? "Actif" : "Désactivé"));
        configurerColonneActions();
    }

    /** Colonne "Actions" avec un bouton Activer/Désactiver dont le texte change selon l'état actuel. */
    private void configurerColonneActions() {
        colonneActions.setCellFactory(colonne -> new TableCell<>() {
            private final Button boutonBasculer = new Button();
            private final HBox conteneur = new HBox(boutonBasculer);

            {
                boutonBasculer.setOnAction(e -> basculerStatut(getUtilisateurDeLaLigne()));
            }

            private Utilisateur getUtilisateurDeLaLigne() {
                return getTableView().getItems().get(getIndex());
            }

            @Override
            protected void updateItem(Void item, boolean vide) {
                super.updateItem(item, vide);
                if (vide) {
                    setGraphic(null);
                } else {
                    Utilisateur utilisateur = getUtilisateurDeLaLigne();
                    boutonBasculer.setText(utilisateur.isActif() ? "Désactiver" : "Activer");
                    setGraphic(conteneur);
                }
            }
        });
    }

    private void chargerDonnees() {
        List<Utilisateur> utilisateurs = utilisateurService.findAllUtilisateurs();
        tableUtilisateurs.setItems(FXCollections.observableArrayList(utilisateurs));
    }

    private void basculerStatut(Utilisateur utilisateur) {
        boolean nouveauStatut = !utilisateur.isActif();
        String action = nouveauStatut ? "activer" : "désactiver";

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Voulez-vous vraiment " + action + " le compte de \"" + utilisateur.getNom() + "\" ?");
        Optional<ButtonType> reponse = confirmation.showAndWait();

        if (reponse.isPresent() && reponse.get() == ButtonType.OK) {
            try {
                utilisateurService.activerDesactiver(utilisateur.getId(), nouveauStatut);
                chargerDonnees();
            } catch (SecurityException e) {
                Alert erreur = new Alert(Alert.AlertType.ERROR);
                erreur.setTitle("Action non autorisée");
                erreur.setHeaderText(null);
                erreur.setContentText(e.getMessage());
                erreur.showAndWait();
            }
        }
    }
}
