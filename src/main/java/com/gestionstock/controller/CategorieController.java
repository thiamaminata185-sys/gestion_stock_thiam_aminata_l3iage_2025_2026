package com.gestionstock.controller;

import com.gestionstock.model.Categorie;
import com.gestionstock.service.CategorieService;
import com.gestionstock.service.CategorieServiceImpl;
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

public class CategorieController {
    @FXML
    private TableView<Categorie> tableCategories;
    @FXML
    private TableColumn<Categorie, String> colonneNom;
    @FXML
    private TableColumn<Categorie, String> colonneDescription;
    @FXML
    private TableColumn<Categorie, Long> colonneNbProduits;
    @FXML
    Button boutonSupprimer;
    @FXML
    private TableColumn<Categorie, Void> colonneActions;

    private final CategorieService categorieService = new CategorieServiceImpl();
    private ObservableList<Categorie> listeCategories;

    @FXML
    public void initialize() {
        configurerColonnes();
        chargerDonnees();
        boutonSupprimer.setDisable(!com.gestionstock.util.SessionUtilisateur.estAdmin());
        tableCategories.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void configurerColonnes() {
        colonneNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colonneDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        // Pas un attribut direct de Categorie : on le calcule via le service pour chaque ligne.
        colonneNbProduits.setCellValueFactory(data ->
                new SimpleLongProperty(categorieService.countProduits(data.getValue().getId())).asObject());
        configurerColonneActions();
    }

    /** Colonne "Actions" avec un bouton Modifier  */
    private void configurerColonneActions() {
        colonneActions.setCellFactory(colonne -> new TableCell<>() {
            private final Button boutonModifier = new Button("Modifier");
            private final HBox conteneur = new HBox(6, boutonModifier);

            {
                boutonModifier.getStyleClass().add("bouton-jaune");
                boutonModifier.setOnAction(e -> ouvrirModification(getCategorieDeLaLigne()));
            }

            private Categorie getCategorieDeLaLigne() {
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
        List<Categorie> categories = categorieService.findAllCategories();
        listeCategories = FXCollections.observableArrayList(categories);
        tableCategories.setItems(listeCategories);
    }

    @FXML
    private void ouvrirAjout() {
        Categorie nouvelleCategorie = new Categorie();
        afficherFormulaire(nouvelleCategorie, "Nouvelle catégorie").ifPresent(categorie -> {
            categorieService.addCategorie(categorie);
            chargerDonnees();
        });
    }

    private void ouvrirModification(Categorie selection) {
        afficherFormulaire(selection, "Modifier la catégorie").ifPresent(categorie -> {
            categorieService.updateCategorie(categorie);
            chargerDonnees();
        });
    }

    @FXML
    private void supprimerCategorie() {
        Categorie selection = tableCategories.getSelectionModel().getSelectedItem();
        if (selection == null) {
            afficherInfo("Aucune sélection", "Veuillez sélectionner une catégorie à supprimer.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Voulez-vous vraiment supprimer la catégorie \"" + selection.getNom() + "\" ?");
        Optional<ButtonType> reponse = confirmation.showAndWait();

        if (reponse.isPresent() && reponse.get() == ButtonType.OK) {
            try {
                categorieService.deleteCategorie(selection.getId());
                chargerDonnees();
            } catch (IllegalStateException e) {
                // Message métier renvoyé par le service (produits encore rattachés)
                afficherErreur("Suppression impossible", e.getMessage());
            }
        }
    }

    /**
     * Charge AddCategorieDialog.fxml dans un Dialog<Categorie> (même principe que ProduitController
     * avec AddProduitDialog.fxml). categorie == null -> mode ajout, sinon -> mode modification.
     */
    private Optional<Categorie> afficherFormulaire(Categorie categorie, String titre) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/gestionstock/AddCategorieDialog.fxml"));
            Parent contenu = loader.load();
            AddCategorieDialogController controleurFormulaire = loader.getController();

            if (categorie.getId() != 0) { // catégorie existante -> mode modification
                controleurFormulaire.preremplirPour(categorie);
            }

            Dialog<Categorie> dialog = new Dialog<>();
            dialog.setTitle(titre);
            dialog.getDialogPane().setContent(contenu);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            Button boutonOk = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            boutonOk.setText("Enregistrer");
            boutonOk.setDisable(true);
            boutonOk.getStyleClass().add("bouton-vert");
            controleurFormulaire.setOnChangement(() -> boutonOk.setDisable(!controleurFormulaire.estValide()));

            dialog.setResultConverter(bouton -> {
                if (bouton == ButtonType.OK) {
                    return controleurFormulaire.construireCategorieSiValide().orElse(null);
                }
                return null;
            });

            return dialog.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            afficherErreur("Erreur", "Impossible d'ouvrir le formulaire catégorie.");
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
