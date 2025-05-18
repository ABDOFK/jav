package controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import dao.DAOException;
import dao.PatientDAO;
import dao.PatientDAOImpl;
import model.Appointment;
import model.Patient;

/**
 * Contrôleur pour la gestion des patients.
 * Implémente le pattern Singleton pour assurer une seule instance.
 */
public class PatientController {
    
    private static final Logger LOGGER = Logger.getLogger(PatientController.class.getName());
    
    // Instance unique (Singleton)
    private static PatientController instance;
    
    // DAO pour les opérations sur les patients
    private final PatientDAO patientDAO;
    
    // Cache des patients récemment consultés (optimisation)
    private final Map<Integer, Patient> patientCache;
    
    /**
     * Constructeur privé (Singleton)
     */
    private PatientController() {
        this.patientDAO = new PatientDAOImpl();
        this.patientCache = new HashMap<>();
    }
    
    /**
     * Obtient l'instance unique du contrôleur de patients
     * 
     * @return L'instance de PatientController
     */
    public static synchronized PatientController getInstance() {
        if (instance == null) {
            instance = new PatientController();
        }
        return instance;
    }
    
    /**
     * Ajoute un nouveau patient
     * 
     * @param patient Le patient à ajouter
     * @return L'ID du patient créé
     * @throws IllegalArgumentException Si le patient est invalide
     * @throws DAOException Si une erreur survient lors de l'accès aux données
     */
    public int addPatient(Patient patient) throws IllegalArgumentException, DAOException {
        // Validation des données
        validatePatient(patient);
        
        // Vérifier si le patient existe déjà
        if (patientDAO.isPatientExists(patient)) {
            throw new IllegalArgumentException("Un patient avec ce nom et cette date de naissance existe déjà.");
        }
        
        // Ajouter le patient
        int patientId = patientDAO.addPatient(patient);
        patient.setId(patientId);
        
        // Ajouter au cache
        patientCache.put(patientId, patient);
        
        LOGGER.info("Patient ajouté avec succès, ID: " + patientId);
        return patientId;
    }
    
    /**
     * Met à jour les informations d'un patient existant
     * 
     * @param patient Le patient avec les données mises à jour
     * @throws IllegalArgumentException Si le patient est invalide
     * @throws DAOException Si une erreur survient lors de l'accès aux données
     */
    public void updatePatient(Patient patient) throws IllegalArgumentException, DAOException {
        // Validation des données
        if (patient.getId() <= 0) {
            throw new IllegalArgumentException("ID de patient invalide.");
        }
        validatePatient(patient);
        
        // Mettre à jour le patient
        patientDAO.updatePatient(patient);
        
        // Mettre à jour le cache
        patientCache.put(patient.getId(), patient);
        
        LOGGER.info("Patient mis à jour avec succès, ID: " + patient.getId());
    }
    
    /**
     * Supprime un patient
     * 
     * @param patientId L'ID du patient à supprimer
     * @throws DAOException Si une erreur survient lors de l'accès aux données
     */
    public void deletePatient(int patientId) throws DAOException {
        // Supprimer le patient
        patientDAO.deletePatient(patientId);
        
        // Supprimer du cache
        patientCache.remove(patientId);
        
        LOGGER.info("Patient supprimé avec succès, ID: " + patientId);
    }
    
    /**
     * Récupère un patient par son ID
     * 
     * @param patientId L'ID du patient
     * @return Le patient correspondant ou null si non trouvé
     * @throws DAOException Si une erreur survient lors de l'accès aux données
     */
    public Patient getPatientById(int patientId) throws DAOException {
        // Vérifier d'abord dans le cache
        if (patientCache.containsKey(patientId)) {
            return patientCache.get(patientId);
        }
        
        // Sinon, récupérer depuis la base de données
        Patient patient = patientDAO.getPatientById(patientId);
        
        // Ajouter au cache si trouvé
        if (patient != null) {
            patientCache.put(patientId, patient);
        }
        
        return patient;
    }
    
    /**
     * Récupère tous les patients
     * 
     * @return Liste de tous les patients
     * @throws DAOException Si une erreur survient lors de l'accès aux données
     */
    public List<Patient> getAllPatients() throws DAOException {
        return patientDAO.getAllPatients();
    }
    
    /**
     * Recherche des patients par nom et/ou prénom
     * 
     * @param lastName Le nom de famille (peut être null)
     * @param firstName Le prénom (peut être null)
     * @return Liste des patients correspondants
     * @throws DAOException Si une erreur survient lors de l'accès aux données
     */
    public List<Patient> searchPatientsByName(String lastName, String firstName) throws DAOException {
        return patientDAO.searchPatientsByName(lastName, firstName);
    }
    
    /**
     * Recherche des patients par numéro de téléphone
     * 
     * @param phone Le numéro de téléphone à rechercher
     * @return Liste des patients correspondants
     * @throws DAOException Si une erreur survient lors de l'accès aux données
     */
    public List<Patient> searchPatientsByPhone(String phone) throws DAOException {
        return patientDAO.searchPatientsByPhone(phone);
    }
    
    /**
     * Recherche avancée de patients selon plusieurs critères
     * 
     * @param lastName Le nom de famille (peut être null)
     * @param firstName Le prénom (peut être null)
     * @param phone Le numéro de téléphone (peut être null)
     * @param email L'adresse email (peut être null)
     * @param minAge L'âge minimum (peut être 0)
     * @param maxAge L'âge maximum (peut être 0)
     * @return Liste des patients correspondants
     * @throws DAOException Si une erreur survient lors de l'accès aux données
     */
    public List<Patient> searchPatientsByCriteria(String lastName, String firstName, 
                                               String phone, String email, 
                                               int minAge, int maxAge) throws DAOException {
        
        Map<String, Object> criteria = new HashMap<>();
        
        if (lastName != null && !lastName.trim().isEmpty()) {
            criteria.put("lastName", lastName.trim());
        }
        
        if (firstName != null && !firstName.trim().isEmpty()) {
            criteria.put("firstName", firstName.trim());
        }
        
        if (phone != null && !phone.trim().isEmpty()) {
            criteria.put("phone", phone.trim());
        }
        
        if (email != null && !email.trim().isEmpty()) {
            criteria.put("email", email.trim());
        }
        
        // Convertir l'âge en dates de naissance
        if (minAge > 0) {
            LocalDate maxBirthDate = LocalDate.now().minusYears(minAge);
            criteria.put("birthDateMax", maxBirthDate);
        }
        
        if (maxAge > 0) {
            LocalDate minBirthDate = LocalDate.now().minusYears(maxAge + 1);
            criteria.put("birthDateMin", minBirthDate);
        }
        
        return patientDAO.searchPatientsByCriteria(criteria);
    }
    
    /**
     * Compte le nombre total de patients
     * 
     * @return Le nombre de patients
     * @throws DAOException Si une erreur survient lors de l'accès aux données
     */
    public int countPatients() throws DAOException {
        return patientDAO.countPatients();
    }
    
    /**
     * Vérifie si un patient existe déjà
     * 
     * @param patient Le patient à vérifier
     * @return true si le patient existe, false sinon
     * @throws DAOException Si une erreur survient lors de l'accès aux données
     */
    public boolean isPatientExists(Patient patient) throws DAOException {
        return patientDAO.isPatientExists(patient);
    }
    
    /**
     * Récupère l'historique des rendez-vous d'un patient
     * 
     * @param patientId L'ID du patient
     * @return Liste des rendez-vous du patient
     * @throws DAOException Si une erreur survient lors de l'accès aux données
     */
    public List<Appointment> getPatientAppointmentHistory(int patientId) throws DAOException {
        AppointmentController appointmentController = AppointmentController.getInstance();
        return appointmentController.getAppointmentsByPatient(patientId);
    }
    
    /**
     * Récupère les prochains rendez-vous d'un patient
     * 
     * @param patientId L'ID du patient
     * @param limit Le nombre maximum de rendez-vous à récupérer
     * @return Liste des prochains rendez-vous du patient
     * @throws DAOException Si une erreur survient lors de l'accès aux données
     */
    public List<Appointment> getPatientUpcomingAppointments(int patientId, int limit) throws DAOException {
        AppointmentController appointmentController = AppointmentController.getInstance();
        return appointmentController.getUpcomingAppointmentsByPatient(patientId, limit);
    }
    
    /**
     * Valide les données d'un patient
     * 
     * @param patient Le patient à valider
     * @throws IllegalArgumentException Si les données du patient sont invalides
     */
    private void validatePatient(Patient patient) throws IllegalArgumentException {
        if (patient == null) {
            throw new IllegalArgumentException("Le patient ne peut pas être null.");
        }
        
        if (patient.getLastName() == null || patient.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom du patient est obligatoire.");
        }
        
        if (patient.getFirstName() == null || patient.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("Le prénom du patient est obligatoire.");
        }
        
        if (patient.getPhone() == null || patient.getPhone().trim().isEmpty()) {
            throw new IllegalArgumentException("Le numéro de téléphone du patient est obligatoire.");
        }
        
        // Validation de l'email si présent
        if (patient.hasEmail() && !isValidEmail(patient.getEmail())) {
            throw new IllegalArgumentException("L'adresse email n'est pas valide.");
        }
        
        // Autres validations peuvent être ajoutées selon les besoins
    }
    
    /**
     * Vérifie si une adresse email est valide
     * 
     * @param email L'adresse email à vérifier
     * @return true si l'email est valide, false sinon
     */
    private boolean isValidEmail(String email) {
        // Validation simple
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
    
    /**
     * Vide le cache des patients
     */
    public void clearCache() {
        patientCache.clear();
        LOGGER.info("Cache des patients vidé");
    }
}