package com.project.server.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class HelperUtil {
    public static String generateFilename(String session, String semester){
        return String.format("Bowen_University_%s_%s_Semester_TimeTable", session, semester);
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
}
