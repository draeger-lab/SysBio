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
import java.io.Serializable;
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
 * Further details of the program configuration can be obtained from this
 * {@link Launcher} by calling {@link #getAppConf()}.
 * 
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 * @since 1.1
 * @date 20:49:11
 */
public abstract class Launcher implements Runnable, Serializable {
	
	/**
	 * A {@link Logger} for this class.
	 */
  private static final transient Logger logger = Logger
      .getLogger(Launcher.class.getName());

  /**
	 * A resource bundle containing label texts for this object.
	 */
  private static final transient ResourceBundle resources = ResourceManager
      .getBundle(GUITools.RESOURCE_LOCATION_FOR_LABELS);
	
	/**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = -612780998835450100L;
		
	/**
	 * Stores given command-line options as key-value pairs.
	 */
	private SBProperties props;
  
  /**
   * Switch to decide if {@link System#exit(int)} should be called when the
   * execution of this {@link Launcher} is finished. This option should be set
   * to <code>true</code> when using a graphical user interface, which will be
   * launched in a separate {@link Thread}.
   */
	private boolean terminateJVMwhenDone;
  
  /**
   * Creates a new {@link Launcher} with an empty list of command-line options
   * but that will terminate the JVM after its execution. This constructor also
   * initializes the logging functionality.
   */
  public Launcher() {
    super();
    printCopyrightMessage();
    this.terminateJVMwhenDone = true;
    this.props = new SBProperties();
    LogUtil.initializeLogging(getLogLevel(), getLogPackages());
  }
	
  /**
   * Copy constructor.
   * 
   * @param launcher
   */
	public Launcher(Launcher launcher) {
    this();
    this.props = launcher.getCommandLineArgs().clone();
    this.terminateJVMwhenDone = launcher.isTerminateJVMwhenDone();
  }
	
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
	  this();
	  parseCmdArgs(args);
		run();
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
			UpdateMessage update = new UpdateMessage(false, getAppName(),
				url, getVersionNumber(), true);
			update.execute();
		}
	}
  
  /**
	 * This method is called in case that no graphical user interface is to be
	 * used. The given properties contain all the key-value pairs that have been
	 * defined on the command line when starting this program.
	 * 
	 * @param appConf
	 */
	public abstract void commandLineMode(AppConf appConf);

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (obj.getClass().equals(getClass())) {
      Launcher l = (Launcher) obj;
      return l.getAppConf().equals(getAppConf())
          && (l.isTerminateJVMwhenDone() == isTerminateJVMwhenDone());
    }
    return false;
  }

  /**
	 * This method tells a caller the name of this program.
	 * 
	 * @return The name of this program.
	 */
	public abstract String getAppName();

  /**
   * 
   * @return
   */
  public SBProperties getCommandLineArgs() {
    return props;
  }

	/**
	 * 
	 * @return a {@link List} of {@link KeyProvider} {@link Class} objects that
	 *         define collections of possible command-line options.
	 */
	public abstract List<Class<? extends KeyProvider>> getCmdLineOptions();
	
	/**
   * 
   * @return
   */
  public abstract List<Class<? extends KeyProvider>> getInteractiveOptions();
	
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
   * 
   * @return
   */
  public AppConf getAppConf() {
    return new AppConf(getAppName(), getVersionNumber(),
      getYearOfProgramRelease(), getCmdLineOptions(), getCommandLineArgs(),
      getInteractiveOptions(), getURLlicenseFile(), getURLOnlineUpdate());
  }
	
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
	 */
	public abstract URL getURLlicenseFile();
	
	/**
	 * 
	 * @return The {@link URL} where the information about online updates can be
	 *         found. May be null.
	 */
	public abstract URL getURLOnlineUpdate();
	
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
	 * @param appConf
	 */
	public void guiMode(AppConf appConf) {
		Window ui = initGUI();
		if (ui != null) {
			ui.setVisible(true);
			GUITools.hideSplashScreen();
			ui.toFront();
		} else {
			if (appConf.getCmdArgs().getBooleanProperty(GUIOptions.GUI)) {
				logger.fine(String.format(
				  resources.getString("NO_GUI_SUPPORTED"),
				  getAppName(),
				  getVersionNumber()));
			} else {
				logger.fine(String.format(
				  "INCOMPLETE_CMD_ARG_LIST",
				  getAppName(),
				  getVersionNumber()));
			}
		}
	}
	
	/* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 859;
    int hashCode = getClass().getName().hashCode();
    hashCode += prime * getAppConf().hashCode();
    hashCode += prime * Boolean.valueOf(terminateJVMwhenDone).hashCode();
    return hashCode;
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
   * @return the terminateJVMwhenDone
   */
  public boolean isTerminateJVMwhenDone() {
    return terminateJVMwhenDone;
  }

  /**
	 * Helper method that initializes the command line mode.
	 * 
	 * @param appConf
	 */
	protected void launchCommandLineMode(AppConf appConf) {
		if (appConf.getCmdArgs().getBoolean(GUIOptions.CHECK_FOR_UPDATES)) {
			try {
				checkForUpdate();					
			} catch (MalformedURLException exc) {
				logger.log(Level.FINE, exc.getMessage(), exc);
			}
		}
		logger.info(String.format(resources.getString("LAUNCHING_CMD_MODE"),
			getAppName()));
		commandLineMode(appConf);
		GUITools.hideSplashScreen();
		if (terminateJVMwhenDone) {
      System.exit(0);
    }
	}

  /**
   * 
   * @param args
   * @return
   */
  public SBProperties parseCmdArgs(String[] args) {
    logger.finer(resources.getString("SCANNING_CMD_ARGS"));
    props = SBPreferences.analyzeCommandLineArguments(getCmdLineOptions(),
      args);
    return props;
  }

  /**
	 * Displays a copyright notice using the logger.
	 */
  public void printCopyrightMessage() {
    StringBuilder message = new StringBuilder();
    message.append(String.format(resources.getString("COPYRIGHT_MESSAGE"),
      getAppName(), getYearWhenProjectWasStarted(),
      getYearOfProgramRelease()));
    URL licenseFile = null;
    licenseFile = getURLlicenseFile();
    if (licenseFile != null) {
      message.append(String.format(resources.getString("LINK_TO_LICENSE_FILE"),
        licenseFile.toString()));
    }
    System.out.println(message.toString());
  }

  /*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
  public void run() {    
    System.setProperty("app.name", getAppName());
    System.setProperty("app.version", getVersionNumber());
        
    // Should we start the GUI?
    if ((props.size() < 1) || props.getBooleanProperty(GUIOptions.GUI)) {
      SwingUtilities.invokeLater(new Runnable() {
        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Runnable#run()
         */
        public void run() {
          AppConf appCnf = getAppConf();
          try {
            guiMode(appCnf);
          } catch (HeadlessException exc) {
            if (props.getBooleanProperty(GUIOptions.GUI)) {
              logger.fine(resources.getString("COULD_NOT_INITIALIZE_GUI"));
            }
            launchCommandLineMode(appCnf);
          }
          if (terminateJVMwhenDone) {
            System.exit(0);
          }
        }
      });
    } else {
      launchCommandLineMode(getAppConf());
    }
  }

  /**
   * @param terminateJVMwhenDone the terminateJVMwhenDone to set
   */
  public void setTerminateJVMwhenDone(boolean terminateJVMwhenDone) {
    this.terminateJVMwhenDone = terminateJVMwhenDone;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return getAppName();
  }
	
}
