package com.gestionstock.controller;

import com.gestionstock.service.ProduitService;
import com.gestionstock.service.ProduitServiceImpl;
import com.gestionstock.model.Produit;
import com.gestionstock.util.DatabaseConfig;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/*
    Contrôleur dédié à addProduitDialog.fxml.
    IMPORTANT : ce fichier FXML avait par erreur fx:controller="ProduitController",
    qui ne possédait ni les champs (nameNom, namePrix, ...) ni les actions des boutons.
    C'est ce contrôleur (AddProduitController) qu'il faut désormais référencer dans le FXML.
 */
public class AddProduitDialogController {

    @FXML
    private TextField nameNom;
    @FXML
    private TextField namePrix;
    @FXML
    private TextField namePrixpromo;
    @FXML
    private TextField nameQus;
    @FXML
    private TextField nameQumin;
    @FXML
    private ComboBox<Categorie> cmbCategorie;
    @FXML
    private ComboBox<Fournisseur> cmbFournisseur;
    @FXML
    private Button btnEnregistrer;
    @FXML
    private Button btnAnnuler;

    private final ProduitService produitService = new ProduitServiceImpl();

    // Callback exécuté après un ajout réussi, pour rafraîchir la liste dans ProduitController
    private Runnable onProduitAjoute;

    public void setOnProduitAjoute(Runnable onProduitAjoute) {
        this.onProduitAjoute = onProduitAjoute;
    }

    @FXML
    public void initialize() {
        chargerCategories();
        chargerFournisseurs();
    }

    private void chargerCategories() {
        ObservableList<Categorie> liste = FXCollections.observableArrayList();
        String sql = "SELECT id, nom FROM categories ORDER BY nom";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                liste.add(new Categorie(rs.getInt("id"), rs.getString("nom")));
            }
        } catch (SQLException e) {
            afficherErreur("Erreur de chargement des catégories : " + e.getMessage());
        }
        cmbCategorie.setItems(liste);
    }

    private void chargerFournisseurs() {
        ObservableList<Fournisseur> liste = FXCollections.observableArrayList();
        String sql = "SELECT id, nom FROM fournisseurs ORDER BY nom";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                liste.add(new Fournisseur(rs.getInt("id"), rs.getString("nom")));
            }
        } catch (SQLException e) {
            afficherErreur("Erreur de chargement des fournisseurs : " + e.getMessage());
        }
        cmbFournisseur.setItems(liste);
    }

    @FXML
    private void handleEnregistrer() {
        String erreur = validerChamps();
        if (erreur != null) {
            afficherErreur(erreur);
            return;
        }

        try {
            Produit p = new Produit();
            p.setNom(nameNom.getText().trim());
            p.setPrix(Double.parseDouble(namePrix.getText().trim().replace(",", ".")));
            p.setQuantiteStock(Integer.parseInt(nameQus.getText().trim()));
            p.setQuantiteMin(Integer.parseInt(nameQumin.getText().trim()));
            //p.setCategorieId(cmbCategorie.getValue().id());
            //p.setFournisseurId(cmbFournisseur.getValue().id());

            //produitDao.addProduit(p);

            if (onProduitAjoute != null) {
                onProduitAjoute.run();
            }

            fermerFenetre();

        } catch (NumberFormatException e) {
            afficherErreur("Prix, quantité en stock et quantité minimum doivent être des nombres valides.");
        } catch (Exception e) {
            afficherErreur("Erreur lors de l'enregistrement : " + e.getMessage());
        }
    }

    @FXML
    private void handleAnnuler() {
        fermerFenetre();
    }

    private String validerChamps() {
        if (nameNom.getText() == null || nameNom.getText().trim().isEmpty()) {
            return "Le nom du produit est obligatoire.";
        }
        if (namePrix.getText() == null || namePrix.getText().trim().isEmpty()) {
            return "Le prix est obligatoire.";
        }
        if (nameQus.getText() == null || nameQus.getText().trim().isEmpty()) {
            return "La quantité en stock est obligatoire.";
        }
        if (nameQumin.getText() == null || nameQumin.getText().trim().isEmpty()) {
            return "La quantité minimum est obligatoire.";
        }
        if (cmbCategorie.getValue() == null) {
            return "Veuillez sélectionner une catégorie.";
        }
        if (cmbFournisseur.getValue() == null) {
            return "Veuillez sélectionner un fournisseur.";
        }
        return null;
    }

    private void afficherErreur(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void fermerFenetre() {
        Stage stage = (Stage) btnAnnuler.getScene().getWindow();
        stage.close();
    }

    // Petits porte-valeurs (id + nom) affichés dans les ComboBox
    private record Categorie(int id, String nom) {
        @Override
        public String toString() {
            return nom;
        }
    }

    private record Fournisseur(int id, String nom) {
        @Override
        public String toString() {
            return nom;
        }
    }
}
