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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import config.DatabaseConnection;
import model.Patient;

/**
 * Implémentation MySQL de l'interface PatientDAO.
 * Gère la persistance des patients dans une base de données MySQL.
 */
public class PatientDAOImpl implements PatientDAO {
    
    private static final Logger LOGGER = Logger.getLogger(PatientDAOImpl.class.getName());
    
    // Requêtes SQL pour les opérations sur les patients
    private static final String SQL_INSERT_PATIENT = 
            "INSERT INTO patients (nom, prenom, date_naissance, telephone, adresse, email, notes_administratives, date_creation_fiche) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    
    private static final String SQL_UPDATE_PATIENT = 
            "UPDATE patients SET nom = ?, prenom = ?, date_naissance = ?, telephone = ?, " +
            "adresse = ?, email = ?, notes_administratives = ? WHERE id_patient = ?";
    
    private static final String SQL_DELETE_PATIENT = 
            "DELETE FROM patients WHERE id_patient = ?";
    
    private static final String SQL_SELECT_PATIENT_BY_ID = 
            "SELECT * FROM patients WHERE id_patient = ?";
    
    private static final String SQL_SELECT_ALL_PATIENTS = 
            "SELECT * FROM patients ORDER BY nom, prenom";
    
    private static final String SQL_SEARCH_PATIENTS_BY_NAME = 
            "SELECT * FROM patients WHERE (nom LIKE ? OR ? IS NULL) AND (prenom LIKE ? OR ? IS NULL) " +
            "ORDER BY nom, prenom";
    
    private static final String SQL_SEARCH_PATIENTS_BY_PHONE = 
            "SELECT * FROM patients WHERE telephone LIKE ? ORDER BY nom, prenom";
    
    private static final String SQL_SEARCH_PATIENTS_BY_EMAIL = 
            "SELECT * FROM patients WHERE email LIKE ? ORDER BY nom, prenom";
    
    private static final String SQL_CHECK_PATIENT_EXISTS = 
            "SELECT COUNT(*) FROM patients WHERE nom = ? AND prenom = ? AND date_naissance = ?";
    
    private static final String SQL_COUNT_PATIENTS = 
            "SELECT COUNT(*) FROM patients";
    
    // Référence à la connexion à la base de données
    private final DatabaseConnection dbConnection;
    
    /**
     * Constructeur par défaut.
     * Récupère l'instance singleton de la connexion à la base de données.
     */
    public PatientDAOImpl() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    /**
     * Crée un objet Patient à partir d'un ResultSet
     * 
     * @param rs Le ResultSet contenant les données du patient
     * @return Un objet Patient initialisé avec les données du ResultSet
     * @throws SQLException En cas d'erreur d'accès aux données du ResultSet
     */
    private Patient createPatientFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id_patient");
        String lastName = rs.getString("nom");
        String firstName = rs.getString("prenom");
        
        // Convertir les dates SQL en LocalDate/LocalDateTime
        Date birthDate = rs.getDate("date_naissance");
        LocalDate localBirthDate = birthDate != null ? birthDate.toLocalDate() : null;
        
        String phone = rs.getString("telephone");
        String address = rs.getString("adresse");
        String email = rs.getString("email");
        String notes = rs.getString("notes_administratives");
        
        Timestamp creationTimestamp = rs.getTimestamp("date_creation_fiche");
        LocalDateTime creationDate = creationTimestamp != null 
                ? creationTimestamp.toLocalDateTime() 
                : LocalDateTime.now();
        
        return new Patient(id, lastName, firstName, localBirthDate, phone, address, email, notes, creationDate);
    }
    
    @Override
    public int addPatient(Patient patient) throws DAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;
        
        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(SQL_INSERT_PATIENT, Statement.RETURN_GENERATED_KEYS);
            
            stmt.setString(1, patient.getLastName());
            stmt.setString(2, patient.getFirstName());
            
            // Convertir LocalDate en java.sql.Date
            LocalDate birthDate = patient.getBirthDate();
            if (birthDate != null) {
                stmt.setDate(3, Date.valueOf(birthDate));
            } else {
                stmt.setNull(3, java.sql.Types.DATE);
            }
            
            stmt.setString(4, patient.getPhone());
            stmt.setString(5, patient.getAddress());
            stmt.setString(6, patient.getEmail());
            stmt.setString(7, patient.getAdministrativeNotes());
            
            // Utiliser la date actuelle si non spécifiée
            LocalDateTime creationDate = patient.getCreationDate();
            if (creationDate == null) {
                creationDate = LocalDateTime.now();
                patient.setCreationDate(creationDate);
            }
            stmt.setTimestamp(8, Timestamp.valueOf(creationDate));
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected == 0) {
                throw new DAOException("L'ajout du patient a échoué, aucune ligne affectée.", 
                                    DAOException.INSERTION_ERROR);
            }
            
            // Récupérer l'ID généré
            generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int patientId = generatedKeys.getInt(1);
                patient.setId(patientId);
                return patientId;
            } else {
                throw new DAOException("L'ajout du patient a échoué, aucun ID généré.", 
                                    DAOException.INSERTION_ERROR);
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'ajout du patient", e);
            
            if (e.getMessage().contains("Duplicate entry")) {
                throw new DAOException("Un patient avec ces informations existe déjà.", e, 
                                    DAOException.DUPLICATE_ERROR);
            }
            
            throw new DAOException("Erreur lors de l'ajout du patient: " + e.getMessage(), 
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
    public void updatePatient(Patient patient) throws DAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(SQL_UPDATE_PATIENT);
            
            stmt.setString(1, patient.getLastName());
            stmt.setString(2, patient.getFirstName());
            
            // Convertir LocalDate en java.sql.Date
            LocalDate birthDate = patient.getBirthDate();
            if (birthDate != null) {
                stmt.setDate(3, Date.valueOf(birthDate));
            } else {
                stmt.setNull(3, java.sql.Types.DATE);
            }
            
            stmt.setString(4, patient.getPhone());
            stmt.setString(5, patient.getAddress());
            stmt.setString(6, patient.getEmail());
            stmt.setString(7, patient.getAdministrativeNotes());
            stmt.setInt(8, patient.getId());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected == 0) {
                throw new DAOException("La mise à jour du patient a échoué, aucune ligne affectée. Patient ID: " + patient.getId(), 
                                    DAOException.UPDATE_ERROR);
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la mise à jour du patient", e);
            
            if (e.getMessage().contains("Duplicate entry")) {
                throw new DAOException("Un patient avec ces informations existe déjà.", e, 
                                    DAOException.DUPLICATE_ERROR);
            }
            
            throw new DAOException("Erreur lors de la mise à jour du patient: " + e.getMessage(), 
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
    public void deletePatient(int patientId) throws DAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = dbConnection.getConnection();
            
            // Vérifier d'abord s'il y a des rendez-vous associés à ce patient
            // On pourrait plutôt compter sur les contraintes de clé étrangère pour cela
            
            stmt = conn.prepareStatement(SQL_DELETE_PATIENT);
            stmt.setInt(1, patientId);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected == 0) {
                throw new DAOException("La suppression du patient a échoué, aucune ligne affectée. Patient ID: " + patientId, 
                                    DAOException.DELETION_ERROR);
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la suppression du patient", e);
            
            if (e.getMessage().contains("foreign key constraint")) {
                throw new DAOException("Impossible de supprimer ce patient car il possède des rendez-vous.", e, 
                                    DAOException.CONSTRAINT_VIOLATION);
            }
            
            throw new DAOException("Erreur lors de la suppression du patient: " + e.getMessage(), 
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
    public Patient getPatientById(int patientId) throws DAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(SQL_SELECT_PATIENT_BY_ID);
            stmt.setInt(1, patientId);
            
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return createPatientFromResultSet(rs);
            } else {
                return null; // Aucun patient trouvé
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération du patient par ID", e);
            throw new DAOException("Erreur lors de la récupération du patient: " + e.getMessage(), 
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
    public List<Patient> getAllPatients() throws DAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(SQL_SELECT_ALL_PATIENTS);
            
            rs = stmt.executeQuery();
            
            List<Patient> patients = new ArrayList<>();
            while (rs.next()) {
                patients.add(createPatientFromResultSet(rs));
            }
            
            return patients;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération de tous les patients", e);
            throw new DAOException("Erreur lors de la récupération des patients: " + e.getMessage(), 
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
    public List<Patient> searchPatientsByName(String lastName, String firstName) throws DAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(SQL_SEARCH_PATIENTS_BY_NAME);
            
            // Traiter le cas où lastName est null
            if (lastName != null && !lastName.isEmpty()) {
                stmt.setString(1, "%" + lastName + "%");
                stmt.setNull(2, java.sql.Types.VARCHAR);
            } else {
                stmt.setString(1, "%");
                stmt.setString(2, "%");
            }
            
            // Traiter le cas où firstName est null
            if (firstName != null && !firstName.isEmpty()) {
                stmt.setString(3, "%" + firstName + "%");
                stmt.setNull(4, java.sql.Types.VARCHAR);
            } else {
                stmt.setString(3, "%");
                stmt.setString(4, "%");
            }
            
            rs = stmt.executeQuery();
            
            List<Patient> patients = new ArrayList<>();
            while (rs.next()) {
                patients.add(createPatientFromResultSet(rs));
            }
            
            return patients;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la recherche de patients par nom", e);
            throw new DAOException("Erreur lors de la recherche de patients: " + e.getMessage(), 
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
    public List<Patient> searchPatientsByPhone(String phone) throws DAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(SQL_SEARCH_PATIENTS_BY_PHONE);
            
            // Recherche approximative (LIKE)
            stmt.setString(1, "%" + phone + "%");
            
            rs = stmt.executeQuery();
            
            List<Patient> patients = new ArrayList<>();
            while (rs.next()) {
                patients.add(createPatientFromResultSet(rs));
            }
            
            return patients;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la recherche de patients par téléphone", e);
            throw new DAOException("Erreur lors de la recherche de patients: " + e.getMessage(), 
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
    public List<Patient> searchPatientsByEmail(String email) throws DAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(SQL_SEARCH_PATIENTS_BY_EMAIL);
            
            // Recherche approximative (LIKE)
            stmt.setString(1, "%" + email + "%");
            
            rs = stmt.executeQuery();
            
            List<Patient> patients = new ArrayList<>();
            while (rs.next()) {
                patients.add(createPatientFromResultSet(rs));
            }
            
            return patients;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la recherche de patients par email", e);
            throw new DAOException("Erreur lors de la recherche de patients: " + e.getMessage(), 
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
    public boolean isPatientExists(Patient patient) throws DAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(SQL_CHECK_PATIENT_EXISTS);
            
            stmt.setString(1, patient.getLastName());
            stmt.setString(2, patient.getFirstName());
            
            // Convertir LocalDate en java.sql.Date
            LocalDate birthDate = patient.getBirthDate();
            if (birthDate != null) {
                stmt.setDate(3, Date.valueOf(birthDate));
            } else {
                stmt.setNull(3, java.sql.Types.DATE);
            }
            
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
            return false;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la vérification d'existence du patient", e);
            throw new DAOException("Erreur lors de la vérification d'existence du patient: " + e.getMessage(), 
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
    public int countPatients() throws DAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(SQL_COUNT_PATIENTS);
            
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
            return 0;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du comptage des patients", e);
            throw new DAOException("Erreur lors du comptage des patients: " + e.getMessage(), 
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
    public List<Patient> searchPatientsByCriteria(Map<String, Object> criteria) throws DAOException {
        if (criteria == null || criteria.isEmpty()) {
            return getAllPatients();
        }
        
        // Construction dynamique de la requête SQL
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM patients WHERE 1=1");
        List<Object> parameters = new ArrayList<>();
        
        // Ajouter les critères à la requête
        for (Map.Entry<String, Object> entry : criteria.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            if (value != null) {
                switch (key) {
                    case "lastName":
                        sqlBuilder.append(" AND nom LIKE ?");
                        parameters.add("%" + value + "%");
                        break;
                    case "firstName":
                        sqlBuilder.append(" AND prenom LIKE ?");
                        parameters.add("%" + value + "%");
                        break;
                    case "phone":
                        sqlBuilder.append(" AND telephone LIKE ?");
                        parameters.add("%" + value + "%");
                        break;
                    case "email":
                        sqlBuilder.append(" AND email LIKE ?");
                        parameters.add("%" + value + "%");
                        break;
                    case "birthDateMin":
                        if (value instanceof LocalDate) {
                            sqlBuilder.append(" AND date_naissance >= ?");
                            parameters.add(Date.valueOf((LocalDate) value));
                        }
                        break;
                    case "birthDateMax":
                        if (value instanceof LocalDate) {
                            sqlBuilder.append(" AND date_naissance <= ?");
                            parameters.add(Date.valueOf((LocalDate) value));
                        }
                        break;
                    default:
                        LOGGER.warning("Critère de recherche inconnu ignoré: " + key);
                        break;
                }
            }
        }
        
        sqlBuilder.append(" ORDER BY nom, prenom");
        
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
                } else if (param instanceof Integer) {
                    stmt.setInt(i + 1, (Integer) param);
                } else if (param instanceof Boolean) {
                    stmt.setBoolean(i + 1, (Boolean) param);
                } else {
                    stmt.setObject(i + 1, param);
                }
            }
            
            rs = stmt.executeQuery();
            
            List<Patient> patients = new ArrayList<>();
            while (rs.next()) {
                patients.add(createPatientFromResultSet(rs));
            }
            
            return patients;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la recherche de patients par critères", e);
            throw new DAOException("Erreur lors de la recherche de patients: " + e.getMessage(), 
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