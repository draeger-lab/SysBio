/* $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2016 by the University of Tuebingen, Germany.
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

import de.zbit.io.FileTools;
import de.zbit.util.Utils;

/**
 * @author Andreas Dr&auml;ger
 * @date 08:05:16
 * @since 1.1
 * @version $Rev$
 */
public class NativeLibraryLoader {
  
  /**
   * The only instance of this class!
   */
  private static NativeLibraryLoader loader = new NativeLibraryLoader();
  
  /**
   * 
   * @param tmpDir
   * @throws IOException
   */
  public static final void deleteTempLibFile(String tmpDir) throws IOException {
    File libFiles[] = loader.createLibFile(tmpDir);
    for (File libFile : libFiles) {
      if (libFile.exists()) {
        libFile.delete();
        loader.libFile = null;
      }
    }
  }
  
  /**
   * @throws IOException
   * 
   */
  public static final void loadMacOSLibrary(String tmpDir) throws IOException {
    File libFiles[] = loader.createLibFile(tmpDir);
    Throwable t = null;
    for (File libFile : libFiles) {
      if (libFile.canWrite()) {
        FileTools.copyStream(NativeLibraryLoader.class.getResourceAsStream(libFile.getName()), libFile);
        libFile.deleteOnExit();
        try {
          System.load(libFile.getAbsolutePath());
          System.loadLibrary(libFile.getName());
        } catch (Throwable exc) {
          if (t == null) {
            t = exc;
          } else {
            t.initCause(exc);
          }
        }
      }
    }
    if (t != null) {
      throw (RuntimeException) t;
    }
  }
  
  /**
   * 
   */
  private File[] libFile;
  
  /**
   * 
   */
  private NativeLibraryLoader() {
    super();
  }
  
  /**
   * 
   * @param tmpDir
   * @return
   * @throws IOException
   */
  private final File[] createLibFile(String tmpDir) throws IOException {
    if (libFile != null) {
      return libFile;
    }
    String osArch = System.getProperty("os.arch").toString();
    String libFileName = "libquaqua";
    if (osArch.startsWith("x86") || osArch.endsWith("64")) {
      libFileName += "64";
      libFile = new File[2];
      libFile[1] = new File(Utils.ensureSlash(tmpDir) + libFileName + ".dylib");
    }
    String suffix = "jnilib";
    libFileName += '.' + suffix;
    libFile[0] = new File(Utils.ensureSlash(tmpDir) + libFileName);
    for (File file : libFile) {
      if (!file.exists()) {
        file.createNewFile();
      }
    }
    return libFile;
  }
  
}
