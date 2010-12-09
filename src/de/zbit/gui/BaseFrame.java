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
import java.awt.MenuItem;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowListener;
import java.beans.EventHandler;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.Icon;
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
import de.zbit.gui.prefs.PreferencesPanel;
import de.zbit.util.StringUtil;
import de.zbit.util.prefs.KeyProvider;
import de.zbit.util.prefs.Option;
import de.zbit.util.prefs.SBPreferences;
import de.zbit.util.prefs.SBProperties;

/**
 * This class provides a basic and easily extendable implementation of a
 * graphical user interface, which already contains some actions and nice
 * features. In this way, this class can serve as a multi-purpose element for
 * any user interface.
 * 
 * @author Andreas Dr&auml;ger
 * @date 2010-11-28
 */
public abstract class BaseFrame extends JFrame {
	
	/**
	 * This enum contains very basic actions of a graphical user interface.
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
		 * preferences. All opened files might also be closed and or saved.
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
		HELP_ONLINE,
		/**
		 * {@link BaseAction} to check for a newer version of this program on the
		 * web.
		 */
		HELP_UPDATE;
		
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
				return name.trim();
			}
			return StringUtil.firstLetterUpperCase(key.toLowerCase().substring(
				key.lastIndexOf('_') + 1));
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
				return toolTip.trim();
			}
			return null;
		}
		
	}
	
	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = -6533854985804740883L;
	
	/**
	 * Switch to avoid checking for updates multiple times.
	 */
	private static boolean UPDATE_CHECKED = false;
	
	/**
	 * A tool bar
	 */
	protected JToolBar toolBar;
	
	/**
	 * Creates a new {@link BaseFrame}.
	 * 
	 * @throws HeadlessException
	 */
	public BaseFrame() throws HeadlessException {
		super();
		init();
	}
	
	/**
	 * Creates a new {@link BaseFrame}, for which it can be decided whether or not
	 * to include {@link BaseAction#FILE_OPEN}, {@link BaseAction#FILE_SAVE}, and
	 * {@link BaseAction#FILE_CLOSE} to the File menu. Default: true.
	 * 
	 * @param loadDefaultFileMenuEntries
	 */
	public BaseFrame(boolean loadDefaultFileMenuEntries) {
		super();
		init();
	}
	
	/**
	 * Creates a new {@link BaseFrame} with the given
	 * {@link GraphicsConfiguration}.
	 * 
	 * @param gc
	 */
	public BaseFrame(GraphicsConfiguration gc) {
		super(gc);
		init();
	}
	
	/**
	 * Creates a new {@link BaseFrame} with the given title.
	 * 
	 * @param title
	 * @throws HeadlessException
	 */
	public BaseFrame(String title) throws HeadlessException {
		super(title);
		init();
	}
	
	/**
	 * Creates a new {@link BaseFrame} with the given title and the given
	 * {@link GraphicsConfiguration}.
	 * 
	 * @param title
	 * @param gc
	 * 
	 */
	public BaseFrame(String title, GraphicsConfiguration gc) {
		super(title, gc);
		init();
	}
	
	/**
	 * This method enables the {@link JMenuItem} that displays the online help and
	 * also enables the {@link JButton} in the {@link JToolBar} linked to this
	 * action. If these elements don't exist, nothing will happen.
	 */
	public final void activateOnlineHelpCommand() {
		GUITools.setEnabled(true, getJMenuBar(), toolBar, BaseAction.HELP_ONLINE,
			BaseAction.HELP_LICENSE);
	}
	
	/**
	 * Override this method to add additional {@link JMenuItem}s to the Edit menu
	 * (if this exists, otherwise the given {@link JMenuItem} will define the Edit
	 * menu). If the {@link BaseAction#EDIT_PREFERENCES} is present in the Edit
	 * menu, these items will be placed on top of it and there will be a
	 * {@link JSeparator} over the preferences item.
	 * 
	 * @return
	 */
	protected JMenuItem[] additionalEditMenuItems() {
		// empty method
		return null;
	}
	
	/**
	 * Override this method to add additional {@link JMenuItem}s to the File menu.
	 * These will be placed after the command {@link BaseAction#FILE_CLOSE} (if
	 * this entry exists, otherwise more to the top) and before the following
	 * {@link JSeparator} over the {@link BaseAction#FILE_EXIT}.
	 * 
	 * @return
	 */
	protected JMenuItem[] additionalFileMenuItems() {
		// empty method
		return null;
	}
	
	/**
	 * Override this method to add additional {@link JMenuItem}s to the help menu.
	 * These will be placed under the {@link JMenuItem} for the online update (if
	 * this one exists, otherwise in a higher position).
	 * 
	 * @return
	 */
	protected JMenuItem[] additionalHelpMenuItems() {
		// empty method
		return null;
	}
	
	/**
	 * Override this method to add any additional {@link JMenu} to this
	 * {@link BaseFrame}'s {@link JMenuBar}. The ordering of the additional
	 * {@link JMenu}s will not be changed and they will be placed between the Edit
	 * and Help menu (if these exist, otherwise more to the left).
	 * 
	 * @return
	 */
	protected JMenu[] additionalMenus() {
		// empty method
		return null;
	}
	
	/**
	 * Closes a {@link File} that is currently open.
	 * 
	 * @return Whether or not calling this method lead to any change on this
	 *         {@link BaseFrame}.
	 */
	public abstract boolean closeFile();
	
	/**
	 * This method runs over all {@link JMenu}s in this {@link BaseFrame}'s
	 * {@link JMenuBar} and looks on every {@link JMenuItem}. If the current
	 * {@link JMenuItem} contains an {@link Icon}, a new {@link JButton} will be
	 * created and added to a newly created {@link JToolBar}. After visiting an
	 * entire {@link JMenu}, a {@link JSeparator} is added to the {@link JToolBar}
	 * . The only {@link JMenuItem} that is definitely excluded from the
	 * {@link JToolBar} is an item whose associated {@link ActionCommand} is
	 * {@link BaseAction#FILE_EXIT}.
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
	 * This creates a new {@link JBrowserPane} which can be embedded in a
	 * {@link JScrollPane} depending on the value of the boolean parameter.
	 * 
	 * @param url
	 *        The {@link URL} of the HTML file to be displayed in the
	 *        {@link JBrowserPane}.
	 * @param preferedWidth
	 *        the width dimension of the {@link JBrowserPane}.
	 * @param preferedHeight
	 *        the height dimension of the {@link JBrowserPane}.
	 * @param scorll
	 *        whether or not to embed the created {@link JBrowserPane} in a
	 *        {@link JScrollPane}.
	 * @return Either an instance of {@link JBrowserPane} that displays the HTML
	 *         file located at the given {@link URL}, or a {@link JScrollPane}
	 *         containing such a {@link JBrowserPane}.
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
	 * Creates the default {@link JMenuBar} for this {@link BaseFrame}. You can
	 * override this method to customize it more extensively for your purposes.
	 * However, there are methods such as {@link #additionalFileMenuItems()},
	 * {@link #additionalEditMenuItems()}, {@link #additionalHelpMenuItems()} and
	 * also {@link #additionalMenus()} to insert more elements to the
	 * {@link JMenuBar}. You can also switch most of the entries in the File menu
	 * off by passing false to this method (this is actually done by overriding
	 * the method {@link #loadsDefaultFileMenuEntries()}, which returns
	 * <code>true</code> by default). If you don't implement the methods
	 * {@link #getURLOnlineHelp}, {@link #getURLAboutMessage()},
	 * {@link #getURLLicense()} or {@link #getURLOnlineUpdate()}, there will be,
	 * for instance no Help menu in this element's {@link JMenuBar}. Similarly, no
	 * Edit menu will occur if there are no instances of {@link PreferencesPanel}
	 * in package <code>de.zbit.gui.prefs</code>. Hence, this method will at least
	 * create a {@link JMenuBar} containing a single menu (File) which will at
	 * least contain the entry for Exit. Everything else depends on either if all
	 * methods mentioned above return a value distinct from null or if
	 * user-defined {@link MenuItem}s or {@link JMenu}s have been declared by
	 * overriding the dedicated methods.
	 * 
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
			menuBar.add(GUITools.createJMenu(
								title == null ? "Edit" : title,
								items,
								(items != null) && (items.length > 0) && (numPrefs > 0) ? new JSeparator()
										: null, numPrefs > 0 ? preferences : null));
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
		JMenuItem help = (getURLOnlineHelp() == null) ? null : GUITools
				.createJMenuItem(EventHandler.create(ActionListener.class, this,
					"showOnlineHelp"), BaseAction.HELP_ONLINE, UIManager
						.getIcon("ICON_HELP_16"),
					KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), 'H', true);
		JMenuItem about = (getURLAboutMessage() == null) ? null : GUITools
				.createJMenuItem(EventHandler.create(ActionListener.class, this,
					"showAboutMessage"), BaseAction.HELP_ABOUT, UIManager
						.getIcon("ICON_INFO_16"),
					KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), 'I', true);
		JMenuItem license = (getURLLicense() == null) ? null : GUITools
				.createJMenuItem(EventHandler.create(ActionListener.class, this,
					"showLicense"), BaseAction.HELP_LICENSE, UIManager
						.getIcon("ICON_LICENSE_16"), KeyStroke.getKeyStroke(KeyEvent.VK_F3,
					0), 'L', true);
		JMenuItem update = ((getURLOnlineUpdate() == null)
				|| (getDottedVersionNumber() == null) || (getDottedVersionNumber()
				.length() == 0)) ? null : GUITools.createJMenuItem(EventHandler.create(
			ActionListener.class, this, "onlineUpdate"), BaseAction.HELP_UPDATE,
			UIManager.getIcon("ICON_GLOBE_16"), KeyStroke.getKeyStroke(
				KeyEvent.VK_F4, 0), 'U', true);
		title = BaseAction.nameProperties.getProperty(BaseAction.HELP);
		JMenu helpMenu = GUITools.createJMenu((title == null) ? "Help" : title,
			help, about, license, update, additionalHelpMenuItems());
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
	 * This creates the main element on the center of this {@link BaseFrame}. This
	 * can be any instance of {@link Component}. It might be helpful to add a
	 * field variable to your instance of {@link BaseFrame} to easily access and
	 * manipulate this element later on. Please note that this method is called by
	 * the constructor and therefore you should not set your main
	 * {@link Component} to null in your derived constructor. The intention of
	 * this method is that you here create some empty {@link Component} and at any
	 * later state you fill it with some content.
	 * 
	 * @return An instance of {@link Component}, which should not be null. It is
	 *         advisable to let this method initialize a field member of a derived
	 *         {@link Object}, which is manipulated at later states in the
	 *         program.
	 */
	protected abstract Component createMainComponent();
	
	/**
	 * Method that is called on exit, i.e.,when this window is closing.
	 */
	public abstract void exit();
	
	/**
	 * The name of this program.
	 * 
	 * @return
	 */
	public abstract String getApplicationName();
	
	/**
	 * This is required to automatically include a list of possible command-line
	 * options into the online help. The array of {@link KeyProvider} classes
	 * contains all those {@link KeyProvider}s whose {@link Option} entries are
	 * valid keys for the command line.
	 * 
	 * @return
	 */
	public abstract Class<? extends KeyProvider>[] getCommandLineOptions();
	
	/**
	 * The version number of this program. This must be a {@link String}
	 * containing only digits and at least one dot or at most two dots. For
	 * instance 1.2 or 1.2.3.
	 * 
	 * @return
	 */
	public abstract String getDottedVersionNumber();
	
	/**
	 * Override this message to change the texts of some or all {@link JMenuItem}s
	 * including their tool tips and also of {@link JButton}s and so on. The
	 * location given here must be a path relative to the location of this class.
	 * 
	 * @return
	 */
	protected String getLocationOfBaseActionProperties() {
		return null;
	}
	
	/**
	 * This method creates a title from the values of
	 * {@link #getApplicationName()} and {@link #getDottedVersionNumber()}. The
	 * value returned may be an empty {@link String} but never null. In case
	 * neither an application name nor a dotted version number are defined, an
	 * empty {@link String} will be returned. If both values are defined, the
	 * returned value will be application name white space version number. In case
	 * that one of the values is missing, the returned {@link String} will be
	 * shorter.
	 * 
	 * @return Creates and returns a {@link String} that combines the value
	 *         returned by {@link #getApplicationName()} and
	 *         {@link #getDottedVersionNumber()} to identify this program.
	 */
	public final String getProgramNameAndVersion() {
		String name = getApplicationName() != null ? getApplicationName() : "";
		if ((getDottedVersionNumber() == null)
				|| (getDottedVersionNumber().length() == 0)) {
			return name;
		} else {
			return name.length() == 0 ? getDottedVersionNumber() : String.format(
				"%s %s", getApplicationName(), getDottedVersionNumber());
		}
	}
	
	/**
	 * The {@link URL} where the about message for this program is located, i.e.,
	 * an HTML file containing information about the people in charge for this
	 * program.
	 * 
	 * @return
	 */
	public abstract URL getURLAboutMessage();
	
	/**
	 * The {@link URL} of the license file under which this application is
	 * distributed.
	 * 
	 * @return
	 */
	public abstract URL getURLLicense();
	
	/**
	 * The {@link URL} of the online help file.
	 * 
	 * @return
	 */
	public abstract URL getURLOnlineHelp();
	
	/**
	 * <p>
	 * The online update expects to find a file called <code>latest.txt</code>
	 * containing only the version number of the latest release of this program as
	 * a {@link String} of digits that contains exactly one dot or at most two
	 * dots. Furthermore, on the given destination must be a second file, called
	 * <code>releaseNotes&lt;VersionNumber&gt;.htm[l]</code> which contains more
	 * detailed information about the latest release.
	 * </p>
	 * <p>
	 * Summarizing, the web address or other directory address where we can find
	 * at least the following two files:
	 * <ul>
	 * <li>latest.txt</li>
	 * <li>releaseNotesX.Y.Z.htm</li>
	 * </ul>
	 * The file <code>latest.txt</code> contains exactly the dotted version number
	 * of the latest release of this software; nothing else! The release notes
	 * file contains HTML code describing the latest changes and the file name
	 * MUST end with the latest version number of the release.
	 * </p>
	 * 
	 * @return The {@link URL} to some directory where to look for the online update.
	 */
	public abstract URL getURLOnlineUpdate();
	
	/**
	 * Initializes this graphical user interface.
	 */
	protected void init() {
		if ((getTitle() == null) || (getTitle().length() == 0)) {
			setTitle(getProgramNameAndVersion());
		}
		GUITools.initLaF(getTitle());
		setDefaultLookAndFeelDecorated(true);
		try {
			Properties defaults = new Properties();
			if (System.getProperty("user.language").equals(
				Locale.GERMAN.getLanguage())) {
				defaults.loadFromXML(BaseFrame.class
						.getResourceAsStream("BaseActionGerman.xml"));
			} else {
				defaults.loadFromXML(BaseFrame.class
						.getResourceAsStream("BaseActionEnglish.xml"));
			}
			BaseAction.nameProperties.setDefaults(defaults);
			String location = getLocationOfBaseActionProperties();
			if (location != null) {
				Properties properties = new Properties();
				try {
					properties.loadFromXML(BaseFrame.class.getResourceAsStream(location));
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
	 * Starts a search for an online update and shows error messages if no update
	 * can be performed for some reason.
	 */
	public void onlineUpdate() {
		onlineUpdate(false);
	}
	
	/**
	 * This actually performs the online update, i.e., this method looks if an
	 * update is available and shows a message if this is the case. If the option
	 * hideErrorMessages is set to <code>false</code>, a message will also be
	 * shown if no update can be found or if an error occurs when searching for
	 * the update. Otherwise, there will be no such method in case of failure.
	 * 
	 * @param hideErrorMessages
	 *        if <code>true</code> no messages will be displayed to the user in
	 *        case of an unsuccessful search for an update.
	 */
	private void onlineUpdate(boolean hideErrorMessages) {
		try {
			GUITools
					.setEnabled(false, getJMenuBar(), toolBar, BaseAction.HELP_UPDATE);
			UpdateMessage update = new UpdateMessage(getApplicationName(),
				getURLOnlineUpdate());
			update.addWindowListener(EventHandler.create(WindowListener.class, this,
				"setOnlineUpdateEnabled", null, "windowClosed"));
			if (!update.checkForUpdate(true, getDottedVersionNumber())
					&& !hideErrorMessages) {
				GUITools.showMessage(String.format(
										"You are using the latest version %s of %s. No newer version could be found.",
										getDottedVersionNumber(), getApplicationName()),
							"No update available");
			}
		} catch (IOException exc) {
			if (!hideErrorMessages) {
				GUITools.showErrorMessage(this, exc);
			}
		}
	}
	
	/**
	 * Opens some {@link File}.
	 * 
	 * @return
	 */
	public abstract void openFile();
	
	/**
	 * Displays the configuration for the {@link PreferencesDialog}.
	 */
	public void preferences() {
		PreferencesDialog.showPreferencesDialog();
	}
	
	/**
	 * Saves some {@link File}.
	 * 
	 */
	public abstract void saveFile();
	
	/**
	 * Enables the {@link JButton} in the {@link JToolBar} (if there is any) and
	 * the entry in the {@link JMenu} that allows the user to search for an online
	 * update.
	 */
	public final void setOnlineUpdateEnabled() {
		GUITools.setEnabled(true, getJMenuBar(), toolBar, BaseAction.HELP_UPDATE);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Window#setVisible(boolean)
	 */
	public void setVisible(boolean b) {
		super.setVisible(b);
		if (!UPDATE_CHECKED) {
			new Thread(new Runnable() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see java.lang.Runnable#run()
				 */
				public void run() {
					SBPreferences prefs = SBPreferences
							.getPreferencesFor(GUIOptions.class);
					if (prefs.getBoolean(GUIOptions.CHECK_FOR_UPDATES)) {
						onlineUpdate(true);
						UPDATE_CHECKED = true;
					}
				}
			}).start();
		}
	}
	
	/**
	 * Shows the about message, i.e., information about the authors of this
	 * program in a {@link JOptionPane} of size 380x220.
	 */
	public final void showAboutMessage() {
		JOptionPane.showMessageDialog(this, createJBrowser(getURLAboutMessage(),
			380, 220, false), String.format("About %s", getProgramNameAndVersion()),
			JOptionPane.INFORMATION_MESSAGE);
	}
	
	/**
	 * Displays the license under which this program is distributed in a
	 * {@link JOptionPane} of size 640x480.
	 */
	public final void showLicense() {
		JOptionPane.showMessageDialog(this, createJBrowser(getURLLicense(), 640,
			480, true), String.format("%s - License", getProgramNameAndVersion()),
			JOptionPane.INFORMATION_MESSAGE, UIManager.getIcon("ICON_LICENSE_64"));
	}
	
	/**
	 * Displays the online help in a {@link JHelpBrowser}.
	 */
	public final void showOnlineHelp() {
		GUITools.setEnabled(false, getJMenuBar(), toolBar, BaseAction.HELP_ONLINE);
		JHelpBrowser.showOnlineHelp(this, EventHandler.create(WindowListener.class,
			this, "activateOnlineHelpCommand", null, "windowClosed"), String.format(
			"%s - Online Help", getProgramNameAndVersion()), getURLOnlineHelp(),
			getCommandLineOptions());
	}
	
}
