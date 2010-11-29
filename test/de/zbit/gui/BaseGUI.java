/**
 * 
 */
package de.zbit.gui;

import java.awt.Component;
import java.net.URL;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import de.zbit.util.prefs.KeyProvider;

/**
 * @author Andreas Dr&auml;ger
 * @date 2010-11-28
 */
public class BaseGUI extends BaseFrame {
	
	/**
	 * 
	 */
	public static String APPLICATION_NAME = "Base GUI";
	
	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = -5082906933982414813L;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new BaseGUI();
	}
	
	/**
	 * 
	 */
	public BaseGUI() {
		super();
		setVisible(true);
	}

	/* (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#additionalEditMenuItems()
	 */
	@Override
	protected JMenuItem[] additionalEditMenuItems() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#additionalFileMenuItems()
	 */
	@Override
	protected JMenuItem[] additionalFileMenuItems() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#additionalHelpMenuItems()
	 */
	@Override
	protected JMenuItem[] additionalHelpMenuItems() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#additionalMenus()
	 */
	@Override
	protected JMenu[] additionalMenus() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#closeFile()
	 */
	@Override
	public void closeFile() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#createJToolBar()
	 */
	protected JToolBar createJToolBar() {
		return createDefaultToolBar();
	}

	/* (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#createMainComponent()
	 */
	protected Component createMainComponent() {
		return new JPanel();
	}

	/* (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#exit()
	 */
	public void exit() {
		System.exit(0);
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
	 * @see de.zbit.gui.BaseFrame#getAboutMessageLocation()
	 */
	@Override
	public URL getURLAboutMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#getLicenseURL()
	 */
	@Override
	public URL getURLLicense() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#getOnlineHelpLocation()
	 */
	@Override
	public URL getURLOnlineHelp() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#openFile()
	 */
	@Override
	public void openFile() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#saveFile()
	 */
	@Override
	public void saveFile() {
		// TODO Auto-generated method stub
		
	}
	
}
