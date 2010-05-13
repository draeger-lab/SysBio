package de.zbit.io;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * Filter for SBML files.
 * 
 * @author draeger
 * 
 */
public class SBMLFileFilter extends FileFilter implements java.io.FileFilter {

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
	 */
	@Override
	public boolean accept(File f) {
		String name = f.getName().toLowerCase();
		return f.isDirectory()
				|| (f.isFile() && (name.endsWith(".sbml") || name
						.endsWith(".xml")));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.filechooser.FileFilter#getDescription()
	 */
	@Override
	public String getDescription() {
		return "SBML files (*.sbml, *.xml)";
	}

}
