/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2013 by the University of Tuebingen, Germany.
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

import java.io.File;

import javax.swing.JMenuBar;
import javax.swing.JToolBar;

/**
 * A common interface for all tabs, used in the {@link BaseFrame}.
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public interface BaseFrameTab {
  
  /**
   * Save the contents of this tab to a {@link File}.
   * @return the saved {@link File}.
   */
  public File saveToFile();
  
	/**
	 * Enable or disable the buttons of the given {@link JMenuBar} The
	 * {@link JToolBar} might be {@code null} if your application does not
	 * use a {@link JToolBar}.
	 * 
	 * @param menuBar
	 * @param toolbar
	 */
  public void updateButtons(JMenuBar menuBar, JToolBar... toolbar);
  
}
