/* dair/src/com/duckspot/dair/HomeServlet.java
 * 
 * Determines if user is logged in to Google, and if he has registered for 
 * ducks in a row, and if so forwards to Servlet bound to READY URL.
 * 
 * Introduces application, and invites USER to register.
 * 
 * History:
 * 03/06/13 PD
 * 03/13/13 PD - remove response.encodeURL & response.encodeRedirectURL calls 
 *               because we no longer use sessions.
 * 03/15/13 PD - separate Home, Security, Register and Settings Servlets.
 * 03/31/13 PD - restructure template to use common layout for similar pages
 */
package com.duckspot.diar;

import com.duckspot.diar.model.Settings;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * HomeServlet is responsible for starting users on a path through 
 * registration, and/or login.
 * 
 * If the user is already logged in, there is a server-side redirect to the
 * "ready" URL (see Settings.getURL()), otherwise a layout with welcome 
 * content is displayed.
 * 
 */
public class HomeServlet extends AbstractServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, 
                         HttpServletResponse response)
            throws IOException, ServletException
    {
        checkSecurity(request);
        
        Settings settings = (Settings)request.getAttribute("settings");
        
        // find out if we're logged in and registered
        if (settings.isUserRegistered()) {
            
            // we get here if user is logged in, and registered            
            
            // forward to READY URL
            getServletContext().getRequestDispatcher(settings.getURL("ready"))
                               .forward(request, response);
        } else {
        
            // prepare and output page
            Map<String,Object> model = new HashMap<String,Object>();
            model.put("settings", settings);
            model.put("pageTitle", "ducks in a row");
            model.put("userBlock", template("layout/userBlock.html", model));
            model.put("content", template("welcome.html", model));
            response.setContentType("text/html");
            PrintWriter out = response.getWriter();
            out.print(template("layout/twoColumn.html", model));
        }
    }
    
    /**
     * Post does this so it also forwards to READY.
     */
    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws IOException, ServletException
    {
        doGet(request, response);
    }
}
