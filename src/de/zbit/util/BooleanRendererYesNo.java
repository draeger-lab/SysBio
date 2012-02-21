/*
 * $Id:  BooleanRendererYesNo.java 17:50:08 wrzodek $
 * $URL: BooleanRendererYesNo.java $
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

import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 * A {@link TableCellRenderer} that can be used to render {@link Boolean}s
 * as <code>Yes</code> or <code>No</code>.
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class BooleanRendererYesNo extends DefaultTableCellRenderer {
  private static final long serialVersionUID = 5163967887075246109L;
  
  
  public BooleanRendererYesNo() { super(); }
  
  
  /* (non-Javadoc)
   * @see javax.swing.table.DefaultTableCellRenderer#setValue(java.lang.Object)
   */
  @Override
  protected void setValue(Object value) {
    String text;
    if (value instanceof Boolean) {
      if ((Boolean)value) {
        text = UIManager.get("OptionPane.yesButtonText").toString();
      } else {
        text = UIManager.get("OptionPane.noButtonText").toString();
      }
    } else {
      text = value.toString();
    }
    
    setText(text);
  }
}
