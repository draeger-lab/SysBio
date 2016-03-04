/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2016 by the University of Tuebingen, Germany.
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

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
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
   * Keep references to configured Loggers so the garbage collector does not delete them
   */
  private static Map<String, Logger> loggerRefs = new TreeMap<String, Logger>();
  
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
        if (!s.equals(basePackage)) {
          Logger.getLogger(s).addHandler(h);
        }
      }
    }
  }
  
  /**
   * Change the log level of all packages for those logging has
   * been initialized with {@link #initializeLogging(Level, String...)}.
   * @param logLevel
   */
  public static void changeLogLevel(Level logLevel) {
    changeLogLevel(logLevel, false);
  }
  
  /**
   * Change the log level of the given packages.
   * @param logLevel
   * @param packages
   */
  public static void changeLogLevel(Level logLevel, String... packages) {
    changeLogLevel(logLevel, false, packages);
  }
  
  /**
   * Change the log level of the base package (de.zbit) and all packages for
   * those logging has been initialized with
   * {@link #initializeLogging(Level, String...)}.
   * 
   * @param logLevel
   * @param initOneLineFormatter also change the formatter of all
   * Handlers to the {@link OneLineFormatter}.
   */
  private static void changeLogLevel(Level logLevel, boolean initOneLineFormatter) {
    Logger.getLogger(basePackage).setLevel(logLevel);
    
    // additional packages to log
    changeLogLevel(logLevel, initOneLineFormatter, packages_to_log);
  }
  
  /**
   * Change the log level of the given packages.
   * 
   * @param logLevel
   * @param initOneLineFormatter
   *          also change the formatter of all Handlers to the
   *          {@link OneLineFormatter}.
   * @param packages
   */
  private static void changeLogLevel(Level logLevel, boolean initOneLineFormatter, String... packages) {
    // packages to change log level for
    if (packages != null) {
      for (String s : packages) {
        Logger l = Logger.getLogger(s);
        for (Handler h : l.getHandlers()) {
          h.setLevel(logLevel);
          if (initOneLineFormatter) {
            h.setFormatter(new OneLineFormatter());
          }
        }
        l.setLevel(logLevel);
        loggerRefs.put(s, l);
      }
    }
  }
  
  
  /**
   * 
   * @return
   */
  public static Level getCurrentLogLevel() {
    return Logger.getLogger("").getHandlers()[0].getLevel();
  }
  
  /**
   * @return packages that have been used in the last
   * call to {@link #initializeLogging(String...)}
   */
  public static String[] getInitializedPackages() {
    return packages_to_log;
  }
  
  /**
   * Initializes logging using the Java logger and sets the given log level for
   * all classes that are in sub-packages of de.zbit.
   * 
   * @param logLevel the desired log level
   * @param packages list of packages for which the logger should be initialized
   *                 additionally
   */
  public static void initializeLogging(Level logLevel, String... packages ) {
    // set console handler to log everything
    for (Handler h : Logger.getLogger("").getHandlers()) {
      h.setLevel(Level.ALL);
      h.setFormatter(new OneLineFormatter());
    }
    
    packages_to_log = packages;
    
    changeLogLevel(logLevel, true);
  }
  
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
  
  /**
   * 
   */
  public static void printLogLevels() {
    LogManager lm = LogManager.getLogManager();
    
    for (String name : Collections.list(lm.getLoggerNames())) {
      Level l = lm.getLogger(name).getLevel();
      if (l != null) {
        System.out.println(name + " = " + l);
      }
    }
  }
  
}
