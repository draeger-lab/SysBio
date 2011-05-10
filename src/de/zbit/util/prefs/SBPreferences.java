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
package de.zbit.util.prefs;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.SortedMap;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import argparser.ArgParser;
import argparser.BooleanHolder;
import argparser.CharHolder;
import argparser.DoubleHolder;
import argparser.FloatHolder;
import argparser.IntHolder;
import argparser.LongHolder;
import argparser.StringHolder;
import de.zbit.io.GeneralFileFilter;
import de.zbit.util.Reflect;
import de.zbit.util.ResourceManager;
import de.zbit.util.Utils;

/**
 * This class is a wrapper for {@link Preferences}, which provides a lot of
 * additional functionality.
 * 
 * @author Andreas Dr&auml;ger
 * @author Clemens Wrzodek
 * @date 2010-10-24
 * @version $Rev$
 * @since 1.0
 */
public class SBPreferences implements Map<Object, Object> {
	
	/**
	 * The location of the localized warnings messages.
	 */
	public static final String WARNINGS_LOCATION = "de.zbit.locales.Warnings";
	
	/**
	 * The logger of this {@link Class}.
	 */
	public static final Logger logger = Logger.getLogger(SBPreferences.class.getName());
	
	/**
	 * The main class, used to start the application.
	 */
	private static Class<?> mainClass = null;
	
	/**
	 * @author Andreas Dr&auml;ger
	 * @date 2010-10-24
	 */
	private class Entry implements Map.Entry<Object, Object> {
		
		/**
		 * 
		 */
		private final Object key;
		/**
		 * 
		 */
		private Object value;
		
		/**
		 * @param key
		 * @param value
		 */
		public Entry(Object key, Object value) {
			this.key = key;
			this.value = value;
		}
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Map.Entry#getKey()
		 */
		public Object getKey() {
			return key;
		}
		
		/*
		 * (non-Javadoc)
		 * @see java.util.Map.Entry#getValue()
		 */
		public Object getValue() {
			return value;
		}
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Map.Entry#setValue(java.lang.Object)
		 */
		public Object setValue(Object value) {
			return this.value = value;
		}
		
	}
	
	/**
	 * Pointers to avoid senselessly parsing configuration files again and again.
	 */
	private static final Map<String, SBProperties> allDefaults = new HashMap<String, SBProperties>();

	/**
	 * Indicates whether to clean invalid user prefs for when loading defaults.
	 */
	private static boolean clean;
	
	/**
	 * @param keyProvider
	 * @param relPath
	 * @param persist
	 * @param usage
	 * @param args
	 * @return
	 * @throws IOException
	 * @throws BackingStoreException
	 */
	public static final SBProperties analyzeCommandLineArguments(
		Class<? extends KeyProvider> keyProvider, String relPath, boolean persist,
		String usage, String args[]) throws IOException, BackingStoreException {
		SBPreferences prefs = getPreferencesFor(keyProvider, relPath);
		return prefs.analyzeCommandLineArguments(usage, args, persist);
	}
	
	/**
	 * @param keyProvider
	 * @param usage
	 * @param args
	 * @return
	 */
	public static final SBProperties analyzeCommandLineArguments(
		Class<? extends KeyProvider> keyProvider, String usage, String args[]) {
		return analyzeCommandLineArguments(keyProvider, usage, args, null);
	}
	
	/**
	 * @param keyProvider
	 * @param usage
	 * @param args
	 * @param defaults
	 * @return
	 */
	public static final SBProperties analyzeCommandLineArguments(
		Class<? extends KeyProvider> keyProvider, String usage, String args[],
		Map<Object, Object> defaults) {
		SBProperties props;
		if (defaults != null) {
			props = new SBProperties(new SBProperties());
		} else {
			props = new SBProperties();
		}
		// create the parser and specify the allowed options ...
		ArgParser parser = new ArgParser(usage);
		Map<Option<?>, Object> options = configureArgParser(parser,
			new HashMap<Option<?>, Object>(), keyProvider, props, defaults);
		parser.matchAllArgs(args);
		Option<?> option;
		for (Map.Entry<Option<?>, Object> entry : options.entrySet()) {
			option = entry.getKey();
			if (option.isSetRangeSpecification()) {
				if (!option.getRange().castAndCheckIsInRange(entry.getValue())) {
					ResourceBundle resources = ResourceManager.getBundle(WARNINGS_LOCATION);
					parser.printErrorAndExit(String.format(resources.getString("OPTION_OUT_OF_RANGE"),
						option));
				}
			}
		}
		putAll(props, options);
		return props;
	}
	
	/**
	 * @param keyProvider
	 * @param relPath
	 * @param usage
	 * @param args
	 * @return
	 */
	public static final SBProperties analyzeCommandLineArguments(
		Class<? extends KeyProvider> keyProvider, String relPath, String usage,
		String args[]) {
		try {
			return analyzeCommandLineArguments(keyProvider, relPath, false, usage,
				args);
		} catch (Exception e) {
			// This can actually never happen because we don't persist anything.
			return new SBProperties();
		}
	}
	
	/**
	 * 
	 * @param defKeys
	 * @param args
	 * @return
	 */
	public static SBProperties analyzeCommandLineArguments(
		Class<? extends KeyProvider> defKeys, String[] args) {
		List<Class<? extends KeyProvider>> l = new ArrayList<Class<? extends KeyProvider>>(
			1);
		l.add(defKeys);
		
		return analyzeCommandLineArguments(l, args);
	}
	
	/**
	 * 
	 * @param defKeys
	 * @param args
	 * @return
	 */
	public static SBProperties analyzeCommandLineArguments(
		Class<? extends KeyProvider>[] defKeys, String[] args) {
		return analyzeCommandLineArguments(Arrays.asList(defKeys), args);
	}
	
	/**
	 * @param defKeys
	 * @param args
	 * @return
	 */
	public static SBProperties analyzeCommandLineArguments(
		List<Class<? extends KeyProvider>> defKeys, String[] args) {
		String usage = generateUsageString();
		SBPreferences prefs[] = new SBPreferences[defKeys.size()];
		Map<Option<?>, Object> options = new HashMap<Option<?>, Object>();
		ArgParser parser = new ArgParser(usage);
		SBProperties props = new SBProperties(new SBProperties());
		
		// Configure argument parser by passing all possible option definitions
		// to it.
		Class<? extends KeyProvider> entry;
		for (int i = 0; i < defKeys.size(); i++) {
			entry = defKeys.get(i);
			try {
				prefs[i] = getPreferencesFor(entry);
				configureArgParser(parser, options, entry, props, loadDefaults(entry));
				
			} catch (Exception e) {
				ResourceBundle resources = ResourceManager.getBundle(WARNINGS_LOCATION);
				Exception exc = new Exception(String.format(
					resources.getString("COULD_NOT_LOAD_PROPERTIES_MESSAGE"), entry.getName()), e);
				exc.printStackTrace();
			}
		}
		
		return analyzeCommandLineArguments(prefs, options, parser, props, usage,
			args);
	}
	
	/**
	 * Parses the given command line arguments using the {@link ArgParser} and
	 * returns a {@link SBProperties} object containing those values. The values
	 * then present in this {@link SBProperties} object will then be made
	 * persistent for all {@link SBPreferences} that have been given to this
	 * method.
	 * 
	 * 
	 * @param prefs the {@link SBPreferences} for which the parsed values should
	 *              be stored persistently
	 * @param options a mapping from {@link Option}s to argument holders
	 * @param parser the {@link ArgParser}
	 * @param props the {@link SBProperties} object where the parsed values will
	 *              be stored
	 * @param usage the usage string
	 * @param args the command line arguments
	 * @return the {@link SBProperties} object where the parsed values are stored
	 */
	private static final SBProperties analyzeCommandLineArguments(
		SBPreferences[] prefs, Map<Option<?>, Object> options, ArgParser parser,
		SBProperties props, String usage, String args[]) {
		
		// Do the actual parsing.
	  // XXX: ArgParser does NOT distinguish between default options and 
	  // user set options. Actually, one has to modify the argparser
	  // and add a new flag to the argument holder, if it is a default value.
		parser.matchAllArgs(args);
		
		// put all read options into this SBProperties object
		putAll(props, options);
		
		// Now all command-line arguments must be made persistent:
		String k, property, value;
		for (int i = 0; i < prefs.length; i++) {
			if (prefs[i] == null) {
				continue;
			}
			// for each possible key (option) of this SBPreferences object
			for (Object key : prefs[i].keySetFull()) {
				k = key.toString();
				// if the SBProperties object contains a value for this option
				if (props.containsKey(k)) {
					property = props.getProperty(k);
					value = prefs[i].getString(k);
					// and if the value in SBPreferences differs from the one in the
					// SBProperties
					if (!value.equals(property)) {
					  // override the SBPreferences value with the SBProperties one
						prefs[i].put(k, property);
					}
				}
			}
			try {
			  // make all preferences persistent
				prefs[i].flush();
			} catch (BackingStoreException e) {
				ResourceBundle resources = ResourceManager.getBundle(WARNINGS_LOCATION);
				Exception exc = new Exception(String.format(
					resources.getString("BACKING_STORE_EXCEPTION_MESSAGE"),
					prefs[i].getKeyProvider().getName()), e);
				exc.printStackTrace();
			}
		}
		
		return props;
	}
	
	/**
	 * Automatically builds the usage string.
	 * 
	 * @param defFileAndKeys
	 *        This {@link List} is supposed to contain the relative paths of the
	 *        configuration files with default preferences as first value and each
	 *        corresponding second value must be the full class name of the
	 *        corresponding keyProvider class, i.e., the name of some
	 *        {@link Class} object that contains as many static final fields of
	 *        {@link Option} instances as the corresponding defaults option file
	 *        contains entries.
	 * @param args
	 *        The given command-line arguments.
	 * @return
	 */
	public static final SBProperties analyzeCommandLineArguments(
		SortedMap<String, Class<? extends KeyProvider>> defFileAndKeys,
		String args[]) {
		
		return analyzeCommandLineArguments(defFileAndKeys, generateUsageString(),
			args);
	}
	
	/**
	 * @param defFileAndKeys
	 *        This {@link List} is supposed to contain the relative paths of the
	 *        configuration files with default preferences as first value and each
	 *        corresponding second value must be the full class name of the
	 *        corresponding keyProvider class, i.e., the name of some
	 *        {@link Class} object that contains as many static final fields of
	 *        {@link Option} instances as the corresponding defaults option file
	 *        contains entries.
	 * @param usage
	 *        A String describing how to invoke the class that is calling this
	 *        method.
	 * @param args
	 *        The given command-line arguments.
	 * @return
	 */
	public static final SBProperties analyzeCommandLineArguments(
		SortedMap<String, Class<? extends KeyProvider>> defFileAndKeys,
		String usage, String args[]) {
		
		SBPreferences prefs[] = new SBPreferences[defFileAndKeys.size()];
		Map<Option<?>, Object> options = new HashMap<Option<?>, Object>();
		ArgParser parser = new ArgParser(usage);
		SBProperties props = new SBProperties(new SBProperties());
		
		// Configure argument parser by passing all possible option definitions
		// to it.
		int i = 0;
		for (Map.Entry<String, Class<? extends KeyProvider>> entry : defFileAndKeys
				.entrySet()) {
			try {
				prefs[i] = getPreferencesFor(entry.getValue(), entry.getKey());
				options.putAll(configureArgParser(parser, options, entry.getValue(),
					props, loadDefaults(entry.getValue(), entry.getKey())));
				
			} catch (Exception e) {
				ResourceBundle resources = ResourceManager.getBundle(WARNINGS_LOCATION);
				Exception exc = new Exception(String.format(
					resources.getString("COULD_NOT_LOAD_PROPERTIES_FROM_FILE"), entry
							.getValue().getName(), entry.getKey()), e);
				exc.printStackTrace();
			} finally {
				i++;
			}
		}
		
		return analyzeCommandLineArguments(prefs, options, parser, props, usage,
			args);
	}
	
	/**
	 * Adds all {@link Option}s of the {@link KeyProvider} to the
	 * {@link ArgParser}. If default values for the options are passed in a
	 * {@link Map} with the keys being either the {@link Option} or a
	 * {@link String} representing the option, the {@link ArgParser} will be
	 * configured to use these as default values for the argument holders and they
	 * are stored as default values in the given {@link SBProperties} object.
	 * 
	 * Also, the mapping of {@link Option}s to their argument holder is stored in
	 * the given <code>options</code> map.
	 * 
	 * @param parser the {@link ArgParser} to be configured
	 * @param options a mapping from {@link Option}s to their argument holders
	 * @param keyProvider the {@link KeyProvider} containing the {@link Option}s
	 * @param props an {@link SBProperties} object in which the default values
	 *              will be stored
	 * @param defaults the default values
	 * @return the mapping from {@link Option}s to their argument holders
	 */
	private static Map<Option<?>, Object> configureArgParser(ArgParser parser,
		Map<Option<?>, Object> options, Class<? extends KeyProvider> keyProvider,
		SBProperties props, Map<Object, Object> defaults) {
		// Iterates over all field of the keyProvider
		for (Field f : keyProvider.getFields()) {
			try {
			  Object fieldValue = f.get(keyProvider);
			  Object argHolder;
				// If the current field is an Option, add it to the ArgParser
				if (fieldValue instanceof Option<?>) {
				  Option<?> option = (Option<?>) fieldValue;
					String key = option.toString();
					if ((defaults != null) && defaults.containsKey(option)) {
						// We here set the default value as pre-defined value
						// if the given Map contains the corresponding key.
						// In this way we make sure that if the user does not
						// give a command-line argument for this option, we will
						// stick with the default value:
						argHolder = option.createArgumentHolder(defaults.get(option));
						props.getDefaults().put(key, defaults.get(option));
					} else {
						if ((defaults != null) && defaults.containsKey(key)) {
							argHolder = option.createArgumentHolder(defaults.get(key));
							props.getDefaults().put(key, defaults.get(key));
						} else {
							// If there is no default value available,
							// we cannot do anything...
							argHolder = option.createArgumentHolder();
						}
					}
					parser.addOption(option.getSpecification(), argHolder);
					options.put(option, argHolder);
				}
			} catch (Exception exc) {
				// This may happen if there are other fields than static options
				// in the key provider. Also happens if the wrong type of
				// argHolder has been added or created! But this method should
				// work fine... (I hope). You can check with -? option.
				// Just ignore...
			  // TODO: Then catch and ignore only this specific error, but still
			  // print other errors
			}
		}
		return options;
	}
	
	/**
	 * Automatically generates a usage string, based on the main class that has
	 * been called. Automatically detects, if the main class is in a jar file or
	 * not and builds the usage string.
	 * 
	 * @return usage String (also called synopsis).
	 */
	public static String generateUsageString() {
		if (mainClass==null) {
		  mainClass = Reflect.getMainClass(); 
		}
		
		String usage;
		if (mainClass == null) {
			// Should never happen...
			usage = "java [program_name]";
		} else {
			usage = generateUsageString(mainClass)
					+ String.format(" [%s]", ResourceManager.getBundle(
						"de.zbit.locales.Labels").getString("OPTIONS"));
		}
		
		return usage;
	}
	
	/**
	 * Generates a usage/synopsis string for the given mainClass. Looks if the
	 * class is inside a jar. If yes, <code>java -jar [NAME].jar</code> is the usage string.
	 * Else <code>java package.ClassName</code> is the usage string.
	 * 
	 * @param mainClass
	 *        of your project
	 * @return usage String
	 */
	public static String generateUsageString(Class<?> mainClass) {
		String synopsis = "java ";
		
		String jarName = Utils.getNameOfJar(mainClass);
		if (jarName != null && jarName.length() > 0) {
			synopsis += "-jar " + jarName;
		} else {
			// class.getName() also returns the package prefix.
			synopsis += mainClass.getName();
		}
		
		return synopsis;
	}
	
	/**
	 * 
	 * @param keyProvider
	 * @param relPath
	 * @return
	 * @throws IOException
	 */
	public static SBPreferences getPreferencesFor(
		Class<? extends KeyProvider> keyProvider) {
		return new SBPreferences(keyProvider);
	}
	
	/**
	 * @param keyProvider
	 * @param relPath
	 * @return
	 * @throws IOException
	 */
	public static SBPreferences getPreferencesFor(
		Class<? extends KeyProvider> keyProvider, String relPath)
		throws IOException {
		return new SBPreferences(keyProvider, relPath);
	}
	
	/**
	 * Parses the keyProvider for defaults values or loads the defaults from
	 * memory.
	 * 
	 * @param keyProvider
	 * @return
	 */
	private static SBProperties loadDefaults(
		Class<? extends KeyProvider> keyProvider) {
		try {
			return loadDefaults(keyProvider, null);
		} catch (IOException e) {
			// Can never happen
		}
		return null;
	}
	
	/**
	 * Parses the configuration file with defaults values or loads the defaults
	 * from memory.
	 * 
	 * @param keyProvider
	 * @param relPath
	 * @return
	 * @throws IOException
	 */
	private static SBProperties loadDefaults(
		Class<? extends KeyProvider> keyProvider, String relPath)
		throws IOException {
		SBProperties defaults;
		String path;
		boolean loadFromXML;
		if ((relPath != null) && (relPath.length() > 0)) {
			path = keyProvider.getPackage().getName().replace('.', '/') + '/'
					+ relPath;
			loadFromXML = true;
		} else {
			path = keyProvider.getName();
			loadFromXML = false;
		}
		if (!allDefaults.containsKey(path)) {
			defaults = new SBProperties();
			
			if (loadFromXML) {
				defaults.loadFromXML(keyProvider.getResourceAsStream(relPath));
			} else {
				defaults.loadFromKeyProvider(keyProvider);
			}
			
			Set<String> options = new HashSet<String>();
			Object fieldValue;
			String k, v;
			ResourceBundle resources = ResourceManager.getBundle(WARNINGS_LOCATION);
			for (Field field : keyProvider.getFields()) {
				try {
					fieldValue = field.get(keyProvider);
					if (fieldValue instanceof Option<?>) {
						k = fieldValue.toString();
						if (defaults.getProperty(k) == null) {
							throw new IllegalArgumentException(String.format(resources
									.getString("NO_DEFAULT_VALUE_FOR_OPTION_IN_FILE"), k, path));
						}
						options.add(k);
					}
				} catch (Exception exc) {
					// ignore non-static fields
					if (exc instanceof IllegalArgumentException) { 
						throw (IllegalArgumentException) exc; 
					}
				}
			}
			for (Map.Entry<Object, Object> e : defaults.entrySet()) {
				k = e.getKey().toString();
				v = e.getValue().toString();
				if (!options.contains(k)) { 
					throw new IllegalArgumentException(String
						.format(resources.getString("NO_SUCH_OPTION_DEFINED_BY_KEYPROVIDER"), 
							k, keyProvider.getName())); 
				}
				if (System.getProperties().containsKey(v)) {
					defaults.setProperty(k, System.getProperty(v));
				}
			}
			allDefaults.put(path, defaults);
			clean = true;
		} else {
			defaults = allDefaults.get(path);
			clean = false;
		}
		return defaults;
	}
	
	/**
	 * Convenient method to put all values gathered from a command line into an
	 * {@link SBProperties} table.
	 * 
	 * @param props
	 *        The {@link SBProperties} where to put all the values from the value
	 *        holders in the given {@link Map} data structure.
	 * @param options
	 *        A map between {@link Option} instances and {@link ArgParser} holders
	 *        for the desired values.
	 */
	private static void putAll(SBProperties props, Map<Option<?>, Object> options) {
		String k, value, v;
		for (Option<?> key : options.keySet()) {
			
			// try {
			k = key.toString();
			if (key.getRequiredType().equals(Float.class)) {
				value = Float.toString(((FloatHolder) options.get(key)).value);
			} else if (key.getRequiredType().equals(Double.class)) {
				value = Double.toString(((DoubleHolder) options.get(key)).value);
			} else if (key.getRequiredType().equals(Short.class)) {
				value = Short.toString((short) ((IntHolder) options.get(key)).value);
			} else if (key.getRequiredType().equals(Integer.class)) {
				value = Integer.toString(((IntHolder) options.get(key)).value);
			} else if (key.getRequiredType().equals(Long.class)) {
				value = Long.toString(((LongHolder) options.get(key)).value);
			} else if (key.getRequiredType().equals(Boolean.class)) {
				value = Boolean.toString(((BooleanHolder) options.get(key)).value);
			} else if (key.getRequiredType().equals(Character.class)) {
				value = Character.toString(((CharHolder) options.get(key)).value);
			} else if (key.getRequiredType().equals(String.class)) {
				value = ((StringHolder) options.get(key)).value;
			} else {
				value = ((StringHolder) options.get(key)).value;
				if (value != null) { value = value.toString(); }
			}
			// This is necessary, because at this point it can't be deduced if the
			// given options were explicitly set or just the default value.
			if (props.isSetDefaults()) {
				v = props.getProperty(k);
				if (v == null) {
					ResourceBundle resources = ResourceManager.getBundle(WARNINGS_LOCATION);
					throw new IllegalArgumentException(String.format(
					resources.getString("NO_VALUE_DEFINED_FOR_PROPERTY"), k)); 
				}
				// XXX: This has the very negative effect that if the command line option was
				// there and set to default, this information is erased right here.
				if (!v.equals(value)) {
					props.setProperty(k, value);
				}
			} else {
				props.setProperty(k, value);
			}
		}
	}
	
	/**
	 * Tries to persistently save all key-value pairs in the given
	 * {@link Properties} for the given {@link KeyProvider}.
	 * 
	 * @param keyProvider
	 * @param properties
	 * @throws BackingStoreException 
	 */
	
	@SuppressWarnings("rawtypes")
	public static void saveProperties(Class<? extends KeyProvider> keyProvider,
		Properties properties) throws BackingStoreException {
		List<Option> optionList = KeyProvider.Tools.optionList(keyProvider);
		Set<String> keySet = new HashSet<String>();
		for (Option o : optionList) {
			keySet.add(o.toString());
		}
		SBPreferences prefs = getPreferencesFor(keyProvider);
		for (Map.Entry<Object, Object> entry : properties.entrySet()) {
			if (keySet.contains(entry.getKey().toString())) {
			  /* XXX: To avoid BackingStoreExceptions, one could check the
			   * Range here, before flusing.
			   */
				prefs.put(entry.getKey(), entry.getValue());
			}
		}
		prefs.flush();
	}
	
	/**
	 * The default values that cannot change!
	 */
	private SBProperties defaults;
	
	/**
	 * Some {@link Class} that contains a certain number of static {@link Field}
	 * objects of type {@link Option} and whose class name is used to address the
	 * node in the user preferences to persist all key-value pairs of user
	 * settings.
	 */
	private final Class<? extends KeyProvider> keyProvider;
	
	/**
	 * User-defined values that may change and may be stored persistently.
	 */
	private final Preferences prefs;
	
	/**
	 * @param keyProvider
	 */
	public SBPreferences(Class<? extends KeyProvider> keyProvider) {
		this.keyProvider = keyProvider;
		this.prefs = Preferences.userNodeForPackage(keyProvider);
		this.defaults = loadDefaults(keyProvider);
		if (clean) {
			this.cleanUserPrefs();
		}
	}
	
	/**
	 * @param keyProvider
	 *        A class that should contain instances of {@link Option} defined as
	 *        public static field members. The package name of this class
	 *        identifies precisely the location of the user-specific settings.
	 * @param relPath
	 *        A {@link String} that specifies a relative path to a resource that
	 *        can be parsed by {@link Properties} class and must contain all
	 *        default values corresponding to the {@link Option} instances defined
	 *        in the keyProvider. For instance, "cfg/MyConf.xml" if cfg is the
	 *        name of a package relative to the keyProvider class.
	 * @throws IOException
	 */
	public SBPreferences(Class<? extends KeyProvider> keyProvider, String relPath)
		throws IOException {
		this.keyProvider = keyProvider;
		this.prefs = Preferences.userNodeForPackage(keyProvider);
		this.defaults = loadDefaults(keyProvider, relPath);
		if (clean) {
			this.cleanUserPrefs();
		}
	}
	
	/**
	 * @param args
	 */
	public void analyzeCommandLineArguments(String args[]) {
		try {
			analyzeCommandLineArguments(generateUsageString(), args, false);
		} catch (BackingStoreException e) {
			// This can never happen because we don't try to persist anything!
		}
	}
	
	/**
	 * @param usage
	 * @param args
	 */
	public void analyzeCommandLineArguments(String usage, String args[]) {
		try {
			analyzeCommandLineArguments(usage, args, false);
		} catch (BackingStoreException e) {
			// This can never happen because we don't try to persist anything!
		}
	}
	
	/**
	 * @param usage
	 * @param args
	 * @param persist
	 *        whether or not to save the command-line argument values directly in
	 *        the user's options.
	 * @return
	 * @throws BackingStoreException
	 */
	public SBProperties analyzeCommandLineArguments(String usage, String args[],
		boolean persist) throws BackingStoreException {
		SBProperties props = analyzeCommandLineArguments(getKeyProvider(), usage,
			args, defaults);
		putAll(props);
		if (persist) {
			flush();
		}
		return props;
	}
	
	/**
	 * Checks whether this instance of {@link SBPreferences} contains the given
	 * {@link Option} as a key and if its associated value satisfies all
	 * {@link Range} constraints.
	 * 
	 * @param option
	 *        The option which is to be checked and whose associated value is to
	 *        be checked.
	 * @return true if this instance of {@link SBPreferences} contains the given
	 *         option and if the value belonging to this option is valid with
	 *         respect to given constraints stored in a {@link Range} object
	 *         associated to the {@link Option}. Returns false if this option is
	 *         not contained as a key in this {@link SBPreferences}.
	 * @throws BackingStoreException
	 *         If this object contains the given option but its associated value
	 *         does not satisfy the given {@link Range} if there is any.
	 */
	public boolean checkPref(Option<?> option) throws BackingStoreException {
		if (containsKey(option)) {
			Object value = get(option);
			ResourceBundle resources = ResourceManager.getBundle(WARNINGS_LOCATION);
			if (value == null) { 
				throw new BackingStoreException(String.format(
				resources.getString("VALUE_FOR_OPTION_NOT_AVAILABLE"), option)); 
			}
			if (option.isSetRangeSpecification()
					&& !option.getRange().castAndCheckIsInRange(get(option))) { 
				throw new BackingStoreException(
				String.format(
							resources.getString("RANGE_CONSTRAINT_VIOLATION_MESSAGE"),
							value, option.formatOptionName(), (option.getRange()
									.isSetConstraints() ? ((GeneralFileFilter) option
									.getRange().getConstraints()).getDescription() : option
									.getRangeSpecification()))); 
			}
			if (option.parseOrCast(value) == null) { 
				throw new BackingStoreException(String.format(
							resources.getString("INVALID_DATA_TYPE_FOR_OPTION"),
							option, option.getRequiredType().getSimpleName())); 
			}
			return true;
		}
		return false;
	}
	
	/**
	 * Checks all key-value pairs stored in these {@link SBPreferences} and throws
	 * a {@link BackingStoreException} for the first {@link Option} whose
	 * corresponding value is out of {@link Range}. If no {@link Exception} is
	 * thrown, this method returns true to indicate that all key-value pairs are
	 * valid.
	 * 
	 * @returns true if all key-value pairs are valid.
	 * @throws BackingStoreException
	 */
	@SuppressWarnings("rawtypes")
	public boolean checkPrefs() throws BackingStoreException {
		Iterator<Option> iterator = optionIterator();
		Option<?> option;
		while (iterator.hasNext()) {
			option = iterator.next();
			checkPref(option);
		}
		return true;
	}

	/**
	 * Removes all those key-value pairs from the user's configuration, for which
	 * no default values are defined.
	 */
	public void cleanUserPrefs() {
		String keys[] = keys();
		Object value;
		boolean remove;
		for (int i = keys.length - 1; i >= 0; i--) {
			remove = false;
			if (!defaults.containsKey(keys[i])) {
				remove = true;
			} else {
				try {
					if (!checkPref(KeyProvider.Tools.getOption(keyProvider, keys[i]))) {
						remove = true;
					}
				} catch (BackingStoreException exc) {
					remove = true;
				}
			}
			if (remove) {
				value = remove(keys[i]);
				ResourceBundle resources = ResourceManager.getBundle(WARNINGS_LOCATION);
				logger.warning(String.format(resources
						.getString("REMOVING_INVALID_ENTRY"), keys[i],
					value == null ? "null" : value.toString()));
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Map#clear()
	 */
	public void clear() {
		try {
			String keys[] = prefs.keys();
			for (int i = keys.length - 1; 0 <= i; i--) {
				prefs.remove(keys[i]);
			}
		} catch (BackingStoreException e) {
			throw new RuntimeException(e);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Map#containsKey(java.lang.Object)
	 */
	public boolean containsKey(Object key) {
		return keySet().contains(key);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Map#containsValue(java.lang.Object)
	 */
	public boolean containsValue(Object value) {
		for (String key : keys()) {
			if (get(key).equals(value)) { return true; }
		}
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Map#entrySet()
	 */
	public Set<java.util.Map.Entry<Object, Object>> entrySet() {
		Set<Map.Entry<Object, Object>> set = new HashSet<Map.Entry<Object, Object>>();
		for (String key : keys()) {
			set.add(new Entry(key, get(key)));
		}
		return set;
	}
	
	/**
	 * @throws BackingStoreException
	 */
	public void flush() throws BackingStoreException {
		if (checkPrefs()) {
			prefs.flush();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Map#get(java.lang.Object)
	 */
	public String get(Object key) {
		return getString(key);
	}
	
	/**
	 * @param key
	 * @return
	 */
	public boolean getBoolean(Object key) {
		String k = key.toString();
		return prefs.getBoolean(k, getDefaultBoolean(k));
	}
	
	/***
	 * @param key
	 * @return
	 */
	public final String getDefault(Object key) {
		return getDefaultString(key);
	}
	
	/**
	 * @param key
	 * @return
	 */
	public final boolean getDefaultBoolean(Object key) {
		return Boolean.parseBoolean(defaults.get(key.toString()).toString());
	}
	
	/**
	 * @param key
	 * @return
	 */
	public final double getDefaultDouble(Object key) {
		return Double.parseDouble(defaults.get(key.toString()));
	}
	
	/**
	 * @param key
	 * @return
	 */
	public final float getDefaultFloat(Object key) {
		return Float.parseFloat(defaults.get(key.toString()));
	}
	
	/**
	 * @param key
	 * @return
	 */
	public final int getDefaultInt(Object key) {
		return Integer.parseInt(defaults.get(key.toString()));
	}
	
	/**
	 * @param key
	 * @return
	 */
	public final long getDefaultLong(Object key) {
		return Long.parseLong(defaults.get(key.toString()));
	}
	
	/**
	 * Creates an unmodifiable copy of the defaults of this {@link SBPreferences}
	 * object and returns a pointer to this copy. The actual defaults must not be
	 * changed, therefore there is no method available to manipulate these.
	 * 
	 * @return An unmodifiable copy of the default preferences.
	 */
	public final Map<Object, Object> getDefaults() {
		return Collections.unmodifiableMap(defaults);
	}
	
	/**
	 * 
	 * @param key
	 * @return
	 */
	private short getDefaultShort(Object key) {
		return Short.parseShort(defaults.get(key.toString()).toString());
	}
	
	/**
	 * @param key
	 * @return
	 */
	public final String getDefaultString(Object key) {
		String def = defaults.getProperty(key.toString());
		if (def == null) { return def; }
		String v = def.toString();
		if (System.getProperties().containsKey(v)) { 
			return System.getProperty(v); 
		}
		return v;
	}
	
	/**
	 * @param key
	 * @return
	 */
	public double getDouble(Object key) {
		String k = key.toString();
		return prefs.getDouble(k, getDefaultDouble(k));
	}

	/**
	 * @param key
	 * @return
	 */
	public float getFloat(Object key) {
		String k = key.toString();
		return prefs.getFloat(k, getDefaultFloat(k));
	}
	
	/**
	 * @param key
	 * @return
	 */
	public int getInt(Object key) {
		String k = key.toString();
		return prefs.getInt(k, getDefaultInt(k));
	}
	
	/**
	 * @return the keyProvider
	 */
	public Class<? extends KeyProvider> getKeyProvider() {
		return keyProvider;
	}
	
	/**
	 * @param key
	 * @return
	 */
	public long getLong(Object key) {
		String k = key.toString();
		return prefs.getLong(k, getDefaultLong(k));
	}
	
	/**
	 * 
	 * @param key
	 * @return
	 */
	public short getShort(Object key) {
		String k = key.toString();
		return (short) prefs.getInt(k, getDefaultShort(k));
	}
	
	/**
	 * @param key
	 * @return
	 */
	public String getString(Object key) {
		String k = key.toString();
		String v = prefs.get(k, getDefaultString(k));
		if (System.getProperties().containsKey(v)) { 
			return System.getProperty(v); 
		}
		return v;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Map#isEmpty()
	 */
	public boolean isEmpty() {
		return keys().length == 0;
	}
	
	/**
	 * Creates an array of keys for all those key-value pairs for which the user
	 * has already specified some values.
	 * 
	 * @return
	 */
	public String[] keys() {
		try {
			return prefs.keys();
		} catch (BackingStoreException e) {
			String keys[] = new String[defaults.size()];
			int i = 0;
			for (Object key : defaults.keySet()) {
				keys[i++] = key.toString();
			}
			return keys;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Map#keySet()
	 */
	public Set<Object> keySet() {
		Set<Object> set = new HashSet<Object>();
		for (String key : keys()) {
			set.add(key);
		}
		return set;
	}
	
	/**
	 * Creates a complete set of all keys defined by this data structure, i.e., it
	 * creates a set of keys for those key-value pairs for which the user has
	 * already specified some value and in addition it also adds all remaining
	 * keys from the default configuration.
	 * 
	 * @return
	 */
	public Set<Object> keySetFull() {
		Set<Object> set = keySet();
		set.addAll(defaults.keySet());
		return set;
	}
	
	/**
	 * Creates a complete array of all keys defined by this data structure, i.e.,
	 * it creates a set of keys for those key-value pairs for which the user has
	 * already specified some value and in addition it also adds all remaining
	 * keys from the default configuration.
	 * 
	 * @return
	 */
	public Object[] keysFull() {
		return keySetFull().toArray(new Object[0]);
	}
	
	/**
	 * Gives an iterator for all the {@link Option} instances (static fields) in
	 * the {@link KeyProvider} belonging to this {@link SBPreferences} instance.
	 * 
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public Iterator<Option> optionIterator() {
		return KeyProvider.Tools.optionIterator(keyProvider);
	}
	
	/**
	 * @param key
	 * @param value
	 */
	public boolean put(Object key, boolean value) {
		String k = key.toString();
		if (!defaults.contains(key)) {
			defaults.put(k, Boolean.valueOf(value));
		}
		boolean oldValue = getBoolean(key);
		prefs.putBoolean(k, value);
		return oldValue;
	}
	
	/**
	 * @param key
	 * @param value
	 */
	public double put(Object key, double value) {
		String k = key.toString();
		if (!defaults.containsKey(key)) {
			defaults.put(k, Double.valueOf(value));
		}
		double oldValue = getDouble(key);
		prefs.putDouble(k, value);
		return oldValue;
	}
	
	/**
	 * @param key
	 * @param value
	 */
	public float put(Object key, float value) {
		String k = key.toString();
		if (!defaults.containsKey(key)) {
			defaults.put(k, Float.valueOf(value));
		}
		float oldValue = getFloat(key);
		prefs.putFloat(k, value);
		return oldValue;
	}
	
	/**
	 * @param key
	 * @param value
	 */
	public int put(Object key, int value) {
		String k = key.toString();
		if (!defaults.containsKey(key)) {
			defaults.put(k, Integer.valueOf(value));
		}
		int oldValue = getInt(key);
		prefs.putInt(k, value);
		return oldValue;
	}
	
	/**
	 * @param key
	 * @param value
	 */
	public long put(Object key, long value) {
		String k = key.toString();
		if (!defaults.containsKey(key)) {
			defaults.put(k, Long.valueOf(value));
		}
		long oldValue = getLong(key);
		prefs.putLong(k, value);
		return oldValue;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Map#put(java.lang.Object, java.lang.Object)
	 */
	public Object put(Object key, Object value) {
		Object o = get(key);
		put(key, value.toString());
		return o;
	}
	
	/**
	 * @param key
	 * @param value
	 */
	public String put(Object key, String value) {
		String k = key.toString();
		if (!defaults.containsKey(k)) {
			defaults.setProperty(k, value);
		}
		String oldValue = getString(key);
		prefs.put(k, value);
		return oldValue;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Map#putAll(java.util.Map)
	 */
	public void putAll(Map<? extends Object, ? extends Object> m) {
		for (Map.Entry<? extends Object, ? extends Object> entry : m.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}
	
	/**
	 * Removes the key-value pair from the preferences but doesn't affect the
	 * default values.
	 * 
	 * @param key
	 */
	public Object remove(Object key) {
		Object o = get(key);
		prefs.remove(key.toString());
		return o;
	}
	
	/**
	 * @param props
	 * @throws BackingStoreException
	 */
	public void save(Properties props) throws BackingStoreException {
		if (!defaults.equals(props)) {
			for (Map.Entry<Object, Object> e : props.entrySet()) {
				prefs.put(e.getKey().toString(), e.getValue().toString());
			}
			prefs.flush();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Map#size()
	 */
	public int size() {
		return keys().length;
	}
	
	/**
	 * Stores the content of this {@link SBPreferences} object in an
	 * {@link OutputStream}. However, it is much better to use the method
	 * {@link #flush()} to automatically store these {@link SBPreferences} in an
	 * operating-system dependent way.
	 * 
	 * @param out
	 * @param comment
	 * @throws IOException
	 */
	public void store(OutputStream out, String comment) throws IOException {
		toProperties().storeToXML(out, comment);
	}
	
	/**
	 * Stores the content of this {@link SBPreferences} object in an XML-formatted
	 * {@link OutputStream}. However, it is much better to use the method
	 * {@link #flush()} to automatically store these {@link SBPreferences} in an
	 * operating-system dependent way.
	 * 
	 * @param out
	 * @param comment
	 * @throws IOException
	 */
	public void storeToXML(OutputStream out, String comment) throws IOException {
		toProperties().storeToXML(out, comment);
	}
	
	/**
	 * Stores the content of this {@link SBPreferences} object in an XML-formatted
	 * {@link OutputStream}. However, it is much better to use the method
	 * {@link #flush()} to automatically store these {@link SBPreferences} in an
	 * operating-system dependent way.
	 * 
	 * @param out
	 * @param comment
	 * @param encoding
	 * @throws IOException
	 */
	public void storeToXML(OutputStream out, String comment, String encoding)
		throws IOException {
		toProperties().storeToXML(out, comment, encoding);
	}
	
	/**
	 * @throws BackingStoreException
	 */
	public void sync() throws BackingStoreException {
		if (checkPrefs()) {
			prefs.sync();
		}
	}
	
	/**
	 * Creates basically a copy of these preferences by creating a new
	 * {@link SBProperties} object with the same defaults as given here and then
	 * copying all user preferences as key-value pairs into this new
	 * {@link SBProperties} object.
	 * 
	 * @return
	 */
	public SBProperties toProperties() {
		SBProperties properties = new SBProperties(defaults);
		for (String key : keys()) {
			properties.put(key, get(key));
		}
		return properties;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return prefs.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Map#values()
	 */
	public Collection<Object> values() {
		Collection<Object> c = new LinkedList<Object>();
		for (String key : keys()) {
			c.add(get(key));
		}
		return c;
	}
	
}
