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

import javax.swing.filechooser.FileFilter;

/**
 * @author Andreas Dr&auml;ger
 * @date 2010-10-22
 * @version $Rev$
 * @since 1.0
 */
public class MultipleFileFilter extends GeneralFileFilter {

	/**
	 * 
	 */
	private String description;
	private FileFilter[] filters;

	/**
	 * 
	 */
	public MultipleFileFilter(String description, FileFilter... filters) {
		super();
		this.description = description;
		this.filters = filters;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
	 */
	public boolean accept(File f) {
		for (FileFilter filter : filters) {
			if (filter.accept(f)) {
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.filechooser.FileFilter#getDescription()
	 */
	public String getDescription() {
		return description;
	}

}
