/**
 * 
 */
package de.zbit.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowListener;
import java.beans.EventHandler;
import java.io.File;
import java.net.URL;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import de.zbit.gui.prefs.MultiplePreferencesPanel;
import de.zbit.gui.prefs.PreferencesDialog;
import de.zbit.util.StringUtil;
import de.zbit.util.prefs.KeyProvider;
import de.zbit.util.prefs.SBProperties;

/**
 * @author Andreas Dr&auml;ger
 * @date 2010-11-28
 */
public abstract class BaseFrame extends JFrame {
	
	/**
	 * 
	 * @author Andreas Dr&auml;ger
	 * @author Finja B&uuml;chel
	 * @date 2010-11-12
	 */
	public static enum BaseAction implements ActionCommand {
		/**
		 * 
		 */
		EDIT,
		/**
		 * {@link BaseAction} to configure the user's preferences in a dialog
		 * window.
		 */
		EDIT_PREFERENCES,
		/**
		 * 
		 */
		FILE,
		/**
		 * {@link BaseAction} to closes the currently opened file.
		 */
		FILE_CLOSE,
		/**
		 * {@link BaseAction} that closes the program and saves all user-defined
		 * preferences.
		 */
		FILE_EXIT,
		/**
		 * {@link BaseAction} to open a file.
		 */
		FILE_OPEN,
		/**
		 * {@link BaseAction} to saves the currently opened file or the result of a
		 * computation in one of the available formats.
		 */
		FILE_SAVE,
		/**
		 * 
		 */
		HELP,
		/**
		 * This {@link BaseAction} shows the imprint of this program and also
		 * explains who to contact if you encounter any problems with this program..
		 */
		HELP_ABOUT,
		/**
		 * {@link BaseAction} that displays the license terms under which this
		 * program is distributed.
		 */
		HELP_LICENSE,
		/**
		 * {@link BaseAction} that show the online help in a web browser.
		 */
		HELP_ONLINE;
		
		/**
		 * Contains the names and tool tips for the current language belonging to
		 * these {@link BaseAction}s.
		 */
		private static SBProperties nameProperties = new SBProperties();
		/**
		 * The separator between the short name of an action and its corresponding
		 * tool tip.
		 */
		private static final String separator = ";";
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see de.zbit.gui.ActionCommand#getName()
		 */
		public String getName() {
			String key = toString();
			String name = nameProperties.getProperty(key);
			if (name != null) {
				if (name.contains(separator)) {
					name = name.split(separator)[0];
				}
				return name;
			}
			return StringUtil.firstLetterUpperCase(key.toLowerCase().substring(
				key.lastIndexOf('_')));
		}
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see de.zbit.gui.ActionCommand#getToolTip()
		 */
		public String getToolTip() {
			String key = toString();
			String toolTip = nameProperties.getProperty(key);
			if (toolTip != null) {
				if (toolTip.contains(separator)) {
					toolTip = toolTip.split(separator)[1];
				}
				return toolTip;
			}
			return null;
		}
		
	}
	
	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = -6533854985804740883L;
	
	/**
	 * A tool bar
	 */
	protected JToolBar toolBar;
	
	/**
	 * @throws HeadlessException
	 */
	public BaseFrame() throws HeadlessException {
		super();
		init();
	}
	
	public BaseFrame(boolean loadDefaultFileMenuEntries) {
		super();
		init();
	}
	
	/**
	 * @param gc
	 */
	public BaseFrame(GraphicsConfiguration gc) {
		super(gc);
		init();
	}
	
	/**
	 * @param title
	 * @throws HeadlessException
	 */
	public BaseFrame(String title) throws HeadlessException {
		super(title);
		init();
	}
	
	/**
	 * @param title
	 * @param gc
	 */
	public BaseFrame(String title, GraphicsConfiguration gc) {
		super(title, gc);
		init();
	}
	
	/**
	 * 
	 */
	public final void activateOnlineHelpCommand() {
		GUITools.setEnabled(true, getJMenuBar(), toolBar,
			BaseAction.HELP_ONLINE, BaseAction.HELP_LICENSE);
	}
	
	/**
	 * 
	 * @return
	 */
	protected JMenuItem[] additionalEditMenuItems() {
		// empty method
		return null;
	}
	
	/**
	 * Additional items to be added to the file menu.
	 * 
	 * @return
	 */
	protected JMenuItem[] additionalFileMenuItems() {
		// empty method
		return null;
	}
	
	/**
	 * 
	 * @return
	 */
	protected JMenuItem[] additionalHelpMenuItems() {
		// empty method
		return null;
	}
	
	/**
	 * @return
	 */
	protected JMenu[] additionalMenus() {
		// empty method
		return null;
	}
	
	/**
	 * Closes a {@link File} that is currently open.
	 */
	public abstract void closeFile();
	
	/**
	 * 
	 * @return
	 */
	public JToolBar createDefaultToolBar() {
		JMenu menu;
		JMenuItem item;
		Object action = null;
		JToolBar toolBar = new JToolBar("Tools");
		for (int i = 0; i < getJMenuBar().getMenuCount(); i++) {
			menu = getJMenuBar().getMenu(i);
			for (int j = 0; j < menu.getItemCount(); j++) {
				item = menu.getItem(j);
				if (item != null) {
					try {
						action = BaseAction.valueOf(item.getActionCommand());
						if (action == BaseAction.FILE_EXIT) {
							action = null;
						}
					} catch (Throwable exc) {
						action = item.getActionCommand();
					}
					if ((item.getIcon() != null) && (action != null)
							&& (item.getActionListeners().length > 0)) {
						JButton button = GUITools.createButton(item.getIcon(), item
								.getActionListeners()[0], action, item.getToolTipText());
						button.setEnabled(item.isEnabled());
						toolBar.add(button);
					}
				}
			}
			if (i < getJMenuBar().getMenuCount() - 1) {
				toolBar.add(new JSeparator(JSeparator.VERTICAL));
			}
		}
		return toolBar;
	}
	
	/**
	 * 
	 * @param url
	 * @param preferedWidth
	 * @param preferedHeight
	 * @param scorll
	 * @return
	 */
	private final JComponent createJBrowser(URL url, int preferedWidth,
		int preferedHeight, boolean scroll) {
		JBrowserPane browser = new JBrowserPane(url);
		browser.removeHyperlinkListener(browser);
		browser.addHyperlinkListener(new SystemBrowser());
		browser.setPreferredSize(new Dimension(preferedWidth, preferedHeight));
		if (scroll) { return new JScrollPane(browser,
			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
			JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED); }
		browser.setBorder(BorderFactory.createLoweredBevelBorder());
		return browser;
	}
	
	/**
	 * @param loadDefaultFileMenuEntries
	 *        Switch to decide weather or not to load the default entries in the
	 *        File menu for Open, Save, and Close.
	 * @return
	 */
	protected JMenuBar createJMenuBar(boolean loadDefaultFileMenuEntries) {
		JMenuBar menuBar = new JMenuBar();
		String title;
		
		/*
		 * File menu
		 */
		JMenuItem openFile = loadDefaultFileMenuEntries ? GUITools.createJMenuItem(
			EventHandler.create(ActionListener.class, this, "openFile"),
			BaseAction.FILE_OPEN, UIManager.getIcon("ICON_OPEN_16"), KeyStroke
					.getKeyStroke('O', InputEvent.CTRL_DOWN_MASK), 'O', true) : null;
		JMenuItem saveFile = loadDefaultFileMenuEntries ? GUITools.createJMenuItem(
			EventHandler.create(ActionListener.class, this, "saveFile"),
			BaseAction.FILE_SAVE, UIManager.getIcon("ICON_SAVE_16"), KeyStroke
					.getKeyStroke('S', InputEvent.CTRL_DOWN_MASK), 'S', false) : null;
		JMenuItem close = loadDefaultFileMenuEntries ? GUITools.createJMenuItem(
			EventHandler.create(ActionListener.class, this, "closeFile"),
			BaseAction.FILE_CLOSE, UIManager.getIcon("ICON_TRASH_16"), KeyStroke
					.getKeyStroke('W', InputEvent.CTRL_DOWN_MASK), 'W', false) : null;
		JMenuItem exit = GUITools.createJMenuItem(EventHandler.create(
			ActionListener.class, this, "exit"), BaseAction.FILE_EXIT, UIManager
				.getIcon("ICON_EXIT_16"), KeyStroke.getKeyStroke(KeyEvent.VK_F4,
			InputEvent.ALT_DOWN_MASK));
		JMenuItem items[] = additionalFileMenuItems();
		boolean addSeparator = (openFile != null) || (saveFile != null)
				|| ((items != null) && (items.length > 0)) || (close != null);
		title = BaseAction.nameProperties.getProperty(BaseAction.FILE);
		menuBar.add(GUITools.createJMenu(title == null ? "File" : title, openFile,
			saveFile, items, close, addSeparator ? new JSeparator() : null, exit));
		
		/*
		 * Edit menu
		 */
		JMenuItem preferences = GUITools.createJMenuItem(EventHandler.create(
			ActionListener.class, this, "preferences"), BaseAction.EDIT_PREFERENCES,
			UIManager.getIcon("ICON_PREFS_16"), KeyStroke.getKeyStroke('E',
				InputEvent.ALT_GRAPH_DOWN_MASK), 'P', true);
		items = additionalEditMenuItems();
		// Speed up the GUI by loading the preferences classes at the beginning
		// and add this menu only if there is at least one preference panel defined.
		int numPrefs = MultiplePreferencesPanel.getPossibleTabCount();
		if ((numPrefs > 0) || ((items != null) && (items.length > 0))) {
			title = BaseAction.nameProperties.getProperty(BaseAction.EDIT);
			menuBar.add(GUITools.createJMenu(title == null ? "Edit" : title, items, (items != null)
					&& (items.length > 0) && (numPrefs > 0) ? new JSeparator() : null,
				numPrefs > 0 ? preferences : null));
		}
		
		/*
		 * Additional menus
		 */
		JMenu[] additionalMenus = additionalMenus();
		if (additionalMenus != null) {
			for (JMenu menu : additionalMenus) {
				if (menu != null) {
					menuBar.add(menu);
				}
			}
		}
		
		/*
		 * Help menu
		 */
		JMenuItem help = GUITools.createJMenuItem(EventHandler.create(
			ActionListener.class, this, "showOnlineHelp"),
			BaseAction.HELP_ONLINE, UIManager.getIcon("ICON_HELP_16"), KeyStroke
					.getKeyStroke(KeyEvent.VK_F1, 0), 'H', true);
		if (getURLOnlineHelp() == null) {
			help = null;
		}
		JMenuItem about = GUITools.createJMenuItem(EventHandler.create(
			ActionListener.class, this, "showAboutMessage"),
			BaseAction.HELP_ABOUT, UIManager.getIcon("ICON_INFO_16"), KeyStroke
					.getKeyStroke(KeyEvent.VK_F2, 0), 'I', true);
		if (getURLAboutMessage() == null) {
			about = null;
		}
		JMenuItem license = GUITools.createJMenuItem(EventHandler.create(
			ActionListener.class, this, "showLicense"), BaseAction.HELP_LICENSE,
			UIManager.getIcon("ICON_LICENSE_16"), KeyStroke.getKeyStroke(
				KeyEvent.VK_F3, 0), 'L', true);
		if (getURLLicense() == null) {
			license = null;
		}
		title = BaseAction.nameProperties.get(BaseAction.HELP);
		JMenu helpMenu = GUITools.createJMenu(title == null ? "Help" : title, help,
			about, license, additionalHelpMenuItems());
		if (helpMenu.getItemCount() > 0) {
			try {
				menuBar.setHelpMenu(helpMenu);
			} catch (Error exc) {
				menuBar.add(helpMenu);
			}
		}
		
		return menuBar;
	}
	
	/**
	 * By default this method simply returns null. You can override it to create
	 * your own tool bar.
	 * 
	 * @return
	 */
	protected abstract JToolBar createJToolBar();
	
	/**
	 * @return
	 */
	protected abstract Component createMainComponent();
	
	/**
	 * Method that is called on exit, i.e.,when this window is closing.
	 */
	
	public abstract void exit();
	
	/**
	 * @return
	 */
	public abstract Class<? extends KeyProvider>[] getCommandLineOptions();
	
	/**
	 * 
	 * @return
	 */
	protected String getLocationOfBaseActionProperties() {
		return null;
	}
	
	/**
	 * 
	 * @return
	 */
	public abstract String getApplicationName();
	
	/**
	 * @return
	 */
	public abstract URL getURLAboutMessage();
	
	/**
	 * @return
	 */
	public abstract URL getURLLicense();
	
	/**
	 * @return
	 */
	public abstract URL getURLOnlineHelp();
	
		
	/**
 * 
 */
	protected void init() {
		GUITools.initLaF(getApplicationName());
		
		try {
			Properties defaults = new Properties();
			defaults.loadFromXML(getClass().getResourceAsStream(
				"BaseActionEnglish.xml"));
			BaseAction.nameProperties.setDefaults(defaults);
			String location = getLocationOfBaseActionProperties();
			if (location != null) {
				Properties properties = new Properties();
				try {
					properties.loadFromXML(getClass().getResourceAsStream(location));
					if (properties != null) {
						BaseAction.nameProperties.putAll(properties);
					}
				} catch (Exception exc) {
				}
			}
		} catch (Exception exc) {
			GUITools.showErrorMessage(this, exc);
		}
				
		// init GUI
		// Do nothing is important! The actual closing is handled in "windowClosing()"
		// which is not called on other close operations!
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(EventHandler.create(WindowListener.class, this, "exit",
			null, "windowClosing"));
		setJMenuBar(createJMenuBar(loadsDefaultFileMenuEntries()));
		
		Container container = getContentPane();
		container.setLayout(new BorderLayout());
		
		toolBar = createJToolBar();
		if (toolBar != null) {
			container.add(toolBar, BorderLayout.NORTH);
		}
		Component component = createMainComponent();
		if (component != null) {
			container.add(component, BorderLayout.CENTER);
		}
		
		pack();
		setMinimumSize(new Dimension(640, 480));
		setLocationRelativeTo(null);
	}

	/**
	 * This method decides whether or not the file menu should already be equipped
	 * with the default entries for "open", "save", and "close". By default, this
	 * method returns <code>true</code>, i.e., the default File menu already
	 * contains all these three items. You may want to override this method and to
	 * return <code>false</code> instead to switch this behavior off.
	 * 
	 * @return Whether or not to equip the file menu with the three entries
	 *         "open", "save", and "close". By default <code>true</code>
	 */
	protected boolean loadsDefaultFileMenuEntries() {
		return true;
	}
	
	/**
	 * Opens some {@link File}.
	 * 
	 * @return
	 */
	public abstract File openFile();
	
	/**
	 * Displays the configuration for the {@link PreferencesDialog}.
	 */
	public void preferences() {
		PreferencesDialog.showPreferencesDialog();
	}
	
	/**
	 * Saves some {@link File}.
	 * 
	 * @return
	 */
	public abstract File saveFile();
	
	/**
	 * 
	 */
	public final void showAboutMessage() {
		JOptionPane.showMessageDialog(this, createJBrowser(getURLAboutMessage(),
			380, 220, false), "About", JOptionPane.INFORMATION_MESSAGE);
	}
	
	/**
	 * 
	 */
	public final void showLicense() {
		JOptionPane.showMessageDialog(this, createJBrowser(getURLLicense(), 640,
			480, true), "License", JOptionPane.INFORMATION_MESSAGE, UIManager
				.getIcon("ICON_LICENSE_64"));
	}
	
	/**
	 * 
	 */
	public final void showOnlineHelp() {
		GUITools.setEnabled(false, getJMenuBar(), toolBar,
			BaseAction.HELP_ONLINE);
		JHelpBrowser.showOnlineHelp(this, EventHandler.create(WindowListener.class,
			this, "activateOnlineHelpCommand", null, "windowClosed"), getTitle()
				+ " - Online Help", getURLOnlineHelp(), getCommandLineOptions());
	}
	
}
