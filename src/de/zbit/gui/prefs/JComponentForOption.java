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
package de.zbit.gui.prefs;

import javax.swing.JComponent;

import de.zbit.util.prefs.Option;

/**
 * This interface should provide common access methods for
 * {@link JComponent}s that are build to represent {@link Option}s.
 * 
 * This acts as a link between the JComponent and the actual option.
 * This eases e.g. Range checking, checking and changing Preferences
 * or Properties, etc.
 * 
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public interface JComponentForOption {
  
  /**
   * This method should return the associated {@link Option}
   * or null, if no option is associated / set.
   * @return
   */
  public Option<?> getOption();
  
  /**
   * This method should return true if and only if an
   * {@link Option} is associated with this {@link JComponent}.
   * @return
   */
  public boolean isSetOption();
  
  /**
   * Links the current {@link JComponent} to the given
   * {@link Option}.
   * @param option
   */
  public void setOption(Option<?> option);
  
  /**
   * This should always return the current value of the
   * {@link JComponent} for the {@link Option}.
   * @return
   */
  public Object getCurrentValue();
  
}
