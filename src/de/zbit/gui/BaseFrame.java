/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2013 by the University of Tuebingen, Germany.
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
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;
import java.awt.MenuItem;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowListener;
import java.beans.EventHandler;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URL;
import java.text.ChoiceFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.PreferenceChangeListener;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import de.zbit.AppConf;
import de.zbit.UserInterface;
import de.zbit.gui.actioncommand.ActionCommand;
import de.zbit.gui.actioncommand.ActionCommandWithIcon;
import de.zbit.gui.mac.MacOSXController;
import de.zbit.gui.mac.MacOSXController2;
import de.zbit.gui.prefs.FileHistory;
import de.zbit.gui.prefs.MultiplePreferencesPanel;
import de.zbit.gui.prefs.PreferencesDialog;
import de.zbit.gui.prefs.PreferencesPanel;
import de.zbit.io.OpenedFile;
import de.zbit.util.ArrayUtils;
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
 * @author Clemens Wrzodek
 * @date 2010-11-28
 * @version $Rev$
 * @since 1.0
 */
public abstract class BaseFrame extends JFrame implements FileHistory,
    GUIOptions, UserInterface {
	
	/**
	 * This {@link Enum} contains very basic actions of a graphical user interface.
	 * 
	 * @author Andreas Dr&auml;ger
	 * @author Finja B&uuml;chel
	 * @date 2010-11-12
	 */
	public static enum BaseAction implements ActionCommandWithIcon {
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
		 * Saves the current changes in the file that has been opened before by the
		 * user. Note that this action means that the user cannot select any different
		 * file format or file location. The program might just warn the user if the
		 * existing file should be really overwritten.
		 */
		FILE_SAVE,
		/**
		 * {@link BaseAction} to saves the currently opened file or the result of a
		 * computation in one of the available formats.
		 */
		FILE_SAVE_AS,
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
		
		/* (non-Javadoc)
		 * @see de.zbit.gui.ActionCommandWithIcon#getIcon()
		 */
		public Icon getIcon() {
			switch (this) {
				case EDIT_PREFERENCES:
					return UIManager.getIcon("ICON_PREFS_16");
				case FILE_OPEN:
					return UIManager.getIcon("ICON_OPEN_16");
				case FILE_SAVE:
					return UIManager.getIcon("ICON_SAVE_16");
				case FILE_SAVE_AS:
					return UIManager.getIcon("ICON_SAVE_16");
				case FILE_CLOSE:
					return UIManager.getIcon("ICON_TRASH_16");
				case FILE_EXIT:
					return UIManager.getIcon("ICON_EXIT_16");
				case HELP_ONLINE:
					return UIManager.getIcon("ICON_HELP_16");
				case HELP_LICENSE:
					return UIManager.getIcon("ICON_LICENSE_16");
				case HELP_UPDATE:
					return UIManager.getIcon("ICON_GLOBE_16");
				case HELP_ABOUT:
					return UIManager.getIcon("ICON_INFO_16");
				default:
					break;
			}
			return null;
		}
		
		/* (non-Javadoc)
		 * @see de.zbit.gui.ActionCommandWithIcon#getName()
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
		
		/* (non-Javadoc)
		 * @see de.zbit.gui.ActionCommandWithIcon#getToolTip()
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
	 * A {@link Logger} for this class.
	 */
	private static final transient Logger logger = Logger.getLogger(BaseFrame.class.getName());
	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = -6533854985804740883L;
	/**
	 * Switch to avoid checking for updates multiple times.
	 */
	private static boolean UPDATE_CHECKED = false;
	
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
	public final static JComponent createJBrowser(URL url, int preferedWidth,
		int preferedHeight, boolean scroll) {
		JBrowserPane browser = new JBrowserPane(url);
		browser.removeHyperlinkListener(browser);
		browser.addHyperlinkListener(new SystemBrowser());
		browser.setPreferredSize(new Dimension(preferedWidth, preferedHeight));
		if (scroll) { 
		  return new JScrollPane(browser,
			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
			JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED); 
		}
		browser.setBorder(BorderFactory.createLoweredBevelBorder());
		return browser;
	}
	
  /**
	 * The configuration of this program.
	 */
  protected AppConf appConf;
	/**
	 * Allows implementing classes to register listeners for changes in
	 * preferences. These listeners are notified in case that the user changes
	 * some preferences within the preferences dialog.
	 */
	protected List<PreferenceChangeListener> listOfPrefChangeListeners;
	
	/**
   * A status bar...
   */
	protected StatusBar statusBar;
	
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
	  this((AppConf) null);
	}
	
	/**
	 * 
	 * @param appConf
	 */
  public BaseFrame(AppConf appConf) {
    super();
    this.appConf = appConf;
    init();
  }
	
	
	/**
	 * Creates a new {@link BaseFrame} with the given
	 * {@link GraphicsConfiguration}.
	 * 
	 * @param title
	 * @param gc
	 */
	public BaseFrame(AppConf appConf, GraphicsConfiguration gc) {
		super(gc);
		this.appConf = appConf;
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
	 *         {@code null}.
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
	 *         above of the exit entry. This method may return {@code null}.
	 */
	protected JMenuItem[] additionalFileMenuItems() {
		// Intentionally left blank
		return null;
	}
	
	/**
	 * Override this method to add additional {@link JMenuItem}s to the help menu.
	 * These will be placed under the {@link JMenuItem} for the online update (if
	 * this one exists, otherwise in a higher position).
	 * 
	 * @return An array of {@link JMenuItem}s that are added to the Help menu.
	 *         This method may return {@code null}.
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
	 *         exists. This method may return {@code null}.
	 */
	protected JMenu[] additionalMenus() {
		// empty method
		return null;
	}
	
	/**
	 * Override this method to create additional menu items for the 'view' menu.
	 * @return
	 */
	protected JMenuItem[] additionalViewMenuItems() {
		return null;
	}
	
	/**
	 * Adds a listener to this {@link BaseFrame} that is notified in case that the
	 * user alters some {@link SBPreferences} within the {@link PreferencesDialog}.
	 * 
	 * @param pcl
	 * @return {@code true} (as specified by {@link Collection#add}).
	 * @throws ClassCastException
	 *         if the class of the specified element prevents it from being added
	 *         to this list
	 * @throws NullPointerException
	 *         if the specified element is {@code null} and this list does not permit {@code null}
	 *         elements
	 * @throws IllegalArgumentException
	 *         if some property of this element prevents it from being added to
	 *         this list
	 */
	public boolean addPreferenceChangeListener(PreferenceChangeListener pcl)
		throws ClassCastException, NullPointerException, IllegalArgumentException {
		return listOfPrefChangeListeners.add(pcl);
	}
	
  /**
	 * 
	 * @param fileList
	 * @param fileHistory
	 */
	protected void addToFileHistory(Collection<File> fileList, JMenu... fileHistory) {
    if (getMaximalFileHistorySize() > 0) {
      // Create the list of files to update the file history in the menu.
      // In addition to the files that have just been opened (above), we
      // also have to consider older files.
      
      // Ensure that the current FileHistory is in the list.
      JMenu c_fileHistory = (JMenu) GUITools.getJMenuItem(getJMenuBar(),
        BaseAction.FILE_OPEN_RECENT);
      if (!ArrayUtils.contains(fileHistory, c_fileHistory)) {
        fileHistory = ArrayUtils.removeNull(fileHistory);
        if (fileHistory == null) {
          fileHistory = new JMenu[] {c_fileHistory};
        } else {
          JMenu[] histories = new JMenu[fileHistory.length + 1];
          histories[0] = c_fileHistory;
          System.arraycopy(fileHistory, 0, histories, 1, fileHistory.length);
          fileHistory = histories;
        }
      }
      
      // Add previous files to history
      File file;
      for (int i = 0; i < c_fileHistory.getItemCount(); i++) {
        file = new File(c_fileHistory.getItem(i).getToolTipText());
        if (file.exists() && file.canRead() && !fileList.contains(file)) {
          try {
            fileList.add(file);
          } catch (UnsupportedOperationException e) {
            // Unmodifiable list as input => Convert to modifiable list.
            List<File> copy = new ArrayList<File>(fileList);
            copy.add(file);
            fileList = copy;
          }
        }
      }
      // update
      updateFileHistory(fileList, fileHistory);
    }
	}
	
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
		boolean isOSX = GUITools.isMacOSX();
		JMenu menu;
		JButton button;
		Object action = null;
		ResourceBundle resources = ResourceManager.getBundle(StringUtil.RESOURCE_LOCATION_FOR_LABELS);
		JToolBar toolBar = new JToolBar(resources.getString("DEFAULT_TOOL_BAR_TITLE"));
		toolBar.setOpaque(true);
		if (isOSX
				&& (((appConf != null) && (appConf.getInteractiveOptions() != null) && 
						(appConf.getInteractiveOptions().length > 0)) || 
						(MultiplePreferencesPanel.getPossibleTabCount() > 0))) {
			button = GUITools.createButton(BaseAction.EDIT_PREFERENCES.getIcon(),
				EventHandler.create(ActionListener.class, this, "preferences"), action,
				BaseAction.EDIT_PREFERENCES.getToolTip());
			button.setOpaque(true);
			button.setBorderPainted(false);
			toolBar.add(button);
			toolBar.add(new JToolBar.Separator());
		}
		for (int i = 0; i < getJMenuBar().getMenuCount(); i++) {
			menu = getJMenuBar().getMenu(i);
			int buttonCount = collectMenuItems(menu, toolBar, isOSX);
			if ((i < getJMenuBar().getMenuCount() - 1) && (buttonCount > 0)) {
				toolBar.add(new JToolBar.Separator());
			}
		}
		if (isOSX) {
			button = GUITools.createButton(BaseAction.HELP_ABOUT.getIcon(),
				EventHandler.create(ActionListener.class, this, "showAboutMessage"),
				action, BaseAction.HELP_ABOUT.getToolTip());
			button.setOpaque(true);
			button.setBorderPainted(false);
			toolBar.add(button);
		}
		return toolBar;
	}
	
	/**
	 * Recursively collects all {@link JMenuItem}s with an icon from the given
	 * {@link JMenu}.
	 * 
	 * @param menu
	 *        the current menu.
	 * @param toolBar
	 *        the {@link JToolBar} where to add the buttons.
	 * @param isOSX
	 *        to save repeatedly performing a check if we are on Mac OS X, this
	 *        variable is given.
	 * @return The number of buttons created for this particular menu. This avoids
	 *         creating separators for empty menus.
	 */
	private int collectMenuItems(JMenu menu, JToolBar toolBar, boolean isOSX) {
		int buttonCount = 0;
		JMenuItem item;
		JButton button;
		Object action;
		for (int j = 0; j < menu.getItemCount(); j++) {
			item = menu.getItem(j);
			if (item instanceof JMenu) {
				// recursive call:
				buttonCount += collectMenuItems((JMenu) item, toolBar, isOSX);
			}
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
					button = GUITools.createButton(item.getIcon(), item
							.getActionListeners()[0], action, item.getToolTipText());
					if (isOSX && (action == BaseAction.HELP_ONLINE)) {
						button.setIcon(null);
						button.putClientProperty("JButton.buttonType", "help");
						button.putClientProperty("JComponent.sizeVariant", "mini");
					} else {
						button.setBorderPainted(false);
						button.setOpaque(true);	
					}
					button.setEnabled(item.isEnabled());
					toolBar.add(button);
					buttonCount++;
				}
			}
		}
		return buttonCount;
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
      	/* (non-Javadoc)
      	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
      	 */
        @SuppressWarnings({ "unchecked" })
        //@Override
        public void actionPerformed(ActionEvent event) {
          if (event.getID() == FileDropHandler.FILE_DROPPED) {
            openFileAndLogHistory((File) event.getSource());
          } else if (event.getID() == FileDropHandler.FILES_DROPPED) {
          	File[] files = ((List<File>) event.getSource()).toArray(new File[] {});
          	openFileAndLogHistory(files);
          }
        }
      }
    );
    setTransferHandler(dragNdrop);
  }
	
	/**
	 * Creates a {@link JMenu} for user edit operations.
	 * 
	 * @return a {@link JMenu} might be {@code null} or empty (zero
	 *         {@link JMenuItem}s).
	 */
	protected JMenu createEditMenu() {
		int numPrefs = -1;
		if (appConf != null) {
			Class<? extends KeyProvider> interactive[] = appConf.getInteractiveOptions();
			if (interactive != null) {
				numPrefs = interactive.length;
			}
		}
    if (numPrefs < 0) {
  		/* 
  		 * Speed up the GUI by loading the preferences classes at the beginning
  		 * and add this menu only if there is at least one preference panel defined.
  		 * In case of options defined directly as a collection of KeyProviders, avoid
  		 * using reflection (time consuming).
  		 */
      numPrefs = MultiplePreferencesPanel.getPossibleTabCount();
    }
    
    boolean macOS = GUITools.isMacOSX();
		JMenuItem items[] = additionalEditMenuItems();
		
    if (((numPrefs > 0) && !macOS) || ((items != null) && (items.length > 0))) {
			JMenu editMenu;
	    String title = BaseAction.EDIT.getName();
			if (macOS || (numPrefs == 0)) {
				// Mac OS has its own preferences menu which is linked differently.
				editMenu = GUITools.createJMenu(title == null ? "Edit" : title,
					BaseAction.EDIT.getToolTip(), (Object[]) items);
			} else {
				// On all other systems we want to have a menu item for preferences.
				JMenuItem preferences = GUITools.createJMenuItem(
					EventHandler.create(ActionListener.class, this, "preferences"),
					BaseAction.EDIT_PREFERENCES,
					KeyStroke.getKeyStroke('E', InputEvent.ALT_GRAPH_DOWN_MASK), 'P',
					true);
				editMenu = GUITools.createJMenu(title == null ? "Edit" : title,
					BaseAction.EDIT.getToolTip(), items, (items != null)
							&& (items.length > 0) ? new JSeparator() : null, preferences);
			}
			editMenu.setActionCommand(BaseAction.EDIT.toString());
			return editMenu;
		}
    return null;
	}
	
	/**
	 * Create a {@link JMenu} with recently opened files.
	 * @return
	 */
	protected JMenu createFileHistory() {
    JMenu fileHistory = null;
    if (getMaximalFileHistorySize() > 0) {
			fileHistory = new JMenu(BaseAction.FILE_OPEN_RECENT.getName());
			fileHistory.setActionCommand(BaseAction.FILE_OPEN_RECENT.toString());
			fileHistory.setEnabled(false);
			String tooltip = BaseAction.FILE_OPEN_RECENT.getToolTip();
			if (tooltip != null) {
				if (GUITools.isMacOSX()) {
					fileHistory.setToolTipText(tooltip);
				} else {
					fileHistory.setToolTipText(StringUtil.toHTML(tooltip,
						StringUtil.TOOLTIP_LINE_LENGTH, false));
				}
			}
			SBPreferences history = SBPreferences.getPreferencesFor(getFileHistoryKeyProvider());
			String fileList = (history != null) ? history.get(FileHistory.LAST_OPENED) : null;
			updateFileHistory(FileHistory.Tools.parseList(fileList), fileHistory);
		}
    return fileHistory;
  }
	
	/**
	 * Creates a {@link JMenu} for dealing with files.
	 * 
	 * @param loadDefaultFileMenuEntries
	 * @return a {@link JMenu} might be {@code null} or empty (zero
	 *         {@link JMenuItem}s).
	 */
	protected JMenu createFileMenu(boolean loadDefaultFileMenuEntries) {
		boolean macOS = GUITools.isMacOSX();
		int ctr_down = macOS ? InputEvent.META_DOWN_MASK : InputEvent.CTRL_DOWN_MASK;
		JMenuItem openFile = null, saveFile = null, saveFileAs = null, closeFile = null;
		if (loadDefaultFileMenuEntries) {
			openFile = GUITools.createJMenuItem(EventHandler.create(
				ActionListener.class, this, "openFileAndLogHistory"),
				BaseAction.FILE_OPEN, KeyStroke.getKeyStroke('O', ctr_down), 'O', true);
			
			saveFileAs = GUITools.createJMenuItem(EventHandler.create(
				ActionListener.class, this, "saveFileAndLogSaveDir"),
				BaseAction.FILE_SAVE_AS, KeyStroke.getKeyStroke('S', ctr_down
						| InputEvent.SHIFT_DOWN_MASK), 'S', false);
			
			if (showsSaveMenuEntry()) {
				saveFile =  GUITools.createJMenuItem(EventHandler.create(
					ActionListener.class, this, "saveFileToOriginal"),
					BaseAction.FILE_SAVE, KeyStroke.getKeyStroke('S', ctr_down), 'S', false);
				/*
				 * The following is important, because otherwise the disk-icon would be
				 * used twice - for save as AND for save. If we have both, only save
				 * should have the icon. This is for two reasons:
				 * 1) It looks strange to have the same icon twice in the menu.
				 * 2) The default toolbar couldn't distinguish and would create
				 *    two identical looking buttons with two different meanings.
				 */
				saveFileAs.setIcon(null);
			}
			
			closeFile = GUITools.createJMenuItem(
				EventHandler.create(ActionListener.class, this, "closeFile"),
				BaseAction.FILE_CLOSE, KeyStroke.getKeyStroke('W', ctr_down), 'W',
				false);
		}
		JMenu fileMenu;
		JMenuItem items[] = additionalFileMenuItems();
		JMenu fileHistory = createFileHistory();
		String title = BaseAction.FILE.getName();
		if (macOS) {
			try {
				// Mac OS has its own "quit" menu item, so we don't want to create a separate one.
				new MacOSXController2(this);
			} catch (Throwable exc) {
				new MacOSXController(this);
			}
			fileMenu = GUITools.createJMenu(title == null ? "File" : title,
					BaseAction.FILE.getToolTip(), openFile, fileHistory, (showsSaveMenuEntry()) ? saveFile : null, saveFileAs, items,
					closeFile);
		} else {
			// On all other platforms we want to have a dedicated "exit" item.
			JMenuItem exit = GUITools.createJMenuItem(
				EventHandler.create(ActionListener.class, this, "exitPre"),
				BaseAction.FILE_EXIT,
				KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_DOWN_MASK));
			boolean addSeparator = (openFile != null) || (saveFile != null) || (saveFileAs != null)
					|| ((items != null) && (items.length > 0)) || (closeFile != null);
			fileMenu = GUITools.createJMenu(title == null ? "File" : title,
					BaseAction.FILE.getToolTip(), openFile, fileHistory, (showsSaveMenuEntry()) ? saveFile : null, saveFileAs, items,
					closeFile, addSeparator ? new JSeparator() : null, exit);
		}
		fileMenu.setActionCommand(BaseAction.FILE.toString());
		return fileMenu;
	}

	/**
	 * Creates a {@link JMenu} for user help.
	 * 
	 * @return a {@link JMenu} might be {@code null} or empty (zero
	 *         {@link JMenuItem}s).
	 */
	protected JMenu createHelpMenu() {
		KeyStroke ks = GUITools.isMacOSX() ? 
				KeyStroke.getKeyStroke(Character.valueOf('?'), InputEvent.META_DOWN_MASK) :
				KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0);
		JMenuItem help = (getURLOnlineHelp() == null) ? null : 
			GUITools.createJMenuItem(
					EventHandler.create(ActionListener.class, this, "showOnlineHelp"),
					BaseAction.HELP_ONLINE, ks, 'H', true);
		JMenuItem license = (getURLLicense() == null) ? null : 
			GUITools.createJMenuItem(
					EventHandler.create(ActionListener.class, this, "showLicense"),
					BaseAction.HELP_LICENSE, 'L', true);
		JMenuItem update = ((getURLOnlineUpdate() == null)
				|| (getDottedVersionNumber() == null) || (getDottedVersionNumber()
				.length() == 0)) ? null : GUITools.createJMenuItem(
			EventHandler.create(ActionListener.class, this, "onlineUpdate"),
			BaseAction.HELP_UPDATE, 'U', true);
		String title = BaseAction.HELP.getName();
		if (title == null) {
			title = "Help";
		}
		JMenu helpMenu;
		if (GUITools.isMacOSX() || (getURLAboutMessage() == null)) {
			// In Mac OS X the about message is shown in a different position.
			helpMenu = GUITools.createJMenu(title, BaseAction.HELP.getToolTip(),
				help, license, update, additionalHelpMenuItems());
		} else {
			JMenuItem about = GUITools.createJMenuItem(
				EventHandler.create(ActionListener.class, this, "showAboutMessage"),
				BaseAction.HELP_ABOUT, KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), 'I',
				true);
			helpMenu = GUITools.createJMenu(title, BaseAction.HELP.getToolTip(),
				help, about, license, update, additionalHelpMenuItems());
		}
		return helpMenu;
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
	 * {@code true} by default). If you don't implement the methods
	 * {@link #getURLOnlineHelp}, {@link #getURLAboutMessage()},
	 * {@link #getURLLicense()} or {@link #getURLOnlineUpdate()}, there will be,
	 * for instance no Help menu in this element's {@link JMenuBar}. Similarly, no
	 * Edit menu will occur if there are no instances of {@link PreferencesPanel}
	 * in package {@link de.zbit.gui.prefs}. Hence, this method will at least
	 * create a {@link JMenuBar} containing a single menu (File) which will at
	 * least contain the entry for Exit. Everything else depends on either if all
	 * methods mentioned above return a value distinct from {@code null} or if
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
		// Additional menus
		JMenu[] additionalMenus = additionalMenus();
		ArrayList<JMenu> listOfMenus = new ArrayList<JMenu>(
			additionalMenus == null ? 0 : additionalMenus.length + 2);
		
		listOfMenus.add(createFileMenu(loadDefaultFileMenuEntries));
		listOfMenus.add(createEditMenu());
		listOfMenus.add(createViewMenu());
		if (additionalMenus != null) {
			listOfMenus.addAll(Arrays.asList(additionalMenus));
		}
		
		JMenuBar menuBar = new JMenuBar();
		for (JMenu menu : listOfMenus) {
			// avoid adding empty menus
			if ((menu != null) && (menu.getItemCount() > 0)) {
				menuBar.add(menu);
			}
		}
		
		// Help menu might be a special case.
		JMenu helpMenu = createHelpMenu();
		if ((helpMenu != null) && (helpMenu.getItemCount() > 0)) {
			helpMenu.setActionCommand(BaseAction.HELP.toString());
			try {
				menuBar.setHelpMenu(helpMenu);
			} catch (Error exc) {
				// The special method setHelpMenu might not yet been implemented. 
				// So, let us just ignore this problem.
				menuBar.add(helpMenu);
			}
		}
		
		return menuBar;
	}

	/**
	 * By default this method simply returns {@code null}. You can override it to create
	 * your own tool bar.
	 * 
	 * @return an instance of {@link JToolBar} or {@code null} if no tool bar is desired.
	 * @see #createDefaultToolBar()
	 */
	protected abstract JToolBar createJToolBar();

	/**
	 * This creates the main element on the center of this {@link BaseFrame}. This
	 * can be any instance of {@link Component}. It might be helpful to add a
	 * field variable to your instance of {@link BaseFrame} to easily access and
	 * manipulate this element later on. Please note that this method is called by
	 * the constructor and therefore you should not set your main
	 * {@link Component} to {@code null} in your derived constructor. The intention of
	 * this method is that you here create some empty {@link Component} and at any
	 * later state you fill it with some content.
	 * 
	 * @return An instance of {@link Component}, which should not be {@code null}. It is
	 *         advisable to let this method initialize a field member of a derived
	 *         {@link Object}, which is manipulated at later states in the
	 *         program.
	 */
	protected abstract Component createMainComponent();

	/**
	 * 
	 * @return
	 */
	public JMenu createViewMenu() {
		ResourceBundle bundle = ResourceManager.getBundle(StringUtil.RESOURCE_LOCATION_FOR_LABELS);
		JCheckBoxMenuItem showStatusBar = new JCheckBoxMenuItem(bundle.getString("STATUS_BAR"), true);
		showStatusBar.addItemListener(new ItemListener() {
			
			/* (non-Javadoc)
			 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
			 */
			@Override
			public void itemStateChanged(ItemEvent evt) {
				Container container = getContentPane();
				if (evt.getStateChange() == ItemEvent.SELECTED) {
					container.add(statusBar, BorderLayout.SOUTH);
					container.validate();
				} else if (evt.getStateChange() == ItemEvent.DESELECTED) {
					container.remove(statusBar);
					container.validate();
				}
			}
		});
		JMenuItem items[] = additionalViewMenuItems();
		
		return GUITools.createJMenu(bundle.getString("VIEW"), items, showStatusBar);
	}

	/**
	 * Method that is called on exit, i.e., when this {@link BaseFrame}
	 * {@link Window} is closing.
	 */
	public void exit() {
	  dispose();
	}

	/**
   * This method is called when the window closes as well as when the user
   * presses the Exit button in the {@link JMenu}. This is used to prepare the
   * exit, before the actual exit method {@link #exit()} is called (i.e., store
   * window size and state).
   */
	public void exitPre() {
	  // Save with/height and window state
    SBPreferences windowProperties = SBPreferences.getPreferencesFor(getClass());
    if (getExtendedState() == Frame.NORMAL) {
      // Do not store width and height of a maximized window.
      // Rather also store the state and restore this"
      windowProperties.put(WINDOW_WIDTH, getWidth());
      windowProperties.put(WINDOW_HEIGHT, getHeight());
    }
    windowProperties.put(WINDOW_STATE, getExtendedState());
    try {
      windowProperties.flush();
    } catch (BackingStoreException exc) {
      // Really not to mention this unimportant error, unless for debugging.
      logger.finest(exc.getLocalizedMessage());
    }    
    // Call real exit method
    exit();
	}

	/**
	 * This convenient method allows callers to change the entry names of
	 * {@link JMenuItem}s in the {@link JMenuBar} of this {@link BaseFrame}. The
	 * returned {@link Map} contains a mapping from each {@link BaseAction} to the
	 * name that is to be associated with it. By default this method returns
	 * {@code null}, but you may override this method in order to customize
	 * the menu bar more easily. In this way, it is even possible to override
	 * single entries of the {@link JMenu} instead of being forced to override the
	 * entire list.
	 * 
	 * @return a mapping from {@link BaseAction} to its display name,
	 *         {@code null} by default.
	 */
  protected Map<BaseAction, String> getAlternativeBaseActionNames() {
		return null;
	}
  
  /* (non-Javadoc)
	 * @see de.zbit.UserInterface#getApplicationName()
	 */
	@Override
	public String getApplicationName() {
	  if (this.appConf != null) {
	    return appConf.getApplicationName();
	  }
	  return getClass().getSimpleName();
	}
	
	/* (non-Javadoc)
	 * @see de.zbit.UserInterface#getCommandLineOptions()
	 */
	@Override
	public Class<? extends KeyProvider>[] getCommandLineOptions() {
	  if (this.appConf != null) {
	    return appConf.getCmdOptions();
	  }
	  return null;
	}
	
	/* (non-Javadoc)
	 * @see de.zbit.UserInterface#getDottedVersionNumber()
	 */
	@Override
	public String getDottedVersionNumber() {
	  if (this.appConf != null) {
	    return appConf.getVersionNumber();
	  }
	  return "0.0.0";
	}
	
	/**
	 * Returns the {@link KeyProvider} that provides an {@link Option} to
	 * persistently store the history of previously opened files.
	 * 
	 * @return By default, this method returns an instance of {@link Class} for
	 *         the {@link FileHistory} interface. This must be added to the list
	 *         of values returned by {@link #getCommandLineOptions()}.
	 */
	public Class<? extends FileHistory> getFileHistoryKeyProvider() {
		return getClass();
	}
	
	/**
   * @return the {@link #toolBar} if it has been initialized in {@link #createJToolBar()}.
   * Else, {@code null} is returned.
   */
  public JToolBar getJToolBar() {
    return toolBar;
  }
	
	/**
	 * Override this message to change the texts of some or all {@link JMenuItem}s
	 * including their tool tips and also of {@link JButton}s and so on. The
	 * location given here must be a path that includes the package name, for
	 * instance {@link de.zbit.locales.MyTexts}, where the file extension
	 * {@code xml} is omitted, but the resource file is required to be an XML
	 * file in the format defined for {@link Properties}. The naming convention
	 * for the resource file says that it should contain a suffix consisting of
	 * underscore, language name, underscore, country name, e.g.,
	 * {@code MyTexts_de_DE.xml}. You may omit this suffix.
	 * 
	 * @return By default, this method returns {@code null}, i.e., the default texts will
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
	public short getMaximalFileHistorySize() {
	  return (short) 10;
	}
	
	/**
	 * @return the default directory to open {@link File}s.
	 */
  public File getOpenDir() {
    SBPreferences prefs = SBPreferences.getPreferencesFor(getClass());
    return new File(prefs.get(OPEN_DIR));
  }
	
	/* (non-Javadoc)
   * @see de.zbit.UserInterface#getProgramNameAndVersion()
   */
	@Override
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
	 * Returns the default directory to save {@link File}s.
	 * 
	 * @return
	 */
	public File getSaveDir() {
		SBPreferences prefs = SBPreferences.getPreferencesFor(getClass());
		return new File(prefs.get(SAVE_DIR));
	}
	
	/**
   * @return the current {@link StatusBar}.
   */
  public StatusBar getStatusBar() {
    return statusBar;
  }

	/* (non-Javadoc)
   * @see de.zbit.UserInterface#getURLOnlineUpdate()
   */
	@Override
	public URL getURLOnlineUpdate() {
	  return (appConf != null) ? appConf.getOnlineUpdate() : null;
	}
	
  /**
	 * Initializes this graphical user interface.
	 */
	protected void init() {
    this.listOfPrefChangeListeners = new LinkedList<PreferenceChangeListener>();

		if ((getTitle() == null) || (getTitle().length() == 0)) {
			setTitle(getProgramNameAndVersion());
		}
		setDefaultLookAndFeelDecorated(true);
		
		try {
			String defaultLocation = "de.zbit.locales.BaseAction";
			BaseAction.nameProperties.setDefaults(ResourceManager
					.getBundle(defaultLocation));
			String location = getLocationOfBaseActionProperties();
			if ((location != null) && (!location.equals(defaultLocation))) {
				try {
					BaseAction.nameProperties.putAll(ResourceManager.getBundle(location));
				} catch (Exception exc) {
					logger.finest(exc.getLocalizedMessage());
				}
			}
			Map<BaseAction, String> alternativeNames = getAlternativeBaseActionNames();
			if (alternativeNames != null) {
				for (Map.Entry<BaseAction, String> entry : alternativeNames.entrySet()) {
					try {
						BaseAction.nameProperties.put(entry.getKey(), entry.getValue());
					} catch (Exception exc) {
						logger.finest(exc.getLocalizedMessage());
					}
				}
			}
		} catch (Exception exc) {
			GUITools.showErrorMessage(this, exc);
		}
		
		// init GUI
		// Do nothing is important! The actual closing is handled in "windowClosing()"
		// which is not called on other close operations!
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(EventHandler.create(WindowListener.class, this, "exitPre",
			null, "windowClosing"));
		boolean loadsDefaultFileMenuEntries = loadsDefaultFileMenuEntries();
		setJMenuBar(createJMenuBar(loadsDefaultFileMenuEntries));
		
		// If the user wishes a OPEN FILE entry, also create openFile by Drag'n Drop.
		if (loadsDefaultFileMenuEntries) {
		  createDragNDropFunctionality();
		}
		
		// Set layout
		Container container = getContentPane();
		container.setLayout(new BorderLayout());
		
		// Create toolbar and main component
		toolBar = createJToolBar();
		if (toolBar != null) {
			container.add(toolBar, BorderLayout.NORTH);
		}
		Component component = createMainComponent();
		if (component != null) {
			container.add(component, BorderLayout.CENTER);
		}
		
		// Initialize status bar
		statusBar = initializeStatusBar();
		// Add status bar to content pane
		container.add(statusBar, BorderLayout.SOUTH);
				
  	// Restore last window size and state
		setMinimumSize(new Dimension(640, 480));
		pack();
		restoreWindowSizeAndState();
		setLocationRelativeTo(null);
	}
	
	/**
	 * Initialize the status bar. This method can be overwritten
	 * and if {@code null} is returned, the status bar is disabled.
	 */
  protected StatusBar initializeStatusBar() {
		// Get current icon
		Icon icon = getIconImage() != null ? new ImageIcon(getIconImage()) : null;

		// Create the status bar
		StatusBar statusBar = new StatusBar(icon, null);
		statusBar.registerAsIconListenerFor(this);
		
		// Capture and display logging messages.
		statusBar.displayLogMessagesInStatusBar();
		
		return statusBar;
  }

	/**
   * This method decides whether or not the file menu should already be equipped
   * with the default entries for "open", "save", and "close". By default, this
   * method returns {@code true}, i.e., the default File menu already
   * contains all these three items. You may want to override this method and to
   * return {@code false} instead to switch this behavior off.
   * 
   * @return Whether or not to equip the file menu with the three entries
   *         "open", "save", and "close". By default {@code true}
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
	 * hideErrorMessages is set to {@code false}, a message will also be
	 * shown if no update can be found or if an error occurs when searching for
	 * the update. Otherwise, there will be no such method in case of failure.
	 * 
	 * @param hideErrorMessages
	 *        if {@code true} no messages will be displayed to the user in
	 *        case of an unsuccessful search for an update.
	 */
	private void onlineUpdate(boolean hideErrorMessages) {
		GUITools.setEnabled(false, getJMenuBar(), toolBar, BaseAction.HELP_UPDATE);
		UpdateMessage update = new UpdateMessage(true, getApplicationName(),
			getURLOnlineUpdate(), getDottedVersionNumber(), hideErrorMessages);
		
		// Set the update window to use the same icon as this window.
		if (this.getIconImage() != null) {
		  update.setIcon(new ImageIcon(getIconImage()));
		}
		  
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
	 *         user or {@code null} if the user cancels the operation.
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
	 * @return the files that originate from the method {@link #openFile(File...)}.
	 */
	@Override
	public File[] openFileAndLogHistory(File... files) {
		if ((files != null) && (files.length > 0)) {
			// check all files if these can be accessed:
			List<File> readableFiles = new ArrayList<File>(files.length);
			List<File> unreadableFiles = new ArrayList<File>(files.length);
			for (File file : files) {
				if (file.exists() && file.canRead()) {
					readableFiles.add(file);
				} else {
					unreadableFiles.add(file);
				}
			}
			if (!unreadableFiles.isEmpty()) {
				showErrorCouldNotLoadFiles(false, unreadableFiles.toArray(new File[] {}));
				if (unreadableFiles.size() == files.length) {
					// This check is important to avoid that further files are opened.
					return null;
				}
			}
			files = readableFiles.toArray(new File[] {});
		}
		
		// select files to be processed:
		files = openFile(files);
		// Remember the baseDir and put files into history.
		if ((files != null) && (files.length > 0)) {
			List<File> fileList = new LinkedList<File>();
			// Process all those files that have just been opened.
			String baseDir = null;
			boolean sameBaseDir = true;
			for (File file : files) {
				if ((file != null) && file.exists() && file.canRead() && !fileList.contains(file)) {
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
				SBPreferences prefs = SBPreferences.getPreferencesFor(getClass());
				prefs.put(OPEN_DIR, baseDir);
				try {
					prefs.flush();
				} catch (BackingStoreException exc) {
					// do NOT show this error, because the user really dosn't know
					// how to handle a "The value for OPEN_DIR is out of range [...]"
					// message.
					logger.finest(exc.getLocalizedMessage());
				}
			}
			// Allow users to close the file(s) again
			GUITools.setEnabled(true, getJMenuBar(), toolBar, BaseAction.FILE_CLOSE);
			addToFileHistory(fileList);
		}
		return files;
	}
	
	/**
	 * Displays the configuration for the {@link PreferencesDialog}.
	 * 
	 * @return 
	 */
	public boolean preferences() {
		if ((appConf != null) && (appConf.getInteractiveOptions() != null)) { 
			return PreferencesDialog.showPreferencesDialog(
				this, listOfPrefChangeListeners, appConf.getInteractiveOptions()); 
		}
		return PreferencesDialog.showPreferencesDialog(this, listOfPrefChangeListeners);
	}
	
	/**
	 * Removes the given listener from this {@link BaseFrame}.
	 * 
	 * @param pcl
	 * @return {@code true} if this list contained the specified element
	 * @throws NullPointerException
	 *         if the specified element is {@code null} and this list does
	 *         not permit {@code null} elements (optional)
	 */
	public boolean removePreferenceChangeListener(PreferenceChangeListener pcl)
		throws NullPointerException {
		return listOfPrefChangeListeners.remove(pcl);
	}
	
	/**
   * Restores the window width, height and state from preferences.
   */
  private void restoreWindowSizeAndState() {
    SBPreferences prefs = SBPreferences.getPreferencesFor(getClass());
    // Avoid that the window may be bigger as the screen. This may cause problems on
    // some computers, e.g., MacOS.
    Dimension screenDimension = getToolkit().getScreenSize();
    int width = Math.min(WINDOW_WIDTH.getValue(prefs), (int) screenDimension.getWidth());
    int height = Math.min(WINDOW_HEIGHT.getValue(prefs), (int) screenDimension.getHeight());
    int state = WINDOW_STATE.getValue(prefs);
    setSize(new Dimension(width, height));
    if (state != Frame.ICONIFIED) {
      // Never set a frame to be initially iconified ;-)
      setExtendedState(state);
    }
  }

	/**
	 * Saves some results or the current work in some {@link File}. If you use a
	 * {@link JTabbedPane}, it is recommended to let your tabs implement the
	 * {@link BaseFrameTab} interface and simply call
	 * {@link BaseFrameTab#saveToFile()}.
	 * <p>
	 * By default, a call to this method is redirected to {@link #saveFileAs()},
	 * because not every program might offer the possibility to save some data in
	 * an opened input file again. If this is possible in your program, you should
	 * override this method.
	 * 
	 * @return the {@link File} into which the content has been saved. If the
	 *         returned value is not {@code null}, the directory in which the
	 *         {@link File} is located is stored as the
	 *         {@link GUIOptions#SAVE_DIR} property of the current class (but only
	 *         if it exists and can be read).
	 */
	public File saveFile() {
		// DO NOT MAKE THIS METHOD ABSTRACT! NOT EVERY PROGRAM ALLOWS STORING DATA
		// IN OPENED INPUT FILES AGAIN!
		File done = saveFileAs();
		if (done != null) {
			// Important to mark the appearance of the window as not modified...
			getRootPane().putClientProperty("Window.documentModified", Boolean.FALSE);
		}
		return done;
	}
	
	/**
	 * Calls {@link #saveFileAs()} and memorizes the directory in which a file was
	 * stored as a {@link GUIOptions#SAVE_DIR} for the current {@link Class}
	 * (which implements {@link GUIOptions}. While saving some file, the actions
	 * {@link BaseAction#FILE_SAVE} and {@link BaseAction#FILE_SAVE_AS} are
	 * disabled. When done, these are enabled again. Override this method if you
	 * want a different behavior.
	 */
	public void saveFileAndLogSaveDir() {
		GUITools.setEnabled(false, getJMenuBar(), getJToolBar(),
			BaseAction.FILE_SAVE, BaseAction.FILE_SAVE_AS);
		File file = saveFileAs();
		if (file != null) {
			if (!file.isDirectory()) {
				file = file.getParentFile();
			}
			if (file.exists() && file.canRead() && file.canWrite()) {
				SBPreferences prefs = SBPreferences.getPreferencesFor(getClass());
				prefs.put(GUIOptions.SAVE_DIR, file.getAbsolutePath());
				try {
		          prefs.flush();
		        } catch (BackingStoreException exc) {
		          // do NOT show this error, because the user really dosn't know
		          // how to handle a "The value for SAVE_DIR is out of range [...]"
		          // message.
		          logger.finest(exc.getLocalizedMessage());
		        };
			}
		}
		GUITools.setEnabled(true, getJMenuBar(), getJToolBar(),
			BaseAction.FILE_SAVE_AS);
	}
	
	/**
	 * Saves some results or the current work in some {@link File}. If you use a
	 * {@link JTabbedPane}, it is recommended to let your tabs implement the
	 * {@link BaseFrameTab} interface and simply call
	 * {@link BaseFrameTab#saveToFile()}.
	 * 
	 * @return the {@link File} into which the content has been saved. If the
	 *         returned value is not {@code null}, the directory in which the
	 *         {@link File} is located is stored as the
	 *         {@link GUIOptions#SAVE_DIR} property of the current class (but only
	 *         if it exists and can be read).
	 */
	@Override
	public abstract File saveFileAs();
	
	/**
	 * Calls {@link #saveFile()} and memorizes the directory in which a file was
	 * stored as a {@link GUIOptions#SAVE_DIR} for the current {@link Class}
	 * (which implements {@link GUIOptions}. While saving some file, the actions
	 * {@link BaseAction#FILE_SAVE} and {@link BaseAction#FILE_SAVE_AS} are
	 * disabled. When done, these are enabled again. Override this method if you
	 * want a different behavior. If {@link #saveFile()} returns {@code null}, than calls
	 * {@link #saveFileAs()}
	 */
	public void saveFileToOriginal() {
		GUITools.setEnabled(false, getJMenuBar(), getJToolBar(),
				BaseAction.FILE_SAVE, BaseAction.FILE_SAVE_AS);
		File file = saveFile();
		if (file == null) {
			saveFileAndLogSaveDir();
		}
		GUITools.setEnabled(true, getJMenuBar(), getJToolBar(),
			BaseAction.FILE_SAVE_AS);
	}

	/**
	 * This method modifies the title of this {@link BaseFrame} depending on the
	 * state of the given selected file. If you pass {@code null} to this method,
	 * the title will be the plain application's name together with the version
	 * number (see also {@link #getApplicationName()} and
	 * {@link #getDottedVersionNumber()}). If the given argument is not
	 * {@code null}, the effect of this method will be different depending on the
	 * platform that is used. For Mac OS X, the title will be marked with the
	 * Mac-specific properties {@code "Window.documentFile"} and
	 * {@code "Window.documentModified"}. The actual title will not be changed.
	 * For all other platforms, the title will be changed to a {@link String}
	 * consisting of the application's name followed by the version number, a
	 * dash, and the name of the file wrapped within the given {@link OpenedFile}.
	 * In this case, the file name will be marked with an asterisk if the file has
	 * been indicated to be changed according to the
	 * {@link OpenedFile#isChanged()} method. Finally, this method also influences
	 * the {@link JMenuBar} and the {@link JToolBar} linked to this frame (if
	 * set). Thereby, the possible user commands {@link BaseAction#FILE_SAVE},
	 * {@link BaseAction#FILE_SAVE_AS}, and {@link BaseAction#FILE_CLOSE} are
	 * manipulated accordingly.
	 * 
	 * @param selectedFile
	 *        the currently selected file that has been opened by the user in this
	 *        program, can be {@code null}. If it is null, the title will be
	 *        changed to its plain form, otherwise, depending on the operating
	 *        system, the title bar will indicate the file and if it is changed.
	 * @see #getProgramNameAndVersion()
	 */
	protected <T> void setFileStateMark(OpenedFile<T> selectedFile) {
		if ((selectedFile == null) || !selectedFile.isSetFile()) {
			logger.fine("Removing file entry from window view if necessary.");
			GUITools.setEnabled(false, getJMenuBar(), getJToolBar(), BaseAction.FILE_SAVE, BaseAction.FILE_SAVE_AS, BaseAction.FILE_CLOSE);
			if (GUITools.isMacOSX()) {
				JRootPane root = getRootPane();
				root.putClientProperty("Window.documentFile", null);
				root.putClientProperty("Window.documentModified", false);
			} else {
				setTitle(getProgramNameAndVersion());
			}
			return;
		}
		
		File file = selectedFile.getFile();
		logger.fine(MessageFormat.format("Adding file {0} to window view.", file.getName()));
		if (GUITools.isMacOSX()) {
			JRootPane root = getRootPane();
			root.putClientProperty("Window.documentFile", file);
			root.putClientProperty("Window.documentModified", Boolean.valueOf(selectedFile.isChanged()));
		} else {
			char changeMark = '*';
			if (selectedFile.isChanged()) {
				setTitle(getProgramNameAndVersion() + " - " + file.getName() + changeMark);
			} else {
				setTitle(getProgramNameAndVersion() + " - " + file.getName());
			}
		}
		GUITools.setEnabled(selectedFile.isChanged(), getJMenuBar(), getJToolBar(), BaseAction.FILE_SAVE);
		GUITools.setEnabled(true, getJMenuBar(), getJToolBar(), BaseAction.FILE_SAVE_AS, BaseAction.FILE_CLOSE);
	}
	
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
	 * update if the given {@link String} equals {@code onlineUpdateExecuted}
	 * .
	 * 
	 * @param command
	 *        If this command equals {@code onlineUpdateExecuted} the online
	 *        update button will be enabled.
	 */
	public final void setOnlineUpdateEnabled(String command) {
		if (command.equals("onlineUpdateExecuted")) {
			setOnlineUpdateEnabled();
		}
	}
	
	/* (non-Javadoc)
	 * @see java.awt.Window#setVisible(boolean)
	 */
	@Override
	public void setVisible(boolean b) {
		if (!UPDATE_CHECKED) {
			SBPreferences prefs = SBPreferences.getPreferencesFor(getClass());
			if (prefs.getBoolean(CHECK_FOR_UPDATES)) {
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
	@Override
	public final void showAboutMessage() {
		ResourceBundle resources = ResourceManager
				.getBundle(StringUtil.RESOURCE_LOCATION_FOR_LABELS);
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
	 * @param history
	 * @param unreadableFiles
	 */
	protected void showErrorCouldNotLoadFiles(boolean history, File... unreadableFiles) {
		ResourceBundle bundleWarnings = ResourceManager.getBundle("de.zbit.locales.Warnings");
		MessageFormat form = new MessageFormat(bundleWarnings.getString(history ? "FILES_NOT_ACCEPTABLE" : "COULD_NOT_LOAD_FILES"));
		ChoiceFormat fileform = new ChoiceFormat(new double[] {0d, 1d, 2d}, bundleWarnings.getStringArray("FILE_PART"));
		form.setFormatByArgumentIndex(0, fileform);
		ChoiceFormat fileExistForm = new ChoiceFormat(new double[] {0d, 1d, 2d}, bundleWarnings.getStringArray("FILE_EXISTENCE"));
		form.setFormatByArgumentIndex(1, fileExistForm);
		
		String fileList = Arrays.toString(unreadableFiles);
		fileList = fileList.substring(1, fileList.length() - 1);
		int lastComma = fileList.lastIndexOf(',');
		if (lastComma > 0) {
			fileList = fileList.substring(0, lastComma) + bundleWarnings.getString("AND") + fileList.substring(lastComma + 1);
		} else if (fileList.length() == 0) {
			fileList = "\"\""; // \u2423
		}
		String message = form.format(
			new Object[] {Long.valueOf(unreadableFiles.length), Long.valueOf(unreadableFiles.length), fileList});
		GUITools.showErrorMessage(this, message);
	}
	
	/**
	 * Displays the license under which this program is distributed in a
	 * {@link JOptionPane} of size 640x480.
	 */
	@Override
	public final void showLicense() {
		ResourceBundle resources = ResourceManager
				.getBundle(StringUtil.RESOURCE_LOCATION_FOR_LABELS);
		JOptionPane.showMessageDialog(this, createJBrowser(getURLLicense(), 640,
			480, true), String.format(resources.getString("LICENSE_OF_THE_PROGRAM"),
			getProgramNameAndVersion()), JOptionPane.INFORMATION_MESSAGE, UIManager
				.getIcon("ICON_LICENSE_64"));
	}
	
	/**
	 * Displays the online help in a {@link JHelpBrowser}.
	 */
	@Override
	public final void showOnlineHelp() {
		GUITools.setEnabled(false, getJMenuBar(), toolBar, BaseAction.HELP_ONLINE);
		ResourceBundle resources = ResourceManager
				.getBundle(StringUtil.RESOURCE_LOCATION_FOR_LABELS);
		JHelpBrowser.showOnlineHelp(this, EventHandler.create(WindowListener.class,
			this, "activateOnlineHelpCommand", null, "windowClosed"), String.format(
			resources.getString("ONLINE_HELP_FOR_THE_PROGRAM"),
			getProgramNameAndVersion()), getURLOnlineHelp(), getCommandLineOptions());
	}

	/**
	 * show the "Save" menu entry if true 
	 * @return
	 */
	protected boolean showsSaveMenuEntry() {
		return false;
	}

	/**
	 * Updates the list of previously opened files in the {@link JMenuBar}. This
	 * method is private because it relies on the correctness of the given
	 * {@link JMenu} and also the given {@link List} of {@link File}s. This method
	 * also makes the file history persistent. If this fails, a dialog with a
	 * precise error message will be displayed to the user.
	 * 
	 * @param listOfFiles
	 *        A {@link List} of those {@link File}s that have been opened
	 *        previously.
   * @param fileHistory
   *        The {@link JMenu}, whose items will be removed and replaced by
   *        references to the files given in the other argument. If the item
   *        count of this {@link JMenu} is zero at the end of this method, it
   *        will be disabled.
	 */
	private final void updateFileHistory(Collection<File> listOfFiles, JMenu... fileHistory) {
		for (JMenu jMenu : fileHistory) {
			jMenu.removeAll(); 
		}
		JMenuItem fileItem;
		List<File> keepFiles = new ArrayList<File>(listOfFiles.size());
		short maximum = getMaximalFileHistorySize();
		Iterator<File> it = listOfFiles.iterator();
		List<File> listOfUnreadableFiles = new ArrayList<File>(listOfFiles.size());
		for (int i = 0; i < Math.min(listOfFiles.size(), maximum); i++) {
			final File file = it.next();
			if (file.exists() && file.canRead()) {
			  for (JMenu jMenu : fileHistory) {
			    // One JMenuItem can only have one parent, thus, create it multiple times
			    // i.e., for every fileHistory once.
			    fileItem = new JMenuItem(file.getName());
			    fileItem.setToolTipText(file.getAbsolutePath());
			    if (maximum <= 10) {
			      fileItem.setAccelerator(KeyStroke.getKeyStroke(String.valueOf(
			        (i + 1 < 10) ? i + 1 : 0).charAt(0), InputEvent.ALT_DOWN_MASK));
			    }
			    fileItem.addActionListener(new ActionListener() {
			      /* (non-Javadoc)
			       * @seejava.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			       */
			    	public void actionPerformed(ActionEvent e) {
			    		openFileAndLogHistory(file);
			    	}
			    });
			    
			    jMenu.add(fileItem);
			  }
			  keepFiles.add(file);
			} else {
				listOfUnreadableFiles.add(file);
			}
		}
		for (JMenu jMenu : fileHistory) {
			jMenu.setEnabled(jMenu.getItemCount() > 0);
		}
		if (!listOfUnreadableFiles.isEmpty()) {
			showErrorCouldNotLoadFiles(true, listOfUnreadableFiles.toArray(new File[] {}));
		}
		// This removes files that cannot be read from the history.
		SBPreferences history = SBPreferences.getPreferencesFor(getFileHistoryKeyProvider());
		if (!keepFiles.isEmpty()) {
			history.put(FileHistory.LAST_OPENED, FileHistory.Tools.toString(keepFiles));
		} else {
			history.remove(FileHistory.LAST_OPENED);
		}
		try {
			history.flush();
		} catch (BackingStoreException exc) {
			GUITools.showErrorMessage(this, exc);
		}
	}
	
}
