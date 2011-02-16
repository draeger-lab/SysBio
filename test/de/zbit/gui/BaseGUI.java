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
package de.zbit.gui;

import java.awt.Component;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import javax.swing.JFileChooser;
import javax.swing.JToolBar;

import de.zbit.util.prefs.KeyProvider;
import de.zbit.util.prefs.SBPreferences;
import de.zbit.util.prefs.SBProperties;

/**
 * @author Andreas Dr&auml;ger
 * @date 2010-11-28
 * @version $Rev$
 */
public class BaseGUI extends BaseFrame {
	
	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = -5082906933982414813L;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SBProperties props = SBPreferences.analyzeCommandLineArguments(
			GUIOptions.class, args);
		SBPreferences prefs = SBPreferences.getPreferencesFor(GUIOptions.class);
		prefs.putAll(props);
		new BaseGUI();
	}
	
	/**
	 * 
	 */
	public BaseGUI() {
		super();
		setVisible(true);
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#closeFile()
	 */
	public boolean closeFile() {
		// TODO Auto-generated method stub
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#createJToolBar()
	 */
	protected JToolBar createJToolBar() {
		return createDefaultToolBar();
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#createMainComponent()
	 */
	protected Component createMainComponent() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#exit()
	 */
	public void exit() {
		dispose();
		System.exit(0);
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#getCommandLineOptions()
	 */
	public Class<? extends KeyProvider>[] getCommandLineOptions() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#getURLAboutMessage()
	 */
	public URL getURLAboutMessage() {
		// TODO Auto-generated method stub
			return getClass().getResource("package.html");
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#getURLLicense()
	 */
	public URL getURLLicense() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#getURLOnlineHelp()
	 */
	public URL getURLOnlineHelp() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#openFile(java.io.File[])
	 */
	public File[] openFile(File... files) {
		if ((files != null) && (files.length > 0)) {
			System.out.printf("files opened:%s\n", Arrays.toString(files));
		} else {
			SBPreferences prefs = SBPreferences.getPreferencesFor(GUIOptions.class);
			files = GUITools.openFileDialog(this, prefs.get(GUIOptions.OPEN_DIR),
				true, true, JFileChooser.FILES_ONLY);
		}
		return files;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see de.zbit.gui.BaseFrame#saveFile()
	 */
	public void saveFile() {
		// TODO Auto-generated method stub
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see de.zbit.gui.BaseFrame#getApplicationName()
	 */
	public String getApplicationName() {
		return "Simple GUI";
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see de.zbit.gui.BaseFrame#getDottedVersionNumber()
	 */
	public String getDottedVersionNumber() {
		return "1.1";
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see de.zbit.gui.BaseFrame#getURLOnlineUpdate()
	 */
	public URL getURLOnlineUpdate() {
		try {
			return new URL(
				"http://www.ra.cs.uni-tuebingen.de/software/SBMLsqueezer/downloads/");
		} catch (MalformedURLException exc) {
			GUITools.showErrorMessage(this, exc);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#getMaximalFileHistorySize()
	 */
	public short getMaximalFileHistorySize() {
		return 5;
	}
}
