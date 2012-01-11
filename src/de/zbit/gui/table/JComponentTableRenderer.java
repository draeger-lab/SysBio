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

import java.awt.Color;
import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import de.zbit.gui.ColorPalette;

/**
 * A {@link TableCellRenderer} that draws {@link JComponent}s.
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class JComponentTableRenderer implements TableCellRenderer, ListCellRenderer {
  
  DefaultListCellRenderer listRenderer = null;
  DefaultTableCellRenderer tableRenderer = null;
  private Color defaultFGcolor=null;
  
  /**
   * Set the default background color to be used for {@link JLabel}s.
   * @param c
   */
  public void setDefaultForegroundColorForJLabels(Color c) {
    defaultFGcolor = c;
  }

  private void configureRenderer(JLabel renderer, Object value) {
    if (value == null) {
      renderer.setText(" ");
    } else if (value instanceof Color) {
      renderer.setText(value.toString());
      renderer.setBackground((Color) value);
    } else {
      renderer.setText(value.toString());
      if (defaultFGcolor != null) {
        renderer.setForeground(defaultFGcolor);
      }
    }
  }

  /* (non-Javadoc)
   * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
   */
  public Component getListCellRendererComponent(JList list, Object value, int index,
      boolean isSelected, boolean cellHasFocus) {
    if (value instanceof Component) return (Component) value;
    else if (listRenderer==null) listRenderer = new DefaultListCellRenderer();
    listRenderer = (DefaultListCellRenderer) listRenderer.getListCellRendererComponent(list, value,
        index, isSelected, cellHasFocus);
    configureRenderer(listRenderer, value);
    return listRenderer;
  }
  
	/**
	 * An array that allows to store the background {@link Color}s of rows. The
	 * first row will have the first background {@link Color}, the second row the
	 * second {@link Color} etc. At the end of the array's length, it will repeat
	 * again.
	 */
	private Color bg[];
	
	/**
	 * Creates a new renderer with default colors for each row, i.e., no special colors set.
	 */
	public JComponentTableRenderer() {
		super();
		this.bg = null;
	}
	
	/**
	 * @return the background colors.
	 */
	public Color[] getBackgroundColors() {
		return bg;
	}
	
	/**
	 * @param bg the background colors to set
	 */
	public void setBackgroundColors(Color... bg) {
		this.bg = bg;
	}

  /* (non-Javadoc)
   * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
   */
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
      boolean hasFocus, int row, int column) {
    if (value instanceof Component) {
    	return (Component) value;
    } else if (tableRenderer == null) {
    	tableRenderer = new DefaultTableCellRenderer();
    }
    tableRenderer = (DefaultTableCellRenderer) tableRenderer.getTableCellRendererComponent(table,
        value, isSelected, hasFocus, row, column);
    configureRenderer(tableRenderer, value);
		if ((bg != null) && (bg.length > 0)) {
			tableRenderer.setBackground(bg[row % bg.length]);
		}
    return tableRenderer;
  }
  
  
}
