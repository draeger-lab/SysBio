/**
 * 
 */
package de.zbit.io;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Pattern;

/**
 * FileFilter that accepts file with a filename matching the given regular
 * expression. The directory name is ignored
 * 
 * @author Florian Mittag
 */
public class RegExpFileFilter implements FileFilter {

	/**
	 * 
	 */
	protected final Pattern p;

	/**
	 * 
	 * @param regexp
	 */
	public RegExpFileFilter(String regexp) {
		p = Pattern.compile(regexp);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.FileFilter#accept(java.io.File)
	 */
	public boolean accept(File pathname) {
		return p.matcher(pathname.getName()).matches();
	}

}
