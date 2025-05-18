package util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import config.AppConfig;
import model.Appointment;
import model.Doctor;

/**
 * Classe utilitaire pour exporter des données au format PDF.
 * Utilise la bibliothèque iText pour générer des documents PDF.
 */
public class PDFExporter {
    
    private static final Logger LOGGER = Logger.getLogger(PDFExporter.class.getName());
    
    // Constantes pour la mise en page
    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
    private static final Font SUBTITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
    private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    private static final Font NORMAL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10);
    
    /**
     * Empêche l'instanciation de cette classe utilitaire
     */
    private PDFExporter() {
        throw new AssertionError("Cette classe ne doit pas être instanciée");
    }
    
    /**
     * Exporte un planning journalier pour un médecin au format PDF
     * 
     * @param doctor Le médecin concerné
     * @param date La date du planning
     * @param appointments La liste des rendez-vous
     * @param outputFilePath Le chemin du fichier PDF à générer
     * @return Le chemin du fichier PDF généré
     * @throws IOException Si une erreur survient lors de la génération du PDF
     */
    public static String exportDailyPlanningToPDF(Doctor doctor, LocalDate date, 
                                                List<Appointment> appointments, 
                                                String outputFilePath) throws IOException {
        
        Document document = new Document(PageSize.A4);
        
        try {
            // Créer le dossier de destination si nécessaire
            File dir = new File(outputFilePath).getParentFile();
            if (dir != null && !dir.exists()) {
                dir.mkdirs();
            }
            
            PdfWriter.getInstance(document, new FileOutputStream(outputFilePath));
            document.open();
            
            // Titre
            Paragraph title = new Paragraph("Planning Journalier", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            
            // Sous-titre avec médecin et date
            Paragraph subtitle = new Paragraph(
                "Dr. " + doctor.getFullName() + " - " + 
                DateTimeFormatter.ofPattern(AppConfig.DATE_FORMAT).format(date),
                SUBTITLE_FONT
            );
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingBefore(10);
            subtitle.setSpacingAfter(20);
            document.add(subtitle);
            
            if (appointments.isEmpty()) {
                Paragraph noAppointments = new Paragraph("Aucun rendez-vous programmé pour cette journée.", NORMAL_FONT);
                noAppointments.setAlignment(Element.ALIGN_CENTER);
                document.add(noAppointments);
            } else {
                // Tableau des rendez-vous
                PdfPTable table = new PdfPTable(5); // 5 colonnes
                table.setWidthPercentage(100);
                table.setSpacingBefore(10f);
                table.setSpacingAfter(10f);
                
                // Définir la largeur relative des colonnes
                float[] columnWidths = {0.15f, 0.15f, 0.3f, 0.2f, 0.2f};
                table.setWidths(columnWidths);
                
                // En-têtes du tableau
                addTableHeader(table, new String[]{"Heure", "Durée", "Patient", "Type", "Statut"});
                
                // Contenu du tableau
                for (Appointment appointment : appointments) {
                    addAppointmentRow(table, appointment);
                }
                
                document.add(table);
            }
            
            // Informations supplémentaires
            Paragraph footer = new Paragraph(
                "Document généré le " + 
                DateTimeFormatter.ofPattern(AppConfig.DATETIME_FORMAT).format(LocalDateTime.now()),
                NORMAL_FONT
            );
            footer.setAlignment(Element.ALIGN_RIGHT);
            footer.setSpacingBefore(20);
            document.add(footer);
            
        } catch (DocumentException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la création du document PDF", e);
            throw new IOException("Erreur lors de la génération du PDF: " + e.getMessage(), e);
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }
        
        return outputFilePath;
    }
    
    /**
     * Exporte un planning hebdomadaire pour un médecin au format PDF
     * 
     * @param doctor Le médecin concerné
     * @param weekStartDate La date de début de la semaine
     * @param appointments La liste des rendez-vous
     * @param outputFilePath Le chemin du fichier PDF à générer
     * @return Le chemin du fichier PDF généré
     * @throws IOException Si une erreur survient lors de la génération du PDF
     */
    public static String exportWeeklyPlanningToPDF(Doctor doctor, LocalDate weekStartDate, 
                                                 List<Appointment> appointments, 
                                                 String outputFilePath) throws IOException {
        
        Document document = new Document(PageSize.A4.rotate()); // Format paysage
        
        try {
            // Créer le dossier de destination si nécessaire
            File dir = new File(outputFilePath).getParentFile();
            if (dir != null && !dir.exists()) {
                dir.mkdirs();
            }
            
            PdfWriter.getInstance(document, new FileOutputStream(outputFilePath));
            document.open();
            
            // Titre
            Paragraph title = new Paragraph("Planning Hebdomadaire", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            
            // Sous-titre avec médecin et semaine
            LocalDate weekEndDate = weekStartDate.plusDays(6);
            Paragraph subtitle = new Paragraph(
                "Dr. " + doctor.getFullName() + " - Semaine du " + 
                DateTimeFormatter.ofPattern(AppConfig.DATE_FORMAT).format(weekStartDate) + 
                " au " + 
                DateTimeFormatter.ofPattern(AppConfig.DATE_FORMAT).format(weekEndDate),
                SUBTITLE_FONT
            );
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingBefore(10);
            subtitle.setSpacingAfter(20);
            document.add(subtitle);
            
            if (appointments.isEmpty()) {
                Paragraph noAppointments = new Paragraph("Aucun rendez-vous programmé pour cette semaine.", NORMAL_FONT);
                noAppointments.setAlignment(Element.ALIGN_CENTER);
                document.add(noAppointments);
            } else {
                // Trier les rendez-vous par jour puis par heure
                appointments.sort((a1, a2) -> a1.getStartDateTime().compareTo(a2.getStartDateTime()));
                
                // Organiser les rendez-vous par jour
                for (int dayOffset = 0; dayOffset < 7; dayOffset++) {
                    LocalDate currentDate = weekStartDate.plusDays(dayOffset);
                    
                    // En-tête du jour
                    Paragraph dayHeader = new Paragraph(
                        currentDate.getDayOfWeek().toString() + " " + 
                        DateTimeFormatter.ofPattern(AppConfig.DATE_FORMAT).format(currentDate),
                        HEADER_FONT
                    );
                    dayHeader.setSpacingBefore(15);
                    dayHeader.setSpacingAfter(5);
                    document.add(dayHeader);
                    
                    // Filtrer les rendez-vous du jour
                    final int dayOffsetFinal = dayOffset;
                    List<Appointment> dayAppointments = appointments.stream()
                            .filter(a -> a.getStartDateTime().toLocalDate().equals(weekStartDate.plusDays(dayOffsetFinal)))
                            .toList();
                    
                    if (dayAppointments.isEmpty()) {
                        Paragraph noDayAppointments = new Paragraph("Aucun rendez-vous", NORMAL_FONT);
                        noDayAppointments.setIndentationLeft(20);
                        document.add(noDayAppointments);
                    } else {
                        // Tableau des rendez-vous du jour
                        PdfPTable table = new PdfPTable(5); // 5 colonnes
                        table.setWidthPercentage(100);
                        
                        // Définir la largeur relative des colonnes
                        float[] columnWidths = {0.15f, 0.15f, 0.3f, 0.2f, 0.2f};
                        table.setWidths(columnWidths);
                        
                        // En-têtes du tableau
                        addTableHeader(table, new String[]{"Heure", "Durée", "Patient", "Type", "Statut"});
                        
                        // Contenu du tableau
                        for (Appointment appointment : dayAppointments) {
                            addAppointmentRow(table, appointment);
                        }
                        
                        document.add(table);
                    }
                }
            }
            
            // Informations supplémentaires
            Paragraph footer = new Paragraph(
                "Document généré le " + 
                DateTimeFormatter.ofPattern(AppConfig.DATETIME_FORMAT).format(LocalDateTime.now()),
                NORMAL_FONT
            );
            footer.setAlignment(Element.ALIGN_RIGHT);
            footer.setSpacingBefore(20);
            document.add(footer);
            
        } catch (DocumentException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la création du document PDF", e);
            throw new IOException("Erreur lors de la génération du PDF: " + e.getMessage(), e);
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }
        
        return outputFilePath;
    }
    
    /**
     * Ajoute les en-têtes à un tableau PDF
     * 
     * @param table Le tableau PDF
     * @param headers Les en-têtes à ajouter
     */
    private static void addTableHeader(PdfPTable table, String[] headers) {
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, HEADER_FONT));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPadding(5);
            table.addCell(cell);
        }
    }
    
    /**
     * Ajoute une ligne de rendez-vous à un tableau PDF
     * 
     * @param table Le tableau PDF
     * @param appointment Le rendez-vous à ajouter
     */
    private static void addAppointmentRow(PdfPTable table, Appointment appointment) {
        // Heure de début
        String startTime = DateTimeFormatter.ofPattern(AppConfig.TIME_FORMAT)
                            .format(appointment.getStartDateTime());
        PdfPCell cellTime = new PdfPCell(new Phrase(startTime, NORMAL_FONT));
        cellTime.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellTime.setPadding(5);
        table.addCell(cellTime);
        
        // Durée
        PdfPCell cellDuration = new PdfPCell(new Phrase(appointment.getDurationMinutes() + " min", NORMAL_FONT));
        cellDuration.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellDuration.setPadding(5);
        table.addCell(cellDuration);
        
        // Patient
        String patientName = appointment.getPatient() != null ? 
                            appointment.getPatient().getFullName() : 
                            "Patient #" + appointment.getPatientId();
        PdfPCell cellPatient = new PdfPCell(new Phrase(patientName, NORMAL_FONT));
        cellPatient.setPadding(5);
        table.addCell(cellPatient);
        
        // Type de consultation
        String type = appointment.getAppointmentType() != null ? 
                    appointment.getAppointmentType() : "";
        PdfPCell cellType = new PdfPCell(new Phrase(type, NORMAL_FONT));
        cellType.setPadding(5);
        table.addCell(cellType);
        
        // Statut
        PdfPCell cellStatus = new PdfPCell(new Phrase(appointment.getStatus().getLabel(), NORMAL_FONT));
        cellStatus.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellStatus.setPadding(5);
        table.addCell(cellStatus);
    }
    
    /**
     * Génère un nom de fichier pour un export PDF de planning
     * 
     * @param doctor Le médecin concerné
     * @param date La date du planning
     * @param isWeekly Indique s'il s'agit d'un planning hebdomadaire
     * @return Un nom de fichier formaté
     */
    public static String generatePlanningFileName(Doctor doctor, LocalDate date, boolean isWeekly) {
        String doctorName = doctor.getFullName().replaceAll("\\s+", "_").toLowerCase();
        String dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String type = isWeekly ? "hebdo" : "jour";
        
        return AppConfig.PDF_EXPORT_PREFIX + doctorName + "_" + type + "_" + dateStr + ".pdf";
    }
    
    /**
     * Génère le chemin complet pour un export PDF de planning
     * 
     * @param doctor Le médecin concerné
     * @param date La date du planning
     * @param isWeekly Indique s'il s'agit d'un planning hebdomadaire
     * @return Le chemin complet du fichier
     */
    public static String generatePlanningFilePath(Doctor doctor, LocalDate date, boolean isWeekly) {
        String fileName = generatePlanningFileName(doctor, date, isWeekly);
        return AppConfig.EXPORT_DIRECTORY + fileName;
    }
}