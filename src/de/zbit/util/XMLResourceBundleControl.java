/**
 * 
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
import java.util.Set;
import java.util.ResourceBundle.Control;

/**
 * This class provides the necessary functionality to load a
 * {@link ResourceBundle} from an XML formatted file (for the specification of
 * the document see {@link Properties}).
 * 
 * Example usage:
 * <pre>
 * ResourceBundle resource = ResourceBundle.getBundle(
      "de.zbit.locales.Labels", new XMLResourceBundleControl());
   String myString = resource.getString("MY_KEY");
 * </pre>
 * 
 * @author Andreas Dr&auml;ger
 * @date 2011-01-04
 */
public class XMLResourceBundleControl extends Control {
	
	/**
	 * 
	 * @author Andreas Dr&auml;ger
	 * @date 2011-01-04
	 */
	public static class XMLResourceBundle extends ResourceBundle {
		
		/**
		 * 
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

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.ResourceBundle#getKeys()
		 */
		public Enumeration<String> getKeys() {
			Set<String> key = properties.stringPropertyNames();
			return Collections.enumeration(key);
		}
		
		/**
		 * @return the properties
		 */
		public Properties toProperties() {
			return properties;
		}
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.ResourceBundle#handleGetObject(java.lang.String)
		 */
		protected Object handleGetObject(String key) {
			return properties.getProperty(key);
		}
		
	}
	
	/**
	 * The extension for XML files.
	 */
	public static final String XML = "xml";
		
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.ResourceBundle.Control#getFormats(java.lang.String)
	 */
	@Override
	public List<String> getFormats(String baseName) {
		return Arrays.asList(XML);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.ResourceBundle.Control#newBundle(java.lang.String,
	 * java.util.Locale, java.lang.String, java.lang.ClassLoader, boolean)
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