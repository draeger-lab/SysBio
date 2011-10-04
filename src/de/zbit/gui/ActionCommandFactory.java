/*
 * $Id$
 * $URL$
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


/**
 * @author Florian Mittag
 * @version $Rev$
 */

/**
 * A factory for creating ActionCommand objects.
 * 
 * @author Florian Mittag
 * @version $Rev$
 */
public class ActionCommandFactory implements ActionCommand {

  protected final String name;
  protected final String tooltip;

  /**
   * Creates a new ActionCommand object with the given name and tooltip.
   * 
   * @param name the name of the ActionCommand
   * @param tooltip the tooltip for the ActionCommand
   * @return the newly created ActionCommand object
   */
  public static ActionCommand create(String name, String tooltip) {
    return new ActionCommandFactory(name, tooltip);
  }
  
  private ActionCommandFactory(String name, String tooltip) {
    this.name = name;
    this.tooltip = tooltip;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.gui.ActionCommand#getName()
   */
  public String getName() {
    return name;
  }

  /* (non-Javadoc)
   * @see de.zbit.gui.ActionCommand#getToolTip()
   */
  public String getToolTip() {
    return tooltip;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return getName();
  }
}
