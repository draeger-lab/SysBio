/* $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2015 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.gui.mac;

import com.apple.mrj.MRJAboutHandler;
import com.apple.mrj.MRJApplicationUtils;
import com.apple.mrj.MRJPrefsHandler;
import com.apple.mrj.MRJQuitHandler;

import de.zbit.gui.BaseFrame;

/**
 * Adaptation of a {@link BaseFrame} to Mac OS X.
 * 
 * @author Andreas Dr&auml;ger
 * @date 10:58:12
 * @since 1.1
 * @version $Rev$
 */
@SuppressWarnings("deprecation")
public class MacOSXController implements MRJAboutHandler, MRJQuitHandler, MRJPrefsHandler {
	
	/**
	 * The actual graphical element whose properties are to be linked to Mac OS X
	 * specific actions.
	 */
	private BaseFrame frame;
	
	/**
	 * 
	 * @param frame
	 */
	public MacOSXController(BaseFrame frame) {
		this.frame = frame;
		MRJApplicationUtils.registerAboutHandler(this);
		MRJApplicationUtils.registerPrefsHandler(this);
		MRJApplicationUtils.registerQuitHandler(this);
	}

	/* (non-Javadoc)
	 * @see com.apple.mrj.MRJAboutHandler#handleAbout()
	 */
	public void handleAbout() {
		frame.showAboutMessage();
	}

	/* (non-Javadoc)
	 * @see com.apple.mrj.MRJPrefsHandler#handlePrefs()
	 */
	public void handlePrefs() throws IllegalStateException {
		frame.preferences();
	}

	/* (non-Javadoc)
	 * @see com.apple.mrj.MRJQuitHandler#handleQuit()
	 */
	public void handleQuit() throws IllegalStateException {
		frame.exitPre();
	}
	
}
