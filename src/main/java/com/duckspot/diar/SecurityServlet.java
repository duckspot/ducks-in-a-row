/* dair/src/com/duckspot/dair/SecurityServlet.java
 * 
 * History:
 * 03/06/13 PD
 * 03/13/13 PD - remove response.encodeURL & response.encodeRedirectURL calls 
 *               because we no longer use sessions.
 * 03/15/13 PD - separate Home, Security, Register and Settings Servlets.
 */
package com.duckspot.diar;

import com.duckspot.jmte.TemplateServlet;
import com.duckspot.diar.model.Settings;
import com.google.appengine.api.datastore.*;
import com.google.appengine.api.users.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * Display message, suggesting login again, or telling user "access denied".
 */
public class SecurityServlet extends AbstractServlet {
    
    UserService userService;
    
    @Override
    public void init() {
        super.init();
        userService = UserServiceFactory.getUserService();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, 
                         HttpServletResponse response)
            throws IOException, ServletException
    {
        checkSecurity(request);
        
        Settings settings = (Settings)request.getAttribute("settings");
        
        Map<String,Object> model = new HashMap<String,Object>();
        model.put("settings", settings);
        model.put("userBlock", template("layout/userBlock.html", model));
        
        // Security filter forwards to /security?continue=failed-url
        String nextURL = request.getParameter("continue");
        if (nextURL == null || nextURL.isEmpty()) {
            nextURL = settings.getURL("home");;
        }
        model.put("securityLoginURL", userService.createLoginURL(nextURL));
        
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.print(template("security.html",model));
    }    
}
