package model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Objects;

/**
 * Classe représentant un patient dans le système.
 * Contient toutes les informations personnelles et administratives d'un patient.
 */
public class Patient {
    
    // Attributs d'un patient
    private int id;
    private String lastName;
    private String firstName;
    private LocalDate birthDate;
    private String phone;
    private String address;
    private String email;
    private String administrativeNotes;
    private LocalDateTime creationDate;
    
    /**
     * Constructeur par défaut
     */
    public Patient() {
        this.creationDate = LocalDateTime.now();
    }
    
    /**
     * Constructeur avec paramètres essentiels
     * 
     * @param lastName Nom de famille du patient
     * @param firstName Prénom du patient
     * @param birthDate Date de naissance
     * @param phone Numéro de téléphone
     */
    public Patient(String lastName, String firstName, LocalDate birthDate, String phone) {
        this();
        this.lastName = lastName;
        this.firstName = firstName;
        this.birthDate = birthDate;
        this.phone = phone;
    }
    
    /**
     * Constructeur complet
     * 
     * @param id Identifiant unique
     * @param lastName Nom de famille du patient
     * @param firstName Prénom du patient
     * @param birthDate Date de naissance
     * @param phone Numéro de téléphone
     * @param address Adresse postale
     * @param email Adresse email
     * @param administrativeNotes Notes administratives
     * @param creationDate Date de création de la fiche
     */
    public Patient(int id, String lastName, String firstName, LocalDate birthDate, 
                  String phone, String address, String email, 
                  String administrativeNotes, LocalDateTime creationDate) {
        this.id = id;
        this.lastName = lastName;
        this.firstName = firstName;
        this.birthDate = birthDate;
        this.phone = phone;
        this.address = address;
        this.email = email;
        this.administrativeNotes = administrativeNotes;
        this.creationDate = creationDate;
    }

    // Getters et Setters
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAdministrativeNotes() {
        return administrativeNotes;
    }

    public void setAdministrativeNotes(String administrativeNotes) {
        this.administrativeNotes = administrativeNotes;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }
    
    /**
     * Calcule l'âge du patient à partir de sa date de naissance
     * 
     * @return L'âge du patient en années
     */
    public int getAge() {
        if (birthDate == null) {
            return 0;
        }
        return Period.between(birthDate, LocalDate.now()).getYears();
    }
    
    /**
     * Récupère le nom complet du patient (prénom + nom)
     * 
     * @return Le nom complet formaté
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    /**
     * Méthode utilitaire pour vérifier si le patient a une adresse email
     * 
     * @return true si une adresse email est définie, false sinon
     */
    public boolean hasEmail() {
        return email != null && !email.trim().isEmpty();
    }
    
    /**
     * Méthode utilitaire pour vérifier si le patient a une adresse postale
     * 
     * @return true si une adresse postale est définie, false sinon
     */
    public boolean hasAddress() {
        return address != null && !address.trim().isEmpty();
    }
    
    /**
     * Méthode utilitaire pour vérifier si le patient a des notes administratives
     * 
     * @return true si des notes administratives sont définies, false sinon
     */
    public boolean hasNotes() {
        return administrativeNotes != null && !administrativeNotes.trim().isEmpty();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Patient patient = (Patient) o;
        return id == patient.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return lastName.toUpperCase() + " " + firstName + 
               (birthDate != null ? " (" + getAge() + " ans)" : "");
    }
}