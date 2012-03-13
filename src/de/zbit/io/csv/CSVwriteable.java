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
package de.zbit.io.csv;

import de.zbit.exception.CorruptInputStreamException;
import de.zbit.io.CSVwriteableTest2;

/**
 * Interface that marks this class as CSVwriteable.
 * The class implementing this interface can be read and written from
 * a TAB-separated file (usign {@link CSVReader#read(String)} and
 * {@link CSVWriter#write(CSVwriteable, String)}).
 * 
 * <p> An example implementation and usage can be found in the 
 * test folder! ({@link de.zbit.io.CSVwriteableTest2}).</p>
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public interface CSVwriteable {
  
  /**
   * Should return a TAB SEPARATED string, describing the current class.
   * If the class is or includes a list/ array, this method is called with
   * an increasing <i>elementNumber</i> until "NULL" is returned.
   * 
   * <p>Notes:
   * <ul>
   * <li> You should neither start, nor end with a tab
   * <li> You should return exactly one line per call. Only if it is really
   * necessary, you might also return a string, containing line separators.
   * </ul>
   * </p>
   * 
   * @param elementNumber - if your class can be written in one line, return
   * this line if <i>elementNumber</i> == 0 and NULL otherwise. If your class
   * needs multiple lines to be written, return them one after another and
   * finally return NULL.
   * @return Tab separated string, representing this class.
   */
  public String toCSV(int elementNumber);
  

  /**
   * This should be the exact reverse from {@link #toCSV(int)}. Read the
   * current class from a line.
   * @see #toCSV(int)
   * @param elements - the components of the current line
   * @param elementNumber - current line number
   * @param CSVversionNumber - this is the version number of the saved file,
   * as returned while saving the file from {@link #getCSVOutputVersionNumber()}
   * @return
   * @throws CorruptInputStreamException - throw this exception if the
   * input Stream is Corrupt.
   */
  public void fromCSV(String[] elements, int elementNumber, int CSVversionNumber) throws CorruptInputStreamException;
  
  /**
   * Return any integer, that identifies the output version of this class.
   * With this number, you can recognize old saved files and
   * adapt your class accordingly.
   * @return
   */
  public int getCSVOutputVersionNumber();
  
}
