/*
 * $Id:  JTableTools.java 15:20:16 wrzodek $
 * $URL: JTableTools.java $
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
package de.zbit.util;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class JTableTools {

  /**
   * Adds search capabilities to a table.
   * Simply start typing when the focus is on a non-editable cell.
   * @param table
   */
  public static void setQuickSearch(final JTable table) {
    //  table.putClientProperty("JTable.autoStartsEdit", Boolean.FALSE);
    final JTextField searchField = new JTextField();
    
    // Define the actual search operations
    class Search {
      private void search() {
        search(0);
      }
      private void search(int rowStart) {
        table.clearSelection();
        String text = searchField.getText();
        if (text.length() <1) { return; }
        for (int row = rowStart; row < table.getRowCount(); row++) {
          for (int col = 0; col < table.getColumnCount(); col++) {
            Object val = table.getValueAt(row, col);
            String value = val != null ? val.toString() : "";
            if (value.toLowerCase().startsWith(text.toLowerCase())) {
              table.changeSelection(row, col, false, false);
              return;
            }
          }
        }
      }
    }
    
    // Add the listeners that active the search
    table.addKeyListener(new KeyAdapter() {
      boolean isDialogVisible = false;
      
      @Override
      public void keyPressed(final KeyEvent evt) {
        if (evt.getKeyCode() == 114) { //F3 => Search next
          if (isDialogVisible) {
            final Search s = new Search();
            s.search(table.getSelectedRow()+1);
          } else {
            showSearchDialog();
          }
          return;
        } else if (evt.getKeyCode() == 70 && evt.getModifiers()==2) {
          // STRG + F
          if (!isDialogVisible) {
            showSearchDialog();
          }
          return;
        }
        
        char ch = evt.getKeyChar(); 
        if (!Character.isLetterOrDigit(ch)) { return; }
        int selectedRow = table.getSelectedRow();
        int selectedColumn = table.getSelectedColumn();
        Object clientProperty = table.getClientProperty("JTable.autoStartsEdit");
        if ((clientProperty == null || (Boolean) clientProperty)
            && selectedRow >= 0
            && selectedColumn >= 0
            && table.isCellEditable(table.getSelectedRow(),
              table.getSelectedColumn())) { return; }
        searchField.setText(String.valueOf(ch));
        
        // Create a dialog looking like a search field at the tables header
        showSearchDialog();
      }

      /**
       * Create a dialog looking like a search field at the tables header
       * @param table table to search
       * @param searchField searchField
       */
      private void showSearchDialog() {
        final Search s = new Search();
        s.search();
        
        final JDialog d = new JDialog();
        d.setUndecorated(true);
        d.setSize(150, 20);
        d.setLocation(table.getTableHeader().getLocationOnScreen());
        final JLabel lb = new JLabel("Search: ");
        d.add(lb, BorderLayout.LINE_START);
        d.add(searchField);
        /*JButton next = new JButton("Find Next");
        next.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            s.search(table.getSelectedRow()+1);
          }
        });
        d.add(next);*/
        d.setVisible(true);
        isDialogVisible = true;
        searchField.getDocument().addDocumentListener(new DocumentListener() {
          public void insertUpdate(final DocumentEvent e) {
            s.search();
          }
          public void removeUpdate(final DocumentEvent e) {
            s.search();
          }
          public void changedUpdate(final DocumentEvent e) {
            s.search();
          }
        });
        searchField.addKeyListener(new KeyAdapter() {
          /* (non-Javadoc)
           * @see java.awt.event.KeyAdapter#keyPressed(java.awt.event.KeyEvent)
           */
          @Override
          public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == 114 || e.getKeyCode() == 13) { //F3 / Enter => Search next
              if (isDialogVisible) {
                final Search s = new Search();
                s.search(table.getSelectedRow()+1);
              } else {
                showSearchDialog();
              }
              e.consume();
            }
          }
        });
        searchField.addFocusListener(new FocusListener() {
          
          @Override
          public void focusGained(final FocusEvent e) {
          }
          
          @Override
          public void focusLost(final FocusEvent e) {
            isDialogVisible=false;
            d.dispose();
          }
        });
        Action exit = new AbstractAction() {
          private static final long serialVersionUID = -5554144842629942687L;
          @Override
          public void actionPerformed(final ActionEvent e) {
            isDialogVisible=false;
            d.dispose();
          }
        };
        // Close on exit or escape
        searchField.setAction(exit);
        searchField.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
          KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "exit");
        searchField.getActionMap().put("exit", exit);
      }
    });
  }
  
}
