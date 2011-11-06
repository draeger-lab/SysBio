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
package de.zbit;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import de.zbit.gui.BaseFrame;
import de.zbit.gui.GUIOptions;
import de.zbit.gui.GUITools;
import de.zbit.gui.UpdateMessage;
import de.zbit.util.logging.LogUtil;
import de.zbit.util.prefs.KeyProvider;
import de.zbit.util.prefs.SBPreferences;
import de.zbit.util.prefs.SBProperties;

/**
 * A basic implementation of an application launcher.
 * 
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 * @date 20:49:11
 */
public abstract class Launcher implements Serializable {
	
	/**
	 * A {@link Logger} for this class.
	 */
	public static Logger logger = Logger.getLogger(Launcher.class.getName());

	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = -8887992352902937300L;
	
	/**
	 * Initializes this program including logging, log levels and packages,
	 * parsing of command-line arguments, initializing of a graphical user
	 * interface or a command-line mode (as defined by the command-line arguments
	 * or the properties of this program).
	 * 
	 * @param args
	 *        the command-line arguments.
	 */
	public Launcher(String args[]) {
		LogUtil.initializeLogging(getLogLevel(), getLogPackages());

    // Locale.setDefault(Locale.US);
	  GUIOptions.GUI.setDefaultValue(Boolean.FALSE);
	  
	  logger.info("Scanning command line arguments...");
		SBProperties props = SBPreferences.analyzeCommandLineArguments(
				getCommandLineOptions(), args);
		
	  // Should we start the GUI?
		if ((args.length < 1) || props.getBooleanProperty(GUIOptions.GUI)) {
			SwingUtilities.invokeLater(new Runnable() {
				/*
				 * (non-Javadoc)
				 * @see java.lang.Runnable#run()
				 */
				public void run() {
					logger.info(String.format(
						"Initializing graphical user interface for %s.",
						getApplicationName()));
					BaseFrame ui = initGUI();
					if (ui != null) {
						ui.setVisible(true);
						GUITools.hideSplashScreen();
						ui.toFront();
					} else {
						logger.info("No graphical user interface supported.");
					}
				}
			});
		} else {
			showAboutMessage();
			if (props.getBoolean(GUIOptions.CHECK_FOR_UPDATES)) {
				try {
					checkForUpdate();					
				} catch (MalformedURLException exc) {
					logger.log(Level.FINE, exc.getMessage(), exc);
				}
			}
			logger.info(String.format("Launching command line mode of %s.",
				getApplicationName()));
			commandLineMode(props);
			GUITools.hideSplashScreen();
		}
	}
	
	/**
	 * This method can be called when starting the command-line mode of this
	 * program. It checks for updates and may display a user notification (on the
	 * console) in case that an update is available.
	 * 
	 * @throws MalformedURLException
	 */
	public void checkForUpdate() throws MalformedURLException {
		URL url = getURLOnlineUpdate();
		if (url != null) {
			UpdateMessage update = new UpdateMessage(false, getApplicationName(),
				url, getVersionNumber(), true);
			update.execute();
		}
	}
	
	/**
	 * This method is called in case that no graphical user interface is to be
	 * used. The given properties contain all the key-value pairs that have been
	 * defined on the command line when starting this program.
	 * 
	 * @param props
	 */
	public abstract void commandLineMode(SBProperties props);
	
	/**
	 * This method tells a caller the name of this program.
	 * 
	 * @return The name of this program.
	 */
	public abstract String getApplicationName();
	
	/**
	 * 
	 * @return a {@link List} of {@link KeyProvider} {@link Class} objects that
	 *         define collections of possible command-line options.
	 */
	public abstract List<Class<? extends KeyProvider>> getCommandLineOptions();
	
	/**
	 * @return The default log level that is the minimal {@link Level} for log
	 *         messages to be displayed to the user.
	 */
	public abstract Level getLogLevel();
	
	/**
	 * @return An array of package names whose log messages should appear.
	 */
	public abstract String[] getLogPackages();
	
	/**
	 * 
	 * @return The {@link URL} where the information about online updates can be
	 *         found. May be null.
	 * @throws MalformedURLException
	 */
	public abstract URL getURLOnlineUpdate() throws MalformedURLException;
	
	/**
	 * 
	 * @return The (dotted) version number of this program, e.g., "0.9.3" (without
	 *         quotes). Must not be null.
	 */
	public abstract String getVersionNumber();
	
	/**
	 * This method does nothing more than creating a new instance of a graphical
	 * user interface for the application and returns it. In case that this
	 * application does not support any GUI, this method may return null.
	 * 
	 * @return the graphical user interface for this application or null if no
	 *         such mode is supported.
	 */
	public abstract BaseFrame initGUI();
	
	/**
	 * Displays a copyright notice using the logger.
	 */
	public void showAboutMessage() {
		logger.info(String.format(
			"%s Copyright \u00A9 %d the University of Tuebingen,",
			getApplicationName(), Calendar.getInstance().get(Calendar.YEAR)));
		logger.info("Center for Bioinformatics Tuebingen (ZBIT).");
	  logger.info("This program comes with ABSOLUTELY NO WARRANTY.");
	  logger.info("This is free software, and you are welcome");
	  logger.info("to redistribute it under certain conditions;");
	  logger.info("see http://www.gnu.org/copyleft/gpl.html for details.");
	}
	
}
