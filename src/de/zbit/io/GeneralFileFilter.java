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

import javax.swing.filechooser.FileFilter;

/**
 * A unified abstract {@link FileFilter} and {@link java.io.FileFilter}.
 * 
 * @author Andreas Dr&auml;ger
 * @date 2010-11-09
 * @version $Rev$
 * @since 1.0
 */
public abstract class GeneralFileFilter extends FileFilter implements
		java.io.FileFilter {
	
}
