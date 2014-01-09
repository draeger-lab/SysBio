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
package de.zbit.gui.table;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.StringTokenizer;

import javax.swing.JTable;

import de.zbit.gui.SystemBrowser;

/**
 * This class reacts to {@link MouseEvent}s in a {@link JTable} and tries to
 * find an hyperlink (mailto or URL) inside of the text within the table element
 * at which the user has clicked. However, it is very simple at the moment. It
 * does not really look, which part of the text has been clicked, but uses the
 * first such link it can find and follows it. It also does not care about the
 * format in which the text in the table is saved.
 * 
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 */
public class JTableHyperlinkMouseListener implements MouseListener {
  
  /* (non-Javadoc)
   * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseReleased(MouseEvent e) {
    JTable table = (JTable) e.getSource();
    int row = table.rowAtPoint(e.getPoint());
    int col = table.columnAtPoint(e.getPoint());
    if (table.getValueAt(row, col) != null) {
      String text = table.getValueAt(row, col).toString();
      if (text.contains("mailto:")) {
        text = text.substring(text.indexOf("mailto:"));
        SystemBrowser.openURL(text.substring(0, text.indexOf('"')));
      } else if (text.contains("http")) {
        text = text.substring(text.indexOf("http"));
        StringTokenizer st = new StringTokenizer(text);
        SystemBrowser.openURL(st.nextToken());
      }
    }
  }
 
  /* (non-Javadoc)
   * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
   */
  @Override
  public void mousePressed(MouseEvent e) {
  }
  
  /* (non-Javadoc)
   * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseExited(MouseEvent e) {
  }
  
  /* (non-Javadoc)
   * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseEntered(MouseEvent e) {
  }
  
  /* (non-Javadoc)
   * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseClicked(MouseEvent e) {
  }
  
}
