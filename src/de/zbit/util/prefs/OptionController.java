/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2011 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.util.prefs;

/**
 * @author Andreas Dr&auml;ger
 * @since 04.04.2011
 * @version $Rev$
 */
public interface OptionController {
	
	/**
	 * 
	 * @param <T>
	 * @param option
	 * @param value
	 * @see #addDependency(Option, Object, boolean) with default of
	 *      <code>enablingControl == true</code>
	 */
	public <T> void addDependency(Option<T> option, T value);
	
	/**
	 * 
	 * @param <T>
	 * @param option
	 * @param value
	 * @param enablingControl
	 */
	public <T> void addDependency(Option<T> option, T value, boolean enablingControl);
	
	/**
	 * 
	 * @return
	 */
	public boolean checkDependency();
	
}
