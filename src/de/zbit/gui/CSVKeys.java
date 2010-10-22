/**
 * 
 */
package de.zbit.gui;

/**
 * @author Andreas Dr&auml;ger
 * @date 2010-10-22
 */
public interface CSVKeys {
	/**
	 * Key to specify the default directory for Comma Separated Value (CSV)
	 * files.
	 */
	public static final String CSV_FILES_OPEN_DIR = "CSV_FILES_OPEN_DIR";
	/**
	 * A comma-separated file to be opened.
	 */
	public static final String CSV_FILE = "CSV_FILE";
	/**
	 * The character that is used to quote strings inside of comma separated
	 * value files.
	 */
	public static final String CSV_FILES_QUOTE_CHAR = "CSV_FILES_QUOTE_CHAR";
	/**
	 * The default directory where comma separated value files are storted.
	 */
	public static final String CSV_FILES_SAVE_DIR = "CSV_FILES_SAVE_DIR";
	/**
	 * The separator character that is written between the entries of a comma
	 * separated value file. Not that actually any UTF8 character can be used as
	 * a separator, not only commas.
	 */
	public static final String CSV_FILES_SEPARATOR_CHAR = "CSV_FILES_SEPARATOR_CHAR";
}
