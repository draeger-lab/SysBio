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
package de.zbit.gui.prefs;

import javax.swing.JCheckBox;

import de.zbit.util.prefs.Option;
import de.zbit.util.prefs.SBPreferences;

/**
 * A simple extension of {@link JCheckBox} that implements
 * the {@link JComponentForOption} interface.
 * 
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class OptionCheckBox extends JCheckBox implements JComponentForOption {
  private static final long serialVersionUID = 5665386342819719358L;
  
  /**
   * Only necessary for using this class in Combination with
   * {@link SBPreferences} and {@link Option}s.
   */
  private Option<?> option=null;
  
  /* (non-Javadoc)
   * @see de.zbit.gui.prefs.JComponentForOption#getOption()
   */
  public Option<?> getOption() {
    return option;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.gui.prefs.JComponentForOption#isSetOption()
   */
  public boolean isSetOption() {
    return option!=null;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.gui.prefs.JComponentForOption#setOption(de.zbit.util.prefs.Option)
   */
  public void setOption(Option<?> option) {
    this.option=option;
  }

  /* (non-Javadoc)
   * @see de.zbit.gui.prefs.JComponentForOption#getCurrentValue()
   */
  public Object getCurrentValue() {
    return isSelected();
  }
}
