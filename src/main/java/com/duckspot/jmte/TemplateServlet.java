package com.duckspot.jmte;

import com.floreysoft.jmte.Engine;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServlet;

/**
 *
 * @author Peter M Dobson
 */
public class TemplateServlet extends HttpServlet {
    
    protected Engine templateEngine;
    protected SimpleDateRenderer simpleDateRenderer;
    
    @Override
    public void init() {
        // TODO: 8 separate DateRenderer & Engine for separate locales?
        templateEngine = new Engine();
        simpleDateRenderer = new SimpleDateRenderer();
        simpleDateRenderer.defineFormat("shortTime", "h:mma", 
                new String[] {"a","p"});        
        simpleDateRenderer.defineFormat("urlTime", "yyyy.MM.dd.HH.mm");
        simpleDateRenderer.defineFormat("urlDate", "yyyy.MM.dd");        
        templateEngine.registerNamedRenderer(simpleDateRenderer);
        templateEngine.registerNamedRenderer(new BooleanRenderer());
        templateEngine.registerNamedRenderer(new HoursMinutesRenderer());
    }
    
    protected String ts(String template, Map<String,Object> model) {
        Locale locale;
        if (model.containsKey("locale")) {
            locale = (Locale)model.get("locale");
        } else {
            locale = Locale.getDefault();
        }
        return templateEngine.transform(template, locale, model);
    }
    
    protected String template(String pathname, Map<String,Object> model) 
            throws IOException {
        Locale locale;
        if (model.containsKey("locale")) {
            locale = (Locale)model.get("locale");
        } else {
            locale = Locale.getDefault();
        }
        // TODO: 8 make path prefix below web.xml servlet-config option
        String fullpath = "/WEB-INF/templates/" + pathname;
        InputStream stream = getServletContext().getResourceAsStream(fullpath);
        if (stream == null)
            throw new Error("template not found: "+pathname);
        java.util.Scanner s = new java.util.Scanner(stream).useDelimiter("\\A");
        String template = s.hasNext() ? s.next() : null;
        // TO DO: 9 consider: are these close calls useful?
        s.close();
        stream.close();
        if (template == null)
            throw new Error("template empty: "+pathname);
        return templateEngine.transform(template, locale, fullpath, model);
    }
}