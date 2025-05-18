import view.LoginView;

/**
 * Classe principale de l'application de gestion de rendez-vous médicaux.
 * Point d'entrée du programme.
 */
public class Application {
    
    /**
     * Méthode principale qui démarre l'application.
     * 
     * @param args Arguments de la ligne de commande (non utilisés)
     */
    public static void main(String[] args) {
        // Configuration de la sécurité et du logging si nécessaire
        configureApplication();
        
        // Lancement de l'interface graphique sur l'EDT (Event Dispatch Thread)
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    // Utiliser le look and feel du système d'exploitation
                    javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                // Créer et afficher la fenêtre de connexion
                LoginView loginView = new LoginView();
                loginView.setVisible(true);
            }
        });
    }
    
    /**
     * Configure les paramètres globaux de l'application.
     */
    private static void configureApplication() {
        // Configurer le niveau de log
        java.util.logging.Logger rootLogger = java.util.logging.Logger.getLogger("");
        rootLogger.setLevel(java.util.logging.Level.INFO);
        
        // Créer le répertoire d'exportation s'il n'existe pas
        java.io.File exportDir = new java.io.File(config.AppConfig.EXPORT_DIRECTORY);
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }
        
        // Autres configurations globales
        System.setProperty("sun.awt.exception.handler", "util.GlobalExceptionHandler");
    }
}