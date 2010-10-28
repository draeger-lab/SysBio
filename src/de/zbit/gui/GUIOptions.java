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
    public static final Option CHECK_FOR_UPDATES = new Option(
	"CHECK_FOR_UPDATES", Boolean.class,
	"Decide whether or not SBMLsqueezer should search for updates at start-up.");
    /**
     * Standard directory where user files can be found.
     */
    public static final Option OPEN_DIR = new Option("OPEN_DIR", File.class,
	"Standard directory where user files can be found.");
    /**
     * Standard directory where the user may save some files.
     */
    public static final Option SAVE_DIR = new Option("SAVE_DIR", File.class,
	"Standard directory where the user may save some files.");
    /**
     * Can be used in combination with = true or = false or just --gui.
     * Specifies whether or not a program should display its graphical user
     * interface.
     */
    public static final Option GUI = new Option(
	"GUI",
	Boolean.class,
	"Can be used in combination with = true or = false or just --gui. Specifies whether or not a program should display its graphical user interface.");

}
