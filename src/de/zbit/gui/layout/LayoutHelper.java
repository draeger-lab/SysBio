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
package de.zbit.gui.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A helper class that provides several methods for working with a
 * {@link GridBagLayout}.
 * 
 * @author Andreas Dr&auml;ger
 * @date 2005-07-29
 * @version $Rev$
 * @since 1.0
 */
public class LayoutHelper {
  
  /**
   * 
   * @param cont
   * @param gbl
   * @param c
   * @param x
   * @param y
   * @param width
   * @param height
   * @param weightx
   * @param weighty
   */
  public static void addComponent(Container cont, GridBagLayout gbl,
    Component c, int x, int y, int width, int height, double weightx,
    double weighty) {
    GridBagConstraints gbc = new GridBagConstraints();
    addComponent(cont, gbl, c, x, y, width, height, weightx, weighty,
      gbc.ipadx, gbc.ipady);
  }
  
  /**
   * 
   * @param cont
   * @param gbl
   * @param c
   * @param x
   * @param y
   * @param width
   * @param height
   * @param weightx
   * @param weighty
   * @param ipadx
   * @param ipady
   */
  public static void addComponent(Container cont, GridBagLayout gbl,
    Component c, int x, int y, int width, int height, double weightx,
    double weighty, int ipadx, int ipady) {
    addComponent(cont, gbl, c, x, y, width, height, weightx, weighty, ipadx,
      ipady, 0, 0, 0, 0);
  }
  
  /**
   * 
   * @param cont
   * @param gbl
   * @param c
   * @param x
   * @param y
   * @param width
   * @param height
   * @param weightx
   * @param weighty
   * @param ipadx
   * @param ipady
   * @param top
   * @param bottom
   * @param left
   * @param right
   */
  public static void addComponent(Container cont, GridBagLayout gbl,
    Component c, int x, int y, int width, int height, double weightx,
    double weighty, int ipadx, int ipady, int top, int bottom, int left, int right) {
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.BOTH;
    gbc.gridx = x;
    gbc.gridy = y;
    gbc.gridwidth = width;
    gbc.gridheight = height;
    gbc.weightx = weightx;
    gbc.weighty = weighty;
    gbc.ipadx = ipadx;
    gbc.ipady = ipady;
    gbc.insets = new Insets(top, left, bottom, right);
    gbl.setConstraints(c, gbc);
    cont.add(c);
  }
  
  /**
   * True if and only if we are currently at the left most position of
   * any row.
   */
  private boolean atRowBeginning;
  
  /**
   * 
   */
  private Container cont;
  
  /**
   * 
   */
  private GridBagLayout gbl;
  
  /**
   * 
   */
  private int row;
  private int maxColCount = 1;
  
  /**
   * Creates a new GridBaglayout and associates this with the given container.
   * 
   * @param cont
   */
  public LayoutHelper(Container cont) {
    this(cont, new GridBagLayout());
  }
  
  /**
   * 
   * @param cont
   * @param gbl
   */
  public LayoutHelper(Container cont, GridBagLayout gbl) {
    this.cont = cont;
    this.gbl = gbl;
    this.cont.setLayout(this.gbl);
    row = 0;
  }
  
  /**
   * 
   * @param sameWidth
   *        if true, a maximum width will be applied to each component, sharing
   *        the total width of the {@link Container}.
   * @param c
   * @param components
   */
  public void add(boolean sameWidth, Component c, Component... components) {
    ensurePointerIsAtBeginningOfARow();
    add(c, 0, row, 1, 1, sameWidth ? 1 : 0, 0);
    for (int i = 0; i < components.length; i++) {
      add(components[i], i + 1, row, 1, 1, sameWidth ? 1 : 0, 0);
    }
    atRowBeginning = false;
  }
  
  /**
   * Adds this component in the next row.
   * @param c
   */
  public void add(Component c) {
    add(c, 1d, 1d);
  }
  
  /**
   * Adds this component in the next row.
   * @param c
   * @param spacer
   */
  public void add(Component c, boolean spaceLine) {
    int width = 1;
    addWithWidth(c, width);
    if (spaceLine) {
      JPanel p = new JPanel();
      p.setOpaque(false);
      add(p, 0, ++row, width, 1, 0, 0);
    }
  }
  
  /**
   * Add one or many components in one line.
   * 
   * @param c
   * @param comps
   */
  public void add(Component c, Component... comps) {
    add(false, c, comps);
  }
  
  /**
   * 
   * @param c
   * @param weightx
   * @param weighty
   */
  public void add(Component c, double weightx, double weighty) {
    addWithWidth(c, 1, weightx, weighty);
  }
  
  /**
   * 
   * @param c
   * @param width
   */
  public void add(Component c, int width) {
    add(c,width,false);
  }
  
  /**
   * 
   * @param c
   * @param width
   * @param spaceLine
   */
  public void add(Component c, int width, boolean spaceLine) {
    ensurePointerIsAtBeginningOfARow();
    add(c, 0, row, width, 1);
    if (spaceLine) {
      JPanel p = new JPanel();
      p.setOpaque(false);
      add(p, 0, ++row, width, 1, 0, 0);
    }
  }
  
  /**
   * 
   * @param c
   * @param row
   * @param column
   */
  public void add(Component c, int row, int column) {
    add(c, row, column, 1, 1, 1d, 1d);
  }
  
  /**
   * 
   * @param c
   * @param x
   * @param y
   * @param width
   */
  public void add(Component c, int x, int y, int width) {
    add(c, x, y, width, 1);
  }
  
  /**
   * Add a component without specifying the row.
   * 
   * @param c
   * @param x
   * @param width
   * @param height
   * @param weightx
   * @param weighty
   */
  public void add(Component c, int x, int width, int height, double weightx,
    double weighty) {
    add(c, x, row, width, height, weightx, weighty);
    row++;
    atRowBeginning = true;
    if (width > maxColCount) {
      maxColCount = width;
    }
  }
  
  /**
   * 
   * @param c
   * @param x
   * @param y
   * @param width
   * @param height
   */
  public void add(Component c, int x, int y, int width, int height) {
    // Note: This method does NOT increase the row!
    LayoutHelper.addComponent(cont, gbl, c, x, y, width, height, 0, 0);
    row = y;
    atRowBeginning = false;
    if (width > maxColCount) {
      maxColCount = width;
    }
  }
  
  /**
   * 
   * @param c
   * @param x
   * @param y
   * @param width
   * @param height
   * @param weightx
   * @param weighty
   */
  public void add(Component c, int x, int y, int width, int height,
    double weightx, double weighty) {
    // Note: This method does NOT increase the row!
    addComponent(cont, gbl, c, x, y, width, height, weightx, weighty);
    row = y;
    atRowBeginning = false;
    if (width > maxColCount) {
      maxColCount = width;
    }
  }
  
  /**
   * 
   * @param c
   * @param x
   * @param y
   * @param width
   * @param height
   * @param weightx
   * @param weighty
   * @param ipadx
   * @param ipady
   */
  public void add(Component c, int x, int y, int width, int height,
    double weightx, double weighty, int ipadx, int ipady) {
    add(c, x, y, width, height, weightx, weighty, ipadx, ipady, 0, 0, 0, 0);
  }
  
  /**
   * 
   * @param c
   * @param x
   * @param y
   * @param width
   * @param height
   * @param weightx
   * @param weighty
   * @param ipadx
   * @param ipady
   * @param top
   * @param bottom
   * @param left
   * @param right
   */
  public void add(Component c, int x, int y, int width, int height,
    double weightx, double weighty, int ipadx, int ipady, int top, int bottom, int left, int right) {
    // Note: This method does NOT increase the row!
    LayoutHelper.addComponent(cont, gbl, c, x, y, width, height,
      weightx, weighty, ipadx, ipady, top, bottom, left, right);
    row = y;
    atRowBeginning = false;
    if (width > maxColCount) {
      maxColCount = width;
    }
  }
  
  /**
   * 
   * @param label
   * @param c
   */
  public void add(String label, Component c) {
    ensurePointerIsAtBeginningOfARow();
    add(label, c, 0, row);
    atRowBeginning = false;
  }
  
  /**
   * A row of components
   * 
   * @param label
   * @param components
   */
  public void add(String label, Component... components) {
    int x = 0;
    ensurePointerIsAtBeginningOfARow();
    add(new JLabel(label), x, row, 1, 1, 0, 0);
    for (Component component : components) {
      JPanel p = new JPanel();
      p.setOpaque(false);
      add(p, ++x, row, 1, 1, 0, 0);
      add(component, ++x, row, 1, 1);
    }
    atRowBeginning=false;
  }
  
  /**
   * 
   * @param label
   * @param c
   * @param spaceLine
   *        If true, a new line with an empty JPanel will be created as a
   *        spacer.
   * @return the created {@link JLabel}
   */
  public JLabel add(String label, Component c, boolean spaceLine) {
    ensurePointerIsAtBeginningOfARow();
    JLabel ret = add(label, c, 0, row);
    if (spaceLine) {
      JPanel p = new JPanel();
      p.setOpaque(false);
      add(p, 0, ++row, 3, 1, 0, 0);
    }
    atRowBeginning=false;
    return ret;
  }
  
  /**
   * Creates a pair of a label and a component separated by a spacing panel.
   * 
   * @param label
   * @param c
   * @param x
   * @param y
   * @return the created {@link JLabel}
   */
  public JLabel add(String label, Component c, int x, int y) {
    JLabel lab = new JLabel(label);
    lab.setOpaque(false);
    lab.setLabelFor(c);
    JPanel p = new JPanel();
    p.setOpaque(false);
    add(lab, x, y, 1, 1, 0, 0);
    add(p, x + 1, y, 1, 1, 0, 0);
    add(c, x + 2, y, 1, 1);
    atRowBeginning = false;
    return lab;
  }
  
  /**
   * 
   * @param label
   * @param c
   * @param width
   * @param weightx
   * @param weighty
   */
  public void add(String label, Component c, int width, int weightx, int weighty) {
    add(label, c, 0, row, weightx, weighty);
    row++;
    atRowBeginning=true;
  }
  
  /**
   * 
   * @param label
   * @param c
   * @param x
   * @param y
   * @param weightx
   * @param weighty
   */
  public void add(String label, Component c, int x, int y, int weightx,
    int weighty) {
    JPanel p = new JPanel();
    p.setOpaque(false);
    add(new JLabel(label), x, y, 1, 1, 0, 0);
    add(p, x + 1, y, 1, 1, 0, 0);
    add(c, x + 2, y, 1, 1, weightx, weighty);
    row = y;
    atRowBeginning = false;
  }
  
  /**
   * Add a {@link JLabel} with text {@code label} and width
   * {@code width} to the current {@link Container}.
   * @param label
   * @param width
   */
  public void add(String label, int width) {
    add(new JLabel(label), width);
  }
  
  /**
   * 
   * @param c
   * @param column
   */
  public void addInColumn(Component c, int column) {
    addWithWidth(c, 1, column);
  }
  
  /**
   * 
   */
  public void addSpacer() {
    ensurePointerIsAtBeginningOfARow();
    JPanel p = new JPanel();
    p.setOpaque(false);
    add(p, 0, ++row, 1, 1, 0d, 0d);
  }
  
  /**
   * Adds this component in the next row, with
   * the given width
   * @param c
   * @param width
   */
  public void addWithWidth(Component c, int width) {
    addWithWidth(c, width, 1d, 1d);
  }
  
  /**
   * 
   * @param c
   * @param width
   * @param weightx
   * @param weighty
   */
  public void addWithWidth(Component c, int width, double weightx, double weighty) {
    addWithWidth(c, 0, width, weightx, weighty);
  }
  
  /**
   * 
   * @param c
   * @param column
   * @param width
   */
  public void addWithWidth(Component c, int column, int width) {
    addWithWidth(c, column, width, 1d, 1d);
  }
  
  /**
   * 
   * @param c
   * @param column
   * @param width
   * @param weightx
   * @param weighty
   */
  public void addWithWidth(Component c, int column, int width, double weightx, double weighty) {
    ensurePointerIsAtBeginningOfARow();
    add(c, column, row, width, 1, weightx, weighty);
    atRowBeginning = false;
  }
  
  /**
   * Ensures that the next components are being written in a new row.
   */
  public void ensurePointerIsAtBeginningOfARow() {
    if (!atRowBeginning) {
      row++;
      atRowBeginning = true;
    }
  }
  
  /**
   * 
   * @return
   */
  public Container getContainer() {
    return cont;
  }
  
  /**
   * 
   * @return
   */
  public int getRow() {
    return row;
  }
  
  /**
   * Manually increment the current row.
   * 
   * @param increment
   */
  public void incrementRowBy(int increment) {
    row += increment;
  }
  
  public int getColumnCount() {
    // TODO Auto-generated method stub
    return maxColCount;
  }
  
}
