# Gestion des Rendez-vous Médicaux

Système de gestion de cabinet médical permettant aux secrétaires et médecins de gérer les rendez-vous, patients, et planning.

## Fonctionnalités

- Authentification des utilisateurs (secrétaires et médecins)
- Gestion des patients (ajout, modification, recherche)
- Gestion des rendez-vous (création, modification, annulation)
- Planning journalier et hebdomadaire des médecins
- Exports PDF des plannings

## Configuration technique

- Java 11+
- Interface graphique Swing
- Base de données MySQL

## Démarrage

1. Assurez-vous d'avoir installé Java 11 ou supérieur
2. Configurez la base de données en utilisant le script schema.sql
3. Lancez l'application via la classe src/view/LoginView.java

## Utilisateurs par défaut

- Secrétaire: username "secretaire", mot de passe "secret"
- Médecin: username "docteur", mot de passe "secret"