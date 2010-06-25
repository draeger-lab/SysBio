package de.zbit.util;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.TTCCLayout;
import org.apache.log4j.varia.NullAppender;

/**
 * Class with helper functions regarding logging.
 * 
 * @author Florian Mittag
 */
public class LogUtil {

  /**
   * Initializes logging using log4j and sets the given Loglevel for all classes
   * that are in subpackages of de.zbit.
   * 
   * @param loglevel
   */
  public static void initializeLogging(Level loglevel) {
    try {
      //Logger rootLogger = Logger.getRootLogger();
      Layout layout = new TTCCLayout();
      
      // only set log level for SysBio classes
      ConsoleAppender consoleAppender = new ConsoleAppender( layout );
      Logger.getLogger("de.zbit").addAppender( consoleAppender );
      Logger.getLogger("de.zbit").setLevel(loglevel);
      
      
      
    } catch( Exception ex ) {
      System.out.println( ex );
    }
  }
}
