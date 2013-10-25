/* dair/src/com/duckspot/dair/SettingsServlet.java
 *
 * History:
 * 03/06/13 PD
 * 03/13/13 PD - remove response.encodeURL & response.encodeRedirectURL calls 
 *               because we no longer use sessions.
 * 03/15/13 PD - separate Home, Security, Register and Settings Servlets.
 * 03/31/13 PD - restructure template to use common layout for similar pages
 * 04/09/13 PD - allow modification Timezone
 * 04/09/13 PD - default name from Google userInfo
 * 04/11/13 PD - set isRegistered()
 * 04/11/13 PD - select calendars to pull
 */
package com.duckspot.diar;

import com.duckspot.diar.google.GoogleServices;
import com.duckspot.diar.google.UserInfo;
import com.duckspot.diar.model.GoogleCalendar;
import com.duckspot.diar.model.GoogleCalendarDAO;
import com.duckspot.diar.model.Settings;
import com.duckspot.diar.model.SettingsDAO;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Prompt for user's name.  Later prompt for Locale.  When data submitted, 
 * redirect to READY.
 */
public class SettingsServlet extends AbstractServlet {

    public static class HtmlOption {
        private String ID;
        private String name;
        private boolean selected;

        HtmlOption(String ID, String name, boolean selected) {
            this.ID = ID;
            this.name = name;
            this.selected = selected;
        }                

        public String getID() {
            return ID;
        }

        public void setID(String ID) {
            this.ID = ID;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }
    }
    
    private HtmlOption[] timeZoneList(String selectedID) {
        
        String[] IDs = TimeZone.getAvailableIDs();
        HtmlOption[] result = new HtmlOption[IDs.length];
        int i = 0;
        Arrays.sort(IDs);        
        for(String ID: IDs) {
            result[i++] = new HtmlOption(ID, ID, ID.equals(selectedID));
        }
        return result;
    }
    
    @Override
    protected void doGet(HttpServletRequest request, 
                         HttpServletResponse response)
            throws IOException, ServletException
    {
        checkSecurity(request);
                
        Settings settings = (Settings)request.getAttribute("settings");
        SettingsDAO settingsDAO = 
                (SettingsDAO)request.getAttribute("settingsDAO");
        GoogleCalendarDAO calendarDAO = new GoogleCalendarDAO(settings);                
        GoogleServices google = settings.getGoogle();        
        
        // default name from userInfo
        UserInfo userInfo = google.getUserInfo();
        if (userInfo != null) {
            String name = settings.getName();
            if (name == null || name.isEmpty()) {
                settings.setName(userInfo.getName());
            }
        }

        Map<String,Object> model = new HashMap<String,Object>();
        
        model.put("settings", settings);
        model.put("userBlock", template("layout/userBlock.html", model));
        
        model.put("form", settings.getForm());                
        model.put("timeZoneList", timeZoneList(settings.getTimeZoneID()));
                
        model.put("google", google);
        for (GoogleCalendar cal: calendarDAO.list()) {
            
        }
        
        // allow content to be specified by init-param
        String content = this.getInitParameter("content");
        if (content == null) {
            content = "settings.html";
        }
        model.put("content", template(content, model));
        
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.print(template("layout/twoColumn.html", model));
    }
    
    /**
     * POST is used to submit settings changes, to be saved in the UserEntity
     * session attribute.
     */
    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws IOException, ServletException
    {
        checkSecurity(request);
        
        Settings settings = (Settings)request.getAttribute("settings");
        SettingsDAO settingsDAO = 
                (SettingsDAO)request.getAttribute("settingsDAO");
        
        Map<String,String> form = new HashMap<String,String>();
        for (String name: request.getParameter("fields").split(",")) {
            form.put(name, request.getParameter(name));
        }
        
        if ("save".equalsIgnoreCase(form.get("action")) || 
            "next".equalsIgnoreCase(form.get("action"))) {
            Map<String,String> errors = settings.submitForm(form);
            if (!errors.isEmpty()) {
                request.setAttribute("errors", errors);
                doGet(request, response);
                return;
            }
            settings.setUserRegistered(true);
            settingsDAO.put(settings);
        }
        response.sendRedirect(settings.getURL("home"));
    }
}
