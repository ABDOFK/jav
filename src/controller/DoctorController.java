package controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import dao.DAOException;
import dao.UserDAO;
import dao.UserDAOImpl;
import model.Appointment;
import model.Doctor;

/**
 * Contrôleur pour la gestion des médecins.
 * Implémente le pattern Singleton pour assurer une seule instance.
 */
public class DoctorController {
    
    private static final Logger LOGGER = Logger.getLogger(DoctorController.class.getName());
    
    // Instance unique (Singleton)
    private static DoctorController instance;
    
    // DAO pour les opérations sur les utilisateurs (dont les médecins)
    private final UserDAO userDAO;
    
    // Cache des médecins récemment consultés (optimisation)
    private final Map<Integer, Doctor> doctorCache;
    
    /**
     * Constructeur privé (Singleton)
     */
    private DoctorController() {
        this.userDAO = new UserDAOImpl();
        this.doctorCache = new HashMap<>();
    }
    
    /**
     * Obtient l'instance unique du contrôleur de médecins
     * 
     * @return L'instance de DoctorController
     */
    public static synchronized DoctorController getInstance() {
        if (instance == null) {
            instance = new DoctorController();
        }
        return instance;
    }
    
    /**
     * Ajoute un nouveau médecin
     * 
     * @param doctor Le médecin à ajouter
     * @return L'ID du médecin créé
     * @throws IllegalArgumentException Si le médecin est invalide
     * @throws DAOException Si une erreur survient lors de l'accès aux données
     */
    public int addDoctor(Doctor doctor) throws IllegalArgumentException, DAOException {
        // Validation des données
        validateDoctor(doctor);
        
        // Vérifier si le nom d'utilisateur existe déjà
        if (userDAO.isUsernameExists(doctor.getUsername())) {
            throw new IllegalArgumentException("Ce nom d'utilisateur existe déjà.");
        }
        
        // Ajouter le médecin
        int doctorId = userDAO.addDoctor(doctor);
        doctor.setId(doctorId);
        
        // Ajouter au cache
        doctorCache.put(doctorId, doctor);
        
        LOGGER.info("Médecin ajouté avec succès, ID: " + doctorId);
        return doctorId;
    }
    
    /**
     * Met à jour les informations d'un médecin existant
     * 
     * @param doctor Le médecin avec les données mises à jour
     * @throws IllegalArgumentException Si le médecin est invalide
     * @throws DAOException Si une erreur survient lors de l'accès aux données
     */
    public void updateDoctor(Doctor doctor) throws IllegalArgumentException, DAOException {
        // Validation des données
        if (doctor.getId() <= 0) {
            throw new IllegalArgumentException("ID de médecin invalide.");
        }
        validateDoctor(doctor);
        
        // Vérifier si le nom d'utilisateur a changé et s'il existe déjà
        Doctor existingDoctor = userDAO.getDoctorById(doctor.getId());
        if (existingDoctor != null && 
            !existingDoctor.getUsername().equals(doctor.getUsername()) && 
            userDAO.isUsernameExists(doctor.getUsername())) {
            throw new IllegalArgumentException("Ce nom d'utilisateur existe déjà.");
        }
        
        // Mettre à jour le médecin
        userDAO.updateDoctor(doctor);
        
        // Mettre à jour le cache
        doctorCache.put(doctor.getId(), doctor);
        
        LOGGER.info("Médecin mis à jour avec succès, ID: " + doctor.getId());
    }
    
    /**
     * Active ou désactive un compte médecin
     * 
     * @param doctorId L'ID du médecin
     * @param active Le nouvel état du compte (actif/inactif)
     * @throws DAOException Si une erreur survient lors de l'accès aux données
     */
    public void setDoctorActive(int doctorId, boolean active) throws DAOException {
        userDAO.setUserActive(doctorId, active);
        
        // Mettre à jour le cache si le médecin y est présent
        if (doctorCache.containsKey(doctorId)) {
            Doctor doctor = doctorCache.get(doctorId);
            doctor.setActive(active);
        }
        
        LOGGER.info("Statut du médecin modifié avec succès, ID: " + doctorId + ", Actif: " + active);
    }
    
    /**
     * Récupère un médecin par son ID
     * 
     * @param doctorId L'ID du médecin
     * @return Le médecin correspondant ou null si non trouvé
     * @throws DAOException Si une erreur survient lors de l'accès aux données
     */
    public Doctor getDoctorById(int doctorId) throws DAOException {
        // Vérifier d'abord dans le cache
        if (doctorCache.containsKey(doctorId)) {
            return doctorCache.get(doctorId);
        }
        
        // Sinon, récupérer depuis la base de données
        Doctor doctor = userDAO.getDoctorById(doctorId);
        
        // Ajouter au cache si trouvé
        if (doctor != null) {
            doctorCache.put(doctorId, doctor);
        }
        
        return doctor;
    }
    
    /**
     * Récupère tous les médecins
     * 
     * @return Liste de tous les médecins
     * @throws DAOException Si une erreur survient lors de l'accès aux données
     */
    public List<Doctor> getAllDoctors() throws DAOException {
        return userDAO.getAllDoctors();
    }
    
    /**
     * Récupère les médecins par spécialité
     * 
     * @param specialty La spécialité à rechercher
     * @return Liste des médecins ayant cette spécialité
     * @throws DAOException Si une erreur survient lors de l'accès aux données
     */
    public List<Doctor> getDoctorsBySpecialty(String specialty) throws DAOException {
        return userDAO.getDoctorsBySpecialty(specialty);
    }
    
    /**
     * Récupère toutes les spécialités médicales disponibles
     * 
     * @return Liste des spécialités uniques
     * @throws DAOException Si une erreur survient lors de l'accès aux données
     */
    public List<String> getAllSpecialties() throws DAOException {
        return userDAO.getAllSpecialties();
    }
    
    /**
     * Récupère le planning d'un médecin pour une date donnée
     * 
     * @param doctorId L'ID du médecin
     * @param date La date du planning
     * @return Liste des rendez-vous du médecin pour la date spécifiée
     * @throws DAOException Si une erreur survient lors de l'accès aux données
     */
    public List<Appointment> getDoctorDailyPlanning(int doctorId, LocalDate date) throws DAOException {
        AppointmentController appointmentController = AppointmentController.getInstance();
        return appointmentController.getAppointmentsByDoctorAndDate(doctorId, date);
    }
    
    /**
     * Récupère le planning hebdomadaire d'un médecin
     * 
     * @param doctorId L'ID du médecin
     * @param weekStartDate La date de début de la semaine
     * @return Liste des rendez-vous du médecin pour la semaine
     * @throws DAOException Si une erreur survient lors de l'accès aux données
     */
    public List<Appointment> getDoctorWeeklyPlanning(int doctorId, LocalDate weekStartDate) throws DAOException {
        LocalDate weekEndDate = weekStartDate.plusDays(6);
        AppointmentController appointmentController = AppointmentController.getInstance();
        return appointmentController.getAppointmentsByDoctorAndDateRange(doctorId, weekStartDate, weekEndDate);
    }
    
    /**
     * Vérifie la disponibilité d'un médecin pour un créneau horaire
     * 
     * @param doctorId L'ID du médecin
     * @param startDateTime Date et heure de début du créneau
     * @param durationMinutes Durée du créneau en minutes
     * @return true si le médecin est disponible, false sinon
     * @throws DAOException Si une erreur survient lors de l'accès aux données
     */
    public boolean isDoctorAvailable(int doctorId, LocalDateTime startDateTime, int durationMinutes) throws DAOException {
        return isDoctorAvailable(doctorId, startDateTime, durationMinutes, 0);
    }
    
    /**
     * Vérifie la disponibilité d'un médecin pour un créneau horaire (pour modification de RDV)
     * 
     * @param doctorId L'ID du médecin
     * @param startDateTime Date et heure de début du créneau
     * @param durationMinutes Durée du créneau en minutes
     * @param excludeAppointmentId ID d'un rendez-vous à exclure de la vérification
     * @return true si le médecin est disponible, false sinon
     * @throws DAOException Si une erreur survient lors de l'accès aux données
     */
    public boolean isDoctorAvailable(int doctorId, LocalDateTime startDateTime, 
                                   int durationMinutes, int excludeAppointmentId) throws DAOException {
        LocalDateTime endDateTime = startDateTime.plusMinutes(durationMinutes);
        
        AppointmentController appointmentController = AppointmentController.getInstance();
        return !appointmentController.hasAppointmentConflict(doctorId, startDateTime, endDateTime, excludeAppointmentId);
    }
    
    /**
     * Exporte le planning d'un médecin en PDF
     * 
     * @param doctorId L'ID du médecin
     * @param date La date du planning
     * @param isWeekly Indique s'il s'agit d'un planning hebdomadaire
     * @return Le chemin du fichier PDF généré
     * @throws DAOException Si une erreur survient lors de l'accès aux données
     * @throws IOException Si une erreur survient lors de la génération du PDF
     */
    public String exportDoctorPlanning(int doctorId, LocalDate date, boolean isWeekly) throws DAOException, java.io.IOException {
        Doctor doctor = getDoctorById(doctorId);
        if (doctor == null) {
            throw new IllegalArgumentException("Médecin non trouvé.");
        }
        
        List<Appointment> appointments;
        PlanningController planningController = PlanningController.getInstance();
        
        if (isWeekly) {
            return planningController.exportWeeklyPlanning(doctor, date);
        } else {
            return planningController.exportDailyPlanning(doctor, date);
        }
    }
    
    /**
     * Valide les données d'un médecin
     * 
     * @param doctor Le médecin à valider
     * @throws IllegalArgumentException Si les données du médecin sont invalides
     */
    private void validateDoctor(Doctor doctor) throws IllegalArgumentException {
        if (doctor == null) {
            throw new IllegalArgumentException("Le médecin ne peut pas être null.");
        }
        
        if (doctor.getUsername() == null || doctor.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom d'utilisateur est obligatoire.");
        }
        
        if (doctor.getPasswordHash() == null || doctor.getPasswordHash().trim().isEmpty()) {
            throw new IllegalArgumentException("Le mot de passe est obligatoire.");
        }
        
        if (doctor.getFullName() == null || doctor.getFullName().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom complet du médecin est obligatoire.");
        }
        
        if (doctor.getSpecialty() == null || doctor.getSpecialty().trim().isEmpty()) {
            throw new IllegalArgumentException("La spécialité du médecin est obligatoire.");
        }
        
        // Autres validations peuvent être ajoutées selon les besoins
    }
    
    /**
     * Vide le cache des médecins
     */
    public void clearCache() {
        doctorCache.clear();
        LOGGER.info("Cache des médecins vidé");
    }
}