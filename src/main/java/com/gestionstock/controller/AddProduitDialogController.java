package com.gestionstock.controller;

import com.gestionstock.model.Categorie;
import com.gestionstock.model.Fournisseur;
import com.gestionstock.model.Produit;
import com.gestionstock.service.CategorieService;
import com.gestionstock.service.CategorieServiceImpl;
import com.gestionstock.service.FournisseurService;
import com.gestionstock.service.FournisseurServiceImpl;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

import java.util.Optional;

/**
 * Contrôleur du contenu du dialog d'ajout/modification de produit (AddProduitDialog.fxml).
 * Ce contrôleur ne gère QUE le formulaire ; c'est ProduitController qui l'englobe dans un
 * javafx.scene.control.Dialog et qui appelle construireProduitSiValide() lors du clic sur "Enregistrer".
 */
public class AddProduitDialogController {
    @FXML
    private TextField champNom;
    @FXML
    private ComboBox<Categorie> comboCategorie;
    @FXML
    private ComboBox<Fournisseur> comboFournisseur;
    @FXML
    private TextField champPrix;
    @FXML
    private TextField champPrixPromo;
    @FXML
    private TextField champQuantiteStock;
    @FXML
    private TextField champQuantiteMin;
    @FXML
    private Label labelErreur;

    private final CategorieService categorieService = new CategorieServiceImpl();
    private final FournisseurService fournisseurService = new FournisseurServiceImpl();

    private Produit produitEnCoursDEdition; // null si on est en mode "ajout"
    private Runnable surChangement; // callback pour rafraîchir l'état du bouton OK du Dialog parent

    @FXML
    public void initialize() {
        chargerListesDeroulantes();

        // Validation "en temps réel" : à chaque frappe, on revalide et on prévient le Dialog parent
        for (TextField champ : new TextField[]{champNom, champPrix, champPrixPromo, champQuantiteStock, champQuantiteMin}) {
            champ.textProperty().addListener((obs, ancien, nouveau) -> notifierChangement());
        }
        comboCategorie.valueProperty().addListener((obs, a, n) -> notifierChangement());
        comboFournisseur.valueProperty().addListener((obs, a, n) -> notifierChangement());
    }

    private void chargerListesDeroulantes() {
        comboCategorie.getItems().setAll(categorieService.findAllCategories());
        comboFournisseur.getItems().setAll(fournisseurService.findAllFournisseurs());

        comboCategorie.setConverter(new StringConverter<>() {
            @Override public String toString(Categorie c) { return c == null ? "" : c.getNom(); }
            @Override public Categorie fromString(String s) { return null; }
        });
        comboFournisseur.setConverter(new StringConverter<>() {
            @Override public String toString(Fournisseur f) { return f == null ? "" : f.getNom(); }
            @Override public Fournisseur fromString(String s) { return null; }
        });
    }

    /** Permet à ProduitController de savoir quand revalider l'état du bouton "Enregistrer". */
    public void setOnChangement(Runnable callback) {
        this.surChangement = callback;
    }

    private void notifierChangement() {
        if (surChangement != null) surChangement.run();
    }

    /** Pré-remplit le formulaire pour un produit existant (mode modification). */
    public void preremplirPour(Produit produit) {
        this.produitEnCoursDEdition = produit;
        champNom.setText(produit.getNom());
        champPrix.setText(String.valueOf(produit.getPrix()));
        champPrixPromo.setText(String.valueOf(produit.getPrixPromo()));
        champQuantiteStock.setText(String.valueOf(produit.getQuantiteStock()));
        champQuantiteMin.setText(String.valueOf(produit.getQuantiteMin()));
        comboCategorie.setValue(produit.getCategorie());
        comboFournisseur.setValue(produit.getFournisseur());
    }

    /**
     * Valide tous les champs selon les règles du sujet. Si tout est correct, retourne le Produit
     * prêt à être sauvegardé. Sinon, affiche le message d'erreur dans labelErreur et retourne empty.
     */
    public Optional<Produit> construireProduitSiValide() {
        String nom = champNom.getText() == null ? "" : champNom.getText().trim();
        if (nom.length() < 2) {
            return erreur("Le nom doit contenir au moins 2 caractères.");
        }
        if (comboCategorie.getValue() == null) {
            return erreur("Veuillez choisir une catégorie.");
        }
        if (comboFournisseur.getValue() == null) {
            return erreur("Veuillez choisir un fournisseur.");
        }

        Double prix = parseDoublePositifStrict(champPrix.getText());
        if (prix == null) {
            return erreur("Le prix doit être un nombre strictement positif.");
        }

        Double prixPromo = null;
        String texteProm = champPrixPromo.getText();
        if (texteProm != null && !texteProm.isBlank()) {
            prixPromo = parseDoublePositifStrict(texteProm);
            if (prixPromo == null) {
                return erreur("Le prix promo doit être un nombre strictement positif.");
            }
            if (prixPromo >= prix) {
                return erreur("Le prix promo doit être strictement inférieur au prix normal.");
            }
        }

        Integer quantiteStock = parseEntierPositifOuNul(champQuantiteStock.getText());
        if (quantiteStock == null) {
            return erreur("La quantité en stock doit être un entier ≥ 0.");
        }
        Integer quantiteMin = parseEntierPositifOuNul(champQuantiteMin.getText());
        if (quantiteMin == null) {
            return erreur("La quantité minimum doit être un entier ≥ 0.");
        }

        labelErreur.setText("");
        Produit produit = (produitEnCoursDEdition != null) ? produitEnCoursDEdition : new Produit();
        produit.setNom(nom);
        produit.setCategorie(comboCategorie.getValue());
        produit.setFournisseur(comboFournisseur.getValue());
        produit.setPrix(prix);
        produit.setPrixPromo(prixPromo);
        produit.setQuantiteStock(quantiteStock);
        produit.setQuantiteMin(quantiteMin);
        return Optional.of(produit);
    }

    /** true si le formulaire est actuellement valide. Utilisé pour activer/désactiver le bouton "Enregistrer". */
    public boolean estValide() {
        return construireProduitSiValide().isPresent();
    }

    private Optional<Produit> erreur(String message) {
        labelErreur.setText(message);
        return Optional.empty();
    }

    private Double parseDoublePositifStrict(String texte) {
        if (texte == null || texte.isBlank()) return null;
        try {
            double valeur = Double.parseDouble(texte.trim().replace(",", "."));
            return valeur > 0 ? valeur : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseEntierPositifOuNul(String texte) {
        if (texte == null || texte.isBlank()) return null;
        try {
            int valeur = Integer.parseInt(texte.trim());
            return valeur >= 0 ? valeur : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
