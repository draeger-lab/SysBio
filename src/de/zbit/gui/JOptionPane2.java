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
   * choice is determined by the {@code initialValue} parameter and
   * the number of choices is determined by the {@code optionType}
   * parameter.
   * <p>
   * If {@code optionType} is {@code YES_NO_OPTION},
   * or {@code YES_NO_CANCEL_OPTION}
   * and the {@code options} parameter is {@code null},
   * then the options are
   * supplied by the look and feel.
   * <p>
   * The {@code messageType} parameter is primarily used to supply
   * a default icon from the look and feel.
   *
   * @param parentComponent determines the {@code Frame}
   *                  in which the dialog is displayed;  if
   *                  {@code null}, or if the
   *                  {@code parentComponent} has no
   *                  {@code Frame}, a
   *                  default {@code Frame} is used
   * @param message   the {@code Object} to display
   * @param title     the title string for the dialog
   * @param optionType an integer designating the options available on the
   *                  dialog: {@code DEFAULT_OPTION},
   *                  {@code YES_NO_OPTION},
   *                  {@code YES_NO_CANCEL_OPTION},
   *                  or {@code OK_CANCEL_OPTION}
   * @param messageType an integer designating the kind of message this is,
   *                  primarily used to determine the icon from the
   *                  pluggable Look and Feel: {@code ERROR_MESSAGE},
   *                  {@code INFORMATION_MESSAGE},
   *                  {@code WARNING_MESSAGE},
   *                  {@code QUESTION_MESSAGE},
   *                  or {@code PLAIN_MESSAGE}
   * @param icon      the icon to display in the dialog
   * @param options   an array of objects indicating the possible choices
   *                  the user can make; if the objects are components, they
   *                  are rendered properly; non-{@code String}
   *                  objects are
   *                  rendered using their {@code toString} methods;
   *                  if this parameter is {@code null},
   *                  the options are determined by the Look and Feel
   * @param initialValue the object that represents the default selection
   *                  for the dialog; only meaningful if {@code options}
   *                  is used; can be {@code null}
   * @param resizable decides whether or not the dialog should be resizable. 
   * @return an integer indicating the option chosen by the user,
   *                  or {@code CLOSED_OPTION} if the user closed
   *                  the dialog
   * @exception HeadlessException if
   *   {@code GraphicsEnvironment.isHeadless} returns
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
      dialog.pack();

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
  
  
  /**
   * Brings up a dialog where the number of choices is determined
   * by the {@code optionType} parameter. In contrast to the original
   * {@link #showConfirmDialog(Component, Object)} method, the resulting
   * dialog will be resizable.
   * 
   * @param parentComponent determines the {@code Frame} in which the
   *      dialog is displayed; if {@code null},
   *      or if the {@code parentComponent} has no
   *      {@code Frame}, a 
   *                  default {@code Frame} is used
   * @param message   the {@code Object} to display
   * @param title     the title string for the dialog
   * @param optionType an int designating the options available on the dialog:
   *                  {@code YES_NO_OPTION},
   *                  {@code YES_NO_CANCEL_OPTION},
   *                  or {@code OK_CANCEL_OPTION}
   * @return an int indicating the option selected by the user
   * @exception HeadlessException if
   *   {@code GraphicsEnvironment.isHeadless} returns
   *   {@code true}
   * @see java.awt.GraphicsEnvironment#isHeadless
   */
  public static int showConfirmDialogResizable(Component parentComponent,
    Object message, String title, int optionType) throws HeadlessException {
    return showOptionDialog(parentComponent, message, title, optionType,
      QUESTION_MESSAGE, null, null, null, true);
  }
  
}
