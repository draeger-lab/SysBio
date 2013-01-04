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
package de.zbit.exception;

import java.io.IOException;

/**
 * Used by, e.g., {@link de.zbit.io.csv.CSVReader#read(de.zbit.io.CSVwriteable, String)}.
 * 
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class CorruptInputStreamException extends IOException {
  private static final long serialVersionUID = 1217199680560353628L;
  
  public CorruptInputStreamException() {
    super();
  }

  public CorruptInputStreamException(String message, Throwable cause) {
    super(message, cause);
  }

  public CorruptInputStreamException(String message) {
    super(message);
  }

  public CorruptInputStreamException(Throwable cause) {
    super(cause);
  }
}
