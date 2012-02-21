/*
 * $Id:  ColorChooserWithPreview.java 16:13:55 wrzodek $
 * $URL: ColorChooserWithPreview.java $
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2012 by the University of Tuebingen, Germany.
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.zbit.gui.prefs.PreferencesPanel;
import de.zbit.util.StringUtil;

/**
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class ColorChooserWithPreview extends JLabel {
  private static final long serialVersionUID = -3229159450765766302L;
  
  /**
   * A list of {@link ChangeListener}s to be notified in case that values change
   * on this {@link PreferencesPanel}.
   */
  private List<ChangeListener> changeListeners;
  
  
  public ColorChooserWithPreview() {
    this(Color.WHITE);
  }
  
  public ColorChooserWithPreview(Color initialColor) {
    super();
    changeListeners = new LinkedList<ChangeListener>();
    setColor(initialColor);
    //setBorder(BorderFactory.createLineBorder(Color.BLACK));
    setBorder(new LineBorder(Color.BLACK, 1, true));
    setDisplayedMnemonicIndex(-1); // Hide the blinking mnemonic
    setPreferredSize(new java.awt.Dimension(15,15));
    
    addCommonColorChangeListeners();
  }
  
  /**
   * @param initialColor
   */
  public void setColor(Color initialColor) {
    setBackground(initialColor);
    setForeground(initialColor);
    super.setOpaque(true);
  }
  
  /* (non-Javadoc)
   * @see javax.swing.JComponent#setOpaque(boolean)
   */
  @Override
  public void setOpaque(boolean isOpaque) {
    // Ignore!
  }

  /**
   * 
   */
  private void addCommonColorChangeListeners() {
    addMouseListener(new MouseListener() {
      /*
       * (non-Javadoc)
       * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
       */
      public void mouseReleased(MouseEvent e) {}
      /*
       * (non-Javadoc)
       * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
       */
      public void mousePressed(MouseEvent e) {}
      /*
       * (non-Javadoc)
       * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
       */
      public void mouseExited(MouseEvent e) {}
      /*
       * (non-Javadoc)
       * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
       */
      public void mouseEntered(MouseEvent e) {}
      /*
       * (non-Javadoc)
       * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
       */
      public void mouseClicked(MouseEvent e) {
        showJColorChooser();
        //e.consume();
      }
    });
    
    addKeyListener(new KeyListener() {
      /*
       * (non-Javadoc)
       * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
       */
      public void keyTyped(KeyEvent e) {
        // Enter/ Confirm keys
        if (e.getKeyChar()=='\n' || e.getKeyCode()==13 || e.getKeyCode()==10 || e.getKeyCode()==16777296) { // letztes = Keypad Enter
          showJColorChooser();
        } else {
          // Do not allow typing in this label
          e.consume();
        }
      }
      
      /*
       * (non-Javadoc)
       * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
       */
      public void keyReleased(KeyEvent e) {}
      
      /*
       * (non-Javadoc)
       * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
       */
      public void keyPressed(KeyEvent e) {}
    });
  }
  
  
  public void addChangeListener(ChangeListener listener) {
    changeListeners.add(listener);
  }

  public Color showJColorChooser() {
    String title = getToolTipText();
    if (title==null || title.length()<1)
      title = getName();
    if (title==null || title.length()<1)
      title = getText();
    if (title==null || title.length()<1)
      title = "Please choose a color.";
    title = StringUtil.removeXML(title);
    
    Color col = JColorChooser.showDialog(this, title, getBackground());
    if (col!=null) {
      setBackground(col);
      ChangeEvent e = new ChangeEvent(this);
      for (ChangeListener c: changeListeners) {
        c.stateChanged(e);
      }
    }
    return col;
  }

  /**
   * @return
   */
  public Object getColor() {
    return getBackground();
  }
  
  /* (non-Javadoc)
   * @see javax.swing.JComponent#getPreferredSize()
   */
  @Override
  public Dimension getPreferredSize() {
    Dimension d = super.getPreferredSize();
    // at least a square. No width less than square is allowed.
    d.width=Math.max(d.width, d.height);
    return d;
  }
  
}
