/**
 * 
 */
package de.zbit.io;

import java.io.File;

import de.zbit.util.prefs.KeyProvider;
import de.zbit.util.prefs.Option;

/**
 * A collection of meaningful {@link Option} instances for parsing and writing
 * of comma-separated value files.
 * 
 * @author Andreas Dr&auml;ger
 * @date 2010-10-22
 */
public interface CSVOptions extends KeyProvider {
	/**
	 * A comma-separated file to be opened.
	 */
	public static final Option<File> CSV_FILE = new Option<File>("CSV_FILE",
		File.class, "A comma-separated file to be opened.", new File(System
				.getProperty("user.dir")));
	/**
	 * Key to specify the default directory for Comma Separated Value (CSV) files.
	 */
	public static final Option<File> CSV_FILES_OPEN_DIR = new Option<File>(
		"CSV_FILES_OPEN_DIR",
		File.class,
		"Key to specify the default directory for Comma Separated Value (CSV) files.",
		new File(System.getProperty("user.dir")));
	/**
	 * The character that is used to quote strings inside of comma separated value
	 * files.
	 */
	public static final Option<Character> CSV_FILES_QUOTE_CHAR = new Option<Character>(
		"CSV_FILES_QUOTE_CHAR",
		Character.class,
		"The character that is used to quote strings inside of comma separated value files.",
		Character.valueOf('"'));
	/**
	 * The default directory where comma separated value files are stored.
	 */
	public static final Option<File> CSV_FILES_SAVE_DIR = new Option<File>(
		"CSV_FILES_SAVE_DIR", File.class,
		"The default directory where comma separated value files are stored.",
		new File(System.getProperty("user.dir")));
	
	/**
	 * The separator character that is written between the entries of a comma
	 * separated value file. Not that actually any UTF8 character can be used as a
	 * separator, not only commas.
	 */
	public static final Option<Character> CSV_FILES_SEPARATOR_CHAR = new Option<Character>(
		"CSV_FILES_SEPARATOR_CHAR",
		Character.class,
		"The separator character that is written between the entries of a comma separated value file. Not that actually any UTF8 character can be used as a separator, not only commas.",
		Character.valueOf(','));
}
