package controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import dao.AppointmentDAO;
import dao.AppointmentDAOImpl;
import dao.DAOException;
import model.Appointment;
import model.AppointmentStatus;
import model.Doctor;
import model.Patient;
import model.Secretary;

/**
 * Contrôleur pour la gestion des rendez-vous.
 * Implémente le pattern Singleton pour assurer une seule instance.
 */
public class AppointmentController {
    
    private static final Logger LOGGER = Logger.getLogger(AppointmentController.class.getName());
    
    // Instance unique (Singleton)
    private static AppointmentController instance;
    
    // DAO pour les opérations sur les rendez-vous
    private final AppointmentDAO appointmentDAO;
    
    // Cache des rendez-vous récemment consultés (optimisation)
    private final Map<Integer, Appointment> appointmentCache;
    
    /**
     * Constructeur privé (Singleton)
     */
    private AppointmentController() {
        this.appointmentDAO = new AppointmentDAOImpl();
        this.appointmentCache = new HashMap<>();
    }
    
    /**
     * Obtient l'instance unique du contrôleur de rendez-vous
     * 
     * @return L'instance de AppointmentController
     */
    public static synchronized AppointmentController getInstance() {
        if (instance == null) {
            instance = new AppointmentController();
        }
        return instance;
    }
    
    /**
     * Ajoute un nouveau rendez-vous
     * 
     * @param appointment Le rendez-vous à ajouter
     * @return L'ID du rendez-vous créé
     * @throws IllegalArgumentException Si le rendez-vous est invalide ou en conflit
     * @throws DAOException Si une erreur survient lors de l'accès aux données
     */
    public int addAppointment(Appointment appointment) throws IllegalArgumentException, DAOException {
        // Validation des données
        validateAppointment(appointment);
        
        // Vérifier les conflits de rendez-vous
        if (hasAppointmentConflict(appointment.getDoctorId(), 
                                  appointment.getStartDateTime(), 
                                  appointment.getEndDateTime(), 
                                  0)) {
            throw new IllegalArgumentException("Ce créneau n'est pas disponible pour le médecin sélectionné.");
        }
        
        // Ajouter le rendez-vous
        int appointmentId = appointmentDAO.addAppointment(appointment);
        appointment.setId(appointmentId);
        
        // Ajouter au cache
        appointmentCache.put(appointmentId, appointment);
        
        LOGGER.info("Rendez-vous ajouté avec succès, ID: " + appointmentId);
        return appointmentId;
    }
    
    /**
     * Met à jour les informations d'un rendez-vous existant
     * 
     * @param appointment Le rendez-vous avec les données mises à jour
     * @throws IllegalArgumentException Si le rendez-vous est invalide ou en conflit
     * @throws DAOException Si une erreur survient lors de l'accès aux données
     */
    public void updateAppointment(Appointment appointment) throws IllegalArgumentException, DAOException {
        // Validation des données
        if (appointment.getId() <= 0) {
            throw new IllegalArgumentException("ID de rendez-vous invalide.");
        }
        validateAppointment(appointment);
        
        // Vérifier les conflits de rendez-vous
        if (hasAppointmentConflict(appointment.getDoctorId(), 
                                  appointment.getStartDateTime(), 
                                  appointment.getEndDateTime(), 
                                  appointment.getId())) {
            throw new IllegalArgumentException("Ce créneau n'est pas disponible pour le médecin sélectionné.");
        }
        
        // Mettre à jour le rendez-vous
        appointmentDAO.updateAppointment(appointment);
        
        // Mettre à jour le cache
        appointmentCache.put(appointment.getId(), appointment);
        
        LOGGER.info("Rendez-vous mis à jour avec succès, ID: " + appointment.getId());
    }
    
    /**
     * Supprime un rendez-vous
     * 
     * @param appointmentId L'ID du rendez-vous à supprimer
     * @throws DAOException Si une erreur survient lors de l'accès aux données
     */
    public void deleteAppointment(int appointmentId) throws DAOException {
        // Supprimer le rendez-vous
        appointmentDAO.deleteAppointment(appointmentId);
        
        // Supprimer du cache
        appointmentCache.remove(appointmentId);
        
        LOGGER.info("Rendez-vous supprimé avec succès, ID: " + appointmentId);
    }
    
    /**
     * Change le statut d'un rendez-vous
     * 
     * @param appointmentId L'ID du rendez-vous
     * @param status Le nouveau statut
     * @throws DAOException Si une erreur survient lors de l'accès aux données
     */
    public void updateAppointmentStatus(int appointmentId, AppointmentStatus status) throws DAOException {
        appointmentDAO.updateAppointmentStatus(appointmentId, status);
        
        // Mettre à jour le cache si le rendez-vous y est présent
        if (appointmentCache.containsKey(appointmentId)) {
            Appointment appointment = appointmentCache.get(appointmentId);
            appointment.setStatus(status);
            appointment.updateLastModified();
        }
        
        LOGGER.info("Statut du rendez-vous modifié avec succès, ID: " + appointmentId + ", Statut: " + status);
    }
    
    /**
     * Annule un rendez-vous
     * 
     * @param appointmentId L'ID du rendez-vous
     * @param cancelledByPatient Indique si l'annulation est initiée par le patient
     * @throws DAOException Si une erreur survient lors de l'accès aux données
     */
    public void cancelAppointment(int appointmentId, boolean cancelledByPatient) throws DAOException {
        AppointmentStatus status = cancelledByPatient ? 
                                 AppointmentStatus.ANNULE_PATIENT : 
                                 AppointmentStatus.ANNULE_CABINET;
        
        updateAppointmentStatus(appointmentId, status);
    }
    
    /**
     * Récupère un rendez-vous par son ID
     * 
     * @param appointmentId L'ID du rendez-vous
     * @return Le rendez-vous correspondant ou null si non trouvé
     * @throws DAOException Si une erreur survient lors de l'accès aux données
     */
    public Appointment getAppointmentById(int appointmentId) throws DAOException {
        // Vérifier d'abord dans le cache
        if (appointmentCache.containsKey(appointmentId)) {
            return appointmentCache.get(appointmentId);
        }
        
        // Sinon, récupérer depuis la base de données
        Appointment appointment = appointmentDAO.getAppointmentById(appointmentId);
        
        // Ajouter au cache si trouvé
        if (appointment != null) {
            appointmentCache.put(appointmentId, appointment);
        }
        
        return appointment;
    }
    
    /**
     * Récupère tous les rendez-vous d'un patient
     * 
     * @param patientId L'ID du patient
     * @return Liste des rendez-vous du patient
     * @throws DAOException Si une erreur survient lors de l'accès aux données
     */
    public List<Appointment> getAppointmentsByPatient(int patientId) throws DAOException {
        return appointmentDAO.getAppointmentsByPatient(patientId);
    }
    
    /**
     * Récupère tous les rendez-vous d'un médecin
     * 
     * @param doctorId L'ID du médecin
     * @return Liste des rendez-vous du médecin
     * @throws DAOException Si une erreur survient lors de l'accès aux données
     */
    public List<Appointment> getAppointmentsByDoctor(int doctorId) throws DAOException {
        return appointmentDAO.getAppointmentsByDoctor(doctorId);
    }
    
    /**
     * Récupère tous les rendez-vous d'un médecin pour une date donnée
     * 
     * @param doctorId L'ID du médecin
     * @param date La date des rendez-vous
     * @return Liste des rendez-vous du médecin pour la date spécifiée
     * @throws DAOException Si une erreur survient lors de l'accès aux données
     */
    public List<Appointment> getAppointmentsByDoctorAndDate(int doctorId, LocalDate date) throws DAOException {
        return appointmentDAO.getAppointmentsByDoctorAndDate(doctorId, date);
    }
    
    /**
     * Récupère tous les rendez-vous d'un médecin entre deux dates
     * 
     * @param doctorId L'ID du médecin
     * @param startDate Date de début
     * @param endDate Date de fin
     * @return Liste des rendez-vous du médecin dans la plage de dates
     * @throws DAOException Si une erreur survient lors de l'accès aux données
     */
    public List<Appointment> getAppointmentsByDoctorAndDateRange(int doctorId, LocalDate startDate, LocalDate endDate) throws DAOException {
        return appointmentDAO.getAppointmentsByDoctorAndDateRange(doctorId, startDate, endDate);
    }
    
    /**
     * Vérifie s'il existe un conflit de rendez-vous pour un médecin
     * 
     * @param doctorId L'ID du médecin
     * @param startDateTime Date et heure de début du rendez-vous
     * @param endDateTime Date et heure de fin du rendez-vous
     * @param excludeAppointmentId ID d'un rendez-vous à exclure de la vérification
     * @return true s'il y a un conflit, false sinon
     * @throws DAOException Si une erreur survient lors de l'accès aux données
     */
    public boolean hasAppointmentConflict(int doctorId, LocalDateTime startDateTime, 
                                        LocalDateTime endDateTime, int excludeAppointmentId) throws DAOException {
        return appointmentDAO.hasAppointmentConflict(doctorId, startDateTime, endDateTime, excludeAppointmentId);
    }
    
    /**
     * Récupère tous les rendez-vous pour une date donnée
     * 
     * @param date La date des rendez-vous
     * @return Liste des rendez-vous pour la date spécifiée
     * @throws DAOException Si une erreur survient lors de l'accès aux données
     */
    public List<Appointment> getAppointmentsByDate(LocalDate date) throws DAOException {
        return appointmentDAO.getAppointmentsByDate(date);
    }
    
    /**
     * Récupère tous les rendez-vous avec un statut spécifique
     * 
     * @param status Le statut des rendez-vous à récupérer
     * @return Liste des rendez-vous ayant le statut spécifié
     * @throws DAOException Si une erreur survient lors de l'accès aux données
     */
    public List<Appointment> getAppointmentsByStatus(AppointmentStatus status) throws DAOException {
        return appointmentDAO.getAppointmentsByStatus(status);
    }
    
    /**
     * Récupère les prochains rendez-vous d'un patient
     * 
     * @param patientId L'ID du patient
     * @param limit Le nombre maximum de rendez-vous à récupérer
     * @return Liste des prochains rendez-vous du patient
     * @throws DAOException Si une erreur survient lors de l'accès aux données
     */
    public List<Appointment> getUpcomingAppointmentsByPatient(int patientId, int limit) throws DAOException {
        return appointmentDAO.getUpcomingAppointmentsByPatient(patientId, limit);
    }
    
    /**
     * Récupère les prochains rendez-vous d'un médecin
     * 
     * @param doctorId L'ID du médecin
     * @param limit Le nombre maximum de rendez-vous à récupérer
     * @return Liste des prochains rendez-vous du médecin
     * @throws DAOException Si une erreur survient lors de l'accès aux données
     */
    public List<Appointment> getUpcomingAppointmentsByDoctor(int doctorId, int limit) throws DAOException {
        return appointmentDAO.getUpcomingAppointmentsByDoctor(doctorId, limit);
    }
    
    /**
     * Crée un nouveau rendez-vous avec tous les détails
     * 
     * @param patient Le patient
     * @param doctor Le médecin
     * @param secretary La secrétaire qui crée le rendez-vous
     * @param startDateTime Date et heure de début
     * @param durationMinutes Durée en minutes
     * @param appointmentType Type de consultation
     * @param notes Notes spécifiques
     * @return Le rendez-vous créé
     * @throws IllegalArgumentException Si les données sont invalides ou en conflit
     * @throws DAOException Si une erreur survient lors de l'accès aux données
     */
    public Appointment createAppointment(Patient patient, Doctor doctor, Secretary secretary,
                                      LocalDateTime startDateTime, int durationMinutes,
                                      String appointmentType, String notes) 
            throws IllegalArgumentException, DAOException {
        
        if (patient == null || doctor == null || secretary == null || startDateTime == null) {
            throw new IllegalArgumentException("Patient, médecin, secrétaire et date/heure ne peuvent pas être null.");
        }
        
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setPatientId(patient.getId());
        appointment.setDoctor(doctor);
        appointment.setDoctorId(doctor.getId());
        appointment.setSecretary(secretary);
        appointment.setSecretaryId(secretary.getId());
        appointment.setStartDateTime(startDateTime);
        appointment.setDurationMinutes(durationMinutes);
        appointment.setAppointmentType(appointmentType);
        appointment.setNotes(notes);
        appointment.setStatus(AppointmentStatus.PLANIFIE);
        
        int appointmentId = addAppointment(appointment);
        appointment.setId(appointmentId);
        
        return appointment;
    }
    
    /**
     * Recherche avancée de rendez-vous selon plusieurs critères
     * 
     * @param criteria Critères de recherche
     * @return Liste des rendez-vous correspondants
     * @throws DAOException Si une erreur survient lors de l'accès aux données
     */
    public List<Appointment> searchAppointmentsByCriteria(Map<String, Object> criteria) throws DAOException {
        return appointmentDAO.searchAppointmentsByCriteria(criteria);
    }
    
    /**
     * Valide les données d'un rendez-vous
     * 
     * @param appointment Le rendez-vous à valider
     * @throws IllegalArgumentException Si les données du rendez-vous sont invalides
     */
    private void validateAppointment(Appointment appointment) throws IllegalArgumentException {
        if (appointment == null) {
            throw new IllegalArgumentException("Le rendez-vous ne peut pas être null.");
        }
        
        if (appointment.getPatientId() <= 0) {
            throw new IllegalArgumentException("Patient invalide.");
        }
        
        if (appointment.getDoctorId() <= 0) {
            throw new IllegalArgumentException("Médecin invalide.");
        }
        
        if (appointment.getSecretaryId() <= 0) {
            throw new IllegalArgumentException("Secrétaire invalide.");
        }
        
        if (appointment.getStartDateTime() == null) {
            throw new IllegalArgumentException("Date et heure de début invalides.");
        }
        
        if (appointment.getDurationMinutes() <= 0) {
            throw new IllegalArgumentException("Durée du rendez-vous invalide.");
        }
        
        // Vérifier que la date n'est pas dans le passé
        if (appointment.getStartDateTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("La date du rendez-vous ne peut pas être dans le passé.");
        }
        
        // Autres validations peuvent être ajoutées selon les besoins
    }
    
    /**
     * Vide le cache des rendez-vous
     */
    public void clearCache() {
        appointmentCache.clear();
        LOGGER.info("Cache des rendez-vous vidé");
    }
}