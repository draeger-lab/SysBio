/**
 * 
 */
package de.zbit.gui;

import java.io.File;
import java.util.ResourceBundle;

import de.zbit.io.SBFileFilter;
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
 * @author wrzodek
 * @date 2010-10-22
 */
public interface GUIOptions extends KeyProvider {
	
	/**
	 * The collection of labels, i.e., descriptions for all options.
	 */
	ResourceBundle bundle = ResourceManager.getBundle("de.zbit.locales.Labels");
	
	/**
	 * An enumeration of supported language codes.
	 * 
	 * @author Andreas Dr&auml;ger
	 * @date 2011-01-09
	 */
	public static enum Language {
		/**
		 * German
		 */
		de,
		/**
		 * English
		 */
		en;
		
		/**
		 * Checks the {@link System} for its default user language and returns the
		 * corresponding {@link Enum} element.
		 * 
		 * @return
		 */
		public static Language getDefault() {
			Language language = en;
			try {
				language = valueOf(System.getProperty("user.language"));
			} catch (Throwable exc) {
				// TODO: Logging, no support for the user's language.
			}
			return language;
		}
	}
	
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
		Boolean.TRUE);
	
	/**
	 * The language for the user interface, error messages and so on.
	 */
	public static final Option<Language> LANGUAGE = new Option<Language>(
		"LANGUAGE", Language.class,
		bundle.getString("LANGUAGE"),
		new Range<Language>(Language.class, Range.toRangeString(Language.class)),
		Language.getDefault());
	
	/**
	 * Standard directory where user files can be found.
	 */
	public static final Option<File> OPEN_DIR = new Option<File>("OPEN_DIR",
		File.class, bundle.getString("OPEN_DIR"),
		new Range<File>(File.class, SBFileFilter.createDirectoryFilter()), new File(System
				.getProperty("user.dir")));
	
	/**
	 * Standard directory where the user may save some files.
	 */
	public static final Option<File> SAVE_DIR = new Option<File>("SAVE_DIR",
		File.class, bundle.getString("SAVE_DIR"),
		new Range<File>(File.class, SBFileFilter.createDirectoryFilter()), new File(System
				.getProperty("user.dir")));
	
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
	
}
