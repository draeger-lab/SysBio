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
package de.zbit.io;

import java.io.IOException;
import java.util.Arrays;

import de.zbit.io.csv.CSVReader;

/**
 * @version $Rev$
 */
public class CSVReaderTest {
  
  /**
   * @param args
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {
    if (args != null && args.length == 1) {
      CSVReader a = new CSVReader(args[0]);
      a.setDisplayProgress(false); // Optional, set to true, if not sysouting
      
      String[] line;
      while ((line = a.getNextLine()) != null) {
        System.out.println(Arrays.toString(line));
      }
      
      a.getHeader(); // The header (if available)
      a.getPreamble(); // Everything, before actual table start
      
    } else {
      System.out.println("It is necessary to enter a csv file!");
    }
    
  }
  
}
