package com.gestionstock.model;

import jakarta.persistence.*;

/**
 * @Entity représente une table de la base de données, Hibernate se charge
 * du mapping entre la classe et la table. Ceci indique qu'il s'agit d'une entité JPA
 *
 * @Table(name = "produits"): représente le nom SQL de la table au niveau de la
 * base de données si on utilise @Entity(name = "produits") seulement on
 * force le nom de la classe à devenir produits au lieu de  Produit
 *
 * @Id identifie l'attribut comme clé primaire de l'entité
 *
 * @GeneratedValue(strategy = GenerationType.IDENTITY): indique du'Hibernate doit laisser la base de données générer
 * la valeur (AUTO_INCREMENT pour MySQL, SERIAL pour PostgreSQL)
 *
 * @Column: personnalise le mapping d'un attribut vers une colonne
 * name : nom de la colonne SQL
 * nullable = false: indique que la colonne ne peut être NULL
 * NB: Si le nom de l'attribut Java correspond au nom de la colonne SQL @Column est optionnel
 *
 * @ManyToOne: Plusieurs Produit pour une seule Categorie
 * @JoinColumn(name = "categorie_id"): indiqu le nom de la colonne de clé étrangère dans la table produits
 */
@Entity
@Table(name = "produits")
public class Produit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String nom;

    @Column(name = "quantite_stock", nullable = false)
    private int quantiteStock;

    @Column(name = "quantite_min", nullable = false)
    private int quantiteMin;

    @Column(nullable = false)
    private double prix;

    private double prixPromo;

    @ManyToOne
    @JoinColumn(name = "categorie_id")
    private Categorie categorie;

    @ManyToOne
    @JoinColumn(name = "fournisseur_id")
    private Fournisseur fournisseur;

    public Produit() {
    }

    public Produit(int id, String nom, int quantiteStock, int quantiteMin, double prix, double prixPromo, Categorie categorie, Fournisseur fournisseur) {
        this.id = id;
        this.nom = nom;
        this.quantiteStock = quantiteStock;
        this.quantiteMin = quantiteMin;
        this.prix = prix;
        this.prixPromo = prixPromo;
        this.categorie = categorie;
        this.fournisseur = fournisseur;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public int getQuantiteStock() {
        return quantiteStock;
    }

    public void setQuantiteStock(int quantiteStock) {
        this.quantiteStock = quantiteStock;
    }

    public int getQuantiteMin() {
        return quantiteMin;
    }

    public void setQuantiteMin(int quantiteMin) {
        this.quantiteMin = quantiteMin;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }


    public double getPrixPromo() {
        return prixPromo;
    }

    public void setPrixPromo(double prixPromo) {
        this.prixPromo = prixPromo;
    }

    public Categorie getCategorie() {
        return categorie;
    }

    public void setCategorie(Categorie categorie) {
        this.categorie = categorie;
    }

    public Fournisseur getFournisseur() {
        return fournisseur;
    }

    public void setFournisseur(Fournisseur fournisseur) {
        this.fournisseur = fournisseur;
    }

    @Override
    public String toString() {
        return "Produit{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", quantiteStock=" + quantiteStock +
                ", quantiteMin=" + quantiteMin +
                ", prix=" + prix +
                ", prixPromo=" + prixPromo +
                ", categorie=" + categorie +
                ", fournisseur=" + fournisseur +
                '}';
    }


}
