package com.gestionstock.service;

import com.gestionstock.model.Categorie;
import com.gestionstock.model.Fournisseur;
import com.gestionstock.model.Produit;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface StatistiquesService {
    double valeurTotaleStock();

    /** Le produit ayant cumulé le plus de quantité (entrées + sorties confondues) sur la période. */
    Optional<Produit> produitLePlusMouvemente(LocalDate debut, LocalDate fin);

    /** La catégorie dont les produits représentent, ensemble, la plus forte valeur de stock. */
    Optional<Categorie> categoriePlusForteValeurStock();

    /** Le fournisseur ayant le plus de produits référencés dans le catalogue. */
    Optional<Fournisseur> fournisseurAvecPlusDeProduits();

    /**
     * Nombre de sorties de stock, sur la période, concernant un produit actuellement en alerte
     * stock bas (quantiteStock <= quantiteMin) : une approximation de "rupture évitée de justesse",
     * faute de conserver un historique du niveau de stock à chaque instant.
     */
    long nombreSortiesVersRuptureEvitee(LocalDate debut, LocalDate fin);

    /** Quantités Entrée/Sortie cumulées par mois, pour le graphique en barres. */
    Map<YearMonth, long[]> mouvementsParMois(LocalDate debut, LocalDate fin);

    /** Valeur de stock (quantiteStock × prix) cumulée par catégorie, pour le camembert. */
    Map<String, Double> repartitionValeurParCategorie();
}
