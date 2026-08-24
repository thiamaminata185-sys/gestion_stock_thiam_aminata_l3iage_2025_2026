package com.gestionstock.service;

import com.gestionstock.model.Categorie;
import com.gestionstock.model.Produit;
import com.gestionstock.util.JPAUtil;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

public class CategorieServiceImpl implements CategorieService {

    @Override
    public List<Categorie> findAllCategories() {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            return em.createQuery(
                    "SELECT c FROM Categorie c ORDER BY c.nom",
                    Categorie.class
            ).getResultList();
        }
    }

    @Override
    public Optional<Categorie> findById(int id) {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            return Optional.ofNullable(em.find(Categorie.class, id));
        }
    }

    @Override
    public void addCategorie(Categorie c) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(c);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException("Erreur lors de la sauvegarde de la catégorie");
        } finally {
            em.close();
        }
    }

    @Override
    public void updateCategorie(Categorie c) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(c);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException("Erreur lors de la modification de la catégorie");
        } finally {
            em.close();
        }
    }

    @Override
    public void deleteCategorie(int id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            // Règle métier : impossible de supprimer une catégorie si des produits y sont
            // encore rattachés. On vérifie explicitement AVANT le remove() : on ne compte
            // pas sur le CascadeType.ALL de la relation @OneToMany (qui supprimerait les
            // produits en même temps, ce qu'on ne veut surtout pas ici).
            long nbProduits = em.createQuery(
                    "SELECT COUNT(p) FROM Produit p WHERE p.categorie.id = :catId", Long.class
            ).setParameter("catId", id).getSingleResult();

            if (nbProduits > 0) {
                em.getTransaction().rollback();
                throw new IllegalStateException(
                        "Impossible de supprimer cette catégorie : " + nbProduits + " produit(s) y sont encore rattachés.");
            }

            Optional<Categorie> categorieOptional = findById(id);
            if(categorieOptional.isPresent()) em.remove(categorieOptional.get());
            em.getTransaction().commit();
        } catch (IllegalStateException e) {
            throw e; // message métier à afficher tel quel dans l'UI
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException("Erreur lors de la suppression de la catégorie");
        } finally {
            em.close();
        }
    }

    @Override
    public long countProduits(int categorieId) {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            return em.createQuery(
                    "SELECT COUNT(p) FROM Produit p WHERE p.categorie.id = :catId", Long.class
            ).setParameter("catId", categorieId).getSingleResult();
        }
    }
}
