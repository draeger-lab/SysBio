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
package de.zbit.util.logging;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;



/**
 * Class with helper functions regarding logging.
 * 
 * @author Florian Mittag
 * @author Clemens Wrzodek
 * @version $Rev$
 * @since 1.0
 */
public class LogUtil {
  
  /**
   * This package is always considered when performing any logging operations here.
   */
  public final static String basePackage = "de.zbit";
  
  /**
   * Remember packages from last {@link #initializeLogging(String...)} call
   */
  private static String packages_to_log[];

  /**
   * Initializes logging using the Java logger and with log level INFO for
   * all classes that are in sub-packages of de.zbit.
   * 
   * @param packages list of packages for which the logger should be initialized
   *                 additionally
   */
  public static void initializeLogging(String... packages) {
    initializeLogging(Level.INFO, packages);
  }
  
  /**
   * Initializes logging using the Java logger and sets the given log level for
   * all classes that are in sub-packages of de.zbit.
   * 
   * @param loglevel the desired log level
   * @param packages list of packages for which the logger should be initialized
   *                 additionally
   */
  public static void initializeLogging(Level logLevel, String... packages ) {
    // initialize logging
    //if (showLoggingConsole) {
    //  Logging.enableLoggingConsole();
    //}
    packages_to_log = packages;
    
    for (Handler h : Logger.getLogger("").getHandlers()) {
      h.setLevel(logLevel);
      h.setFormatter(new OneLineFormatter());
    }
    Logger.getLogger(basePackage).setLevel(logLevel);

    // additional packages to log
		if (packages != null) {
			for (String s : packages) {
				for (Handler h : Logger.getLogger(s).getHandlers()) {
					h.setLevel(logLevel);
					h.setFormatter(new OneLineFormatter());
				}
				Logger.getLogger(s).setLevel(logLevel);
			}
		}
  }
  
  /**
   * @return packages that have been used in the last
   * call to {@link #initializeLogging(String...)}
   */
  public static String[] getInitializedPackages() {
    return packages_to_log;
  }
  
  /**
   * Add a new handler
   * @param h
   * @param packages
   */
  public static void addHandler(Handler h, String... packages ) {
    Logger.getLogger(basePackage).addHandler(h);

    // additional packages to handle
    if (packages != null) {
      for (String s : packages) {
        Logger.getLogger(s).addHandler(h);
      }
    }
  }
  
  /**
   * Remove a handler
   * @param h
   * @param packages
   */
  public static void removeHandler(Handler h, String... packages ) {
    Logger.getLogger(basePackage).removeHandler(h);

    // additional packages to handle
    if (packages != null) {
      for (String s : packages) {
        Logger.getLogger(s).removeHandler(h);
      }
    }
  }

  /*
  public static void endLogging() {
    if (Logging.loggingConsoleIsVisible()) {
      Logging.deactivateLoggingConsole();
    }
  }
  */
  
}
