package com.gestionstock.service;

import com.gestionstock.model.Produit;
import com.gestionstock.model.enums.TypeMouvement;
import com.gestionstock.util.JPAUtil;
import jakarta.persistence.EntityManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class DashboardServiceImpl implements DashboardService {

    @Override
    public long compterProduits() {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            return em.createQuery("SELECT COUNT(p) FROM Produit p", Long.class).getSingleResult();
        }
    }

    @Override
    public List<Produit> produitsEnStockBas() {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            // Règle  : un produit est en alerte stock bas quand quantiteStock <= quantiteMin
            return em.createQuery(
                    "SELECT p FROM Produit p WHERE p.quantiteStock <= p.quantiteMin ORDER BY p.nom",
                    Produit.class
            ).getResultList();
        }
    }

    @Override
    public double valeurTotaleStock() {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            Double total = em.createQuery(
                    "SELECT SUM(p.quantiteStock * p.prix) FROM Produit p", Double.class
            ).getSingleResult();
            // SUM() sur une table vide renvoie NULL en JPQL/SQL, pas 0 : il faut le gérer explicitement.
            return total != null ? total : 0.0;
        }
    }

    @Override
    public long compterEntreesDuJour() {
        return compterMouvementsDuJour(TypeMouvement.ENTRE);
    }

    @Override
    public long compterSortiesDuJour() {
        return compterMouvementsDuJour(TypeMouvement.SORTIE);
    }

    private long compterMouvementsDuJour(TypeMouvement type) {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            LocalDate aujourdHui = LocalDate.now();
            LocalDateTime debutJour = aujourdHui.atStartOfDay();
            LocalDateTime finJour = LocalDateTime.of(aujourdHui, LocalTime.MAX);

            return em.createQuery(
                    "SELECT COUNT(m) FROM Mouvement m WHERE m.type = :type " +
                            "AND m.dateMouvement BETWEEN :debut AND :fin", Long.class
            ).setParameter("type", type)
                    .setParameter("debut", debutJour)
                    .setParameter("fin", finJour)
                    .getSingleResult();
        }
    }
}
