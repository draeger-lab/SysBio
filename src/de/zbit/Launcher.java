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

import java.awt.HeadlessException;
import java.awt.Window;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import de.zbit.gui.GUIOptions;
import de.zbit.gui.GUITools;
import de.zbit.gui.UpdateMessage;
import de.zbit.util.ResourceManager;
import de.zbit.util.logging.LogUtil;
import de.zbit.util.prefs.KeyProvider;
import de.zbit.util.prefs.SBPreferences;
import de.zbit.util.prefs.SBProperties;

/**
 * A basic implementation of an application launcher. If launching a program
 * using an implementation of this class, the {@link System} will contain the
 * following new {@link Properties} in order to make these accessible within
 * other objects, too:
 * <ul>
 * <li><code>app.name</code>: the name of the program</li>
 * <li><code>app.version</code>: the version number of the program</li>
 * </ul>
 * 
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 * @since 1.1
 * @date 20:49:11
 */
public abstract class Launcher {
	
	/**
	 * A {@link Logger} for this class.
	 */
	private static Logger logger = Logger.getLogger(Launcher.class.getName());
	/**
	 * A resource bundle containing label texts for this object.
	 */
  private static final ResourceBundle resources = ResourceManager.getBundle(GUITools.RESOURCE_LOCATION_FOR_LABELS);
	
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
		
	  logger.fine(resources.getString("SCANNING_CMD_ARGS"));
		final SBProperties props = SBPreferences.analyzeCommandLineArguments(
				getCommandLineOptions(), args);
		
		System.setProperty("app.name", getApplicationName());
		System.setProperty("app.version", getVersionNumber());
		
	  // Should we start the GUI?
		if ((args.length < 1) || props.getBooleanProperty(GUIOptions.GUI)) {
			SwingUtilities.invokeLater(new Runnable() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see java.lang.Runnable#run()
				 */
				public void run() {
					try {
						guiMode(props);
					} catch (HeadlessException exc) {
						if (props.getBooleanProperty(GUIOptions.GUI)) {
							logger.fine(resources.getString("COULD_NOT_INITIALIZE_GUI"));
						}
						launchCommandLineMode(props);
					}
					System.exit(0);
				}
			});
		} else {
			launchCommandLineMode(props);
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
	   * This method returns the default log level that is the minimal {@link Level}
	   * for log messages to be displayed to the user.
	   * 
	   * @return By default, this method returns {@link Level#FINE}. If something
	   *         different is desired, this method should be overridden in an
	   *         implementing class.
	   */
		public Level getLogLevel() {
		  return Level.FINE;
		}
	
	/**
 * @return An array of package names whose log messages should appear.
 */
public abstract String[] getLogPackages();
	
  /**
	 * Gives the location where the license of this program is documented. This
	 * could be, for instance, <a
	 * href="http://www.gnu.org/copyleft/gpl.html">http:
	 * //www.gnu.org/copyleft/gpl.html</a> in case of the GPL. This link to a
	 * license file is required for the command-line mode, because in this case a
	 * copyright notice is displayed upon startup. If your program doesn't support
	 * a command-line mode, you may return null here, but since this is a public
	 * method, this is not recommended.
	 * 
	 * @return A link to the license file of this program.
   * @throws MalformedURLException 
	 */
	public abstract URL getURLlicenseFile() throws MalformedURLException;
	
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
	 * Gives the year when your program was released under the version
	 * {@link #getVersionNumber()}.
	 * 
	 * @return the year of your program's release.
	 */
	public abstract short getYearOfProgramRelease();
	
	/**
	 * Information about the year when this project was first initialized.
	 * 
	 * @return The year in which the first code for this program was written.
	 */
	public abstract short getYearWhenProjectWasStarted();
	
	/**
	 * This method initializes and starts the graphical user interface if
	 * possible. You can override this method in order to change its behavior.
	 * 
	 * @param props
	 */
	public void guiMode(SBProperties props) {
		Window ui = initGUI();
		if (ui != null) {
			ui.setVisible(true);
			GUITools.hideSplashScreen();
			ui.toFront();
		} else {
			if (props.getBooleanProperty(GUIOptions.GUI)) {
				logger.fine(String.format(
				  resources.getString("NO_GUI_SUPPORTED"),
				  getApplicationName(),
				  getVersionNumber()));
			} else {
				logger.fine(String.format(
				  "INCOMPLETE_CMD_ARG_LIST",
				  getApplicationName(),
				  getVersionNumber()));
			}
		}
	}
	
	/**
	 * This method does nothing more than creating a new instance of a graphical
	 * user interface for the application and returns it. In case that this
	 * application does not support any GUI, this method may return null.
	 * 
	 * @return the graphical user interface for this application or null if no
	 *         such mode is supported.
	 */
	public abstract Window initGUI();

	/**
	 * Helper method that initializes the command line mode.
	 * 
	 * @param props
	 */
	protected void launchCommandLineMode(SBProperties props) {
		printCopyrightMessage();
		if (props.getBoolean(GUIOptions.CHECK_FOR_UPDATES)) {
			try {
				checkForUpdate();					
			} catch (MalformedURLException exc) {
				logger.log(Level.FINE, exc.getMessage(), exc);
			}
		}
		logger.info(String.format(resources.getString("LAUNCHING_CMD_MODE"),
			getApplicationName()));
		commandLineMode(props);
		GUITools.hideSplashScreen();
	}

	/**
	 * Displays a copyright notice using the logger.
	 */
	public void printCopyrightMessage() {
		logger.info(String.format(resources.getString("COPYRIGHT_MESSAGE"),
			getApplicationName(), getYearWhenProjectWasStarted(),
			getYearOfProgramRelease()));
		URL licenseFile = null;
		try {
			licenseFile = getURLlicenseFile();
		} catch (MalformedURLException exc) {
		}
		if (licenseFile != null) {
			logger.info(String.format(resources.getString("LINK_TO_LICENSE_FILE"),
				licenseFile.toString()));
		}
	}
	
}
