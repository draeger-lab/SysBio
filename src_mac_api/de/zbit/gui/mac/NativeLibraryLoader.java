/* $Id$
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
package de.zbit.gui.mac;

import java.io.File;
import java.io.IOException;

import de.zbit.util.Utils;

/**
 * @author Andreas Dr&auml;ger
 * @date 08:05:16
 * @since 1.1
 * @version $Rev$
 */
public class NativeLibraryLoader {
	
	/**
	 * 
	 * @param tmpDir
	 * @return
	 */
	private static File createLibFile(String tmpDir) {
		String osArch = System.getProperty("os.arch").toString();
		String suffix = "jnilib";
		String libFileName = "libquaqua";
		if (osArch.startsWith("x86") || osArch.endsWith("64")) {
			libFileName += "64";
		}
		libFileName += '.' + suffix;
		return new File(Utils.ensureSlash(tmpDir) + libFileName);
	}
	
	/**
	 * 
	 * @param tmpDir
	 */
	public static void deleteTempLibFile(String tmpDir) {
		File libFile = createLibFile(tmpDir);
		if (libFile.exists()) {
			libFile.delete();
		}
	}

	/**
	 * @throws IOException 
	 * 
	 */
	public static final void loadMacOSLibrary(String tmpDir) throws IOException {
		File libFile = createLibFile(tmpDir);
		if (libFile.canWrite()) {
			Utils.copyStream(NativeLibraryLoader.class.getResourceAsStream(libFile.getName()), libFile);
			System.load(libFile.getAbsolutePath());
			System.loadLibrary(libFile.getName());
		}
	}

	/**
	 * 
	 */
	private NativeLibraryLoader() {
		super();
	}
	
}
