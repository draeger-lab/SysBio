/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2012 by the University of Tuebingen, Germany.
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

import java.io.IOException;

/**
 * @author Finja B&uuml;chel
 * @version $Rev$
 */
public class FileToolTest {
  
  /**
   * Just for testing.
   * @param args
   */
  public static void main(String[] args) {
    
    try {
//      FileTools.splitFile("H:/ValidationData/proDGe_DROME_raw.txt", "H:/ValidationData", "proDGe_DROME_raw", ".txt", 8000000);     
//      FileTools.splitFile("H:/ValidationData/proDGe_RAT_raw.txt", "H:/ValidationData", "proDGe_RAT_raw", ".txt", 8000000);
      FileTools.splitFile("/home/buechel/ValidationData/proDGe_MOUSE_raw.txt", "/home/buechel/ValidationData", "proDGe_MOUSE_raw_", ".txt", 8000000);
      FileTools.splitFile("/home/buechel/ValidationData/proDGe_HUMAN_raw.txt", "/home/buechel/ValidationData", "proDGe_HUMAN_raw", ".txt", 8000000);
      
    } catch (IOException e) {
      e.printStackTrace();
    }
    
    System.out.println(FileTools.which("pdflatex"));
  }
  
}
