package de.zbit.util;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.zbit.logging.OneLineFormatter;


/**
 * Class with helper functions regarding logging.
 * 
 * @author Florian Mittag
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
    for (String s : packages) {
      for (Handler h : Logger.getLogger(s).getHandlers()) {
        h.setLevel(logLevel);
        h.setFormatter(new OneLineFormatter());
      }
      Logger.getLogger(s).setLevel(logLevel);
      
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
