/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2012 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;
import java.util.Set;

/**
 * This class provides the necessary functionality to load a
 * {@link ResourceBundle} from an XML formatted file (for the specification of
 * the document see {@link Properties}).
 * 
 * Example usage:
 * <pre>
 * ResourceBundle resource = ResourceManager.getBundle("de.zbit.locales.Labels");
   String myString = resource.getString("MY_KEY");
 * </pre>
 * 
 * @author Andreas Dr&auml;ger
 * @date 2011-01-04
 * @version $Rev$
 * @since 1.0
 */
public class XMLResourceBundleControl extends Control {
	
	/**
	 * An adapter class that wrapps a {@link Properties} class and provides its
	 * functionality as a {@link ResourceBundle}.
	 * 
	 * Usually, this will simply return the elements for a given key as it would
	 * be in an instance of {@link Properties}. The only difference is the support
	 * for {@link String} arrays: If a returned element is surrounded with square
	 * brackets (<code>'['</code> and <code>']'</code>), these brackets are
	 * removed from the string and the separator char <code>';'</code> is used to
	 * split the given {@link String} into an array of {@link String}s.
	 * 
	 * @author Andreas Dr&auml;ger
	 * @date 2011-01-04
	 */
	private static class XMLResourceBundle extends ResourceBundle {
		
		/**
		 * The wrapped element.
		 */
		private Properties properties;
		
		/**
		 * 
		 * @param stream
		 * @throws IOException
		 */
		public XMLResourceBundle(InputStream stream) throws IOException,
			InvalidPropertiesFormatException {
			Properties defaults = new Properties();
			defaults.loadFromXML(stream);
			properties = new Properties(defaults);
		}
		
		/* (non-Javadoc)
		 * @see java.util.ResourceBundle#getKeys()
		 */
		public Enumeration<String> getKeys() {
			Set<String> key = properties.stringPropertyNames();
			return Collections.enumeration(key);
		}
		
		/* (non-Javadoc)
		 * @see java.util.ResourceBundle#handleGetObject(java.lang.String)
		 */
		protected Object handleGetObject(String key) {
			String element = properties.getProperty(key);
			if ((element != null) && (element.length() > 0)
					&& (element.charAt(0) == '[')
					&& (element.charAt(element.length() - 1) == ']')) {
				return element.substring(1, element.length() - 1).split(";");
			}
			return element;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return properties.toString();
		} 
		
	}
	
	/**
	 * The extension for XML files.
	 */
	public static final String XML = "xml";
	
	/* (non-Javadoc)
	 * @see java.util.ResourceBundle.Control#getFormats(java.lang.String)
	 */
	@Override
	public List<String> getFormats(String baseName) {
		return Arrays.asList(XML);
	}
	
	/* (non-Javadoc)
	 * @see java.util.ResourceBundle.Control#newBundle(java.lang.String, java.util.Locale, java.lang.String, java.lang.ClassLoader, boolean)
	 */
	@Override
	public ResourceBundle newBundle(String baseName, Locale locale,
		String format, ClassLoader loader, boolean reload)
				throws IllegalAccessException, InstantiationException, IOException {
		if ((baseName == null) || (locale == null) || (format == null)
				|| (loader == null)) { 
			throw new NullPointerException(); 
		}
		ResourceBundle bundle = null;
		
		if (format.equalsIgnoreCase(XML)) {
			// 1. Localize resource file
			String bundleName = toBundleName(baseName, locale);
			String resName = toResourceName(bundleName, format);
			URL url = loader.getResource(resName);
			
			// 2. Create Stream to the resource file
			if (url != null) {
				URLConnection connection = url.openConnection();
				if (connection != null) {
					if (reload) {
						connection.setUseCaches(false);
					}
					InputStream stream = connection.getInputStream();
					if (stream != null) {
						// 3. Create ResourceBundle object
						bundle = new XMLResourceBundle(stream);
						stream.close();
					}
				}
			}
		}
		// 4. Return the bundle.
		return bundle;
	}
	
}
