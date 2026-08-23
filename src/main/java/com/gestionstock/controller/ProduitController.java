package com.gestionstock.controller;

import com.gestionstock.model.Categorie;
import com.gestionstock.model.Fournisseur;
import com.gestionstock.service.ProduitService;
import com.gestionstock.service.ProduitServiceImpl;
import com.gestionstock.model.Produit;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.stage.Modality;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class ProduitController {
    @FXML
    TableView<Produit> tableProduits;
    @FXML
    TableColumn<Produit, Integer> colonneNom;
    @FXML
    TableColumn<Produit, Double> colonnePrix;
    @FXML
    TableColumn<Produit, Double> colonnePrixPromo;
    @FXML
    TableColumn<Produit, Integer> colonneStock;
    @FXML
    TableColumn<Produit, Integer> colonneStockMin;
    @FXML
    TableColumn<Produit, String> colonneCategorie;
    @FXML
    TableColumn<Produit, String> colonneFournisseur;
    @FXML
    TextField champRecherche;

    private final ProduitService produitService = new ProduitServiceImpl();

    // Liste complète chargée depuis la base, utilisée comme référence pour la recherche
    private ObservableList<Produit> listeProduits;

    @FXML
    public void initialize() {
        configurerColones();
        chargerDonnees();
    }

    private void configurerColones() {
          /*
            - PropertyValueFactory: indique à la colonne d'afficher la valeur retournée par getNom() sur chaque objet Produit
            - ObservableList: C'est une liste spéciale qui permet de mettre à jour automatiquement TableView lorsque
            des éléments sont ajoutés ou supprimés.
            - FXCollections.observableArrayList: crée une ObservableList à partir d'objets

            A RETENIR: PropertyValueFactory<>("nom") appelle automatiquement la méthode getNom()
            de la classe Produit. Il faut donc que les getters soient définis dans la classe modèle
         */
        // Lier chaque colonne à un attribut de la classe Produit
        colonneNom.setCellValueFactory( new PropertyValueFactory<>("nom"));
        colonnePrix.setCellValueFactory( new PropertyValueFactory<>("prix"));
        colonnePrixPromo.setCellValueFactory( new PropertyValueFactory<>("prixPromo"));
        colonneStock.setCellValueFactory( new PropertyValueFactory<>("quantiteStock"));
        colonneStockMin.setCellValueFactory( new PropertyValueFactory<>("quantiteMin"));
        colonneCategorie.setCellValueFactory( data -> {
            Categorie cat = data.getValue().getCategorie();
            return new SimpleStringProperty(cat != null ? cat.getNom() : "");
        });
        colonneFournisseur.setCellValueFactory( data -> {
            Fournisseur fournisseur = data.getValue().getFournisseur();
            return new SimpleStringProperty(fournisseur != null ? fournisseur.getNom() : "");
        });
    }

    private void chargerDonnees() {
        // Charger des données depuis la base via JDBC API
        List<Produit> produits = produitService.findAllProduits();

        listeProduits = FXCollections.observableArrayList(produits);

        tableProduits.setItems(listeProduits);
    }

    @FXML
    private void rechercherProduits() {
        String recherche = champRecherche.getText();

        if (recherche == null || recherche.isBlank()) {
            tableProduits.setItems(listeProduits);
            return;
        }

        String rechercheMinuscule = recherche.trim().toLowerCase();

        ObservableList<Produit> resultats = listeProduits.filtered(produit ->
                (produit.getNom() != null && produit.getNom().toLowerCase().contains(rechercheMinuscule))
                        //|| (produit.getCategorie() != null && produit.getCategorie_nom().toLowerCase().contains(rechercheMinuscule))
        );

        tableProduits.setItems(resultats);
    }

    @FXML
    private void supprimerProduit() {
        Produit produitSelectionne = tableProduits.getSelectionModel().getSelectedItem();

        if (produitSelectionne == null) {
            Alert alerteInfo = new Alert(Alert.AlertType.INFORMATION);
            alerteInfo.setTitle("Aucune sélection");
            alerteInfo.setHeaderText(null);
            alerteInfo.setContentText("Veuillez sélectionner un produit à supprimer.");
            alerteInfo.showAndWait();
            return;
        }

        Alert alerteConfirmation = new Alert(Alert.AlertType.CONFIRMATION);
        alerteConfirmation.setTitle("Confirmation de suppression");
        alerteConfirmation.setHeaderText(null);
        alerteConfirmation.setContentText("Voulez-vous vraiment supprimer le produit \"" + produitSelectionne.getNom() + "\" ?");

        Optional<ButtonType> reponse = alerteConfirmation.showAndWait();

        if (reponse.isPresent() && reponse.get() == ButtonType.OK) {
            produitService.deleteProduit(produitSelectionne.getId());
            chargerDonnees();
        }
    }
    @FXML
    private void ajouterProduits() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/gestionstock/addProduitDialog.fxml")
            );
            Parent racine = loader.load();

            AddProduitDialogController controleurAjout = loader.getController();
            controleurAjout.setOnProduitAjoute(this::chargerDonnees);

            Stage fenetreAjout = new Stage();
            fenetreAjout.setTitle("Ajouter un produit");
            fenetreAjout.initModality(Modality.APPLICATION_MODAL);
            fenetreAjout.setScene(new Scene(racine));
            fenetreAjout.showAndWait();

        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible d'ouvrir la fenêtre d'ajout");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
}
