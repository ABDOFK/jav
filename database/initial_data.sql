-- Données initiales pour l'application de gestion des rendez-vous médicaux
USE medical_appointments;

-- Insertion des utilisateurs (secrétaires et médecins)
-- Note: Les mots de passe sont hachés avec SHA-256
-- Le mot de passe "secret" a pour hash: "2bb80d537b1da3e38bd30361aa855686bde0eacd7162fef6a25fe97bf527a25b"

-- Secrétaires
INSERT INTO utilisateurs (nom_utilisateur, mot_de_passe_hash, role, nom_complet, actif, date_creation_compte)
VALUES 
('secretaire1', '2bb80d537b1da3e38bd30361aa855686bde0eacd7162fef6a25fe97bf527a25b', 'SECRETAIRE', 'Sophie Martin', TRUE, NOW()),
('secretaire2', '2bb80d537b1da3e38bd30361aa855686bde0eacd7162fef6a25fe97bf527a25b', 'SECRETAIRE', 'Julie Dubois', TRUE, NOW());

-- Médecins
INSERT INTO utilisateurs (nom_utilisateur, mot_de_passe_hash, role, nom_complet, actif, date_creation_compte)
VALUES 
('docteur1', '2bb80d537b1da3e38bd30361aa855686bde0eacd7162fef6a25fe97bf527a25b', 'MEDECIN', 'Dr. Thomas Petit', TRUE, NOW()),
('docteur2', '2bb80d537b1da3e38bd30361aa855686bde0eacd7162fef6a25fe97bf527a25b', 'MEDECIN', 'Dr. Marie Leroy', TRUE, NOW()),
('docteur3', '2bb80d537b1da3e38bd30361aa855686bde0eacd7162fef6a25fe97bf527a25b', 'MEDECIN', 'Dr. Philippe Bernard', TRUE, NOW());

-- Ajout des informations spécifiques des médecins
INSERT INTO medecins (id_medecin, specialite, horaires_disponibilite, telephone_professionnel)
VALUES 
((SELECT id_utilisateur FROM utilisateurs WHERE nom_utilisateur = 'docteur1'), 
 'Médecine générale', 
 'lundi:09:00-12:00,14:00-18:00;mardi:09:00-12:00,14:00-18:00;mercredi:09:00-12:00;jeudi:09:00-12:00,14:00-18:00;vendredi:09:00-12:00,14:00-18:00', 
 '0612345678'),
 
((SELECT id_utilisateur FROM utilisateurs WHERE nom_utilisateur = 'docteur2'), 
 'Pédiatrie', 
 'lundi:09:00-12:00,14:00-17:00;mardi:09:00-12:00,14:00-17:00;jeudi:09:00-12:00,14:00-17:00;vendredi:09:00-12:00,14:00-17:00', 
 '0623456789'),
 
((SELECT id_utilisateur FROM utilisateurs WHERE nom_utilisateur = 'docteur3'), 
 'Cardiologie', 
 'lundi:14:00-18:00;mardi:09:00-12:00,14:00-18:00;mercredi:09:00-12:00;jeudi:14:00-18:00;vendredi:09:00-12:00', 
 '0634567890');

-- Insertion de patients
INSERT INTO patients (nom, prenom, date_naissance, telephone, adresse, email, notes_administratives, date_creation_fiche)
VALUES 
('Dupont', 'Jean', '1975-05-12', '0612345678', '15 rue des Lilas, 75001 Paris', 'jean.dupont@email.com', 'RAS', NOW()),
('Martin', 'Lucie', '1982-09-24', '0623456789', '8 avenue Victor Hugo, 75016 Paris', 'lucie.martin@email.com', 'Allergie pénicilline', NOW()),
('Robert', 'Marc', '1965-03-18', '0634567890', '23 boulevard Voltaire, 75011 Paris', 'marc.robert@email.com', 'Hypertension', NOW()),
('Durand', 'Sophie', '1990-11-30', '0645678901', '42 rue du Commerce, 75015 Paris', 'sophie.durand@email.com', 'RAS', NOW()),
('Leroy', 'Thomas', '1988-07-05', '0656789012', '3 place de la République, 75003 Paris', 'thomas.leroy@email.com', 'Asthmatique', NOW()),
('Moreau', 'Camille', '1995-01-15', '0667890123', '17 rue Saint-Antoine, 75004 Paris', 'camille.moreau@email.com', 'Enceinte - 6 mois', NOW()),
('Petit', 'Alice', '1979-08-22', '0678901234', '29 rue de Vaugirard, 75006 Paris', 'alice.petit@email.com', 'Diabétique type 2', NOW()),
('Roux', 'Antoine', '1970-12-03', '0689012345', '14 rue Mouffetard, 75005 Paris', 'antoine.roux@email.com', 'Suivi cardiaque', NOW()),
('Simon', 'Léa', '1985-04-27', '0690123456', '55 avenue des Gobelins, 75013 Paris', 'lea.simon@email.com', 'RAS', NOW()),
('Michel', 'Lucas', '1992-06-09', '0601234567', '7 rue de la Roquette, 75011 Paris', 'lucas.michel@email.com', 'Allergie fruits de mer', NOW());

-- Insertion de quelques rendez-vous (à adapter avec les dates actuelles)
INSERT INTO rendez_vous (id_patient_fk, id_medecin_fk, id_secretaire_creation_fk, date_heure_debut, duree_minutes, 
                       type_consultation, statut_rdv, notes_rdv, date_creation_rdv, date_derniere_maj_rdv)
VALUES 
-- Pour le Dr. Thomas Petit (Médecin généraliste)
((SELECT id_patient FROM patients WHERE nom = 'Dupont' AND prenom = 'Jean'),
 (SELECT id_medecin FROM medecins WHERE id_medecin = (SELECT id_utilisateur FROM utilisateurs WHERE nom_utilisateur = 'docteur1')),
 (SELECT id_utilisateur FROM utilisateurs WHERE nom_utilisateur = 'secretaire1'),
 DATE_ADD(CURRENT_DATE(), INTERVAL 1 DAY) + INTERVAL 9 HOUR,
 30,
 'Consultation standard',
 'CONFIRME',
 'Renouvellement ordonnance',
 NOW(), NOW()),

((SELECT id_patient FROM patients WHERE nom = 'Martin' AND prenom = 'Lucie'),
 (SELECT id_medecin FROM medecins WHERE id_medecin = (SELECT id_utilisateur FROM utilisateurs WHERE nom_utilisateur = 'docteur1')),
 (SELECT id_utilisateur FROM utilisateurs WHERE nom_utilisateur = 'secretaire1'),
 DATE_ADD(CURRENT_DATE(), INTERVAL 1 DAY) + INTERVAL 10 HOUR,
 30,
 'Consultation standard',
 'PLANIFIE',
 'Mal de gorge',
 NOW(), NOW()),

-- Pour Dr. Marie Leroy (Pédiatre)
((SELECT id_patient FROM patients WHERE nom = 'Durand' AND prenom = 'Sophie'),
 (SELECT id_medecin FROM medecins WHERE id_medecin = (SELECT id_utilisateur FROM utilisateurs WHERE nom_utilisateur = 'docteur2')),
 (SELECT id_utilisateur FROM utilisateurs WHERE nom_utilisateur = 'secretaire2'),
 DATE_ADD(CURRENT_DATE(), INTERVAL 2 DAY) + INTERVAL 14 HOUR + INTERVAL 30 MINUTE,
 45,
 'Première consultation',
 'PLANIFIE',
 'Consultation avec enfant 3 ans',
 NOW(), NOW()),

-- Pour Dr. Philippe Bernard (Cardiologue)
((SELECT id_patient FROM patients WHERE nom = 'Roux' AND prenom = 'Antoine'),
 (SELECT id_medecin FROM medecins WHERE id_medecin = (SELECT id_utilisateur FROM utilisateurs WHERE nom_utilisateur = 'docteur3')),
 (SELECT id_utilisateur FROM utilisateurs WHERE nom_utilisateur = 'secretaire2'),
 DATE_ADD(CURRENT_DATE(), INTERVAL 3 DAY) + INTERVAL 15 HOUR,
 60,
 'Suivi',
 'CONFIRME',
 'Contrôle post-opératoire',
 NOW(), NOW());