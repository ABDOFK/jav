package config;

/**
 * Classe de configuration centralisée de l'application.
 * Contient toutes les constantes et paramètres de configuration.
 */
public class AppConfig {
    // Informations sur l'application
    public static final String APP_NAME = "Gestion des Rendez-vous Médicaux";
    public static final String APP_VERSION = "1.0.0";
    
    // Configuration de l'interface utilisateur
    public static final int WINDOW_WIDTH = 1024;
    public static final int WINDOW_HEIGHT = 768;
    public static final String APP_ICON_PATH = "/resources/icon.png";
    
    // Formats de date/heure
    public static final String DATE_FORMAT = "dd/MM/yyyy";
    public static final String TIME_FORMAT = "HH:mm";
    public static final String DATETIME_FORMAT = "dd/MM/yyyy HH:mm";
    
    // Durées par défaut des rendez-vous (en minutes)
    public static final int[] DEFAULT_APPOINTMENT_DURATIONS = {15, 30, 45, 60};
    
    // Horaires de travail typiques
    public static final String WORK_START_TIME = "08:00";
    public static final String WORK_END_TIME = "18:00";
    
    // Configuration des exports
    public static final String EXPORT_DIRECTORY = System.getProperty("user.home") + "/Documents/MedicalAppointments/exports/";
    public static final String PDF_EXPORT_PREFIX = "planning_";
    
    // Limites et contraintes diverses
    public static final int MAX_LOGIN_ATTEMPTS = 3;
    public static final int MIN_PASSWORD_LENGTH = 6;
    
    // Types de consultation
    public static final String[] APPOINTMENT_TYPES = {
        "Consultation standard", 
        "Première consultation", 
        "Urgence", 
        "Suivi", 
        "Contrôle"
    };
    
    // Messages d'erreur courants
    public static class ErrorMessages {
        public static final String DB_CONNECTION_ERROR = "Erreur de connexion à la base de données.";
        public static final String LOGIN_FAILED = "Nom d'utilisateur ou mot de passe incorrect.";
        public static final String PATIENT_NOT_FOUND = "Patient non trouvé.";
        public static final String APPOINTMENT_CONFLICT = "Conflit d'horaire : ce créneau n'est plus disponible.";
        public static final String GENERIC_ERROR = "Une erreur s'est produite. Veuillez réessayer.";
    }
    
    // Messages de succès courants
    public static class SuccessMessages {
        public static final String LOGIN_SUCCESS = "Connexion réussie.";
        public static final String PATIENT_ADDED = "Patient ajouté avec succès.";
        public static final String PATIENT_UPDATED = "Informations patient mises à jour.";
        public static final String APPOINTMENT_CREATED = "Rendez-vous créé avec succès.";
        public static final String APPOINTMENT_MODIFIED = "Rendez-vous modifié avec succès.";
        public static final String APPOINTMENT_CANCELLED = "Rendez-vous annulé avec succès.";
        public static final String PLANNING_EXPORTED = "Planning exporté avec succès.";
    }
    
    // Empêcher l'instanciation de cette classe utilitaire
    private AppConfig() {
        throw new AssertionError("Cette classe ne doit pas être instanciée");
    }
}