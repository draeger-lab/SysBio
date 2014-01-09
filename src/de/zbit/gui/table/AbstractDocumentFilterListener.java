/* $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2011-2014 by the University of Tuebingen, Germany.
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

import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.JTextComponent;

/**
 * A helpful class to filter the content of a {@link TableModel}. Example usage:
 * 
 * <pre>
 * MyTableModel tableModel = ... // MyTableModel extends AbstractTableModel;
 * JTable table = new JTable(tableModel);
 * table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 * TableRowSorter<MyTableModel> sorter = new TableRowSorter<MyTableModel>(tableModel);
 * table.setRowSorter(sorter);
 * JTextField filterText = new JTextField(40);
 * // Whenever filterText changes, invoke newFilter:
 * filterText.getDocument().addDocumentListener(new DocumentFilterListener<MyTableModel>(filterText, sorter));
 * // DocumentFilterListener is your filter class that extends AbstractDocumentFilterListener.
 * // Don't forget to add text field and table to your GUI.
 * </pre>
 * 
 * @author Andreas Dr&auml;ger
 * @since 1.1
 * @version $Rev$
 */
public abstract class AbstractDocumentFilterListener<M extends TableModel> implements
		DocumentListener {
	
	/**
	 * A sort mechanism for the rows of the table of interest.
	 */
	private TableRowSorter<M> sorter;
	/**
	 * The element in which the user can tye some search text. 
	 */
	private JTextComponent tf;
	
	/**
	 * 
	 * @param <T>
	 * @param tf
	 * @param sorter
	 */
	public AbstractDocumentFilterListener(JTextComponent tf, TableRowSorter<M> sorter) {
		super();
		this.tf = tf;
		this.sorter = sorter;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
	 */
	public void changedUpdate(DocumentEvent arg0) {
		filter();
	}
	
	/**
	 * An example use case:
	 * 
	 * <pre>
	 * // here, 1 is the index of the column to be queried:
	 * return RowFilter.regexFilter(text, 1);
	 * </pre>
	 * 
	 * @param text
	 * @return A {@link RowFilter} for the {@link TableModel} of interest.
	 */
	protected abstract RowFilter<M, Object> createFilter(String text) throws Exception;
	
  /**
   * Update the row filter regular expression from the expression in
   * the text box.
   */
  private void filter() {
      RowFilter<M, Object> rf = null;
      // If current expression doesn't parse, don't update.
      try {
      	rf = createFilter(tf.getText());
      } catch (Exception exc) {
      	return;
      }
      sorter.setRowFilter(rf);
  }

	/* (non-Javadoc)
	 * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
	 */
	public void insertUpdate(DocumentEvent arg0) {
		filter();
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
	 */
	public void removeUpdate(DocumentEvent arg0) {
		filter();
	}
	
}
