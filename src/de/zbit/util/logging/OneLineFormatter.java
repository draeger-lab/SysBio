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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;


/**
 * A simple formatter for the Java logger that puts everything in one line.
 * Based on the java.util.logging.SimpleFormatter
 * 
 * @author Florian Mittag
 * @version $Rev$
 * @since 1.0
 */
public class OneLineFormatter extends Formatter {
  
  /**
   * 
   */
  private final static String format = "{0,date} {0,time}";
  
  /**
   * 
   */
  private Object args[] = new Object[1];
  
  /**
   * 
   */
  Date dat = new Date();
  
  /**
   * 
   */
  private boolean displayClassName;
  
  /**
   * 
   */
  private boolean displayMethodName;
  
  /**
   * 
   */
  private boolean displayTime;
  
  /**
   * 
   */
  private MessageFormat formatter;
  
  /**
   * Line separator string. This is the value of the line.separator property at
   * the moment that the {@link SimpleFormatter} was created.
   */
  private String lineSeparator = System.getProperty("line.separator");
  
  /**
   * 
   */
  public OneLineFormatter() {
    this(true, true, true);
  }
  
  /**
   * @param displayTime
   * @param displayClassName
   * @param displayMethodName
   */
  public OneLineFormatter(boolean displayTime, boolean displayClassName,
    boolean displayMethodName) {
    super();
    this.displayTime = displayTime;
    this.displayClassName = displayClassName;
    this.displayMethodName = displayMethodName;
  }
  
  /**
   * 
   */
  @Override
  public synchronized String format(LogRecord record) {
    StringBuffer sb = new StringBuffer();
    if (displayTime) {
      // Minimize memory allocations here.
      dat.setTime(record.getMillis());
      args[0] = dat;
      StringBuffer text = new StringBuffer();
      if (formatter == null) {
        formatter = new MessageFormat(format);
      }
      formatter.format(args, text, null);
      sb.append(text);
      sb.append(" ");
    }
    if (displayClassName) {
      if (record.getSourceClassName() != null) {
        sb.append(record.getSourceClassName());
      } else {
        sb.append(record.getLoggerName());
      }
      if (displayMethodName) {
        if (record.getSourceMethodName() != null) {
          sb.append(" ");
          sb.append(record.getSourceMethodName());
        }
      }
    }
    if (sb.length() > 0) {
      sb.append(" --- ");
    }
    String message = formatMessage(record);
    sb.append(record.getLevel().getLocalizedName());
    sb.append(": ");
    sb.append(message);
    sb.append(lineSeparator);
    if (record.getThrown() != null) {
      try {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        record.getThrown().printStackTrace(pw);
        pw.close();
        sb.append(sw.toString());
      } catch (Exception ex) {
      }
    }
    return sb.toString();
  }
  
  /**
   * @return the displayClassName
   */
  public boolean isDisplayClassName() {
    return displayClassName;
  }
  
  /**
   * @return the displayMethodName
   */
  public boolean isDisplayMethodName() {
    return displayMethodName;
  }
  
  /**
   * @return the displayTime
   */
  public boolean isDisplayTime() {
    return displayTime;
  }
  
  /**
   * @param displayClassName the displayClassName to set
   */
  public void setDisplayClassName(boolean displayClassName) {
    this.displayClassName = displayClassName;
  }
  
  /**
   * @param displayMethodName the displayMethodName to set
   */
  public void setDisplayMethodName(boolean displayMethodName) {
    this.displayMethodName = displayMethodName;
  }
  
  /**
   * @param displayTime the displayTime to set
   */
  public void setDisplayTime(boolean displayTime) {
    this.displayTime = displayTime;
  }
  
}
