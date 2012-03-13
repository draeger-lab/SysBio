/*
 * $Id:  ExpandablePanel.java 10:28:01 wrzodek $
 * $URL: ExpandablePanel.java $
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
package de.zbit.gui.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;

import de.zbit.gui.GUITools;
import de.zbit.gui.layout.LayoutHelper;

/**
 * This is a panel that may be expanded or collapsed at a
 * mouse click on a header. It controls the visibility of
 * a child {@link JPanel}, via click on a auto-generated
 * and auto-updated {@link JLabel}.
 * 
 * <p>Note:<br/>You might want to change the layout of the
 * parent container, depending on the expansion state of this
 * container. You might do this automatically by
 * {@link #setPackParentWindow(boolean)} to true or by adding
 * a {@link #addTreeExpansionListener(TreeExpansionListener)}
 * to listen for expansion changes.
 * 
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class ExpandablePanel extends JPanel implements MouseListener {
  private static final long serialVersionUID = -6328374823659047466L;

  /**
   * The header the user may click on to expand the {@link #panel}.
   */
  private JLabel header;
  
  /**
   * ToolTip to be set on the header.
   */
  private String headerToolTip = null;
  
  /**
   * Panel that is controlled (toggled) by this one.
   */
  private JPanel panel;
  
  /**
   * If true, the parent window of this panel will be packed
   * upon state change.
   */
  private boolean packParentWindow = false;
  
  /**
   * Draw the border NOT on this panel, but on the to childs
   * ({@link #header} and {@link #panel}).
   */
  private Border intendedBorder=getBorder();
  
  /**
   * a special Border for the label.
   */
  private Border labelBorder;
  
  /** A list of event listeners for this component. */
  protected EventListenerList listenerList = new EventListenerList();
  
  /**
   * Creates a new panel that may be expanded or collapsed at a
   * mouse click on a header.
   * @param label label of this {@link #ExpandablePanel(String, JPanel)}. This is
   * the text, the user may click on to expand or collapse the <code>panel</code>.
   * @param panel the panel to collapse of expand.
   */
  public ExpandablePanel(String label, JPanel panel) {
    this(label, panel, false);
  }
  /**
   * Creates a new panel that may be expanded or collapsed at a
   * mouse click on a header.
   * @param label label of this {@link #ExpandablePanel(String, JPanel)}. This is
   * the text, the user may click on to expand or collapse the <code>panel</code>.
   * @param panel the panel to collapse of expand.
   * @param collapsed set the initial state to collapsed (<code>TRUE</code>)
   * or expanded (<code>FALSE</code>).
   */
  public ExpandablePanel(String label, JPanel panel, boolean collapsed) {
    this(label, panel, collapsed, false);
  }
  /**
   * Creates a new panel that may be expanded or collapsed at a
   * mouse click on a header.
   * @param label label of this {@link #ExpandablePanel(String, JPanel)}. This is
   * the text, the user may click on to expand or collapse the <code>panel</code>.
   * @param panel the panel to collapse of expand.
   * @param collapsed set the initial state to collapsed (<code>TRUE</code>)
   * or expanded (<code>FALSE</code>).
   * @param packParentWindow invoke the <code>pack()</code> method on the
   * parent window.
   */
  public ExpandablePanel(String label, JPanel panel, boolean collapsed, boolean packParentWindow) {
    super();
    this.panel = panel;
    this.packParentWindow = packParentWindow;
    init(label, collapsed);
  }
  
  /**
   * Initialize this panel.
   * @param label
   * @param collapsed
   */
  private void init(String label, boolean collapsed) {
    if (label==null) label = createClickAdviceString(collapsed);
    header = new JLabel(label);
    header.addMouseListener(this);
    
    // Important to set visibility here (before adding the
    // component to this panel). Else, the parent
    // container will start calculating the bounds, based
    // maybe on a wrong state
    panel.setVisible(!collapsed);
    
    // Initially create a simple border
    Color lineColor = UIManager.getColor("Label.foreground");
    if (lineColor==null) lineColor = Color.BLACK;
    if(intendedBorder==null) {
      intendedBorder = BorderFactory.createLineBorder(lineColor, 2);
    }
    labelBorder = BorderFactory.createLineBorder(lineColor, 2);
    
    // Add components
    setLayout(new BorderLayout());
    add(header, BorderLayout.NORTH);
    add(panel, BorderLayout.CENTER);
    
    setState(collapsed);
  }
  
  /* (non-Javadoc)
   * @see javax.swing.JComponent#setBorder(javax.swing.border.Border)
   */
  @Override
  public void setBorder(Border border) {
    intendedBorder = border;
    update();
  }
  
  /**
   * Collapses this panel.
   */
  public void collapse() {
    setState(true);
  }

  /**
   * Expands this panel.
   */
  public void expand() {
    setState(false);
  }

  /**
   * @return true if and only if a {@link #panel} is not set
   * or currently not visible.
   */
  public boolean isCollapsed() {
    return panel==null || !panel.isVisible();
  }
  
  /**
   * @return true if and only if a {@link #panel} is set
   * and currently visible.
   */
  public boolean isExpanded() {
    return panel!=null && panel.isVisible();
  }
  
  /**
   * @return true if and only if the {@link #panel} {@link #isCollapsed()}.
   */
  public boolean getState() {
    return isCollapsed();
  }
  
  /**
   * Change the visibility of the {@link #panel}.
   * @param collapsed
   */
  public void setState(boolean collapsed) {
    if (panel==null || header==null) return; // Not yet initialized.
    
    // Set the appropriate icon
    Icon icon = null;
    if (collapsed) {
      icon = UIManager.getIcon("Tree.collapsedIcon");
    } else {
      icon = UIManager.getIcon("Tree.expandedIcon");
    }
    header.setIcon(icon);
    
    // Set the correct ToolTip
    String clickAdvice = createClickAdviceString(collapsed);
    if (headerToolTip!=null) {
      String tt = String.format("%s - %s", headerToolTip, clickAdvice);
      header.setToolTipText(tt);
    } else {
      header.setToolTipText(clickAdvice);
    }
    
    // Set the correct caption, if the initial label was null.
    if (header.getText().equals(createClickAdviceString(!collapsed))) {
      header.setText(clickAdvice);
    }
    
    // Set cursor
    header.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    
    // Change colors
    Color bgColor = null, fgColor = null;
    if (collapsed) {
      // Well, I like it to have same solid colors for
      // collapsed and expanded..
      bgColor = UIManager.getColor("Label.foreground");
      fgColor = UIManager.getColor("Label.background");
    } else {
      bgColor = UIManager.getColor("Label.foreground");
      fgColor = UIManager.getColor("Label.background");
    }
    header.setOpaque(true);
    header.setBackground(bgColor);
    header.setForeground(fgColor);
    
    
    // Change panel visibility
    boolean newVisibility = !collapsed;
    boolean stateHasChanged = false;
    if (newVisibility!=panel.isVisible()) {
      stateHasChanged = true;
    }
    panel.setVisible(newVisibility);
    
    // Paint borders
    header.setBorder(labelBorder!=null?labelBorder:intendedBorder);
    panel.setBorder(panel.isVisible()?intendedBorder:null);
    
    // Pack parent window
    validate();
    repaint();
    if (packParentWindow) {
      GUITools.packParentWindow(this);
    }
    
    // Notify listeners
    if (stateHasChanged) {
      fireExpansionEvent(collapsed);
    }
  }
  
  /**
   * @param collapsed current state
   * @return "Click to expand /collapse." (future action)
   */
  private String createClickAdviceString(boolean collapsed) {
    return String.format("Click to %s.", (collapsed?"expand":"collapse"));
  }
  /* (non-Javadoc)
   * @see javax.swing.JComponent#getPreferredSize()
   */
  @Override
  public Dimension getPreferredSize() {
    Dimension d = super.getPreferredSize();
    if (panel==null || header==null) return d; // Not yet initialized.
    
    // Width should be constantly the same (maximum of panel and header).
    d.width = Math.max(d.width, Math.max(header.getPreferredSize().width, panel.getPreferredSize().width));
    //...still varies a little bit, due to removal of the border.
    return d;
  }
  
  /* (non-Javadoc)
   * @see javax.swing.JComponent#getMinimumSize()
   */
  @Override
  public Dimension getMinimumSize() {
    Dimension d = super.getMinimumSize();
    if (panel==null || header==null) return d; // Not yet initialized.
    
    // Width should be constantly the same (maximum of panel and header).
    d.width = Math.max(d.width, Math.max(header.getMinimumSize().width, panel.getMinimumSize().width));
    //...still varies a little bit, due to removal of the border.
    return d;
  }
  
  /**
   * Simply re-build this panel.
   */
  public void update() {
    if (panel==null) return; // Not yet initialized.
    setState(!panel.isVisible());
  }
  
  /**
   * Toggles the current state, i.e. sets the state to
   * collapsed if it is currently expanded and via versa.
   */
  public void toggle() {
    if (panel==null) return; // Not yet initialized.
    setState(panel.isVisible());
  }
  
  /* (non-Javadoc)
   * @see javax.swing.JComponent#setToolTipText(java.lang.String)
   */
  @Override
  public void setToolTipText(String text) {
    super.setToolTipText(text);
    headerToolTip = text;
    update();
  }
  
  
  /**
   * @return true if the <code>pack()</code> method on the
   * parent window is invoked upon state change.
   */
  public boolean isPackParentWindow() {
    return packParentWindow;
  }
  
  /**
   * @param packParentWindow if true, invokes the <code>pack()</code>
   * method on the parent window upon state change.
   */
  public void setPackParentWindow(boolean packParentWindow) {
    this.packParentWindow = packParentWindow;
  }
  
  
  /**
   * Adds a listener for <code>TreeExpansion</code> events.
   *
   * @param tel a TreeExpansionListener that will be notified when
   * a tree node is expanded or collapsed (a "negative expansion")
   */
  public void addTreeExpansionListener(TreeExpansionListener tel) {
    listenerList.add(TreeExpansionListener.class, tel);
  }
  
  /**
   * @see #addTreeExpansionListener(TreeExpansionListener)
   * @param tel
   */
  public void addExpansionListener(TreeExpansionListener tel) {
    // Just for a better visibility than addTreeExpansionListener
    addTreeExpansionListener(tel);
  }
  
  /**
   * Removes a listener for <code>TreeExpansion</code> events.
   * @param tel the <code>TreeExpansionListener</code> to remove
   */
  public void removeTreeExpansionListener(TreeExpansionListener tel) {
    listenerList.remove(TreeExpansionListener.class, tel);
  }
  
  /**
   * Returns an array of all the <code>TreeExpansionListener</code>s added
   * to this JTree with addTreeExpansionListener().
   *
   * @return all of the <code>TreeExpansionListener</code>s added or an empty
   *         array if no listeners have been added
   */
  public TreeExpansionListener[] getTreeExpansionListeners() {
    return (TreeExpansionListener[])listenerList.getListeners(TreeExpansionListener.class);
  }
  
  /**
   * Notifies all listeners that have registered interest for
   * notification on this event type.
   *
   * @param collapsed <code>TRUE</code> if you want to fire a collapse
   * event. Else, a expanded event will be fired.
   * @see EventListenerList
   */
  public void fireExpansionEvent(boolean collapsed) {
    TreeExpansionEvent e = new TreeExpansionEvent(this, null);
    for (Object listener:listenerList.getListenerList()) {
      if (listener instanceof TreeExpansionListener) {
        if (collapsed) {
          ((TreeExpansionListener)listener).treeCollapsed(e);
        } else {
          ((TreeExpansionListener)listener).treeExpanded(e);
        }
      }
    }
  }
  
  
  
  /**
   * JUST FOR DEMONSTRATION AND EXAMPLE USAGE.
   */
  public static void main(String[] args) {  
    
    ExpandablePanel cp = new ExpandablePanel("test", buildPanel("child"),true,true);
    cp.setToolTipText("This is any tooltip!");
    
    JPanel parent = new JPanel();
    LayoutHelper lh = new LayoutHelper(parent);
    lh.add(cp);
    lh.add(buildPanel("No controlled"));
    
    JFrame f = new JFrame(); 
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
    f.getContentPane().add((parent));
    f.setSize(360, 500);
    f.setLocation(200, 100);
    f.pack();
    f.setVisible(true);  
  }  
  
  /**
   * Create any DEMO panel.
   * @param someText
   * @return
   */
  public static JPanel buildPanel(String someText) {      
    JPanel p1 = new JPanel();
    LayoutHelper lh = new LayoutHelper(p1);
    
    for (int i=0; i<2; i++) {
      lh.add(new JButton(someText+" button "+ i),
        new JButton(someText+" button "+ (i) + ".2"));
    }
    return p1;  
  }

  /* (non-Javadoc)
   * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
   */
  public void mouseClicked(MouseEvent e) {
    toggle();
  }

  /* (non-Javadoc)
   * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
   */
  public void mousePressed(MouseEvent e) {}

  /* (non-Javadoc)
   * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
   */
  public void mouseReleased(MouseEvent e) {}

  /* (non-Javadoc)
   * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
   */
  public void mouseEntered(MouseEvent e) {}

  /* (non-Javadoc)
   * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
   */
  public void mouseExited(MouseEvent e) {}
}  


