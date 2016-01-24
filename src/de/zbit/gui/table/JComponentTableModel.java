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
package de.zbit.gui.table;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.table.AbstractTableModel;

/**
 * A {@link AbstractTableModel} that renders {@link JComponent}s inside a
 * table, instead of calling the toString() method of the respective object. 
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class JComponentTableModel extends AbstractTableModel {
  private static final long serialVersionUID = -7992860290767020097L;
  
  private Object[][] rowData;
  private Object[] columnNames;
  
  public JComponentTableModel(final Object[][] rowData, final Object[] columnNames) {
    super();
    this.rowData = rowData;
    this.columnNames = columnNames;
  }
  
  /* (non-Javadoc)
   * @see javax.swing.table.AbstractTableModel#getColumnName(int)
   */
  @Override
  public String getColumnName(int column) {
    return columnNames[column].toString();
  }

  /* (non-Javadoc)
   * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
   */
  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return (getValueAt(rowIndex, columnIndex) instanceof Component);
  }

  /* (non-Javadoc)
   * @see javax.swing.table.TableModel#getColumnCount()
   */
  public int getColumnCount() {
    return columnNames.length;
  }

  /* (non-Javadoc)
   * @see javax.swing.table.TableModel#getRowCount()
   */
  public int getRowCount() {
    return rowData.length;
  }

  /* (non-Javadoc)
   * @see javax.swing.table.TableModel#getValueAt(int, int)
   */
  public Object getValueAt(int rowIndex, int columnIndex) {
    return rowData[rowIndex][columnIndex];
  }
  
  
}
