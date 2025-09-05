package com.fosagri.application.utils;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;

public class AgeUtils {
    
    public static int calculateAge(Date birthDate) {
        if (birthDate == null) {
            return 0;
        }
        
        LocalDate birth;
        if (birthDate instanceof java.sql.Date) {
            // Handle java.sql.Date which doesn't support toInstant()
            birth = ((java.sql.Date) birthDate).toLocalDate();
        } else {
            // Handle java.util.Date
            birth = birthDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        
        LocalDate now = LocalDate.now();
        
        return Period.between(birth, now).getYears();
    }
    
    public static boolean isAdult(Date birthDate) {
        return calculateAge(birthDate) >= 18;
    }
}