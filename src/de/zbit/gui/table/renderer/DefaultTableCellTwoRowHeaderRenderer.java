/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2014 by the University of Tuebingen, Germany.
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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Set;
import java.util.logging.Logger;

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
  public static final transient Logger log = Logger.getLogger(DefaultTableCellTwoRowHeaderRenderer.class.getName());
  
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
    
    if (pos > 0) {
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
  
  /* (non-Javadoc)
   * @see javax.swing.JComponent#getPreferredSize()
   */
  @Override
  public Dimension getPreferredSize() {
    /* PROBLEM: we CANNOT set the preferred width of this renderer,
     * because it get's out of sync with the content upon resize (java bug).
     * HOWEVER, we cannot simply set a preferred height from outside
     * this method. Thus, return a modified preferred height here.
     * BECAUASE: we MUST change the height in order ot make two lines
     * visible here.
     */
    Dimension pref = super.getPreferredSize();
    pref.height = (int) (pref.height * 2.3);
    return pref;
  }
  
  /* (non-Javadoc)
   * @see javax.swing.JComponent#setPreferredSize(java.awt.Dimension)
   */
  @Override
  public void setPreferredSize(Dimension preferredSize) {
    super.setPreferredSize(preferredSize);
    log.warning("You changed the preferred size of the header column. If you are not setting the same preferred size on the content, you will see strange effects when resizing column! This is a serious warning, you should better avoid setting a preferred size.");
  }
  
}
