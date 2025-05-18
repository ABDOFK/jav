package model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Classe abstraite représentant un utilisateur du système.
 * Sert de classe de base pour les classes Secretary et Doctor.
 */
public abstract class User {
    
    // Attributs communs à tous les utilisateurs
    private int id;
    private String username;
    private String passwordHash; // Stockage sécurisé (mot de passe haché)
    private String role;
    private String fullName;
    private boolean active;
    private LocalDateTime creationDate;
    
    /**
     * Constructeur par défaut
     */
    public User() {
        this.active = true;
        this.creationDate = LocalDateTime.now();
    }
    
    /**
     * Constructeur avec paramètres essentiels
     * 
     * @param username Nom d'utilisateur pour la connexion
     * @param passwordHash Mot de passe haché pour l'authentification
     * @param role Rôle de l'utilisateur (SECRETAIRE, MEDECIN)
     * @param fullName Nom complet de l'utilisateur
     */
    public User(String username, String passwordHash, String role, String fullName) {
        this();
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.fullName = fullName;
    }
    
    /**
     * Constructeur avec tous les paramètres
     * 
     * @param id Identifiant unique
     * @param username Nom d'utilisateur pour la connexion
     * @param passwordHash Mot de passe haché pour l'authentification
     * @param role Rôle de l'utilisateur (SECRETAIRE, MEDECIN)
     * @param fullName Nom complet de l'utilisateur
     * @param active État du compte (actif/inactif)
     * @param creationDate Date de création du compte
     */
    public User(int id, String username, String passwordHash, String role, 
                String fullName, boolean active, LocalDateTime creationDate) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.fullName = fullName;
        this.active = active;
        this.creationDate = creationDate;
    }

    // Getters et setters
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }
    
    /**
     * Vérifie si l'utilisateur a un rôle spécifique
     * 
     * @param roleToCheck Le rôle à vérifier
     * @return true si l'utilisateur a ce rôle, false sinon
     */
    public boolean hasRole(String roleToCheck) {
        return role.equalsIgnoreCase(roleToCheck);
    }
    
    /**
     * Méthode abstraite pour obtenir une description du type d'utilisateur
     * Doit être implémentée par les sous-classes
     * 
     * @return Description du type d'utilisateur
     */
    public abstract String getUserType();
    
    /**
     * Méthode pour vérifier si l'utilisateur peut se connecter
     * 
     * @return true si l'utilisateur est actif, false sinon
     */
    public boolean canLogin() {
        return isActive();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id && 
               Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username);
    }

    @Override
    public String toString() {
        return fullName + " (" + role + ")";
    }
}