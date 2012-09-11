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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import de.zbit.util.prefs.SBPreferences;

/**
 * A {@link ResourceManager} avoids loading instances of {@link ResourceBundle}
 * multiple times by keeping these in a {@link Map} data structure. As
 * {@link ResourceBundle}s are a kind of unmodifiable {@link Map}-like data
 * structures, these do not have to be loaded again and again (in contrast to,
 * e.g., {@link SBPreferences}).
 * 
 * @author Andreas Dr&auml;ger
 * @date 2011-01-05
 * @version $Rev$
 * @since 1.0
 */
public class ResourceManager {
	
	/**
	 * {@link Locale}-dependent memory for already loaded {@link ResourceBundle}
	 * instances.
	 */
	private static Map<String, Map<Locale, ResourceBundle>> loaded;
	
	/**
	 * Loads the {@link ResourceBundle} with the given name for the default
	 * {@link Locale}. This method first tries to obtain the desired resource from
	 * the cache. Only if the resource has not yet been loaded it will try to
	 * obtain it from the file system. In this case it will also memorize the
	 * resource in the cache afterwards.
	 * 
	 * @param baseName
	 *        the name of the desired {@link ResourceBundle}.
	 * @return
	 * @see #getBundle(String, Locale)
	 * @exception NullPointerException
	 *            if <code>baseName</code>, <code>locales</code> or
	 *            <code>control</code> is {@code null}
	 * @exception MissingResourceException
	 *            if no resource bundle for the specified base name in any of the
	 *            {@link Locale}s can be found.
	 * @exception IllegalArgumentException
	 *            if the given <code>control</code> doesn't perform properly
	 *            (e.g., <code>control.getCandidateLocales</code> returns null.)
	 *            Note that validation of <code>control</code> is performed as
	 *            needed.
	 */
	public static ResourceBundle getBundle(String baseName) {
		return getBundle(baseName, Locale.getDefault());
	}
	
	/**
	 * Loads the {@link ResourceBundle} with the given name for the given
	 * {@link Locale}. This method first tries to obtain the desired resource from
	 * the cache. Only if the resource has not yet been loaded it will try to
	 * obtain it from the file system. In this case it will also memorize the
	 * resource in the cache afterwards.
	 * 
	 * @param baseName
	 * @param locale
	 * @return
	 * @exception NullPointerException
	 *            if <code>baseName</code>, <code>locales</code> or
	 *            <code>control</code> is {@code null}
	 * @exception MissingResourceException
	 *            if no resource bundle for the specified base name in any of the
	 *            {@link Locale}s can be found.
	 * @exception IllegalArgumentException
	 *            if the given <code>control</code> doesn't perform properly
	 *            (e.g., <code>control.getCandidateLocales</code> returns null.)
	 *            Note that validation of <code>control</code> is performed as
	 *            needed.
	 */
	public static ResourceBundle getBundle(String baseName, Locale locale) {
		if (loaded == null) {
			loaded = new HashMap<String, Map<Locale, ResourceBundle>>();
		}
		Map<Locale, ResourceBundle> bundle = loaded.get(baseName);
		ResourceBundle resource = null;
		if (bundle == null) {
			resource = loadBundle(baseName, locale);
			bundle = new HashMap<Locale, ResourceBundle>();
			bundle.put(locale, resource);
			loaded.put(baseName, bundle);
		} else {
			resource = bundle.get(locale);
			if (resource == null) {
				resource = loadBundle(baseName, locale);
				bundle.put(locale, resource);
			}
		}
		return resource;
	}
	
	/**
	 * This loads a {@link ResourceBundle} from the file system for the given
	 * {@link Locale}.
	 * 
	 * @param baseName
	 * @param locale
	 * @return the desired {@link ResourceBundle}
	 * @exception NullPointerException
	 *            if <code>baseName</code>, <code>locales</code> or
	 *            <code>control</code> is {@code null}
	 * @exception MissingResourceException
	 *            if no resource bundle for the specified base name in any of the
	 *            {@link Locale}s can be found.
	 * @exception IllegalArgumentException
	 *            if the given <code>control</code> doesn't perform properly
	 *            (e.g., <code>control.getCandidateLocales</code> returns null.)
	 *            Note that validation of <code>control</code> is performed as
	 *            needed.
	 */
	private static ResourceBundle loadBundle(String baseName, Locale locale) {
		ResourceBundle resource = null;
		Throwable exception = null;
		try {
			resource = ResourceBundle.getBundle(baseName, locale,
				new XMLResourceBundleControl());
		} catch (Throwable exc) {
			exception = exc;
			try {
				resource = ResourceBundle.getBundle(baseName, locale);
			} catch (Throwable e) {
				exception = e;
			}
		}
		if (exception != null) {
			if (exception instanceof MissingResourceException) {
				throw (MissingResourceException) exception;
			} else if (exception instanceof NullPointerException) {
				throw (NullPointerException) exception;
			} else if (exception instanceof IllegalArgumentException) {
				throw (IllegalArgumentException) exception;
			} else {
				throw new IllegalArgumentException(exception);
			}
		}
		return resource;
	}
	
}
