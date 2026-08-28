package com.gestionstock.service;

import com.gestionstock.model.Utilisateur;
import com.gestionstock.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;
import java.util.Optional;

public class UtilisateurServiceImpl implements UtilisateurService {

    @Override
    public Optional<Utilisateur> authentifier(String email, String motDePasseEnClair) {
        Optional<Utilisateur> utilisateurTrouve = findByEmail(email);

        if (utilisateurTrouve.isEmpty()) {
            return Optional.empty(); // email inconnu
        }

        Utilisateur utilisateur = utilisateurTrouve.get();

        if (!utilisateur.isActif()) {
            return Optional.empty(); // compte désactivé (le contrôleur affichera un message dédié)
        }

        // BCrypt.checkpw recalcule le hachage du mot de passe saisi avec le MÊME sel que celui
        // stocké (le sel est inclus dans motDePasseHash) et compare les deux hachages.
        // On ne compare JAMAIS le mot de passe en clair directement.
        boolean motDePasseCorrect = BCrypt.checkpw(motDePasseEnClair, utilisateur.getMotDePasseHash());

        return motDePasseCorrect ? Optional.of(utilisateur) : Optional.empty();
    }

    @Override
    public List<Utilisateur> findAllUtilisateurs() {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            return em.createQuery("SELECT u FROM Utilisateur u ORDER BY u.nom", Utilisateur.class)
                    .getResultList();
        }
    }

    @Override
    public Optional<Utilisateur> findByEmail(String email) {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            return Optional.of(em.createQuery(
                            "SELECT u FROM Utilisateur u WHERE u.email = :email", Utilisateur.class)
                    .setParameter("email", email)
                    .getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public void addUtilisateur(Utilisateur utilisateur, String motDePasseEnClair) {
        // BCrypt.gensalt() génère un sel aléatoire à chaque appel : deux utilisateurs avec le même
        // mot de passe auront des hachages différents en base, ce qui protège contre les attaques
        // par table arc-en-ciel (rainbow tables).
        utilisateur.setMotDePasseHash(BCrypt.hashpw(motDePasseEnClair, BCrypt.gensalt()));

        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(utilisateur);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException("Erreur lors de la création de l'utilisateur (email déjà utilisé ?)");
        } finally {
            em.close();
        }
    }

    @Override
    public void activerDesactiver(Long id, boolean actif) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Utilisateur utilisateur = em.find(Utilisateur.class, id);
            if (utilisateur != null) {
                utilisateur.setActif(actif);
                em.merge(utilisateur);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException("Erreur lors de la mise à jour du compte utilisateur");
        } finally {
            em.close();
        }
    }
}
