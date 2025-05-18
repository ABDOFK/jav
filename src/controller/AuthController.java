package controller;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

import dao.DAOException;
import dao.UserDAO;
import dao.UserDAOImpl;
import model.Doctor;
import model.Secretary;
import model.User;

/**
 * Contrôleur pour la gestion de l'authentification.
 * Implémente le pattern Singleton pour assurer une seule instance et une gestion centralisée.
 */
public class AuthController {
    
    private static final Logger LOGGER = Logger.getLogger(AuthController.class.getName());
    
    // Instance unique (Singleton)
    private static AuthController instance;
    
    // DAO pour les opérations sur les utilisateurs
    private final UserDAO userDAO;
    
    // Utilisateur actuellement connecté
    private User currentUser;
    
    // Compteur d'échecs de connexion
    private int loginAttempts;
    
    /**
     * Constructeur privé (Singleton)
     */
    private AuthController() {
        this.userDAO = new UserDAOImpl();
        this.currentUser = null;
        this.loginAttempts = 0;
    }
    
    /**
     * Obtient l'instance unique du contrôleur d'authentification
     * 
     * @return L'instance de AuthController
     */
    public static synchronized AuthController getInstance() {
        if (instance == null) {
            instance = new AuthController();
        }
        return instance;
    }
    
    /**
     * Tente d'authentifier un utilisateur
     * 
     * @param username Nom d'utilisateur
     * @param password Mot de passe en clair
     * @return true si l'authentification réussit, false sinon
     */
    public boolean login(String username, String password) {
        if (username == null || username.trim().isEmpty() || 
            password == null || password.trim().isEmpty()) {
            LOGGER.warning("Tentative de connexion avec des identifiants vides");
            incrementLoginAttempts();
            return false;
        }
        
        try {
            String hashedPassword = hashPassword(password);
            User user = userDAO.authenticateUser(username, hashedPassword);
            
            if (user != null) {
                // Authentification réussie
                this.currentUser = user;
                this.loginAttempts = 0;
                LOGGER.info("Connexion réussie pour l'utilisateur: " + username);
                return true;
            } else {
                // Échec d'authentification
                incrementLoginAttempts();
                LOGGER.warning("Échec de connexion pour l'utilisateur: " + username);
                return false;
            }
            
        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'authentification", e);
            incrementLoginAttempts();
            return false;
        }
    }
    
    /**
     * Déconnexion de l'utilisateur courant
     */
    public void logout() {
        if (currentUser != null) {
            LOGGER.info("Déconnexion de l'utilisateur: " + currentUser.getUsername());
            this.currentUser = null;
        }
    }
    
    /**
     * Vérifie si un utilisateur est actuellement connecté
     * 
     * @return true si un utilisateur est connecté, false sinon
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    /**
     * Obtient l'utilisateur actuellement connecté
     * 
     * @return L'utilisateur connecté ou null si aucun
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Vérifie si l'utilisateur courant est une secrétaire
     * 
     * @return true si l'utilisateur est une secrétaire, false sinon
     */
    public boolean isCurrentUserSecretary() {
        return isLoggedIn() && currentUser instanceof Secretary;
    }
    
    /**
     * Vérifie si l'utilisateur courant est un médecin
     * 
     * @return true si l'utilisateur est un médecin, false sinon
     */
    public boolean isCurrentUserDoctor() {
        return isLoggedIn() && currentUser instanceof Doctor;
    }
    
    /**
     * Récupère la secrétaire actuellement connectée
     * 
     * @return La secrétaire connectée ou null
     */
    public Secretary getCurrentSecretary() {
        return isCurrentUserSecretary() ? (Secretary) currentUser : null;
    }
    
    /**
     * Récupère le médecin actuellement connecté
     * 
     * @return Le médecin connecté ou null
     */
    public Doctor getCurrentDoctor() {
        return isCurrentUserDoctor() ? (Doctor) currentUser : null;
    }
    
    /**
     * Vérifie si l'utilisateur courant a un rôle spécifique
     * 
     * @param role Le rôle à vérifier
     * @return true si l'utilisateur a ce rôle, false sinon
     */
    public boolean hasRole(String role) {
        return isLoggedIn() && currentUser.hasRole(role);
    }
    
    /**
     * Incrémente le compteur d'échecs de connexion
     */
    private void incrementLoginAttempts() {
        this.loginAttempts++;
        LOGGER.info("Nombre d'échecs de connexion: " + this.loginAttempts);
    }
    
    /**
     * Obtient le nombre d'échecs de connexion
     * 
     * @return Le nombre d'échecs de connexion
     */
    public int getLoginAttempts() {
        return loginAttempts;
    }
    
    /**
     * Réinitialise le compteur d'échecs de connexion
     */
    public void resetLoginAttempts() {
        this.loginAttempts = 0;
    }
    
    /**
     * Hache un mot de passe avec l'algorithme SHA-256
     * 
     * @param password Le mot de passe en clair
     * @return Le mot de passe haché
     */
    public String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
            
        } catch (NoSuchAlgorithmException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du hachage du mot de passe", e);
            // Fallback simple en cas d'erreur (à éviter en production)
            return password;
        }
    }
    
    /**
     * Change le mot de passe d'un utilisateur
     * 
     * @param userId ID de l'utilisateur
     * @param oldPassword Ancien mot de passe
     * @param newPassword Nouveau mot de passe
     * @return true si le changement réussit, false sinon
     */
    public boolean changePassword(int userId, String oldPassword, String newPassword) {
        try {
            // Vérifier que l'ancien mot de passe est correct
            User user = userDAO.getUserById(userId);
            if (user == null) {
                LOGGER.warning("Tentative de changement de mot de passe pour un utilisateur inexistant: " + userId);
                return false;
            }
            
            String hashedOldPassword = hashPassword(oldPassword);
            if (!hashedOldPassword.equals(user.getPasswordHash())) {
                LOGGER.warning("Ancien mot de passe incorrect pour l'utilisateur: " + userId);
                return false;
            }
            
            // Changer le mot de passe
            String hashedNewPassword = hashPassword(newPassword);
            userDAO.changePassword(userId, hashedNewPassword);
            LOGGER.info("Mot de passe changé avec succès pour l'utilisateur: " + userId);
            
            // Mettre à jour l'utilisateur courant si nécessaire
            if (currentUser != null && currentUser.getId() == userId) {
                currentUser.setPasswordHash(hashedNewPassword);
            }
            
            return true;
            
        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du changement de mot de passe", e);
            return false;
        }
    }
}