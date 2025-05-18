package dao;

import java.util.List;
import model.User;
import model.Secretary;
import model.Doctor;

/**
 * Interface DAO pour la gestion des utilisateurs (secrétaires et médecins).
 * Définit les méthodes CRUD et de recherche pour la persistance des utilisateurs.
 */
public interface UserDAO {
    
    /**
     * Ajoute un nouvel utilisateur dans la base de données
     * 
     * @param user L'utilisateur à ajouter
     * @return L'ID généré de l'utilisateur
     * @throws DAOException En cas d'erreur de persistance
     */
    int addUser(User user) throws DAOException;
    
    /**
     * Met à jour les informations d'un utilisateur existant
     * 
     * @param user L'utilisateur avec les données mises à jour
     * @throws DAOException En cas d'erreur de persistance
     */
    void updateUser(User user) throws DAOException;
    
    /**
     * Active ou désactive un compte utilisateur
     * 
     * @param userId L'ID de l'utilisateur
     * @param active Le nouvel état du compte (actif/inactif)
     * @throws DAOException En cas d'erreur de persistance
     */
    void setUserActive(int userId, boolean active) throws DAOException;
    
    /**
     * Récupère un utilisateur par son ID
     * 
     * @param userId L'ID de l'utilisateur à récupérer
     * @return L'utilisateur correspondant ou null si non trouvé
     * @throws DAOException En cas d'erreur de persistance
     */
    User getUserById(int userId) throws DAOException;
    
    /**
     * Récupère un utilisateur par son nom d'utilisateur
     * 
     * @param username Le nom d'utilisateur à rechercher
     * @return L'utilisateur correspondant ou null si non trouvé
     * @throws DAOException En cas d'erreur de persistance
     */
    User getUserByUsername(String username) throws DAOException;
    
    /**
     * Vérifie les identifiants d'un utilisateur pour l'authentification
     * 
     * @param username Le nom d'utilisateur
     * @param passwordHash Le mot de passe haché
     * @return L'utilisateur correspondant ou null si aucune correspondance
     * @throws DAOException En cas d'erreur de persistance
     */
    User authenticateUser(String username, String passwordHash) throws DAOException;
    
    /**
     * Récupère tous les utilisateurs
     * 
     * @return Liste de tous les utilisateurs
     * @throws DAOException En cas d'erreur de persistance
     */
    List<User> getAllUsers() throws DAOException;
    
    /**
     * Récupère tous les utilisateurs d'un rôle spécifique
     * 
     * @param role Le rôle à filtrer
     * @return Liste des utilisateurs du rôle spécifié
     * @throws DAOException En cas d'erreur de persistance
     */
    List<User> getUsersByRole(String role) throws DAOException;
    
    /**
     * Ajoute une nouvelle secrétaire dans la base de données
     * 
     * @param secretary La secrétaire à ajouter
     * @return L'ID généré de la secrétaire
     * @throws DAOException En cas d'erreur de persistance
     */
    int addSecretary(Secretary secretary) throws DAOException;
    
    /**
     * Met à jour les informations d'une secrétaire existante
     * 
     * @param secretary La secrétaire avec les données mises à jour
     * @throws DAOException En cas d'erreur de persistance
     */
    void updateSecretary(Secretary secretary) throws DAOException;
    
    /**
     * Récupère une secrétaire par son ID
     * 
     * @param id L'ID de la secrétaire
     * @return La secrétaire correspondante ou null si non trouvée
     * @throws DAOException En cas d'erreur de persistance
     */
    Secretary getSecretaryById(int id) throws DAOException;
    
    /**
     * Récupère toutes les secrétaires
     * 
     * @return Liste de toutes les secrétaires
     * @throws DAOException En cas d'erreur de persistance
     */
    List<Secretary> getAllSecretaries() throws DAOException;
    
    /**
     * Ajoute un nouveau médecin dans la base de données
     * 
     * @param doctor Le médecin à ajouter
     * @return L'ID généré du médecin
     * @throws DAOException En cas d'erreur de persistance
     */
    int addDoctor(Doctor doctor) throws DAOException;
    
    /**
     * Met à jour les informations d'un médecin existant
     * 
     * @param doctor Le médecin avec les données mises à jour
     * @throws DAOException En cas d'erreur de persistance
     */
    void updateDoctor(Doctor doctor) throws DAOException;
    
    /**
     * Récupère un médecin par son ID
     * 
     * @param id L'ID du médecin
     * @return Le médecin correspondant ou null si non trouvé
     * @throws DAOException En cas d'erreur de persistance
     */
    Doctor getDoctorById(int id) throws DAOException;
    
    /**
     * Récupère tous les médecins
     * 
     * @return Liste de tous les médecins
     * @throws DAOException En cas d'erreur de persistance
     */
    List<Doctor> getAllDoctors() throws DAOException;
    
    /**
     * Récupère les médecins par spécialité
     * 
     * @param specialty La spécialité à rechercher
     * @return Liste des médecins ayant cette spécialité
     * @throws DAOException En cas d'erreur de persistance
     */
    List<Doctor> getDoctorsBySpecialty(String specialty) throws DAOException;
    
    /**
     * Récupère toutes les spécialités médicales disponibles dans le système
     * 
     * @return Liste des spécialités uniques
     * @throws DAOException En cas d'erreur de persistance
     */
    List<String> getAllSpecialties() throws DAOException;
    
    /**
     * Vérifie si un nom d'utilisateur est déjà utilisé
     * 
     * @param username Le nom d'utilisateur à vérifier
     * @return true si le nom est déjà utilisé, false sinon
     * @throws DAOException En cas d'erreur de persistance
     */
    boolean isUsernameExists(String username) throws DAOException;
    
    /**
     * Change le mot de passe d'un utilisateur
     * 
     * @param userId L'ID de l'utilisateur
     * @param newPasswordHash Le nouveau mot de passe haché
     * @throws DAOException En cas d'erreur de persistance
     */
    void changePassword(int userId, String newPasswordHash) throws DAOException;
}