/**
 * 
 */
package de.zbit.util.prefs;

import java.lang.reflect.Field;
import java.util.Properties;

/**
 * This extension of {@link Properties} can only contain key-value pairs of type
 * {@link String}, {@link String}, which makes searching for existing keys
 * simpler and avoids exceptions when storing the content in an XML file.
 * Furthermore, it also allows access to the default value collection, which is
 * not possible in its super class.
 * 
 * @author Andreas Dr&auml;ger
 * @date 2010-10-22
 */
public class SBProperties extends Properties {
	
	/**
	 * Generated serial version identifier
	 */
	private static final long serialVersionUID = 4883076517282212786L;
	
	/**
     * 
     */
	public SBProperties() {
		super();
	}
	
	/**
	 * @param defaults
	 */
	public SBProperties(SBProperties defaults) {
		super(defaults);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Hashtable#contains(java.lang.Object)
	 */
	@Override
	public synchronized boolean contains(Object value) {
		return super.containsValue(value.toString());
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Hashtable#containsKey(java.lang.Object)
	 */
	@Override
	public synchronized boolean containsKey(Object key) {
		return super.containsKey(key.toString());
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Hashtable#containsValue(java.lang.Object)
	 */
	@Override
	public boolean containsValue(Object value) {
		return super.containsValue(value.toString());
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Hashtable#get(java.lang.Object)
	 */
	@Override
	public String get(Object key) {
		Object o = super.get(key.toString());
		return o != null ? o.toString() : null;
	}
	
	/**
	 * 
	 * @param key
	 * @return
	 */
	public boolean getBoolean(Object key) {
		return Boolean.parseBoolean(get(key));
	}
	
	/**
	 * 
	 * @param key
	 * @return
	 */
	public boolean getBooleanProperty(Object key) {
		return Boolean.parseBoolean(getProperty(key));
	}
	
	/**
	 * @return
	 */
	public SBProperties getDefaults() {
		return (SBProperties) defaults;
	}
	
	/**
	 * 
	 * @param key
	 * @return
	 */
	public double getDouble(Object key) {
		return Double.parseDouble(get(key));
	}
	
	/**
	 * 
	 * @param key
	 * @return
	 */
	public double getDoubleProperty(Object key) {
		return Double.parseDouble(getProperty(key));
	}
	
	/**
	 * 
	 * @param key
	 * @return
	 */
	public float getFloat(Object key) {
		return Float.parseFloat(get(key));
	}
	
	/**
	 * 
	 * @param key
	 * @return
	 */
	public float getFloatProperty(Object key) {
		return Float.parseFloat(getProperty(key));
	}
	
	public int getInt(Object key) {
		return Integer.parseInt(get(key));
	}
	
	public int getIntProperty(Object key) {
		return Integer.parseInt(getProperty(key));
	}
	
	/**
	 * 
	 * @param key
	 * @return
	 */
	public long getLong(Object key) {
		return Long.parseLong(get(key));
	}
	
	/**
	 * 
	 * @param key
	 * @return
	 */
	public long getLongProperty(Object key) {
		return Long.parseLong(getProperty(key));
	}
	
	/**
	 * 
	 * @param key
	 * @return
	 */
	public String getProperty(Object key) {
		return getProperty(key.toString());
	}
	
	/**
	 * 
	 * @param key
	 * @return
	 */
	public short getShort(Object key) {
		return Short.parseShort(get(key));
	}
	
	/**
	 * 
	 * @param key
	 * @return
	 */
	public short getShortProperty(Object key) {
		return Short.parseShort(getProperty(key));
	}
	
	/**
	 * @return
	 */
	public boolean isSetDefaults() {
		return defaults != null;
	}
	
	/**
	 * Loads all properties from this class from the public static final Options -
	 * default values in the keyProvider.
	 * 
	 * @param keyProvider
	 */
	public void loadFromKeyProvider(Class<?> keyProvider) {
		Object fieldValue;
		String k;
		
		for (Field field : keyProvider.getFields()) {
			try {
				fieldValue = field.get(keyProvider);
				if (fieldValue instanceof Option<?>) {
					k = fieldValue.toString();
					// Would be possible to check for already setted values.
					//if (defaults.getProperty(k) != null) {}
					this.put(k, ((Option<?>) fieldValue).getDefaultValue());
				}
			} catch (Exception exc) {
				exc.printStackTrace(); // XXX: Remove me.
				// ignore non-static fields
			}
		}
	}
	
	/**
	 * This method ensures that only {@link String} instances can be put into this
	 * table. This is necessary because, when writing this to XML the super class
	 * of this object will throw exceptions for non-{@link String} keys and
	 * values. Furthermore, this also ensures that properties can always be found
	 * again because we only have to compare Strings no matter what is actually
	 * put here. {@inheritDoc}
	 */
	@Override
	public synchronized Object put(Object key, Object value) {
		return super.put(key.toString(), value == null ? "" : value.toString());
	}
	
	/**
	 * @param defaults
	 */
	public void setDefaults(Properties defaults) {
		if (this.defaults == null) {
			this.defaults = new Properties();
		}
		this.defaults.putAll(defaults);
	}
	
}
