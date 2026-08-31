package com.gestionstock.service;

import com.gestionstock.model.Categorie;
import com.gestionstock.model.Fournisseur;
import com.gestionstock.model.Mouvement;
import com.gestionstock.model.Produit;
import com.gestionstock.model.enums.TypeMouvement;
import com.gestionstock.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

public class StatistiquesServiceImpl implements StatistiquesService {

    @Override
    public double valeurTotaleStock() {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            Double total = em.createQuery(
                    "SELECT SUM(p.quantiteStock * p.prix) FROM Produit p", Double.class
            ).getSingleResult();
            return total != null ? total : 0.0;
        }
    }

    @Override
    public Optional<Produit> produitLePlusMouvemente(LocalDate debut, LocalDate fin) {
        List<Mouvement> mouvements = mouvementsSurPeriode(debut, fin);

        // On regroupe par produit et on additionne les quantités (entrées + sorties confondues),
        // puis on prend le produit dont le total est le plus élevé.
        Map<Produit, Long> totalParProduit = mouvements.stream()
                .filter(m -> m.getProduit() != null)
                .collect(Collectors.groupingBy(Mouvement::getProduit, Collectors.summingLong(Mouvement::getQuantite)));

        return totalParProduit.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
    }

    @Override
    public Optional<Categorie> categoriePlusForteValeurStock() {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            List<Produit> produits = em.createQuery("SELECT p FROM Produit p", Produit.class).getResultList();

            Map<Categorie, Double> valeurParCategorie = produits.stream()
                    .filter(p -> p.getCategorie() != null)
                    .collect(Collectors.groupingBy(Produit::getCategorie,
                            Collectors.summingDouble(p -> p.getQuantiteStock() * p.getPrix())));

            return valeurParCategorie.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey);
        }
    }

    @Override
    public Optional<Fournisseur> fournisseurAvecPlusDeProduits() {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            List<Produit> produits = em.createQuery("SELECT p FROM Produit p", Produit.class).getResultList();

            Map<Fournisseur, Long> nbParFournisseur = produits.stream()
                    .filter(p -> p.getFournisseur() != null)
                    .collect(Collectors.groupingBy(Produit::getFournisseur, Collectors.counting()));

            return nbParFournisseur.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey);
        }
    }

    @Override
    public long nombreSortiesVersRuptureEvitee(LocalDate debut, LocalDate fin) {
        // Approximation assumée (voir Javadoc de l'interface) : on ne stocke pas l'historique du
        // niveau de stock à chaque instant, donc on se base sur l'état ACTUEL du produit plutôt
        // que sur son état au moment précis du mouvement.
        return mouvementsSurPeriode(debut, fin).stream()
                .filter(m -> m.getType() == TypeMouvement.SORTIE)
                .filter(m -> m.getProduit() != null && m.getProduit().getQuantiteStock() <= m.getProduit().getQuantiteMin())
                .count();
    }

    @Override
    public Map<YearMonth, long[]> mouvementsParMois(LocalDate debut, LocalDate fin) {
        List<Mouvement> mouvements = mouvementsSurPeriode(debut, fin);

        Map<YearMonth, long[]> resultat = new TreeMap<>(); // TreeMap : trié par mois croissant, pratique pour le graphique
        for (Mouvement m : mouvements) {
            YearMonth mois = YearMonth.from(m.getDateMouvement());
            long[] compteurs = resultat.computeIfAbsent(mois, k -> new long[2]); // [0]=entrées, [1]=sorties
            if (m.getType() == TypeMouvement.ENTRE) {
                compteurs[0] += m.getQuantite();
            } else {
                compteurs[1] += m.getQuantite();
            }
        }
        return resultat;
    }

    @Override
    public Map<String, Double> repartitionValeurParCategorie() {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            List<Produit> produits = em.createQuery("SELECT p FROM Produit p", Produit.class).getResultList();

            return produits.stream()
                    .filter(p -> p.getCategorie() != null)
                    .collect(Collectors.groupingBy(p -> p.getCategorie().getNom(),
                            Collectors.summingDouble(p -> p.getQuantiteStock() * p.getPrix())));
        }
    }

    private List<Mouvement> mouvementsSurPeriode(LocalDate debut, LocalDate fin) {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            StringBuilder jpql = new StringBuilder("SELECT m FROM Mouvement m WHERE 1=1");
            if (debut != null) jpql.append(" AND m.dateMouvement >= :debut");
            if (fin != null) jpql.append(" AND m.dateMouvement <= :fin");

            TypedQuery<Mouvement> query = em.createQuery(jpql.toString(), Mouvement.class);
            if (debut != null) query.setParameter("debut", debut.atStartOfDay());
            if (fin != null) query.setParameter("fin", LocalDateTime.of(fin, LocalTime.MAX));

            return query.getResultList();
        }
    }
}
