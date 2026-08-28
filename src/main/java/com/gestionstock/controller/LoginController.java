package com.gestionstock.controller;

import com.gestionstock.model.Utilisateur;
import com.gestionstock.service.UtilisateurService;
import com.gestionstock.service.UtilisateurServiceImpl;
import com.gestionstock.util.SessionUtilisateur;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class LoginController {
    @FXML
    private TextField champEmail;
    @FXML
    private PasswordField champMotDePasse;
    @FXML
    private Label labelErreur;

    private final UtilisateurService utilisateurService = new UtilisateurServiceImpl();

    @FXML
    private void seConnecter() {
        String email = champEmail.getText() == null ? "" : champEmail.getText().trim();
        String motDePasse = champMotDePasse.getText();

        if (email.isEmpty() || motDePasse == null || motDePasse.isEmpty()) {
            labelErreur.setText("Veuillez renseigner l'email et le mot de passe.");
            return;
        }

        Optional<Utilisateur> resultat = utilisateurService.authentifier(email, motDePasse);

        if (resultat.isEmpty()) {
            // On ne précise jamais si c'est l'email OU le mot de passe qui est faux (sécurité :
            // ça éviterait de révéler à un attaquant si un email existe en base).
            // On vérifie séparément le cas "compte désactivé" pour donner un message plus utile.
            boolean compteExisteEtEstInactif = utilisateurService.findByEmail(email)
                    .map(u -> !u.isActif())
                    .orElse(false);

            labelErreur.setText(compteExisteEtEstInactif
                    ? "Ce compte a été désactivé. Contactez un administrateur."
                    : "Email ou mot de passe incorrect.");
            return;
        }

        Utilisateur utilisateurConnecte = resultat.get();
        SessionUtilisateur.connecter(utilisateurConnecte);
        chargerMenuPrincipal();
    }

    private void chargerMenuPrincipal() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/gestionstock/main.fxml"));
            Parent racine = loader.load();

            Stage stage = (Stage) champEmail.getScene().getWindow();
            Scene scene = new Scene(racine);
            scene.getStylesheets().add(getClass().getResource("/com/gestionstock/style.css").toExternalForm());
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            labelErreur.setText("Impossible de charger l'application. Réessayez.");
        }
    }
}
