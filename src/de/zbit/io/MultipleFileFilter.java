/**
 * 
 */
package de.zbit.io;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * @author Andreas Dr&auml;ger
 * @date 2010-10-22
 * 
 */
public class MultipleFileFilter extends FileFilter implements
		java.io.FileFilter {

	/**
	 * 
	 */
	private String description;
	private FileFilter[] filters;

	/**
	 * 
	 */
	public MultipleFileFilter(String description, FileFilter... filters) {
		super();
		this.description = description;
		this.filters = filters;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
	 */
	public boolean accept(File f) {
		for (FileFilter filter : filters) {
			if (filter.accept(f)) {
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.filechooser.FileFilter#getDescription()
	 */
	public String getDescription() {
		return description;
	}

}
