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

import java.beans.EventHandler;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.zbit.gui.GUIOptions;
import de.zbit.gui.GUITools;
import de.zbit.gui.UpdateMessage;
import de.zbit.util.ResourceManager;
import de.zbit.util.logging.LogUtil;
import de.zbit.util.prefs.KeyProvider;
import de.zbit.util.prefs.Option;
import de.zbit.util.prefs.SBPreferences;
import de.zbit.util.prefs.SBProperties;

/**
 * A basic implementation of an application launcher. If launching a program
 * using an implementation of this class, the {@link System} will contain the
 * following new {@link Properties} in order to make these accessible within
 * other objects, too:
 * <table>
 * <tr>
 * <th>Key</th>
 * <th>Content</th>
 * </tr>
 * <tr>
 * <td><code>app.name</code></td>
 * <td>the name of the program</td>
 * </tr>
 * <tr>
 * <td><code>app.version</code></td>
 * <td>the version number of the program</td>
 * </tr>
 * </table>
 * If the user operates on Mac OS X, all necessary {@link System}
 * {@link Properties} specific to this environment will also be set. This might
 * make using <code>-Xdock:name="Some title" -Xdock:icon=path/to/icon</code> on
 * command line unnecessary.
 * <p>
 * Further details of the program configuration can be
 * obtained from this {@link Launcher} by calling {@link #getAppConf()}.
 * </p><p>
 * Please note that this class does not import any classes that are related to
 * GUI elements (in some standard Java packages, such as SWING or AWT). When
 * making a reference to such a class, the complete class name including its
 * package declaration is used in order to prevent that on systems that do not
 * have these classes, the Launcher might not be usable.
 * </p>
 * 
 * @author Andreas Dr&auml;ger
 * @author Clemens Wrzodek
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
			.getBundle("de.zbit.locales.Labels");

	/**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = -612780998835450100L;

	/**
   * Grants access to the {@link ResourceBundle} used by this {@link Launcher}
   * in order to support a full local-specific programming.
   * 
   * @return the resources
   */
  public static ResourceBundle getResources() {
    return resources;
  }

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
    this(true);
  }

	/**
   * Creates a new {@link Launcher} with an empty list of command-line options
   * but that will terminate the JVM after its execution. This constructor also
   * initializes the logging functionality.
   * @param showCopyrightMessage
   */
  private Launcher(boolean showCopyrightMessage) {
    super();
    
    configureSystemProperties();
    
		try {
			/*
			 * Do not call getAppConf() here because this will cause several other
			 * operations to be executed...
			 */
			GUITools.configureSplashScreen(getVersionNumber(),
				getYearWhenProjectWasStarted(), getYearOfProgramRelease(),
				addVersionNumberToSplashScreen(), addCopyrightToSplashScreen());
		} catch (Throwable t) {
			/* 
			 * Ignore any problems here, because it might happen that under some
			 * environments splash screens cannot be displayed, which is not a
			 * serious problem. 
			 */
		}
    
	  if (showCopyrightMessage) {
			printCopyrightMessage();
    }
    
    this.terminateJVMwhenDone = true;
    this.props = new SBProperties();
    LogUtil.initializeLogging(getLogLevel(), getLogPackages());
  }

	/**
	 * Copy constructor. This constructor will neither show a splash screen nor
	 * print the license agreement on the standard out.
	 * 
	 * @param launcher the original {@link Launcher} to be cloned.
	 */
	public Launcher(Launcher launcher) {
    this(false);
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
	 * Decides whether or not the copyright notice of the copyright holder of this
	 * program should be displayed on a layer on top of the splash screen of the
	 * program (if there is any).
	 * 
	 * @return <code>true</code> if the copyright message should be displayed,
	 *         <code>false</code> otherwise.
	 */
  protected boolean addCopyrightToSplashScreen() {
    return true;
  }

  /**
	 * Decides whether or not the version number of this program should be shown
	 * on a layer on top of the program's splash screen (if there is any).
	 * 
	 * @return <code>true</code> if the version number should be displayed,
	 *         <code>false</code> otherwise.
	 */
  protected boolean addVersionNumberToSplashScreen() {
    return true;
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
		
	/**
	 * Configures necessary properties of the {@link System} in order to support
	 * certain platform dependencies correctly. This method should be called very
	 * early when the program is launched (as early as possible).
	 */
  protected void configureSystemProperties() {
    /* 
     * In this case we can assume that this is the first instance of 
     * Launcher that has been initialized. So let's also set a minimum
     * of Mac OS X specific properties if we operate on a Mac. 
     */
  	Properties p = System.getProperties();
  	String title = getAppName();
  	
		if ((p.getProperty("mrj.version") != null)
				|| (p.getProperty("os.name").toLowerCase().indexOf("mac") != -1)) {
      p.setProperty("apple.awt.graphics.EnableQ2DX", "true");
      p.setProperty("apple.laf.useScreenMenuBar", "true");
      p.setProperty("com.apple.macos.smallTabs", "true");
      p.setProperty("com.apple.macos.useScreenMenuBar", "true");
      /* 
       * Note: the xDock name property must be set before parsing 
       * command-line arguments! See above!
       */
      p.setProperty("com.apple.mrj.application.apple.menu.about.name", title);
      
      p.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
      p.setProperty("com.apple.mrj.application.live-resize", "true");
    }
    // Use the systems proxy settings to establish connections
    // This must also be done prior to any other calls.
    p.setProperty("java.net.useSystemProxies", "true");
    
    p.setProperty("app.name", title);
    p.setProperty("app.version", getVersionNumber());
  }
  
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
   * Calls #exit(java.awt.Window, boolean) with <code>null</code> as first
   * argument. The second is taken from {@link #isTerminateJVMwhenDone()}.
   */
	public void exit() {
    exit(null, isTerminateJVMwhenDone());
  }
	
  /**
   * Calls {@link #exit(java.awt.Window, boolean)} with the given window as
   * first argument, the second argument is set to <code>true</code>.
   * 
   * @param window
   */
	public void exit(java.awt.Window window) {
	  exit(window, true);
	}
  
  /**
   * Saves all interactive actions and closes this application (if
   * {@link #terminateJVMwhenDone} is set to <code>true</code>), this means that
   * a call of this method will terminate the running Java Virtual Machine
   * (JVM).
   * 
   * @param window
   *        the parent window that has just been closed.
   * @param terminateJVMwhenDone
   *        whether or not to terminate the JVM (and making all interactive
   *        options persistent).
   */
  public void exit(java.awt.Window window, boolean terminateJVMwhenDone) {
    if (terminateJVMwhenDone) {
      StringBuilder exception = new StringBuilder();
      if (getInteractiveOptions() != null) {
        for (Class<? extends KeyProvider> clazz : getInteractiveOptions()) {
          SBPreferences prefs = SBPreferences.getPreferencesFor(clazz);
          try {
            prefs.flush();
          } catch (Exception exc) {
            exception.append(exc.getLocalizedMessage());
            exception.append('\n');
          }
        }
      }
      if (exception.length() > 0) {
        boolean showExc = false;
        if (showsGUI()) {
          try {
            GUITools.showErrorMessage(window, exception.toString());
          } catch (java.awt.HeadlessException exc) {
            showExc = true;
          }
        }
        if (showExc) {
          logger.warning('\n' + exception.toString());
        }
      }
      System.exit(0);
    }
  }
	
  /**
	 * Creates and returns an exhaustive data structure that provides several
	 * characteristic features of this program.
	 * 
	 * @return A specialized data structure that encapsulates information about a
	 *         program.
	 */
	public AppConf getAppConf() {
		return new AppConf(getAppName(), getVersionNumber(),
			getYearOfProgramRelease(), getYearWhenProjectWasStarted(),
			getCmdLineOptions(), getCommandLineArgs(), getInteractiveOptions(),
			getURLlicenseFile(), getURLOnlineUpdate());
	}
	
  /**
	 * This method tells a caller the name of this program. By default this
	 * will be equivalent to calling {@link Class#getSimpleName()} for the
	 * current object.
	 * 
	 * @return The name of this program.
	 */
	public String getAppName() {
	  return getClass().getSimpleName();
	}
  
  /**
	 * This method provides information about the {@link Option} containing
	 * {@link KeyProvider} {@link Class} objects that are allowable command line
	 * options for this program.
	 * 
	 * @return a {@link List} of {@link KeyProvider} {@link Class} objects that
	 *         define collections of possible command-line options.
	 */
	public abstract List<Class<? extends KeyProvider>> getCmdLineOptions();
  
  /**
   * These options influence the behavior of the program before it is actually
   * started, i.e., options that are parsed from the command line before really
   * launching the program.
   * 
   * @return
   */
  public SBProperties getCommandLineArgs() {
    return props;
  }

  /**
   * These are options that can be used to manipulate the behavior of a program
   * while the program is already running. For instance, these options may be
   * called in a graphical user interface or in the command line mode where the
   * user may interactively select some options. The {@link #exit()} method of
   * this {@link Launcher} will also try to make all key-value pairs related to
   * these interactive options persistent right before closing the JVM.
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
    return Level.INFO;
  }

  /**
   * @return An array of package names whose log messages should appear.
   */
  public String[] getLogPackages() {
    return new String[] {"de.zbit"};
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
	 *         quotes). Must not be <code>null</code>.
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
		java.awt.Window ui = initGUI(appConf);
		if (ui != null) {
			if (terminateJVMwhenDone) {
        ui.addWindowListener(EventHandler.create(
          java.awt.event.WindowListener.class, this, "exit", "window",
          "windowClosed"));
				setTerminateJVMwhenDone(false);
			}
			ui.setLocationRelativeTo(null);
		  ui.setVisible(true);
			// Since we are in the GUI mode, there is no need to try/catch the following command:
			GUITools.hideSplashScreen();
			ui.toFront();
		} else {
			if (appConf.getCmdArgs().getBooleanProperty(GUIOptions.GUI)) {
				logger.warning(String.format(
				  resources.getString("NO_GUI_SUPPORTED"),
				  getAppName(),
				  getVersionNumber()));
			} else {
				logger.warning(String.format(
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
   * @param appConf
   *        the configuration of the current application.
   * 
   * @return the graphical user interface for this application or null if no
   *         such mode is supported.
   */
	public abstract java.awt.Window initGUI(AppConf appConf);
	
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
		try {
			GUITools.hideSplashScreen();
		} catch (Throwable t) {
			// Ignore problems when dealing with splash screens in command line mode.
		}
		exit();
	}

  /**
   * 
   * @param args
   * @return
   */
  public SBProperties parseCmdArgs(String[] args) {
    logger.fine(resources.getString("SCANNING_CMD_ARGS"));
    props = SBPreferences.analyzeCommandLineArguments(getCmdLineOptions(),
      args);
    return props;
  }
	
	/**
	 * Displays a copyright notice using the System.out.
	 */
  public void printCopyrightMessage() {
    StringBuilder message = new StringBuilder();
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 60; i++) {
      sb.append('-');
    }
    message.append(sb);
    message.append('\n');
    message.append(String.format(resources.getString("COPYRIGHT_MESSAGE"),
      getAppName(), getYearWhenProjectWasStarted(),
      getYearOfProgramRelease()));
    URL licenseFile = null;
    licenseFile = getURLlicenseFile();
    if (licenseFile != null) {
      message.append(String.format(resources.getString("LINK_TO_LICENSE_FILE"),
        licenseFile.toString()));
    }
    message.append('\n');
    message.append(sb);
    System.out.println(message.toString());
  }
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
  public void run() {
    // Should we start the GUI?
    if (showsGUI()) {
    	javax.swing.SwingUtilities.invokeLater(new Runnable() {
        /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        public void run() {
          AppConf appCnf = getAppConf();
          try {
          	// Now we're preparing the GUI mode.
      			GUITools.initLaF();
            guiMode(appCnf);
          } catch (java.awt.HeadlessException exc) {
            if (props.getBooleanProperty(GUIOptions.GUI)) {
              logger.severe(resources.getString("COULD_NOT_INITIALIZE_GUI"));
            }
            launchCommandLineMode(appCnf);
          }
          exit();
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

  /**
   * Decides whether or not to show a graphical user interface.
   * 
   * @return <code>true</code> if a graphical user interface should be shown,
   *         <code>false</code> otherwise.
   */
  public boolean showsGUI() {
    return (props.size() < 1) || props.getBooleanProperty(GUIOptions.GUI);
  }
	
	/* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return getAppName();
  }
	
}
