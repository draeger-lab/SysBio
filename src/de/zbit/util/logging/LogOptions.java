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

import java.io.File;
import java.util.logging.Level;

import de.zbit.io.filefilter.SBFileFilter;
import de.zbit.util.Reflect;
import de.zbit.util.prefs.KeyProvider;
import de.zbit.util.prefs.Option;
import de.zbit.util.prefs.Range;

/**
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 */
public interface LogOptions extends KeyProvider {
  
  /**
   * This allows to change the Log-{@link Level}. It it intended to not allow
   * levels ALL or OFF (giving bounds of -/+ Integer max values is not
   * reasonable for the range).
   */
  public static final Option<String> LOG_LEVEL = new Option<String>(
      "LOG_LEVEL", String.class, "Change the log-level of this application. This option will influence how fine-grained error and other log messages will be that you receive while executing this program.",
      new Range<String>(String.class, Reflect.getStaticFinalVariablesAsEnumeration(Level.class)),
      Level.INFO.getName());
  
  /**
   * This option allows you to specify a log file to which all information of
   * the program will be written.
   */
  public static final Option<File> LOG_FILE = new Option<File>(
      "LOG_FILE", File.class, "This option allows you to specify a log file to which all information of the program will be written.",
      new Range<File>(File.class, SBFileFilter.createLogFileFilter()), null);
  
}
