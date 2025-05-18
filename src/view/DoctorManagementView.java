package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import controller.AuthController;
import controller.DoctorController;
import dao.DAOException;
import model.Doctor;

/**
 * Vue pour la gestion des médecins.
 * Permet aux utilisateurs administrateurs de créer, modifier et désactiver des comptes médecins.
 */
public class DoctorManagementView extends JPanel {
    
    private static final long serialVersionUID = 1L;
    
    // Contrôleurs
    private final DoctorController doctorController;
    private final AuthController authController;
    
    // Composants de l'interface
    private JPanel listPanel;
    private JPanel detailsPanel;
    
    private JList<Doctor> doctorList;
    private DefaultListModel<Doctor> listModel;
    
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField fullNameField;
    private JTextField specialtyField;
    private JTextField workHoursField;
    private JTextField phoneField;
    private JCheckBox activeCheckbox;
    
    private JButton newButton;
    private JButton saveButton;
    private JButton deleteButton;
    private JButton cancelButton;
    
    // État courant
    private Doctor currentDoctor;
    private boolean editMode = false;
    
    /**
     * Constructeur par défaut
     */
    public DoctorManagementView() {
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
        
        // Panneau de liste des médecins
        listPanel = createListPanel();
        add(listPanel, BorderLayout.WEST);
        
        // Panneau de détails du médecin
        detailsPanel = createDetailsPanel();
        add(detailsPanel, BorderLayout.CENTER);
        
        // Désactiver les champs de saisie au démarrage
        setFieldsEnabled(false);
    }
    
    /**
     * Crée le panneau de liste des médecins
     * 
     * @return Le panneau de liste
     */
    private JPanel createListPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Liste des médecins"));
        panel.setPreferredSize(new Dimension(250, 500));
        
        // Création du modèle de liste
        listModel = new DefaultListModel<>();
        
        // Création de la liste
        doctorList = new JList<>(listModel);
        doctorList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Gérer la sélection d'un médecin
        doctorList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    Doctor selectedDoctor = doctorList.getSelectedValue();
                    if (selectedDoctor != null) {
                        displayDoctor(selectedDoctor);
                    }
                }
            }
        });
        
        // Panneau défilant pour la liste
        JScrollPane scrollPane = new JScrollPane(doctorList);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Boutons d'action
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        newButton = new JButton("Nouveau");
        newButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createNewDoctor();
            }
        });
        buttonPanel.add(newButton);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Crée le panneau de détails du médecin
     * 
     * @return Le panneau de détails
     */
    private JPanel createDetailsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Détails du médecin"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        
        // Nom d'utilisateur
        JLabel usernameLabel = new JLabel("Nom d'utilisateur :");
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(usernameLabel, gbc);
        
        usernameField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(usernameField, gbc);
        
        // Mot de passe
        JLabel passwordLabel = new JLabel("Mot de passe :");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panel.add(passwordLabel, gbc);
        
        passwordField = new JPasswordField(20);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        panel.add(passwordField, gbc);
        
        // Nom complet
        JLabel fullNameLabel = new JLabel("Nom complet :");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        panel.add(fullNameLabel, gbc);
        
        fullNameField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        panel.add(fullNameField, gbc);
        
        // Spécialité
        JLabel specialtyLabel = new JLabel("Spécialité :");
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        panel.add(specialtyLabel, gbc);
        
        specialtyField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        panel.add(specialtyField, gbc);
        
        // Horaires de travail
        JLabel workHoursLabel = new JLabel("Horaires de travail :");
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        panel.add(workHoursLabel, gbc);
        
        workHoursField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        panel.add(workHoursField, gbc);
        
        // Téléphone professionnel
        JLabel phoneLabel = new JLabel("Téléphone pro :");
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 1;
        panel.add(phoneLabel, gbc);
        
        phoneField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        panel.add(phoneField, gbc);
        
        // Actif
        activeCheckbox = new JCheckBox("Compte actif");
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 3;
        panel.add(activeCheckbox, gbc);
        
        // Libellé d'information pour les horaires
        JLabel hoursInfoLabel = new JLabel("Format des horaires: jour:heure_début-heure_fin,heure_début-heure_fin;jour:...");
        hoursInfoLabel.setFont(hoursInfoLabel.getFont().deriveFont(10.0f));
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 3;
        panel.add(hoursInfoLabel, gbc);
        
        // Exemple d'horaires
        JLabel hoursExampleLabel = new JLabel("Exemple: lundi:09:00-12:30,14:00-18:00;mardi:09:00-12:30");
        hoursExampleLabel.setFont(hoursExampleLabel.getFont().deriveFont(10.0f));
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 3;
        panel.add(hoursExampleLabel, gbc);
        
        // Panneau de boutons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        saveButton = new JButton("Enregistrer");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveDoctor();
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
                deleteDoctor();
            }
        });
        buttonPanel.add(deleteButton);
        
        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(buttonPanel, gbc);
        
        return panel;
    }
    
    /**
     * Charge les données initiales
     */
    private void loadData() {
        try {
            // Charger tous les médecins
            List<Doctor> doctors = doctorController.getAllDoctors();
            
            // Mettre à jour la liste
            listModel.clear();
            for (Doctor doctor : doctors) {
                listModel.addElement(doctor);
            }
            
        } catch (DAOException e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors du chargement des médecins: " + e.getMessage(),
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
     * Affiche les détails d'un médecin
     * 
     * @param doctor Le médecin à afficher
     */
    private void displayDoctor(Doctor doctor) {
        try {
            // Charger les détails complets du médecin
            currentDoctor = doctorController.getDoctorById(doctor.getId());
            
            // Afficher les informations dans les champs
            usernameField.setText(currentDoctor.getUsername());
            passwordField.setText(""); // Par sécurité, ne pas afficher le mot de passe
            fullNameField.setText(currentDoctor.getFullName());
            specialtyField.setText(currentDoctor.getSpecialty());
            workHoursField.setText(currentDoctor.getWorkHours());
            phoneField.setText(currentDoctor.getProfessionalPhone());
            activeCheckbox.setSelected(currentDoctor.isActive());
            
            // Activer les boutons appropriés
            setFieldsEnabled(true);
            editMode = false;
            
        } catch (DAOException e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors du chargement des détails du médecin: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Crée un nouveau médecin
     */
    private void createNewDoctor() {
        currentDoctor = new Doctor();
        
        // Initialiser les champs
        clearFields();
        
        // Activer les champs
        setFieldsEnabled(true);
        editMode = true;
        
        // Focus sur le champ nom d'utilisateur
        usernameField.requestFocus();
    }
    
    /**
     * Enregistre le médecin courant
     */
    private void saveDoctor() {
        try {
            // Vérifier les champs obligatoires
            if (usernameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Le nom d'utilisateur est obligatoire.",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (editMode && new String(passwordField.getPassword()).trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Le mot de passe est obligatoire.",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (fullNameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Le nom complet est obligatoire.",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (specialtyField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "La spécialité est obligatoire.",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Mettre à jour l'objet médecin
            currentDoctor.setUsername(usernameField.getText().trim());
            
            // Mettre à jour le mot de passe si saisi
            String password = new String(passwordField.getPassword()).trim();
            if (!password.isEmpty()) {
                currentDoctor.setPasswordHash(authController.hashPassword(password));
            }
            
            currentDoctor.setFullName(fullNameField.getText().trim());
            currentDoctor.setSpecialty(specialtyField.getText().trim());
            currentDoctor.setWorkHours(workHoursField.getText().trim());
            currentDoctor.setProfessionalPhone(phoneField.getText().trim());
            currentDoctor.setActive(activeCheckbox.isSelected());
            
            // Enregistrer le médecin
            if (currentDoctor.getId() == 0) {
                // Nouveau médecin
                doctorController.addDoctor(currentDoctor);
                JOptionPane.showMessageDialog(this,
                        "Médecin ajouté avec succès.",
                        "Succès", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Mise à jour d'un médecin existant
                doctorController.updateDoctor(currentDoctor);
                JOptionPane.showMessageDialog(this,
                        "Médecin mis à jour avec succès.",
                        "Succès", JOptionPane.INFORMATION_MESSAGE);
            }
            
            // Rafraîchir les données
            refreshData();
            
        } catch (DAOException e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors de l'enregistrement du médecin: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Annule l'édition en cours
     */
    private void cancelEditing() {
        // Si en mode édition d'un médecin existant, réafficher ses données
        if (!editMode && currentDoctor != null && currentDoctor.getId() > 0) {
            displayDoctor(currentDoctor);
        } else {
            // Sinon, vider les champs
            clearFields();
            setFieldsEnabled(false);
        }
    }
    
    /**
     * Supprime le médecin courant (ou le désactive)
     */
    private void deleteDoctor() {
        if (currentDoctor == null || currentDoctor.getId() == 0) {
            return;
        }
        
        // Au lieu de supprimer complètement, on propose de désactiver le compte
        int choice = JOptionPane.showConfirmDialog(this,
                "Voulez-vous désactiver ce compte médecin ?",
                "Confirmation", JOptionPane.YES_NO_CANCEL_OPTION);
        
        if (choice == JOptionPane.YES_OPTION) {
            try {
                doctorController.setDoctorActive(currentDoctor.getId(), false);
                JOptionPane.showMessageDialog(this,
                        "Compte médecin désactivé avec succès.",
                        "Succès", JOptionPane.INFORMATION_MESSAGE);
                
                // Mettre à jour l'interface
                activeCheckbox.setSelected(false);
                currentDoctor.setActive(false);
                
                // Rafraîchir les données
                refreshData();
                
            } catch (DAOException e) {
                JOptionPane.showMessageDialog(this,
                        "Erreur lors de la désactivation du compte: " + e.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Efface les champs de saisie
     */
    private void clearFields() {
        usernameField.setText("");
        passwordField.setText("");
        fullNameField.setText("");
        specialtyField.setText("");
        workHoursField.setText("");
        phoneField.setText("");
        activeCheckbox.setSelected(true);
    }
    
    /**
     * Active ou désactive les champs de saisie
     * 
     * @param enabled true pour activer, false pour désactiver
     */
    private void setFieldsEnabled(boolean enabled) {
        usernameField.setEnabled(enabled && editMode); // Nom d'utilisateur éditable uniquement pour les nouveaux médecins
        passwordField.setEnabled(enabled);
        fullNameField.setEnabled(enabled);
        specialtyField.setEnabled(enabled);
        workHoursField.setEnabled(enabled);
        phoneField.setEnabled(enabled);
        activeCheckbox.setEnabled(enabled);
        
        saveButton.setEnabled(enabled);
        cancelButton.setEnabled(enabled);
        deleteButton.setEnabled(enabled && currentDoctor != null && currentDoctor.getId() > 0 && !editMode);
    }
}