package com.gestionstock.service;

import com.gestionstock.model.Mouvement;
import com.gestionstock.model.enums.TypeMouvement;

import java.time.LocalDate;
import java.util.List;

public interface MouvementService {
    /**
     * Enregistre un mouvement de stock ET met à jour la quantité en stock du produit,
     * de façon transactionnelle (les deux réussissent ou échouent ensemble).
     * Lève IllegalArgumentException si la règle métier n'est pas respectée (ex: SORTIE
     * qui rendrait le stock négatif, motif manquant pour une SORTIE, quantité <= 0).
     */
    void enregistrerMouvement(Mouvement mouvement);

    List<Mouvement> findAll();

    /**
     * Filtre l'historique par type (null = tous), et par période (bornes incluses, null = pas de borne).
     */
    List<Mouvement> rechercher(TypeMouvement type, LocalDate dateDebut, LocalDate dateFin);
}
