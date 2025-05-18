package dao;

/**
 * Exception personnalisée pour la couche DAO.
 * Permet de centraliser la gestion des erreurs liées à l'accès aux données.
 */
public class DAOException extends Exception {
    
    /**
     * Code de sérialisation pour Serializable
     */
    private static final long serialVersionUID = 1L;

    /**
     * Code d'erreur pour identifier le type spécifique d'erreur
     */
    private final int errorCode;
    
    /**
     * Constantes pour les codes d'erreur courants
     */
    public static final int CONNECTION_ERROR = 1000;
    public static final int INSERTION_ERROR = 1001;
    public static final int UPDATE_ERROR = 1002;
    public static final int DELETION_ERROR = 1003;
    public static final int RETRIEVAL_ERROR = 1004;
    public static final int DUPLICATE_ERROR = 1005;
    public static final int CONSTRAINT_VIOLATION = 1006;
    public static final int TRANSACTION_ERROR = 1007;
    public static final int UNKNOWN_ERROR = 9999;
    
    /**
     * Constructeur avec message d'erreur
     * 
     * @param message Message d'erreur détaillé
     */
    public DAOException(String message) {
        super(message);
        this.errorCode = UNKNOWN_ERROR;
    }
    
    /**
     * Constructeur avec message d'erreur et code d'erreur
     * 
     * @param message Message d'erreur détaillé
     * @param errorCode Code d'erreur spécifique
     */
    public DAOException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    /**
     * Constructeur avec message d'erreur et cause
     * 
     * @param message Message d'erreur détaillé
     * @param cause Exception source de l'erreur
     */
    public DAOException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = UNKNOWN_ERROR;
    }
    
    /**
     * Constructeur complet avec message, cause et code d'erreur
     * 
     * @param message Message d'erreur détaillé
     * @param cause Exception source de l'erreur
     * @param errorCode Code d'erreur spécifique
     */
    public DAOException(String message, Throwable cause, int errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    /**
     * Récupère le code d'erreur associé à cette exception
     * 
     * @return Le code d'erreur
     */
    public int getErrorCode() {
        return errorCode;
    }
    
    /**
     * Vérifie si l'erreur est liée à un problème de connexion
     * 
     * @return true si c'est une erreur de connexion, false sinon
     */
    public boolean isConnectionError() {
        return errorCode == CONNECTION_ERROR;
    }
    
    /**
     * Vérifie si l'erreur est liée à une violation de contrainte (clé étrangère, unicité, etc.)
     * 
     * @return true si c'est une violation de contrainte, false sinon
     */
    public boolean isConstraintViolation() {
        return errorCode == CONSTRAINT_VIOLATION;
    }
    
    /**
     * Vérifie si l'erreur est liée à une duplication de données (unicité)
     * 
     * @return true si c'est une erreur de duplication, false sinon
     */
    public boolean isDuplicateError() {
        return errorCode == DUPLICATE_ERROR;
    }
    
    /**
     * Génère une représentation textuelle cohérente de l'exception
     */
    @Override
    public String toString() {
        return "DAOException [code=" + errorCode + "] " + getMessage();
    }
}