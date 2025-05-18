package view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import config.AppConfig;
import controller.AuthController;
import model.Doctor;
import model.Secretary;
import model.User;

/**
 * Vue principale de l'application.
 * Affiche différentes interfaces selon le type d'utilisateur (secrétaire ou médecin).
 */
public class MainView extends JFrame {
    
    private static final long serialVersionUID = 1L;
    
    // Contrôleur d'authentification
    private final AuthController authController;
    
    // Utilisateur connecté
    private final User currentUser;
    
    // Composants de l'interface
    private JPanel contentPane;
    private JPanel headerPanel;
    private JPanel sidePanel;
    private JPanel mainPanel;
    private CardLayout cardLayout;
    
    // Panels de fonctionnalités
    private JPanel dashboardPanel;
    private PatientManagementView patientPanel;
    private AppointmentView appointmentPanel;
    private PlanningView planningPanel;
    
    /**
     * Constructeur pour la secrétaire
     * 
     * @param secretary La secrétaire connectée
     */
    public MainView(Secretary secretary) {
        this.authController = AuthController.getInstance();
        this.currentUser = secretary;
        initializeUI();
        setupSecretaryUI();
    }
    
    /**
     * Constructeur pour le médecin
     * 
     * @param doctor Le médecin connecté
     */
    public MainView(Doctor doctor) {
        this.authController = AuthController.getInstance();
        this.currentUser = doctor;
        initializeUI();
        setupDoctorUI();
    }
    
    /**
     * Initialise l'interface utilisateur commune
     */
    private void initializeUI() {
        // Configuration de la fenêtre
        setTitle(AppConfig.APP_NAME);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setBounds(100, 100, AppConfig.WINDOW_WIDTH, AppConfig.WINDOW_HEIGHT);
        setLocationRelativeTo(null); // Centrer la fenêtre
        
        // Gestionnaire de fermeture personnalisé
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmExit();
            }
        });
        
        // Panneau principal
        contentPane = new JPanel(new BorderLayout(0, 0));
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        
        // Panneau d'en-tête
        headerPanel = createHeaderPanel();
        contentPane.add(headerPanel, BorderLayout.NORTH);
        
        // Panneau latéral
        sidePanel = createSidePanel();
        contentPane.add(sidePanel, BorderLayout.WEST);
        
        // Panneau principal (avec CardLayout pour les différentes vues)
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        contentPane.add(mainPanel, BorderLayout.CENTER);
        
        // Création des panels de fonctionnalités
        dashboardPanel = createDashboardPanel();
        mainPanel.add(dashboardPanel, "dashboard");
        
        // Barre de menu
        JMenuBar menuBar = createMenuBar();
        setJMenuBar(menuBar);
    }
    
    /**
     * Configure l'interface pour la secrétaire
     */
    private void setupSecretaryUI() {
        // Titre spécifique
        setTitle(AppConfig.APP_NAME + " - Secrétariat");
        
        // Création des panels spécifiques à la secrétaire
        patientPanel = new PatientManagementView();
        mainPanel.add(patientPanel, "patients");
        
        appointmentPanel = new AppointmentView();
        mainPanel.add(appointmentPanel, "appointments");
        
        planningPanel = new PlanningView();
        mainPanel.add(planningPanel, "planning");
        
        // Afficher le tableau de bord par défaut
        cardLayout.show(mainPanel, "dashboard");
    }
    
    /**
     * Configure l'interface pour le médecin
     */
    private void setupDoctorUI() {
        // Titre spécifique
        setTitle(AppConfig.APP_NAME + " - Dr. " + currentUser.getFullName());
        
        // Création des panels spécifiques au médecin
        planningPanel = new PlanningView((Doctor) currentUser);
        mainPanel.add(planningPanel, "planning");
        
        // Masquer certains boutons du menu latéral
        for (Component component : sidePanel.getComponents()) {
            if (component instanceof JButton) {
                JButton button = (JButton) component;
                String actionCommand = button.getActionCommand();
                if ("patients".equals(actionCommand) || "appointments".equals(actionCommand)) {
                    button.setVisible(false);
                }
            }
        }
        
        // Afficher directement le planning du médecin
        cardLayout.show(mainPanel, "planning");
    }
    
    /**
     * Crée le panneau d'en-tête
     * 
     * @return Le panneau d'en-tête
     */
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Titre de l'application
        JLabel titleLabel = new JLabel(AppConfig.APP_NAME);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(titleLabel, BorderLayout.CENTER);
        
        // Informations sur l'utilisateur connecté
        JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JLabel userLabel = new JLabel("Connecté en tant que : " + currentUser.getFullName());
        userInfoPanel.add(userLabel);
        
        JButton logoutButton = new JButton("Déconnexion");
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logout();
            }
        });
        userInfoPanel.add(logoutButton);
        
        panel.add(userInfoPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    /**
     * Crée le panneau latéral avec les boutons de navigation
     * 
     * @return Le panneau latéral
     */
    private JPanel createSidePanel() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(200, getHeight()));
        
        // Bouton pour le tableau de bord
        JButton dashboardButton = createNavButton("Tableau de bord", "dashboard");
        panel.add(dashboardButton);
        panel.add(Box.createVerticalStrut(10));
        
        // Bouton pour la gestion des patients
        JButton patientsButton = createNavButton("Patients", "patients");
        panel.add(patientsButton);
        panel.add(Box.createVerticalStrut(10));
        
        // Bouton pour la gestion des rendez-vous
        JButton appointmentsButton = createNavButton("Rendez-vous", "appointments");
        panel.add(appointmentsButton);
        panel.add(Box.createVerticalStrut(10));
        
        // Bouton pour le planning
        JButton planningButton = createNavButton("Planning", "planning");
        panel.add(planningButton);
        
        // Espace flexible pour pousser les boutons vers le haut
        panel.add(Box.createVerticalGlue());
        
        return panel;
    }
    
    /**
     * Crée un bouton de navigation pour le panneau latéral
     * 
     * @param text Le texte du bouton
     * @param cardName Le nom de la carte à afficher
     * @return Le bouton de navigation
     */
    private JButton createNavButton(String text, String cardName) {
        JButton button = new JButton(text);
        button.setMaximumSize(new Dimension(180, 40));
        button.setPreferredSize(new Dimension(180, 40));
        button.setActionCommand(cardName);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(mainPanel, cardName);
            }
        });
        return button;
    }
    
    /**
     * Crée le panneau de tableau de bord
     * 
     * @return Le panneau de tableau de bord
     */
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel welcomeLabel = new JLabel("Bienvenue, " + currentUser.getFullName() + " !");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(welcomeLabel, BorderLayout.NORTH);
        
        // Contenu du tableau de bord (à personnaliser)
        JPanel dashboardContent = new JPanel();
        dashboardContent.setLayout(new BoxLayout(dashboardContent, BoxLayout.Y_AXIS));
        dashboardContent.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel infoLabel = new JLabel("Utilisez le menu latéral pour accéder aux différentes fonctionnalités.");
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        dashboardContent.add(infoLabel);
        
        // Afficher d'autres informations selon le type d'utilisateur
        if (currentUser instanceof Secretary) {
            JLabel secretaryInfoLabel = new JLabel("Vous êtes connecté en tant que secrétaire.");
            secretaryInfoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            dashboardContent.add(Box.createVerticalStrut(20));
            dashboardContent.add(secretaryInfoLabel);
        } else if (currentUser instanceof Doctor) {
            Doctor doctor = (Doctor) currentUser;
            JLabel doctorInfoLabel = new JLabel("Vous êtes connecté en tant que médecin. Spécialité : " + doctor.getSpecialty());
            doctorInfoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            dashboardContent.add(Box.createVerticalStrut(20));
            dashboardContent.add(doctorInfoLabel);
        }
        
        panel.add(dashboardContent, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Crée la barre de menu
     * 
     * @return La barre de menu
     */
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // Menu Fichier
        JMenu fileMenu = new JMenu("Fichier");
        
        JMenuItem exportMenuItem = new JMenuItem("Exporter...");
        exportMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportData();
            }
        });
        fileMenu.add(exportMenuItem);
        
        fileMenu.addSeparator();
        
        JMenuItem exitMenuItem = new JMenuItem("Quitter");
        exitMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                confirmExit();
            }
        });
        fileMenu.add(exitMenuItem);
        
        menuBar.add(fileMenu);
        
        // Menu Édition
        JMenu editMenu = new JMenu("Édition");
        
        JMenuItem refreshMenuItem = new JMenuItem("Actualiser");
        refreshMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshCurrentView();
            }
        });
        editMenu.add(refreshMenuItem);
        
        menuBar.add(editMenu);
        
        // Menu Aide
        JMenu helpMenu = new JMenu("Aide");
        
        JMenuItem aboutMenuItem = new JMenuItem("À propos");
        aboutMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAboutDialog();
            }
        });
        helpMenu.add(aboutMenuItem);
        
        menuBar.add(helpMenu);
        
        return menuBar;
    }
    
    /**
     * Exporte les données selon la vue active
     */
    private void exportData() {
        // Déterminer quelle vue est active
        Component activeComponent = mainPanel.getComponent(0);
        for (Component component : mainPanel.getComponents()) {
            if (component.isVisible()) {
                activeComponent = component;
                break;
            }
        }
        
        // Déléguer l'export à la vue active
        if (activeComponent == planningPanel) {
            planningPanel.exportPlanning();
        } else {
            JOptionPane.showMessageDialog(this,
                    "L'exportation n'est disponible que pour le planning.",
                    "Information", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
     * Actualise la vue courante
     */
    private void refreshCurrentView() {
        // Déterminer quelle vue est active
        Component activeComponent = mainPanel.getComponent(0);
        for (Component component : mainPanel.getComponents()) {
            if (component.isVisible()) {
                activeComponent = component;
                break;
            }
        }
        
        // Actualiser la vue active
        if (activeComponent == patientPanel) {
            patientPanel.refreshData();
        } else if (activeComponent == appointmentPanel) {
            appointmentPanel.refreshData();
        } else if (activeComponent == planningPanel) {
            planningPanel.refreshData();
        }
    }
    
    /**
     * Affiche la boîte de dialogue "À propos"
     */
    private void showAboutDialog() {
        JOptionPane.showMessageDialog(this,
                AppConfig.APP_NAME + " v" + AppConfig.APP_VERSION + "\n\n" +
                "Application de gestion des rendez-vous pour un cabinet médical\n" +
                "Développée par Hatim Tajimi et Houssam El Moutaqui\n\n" +
                "© 2024 Tous droits réservés",
                "À propos", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Déconnecte l'utilisateur et retourne à la vue de connexion
     */
    private void logout() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Êtes-vous sûr de vouloir vous déconnecter ?",
                "Confirmation", JOptionPane.YES_NO_OPTION);
        
        if (choice == JOptionPane.YES_OPTION) {
            authController.logout();
            
            // Ouvrir la vue de connexion
            SwingUtilities.invokeLater(() -> {
                LoginView loginView = new LoginView();
                loginView.setVisible(true);
                this.dispose(); // Fermer la fenêtre principale
            });
        }
    }
    
    /**
     * Demande une confirmation avant de quitter l'application
     */
    private void confirmExit() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Êtes-vous sûr de vouloir quitter l'application ?",
                "Confirmation", JOptionPane.YES_NO_OPTION);
        
        if (choice == JOptionPane.YES_OPTION) {
            authController.logout();
            dispose();
            System.exit(0);
        }
    }
}