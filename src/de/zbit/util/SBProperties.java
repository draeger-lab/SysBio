/**
 * 
 */
package de.zbit.util;

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
		return super.getProperty(key.toString());
	}

	/**
	 * @return
	 */
	public SBProperties getDefaults() {
		return (SBProperties) defaults;
	}

	/**
	 * @return
	 */
	public boolean isSetDefaults() {
		return defaults != null;
	}

	/**
	 * This method ensures that only {@link String} instances can be put into
	 * this table. This is necessary because, when writing this to XML the super
	 * class of this object will throw exceptions for non-{@link String} keys
	 * and values. Furthermore, this also ensures that properties can always be
	 * found again because we only have to compare Strings no matter what is
	 * actually put here. {@inheritDoc}
	 */
	@Override
	public synchronized Object put(Object key, Object value) {
		return super.put(key.toString(), value.toString());
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
