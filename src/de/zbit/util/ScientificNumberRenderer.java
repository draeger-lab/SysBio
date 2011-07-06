/*
 * $Id:  ScientificNumberRenderer.java 22:11:59 wrzodek $
 * $URL: ScientificNumberRenderer.java $
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2011 by the University of Tuebingen, Germany.
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

import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.table.DefaultTableCellRenderer;

/**
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class ScientificNumberRenderer extends DefaultTableCellRenderer {
  private static final long serialVersionUID = 6924185889211247242L;
  
  /**
   * The actual formatter
   */
  NumberFormat formatter;
  
  /**
   * A boundary that defines when to use the formatter
   */
  double boundary;
  
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
    if (formatter == null) {
      formatter = getScientificNumberFormat();
    }
    
    // Format number
    String text = value.toString();
    try {
      if (value instanceof Number) {
        double d = Math.abs(((Number)value).doubleValue());
        if (d>=boundary || d<=1/boundary) {
          text = (value == null) ? "" : formatter.format(value);
        }
      }
    } catch (Exception e) {
      
    }
    
    // Ausnahmen
    if (text.endsWith("E0")) text = text.substring(0, text.length()-2);
    
    setText(text);
  }

  /**
   * @return new DecimalFormat("0.###E0");
   */
  public static NumberFormat getScientificNumberFormat() {
    return new DecimalFormat("0.###E0");
  }
  
}
