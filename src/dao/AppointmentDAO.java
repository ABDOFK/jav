package dao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import model.Appointment;
import model.AppointmentStatus;

/**
 * Interface DAO pour la gestion des rendez-vous.
 * Définit les méthodes CRUD et de recherche pour la persistance des rendez-vous.
 */
public interface AppointmentDAO {
    
    /**
     * Ajoute un nouveau rendez-vous dans la base de données
     * 
     * @param appointment Le rendez-vous à ajouter
     * @return L'ID généré du rendez-vous
     * @throws DAOException En cas d'erreur de persistance
     */
    int addAppointment(Appointment appointment) throws DAOException;
    
    /**
     * Met à jour les informations d'un rendez-vous existant
     * 
     * @param appointment Le rendez-vous avec les données mises à jour
     * @throws DAOException En cas d'erreur de persistance
     */
    void updateAppointment(Appointment appointment) throws DAOException;
    
    /**
     * Supprime un rendez-vous de la base de données
     * 
     * @param appointmentId L'ID du rendez-vous à supprimer
     * @throws DAOException En cas d'erreur de persistance
     */
    void deleteAppointment(int appointmentId) throws DAOException;
    
    /**
     * Change le statut d'un rendez-vous
     * 
     * @param appointmentId L'ID du rendez-vous
     * @param status Le nouveau statut
     * @throws DAOException En cas d'erreur de persistance
     */
    void updateAppointmentStatus(int appointmentId, AppointmentStatus status) throws DAOException;
    
    /**
     * Récupère un rendez-vous par son ID
     * 
     * @param appointmentId L'ID du rendez-vous à récupérer
     * @return Le rendez-vous correspondant ou null si non trouvé
     * @throws DAOException En cas d'erreur de persistance
     */
    Appointment getAppointmentById(int appointmentId) throws DAOException;
    
    /**
     * Récupère tous les rendez-vous d'un patient
     * 
     * @param patientId L'ID du patient
     * @return Liste des rendez-vous du patient
     * @throws DAOException En cas d'erreur de persistance
     */
    List<Appointment> getAppointmentsByPatient(int patientId) throws DAOException;
    
    /**
     * Récupère tous les rendez-vous d'un médecin
     * 
     * @param doctorId L'ID du médecin
     * @return Liste des rendez-vous du médecin
     * @throws DAOException En cas d'erreur de persistance
     */
    List<Appointment> getAppointmentsByDoctor(int doctorId) throws DAOException;
    
    /**
     * Récupère tous les rendez-vous d'un médecin pour une date donnée
     * 
     * @param doctorId L'ID du médecin
     * @param date La date des rendez-vous
     * @return Liste des rendez-vous du médecin pour la date spécifiée
     * @throws DAOException En cas d'erreur de persistance
     */
    List<Appointment> getAppointmentsByDoctorAndDate(int doctorId, LocalDate date) throws DAOException;
    
    /**
     * Récupère tous les rendez-vous d'un médecin entre deux dates
     * 
     * @param doctorId L'ID du médecin
     * @param startDate Date de début
     * @param endDate Date de fin
     * @return Liste des rendez-vous du médecin dans la plage de dates
     * @throws DAOException En cas d'erreur de persistance
     */
    List<Appointment> getAppointmentsByDoctorAndDateRange(int doctorId, LocalDate startDate, LocalDate endDate) throws DAOException;
    
    /**
     * Vérifie s'il existe un conflit de rendez-vous pour un médecin
     * 
     * @param doctorId L'ID du médecin
     * @param startDateTime Date et heure de début du rendez-vous
     * @param endDateTime Date et heure de fin du rendez-vous
     * @param excludeAppointmentId ID d'un rendez-vous à exclure de la vérification (pour les mises à jour)
     * @return true s'il y a un conflit, false sinon
     * @throws DAOException En cas d'erreur de persistance
     */
    boolean hasAppointmentConflict(int doctorId, LocalDateTime startDateTime, LocalDateTime endDateTime, int excludeAppointmentId) throws DAOException;
    
    /**
     * Récupère tous les rendez-vous pour une date donnée
     * 
     * @param date La date des rendez-vous
     * @return Liste des rendez-vous pour la date spécifiée
     * @throws DAOException En cas d'erreur de persistance
     */
    List<Appointment> getAppointmentsByDate(LocalDate date) throws DAOException;
    
    /**
     * Récupère tous les rendez-vous avec un statut spécifique
     * 
     * @param status Le statut des rendez-vous à récupérer
     * @return Liste des rendez-vous ayant le statut spécifié
     * @throws DAOException En cas d'erreur de persistance
     */
    List<Appointment> getAppointmentsByStatus(AppointmentStatus status) throws DAOException;
    
    /**
     * Compte le nombre de rendez-vous pour un médecin et une date donnée
     * 
     * @param doctorId L'ID du médecin
     * @param date La date des rendez-vous
     * @return Le nombre de rendez-vous
     * @throws DAOException En cas d'erreur de persistance
     */
    int countAppointmentsByDoctorAndDate(int doctorId, LocalDate date) throws DAOException;
    
    /**
     * Récupère les prochains rendez-vous d'un patient
     * 
     * @param patientId L'ID du patient
     * @param limit Le nombre maximum de rendez-vous à récupérer
     * @return Liste des prochains rendez-vous du patient
     * @throws DAOException En cas d'erreur de persistance
     */
    List<Appointment> getUpcomingAppointmentsByPatient(int patientId, int limit) throws DAOException;
    
    /**
     * Récupère les prochains rendez-vous d'un médecin
     * 
     * @param doctorId L'ID du médecin
     * @param limit Le nombre maximum de rendez-vous à récupérer
     * @return Liste des prochains rendez-vous du médecin
     * @throws DAOException En cas d'erreur de persistance
     */
    List<Appointment> getUpcomingAppointmentsByDoctor(int doctorId, int limit) throws DAOException;
    
    /**
     * Recherche avancée de rendez-vous selon plusieurs critères
     * 
     * @param criteria Map de critères de recherche (nom de colonne -> valeur)
     * @return Liste des rendez-vous correspondants aux critères
     * @throws DAOException En cas d'erreur de persistance
     */
    List<Appointment> searchAppointmentsByCriteria(java.util.Map<String, Object> criteria) throws DAOException;
}