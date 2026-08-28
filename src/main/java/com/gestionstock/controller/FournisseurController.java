package com.gestionstock.controller;

import com.gestionstock.model.Fournisseur;
import com.gestionstock.service.FournisseurService;
import com.gestionstock.service.FournisseurServiceImpl;
import javafx.beans.property.SimpleLongProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class FournisseurController {
    @FXML
    private TableView<Fournisseur> tableFournisseurs;
    @FXML
    private TableColumn<Fournisseur, String> colonneNom;
    @FXML
    private TableColumn<Fournisseur, String> colonneEmail;
    @FXML
    private TableColumn<Fournisseur, String> colonneTel;
    @FXML
    private TableColumn<Fournisseur, Long> colonneNbProduits;
    @FXML
    Button boutonSupprimer;
    @FXML
    private TableColumn<Fournisseur, Void> colonneActions;

    private final FournisseurService fournisseurService = new FournisseurServiceImpl();
    private ObservableList<Fournisseur> listeFournisseurs;

    @FXML
    public void initialize() {
        configurerColonnes();
        chargerDonnees();
        boutonSupprimer.setDisable(!com.gestionstock.util.SessionUtilisateur.estAdmin());
    }

    private void configurerColonnes() {
        colonneNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colonneEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colonneTel.setCellValueFactory(new PropertyValueFactory<>("tel"));
        colonneNbProduits.setCellValueFactory(data ->
                new SimpleLongProperty(fournisseurService.countProduits(data.getValue().getId())).asObject());
        configurerColonneActions();
    }

    /** Colonne "Actions" avec un bouton Modifier et un bouton Supprimer sur CHAQUE ligne. */
    private void configurerColonneActions() {
        colonneActions.setCellFactory(colonne -> new TableCell<>() {
            private final Button boutonModifier = new Button("Modifier");
            private final HBox conteneur = new HBox(6, boutonModifier);

            {
                boutonModifier.setOnAction(e -> ouvrirModification(getFournisseurDeLaLigne()));
            }

            private Fournisseur getFournisseurDeLaLigne() {
                return getTableView().getItems().get(getIndex());
            }

            @Override
            protected void updateItem(Void item, boolean vide) {
                super.updateItem(item, vide);
                setGraphic(vide ? null : conteneur);
            }
        });
    }

    private void chargerDonnees() {
        List<Fournisseur> fournisseurs = fournisseurService.findAllFournisseurs();
        listeFournisseurs = FXCollections.observableArrayList(fournisseurs);
        tableFournisseurs.setItems(listeFournisseurs);
    }

    @FXML
    private void ouvrirAjout() {
        Fournisseur nouveau = new Fournisseur();
        afficherFormulaire(nouveau, "Nouveau fournisseur").ifPresent(f -> {
            fournisseurService.addFournisseur(f);
            chargerDonnees();
        });
    }

    private void ouvrirModification(Fournisseur selection) {
        afficherFormulaire(selection, "Modifier le fournisseur").ifPresent(f -> {
            fournisseurService.updateFournisseur(f);
            chargerDonnees();
        });
    }

    @FXML
    private void supprimerFournisseur() {
        Fournisseur selection = tableFournisseurs.getSelectionModel().getSelectedItem();
        if (selection == null) {
            afficherInfo("Aucune sélection", "Veuillez sélectionner un fournisseur à supprimer.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Voulez-vous vraiment supprimer le fournisseur \"" + selection.getNom() + "\" ?");
        Optional<ButtonType> reponse = confirmation.showAndWait();

        if (reponse.isPresent() && reponse.get() == ButtonType.OK) {
            try {
                fournisseurService.deleteFournisseur(selection.getId());
                chargerDonnees();
            } catch (IllegalStateException e) {
                afficherErreur("Suppression impossible", e.getMessage());
            }
        }
    }

    /**
     * Charge AddFournisseurDialog.fxml dans un Dialog<Fournisseur> (même principe que ProduitController
     * avec AddProduitDialog.fxml). fournisseur.getId() == 0 -> mode ajout, sinon -> mode modification.
     */
    private Optional<Fournisseur> afficherFormulaire(Fournisseur fournisseur, String titre) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/gestionstock/AddFournisseurDialog.fxml"));
            Parent contenu = loader.load();
            AddFournisseurDialogController controleurFormulaire = loader.getController();

            if (fournisseur.getId() != 0) {
                controleurFormulaire.preremplirPour(fournisseur);
            }

            Dialog<Fournisseur> dialog = new Dialog<>();
            dialog.setTitle(titre);
            dialog.getDialogPane().setContent(contenu);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            Button boutonOk = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            boutonOk.setText("Enregistrer");
            boutonOk.setDisable(true);
            controleurFormulaire.setOnChangement(() -> boutonOk.setDisable(!controleurFormulaire.estValide()));

            dialog.setResultConverter(bouton -> {
                if (bouton == ButtonType.OK) {
                    return controleurFormulaire.construireFournisseurSiValide().orElse(null);
                }
                return null;
            });

            return dialog.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            afficherErreur("Erreur", "Impossible d'ouvrir le formulaire fournisseur.");
            return Optional.empty();
        }
    }

    private void afficherErreur(String titre, String message) {
        Alert alerte = new Alert(Alert.AlertType.ERROR);
        alerte.setTitle(titre);
        alerte.setHeaderText(null);
        alerte.setContentText(message);
        alerte.showAndWait();
    }

    private void afficherInfo(String titre, String message) {
        Alert alerte = new Alert(Alert.AlertType.INFORMATION);
        alerte.setTitle(titre);
        alerte.setHeaderText(null);
        alerte.setContentText(message);
        alerte.showAndWait();
    }
}
