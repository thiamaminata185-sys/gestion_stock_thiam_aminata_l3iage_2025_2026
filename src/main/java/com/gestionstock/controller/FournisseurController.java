package com.gestionstock.controller;

import com.gestionstock.model.Fournisseur;
import com.gestionstock.service.FournisseurService;
import com.gestionstock.service.FournisseurServiceImpl;
import javafx.beans.property.SimpleLongProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

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

    private final FournisseurService fournisseurService = new FournisseurServiceImpl();
    private ObservableList<Fournisseur> listeFournisseurs;

    // Email : format standard simple. Téléphone : 9 chiffres commençant par 77, 78, 75, 76 ou 70 (règle du sujet).
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern TEL_PATTERN = Pattern.compile("^(77|78|75|76|70)\\d{7}$");

    @FXML
    public void initialize() {
        configurerColonnes();
        chargerDonnees();
    }

    private void configurerColonnes() {
        colonneNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colonneEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colonneTel.setCellValueFactory(new PropertyValueFactory<>("tel"));
        colonneNbProduits.setCellValueFactory(data ->
                new SimpleLongProperty(fournisseurService.countProduits(data.getValue().getId())).asObject());
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

    @FXML
    private void ouvrirModification() {
        Fournisseur selection = tableFournisseurs.getSelectionModel().getSelectedItem();
        if (selection == null) {
            afficherInfo("Aucune sélection", "Veuillez sélectionner un fournisseur à modifier.");
            return;
        }
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

    private Optional<Fournisseur> afficherFormulaire(Fournisseur fournisseur, String titre) {
        Dialog<Fournisseur> dialog = new Dialog<>();
        dialog.setTitle(titre);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField champNom = new TextField(fournisseur.getNom());
        champNom.setPromptText("Nom du fournisseur");
        TextField champEmail = new TextField(fournisseur.getEmail());
        champEmail.setPromptText("email@exemple.com (optionnel)");
        TextField champTel = new TextField(fournisseur.getTel());
        champTel.setPromptText("77XXXXXXX");

        Label labelErreur = new Label();
        labelErreur.setStyle("-fx-text-fill: red;");
        labelErreur.setWrapText(true);
        labelErreur.setMaxWidth(280);

        GridPane grille = new GridPane();
        grille.setHgap(10);
        grille.setVgap(10);
        grille.addRow(0, new Label("Nom *"), champNom);
        grille.addRow(1, new Label("Email"), champEmail);
        grille.addRow(2, new Label("Téléphone"), champTel);
        grille.add(labelErreur, 0, 3, 2, 1);
        dialog.getDialogPane().setContent(grille);

        Button boutonOk = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);

        Runnable validerFormulaire = () -> {
            String erreur = validerChamps(champNom.getText(), champEmail.getText(), champTel.getText());
            labelErreur.setText(erreur == null ? "" : erreur);
            boutonOk.setDisable(erreur != null);
        };
        champNom.textProperty().addListener((obs, a, n) -> validerFormulaire.run());
        champEmail.textProperty().addListener((obs, a, n) -> validerFormulaire.run());
        champTel.textProperty().addListener((obs, a, n) -> validerFormulaire.run());
        validerFormulaire.run();

        dialog.setResultConverter(boutonClique -> {
            if (boutonClique == ButtonType.OK) {
                fournisseur.setNom(champNom.getText().trim());
                fournisseur.setEmail(champEmail.getText() == null || champEmail.getText().isBlank() ? null : champEmail.getText().trim());
                fournisseur.setTel(champTel.getText() == null || champTel.getText().isBlank() ? null : champTel.getText().trim());
                return fournisseur;
            }
            return null;
        });

        return dialog.showAndWait();
    }

    /**
     * Retourne un message d'erreur si un champ est invalide, ou null si tout est correct.
     * Règles du sujet : nom >= 2 caractères ; email au format valide s'il est renseigné (optionnel) ;
     * téléphone au format valide s'il est renseigné (9 chiffres, débutant par 77/78/75/76/70).
     */
    private String validerChamps(String nom, String email, String tel) {
        if (nom == null || nom.trim().length() < 2) {
            return "Le nom doit contenir au moins 2 caractères.";
        }
        if (email != null && !email.isBlank() && !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            return "Format d'email invalide.";
        }
        if (tel != null && !tel.isBlank() && !TEL_PATTERN.matcher(tel.trim()).matches()) {
            return "Téléphone invalide (9 chiffres, débutant par 77, 78, 75, 76 ou 70).";
        }
        return null;
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
