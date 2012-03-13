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
package de.zbit.io.filefilter;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * A unified abstract {@link FileFilter} and {@link java.io.FileFilter}.
 * It can also be used as a wrapper for both elements.
 * 
 * @author Andreas Dr&auml;ger
 * @date 2010-11-09
 * @version $Rev$
 * @since 1.0
 */
public abstract class GeneralFileFilter extends FileFilter implements
		java.io.FileFilter {
	    
  /**
   * Allows users to initialize this {@link GeneralFileFilter} with another
   * {@link java.io.FileFilter}. In this way, the {@link GeneralFileFilter} can
   * work as an adapter/wrapper.
   */
  private java.io.FileFilter filter;
  
  /**
   * Generates a new {@link GeneralFileFilter} that does not wrap any other
   * {@link FileFilter}s nor {@link java.io.FileFilter}s.
   */
  public GeneralFileFilter() {
    super();
  }

  /**
   * 
   * @param filter
   */
  public GeneralFileFilter(java.io.FileFilter filter) {
    this();
    setFileFilter(filter);
  }
  
  /* (non-Javadoc)
   * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
   */
  @Override
  public boolean accept(File f) {
    if (isSetFileFilter()) {
        return filter.accept(f);
    }
    return false;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#clone()
   */
  @Override
  protected abstract GeneralFileFilter clone() throws CloneNotSupportedException;

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (obj.getClass().equals(getClass())) {
      return obj.hashCode() == hashCode();
    }
    return super.equals(obj);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 577;
    int hashCode = getClass().getName().toString().hashCode();
    if (isSetFileFilter()) {
      hashCode += prime * filter.hashCode();
    }
    return hashCode;
  }

  /**
   * Checks whether the {@link java.io.FileFilter} is not null.
   * 
   * @return
   */
  public boolean isSetFileFilter() {
    return filter != null;
  }

  /**
   * 
   * @param filter
   */
  public void setFileFilter(java.io.FileFilter filter) {
    this.filter = filter;
  }  
  
}
