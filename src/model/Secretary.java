package model;

import java.time.LocalDateTime;

/**
 * Classe représentant une secrétaire dans le système.
 * Étend la classe abstraite User avec des fonctionnalités spécifiques au rôle de secrétaire.
 */
public class Secretary extends User {
    
    // Rôle constant pour les secrétaires
    public static final String ROLE = "SECRETAIRE";
    
    // Attributs spécifiques à une secrétaire pourraient être ajoutés ici
    
    /**
     * Constructeur par défaut
     */
    public Secretary() {
        super();
        setRole(ROLE);
    }
    
    /**
     * Constructeur avec paramètres essentiels
     * 
     * @param username Nom d'utilisateur pour la connexion
     * @param passwordHash Mot de passe haché pour l'authentification
     * @param fullName Nom complet de la secrétaire
     */
    public Secretary(String username, String passwordHash, String fullName) {
        super(username, passwordHash, ROLE, fullName);
    }
    
    /**
     * Constructeur avec tous les paramètres
     * 
     * @param id Identifiant unique
     * @param username Nom d'utilisateur pour la connexion
     * @param passwordHash Mot de passe haché pour l'authentification
     * @param fullName Nom complet de la secrétaire
     * @param active État du compte (actif/inactif)
     * @param creationDate Date de création du compte
     */
    public Secretary(int id, String username, String passwordHash, String fullName, 
                     boolean active, LocalDateTime creationDate) {
        super(id, username, passwordHash, ROLE, fullName, active, creationDate);
    }
    
    /**
     * Implémentation de la méthode abstraite de User
     * 
     * @return Le type d'utilisateur (Secrétaire)
     */
    @Override
    public String getUserType() {
        return "Secrétaire";
    }
    
    /**
     * Vérifie si la secrétaire peut ajouter un patient
     * 
     * @return true car les secrétaires peuvent toujours ajouter des patients
     */
    public boolean canAddPatient() {
        return true;
    }
    
    /**
     * Vérifie si la secrétaire peut gérer les rendez-vous
     * 
     * @return true car les secrétaires peuvent toujours gérer les rendez-vous
     */
    public boolean canManageAppointments() {
        return true;
    }
    
    /**
     * Vérifie si la secrétaire peut exporter les plannings
     * 
     * @return true car les secrétaires peuvent toujours exporter les plannings
     */
    public boolean canExportPlannings() {
        return true;
    }
    
    /**
     * Vérifie si la secrétaire peut modifier les informations des médecins
     * 
     * @return true car les secrétaires peuvent toujours modifier les informations des médecins
     */
    public boolean canManageDoctors() {
        return true;
    }
    
    @Override
    public String toString() {
        return "Secrétaire: " + getFullName();
    }
}