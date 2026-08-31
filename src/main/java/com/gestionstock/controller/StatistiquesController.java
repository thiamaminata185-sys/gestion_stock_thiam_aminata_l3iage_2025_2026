package com.gestionstock.controller;

import com.gestionstock.model.Categorie;
import com.gestionstock.model.Fournisseur;
import com.gestionstock.model.Produit;
import com.gestionstock.service.StatistiquesService;
import com.gestionstock.service.StatistiquesServiceImpl;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class StatistiquesController {
    @FXML
    private DatePicker dateDebut;
    @FXML
    private DatePicker dateFin;
    @FXML
    private Label labelValeurStock;
    @FXML
    private Label labelProduitTop;
    @FXML
    private Label labelCategorieTop;
    @FXML
    private Label labelFournisseurTop;
    @FXML
    private Label labelRuptureEvitee;
    @FXML
    private BarChart<String, Number> graphiqueBarres;
    @FXML
    private CategoryAxis axeXMois;
    @FXML
    private PieChart graphiqueCamembert;

    private final StatistiquesService statistiquesService = new StatistiquesServiceImpl();
    private static final NumberFormat FORMAT_MONTANT = NumberFormat.getNumberInstance(Locale.FRANCE);
    private static final DateTimeFormatter FORMAT_MOIS = DateTimeFormatter.ofPattern("MM/yyyy");

    @FXML
    public void initialize() {
        rafraichir(null, null);
    }

    @FXML
    private void appliquerFiltre() {
        rafraichir(dateDebut.getValue(), dateFin.getValue());
    }

    @FXML
    private void reinitialiserFiltre() {
        dateDebut.setValue(null);
        dateFin.setValue(null);
        rafraichir(null, null);
    }

    private void rafraichir(LocalDate debut, LocalDate fin) {
        // La valeur totale du stock, la catégorie top et le fournisseur top ne dépendent pas de la
        // période choisie (ce sont des états actuels de l'inventaire, pas des mouvements) ; le
        // produit le plus mouvementé, le nombre de ruptures évitées et le graphique en barres, si.
        labelValeurStock.setText(FORMAT_MONTANT.format(statistiquesService.valeurTotaleStock()) + " FCFA");

        Optional<Produit> produitTop = statistiquesService.produitLePlusMouvemente(debut, fin);
        labelProduitTop.setText(produitTop.map(Produit::getNom).orElse("Aucun mouvement sur la période"));

        Optional<Categorie> categorieTop = statistiquesService.categoriePlusForteValeurStock();
        labelCategorieTop.setText(categorieTop.map(Categorie::getNom).orElse("—"));

        Optional<Fournisseur> fournisseurTop = statistiquesService.fournisseurAvecPlusDeProduits();
        labelFournisseurTop.setText(fournisseurTop.map(Fournisseur::getNom).orElse("—"));

        long ruptureEvitee = statistiquesService.nombreSortiesVersRuptureEvitee(debut, fin);
        labelRuptureEvitee.setText(String.valueOf(ruptureEvitee));

        remplirGraphiqueBarres(debut, fin);
        remplirCamembert();
    }

    private void remplirGraphiqueBarres(LocalDate debut, LocalDate fin) {
        Map<YearMonth, long[]> parMois = statistiquesService.mouvementsParMois(debut, fin);

        XYChart.Series<String, Number> serieEntrees = new XYChart.Series<>();
        serieEntrees.setName("Entrées");
        XYChart.Series<String, Number> serieSorties = new XYChart.Series<>();
        serieSorties.setName("Sorties");

        for (Map.Entry<YearMonth, long[]> entree : parMois.entrySet()) {
            String moisLabel = entree.getKey().format(FORMAT_MOIS);
            serieEntrees.getData().add(new XYChart.Data<>(moisLabel, entree.getValue()[0]));
            serieSorties.getData().add(new XYChart.Data<>(moisLabel, entree.getValue()[1]));
        }

        graphiqueBarres.getData().setAll(serieEntrees, serieSorties);
    }

    private void remplirCamembert() {
        Map<String, Double> repartition = statistiquesService.repartitionValeurParCategorie();

        graphiqueCamembert.setData(FXCollections.observableArrayList(
                repartition.entrySet().stream()
                        .map(e -> new PieChart.Data(e.getKey() + " (" + FORMAT_MONTANT.format(e.getValue()) + ")", e.getValue()))
                        .toList()
        ));
    }
}
