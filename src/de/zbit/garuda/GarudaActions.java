/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2016 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.garuda;

import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.UIManager;

import de.zbit.gui.actioncommand.ActionCommandWithIcon;
import de.zbit.util.ResourceManager;

/**
 * Garuda-related {@link ActionCommandWithIcon}. In order to use this in a
 * graphical user interface, you could, for instance, do the following:
 * 
 * <pre>
 * JMenuItem sentFile = GUITools.createJMenuItem(this,
 * 	GarudaActions.SENT_TO_GARUDA, false);
 * ResourceBundle garudaBundle = ResourceManager
 * 		.getBundle(&quot;de.zbit.garuda.locales.Labels&quot;);
 * JMenu garudaMenu = GUITools.createJMenu(garudaBundle.getString(&quot;GARUDA&quot;),
 * 	sentFile);
 * garudaMenu.setToolTipText(garudaBundle.getString(&quot;GARUDA_TOOLTIP&quot;));
 * </pre>
 * 
 * Or you could directly use the {@link GarudaGUIfactory}, where this is already
 * implemented.
 * 
 * @author Andreas Dr&auml;ger
 * @date 11:41:08
 * @since 1.1
 * @version $Rev$
 */
public enum GarudaActions implements ActionCommandWithIcon {
  
  /**
   * Sents the current file to a compatible software in the Garuda platform.
   */
  SENT_TO_GARUDA;
  
  /**
   * Localization support.
   */
  private static final transient ResourceBundle bundle = ResourceManager.getBundle("de.zbit.garuda.locales.Labels");
  
  /* (non-Javadoc)
   * @see de.zbit.gui.actioncommand.ActionCommand#getName()
   */
  @Override
  public String getName() {
    return bundle.getString(name());
  }
  
  /* (non-Javadoc)
   * @see de.zbit.gui.actioncommand.ActionCommand#getToolTip()
   */
  @Override
  public String getToolTip() {
    return bundle.getString(name() + "_TOOLTIP");
  }
  
  /* (non-Javadoc)
   * @see de.zbit.gui.actioncommand.ActionCommandWithIcon#getIcon()
   */
  @Override
  public Icon getIcon() {
    return UIManager.getIcon("ICON_GARUDA_16");
  }
  
}
