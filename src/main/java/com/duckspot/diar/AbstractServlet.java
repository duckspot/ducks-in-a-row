/* dair/src/duckspot/google/appengine/AbstractServlet.java
 * 
 * History:
 * 04/02/13 PD - update and fix security version test
 */

package com.duckspot.diar;

import com.duckspot.jmte.TemplateServlet;
import javax.servlet.http.HttpServletRequest;

/**
 * DsServlet provides one common routine that many DiarServlets need.
 * 
 * @author Peter M Dobson
 */
public abstract class AbstractServlet extends TemplateServlet {

    /**
     * Check that SecurityFilter ran and set up request attributes 
     * appropriately.  Throw exception if securityFilter didn't run or is
     * wrong version.
     * 
     * @param request 
     */
    protected static void checkSecurity(HttpServletRequest request) {
        
        // security
        String diarSecurity = (String)request.getAttribute("diarSecurity");
        if (diarSecurity == null) {
            throw new Error("SecurityFilter didn't run");
        }
        float securityVersion = Float.parseFloat(diarSecurity);
        if (securityVersion < 2.1f) {
            throw new Error("SecurityFilter version 2.1 required");
        }
    }
    
    /**
     * Calculate the URL of the current requested server from the request.
     * 
     * @param  HTTP request
     * @return start of URL including protocol, server name, and perhaps 
     *         port number of URL.
     */
    protected static String getServerURL(HttpServletRequest request) {        
        String protocol = request.getProtocol().split("/")[0].toLowerCase();
        String serverName = request.getServerName();
        int port = request.getServerPort();
        int defaultPort = 80;
        if ("https".equals(protocol)) {
            defaultPort = 443;
        }        
        if (port == defaultPort) {
            return String.format("%s://%s", protocol, serverName);
        } else {
            return String.format("%s://%s:%d",protocol, serverName, port);
        }        
    }
}
