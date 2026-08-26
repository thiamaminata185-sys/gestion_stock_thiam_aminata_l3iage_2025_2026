package com.gestionstock.service;

import com.gestionstock.model.Fournisseur;
import com.gestionstock.model.Produit;
import com.gestionstock.util.JPAUtil;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

public class FournisseurServiceImpl implements FournisseurService {

    @Override
    public List<Fournisseur> findAllFournisseurs() {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            return em.createQuery(
                    "SELECT f FROM Fournisseur f ORDER BY f.nom",
                    Fournisseur.class
            ).getResultList();
        }
    }

    @Override
    public Optional<Fournisseur> findById(int id) {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            return Optional.ofNullable(em.find(Fournisseur.class, id));
        }
    }

    @Override
    public void addFournisseur(Fournisseur f) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(f);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException("Erreur lors de la sauvegarde du fournisseur");
        } finally {
            em.close();
        }
    }

    @Override
    public void updateFournisseur(Fournisseur f) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(f);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException("Erreur lors de la modification du fournisseur");
        } finally {
            em.close();
        }
    }

    @Override
    public void deleteFournisseur(int id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            long nbProduits = em.createQuery(
                    "SELECT COUNT(p) FROM Produit p WHERE p.fournisseur.id = :fourId", Long.class
            ).setParameter("fourId", id).getSingleResult();

            if (nbProduits > 0) {
                em.getTransaction().rollback();
                throw new IllegalStateException(
                        "Impossible de supprimer ce fournisseur : " + nbProduits + " produit(s) y sont encore rattachés.");
            }
            Optional<Fournisseur> fournisseurOptional = findById(id);
            if(fournisseurOptional.isPresent()) em.remove(fournisseurOptional.get()); //correction
            em.getTransaction().commit();
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException("Erreur lors de la suppression du fournisseur");
        } finally {
            em.close();
        }
    }

    @Override
    public long countProduits(int fournisseurId) {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            return em.createQuery(
                    "SELECT COUNT(p) FROM Produit p WHERE p.fournisseur.id = :fourId", Long.class
            ).setParameter("fourId", fournisseurId).getSingleResult();
        }
    }
}
