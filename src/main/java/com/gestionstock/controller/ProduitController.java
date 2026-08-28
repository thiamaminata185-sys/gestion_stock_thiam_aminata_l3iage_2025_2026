package com.gestionstock.controller;

import com.gestionstock.model.Categorie;
import com.gestionstock.model.Fournisseur;
import com.gestionstock.service.CategorieService;
import com.gestionstock.service.CategorieServiceImpl;
import com.gestionstock.service.FournisseurService;
import com.gestionstock.service.FournisseurServiceImpl;
import com.gestionstock.service.ProduitService;
import com.gestionstock.service.ProduitServiceImpl;
import com.gestionstock.model.Produit;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableCell;
import javafx.scene.control.*;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;

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
    TableColumn<Produit, String> colonnePrixPromo;
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
    @FXML
    Button boutonSupprimer;
    @FXML
    ComboBox<Categorie> filtreCategorie;
    @FXML
    ComboBox<Fournisseur> filtreFournisseur;
    @FXML
    CheckBox filtreStockBas;
    @FXML
    TableColumn<Produit, Void> colonneActions;

    private final ProduitService produitService = new ProduitServiceImpl();
    private final CategorieService categorieService = new CategorieServiceImpl();
    private final FournisseurService fournisseurService = new FournisseurServiceImpl();

    // Liste complète chargée depuis la base, utilisée comme référence pour la recherche/les filtres
    private ObservableList<Produit> listeProduits;

    @FXML
    public void initialize() {
        configurerColones();
        configurerFiltres();
        chargerDonnees();
        boutonSupprimer.setDisable(!com.gestionstock.util.SessionUtilisateur.estAdmin());
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
        colonnePrixPromo.setCellValueFactory(new PropertyValueFactory<>("prixPromo"));
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
        configurerColonneActions();
    }

    /**
     * Construit une colonne "Actions" avec un bouton Modifier
     * DANS CHAQUE LIGNE du tableau (au lieu d'un bouton unique en haut qui agissait
     * sur la ligne sélectionnée). Chaque cellule connaît son propre Produit via getIndex().
     */
    private void configurerColonneActions() {
        colonneActions.setCellFactory(colonne -> new TableCell<>() {
            private final Button boutonModifier = new Button("Modifier");
            private final HBox conteneur = new HBox(6, boutonModifier);

            {
                boutonModifier.setOnAction(e -> ouvrirFormulaire(getProduitDeLaLigne()));
            }

            private Produit getProduitDeLaLigne() {
                return getTableView().getItems().get(getIndex());
            }

            @Override
            protected void updateItem(Void item, boolean vide) {
                super.updateItem(item, vide);
                setGraphic(vide ? null : conteneur);
            }
        });
    }

    private void configurerFiltres() {
        filtreCategorie.getItems().add(null); // "Toutes catégories"
        filtreCategorie.getItems().addAll(categorieService.findAllCategories());
        filtreCategorie.setConverter(new StringConverter<>() {
            @Override public String toString(Categorie c) { return c == null ? "Toutes catégories" : c.getNom(); }
            @Override public Categorie fromString(String s) { return null; }
        });

        filtreFournisseur.getItems().add(null); // "Tous fournisseurs"
        filtreFournisseur.getItems().addAll(fournisseurService.findAllFournisseurs());
        filtreFournisseur.setConverter(new StringConverter<>() {
            @Override public String toString(Fournisseur f) { return f == null ? "Tous fournisseurs" : f.getNom(); }
            @Override public Fournisseur fromString(String s) { return null; }
        });

        filtreCategorie.valueProperty().addListener((obs, a, n) -> appliquerFiltres());
        filtreFournisseur.valueProperty().addListener((obs, a, n) -> appliquerFiltres());
        filtreStockBas.selectedProperty().addListener((obs, a, n) -> appliquerFiltres());
    }

    private void chargerDonnees() {
        // Charger des données depuis la base via JDBC API
        List<Produit> produits = produitService.findAllProduits();

        listeProduits = FXCollections.observableArrayList(produits);

        appliquerFiltres();
    }

    @FXML
    private void rechercherProduits() {
        appliquerFiltres();
    }

    /**
     * Combine recherche texte + les 3 filtres (catégorie, fournisseur, stock bas uniquement)
     * en une seule passe sur la liste de référence chargée depuis la base.
     */
    private void appliquerFiltres() {
        String recherche = champRecherche.getText();
        String rechercheMinuscule = (recherche == null) ? "" : recherche.trim().toLowerCase();
        Categorie categorieChoisie = filtreCategorie.getValue();
        Fournisseur fournisseurChoisi = filtreFournisseur.getValue();
        boolean stockBasUniquement = filtreStockBas.isSelected();

        ObservableList<Produit> resultats = listeProduits.filtered(produit ->
                (rechercheMinuscule.isEmpty() || (produit.getNom() != null && produit.getNom().toLowerCase().contains(rechercheMinuscule)))
                        && (categorieChoisie == null || (produit.getCategorie() != null && produit.getCategorie().getId() == categorieChoisie.getId()))
                        && (fournisseurChoisi == null || (produit.getFournisseur() != null && produit.getFournisseur().getId() == fournisseurChoisi.getId()))
                        && (!stockBasUniquement || produit.getQuantiteStock() <= produit.getQuantiteMin())
        );

        tableProduits.setItems(resultats);
    }

    @FXML
    private void ouvrirAjout() {
        ouvrirFormulaire(null);
    }

    /**
     * Ouvre AddProduitDialog.fxml dans un Dialog<Produit>. produitExistant == null -> mode ajout,
     * sinon -> mode modification (le formulaire est pré-rempli).
     */
    private void ouvrirFormulaire(Produit produitExistant) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/gestionstock/AddProduitDialog.fxml"));
            Parent contenu = loader.load();
            AddProduitDialogController controleurFormulaire = loader.getController();

            if (produitExistant != null) {
                controleurFormulaire.preremplirPour(produitExistant);
            }

            Dialog<Produit> dialog = new Dialog<>();
            dialog.setTitle(produitExistant == null ? "Nouveau produit" : "Modifier le produit");
            dialog.getDialogPane().setContent(contenu);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            Button boutonOk = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            boutonOk.setText("Enregistrer");
            boutonOk.setDisable(true); // désactivé tant que le formulaire n'est pas valide
            controleurFormulaire.setOnChangement(() -> boutonOk.setDisable(!controleurFormulaire.estValide()));

            dialog.setResultConverter(bouton -> {
                if (bouton == ButtonType.OK) {
                    return controleurFormulaire.construireProduitSiValide().orElse(null);
                }
                return null;
            });

            Optional<Produit> resultat = dialog.showAndWait();
            resultat.ifPresent(produit -> {
                if (produitExistant == null) {
                    produitService.addProduit(produit);
                } else {
                    produitService.updateProduit(produit);
                }
                chargerDonnees();
            });
        } catch (IOException e) {
            e.printStackTrace();
            afficherErreur("Erreur", "Impossible d'ouvrir le formulaire produit.");
        }
    }

    @FXML
    private void supprimerProduit() {
        Produit produitSelectionne = tableProduits.getSelectionModel().getSelectedItem();

        if (produitSelectionne == null) {
            afficherInfo("Aucune sélection", "Veuillez sélectionner un produit à supprimer.");
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
