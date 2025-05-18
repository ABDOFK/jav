package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import config.DatabaseConnection;
import model.Appointment;
import model.AppointmentStatus;
import model.Doctor;
import model.Patient;
import model.Secretary;

/**
 * Implémentation MySQL de l'interface AppointmentDAO.
 * Gère la persistance des rendez-vous dans une base de données MySQL.
 */
public class AppointmentDAOImpl implements AppointmentDAO {
    
    private static final Logger LOGGER = Logger.getLogger(AppointmentDAOImpl.class.getName());
    
    // Requêtes SQL pour les opérations sur les rendez-vous
    private static final String SQL_INSERT_APPOINTMENT = 
            "INSERT INTO rendez_vous (id_patient_fk, id_medecin_fk, id_secretaire_creation_fk, " +
            "date_heure_debut, duree_minutes, type_consultation, statut_rdv, notes_rdv, " +
            "date_creation_rdv, date_derniere_maj_rdv) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
    private static final String SQL_UPDATE_APPOINTMENT = 
            "UPDATE rendez_vous SET id_patient_fk = ?, id_medecin_fk = ?, " +
            "date_heure_debut = ?, duree_minutes = ?, type_consultation = ?, " +
            "statut_rdv = ?, notes_rdv = ?, date_derniere_maj_rdv = ? " +
            "WHERE id_rendezvous = ?";
    
    private static final String SQL_DELETE_APPOINTMENT = 
            "DELETE FROM rendez_vous WHERE id_rendezvous = ?";
    
    private static final String SQL_UPDATE_APPOINTMENT_STATUS = 
            "UPDATE rendez_vous SET statut_rdv = ?, date_derniere_maj_rdv = ? " +
            "WHERE id_rendezvous = ?";
    
    private static final String SQL_SELECT_APPOINTMENT_BY_ID = 
            "SELECT rv.*, p.nom as patient_nom, p.prenom as patient_prenom, " +
            "u.nom_complet as medecin_nom " +
            "FROM rendez_vous rv " +
            "JOIN patients p ON rv.id_patient_fk = p.id_patient " +
            "JOIN utilisateurs u ON rv.id_medecin_fk = u.id_utilisateur " +
            "WHERE rv.id_rendezvous = ?";
    
    private static final String SQL_SELECT_APPOINTMENTS_BY_PATIENT = 
            "SELECT rv.*, p.nom as patient_nom, p.prenom as patient_prenom, " +
            "u.nom_complet as medecin_nom " +
            "FROM rendez_vous rv " +
            "JOIN patients p ON rv.id_patient_fk = p.id_patient " +
            "JOIN utilisateurs u ON rv.id_medecin_fk = u.id_utilisateur " +
            "WHERE rv.id_patient_fk = ? " +
            "ORDER BY rv.date_heure_debut";
    
    private static final String SQL_SELECT_APPOINTMENTS_BY_DOCTOR = 
            "SELECT rv.*, p.nom as patient_nom, p.prenom as patient_prenom, " +
            "u.nom_complet as medecin_nom " +
            "FROM rendez_vous rv " +
            "JOIN patients p ON rv.id_patient_fk = p.id_patient " +
            "JOIN utilisateurs u ON rv.id_medecin_fk = u.id_utilisateur " +
            "WHERE rv.id_medecin_fk = ? " +
            "ORDER BY rv.date_heure_debut";
    
    private static final String SQL_SELECT_APPOINTMENTS_BY_DOCTOR_AND_DATE = 
            "SELECT rv.*, p.nom as patient_nom, p.prenom as patient_prenom, " +
            "u.nom_complet as medecin_nom " +
            "FROM rendez_vous rv " +
            "JOIN patients p ON rv.id_patient_fk = p.id_patient " +
            "JOIN utilisateurs u ON rv.id_medecin_fk = u.id_utilisateur " +
            "WHERE rv.id_medecin_fk = ? " +
            "AND DATE(rv.date_heure_debut) = ? " +
            "ORDER BY rv.date_heure_debut";
    
    private static final String SQL_SELECT_APPOINTMENTS_BY_DOCTOR_AND_DATE_RANGE = 
            "SELECT rv.*, p.nom as patient_nom, p.prenom as patient_prenom, " +
            "u.nom_complet as medecin_nom " +
            "FROM rendez_vous rv " +
            "JOIN patients p ON rv.id_patient_fk = p.id_patient " +
            "JOIN utilisateurs u ON rv.id_medecin_fk = u.id_utilisateur " +
            "WHERE rv.id_medecin_fk = ? " +
            "AND DATE(rv.date_heure_debut) BETWEEN ? AND ? " +
            "ORDER BY rv.date_heure_debut";
    
    private static final String SQL_CHECK_APPOINTMENT_CONFLICT = 
            "SELECT COUNT(*) FROM rendez_vous " +
            "WHERE id_medecin_fk = ? " +
            "AND id_rendezvous != ? " +
            "AND statut_rdv NOT IN ('ANNULE_PATIENT', 'ANNULE_CABINET') " +
            "AND ((date_heure_debut < ? AND DATE_ADD(date_heure_debut, INTERVAL duree_minutes MINUTE) > ?) " +
            "OR (date_heure_debut >= ? AND date_heure_debut < ?))";
    
    private static final String SQL_SELECT_APPOINTMENTS_BY_DATE = 
            "SELECT rv.*, p.nom as patient_nom, p.prenom as patient_prenom, " +
            "u.nom_complet as medecin_nom " +
            "FROM rendez_vous rv " +
            "JOIN patients p ON rv.id_patient_fk = p.id_patient " +
            "JOIN utilisateurs u ON rv.id_medecin_fk = u.id_utilisateur " +
            "WHERE DATE(rv.date_heure_debut) = ? " +
            "ORDER BY rv.date_heure_debut";
    
    private static final String SQL_SELECT_APPOINTMENTS_BY_STATUS = 
            "SELECT rv.*, p.nom as patient_nom, p.prenom as patient_prenom, " +
            "u.nom_complet as medecin_nom " +
            "FROM rendez_vous rv " +
            "JOIN patients p ON rv.id_patient_fk = p.id_patient " +
            "JOIN utilisateurs u ON rv.id_medecin_fk = u.id_utilisateur " +
            "WHERE rv.statut_rdv = ? " +
            "ORDER BY rv.date_heure_debut";
    
    private static final String SQL_COUNT_APPOINTMENTS_BY_DOCTOR_AND_DATE = 
            "SELECT COUNT(*) FROM rendez_vous " +
            "WHERE id_medecin_fk = ? " +
            "AND DATE(date_heure_debut) = ? " +
            "AND statut_rdv NOT IN ('ANNULE_PATIENT', 'ANNULE_CABINET')";
    
    private static final String SQL_SELECT_UPCOMING_APPOINTMENTS_BY_PATIENT = 
            "SELECT rv.*, p.nom as patient_nom, p.prenom as patient_prenom, " +
            "u.nom_complet as medecin_nom " +
            "FROM rendez_vous rv " +
            "JOIN patients p ON rv.id_patient_fk = p.id_patient " +
            "JOIN utilisateurs u ON rv.id_medecin_fk = u.id_utilisateur " +
            "WHERE rv.id_patient_fk = ? " +
            "AND rv.date_heure_debut >= NOW() " +
            "AND rv.statut_rdv NOT IN ('ANNULE_PATIENT', 'ANNULE_CABINET', 'REALISE', 'ABSENT') " +
            "ORDER BY rv.date_heure_debut " +
            "LIMIT ?";
    
    private static final String SQL_SELECT_UPCOMING_APPOINTMENTS_BY_DOCTOR = 
            "SELECT rv.*, p.nom as patient_nom, p.prenom as patient_prenom, " +
            "u.nom_complet as medecin_nom " +
            "FROM rendez_vous rv " +
            "JOIN patients p ON rv.id_patient_fk = p.id_patient " +
            "JOIN utilisateurs u ON rv.id_medecin_fk = u.id_utilisateur " +
            "WHERE rv.id_medecin_fk = ? " +
            "AND rv.date_heure_debut >= NOW() " +
            "AND rv.statut_rdv NOT IN ('ANNULE_PATIENT', 'ANNULE_CABINET') " +
            "ORDER BY rv.date_heure_debut " +
            "LIMIT ?";
    
    // Référence aux autres DAO pour charger les objets liés
    private final PatientDAO patientDAO;
    private final UserDAO userDAO;
    
    // Référence à la connexion à la base de données
    private final DatabaseConnection dbConnection;
    
    /**
     * Constructeur par défaut.
     * Récupère l'instance singleton de la connexion à la base de données.
     */
    public AppointmentDAOImpl() {
        this.dbConnection = DatabaseConnection.getInstance();
        this.patientDAO = new PatientDAOImpl();
        this.userDAO = new UserDAOImpl();
    }
    
    /**
     * Crée un objet Appointment à partir d'un ResultSet
     * 
     * @param rs Le ResultSet contenant les données du rendez-vous
     * @return Un objet Appointment initialisé avec les données du ResultSet
     * @throws SQLException En cas d'erreur d'accès aux données du ResultSet
     */
    private Appointment createAppointmentFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id_rendezvous");
        int patientId = rs.getInt("id_patient_fk");
        int doctorId = rs.getInt("id_medecin_fk");
        int secretaryId = rs.getInt("id_secretaire_creation_fk");
        
        // Convertir Timestamp en LocalDateTime
        Timestamp startTimestamp = rs.getTimestamp("date_heure_debut");
        LocalDateTime startDateTime = startTimestamp.toLocalDateTime();
        
        int durationMinutes = rs.getInt("duree_minutes");
        String appointmentType = rs.getString("type_consultation");
        String statusString = rs.getString("statut_rdv");
        AppointmentStatus status = AppointmentStatus.fromLabel(statusString);
        String notes = rs.getString("notes_rdv");
        
        Timestamp creationTimestamp = rs.getTimestamp("date_creation_rdv");
        LocalDateTime creationDateTime = creationTimestamp.toLocalDateTime();
        
        Timestamp lastModifiedTimestamp = rs.getTimestamp("date_derniere_maj_rdv");
        LocalDateTime lastModifiedDateTime = lastModifiedTimestamp.toLocalDateTime();
        
        Appointment appointment = new Appointment(id, patientId, doctorId, secretaryId, 
                                                startDateTime, durationMinutes, appointmentType, 
                                                status, notes, creationDateTime, lastModifiedDateTime);
        
        // Si les informations patient et médecin sont incluses dans le résultat
        try {
            if (rs.getMetaData().getColumnCount() > 11) {
                String patientLastName = rs.getString("patient_nom");
                String patientFirstName = rs.getString("patient_prenom");
                String doctorName = rs.getString("medecin_nom");
                
                if (patientLastName != null && patientFirstName != null) {
                    Patient patient = new Patient();
                    patient.setId(patientId);
                    patient.setLastName(patientLastName);
                    patient.setFirstName(patientFirstName);
                    appointment.setPatient(patient);
                }
                
                if (doctorName != null) {
                    Doctor doctor = new Doctor();
                    doctor.setId(doctorId);
                    doctor.setFullName(doctorName);
                    appointment.setDoctor(doctor);
                }
            }
        } catch (SQLException e) {
            // Si les colonnes supplémentaires ne sont pas présentes, on ignore silencieusement
            LOGGER.log(Level.FINE, "Colonnes supplémentaires non disponibles pour le rendez-vous", e);
        }
        
        return appointment;
    }
    
    /**
     * Charge les objets Patient et Doctor liés à un Appointment
     * 
     * @param appointment Le rendez-vous pour lequel charger les objets liés
     * @throws DAOException En cas d'erreur lors du chargement des objets liés
     */
    private void loadRelatedObjects(Appointment appointment) throws DAOException {
        // Charger le patient
        if (appointment.getPatient() == null || appointment.getPatient().getLastName() == null) {
            Patient patient = patientDAO.getPatientById(appointment.getPatientId());
            appointment.setPatient(patient);
        }
        
        // Charger le médecin
        if (appointment.getDoctor() == null || appointment.getDoctor().getFullName() == null) {
            Doctor doctor = userDAO.getDoctorById(appointment.getDoctorId());
            appointment.setDoctor(doctor);
        }
        
        // Charger la secrétaire (si nécessaire)
        if (appointment.getSecretary() == null) {
            Secretary secretary = userDAO.getSecretaryById(appointment.getSecretaryId());
            appointment.setSecretary(secretary);
        }
    }
    
    @Override
    public int addAppointment(Appointment appointment) throws DAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;
        
        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(SQL_INSERT_APPOINTMENT, Statement.RETURN_GENERATED_KEYS);
            
            stmt.setInt(1, appointment.getPatientId());
            stmt.setInt(2, appointment.getDoctorId());
            stmt.setInt(3, appointment.getSecretaryId());
            stmt.setTimestamp(4, Timestamp.valueOf(appointment.getStartDateTime()));
            stmt.setInt(5, appointment.getDurationMinutes());
            stmt.setString(6, appointment.getAppointmentType());
            stmt.setString(7, appointment.getStatus().name());
            stmt.setString(8, appointment.getNotes());
            
            // Utiliser la date actuelle si non spécifiée
            LocalDateTime now = LocalDateTime.now();
            if (appointment.getCreationDateTime() == null) {
                appointment.setCreationDateTime(now);
            }
            if (appointment.getLastModifiedDateTime() == null) {
                appointment.setLastModifiedDateTime(now);
            }
            
            stmt.setTimestamp(9, Timestamp.valueOf(appointment.getCreationDateTime()));
            stmt.setTimestamp(10, Timestamp.valueOf(appointment.getLastModifiedDateTime()));
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected == 0) {
                throw new DAOException("L'ajout du rendez-vous a échoué, aucune ligne affectée.", 
                                    DAOException.INSERTION_ERROR);
            }
            
            // Récupérer l'ID généré
            generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int appointmentId = generatedKeys.getInt(1);
                appointment.setId(appointmentId);
                return appointmentId;
            } else {
                throw new DAOException("L'ajout du rendez-vous a échoué, aucun ID généré.", 
                                    DAOException.INSERTION_ERROR);
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'ajout du rendez-vous", e);
            throw new DAOException("Erreur lors de l'ajout du rendez-vous: " + e.getMessage(), 
                                e, DAOException.INSERTION_ERROR);
        } finally {
            // Fermer les ressources
            try {
                if (generatedKeys != null) generatedKeys.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Erreur lors de la fermeture des ressources", e);
            }
        }
    }
    
    @Override
    public void updateAppointment(Appointment appointment) throws DAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(SQL_UPDATE_APPOINTMENT);
            
            stmt.setInt(1, appointment.getPatientId());
            stmt.setInt(2, appointment.getDoctorId());
            stmt.setTimestamp(3, Timestamp.valueOf(appointment.getStartDateTime()));
            stmt.setInt(4, appointment.getDurationMinutes());
            stmt.setString(5, appointment.getAppointmentType());
            stmt.setString(6, appointment.getStatus().name());
            stmt.setString(7, appointment.getNotes());
            
            // Mettre à jour la date de dernière modification
            LocalDateTime now = LocalDateTime.now();
            appointment.setLastModifiedDateTime(now);
            stmt.setTimestamp(8, Timestamp.valueOf(now));
            
            stmt.setInt(9, appointment.getId());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected == 0) {
                throw new DAOException("La mise à jour du rendez-vous a échoué, aucune ligne affectée. ID: " + appointment.getId(), 
                                    DAOException.UPDATE_ERROR);
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la mise à jour du rendez-vous", e);
            throw new DAOException("Erreur lors de la mise à jour du rendez-vous: " + e.getMessage(), 
                                e, DAOException.UPDATE_ERROR);
        } finally {
            // Fermer les ressources
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Erreur lors de la fermeture des ressources", e);
            }
        }
    }
    
    @Override
    public void deleteAppointment(int appointmentId) throws DAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(SQL_DELETE_APPOINTMENT);
            stmt.setInt(1, appointmentId);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected == 0) {
                throw new DAOException("La suppression du rendez-vous a échoué, aucune ligne affectée. ID: " + appointmentId, 
                                    DAOException.DELETION_ERROR);
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la suppression du rendez-vous", e);
            throw new DAOException("Erreur lors de la suppression du rendez-vous: " + e.getMessage(), 
                                e, DAOException.DELETION_ERROR);
        } finally {
            // Fermer les ressources
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Erreur lors de la fermeture des ressources", e);
            }
        }
    }
    
    @Override
    public void updateAppointmentStatus(int appointmentId, AppointmentStatus status) throws DAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(SQL_UPDATE_APPOINTMENT_STATUS);
            
            stmt.setString(1, status.name());
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(3, appointmentId);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected == 0) {
                throw new DAOException("La mise à jour du statut du rendez-vous a échoué, aucune ligne affectée. ID: " + appointmentId, 
                                    DAOException.UPDATE_ERROR);
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la mise à jour du statut du rendez-vous", e);
            throw new DAOException("Erreur lors de la mise à jour du statut du rendez-vous: " + e.getMessage(), 
                                e, DAOException.UPDATE_ERROR);
        } finally {
            // Fermer les ressources
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Erreur lors de la fermeture des ressources", e);
            }
        }
    }
    
    @Override
    public Appointment getAppointmentById(int appointmentId) throws DAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(SQL_SELECT_APPOINTMENT_BY_ID);
            stmt.setInt(1, appointmentId);
            
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                Appointment appointment = createAppointmentFromResultSet(rs);
                loadRelatedObjects(appointment);
                return appointment;
            } else {
                return null; // Aucun rendez-vous trouvé
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération du rendez-vous par ID", e);
            throw new DAOException("Erreur lors de la récupération du rendez-vous: " + e.getMessage(), 
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
    public List<Appointment> getAppointmentsByPatient(int patientId) throws DAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(SQL_SELECT_APPOINTMENTS_BY_PATIENT);
            stmt.setInt(1, patientId);
            
            rs = stmt.executeQuery();
            
            List<Appointment> appointments = new ArrayList<>();
            while (rs.next()) {
                Appointment appointment = createAppointmentFromResultSet(rs);
                appointments.add(appointment);
            }
            
            // Charger les objets liés pour tous les rendez-vous
            for (Appointment appointment : appointments) {
                loadRelatedObjects(appointment);
            }
            
            return appointments;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération des rendez-vous par patient", e);
            throw new DAOException("Erreur lors de la récupération des rendez-vous: " + e.getMessage(), 
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
    public List<Appointment> getAppointmentsByDoctor(int doctorId) throws DAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(SQL_SELECT_APPOINTMENTS_BY_DOCTOR);
            stmt.setInt(1, doctorId);
            
            rs = stmt.executeQuery();
            
            List<Appointment> appointments = new ArrayList<>();
            while (rs.next()) {
                Appointment appointment = createAppointmentFromResultSet(rs);
                appointments.add(appointment);
            }
            
            // Charger les objets liés pour tous les rendez-vous
            for (Appointment appointment : appointments) {
                loadRelatedObjects(appointment);
            }
            
            return appointments;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération des rendez-vous par médecin", e);
            throw new DAOException("Erreur lors de la récupération des rendez-vous: " + e.getMessage(), 
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
    public List<Appointment> getAppointmentsByDoctorAndDate(int doctorId, LocalDate date) throws DAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(SQL_SELECT_APPOINTMENTS_BY_DOCTOR_AND_DATE);
            stmt.setInt(1, doctorId);
            stmt.setDate(2, Date.valueOf(date));
            
            rs = stmt.executeQuery();
            
            List<Appointment> appointments = new ArrayList<>();
            while (rs.next()) {
                Appointment appointment = createAppointmentFromResultSet(rs);
                appointments.add(appointment);
            }
            
            // Charger les objets liés pour tous les rendez-vous
            for (Appointment appointment : appointments) {
                loadRelatedObjects(appointment);
            }
            
            return appointments;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération des rendez-vous par médecin et date", e);
            throw new DAOException("Erreur lors de la récupération des rendez-vous: " + e.getMessage(), 
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
    public List<Appointment> getAppointmentsByDoctorAndDateRange(int doctorId, LocalDate startDate, LocalDate endDate) throws DAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(SQL_SELECT_APPOINTMENTS_BY_DOCTOR_AND_DATE_RANGE);
            stmt.setInt(1, doctorId);
            stmt.setDate(2, Date.valueOf(startDate));
            stmt.setDate(3, Date.valueOf(endDate));
            
            rs = stmt.executeQuery();
            
            List<Appointment> appointments = new ArrayList<>();
            while (rs.next()) {
                Appointment appointment = createAppointmentFromResultSet(rs);
                appointments.add(appointment);
            }
            
            // Charger les objets liés pour tous les rendez-vous
            for (Appointment appointment : appointments) {
                loadRelatedObjects(appointment);
            }
            
            return appointments;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération des rendez-vous par médecin et plage de dates", e);
            throw new DAOException("Erreur lors de la récupération des rendez-vous: " + e.getMessage(), 
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
    public boolean hasAppointmentConflict(int doctorId, LocalDateTime startDateTime, LocalDateTime endDateTime, int excludeAppointmentId) throws DAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(SQL_CHECK_APPOINTMENT_CONFLICT);
            stmt.setInt(1, doctorId);
            stmt.setInt(2, excludeAppointmentId); // ID à exclure (pour les mises à jour)
            stmt.setTimestamp(3, Timestamp.valueOf(endDateTime)); // Fin du RDV A doit être avant le début du RDV B
            stmt.setTimestamp(4, Timestamp.valueOf(startDateTime)); // Début du RDV A doit être après la fin du RDV B
            stmt.setTimestamp(5, Timestamp.valueOf(startDateTime)); // Début du RDV A
            stmt.setTimestamp(6, Timestamp.valueOf(endDateTime)); // Fin du RDV A
            
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0; // Il y a un conflit si au moins un rendez-vous est trouvé
            }
            
            return false;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la vérification des conflits de rendez-vous", e);
            throw new DAOException("Erreur lors de la vérification des conflits de rendez-vous: " + e.getMessage(), 
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
    public List<Appointment> getAppointmentsByDate(LocalDate date) throws DAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(SQL_SELECT_APPOINTMENTS_BY_DATE);
            stmt.setDate(1, Date.valueOf(date));
            
            rs = stmt.executeQuery();
            
            List<Appointment> appointments = new ArrayList<>();
            while (rs.next()) {
                Appointment appointment = createAppointmentFromResultSet(rs);
                appointments.add(appointment);
            }
            
            // Charger les objets liés pour tous les rendez-vous
            for (Appointment appointment : appointments) {
                loadRelatedObjects(appointment);
            }
            
            return appointments;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération des rendez-vous par date", e);
            throw new DAOException("Erreur lors de la récupération des rendez-vous: " + e.getMessage(), 
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
    public List<Appointment> getAppointmentsByStatus(AppointmentStatus status) throws DAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(SQL_SELECT_APPOINTMENTS_BY_STATUS);
            stmt.setString(1, status.name());
            
            rs = stmt.executeQuery();
            
            List<Appointment> appointments = new ArrayList<>();
            while (rs.next()) {
                Appointment appointment = createAppointmentFromResultSet(rs);
                appointments.add(appointment);
            }
            
            // Charger les objets liés pour tous les rendez-vous
            for (Appointment appointment : appointments) {
                loadRelatedObjects(appointment);
            }
            
            return appointments;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération des rendez-vous par statut", e);
            throw new DAOException("Erreur lors de la récupération des rendez-vous: " + e.getMessage(), 
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
    public int countAppointmentsByDoctorAndDate(int doctorId, LocalDate date) throws DAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(SQL_COUNT_APPOINTMENTS_BY_DOCTOR_AND_DATE);
            stmt.setInt(1, doctorId);
            stmt.setDate(2, Date.valueOf(date));
            
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
            return 0;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du comptage des rendez-vous par médecin et date", e);
            throw new DAOException("Erreur lors du comptage des rendez-vous: " + e.getMessage(), 
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
    public List<Appointment> getUpcomingAppointmentsByPatient(int patientId, int limit) throws DAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(SQL_SELECT_UPCOMING_APPOINTMENTS_BY_PATIENT);
            stmt.setInt(1, patientId);
            stmt.setInt(2, limit);
            
            rs = stmt.executeQuery();
            
            List<Appointment> appointments = new ArrayList<>();
            while (rs.next()) {
                Appointment appointment = createAppointmentFromResultSet(rs);
                appointments.add(appointment);
            }
            
            // Charger les objets liés pour tous les rendez-vous
            for (Appointment appointment : appointments) {
                loadRelatedObjects(appointment);
            }
            
            return appointments;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération des prochains rendez-vous par patient", e);
            throw new DAOException("Erreur lors de la récupération des rendez-vous: " + e.getMessage(), 
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
    public List<Appointment> getUpcomingAppointmentsByDoctor(int doctorId, int limit) throws DAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(SQL_SELECT_UPCOMING_APPOINTMENTS_BY_DOCTOR);
            stmt.setInt(1, doctorId);
            stmt.setInt(2, limit);
            
            rs = stmt.executeQuery();
            
            List<Appointment> appointments = new ArrayList<>();
            while (rs.next()) {
                Appointment appointment = createAppointmentFromResultSet(rs);
                appointments.add(appointment);
            }
            
            // Charger les objets liés pour tous les rendez-vous
            for (Appointment appointment : appointments) {
                loadRelatedObjects(appointment);
            }
            
            return appointments;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération des prochains rendez-vous par médecin", e);
            throw new DAOException("Erreur lors de la récupération des rendez-vous: " + e.getMessage(), 
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
    public List<Appointment> searchAppointmentsByCriteria(Map<String, Object> criteria) throws DAOException {
        if (criteria == null || criteria.isEmpty()) {
            throw new DAOException("Critères de recherche non spécifiés", DAOException.RETRIEVAL_ERROR);
        }
        
        // Construction dynamique de la requête SQL
        StringBuilder sqlBuilder = new StringBuilder("SELECT rv.*, p.nom as patient_nom, p.prenom as patient_prenom, " +
                                                   "u.nom_complet as medecin_nom " +
                                                   "FROM rendez_vous rv " +
                                                   "JOIN patients p ON rv.id_patient_fk = p.id_patient " +
                                                   "JOIN utilisateurs u ON rv.id_medecin_fk = u.id_utilisateur " +
                                                   "WHERE 1=1");
        
        List<Object> parameters = new ArrayList<>();
        
        // Ajouter les critères à la requête
        for (Map.Entry<String, Object> entry : criteria.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            if (value != null) {
                switch (key) {
                    case "doctorId":
                        sqlBuilder.append(" AND rv.id_medecin_fk = ?");
                        parameters.add(value);
                        break;
                    case "patientId":
                        sqlBuilder.append(" AND rv.id_patient_fk = ?");
                        parameters.add(value);
                        break;
                    case "status":
                        if (value instanceof AppointmentStatus) {
                            sqlBuilder.append(" AND rv.statut_rdv = ?");
                            parameters.add(((AppointmentStatus) value).name());
                        } else if (value instanceof String) {
                            sqlBuilder.append(" AND rv.statut_rdv = ?");
                            parameters.add(value);
                        }
                        break;
                    case "date":
                        if (value instanceof LocalDate) {
                            sqlBuilder.append(" AND DATE(rv.date_heure_debut) = ?");
                            parameters.add(Date.valueOf((LocalDate) value));
                        }
                        break;
                    case "startDateMin":
                        if (value instanceof LocalDateTime) {
                            sqlBuilder.append(" AND rv.date_heure_debut >= ?");
                            parameters.add(Timestamp.valueOf((LocalDateTime) value));
                        } else if (value instanceof LocalDate) {
                            LocalDateTime startOfDay = ((LocalDate) value).atStartOfDay();
                            sqlBuilder.append(" AND rv.date_heure_debut >= ?");
                            parameters.add(Timestamp.valueOf(startOfDay));
                        }
                        break;
                    case "startDateMax":
                        if (value instanceof LocalDateTime) {
                            sqlBuilder.append(" AND rv.date_heure_debut <= ?");
                            parameters.add(Timestamp.valueOf((LocalDateTime) value));
                        } else if (value instanceof LocalDate) {
                            LocalDateTime endOfDay = ((LocalDate) value).atTime(23, 59, 59);
                            sqlBuilder.append(" AND rv.date_heure_debut <= ?");
                            parameters.add(Timestamp.valueOf(endOfDay));
                        }
                        break;
                    case "appointmentType":
                        sqlBuilder.append(" AND rv.type_consultation = ?");
                        parameters.add(value);
                        break;
                    case "patientName":
                        sqlBuilder.append(" AND (p.nom LIKE ? OR p.prenom LIKE ?)");
                        parameters.add("%" + value + "%");
                        parameters.add("%" + value + "%");
                        break;
                    default:
                        LOGGER.warning("Critère de recherche inconnu ignoré: " + key);
                        break;
                }
            }
        }
        
        sqlBuilder.append(" ORDER BY rv.date_heure_debut");
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(sqlBuilder.toString());
            
            // Définir les paramètres
            for (int i = 0; i < parameters.size(); i++) {
                Object param = parameters.get(i);
                if (param instanceof String) {
                    stmt.setString(i + 1, (String) param);
                } else if (param instanceof Date) {
                    stmt.setDate(i + 1, (Date) param);
                } else if (param instanceof Timestamp) {
                    stmt.setTimestamp(i + 1, (Timestamp) param);
                } else if (param instanceof Integer) {
                    stmt.setInt(i + 1, (Integer) param);
                } else if (param instanceof Boolean) {
                    stmt.setBoolean(i + 1, (Boolean) param);
                } else {
                    stmt.setObject(i + 1, param);
                }
            }
            
            rs = stmt.executeQuery();
            
            List<Appointment> appointments = new ArrayList<>();
            while (rs.next()) {
                Appointment appointment = createAppointmentFromResultSet(rs);
                appointments.add(appointment);
            }
            
            // Charger les objets liés pour tous les rendez-vous
            for (Appointment appointment : appointments) {
                loadRelatedObjects(appointment);
            }
            
            return appointments;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la recherche de rendez-vous par critères", e);
            throw new DAOException("Erreur lors de la recherche de rendez-vous: " + e.getMessage(), 
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
}