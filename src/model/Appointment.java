package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import config.AppConfig;

/**
 * Classe représentant un rendez-vous dans le système.
 * Contient les informations complètes d'un rendez-vous, y compris les références
 * au patient et au médecin concernés.
 */
public class Appointment {
    
    // Attributs d'un rendez-vous
    private int id;
    private int patientId;
    private int doctorId;
    private int secretaryId; // ID de la secrétaire qui a créé le RDV
    private LocalDateTime startDateTime;
    private int durationMinutes;
    private String appointmentType;
    private AppointmentStatus status;
    private String notes;
    private LocalDateTime creationDateTime;
    private LocalDateTime lastModifiedDateTime;
    
    // Objets liés (non persistés directement, mais utilisés pour faciliter l'affichage)
    private Patient patient;
    private Doctor doctor;
    private Secretary secretary;
    
    /**
     * Constructeur par défaut
     */
    public Appointment() {
        this.status = AppointmentStatus.PLANIFIE;
        this.creationDateTime = LocalDateTime.now();
        this.lastModifiedDateTime = LocalDateTime.now();
    }
    
    /**
     * Constructeur avec paramètres essentiels
     * 
     * @param patientId ID du patient
     * @param doctorId ID du médecin
     * @param secretaryId ID de la secrétaire
     * @param startDateTime Date et heure de début
     * @param durationMinutes Durée en minutes
     */
    public Appointment(int patientId, int doctorId, int secretaryId, 
                      LocalDateTime startDateTime, int durationMinutes) {
        this();
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.secretaryId = secretaryId;
        this.startDateTime = startDateTime;
        this.durationMinutes = durationMinutes;
    }
    
    /**
     * Constructeur complet
     * 
     * @param id Identifiant unique
     * @param patientId ID du patient
     * @param doctorId ID du médecin
     * @param secretaryId ID de la secrétaire
     * @param startDateTime Date et heure de début
     * @param durationMinutes Durée en minutes
     * @param appointmentType Type de consultation
     * @param status Statut du rendez-vous
     * @param notes Notes spécifiques
     * @param creationDateTime Date et heure de création
     * @param lastModifiedDateTime Date et heure de dernière modification
     */
    public Appointment(int id, int patientId, int doctorId, int secretaryId, 
                      LocalDateTime startDateTime, int durationMinutes,
                      String appointmentType, AppointmentStatus status,
                      String notes, LocalDateTime creationDateTime,
                      LocalDateTime lastModifiedDateTime) {
        this.id = id;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.secretaryId = secretaryId;
        this.startDateTime = startDateTime;
        this.durationMinutes = durationMinutes;
        this.appointmentType = appointmentType;
        this.status = status;
        this.notes = notes;
        this.creationDateTime = creationDateTime;
        this.lastModifiedDateTime = lastModifiedDateTime;
    }
    
    // Getters et Setters
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public int getSecretaryId() {
        return secretaryId;
    }

    public void setSecretaryId(int secretaryId) {
        this.secretaryId = secretaryId;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getAppointmentType() {
        return appointmentType;
    }

    public void setAppointmentType(String appointmentType) {
        this.appointmentType = appointmentType;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreationDateTime() {
        return creationDateTime;
    }

    public void setCreationDateTime(LocalDateTime creationDateTime) {
        this.creationDateTime = creationDateTime;
    }

    public LocalDateTime getLastModifiedDateTime() {
        return lastModifiedDateTime;
    }

    public void setLastModifiedDateTime(LocalDateTime lastModifiedDateTime) {
        this.lastModifiedDateTime = lastModifiedDateTime;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public Secretary getSecretary() {
        return secretary;
    }

    public void setSecretary(Secretary secretary) {
        this.secretary = secretary;
    }
    
    /**
     * Calcule l'heure de fin du rendez-vous
     * 
     * @return Date et heure de fin
     */
    public LocalDateTime getEndDateTime() {
        return startDateTime.plusMinutes(durationMinutes);
    }
    
    /**
     * Vérifie si le rendez-vous est actif (non annulé et non terminé)
     * 
     * @return true si le rendez-vous est actif, false sinon
     */
    public boolean isActive() {
        return status.isActive();
    }
    
    /**
     * Vérifie si le rendez-vous est annulé
     * 
     * @return true si le rendez-vous est annulé, false sinon
     */
    public boolean isCancelled() {
        return status.isCancelled();
    }
    
    /**
     * Vérifie si le rendez-vous est terminé
     * 
     * @return true si le rendez-vous est terminé, false sinon
     */
    public boolean isCompleted() {
        return status.isCompleted();
    }
    
    /**
     * Vérifie si le rendez-vous a lieu à une date future
     * 
     * @return true si le rendez-vous est dans le futur, false sinon
     */
    public boolean isFuture() {
        return startDateTime.isAfter(LocalDateTime.now());
    }
    
    /**
     * Vérifie si le rendez-vous a lieu aujourd'hui
     * 
     * @return true si le rendez-vous a lieu aujourd'hui, false sinon
     */
    public boolean isToday() {
        return startDateTime.toLocalDate().equals(LocalDateTime.now().toLocalDate());
    }
    
    /**
     * Vérifie si deux rendez-vous sont en conflit (même médecin, même période)
     * 
     * @param other L'autre rendez-vous à comparer
     * @return true s'il y a conflit, false sinon
     */
    public boolean conflictsWith(Appointment other) {
        // Si ce n'est pas le même médecin, pas de conflit
        if (this.doctorId != other.doctorId) {
            return false;
        }
        
        // Un rendez-vous ne peut pas être en conflit avec lui-même
        if (this.id == other.id && this.id != 0) {
            return false;
        }
        
        // Calcul des heures de début et de fin pour les deux rendez-vous
        LocalDateTime thisStart = this.startDateTime;
        LocalDateTime thisEnd = this.getEndDateTime();
        LocalDateTime otherStart = other.startDateTime;
        LocalDateTime otherEnd = other.getEndDateTime();
        
        // Vérification du chevauchement
        return (thisStart.isBefore(otherEnd) && thisEnd.isAfter(otherStart));
    }
    
    /**
     * Met à jour la date de dernière modification avec la date actuelle
     */
    public void updateLastModified() {
        this.lastModifiedDateTime = LocalDateTime.now();
    }
    
    /**
     * Renvoie une représentation formatée de la date et l'heure de début
     * 
     * @return Date et heure formatées
     */
    public String getFormattedDateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(AppConfig.DATETIME_FORMAT);
        return startDateTime.format(formatter);
    }
    
    /**
     * Renvoie une représentation formatée de l'heure de début
     * 
     * @return Heure formatée
     */
    public String getFormattedTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(AppConfig.TIME_FORMAT);
        return startDateTime.format(formatter);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Appointment that = (Appointment) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        String patientName = (patient != null) ? patient.getFullName() : "Patient #" + patientId;
        String doctorName = (doctor != null) ? doctor.getFullName() : "Médecin #" + doctorId;
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(AppConfig.DATETIME_FORMAT);
        return "RDV le " + startDateTime.format(formatter) + 
               " (" + durationMinutes + " min) - " + 
               patientName + " avec " + doctorName + 
               " - Statut: " + status.getLabel();
    }
}