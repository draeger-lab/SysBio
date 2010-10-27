/**
 * 
 */
package de.zbit.util;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.InvalidPropertiesFormatException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import argparser.ArgParser;
import argparser.BooleanHolder;
import argparser.CharHolder;
import argparser.DoubleHolder;
import argparser.FloatHolder;
import argparser.IntHolder;
import argparser.LongHolder;
import argparser.ObjectHolder;
import argparser.StringHolder;

/**
 * @author Andreas Dr&auml;ger
 * @date 2010-10-24
 */
public class SBPreferences implements Map<Object, Object> {

	/**
	 * 
	 * @author Andreas Dr&auml;ger
	 * @date 2010-10-24
	 */
	private class Entry implements Map.Entry<Object, Object> {

		/**
		 * 
		 */
		private final Object key;
		private Object value;

		/**
		 * 
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

		/**
		 * 
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
	 * 
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
	 * 
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
		Map<Option, Object> options = new HashMap<Option, Object>();
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
		parser.matchAllArgs(args);
		for (Option key : options.keySet()) {
			k = key.toString();
			if (key.getRequiredType().equals(Float.class)) {
				props.put(k, ((FloatHolder) options.get(key)).value);
			} else if (key.getRequiredType().equals(Double.class)) {
				props.put(k, ((DoubleHolder) options.get(key)).value);
			} else if (key.getRequiredType().equals(Short.class)) {
				props.put(key, (short) ((IntHolder) options.get(key)).value);
			} else if (key.getRequiredType().equals(Integer.class)) {
				props.put(k, ((IntHolder) options.get(key)).value);
			} else if (key.getRequiredType().equals(Long.class)) {
				props.put(k, ((LongHolder) options.get(key)).value);
			} else if (key.getRequiredType().equals(Boolean.class)) {
				props.put(k, ((BooleanHolder) options.get(key)).value);
			} else if (key.getRequiredType().equals(Character.class)) {
				props.put(k, ((CharHolder) options.get(key)).value);
			} else if (key.getRequiredType().equals(String.class)) {
				props.put(k, ((StringHolder) options.get(key)).value);
			} else {
				props.put(k, ((ObjectHolder) options.get(key)).value);
			}
		}
		return props;
	}

	/**
	 * The default values that cannot change!
	 * 
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
	 * 
	 * @param keyProvider
	 *            A class that should contain instances of {@link Option}
	 *            defined as public static field members. The package name of
	 *            this class identifies precisely the location of the
	 *            user-specific settings.
	 * @param relPath
	 *            A {@link String} that specifies a relative path to a resource
	 *            that can be parsed by {@link Properties} class and must
	 *            contain all default values corresponding to the {@link Option}
	 *            instances defined in the keyProvider. For instance,
	 *            "cfg/MyConf.xml" if cfg is the name of a package relative to
	 *            the keyProvider class.
	 * @throws InvalidPropertiesFormatException
	 * @throws IOException
	 */
	public SBPreferences(Class<?> keyProvider, String relPath)
			throws InvalidPropertiesFormatException, IOException {
		this.keyProvider = keyProvider;
		this.prefs = Preferences.userNodeForPackage(keyProvider);
		defaults = new Properties();
		defaults.loadFromXML(keyProvider.getResourceAsStream(relPath));
	}

	/**
	 * 
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
	 * 
	 * @param usage
	 * @param args
	 * @param persist
	 *            whether or not to save the command-line argument values
	 *            directly in the user's options.
	 * @throws BackingStoreException
	 */
	public void analyzeCommandLineArguments(String usage, String args[],
			boolean persist) throws BackingStoreException {
		putAll(analyzeCommandLineArguments(getKeyProvider(), usage, args,
				defaults));
		if (persist) {
			flush();
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
			if (get(key).equals(value)) {
				return true;
			}
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
	 * 
	 */
	public void flush() throws BackingStoreException {
		prefs.flush();
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
	 * 
	 * @param key
	 * @return
	 */
	public boolean getBoolean(Object key) {
		String k = key.toString();
		return prefs.getBoolean(k, getDefaultBoolean(k));
	}

	/***
	 * 
	 * @param key
	 * @return
	 */
	public final String getDefault(Object key) {
		return getDefaultString(key);
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	public final boolean getDefaultBoolean(Object key) {
		return Boolean.parseBoolean(defaults.get(key.toString()).toString());
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	public final double getDefaultDouble(Object key) {
		return Double.parseDouble(defaults.get(key.toString()).toString());
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	public final float getDefaultFloat(Object key) {
		return Float.parseFloat(defaults.get(key.toString()).toString());
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	public final int getDefaultInt(Object key) {
		return Integer.parseInt(defaults.get(key.toString()).toString());
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	public final long getDefaultLong(Object key) {
		return Long.parseLong(defaults.get(key.toString()).toString());
	}

	/**
	 * 
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
	 * 
	 * @param key
	 * @return
	 */
	public double getDouble(Object key) {
		String k = key.toString();
		return prefs.getDouble(k, getDefaultDouble(k));
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	public float getFloat(Object key) {
		String k = key.toString();
		return prefs.getFloat(k, getDefaultFloat(k));
	}

	/**
	 * 
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
	 * 
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
	 * 
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
	 * 
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
	 * 
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
	 * 
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
	 * 
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
	 * 
	 * @param key
	 * @param value
	 */
	public String put(Object key, String value) {
		String k = key.toString();
		if (!defaults.contains(key)) {
			defaults.put(k, value);
		}
		String oldValue = get(key);
		prefs.put(k, value);
		return oldValue;
	}

	/*
	 * (non-Javadoc)
	 * 
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
	 * 
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
	 * 
	 * @see java.util.Map#size()
	 */
	public int size() {
		return keys().length;
	}

	/**
	 * 
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
