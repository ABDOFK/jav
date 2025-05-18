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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;

import config.AppConfig;
import controller.AuthController;
import model.Doctor;
import model.Secretary;

/**
 * Vue d'authentification pour l'application.
 * Permet aux utilisateurs (secrétaires et médecins) de se connecter.
 */
public class LoginView extends JFrame {
    
    private static final long serialVersionUID = 1L;
    
    // Contrôleur d'authentification
    private final AuthController authController;
    
    // Composants de l'interface
    private JPanel contentPane;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel errorLabel;
    
    /**
     * Constructeur principal qui initialise la vue de connexion
     */
    public LoginView() {
        this.authController = AuthController.getInstance();
        initializeUI();
    }
    
    /**
     * Initialise l'interface utilisateur de la vue de connexion
     */
    private void initializeUI() {
        // Configuration de la fenêtre
        setTitle(AppConfig.APP_NAME + " - Connexion");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 450, 350);
        setLocationRelativeTo(null); // Centrer la fenêtre
        
        // Panneau principal
        contentPane = new JPanel(new BorderLayout(0, 0));
        contentPane.setBorder(new EmptyBorder(20, 20, 20, 20));
        setContentPane(contentPane);
        
        // Panneau d'en-tête
        JPanel headerPanel = createHeaderPanel();
        contentPane.add(headerPanel, BorderLayout.NORTH);
        
        // Panneau de formulaire
        JPanel formPanel = createFormPanel();
        contentPane.add(formPanel, BorderLayout.CENTER);
        
        // Panneau de boutons
        JPanel buttonPanel = createButtonPanel();
        contentPane.add(buttonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Crée le panneau d'en-tête avec le logo et le titre
     * 
     * @return Le panneau d'en-tête
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        // Logo (à remplacer par votre propre icône)
        // ImageIcon iconLogo = new ImageIcon(getClass().getResource("/resources/logo.png"));
        // JLabel logoLabel = new JLabel(iconLogo);
        // headerPanel.add(logoLabel);
        
        // Titre
        JLabel titleLabel = new JLabel(AppConfig.APP_NAME);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(titleLabel);
        
        return headerPanel;
    }
    
    /**
     * Crée le panneau de formulaire avec les champs de saisie
     * 
     * @return Le panneau de formulaire
     */
    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Libellé pour le nom d'utilisateur
        JLabel usernameLabel = new JLabel("Nom d'utilisateur :");
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(usernameLabel, gbc);
        
        // Champ de saisie pour le nom d'utilisateur
        usernameField = new JTextField();
        usernameField.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1;
        gbc.gridy = 0;
        formPanel.add(usernameField, gbc);
        
        // Libellé pour le mot de passe
        JLabel passwordLabel = new JLabel("Mot de passe :");
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(passwordLabel, gbc);
        
        // Champ de saisie pour le mot de passe
        passwordField = new JPasswordField();
        passwordField.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1;
        gbc.gridy = 1;
        formPanel.add(passwordField, gbc);
        
        // Ajouter un écouteur de touche pour se connecter en appuyant sur Entrée
        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performLogin();
                }
            }
        });
        
        // Libellé pour les messages d'erreur
        errorLabel = new JLabel("");
        errorLabel.setForeground(Color.RED);
        errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        formPanel.add(errorLabel, gbc);
        
        return formPanel;
    }
    
    /**
     * Crée le panneau de boutons
     * 
     * @return Le panneau de boutons
     */
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        // Bouton de connexion
        loginButton = new JButton("Se connecter");
        loginButton.setPreferredSize(new Dimension(150, 40));
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performLogin();
            }
        });
        
        buttonPanel.add(loginButton);
        
        return buttonPanel;
    }
    
    /**
     * Exécute la tentative de connexion
     */
    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        // Validation basique des champs
        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Veuillez remplir tous les champs.");
            return;
        }
        
        // Tentative d'authentification
        if (authController.login(username, password)) {
            // Connexion réussie
            errorLabel.setText("");
            openAppropriateView();
        } else {
            // Échec de connexion
            errorLabel.setText("Nom d'utilisateur ou mot de passe incorrect.");
            passwordField.setText("");
            
            // Vérification du nombre d'échecs de connexion
            int attempts = authController.getLoginAttempts();
            if (attempts >= AppConfig.MAX_LOGIN_ATTEMPTS) {
                JOptionPane.showMessageDialog(this,
                        "Trop de tentatives échouées. L'application va se fermer.",
                        "Sécurité", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        }
    }
    
    /**
     * Ouvre la vue appropriée en fonction du type d'utilisateur connecté
     */
    private void openAppropriateView() {
        if (authController.isCurrentUserSecretary()) {
            // Ouvrir la vue principale pour la secrétaire
            Secretary secretary = authController.getCurrentSecretary();
            SwingUtilities.invokeLater(() -> {
                MainView mainView = new MainView(secretary);
                mainView.setVisible(true);
                this.dispose(); // Fermer la fenêtre de connexion
            });
        } else if (authController.isCurrentUserDoctor()) {
            // Ouvrir la vue principale pour le médecin
            Doctor doctor = authController.getCurrentDoctor();
            SwingUtilities.invokeLater(() -> {
                MainView mainView = new MainView(doctor);
                mainView.setVisible(true);
                this.dispose(); // Fermer la fenêtre de connexion
            });
        } else {
            // Cas inattendu
            JOptionPane.showMessageDialog(this,
                    "Type d'utilisateur non reconnu. Contactez l'administrateur.",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Point d'entrée principal de l'application
     * 
     * @param args Les arguments de la ligne de commande
     */
    public static void main(String[] args) {
        try {
            // Configurer le look and feel du système
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | 
                IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        
        // Lancer l'application
        SwingUtilities.invokeLater(() -> {
            LoginView loginView = new LoginView();
            loginView.setVisible(true);
        });
    }
}