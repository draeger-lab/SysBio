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
import java.io.FileFilter;
import java.util.regex.Pattern;

/**
 * FileFilter that accepts file with a filename matching the given regular
 * expression. The directory name is ignored
 * 
 * @author Florian Mittag
 * @version $Rev$
 * @since 1.0
 */
public class RegExpFileFilter implements FileFilter {

	/**
	 * 
	 */
	protected final Pattern p;

	/**
	 * 
	 * @param regexp
	 */
	public RegExpFileFilter(String regexp) {
		p = Pattern.compile(regexp);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.FileFilter#accept(java.io.File)
	 */
	public boolean accept(File pathname) {
		return p.matcher(pathname.getName()).matches();
	}

}
