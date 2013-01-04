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
package de.zbit.gui.table.renderer;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.table.DefaultTableCellRenderer;

import de.zbit.math.MathUtils;

/**
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class ScientificNumberRenderer extends DefaultTableCellRenderer {
  private static final long serialVersionUID = 6924185889211247242L;
  
  /**
   * Default scientific number format
   */
  private static NumberFormat scientificFormat = new DecimalFormat("0.###E0");
  
  /**
   * The actual formatter
   */
  private NumberFormat formatter;
  
  /**
   * A boundary that defines when to use the formatter
   */
  private double boundary;
  
  public ScientificNumberRenderer() { 
    this(0);
  }
  
  /**
   * @param boundary a boundary when to apply the {@link ScientificNumberRenderer}.
   * If the value is greater than or equal to this value, or smaller than
   * 1/<code>boundary</code>, the {@link ScientificNumberRenderer} is applied.
   * Set to <code>0</code> to always apply this renderer.
   */
  public ScientificNumberRenderer(double boundary) {
    super();
    this.boundary = boundary;
  }
  
  public void setValue(Object value) {
    String text = getNiceString(value, formatter, boundary);
    
    setText(text);
  }

  /**
   * 
   * @param value instance of {@link Number}
   * @param formatter scientific number formatter to use.
   * Set to {@code null} to automatically initialize a formatter.
   * @param boundary defines threshold when to use the scientific formatter.
   * Set to 0 to always use it. Recommended value: 100.
   * @return nice number string.
   */
  public static String getNiceString(Object value, NumberFormat formatter, double boundary) {
    if (formatter == null) {
      formatter = getScientificNumberFormat();
    }
    
    // Format number
    String text = value==null?"":value.toString();
    try {
      if (value instanceof Number) {
        double d = Math.abs(((Number)value).doubleValue());
        if (d>=boundary || d<=1/boundary) {
          text = (value == null) ? "" : formatter.format(value);
        } else {
          text = Double.toString(MathUtils.round(((Number)value).doubleValue(), 4));
        }
      }
    } catch (Exception e) {
      
    }
    
    // Ausnahmen
    if (text.endsWith("E0")) text = text.substring(0, text.length()-2);
    return text;
  }

  /**
   * @return new DecimalFormat("0.###E0");
   */
  public static NumberFormat getScientificNumberFormat() {
    return scientificFormat;
  }
  
}
