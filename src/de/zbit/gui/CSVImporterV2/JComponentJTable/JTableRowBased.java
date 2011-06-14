/*
 * $Id$ $URL$
 * --------------------------------------------------------------------- This
 * file is part of the SysBio API library.
 * 
 * Copyright (C) 2011 by the University of Tuebingen, Germany.
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation. A copy of the license agreement is provided in the file
 * named "LICENSE.txt" included with this software distribution and also
 * available online as <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.gui.CSVImporterV2.JComponentJTable;

import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import de.zbit.util.ValuePair;

/**
 * The default {@link JTable} is column based. I.e., it contains methods like
 * {@link JTable#getColumnModel()}.
 * <p>
 * This class is a row based extension for {@link JTable}s, that provides
 * methods like {@link #getRowEditorModel()}.
 * Furthermore, methods are provided that allow to set {@link TableCellEditor}s
 * for each cell separately
 * 
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class JTableRowBased extends JTable {
  private static final long serialVersionUID = 5852443694428261560L;

  /**
   * Row-specific {@link TableCellEditor}s.
   */
  protected RowEditorModel rm;
  
  /**
   * Cell-specific {@link TableCellEditor}s (ValuePair of row and col).
   * (Override row and col editors).
   */
  private Hashtable<ValuePair<Integer, Integer>, TableCellEditor> cellEditors = new Hashtable<ValuePair<Integer, Integer>, TableCellEditor>();
  
  public JTableRowBased() {
    super();
    rm = null;
  }
  
  public JTableRowBased(TableModel tm) {
    super(tm);
    rm = null;
  }
  
  public JTableRowBased(TableModel tm, TableColumnModel cm) {
    super(tm, cm);
    rm = null;
  }
  
  public JTableRowBased(TableModel tm, TableColumnModel cm,
    ListSelectionModel sm) {
    super(tm, cm, sm);
    rm = null;
  }
  
  public JTableRowBased(int rows, int cols) {
    super(rows, cols);
    rm = null;
  }
  
  public JTableRowBased(final Vector rowData, final Vector columnNames) {
    super(rowData, columnNames);
    rm = null;
  }
  
  public JTableRowBased(final Object[][] rowData, final Object[] colNames) {
    super(rowData, colNames);
    rm = null;
  }
  
  // new constructor
  public JTableRowBased(TableModel tm, RowEditorModel rm) {
    super(tm, null, null);
    this.rm = rm;
  }
  
  public void setRowEditorModel(RowEditorModel rm) {
    this.rm = rm;
  }
  
  public RowEditorModel getRowEditorModel() {
    return rm;
  }
  
  public void setCellEditor(int row, int col, TableCellEditor editor) {
    cellEditors.put(new ValuePair<Integer, Integer>(row, col), editor);
  }
  
  public TableCellEditor getCellEditor(int row, int col) {
    TableCellEditor tmpEditor = cellEditors.get(new ValuePair<Integer, Integer>(row, col));
    
    if (tmpEditor==null && rm != null) tmpEditor = rm.getEditor(row);
    if (tmpEditor != null) return tmpEditor;
    return super.getCellEditor(row, col);
  }
  
}
