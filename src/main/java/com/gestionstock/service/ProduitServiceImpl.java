package com.gestionstock.service;

import com.gestionstock.model.Produit;
import com.gestionstock.util.DatabaseConfig;
import com.gestionstock.util.SessionUtilisateur;
import com.gestionstock.util.JPAUtil;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProduitServiceImpl implements ProduitService {


    @Override
    public List<Produit> findAllProduits() {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            return em.createQuery(
                    "SELECT p FROM Produit p " +
                            "LEFT JOIN FETCH p.categorie " +
                            "LEFT JOIN FETCH p.fournisseur " +
                            "ORDER BY p.nom",
                    Produit.class
            ).getResultList();
        }
    }

    @Override
    public Optional<Produit> findById(int id) {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            return Optional.ofNullable(em.find(Produit.class, id));
        }
    }

    @Override
    public List<Produit> findByCategorie(int categorieId) {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            return em.createQuery(
                    "SELECT p FROM Produit p " +
                            "WHERE p.categorie.id = :catId " +
                            "ORDER BY p.nom",
                    Produit.class
            ).setParameter("catId", categorieId).getResultList();
        }
    }

    @Override
    public void addProduit(Produit p) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(p);
            em.getTransaction().commit();
        } catch (Exception e){
            em.getTransaction().rollback(); // pour enlever ce qu'on avait insérer sur la base données
            throw new RuntimeException("Erreur lors de la sauvegarde du produit");
        } finally {
            em.close();
        }
    }

    @Override
    public void updateProduit(Produit p) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(p);
            em.getTransaction().commit();
        } catch (Exception e){
            em.getTransaction().rollback(); // pour enlever ce qu'on avait insérer sur la base données
            throw new RuntimeException("Erreur lors de la modification du produit");
        } finally {
            em.close();
        }
    }

    @Override
    public void deleteProduit(int id) {
        // Sécurité côté serveur : ne JAMAIS se fier uniquement à l'UI (bouton désactivé).
        // Même si quelqu'un contournait l'interface, cette vérification bloque la suppression.
        if (!SessionUtilisateur.estAdmin()) {
            throw new SecurityException("Seul un administrateur peut supprimer un produit.");
        }
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            // em.find() re-cherche l'entité via CET EntityManager (celui de la transaction en cours),
            // car findById() ci-dessus utilise un AUTRE EntityManager (déjà fermé) : on ne peut pas
            // remove() une entité "détachée" d'un EntityManager différent.
            Produit produit = em.find(Produit.class, id);
            if (produit != null) {
                em.remove(produit);
            }
            em.getTransaction().commit();
        } catch (Exception e){
            em.getTransaction().rollback();
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la suppression du produit",e);
        } finally {
            em.close();
        }
    }

    public List<Produit> findByStockBas() {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            return em.createQuery(
                    "SELECT p FROM Produit p " +
                            "WHERE p.quantiteStock < p.quantiteMin " +
                            "ORDER BY p.quantiteStock",
                    Produit.class
            ).getResultList();
        }
    }
}
