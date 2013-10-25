/* dair/src/com/duckspot/dair/GoogleAuthCallbackServlet.java
 * 
 * History:
 *  4/ 6/13 PD
 *  4/ 9/13 PD add call to googleAuth.setRedirectURI()
 *  4/12/13 PD updated nextURL code to use OAUTH2 scope as nextURL
 *  4/12/13 PD better error handling
 */
package com.duckspot.diar;

import com.duckspot.diar.model.GoogleAuth;
import com.duckspot.diar.model.Settings;
import com.duckspot.diar.model.SettingsDAO;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Receives authorization to access Google account information.
 * 
 */
public class GoogleAuthCallbackServlet extends AbstractServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, 
                         HttpServletResponse response)
            throws IOException, ServletException
    {
        checkSecurity(request);

        Settings settings = (Settings)request.getAttribute("settings");
        SettingsDAO settingsDAO = 
                (SettingsDAO)request.getAttribute("settingsDAO");
        
        // errors forward to backURL
        // success redirects to nextURL
        String[] s = request.getParameter("state").split("::");
        String backURL = s[0];
        String nextURL = s[1];
        
        // error messages collected in this map
        Map<String,String> errors = new HashMap<String,String>();            
        
        // deal with error from OAUTH2        
        String error = request.getParameter("error");
        if (error != null) {
            
            // send errors attribute to backURL
            errors.put("top","Google Authorization Error: "+error);
            request.setAttribute("errors",errors);
            if (backURL != null) {
                request.getRequestDispatcher(backURL)
                       .forward(request, response);
            }
            throw new Error(errors.get("top")); 
        }
        
        // get authorization from Google
        GoogleAuth googleAuth = settings.getGoogleAuth();
        googleAuth.setRedirectURI(getServerURL(request) + "/oauth2callback");
        error = googleAuth.requestAuthTokens(request.getParameter("code"));
        if (error != null) {

            // send errors attribute to backURL
            errors.put("top","Google Authorization Token Request Error: "+error);
            request.setAttribute("errors",errors);
            if (backURL != null) {
                request.getRequestDispatcher(backURL)
                       .forward(request, response);
            }
            throw new Error(errors.get("top")); 
        }
        
        // write settings (they have a copy of approved GoogleAuthScope)
        settingsDAO.put(settings);
        response.sendRedirect(nextURL);
    }
}
