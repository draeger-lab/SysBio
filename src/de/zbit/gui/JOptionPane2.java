/*
 * $Id$
 * $URL$
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

import java.awt.Component;
import java.awt.HeadlessException;

import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

/**
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 */
public class JOptionPane2 extends JOptionPane {
  
  /**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = -7252097369536896054L;

  /**
   * Brings up a dialog with a specified icon, where the initial
   * choice is determined by the <code>initialValue</code> parameter and
   * the number of choices is determined by the <code>optionType</code>
   * parameter.
   * <p>
   * If <code>optionType</code> is <code>YES_NO_OPTION</code>,
   * or <code>YES_NO_CANCEL_OPTION</code>
   * and the <code>options</code> parameter is {@code null},
   * then the options are
   * supplied by the look and feel.
   * <p>
   * The <code>messageType</code> parameter is primarily used to supply
   * a default icon from the look and feel.
   *
   * @param parentComponent determines the <code>Frame</code>
   *                  in which the dialog is displayed;  if
   *                  {@code null}, or if the
   *                  <code>parentComponent</code> has no
   *                  <code>Frame</code>, a
   *                  default <code>Frame</code> is used
   * @param message   the <code>Object</code> to display
   * @param title     the title string for the dialog
   * @param optionType an integer designating the options available on the
   *                  dialog: <code>DEFAULT_OPTION</code>,
   *                  <code>YES_NO_OPTION</code>,
   *                  <code>YES_NO_CANCEL_OPTION</code>,
   *                  or <code>OK_CANCEL_OPTION</code>
   * @param messageType an integer designating the kind of message this is,
   *                  primarily used to determine the icon from the
   *                  pluggable Look and Feel: <code>ERROR_MESSAGE</code>,
   *                  <code>INFORMATION_MESSAGE</code>,
   *                  <code>WARNING_MESSAGE</code>,
   *                  <code>QUESTION_MESSAGE</code>,
   *                  or <code>PLAIN_MESSAGE</code>
   * @param icon      the icon to display in the dialog
   * @param options   an array of objects indicating the possible choices
   *                  the user can make; if the objects are components, they
   *                  are rendered properly; non-<code>String</code>
   *                  objects are
   *                  rendered using their <code>toString</code> methods;
   *                  if this parameter is {@code null},
   *                  the options are determined by the Look and Feel
   * @param initialValue the object that represents the default selection
   *                  for the dialog; only meaningful if <code>options</code>
   *                  is used; can be {@code null}
   * @param resizable decides whether or not the dialog should be resizable. 
   * @return an integer indicating the option chosen by the user,
   *                  or <code>CLOSED_OPTION</code> if the user closed
   *                  the dialog
   * @exception HeadlessException if
   *   <code>GraphicsEnvironment.isHeadless</code> returns
   *   {@code true}
   * @see java.awt.GraphicsEnvironment#isHeadless
   */
  public static int showOptionDialog(Component parentComponent,
      Object message, String title, int optionType, int messageType,
      Icon icon, Object[] options, Object initialValue, boolean resizable)
      throws HeadlessException {
      JOptionPane             pane = new JOptionPane(message, messageType,
                                                     optionType, icon,
                                                     options, initialValue);

      pane.setInitialValue(initialValue);
      pane.setComponentOrientation(((parentComponent == null) ?
          getRootFrame() : parentComponent).getComponentOrientation());

      JDialog dialog = pane.createDialog(parentComponent, title);
      dialog.setResizable(resizable);

      pane.selectInitialValue();
      dialog.setVisible(true);
      dialog.dispose();

      Object        selectedValue = pane.getValue();

      if(selectedValue == null)
          return CLOSED_OPTION;
      if(options == null) {
          if(selectedValue instanceof Integer)
              return ((Integer)selectedValue).intValue();
          return CLOSED_OPTION;
      }
      for(int counter = 0, maxCounter = options.length;
          counter < maxCounter; counter++) {
          if(options[counter].equals(selectedValue))
              return counter;
      }
      return CLOSED_OPTION;
  }
    
}
