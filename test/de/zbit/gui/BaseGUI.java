/**
 * 
 */
package de.zbit.gui;

import java.awt.Component;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import javax.swing.JToolBar;

import de.zbit.util.prefs.KeyProvider;

/**
 * @author Andreas Dr&auml;ger
 * @date 2010-11-28
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
		System.setProperty("user.language", Locale.ENGLISH.getLanguage());
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
		return null;
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
	 * 
	 * @see de.zbit.gui.BaseFrame#openFile()
	 */
	public void openFile() {
		// TODO Auto-generated method stub
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
}
