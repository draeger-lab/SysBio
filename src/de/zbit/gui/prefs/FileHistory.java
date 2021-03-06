/*
 * $Id$
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
package de.zbit.gui.prefs;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.zbit.util.ResourceManager;
import de.zbit.util.StringUtil;
import de.zbit.util.prefs.KeyProvider;
import de.zbit.util.prefs.Option;

/**
 * This interface provides one single {@link Option} to memorize a {@link List}
 * of {@link File} objects that can be accessed in a graphical user interface
 * and used to open {@link File}s that have been used earlier by the user.
 * 
 * @author Andreas Dr&auml;ger
 * @date 2010-12-10
 * @version $Rev$
 * @since 1.0
 */
public interface FileHistory extends KeyProvider {
  
  
  /**
   * The separator symbol within lists of files.
   */
  public static final String SEPARATOR = System
      .getProperty("path.separator");
  
  /**
   * An empty {@link List}.
   */
  public static final List<File> emptyList = new ArrayList<File>(0);
  
  /**
   * 
   */
  @SuppressWarnings("unchecked")
  public static Option<List<File>> LAST_OPENED = new Option<List<File>>(
      "LAST_OPENED", (Class<List<File>>) emptyList.getClass(), ResourceManager
      .getBundle(StringUtil.RESOURCE_LOCATION_FOR_LABELS).getString(
          "LAST_OPENED"), emptyList, false);
  
  /**
   * A collection of tools to facilitate working with the values associated
   * with a {@link FileHistory}.
   * 
   * @author Andreas Dr&auml;ger
   * @date 2010-12-11
   */
  public static class Tools {
    
    /**
     * Creates a {@link String} representation of a list of file, which
     * is surrounded with square brackets, and contains absolute paths to
     * files separated with the given symbol (SEPARATOR).
     * 
     * @param listOfFiles
     * @return
     */
    public static String toString(List<File> listOfFiles) {
      StringBuilder sb = new StringBuilder();
      sb.append('[');
      int i = 0;
      for (File file : listOfFiles) {
        if (i > 0) {
          sb.append(SEPARATOR);
        }
        sb.append(file.getAbsolutePath());
        i++;
      }
      sb.append(']');
      return sb.toString();
    }
    
    /**
     * Parses a list of files from a given {@link String}.
     * @param fileList can be empty but never null.
     * @return
     * @see #toString(List)
     */
    public static List<File> parseList(String fileList) {
      List<File> listOfFiles = new LinkedList<File>();
      if ((fileList != null) && (fileList.length() > 0)) {
        if (fileList.startsWith("[")) {
          fileList = fileList.substring(1);
        }
        if (fileList.endsWith("]")) {
          fileList = fileList.substring(0, fileList.length() - 1);
        }
        if (fileList.length() > 0) {
          String files[] = fileList.split(FileHistory.SEPARATOR);
          File file;
          for (String filePath : files) {
            file = new File(filePath);
            if (!listOfFiles.contains(file)) {
              listOfFiles.add(file);
            }
          }
        }
      }
      return listOfFiles;
    }
  }
  
}
