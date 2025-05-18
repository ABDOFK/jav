package dao;

import java.util.List;
import model.Doctor;

/**
 * Interface DAO pour la gestion des médecins.
 * Définit les méthodes CRUD et de recherche spécifiques aux médecins.
 * Cette interface étend les fonctionnalités de UserDAO avec des méthodes
 * spécifiques aux médecins.
 */
public interface DoctorDAO {
    
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
     * Active ou désactive un compte médecin
     * 
     * @param doctorId L'ID du médecin
     * @param active Le nouvel état du compte (actif/inactif)
     * @throws DAOException En cas d'erreur de persistance
     */
    void setDoctorActive(int doctorId, boolean active) throws DAOException;
    
    /**
     * Récupère toutes les spécialités médicales disponibles dans le système
     * 
     * @return Liste des spécialités uniques
     * @throws DAOException En cas d'erreur de persistance
     */
    List<String> getAllSpecialties() throws DAOException;
    
    /**
     * Récupère les médecins disponibles pour une date et heure spécifiques
     * 
     * @param dateTime La date et heure à vérifier
     * @return Liste des médecins disponibles
     * @throws DAOException En cas d'erreur de persistance
     */
    List<Doctor> getAvailableDoctors(java.time.LocalDateTime dateTime) throws DAOException;
    
    /**
     * Récupère les créneaux disponibles pour un médecin à une date donnée
     * 
     * @param doctorId L'ID du médecin
     * @param date La date à vérifier
     * @return Liste des créneaux horaires disponibles (format HH:mm)
     * @throws DAOException En cas d'erreur de persistance
     */
    List<String> getAvailableTimeSlots(int doctorId, java.time.LocalDate date) throws DAOException;
}