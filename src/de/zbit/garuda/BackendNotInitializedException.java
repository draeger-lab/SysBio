/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2014 by the University of Tuebingen, Germany.
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

/**
 * A specialized {@link Exception} to be used in case that the Garuda backend
 * has not yet been initialized.
 * 
 * @author Andreas Dr&auml;ger
 * @date 09:05:28
 * @since 1.1
 * @version $Rev$
 */
public class BackendNotInitializedException extends Exception {
	
	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = 2670979247368834675L;

	/**
	 * 
	 */
	public BackendNotInitializedException() {
		super();
	}
	
	/**
	 * @param message
	 */
	public BackendNotInitializedException(String message) {
		super(message);
	}
	
	/**
	 * @param cause
	 */
	public BackendNotInitializedException(Throwable cause) {
		super(cause);
	}
	
	/**
	 * @param message
	 * @param cause
	 */
	public BackendNotInitializedException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
