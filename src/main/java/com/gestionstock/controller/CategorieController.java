package com.gestionstock.controller;

import com.gestionstock.model.Categorie;
import com.gestionstock.service.CategorieService;
import com.gestionstock.service.CategorieServiceImpl;
import javafx.beans.property.SimpleLongProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

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

    private final CategorieService categorieService = new CategorieServiceImpl();
    private ObservableList<Categorie> listeCategories;

    @FXML
    public void initialize() {
        configurerColonnes();
        chargerDonnees();
    }

    private void configurerColonnes() {
        colonneNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colonneDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        // Pas un attribut direct de Categorie : on le calcule via le service pour chaque ligne.
        colonneNbProduits.setCellValueFactory(data ->
                new SimpleLongProperty(categorieService.countProduits(data.getValue().getId())).asObject());
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

    @FXML
    private void ouvrirModification() {
        Categorie selection = tableCategories.getSelectionModel().getSelectedItem();
        if (selection == null) {
            afficherInfo("Aucune sélection", "Veuillez sélectionner une catégorie à modifier.");
            return;
        }
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
     * Construit et affiche un formulaire (nom + description) réutilisé pour l'ajout ET la modification.
     * Retourne la Categorie mise à jour si l'utilisateur valide, vide sinon (Annuler).
     */
    private Optional<Categorie> afficherFormulaire(Categorie categorie, String titre) {
        Dialog<Categorie> dialog = new Dialog<>();
        dialog.setTitle(titre);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField champNom = new TextField(categorie.getNom());
        champNom.setPromptText("Nom de la catégorie");
        TextArea champDescription = new TextArea(categorie.getDescription());
        champDescription.setPromptText("Description");
        champDescription.setPrefRowCount(3);

        Label labelErreur = new Label();
        labelErreur.setStyle("-fx-text-fill: red;");

        GridPane grille = new GridPane();
        grille.setHgap(10);
        grille.setVgap(10);
        grille.addRow(0, new Label("Nom *"), champNom);
        grille.addRow(1, new Label("Description"), champDescription);
        grille.add(labelErreur, 0, 2, 2, 1);
        dialog.getDialogPane().setContent(grille);

        // Validation en temps réel : le bouton OK reste désactivé tant que le nom est vide
        Button boutonOk = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        boutonOk.setDisable(champNom.getText() == null || champNom.getText().trim().length() < 2);
        champNom.textProperty().addListener((obs, ancien, nouveau) -> {
            boolean valide = nouveau != null && nouveau.trim().length() >= 2;
            boutonOk.setDisable(!valide);
            labelErreur.setText(valide ? "" : "Le nom doit contenir au moins 2 caractères.");
        });

        dialog.setResultConverter(boutonClique -> {
            if (boutonClique == ButtonType.OK) {
                categorie.setNom(champNom.getText().trim());
                categorie.setDescription(champDescription.getText());
                return categorie;
            }
            return null;
        });

        return dialog.showAndWait();
    }

    private void afficherInfo(String titre, String message) {
        Alert alerte = new Alert(Alert.AlertType.INFORMATION);
        alerte.setTitle(titre);
        alerte.setHeaderText(null);
        alerte.setContentText(message);
        alerte.showAndWait();
    }

    private void afficherErreur(String titre, String message) {
        Alert alerte = new Alert(Alert.AlertType.ERROR);
        alerte.setTitle(titre);
        alerte.setHeaderText(null);
        alerte.setContentText(message);
        alerte.showAndWait();
    }
}
