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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.SystemColor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

import de.zbit.util.AbstractProgressBar;
import de.zbit.util.ProgressListener;
import de.zbit.util.ResourceManager;
import de.zbit.util.logging.LogUtil;

/**
 * A pseudo-{@link StatusBar} implementation for Swing. This can be used
 * to display a status information (e.g., attached to a logger with
 * {@link #displayLogMessagesInStatusBar()}) and a
 * {@link AbstractProgressBar} at the bottom of a {@link java.awt.Window}.
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class StatusBar extends JPanel implements ProgressListener {
	
	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = -5115329140165258105L;
	/**
	 * A {@link Logger} for this class.
	 */
	public static final transient Logger log = Logger.getLogger(StatusBar.class.getName());
	
	/**
	 * Localization support.
	 */
	private static final transient ResourceBundle bundle = ResourceManager.getBundle("de.zbit.locales.Labels");

	/**
	 * A Label that is used to display status information
	 */
	final private JLabel statusLabel;

	/**
	 * The parent panel in west border that contains {@link #statusLabel} in left slot.
	 */
	private JPanel leftPanel;
	
	/**
	 * An additional progressbar that can be visualized in the status bar.
	 */
	private ProgressBarSwing progressBar = null;

	/**
	 * This is the default status message text.
	 */
	public static String defaultText = bundle.getString("READY");

	/**
	 * A log handler that is used to catch and display log messages.
	 */
	private LimitLogHandler handler;
	
	private static boolean limitLogLength = false;
	private static int maxLogSize = 0;

	/**
	 * 
	 */
	public StatusBar() {
		this(null, defaultText);
	}

	/**
	 * 
	 * @param initialMessage
	 */
	public StatusBar(String initialMessage) {
		this(null, initialMessage);
	}

	/**
	 * Create a new status bar with an icon and initial message.
	 * @param icon
	 * @param initialMessage
	 */
	public StatusBar(Icon icon, String initialMessage) {
		super();
		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(10, 23));

		// Create the status label
		if (initialMessage == null) {
			initialMessage = defaultText;
		}
		leftPanel = new JPanel(new BorderLayout());
		((BorderLayout)leftPanel.getLayout()).setHgap(10);
		statusLabel = new JLabel(initialMessage,icon, SwingConstants.LEFT);
		
		handler = new LimitLogHandler(getFontMetrics(getFont()));
		
		leftPanel.add(statusLabel, BorderLayout.WEST);
		leftPanel.setOpaque(false);

		add(leftPanel, BorderLayout.WEST);
		setBackground(SystemColor.control);
	}


	/**
	 * Display {@link Level#INFO} log messages, issued in
	 * {@value LogUtil#basePackage} (currently: "de.zbit") and
	 * {@link LogUtil#getInitializedPackages()} in the
	 * {@link #statusLabel}.
	 */
	public void displayLogMessagesInStatusBar() {
		// Display with info message in all de.zbit packages.
		displayLogMessagesInStatusBar(Level.INFO, LogUtil.getInitializedPackages());
	}
	/**
	 * Display log messages in the status bar.
	 * @param level minimum level for a message to get displayed
	 * @param packages to log IN ADDITION TO {@link LogUtil#basePackage}!
	 */
	public void displayLogMessagesInStatusBar(Level level, String... packages) {
		LogUtil.removeHandler(handler, packages);
		handler.setLevel(level);
		handler.setFormatter(new SimpleFormatter());

		// Add the log handler
		LogUtil.addHandler(handler, packages);
	}

	/**
	 * This Method changes the layout of the frame to a BorderLayout,
	 * creates and returns a {@link StatusBar} with the icon
	 * of the parent frame and displays log messages in the status bar.
	 * @param frame
	 * @return
	 */
	public static StatusBar addStatusBar(final JFrame frame) {
		// Ensure a border layout
		Container contentPane = frame.getContentPane();
		if (!contentPane.getLayout().getClass().equals(BorderLayout.class)) {
			contentPane.setLayout(new BorderLayout());
		}

		// Get current icon
		Icon icon = frame.getIconImage() != null ? new ImageIcon(frame.getIconImage()) : null;

		// Create the status bar
		final StatusBar statusBar = new StatusBar(icon, null);

		// Change status bar icon with frame
		frame.addPropertyChangeListener("iconImage", new PropertyChangeListener() {
			/* (non-Javadoc)
			 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
			 */
			public void propertyChange(PropertyChangeEvent evt) {
				Icon icon = null;
				icon = frame.getIconImage()!=null?new ImageIcon(frame.getIconImage()):null;
				statusBar.setIcon(icon);
			}
		});

		// Add status bar to content pane
		contentPane.add(statusBar, BorderLayout.SOUTH);

		// Capture and display logging messages.
		statusBar.displayLogMessagesInStatusBar();

		return statusBar;
	}
	
	/**
	 * A simple wrapper for {@link #showProgress()};
	 * @return
	 * @see #showProgress();
	 */
	public AbstractProgressBar getProgressBar() {
	  return showProgress();
	}

	/**
	 * Use the statusbar to display the progress of an operation.
	 * @return
	 */
	public synchronized AbstractProgressBar showProgress() {
		initializeProgressBar();
		return this.progressBar;
	}

	/**
	 * Hide the {@link #progressBar} if it is visible.
	 */
	public synchronized void hideProgress() {
		if (progressBar != null) {
			progressBar.getProgressBar().setVisible(false);
		}
		return;
	}

	/**
	 * Attach the statusBar to an existing progressbar. This will display
	 * the progress in the status bar and hide it automatically, as soon
	 * as the operation is done (100%).
	 * @param progress
	 */
	public synchronized void showProgress(AbstractProgressBar progress) {
		initializeProgressBar();
		synchronized (progressBar) {
			if (progress!=null) {
				progress.addProgressListener(this);
			}
		}
	}

	/**
	 * Ensures that {@link #progressBar} is valid and the contained
	 * visualization is visible.
	 */
	private synchronized void initializeProgressBar() {
		JProgressBar bar;
		if (progressBar==null) {
			// Create a smaller panel for the statusBar
			Dimension panelSize = new Dimension(100, 15);
			JPanel panel = new JPanel();
			panel.setOpaque(false);
			panel.setPreferredSize(panelSize);
			bar = new JProgressBar();
			bar.setPreferredSize(new Dimension(panelSize.width, panelSize.height));
			panel.add(bar);

			progressBar = new ProgressBarSwing(bar);
			leftPanel.add(panel, BorderLayout.CENTER);
			bar.setStringPainted(false);
			
		} else {
			bar = progressBar.getProgressBar();
		}
		bar.setVisible(true);
	}

	/**
	 * set a limit to the length of log messages
	 * @param size	the maximum length of log messages
	 */
	public void limitLogMessageLength(int width){
		maxLogSize = width;
		limitLogLength = true;
		
		Dimension d = new Dimension(width, statusLabel.getHeight());
		statusLabel.setPreferredSize(d);
		statusLabel.setMaximumSize(d);
	}
	
	public void unsetLogMessageLimit(){
		limitLogLength = false;
		Dimension d = new Dimension(this.getWidth(), statusLabel.getHeight());
		statusLabel.setPreferredSize(d);
		statusLabel.setMaximumSize(d);
	}
	

	/**
	 * Set a nice icon for this status bar.
	 * @param image
	 */
	public void setIcon(Icon image) {
		statusLabel.setIcon(image);
	}

	/* (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		/*
		 * Paint bars that make this component look like a StatusBar.
		 */
		int y = 0;
		g.setColor(new Color(156, 154, 140));
		g.drawLine(0, y, getWidth(), y);
		y++;
		g.setColor(new Color(196, 194, 183));
		g.drawLine(0, y, getWidth(), y);
		y++;
		g.setColor(new Color(218, 215, 201));
		g.drawLine(0, y, getWidth(), y);
		y++;
		g.setColor(new Color(233, 231, 217));
		g.drawLine(0, y, getWidth(), y);

		y = getHeight() - 3;
		g.setColor(new Color(233, 232, 218));
		g.drawLine(0, y, getWidth(), y);
		y++;
		g.setColor(new Color(233, 231, 216));
		g.drawLine(0, y, getWidth(), y);
		y = getHeight() - 1;
		g.setColor(new Color(221, 221, 220));
		g.drawLine(0, y, getWidth(), y);

	}

	/* (non-Javadoc)
	 * @see de.zbit.util.ProgressListener#percentageChanged(int, double, java.lang.String)
	 */
	public void percentageChanged(int percent, double miliSecondsRemaining, String additionalText) {
		if ((progressBar == null) || !progressBar.getProgressBar().isVisible()) {
			initializeProgressBar();
		}
		synchronized (progressBar) {
			// Update progressbar
			progressBar.drawProgressBar(percent, miliSecondsRemaining, additionalText);

			// Hide when done.
			if (percent == 100) {
				progressBar.getProgressBar().setVisible(false);
			}
		}
	}

	/**
	 * Reset to default state.
	 */
	public void reset() {
		unsetLogMessageLimit();
		statusLabel.setText(defaultText);
		if (progressBar != null) {
			progressBar.getProgressBar().setVisible(false);
		}
	}

	private class LimitLogHandler extends Handler{
		
		private FontMetrics fontMetrics;

		public LimitLogHandler(FontMetrics fontMetrics){
			this.fontMetrics = fontMetrics;
		}
		
		/* (non-Javadoc)
		 * @see java.util.logging.Handler#close()
		 */
		public void close() throws SecurityException {
			statusLabel.setText(StatusBar.defaultText);
		}
		/* (non-Javadoc)
		 * @see java.util.logging.Handler#flush()
		 */
		public void flush() {
			statusLabel.validate();
			statusLabel.repaint();
		}

		/* (non-Javadoc)
		 * @see java.util.logging.Handler#publish(java.util.logging.LogRecord)
		 */
		public void publish(LogRecord record) {
			// Initial space (a little distance to the border)
			StringBuffer message = new StringBuffer(" ");

			// Append Warning message and make red for warnings
			if (record.getLevel().intValue()>=Level.WARNING.intValue()) {
				message.append(record.getLevel().getLocalizedName());
				message.append(": ");
				statusLabel.setForeground(Color.RED);
			} else if (!statusLabel.getForeground().equals(Color.BLACK)) {
				statusLabel.setForeground(Color.BLACK);
			}

			// Append localized message
			if (getFormatter()!=null) {
				// This will e.g., localize the message!
				message.append(getFormatter().formatMessage(record));
			} else {
				message.append(record.getMessage());
			}

			String m = message.toString();

			if (!m.equals(statusLabel.getText())) {
				if(limitLogLength){
					String limitedMessage = "";
					int maxIndex = 0;
					for(int i = 0; i < m.length(); i++){
						limitedMessage += m.charAt(i);
						if(fontMetrics.stringWidth(limitedMessage) < maxLogSize){
							// Set the status label text.
							maxIndex = i+1;
						}
					}
					m = m.substring(0, maxIndex);
				}
				// Set the status label text.
				statusLabel.setText(m);  	  

			} 
		}

	}

}
