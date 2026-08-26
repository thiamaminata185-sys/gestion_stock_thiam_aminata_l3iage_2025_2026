package com.gestionstock.controller;

import com.gestionstock.model.Fournisseur;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Contrôleur du contenu du dialog d'ajout/modification de fournisseur (AddFournisseurDialog.fxml).
 */
public class AddFournisseurDialogController {
    @FXML
    private TextField champNom;
    @FXML
    private TextField champEmail;
    @FXML
    private TextField champTel;
    @FXML
    private Label labelErreur;

    private Fournisseur fournisseurEnCoursDEdition;
    private Runnable surChangement;


    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern TEL_PATTERN = Pattern.compile("^(77|78|75|76|70)\\d{7}$");

    @FXML
    public void initialize() {
        champNom.textProperty().addListener((obs, a, n) -> notifierChangement());
        champEmail.textProperty().addListener((obs, a, n) -> notifierChangement());
        champTel.textProperty().addListener((obs, a, n) -> notifierChangement());
    }

    public void setOnChangement(Runnable callback) {
        this.surChangement = callback;
    }

    private void notifierChangement() {
        if (surChangement != null) surChangement.run();
    }

    public void preremplirPour(Fournisseur fournisseur) {
        this.fournisseurEnCoursDEdition = fournisseur;
        champNom.setText(fournisseur.getNom());
        champEmail.setText(fournisseur.getEmail());
        champTel.setText(fournisseur.getTel());
    }

    public Optional<Fournisseur> construireFournisseurSiValide() {
        String nom = champNom.getText() == null ? "" : champNom.getText().trim();
        if (nom.length() < 2) {
            return erreur("Le nom doit contenir au moins 2 caractères.");
        }

        String email = champEmail.getText();
        if (email != null && !email.isBlank() && !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            return erreur("Format d'email invalide.");
        }

        String tel = champTel.getText();
        if (tel != null && !tel.isBlank() && !TEL_PATTERN.matcher(tel.trim()).matches()) {
            return erreur("Téléphone invalide (9 chiffres, débutant par 77, 78, 75, 76 ou 70).");
        }

        labelErreur.setText("");
        Fournisseur fournisseur = (fournisseurEnCoursDEdition != null) ? fournisseurEnCoursDEdition : new Fournisseur();
        fournisseur.setNom(nom);
        fournisseur.setEmail(email == null || email.isBlank() ? null : email.trim());
        fournisseur.setTel(tel == null || tel.isBlank() ? null : tel.trim());
        return Optional.of(fournisseur);
    }

    public boolean estValide() {
        return construireFournisseurSiValide().isPresent();
    }

    private Optional<Fournisseur> erreur(String message) {
        labelErreur.setText(message);
        return Optional.empty();
    }
}
