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
package de.zbit.io;

import java.io.File;
import java.util.ResourceBundle;

import de.zbit.util.ResourceManager;

/**
 * A {@link GeneralFileFilter} that accepts any kind of file or directory.
 * 
 * @author Andreas Dr&auml;ger
 * @date 2011-01-04
 * @version $Rev$
 * @since 1.0
 */
public class SBAcceptAllFileFilter extends GeneralFileFilter {
	
	/**
	 * 
	 */
	public SBAcceptAllFileFilter() {
	  super();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
	 */
	public boolean accept(File f) {
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.filechooser.FileFilter#getDescription()
	 */
	public String getDescription() {
		ResourceBundle resource = ResourceManager
				.getBundle("de.zbit.locales.Labels");
		return resource.getString("ACCEPT_ALL_FILES");
	}

  /*
   * (non-Javadoc)
   * @see de.zbit.io.GeneralFileFilter#clone()
   */
  protected SBAcceptAllFileFilter clone() throws CloneNotSupportedException {
    return new SBAcceptAllFileFilter();
  }
	
}
