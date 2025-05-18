package util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import config.AppConfig;

/**
 * Classe utilitaire pour manipuler les dates et heures dans l'application.
 * Centralise les opérations de formatage et d'analyse des dates.
 */
public class DateTimeUtils {
    
    // Constantes de formatage (correspondant à celles définies dans AppConfig)
    private static final DateTimeFormatter DATE_FORMATTER = 
            DateTimeFormatter.ofPattern(AppConfig.DATE_FORMAT);
    private static final DateTimeFormatter TIME_FORMATTER = 
            DateTimeFormatter.ofPattern(AppConfig.TIME_FORMAT);
    private static final DateTimeFormatter DATETIME_FORMATTER = 
            DateTimeFormatter.ofPattern(AppConfig.DATETIME_FORMAT);
    
    /**
     * Empêche l'instanciation de cette classe utilitaire
     */
    private DateTimeUtils() {
        throw new AssertionError("Cette classe ne doit pas être instanciée");
    }
    
    /**
     * Formate une date (LocalDate) en chaîne de caractères selon le format défini
     * 
     * @param date La date à formater
     * @return La date formatée, ou une chaîne vide si date est null
     */
    public static String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : "";
    }
    
    /**
     * Formate une heure (LocalTime) en chaîne de caractères selon le format défini
     * 
     * @param time L'heure à formater
     * @return L'heure formatée, ou une chaîne vide si time est null
     */
    public static String formatTime(LocalTime time) {
        return time != null ? time.format(TIME_FORMATTER) : "";
    }
    
    /**
     * Formate une date et heure (LocalDateTime) en chaîne de caractères selon le format défini
     * 
     * @param dateTime La date et heure à formater
     * @return La date et heure formatée, ou une chaîne vide si dateTime est null
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATETIME_FORMATTER) : "";
    }
    
    /**
     * Analyse une chaîne de caractères pour la convertir en date
     * 
     * @param dateStr La chaîne de caractères représentant une date
     * @return La date analysée, ou null si la chaîne est vide ou mal formatée
     */
    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        
        try {
            return LocalDate.parse(dateStr.trim(), DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            // Si le format standard échoue, essayer d'autres formats courants
            try {
                return LocalDate.parse(dateStr.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (DateTimeParseException ex) {
                return null;
            }
        }
    }
    
    /**
     * Analyse une chaîne de caractères pour la convertir en heure
     * 
     * @param timeStr La chaîne de caractères représentant une heure
     * @return L'heure analysée, ou null si la chaîne est vide ou mal formatée
     */
    public static LocalTime parseTime(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return null;
        }
        
        try {
            return LocalTime.parse(timeStr.trim(), TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            // Si le format standard échoue, essayer d'autres formats courants
            try {
                return LocalTime.parse(timeStr.trim(), DateTimeFormatter.ISO_LOCAL_TIME);
            } catch (DateTimeParseException ex) {
                return null;
            }
        }
    }
    
    /**
     * Analyse une chaîne de caractères pour la convertir en date et heure
     * 
     * @param dateTimeStr La chaîne de caractères représentant une date et heure
     * @return La date et heure analysée, ou null si la chaîne est vide ou mal formatée
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }
        
        try {
            return LocalDateTime.parse(dateTimeStr.trim(), DATETIME_FORMATTER);
        } catch (DateTimeParseException e) {
            // Si le format standard échoue, essayer d'autres formats courants
            try {
                return LocalDateTime.parse(dateTimeStr.trim(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (DateTimeParseException ex) {
                return null;
            }
        }
    }
    
    /**
     * Combine une date et une heure en un objet LocalDateTime
     * 
     * @param date La date
     * @param time L'heure
     * @return Un objet LocalDateTime combinant la date et l'heure spécifiées
     */
    public static LocalDateTime combineDateTime(LocalDate date, LocalTime time) {
        if (date == null || time == null) {
            return null;
        }
        return LocalDateTime.of(date, time);
    }
    
    /**
     * Génère une liste de créneaux horaires à partir des horaires de début et de fin avec un intervalle donné
     * 
     * @param startTime Heure de début
     * @param endTime Heure de fin
     * @param intervalMinutes Intervalle en minutes entre chaque créneau
     * @return Liste des créneaux horaires formatés
     */
    public static List<String> generateTimeSlots(LocalTime startTime, LocalTime endTime, int intervalMinutes) {
        List<String> timeSlots = new ArrayList<>();
        
        LocalTime current = startTime;
        while (current.isBefore(endTime) || current.equals(endTime)) {
            timeSlots.add(formatTime(current));
            current = current.plusMinutes(intervalMinutes);
        }
        
        return timeSlots;
    }
    
    /**
     * Génère une liste de créneaux horaires à partir des horaires de début et de fin formatés avec un intervalle donné
     * 
     * @param startTimeStr Heure de début formatée
     * @param endTimeStr Heure de fin formatée
     * @param intervalMinutes Intervalle en minutes entre chaque créneau
     * @return Liste des créneaux horaires formatés
     */
    public static List<String> generateTimeSlots(String startTimeStr, String endTimeStr, int intervalMinutes) {
        LocalTime startTime = parseTime(startTimeStr);
        LocalTime endTime = parseTime(endTimeStr);
        
        if (startTime == null || endTime == null) {
            return new ArrayList<>();
        }
        
        return generateTimeSlots(startTime, endTime, intervalMinutes);
    }
    
    /**
     * Vérifie si deux plages de dates se chevauchent
     * 
     * @param start1 Début de la première plage
     * @param end1 Fin de la première plage
     * @param start2 Début de la seconde plage
     * @param end2 Fin de la seconde plage
     * @return true si les plages se chevauchent, false sinon
     */
    public static boolean isOverlapping(LocalDateTime start1, LocalDateTime end1, 
                                     LocalDateTime start2, LocalDateTime end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }
    
    /**
     * Récupère le premier jour de la semaine pour une date donnée
     * 
     * @param date La date de référence
     * @return Le premier jour (lundi) de la semaine contenant la date donnée
     */
    public static LocalDate getFirstDayOfWeek(LocalDate date) {
        return date.minusDays(date.getDayOfWeek().getValue() - 1);
    }
    
    /**
     * Récupère le dernier jour de la semaine pour une date donnée
     * 
     * @param date La date de référence
     * @return Le dernier jour (dimanche) de la semaine contenant la date donnée
     */
    public static LocalDate getLastDayOfWeek(LocalDate date) {
        return getFirstDayOfWeek(date).plusDays(6);
    }
    
    /**
     * Calcule le nombre de jours entre deux dates
     * 
     * @param startDate Date de début
     * @param endDate Date de fin
     * @return Le nombre de jours entre les deux dates (inclusif)
     */
    public static long getDaysBetween(LocalDate startDate, LocalDate endDate) {
        return java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }
}