/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2011 by the University of Tuebingen, Germany.
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
import java.util.ResourceBundle;

import de.zbit.util.ResourceManager;
import de.zbit.util.prefs.KeyProvider;
import de.zbit.util.prefs.Option;
import de.zbit.util.prefs.OptionGroup;
import de.zbit.util.prefs.Range;

/**
 * A collection of meaningful {@link Option} instances for parsing and writing
 * of comma-separated value files.
 * 
 * @author Andreas Dr&auml;ger
 * @date 2010-10-22
 * @version $Rev$
 * @since 1.0
 */
public interface CSVOptions extends KeyProvider {
  
  /**
   * For localization.
   */
  static final ResourceBundle bundle = ResourceManager
      .getBundle("de.zbit.locales.Labels");

	/**
	 * A comma-separated file to be opened.
	 */
	public static final Option<File> CSV_FILE = new Option<File>("CSV_FILE",
		File.class, bundle.getString("CSV_FILE_TOOLTIP"), new File(
			System.getProperty("user.dir")), bundle.getString("CSV_FILE"));
  
	/**
	 * Key to specify the default directory for Comma Separated Value (CSV) files.
	 */
	public static final Option<File> CSV_FILES_OPEN_DIR = new Option<File>(
		"CSV_FILES_OPEN_DIR", File.class,
		bundle.getString("CSV_FILES_OPEN_DIR_TOOLTIP"), new File(
			System.getProperty("user.dir")), bundle.getString("CSV_FILES_OPEN_DIR"),
		false);

	/**
	 * The character that is used to quote strings inside of comma separated value
	 * files.
	 */
	public static final Option<Character> CSV_FILES_QUOTE_CHAR = new Option<Character>(
		"CSV_FILES_QUOTE_CHAR", Character.class,
		bundle.getString("CSV_FILES_QUOTE_CHAR_TOOLTIP"), Character.valueOf('"'),
		bundle.getString("CSV_FILES_QUOTE_CHAR"));

	/**
	 * The default directory where comma separated value files are stored.
	 */
	public static final Option<File> CSV_FILES_SAVE_DIR = new Option<File>(
		"CSV_FILES_SAVE_DIR", File.class,
		bundle.getString("CSV_FILES_SAVE_DIR_TOOLTIP"), new File(
			System.getProperty("user.dir")), bundle.getString("CSV_FILES_SAVE_DIR"),
		false);

	/**
	 * The separator character that is written between the entries of a comma
	 * separated value file. Not that actually any UTF8 character can be used as a
	 * separator, not only commas.
	 */
	public static final Option<Character> CSV_FILES_SEPARATOR_CHAR = new Option<Character>(
		"CSV_FILES_SEPARATOR_CHAR", Character.class,
		bundle.getString("CSV_FILES_SEPARATOR_CHAR_TOOLTIP"), new Range<Character>(
			Character.class, ',', ';', '|', '/', '\t', ' '), Character.valueOf(','),
		bundle.getString("CSV_FILES_SEPARATOR_CHAR"));

  /**
   * Groups all options for CSV file selection
   */
	@SuppressWarnings("unchecked")
	public static final OptionGroup<File> CSV_FILE_SELECTION = new OptionGroup<File>(
		bundle.getString("CSV_FILE_SELECTION"),
		bundle.getString("CSV_FILE_SELECTION_TOOLTIP"), CSV_FILE,
		CSV_FILES_OPEN_DIR, CSV_FILES_SAVE_DIR);
  
  /**
   * Groups the special characters that are important in CSV files.
   */
	@SuppressWarnings("unchecked")
	public static final OptionGroup<Character> CSV_FILE_CHARACTERS = new OptionGroup<Character>(
		bundle.getString("CSV_FILE_CHARACTERS"),
		bundle.getString("CSV_FILE_CHARACTERS_TOOLTIP"), CSV_FILES_SEPARATOR_CHAR,
		CSV_FILES_QUOTE_CHAR);

}
