/*
 * $Id:  ActionCommandComboBoxModel.java 16:18:36 wrzodek $
 * $URL: ActionCommandComboBoxModel.java $
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
package de.zbit.gui;

import java.awt.Component;
import java.io.Serializable;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 * A {@link ComboBoxModel} that displays the names and tooltips
 * of {@link ActionCommand}s.
 * <p>It furthermore displays {@link Component} directly
 * as components and does not generate a {@link JLabel} with
 * the {@link Component#toString()} method.
 * 
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class ActionCommandRenderer extends JLabel implements ListCellRenderer, TableCellRenderer, Serializable {
  private static final long serialVersionUID = 6825133145583461124L;

  /**
   * Initialize when required.
   */
  private DefaultTableCellRenderer defaultTableRenderer = null;
  
  /**
   * Initialize when required.
   */
  private DefaultListCellRenderer defaultListRenderer = null;
  

  /* (non-Javadoc)
   * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
   */
  @Override
  public Component getTableCellRendererComponent(JTable table, Object value,
    boolean isSelected, boolean hasFocus, int row, int column) {
    if (defaultTableRenderer==null) {
      defaultTableRenderer = new DefaultTableCellRenderer();
    }
    
    // Get properties
    String label = value.toString();
    String toolTip = null;
    Component c = null;
    if (value instanceof Component) {
      c = (Component) value;
    } else if (value instanceof ActionCommand) {
     label = ((ActionCommand)value).getName();
     toolTip = ((ActionCommand)value).getToolTip();
    }
    
    // Generate component
    if (c==null) {
      c = defaultTableRenderer.getTableCellRendererComponent(table, label, isSelected, hasFocus, row, column);
    }
    if (toolTip!=null && toolTip.length()>0 && (c instanceof JComponent)) {
      ((JComponent)c).setToolTipText(toolTip);
    }
    
    return c;
  }

  /* (non-Javadoc)
   * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
   */
  @Override
  public Component getListCellRendererComponent(JList list, Object value,
    int index, boolean isSelected, boolean cellHasFocus) {
    if (defaultListRenderer==null) {
      defaultListRenderer = new DefaultListCellRenderer();
    }
    
    // Get properties
    String label = value.toString();
    String toolTip = null;
    Component c = null;
    if (value instanceof Component) {
      c = (Component) value;
    } else if (value instanceof ActionCommand) {
     label = ((ActionCommand)value).getName();
     toolTip = ((ActionCommand)value).getToolTip();
    }
    
    // Generate component
    if (c==null) {
      c = defaultListRenderer.getListCellRendererComponent(list, label, index, isSelected, cellHasFocus);
    }
    if (toolTip!=null && toolTip.length()>0 && (c instanceof JComponent)) {
      ((JComponent)c).setToolTipText(toolTip);
    }
    
    return c;
  }
  
  
  
  
}
