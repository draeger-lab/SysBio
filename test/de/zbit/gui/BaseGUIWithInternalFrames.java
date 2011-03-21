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

import java.io.File;
import java.net.URL;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JToolBar;

import de.zbit.io.SBFileFilter;
import de.zbit.util.prefs.KeyProvider;

/**
 * @author Andreas Dr&auml;ger
 * @since 18.03.2011
 * @version $Rev$
 */
public class BaseGUIWithInternalFrames extends BaseFrameWithInternalFrames {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6365692957218072643L;
	
	/* (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#createJToolBar()
	 */
	@Override
	protected JToolBar createJToolBar() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/* (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#exit()
	 */
	@Override
	public void exit() {
		System.exit(0);
	}
	
	/* (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#getApplicationName()
	 */
	@Override
	public String getApplicationName() {
		return "InternalFrameTest";
	}
	
	/* (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#getCommandLineOptions()
	 */
	@Override
	public Class<? extends KeyProvider>[] getCommandLineOptions() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/* (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#getDottedVersionNumber()
	 */
	@Override
	public String getDottedVersionNumber() {
		return "0.1";
	}
	
	/* (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#getMaximalFileHistorySize()
	 */
	@Override
	public short getMaximalFileHistorySize() {
		return 0;
	}
	
	/* (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#getURLAboutMessage()
	 */
	@Override
	public URL getURLAboutMessage() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/* (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#getURLLicense()
	 */
	@Override
	public URL getURLLicense() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/* (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#getURLOnlineHelp()
	 */
	@Override
	public URL getURLOnlineHelp() {
		return getClass().getResource("package.html");
	}
	
	/* (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#getURLOnlineUpdate()
	 */
	@Override
	public URL getURLOnlineUpdate() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/* (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#openFile(java.io.File[])
	 */
	@Override
	protected File[] openFile(File... files) {
		if (files.length == 0) {
			files = GUITools
					.openFileDialog(this, getOpenDir().getAbsolutePath(), true, true,
						JFileChooser.FILES_ONLY, SBFileFilter.createAllFileFilter());
		}
		for (File file : files) {
			JInternalFrame iFrame = new JInternalFrame(file.getName(), true, true, true, true);
			iFrame.setSize(300, 300);
			iFrame.setVisible(true);
			desktop.add(iFrame);
		}
		return files;
	}
	
	/* (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#saveFile()
	 */
	@Override
	public void saveFile() {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		JFrame f = new BaseGUIWithInternalFrames();
		f.setVisible(true);
	}

	/* (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#closeFile()
	 */
	@Override
	public boolean closeFile() {
		// TODO Auto-generated method stub
		return false;
	}
	
}
