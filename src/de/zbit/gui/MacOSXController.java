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
package de.zbit.gui;

import com.apple.eawt.AboutHandler;
import com.apple.eawt.AppEvent.AboutEvent;
import com.apple.eawt.AppEvent.PreferencesEvent;
import com.apple.eawt.AppEvent.QuitEvent;
import com.apple.eawt.Application;
import com.apple.eawt.PreferencesHandler;
import com.apple.eawt.QuitHandler;
import com.apple.eawt.QuitResponse;

/**
 * Adaptation of a {@link BaseFrame} to Mac OS X.
 * 
 * @author Andreas Dr&auml;ger
 * @date 10:58:12
 * @since 1.1
 * @version $Rev$
 */
public class MacOSXController implements AboutHandler, PreferencesHandler, QuitHandler {
	
	private BaseFrame frame;
	
	/**
	 * 
	 * @param frame
	 */
	public MacOSXController(BaseFrame frame) {
		this.frame = frame;
		Application app = Application.getApplication();
		app.setAboutHandler(this);
		app.setPreferencesHandler(this);
		app.setQuitHandler(this);
	}

	/* (non-Javadoc)
	 * @see com.apple.eawt.AboutHandler#handleAbout(com.apple.eawt.AppEvent.AboutEvent)
	 */
	public void handleAbout(AboutEvent arg0) {
		frame.showAboutMessage();
	}

	/* (non-Javadoc)
	 * @see com.apple.eawt.PreferencesHandler#handlePreferences(com.apple.eawt.AppEvent.PreferencesEvent)
	 */
	public void handlePreferences(PreferencesEvent arg0) {
		frame.preferences();
	}

	/* (non-Javadoc)
	 * @see com.apple.eawt.QuitHandler#handleQuitRequestWith(com.apple.eawt.AppEvent.QuitEvent, com.apple.eawt.QuitResponse)
	 */
	public void handleQuitRequestWith(QuitEvent arg0, QuitResponse arg1) {
		frame.exitPre();
	}
	
}
