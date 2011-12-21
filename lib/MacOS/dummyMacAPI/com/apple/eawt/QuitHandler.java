/* $Id$
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
package com.apple.eawt;

import com.apple.eawt.AppEvent.QuitEvent;

/**
 * Dummy interface to imitate the API of the Mac OS X specific Java API.
 * 
 * @author Andreas Dr&auml;ger
 * @date 10:58:12
 * @since 1.1
 * @version $Rev$
 */
public interface QuitHandler {
	
	/**
	 * Imitates the corresponding method in the real Mac OS X specific Java API.
	 * 
	 * @param evt
	 * @param qr
	 */
	public void handleQuitRequestWith(QuitEvent evt, QuitResponse qr);
	
}