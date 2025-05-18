package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.BoxLayout;
import controller.AppointmentController;
import controller.AuthController;
import controller.DoctorController;
import controller.PlanningController;
import dao.DAOException;
import model.Appointment;
import model.AppointmentStatus;
import model.Doctor;
import util.DateTimeUtils;

/**
 * Vue pour la gestion des plannings des médecins.
 * Permet de visualiser et d'exporter les plannings journaliers et hebdomadaires.
 */
public class PlanningView extends JPanel {
    
    private static final long serialVersionUID = 1L;
    
    // Contrôleurs
    private final PlanningController planningController;
    private final DoctorController doctorController;
    private final AppointmentController appointmentController;
    private final AuthController authController;
    
    // Composants de l'interface
    private JPanel controlPanel;
    private JPanel contentPanel;
    
    private JComboBox<Doctor> doctorComboBox;
    private JComboBox<LocalDate> dateComboBox;
    private JCheckBox weeklyViewCheckbox;
    private JButton previousButton;
    private JButton nextButton;
    private JButton todayButton;
    private JButton exportButton;
    
    // État courant
    private Doctor currentDoctor;
    private LocalDate currentDate;
    private boolean weeklyView = false;
    private List<Appointment> currentAppointments;
    
    /**
     * Constructeur pour secrétaire (tous les médecins)
     */
    public PlanningView() {
        this.planningController = PlanningController.getInstance();
        this.doctorController = DoctorController.getInstance();
        this.appointmentController = AppointmentController.getInstance();
        this.authController = AuthController.getInstance();
        
        initializeUI();
        this.currentDate = LocalDate.now();

        loadDoctors();
        
        // Date par défaut = aujourd'hui
        currentDate = LocalDate.now();
        updateDateComboBox();
    }
    
    /**
     * Constructeur pour médecin (planning personnel uniquement)
     * 
     * @param doctor Le médecin connecté
     */
    public PlanningView(Doctor doctor) {
        this.planningController = PlanningController.getInstance();
        this.doctorController = DoctorController.getInstance();
        this.appointmentController = AppointmentController.getInstance();
        this.authController = AuthController.getInstance();
        this.currentDate = LocalDate.now();
        this.currentDoctor = doctor;
        
        initializeUI();
        
        // Désactiver la sélection de médecin pour un médecin connecté
        doctorComboBox.setEnabled(false);
        doctorComboBox.addItem(doctor);
        doctorComboBox.setSelectedItem(doctor);
        
        // Date par défaut = aujourd'hui
        currentDate = LocalDate.now();
        updateDateComboBox();
        
        // Charger le planning du médecin
        loadPlanning();
    }
    
    /**
     * Initialise l'interface utilisateur
     */
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Panneau de contrôle
        controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.NORTH);
        
        // Panneau de contenu (calendrier)
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createTitledBorder("Planning"));
        add(contentPanel, BorderLayout.CENTER);
    }
    
    /**
     * Crée le panneau de contrôle
     * 
     * @return Le panneau de contrôle
     */
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Options"));
        
        // Panneau de sélection
        JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        
        // Sélection du médecin
        JLabel doctorLabel = new JLabel("Médecin :");
        selectionPanel.add(doctorLabel);
        
        doctorComboBox = new JComboBox<>();
        doctorComboBox.setPreferredSize(new Dimension(200, 25));
        doctorComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentDoctor = (Doctor) doctorComboBox.getSelectedItem();
                loadPlanning();
            }
        });
        selectionPanel.add(doctorComboBox);
        
        // Sélection de la date
        JLabel dateLabel = new JLabel("Date :");
        selectionPanel.add(dateLabel);
        
        dateComboBox = new JComboBox<>();
        dateComboBox.setRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof LocalDate) {
                    LocalDate date = (LocalDate) value;
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy", Locale.FRENCH);
                    value = date.format(formatter);
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });
        dateComboBox.setPreferredSize(new Dimension(200, 25));
        dateComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (dateComboBox.getSelectedItem() instanceof LocalDate) {
                    currentDate = (LocalDate) dateComboBox.getSelectedItem();
                    loadPlanning();
                }
            }
        });
        selectionPanel.add(dateComboBox);
        
        // Vue hebdomadaire
        weeklyViewCheckbox = new JCheckBox("Vue hebdomadaire");
        weeklyViewCheckbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                weeklyView = weeklyViewCheckbox.isSelected();
                loadPlanning();
            }
        });
        selectionPanel.add(weeklyViewCheckbox);
        
        panel.add(selectionPanel, BorderLayout.CENTER);
        
        // Boutons de navigation
        JPanel navigationPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        
        previousButton = new JButton("◀ Précédent");
        previousButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                navigateToPrevious();
            }
        });
        navigationPanel.add(previousButton);
        
        todayButton = new JButton("Aujourd'hui");
        todayButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                navigateToToday();
            }
        });
        navigationPanel.add(todayButton);
        
        nextButton = new JButton("Suivant ▶");
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                navigateToNext();
            }
        });
        navigationPanel.add(nextButton);
        
        exportButton = new JButton("Exporter PDF");
        exportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportPlanning();
            }
        });
        navigationPanel.add(exportButton);
        
        panel.add(navigationPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    /**
     * Charge la liste des médecins
     */
    private void loadDoctors() {
        try {
            List<Doctor> doctors = doctorController.getAllDoctors();
            
            doctorComboBox.removeAllItems();
            for (Doctor doctor : doctors) {
                doctorComboBox.addItem(doctor);
            }
            
            if (!doctors.isEmpty()) {
                currentDoctor = doctors.get(0);
                doctorComboBox.setSelectedItem(currentDoctor);
                loadPlanning();
            }
            
        } catch (DAOException e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors du chargement des médecins: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Met à jour le combobox des dates (semaine courante)
     */
    private void updateDateComboBox() {
        dateComboBox.removeAllItems();
        
        // Ajouter les dates de la semaine en cours
        LocalDate startDate = currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        for (int i = 0; i < 7; i++) {
            LocalDate date = startDate.plusDays(i);
            dateComboBox.addItem(date);
        }
        
        dateComboBox.setSelectedItem(currentDate);
    }
    
    /**
     * Charge et affiche le planning
     */
    private void loadPlanning() {
        if (currentDoctor == null) {
            return;
        }
        if (currentDate == null) {
        currentDate = LocalDate.now();
        updateDateComboBox();
    }
        try {
            if (weeklyView) {
                loadWeeklyPlanning();
            } else {
                loadDailyPlanning();
            }
        } catch (DAOException e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors du chargement du planning: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Charge et affiche le planning d'une journée
     * 
     * @throws DAOException En cas d'erreur de chargement des données
     */
    private void loadDailyPlanning() throws DAOException {
        // Récupérer les rendez-vous du médecin pour la date spécifiée
        currentAppointments = appointmentController.getAppointmentsByDoctorAndDate(currentDoctor.getId(), currentDate);
        
        // Créer le panneau de planning journalier
        JPanel dailyPanel = createDailyPlanningPanel();
        
        // Mettre à jour le panneau de contenu
        contentPanel.removeAll();
        contentPanel.add(new JScrollPane(dailyPanel), BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    /**
     * Charge et affiche le planning d'une semaine
     * 
     * @throws DAOException En cas d'erreur de chargement des données
     */
    private void loadWeeklyPlanning() throws DAOException {
        // Calculer le premier jour de la semaine
        LocalDate weekStartDate = currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEndDate = weekStartDate.plusDays(6); // Dimanche
        
        // Récupérer les rendez-vous du médecin pour la semaine
        currentAppointments = appointmentController.getAppointmentsByDoctorAndDateRange(
                currentDoctor.getId(), weekStartDate, weekEndDate);
        
        // Créer le panneau de planning hebdomadaire
        JPanel weeklyPanel = createWeeklyPlanningPanel(weekStartDate);
        
        // Mettre à jour le panneau de contenu
        contentPanel.removeAll();
        contentPanel.add(new JScrollPane(weeklyPanel), BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    /**
     * Crée un panneau de planning journalier
     * 
     * @return Le panneau de planning journalier
     */
    
    private JPanel createDailyPlanningPanel() {
     if (currentDate == null) {
        currentDate = LocalDate.now();
    }
    JPanel panel = new JPanel(new BorderLayout(0, 10));
    
    // En-tête avec date et médecin
    JPanel headerPanel = new JPanel(new BorderLayout());
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy", Locale.FRENCH);
    JLabel dateLabel = new JLabel(currentDate.format(dateFormatter), SwingConstants.CENTER);
    dateLabel.setFont(new Font("Arial", Font.BOLD, 16));
    headerPanel.add(dateLabel, BorderLayout.NORTH);
    
    JLabel doctorLabel = new JLabel("Dr. " + currentDoctor.getFullName() + " - " + currentDoctor.getSpecialty(), SwingConstants.CENTER);
    doctorLabel.setFont(new Font("Arial", Font.ITALIC, 14));
    headerPanel.add(doctorLabel, BorderLayout.CENTER);
    
    panel.add(headerPanel, BorderLayout.NORTH);
    
    // Panneau de planning
    JPanel timelinePanel = new JPanel(new GridLayout(0, 1, 0, 1));
    timelinePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    
    // Générer des créneaux horaires avec gestion sécurisée
    String workStart = "09:00"; // Valeur par défaut
    String workEnd = "18:00";   // Valeur par défaut
    
    // Traiter les horaires de travail du médecin de manière sécurisée
    if (currentDoctor.getWorkHours() != null && !currentDoctor.getWorkHours().isEmpty()) {
        try {
            String workHours = currentDoctor.getWorkHours();
            // Récupérer le jour de la semaine actuel
            String dayName = currentDate.getDayOfWeek().toString().toLowerCase();
            
            // Chercher les horaires pour ce jour spécifique
            if (workHours.contains(dayName + ":")) {
                String[] days = workHours.split(";");
                for (String day : days) {
                    if (day.startsWith(dayName + ":")) {
                        String[] parts = day.split(":");
                        if (parts.length > 1) {
                            String timeRanges = parts[1];
                            String[] ranges = timeRanges.split(",");
                            if (ranges.length > 0) {
                                String[] times = ranges[0].split("-");
                                if (times.length >= 2) {
                                    workStart = times[0].trim();
                                    workEnd = times[1].trim();
                                }
                            }
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            // En cas d'erreur, utiliser les valeurs par défaut
            System.err.println("Erreur lors du traitement des horaires de travail: " + e.getMessage());
        }
    }
    
    List<String> timeSlots = DateTimeUtils.generateTimeSlots(workStart, workEnd, 15);
    
    // Continuez avec le reste de la méthode...
        // Ajouter chaque créneau horaire
        for (String timeSlot : timeSlots) {
            JPanel slotPanel = new JPanel(new BorderLayout());
            slotPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)));
            
            JLabel timeLabel = new JLabel(timeSlot);
            timeLabel.setPreferredSize(new Dimension(60, 25));
            slotPanel.add(timeLabel, BorderLayout.WEST);
            
            // Vérifier si un rendez-vous existe pour ce créneau
            LocalDate date = currentDate;
            int hour = Integer.parseInt(timeSlot.split(":")[0]);
            int minute = Integer.parseInt(timeSlot.split(":")[1]);
            
            boolean hasAppointment = false;
            for (Appointment appointment : currentAppointments) {
                LocalDate appointmentDate = appointment.getStartDateTime().toLocalDate();
                int appointmentHour = appointment.getStartDateTime().getHour();
                int appointmentMinute = appointment.getStartDateTime().getMinute();
                
                if (date.equals(appointmentDate) && hour == appointmentHour && minute == appointmentMinute) {
                    hasAppointment = true;
                    
                    JPanel appointmentPanel = createAppointmentPanel(appointment);
                    slotPanel.add(appointmentPanel, BorderLayout.CENTER);
                    break;
                }
            }
            
            // Si pas de rendez-vous, ajouter un espace vide
            if (!hasAppointment) {
                JPanel emptyPanel = new JPanel();
                emptyPanel.setBackground(Color.WHITE);
                emptyPanel.setPreferredSize(new Dimension(200, 25));
                slotPanel.add(emptyPanel, BorderLayout.CENTER);
            }
            
            timelinePanel.add(slotPanel);
        }
        
        panel.add(new JScrollPane(timelinePanel), BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Crée un panneau de planning hebdomadaire
     * 
     * @param weekStartDate Premier jour de la semaine
     * @return Le panneau de planning hebdomadaire
     */
    private JPanel createWeeklyPlanningPanel(LocalDate weekStartDate) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        
        // En-tête avec semaine et médecin
        JPanel headerPanel = new JPanel(new BorderLayout());
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String weekRange = "Semaine du " + dateFormatter.format(weekStartDate) + 
                          " au " + dateFormatter.format(weekStartDate.plusDays(6));
        JLabel weekLabel = new JLabel(weekRange, SwingConstants.CENTER);
        weekLabel.setFont(new Font("Arial", Font.BOLD, 16));
        headerPanel.add(weekLabel, BorderLayout.NORTH);
        
        JLabel doctorLabel = new JLabel("Dr. " + currentDoctor.getFullName() + " - " + currentDoctor.getSpecialty(), SwingConstants.CENTER);
        doctorLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        headerPanel.add(doctorLabel, BorderLayout.CENTER);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // Panneau de planning
        JPanel weekPanel = new JPanel(new GridLayout(1, 7, 5, 0));
        
        // Créer un panneau pour chaque jour de la semaine
        for (int i = 0; i < 7; i++) {
            LocalDate dayDate = weekStartDate.plusDays(i);
            JPanel dayPanel = createDayPanel(dayDate);
            weekPanel.add(dayPanel);
        }
        
        panel.add(new JScrollPane(weekPanel), BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Crée un panneau pour un jour dans la vue hebdomadaire
     * 
     * @param date La date du jour
     * @return Le panneau du jour
     */
    private JPanel createDayPanel(LocalDate date) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        
        // En-tête du jour
        String dayName = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.FRENCH);
        String dayHeader = dayName + " " + date.getDayOfMonth();
        JLabel headerLabel = new JLabel(dayHeader, SwingConstants.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 12));
        headerLabel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
        panel.add(headerLabel, BorderLayout.NORTH);
        
        // Contenu des rendez-vous du jour
        JPanel appointmentsPanel = new JPanel();
        appointmentsPanel.setLayout(new BoxLayout(appointmentsPanel, BoxLayout.Y_AXIS));
        appointmentsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Filtrer les rendez-vous pour ce jour
        List<Appointment> dayAppointments = currentAppointments.stream()
                .filter(a -> a.getStartDateTime().toLocalDate().equals(date))
                .sorted((a1, a2) -> a1.getStartDateTime().compareTo(a2.getStartDateTime()))
                .collect(java.util.stream.Collectors.toList());
        
        if (dayAppointments.isEmpty()) {
            JLabel emptyLabel = new JLabel("Aucun rendez-vous", SwingConstants.CENTER);
            emptyLabel.setForeground(Color.GRAY);
            appointmentsPanel.add(emptyLabel);
        } else {
            // Ajouter chaque rendez-vous
            for (Appointment appointment : dayAppointments) {
                JPanel appointmentPanel = createCompactAppointmentPanel(appointment);
                appointmentsPanel.add(appointmentPanel);
                appointmentsPanel.add(Box.createVerticalStrut(5));
            }
        }
        
        panel.add(new JScrollPane(appointmentsPanel), BorderLayout.CENTER);
        
        // Bordure différente si c'est le jour courant
        if (date.equals(LocalDate.now())) {
            panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.BLUE, 2),
                    BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        } else {
            panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                    BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        }
        
        return panel;
    }
    
    /**
     * Crée un panneau de rendez-vous pour la vue journalière
     * 
     * @param appointment Le rendez-vous à afficher
     * @return Le panneau de rendez-vous
     */
    private JPanel createAppointmentPanel(Appointment appointment) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(getStatusColor(appointment.getStatus()), 2),
                BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        panel.setBackground(new Color(240, 240, 255));
        
        // Heure et durée
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        String timeText = appointment.getStartDateTime().format(timeFormatter) + 
                         " - " + appointment.getEndDateTime().format(timeFormatter) +
                         " (" + appointment.getDurationMinutes() + " min)";
        JLabel timeLabel = new JLabel(timeText);
        timeLabel.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(timeLabel, BorderLayout.NORTH);
        
        // Patient et type
        String patientName = appointment.getPatient() != null ? 
                           appointment.getPatient().getFullName() : 
                           "Patient #" + appointment.getPatientId();
        JLabel patientLabel = new JLabel(patientName);
        patientLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        panel.add(patientLabel, BorderLayout.CENTER);
        
        String typeAndStatus = appointment.getAppointmentType() + " - " + appointment.getStatus().getLabel();
        JLabel typeLabel = new JLabel(typeAndStatus);
        typeLabel.setFont(new Font("Arial", Font.ITALIC, 10));
        panel.add(typeLabel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Crée un panneau de rendez-vous compact pour la vue hebdomadaire
     * 
     * @param appointment Le rendez-vous à afficher
     * @return Le panneau de rendez-vous compact
     */
    private JPanel createCompactAppointmentPanel(Appointment appointment) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(getStatusColor(appointment.getStatus()), 1),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        panel.setBackground(new Color(240, 240, 255));
        
        // Heure
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        JLabel timeLabel = new JLabel(appointment.getStartDateTime().format(timeFormatter));
        timeLabel.setFont(new Font("Arial", Font.BOLD, 10));
        panel.add(timeLabel, BorderLayout.WEST);
        
        // Patient
        String patientName = appointment.getPatient() != null ? 
                           appointment.getPatient().getFullName() : 
                           "Patient #" + appointment.getPatientId();
        JLabel patientLabel = new JLabel(patientName);
        patientLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        panel.add(patientLabel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Obtient une couleur selon le statut du rendez-vous
     * 
     * @param status Le statut du rendez-vous
     * @return La couleur correspondante
     */
    private Color getStatusColor(AppointmentStatus status) {
        switch (status) {
            case PLANIFIE:
                return Color.BLUE;
            case CONFIRME:
                return Color.GREEN;
            case ANNULE_PATIENT:
            case ANNULE_CABINET:
                return Color.RED;
            case REALISE:
                return Color.DARK_GRAY;
            case ABSENT:
                return Color.ORANGE;
            default:
                return Color.BLACK;
        }
    }
    
    /**
     * Navigue vers la période précédente (jour ou semaine)
     */
    private void navigateToPrevious() {
        if (weeklyView) {
            // Semaine précédente
            currentDate = currentDate.minusWeeks(1);
        } else {
            // Jour précédent
            currentDate = currentDate.minusDays(1);
        }
        
        updateDateComboBox();
        loadPlanning();
    }
    
    /**
     * Navigue vers la période suivante (jour ou semaine)
     */
    private void navigateToNext() {
        if (weeklyView) {
            // Semaine suivante
            currentDate = currentDate.plusWeeks(1);
        } else {
            // Jour suivant
            currentDate = currentDate.plusDays(1);
        }
        
        updateDateComboBox();
        loadPlanning();
    }
    
    /**
     * Navigue vers la date du jour
     */
    private void navigateToToday() {
        currentDate = LocalDate.now();
        updateDateComboBox();
        loadPlanning();
    }
    
    /**
     * Exporte le planning en PDF
     */
    public void exportPlanning() {
        if (currentDoctor == null) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez sélectionner un médecin.",
                    "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        try {
            String pdfPath;
            
            if (weeklyView) {
                // Exporter le planning hebdomadaire
                LocalDate weekStartDate = currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                pdfPath = planningController.exportWeeklyPlanning(currentDoctor, weekStartDate);
            } else {
                // Exporter le planning journalier
                pdfPath = planningController.exportDailyPlanning(currentDoctor, currentDate);
            }
            
            // Demander à l'utilisateur s'il veut ouvrir le fichier
            int response = JOptionPane.showConfirmDialog(this,
                    "Le planning a été exporté avec succès.\nVoulez-vous ouvrir le fichier PDF ?",
                    "Export réussi", JOptionPane.YES_NO_OPTION);
            
            if (response == JOptionPane.YES_OPTION) {
                File pdfFile = new File(pdfPath);
                if (pdfFile.exists()) {
                    java.awt.Desktop.getDesktop().open(pdfFile);
                }
            }
            
        } catch (DAOException | IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors de l'exportation du planning: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Rafraîchit les données affichées
     */
    public void refreshData() {
        loadPlanning();
    }
}