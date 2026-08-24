package com.gestionstock.service;

import com.gestionstock.model.Categorie;

import java.util.List;
import java.util.Optional;

public interface CategorieService {
    List<Categorie> findAllCategories();
    Optional<Categorie> findById(int id);
    void addCategorie(Categorie c);
    void updateCategorie(Categorie c);
    void deleteCategorie(int id);
    // Nombre de produits rattachés à la catégorie (utile pour l'affichage et pour bloquer la suppression)
    long countProduits(int categorieId);
}
