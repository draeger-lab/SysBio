/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2013 by the University of Tuebingen, Germany.
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

import java.util.Hashtable;

import javax.swing.table.TableCellEditor;

/**
 * A simple holder class, that holds {@link TableCellEditor}s
 * for multiple rows.
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class RowEditorModel {
  private Hashtable<Integer, TableCellEditor> data;
  
  public RowEditorModel() {
    data = new Hashtable<Integer, TableCellEditor>();
  }
  
  public void addEditorForRow(int row, TableCellEditor e) {
    data.put(new Integer(row), e);
  }
  
  public void removeEditorForRow(int row) {
    data.remove(new Integer(row));
  }
  
  public TableCellEditor getEditor(int row) {
    return (TableCellEditor) data.get(new Integer(row));
  }
}
