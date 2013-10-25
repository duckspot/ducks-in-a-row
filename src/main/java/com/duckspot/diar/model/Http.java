/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.duckspot.diar.model;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 *
 * @author Us
 */
public class Http {
    
    /**
     * Perform an HTTP GET operation.
     * 
     * @param url
     * @param urlParameters
     * @return 
     */
    
    public static String get(URL url) {
        HttpURLConnection connection = null;
        try {
            //Create connection
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");            
            connection.setUseCaches(false);
            connection.setDoInput(true);            

            //Get Response	
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();

        } catch (Exception ex) {

            throw new Error("unexpected exception", ex);

        } finally {

            if (connection != null) {
                connection.disconnect();
            }
        }
    }
    
    public static String post(URL url, String urlParameters) {
        HttpURLConnection connection = null;
        try {
            //Create connection
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");

            connection.setRequestProperty("Content-Length", ""
                    + Integer.toString(urlParameters.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            //Send request
            DataOutputStream wr = new DataOutputStream(
                    connection.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();

            //Get Response	
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();

        } catch (Exception ex) {

            throw new Error("unexpected exception", ex);

        } finally {

            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
