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
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = -5082906933982414813L;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new BaseGUI();
	}
	
	
	
	/* (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#getLocationOfBaseActionProperties()
	 */
	@Override
	protected String getLocationOfBaseActionProperties() {
		return "BaseActionGerman.xml";
	}



	public BaseGUI() {
		super();
		setVisible(true);
	}

	@Override
	public void closeFile() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected JToolBar createJToolBar() {
		return createDefaultToolBar();
	}

	@Override
	protected Component createMainComponent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void exit() {
		dispose();
	}

	@Override
	public Class<? extends KeyProvider>[] getCommandLineOptions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public URL getURLAboutMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public URL getURLLicense() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public URL getURLOnlineHelp() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public File openFile() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public File saveFile() {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public String getApplicationName() {
		return "Simple GUI";
	}
}
