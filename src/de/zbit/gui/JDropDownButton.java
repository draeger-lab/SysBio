/*
 * $Id:  JDropDownButton.java 16:27:25 wrzodek $
 * $URL: JDropDownButton.java $
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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.accessibility.Accessible;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.UIManager;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

/**
 * A modified version of {@link JToggleButton} that contains an arrow
 * on the right side and a {@link JPopupMenu} that is displayed when
 * the button is pressed.
 * 
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class JDropDownButton extends JToggleButton implements Accessible, ActionListener, PopupMenuListener {
  private static final long serialVersionUID = -6023636214344546808L;

  @SuppressWarnings("unused")
  private static final String uiClassID = "DropDownButtonUI";
  
  /**
   * The {@link JPopupMenu} that is displayed when the button is pressed.
   */
  private JPopupMenu popUpMenu;
  
  
  /**
   * Don't immediately setSelected to false, if the {@link #popUpMenu}
   * is hidden because the actionPerformed() event is triggered after
   * this event if the popup has been hidden because this button has
   * been pressed! Thus, remember the hiding time and ignore Action events
   * occuring close to this time.
   */
  private long lastPopUpHiddenEvent = 0;
  
  /**
   * Creates an initially unselected DropDown button
   * without setting the text or image.
   */
  public JDropDownButton () {
    this(null, null, false, null);
  }
  
  /**
   * Creates an initially unselected DropDown button
   * with the given {@link JPopupMenu}.
   * @param menu {@link JPopupMenu} to show when the button is pressed.
   */
  public JDropDownButton (JPopupMenu menu) {
    this(menu.getLabel(), null, false, menu);
  }
  
  /**
   * Creates an initially unselected DropDown button
   * with the specified image but no text.
   * @param icon  the image that the button should display
   * @param menu {@link JPopupMenu} to show when the button is pressed.
   */
  public JDropDownButton(Icon icon, JPopupMenu menu) {
    this(menu.getLabel(), icon, false, menu);
  }
  
  /**
   * Creates an unselected DropDown button with the specified text.
   * @param text  the string displayed on the DropDown button
   * @param menu {@link JPopupMenu} to show when the button is pressed.
   */
  public JDropDownButton (String text, JPopupMenu menu) {
    this(text, null, false, menu);
  }
  
  /**
   * Creates a DropDown button that has the specified text and image,
   * and that is initially unselected.
   * @param text the string displayed on the button
   * @param icon  the image that the button should display
   * @param menu {@link JPopupMenu} to show when the button is pressed.
   */
  public JDropDownButton(String text, Icon icon, JPopupMenu menu) {
    this(text, icon, false, menu);
  }
  
  /**
   * Creates a DropDown button with the specified text, image, and
   * selection state.
   * @param text the text of the DropDown button
   * @param icon the image that the button should display
   * @param selected  if true, the button is initially selected;
   *                  otherwise, the button is initially unselected
   * @param menu {@link JPopupMenu} to show when the button is pressed.
   */
  public JDropDownButton (String text, Icon icon, boolean selected, JPopupMenu menu) {
    super(text,icon,selected);
    setPopUpMenu(menu);
    
    addActionListener(this);
    if (selected) {
      showPopUpMenu();
    }
    super.setSize(getSize().width+50, getSize().height);
  }


  /**
   * @return the popUpMenu
   */
  public JPopupMenu getPopUpMenu() {
    return popUpMenu;
  }

  /**
   * @param popUpMenu the popUpMenu to set
   */
  public void setPopUpMenu(JPopupMenu popUpMenu) {
    this.popUpMenu = popUpMenu;
    
    // Deselect this button when the popup is being hidden.
    popUpMenu.addPopupMenuListener(this);
  }

  /* (non-Javadoc)
   * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
   */
  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    
    // Paint an arrow on the right side.
    Icon arrow;
    if (isSelected()) {
      arrow = UIManager.getIcon("Table.ascendingSortIcon");
    } else {
      arrow = UIManager.getIcon("Table.descendingSortIcon");
    }
    int x = super.getWidth()-arrow.getIconWidth()-3;
    int y = super.getHeight()/2-arrow.getIconHeight()/2;
    arrow.paintIcon(this, g, x, y);
  }
  

  private Dimension addArrowWidth(Dimension superClassSize) {
    // Extend space for the arrow icon
    Icon arrow = UIManager.getIcon("Table.ascendingSortIcon");
    superClassSize.width +=arrow.getIconWidth()+6;
    return superClassSize;
  }
  
  /* (non-Javadoc)
   * @see javax.swing.JComponent#getPreferredSize()
   */
  @Override
  public Dimension getPreferredSize() {
    return addArrowWidth(super.getPreferredSize());
  }
  
  /* (non-Javadoc)
   * @see javax.swing.JComponent#getMinimumSize()
   */
  @Override
  public Dimension getMinimumSize() {
    return addArrowWidth(super.getMinimumSize());
  }
  
  /* (non-Javadoc)
   * @see java.awt.Component#getBounds()
   */
  @Override
  public Rectangle getBounds() {
    Rectangle r = super.getBounds();
    
    Icon arrow = UIManager.getIcon("Table.ascendingSortIcon");
    r.width +=arrow.getIconWidth()+6;
    
    return r;
  }
  
  /**
   * Just for demonstration purposes.
   * @param args ignored.
   */
  public static void main(String[] args) {
    JFrame that = new JFrame();
    that.setSize(400, 250);
    that.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
    JPopupMenu popup = new JPopupMenu("test");
    popup.add(new JMenuItem("TestMenuItem"));

    that.getContentPane().add(
      new JDropDownButton("text", (Icon)UIManager.getIcon("FileChooser.homeFolderIcon"), popup),
      BorderLayout.SOUTH);
    that.setVisible(true);
  }

  /* (non-Javadoc)
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    if (popUpMenu==null) return;
    if (isSelected()) {
      if (!popUpMenu.isVisible()) {
        if (System.currentTimeMillis()-lastPopUpHiddenEvent<250) {
          // Double action. This component has been deselected by pressing it.
          setSelected(false);
          return;
        }
        
        showPopUpMenu();
      }
    } else {
      if (popUpMenu.isVisible()) {
        popUpMenu.setVisible(false);
      }
    }
  }

  private void showPopUpMenu() {
    // Fit popUpSize to Button size
    Dimension d = popUpMenu.getPreferredSize();
    popUpMenu.setPopupSize((int)Math.max(getWidth(), d.getWidth()), (int)d.getHeight());
    
    popUpMenu.show(this, 0, getHeight());
  }

  /* (non-Javadoc)
   * @see javax.swing.event.PopupMenuListener#popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent)
   */
  @Override
  public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
    // Ignore. probably made visible by other components
  }

  /* (non-Javadoc)
   * @see javax.swing.event.PopupMenuListener#popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent)
   */
  @Override
  public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
    if (isSelected()) {
      // Don't immediately setSelected to false, because the
      // actionPerformed() event is triggered after this event
      // if the popup has been hidden because this button has
      // been pressed!
      //deselect(105);
      lastPopUpHiddenEvent=System.currentTimeMillis();
      setSelected(false);
    }
  }
  
  /**
   * Deselects this button in <code>millis</code> milliseconds.
   * @param millis
   */
  public void deselect(final int millis) {
    Thread r = new Thread(){
      @Override
      public void run() {
        try {
          Thread.sleep(millis);
        } catch (InterruptedException e) {}
        setSelected(false); 
      }
    };
    r.start();
  }

  
  /* (non-Javadoc)
   * @see javax.swing.event.PopupMenuListener#popupMenuCanceled(javax.swing.event.PopupMenuEvent)
   */
  @Override
  public void popupMenuCanceled(PopupMenuEvent e) {
    // don't care
  }
  
  
}
