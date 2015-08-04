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
package de.zbit.util.logging;

import java.util.logging.ErrorManager;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 */
public class ConsoleHandler extends java.util.logging.ConsoleHandler {
  
  /**
   * 
   */
  public ConsoleHandler() {
    super();
  }
  
  /* (non-Javadoc)
   * @see java.util.logging.ConsoleHandler#publish(java.util.logging.LogRecord)
   */
  @Override
  public synchronized void publish(LogRecord record) {
    if (!isLoggable(record)) {
      return;
    }
    if (record.getLevel().intValue() >= Level.WARNING.intValue()) {
      super.publish(record);
    } else {
      try {
        String message = getFormatter().format(record);
        System.out.write(message.getBytes());
      } catch (Exception ex) {
        // We don't want to throw an exception here, but we
        // report the exception to any registered ErrorManager.
        reportError(null, ex, ErrorManager.FORMAT_FAILURE);
        return;
      }
    }
  }
  
  /* (non-Javadoc)
   * @see java.util.logging.StreamHandler#flush()
   */
  @Override
  public synchronized void flush() {
    super.flush();
    try {
      System.out.flush();
    } catch (Exception ex) {
      // We don't want to throw an exception here, but we
      // report the exception to any registered ErrorManager.
      reportError(null, ex, ErrorManager.FLUSH_FAILURE);
    }
  }
  
}
