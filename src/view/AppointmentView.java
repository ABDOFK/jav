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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
import controller.AuthController;
import controller.DoctorController;
import controller.PatientController;
import dao.DAOException;
import model.Appointment;
import model.AppointmentStatus;
import model.Doctor;
import model.Patient;
import model.Secretary;
import util.DateTimeUtils;

/**
 * Vue pour la gestion des rendez-vous.
 * Permet aux utilisateurs de créer, modifier et annuler des rendez-vous.
 */
public class AppointmentView extends JPanel {
    
    private static final long serialVersionUID = 1L;
    
    // Contrôleurs
    private final AppointmentController appointmentController;
    private final PatientController patientController;
    private final DoctorController doctorController;
    private final AuthController authController;
    
    // Composants de l'interface
    private JPanel searchPanel;
    private JPanel appointmentListPanel;
    private JPanel appointmentDetailsPanel;
    
    private JTextField searchPatientField;
    private JComboBox<Doctor> doctorComboBox;
    private JTextField searchDateField;
    private JButton searchButton;
    
    private JTable appointmentTable;
    private DefaultTableModel tableModel;
    
    private JTextField patientField;
    private JComboBox<Doctor> appointmentDoctorComboBox;
    private JTextField dateField;
    private JComboBox<String> timeComboBox;
    private JComboBox<Integer> durationComboBox;
    private JComboBox<String> typeComboBox;
    private JComboBox<AppointmentStatus> statusComboBox;
    private JTextArea notesArea;
    
    private JButton newButton;
    private JButton saveButton;
    private JButton cancelButton;
    private JButton deleteButton;
    
    // État courant
    private List<Appointment> appointments;
    private Appointment currentAppointment;
    private boolean editMode = false;
    
    /**
     * Constructeur par défaut
     */
    public AppointmentView() {
        this.appointmentController = AppointmentController.getInstance();
        this.patientController = PatientController.getInstance();
        this.doctorController = DoctorController.getInstance();
        this.authController = AuthController.getInstance();
        
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
        
        // Panneau de liste des rendez-vous
        appointmentListPanel = createAppointmentListPanel();
        add(appointmentListPanel, BorderLayout.CENTER);
        
        // Panneau de détails du rendez-vous
        appointmentDetailsPanel = createAppointmentDetailsPanel();
        add(appointmentDetailsPanel, BorderLayout.EAST);
        
        // Désactiver les champs de saisie au démarrage
        setFieldsEnabled(false);
    }
    
    /**
     * Crée le panneau de recherche des rendez-vous
     * 
     * @return Le panneau de recherche
     */
    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Recherche de rendez-vous"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Recherche par patient
        JLabel patientLabel = new JLabel("Patient :");
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(patientLabel, gbc);
        
        searchPatientField = new JTextField(15);
        gbc.gridx = 1;
        gbc.gridy = 0;
        panel.add(searchPatientField, gbc);
        
        // Recherche par médecin
        JLabel doctorLabel = new JLabel("Médecin :");
        gbc.gridx = 2;
        gbc.gridy = 0;
        panel.add(doctorLabel, gbc);
        
        doctorComboBox = new JComboBox<>();
        doctorComboBox.setPreferredSize(new Dimension(150, 25));
        gbc.gridx = 3;
        gbc.gridy = 0;
        panel.add(doctorComboBox, gbc);
        
        // Recherche par date
        JLabel dateLabel = new JLabel("Date :");
        gbc.gridx = 4;
        gbc.gridy = 0;
        panel.add(dateLabel, gbc);
        
        searchDateField = new JTextField(10);
        gbc.gridx = 5;
        gbc.gridy = 0;
        panel.add(searchDateField, gbc);
        
        // Bouton de recherche
        searchButton = new JButton("Rechercher");
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchAppointments();
            }
        });
        gbc.gridx = 6;
        gbc.gridy = 0;
        panel.add(searchButton, gbc);
        
        return panel;
    }
    
    /**
     * Crée le panneau de liste des rendez-vous
     * 
     * @return Le panneau de liste
     */
    private JPanel createAppointmentListPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Liste des rendez-vous"));
        
        // Création du modèle de tableau
        String[] columnNames = {"Date", "Heure", "Patient", "Médecin", "Type", "Statut"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Rendre toutes les cellules non éditables
            }
        };
        
        // Création du tableau
        appointmentTable = new JTable(tableModel);
        appointmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        appointmentTable.getTableHeader().setReorderingAllowed(false);
        
        // Gérer la sélection d'un rendez-vous
        appointmentTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int selectedRow = appointmentTable.getSelectedRow();
                    if (selectedRow >= 0 && selectedRow < appointments.size()) {
                        displayAppointment(appointments.get(selectedRow));
                    }
                }
            }
        });
        
        // Panneau défilant pour le tableau
        JScrollPane scrollPane = new JScrollPane(appointmentTable);
        scrollPane.setPreferredSize(new Dimension(600, 300));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Boutons d'action
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        newButton = new JButton("Nouveau");
        newButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createNewAppointment();
            }
        });
        buttonPanel.add(newButton);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Crée le panneau de détails du rendez-vous
     * 
     * @return Le panneau de détails
     */
    private JPanel createAppointmentDetailsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Détails du rendez-vous"));
        panel.setPreferredSize(new Dimension(400, 500));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        
        // Patient
        JLabel patientLabel = new JLabel("Patient :");
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(patientLabel, gbc);
        
        patientField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(patientField, gbc);
        
        JButton selectPatientButton = new JButton("...");
        selectPatientButton.setMargin(new Insets(0, 5, 0, 5));
        selectPatientButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectPatient();
            }
        });
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        panel.add(selectPatientButton, gbc);
        
        // Médecin
        JLabel doctorLabel = new JLabel("Médecin :");
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(doctorLabel, gbc);
        
        appointmentDoctorComboBox = new JComboBox<>();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        panel.add(appointmentDoctorComboBox, gbc);
        
        // Date
        JLabel dateLabel = new JLabel("Date :");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        panel.add(dateLabel, gbc);
        
        dateField = new JTextField(10);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        panel.add(dateField, gbc);
        
        // Heure
        JLabel timeLabel = new JLabel("Heure :");
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        panel.add(timeLabel, gbc);
        
        timeComboBox = new JComboBox<>();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        panel.add(timeComboBox, gbc);
        
        // Durée
        JLabel durationLabel = new JLabel("Durée (min) :");
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        panel.add(durationLabel, gbc);
        
        durationComboBox = new JComboBox<>();
        for (int duration : AppConfig.DEFAULT_APPOINTMENT_DURATIONS) {
            durationComboBox.addItem(duration);
        }
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.gridwidth = 3;
        panel.add(durationComboBox, gbc);
        
        // Type de consultation
        JLabel typeLabel = new JLabel("Type :");
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 1;
        panel.add(typeLabel, gbc);
        
        typeComboBox = new JComboBox<>(AppConfig.APPOINTMENT_TYPES);
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.gridwidth = 3;
        panel.add(typeComboBox, gbc);
        
        // Statut
        JLabel statusLabel = new JLabel("Statut :");
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 1;
        panel.add(statusLabel, gbc);
        
        statusComboBox = new JComboBox<>(AppointmentStatus.values());
        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.gridwidth = 3;
        panel.add(statusComboBox, gbc);
        
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
        gbc.gridwidth = 4;
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
                saveAppointment();
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
                deleteAppointment();
            }
        });
        buttonPanel.add(deleteButton);
        
        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.gridwidth = 4;
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
            // Charger les médecins pour le filtre de recherche
            List<Doctor> doctors = doctorController.getAllDoctors();
            doctorComboBox.removeAllItems();
            doctorComboBox.addItem(null); // Option "Tous les médecins"
            
            for (Doctor doctor : doctors) {
                doctorComboBox.addItem(doctor);
                appointmentDoctorComboBox.addItem(doctor);
            }
            
            // Charger les rendez-vous du jour
            LocalDate today = LocalDate.now();
            appointments = appointmentController.getAppointmentsByDate(today);
            updateAppointmentTable();
            
            // Initialiser le champ de date avec la date du jour
            searchDateField.setText(DateTimeUtils.formatDate(today));
            
            // Initialiser les créneaux horaires disponibles
            initializeTimeSlots();
            
        } catch (DAOException e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors du chargement des données: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Initialise les créneaux horaires dans le combo box
     */
    private void initializeTimeSlots() {
        timeComboBox.removeAllItems();
        
        // Générer des créneaux de 15 minutes de l'heure d'ouverture à l'heure de fermeture
        List<String> slots = DateTimeUtils.generateTimeSlots(
                AppConfig.WORK_START_TIME, 
                AppConfig.WORK_END_TIME, 
                15);
        
        for (String slot : slots) {
            timeComboBox.addItem(slot);
        }
    }
    
    /**
     * Rafraîchit les données affichées
     */
    public void refreshData() {
        try {
            // Récupérer la date de recherche actuelle ou la date du jour
            String dateStr = searchDateField.getText().trim();
            LocalDate date = dateStr.isEmpty() ? 
                    LocalDate.now() : 
                    DateTimeUtils.parseDate(dateStr);
            
            if (date == null) {
                date = LocalDate.now();
            }
            
            // Récupérer le médecin sélectionné
            Doctor selectedDoctor = (Doctor) doctorComboBox.getSelectedItem();
            
            // Récupérer le texte de recherche patient
            String patientSearch = searchPatientField.getText().trim();
            
            if (selectedDoctor != null && !patientSearch.isEmpty()) {
                // Recherche par médecin et patient
                // Simplification: on récupère tous les RDV du médecin et on filtre côté client
                appointments = appointmentController.getAppointmentsByDoctorAndDate(selectedDoctor.getId(), date);
                filterAppointmentsByPatient(patientSearch);
            } else if (selectedDoctor != null) {
                // Recherche par médecin uniquement
                appointments = appointmentController.getAppointmentsByDoctorAndDate(selectedDoctor.getId(), date);
            } else if (!patientSearch.isEmpty()) {
                // Recherche par patient uniquement
                // Simplification: on récupère tous les RDV de la date et on filtre côté client
                appointments = appointmentController.getAppointmentsByDate(date);
                filterAppointmentsByPatient(patientSearch);
            } else {
                // Recherche par date uniquement
                appointments = appointmentController.getAppointmentsByDate(date);
            }
            
            updateAppointmentTable();
            
        } catch (DAOException e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors du rafraîchissement des données: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Filtre la liste des rendez-vous par nom de patient
     * 
     * @param patientName Nom du patient à rechercher
     */
    private void filterAppointmentsByPatient(String patientName) {
        if (patientName.isEmpty()) {
            return;
        }
        
        patientName = patientName.toLowerCase();
        List<Appointment> filteredAppointments = new ArrayList<>();
        
        for (Appointment appointment : appointments) {
            Patient patient = appointment.getPatient();
            if (patient != null) {
                String fullName = patient.getFullName().toLowerCase();
                if (fullName.contains(patientName)) {
                    filteredAppointments.add(appointment);
                }
            }
        }
        
        appointments = filteredAppointments;
    }
    
    /**
     * Met à jour le tableau des rendez-vous
     */
    private void updateAppointmentTable() {
        // Vider le tableau
        tableModel.setRowCount(0);
        
        // Remplir avec les rendez-vous
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(AppConfig.DATE_FORMAT);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern(AppConfig.TIME_FORMAT);
        
        for (Appointment appointment : appointments) {
            String date = appointment.getStartDateTime().format(dateFormatter);
            String time = appointment.getStartDateTime().format(timeFormatter);
            String patient = appointment.getPatient() != null ? 
                           appointment.getPatient().getFullName() : 
                           "Patient #" + appointment.getPatientId();
            String doctor = appointment.getDoctor() != null ? 
                          appointment.getDoctor().getFullName() : 
                          "Médecin #" + appointment.getDoctorId();
            String type = appointment.getAppointmentType();
            String status = appointment.getStatus().getLabel();
            
            tableModel.addRow(new Object[]{date, time, patient, doctor, type, status});
        }
        
        // Désélectionner tout
        appointmentTable.clearSelection();
        clearFields();
        setFieldsEnabled(false);
    }
    
    /**
     * Affiche les détails d'un rendez-vous
     * 
     * @param appointment Le rendez-vous à afficher
     */
    private void displayAppointment(Appointment appointment) {
        currentAppointment = appointment;
        
        // Afficher les informations dans les champs
        Patient patient = appointment.getPatient();
        patientField.setText(patient != null ? patient.getFullName() : "");
        
        Doctor doctor = appointment.getDoctor();
        if (doctor != null) {
            for (int i = 0; i < appointmentDoctorComboBox.getItemCount(); i++) {
                Doctor item = appointmentDoctorComboBox.getItemAt(i);
                if (item.getId() == doctor.getId()) {
                    appointmentDoctorComboBox.setSelectedIndex(i);
                    break;
                }
            }
        }
        
        // Date et heure
        LocalDateTime startDateTime = appointment.getStartDateTime();
        dateField.setText(DateTimeUtils.formatDate(startDateTime.toLocalDate()));
        
        String timeStr = DateTimeUtils.formatTime(startDateTime.toLocalTime());
        boolean timeFound = false;
        for (int i = 0; i < timeComboBox.getItemCount(); i++) {
            if (timeComboBox.getItemAt(i).equals(timeStr)) {
                timeComboBox.setSelectedIndex(i);
                timeFound = true;
                break;
            }
        }
        if (!timeFound && timeComboBox.isEditable()) {
            timeComboBox.setSelectedItem(timeStr);
        }
        
        // Durée
        int duration = appointment.getDurationMinutes();
        boolean durationFound = false;
        for (int i = 0; i < durationComboBox.getItemCount(); i++) {
            if ((Integer) durationComboBox.getItemAt(i) == duration) {
                durationComboBox.setSelectedIndex(i);
                durationFound = true;
                break;
            }
        }
        if (!durationFound) {
            durationComboBox.addItem(duration);
            durationComboBox.setSelectedItem(duration);
        }
        
        // Type et statut
        String type = appointment.getAppointmentType();
        boolean typeFound = false;
        for (int i = 0; i < typeComboBox.getItemCount(); i++) {
            if (typeComboBox.getItemAt(i).equals(type)) {
                typeComboBox.setSelectedIndex(i);
                typeFound = true;
                break;
            }
        }
        if (!typeFound && typeComboBox.isEditable()) {
            typeComboBox.setSelectedItem(type);
        }
        
        statusComboBox.setSelectedItem(appointment.getStatus());
        
        // Notes
        notesArea.setText(appointment.getNotes());
        
        // Activer les boutons appropriés
        setFieldsEnabled(true);
        editMode = false;
    }
    
    /**
     * Crée un nouveau rendez-vous
     */
    private void createNewAppointment() {
        currentAppointment = new Appointment();
        currentAppointment.setCreationDateTime(LocalDateTime.now());
        currentAppointment.setStatus(AppointmentStatus.PLANIFIE);
        
        // Utiliser l'utilisateur courant comme créateur
        if (authController.isCurrentUserSecretary()) {
            Secretary secretary = authController.getCurrentSecretary();
            currentAppointment.setSecretary(secretary);
            currentAppointment.setSecretaryId(secretary.getId());
        }
        
        // Initialiser les champs
        clearFields();
        
        // Initialiser la date à aujourd'hui
        dateField.setText(DateTimeUtils.formatDate(LocalDate.now()));
        
        // Activer les champs
        setFieldsEnabled(true);
        editMode = true;
        
        // Focus sur le champ patient
        patientField.requestFocus();
    }
    
    /**
     * Enregistre le rendez-vous courant
     */
    private void saveAppointment() {
        try {
            // Vérifier les champs obligatoires
            if (patientField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Veuillez sélectionner un patient.",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (appointmentDoctorComboBox.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this,
                        "Veuillez sélectionner un médecin.",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (dateField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Veuillez entrer une date.",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (timeComboBox.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this,
                        "Veuillez sélectionner une heure.",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Parser la date et l'heure
            LocalDate date = DateTimeUtils.parseDate(dateField.getText());
            LocalTime time = DateTimeUtils.parseTime((String) timeComboBox.getSelectedItem());
            
            if (date == null || time == null) {
                JOptionPane.showMessageDialog(this,
                        "Date ou heure invalide.",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Mettre à jour l'objet rendez-vous
            Doctor doctor = (Doctor) appointmentDoctorComboBox.getSelectedItem();
            currentAppointment.setDoctor(doctor);
            currentAppointment.setDoctorId(doctor.getId());
            
            // Parser le patient (simplification: le patient doit déjà exister)
            Patient patient = currentAppointment.getPatient();
            if (patient == null) {
                JOptionPane.showMessageDialog(this,
                        "Veuillez sélectionner un patient valide.",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            LocalDateTime startDateTime = LocalDateTime.of(date, time);
            currentAppointment.setStartDateTime(startDateTime);
            
            Integer duration = (Integer) durationComboBox.getSelectedItem();
            currentAppointment.setDurationMinutes(duration);
            
            String type = (String) typeComboBox.getSelectedItem();
            currentAppointment.setAppointmentType(type);
            
            AppointmentStatus status = (AppointmentStatus) statusComboBox.getSelectedItem();
            currentAppointment.setStatus(status);
            
            currentAppointment.setNotes(notesArea.getText());
            
            // Mettre à jour la date de dernière modification
            currentAppointment.updateLastModified();
            
            // Enregistrer le rendez-vous
            if (currentAppointment.getId() == 0) {
                // Nouveau rendez-vous
                appointmentController.addAppointment(currentAppointment);
                JOptionPane.showMessageDialog(this,
                        AppConfig.SuccessMessages.APPOINTMENT_CREATED,
                        "Succès", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Mise à jour d'un rendez-vous existant
                appointmentController.updateAppointment(currentAppointment);
                JOptionPane.showMessageDialog(this,
                        AppConfig.SuccessMessages.APPOINTMENT_MODIFIED,
                        "Succès", JOptionPane.INFORMATION_MESSAGE);
            }
            
            // Rafraîchir les données
            refreshData();
            
        } catch (DAOException e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors de l'enregistrement du rendez-vous: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Annule l'édition en cours
     */
    private void cancelEditing() {
        // Si en mode édition d'un rendez-vous existant, réafficher ses données
        if (!editMode && currentAppointment != null && currentAppointment.getId() > 0) {
            displayAppointment(currentAppointment);
        } else {
            // Sinon, vider les champs
            clearFields();
            setFieldsEnabled(false);
        }
    }
    
    /**
     * Supprime le rendez-vous courant
     */
    private void deleteAppointment() {
        if (currentAppointment == null || currentAppointment.getId() == 0) {
            return;
        }
        
        int choice = JOptionPane.showConfirmDialog(this,
                "Êtes-vous sûr de vouloir supprimer ce rendez-vous ?",
                "Confirmation", JOptionPane.YES_NO_OPTION);
        
        if (choice == JOptionPane.YES_OPTION) {
            try {
                appointmentController.deleteAppointment(currentAppointment.getId());
                JOptionPane.showMessageDialog(this,
                        AppConfig.SuccessMessages.APPOINTMENT_CANCELLED,
                        "Succès", JOptionPane.INFORMATION_MESSAGE);
                
                // Rafraîchir les données
                refreshData();
                
            } catch (DAOException e) {
                JOptionPane.showMessageDialog(this,
                        "Erreur lors de la suppression du rendez-vous: " + e.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Effectue la recherche des rendez-vous selon les critères
     */
    private void searchAppointments() {
        refreshData();
    }
    
    /**
     * Ouvre un dialogue pour sélectionner un patient
     */
    private void selectPatient() {
        try {
            // Liste des patients
            List<Patient> patients = patientController.getAllPatients();
            
            if (patients.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Aucun patient trouvé dans la base de données.",
                        "Information", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            // Créer un modèle pour la liste des patients
            DefaultComboBoxModel<Patient> model = new DefaultComboBoxModel<>();
            for (Patient patient : patients) {
                model.addElement(patient);
            }
            
            // Créer un combo box pour la sélection
            JComboBox<Patient> comboBox = new JComboBox<>(model);
            
            // Afficher le dialogue de sélection
            int result = JOptionPane.showConfirmDialog(this,
                    comboBox,
                    "Sélectionner un patient",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);
            
            if (result == JOptionPane.OK_OPTION) {
                Patient selectedPatient = (Patient) comboBox.getSelectedItem();
                if (selectedPatient != null) {
                    // Mettre à jour l'UI et le rendez-vous
                    patientField.setText(selectedPatient.getFullName());
                    currentAppointment.setPatient(selectedPatient);
                    currentAppointment.setPatientId(selectedPatient.getId());
                }
            }
            
        } catch (DAOException e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors de la récupération des patients: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Efface les champs de saisie
     */
    private void clearFields() {
        patientField.setText("");
        appointmentDoctorComboBox.setSelectedIndex(-1);
        dateField.setText("");
        timeComboBox.setSelectedIndex(-1);
        durationComboBox.setSelectedIndex(-1);
        typeComboBox.setSelectedIndex(-1);
        statusComboBox.setSelectedItem(AppointmentStatus.PLANIFIE);
        notesArea.setText("");
    }
    
    /**
     * Active ou désactive les champs de saisie
     * 
     * @param enabled true pour activer, false pour désactiver
     */
    private void setFieldsEnabled(boolean enabled) {
        patientField.setEnabled(enabled);
        appointmentDoctorComboBox.setEnabled(enabled);
        dateField.setEnabled(enabled);
        timeComboBox.setEnabled(enabled);
        durationComboBox.setEnabled(enabled);
        typeComboBox.setEnabled(enabled);
        statusComboBox.setEnabled(enabled);
        notesArea.setEnabled(enabled);
        
        saveButton.setEnabled(enabled);
        cancelButton.setEnabled(enabled);
        deleteButton.setEnabled(enabled && currentAppointment != null && currentAppointment.getId() > 0);
    }
}
