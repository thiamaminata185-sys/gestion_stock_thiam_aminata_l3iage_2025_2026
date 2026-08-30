package com.gestionstock.controller;

import com.gestionstock.model.Produit;
import com.gestionstock.service.DashboardService;
import com.gestionstock.service.DashboardServiceImpl;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class DashboardController {
    @FXML
    private Label labelTotalProduits;
    @FXML
    private Label labelStockBas;
    @FXML
    private Label labelValeurStock;
    @FXML
    private Label labelMouvementsDuJour;
    @FXML
    private ListView<String> listeStockBas;

    private final DashboardService dashboardService = new DashboardServiceImpl();
    // Format monétaire lisible (espace comme séparateur de milliers, pas de décimales) : "550 000 FCFA"
    private static final NumberFormat FORMAT_MONTANT = NumberFormat.getNumberInstance(Locale.FRANCE);

    @FXML
    public void initialize() {
        rafraichir();
    }

    /**
     * Recharge toutes les statistiques depuis la base. Comme MainController recrée cet écran
     * (via FXMLLoader) à chaque fois qu'on clique sur "Dashboard" dans le menu, initialize() est
     * rappelée et les chiffres sont donc TOUJOURS recalculés à l'affichage (jamais de valeur figée).
     */
    private void rafraichir() {
        labelTotalProduits.setText(String.valueOf(dashboardService.compterProduits()));

        List<Produit> produitsStockBas = dashboardService.produitsEnStockBas();
        labelStockBas.setText(String.valueOf(produitsStockBas.size()));

        labelValeurStock.setText(FORMAT_MONTANT.format(dashboardService.valeurTotaleStock()) + " FCFA");

        long entrees = dashboardService.compterEntreesDuJour();
        long sorties = dashboardService.compterSortiesDuJour();
        labelMouvementsDuJour.setText(entrees + " entrée(s) / " + sorties + " sortie(s)");

        List<String> lignesAlerte = produitsStockBas.stream()
                .map(p -> p.getNom() + " — stock: " + p.getQuantiteStock() + " (min: " + p.getQuantiteMin() + ")")
                .toList();
        listeStockBas.setItems(FXCollections.observableArrayList(lignesAlerte));
    }
}
