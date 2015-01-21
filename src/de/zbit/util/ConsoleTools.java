/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2015 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.util;

import java.io.File;

import de.zbit.io.FileTools;

/**
 * Sevaral tools, regarding the console.
 * Before using any other tool, you should check wether the output
 * stream is actually a console. Please do this with {@link #isTTY()}.
 * 
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class ConsoleTools {
  
  /**
   * TTY is a console that accepts ANSI commands.
   * This method checks whether or not the {@link System#out} stream
   * is a ANSI compliant system console or not.
   * 
   * <p>Note: This function does NOT work in Java 1.5 and is
   * thus a redirection to a separate class to catch this error.</p>
   * @return true if and only if the {@link System#out} is a ANSI
   * compliant console. Otherwise (e.g., a file or the clipse Console
   * window) false is treturned
   * 
   */
  public static boolean isTTY() {
    try {
      // is TTY Check is only available for java 1.6. So a wrapper to
      // determine java version is needed for Java 1.5 compatibility.
      String v = System.getProperty("java.version");
      if (v!=null && v.length()>2) {
        /*
         * Use simply style for java version <1.6, because it's not 
         * possible to determine if output goes into file or not
         * (System.console not available in JDK 1.5).
         */
        double d = Double.parseDouble(v.substring(0, 3));
        if (d<1.6) return false;
      }
      
      return isTTY_Java16only.isTty();
    } catch (Throwable e) {
      return false;
    }
  }
  
  
  /**
   * Get the width of a console (the number of columns).
   * 
   * <p>Note: Unfortunately, since java always creates a new process,
   * this method will return always 80.</p>
   * 
   * @return number of columns (characters in a line) of the current
   * console window.
   */
  public static int getColumns() {
    int cols = 80; // -1 = could not get value; 80 = default
    
    /* Possibilities (all non working from Java)
     * - tput cols
     * - echo $COLUMNS /// bash -c "echo $COLUMNS"
     * - stty size
     * 
     * tput and stty gives default values, whereas the environment variable doesn't exist.
     */
    String variable = System.getenv("COLUMNS");
    if (variable!=null && Utils.isNumber(variable, true))
      return Integer.parseInt(System.getenv("COLUMNS"));
    
    File tput = FileTools.which("tput");
    if (tput!=null && tput.exists()) {
      String ret = ProcessExecutioner.executeProcess(tput.getPath(), "cols");
      if (ret!=null) {
        ret = ret.trim();
        if (Utils.isNumber(ret, true)) cols = Integer.parseInt(ret);
      }
    }
    
    return cols;
  }
  
}
