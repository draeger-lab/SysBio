/**
 * 
 */
package de.zbit.util;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
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

/**
 * @author Andreas Dr&auml;ger
 * @date 2010-10-24
 */
public class SBPreferences implements Map<Object, Object> {

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
	 * @see java.util.Map.Entry#getKey()
	 */
	public Object getKey() {
	    return key;
	}

	/**
		 * 
		 */
	public Object getValue() {
	    return value;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Map.Entry#setValue(java.lang.Object)
	 */
	public Object setValue(Object value) {
	    return this.value = value;
	}

    }

    /**
     * Pointers to avoid senselessly parsing configuration files again and
     * again.
     */
    private static final Map<String, Properties> allDefaults = new HashMap<String, Properties>();

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
	Class<?> keyProvider, String relPath, boolean persist, String usage,
	String args[]) throws IOException, BackingStoreException {
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
	Class<?> keyProvider, String usage, String args[]) {
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
	Class<?> keyProvider, String usage, String args[],
	Map<Object, Object> defaults) {
	SBProperties props;
	if (defaults != null) {
	    props = new SBProperties(new Properties());
	} else {
	    props = new SBProperties();
	}
	// create the parser and specify the allowed options ...
	ArgParser parser = new ArgParser(usage);
	Map<Option, Object> options = configureArgParser(parser,
	    new HashMap<Option, Object>(), keyProvider, props, defaults);
	parser.matchAllArgs(args);
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
	Class<?> keyProvider, String relPath, String usage, String args[]) {
	try {
	    return analyzeCommandLineArguments(keyProvider, relPath, false,
		usage, args);
	} catch (Exception e) {
	    // This can actually never happen because we don't persist anything.
	    return new SBProperties();
	}
    }

    /**
     * Automatically builds the usage string.
     * @param defFileAndKeys
     *        This {@link List} is supposed to contain the relative paths of
     *        the configuration files with default preferences as first
     *        value and each corresponding second value must be the full
     *        class name of the corresponding keyProvider class, i.e., the
     *        name of some {@link Class} object that contains as many static
     *        final fields of {@link Option} instances as the corresponding
     *        defaults option file contains entries.
     * @param args
     *        The given command line arguments.
     * @return
     */
    public static final SBProperties analyzeCommandLineArguments(
        SortedMap<String, Class<?>> defFileAndKeys, String args[]) {
      
      
      return analyzeCommandLineArguments(defFileAndKeys, generateUsageString(), args);
    }
    /**
     * @param defFileAndKeys
     *        This {@link List} is supposed to contain the relative paths of
     *        the configuration files with default preferences as first
     *        value and each corresponding second value must be the full
     *        class name of the corresponding keyProvider class, i.e., the
     *        name of some {@link Class} object that contains as many static
     *        final fields of {@link Option} instances as the corresponding
     *        defaults option file contains entries.
     * @param usage
     *        A String describing how to invoke the class that is calling
     *        this method.
     * @param args
     *        The given command line arguments.
     * @return
     */
    public static final SBProperties analyzeCommandLineArguments(
	SortedMap<String, Class<?>> defFileAndKeys, String usage, String args[]) {
	SBProperties props = new SBProperties(new Properties());
	ArgParser parser = new ArgParser(usage);
	Map<Option, Object> options = new HashMap<Option, Object>();
	SBPreferences prefs[] = new SBPreferences[defFileAndKeys.size()];

	// Configure argument parser by passing all possible option definitions
	// to it.
	int i = 0;
	for (Map.Entry<String, Class<?>> entry : defFileAndKeys.entrySet()) {
	    try {
		prefs[i] = getPreferencesFor(entry.getValue(), entry.getKey());
		options.putAll(configureArgParser(parser, options, entry
			.getValue(), props, loadDefaults(entry.getValue(),
		    entry.getKey())));
	    } catch (Exception e) {
		Exception exc = new Exception(String.format(
		    "Could not load properties for %s from config file %s.",
		    entry.getValue().getName(), entry.getKey()), e);
		exc.printStackTrace();
	    } finally {
		i++;
	    }
	}

	// Do the actual parsing
	parser.matchAllArgs(args);
	putAll(props, options);

	// Now all command line arguments must be made persistent:
	String k, property, value;
	for (i = 0; i < prefs.length; i++) {
	    for (Object key : prefs[i].keySetFull()) {
		k = key.toString();
		if (props.containsKey(k)) {
		    property = props.getProperty(k);
		    value = prefs[i].get(k);
		    if (!value.equals(property)) {
			prefs[i].put(k, property);
		    }
		}
	    }
	    try {
		prefs[i].flush();
	    } catch (BackingStoreException e) {
		Exception exc = new Exception(
		    String
			    .format(
				"Could not persistently store the user configuration for %s.",
				prefs[i].getKeyProvider().getName()), e);
		exc.printStackTrace();
	    }
	}

	return props;
    }
    
    /**
     * Generates a usage/synopsis string for the given mainClass.
     * Looks if the class is inside a jar. If yes,
     * "java -jar [NAME].jar" is the usage string. Else
     * "java package.ClassName" is the usage string.
     * @param mainClass of your project
     * @return usage String
     */
    public static String generateUsageString(Class<?> mainClass) {
      String synopsis = "java ";
      
      String jarName = Utils.getNameOfJar(mainClass);
      if (jarName!=null && jarName.length()>0) {
        synopsis+="-jar " + jarName;
      } else {
        // class.getName() also returns the package prefix.
        synopsis += mainClass.getName();
      }
      
      return synopsis;
    }
    
    /**
     * Automatically generates a usage string, based on the main class
     * that has been called.
     * Automatically detects, if the main class is in a jar file or not
     * and builds the usage string.
     * @return usage String (also called synopsis).
     */
    public static String generateUsageString() {
      Class<?> mainClass = null;
      
      // Get the main class from the stackTrace
      final Throwable t = new Throwable();
      for (StackTraceElement e: t.getStackTrace()) {
        // Search the main class
        if (e.getMethodName().equalsIgnoreCase("main")) {
          // Get it's name
          try {
            mainClass = Class.forName(e.getClassName());
            break;
          } catch (ClassNotFoundException e1) {
            // Not possible, because class is in StackTrace
          }
        }
      }
      
      String usage;
      if (mainClass==null) {
        // Should never happen...
        usage = "java [program_name]";
      } else {
        usage = generateUsageString(mainClass) + " [options]";
      }
      
      return usage;
    }

    /**
     * @param parser
     * @param options
     * @param keyProvider
     * @param props
     * @param defaults
     * @return
     */
    private static Map<Option, Object> configureArgParser(ArgParser parser,
	Map<Option, Object> options, Class<?> keyProvider, SBProperties props,
	Map<Object, Object> defaults) {
	Object fieldValue, argHolder;
	Option option;
	String k;
	for (Field f : keyProvider.getFields()) {
	    try {
		fieldValue = f.get(keyProvider);
		if (fieldValue instanceof Option) {
		    option = (Option) fieldValue;
		    k = option.toString();
		    if ((defaults != null) && defaults.containsKey(option)) {
			// We here set the default value as pre-defined value
			// if the given Map contains the corresponding key.
			// In this way we make sure that if the user does not
			// give a command-line argument for this option, we will
			// stick with the default value:
			argHolder = option.createArgumentHolder(defaults
				.get(option));
			props.getDefaults().put(k, defaults.get(option));
		    } else {
			if ((defaults != null) && defaults.containsKey(k)) {
			    argHolder = option.createArgumentHolder(defaults
				    .get(k));
			    props.getDefaults().put(k, defaults.get(k));
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
		// This my happen if there are other fields than static options
		// in the key provider. Also happens if the wrong type of
		// argHolder has been added or created! But this method should
		// work fine... (I hope). You can check with -? option.
		// Just ignore...
	    }
	}
	return options;
    }

    /**
     * @param keyProvider
     * @param relPath
     * @return
     * @throws IOException
     */
    public static SBPreferences getPreferencesFor(Class<?> keyProvider,
	String relPath) throws IOException {
	return new SBPreferences(keyProvider, relPath);
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
    private static Properties loadDefaults(Class<?> keyProvider, String relPath)
	throws IOException {
	Properties defaults;
	String path = keyProvider.getPackage().getName().replace('.', '/')
		+ '/' + relPath;
	if (!allDefaults.containsKey(path)) {
	    defaults = new Properties();
	    defaults.loadFromXML(keyProvider.getResourceAsStream(relPath));
	    Set<String> options = new HashSet<String>();
	    Object fieldValue;
	    String k, v;
	    for (Field field : keyProvider.getFields()) {
		try {
		    fieldValue = field.get(keyProvider);
		    if (fieldValue instanceof Option) {
			k = fieldValue.toString();
			if (defaults.getProperty(k) == null) {
			    throw new IllegalArgumentException(
				String
					.format(
					    "No default value available for option %s in file %s.",
					    k, path));
			}
			options.add(k);
		    }
		} catch (Exception exc) {
		    // ignore non-static fields
		}
	    }
	    for (Map.Entry<Object, Object> e : defaults.entrySet()) {
		k = e.getKey().toString();
		v = e.getValue().toString();
		if (!options.contains(k)) {
		    throw new IllegalArgumentException(String
			    .format("No option %s defined by %s.", k,
				keyProvider.getName()));
		}
		if (System.getProperties().containsKey(v)) {
		    defaults.setProperty(k, System.getProperty(v));
		}
	    }
	    allDefaults.put(path, defaults);
	} else {
	    defaults = allDefaults.get(path);
	}
	return defaults;
    }

    /**
     * Convenient method to put all values gathered from a command line into an
     * {@link SBProperties} table.
     * 
     * @param props
     *        The {@link SBProperties} where to put all the values from the
     *        value holders in the given {@link Map} data structure.
     * @param options
     *        A map between {@link Option} instances and {@link ArgParser}
     *        holders for the desired values.
     */
    private static void putAll(SBProperties props, Map<Option, Object> options) {
	String k, v, value;
	for (Option key : options.keySet()) {
	  
	  //try {
	    k = key.toString();
	    if (key.getRequiredType().equals(Float.class)) {
		value = Float.toString(((FloatHolder) options.get(key)).value);
	    } else if (key.getRequiredType().equals(Double.class)) {
		value = Double
			.toString(((DoubleHolder) options.get(key)).value);
	    } else if (key.getRequiredType().equals(Short.class)) {
		value = Short
			.toString((short) ((IntHolder) options.get(key)).value);
	    } else if (key.getRequiredType().equals(Integer.class)) {
		value = Integer.toString(((IntHolder) options.get(key)).value);
	    } else if (key.getRequiredType().equals(Long.class)) {
		value = Long.toString(((LongHolder) options.get(key)).value);
	    } else if (key.getRequiredType().equals(Boolean.class)) {
		value = Boolean
			.toString(((BooleanHolder) options.get(key)).value);
	    } else if (key.getRequiredType().equals(Character.class)) {
		value = Character
			.toString(((CharHolder) options.get(key)).value);
	    } else if (key.getRequiredType().equals(String.class)) {
		value = ((StringHolder) options.get(key)).value;
	    } else {
		value = ((StringHolder) options.get(key)).value;
		if (value!=null) value = value.toString();
	    }
	    if (props.isSetDefaults()) {
		v = props.getProperty(k);
		if (v == null) {
		    throw new IllegalArgumentException(String.format(
			"No default value defined for property %s.", k));
		}
		if (!v.equals(value)) {
		    props.setProperty(k, value);
		}
	    } else {
		props.setProperty(k, value);
	    }
	}
    }

    /**
     * The default values that cannot change!
     */
    private Properties defaults;

    /**
     * Some {@link Class} that contains a certain number of static {@link Field}
     * objects of type {@link Option} and whose class name is used to address
     * the node in the user preferences to persist all key-value pairs of user
     * settings.
     */
    private final Class<?> keyProvider;

    /**
     * User-defined values that may change and may be stored persistently.
     */
    private final Preferences prefs;

    /**
     * @param keyProvider
     *        A class that should contain instances of {@link Option} defined as
     *        public static field members. The package name of
     *        this class identifies precisely the location of the
     *        user-specific settings.
     * @param relPath
     *        A {@link String} that specifies a relative path to a resource
     *        that can be parsed by {@link Properties} class and must
     *        contain all default values corresponding to the {@link Option}
     *        instances defined in the keyProvider. For instance,
     *        "cfg/MyConf.xml" if cfg is the name of a package relative to
     *        the keyProvider class.
     * @throws IOException
     */
    public SBPreferences(Class<?> keyProvider, String relPath)
	throws IOException {
	this.keyProvider = keyProvider;
	this.prefs = Preferences.userNodeForPackage(keyProvider);
	this.defaults = loadDefaults(keyProvider, relPath);
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
     *        whether or not to save the command-line argument values
     *        directly in the user's options.
     * @return
     * @throws BackingStoreException
     */
    public SBProperties analyzeCommandLineArguments(String usage,
	String args[], boolean persist) throws BackingStoreException {
	SBProperties props = analyzeCommandLineArguments(getKeyProvider(),
	    usage, args, defaults);
	putAll(props);
	if (persist) {
	    flush();
	}
	return props;
    }

    /*
     * (non-Javadoc)
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
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    public boolean containsKey(Object key) {
	return keySet().contains(key);
    }

    /*
     * (non-Javadoc)
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    public boolean containsValue(Object value) {
	for (String key : keys()) {
	    if (get(key).equals(value)) {
		return true;
	    }
	}
	return false;
    }

    /*
     * (non-Javadoc)
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
	prefs.flush();
    }

    /*
     * (non-Javadoc)
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
	return Double.parseDouble(defaults.get(key.toString()).toString());
    }

    /**
     * @param key
     * @return
     */
    public final float getDefaultFloat(Object key) {
	return Float.parseFloat(defaults.get(key.toString()).toString());
    }

    /**
     * @param key
     * @return
     */
    public final int getDefaultInt(Object key) {
	return Integer.parseInt(defaults.get(key.toString()).toString());
    }

    /**
     * @param key
     * @return
     */
    public final long getDefaultLong(Object key) {
	return Long.parseLong(defaults.get(key.toString()).toString());
    }

    /**
     * Creates an unmodifiable copy of the defaults of this
     * {@link SBPreferences} object and returns a pointer to this copy. The
     * actual defaults must not be changed, therefore there is no method
     * available to manipulate these.
     * 
     * @return An unmodifiable copy of the default preferences.
     */
    public final Map<Object, Object> getDefaults() {
	return Collections.unmodifiableMap(defaults);
    }

    /**
     * @param key
     * @return
     */
    public final String getDefaultString(Object key) {
	String v = defaults.get(key.toString()).toString();
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
    public Class<?> getKeyProvider() {
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
     * Creates a complete set of all keys defined by this data structure, i.e.,
     * it creates a set of keys for those key-value pairs for which the user has
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
     * Creates a complete array of all keys defined by this data structure,
     * i.e., it creates a set of keys for those key-value pairs for which the
     * user has already specified some value and in addition it also adds all
     * remaining keys from the default configuration.
     * 
     * @return
     */
    public Object[] keysFull() {
	return keySetFull().toArray(new Object[0]);
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
	String oldValue = get(key);
	prefs.put(k, value);
	return oldValue;
    }

    /*
     * (non-Javadoc)
     * @see java.util.Map#putAll(java.util.Map)
     */
    public void putAll(Map<? extends Object, ? extends Object> m) {
	for (Object key : m.keySet()) {
	    put(key, m.get(key));
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
	    for (Object key : props.keySet()) {
		prefs.put(key.toString(), props.get(key).toString());
	    }
	    defaults = props;
	    prefs.flush();
	}
    }

    /*
     * (non-Javadoc)
     * @see java.util.Map#size()
     */
    public int size() {
	return keys().length;
    }

    /**
     * @throws BackingStoreException
     */
    public void sync() throws BackingStoreException {
	prefs.sync();
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
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	return prefs.toString();
    }

    /*
     * (non-Javadoc)
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
