/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2014 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.io.proxy;

import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

import javax.swing.JOptionPane;

import de.zbit.gui.GUITools;
import de.zbit.gui.prefs.PreferencesDialog;
import de.zbit.util.prefs.KeyProvider;
import de.zbit.util.prefs.Option;
import de.zbit.util.prefs.SBPreferences;

/**
 * @author Clemens Wrzodek
 * @date 2012-09-13
 * @version $Rev$
 */
public interface ProxySelection extends KeyProvider {
  
  /**
   * Several tools for the options defined in {@link ProxySelection}.
   * @author Clemens Wrzodek
   * @version $Rev$
   */
  public static class Tools {
    
    /**
     * Shows a dialog to change the {@link ProxySelection} settings.
     */
    public void showDialog() {
      
      // Create a listener that changes the proxy upon configuration change
      PreferenceChangeListener l = new PreferenceChangeListener() {
        
        @Override
        public void preferenceChange(PreferenceChangeEvent evt) {
          if (evt.getKey().equals(PROXY_HOST.toString()) ||
              evt.getKey().equals(PROXY_PASSWORD.toString()) ||
              evt.getKey().equals(PROXY_USERNAME.toString()) ||
              evt.getKey().equals(PROXY_PORT.toString())) {
            setProxyServerFromSBPreferences();
          }
        }
      };
      
      // Show the actual dialog
      PreferencesDialog.showPreferencesDialog(l, ProxySelection.class);
    }
    
    /**
     * This method will check the prefereces if a proxy server has been stored there.
     * If yes, the user will be asked if he wants to use the proxy or not.
     * Depending on this decision, the settings are either applied or erased.
     * <p>This method should be called upon each application start that want
     * to support proxy servers.
     */
    public void initializeProxyServer() {
      SBPreferences prefs = SBPreferences.getPreferencesFor(ProxySelection.class);
      String host = prefs.get(PROXY_HOST);
      if ((host != null) && (host.trim().length() > 0)) {
        int answer = GUITools.showQuestionMessage(null, String.format(
          "Previously stored settings for a proxy server have been detected ('%s').\nDo you want to use this proxy?", host),
          "Proxy server", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) {
          prefs.put(PROXY_HOST.toString(), "");
          prefs.put(PROXY_PASSWORD.toString(), "");
          prefs.put(PROXY_USERNAME.toString(), "");
          prefs.put(PROXY_PORT.toString(), "");
        }
        
        setProxyServerFromSBPreferences(prefs);
      }
    }
    
    /**
     * This will change the JVM proxy settings to the values, stored in previous
     * instances of this dialog.
     */
    public void setProxyServerFromSBPreferences() {
      SBPreferences prefs = SBPreferences.getPreferencesFor(ProxySelection.class);
      setProxyServerFromSBPreferences(prefs);
    }
    
    /**
     * This will change the JVM proxy settings to the values, stored in previous
     * instances of this dialog.
     * @param prefs
     */
    public void setProxyServerFromSBPreferences(SBPreferences prefs) {
      
      String host = prefs.get(PROXY_HOST);
      String port = prefs.get(PROXY_PORT);
      String user = prefs.get(PROXY_USERNAME);
      String pass = prefs.get(PROXY_PASSWORD);
      
      for (int i = 0; i < 4; i++) {
        String settingName = "";
        String settingValue = "";
        if (i == 0) {
          settingName = "Host";
          settingValue = host;
          if ((host == null) || (host.trim().length() < 1)) {
            System.setProperty("proxySet", "false");
            System.setProperty("http.proxySet", "false");
          } else {
            System.setProperty("proxySet", "true");
            System.setProperty("http.proxySet", "true");
          }
        } else if (i==1) {
          settingName = "Port";
          settingValue = port;
        } else if (i==2) {
          settingName = "User";
          settingValue = user;
          
        } else if (i==3) {
          settingName = "Password";
          settingValue = pass;
        }
        
        if (settingValue.trim().length()<1) {
          settingValue = null;
        }
        
        // Set the values for http, https, ftp and socks
        if (settingValue!=null) {
          System.setProperty("proxy" + settingName, settingValue);
          System.setProperty("http.proxy" + settingName, settingValue);
          System.setProperty("https.proxy" + settingName, settingValue);
          System.setProperty("ftp.proxy" + settingName, settingValue);
          System.setProperty("ftp.prox" + settingName, settingValue);
          System.setProperty("socks.proxy" + settingName, settingValue);
          System.setProperty("socksProxy" + settingName, settingValue);
        } else {
          System.clearProperty("proxy" + settingName);
          System.clearProperty("http.proxy" + settingName);
          System.clearProperty("https.proxy" + settingName);
          System.clearProperty("ftp.proxy" + settingName);
          System.clearProperty("ftp.prox" + settingName);
          System.clearProperty("socks.proxy" + settingName);
          System.clearProperty("socksProxy" + settingName);
        }
      }
    }
  }
  
  public static final Option<String> PROXY_HOST = new Option<String>("PROXY_HOST",
      String.class, "Please specify your proxy server (e.g., webcache.mydomain.com).", System.getProperty("http.proxyHost"));
  
  // Integer will generate a (in this case inappropriate) decimal format (8,080) instaead of 8080
  //  public static final Option<Integer> PROXY_PORT = new Option<Integer>("PROXY_PORT",
  //      Integer.class, "Please specify your proxy port (often 80 or 8080).", 8080);
  public static final Option<String> PROXY_PORT = new Option<String>("PROXY_PORT",
      String.class, "Please specify your proxy port (often 80 or 8080).", System.getProperty("http.proxyPort"));
  
  public static final Option<String> PROXY_USERNAME = new Option<String>("PROXY_USERNAME",
      String.class, "OPTIONAL: Please specify the username for your proxy server.", System.getProperty("http.proxyUser"));
  
  public static final Option<String> PROXY_PASSWORD = new Option<String>("PROXY_PASSWORD",
      String.class, "OPTIONAL: Please specify the password for your proxy server.", System.getProperty("http.proxyPassword"));
  
}
