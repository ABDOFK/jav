package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gère la connexion à la base de données MySQL en utilisant le pattern Singleton
 * pour assurer qu'une seule instance de connexion est utilisée dans l'application.
 */
public class DatabaseConnection {
    
    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());
    
    // Constantes de connexion à la base de données
    private static final String DB_URL = "jdbc:mysql://localhost:3306/medical_appointments";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";
    private static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";
    
    // Instance unique (Singleton)
    private static DatabaseConnection instance;
    
    // La connexion à la base de données
    private Connection connection;
    
    /**
     * Constructeur privé (Singleton)
     * Initialise la connexion à la base de données.
     */
    private DatabaseConnection() {
        try {
            // Charger le driver JDBC
            Class.forName(DB_DRIVER);
            
            // Établir la connexion
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            LOGGER.info("Connexion à la base de données établie avec succès");
            
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Driver JDBC non trouvé", e);
            throw new RuntimeException("Driver JDBC non trouvé", e);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Échec de connexion à la base de données", e);
            throw new RuntimeException("Échec de connexion à la base de données", e);
        }
    }
    
    /**
     * Obtient l'instance unique de la connexion (Singleton)
     * @return L'instance de DatabaseConnection
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }
    
    /**
     * Récupère la connexion à la base de données
     * @return L'objet Connection
     */
    public Connection getConnection() {
        try {
            // Vérifier si la connexion est fermée ou invalide
            if (connection == null || connection.isClosed()) {
                // Rétablir la connexion
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                LOGGER.info("Reconnexion à la base de données établie");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la vérification/reconnexion", e);
            throw new RuntimeException("Erreur lors de la vérification/reconnexion", e);
        }
        
        return connection;
    }
    
    /**
     * Ferme la connexion à la base de données
     */
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                LOGGER.info("Connexion à la base de données fermée");
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Erreur lors de la fermeture de la connexion", e);
            } finally {
                connection = null;
            }
        }
    }
}
