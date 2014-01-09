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

import java.awt.Color;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;

import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import de.zbit.util.ResourceManager;

/**
 * A Browser like Editor pane. This was part of SBMLsqueezer version 1.0 before.
 * 
 * @since 1.0
 * @version
 * @author Andreas Dr&auml;ger
 * @link 
 *       http://www.galileocomputing.de/openbook/javainsel6/javainsel_14_016.htm#
 *       Xxx1001419
 * @version $Rev$
 * @since 1.0
 */
public class JBrowserPane extends JEditorPane implements HyperlinkListener {

	/**
     * 
     */
	private static final long serialVersionUID = 1L;

	/**
	 * A list that logs all visited pages
	 */
	private LinkedList<URL> history;
	/**
	 * The current position within the list of visited web sites.
	 */
	private int currentPosition;

	/**
	 * @param url
	 * @throws MalformedURLException
	 */
	public JBrowserPane(String url) throws MalformedURLException {
		this(new URL(url));
	}

	/**
	 * 
	 * @param location
	 */
	public JBrowserPane(URL location) {
		super();
		setEditable(false);
		addHyperlinkListener(this);
		setBackground(Color.WHITE);
		try {
			setPage(location);
			history = new LinkedList<URL>();
			history.addLast(location);
			currentPosition = history.size() - 1;
		} catch (IOException exc) {
			JOptionPane.showMessageDialog(this, exc.getMessage(), exc
					.getClass().getName(), JOptionPane.ERROR_MESSAGE);
			exc.printStackTrace();
		}
	}

	/**
	 * Goes one page back in history.
	 * 
	 * @return Returns false if we reached the first visited page. True
	 *         otherwise.
	 */
	public boolean back() {
		if (currentPosition > 0) {
			visitPage(history.get(--currentPosition));
		}
		if (currentPosition == 0) {
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.event.HyperlinkListener#hyperlinkUpdate(javax.swing.event
	 * .HyperlinkEvent)
	 */
	@Override
  public void hyperlinkUpdate(HyperlinkEvent event) {
		HyperlinkEvent.EventType typ = event.getEventType();

		if (typ == HyperlinkEvent.EventType.ACTIVATED) {
			URL url = event.getURL();
			visitPage(url);
		}
	}

	/**
	 * Goes one page forward in history
	 * 
	 * @return Returns false if we reached the last visited page. True
	 *         otherwise.
	 */
	public boolean next() {
		if (currentPosition < history.size() - 1)
			visitPage(history.get(++currentPosition));
		if (currentPosition == history.size() - 1)
			return false;
		return true;
	}

	/**
	 * Sets the current page to be displayed to the given URL.
	 * 
	 * @param url
	 */
	private void visitPage(URL url) {
		try {
			setPage(url);
			if (!history.contains(url)) {
				history.add(++currentPosition, url);
			} else {
				currentPosition = history.indexOf(url);
			}
		} catch (IOException exc) {
			JOptionPane.showMessageDialog(this, ResourceManager.getBundle(
				"de.zbit.locales.Warnings").getString("CANNOT_FOLLOW_LINK")
					+ url.toExternalForm(), exc.getClass().getName(),
				JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Returns the number of pages visited so far.
	 * 
	 * @return the number of pages visited so far
	 */
	public int getNumPagesVisited() {
		return history.size();
	}

}
