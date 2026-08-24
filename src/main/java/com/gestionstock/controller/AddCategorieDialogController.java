package com.gestionstock.controller;

import com.gestionstock.model.Categorie;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.Optional;

/**
 * Contrôleur du contenu du dialog d'ajout/modification de catégorie (AddCategorieDialog.fxml).
 * Même principe que AddProduitDialogController : ce contrôleur gère uniquement le formulaire,
 * c'est CategorieController qui l'englobe dans un Dialog<Categorie>.
 */
public class AddCategorieDialogController {
    @FXML
    private TextField champNom;
    @FXML
    private TextArea champDescription;
    @FXML
    private Label labelErreur;

    private Categorie categorieEnCoursDEdition; // null si on est en mode "ajout"
    private Runnable surChangement;

    @FXML
    public void initialize() {
        champNom.textProperty().addListener((obs, a, n) -> notifierChangement());
        champDescription.textProperty().addListener((obs, a, n) -> notifierChangement());
    }

    public void setOnChangement(Runnable callback) {
        this.surChangement = callback;
    }

    private void notifierChangement() {
        if (surChangement != null) surChangement.run();
    }

    /** Pré-remplit le formulaire pour une catégorie existante (mode modification). */
    public void preremplirPour(Categorie categorie) {
        this.categorieEnCoursDEdition = categorie;
        champNom.setText(categorie.getNom());
        champDescription.setText(categorie.getDescription());
    }

    /** Valide le formulaire (règle du sujet : nom obligatoire, min 2 caractères) et construit la Categorie. */
    public Optional<Categorie> construireCategorieSiValide() {
        String nom = champNom.getText() == null ? "" : champNom.getText().trim();
        if (nom.length() < 2) {
            labelErreur.setText("Le nom doit contenir au moins 2 caractères.");
            return Optional.empty();
        }

        labelErreur.setText("");
        Categorie categorie = (categorieEnCoursDEdition != null) ? categorieEnCoursDEdition : new Categorie();
        categorie.setNom(nom);
        categorie.setDescription(champDescription.getText());
        return Optional.of(categorie);
    }

    /** true si le formulaire est actuellement valide. Utilisé pour activer/désactiver le bouton "Enregistrer". */
    public boolean estValide() {
        return construireCategorieSiValide().isPresent();
    }
}
