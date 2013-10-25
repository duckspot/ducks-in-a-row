package com.duckspot.jmte;

import com.duckspot.diar.model.Util;
import com.floreysoft.jmte.NamedRenderer;
import com.floreysoft.jmte.RenderFormatInfo;
import java.util.Locale;

public class BooleanRenderer implements NamedRenderer {
    
    
    public String getName() {
        return "boolean";
    }

    public RenderFormatInfo getFormatInfo() {
        return null;
    }

    public Class<?>[] getSupportedClasses() {
        return new Class<?>[] { Boolean.class };
    }
    
    public String render(Object o, String format, Locale locale) {

        return Util.renderBoolean((Boolean)o, format);        
    }
}
