package com.duckspot.jmte;

import com.floreysoft.jmte.NamedRenderer;
import com.floreysoft.jmte.RenderFormatInfo;
import java.util.Locale;

public class HoursMinutesRenderer implements NamedRenderer {
    
    
    public String getName() {
        return "hoursMinutes";
    }

    public RenderFormatInfo getFormatInfo() {
        return null;
    }

    public Class<?>[] getSupportedClasses() {
        return new Class<?>[] { Number.class };
    }
    
    public String render(Object o, String format, Locale locale) {
                
        int minutes = ((Number)o).intValue();
        int hours = minutes / 60;
        if (hours > 24) {
            return "-";
        }
        minutes %= 60;
        return String.format("%d:%02d", hours, minutes);
    }    
}
