/**
 * 
 */
package de.zbit.gui;

/**
 * @author Andreas Dr&auml;ger
 * @date 2010-10-22
 */
public interface GUIKeys {
	/**
	 * Decide whether or not SBMLsqueezer should search for updates at start-up.
	 */
	public static final String CHECK_FOR_UPDATES = "CHECK_FOR_UPDATES";
	/**
	 * Standard directory where user files can be found.
	 */
	public static final String OPEN_DIR = "OPEN_DIR";
	/**
	 * Standard directory where the user may save some files.
	 */
	public static final String SAVE_DIR = "SAVE_DIR";
	/**
	 * Can be used in combination with = true or = false or just --gui.
	 * Specifies whether or not a program should display its graphical user
	 * interface.
	 */
	public static final String GUI = "GUI";
	/**
	 * The minimal value for JSpinners in the GUI.
	 */
	public static final String SPINNER_MIN_VALUE = "SPINNER_MIN_VALUE";
	/**
	 * The maximal value for JSpinners in the GUI.
	 */
	public static final String SPINNER_MAX_VALUE = "SPINNER_MAX_VALUE";
	/**
	 * This is important for the graphical user interface as it defines the step
	 * size between two values in input masks.
	 */
	public static final String SPINNER_STEP_SIZE = "SPINNER_STEP_SIZE";
}
