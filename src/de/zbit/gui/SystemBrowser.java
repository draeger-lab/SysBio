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
package de.zbit.gui;

import java.awt.Cursor;
import java.awt.Desktop;
import java.net.URI;

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
	
	/**
	 * If the mouse cursor lies over a text part which is a hyperlink, it becomes
	 * a hand cursor. Otherwise, it is the default cursor. By clicking in a
	 * hyperlink, this link is opened in a external web browser application. In
	 * case of Windows or Macintosh operating systems, the default browser is
	 * used. In case of Linux, a available web browser is searched an run.
	 */
	@Override
  public void hyperlinkUpdate(HyperlinkEvent event) {
		if (event.getEventType() == HyperlinkEvent.EventType.ENTERED) {
			((JEditorPane) event.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		} else if (event.getEventType() == HyperlinkEvent.EventType.EXITED) {
			((JEditorPane) event.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		} else if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			JEditorPane pane = (JEditorPane) event.getSource();
			if (event instanceof HTMLFrameHyperlinkEvent) {
				HTMLDocument doc = (HTMLDocument) pane.getDocument();
				doc.processHTMLFrameHyperlinkEvent((HTMLFrameHyperlinkEvent) event);
			} else {
				  openURL(event.getURL().toString());
			}
		}
	}
	

  /**
   * Opens the specified web page in the user's default browser
   * 
   * @param url A web address (URL) of a web page (ex: "http://www.google.com/")
   */
  public static void openURL(String url) {
    if (Desktop.isDesktopSupported()) {
      try {
        Desktop.getDesktop().browse(URI.create(url));
      } catch (Exception e) {
        GUITools.showErrorMessage(null, e);
      }
    }
  }
}
