package com.duckspot.jmte;

import com.floreysoft.jmte.NamedRenderer;
import com.floreysoft.jmte.RenderFormatInfo;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SimpleDateRenderer implements NamedRenderer {
    
    private Map<String,DateFormat> formats = new HashMap<String,DateFormat>();    
    private Locale locale = Locale.getDefault();
    
    public String getName() {
        return "simpleDate";
    }

    public RenderFormatInfo getFormatInfo() {
        return null;
    }

    public Class<?>[] getSupportedClasses() {
        return new Class<?>[] { Date.class, Calendar.class };
    }
    
    public String render(Object o, String format, Locale locale) {
        
        DateFormat df = formats.get(format);
        if (df == null) {
            df = new SimpleDateFormat(format, locale);
            formats.put(format, df);
        }
        if (o instanceof Calendar) {
            return df.format(((Calendar)o).getTime());
        }
        return df.format(o);
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }
    
    public void defineFormat(String name, String format) {
        formats.put(name, new SimpleDateFormat(format, locale));
    }
    
    public void defineFormat(String name, String format, String[] newAmpms) {
        SimpleDateFormat sdf = new SimpleDateFormat(format, locale);
        DateFormatSymbols dfs = sdf.getDateFormatSymbols();
        dfs.setAmPmStrings(newAmpms);
        sdf.setDateFormatSymbols(dfs);
        formats.put(name, sdf);
    }
}
