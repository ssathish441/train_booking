package com.sathish.thodar.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

public class ParseHelper {
    
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) return false;
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return Pattern.compile(emailRegex).matcher(email).matches();
    }

    public static boolean isValidMobile(String mobileNo) {
        return mobileNo != null && mobileNo.matches("\\d{10}");
    }

    public static Long dateToEpoch(String dateString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            sdf.setLenient(false); 
            Date date = sdf.parse(dateString);
            return date.getTime();
        } catch (ParseException e) {
            return null; 
        }
    }

    public static String epochToDateString(Long epochTime) {
        if (epochTime == null) return "N/A";
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        return sdf.format(new Date(epochTime));
    }

    public static String epochToDateTimeString(Long epochTime) {
        if (epochTime == null) return "N/A";
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        return sdf.format(new Date(epochTime));
    }
}