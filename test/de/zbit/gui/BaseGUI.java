/**
 * 
 */
package de.zbit.gui;

import java.awt.Component;
import java.io.File;
import java.net.URL;

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
	 * @see de.zbit.gui.BaseFrame#exit()
	 */
	public void exit() {
		System.exit(0);
	}

	/* (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#createJToolBar()
	 */
	@Override
	protected JToolBar createJToolBar() {
		return createDefaultToolBar();
	}

	/* (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#createMainComponent()
	 */
	@Override
	protected Component createMainComponent() {
		// TODO Auto-generated method stub
		return null;
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
	 * @see de.zbit.gui.BaseFrame#openFile()
	 */
	@Override
	public File openFile() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#saveFile()
	 */
	@Override
	public File saveFile() {
		// TODO Auto-generated method stub
		return null;
	}

  /* (non-Javadoc)
   * @see de.zbit.gui.BaseFrame#getTitle()
   */
  @Override
  public String getTitle() {
    // TODO Auto-generated method stub
    return null;
  }
	
}
