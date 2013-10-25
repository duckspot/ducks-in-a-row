/* dair/src/com/duckspot/dair/GoogleAuthServlet.java
 * 
 * History:
 *  3/06/13 PD
 *  3/13/13 PD - remove response.encodeURL & response.encodeRedirectURL calls 
 *               because we no longer use sessions.
 *  3/15/13 PD - separate Home, Security, Register and Settings Servlets.
 *  3/31/13 PD - restructure template to use common layout for similar pages
 *  4/11/13 PD - adjust to updates to Google authorizations
 *  4/12/13 PD - restructured to use GoogleAuthScope.getForm() and submitForm()
 */
package com.duckspot.diar;

import com.duckspot.diar.model.GoogleAuth;
import com.duckspot.diar.model.GoogleAuthData;
import com.duckspot.diar.model.GoogleAuthScope;
import com.duckspot.diar.model.Settings;
import com.duckspot.diar.model.SettingsDAO;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Adjust Google Authorization settings, and request authorization.
 */
public class GoogleAuthServlet extends AbstractServlet {
    
    static Map<String,String> empty = new HashMap<String,String>();
    
    @Override
    protected void doGet(HttpServletRequest request, 
                         HttpServletResponse response)
            throws IOException, ServletException
    {
        checkSecurity(request);
        
        Settings settings = (Settings)request.getAttribute("settings");
        
        if (!settings.isUserLoggedIn()) {
            
            // no user logged in - redirect to home, and to tell user to Login
            response.sendRedirect(settings.getURL("home"));            
            return;
        }                        
        
        // setup userBlock
        Map<String,Object> model = new HashMap<String,Object>();
        model.put("settings", settings);
        model.put("userBlock", template("layout/userBlock.html", model));
        
        // forms submits to myURL
        model.put("myURL", request.getRequestURI());
        
        // grab form resubmission after errors info 
        // (inserted in request by doPost before calling doGet)
        Map<String,String> form = 
                (Map<String,String>)request.getAttribute("form");
        Map<String,String> errors = 
                (Map<String,String>)request.getAttribute("errors");
System.out.println("GoogleAuthServlet: 64: errors: "+errors);
if (errors != null) System.out.println("GoogleAuthServlet: 64: errors: "+errors.get("top"));
        // default form values
        if (form == null) {
            
            // GoogleAuthScope form
            form = settings.getGoogleAuthScope().getForm();
            
            // with nextURL hidden field
            String nextURL = this.getInitParameter("nextURL");
            System.out.println("GoogleAuthServlet: 73: nextURL: "+nextURL);
            if (nextURL == null) {
                nextURL = request.getHeader("Referer");
            }
            System.out.println("GoogleAuthServlet: 77: nextURL: "+nextURL);
            if (nextURL == null) {
                nextURL = settings.getURL("ready");                
            }
            System.out.println("GoogleAuthServlet: 81: nextURL: "+nextURL);
            
            form.put("nextURL", nextURL);            
        }
        model.put("form", form);
            
        // default errors
        if (errors == null) {
            errors = empty;
        }
        model.put("errors", errors);
        
        // allow content to be specified by init-param
        String content = this.getInitParameter("content");
        if (content == null) {
            content = "googleAuth.html";
        }
        model.put("content",template(content, model));
        
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.print(template("layout/twoColumn.html", model));
    }
    
    @Override
    protected void doPost(HttpServletRequest request, 
                         HttpServletResponse response)
            throws IOException, ServletException
    {
        checkSecurity(request);
        
        Settings settings = (Settings)request.getAttribute("settings");
        SettingsDAO settingsDAO = 
                (SettingsDAO)request.getAttribute("settingsDAO");
        
        // get form data
        Map<String,String> form = new HashMap<String,String>();
        for(String name: request.getParameter("fields").split(",")) {
            form.put(name, request.getParameter(name));
        }
        System.out.println("GoogleAuthServlet: 121: nextURL:"+form.get("nextURL"));
        
        // grab authorization objects
        GoogleAuth googleAuth = settings.getGoogleAuth();
        GoogleAuthData authData = googleAuth.getAuth();
        GoogleAuthScope scope = authData.getGoogleAuthScope();
        
        // save or authorize actions
        if ("save".equalsIgnoreCase(form.get("action")) || 
            "authorize".equalsIgnoreCase(form.get("action"))) {
            
            // submit changes to scope
            Map<String,String> errors = scope.submitForm(form);
            
            if (!errors.isEmpty()) {
                
                // send errors & form submission to doGet via request attr
                request.setAttribute("form", form);
                request.setAttribute("errors", errors);
                doGet(request, response);
                return;
            }
            
            // write scope to Datastore, with no authorization
            authData.setAccessToken(null);
            authData.setRefreshToken(null);
            googleAuth.put(authData);
        }
        
        // use 
        googleAuth.setRedirectURI(getServerURL(request) + "/oauth2callback");
        
        // pass thisURL::nextURL to GoogleAuthCallBack via state API parameter
        String thisURL = request.getRequestURI();
        String nextURL = form.get("nextURL");
        String state = thisURL + "::" + nextURL;
        
        response.sendRedirect(googleAuth.getAuthURL(state));
    }
}
