package de.zbit.util;

import java.io.File;

/**
 * Very simple extension of {@link java.io.File} that should
 * represent directories.
 * @author wrzodek
 */
public class Directory extends File {
	private static final long serialVersionUID = -578765693667742535L;
	
	/**
	 * @param pathname
	 */
	public Directory(String pathname) {
		super(pathname);
		// Do not throw an exception... else, this
		// class can't be used in constructors.
		if (!isDirectory()) System.err.println("Not a directory.");
	}
	
}
