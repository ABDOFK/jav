package controller;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import config.AppConfig;
import dao.DAOException;
import model.Appointment;
import model.Doctor;
import util.DateTimeUtils;
import util.PDFExporter;

/**
 * Contrôleur pour la gestion des plannings.
 * Implémente le pattern Singleton pour assurer une seule instance.
 */
public class PlanningController {
    
    private static final Logger LOGGER = Logger.getLogger(PlanningController.class.getName());
    
    // Instance unique (Singleton)
    private static PlanningController instance;
    
    // Référence aux autres contrôleurs
    private final AppointmentController appointmentController;
    private final DoctorController doctorController;
    
    /**
     * Constructeur privé (Singleton)
     */
    private PlanningController() {
        this.appointmentController = AppointmentController.getInstance();
        this.doctorController = DoctorController.getInstance();
        
        // Créer le répertoire d'export si nécessaire
        File exportDir = new File(AppConfig.EXPORT_DIRECTORY);
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }
    }
    
    /**
     * Obtient l'instance unique du contrôleur de plannings
     * 
     * @return L'instance de PlanningController
     */
    public static synchronized PlanningController getInstance() {
        if (instance == null) {
            instance = new PlanningController();
        }
        return instance;
    }
    
    /**
     * Exporte le planning journalier d'un médecin en PDF
     * 
     * @param doctor Le médecin
     * @param date La date du planning
     * @return Le chemin du fichier PDF généré
     * @throws DAOException Si une erreur survient lors de l'accès aux données
     * @throws IOException Si une erreur survient lors de la génération du PDF
     */
    public String exportDailyPlanning(Doctor doctor, LocalDate date) throws DAOException, IOException {
        // Récupérer les rendez-vous du médecin pour la date spécifiée
        List<Appointment> appointments = appointmentController.getAppointmentsByDoctorAndDate(doctor.getId(), date);
        
        // Générer le chemin du fichier PDF
        String outputFilePath = PDFExporter.generatePlanningFilePath(doctor, date, false);
        
        // Exporter le planning en PDF
        return PDFExporter.exportDailyPlanningToPDF(doctor, date, appointments, outputFilePath);
    }
    
    /**
     * Exporte le planning hebdomadaire d'un médecin en PDF
     * 
     * @param doctor Le médecin
     * @param date Une date dans la semaine
     * @return Le chemin du fichier PDF généré
     * @throws DAOException Si une erreur survient lors de l'accès aux données
     * @throws IOException Si une erreur survient lors de la génération du PDF
     */
    public String exportWeeklyPlanning(Doctor doctor, LocalDate date) throws DAOException, IOException {
        // Obtenir le premier jour de la semaine
        LocalDate weekStartDate = DateTimeUtils.getFirstDayOfWeek(date);
        LocalDate weekEndDate = weekStartDate.plusDays(6);
        
        // Récupérer les rendez-vous du médecin pour la semaine
        List<Appointment> appointments = appointmentController.getAppointmentsByDoctorAndDateRange(
                doctor.getId(), weekStartDate, weekEndDate);
        
        // Générer le chemin du fichier PDF
        String outputFilePath = PDFExporter.generatePlanningFilePath(doctor, weekStartDate, true);
        
        // Exporter le planning en PDF
        return PDFExporter.exportWeeklyPlanningToPDF(doctor, weekStartDate, appointments, outputFilePath);
    }
    
    /**
     * Compte le nombre de rendez-vous pour un médecin et une date donnée
     * 
     * @param doctorId L'ID du médecin
     * @param date La date des rendez-vous
     * @return Le nombre de rendez-vous
     * @throws DAOException Si une erreur survient lors de l'accès aux données
     */
    public int countAppointmentsByDoctorAndDate(int doctorId, LocalDate date) throws DAOException {
        List<Appointment> appointments = appointmentController.getAppointmentsByDoctorAndDate(doctorId, date);
        return appointments.size();
    }
    
    /**
     * Vérifie si un médecin a des rendez-vous pour une date donnée
     * 
     * @param doctorId L'ID du médecin
     * @param date La date à vérifier
     * @return true si le médecin a des rendez-vous, false sinon
     * @throws DAOException Si une erreur survient lors de l'accès aux données
     */
    public boolean hasDoctorAppointmentsForDate(int doctorId, LocalDate date) throws DAOException {
        return countAppointmentsByDoctorAndDate(doctorId, date) > 0;
    }
    
    /**
     * Obtient la liste des jours avec des rendez-vous pour un médecin et un mois donné
     * 
     * @param doctorId L'ID du médecin
     * @param year L'année
     * @param month Le mois (1-12)
     * @return Liste des jours du mois ayant des rendez-vous
     * @throws DAOException Si une erreur survient lors de l'accès aux données
     */
    public List<Integer> getDaysWithAppointmentsForMonth(int doctorId, int year, int month) throws DAOException {
        // Cette méthode pourrait être implémentée mais nécessiterait une requête SQL spécifique
        // ou une analyse des rendez-vous du mois pour extraire les jours
        throw new UnsupportedOperationException("Méthode non implémentée");
    }
}