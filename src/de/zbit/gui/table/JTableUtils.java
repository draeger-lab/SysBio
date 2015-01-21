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
package de.zbit.gui.table;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.AbstractButton;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

/**
 * This class contains Utilities (e.g. renderers) for JTables.
 * 
 * <p>
 * Usage example:
 * <pre>
 *    JTableUtils tableUtils = new JTableUtils();
 *    table.getColumn("Button").setCellRenderer(tableUtils.new ButtonRenderer());
 *    table.getColumn("Button").setCellEditor  (tableUtils.new ButtonEditor(new JCheckBox()));
 * </pre>
 * </p> 
 * 
 * @author Clemens Wrzodek
 * @version $Rev$
 * @since 1.0
 */
public class JTableUtils implements Serializable {
  private static final long serialVersionUID = 3645923028183309376L;
  
  
  /**
   * Set as Column renderer on JButton columns in tables to display them
   * properly.
   * @author Clemens Wrzodek
   */
  public class ButtonRenderer extends JButton implements TableCellRenderer {
    private static final long serialVersionUID = 3907152523374459684L;

    public ButtonRenderer() {
      setOpaque(true);
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
     */
    public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column) {
      if (isSelected) {
        setForeground(table.getSelectionForeground());
        setBackground(table.getSelectionBackground());
      } else {
        setForeground(table.getForeground());
        setBackground(UIManager.getColor("Button.background"));
      }
      
      String text = "";
      if (value==null) {
        text = "";
      } else if (value instanceof AbstractButton) {
        text =((AbstractButton)value).getText();
        //setPreferredSize(((AbstractButton)value).getPreferredSize());
      } else
        text =  value.toString();
      
      setText(text);
      return this;
    }
  }
  
    
  /**
   * Set as CellEditor on JButton columns in tables to edit them
   * properly.
   * @author Clemens Wrzodek
   */
  public class ButtonEditor extends DefaultCellEditor {
    private static final long serialVersionUID = 3746708554280012094L;

    protected AbstractButton button;

    private String label;

    private boolean isPushed;

    public ButtonEditor(JCheckBox checkBox) {
      super(checkBox);
      button = new JButton();
      button.setOpaque(true);
      button.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          fireEditingStopped();
        }
      });
    }

    /* (non-Javadoc)
     * @see javax.swing.DefaultCellEditor#getTableCellEditorComponent(javax.swing.JTable, java.lang.Object, boolean, int, int)
     */
    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
        boolean isSelected, int row, int column) {
      if (isSelected) {
        button.setForeground(table.getSelectionForeground());
        button.setBackground(table.getSelectionBackground());
      } else {
        button.setForeground(table.getForeground());
        button.setBackground(table.getBackground());
      }
      
      if (value==null) {
        label="";
      } else if (value instanceof AbstractButton) {
        label =((AbstractButton)value).getText();
      } else
        label =  value.toString();
      
      
      button.setText(label);
      isPushed = true;
      
      if (value!=null && value instanceof AbstractButton) {
        button = (AbstractButton) value;
      }
      
      return button;
    }

    /* (non-Javadoc)
     * @see javax.swing.DefaultCellEditor#getCellEditorValue()
     */
    @Override
    public Object getCellEditorValue() {
      if (isPushed) {
        for (ActionListener i : button.getActionListeners()) {
          i.actionPerformed(new ActionEvent(null,0,""));
        }
      }
      isPushed = false;
      return new String(label);
    }

    /* (non-Javadoc)
     * @see javax.swing.DefaultCellEditor#stopCellEditing()
     */
    @Override
    public boolean stopCellEditing() {
      isPushed = false;
      return super.stopCellEditing();
    }

    /* (non-Javadoc)
     * @see javax.swing.AbstractCellEditor#fireEditingStopped()
     */
    @Override
    protected void fireEditingStopped() {
      super.fireEditingStopped();
    }
  }
  
  
  public class PartiallyEditableTableModel extends DefaultTableModel {
    private static final long serialVersionUID = 937812169660694417L;
    
    private ArrayList<Integer> editableCols = new ArrayList<Integer>();
    
    public PartiallyEditableTableModel() {
      super();
    }
    public PartiallyEditableTableModel(int editableColumn) {
      this();
      setColEditable(editableColumn, true);
    }
    
    /* (non-Javadoc)
     * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
     */
    @Override
    public Class<?> getColumnClass(int column) {
      Class<?> returnValue;
      if ((column >= 0) && (column < getColumnCount())) {
        returnValue = getValueAt(0, column).getClass();
      } else {
        returnValue = Object.class;
      }
      return returnValue;
    }

    
    /**
     * Set wether a column is editable or not.
     * @param col
     * @param isEditable
     */
    public void setColEditable(int col, boolean isEditable) {
      int pos = editableCols.indexOf(col);
      
      if (isEditable) {
        if (pos<0) editableCols.add(col);
      } else {
        if (pos>=0) editableCols.remove(pos);
      }
    }
    
    
    /* (non-Javadoc)
     * @see javax.swing.table.DefaultTableModel#isCellEditable(int, int)
     */
    @Override
    public boolean isCellEditable(int row, int column) {
      return editableCols.contains(column);
    }
    
  }


  /**
   * Resize columns to a reasonable width.
   * 
   * @param table
   * @param minWidth each column, with a width lower than
   * this value will get this value as minimum column width
   * assigned.
   */
  public static void resizeColumns(JTable table, int minWidth) {
  	if (table.getColumnModel().getColumnCount() > 0) {
  		int width = table.getColumnModel().getColumn(0).getWidth();
  		width = Math.max(width, minWidth);
  		for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
  			table.getColumnModel().getColumn(i).setPreferredWidth(width);
  		}
  	}
  }
  
}
