/**
 * 
 */
package de.zbit.gui;

import java.io.File;

import de.zbit.util.Option;

/**
 * A collection of meaningful {@link Option} instances for graphical user
 * interfaces.
 * 
 * @author Andreas Dr&auml;ger
 * @date 2010-10-22
 */
public interface GUIOptions {
	/**
	 * The path to the associated configuration file, which contains one default
	 * value for each option defined in this interface.
	 */
	public static final String CONFIG_FILE_LOCATION = "GUI_Configuration.xml";
	/**
	 * Decide whether or not SBMLsqueezer should search for updates at start-up.
	 */
	public static final Option<Boolean> CHECK_FOR_UPDATES = new Option<Boolean>(
		"CHECK_FOR_UPDATES", Boolean.class,
		"Decide whether or not SBMLsqueezer should search for updates at start-up.", true);
	/**
	 * Standard directory where user files can be found.
	 */
	public static final Option<File> OPEN_DIR = new Option<File>("OPEN_DIR", File.class,
		"Standard directory where user files can be found.", System.getProperty("user.dir"));
	/**
	 * Standard directory where the user may save some files.
	 */
	public static final Option<File> SAVE_DIR = new Option<File>("SAVE_DIR", File.class,
		"Standard directory where the user may save some files.", System.getProperty("user.dir"));
	/**
	 * Can be used in combination with = true or = false or just --gui. Specifies
	 * whether or not a program should display its graphical user interface.
	 */
	public static final Option<Boolean> GUI = new Option<Boolean>(
		"GUI", Boolean.class,
		"Can be used in combination with = true or = false or just --gui. Specifies whether or not a program should display its graphical user interface.",
		true);
	
}
