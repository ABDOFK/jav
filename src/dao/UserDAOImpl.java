package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import config.DatabaseConnection;
import model.Doctor;
import model.Secretary;
import model.User;

/**
 * Implémentation MySQL de l'interface UserDAO.
 * Gère la persistance des utilisateurs (secrétaires et médecins) dans une base
 * de données MySQL.
 */
public class UserDAOImpl implements UserDAO {

    private static final Logger LOGGER = Logger.getLogger(UserDAOImpl.class.getName());

    // Requêtes SQL pour les utilisateurs généraux
    private static final String SQL_INSERT_USER = "INSERT INTO utilisateurs (nom_utilisateur, mot_de_passe_hash, role, nom_complet, actif, date_creation_compte) "
            +
            "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE_USER = "UPDATE utilisateurs SET nom_utilisateur = ?, role = ?, nom_complet = ?, actif = ? "
            +
            "WHERE id_utilisateur = ?";

    private static final String SQL_SET_USER_ACTIVE = "UPDATE utilisateurs SET actif = ? WHERE id_utilisateur = ?";

    private static final String SQL_SELECT_USER_BY_ID = "SELECT * FROM utilisateurs WHERE id_utilisateur = ?";

    private static final String SQL_SELECT_USER_BY_USERNAME = "SELECT * FROM utilisateurs WHERE nom_utilisateur = ?";

    private static final String SQL_SELECT_ALL_USERS = "SELECT * FROM utilisateurs ORDER BY nom_complet";

    private static final String SQL_SELECT_USERS_BY_ROLE = "SELECT * FROM utilisateurs WHERE role = ? ORDER BY nom_complet";

    private static final String SQL_AUTHENTICATE_USER = "SELECT * FROM utilisateurs WHERE nom_utilisateur = ? AND mot_de_passe_hash = ? AND actif = TRUE";

    private static final String SQL_CHANGE_PASSWORD = "UPDATE utilisateurs SET mot_de_passe_hash = ? WHERE id_utilisateur = ?";

    // Requêtes SQL pour les médecins
    private static final String SQL_INSERT_DOCTOR = "INSERT INTO medecins (id_medecin, specialite, horaires_disponibilite, telephone_professionnel) "
            +
            "VALUES (?, ?, ?, ?)";

    private static final String SQL_UPDATE_DOCTOR = "UPDATE medecins SET specialite = ?, horaires_disponibilite = ?, telephone_professionnel = ? "
            +
            "WHERE id_medecin = ?";

    private static final String SQL_SELECT_DOCTOR_BY_ID = "SELECT u.*, m.specialite, m.horaires_disponibilite, m.telephone_professionnel "
            +
            "FROM utilisateurs u JOIN medecins m ON u.id_utilisateur = m.id_medecin " +
            "WHERE u.id_utilisateur = ?";

    private static final String SQL_SELECT_ALL_DOCTORS = "SELECT u.*, m.specialite, m.horaires_disponibilite, m.telephone_professionnel "
            +
            "FROM utilisateurs u JOIN medecins m ON u.id_utilisateur = m.id_medecin " +
            "WHERE u.role = 'MEDECIN' ORDER BY u.nom_complet";

    private static final String SQL_SELECT_DOCTORS_BY_SPECIALTY = "SELECT u.*, m.specialite, m.horaires_disponibilite, m.telephone_professionnel "
            +
            "FROM utilisateurs u JOIN medecins m ON u.id_utilisateur = m.id_medecin " +
            "WHERE m.specialite = ? ORDER BY u.nom_complet";

    private static final String SQL_SELECT_ALL_SPECIALTIES = "SELECT DISTINCT specialite FROM medecins ORDER BY specialite";

    // Référence à la connexion à la base de données
    private final DatabaseConnection dbConnection;

    /**
     * Constructeur par défaut.
     * Récupère l'instance singleton de la connexion à la base de données.
     */
    public UserDAOImpl() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    /**
     * Crée et retourne une instance d'User appropriée selon le rôle
     * (Factory Method pattern)
     * 
     * @param rs Le ResultSet contenant les données de l'utilisateur
     * @return Une instance d'User, Secretary ou Doctor selon le rôle
     * @throws SQLException En cas d'erreur d'accès aux données du ResultSet
     */
    private User createUserFromResultSet(ResultSet rs) throws SQLException {
        String role = rs.getString("role");

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

        // Créer une instance selon le rôle
        if ("SECRETAIRE".equals(role)) {
            return new Secretary(id, username, passwordHash, fullName, active, creationDate);
        } else if ("MEDECIN".equals(role)) {
            // Pour un médecin, il faudrait idéalement récupérer les données spécifiques
            // Mais ici on retourne une instance basique qui sera enrichie si nécessaire
            Doctor doctor = new Doctor(id, username, passwordHash, fullName, active, creationDate, null, null, null);
            doctor.setRole(role);
            return doctor;
        } else {
            // Cas générique pour un autre rôle potentiel
            throw new SQLException("Rôle non supporté: " + role);
        }
    }

    /**
     * Crée et retourne une instance de Doctor avec toutes les données spécifiques
     * 
     * @param rs Le ResultSet contenant les données du médecin
     * @return Une instance de Doctor complète
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

    @Override
    public int addUser(User user) throws DAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;

        try {
            conn = dbConnection.getConnection();

            // Désactiver l'auto-commit pour la transaction
            conn.setAutoCommit(false);

            // Insérer l'utilisateur de base
            stmt = conn.prepareStatement(SQL_INSERT_USER, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getRole());
            stmt.setString(4, user.getFullName());
            stmt.setBoolean(5, user.isActive());
            stmt.setTimestamp(6, Timestamp.valueOf(user.getCreationDate()));

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new DAOException("L'insertion de l'utilisateur a échoué, aucune ligne affectée.",
                        DAOException.INSERTION_ERROR);
            }

            // Récupérer l'ID généré
            generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int userId = generatedKeys.getInt(1);
                user.setId(userId);

                // Si c'est un médecin, insérer les données spécifiques
                if (user instanceof Doctor) {
                    Doctor doctor = (Doctor) user;
                    addDoctorSpecificData(conn, doctor);
                }

                // Valider la transaction
                conn.commit();

                return userId;
            } else {
                conn.rollback();
                throw new DAOException("L'insertion de l'utilisateur a échoué, aucun ID généré.",
                        DAOException.INSERTION_ERROR);
            }

        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Erreur lors du rollback", ex);
            }

            LOGGER.log(Level.SEVERE, "Erreur lors de l'ajout de l'utilisateur", e);

            if (e.getMessage().contains("Duplicate entry") && e.getMessage().contains("nom_utilisateur")) {
                throw new DAOException("Ce nom d'utilisateur existe déjà.", e, DAOException.DUPLICATE_ERROR);
            }

            throw new DAOException("Erreur lors de l'ajout de l'utilisateur: " + e.getMessage(),
                    e, DAOException.INSERTION_ERROR);
        } finally {
            // Fermer les ressources
            try {
                if (generatedKeys != null)
                    generatedKeys.close();
                if (stmt != null)
                    stmt.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Erreur lors de la fermeture des ressources", e);
            }
        }
    }

    /**
     * Méthode privée pour ajouter les données spécifiques d'un médecin
     * 
     * @param conn   La connexion à utiliser dans la transaction
     * @param doctor Le médecin dont les données spécifiques doivent être ajoutées
     * @throws SQLException En cas d'erreur SQL
     */
    private void addDoctorSpecificData(Connection conn, Doctor doctor) throws SQLException {
        PreparedStatement stmt = null;

        try {
            stmt = conn.prepareStatement(SQL_INSERT_DOCTOR);
            stmt.setInt(1, doctor.getId());
            stmt.setString(2, doctor.getSpecialty());
            stmt.setString(3, doctor.getWorkHours());
            stmt.setString(4, doctor.getProfessionalPhone());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("L'insertion des données spécifiques du médecin a échoué.");
            }
        } finally {
            if (stmt != null)
                stmt.close();
        }
    }

    @Override
    public void updateUser(User user) throws DAOException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = dbConnection.getConnection();

            // Désactiver l'auto-commit pour la transaction
            conn.setAutoCommit(false);

            // Mettre à jour les données de base de l'utilisateur
            stmt = conn.prepareStatement(SQL_UPDATE_USER);
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getRole());
            stmt.setString(3, user.getFullName());
            stmt.setBoolean(4, user.isActive());
            stmt.setInt(5, user.getId());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new DAOException("La mise à jour de l'utilisateur a échoué, aucune ligne affectée.",
                        DAOException.UPDATE_ERROR);
            }

            // Si c'est un médecin, mettre à jour les données spécifiques
            if (user instanceof Doctor) {
                Doctor doctor = (Doctor) user;
                updateDoctorSpecificData(conn, doctor);
            }

            // Valider la transaction
            conn.commit();

        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Erreur lors du rollback", ex);
            }

            LOGGER.log(Level.SEVERE, "Erreur lors de la mise à jour de l'utilisateur", e);

            if (e.getMessage().contains("Duplicate entry") && e.getMessage().contains("nom_utilisateur")) {
                throw new DAOException("Ce nom d'utilisateur existe déjà.", e, DAOException.DUPLICATE_ERROR);
            }

            throw new DAOException("Erreur lors de la mise à jour de l'utilisateur: " + e.getMessage(),
                    e, DAOException.UPDATE_ERROR);
        } finally {
            // Fermer les ressources
            try {
                if (stmt != null)
                    stmt.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Erreur lors de la fermeture des ressources", e);
            }
        }
    }

    /**
     * Méthode privée pour mettre à jour les données spécifiques d'un médecin
     * 
     * @param conn   La connexion à utiliser dans la transaction
     * @param doctor Le médecin dont les données spécifiques doivent être mises à
     *               jour
     * @throws SQLException En cas d'erreur SQL
     */
    private void updateDoctorSpecificData(Connection conn, Doctor doctor) throws SQLException {
        PreparedStatement stmt = null;

        try {
            stmt = conn.prepareStatement(SQL_UPDATE_DOCTOR);
            stmt.setString(1, doctor.getSpecialty());
            stmt.setString(2, doctor.getWorkHours());
            stmt.setString(3, doctor.getProfessionalPhone());
            stmt.setInt(4, doctor.getId());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("La mise à jour des données spécifiques du médecin a échoué.");
            }
        } finally {
            if (stmt != null)
                stmt.close();
        }
    }

    @Override
    public void setUserActive(int userId, boolean active) throws DAOException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(SQL_SET_USER_ACTIVE);
            stmt.setBoolean(1, active);
            stmt.setInt(2, userId);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new DAOException("La mise à jour du statut de l'utilisateur a échoué, aucune ligne affectée.",
                        DAOException.UPDATE_ERROR);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la mise à jour du statut de l'utilisateur", e);
            throw new DAOException("Erreur lors de la mise à jour du statut de l'utilisateur: " + e.getMessage(),
                    e, DAOException.UPDATE_ERROR);
        } finally {
            // Fermer les ressources
            try {
                if (stmt != null)
                    stmt.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Erreur lors de la fermeture des ressources", e);
            }
        }
    }

    @Override
    public User getUserById(int userId) throws DAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(SQL_SELECT_USER_BY_ID);
            stmt.setInt(1, userId);

            rs = stmt.executeQuery();

            if (rs.next()) {
                return createUserFromResultSet(rs);
            } else {
                return null; // Aucun utilisateur trouvé
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération de l'utilisateur par ID", e);
            throw new DAOException("Erreur lors de la récupération de l'utilisateur: " + e.getMessage(),
                    e, DAOException.RETRIEVAL_ERROR);
        } finally {
            // Fermer les ressources
            try {
                if (rs != null)
                    rs.close();
                if (stmt != null)
                    stmt.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Erreur lors de la fermeture des ressources", e);
            }
        }
    }

    @Override
    public User getUserByUsername(String username) throws DAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(SQL_SELECT_USER_BY_USERNAME);
            stmt.setString(1, username);

            rs = stmt.executeQuery();

            if (rs.next()) {
                return createUserFromResultSet(rs);
            } else {
                return null; // Aucun utilisateur trouvé
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération de l'utilisateur par nom d'utilisateur", e);
            throw new DAOException("Erreur lors de la récupération de l'utilisateur: " + e.getMessage(),
                    e, DAOException.RETRIEVAL_ERROR);
        } finally {
            // Fermer les ressources
            try {
                if (rs != null)
                    rs.close();
                if (stmt != null)
                    stmt.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Erreur lors de la fermeture des ressources", e);
            }
        }
    }

    @Override
    public User authenticateUser(String username, String passwordHash) throws DAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(SQL_AUTHENTICATE_USER);
            stmt.setString(1, username);
            stmt.setString(2, passwordHash);

            rs = stmt.executeQuery();

            if (rs.next()) {
                return createUserFromResultSet(rs);
            } else {
                return null; // Authentification échouée
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'authentification de l'utilisateur", e);
            throw new DAOException("Erreur lors de l'authentification: " + e.getMessage(),
                    e, DAOException.RETRIEVAL_ERROR);
        } finally {
            // Fermer les ressources
            try {
                if (rs != null)
                    rs.close();
                if (stmt != null)
                    stmt.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Erreur lors de la fermeture des ressources", e);
            }
        }
    }

    @Override
    public List<User> getAllUsers() throws DAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(SQL_SELECT_ALL_USERS);

            rs = stmt.executeQuery();

            List<User> users = new ArrayList<>();
            while (rs.next()) {
                users.add(createUserFromResultSet(rs));
            }

            return users;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération de tous les utilisateurs", e);
            throw new DAOException("Erreur lors de la récupération des utilisateurs: " + e.getMessage(),
                    e, DAOException.RETRIEVAL_ERROR);
        } finally {
            // Fermer les ressources
            try {
                if (rs != null)
                    rs.close();
                if (stmt != null)
                    stmt.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Erreur lors de la fermeture des ressources", e);
            }
        }
    }

    @Override
    public List<User> getUsersByRole(String role) throws DAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(SQL_SELECT_USERS_BY_ROLE);
            stmt.setString(1, role);

            rs = stmt.executeQuery();

            List<User> users = new ArrayList<>();
            while (rs.next()) {
                users.add(createUserFromResultSet(rs));
            }

            return users;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération des utilisateurs par rôle", e);
            throw new DAOException("Erreur lors de la récupération des utilisateurs: " + e.getMessage(),
                    e, DAOException.RETRIEVAL_ERROR);
        } finally {
            // Fermer les ressources
            try {
                if (rs != null)
                    rs.close();
                if (stmt != null)
                    stmt.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Erreur lors de la fermeture des ressources", e);
            }
        }
    }

    @Override
    public int addSecretary(Secretary secretary) throws DAOException {
        // Utiliser la méthode générique addUser
        return addUser(secretary);
    }

    @Override
    public void updateSecretary(Secretary secretary) throws DAOException {
        // Utiliser la méthode générique updateUser
        updateUser(secretary);
    }

    @Override
    public Secretary getSecretaryById(int id) throws DAOException {
        User user = getUserById(id);

        if (user == null || !user.hasRole("SECRETAIRE")) {
            return null;
        }

        return (Secretary) user;
    }

    @Override
    public List<Secretary> getAllSecretaries() throws DAOException {
        List<User> users = getUsersByRole("SECRETAIRE");
        List<Secretary> secretaries = new ArrayList<>(users.size());

        for (User user : users) {
            if (user instanceof Secretary) {
                secretaries.add((Secretary) user);
            }
        }

        return secretaries;
    }

    @Override
    public int addDoctor(Doctor doctor) throws DAOException {
        return addUser(doctor);
    }

    @Override
    public void updateDoctor(Doctor doctor) throws DAOException {
        updateUser(doctor);
    }

    @Override
    public Doctor getDoctorById(int id) throws DAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(SQL_SELECT_DOCTOR_BY_ID);
            stmt.setInt(1, id);

            rs = stmt.executeQuery();

            if (rs.next()) {
                return createDoctorFromResultSet(rs);
            } else {
                return null; // Aucun médecin trouvé
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération du médecin par ID", e);
            throw new DAOException("Erreur lors de la récupération du médecin: " + e.getMessage(),
                    e, DAOException.RETRIEVAL_ERROR);
        } finally {
            // Fermer les ressources
            try {
                if (rs != null)
                    rs.close();
                if (stmt != null)
                    stmt.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Erreur lors de la fermeture des ressources", e);
            }
        }
    }

    @Override
    public List<Doctor> getAllDoctors() throws DAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(SQL_SELECT_ALL_DOCTORS);

            rs = stmt.executeQuery();

            List<Doctor> doctors = new ArrayList<>();
            while (rs.next()) {
                doctors.add(createDoctorFromResultSet(rs));
            }

            return doctors;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération de tous les médecins", e);
            throw new DAOException("Erreur lors de la récupération des médecins: " + e.getMessage(),
                    e, DAOException.RETRIEVAL_ERROR);
        } finally {
            // Fermer les ressources
            try {
                if (rs != null)
                    rs.close();
                if (stmt != null)
                    stmt.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Erreur lors de la fermeture des ressources", e);
            }
        }
    }

    @Override
    public List<Doctor> getDoctorsBySpecialty(String specialty) throws DAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(SQL_SELECT_DOCTORS_BY_SPECIALTY);
            stmt.setString(1, specialty);

            rs = stmt.executeQuery();

            List<Doctor> doctors = new ArrayList<>();
            while (rs.next()) {
                doctors.add(createDoctorFromResultSet(rs));
            }

            return doctors;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération des médecins par spécialité", e);
            throw new DAOException("Erreur lors de la récupération des médecins: " + e.getMessage(),
                    e, DAOException.RETRIEVAL_ERROR);
        } finally {
            // Fermer les ressources
            try {
                if (rs != null)
                    rs.close();
                if (stmt != null)
                    stmt.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Erreur lors de la fermeture des ressources", e);
            }
        }
    }

    @Override
    public List<String> getAllSpecialties() throws DAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(SQL_SELECT_ALL_SPECIALTIES);

            rs = stmt.executeQuery();

            List<String> specialties = new ArrayList<>();
            while (rs.next()) {
                specialties.add(rs.getString("specialite"));
            }

            return specialties;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération des spécialités", e);
            throw new DAOException("Erreur lors de la récupération des spécialités: " + e.getMessage(),
                    e, DAOException.RETRIEVAL_ERROR);
        } finally {
            // Fermer les ressources
            try {
                if (rs != null)
                    rs.close();
                if (stmt != null)
                    stmt.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Erreur lors de la fermeture des ressources", e);
            }
        }
    }

    @Override
    public boolean isUsernameExists(String username) throws DAOException {
        User user = getUserByUsername(username);
        return user != null;
    }

    @Override
    public void changePassword(int userId, String newPasswordHash) throws DAOException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(SQL_CHANGE_PASSWORD);
            stmt.setString(1, newPasswordHash);
            stmt.setInt(2, userId);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new DAOException("Le changement de mot de passe a échoué, aucune ligne affectée.",
                        DAOException.UPDATE_ERROR);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du changement de mot de passe", e);
            throw new DAOException("Erreur lors du changement de mot de passe: " + e.getMessage(),
                    e, DAOException.UPDATE_ERROR);
        } finally {
            // Fermer les ressources
            // Fermer les ressources
            try {
                if (stmt != null)
                    stmt.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Erreur lors de la fermeture des ressources", e);
            }
        }
    }
}