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
package de.zbit.garuda;

import java.util.ResourceBundle;

import de.zbit.util.ResourceManager;
import de.zbit.util.prefs.KeyProvider;
import de.zbit.util.prefs.Option;

/**
 * Useful options for the connection to the Garuda Core.
 * 
 * @author Andreas Dr&auml;ger
 * @date 14:05:45
 * @since 1.1
 * @version $Rev$
 */
public interface GarudaOptions extends KeyProvider {
	
	/**
	 * Localization support
	 */
	public static final ResourceBundle bundle = ResourceManager.getBundle("de.zbit.garuda.locales.Labels");
	
	/**
	 * Decides whether or not the current application should attempt to connect to
	 * the Garuda Core.
	 */
	public static final Option<Boolean> CONNECT_TO_GARUDA = new Option<Boolean>(
		"CONNECT_TO_GARUDA", Boolean.class, bundle, Boolean.TRUE);
	
}
