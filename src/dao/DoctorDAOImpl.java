package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import config.AppConfig;
import config.DatabaseConnection;
import model.Appointment;
import model.Doctor;
import util.DateTimeUtils;

/**
 * Implémentation MySQL de l'interface DoctorDAO.
 * Gère la persistance des médecins dans une base de données MySQL.
 * Cette classe délègue la plupart des opérations à UserDAOImpl.
 */
public class DoctorDAOImpl implements DoctorDAO {
    
    private static final Logger LOGGER = Logger.getLogger(DoctorDAOImpl.class.getName());
    
    // Requêtes SQL spécifiques aux médecins
    private static final String SQL_GET_AVAILABLE_DOCTORS = 
            "SELECT u.*, m.specialite, m.horaires_disponibilite, m.telephone_professionnel " +
            "FROM utilisateurs u " +
            "JOIN medecins m ON u.id_utilisateur = m.id_medecin " +
            "WHERE u.role = 'MEDECIN' AND u.actif = TRUE " +
            "AND NOT EXISTS (" +
            "    SELECT 1 FROM rendez_vous rv " +
            "    WHERE rv.id_medecin_fk = u.id_utilisateur " +
            "    AND rv.statut_rdv NOT IN ('ANNULE_PATIENT', 'ANNULE_CABINET') " +
            "    AND (? BETWEEN rv.date_heure_debut AND DATE_ADD(rv.date_heure_debut, INTERVAL rv.duree_minutes MINUTE) " +
            "    OR DATE_ADD(?, INTERVAL ? MINUTE) BETWEEN rv.date_heure_debut AND DATE_ADD(rv.date_heure_debut, INTERVAL rv.duree_minutes MINUTE)) " +
            ") " +
            "ORDER BY u.nom_complet";
    
    // Référence à la connexion à la base de données
    private final DatabaseConnection dbConnection;
    
    // Référence aux autres DAO pour les opérations communes
    private final UserDAO userDAO;
    private final AppointmentDAO appointmentDAO;
    
    /**
     * Constructeur par défaut.
     * Récupère l'instance singleton de la connexion à la base de données.
     */
    public DoctorDAOImpl() {
        this.dbConnection = DatabaseConnection.getInstance();
        this.userDAO = new UserDAOImpl();
        this.appointmentDAO = new AppointmentDAOImpl();
    }
    
    @Override
    public int addDoctor(Doctor doctor) throws DAOException {
        return userDAO.addDoctor(doctor);
    }
    
    @Override
    public void updateDoctor(Doctor doctor) throws DAOException {
        userDAO.updateDoctor(doctor);
    }
    
    @Override
    public Doctor getDoctorById(int id) throws DAOException {
        return userDAO.getDoctorById(id);
    }
    
    @Override
    public List<Doctor> getAllDoctors() throws DAOException {
        return userDAO.getAllDoctors();
    }
    
    @Override
    public List<Doctor> getDoctorsBySpecialty(String specialty) throws DAOException {
        return userDAO.getDoctorsBySpecialty(specialty);
    }
    
    @Override
    public void setDoctorActive(int doctorId, boolean active) throws DAOException {
        userDAO.setUserActive(doctorId, active);
    }
    
    @Override
    public List<String> getAllSpecialties() throws DAOException {
        return userDAO.getAllSpecialties();
    }
    
    @Override
    public List<Doctor> getAvailableDoctors(LocalDateTime dateTime) throws DAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(SQL_GET_AVAILABLE_DOCTORS);
            
            // Par défaut, on considère une durée de rendez-vous de 30 minutes
            int defaultDuration = 30;
            
            // Paramètres pour vérifier la disponibilité
            stmt.setTimestamp(1, Timestamp.valueOf(dateTime));
            stmt.setTimestamp(2, Timestamp.valueOf(dateTime));
            stmt.setInt(3, defaultDuration);
            
            rs = stmt.executeQuery();
            
            List<Doctor> availableDoctors = new ArrayList<>();
            while (rs.next()) {
                Doctor doctor = createDoctorFromResultSet(rs);
                
                // Vérifier également les horaires de disponibilité du médecin
                if (isDoctorWorkingHours(doctor, dateTime)) {
                    availableDoctors.add(doctor);
                }
            }
            
            return availableDoctors;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération des médecins disponibles", e);
            throw new DAOException("Erreur lors de la récupération des médecins disponibles: " + e.getMessage(), 
                                e, DAOException.RETRIEVAL_ERROR);
        } finally {
            // Fermer les ressources
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Erreur lors de la fermeture des ressources", e);
            }
        }
    }
    
    @Override
    public List<String> getAvailableTimeSlots(int doctorId, LocalDate date) throws DAOException {
        try {
            // Récupérer le médecin
            Doctor doctor = getDoctorById(doctorId);
            if (doctor == null) {
                throw new DAOException("Médecin non trouvé avec l'ID: " + doctorId, DAOException.RETRIEVAL_ERROR);
            }
            
            // Récupérer les horaires de travail du médecin pour ce jour
            Map<String, List<String>> workHours = doctor.getStructuredWorkHours();
            String dayOfWeek = date.getDayOfWeek().toString().toLowerCase();
            
            if (!workHours.containsKey(dayOfWeek)) {
                // Le médecin ne travaille pas ce jour-là
                return new ArrayList<>();
            }
            
            // Récupérer les plages horaires de travail de ce jour
            List<String> dayWorkHours = workHours.get(dayOfWeek);
            List<String> availableSlots = new ArrayList<>();
            
            // Pour chaque plage horaire de travail
            for (String workSlot : dayWorkHours) {
                String[] parts = workSlot.split("-");
                if (parts.length == 2) {
                    String startStr = parts[0].trim();
                    String endStr = parts[1].trim();
                    
                    // Générer les créneaux disponibles avec un intervalle de 15 minutes
                    List<String> slots = DateTimeUtils.generateTimeSlots(startStr, endStr, 15);
                    
                    // Récupérer les rendez-vous du médecin pour cette date
                    List<Appointment> appointments = appointmentDAO.getAppointmentsByDoctorAndDate(doctorId, date);
                    
                    // Filtrer les créneaux déjà pris
                    for (String slot : slots) {
                        LocalTime slotTime = LocalTime.parse(slot, DateTimeFormatter.ofPattern(AppConfig.TIME_FORMAT));
                        LocalDateTime slotDateTime = LocalDateTime.of(date, slotTime);
                        
                        boolean isAvailable = true;
                        for (Appointment appointment : appointments) {
                            LocalDateTime appointmentStart = appointment.getStartDateTime();
                            LocalDateTime appointmentEnd = appointment.getEndDateTime();
                            
                            // Vérifier si le créneau est occupé
                            if ((slotDateTime.isEqual(appointmentStart) || slotDateTime.isAfter(appointmentStart)) 
                                && slotDateTime.isBefore(appointmentEnd)) {
                                isAvailable = false;
                                break;
                            }
                        }
                        
                        if (isAvailable) {
                            availableSlots.add(slot);
                        }
                    }
                }
            }
            
            return availableSlots;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération des créneaux disponibles", e);
            throw new DAOException("Erreur lors de la récupération des créneaux disponibles: " + e.getMessage(), 
                                e, DAOException.RETRIEVAL_ERROR);
        }
    }
    
    /**
     * Vérifie si un médecin travaille à une date et heure données
     * 
     * @param doctor Le médecin à vérifier
     * @param dateTime La date et heure à vérifier
     * @return true si le médecin travaille à ce moment, false sinon
     */
    private boolean isDoctorWorkingHours(Doctor doctor, LocalDateTime dateTime) {
        try {
            // Récupérer les horaires de travail du médecin
            Map<String, List<String>> workHours = doctor.getStructuredWorkHours();
            String dayOfWeek = dateTime.getDayOfWeek().toString().toLowerCase();
            
            if (!workHours.containsKey(dayOfWeek)) {
                // Le médecin ne travaille pas ce jour-là
                return false;
            }
            
            // Récupérer les plages horaires de travail de ce jour
            List<String> dayWorkHours = workHours.get(dayOfWeek);
            LocalTime checkTime = dateTime.toLocalTime();
            
            // Pour chaque plage horaire de travail
            for (String workSlot : dayWorkHours) {
                String[] parts = workSlot.split("-");
                if (parts.length == 2) {
                    LocalTime startTime = LocalTime.parse(parts[0].trim(), DateTimeFormatter.ofPattern(AppConfig.TIME_FORMAT));
                    LocalTime endTime = LocalTime.parse(parts[1].trim(), DateTimeFormatter.ofPattern(AppConfig.TIME_FORMAT));
                    
                    // Vérifier si l'heure est dans cette plage
                    if ((checkTime.equals(startTime) || checkTime.isAfter(startTime)) 
                        && checkTime.isBefore(endTime)) {
                        return true;
                    }
                }
            }
            
            return false;
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erreur lors de la vérification des horaires de travail", e);
            return false;
        }
    }
    
    /**
     * Crée un objet Doctor à partir d'un ResultSet
     * 
     * @param rs Le ResultSet contenant les données du médecin
     * @return Un objet Doctor initialisé avec les données du ResultSet
     * @throws SQLException En cas d'erreur d'accès aux données du ResultSet
     */
    private Doctor createDoctorFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id_utilisateur");
        String username = rs.getString("nom_utilisateur");
        String passwordHash = rs.getString("mot_de_passe_hash");
        String fullName = rs.getString("nom_complet");
        boolean active = rs.getBoolean("actif");
        
        // Convertir Timestamp en LocalDateTime
        Timestamp creationTimestamp = rs.getTimestamp("date_creation_compte");
        LocalDateTime creationDate = creationTimestamp != null
                ? creationTimestamp.toLocalDateTime()
                : LocalDateTime.now();
        
        // Données spécifiques au médecin
        String specialty = rs.getString("specialite");
        String workHours = rs.getString("horaires_disponibilite");
        String professionalPhone = rs.getString("telephone_professionnel");
        
        return new Doctor(id, username, passwordHash, fullName, active, creationDate,
                specialty, workHours, professionalPhone);
    }
}