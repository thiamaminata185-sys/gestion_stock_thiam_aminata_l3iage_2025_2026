package com.gestionstock.util;

import com.gestionstock.model.Utilisateur;
import com.gestionstock.model.enums.RoleUtilisateur;

/**
 * Retient l'utilisateur connecté pendant toute la durée de vie de l'application.
 * Un singleton convient bien ici : il n'y a qu'un seul utilisateur connecté à la fois
 * dans cette application desktop (contrairement à une appli web multi-utilisateurs).
 */
public class SessionUtilisateur {
    private static Utilisateur utilisateurConnecte;

    private SessionUtilisateur() {
        // classe utilitaire : pas d'instanciation
    }

    public static void connecter(Utilisateur utilisateur) {
        utilisateurConnecte = utilisateur;
    }

    public static void deconnecter() {
        utilisateurConnecte = null;
    }

    public static Utilisateur getUtilisateurConnecte() {
        return utilisateurConnecte;
    }

    public static boolean estAdmin() {
        return utilisateurConnecte != null && utilisateurConnecte.getRole() == RoleUtilisateur.ADMIN;
    }

    public static boolean estConnecte() {
        return utilisateurConnecte != null;
    }
}
