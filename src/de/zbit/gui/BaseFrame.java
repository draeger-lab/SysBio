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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;
import java.awt.MenuItem;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowListener;
import java.beans.EventHandler;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.prefs.BackingStoreException;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import de.zbit.gui.prefs.FileHistory;
import de.zbit.gui.prefs.MultiplePreferencesPanel;
import de.zbit.gui.prefs.PreferencesDialog;
import de.zbit.gui.prefs.PreferencesPanel;
import de.zbit.util.ResourceManager;
import de.zbit.util.StringUtil;
import de.zbit.util.prefs.KeyProvider;
import de.zbit.util.prefs.Option;
import de.zbit.util.prefs.SBPreferences;
import de.zbit.util.prefs.SBProperties;

/**
 * This class provides a basic and easily extendible implementation of a
 * graphical user interface, which already contains some actions and nice
 * features. In this way, this class can serve as a multi-purpose element for
 * any user interface.
 * 
 * @author Andreas Dr&auml;ger
 * @date 2010-11-28
 * @version $Rev$
 * @since 1.0
 */
public abstract class BaseFrame extends JFrame {
	
	/**
	 * This {@link Enum} contains very basic actions of a graphical user interface.
	 * 
	 * @author Andreas Dr&auml;ger
	 * @author Finja B&uuml;chel
	 * @date 2010-11-12
	 */
	public static enum BaseAction implements ActionCommand {
		/**
		 * The {@link ActionCommand} for the Edit menu.
		 */
		EDIT,
		/**
		 * {@link BaseAction} to configure the user's preferences in a dialog
		 * window.
		 */
		EDIT_PREFERENCES,
		/**
		 * The {@link ActionCommand} for the File menu.
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
		 * {@link BaseAction} to access the history of previously opened files.
		 */
		FILE_OPEN_RECENT,
		/**
		 * {@link BaseAction} to saves the currently opened file or the result of a
		 * computation in one of the available formats.
		 */
		FILE_SAVE,
		/**
		 * The {@link ActionCommand} for the help menu. 
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
				name = name.trim();
				if (name.length() > 0) {
					return name;
				}
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
				toolTip = toolTip.trim();
				if (toolTip.length() > 0) {
					return toolTip;
				}
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
	 * @return An array of {@link JMenuItem}s that are added to the Edit menu
	 *         above of the preferences entry if it exists. This method may return
	 *         null.
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
	 * @return An array of {@link JMenuItem}s that are added to the File menu
	 *         above of the exit entry. This method may return null.
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
	 * @return An array of {@link JMenuItem}s that are added to the Help menu.
	 *         This method may return null.
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
	 * @return An array of {@link JMenu}s that are to be placed in this
	 *         {@link BaseFrame}'s {@link JMenuBar} left of the Help menu if it
	 *         exists. This method may return null.
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
	 * @return a {@link JToolBar} that contains one {@link JButton} for each
	 *         {@link JMenuItem} in this {@link BaseFrame}'s {@link JMenuBar} that
	 *         contains an {@link Icon} and groups all these buttons according to
	 *         the {@link JMenu} they belong to. Each one of these {@link JButton}s
	 *         refers to the identical {@link ActionListener} and has the identical
	 *         action command as the corresponding {@link JMenuItem}.
	 */
	public JToolBar createDefaultToolBar() {
		JMenu menu;
		JMenuItem item;
		Object action = null;
		ResourceBundle resources = ResourceManager.getBundle(GUITools.RESOURCE_LOCATION_FOR_LABELS);
		JToolBar toolBar = new JToolBar(resources.getString("DEFAULT_TOOL_BAR_TITLE"));
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
	 * <p>
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
	 * </p>
	 * <p>
	 * All predefined {@link JMenu}s and {@link JMenuItem}s in this
	 * {@link JMenuBar} are also equipped with an action command {@link String},
	 * which is taken from {@link BaseAction#toString()}. This allows to query for
	 * desired {@link JMenuItem}s or {@link JMenu}s using the method
	 * {@link GUITools#getJMenuItem(JMenuBar, Object)}. This means, it is not
	 * necessary to memorize any {@link JMenu}s or {@link JMenuItem} instances as
	 * field variables.
	 * </p>
	 * <p>
	 * The texts, i.e., labels, for all {@link JMenuItem}s in this {@link JMenu}
	 * are taken from a configuration file depending on the language properties of
	 * the {@link System}.
	 * </p>
	 * 
	 * @param loadDefaultFileMenuEntries
	 *        Switch to decide weather or not to load the default entries in the
	 *        File menu for Open, Save, and Close.
	 * @return A {@link JMenuBar} that already contains some predefined menus.
	 * @see #getLocationOfBaseActionProperties()
	 * @see GUITools#getJMenuItem(JMenuBar, Object)
	 */
	protected JMenuBar createJMenuBar(boolean loadDefaultFileMenuEntries) {
		JMenuBar menuBar = new JMenuBar();
		String title;
		
		/*
		 * File menu
		 */
		JMenuItem openFile = null, saveFile = null, closeFile = null;
		if (loadDefaultFileMenuEntries) {
			openFile = GUITools.createJMenuItem(EventHandler.create(
				ActionListener.class, this, "openFileAndLogHistory"),
				BaseAction.FILE_OPEN, UIManager.getIcon("ICON_OPEN_16"), KeyStroke
						.getKeyStroke('O', InputEvent.CTRL_DOWN_MASK), 'O', true);
			saveFile = GUITools.createJMenuItem(EventHandler.create(
				ActionListener.class, this, "saveFile"), BaseAction.FILE_SAVE,
				UIManager.getIcon("ICON_SAVE_16"), KeyStroke.getKeyStroke('S',
					InputEvent.CTRL_DOWN_MASK), 'S', false);
			closeFile = GUITools.createJMenuItem(EventHandler.create(
				ActionListener.class, this, "closeFile"), BaseAction.FILE_CLOSE,
				UIManager.getIcon("ICON_TRASH_16"), KeyStroke.getKeyStroke('W',
					InputEvent.CTRL_DOWN_MASK), 'W', false);
		}
		JMenuItem exit = GUITools.createJMenuItem(EventHandler.create(
			ActionListener.class, this, "exit"), BaseAction.FILE_EXIT, UIManager
				.getIcon("ICON_EXIT_16"), KeyStroke.getKeyStroke(KeyEvent.VK_F4,
			InputEvent.ALT_DOWN_MASK));
		JMenuItem items[] = additionalFileMenuItems();
		boolean addSeparator = (openFile != null) || (saveFile != null)
				|| ((items != null) && (items.length > 0)) || (closeFile != null);
		JMenu fileHistory = null;
		if (getMaximalFileHistorySize() > 0) {
			fileHistory = new JMenu(BaseAction.FILE_OPEN_RECENT.getName());
			fileHistory.setActionCommand(BaseAction.FILE_OPEN_RECENT.toString());
			fileHistory.setEnabled(false);
			String tooltip = BaseAction.FILE_OPEN_RECENT.getToolTip();
			if (tooltip != null) {
				fileHistory.setToolTipText(StringUtil.toHTML(tooltip,
					GUITools.TOOLTIP_LINE_LENGTH, false));
			}
			SBPreferences history = SBPreferences
					.getPreferencesFor(getFileHistoryKeyProvider());
			String fileList = (history != null) ? history
					.get(FileHistory.LAST_OPENED) : null;
			updateFileHistory(fileHistory, FileHistory.Tools.parseList(fileList));
		}
		title = BaseAction.FILE.getName();
		JMenu fileMenu = GUITools.createJMenu(title == null ? "File" : title,
				BaseAction.FILE.getToolTip(), openFile, fileHistory, saveFile, items,
				closeFile, addSeparator ? new JSeparator() : null, exit);
		fileMenu.setActionCommand(BaseAction.FILE.toString());
		menuBar.add(fileMenu);

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
			title = BaseAction.EDIT.getName();
			JMenu editMenu = GUITools.createJMenu(
				title == null ? "Edit" : title, BaseAction.EDIT.getToolTip(), items,
						(items != null) && (items.length > 0) && (numPrefs > 0) ? new JSeparator()
								: null, numPrefs > 0 ? preferences : null);
			editMenu.setActionCommand(BaseAction.EDIT.toString());
			menuBar.add(editMenu);
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
		title = BaseAction.HELP.getName();
		JMenu helpMenu = GUITools.createJMenu((title == null) ? "Help" : title,
			BaseAction.HELP.getToolTip(), help, about, license, update,
			additionalHelpMenuItems());
		if (helpMenu.getItemCount() > 0) {
			helpMenu.setActionCommand(BaseAction.HELP.toString());
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
	 * @return an instance of {@link JToolBar} or null if no tool bar is desired.
	 * @see #createDefaultToolBar()
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
	 * Method that is called on exit, i.e., when this {@link BaseFrame}
	 * {@link Window} is closing.
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
	 * instance "1.2" or "1.2.3".
	 * 
	 * @return
	 */
	public abstract String getDottedVersionNumber();
	
	/**
	 * Returns the {@link KeyProvider} that provides an {@link Option} to
	 * persistently store the history of previously opened files.
	 * 
	 * @return By default, this method returns an instance of {@link Class} for
	 *         the {@link FileHistory} interface. To avoid that multiple
	 *         applications that implement this {@link BaseFrame} override each
	 *         other's file history, implementing classes should override this
	 *         method and return a class file corresponding to something that is
	 *         derived from {@link FileHistory} and that is located in a different
	 *         package.
	 */
	public Class<? extends FileHistory> getFileHistoryKeyProvider() {
		return FileHistory.class;
	}
	
	/**
	 * Override this message to change the texts of some or all {@link JMenuItem}s
	 * including their tool tips and also of {@link JButton}s and so on. The
	 * location given here must be a path that includes the package name, for
	 * instance <code>de.zbit.locales.MyTexts</code>, where the file extension
	 * <code>xml</code> is omitted, but the resource file is required to be an XML
	 * file in the format defined for {@link Properties}. The naming convention
	 * for the resource file says that it should contain a suffix consisting of
	 * underscore, language name, underscore, country name, e.g.,
	 * <code>MyTexts_de_DE.xml</code>. You may omit this suffix.
	 * 
	 * @return By default, this method returns null, i.e., the default texts will
	 *         be set for all menu items depending on the {@link System}'s default
	 *         {@link Locale}.
	 */
	protected String getLocationOfBaseActionProperties() {
		return null;
	}
	
	/**
	 * Gives the number of files to be memorized and displayed in the file
	 * history. This will be made persistent using an instance of
	 * {@link FileHistory}.
	 * 
	 * @return If this method returns a value between 1 and 10, every item in the
	 *         file history will also be accessible with a {@link KeyStroke} by
	 *         pushing {@link InputEvent#ALT_DOWN_MASK} and a number from 0
	 *         through 9. For larger values no such combination will be created
	 *         for any item. By returning 0 or less you can switch the file
	 *         history function completely off.
	 */
	public abstract short getMaximalFileHistorySize();
	
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
		
		// Set this as property for static classes.
		System.setProperty("app.name", getApplicationName());
		
		GUITools.initLaF(getTitle());
		setDefaultLookAndFeelDecorated(true);
		
		try {
			BaseAction.nameProperties.setDefaults(ResourceManager
					.getBundle("de.zbit.locales.BaseAction"));
			String location = getLocationOfBaseActionProperties();
			if (location != null) {
				try {
					BaseAction.nameProperties.putAll(ResourceManager.getBundle(location));
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
		
		// If the user whishes a OPEN FILE entry, also create openFile by Drag'n Drop.
		if (loadsDefaultFileMenuEntries()) {
		  createDragNDropFunctionality();
		}
		
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
   * Adds a drag'n drop functionality to this panel. This should
   * be called, whenever the user decides to have a "File Open"
   * option on his menu bar.
   * It uses the {@link #openFileAndLogHistory(File...)} method
   * to open the file(s).
   */
  private void createDragNDropFunctionality() {
    // Make this panel responsive to drag'n drop events.
    FileDropHandler dragNdrop = new FileDropHandler(
      new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          openFileAndLogHistory((File) event.getSource());
        }
      }
    );
    
    this.setTransferHandler(dragNdrop);
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
		GUITools.setEnabled(false, getJMenuBar(), toolBar, BaseAction.HELP_UPDATE);
		UpdateMessage update = new UpdateMessage(true, getApplicationName(),
			getURLOnlineUpdate(), getDottedVersionNumber(), hideErrorMessages);
		update.addWindowListener(EventHandler.create(WindowListener.class, this,
			"setOnlineUpdateEnabled", null, "windowClosed"));
		update.addPropertyChangeListener(EventHandler.create(
			PropertyChangeListener.class, this, "setOnlineUpdateEnabled",
			"getPropertyName"));
		update.execute();
	}
	
	/**
	 * <p>
	 * This method is linked to the {@link BaseAction#FILE_OPEN} and will be
	 * called when ever any element associated with this action command is
	 * activated. The intention of this method is that the user can either choose
	 * which file to open or that one or multiple instances of {@link File} are
	 * passed to this method. In both cases this method knows what to do with
	 * these {@link File}s or their content. For instance, it might be necessary
	 * to change the content of this {@link BaseFrame}'s main component according
	 * to the content of the selected or given {@link File}(s).
	 * </p>
	 * <p>
	 * However, this method will not be called directly in this case, it is rather
	 * wrapped in {@link #openFileAndLogHistory(File...)}. This method tries to
	 * memorize all opened files to make them accessible to the user more easily
	 * in the {@link JMenuBar} of this {@link BaseFrame}. Therefore, it is
	 * necessary that this method returns all {@link File} instances that are
	 * selected by the user when executing this method.
	 * </p>
	 * <p>
	 * In case that one or multiple instances of {@link File} are passed to this
	 * method, here is the correct location to decide whether these {@link File}
	 * objects can be processed. Maybe a warning must be displayed to the user or
	 * maybe invalid input {@link File}s are simply to be ignored.
	 * </p>
	 * 
	 * @return An array of all those {@link File} instances that are either
	 *         selected by the user or the left over from the given arguments,
	 *         i.e., the subset of valid input files from the given {@link File}
	 *         arguments.
	 * @see #createMainComponent()
	 * @see #openFileAndLogHistory(File...)
	 */
	protected abstract File[] openFile(File... files);
	
	/**
	 * This method wraps {@link #openFileAndLogHistory(File...)} and simply
	 * provides a way to call this other method without the necessity to pass any
	 * arguments to it.
	 * 
	 * @return An array of {@link File} objects that have been selected by the
	 *         user or null if the user cancels the operation.
	 * @see #openFileAndLogHistory(File...)
	 * @see #openFile(File...)
	 */
	public final File[] openFileAndLogHistory() {
		return openFileAndLogHistory(new File[0]);
	}
	
	/**
	 * This method passes the given files to the method {@link #openFile(File...)}
	 * and then memorizes the files returned by this method in the file history,
	 * which is then made accessible to the user via the {@link JMenuBar}. It also
	 * tries to store the open directory for the next access. This is only
	 * possible if all files returned by {@link #openFile(File...)} have the same
	 * parent directory. In case that an error occurs while making the new base
	 * directory persistent, a dialog will be displayed to the user that shows the
	 * precise error message.
	 * 
	 * @param files
	 *        any number of files that should be passed to the method
	 *        {@link #openFile(File...)}.
	 * @return the files that originate from the method {@link #openFile(File...)}
	 *         .
	 */
	protected final File[] openFileAndLogHistory(File... files) {
		files = openFile(files);
		// Remember the baseDir and put files into history.
    if ((files != null) && (files.length > 0)) {
      List<File> fileList = new LinkedList<File>();
      // Process all those files that have just been opened.
      String baseDir = null;
      boolean sameBaseDir = true;
      for (File file : files) {
        if (file.exists() && file.canRead() && !fileList.contains(file)) {
          if (baseDir == null) {
            baseDir = file.getParent();
          } else if (!baseDir.equals(file.getParent())) {
            sameBaseDir = false;
          }
          fileList.add(file);
        }
      }
      // Memorize the default open directory.
      if (sameBaseDir && (baseDir != null)) {
        SBPreferences prefs = SBPreferences.getPreferencesFor(GUIOptions.class);
        prefs.put(GUIOptions.OPEN_DIR, baseDir);
        try {
          prefs.flush();
        } catch (BackingStoreException exc) {
          GUITools.showErrorMessage(this, exc);
        }
      }
      if (getMaximalFileHistorySize() > 0) {
        // Create the list of files to update the file history in the menu.
        // In addition to the files that have just been opened (above), we
        // also have to consider older files.
        File file;
        JMenu fileHistory = (JMenu) GUITools.getJMenuItem(getJMenuBar(),
          BaseAction.FILE_OPEN_RECENT);
        for (int i = 0; i < fileHistory.getItemCount(); i++) {
          file = new File(fileHistory.getItem(i).getToolTipText());
          if (file.exists() && file.canRead() && !fileList.contains(file)) {
            fileList.add(file);
          }
        }
        updateFileHistory(fileHistory, fileList);
      }
    }
		return files;
	}
	
	/**
	 * Displays the configuration for the {@link PreferencesDialog}.
	 */
	public void preferences() {
		PreferencesDialog.showPreferencesDialog();
	}

	/**
	 * Saves some results or the current work in some {@link File}.
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
	
	/**
	 * Enables the {@link JButton} in the {@link JToolBar} (if there is any) and
	 * the entry in the {@link JMenu} that allows the user to search for an online
	 * update if the given {@link String} equals <code>onlineUpdateExecuted</code>
	 * .
	 * 
	 * @param command
	 *        If this command equals <code>onlineUpdateExecuted</code> the online
	 *        update button will be enabled.
	 */
	public final void setOnlineUpdateEnabled(String command) {
		if (command.equals("onlineUpdateExecuted")) {
			setOnlineUpdateEnabled();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Window#setVisible(boolean)
	 */
	public void setVisible(boolean b) {
		if (!UPDATE_CHECKED) {
			SBPreferences prefs = SBPreferences.getPreferencesFor(GUIOptions.class);
			if (prefs.getBoolean(GUIOptions.CHECK_FOR_UPDATES)) {
				onlineUpdate(true);
				UPDATE_CHECKED = true;
			}
		}
		super.setVisible(b);
	}
	
	/**
	 * Shows the about message, i.e., information about the authors of this
	 * program in a {@link JOptionPane} of size 380x220.
	 */
	public final void showAboutMessage() {
		ResourceBundle resources = ResourceManager
				.getBundle(GUITools.RESOURCE_LOCATION_FOR_LABELS);
		JPanel panel = new JPanel(new BorderLayout());
		JLabel label = new JLabel();
		label.setIcon(UIManager.getIcon("UT_WBMW_mathnat_4C_380x45"));
		Dimension dimension = label.getPreferredSize();
		dimension.setSize(dimension.getWidth(), dimension.getHeight() + 10);
		label.setPreferredSize(dimension);
		panel.add(label, BorderLayout.NORTH);
		panel.add(createJBrowser(getURLAboutMessage(), 380, 220, true),
			BorderLayout.CENTER);
		JOptionPane.showMessageDialog(this, panel, String.format(resources
				.getString("ABOUT_THE_PROGRAM"), getProgramNameAndVersion()),
			JOptionPane.INFORMATION_MESSAGE);
	}
	
	/**
	 * Displays the license under which this program is distributed in a
	 * {@link JOptionPane} of size 640x480.
	 */
	public final void showLicense() {
		ResourceBundle resources = ResourceManager
				.getBundle(GUITools.RESOURCE_LOCATION_FOR_LABELS);
		JOptionPane.showMessageDialog(this, createJBrowser(getURLLicense(), 640,
			480, true), String.format(resources.getString("LICENSE_OF_THE_PROGRAM"),
			getProgramNameAndVersion()), JOptionPane.INFORMATION_MESSAGE, UIManager
				.getIcon("ICON_LICENSE_64"));
	}
	
	/**
	 * Displays the online help in a {@link JHelpBrowser}.
	 */
	public final void showOnlineHelp() {
		GUITools.setEnabled(false, getJMenuBar(), toolBar, BaseAction.HELP_ONLINE);
		ResourceBundle resources = ResourceManager
				.getBundle(GUITools.RESOURCE_LOCATION_FOR_LABELS);
		JHelpBrowser.showOnlineHelp(this, EventHandler.create(WindowListener.class,
			this, "activateOnlineHelpCommand", null, "windowClosed"), String.format(
			resources.getString("ONLINE_HELP_FOR_THE_PROGRAM"),
			getProgramNameAndVersion()), getURLOnlineHelp(), getCommandLineOptions());
	}
	
	/**
	 * Updates the list of previously opened files in the {@link JMenuBar}. This
	 * method is private because it relies on the correctness of the given
	 * {@link JMenu} and also the given {@link List} of {@link File}s. This method
	 * also makes the file history persistent. If this fails, a dialog with a
	 * precise error message will be displayed to the user.
	 * 
	 * @param fileHistory
	 *        The {@link JMenu}, whose items will be removed and replaced by
	 *        references to the files given in the other argument. If the item
	 *        count of this {@link JMenu} is zero at the end of this method, it
	 *        will be disabled.
	 * @param listOfFiles
	 *        A {@link List} of those {@link File}s that have been opened
	 *        previously.
	 */
	private final void updateFileHistory(JMenu fileHistory, List<File> listOfFiles) {
		fileHistory.removeAll();
		JMenuItem fileItem;
		List<File> keepFiles = new LinkedList<File>();
		short maximum = getMaximalFileHistorySize();
		for (int i = 0; i < Math.min(listOfFiles.size(), maximum); i++) {
			final File file = listOfFiles.get(i);
			if (file.exists() && file.canRead()) {
				fileItem = new JMenuItem(file.getName());
				fileItem.setToolTipText(file.getAbsolutePath());
				if (maximum <= 10) {
					fileItem.setAccelerator(KeyStroke.getKeyStroke(String.valueOf(
						(i + 1 < 10) ? i + 1 : 0).charAt(0), InputEvent.ALT_DOWN_MASK));
				}
				fileItem.addActionListener(new ActionListener() {
					/*
					 * (non-Javadoc)
					 * 
					 * @seejava.awt.event.ActionListener#actionPerformed(java.awt.event.
					 * ActionEvent)
					 */
					public void actionPerformed(ActionEvent e) {
						openFileAndLogHistory(file);
					}
				});
				fileHistory.add(fileItem);
				keepFiles.add(file);
			}
		}
		fileHistory.setEnabled(fileHistory.getItemCount() > 0);
		// This removes files that cannot be read from the history.
		SBPreferences history = SBPreferences
				.getPreferencesFor(getFileHistoryKeyProvider());
		history.put(FileHistory.LAST_OPENED, FileHistory.Tools.toString(keepFiles));
		try {
			history.flush();
		} catch (BackingStoreException exc) {
			GUITools.showErrorMessage(this, exc);
		}
	}
	
}
