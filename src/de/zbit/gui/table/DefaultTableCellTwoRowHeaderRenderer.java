/*
 * $Id$
 * $URL$
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
package de.zbit.gui.table;

import java.awt.Component;
import java.awt.GridLayout;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;

import sun.swing.table.DefaultTableCellHeaderRenderer;

/**
 * An implementation for a header with two rows.
 * The new line character '\n' splits the first and second rows.
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class DefaultTableCellTwoRowHeaderRenderer extends DefaultTableCellHeaderRenderer {
  private static final long serialVersionUID = 4654088278313349629L;
  
  /**
   * Column indices, whose left border should be painted bold.
   */
  Set<Integer> boldBorders;
  
  public DefaultTableCellTwoRowHeaderRenderer() {
    this(null);
  }
  
  /**
   * @param boldBorders currently ignored.
   */
  public DefaultTableCellTwoRowHeaderRenderer(Set<Integer> boldBorders) {
    super();
    this.boldBorders = boldBorders;
  }

  /* (non-Javadoc)
   * @see sun.swing.table.DefaultTableCellHeaderRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
   */
  @Override
  public Component getTableCellRendererComponent(JTable table, Object value,boolean isSelected,boolean hasFocus,int row,int column) {
    /* XXX: This could be improved by manually setting a (matte) border
     * that makes, e.g., three cells look like one big ([ | TEXT | ]).
     */
    String v = value.toString();
    int pos = v.indexOf('\n');
    
    if (pos>0) {
      JLabel firstLabel =  new JLabel(v.substring(0, pos), JLabel.CENTER);
      JLabel secondLabel = (JLabel) super.getTableCellRendererComponent(table, v.substring(pos+1), isSelected, hasFocus, row, column);
      firstLabel.setBorder(secondLabel.getBorder());
      firstLabel.setBackground(secondLabel.getBackground());
      firstLabel.setForeground(secondLabel.getForeground());
      
      // Eventually create a bold border
      // XXX: Offset possible when "#" col is shown.
//      if (boldBorders!=null && boldBorders.contains(column)) {
//        Border b = new MatteBorder(1, 2, 1, 1, secondLabel.getForeground());
//        firstLabel.setBorder(b);
//        secondLabel.setBorder(b);
//      } else {
//        System.out.println(column + " ::: " + boldBorders);
//      }
        
      JPanel p = new JPanel(new GridLayout(2, 1));
      p.add(firstLabel);
      p.add(secondLabel);
      return p;
    } else {
      return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    }
  }
  
  
}
