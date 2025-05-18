-- Base de données pour l'application de gestion des rendez-vous médicaux
-- Schéma de base de données

CREATE DATABASE IF NOT EXISTS medical_appointments;
USE medical_appointments;

-- Table des utilisateurs (médecins et secrétaires)
CREATE TABLE utilisateurs (
    id_utilisateur INT AUTO_INCREMENT PRIMARY KEY,
    nom_utilisateur VARCHAR(50) NOT NULL UNIQUE,
    mot_de_passe_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL, -- MEDECIN ou SECRETAIRE
    nom_complet VARCHAR(100) NOT NULL,
    actif BOOLEAN NOT NULL DEFAULT TRUE,
    date_creation_compte DATETIME NOT NULL,
    
    INDEX idx_utilisateurs_role (role),
    INDEX idx_utilisateurs_username (nom_utilisateur)
);

-- Table des médecins (extension de la table utilisateurs)
CREATE TABLE medecins (
    id_medecin INT PRIMARY KEY,
    specialite VARCHAR(100) NOT NULL,
    horaires_disponibilite TEXT,
    telephone_professionnel VARCHAR(20),
    
    FOREIGN KEY (id_medecin) REFERENCES utilisateurs(id_utilisateur) ON DELETE CASCADE,
    INDEX idx_medecins_specialite (specialite)
);

-- Table des patients
CREATE TABLE patients (
    id_patient INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(50) NOT NULL,
    prenom VARCHAR(50) NOT NULL,
    date_naissance DATE,
    telephone VARCHAR(20) NOT NULL,
    adresse TEXT,
    email VARCHAR(100),
    notes_administratives TEXT,
    date_creation_fiche DATETIME NOT NULL,
    
    INDEX idx_patients_nom (nom, prenom),
    INDEX idx_patients_telephone (telephone)
);

-- Table des rendez-vous
CREATE TABLE rendez_vous (
    id_rendezvous INT AUTO_INCREMENT PRIMARY KEY,
    id_patient_fk INT NOT NULL,
    id_medecin_fk INT NOT NULL,
    id_secretaire_creation_fk INT NOT NULL,
    date_heure_debut DATETIME NOT NULL,
    duree_minutes INT NOT NULL,
    type_consultation VARCHAR(50),
    statut_rdv VARCHAR(30) NOT NULL, -- PLANIFIE, CONFIRME, ANNULE_PATIENT, ANNULE_CABINET, REALISE, ABSENT
    notes_rdv TEXT,
    date_creation_rdv DATETIME NOT NULL,
    date_derniere_maj_rdv DATETIME NOT NULL,
    
    FOREIGN KEY (id_patient_fk) REFERENCES patients(id_patient) ON DELETE RESTRICT,
    FOREIGN KEY (id_medecin_fk) REFERENCES medecins(id_medecin) ON DELETE RESTRICT,
    FOREIGN KEY (id_secretaire_creation_fk) REFERENCES utilisateurs(id_utilisateur) ON DELETE RESTRICT,
    
    INDEX idx_rdv_patient (id_patient_fk),
    INDEX idx_rdv_medecin (id_medecin_fk),
    INDEX idx_rdv_date (date_heure_debut),
    INDEX idx_rdv_statut (statut_rdv)
);

-- Contrainte pour éviter les doublons de rendez-vous (même médecin, même créneau)
-- Cette contrainte est gérée dans le code de l'application via la méthode hasAppointmentConflict