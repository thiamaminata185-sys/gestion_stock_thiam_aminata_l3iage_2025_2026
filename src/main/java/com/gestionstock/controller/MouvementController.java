package com.gestionstock.controller;

import com.gestionstock.model.Mouvement;
import com.gestionstock.model.enums.TypeMouvement;
import com.gestionstock.service.MouvementService;
import com.gestionstock.service.MouvementServiceImpl;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class MouvementController {
    @FXML
    private TableView<Mouvement> tableMouvements;
    @FXML
    private TableColumn<Mouvement, String> colonneDate;
    @FXML
    private TableColumn<Mouvement, String> colonneProduit;
    @FXML
    private TableColumn<Mouvement, TypeMouvement> colonneType;
    @FXML
    private TableColumn<Mouvement, Integer> colonneQuantite;
    @FXML
    private TableColumn<Mouvement, String> colonneMotif;
    @FXML
    private TableColumn<Mouvement, String> colonneUtilisateur;
    @FXML
    private ComboBox<TypeMouvement> filtreType;
    @FXML
    private DatePicker dateDebut;
    @FXML
    private DatePicker dateFin;

    private final MouvementService mouvementService = new MouvementServiceImpl();
    private static final DateTimeFormatter FORMAT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        configurerColonnes();
        configurerFiltreType();
        chargerDonnees();
        tableMouvements.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void configurerColonnes() {
        colonneDate.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getDateMouvement().format(FORMAT_DATE)));
        colonneProduit.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getProduit() != null ? data.getValue().getProduit().getNom() : ""));
        colonneType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colonneQuantite.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        colonneMotif.setCellValueFactory(new PropertyValueFactory<>("motif"));
        colonneUtilisateur.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getUtilisateur() != null ? data.getValue().getUtilisateur().getNom() : ""));
    }

    private void configurerFiltreType() {
        filtreType.getItems().add(null); // "Toutes"
        filtreType.getItems().addAll(TypeMouvement.values());
        filtreType.setConverter(new StringConverter<>() {
            @Override public String toString(TypeMouvement t) {
                if (t == null) return "Toutes";
                return t == TypeMouvement.ENTRE ? "Entrées" : "Sorties";
            }
            @Override public TypeMouvement fromString(String s) { return null; }
        });
        filtreType.setValue(null);
    }

    private void chargerDonnees() {
        List<Mouvement> mouvements = mouvementService.findAll();
        tableMouvements.setItems(FXCollections.observableArrayList(mouvements));
    }

    @FXML
    private void appliquerFiltres() {
        TypeMouvement type = filtreType.getValue();
        LocalDate debut = dateDebut.getValue();
        LocalDate fin = dateFin.getValue();
        List<Mouvement> resultats = mouvementService.rechercher(type, debut, fin);
        tableMouvements.setItems(FXCollections.observableArrayList(resultats));
    }

    @FXML
    private void reinitialiserFiltres() {
        filtreType.setValue(null);
        dateDebut.setValue(null);
        dateFin.setValue(null);
        chargerDonnees();
    }

    @FXML
    private void ouvrirAjout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/gestionstock/AddMouvementDialog.fxml"));
            Parent contenu = loader.load();
            AddMouvementDialogController controleurFormulaire = loader.getController();

            Dialog<Mouvement> dialog = new Dialog<>();
            dialog.setTitle("Nouveau mouvement de stock");
            dialog.getDialogPane().setContent(contenu);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            Button boutonOk = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            boutonOk.setText("Enregistrer");
            boutonOk.setDisable(true);
            controleurFormulaire.setOnChangement(() -> boutonOk.setDisable(!controleurFormulaire.estValide()));

            dialog.setResultConverter(bouton -> {
                if (bouton == ButtonType.OK) {
                    return controleurFormulaire.construireMouvementSiValide().orElse(null);
                }
                return null;
            });

            Optional<Mouvement> resultat = dialog.showAndWait();
            resultat.ifPresent(mouvement -> {
                try {
                    mouvementService.enregistrerMouvement(mouvement);
                    chargerDonnees();
                } catch (IllegalArgumentException e) {
                    // Cas rare : le stock a pu changer entre l'ouverture du dialog et la validation
                    // (double vérification "défense en profondeur", comme pour les suppressions).
                    afficherErreur("Mouvement refusé", e.getMessage());
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            afficherErreur("Erreur", "Impossible d'ouvrir le formulaire de mouvement.");
        }
    }

    private void afficherErreur(String titre, String message) {
        Alert alerte = new Alert(Alert.AlertType.ERROR);
        alerte.setTitle(titre);
        alerte.setHeaderText(null);
        alerte.setContentText(message);
        alerte.showAndWait();
    }
}
