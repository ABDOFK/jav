package dao;

import java.util.List;
import model.Patient;

/**
 * Interface DAO pour la gestion des patients.
 * Définit les méthodes CRUD et de recherche pour la persistance des patients.
 */
public interface PatientDAO {
    
    /**
     * Ajoute un nouveau patient dans la base de données
     * 
     * @param patient Le patient à ajouter
     * @return L'ID généré du patient
     * @throws DAOException En cas d'erreur de persistance
     */
    int addPatient(Patient patient) throws DAOException;
    
    /**
     * Met à jour les informations d'un patient existant
     * 
     * @param patient Le patient avec les données mises à jour
     * @throws DAOException En cas d'erreur de persistance
     */
    void updatePatient(Patient patient) throws DAOException;
    
    /**
     * Supprime un patient de la base de données
     * 
     * @param patientId L'ID du patient à supprimer
     * @throws DAOException En cas d'erreur de persistance
     */
    void deletePatient(int patientId) throws DAOException;
    
    /**
     * Récupère un patient par son ID
     * 
     * @param patientId L'ID du patient à récupérer
     * @return Le patient correspondant ou null si non trouvé
     * @throws DAOException En cas d'erreur de persistance
     */
    Patient getPatientById(int patientId) throws DAOException;
    
    /**
     * Récupère tous les patients de la base de données
     * 
     * @return Liste de tous les patients
     * @throws DAOException En cas d'erreur de persistance
     */
    List<Patient> getAllPatients() throws DAOException;
    
    /**
     * Recherche des patients par nom et/ou prénom
     * 
     * @param lastName Le nom de famille (peut être null)
     * @param firstName Le prénom (peut être null)
     * @return Liste des patients correspondants au critère de recherche
     * @throws DAOException En cas d'erreur de persistance
     */
    List<Patient> searchPatientsByName(String lastName, String firstName) throws DAOException;
    
    /**
     * Recherche des patients par numéro de téléphone
     * 
     * @param phone Le numéro de téléphone à rechercher
     * @return Liste des patients correspondants (normalement un seul)
     * @throws DAOException En cas d'erreur de persistance
     */
    List<Patient> searchPatientsByPhone(String phone) throws DAOException;
    
    /**
     * Recherche des patients par email
     * 
     * @param email L'adresse email à rechercher
     * @return Liste des patients correspondants (normalement un seul)
     * @throws DAOException En cas d'erreur de persistance
     */
    List<Patient> searchPatientsByEmail(String email) throws DAOException;
    
    /**
     * Vérifie si un patient existe déjà avec le même nom, prénom et date de naissance
     * 
     * @param patient Le patient à vérifier
     * @return true si un patient similaire existe déjà, false sinon
     * @throws DAOException En cas d'erreur de persistance
     */
    boolean isPatientExists(Patient patient) throws DAOException;
    
    /**
     * Compte le nombre total de patients
     * 
     * @return Le nombre total de patients
     * @throws DAOException En cas d'erreur de persistance
     */
    int countPatients() throws DAOException;
    
    /**
     * Recherche avancée de patients selon plusieurs critères
     * 
     * @param criteria Map de critères de recherche (nom de colonne -> valeur)
     * @return Liste des patients correspondants aux critères
     * @throws DAOException En cas d'erreur de persistance
     */
    List<Patient> searchPatientsByCriteria(java.util.Map<String, Object> criteria) throws DAOException;
}