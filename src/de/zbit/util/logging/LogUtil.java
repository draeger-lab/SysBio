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
 * @version $Rev$
 * @since 1.0
 */
public class LogUtil {

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
  
    
    for (Handler h : Logger.getLogger("").getHandlers()) {
      h.setLevel(logLevel);
      h.setFormatter(new OneLineFormatter());
    }
    Logger.getLogger("de.zbit").setLevel(logLevel);

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

  /*
  public static void endLogging() {
    if (Logging.loggingConsoleIsVisible()) {
      Logging.deactivateLoggingConsole();
    }
  }
  */
  
}
