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
package de.zbit.util;

/**
 * TTY is a console that accepts ANSI commands.
 * This class checks whether or not the System's console
 * is of type TTY.
 * 
 * @author Clemens Wrzodek
 * @version $Rev$
 * @since 1.0
 */
public class isTTY_Java16only {
	/**
	 * Returns if underlying System.out stream is a console or not. DO NOT USE
	 * THIS FUNCTION WITH JAVA 1.5. THIS OR SIMILAR FUNCTIONS ARE ONLY AVAILABLE
	 * VIA A NATIVE INTERFACE AND THUS NEARLY IMPOSSIBLE IN JDK5 !!!
	 * 
	 * @return
	 */
	public static boolean isTty() throws Throwable {
		if ((System.console() == null) || (System.console().writer() == null)) {
			return false;
		}
		return true;
	}
}
