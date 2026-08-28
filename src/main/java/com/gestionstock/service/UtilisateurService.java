package com.gestionstock.service;

import com.gestionstock.model.Utilisateur;

import java.util.List;
import java.util.Optional;

public interface UtilisateurService {
    /**
     * Vérifie email + mot de passe (en clair, saisi par l'utilisateur) contre la base.
     * Retourne l'Utilisateur si les identifiants sont corrects ET le compte est actif, vide sinon.
     */
    Optional<Utilisateur> authentifier(String email, String motDePasseEnClair);

    List<Utilisateur> findAllUtilisateurs();
    Optional<Utilisateur> findByEmail(String email);
    void addUtilisateur(Utilisateur utilisateur, String motDePasseEnClair);
    void activerDesactiver(Long id, boolean actif);
}
