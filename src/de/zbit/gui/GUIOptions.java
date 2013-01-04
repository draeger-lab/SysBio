/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2013 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.gui;

import java.awt.Frame;
import java.io.File;
import java.util.ResourceBundle;
import java.util.logging.Level;

import de.zbit.io.filefilter.SBFileFilter;
import de.zbit.util.Reflect;
import de.zbit.util.ResourceManager;
import de.zbit.util.prefs.KeyProvider;
import de.zbit.util.prefs.Option;
import de.zbit.util.prefs.OptionGroup;
import de.zbit.util.prefs.Range;

/**
 * A collection of meaningful {@link Option} instances for graphical user
 * interfaces.
 * 
 * @author Andreas Dr&auml;ger
 * @author Clemens Wrzodek
 * @date 2010-10-22
 * @version $Rev$
 * @since 1.0
 */
public interface GUIOptions extends KeyProvider {
	
	/**
	 * The collection of labels, i.e., descriptions for all options.
	 */
	ResourceBundle bundle = ResourceManager.getBundle("de.zbit.locales.Labels");
	
	/**
	 * Decide whether or not this program should search for updates at start-up.
	 */
	public static final Option<Boolean> CHECK_FOR_UPDATES = new Option<Boolean>(
		"CHECK_FOR_UPDATES",
		Boolean.class,
		bundle.getString("CHECK_FOR_UPDATES"),
		Boolean.TRUE);
	
	/**
	 * Can be used in combination with = true or = false or just --gui. Specifies
	 * whether or not a program should display its graphical user interface.
	 */
	public static final Option<Boolean> GUI = new Option<Boolean>(
		"GUI",
		Boolean.class,
		bundle.getString("GUI"),
		Boolean.FALSE);
	
	/**
	 * Standard directory where user files can be found.
	 */
	public static final Option<File> OPEN_DIR = new Option<File>("OPEN_DIR",
		File.class, bundle.getString("OPEN_DIR"), new Range<File>(File.class,
			SBFileFilter.createDirectoryFilter()), new File(
			System.getProperty("user.dir")), false);
	
	/**
	 * Standard directory where the user may save some files.
	 */
	public static final Option<File> SAVE_DIR = new Option<File>("SAVE_DIR",
		File.class, bundle.getString("SAVE_DIR"), new Range<File>(File.class,
			SBFileFilter.createDirectoryFilter()), new File(
			System.getProperty("user.dir")), false);
	
	/**
	 * Define the default directories to open and save files. These directories
	 * will be used as the first search target when selecting files in this
	 * graphical user interface.
	 */
	@SuppressWarnings("unchecked")
	public static final OptionGroup<File> DEFAULT_DIRECTORIES = new OptionGroup<File>(
		bundle.getString("DEFAULT_DIRECTORIES"),
		bundle.getString("DEFAULT_DIRECTORIES_TOOLTIP"),
		OPEN_DIR, SAVE_DIR);
	
	// TODO: This will lead to the strange effect that ALL windows will have the identical size! 
	// The same holds truth for OPEN_DIR, SAVE_DIR, DEFAULT_DIRECTORIES, etc.

	/**
	 * Defines the width of the window.
	 */
	public static final Option<Integer> WINDOW_WIDTH = new Option<Integer>(
		"WINDOW_WIDTH", Integer.class, bundle.getString("WINDOW_WIDTH"),
		Integer.valueOf(640), false);
	
	/**
	 * Defines the height of the window.
	 */
	public static final Option<Integer> WINDOW_HEIGHT = new Option<Integer>(
		"WINDOW_HEIGHT", Integer.class, bundle.getString("WINDOW_HEIGHT"),
		Integer.valueOf(480), false);

   /**
    * The last window state (iconified, maximized, etc).
    */
   public static final Option<Integer> WINDOW_STATE = new Option<Integer>("WINDOW_STATE",
       Integer.class,"State of the window (maximized, minimized, etc).",Frame.NORMAL, false);
	
   /**
    * This allows to change the Log-{@link Level}.
    * It it intented to not allow levels ALL or OFF
    * (giving bounds of -/+ Intenger maxvalues is not
    * resonable for the range).
    */
   public static final Option<String> LOG_LEVEL = new Option<String>(
       "LOG_LEVEL", String.class, "Change the log-level of this application.",
       new Range<String>(String.class, Reflect.getStaticFinalVariablesAsEnumeration(Level.class)),
       Level.INFO.getName());
   
}
