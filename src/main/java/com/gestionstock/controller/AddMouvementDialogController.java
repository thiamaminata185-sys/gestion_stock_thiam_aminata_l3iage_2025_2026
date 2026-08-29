package com.gestionstock.controller;

import com.gestionstock.model.Mouvement;
import com.gestionstock.model.Produit;
import com.gestionstock.model.enums.TypeMouvement;
import com.gestionstock.service.ProduitService;
import com.gestionstock.service.ProduitServiceImpl;
import com.gestionstock.util.SessionUtilisateur;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

import java.util.Optional;

/**
 * Contrôleur du contenu du dialog d'ajout de mouvement (AddMouvementDialog.fxml).
 * Contrairement au formulaire Produit, il n'y a pas de "mode modification" ici : un mouvement
 * de stock, une fois enregistré, ne se modifie pas (ce serait fausser l'historique de traçabilité).
 */
public class AddMouvementDialogController {
    @FXML
    private ComboBox<Produit> comboProduit;
    @FXML
    private RadioButton radioEntree;
    @FXML
    private RadioButton radioSortie;
    @FXML
    private TextField champQuantite;
    @FXML
    private TextField champMotif;
    @FXML
    private Label labelStockActuel;
    @FXML
    private Label labelStockApres;
    @FXML
    private Label labelErreur;

    private final ProduitService produitService = new ProduitServiceImpl();
    private Runnable surChangement;

    @FXML
    public void initialize() {
        comboProduit.getItems().setAll(produitService.findAllProduits());
        comboProduit.setConverter(new StringConverter<>() {
            @Override public String toString(Produit p) { return p == null ? "" : p.getNom(); }
            @Override public Produit fromString(String s) { return null; }
        });

        comboProduit.valueProperty().addListener((obs, a, n) -> { rafraichirApercu(); notifierChangement(); });
        radioEntree.selectedProperty().addListener((obs, a, n) -> { rafraichirApercu(); notifierChangement(); });
        champQuantite.textProperty().addListener((obs, a, n) -> { rafraichirApercu(); notifierChangement(); });
        champMotif.textProperty().addListener((obs, a, n) -> notifierChangement());
    }

    public void setOnChangement(Runnable callback) {
        this.surChangement = callback;
    }

    private void notifierChangement() {
        if (surChangement != null) surChangement.run();
    }

    /** Met à jour "Stock actuel" et "Stock après" en direct, avant même de valider (exigence du sujet). */
    private void rafraichirApercu() {
        Produit produit = comboProduit.getValue();
        if (produit == null) {
            labelStockActuel.setText("—");
            labelStockApres.setText("—");
            return;
        }

        int stockActuel = produit.getQuantiteStock();
        labelStockActuel.setText(String.valueOf(stockActuel));

        Integer quantite = parseEntierPositif(champQuantite.getText());
        if (quantite == null) {
            labelStockApres.setText("—");
            return;
        }

        int stockApres = radioEntree.isSelected() ? stockActuel + quantite : stockActuel - quantite;
        labelStockApres.setText(String.valueOf(stockApres));
        // Si la sortie dépasserait le stock disponible, on le signale visuellement tout de suite
        labelStockApres.setStyle(stockApres < 0 ? "-fx-font-weight: bold; -fx-text-fill: red;" : "-fx-font-weight: bold;");
    }

    /** Valide le formulaire (règles du sujet) et construit le Mouvement prêt à être enregistré. */
    public Optional<Mouvement> construireMouvementSiValide() {
        Produit produit = comboProduit.getValue();
        if (produit == null) {
            return erreur("Veuillez choisir un produit.");
        }

        Integer quantite = parseEntierPositif(champQuantite.getText());
        if (quantite == null) {
            return erreur("La quantité doit être un entier strictement positif.");
        }

        TypeMouvement type = radioEntree.isSelected() ? TypeMouvement.ENTRE : TypeMouvement.SORTIE;
        String motif = champMotif.getText() == null ? "" : champMotif.getText().trim();

        if (type == TypeMouvement.SORTIE) {
            if (motif.isEmpty()) {
                return erreur("Le motif est obligatoire pour une sortie de stock.");
            }
            if (quantite > produit.getQuantiteStock()) {
                return erreur("Stock insuffisant : il ne reste que " + produit.getQuantiteStock() + " unité(s).");
            }
        }

        labelErreur.setText("");
        Mouvement mouvement = new Mouvement();
        mouvement.setProduit(produit);
        mouvement.setType(type);
        mouvement.setQuantite(quantite);
        mouvement.setMotif(motif.isEmpty() ? null : motif);
        mouvement.setUtilisateur(SessionUtilisateur.getUtilisateurConnecte());
        return Optional.of(mouvement);
    }

    public boolean estValide() {
        return construireMouvementSiValide().isPresent();
    }

    private Optional<Mouvement> erreur(String message) {
        labelErreur.setText(message);
        return Optional.empty();
    }

    private Integer parseEntierPositif(String texte) {
        if (texte == null || texte.isBlank()) return null;
        try {
            int valeur = Integer.parseInt(texte.trim());
            return valeur > 0 ? valeur : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
