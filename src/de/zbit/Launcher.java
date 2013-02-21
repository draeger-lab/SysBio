/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2013 by the University of Tuebingen, Germany.
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
import java.security.AccessControlException;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.zbit.gui.GUIOptions;
import de.zbit.gui.GUITools;
import de.zbit.gui.UpdateMessage;
import de.zbit.gui.mac.NativeLibraryLoader;
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
 * <td>{@code app.name}</td>
 * <td>the name of the program</td>
 * </tr>
 * <tr>
 * <td>{@code app.version}</td>
 * <td>the version number of the program</td>
 * </tr>
 * </table>
 * If the user operates on Mac OS X, all necessary {@link System}
 * {@link Properties} specific to this environment will also be set. This might
 * make using {@code -Xdock:name="Some title" -Xdock:icon=path/to/icon} on
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
	private static final transient ResourceBundle resources = 
			ResourceManager.getBundle("de.zbit.locales.Launcher");

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
   * Checks whether the operating system is MacOS.
   * @return
   */
	private static final boolean isMacOS() {
		return (System.getProperty("mrj.version") != null)
				|| (System.getProperty("os.name").toLowerCase().indexOf("mac") != -1);
	}

	/**
	 * Stores given command-line options as key-value pairs.
	 */
	private SBProperties props;

	/**
   * Switch to decide if {@link System#exit(int)} should be called when the
   * execution of this {@link Launcher} is finished. This option should be set
   * to {@code true} when using a graphical user interface, which will be
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
    
    // This must be done as early as possible!
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
	  if ((args != null) && (args.length > 0)) {
	  	parseCmdArgs(args);
	  }
		run();
	}

  /**
	 * Decides whether or not the copyright notice of the copyright holder of this
	 * program should be displayed on a layer on top of the splash screen of the
	 * program (if there is any).
	 * 
	 * @return {@code true} if the copyright message should be displayed,
	 *         {@code false} otherwise.
	 */
  protected boolean addCopyrightToSplashScreen() {
    return true;
  }
	
  /**
	 * Decides whether or not the version number of this program should be shown
	 * on a layer on top of the program's splash screen (if there is any).
	 * 
	 * @return {@code true} if the version number should be displayed,
	 *         {@code false} otherwise.
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
  	String title = getAppName();
  	
  	try {
  		
  		if (isMacOS()) {
  			/* 
  			 * Note: the xDock name property must be set before parsing 
  			 * command-line arguments! See above!
  			 * See
  			 * http://developer.apple.com/library/mac/#documentation/Java/Reference/Java_PropertiesRef/Articles/JavaSystemProperties.html
  			 * for details.
  			 */
  			System.setProperty("apple.awt.graphics.EnableQ2DX", "true");
  			System.setProperty("apple.awt.showGrowBox", "true");
  			// These are actually already the default values:
  			//  			System.setProperty("apple.awt.UIElement", "false");
  			//  			System.setProperty("apple.awt.fileDialogForDirectories", "false");
  			//  			System.setProperty("apple.awt.brushMetalLook", "false");
  			//  			System.setProperty("apple.awt.fakefullscreen", "false");
  			System.setProperty("apple.awt.TextAntialiasing", "true");
  			
  			System.setProperty("apple.laf.useScreenMenuBar", "true");
  			
  			System.setProperty("com.apple.macos.smallTabs", "true");
  			System.setProperty("com.apple.macos.useScreenMenuBar", "true");
  			
  			System.setProperty("com.apple.mrj.application.apple.menu.about.name", title);
  			System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
  			System.setProperty("com.apple.mrj.application.live-resize", "true");
  			
  			
  			String classPath = System.getProperty("java.class.path");
  			String javaSystemDir = "/System/Library/Java";
  			if (!classPath.contains(javaSystemDir)) {
  				if (classPath.length() > 0) {
  					classPath += ':';
  				}
  				classPath += javaSystemDir;
  				System.setProperty("java.class.path", classPath);
  			}
  			
  			try {
  				String tmpDirName = System.getProperty("user.dir");
  				String libPath = System.getProperty("java.library.path").toString();
  				if (!libPath.contains(tmpDirName)) {
  					if (libPath.length() > 0) {
  						libPath += ':';
  					}
  					libPath += tmpDirName;
  					System.setProperty("java.library.path", libPath);
  				}
  				NativeLibraryLoader.loadMacOSLibrary(tmpDirName);
  			} catch (Throwable exc) {
  				// Ignore this problem.
  				logger.fine(exc.getLocalizedMessage());
  			}
  		}
  		// Use the systems proxy settings to establish connections
  		// This must also be done prior to any other calls.
  		System.setProperty("java.net.useSystemProxies", "true");
  		
  		System.setProperty("app.name", title);
  		System.setProperty("app.version", getVersionNumber());
  		if (getCitation(true) != null) {
  		  System.setProperty("app.citation.html", getCitation(true));
  		}
      if (getCitation(false) != null) {
        System.setProperty("app.citation", getCitation(false));
      }
  		
  		
  	} catch (AccessControlException exc) {
  		/* This happens when executing a program as a Java(TM) Web Start Application
  		 * In this case, you should include the following code into your JNLP file:
  		 * <security>
       *   <all-permissions/>
       * </security>
  		 */
  		logger.warning(exc.getLocalizedMessage());
  	}
  }
  
  /**
   * Optional method that returns a citation for the implementing application.
   * 
   * @param HTMLstyle
   *        if {@code true}, a HTML style citation should be returned
   *        (using HTML-special chars and formatting).
   * @return complete citation string.
   */
  public String getCitation(boolean HTMLstyle) {
    // Is implemented, because this method should be OPTIONAL.
    // Example for a return in HTML-style could be:
    // "KEGGtranslator: visualizing and converting the KEGG PATHWAY database to various formats. Wrzodek C, Dr&#228;ger A, Zell A.<i>Bioinformatics</i>. 2011, <b>27</b>:2314-2315"
    // Please do not use HTML-names (such as "&auml;") but rather unicode encodings (as "&#228;").
    return null;
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
   * Calls #exit(java.awt.Window, boolean) with {@code null} as first
   * argument. The second is taken from {@link #isTerminateJVMwhenDone()}.
   */
	public void exit() {
    exit(null, isTerminateJVMwhenDone());
  }
	
  /**
   * Calls {@link #exit(java.awt.Window, boolean)} with the given window as
   * first argument, the second argument is set to {@code true}.
   * 
   * @param window
   */
	public void exit(java.awt.Window window) {
	  exit(window, true);
	}
  
	/**
	 * Saves all interactive actions and closes this application (if
	 * {@link #terminateJVMwhenDone} is set to {@code true}), this means that
	 * a call of this method will terminate the running Java Virtual Machine
	 * (JVM).
	 * 
	 * @param window
	 *        the parent window that has just been closed.
	 * @param terminateJVMwhenDone
	 *        whether or not to terminate the JVM (and making selected options
	 *        persistent).
	 * @see #getPersistentOptions()
	 */
	public void exit(java.awt.Window window, boolean terminateJVMwhenDone) {
		if (terminateJVMwhenDone) {
			/*
			 * Do not manipulate any preferences here. This must be done in the
			 * individual program. Otherwise it might happen that changes are
			 * overwritten.
			 */
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
   * @return By default, this method returns {@link Level#INFO}. If something
   *         different is desired, this method should be overridden in an
   *         implementing class.
   * @see {@link GUIOptions#LOG_LEVEL} allows to overwrite this value from
   *         the command-line.
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
   * This is a list of options, given on the command-line (i.e. should also
   * be contained in {@link #getCmdLineOptions()}), that are made persistent.
   * @return
   */
  public List<Class<? extends KeyProvider>> getPersistentOptions() {
    // Per definition, interactive options should be made persistent!
    return getInteractiveOptions();
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
	 *         quotes). Must not be {@code null}.
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
				logger.warning(MessageFormat.format(
				  resources.getString("NO_GUI_SUPPORTED"),
				  getAppName(),
				  getVersionNumber()));
			} else {
				logger.warning(MessageFormat.format("INCOMPLETE_CMD_ARG_LIST",
				  getAppName(),getVersionNumber()));
			  // TODO: No arguments and no GUI => It would be good if we show
				// the output of "--help" here!
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
		logger.info(MessageFormat.format(
			resources.getString("LAUNCHING_CMD_MODE"),
			getAppName()));
		try {
			GUITools.hideSplashScreen();
		} catch (Throwable t) {
			// Ignore problems when dealing with splash screens in command line mode.
		}
		commandLineMode(appConf);
		exit();
	}

  /**
   * 
   * @param args
   * @return
   */
  @SuppressWarnings("rawtypes")
  public SBProperties parseCmdArgs(String[] args) {
    logger.fine(resources.getString("SCANNING_CMD_ARGS"));
    // This does not make command-line arguments persistent.
    props = SBPreferences.analyzeCmdArgs(getCmdLineOptions(), args);
    
    // Do not make all, but some defined subset of them persistent
    List<Class<? extends KeyProvider>> persist = getPersistentOptions();
    if (persist!=null) {
      for (Class<? extends KeyProvider> kp : persist) {
        SBPreferences toflush = new SBPreferences(kp);
        Iterator<Option> it = KeyProvider.Tools.optionIterator(kp);
        while (it!=null && it.hasNext()) {
          Option<?> o = it.next();
          if (props.containsKey(o.toString())) {
            toflush.put(o, o.getValue(props));
          }
        }
        if (toflush.size()>0) {
          try {
            toflush.flush();
          } catch (Exception e) {
            logger.log(Level.WARNING, e.getLocalizedMessage(), e);
          }
        }
      }
    }
    
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
    message.append(MessageFormat.format(
    	resources.getString("COPYRIGHT_MESSAGE"),
    	MessageFormat.format(
    		resources.getString("PROGRAM_NAME_AND_VERSION"),
    		getAppName(), getVersionNumber()),
    	MessageFormat.format(
    		resources.getString("COPYRIGHT_HOLDER"),
    		getYearWhenProjectWasStarted(),
    		getYearOfProgramRelease(),
    		MessageFormat.format(
    			resources.getString("PROVIDER"),
    			resources.getString("ORGANIZATION"),
    			resources.getString("INSTITUTE"))
    		)
    	)
    );
    URL licenseFile = null;
    licenseFile = getURLlicenseFile();
    if (licenseFile != null) {
      message.append(MessageFormat.format(
      	resources.getString("LINK_TO_LICENSE_FILE"),
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
    // Get appConf command-line options
    final AppConf appConf = getAppConf();
    
    // Eventually change the log-level
    String logLevel = appConf.getCmdArgs().get(GUIOptions.LOG_LEVEL);
    if (logLevel != null) {
      LogUtil.changeLogLevel(Level.parse(logLevel.toUpperCase()));
    }
    
    /* 
     * Give the opportunity to load certain libraries or perform
     * other necessary operations in order to initialize the program.
     */
    setUp();
    
    // Should we start the GUI?
    if (showsGUI()) {
    	javax.swing.SwingUtilities.invokeLater(new Runnable() {
        /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        public void run() {
          try {
          	// Now we're preparing the GUI mode.
      			GUITools.initLaF();
            guiMode(appConf);
          } catch (java.awt.HeadlessException exc) {
            if (props.getBooleanProperty(GUIOptions.GUI)) {
              logger.severe(resources.getString("COULD_NOT_INITIALIZE_GUI"));
            }
            launchCommandLineMode(appConf);
          }
          exit();
        }
      });
    } else {
      launchCommandLineMode(appConf);
    }
  }

	/**
   * @param terminateJVMwhenDone the {@link #terminateJVMwhenDone} to set
   */
  public void setTerminateJVMwhenDone(boolean terminateJVMwhenDone) {
    this.terminateJVMwhenDone = terminateJVMwhenDone;
  }

	/**
	 * This method is called before launching the actual program, i.e., after all
	 * commend-line arguments have been read, but before deciding if the graphical
	 * user interface or the command-line mode of the program should be started.
	 * This method gives derived objects an opportunity to initialize certain
	 * resources, such as external libraries etc. By default, this method does
	 * nothing.
	 */
  protected void setUp() {
		// Empty method body. Implementation can be done in derived elements.
	}

  /**
   * Decides whether or not to show a graphical user interface.
   * 
   * @return {@code true} if a graphical user interface should be shown,
   *         {@code false} otherwise.
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
