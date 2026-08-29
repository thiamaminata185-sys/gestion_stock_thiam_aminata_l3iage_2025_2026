package com.gestionstock.service;

import com.gestionstock.model.Mouvement;
import com.gestionstock.model.Produit;
import com.gestionstock.model.enums.TypeMouvement;
import com.gestionstock.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class MouvementServiceImpl implements MouvementService {

    @Override
    public void enregistrerMouvement(Mouvement mouvement) {
        // Validations du sujet (section "Mouvements") : quantité strictement positive,
        // motif obligatoire pour une SORTIE.
        if (mouvement.getQuantite() <= 0) {
            throw new IllegalArgumentException("La quantité doit être un entier strictement positif.");
        }
        if (mouvement.getType() == TypeMouvement.SORTIE
                && (mouvement.getMotif() == null || mouvement.getMotif().isBlank())) {
            throw new IllegalArgumentException("Le motif est obligatoire pour une sortie de stock.");
        }

        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            // On recharge le produit via CET EntityManager (celui de la transaction en cours) pour
            // avoir une entité managée à jour, et non l'entité potentiellement "périmée" venue du
            // formulaire (chargée par un autre EntityManager, déjà fermé).
            Produit produit = em.find(Produit.class, mouvement.getProduit().getId());
            if (produit == null) {
                throw new IllegalArgumentException("Le produit sélectionné n'existe plus.");
            }

            if (mouvement.getType() == TypeMouvement.ENTRE) {
                produit.setQuantiteStock(produit.getQuantiteStock() + mouvement.getQuantite());
            } else { // SORTIE
                if (mouvement.getQuantite() > produit.getQuantiteStock()) {
                    // Règle métier : une sortie ne doit JAMAIS rendre le stock négatif.
                    throw new IllegalArgumentException(
                            "Stock insuffisant : il ne reste que " + produit.getQuantiteStock() + " unité(s) en stock.");
                }
                produit.setQuantiteStock(produit.getQuantiteStock() - mouvement.getQuantite());
            }

            mouvement.setProduit(produit);
            if (mouvement.getDateMouvement() == null) {
                mouvement.setDateMouvement(LocalDateTime.now());
            }

            em.merge(produit);     // met à jour la quantiteStock du produit
            em.persist(mouvement); // insère le mouvement
            // Un seul commit pour les deux opérations : soit tout réussit, soit tout est annulé
            // (rollback) si l'une des deux échoue. C'est exactement ce qu'exige le sujet.
            em.getTransaction().commit();
        } catch (IllegalArgumentException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e; // message métier à afficher tel quel dans l'UI
        } catch (Exception e) {
            e.printStackTrace();
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Erreur lors de l'enregistrement du mouvement de stock",e);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Mouvement> findAll() {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            return em.createQuery(
                    "SELECT m FROM Mouvement m ORDER BY m.dateMouvement DESC", Mouvement.class
            ).getResultList();
        }
    }

    @Override
    public List<Mouvement> rechercher(TypeMouvement type, LocalDate dateDebut, LocalDate dateFin) {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            StringBuilder jpql = new StringBuilder("SELECT m FROM Mouvement m WHERE 1=1");
            if (type != null) jpql.append(" AND m.type = :type");
            if (dateDebut != null) jpql.append(" AND m.dateMouvement >= :debut");
            if (dateFin != null) jpql.append(" AND m.dateMouvement <= :fin");
            jpql.append(" ORDER BY m.dateMouvement DESC");

            TypedQuery<Mouvement> query = em.createQuery(jpql.toString(), Mouvement.class);
            if (type != null) query.setParameter("type", type);
            if (dateDebut != null) query.setParameter("debut", dateDebut.atStartOfDay());
            if (dateFin != null) query.setParameter("fin", LocalDateTime.of(dateFin, LocalTime.MAX));

            return query.getResultList();
        }
    }
}
