package com.project.server.util;

import com.project.server.db.models.AppUser;
import com.project.server.db.models.TimeTable;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class HelperUtil {
    public static String generateFilename(String tenantName, String session, String semester){
        return String.format("%s_%s_%s_Semester_TimeTable.docx", sanitizeForFilename(tenantName), session, semester);
    }

    public static String generateStudentFileName(String tenantName, String department, String level){
        return String.format("%s_%s_%s_timetable.docx", sanitizeForFilename(tenantName), level, sanitizeForFilename(department));
    }

    private static String sanitizeForFilename(String value){
        return String.join("_", value.split(" "));
    }
    public static String generateStaffFileName(String userId){
        return String.format("%s_schedules.docx", userId);
    }

    public static LocalDate parseDate(String date){
        return LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public static String getCurrentUserName(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication != null && authentication.isAuthenticated()){
            Object principal = authentication.getPrincipal();

            if(principal instanceof UserDetails){
                return ((UserDetails) principal).getUsername();
            }
        }
        return null;
    }

    public static XWPFRun createTextRun(XWPFDocument document){
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.CENTER);

        return paragraph.createRun();
    }

    public static List<String> generateTimeSlots() {
        List<String> timeSlots = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        LocalTime start = LocalTime.of(8, 0);
        LocalTime end = LocalTime.of(18, 0);

        while (start.isBefore(end)) {
            LocalTime next = start.plusHours(1);
            timeSlots.add(start.format(formatter) + " - " + next.format(formatter));
            start = next;
        }

        return timeSlots;
    }
}
