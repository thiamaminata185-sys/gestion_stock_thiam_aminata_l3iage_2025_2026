package com.gestionstock.model;

import com.gestionstock.model.enums.RoleUtilisateur;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;

@Entity
@Table(name = "utilisateurs")
public class Utilisateur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 150)
    private String email;

    @Column(nullable = false, length = 100)
    private String nom;

    // Jamais le mot de passe en clair ici : uniquement son hachage BCrypt (voir UtilisateurServiceImpl)
    @Column(name = "mot_de_passe_hash", nullable = false)
    private String motDePasseHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleUtilisateur role;

    @CreationTimestamp
    @Column(name = "date_creation")
    private LocalDate dateCreation;

    private boolean actif = true;

    public Utilisateur() {
    }

    public Utilisateur(Long id, String email, String nom, String motDePasseHash, RoleUtilisateur role, LocalDate dateCreation, boolean actif) {
        this.id = id;
        this.email = email;
        this.nom = nom;
        this.motDePasseHash = motDePasseHash;
        this.role = role;
        this.dateCreation = dateCreation;
        this.actif = actif;
    }

    public void setDateCreation(LocalDate dateCreation) {
        this.dateCreation = dateCreation;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getMotDePasseHash() {
        return motDePasseHash;
    }

    public void setMotDePasseHash(String motDePasseHash) {
        this.motDePasseHash = motDePasseHash;
    }

    public RoleUtilisateur getRole() {
        return role;
    }

    public void setRole(RoleUtilisateur role) {
        this.role = role;
    }

    public LocalDate getDateCreation() {
        return dateCreation;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }
}
