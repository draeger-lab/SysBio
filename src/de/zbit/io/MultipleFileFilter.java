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
package de.zbit.io;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * A {@link FileFilter} that combines several other {@link FileFilter} objects.
 * This {@link FileFilter} accepts a file if one of the member
 * {@link FileFilter}s accepts the file. It has just one single description that
 * must explain all of the given {@link FileFilter} instances.
 * 
 * @author Andreas Dr&auml;ger
 * @date 2010-10-22
 * @version $Rev$
 * @since 1.0
 */
public class MultipleFileFilter extends GeneralFileFilter {

	/**
	 * The unified description for all {@link FileFilter}s. 
	 */
	private String description;
  /**
   * All {@link FileFilter} instances that will be checked when calling the
   * {@link #accept(File)} method.
   */
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
	@Override
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
   * @see de.zbit.io.GeneralFileFilter#clone()
   */
  protected MultipleFileFilter clone() throws CloneNotSupportedException {
    return new MultipleFileFilter(description, filters);
  }

  /*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.filechooser.FileFilter#getDescription()
	 */
	public String getDescription() {
		return description;
	}

  /* (non-Javadoc)
   * @see de.zbit.io.GeneralFileFilter#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 919;
    int hashCode = super.hashCode();
    for (FileFilter filter : filters) {
      hashCode += prime * filter.hashCode();
    }
    return hashCode;
  }

}
