package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Classe représentant un médecin dans le système.
 * Étend la classe abstraite User avec des fonctionnalités spécifiques au rôle de médecin.
 */
public class Doctor extends User {
    
    // Rôle constant pour les médecins
    public static final String ROLE = "MEDECIN";
    
    // Attributs spécifiques au médecin
    private String specialty;
    private String workHours; // Format JSON ou texte structuré pour la flexibilité
    private String professionalPhone;
    
    /**
     * Constructeur par défaut
     */
    public Doctor() {
        super();
        setRole(ROLE);
    }
    
    /**
     * Constructeur avec paramètres essentiels
     * 
     * @param username Nom d'utilisateur pour la connexion
     * @param passwordHash Mot de passe haché pour l'authentification
     * @param fullName Nom complet du médecin
     * @param specialty Spécialité médicale du médecin
     */
    public Doctor(String username, String passwordHash, String fullName, String specialty) {
        super(username, passwordHash, ROLE, fullName);
        this.specialty = specialty;
    }
    
    /**
     * Constructeur complet
     * 
     * @param id Identifiant unique
     * @param username Nom d'utilisateur pour la connexion
     * @param passwordHash Mot de passe haché pour l'authentification
     * @param fullName Nom complet du médecin
     * @param active État du compte (actif/inactif)
     * @param creationDate Date de création du compte
     * @param specialty Spécialité médicale du médecin
     * @param workHours Horaires de travail formatés
     * @param professionalPhone Téléphone professionnel
     */
    public Doctor(int id, String username, String passwordHash, String fullName, 
                  boolean active, LocalDateTime creationDate, String specialty, 
                  String workHours, String professionalPhone) {
        super(id, username, passwordHash, ROLE, fullName, active, creationDate);
        this.specialty = specialty;
        this.workHours = workHours;
        this.professionalPhone = professionalPhone;
    }

    // Getters et setters spécifiques

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public String getWorkHours() {
        return workHours;
    }

    public void setWorkHours(String workHours) {
        this.workHours = workHours;
    }

    public String getProfessionalPhone() {
        return professionalPhone;
    }

    public void setProfessionalPhone(String professionalPhone) {
        this.professionalPhone = professionalPhone;
    }
    
    /**
     * Implémentation de la méthode abstraite de User
     * 
     * @return Le type d'utilisateur (Médecin)
     */
    @Override
    public String getUserType() {
        return "Médecin";
    }
    
    /**
     * Construit une représentation structurée des horaires de travail à partir des données brutes
     * 
     * @return Une map jours-créneaux horaires
     */
    public Map<String, List<String>> getStructuredWorkHours() {
        Map<String, List<String>> structured = new HashMap<>();
        
        // Si pas d'horaires définis, retourner une map vide
        if (workHours == null || workHours.isEmpty()) {
            return structured;
        }
        
        // Format simplifié pour ce prototype
        // Exemple: "lundi:09:00-12:30,14:00-18:00;mardi:09:00-12:30,14:00-18:00"
        String[] dayEntries = workHours.split(";");
        for (String dayEntry : dayEntries) {
            String[] parts = dayEntry.split(":");
            if (parts.length == 2) {
                String day = parts[0].trim();
                String[] timeSlots = parts[1].split(",");
                
                List<String> slots = new ArrayList<>();
                for (String slot : timeSlots) {
                    slots.add(slot.trim());
                }
                
                structured.put(day, slots);
            }
        }
        
        return structured;
    }
    
    /**
     * Met à jour les horaires de travail à partir d'une structure de données
     * 
     * @param structuredHours Map jours-créneaux horaires
     */
    public void setStructuredWorkHours(Map<String, List<String>> structuredHours) {
        StringBuilder builder = new StringBuilder();
        
        boolean firstDay = true;
        for (Map.Entry<String, List<String>> entry : structuredHours.entrySet()) {
            if (!firstDay) {
                builder.append(";");
            }
            
            builder.append(entry.getKey()).append(":");
            
            List<String> slots = entry.getValue();
            for (int i = 0; i < slots.size(); i++) {
                if (i > 0) {
                    builder.append(",");
                }
                builder.append(slots.get(i));
            }
            
            firstDay = false;
        }
        
        this.workHours = builder.toString();
    }
    
    /**
     * Vérifie si le médecin peut consulter son propre planning
     * 
     * @return true car les médecins peuvent toujours consulter leur planning
     */
    public boolean canViewOwnPlanning() {
        return true;
    }
    
    /**
     * Vérifie si le médecin peut exporter son propre planning
     * 
     * @return true car les médecins peuvent toujours exporter leur planning
     */
    public boolean canExportOwnPlanning() {
        return true;
    }
    
    @Override
    public String toString() {
        return "Dr. " + getFullName() + " (" + specialty + ")";
    }
}