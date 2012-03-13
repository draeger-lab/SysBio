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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Iterating through a directory (you may include subdirectories).
 * 
 * @author Clemens Wrzodek
 * @version $Rev$
 * @since 1.0
 */
public class DirectoryParser implements Iterator<String> {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DirectoryParser d = new DirectoryParser("S:\\SVM\\tools", "ft");
		while (d.hasNext())
			System.out.println(d.next());
	}
	/**
	 * 
	 */
	private String path = ".";
	/**
	 * 
	 */
	private String extension = "";
	/**
	 * 
	 */
	private int curPos = 0;
    /**
	 * 
	 */
	private String[] contents = null;

	/**
     * 
     */
	private boolean recurseIntoSubdirectories = false;

	/**
	 * 
	 */
	public DirectoryParser() {
	}

	/**
	 * 
	 * @param path
	 */
	public DirectoryParser(String path) {
		this();
		setPath(path);
	}

	/**
	 * 
	 * @param path
	 * @param extension
	 */
	public DirectoryParser(String path, String extension) {
		this(path);
		setExtension(extension);
	}

	/**
	 * Creates a new DirectoryParser instance.
   * @param cgiDir - if it is a directory, a new DirectoryParser
   * will be created with this directory as input. If it is a file,
   * a new DirectoryParser will be created with the directory in
   * which the file resides as input.
   */
  public DirectoryParser(File cgiDir) {
    this (cgiDir.isDirectory()?cgiDir.getPath():cgiDir.getParent());
  }

  /**
	 * 
	 * @param path
	 * @return
	 */
	private static String appendSlash(String path) {
		if (!path.endsWith("\\") && !path.endsWith("/"))
			if (path.contains("/"))
				path += "/";
			else if (path.contains("\\"))
				path += "\\";
			else
				path += "/";
		return path;
	}

	/**
	 * 
	 * @param item
	 * @param caseSensitive
	 * @return
	 */
	public boolean contains(String item, boolean caseSensitive) {
		if (contents == null)
			readDir();
		if (contents == null)
			return false;

		for (String s : this.contents)
			if (caseSensitive ? s.equals(item) : s.equalsIgnoreCase(item))
				return true;
		return false;
	}

	/**
	 * 
	 * @return
	 */
	public String[] getAll() {
		if (contents == null)
			readDir();
		if (contents == null)
			return null;
		return this.contents.clone();
	}

	/**
	 * 
	 * @return
	 */
	public int getCount() {
		if (contents == null)
			readDir();
		if (contents == null)
			return 0;
		return this.contents.length;
	}

	/**
	 * 
	 * @return
	 */
	public String getExtension() {
		return extension;
	}

	/**
	 * @return the {@link #path}, always with ending slash.
	 */
	public String getPath() {
		return getPath(this.path);
	}

	/**
	 * 
	 * @param path
	 * @return
	 */
	private static String getPath(String path) {
		path = appendSlash(path);
		return path;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Iterator#hasNext()
	 */
	// @Override
	public boolean hasNext() {
		if (contents == null) {
			readDir();
			if (contents == null)
				return false;
		}
		if (curPos >= contents.length)
			return false;
		return true;
	}

	/**
	 * 
	 * @return
	 */
	public boolean hasPrevious() {
		if (contents == null) {
			readDir();
			if (contents == null)
				return false;
		}
		if (curPos <= 0)
			return false;
		return true;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isRecurseIntoSubdirectories() {
		return recurseIntoSubdirectories;
	}

	/**
	 * 
	 */
	public void jumpToEnd() {
		if (contents == null)
			readDir();
		if (contents != null)
			curPos = contents.length;
	}

	/**
	 * 
	 */
	public void jumpToStart() {
		curPos = 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Iterator#next()
	 */
	// @Override
	public String next() {
		if (curPos < (contents.length)) {
			return contents[curPos++];
		} else
			return null;
	}

	/**
	 * 
	 * @return
	 */
	public String previous() {
		if (curPos > 0) {
			return contents[--curPos];
		} else
			return null;
	}

	/**
	 * 
	 * @return
	 */
	private ArrayList<String> readDir() {
		return readDir(this.path);
	}

	/**
	 * 
	 * @param path
	 * @return
	 */
	private ArrayList<String> readDir(String path) {
		path = appendSlash(path);
		File fPath = new File(path);
		if (!fPath.isDirectory()) {
			System.err.println("'" + path + "' is not a directory.");
			return null;
		}
		String[] allFiles = fPath.list();

		// Add all files which match extension and recurse into subdirs
		// (appending the right path).
		String pathPrefix = path.replace(this.path, "");
		ArrayList<String> myFiles = new ArrayList<String>();
		if (allFiles!=null) {
		  for (String file : allFiles) {
		    if (recurseIntoSubdirectories
		        && new File(path + file).isDirectory()) {
		      myFiles.addAll(readDir(path + file));
		    } else {
		      if (extension == null || extension.trim().length() == 0) {
		        myFiles.add(pathPrefix + file);
		      } else {
		        if (file.toLowerCase().endsWith(extension.toLowerCase()))
		          myFiles.add(pathPrefix + file);
		      }
		    }
		  }
		}

		// To designated Array.
		if (path.equals(appendSlash(this.path))) {
			contents = new String[myFiles.size()];
			contents = myFiles.toArray(contents);
			Arrays.sort(contents); // XXX: Sort Alphabetically
		}

		return myFiles;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Iterator#remove()
	 */
	public void remove() {
		System.err.println("Remove not supported.");
	}

	/**
	 * 
	 */
	public void reset() {
		curPos = 0;
		contents = null;
	}

	/**
	 * 
	 * @param extension
	 */
	public void setExtension(String extension) {
		if (extension.contains("*"))
			extension = extension.replace("*", ""); // Prevent things like
		// "*.dat"
		this.extension = extension;
		reset();
	}

	/**
	 * 
	 * @param path
	 */
	public void setPath(String path) {
		this.path = getPath(path); // Append / or \\
		reset();
	}

	/**
	 * 
	 * @param recurseIntoSubdirectories
	 */
	public void setRecurseIntoSubdirectories(boolean recurseIntoSubdirectories) {
		if (this.recurseIntoSubdirectories != recurseIntoSubdirectories) {
			this.recurseIntoSubdirectories = recurseIntoSubdirectories;
			reset();
		}
	}

}
