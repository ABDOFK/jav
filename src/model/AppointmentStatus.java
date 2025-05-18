package model;

/**
 * Énumération des différents statuts possibles pour un rendez-vous.
 * Utilisée pour maintenir la cohérence et éviter les erreurs de saisie.
 */
public enum AppointmentStatus {
    PLANIFIE("Planifié", "Le rendez-vous est prévu"),
    CONFIRME("Confirmé", "Le rendez-vous a été confirmé par le patient"),
    ANNULE_PATIENT("Annulé par patient", "Le patient a annulé le rendez-vous"),
    ANNULE_CABINET("Annulé par cabinet", "Le cabinet a annulé le rendez-vous"),
    REALISE("Réalisé", "Le rendez-vous a eu lieu"),
    ABSENT("Patient absent", "Le patient ne s'est pas présenté");
    
    private final String label;
    private final String description;
    
    /**
     * Constructeur privé pour l'énumération
     * @param label Libellé court pour l'affichage
     * @param description Description plus détaillée du statut
     */
    private AppointmentStatus(String label, String description) {
        this.label = label;
        this.description = description;
    }
    
    /**
     * Obtient le libellé du statut
     * @return Le libellé du statut pour l'affichage
     */
    public String getLabel() {
        return label;
    }
    
    /**
     * Obtient la description du statut
     * @return La description détaillée du statut
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Convertit la valeur de l'énumération en chaîne de caractères utilisable pour l'affichage
     * @return Le libellé du statut
     */
    @Override
    public String toString() {
        return label;
    }
    
    /**
     * Obtient un statut à partir de son libellé
     * @param label Le libellé du statut à rechercher
     * @return Le statut correspondant ou PLANIFIE par défaut si non trouvé
     */
    public static AppointmentStatus fromLabel(String label) {
        for (AppointmentStatus status : values()) {
            if (status.getLabel().equalsIgnoreCase(label)) {
                return status;
            }
        }
        return PLANIFIE; // Valeur par défaut
    }
    
    /**
     * Vérifie si le rendez-vous est actif (non annulé et non terminé)
     * @return true si le rendez-vous est actif, false sinon
     */
    public boolean isActive() {
        return this == PLANIFIE || this == CONFIRME;
    }
    
    /**
     * Vérifie si le rendez-vous est annulé (par le patient ou le cabinet)
     * @return true si le rendez-vous est annulé, false sinon
     */
    public boolean isCancelled() {
        return this == ANNULE_PATIENT || this == ANNULE_CABINET;
    }
    
    /**
     * Vérifie si le rendez-vous est terminé (réalisé ou patient absent)
     * @return true si le rendez-vous est terminé, false sinon
     */
    public boolean isCompleted() {
        return this == REALISE || this == ABSENT;
    }
}