/*
 *  SBMLsqueezer creates rate equations for reactions in SBML files
 *  (http://sbml.org).
 *  Copyright (C) 2009 ZBIT, University of Tübingen, Andreas Dräger
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.zbit.util;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * This class manages storing and retrieving a configuration of the user. A
 * {@link Configuration} deals with user-defined preferences assuming that a
 * specialized {@link Enum} exists that contains the keys for the key-value
 * pairs to be defined by the user.
 * 
 * @author Andreas Dr&auml;ger
 * @date 2010-10-21
 * 
 */
public class Configuration {

	/**
	 * An optional comment for the configuration file.
	 */
	private String commentCfgFile;
	/**
	 * Path to the configuration file with default properties.
	 */
	private String defaultsCfgFile;
	/**
	 * A hash table of key value pairs representing the user's program
	 * configuration.
	 */
	private SBProperties properties;
	/**
	 * The root node for the user's preferences.
	 */
	private String userPrefNode;

	/**
	 * 
	 * @param keys
	 * @param userPrefNode
	 * @param defaultsCfgFile
	 */
	public Configuration(Class<?> keys, String userPrefNode,
			String defaultsCfgFile) {
		this(keys, userPrefNode, defaultsCfgFile, null);

	}

	private Set<String> keySet;

	/**
	 * 
	 * @param keys
	 * @param userPrefNode
	 * @param defaultsCfgFile
	 * @param commentCfgFile
	 */
	public Configuration(Class<?> keys, String userPrefNode,
			String defaultsCfgFile, String commentCfgFile) {
		this.userPrefNode = userPrefNode;
		this.defaultsCfgFile = defaultsCfgFile;
		this.commentCfgFile = commentCfgFile;
		this.keySet = new HashSet<String>();
		for (Field f : keys.getFields()) {
			keySet.add(f.getName());
		}
		try {
			properties = initProperties();
		} catch (BackingStoreException e) {
			properties = new SBProperties(getDefaultProperties());
		}
	}

	/**
	 * 
	 * @param args
	 * @return
	 */
	public SBProperties analyzeCommandLineArguments(String[] args) {
		SBProperties p = new SBProperties(getDefaultProperties());
		short count;
		for (String arg : args) {
			count = 0;
			while (arg.startsWith("-")) {
				arg = arg.substring(1);
				count++;
			}
			if (count != 2) {
				throw new IllegalArgumentException(String.format(
						"Unknown argument %s.", arg));
			}
			Object value = Boolean.TRUE;
			if (arg.contains("=")) {
				String keyVal[] = arg.split("=");
				if (keyVal.length > 2) {
					throw new IllegalArgumentException(
							"Key-value pair must contain exactly one '=' symbol.");
				}
				arg = keyVal[0];
				value = keyVal[1];
			}
			String argument = arg.toUpperCase().replace('-', '_');
			if (keySet.contains(argument)) {
				p.put(argument, value);
			} else {
				throw new IllegalArgumentException(String.format(
						"Unknown argument %s.", arg));
			}
		}
		return correctProperties(p);
	}

	/**
	 * 
	 * @param prefs
	 * @return
	 * @throws BackingStoreException
	 */
	public SBProperties convert(Preferences prefs) throws BackingStoreException {
		SBProperties p = new SBProperties();
		for (String key : prefs.keys()) {
			p.put(key, prefs.get(key, "null"));
		}
		return correctProperties(p);
	}

	/**
	 * Creates an instance of the given properties, in which all keys are
	 * literals from the configuration enum and all values are objects such as
	 * Boolean, Integer, Double, Kinetics and so on.
	 * 
	 * @param properties
	 * @return
	 */
	public SBProperties correctProperties(SBProperties properties) {
		Object k[] = properties.keySet().toArray();
		SBProperties props = new SBProperties();
		String key;
		for (int i = k.length - 1; i >= 0; i--) {
			key = k[i].toString();
			if (!keySet.contains(key)) {
				continue;
			}
			String val = properties.get(key).toString();

			if (val.startsWith("user.")) {
				props.put(key, System.getProperty(val));
			} else if (val.equalsIgnoreCase("true")
					|| val.equalsIgnoreCase("false")) {
				props.put(key, Boolean.parseBoolean(val));
			} else {
				try {
					props.put(key, Integer.valueOf(val));
				} catch (NumberFormatException e1) {
					try {
						props.put(key, Float.valueOf(val));
					} catch (NumberFormatException e2) {
						try {
							props.put(key, Double.valueOf(val));
						} catch (NumberFormatException e3) {
							if (val.length() == 1) {
								props
										.put(key, Character.valueOf(val
												.charAt(0)));
							} else {
								props.put(key, val);
							}
						}
					}
				}
			}
		}
		return props;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof Configuration) {
			Configuration cfg = (Configuration) o;
			boolean equal = cfg.isSetCommentCfgFile() == isSetCommentCfgFile();
			equal &= cfg.isSetDefaultsCfgFile() == isSetDefaultsCfgFile();
			equal &= cfg.isSetUserPrefNode() == isSetUserPrefNode();
			equal &= cfg.getKeys().equals(getKeys());
			if (equal) {
				if (isSetCommentCfgFile()) {
					equal &= cfg.getCommentCfgFile()
							.equals(getCommentCfgFile());
				}
				if (isSetDefaultsCfgFile()) {
					equal &= cfg.getDefaultsCfgFile().equals(
							getDefaultsCfgFile());
				}
				if (isSetUserPrefNode()) {
					equal &= cfg.getUserPrefNode().equals(getUserPrefNode());
				}
			}
			return equal;
		}
		return false;
	}

	/**
	 * @return the commentCfgFile
	 */
	public String getCommentCfgFile() {
		return isSetCommentCfgFile() ? commentCfgFile : "";
	}

	/**
	 * Reads the default configuration file and returns a properties hash map
	 * that contains pairs of configuration keys and the entries from the file.
	 * 
	 * @return
	 */
	public SBProperties getDefaultProperties() {
		SBProperties defaults = new SBProperties();
		try {
			defaults.loadFromXML(defaults.getClass().getResourceAsStream(
					defaultsCfgFile));
		} catch (Exception exc) {
			try {
				defaults.load(defaults.getClass().getResourceAsStream(
						defaultsCfgFile));
				// defaults = Resource.readProperties(defaultsCfgFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return correctProperties(defaults);
	}

	/**
	 * 
	 * @return
	 */
	public String getDefaultsCfgFile() {
		return defaultsCfgFile;
	}

	/**
	 * 
	 * @return
	 */
	public Set<String> getKeys() {
		return keySet;
	}

	/**
	 * 
	 * @return
	 */
	public SBProperties getProperties() {
		if (properties == null) {
			try {
				initProperties();
			} catch (BackingStoreException e) {
				properties = new SBProperties(getDefaultProperties());
			}
		}
		return properties;
	}

	/**
	 * 
	 * @return
	 */
	public String getUserPrefNode() {
		return userPrefNode;
	}

	/**
	 * 
	 * @return
	 * @throws BackingStoreException
	 */
	public SBProperties initProperties() throws BackingStoreException {
		Preferences prefs = Preferences.userRoot().node(userPrefNode);
		Properties defaults = getDefaultProperties();
		boolean change = false;
		for (Object k : defaults.keySet()) {
			Object v = defaults.get(k);
			if (prefs.get(k.toString(), "null").equals("null")) {
				put(prefs, k, v);
				change = true;
			}
		}
		if (change) {
			prefs.flush();
		}
		SBProperties props = convert(prefs);
		properties = props;
		properties.setDefaults(defaults);
		return properties;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isSetCommentCfgFile() {
		return commentCfgFile != null;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isSetDefaultsCfgFile() {
		return defaultsCfgFile != null;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isSetUserPrefNode() {
		return userPrefNode != null;
	}

	/**
	 * 
	 * @param prefs
	 * @param k
	 * @param v
	 */
	private void put(Preferences prefs, Object k, Object v) {
		if (v instanceof Boolean) {
			prefs.putBoolean(k.toString(), ((Boolean) v).booleanValue());
		} else if (v instanceof Double) {
			prefs.putDouble(k.toString(), ((Double) v).doubleValue());
		} else if (v instanceof Float) {
			prefs.putFloat(k.toString(), ((Float) v).floatValue());
		} else if (v instanceof Integer) {
			prefs.putInt(k.toString(), ((Integer) v).intValue());
		} else if (v instanceof Long) {
			prefs.putLong(k.toString(), ((Long) v).longValue());
		} else {
			prefs.put(k.toString(), v.toString());
		}
	}

	/**
	 * 
	 * @param value
	 *            The new {@link Properties} value for this key.
	 * @return the previous value belonging to this key or null if there was no
	 *         other value.
	 */
	public Object putProperty(Object value) {
		return properties.put(this, value);
	}

	/**
	 * Saves the currently valid {@link Properties}.
	 * 
	 * @see #saveProperties(Properties)
	 * @throws BackingStoreException
	 */
	public void saveProperties() throws BackingStoreException {
		saveProperties(properties);
	}

	/**
	 * 
	 * @param props
	 * @throws BackingStoreException
	 */
	public void saveProperties(SBProperties props) throws BackingStoreException {
		if (!initProperties().equals(props)) {
			Preferences pref = Preferences.userRoot().node(userPrefNode);
			for (Object key : props.keySet()) {
				put(pref, key, props.get(key));
			}
			properties = props;
			pref.flush();
		}
	}

	/**
	 * @param commentCfgFile
	 *            the commentCfgFile to set
	 */
	public void setCommentCfgFile(String commentCfgFile) {
		this.commentCfgFile = commentCfgFile;
	}

	/**
	 * 
	 * @param defaultsCfgFile
	 */
	public void setDefaultsCfgFile(String defaultsCfgFile) {
		this.defaultsCfgFile = defaultsCfgFile;
	}

	/**
	 * 
	 * @param usrPrefNode
	 */
	public void setUserPrefNode(String usrPrefNode) {
		userPrefNode = usrPrefNode;
	}

}
