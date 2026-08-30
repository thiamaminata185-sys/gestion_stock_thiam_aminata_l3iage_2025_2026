package com.gestionstock.service;

import com.gestionstock.model.Produit;

import java.util.List;

public interface DashboardService {
    long compterProduits();

    List<Produit> produitsEnStockBas();

    /** Somme de (quantiteStock × prix) sur tous les produits. */
    double valeurTotaleStock();

    /** Nombre de mouvements de type ENTREE enregistrés aujourd'hui (00h00 à 23h59). */
    long compterEntreesDuJour();

    /** Nombre de mouvements de type SORTIE enregistrés aujourd'hui. */
    long compterSortiesDuJour();
}
