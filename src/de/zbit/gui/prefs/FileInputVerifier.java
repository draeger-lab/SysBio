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
package de.zbit.gui.prefs;

import java.io.File;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.text.JTextComponent;

/**
 * This class checks any type of {@link JTextComponent} whether its text
 * represents a valid file or directory. In all other cases it returns false.
 * 
 * @author Andreas Dr&auml;ger
 * @date 2010-11-04
 * @version $Rev$
 * @since 1.0
 */
public class FileInputVerifier extends InputVerifier {
	
	/**
	 * This enum helps to distinguish between files and directories.
	 * 
	 * @author draeger
	 * @date 2010-11-04
	 */
	public enum FileType {
		/**
		 * Identifier for a valid directory.
		 */
		DIRECTORY,
		/**
		 * Identifier for a valid file.
		 */
		FILE;
	}
	
	/**
	 * Whether to check for files or directories.
	 */
	private FileType mode;
	
	/**
	 * This creates a {@link FileInputVerifier} that allows for both, files and
	 * directories.
	 */
	public FileInputVerifier() {
		mode = null;
	}
	
	/**
	 * The {@link Type} argument allows to select whether to allow for files or
	 * directories.
	 * 
	 * @param mode
	 */
	public FileInputVerifier(FileType mode) {
		this.mode = mode;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.InputVerifier#verify(javax.swing.JComponent)
	 */
	@Override
	public boolean verify(JComponent input) {
		if (input instanceof JTextComponent) {
			String text = ((JTextComponent) input).getText();
			File file = new File(text);
			if (((mode == null) && (file.isFile() || file.isDirectory()))
					|| (file.isFile() && (mode == FileType.FILE))
					|| (file.isDirectory() && (mode == FileType.DIRECTORY))) {
				return true; }
		}
		return false;
	}
}
