/* $Id$
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
package de.zbit.io;

import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * A class that contains methods to walk through a file system and do operations such as copying etc.
 * 
 * <p>Note: for a real "FileWalker", consider using the {@link DirectoryParser}!</p>
 * 
 * @author Andreas Dr&auml;ger
 * @since 1.1
 * @version $Rev$
 */
public class FileWalker {
	
	/**
	 * A {@link Logger} for this class.
	 */
	private static final transient Logger logger = Logger.getLogger(FileWalker.class.getName());
	
	/**
	 * <p>
	 * Walks recursively through all files of a given directory, creates
	 * corresponding sub-folders in the given target directory and returns a map
	 * of original existing files to the absolute paths of (not yet existing)
	 * output files in the target directory.
	 * </p>
	 * <p>
	 * In this way it is very easy to run some application in batch mode: Simply
	 * copy the directory structure of the source (with this method) and then
	 * process all input files and write the output to the corresponding target
	 * file.
	 * </p>
	 * 
	 * @param source
	 * @param targetDir
	 * @param filter
	 * @param skipHiddenFiles
	 * @return
	 * @throws SecurityException
	 */
	public static Map<File, String> filterAndCreate(File source, File targetDir, FileFilter filter, boolean skipHiddenFiles) throws SecurityException {
		logger.log(Level.FINE, "entering " + source.getAbsolutePath());
		Map<File, String> inputToOutput = new HashMap<File, String>();
		if (!targetDir.exists()) {
			logger.log(Level.FINE, "creating directory " + targetDir.getAbsolutePath());
			targetDir.mkdir();
		}
		if (source.exists() && source.canRead() && (!skipHiddenFiles || !source.isHidden())) {
			if (source.isDirectory()) {
				for (File f : source.listFiles()) {
					if (f.isFile() && f.canRead() && filter.accept(f)) {
						logger.log(Level.FINE, "adding file " + f.getAbsolutePath());
						inputToOutput.put(f, targetDir.getAbsolutePath() + '/' + f.getName());
					} else if (f.isDirectory() && f.canRead() && f.canExecute() && (!skipHiddenFiles || !source.isHidden())) {
						inputToOutput.putAll(filterAndCreate(f, new File(targetDir.getAbsolutePath() + '/' + f.getName()), filter, skipHiddenFiles));
					}
				}
			} else if (filter.accept(source)) {
				logger.log(Level.FINE, "adding file " + source.getAbsolutePath());
				inputToOutput.put(source, targetDir.getAbsolutePath() + '/' + source.getName());
			}
		} else {
			logger.log(Level.FINE, "skipping " + source.getAbsolutePath());
		}
		return inputToOutput;
	}
	
}
