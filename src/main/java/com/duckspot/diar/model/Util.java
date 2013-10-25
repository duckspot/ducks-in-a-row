/* diar/src/com/duckspot/diar/model/Util.java
 * 
 * History:
 * 3/22/13 PD
 * 3/24/13 PD move parseHoursMinutes here, and fix bugs in it
 */
package com.duckspot.diar.model;

import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Peter M Dobson
 */
public class Util {
    
    private static final Pattern notDigits = Pattern.compile("[^0-9]+");

    public static int minutesBetween(Date a, Date b) {        
        long ms = a.getTime() - b.getTime();
        return (int)(ms / 60L / 1000L);
    }
    
    public static Date addMinutes(Date d, int minutes) {        
        long ms = minutes * 60L * 1000L;
        return new Date(d.getTime() + ms);
    }
    
    public static boolean isTrue(String s) {
        if (s == null)
            return false;
        return "true".equalsIgnoreCase(s) || "checked".equalsIgnoreCase(s);
    }
    
    public static void clearSeconds(Calendar calendar) {
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }
    
    public static void clearTime(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        clearSeconds(calendar);
    }
    
    public static String renderHoursMinutes(int minutes) {        
        return String.format("%d:%02d", minutes/60, minutes%60);        
    }
    public static int parseHoursMinutes(String string) {
        
        int minutes = 0;
        Scanner parse = new Scanner(string);
        parse.useDelimiter(notDigits);
        if (parse.hasNext()) {            
            minutes = parse.nextInt();
        }
        if (parse.hasNext()) {
            minutes = minutes * 60 + parse.nextInt();
        }
        return minutes;
    }
    
    private static final Pattern timePattern = 
            Pattern.compile("\\s?(\\d+):(\\d\\d)(\\w*)");
    /**
     * Permissive parse of a time string, into minutes past midnight.  
     * Accepts 24 hour clock, or time followed by 'AM', 'PM'.  
     * AM and PM can be lower case, and can be a single character.
     * 
     * @param t
     * @return minutes past midnight, or -1 if unrecognizable or empty string.
     */
    public static int parseTime(String t) {
        Matcher m = timePattern.matcher(t);        
        if (!m.find()) {
            return -1;
        }
        int hour = Integer.parseInt(m.group(1));
        int minute = Integer.parseInt(m.group(2));
        String ampm = m.group(3);
        boolean am = false;
        boolean pm = false;
        if (ampm.length() > 0) {
            am = ampm.substring(0, 1).equalsIgnoreCase("a");
            pm = ampm.substring(0, 1).equalsIgnoreCase("p");
        }
        if ((am || pm) && hour == 12) {
            hour = 0;
        }
        if (pm) {
            hour += 12;
        }
        return hour * 60 + minute;        
    }
    
    public static String renderBoolean(boolean b, String format) {
        if ("selected".equals(format) || "checked".equals(format)) {
            if (b)
                return format;
            else
                return "";
        } else {
            return String.valueOf(b);
        }
    }    
}
