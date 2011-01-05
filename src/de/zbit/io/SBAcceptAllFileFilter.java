/**
 * 
 */
package de.zbit.io;

import java.io.File;
import java.util.ResourceBundle;

import de.zbit.util.ResourceManager;

/**
 * A {@link GeneralFileFilter} that accepts any kind of file or directory.
 * 
 * @author Andreas Dr&auml;ger
 * @date 2011-01-04
 */
public class SBAcceptAllFileFilter extends GeneralFileFilter {
	
	/**
	 * 
	 */
	public SBAcceptAllFileFilter() {
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
	 */
	public boolean accept(File f) {
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.filechooser.FileFilter#getDescription()
	 */
	public String getDescription() {
		ResourceBundle resource = ResourceManager
				.getBundle("de.zbit.locales.Labels");
		return resource.getString("ACCEPT_ALL_FILES");
	}
	
}
