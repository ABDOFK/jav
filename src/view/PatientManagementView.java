package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import config.AppConfig;
import controller.AppointmentController;
import controller.PatientController;
import dao.DAOException;
import model.Appointment;
import model.Patient;
import util.DateTimeUtils;

/**
 * Vue pour la gestion des patients.
 * Permet aux utilisateurs de créer, modifier et rechercher des patients.
 */
public class PatientManagementView extends JPanel {
    
    private static final long serialVersionUID = 1L;
    
    // Contrôleurs
    private final PatientController patientController;
    private final AppointmentController appointmentController;
    
    // Composants de l'interface
    private JPanel searchPanel;
    private JPanel patientListPanel;
    private JPanel patientDetailsPanel;
    
    private JTextField searchLastNameField;
    private JTextField searchFirstNameField;
    private JTextField searchPhoneField;
    private JButton searchButton;
    
    private JTable patientTable;
    private DefaultTableModel tableModel;
    
    private JTextField lastNameField;
    private JTextField firstNameField;
    private JTextField birthDateField;
    private JTextField phoneField;
    private JTextField addressField;
    private JTextField emailField;
    private JTextArea notesArea;
    
    private JButton newButton;
    private JButton saveButton;
    private JButton cancelButton;
    private JButton deleteButton;
    private JButton appointmentsButton;
    
    // État courant
    private List<Patient> patients;
    private Patient currentPatient;
    private boolean editMode = false;
    
    /**
     * Constructeur par défaut
     */
    public PatientManagementView() {
        this.patientController = PatientController.getInstance();
        this.appointmentController = AppointmentController.getInstance();
        
        initializeUI();
        loadData();
    }
    
    /**
     * Initialise l'interface utilisateur
     */
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Panneau de recherche
        searchPanel = createSearchPanel();
        add(searchPanel, BorderLayout.NORTH);
        
        // Panneau de liste des patients
        patientListPanel = createPatientListPanel();
        add(patientListPanel, BorderLayout.CENTER);
        
        // Panneau de détails du patient
        patientDetailsPanel = createPatientDetailsPanel();
        add(patientDetailsPanel, BorderLayout.EAST);
        
        // Désactiver les champs de saisie au démarrage
        setFieldsEnabled(false);
    }
    
    /**
     * Crée le panneau de recherche des patients
     * 
     * @return Le panneau de recherche
     */
    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Recherche de patients"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Recherche par nom
        JLabel lastNameLabel = new JLabel("Nom :");
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(lastNameLabel, gbc);
        
        searchLastNameField = new JTextField(15);
        gbc.gridx = 1;
        gbc.gridy = 0;
        panel.add(searchLastNameField, gbc);
        
        // Recherche par prénom
        JLabel firstNameLabel = new JLabel("Prénom :");
        gbc.gridx = 2;
        gbc.gridy = 0;
        panel.add(firstNameLabel, gbc);
        
        searchFirstNameField = new JTextField(15);
        gbc.gridx = 3;
        gbc.gridy = 0;
        panel.add(searchFirstNameField, gbc);
        
        // Recherche par téléphone
        JLabel phoneLabel = new JLabel("Téléphone :");
        gbc.gridx = 4;
        gbc.gridy = 0;
        panel.add(phoneLabel, gbc);
        
        searchPhoneField = new JTextField(10);
        gbc.gridx = 5;
        gbc.gridy = 0;
        panel.add(searchPhoneField, gbc);
        
        // Bouton de recherche
        searchButton = new JButton("Rechercher");
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchPatients();
            }
        });
        gbc.gridx = 6;
        gbc.gridy = 0;
        panel.add(searchButton, gbc);
        
        return panel;
    }
    
    /**
     * Crée le panneau de liste des patients
     * 
     * @return Le panneau de liste
     */
    private JPanel createPatientListPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Liste des patients"));
        
        // Création du modèle de tableau
        String[] columnNames = {"Nom", "Prénom", "Date de naissance", "Téléphone", "Email"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Rendre toutes les cellules non éditables
            }
        };
        
        // Création du tableau
        patientTable = new JTable(tableModel);
        patientTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        patientTable.getTableHeader().setReorderingAllowed(false);
        
        // Gérer la sélection d'un patient
        patientTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int selectedRow = patientTable.getSelectedRow();
                    if (selectedRow >= 0 && selectedRow < patients.size()) {
                        displayPatient(patients.get(selectedRow));
                    }
                }
            }
        });
        
        // Panneau défilant pour le tableau
        JScrollPane scrollPane = new JScrollPane(patientTable);
        scrollPane.setPreferredSize(new Dimension(600, 300));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Boutons d'action
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        newButton = new JButton("Nouveau");
        newButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createNewPatient();
            }
        });
        buttonPanel.add(newButton);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Crée le panneau de détails du patient
     * 
     * @return Le panneau de détails
     */
    private JPanel createPatientDetailsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Détails du patient"));
        panel.setPreferredSize(new Dimension(400, 500));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        
        // Nom
        JLabel lastNameLabel = new JLabel("Nom :");
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(lastNameLabel, gbc);
        
        lastNameField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(lastNameField, gbc);
        
        // Prénom
        JLabel firstNameLabel = new JLabel("Prénom :");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panel.add(firstNameLabel, gbc);
        
        firstNameField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        panel.add(firstNameField, gbc);
        
        // Date de naissance
        JLabel birthDateLabel = new JLabel("Date de naissance :");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        panel.add(birthDateLabel, gbc);
        
        birthDateField = new JTextField(10);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        panel.add(birthDateField, gbc);
        
        // Format de date
        JLabel dateFormatLabel = new JLabel("Format: " + AppConfig.DATE_FORMAT);
        dateFormatLabel.setFont(dateFormatLabel.getFont().deriveFont(10.0f));
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        panel.add(dateFormatLabel, gbc);
        
        // Téléphone
        JLabel phoneLabel = new JLabel("Téléphone :");
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        panel.add(phoneLabel, gbc);
        
        phoneField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        panel.add(phoneField, gbc);
        
        // Adresse
        JLabel addressLabel = new JLabel("Adresse :");
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 1;
        panel.add(addressLabel, gbc);
        
        addressField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        panel.add(addressField, gbc);
        
        // Email
        JLabel emailLabel = new JLabel("Email :");
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 1;
        panel.add(emailLabel, gbc);
        
        emailField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        panel.add(emailField, gbc);
        
        // Notes
        JLabel notesLabel = new JLabel("Notes :");
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 1;
        panel.add(notesLabel, gbc);
        
        notesArea = new JTextArea(5, 20);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        JScrollPane notesScrollPane = new JScrollPane(notesArea);
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        panel.add(notesScrollPane, gbc);
        
        // Panneau de boutons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        saveButton = new JButton("Enregistrer");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                savePatient();
            }
        });
        buttonPanel.add(saveButton);
        
        cancelButton = new JButton("Annuler");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelEditing();
            }
        });
        buttonPanel.add(cancelButton);
        
        deleteButton = new JButton("Supprimer");
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deletePatient();
            }
        });
        buttonPanel.add(deleteButton);
        
        appointmentsButton = new JButton("Rendez-vous");
        appointmentsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                viewPatientAppointments();
            }
        });
        buttonPanel.add(appointmentsButton);
        
        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0;
        gbc.weighty = 0;
        panel.add(buttonPanel, gbc);
        
        return panel;
    }
    
    /**
     * Charge les données initiales
     */
    private void loadData() {
        try {
            // Charger tous les patients ou un nombre limité pour les grandes bases
            patients = patientController.getAllPatients();
            updatePatientTable();
            
        } catch (DAOException e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors du chargement des patients: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Rafraîchit les données affichées
     */
    public void refreshData() {
        loadData();
    }
    
    /**
     * Met à jour le tableau des patients
     */
    private void updatePatientTable() {
        // Vider le tableau
        tableModel.setRowCount(0);
        
        // Formatter pour les dates
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(AppConfig.DATE_FORMAT);
        
        // Remplir avec les patients
        for (Patient patient : patients) {
            String lastName = patient.getLastName();
            String firstName = patient.getFirstName();
            String birthDate = patient.getBirthDate() != null ? 
                             patient.getBirthDate().format(dateFormatter) : "";
            String phone = patient.getPhone();
            String email = patient.getEmail();
            
            tableModel.addRow(new Object[]{lastName, firstName, birthDate, phone, email});
        }
        
        // Désélectionner tout
        patientTable.clearSelection();
        clearFields();
        setFieldsEnabled(false);
    }
    
    /**
     * Affiche les détails d'un patient
     * 
     * @param patient Le patient à afficher
     */
    private void displayPatient(Patient patient) {
        try {
            // Charger les détails complets du patient
            currentPatient = patientController.getPatientById(patient.getId());
            
            // Formatter pour les dates
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(AppConfig.DATE_FORMAT);
            
            // Afficher les informations dans les champs
            lastNameField.setText(currentPatient.getLastName());
            firstNameField.setText(currentPatient.getFirstName());
            birthDateField.setText(currentPatient.getBirthDate() != null ? 
                                currentPatient.getBirthDate().format(dateFormatter) : "");
            phoneField.setText(currentPatient.getPhone());
            addressField.setText(currentPatient.getAddress());
            emailField.setText(currentPatient.getEmail());
            notesArea.setText(currentPatient.getAdministrativeNotes());
            
            // Activer les boutons appropriés
            setFieldsEnabled(true);
            editMode = false;
            
        } catch (DAOException e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors du chargement des détails du patient: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Crée un nouveau patient
     */
    private void createNewPatient() {
        currentPatient = new Patient();
        currentPatient.setCreationDate(java.time.LocalDateTime.now());
        
        // Initialiser les champs
        clearFields();
        
        // Activer les champs
        setFieldsEnabled(true);
        editMode = true;
        
        // Focus sur le champ nom
        lastNameField.requestFocus();
    }
    
    /**
     * Enregistre le patient courant
     */
    private void savePatient() {
        try {
            // Vérifier les champs obligatoires
            if (lastNameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Le nom est obligatoire.",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (firstNameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Le prénom est obligatoire.",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (phoneField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Le téléphone est obligatoire.",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Parser la date de naissance
            LocalDate birthDate = null;
            if (!birthDateField.getText().trim().isEmpty()) {
                birthDate = DateTimeUtils.parseDate(birthDateField.getText());
                if (birthDate == null) {
                    JOptionPane.showMessageDialog(this,
                            "Format de date de naissance invalide. Utilisez le format " + AppConfig.DATE_FORMAT,
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            
            // Mettre à jour l'objet patient
            currentPatient.setLastName(lastNameField.getText().trim());
            currentPatient.setFirstName(firstNameField.getText().trim());
            currentPatient.setBirthDate(birthDate);
            currentPatient.setPhone(phoneField.getText().trim());
            currentPatient.setAddress(addressField.getText().trim());
            currentPatient.setEmail(emailField.getText().trim());
            currentPatient.setAdministrativeNotes(notesArea.getText());
            
            // Enregistrer le patient
            if (currentPatient.getId() == 0) {
                // Nouveau patient
                patientController.addPatient(currentPatient);
                JOptionPane.showMessageDialog(this,
                        AppConfig.SuccessMessages.PATIENT_ADDED,
                        "Succès", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Mise à jour d'un patient existant
                patientController.updatePatient(currentPatient);
                JOptionPane.showMessageDialog(this,
                        AppConfig.SuccessMessages.PATIENT_UPDATED,
                        "Succès", JOptionPane.INFORMATION_MESSAGE);
            }
            
            // Rafraîchir les données
            refreshData();
            
        } catch (DAOException e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors de l'enregistrement du patient: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Annule l'édition en cours
     */
    private void cancelEditing() {
        // Si en mode édition d'un patient existant, réafficher ses données
        if (!editMode && currentPatient != null && currentPatient.getId() > 0) {
            displayPatient(currentPatient);
        } else {
            // Sinon, vider les champs
            clearFields();
            setFieldsEnabled(false);
        }
    }
    
    /**
     * Supprime le patient courant
     */
    private void deletePatient() {
        if (currentPatient == null || currentPatient.getId() == 0) {
            return;
        }
        
        // Vérifier si le patient a des rendez-vous
        try {
            List<Appointment> appointments = appointmentController.getAppointmentsByPatient(currentPatient.getId());
            
            if (!appointments.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Ce patient ne peut pas être supprimé car il a des rendez-vous associés.",
                        "Information", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            int choice = JOptionPane.showConfirmDialog(this,
                    "Êtes-vous sûr de vouloir supprimer ce patient ?",
                    "Confirmation", JOptionPane.YES_NO_OPTION);
            
            if (choice == JOptionPane.YES_OPTION) {
                patientController.deletePatient(currentPatient.getId());
                JOptionPane.showMessageDialog(this,
                        "Patient supprimé avec succès.",
                        "Succès", JOptionPane.INFORMATION_MESSAGE);
                
                // Rafraîchir les données
                refreshData();
            }
            
        } catch (DAOException e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors de la suppression du patient: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Affiche les rendez-vous du patient courant
     */
    private void viewPatientAppointments() {
        if (currentPatient == null || currentPatient.getId() == 0) {
            return;
        }
        
        try {
            List<Appointment> appointments = patientController.getPatientAppointmentHistory(currentPatient.getId());
            
            if (appointments.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Ce patient n'a pas de rendez-vous.",
                        "Information", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            // Créer un modèle de tableau pour les rendez-vous
            String[] columnNames = {"Date", "Heure", "Médecin", "Type", "Statut"};
            DefaultTableModel appointmentModel = new DefaultTableModel(columnNames, 0) {
                private static final long serialVersionUID = 1L;
                
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(AppConfig.DATE_FORMAT);
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern(AppConfig.TIME_FORMAT);
            
            for (Appointment appointment : appointments) {
                String date = appointment.getStartDateTime().format(dateFormatter);
                String time = appointment.getStartDateTime().format(timeFormatter);
                String doctor = appointment.getDoctor() != null ? 
                              appointment.getDoctor().getFullName() : 
                              "Médecin #" + appointment.getDoctorId();
                String type = appointment.getAppointmentType();
                String status = appointment.getStatus().getLabel();
                
                appointmentModel.addRow(new Object[]{date, time, doctor, type, status});
            }
            
            JTable appointmentTable = new JTable(appointmentModel);
            appointmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            appointmentTable.getTableHeader().setReorderingAllowed(false);
            
            JScrollPane scrollPane = new JScrollPane(appointmentTable);
            scrollPane.setPreferredSize(new Dimension(600, 300));
            
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(new JLabel("Historique des rendez-vous pour " + currentPatient.getFullName(), SwingConstants.CENTER), BorderLayout.NORTH);
            panel.add(scrollPane, BorderLayout.CENTER);
            
            JOptionPane.showMessageDialog(this, panel, "Rendez-vous du patient", JOptionPane.PLAIN_MESSAGE);
            
        } catch (DAOException e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors de la récupération des rendez-vous: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Effectue la recherche des patients selon les critères
     */
    private void searchPatients() {
        try {
            String lastName = searchLastNameField.getText().trim();
            String firstName = searchFirstNameField.getText().trim();
            String phone = searchPhoneField.getText().trim();
            
            if (lastName.isEmpty() && firstName.isEmpty() && phone.isEmpty()) {
                // Recherche sans critère, charger tous les patients
                patients = patientController.getAllPatients();
            } else if (!phone.isEmpty()) {
                // Priorité à la recherche par téléphone
                patients = patientController.searchPatientsByPhone(phone);
            } else {
                // Recherche par nom et/ou prénom
                patients = patientController.searchPatientsByName(lastName, firstName);
            }
            
            updatePatientTable();
            
        } catch (DAOException e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors de la recherche de patients: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Efface les champs de saisie
     */
    private void clearFields() {
        lastNameField.setText("");
        firstNameField.setText("");
        birthDateField.setText("");
        phoneField.setText("");
        addressField.setText("");
        emailField.setText("");
        notesArea.setText("");
    }
    
    /**
     * Active ou désactive les champs de saisie
     * 
     * @param enabled true pour activer, false pour désactiver
     */
    private void setFieldsEnabled(boolean enabled) {
        lastNameField.setEnabled(enabled);
        firstNameField.setEnabled(enabled);
        birthDateField.setEnabled(enabled);
        phoneField.setEnabled(enabled);
        addressField.setEnabled(enabled);
        emailField.setEnabled(enabled);
        notesArea.setEnabled(enabled);
        
        saveButton.setEnabled(enabled);
        cancelButton.setEnabled(enabled);
        deleteButton.setEnabled(enabled && currentPatient != null && currentPatient.getId() > 0 && !editMode);
        appointmentsButton.setEnabled(enabled && currentPatient != null && currentPatient.getId() > 0);
    }
}