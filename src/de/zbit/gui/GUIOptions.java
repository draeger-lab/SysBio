/**
 * 
 */
package de.zbit.gui;

import java.io.File;

import de.zbit.io.Directory;
import de.zbit.util.prefs.KeyProvider;
import de.zbit.util.prefs.Option;

/**
 * A collection of meaningful {@link Option} instances for graphical user
 * interfaces.
 * 
 * @author Andreas Dr&auml;ger
 * @author wrzodek
 * @date 2010-10-22
 */
public interface GUIOptions extends KeyProvider {
	/**
	 * Decide whether or not SBMLsqueezer should search for updates at start-up.
	 */
	public static final Option<Boolean> CHECK_FOR_UPDATES = new Option<Boolean>(
		"CHECK_FOR_UPDATES",
		Boolean.class,
		"Decide whether or not SBMLsqueezer should search for updates at start-up.",
		Boolean.TRUE);
	/**
	 * Can be used in combination with = true or = false or just --gui. Specifies
	 * whether or not a program should display its graphical user interface.
	 */
	public static final Option<Boolean> GUI = new Option<Boolean>(
		"GUI",
		Boolean.class,
		"Can be used in combination with = true or = false or just --gui. Specifies whether or not a program should display its graphical user interface.",
		Boolean.TRUE);
	/**
	 * Standard directory where user files can be found.
	 */
	public static final Option<Directory> OPEN_DIR = new Option<Directory>("OPEN_DIR",
			Directory.class, "Standard directory where user files can be found.", new Directory(
			System.getProperty("user.dir")));
	/**
	 * Standard directory where the user may save some files.
	 */
	public static final Option<Directory> SAVE_DIR = new Option<Directory>("SAVE_DIR",
			Directory.class, "Standard directory where the user may save some files.",
		new Directory(System.getProperty("user.dir")));
	
}
