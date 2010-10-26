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
		private Object key, value;

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
	 * The default values that cannot change!
	 */
	private static Properties defaults;

	/**
	 * Some {@link Class} that contains a certain number of static {@link Field}
	 * objects of type {@link Option} and whose class name is used to address
	 * the node in the user preferences to persist all key-value pairs of user
	 * settings.
	 */
	private Class<?> keyProvider;

	/**
	 * User-defined values that may change and may be stored persistently.
	 */
	private Preferences prefs;

	/**
	 * 
	 * @param keyProvider A class that should contain instances of {@link Option}
	 * defined as public static field members. The package name of this class identifies
	 * precisely the location of the user-specific settings.
	 * @param relPath A {@link String} that specifies a relative path to a resource
	 * that can be parsed by {@link Properties} class and must contain all default
	 * values corresponding to the {@link Option} instances defined in the keyProvider. For instance,
	 * "cfg/MyConf.xml"
	 * @throws InvalidPropertiesFormatException
	 * @throws IOException
	 */
	public SBPreferences(Class<?> keyProvider, String relPath)
			throws InvalidPropertiesFormatException, IOException {
		this.keyProvider = keyProvider;
		this.prefs = Preferences.userNodeForPackage(keyProvider);
		this.defaults = new Properties();
		this.defaults.loadFromXML(keyProvider.getResourceAsStream(relPath));
	}

	/**
	 * 
	 * @param usage
	 * @param args
	 */
	public void analyzeCommandLineArguments(String usage, String args[]) {
		// create the parser and specify the allowed options ...
		ArgParser parser = new ArgParser(usage);
		Class<?> keyProvider = getKeyProvider();
		Map<Option, Object> options = new HashMap<Option, Object>();
		Object fieldValue, argHolder;
		Option option;
		for (Field f : keyProvider.getFields()) {
			try {
				fieldValue = f.get(keyProvider);
				if (fieldValue instanceof Option) {
					option = (Option) fieldValue;
					argHolder = option.createArgumentHolder(get(option));
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
			if (key.getRequiredType().equals(Float.class)) {
				put(key, ((FloatHolder) options.get(key)).value);
			} else if (key.getRequiredType().equals(Double.class)) {
				put(key, ((DoubleHolder) options.get(key)).value);
			} else if (key.getRequiredType().equals(Short.class)) {
				put(key, (short) ((IntHolder) options.get(key)).value);
			} else if (key.getRequiredType().equals(Integer.class)) {
				put(key, ((IntHolder) options.get(key)).value);
			} else if (key.getRequiredType().equals(Long.class)) {
				put(key, ((LongHolder) options.get(key)).value);
			} else if (key.getRequiredType().equals(Boolean.class)) {
				put(key, ((BooleanHolder) options.get(key)).value);
			} else if (key.getRequiredType().equals(Character.class)) {
				put(key, ((CharHolder) options.get(key)).value);
			} else if (key.getRequiredType().equals(String.class)) {
				put(key, ((StringHolder) options.get(key)).value);
			} else {
				put(key, ((ObjectHolder) options.get(key)).value);
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
	  // TODO: getDefaultBoolean... and also for all other get methods!
		String k = key.toString();
		return prefs.getBoolean(k, Boolean.parseBoolean(defaults.get(k)
				.toString()));
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	public double getDouble(Object key) {
		String k = key.toString();
		return prefs.getDouble(k, Double
				.parseDouble(defaults.get(k).toString()));
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	public float getFloat(Object key) {
		String k = key.toString();
		return prefs.getFloat(k, Float.parseFloat(defaults.get(k).toString()));
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	public int getInt(Object key) {
		String k = key.toString();
		return prefs.getInt(k, Integer.parseInt(defaults.get(k).toString()));
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
		return prefs.getLong(k, Long.parseLong(defaults.get(k).toString()));
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	public String getString(Object key) {
		String k = key.toString();
		return prefs.get(k, defaults.get(k).toString());
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
