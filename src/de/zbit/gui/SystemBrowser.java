/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2011 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.gui;

import java.awt.Cursor;
import java.util.Arrays;

import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;

/**
 * Provides a method to open the default browser of the window system under the
 * user's operating system when following a hyper link. This was part of SBMLsqueezer version 1.2.
 * 
 * @author Andreas Dr&auml;ger
 * @author Hannes Borch
 * @author Clemens Wrzodek
 * @since 1.0 (originates from SBMLsqueezer 1.2)
 * @version $Rev$
 */
public class SystemBrowser implements HyperlinkListener {
	
	public SystemBrowser() {
	}
	
	/**
	 * If the mouse cursor lies over a text part which is a hyperlink, it becomes
	 * a hand cursor. Otherwise, it is the default cursor. By clicking in a
	 * hyperlink, this link is opened in a external web browser application. In
	 * case of Windows or Macintosh operating systems, the default browser is
	 * used. In case of Linux, a available web browser is searched an run.
	 */
	public void hyperlinkUpdate(HyperlinkEvent event) {
		if (event.getEventType() == HyperlinkEvent.EventType.ENTERED) {
			((JEditorPane) event.getSource()).setCursor(Cursor
					.getPredefinedCursor(Cursor.HAND_CURSOR));
		} else if (event.getEventType() == HyperlinkEvent.EventType.EXITED) {
			((JEditorPane) event.getSource()).setCursor(Cursor
					.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		} else if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			JEditorPane pane = (JEditorPane) event.getSource();
			if (event instanceof HTMLFrameHyperlinkEvent) {
				HTMLDocument doc = (HTMLDocument) pane.getDocument();
				doc.processHTMLFrameHyperlinkEvent((HTMLFrameHyperlinkEvent) event);
			} else try {
				if (System.getProperty("os.name").contains("Windows"))
					Runtime.getRuntime().exec(
						"rundll32 url.dll,FileProtocolHandler " + event.getURL());
				else if (System.getProperty("os.name").contains("Mac OS"))
					Runtime.getRuntime().exec("open " + event.getURL());
				else {
				  openURL(event.getURL().toString());
				  /*
					String[] browsers = { "firefox", "opera", "konqueror", "epiphany",
							"mozilla", "netscape" };
					String browser = null;
					for (int i = 0; i < browsers.length && browser == null; i++)
						if (Runtime.getRuntime().exec("which " + browsers[i]).waitFor() == 0) {
							browser = browsers[i];
						}
					if (browser == null) {
						throw new Exception("Could not find web browser");
					} else {
						Runtime.getRuntime().exec(browser + " " + event.getURL());
					}*/
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	

  static final String[] browsers = { "google-chrome", "firefox", "opera",
     "epiphany", "konqueror", "conkeror", "midori", "kazehakase", "mozilla" };
  static final String errMsg = "Error attempting to launch web browser";

  /**
   * Opens the specified web page in the user's default browser
   * @param url A web address (URL) of a web page (ex: "http://www.google.com/")
   */
  public static void openURL(String url) {
    try {  //attempt to use Desktop library from JDK 1.6+
      Class<?> d = Class.forName("java.awt.Desktop");
      d.getDeclaredMethod("browse", new Class[] {java.net.URI.class}).invoke(
        d.getDeclaredMethod("getDesktop").invoke(null),
        new Object[] {java.net.URI.create(url)});
      //above code mimicks:  java.awt.Desktop.getDesktop().browse()
    }
    catch (Exception ignore) {  //library not available or failed
      String osName = System.getProperty("os.name");
      try {
        if (osName.startsWith("Mac OS")) {
          Class.forName("com.apple.eio.FileManager").getDeclaredMethod(
            "openURL", new Class[] {String.class}).invoke(null,
              new Object[] {url});
        }
        else if (osName.startsWith("Windows"))
          Runtime.getRuntime().exec(
            "rundll32 url.dll,FileProtocolHandler " + url);
        else { //assume Unix or Linux
          String browser = null;
          for (String b : browsers)
            if (browser == null && Runtime.getRuntime().exec(new String[]
                                                                        {"which", b}).getInputStream().read() != -1)
              Runtime.getRuntime().exec(new String[] {browser = b, url});
          if (browser == null)
            throw new Exception(Arrays.toString(browsers));
        }
      }
      catch (Exception e) {
        GUITools.showErrorMessage(null, e);
      }
    }
  }
  
}
