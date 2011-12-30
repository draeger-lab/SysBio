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
package de.zbit.gui;

import java.awt.Component;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.logging.Logger;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import de.zbit.util.Reflect;
import de.zbit.util.StringUtil;
import de.zbit.util.prefs.KeyProvider;

/**
 * A {@link ComboBoxModel} that displays the names and ToolTips
 * of {@link ActionCommand}s.
 * <p>It furthermore displays {@link Component} directly
 * as components and does not generate a {@link JLabel} with
 * the {@link Component#toString()} method.
 * <p>Classes are displayed with {@link Class#getSimpleName()}.
 * <p>Icons of {@link ActionCommandWithIcon} are also displayed.
 * 
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class ActionCommandRenderer extends JLabel implements ListCellRenderer,
    TableCellRenderer, Serializable {
  
  /**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = 6825133145583461124L;
  
  /**
   * A {@link Logger} for this class.
   */
  private static final transient Logger logger = Logger
      .getLogger(ActionCommandRenderer.class.getName());
  
  /**
   * If this is <code>TRUE</code>, each <code>value</code> of type
   * {@link Class} will get {@link Class#getName()} as ToolTip.
   */
  public static boolean setToolTipToFullClassNameForClasses=true;
  
  /**
   * Initialize when required.
   */
  private TableCellRenderer defaultTableRenderer = null;
  
  /**
   * Initialize when required.
   */
  private ListCellRenderer defaultListRenderer = null;
  
  /**
   * If true tooltips are displayed as HTML-entities.
   */
  private boolean showAsHTML = false;
  
  /**
   * 
   */
  public ActionCommandRenderer() {
    this(false);
  }
  
  /**
   * @param showAsHTML will show all tooltips as HTML entities.
   * May be slower than native text!
   */
  public ActionCommandRenderer(boolean showAsHTML) {
    this.showAsHTML = showAsHTML;
  }
  
  
  /* (non-Javadoc)
   * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
   */
  public Component getTableCellRendererComponent(JTable table, Object value,
    boolean isSelected, boolean hasFocus, int row, int column) {
    
    // Get properties
    String label = value.toString();
    String toolTip = null;
    Component c = null;
    Icon icon = null;
    if (value instanceof Component) {
      c = (Component) value;
    } else if (value instanceof ActionCommand) {
      label = ((ActionCommand)value).getName();
      toolTip = ((ActionCommand)value).getToolTip();
      if (value instanceof ActionCommandWithIcon) {
        icon = (((ActionCommandWithIcon)value).getIcon());
      }
    } else if (value instanceof Class<?>) {
      label = ((Class<?>) value).getSimpleName();
      if (setToolTipToFullClassNameForClasses) {
        toolTip = ((Class<?>) value).getName();
      }
    }
    
    // Generate component
    if (c == null) {
      if (defaultTableRenderer == null) {
        defaultTableRenderer = new DefaultTableCellRenderer();
      }
      c = defaultTableRenderer.getTableCellRendererComponent(table, label, isSelected, hasFocus, row, column);
    }
    
    // Set ToolTip
    if (toolTip!=null && toolTip.length()>0 && (c instanceof JComponent)) {
      if (showAsHTML) {
        ((JComponent) c).setToolTipText(StringUtil.toHTML(toolTip, GUITools.TOOLTIP_LINE_LENGTH));
      } else {
        ((JComponent) c).setToolTipText(toolTip);
      }
    } else {
      ((JComponent) c).setToolTipText(null);
    }
    
    // Set Icon
    // Unfortunately, there is no interface for "setIcon".
    // Another approach would be, to expect c to be a JLabel.
    Method iconMethod = null;
    try {
      iconMethod = Reflect.getMethod(c, "setIcon", Icon.class);
    } catch (Exception exc) {
      logger.finest(exc.getLocalizedMessage());
    }
    if (iconMethod!=null) {
      try {
        iconMethod.invoke(c, icon);
      } catch (Exception exc) {
        logger.finest(exc.getLocalizedMessage());
      }
    } 
    
    return c;
  }
  
  /* (non-Javadoc)
   * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
   */
  public Component getListCellRendererComponent(JList list, Object value,
    int index, boolean isSelected, boolean cellHasFocus) {
    
    // Get properties
    String label = value.toString();
    String toolTip = null;
    Icon icon = null;
    Component c = null;
    if (value instanceof Component) {
      c = (Component) value;
    } else if (value instanceof ActionCommand) {
      label = ((ActionCommand) value).getName();
      toolTip = ((ActionCommand) value).getToolTip();
      if (value instanceof ActionCommandWithIcon) {
        icon = (((ActionCommandWithIcon) value).getIcon());
      }
    } else if (value instanceof Class<?>) {
      label = KeyProvider.Tools.createTitle((Class<?>) value);
      if (setToolTipToFullClassNameForClasses) {
        toolTip = ((Class<?>) value).getName();
      }
    }
    
    
    // Generate component
    if (c == null) {
      /*
       * Get the systems default renderer.
       */
      if (defaultListRenderer == null) {
        // new DefaultListCellRenderer(); Is not necessarily the default!
        // even UIManager.get("List.cellRenderer"); returns a different value!
        try {
          defaultListRenderer = new JComboBox().getRenderer();
          if (defaultListRenderer == null) {
            defaultListRenderer = (ListCellRenderer) UIManager.get("List.cellRenderer");
          }
        } catch (Throwable exc){
          logger.warning(exc.getLocalizedMessage());
        }
        if (defaultListRenderer == null) {
          defaultListRenderer = new DefaultListCellRenderer();
        }
      }
      //-------------------
      
      c = defaultListRenderer.getListCellRendererComponent(list, label, index, isSelected, cellHasFocus);
    }
        
    // Set ToolTip
    if ((toolTip != null) && (toolTip.length() > 0) && (c instanceof JComponent)) {
      if (showAsHTML) {
        ((JComponent) c).setToolTipText(StringUtil.toHTML(toolTip, GUITools.TOOLTIP_LINE_LENGTH));
      } else {
        ((JComponent) c).setToolTipText(toolTip);
      }
    } else {
      ((JComponent) c).setToolTipText(null);
    }
    
    // Set Icon
    // Unfortunately, there is no interface for "setIcon".
    // Another approach would be, to expect c to be a JLabel.
    Method iconMethod = null;
    try {
      iconMethod = Reflect.getMethod(c, "setIcon", Icon.class);
    } catch (Exception exc) {
      logger.finest(exc.getLocalizedMessage());
    }
    if (iconMethod != null) {
      try {
        iconMethod.invoke(c, icon);
      } catch (Exception exc) {
        logger.finest(exc.getLocalizedMessage());
      }
    } 
    
    return c;
  }  

}
