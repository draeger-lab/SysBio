/* $Id$
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
package de.zbit.gui.mac;

import java.awt.Image;
import java.beans.EventHandler;
import java.beans.PropertyChangeListener;
import java.util.List;

import com.apple.eawt.AboutHandler;
import com.apple.eawt.AppEvent.AboutEvent;
import com.apple.eawt.AppEvent.PreferencesEvent;
import com.apple.eawt.AppEvent.QuitEvent;
import com.apple.eawt.Application;
import com.apple.eawt.PreferencesHandler;
import com.apple.eawt.QuitHandler;
import com.apple.eawt.QuitResponse;
import com.apple.eawt.QuitStrategy;

import de.zbit.gui.BaseFrame;
import de.zbit.gui.ImageTools;

/**
 * @author Andreas Dr&auml;ger
 * @author Clemens Wrzodek
 * @since 1.1
 * @version $Rev$
 */
public class MacOSXController2 implements AboutHandler, PreferencesHandler, QuitHandler {
	
	/**
	 * The actual graphical element whose properties are to be linked to Mac OS X
	 * specific actions.
	 */
	private BaseFrame frame;
	
	/**
	 * 
	 * @param frame
	 */
	public MacOSXController2(BaseFrame frame) {
		this.frame = frame;
		Application app = Application.getApplication();
		app.setAboutHandler(this);
		app.setPreferencesHandler(this);
		app.setQuitHandler(this);
		app.setQuitStrategy(QuitStrategy.SYSTEM_EXIT_0);
		
		frame.addPropertyChangeListener("iconImage", EventHandler.create(
			PropertyChangeListener.class, this, "setXDocIconToFrameIcon"));
		// -Xdock:icon="bin/de/zbit/kegg/gui/img/KEGGtranslatorIcon_256.png"
		// -Xdock:name="KEGGtranslator"
		
	}
	
	/**
	 * 
	 */
	public void setXDocIconToFrameIcon() {
		Application app = Application.getApplication();
		Image icon = frame.getIconImage() != null ? frame.getIconImage() : null;
		List<Image> listOfImages = frame.getIconImages();
		if ((listOfImages != null) && (listOfImages.size() > 0)) {
			app.setDockIconImage(ImageTools.getImageOfHighestResolution(listOfImages, null));
		} else {
		  app.setDockIconImage(icon);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.apple.eawt.AboutHandler#handleAbout(com.apple.eawt.AppEvent.AboutEvent)
	 */
	public void handleAbout(AboutEvent aEvt) {
		frame.showAboutMessage();
	}
	
	/* (non-Javadoc)
	 * @see com.apple.eawt.PreferencesHandler#handlePreferences(com.apple.eawt.AppEvent.PreferencesEvent)
	 */
	public void handlePreferences(PreferencesEvent pEvt) {
		frame.preferences();
	}
	
	/* (non-Javadoc)
	 * @see com.apple.eawt.QuitHandler#handleQuitRequestWith(com.apple.eawt.AppEvent.QuitEvent, com.apple.eawt.QuitResponse)
	 */
	public void handleQuitRequestWith(QuitEvent qEvt, QuitResponse qr) {
		frame.exitPre();
		if (frame.isDisplayable()) {
			qr.cancelQuit();
		} else {
		  qr.performQuit();
		}
	}
	
}
