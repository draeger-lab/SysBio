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
package de.zbit.exception;

/**
 * Used in "InfoManagement" to distinct between temporary errors and actual
 * unsuccessfull retrievements.
 * 
 * @author Clemens Wrzodek
 * @version $Rev$
 * @since 1.0
 */
public class UnsuccessfulRetrieveException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4556630433492743411L;

	/**
	 * 
	 */
	public UnsuccessfulRetrieveException() {
		super();
	}

  public UnsuccessfulRetrieveException(String message, Throwable cause) {
    super(message, cause);
  }

  public UnsuccessfulRetrieveException(String message) {
    super(message);
  }

  public UnsuccessfulRetrieveException(Throwable cause) {
    super(cause);
  }
}
