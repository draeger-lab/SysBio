/**
 * 
 */
package de.zbit.util;

import java.util.Properties;

/**
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
	public SBProperties(Properties defaults) {
		super(defaults);
	}

	/**
	 * 
	 * @return
	 */
	public Properties getDefaults() {
		return defaults;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isSetDefaults() {
		return defaults == null;
	}

	/**
	 * 
	 * @param defaults
	 */
	public void setDefaults(Properties defaults) {
		if (defaults == null) {
			defaults = new Properties();
		}
		defaults.putAll(defaults);
	}

}
