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
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.awt.SplashScreen;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.filechooser.FileFilter;

import de.zbit.io.OpenFile;
import de.zbit.util.Reflect;
import de.zbit.util.ResourceManager;
import de.zbit.util.StringUtil;
import de.zbit.util.Utils;
import de.zbit.util.ValuePair;

/**
 * This class contains various GUI tools.
 * 
 * @author Andreas Dr&auml;ger
 * @author Hannes Borch
 * @author Clemens Wrzodek
 * @version $Rev$
 * @since 1.0
 */
public class GUITools {
  
  /**
   * The location for texts of labels. 
   */
  public static final String RESOURCE_LOCATION_FOR_LABELS = "de.zbit.locales.Labels";
  
  /**
   * 
   */
  private static final Logger logger = Logger.getLogger(GUITools.class.getName());
  
  /**
   * The number of symbols per line in tool tip texts.
   */
  public static int TOOLTIP_LINE_LENGTH = 60;
  
  static {
    // ImageTools.initImages(GUITools.class.getResource("img"));
    
    String iconPaths[] = {
        "ICON_ARROW_LEFT_16.png",
        "ICON_ARROW_LEFT_32.png",
        "ICON_ARROW_RIGHT_16.png",
        "ICON_ARROW_RIGHT_32.png",
        "ICON_DOCUMENT_16.png",
        "ICON_DOCUMENT_32.png",
        "ICON_DOCUMENT_48.png",
        "ICON_DOCUMENT_64.png",
        "ICON_EXIT_16.png",
        "ICON_GEAR_16.png",
        "ICON_GEAR_64.png",
        "ICON_GLOBE_16.png",
        "ICON_GLOBE_64.png",
        "ICON_HELP_16.png",
        "ICON_HELP_48.png",
        "ICON_HELP_64.png",
        "ICON_INFO_16.png",
        "ICON_INFO_64.png",
        "ICON_LICENSE_16.png",
        "ICON_LICENSE_64.png",
        "ICON_LICENSE_48.png",
        "ICON_OPEN_16.png",
        "ICON_PENCIL_16.png",
        "ICON_PENCIL_32.png",
        "ICON_PENCIL_48.png",
        "ICON_PENCIL_64.png",
        "ICON_PREFS_16.png",
        "ICON_SAVE_16.png",
        "ICON_TICK_16.png",
        "ICON_TRASH_16.png",
        "ICON_SEARCH_16.png",
        "UT_BM_Rot_RGB_tr_36x64.png",
        "UT_WBMS_Rot_RGB_tr_64x62.png",
        "UT_WBMW_mathnat_4C_380x45.png"
    };
    for (String path : iconPaths) {
      URL u = GUITools.class.getResource("img/" + path);
      if (u!=null) {
        UIManager.put(path.substring(0, path.lastIndexOf('.')), new ImageIcon(u));
      }
    }
  }
  
  /**
   * Sets the dimension of all given {@link JComponent} instances to the maximal
   * size, i.e., all components will be set to equal size, which is the maximal
   * preferred size of one of the components.
   * 
   * @param components
   */
  public static void calculateAndSetMaxWidth(JComponent... components) {
    double maxWidth = 0d, maxHeight = 0d;
    Dimension curr;
    for (JComponent component : components) {
      curr = component.getPreferredSize();
      if (curr.getWidth() > maxWidth) {
        maxWidth = curr.getWidth();
      }
      if (curr.getHeight() > maxHeight) {
        maxHeight = curr.getHeight();
      }
    }
    for (JComponent component : components) {
      component.setPreferredSize(new Dimension((int) maxWidth, (int) maxHeight));
    }
  }
  
  
  /**
   * Checks whether the first container contains the second one.
   * 
   * @param c
   * @param insight
   * @return True if c contains insight.
   */
  public static boolean contains(Component c, Component insight) {
    boolean contains = c.equals(insight);
    if ((c instanceof Container) && !contains)
      for (Component c1 : ((Container) c).getComponents()) {
        if (c1.equals(insight))
          return true;
        else contains |= contains(c1, insight);
      }
    return contains;
  }
  
  /**
   * Creates a JButton with the given properties. The tool tip becomes an HTML
   * formatted string with a line break after {@link #TOOLTIP_LINE_LENGTH} symbols.
   * 
   * @param icon
   * @param listener
   * @param com
   * @param toolTip
   * @return
   */
  public static JButton createButton(Icon icon, ActionListener listener,
    Object command, String toolTip) {
    JButton button = new JButton();
    if (icon != null) {
      button.setIcon(icon);
    }
    if (listener != null) {
      button.addActionListener(listener);
    }
    if (command != null) {
      button.setActionCommand(command.toString());
    }
    if (toolTip != null) {
      button.setToolTipText(StringUtil.toHTML(toolTip, TOOLTIP_LINE_LENGTH ));
    }
    return button;
  }
  
  /**
   * @param text
   * @param icon
   * @param listener
   * @param command
   * @param toolTip
   * @return
   */
  public static JButton createButton(String text, Icon icon,
    ActionListener listener, Object command, String toolTip) {
    JButton button = createButton(icon, listener, command, toolTip);
    if (text != null) {
      button.setText(text);
    }
    return button;
  }
  
  /**
   * Creates and returns a JCheckBox with all the given properties.
   * 
   * @param label
   * @param selected
   * @param name
   *        The name for the component to be identifiable by the ItemListener
   * @param toolTip
   * @param listener
   * @return
   */
  public static JCheckBox createJCheckBox(String label, boolean selected,
    Object command, String toolTip, ItemListener... listener) {
    JCheckBox chkbx = new JCheckBox(label, selected);
    chkbx.setActionCommand(command.toString());
    for (ItemListener l : listener) {
      chkbx.addItemListener(l);
    }
    chkbx.setToolTipText(StringUtil.toHTML(toolTip, TOOLTIP_LINE_LENGTH));
    return chkbx;
  }
  
  /**
   * @param dir
   * @param allFilesAcceptable
   * @param multiSelectionAllowed
   * @param mode e.g., JFileChooser.FILES_ONLY
   * @return
   */
  public static JFileChooser createJFileChooser(String dir,
    boolean allFilesAcceptable, boolean multiSelectionAllowed, int mode) {
    JFileChooser chooser = new JFileChooser(dir);
    chooser.setAcceptAllFileFilterUsed(allFilesAcceptable);
    chooser.setMultiSelectionEnabled(multiSelectionAllowed);
    chooser.setFileSelectionMode(mode);
    return chooser;
  }
  
  /**
   * @param dir
   * @param allFilesAcceptable
   * @param multiSelectionAllowed
   * @param mode e.g., JFileChooser.FILES_ONLY
   * @param filter
   * @return
   */
  public static JFileChooser createJFileChooser(String dir,
    boolean allFilesAcceptable, boolean multiSelectionAllowed, int mode,
    FileFilter... filter) {
    JFileChooser chooser = createJFileChooser(dir, allFilesAcceptable,
      multiSelectionAllowed, mode);
    if (filter != null) {
      int i = filter.length - 1;
      while (0 <= i) {
        chooser.addChoosableFileFilter(filter[i--]);
      }
      if (i >= 0) {
        chooser.setFileFilter(filter[i]);
      }
    }
    return chooser;
  }
  
  /**
   * Shows a {@link JFileChooser} to the user, appends the selected file extension to the
   * selected file and checks weather the selected file is writable. If the file already
   * exists, the user will be asked if he wants to overwrite the file. If the file is not
   * writable, an error message id displayed to the user.
   * @param parent parent component for makeing a modal dialog. May be null.
   * @param saveDir initial directory for the {@link JFileChooser}. May be null.
   * @param filter {@link FileFilter} the user may choose from. May be null.
   * @return a file selected by the user if and only if this file is writable and the user
   * confirmed every warning. Else, null is returned. 
   */
  public static File showSaveFileChooser(Component parent, String saveDir, FileFilter... filter) {
    JFileChooser fc = GUITools.createJFileChooser(saveDir, (filter==null || filter.length<1),
      false, JFileChooser.FILES_ONLY, filter);
    if (fc.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return null;
    
    // Get selected file and try to prepend extension
    File f = fc.getSelectedFile();
    
    // Eventually append file extension
    if (!f.getName().contains(".")) {
      String extension = fc.getFileFilter().getDescription();
      int pos = extension.lastIndexOf("*.");
      if (pos>=0) {
        extension = Utils.getWord(extension, pos+2, false);
        // *.* => (extension.length()==0)
        if (extension!=null && extension.length()>0) {
          f = new File(f.getPath() + "." + extension);
        }
      }
    }
    
    // Check if file exists and is writable
    boolean fileExists = f.exists();
    if (!fileExists) try {
      f.createNewFile();
    } catch (IOException e) {
      GUITools.showErrorMessage(parent, e);
      return null;
    }
    if (!f.canWrite() || f.isDirectory()) {
      GUITools.showNowWritingAccessWarning(parent, f);
    } else if (!fileExists || (fileExists && GUITools.overwriteExistingFile(parent, f))) {
      // This is the usual case
      return f;
    }
    return null;
  }
  
  /**
   * Creates a new JMenuItem with the given text as label and the mnemonic. All
   * given menu items are added to the menu.
   * 
   * @param text
   * @param mnemonic
   * @param menuItems
   *        instances of {@link JMenuItem} or {@link JSeparator}. Other
   *        {@link Object}s are ignored.
   * @return
   */
  public static JMenu createJMenu(String text, char mnemonic,
    Object... menuItems) {
    JMenu menu = new JMenu(text);
    menu.setMnemonic(mnemonic);
    for (Object item : menuItems) {
      if (item instanceof JMenuItem) {
        menu.add((JMenuItem) item);
      } else if (item instanceof JSeparator) {
        menu.add((JSeparator) item);
      } else if (item instanceof JMenuItem[]) {
        for (JMenuItem i : (JMenuItem[]) item) {
          menu.add(i);
        }
      } else if (item instanceof JMenu) {
        menu.add((JMenu) item);
      }
    }
    return menu;
  }
  
  /**
   * Creates a new {@link JMenu} with the given menu items and sets the first
   * letter in the menu's name as mnemonic.
   * 
   * @param text
   * @param menuItems
   * @return
   */
  public static JMenu createJMenu(String text, Object... menuItems) {
    return createJMenu(text, text.charAt(0), menuItems);
  }
  
  /**
   * 
   * @param text
   * @param tooltip
   * @param menuItems
   * @return
   */
  public static JMenu createJMenu(String text, String tooltip, Object... menuItems) {
    JMenu menu = createJMenu(text, menuItems);
    if ((tooltip != null) && (tooltip.length() > 0) && (!tooltip.equals(text))) {
      menu.setToolTipText(StringUtil.toHTML(tooltip, TOOLTIP_LINE_LENGTH));
    }
    return menu;
  }
  
  /**
   * Creates an entry for the menu bar.
   * 
   * @param listener
   * @param command
   * @return
   */
  public static JMenuItem createJMenuItem(ActionListener listener,
    ActionCommand command) {
    return createJMenuItem(listener, command, (Icon) null);
  }
  
  /**
   * @param listener
   * @param command
   * @param icon
   * @return
   */
  public static JMenuItem createJMenuItem(ActionListener listener,
    ActionCommand command, Icon icon) {
    return createJMenuItem(listener, command, icon, null);
  }
  
  /**
   * Creates an entry for the menu bar.
   * 
   * @param listener
   * @param command
   * @param icon
   * @param mnemonic
   * @return
   */
  public static JMenuItem createJMenuItem(ActionListener listener,
    ActionCommand command, Icon icon, char mnemonic) {
    return createJMenuItem(listener, command, icon, null, Character
      .valueOf(mnemonic));
  }
  
  /**
   * Creates a new item for a {@link JMenu}.
   * 
   * @param listener
   *        the ActionListener to be added
   * @param command
   *        the action command for this button, i.e., the item in the menu. This
   *        will be converted to a {@link String} using the {@link
   *        String.#toString()} method.
   * @param icon
   *        the icon of the JMenuItem (can be null)
   * @param keyStroke
   *        the KeyStroke which will serve as an accelerator
   * @return A new {@link JMenuItem} with the given features.
   */
  public static JMenuItem createJMenuItem(ActionListener listener,
    ActionCommand command, Icon icon, KeyStroke keyStroke) {
    return createJMenuItem(listener, command, icon, keyStroke, null);
  }
  
  /**
   * Creates an entry for the menu bar.
   * 
   * @param listener
   * @param command
   * @param icon
   * @param ks
   * @param mnemonic
   * @return
   */
  public static JMenuItem createJMenuItem(ActionListener listener,
    ActionCommand command, Icon icon, KeyStroke ks, Character mnemonic) {
    return createJMenuItem(listener, command, icon, ks, mnemonic, JMenuItem.class);
  }

  /**
   * Creates an entry for the menu bar.
   * 
   * @param listener
   * @param command
   * @param icon
   * @param ks
   * @param mnemonic
   * @param type may also be {@link JCheckBoxMenuItem} or other derivates
   * of {@link JMenuItem}.
   * @return
   */
  public static JMenuItem createJMenuItem(ActionListener listener,
      ActionCommand command, Icon icon, KeyStroke ks, Character mnemonic,
      Class<?extends JMenuItem> type) {
    if (type==null) type = JMenuItem.class;
    JMenuItem item;
    try {
      item = type.newInstance();
    } catch (Exception e) {
      logger.log(Level.WARNING, "Cannot instantiate class.", e);
      item = new JMenuItem();
    }

    if (ks != null) {
      item.setAccelerator(ks);
    }
    if (listener != null) {
      item.addActionListener(listener);
    }
    if (mnemonic != null) {
      item.setMnemonic(mnemonic.charValue());
    }
    if (command != null) {
      item.setText(command.getName());
      String toolTip = command.getToolTip();
      if (toolTip != null) {
        item.setToolTipText(StringUtil.toHTML(toolTip, TOOLTIP_LINE_LENGTH));
      }
      item.setActionCommand(command.toString());
    }
    if (icon != null) {
      item.setIcon(icon);
    }
    return item;
  }
  
  /**
   * @param listener
   * @param command
   * @param icon
   * @param ks
   * @param mnemonic
   * @param enabled
   * @return
   */
  public static JMenuItem createJMenuItem(ActionListener listener,
    ActionCommand command, Icon icon, KeyStroke ks, Character mnemonic,
    boolean enabled) {
    JMenuItem item = createJMenuItem(listener, command, icon, ks, mnemonic);
    item.setEnabled(enabled);
    return item;
  }
  
  /**
   * @param listener
   * @param command
   * @param keyStroke
   * @return
   */
  public static JMenuItem createJMenuItem(ActionListener listener,
    ActionCommand command, KeyStroke keyStroke) {
    return createJMenuItem(listener, command, null, keyStroke);
  }
  
  
  /**
   * Creates a new {@link JButton} with action listeners that invoke
   * a specific method.
   * @param listener the ActionListener to be added
   * @param command the action command for this button, i.e., the item
   *        in the menu. This will be converted to a {@link String} using 
   *        the {@link String.#toString()} method.
   * @param icon the icon of the JButton (can be null)
   * @return A new {@link JButton} with the given features.
   */
  public static JButton createJButton(ActionListener listener,
    ActionCommand command, Icon icon) {
    return createJButton(listener, command, icon, null);
  }
  
  /**
   * Creates a {@link JButton}.
   * @param listener
   * @param command
   * @param icon
   * @param mnemonic
   * @return
   */
  public static JButton createJButton(ActionListener listener,
    ActionCommand command, Icon icon, Character mnemonic) {
    JButton button = new JButton();
    if (listener != null) {
      button.addActionListener(listener);
    }
    if (mnemonic != null) {
      button.setMnemonic(mnemonic.charValue());
    }
    if (command != null) {
      button.setText(command.getName());
      String toolTip = command.getToolTip();
      if (toolTip != null) {
        button.setToolTipText(StringUtil.toHTML(toolTip, TOOLTIP_LINE_LENGTH));
      }
      button.setActionCommand(command.toString());
    }
    if (icon != null) {
      button.setIcon(icon);
    }
    return button;
  }
  
  /**
   * Create a panel for a component and adds a title to it. 
   */
  public static JComponent createTitledPanel(JComponent content, String title) {
    /*JPanel panel = new JPanel();
	    JLabel label = new JLabel(title);
	    label.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
	    label.setBackground(new Color(231, 219,182));
	    label.setOpaque(true);
	    label.setForeground(Color.DARK_GRAY);
	    label.setFont(label.getFont().deriveFont(Font.BOLD));
	    label.setFont(label.getFont().deriveFont(13.0f));
	    panel.setLayout(new BorderLayout());
	    panel.add(label, BorderLayout.NORTH);
	    panel.add(content, BorderLayout.CENTER);*/
    //return panel;
    
    content.setBorder(BorderFactory.createTitledBorder(title));
    
    return content;
  }
  
  /**
   * Searches for the parent #{@link java.awt.Window} of the given component c.
   * Checks if this Window contains a #{@link javax.swing.AbstractButton} which
   * is called "Ok" and disables this button.
   * @param c
   * @return true if and only if an ok-button has been disabled. Else, false.
   */
  public static boolean disableOkButton(Component c) {
    // Seach for parent window
    c = getParentWindow(c);
    
    // Search for ok button and check if all other are enabled.
    if (c != null) {
      // c is now a Window.
      Component okButton = searchFor((Window) c, AbstractButton.class,
        "getText", getOkButtonText());
      if (okButton != null) {
        okButton.setEnabled(false);
        return true;
      }
    }
    return false;
  }
  
  
  /**
   * Checks if this "c" contains a #{@link javax.swing.AbstractButton} which
   * is called "OK" and enables this button if and only if it a) exists and is
   * disabled and b) all other elements on this container and all
   * contained containers are enabled.
   * @param c
   * @return true if and only if an ok-button has been enabled. Else, false.
   */
  public static synchronized boolean enableOkButtonIfAllComponentsReady(Container c) {
    // Seach for parent window
    // Do NOT uncomment it. Leads to unexpected behaviour.
    /*while (c!=null) {
	      if (c instanceof Window) {
	        //((Window)c).pack();
	        break;
	      }
	      c = c.getParent();
	    }*/
    
    // Search for ok button and check if all other are enabled.
    if (c!=null) {
      // c is now a Window.
      Component okButton = searchFor(c, AbstractButton.class, "getText",
        getOkButtonText());
      if (okButton!=null) {
        boolean previousState = okButton.isEnabled();
        okButton.setEnabled(true);
        if (isEnabled(c)) {
          return true;
        } else {
          okButton.setEnabled(previousState);
          return false;
        }
      }
    }
    return false;
  }
  
  /**
   * Simply checks if all elements on "c" are enabled and if yes, the given "okButton" will
   * be enabled. Else, the current state stays untouched.
   * 
   * In opposite to {@link #enableOkButtonIfAllComponentsReady(Component)}, this function 
   * uses the given okButton.
   * @param c
   * @param okButton
   * @return
   */
  public static synchronized boolean enableOkButtonIfAllComponentsReady(Container c, AbstractButton okButton) {
    boolean previousState = okButton.isEnabled();
    okButton.setEnabled(true);
    if (isEnabled(c)) {
      return true;
    } else {
      okButton.setEnabled(previousState);
      return false;
    }
  }
  
  public static synchronized boolean enableOkButton(Container c) {
    // Search for ok button and enable.
    if (c!=null) {
      // c is now a Window.
      Component okButton = searchFor(c, AbstractButton.class, "getText", getOkButtonText());
      if (okButton!=null) {
        okButton.setEnabled(true);
        return true;
      }
    }
    return false;
  }
  
  /**
   * Tries to get the lokalized cancel Button Message, as it occurs
   * in the UIManager (e.g. JOptionPane).
   * @return
   */
  public static String getCancelButtonText() {
    
    // First, get it from the UI Manager
    Object cancel = UIManager.get("OptionPane.cancelButtonText");
    if (cancel!=null) return cancel.toString();
    
    // Second, try to get it from the internal resources
    ResourceBundle resource = ResourceManager.getBundle(RESOURCE_LOCATION_FOR_LABELS);
    cancel = resource==null?null: resource.getString("CANCEL");
    if (cancel!=null) {
      if (cancel.toString().contains(";")) return cancel.toString().split(";")[0];
      else return cancel.toString();
    }
    
    return "Cancel";
  }
  
  /**
   * Computes and returns the dimension, i.e., the size of a given icon.
   * 
   * @param icon
   *        an icon whose dimension is required.
   * @return The dimension of the given icon.
   */
  public static Dimension getDimension(Icon icon) {
    return icon == null ? new Dimension(0, 0) : new Dimension(icon
      .getIconWidth(), icon.getIconHeight());
  }
  
  /**
   * @param jMenuBar
   * @param fileOpenRecent
   * @return
   */
  public static JMenuItem getJMenuItem(JMenuBar menuBar, Object command) {
    JMenu menu;
    JMenuItem item;
    for (int i = 0; i < menuBar.getMenuCount(); i++) {
      menu = menuBar.getMenu(i);
      if ((menu.getActionCommand() != null)
          && (menu.getActionCommand().equals(command.toString()))) { return menu; }
      for (int j = 0; j < menu.getItemCount(); j++) {
        item = menu.getItem(j);
        if ((item != null) && (item.getActionCommand() != null)
            && (item.getActionCommand().equals(command.toString()))) { return item; }
      }
    }
    return null;
  }
  
  /**
   * Tries to get the lokalized ok Button Message, as it occurs
   * in the UIManager (e.g. JOptionPane).
   * @return
   */
  public static String getOkButtonText() {
    
    // First, get it from the UI Manager
    Object ok = UIManager.get("OptionPane.okButtonText");
    if (ok!=null) return ok.toString();
    
    // Second, try to get it from the internal resources
    ResourceBundle resource = ResourceManager.getBundle(RESOURCE_LOCATION_FOR_LABELS);
    ok = resource==null?null: resource.getString("OK");
    if (ok!=null) {
      if (ok.toString().contains(";")) return ok.toString().split(";")[0];
      else return ok.toString();
    }
    
    return "OK";
  }
  
  /**
   * Searches for the parent #{@link java.awt.Window} of the given component c.
   * @param c
   * @return the window if found, or null if not found.
   */
  public static Window getParentWindow(Component c) {
    while (c!=null) {
      if (c instanceof Window) {
        return (Window) c;
      }
      c = c.getParent();
    }
    return null;
  }
  
  
  /**
   * The JVM command line allows to show splash screens. If the user made
   * use of this functionality, the following code will hide the screen.
   * Otherwise, this function does nothing.
   */
  public static void hideSplashScreen() {
    try {
      final SplashScreen splash = SplashScreen.getSplashScreen();
      if (splash == null) {
        return;
      }
      splash.close();
    } catch (Throwable t) {}
  }
  
  /**
   * @param g
   * @param incrementBy
   * @return
   */
  public static Font incrementFontSize(Font g, int incrementBy) {
    return g.deriveFont((float) (g.getSize() + incrementBy));
  }
  
  /**
   * Initializes the look and feel.
   * 
   * @param title
   *        - Name of your application.
   */
  public static void initLaF(String title) {
    // Locale.setDefault(Locale.ENGLISH);
    // For MacOS X
    boolean isMacOSX = false;
    if (System.getProperty("mrj.version") != null) {
      isMacOSX = true;
      System.setProperty("com.apple.macos.smallTabs", "true");
      System.setProperty("com.apple.mrj.application.live-resize", "true");
      System.setProperty("com.apple.mrj.application.apple.menu.about.name",
        title);
      System.setProperty("apple.laf.useScreenMenuBar", "true");
    }
    try {			
      UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());
      String osName = System.getProperty("os.name");
      if (osName.equals("Linux") || osName.equals("FreeBSD")) {
        UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
        // UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
      } else if (isMacOSX) {
        UIManager.setLookAndFeel("ch.randelshofer.quaqua.QuaquaLookAndFeel");
      } else if (osName.contains("Windows")) {
        UIManager
        .setLookAndFeel(new com.sun.java.swing.plaf.windows.WindowsLookAndFeel());
      } else {
        // UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      }
      
    } catch (Exception e) {
      // If Nimbus is not available, you can set the GUI to another look
      // and feel.
      // Native look and feel for Windows, MacOS X. GTK look and
      // feel for Linux, FreeBSD
      try {
        for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
          if ("Nimbus".equals(info.getName())) {
            UIManager.setLookAndFeel(info.getClassName());
            break;
          }
        }
        
      } catch (Exception exc) {
        JOptionPane.showMessageDialog(null, StringUtil.toHTML(exc.getLocalizedMessage(),
          TOOLTIP_LINE_LENGTH), exc.getClass().getName(), JOptionPane.WARNING_MESSAGE);
        logger.log(Level.WARNING, exc.getLocalizedMessage(), exc);
      }
    }
  }
  
  /**
   * Checks, if all elements on c are enabled.
   * @param c
   * @return true if and only if c and all components on c are enabled.
   */
  public static boolean isEnabled(Container c) {
    Component inside;
    boolean enabled = c.isEnabled();
    for (int i = 0; i < c.getComponentCount(); i++) {
      inside = c.getComponent(i);
      enabled &= inside.isEnabled();
      if (!enabled) return false; // shortcut.
      if (inside instanceof Container) {
        enabled&=isEnabled((Container) inside);
      }
      if (!enabled) return false; // shortcut.
    }
    return enabled;
  }
  
  /**
   * @param parent
   * @param dir
   * @param allFilesAcceptable
   * @param multiSelectionAllowed
   * @param mode e.g., JFileChooser.FILES_ONLY
   * @param filter
   * @return null if for some reason the no {@link File} has been selected or
   *         the {@link File} cannot be read, else the selected {@link File}.
   */
  public static File[] openFileDialog(final Component parent, String dir,
    boolean allFilesAcceptable, boolean multiSelectionAllowed, int mode,
    FileFilter... filter) {
    File[] ret=null;
    JFileChooser chooser = createJFileChooser(dir, allFilesAcceptable,
      multiSelectionAllowed, mode, filter);
    if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
      if (multiSelectionAllowed) {
        ret = chooser.getSelectedFiles();
      } else {
        ret = new File[]{chooser.getSelectedFile()};
      }
      for (File f: ret) {
        if (!f.canRead()) {
          showNowReadingAccessWarning(parent, f);
          return null;
        }
      }
    }
    return ret;
  }
  
  /**
   * @param parent
   * @param dir
   * @param allFilesAcceptable
   * @param multiSelectionAllowed
   * @param mode e.g., JFileChooser.FILES_ONLY
   * @param filter
   * @return null if for some reason the no {@link File} has been selected or
   *         the {@link File} cannot be read, else the selected {@link File}.
   */
  public static File openFileDialog(final Component parent, String dir,
    boolean allFilesAcceptable, int mode,
    FileFilter... filter) {
    JFileChooser chooser = createJFileChooser(dir, allFilesAcceptable,
      false, mode, filter);
    if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
      File f = chooser.getSelectedFile();
      if (!f.canRead()) {
        showNowReadingAccessWarning(parent, f);
      } else {
        return f;
      }
    }
    return null;
  }
  
  /**
   * Create an open file dialog that let's the user
   * select any file.
   * @param parent
   * @param dialogTitle allows to change the title of the
   * file dialog.
   * @param acceptURLs if true, will also accept any http
   * or ftp url as input and return this url.
   * @return
   */
  public static String openFileDialog(final Component parent,
    String dialogTitle, boolean acceptURLs) {
    JFileChooser chooser = createJFileChooser(null, true,
      false, JFileChooser.FILES_ONLY, (FileFilter)null);
    if (dialogTitle!=null) chooser.setDialogTitle(dialogTitle);
    if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
      File f = chooser.getSelectedFile();
      String fString = f.getPath();
      if (acceptURLs) {
        // Stupid dialog converts the url to something like
        // C:\Users\Documents\http:\www.broadinstitute.org\c3.gmt
        int pos = fString.indexOf("http:");
        int pos2 = fString.indexOf("ftp:");
        pos = Math.max(pos, pos2);
        if (pos>=0) {
          String url = fString.substring(pos, fString.length());
          if (StringUtil.fileSeparator()=='\\') {
            url = url.replace('\\', '/');
          }
          url = url.replaceFirst(Pattern.quote("http:/"), "http://");
          url = url.replaceFirst(Pattern.quote("ftp:/"), "ftp://");
          return url;
        }
      }
      if (!f.canRead()) {
        showNowReadingAccessWarning(parent, f);
      } else {
        return f.getPath();
      }
    }
    return null;
  }
  
  /**
   * @param parent
   * @param out
   * @return
   */
  public static boolean overwriteExistingFile(Component parent, File out) {
    return GUITools.overwriteExistingFileDialog(parent, out) == JOptionPane.YES_OPTION;
  }
  
  /**
   * Shows a dialog that asks whether or not to overwrite an existing file and
   * returns the answer from JOptionPane constants.
   * 
   * @param parent
   * @param out
   * @return An integer representing the user's choice.
   */
  public static int overwriteExistingFileDialog(Component parent, File out) {
    ResourceBundle resource = ResourceManager
    .getBundle(RESOURCE_LOCATION_FOR_LABELS);
    return JOptionPane.showConfirmDialog(parent, StringUtil.toHTML(String
      .format(resource.getString("OVERRIDE_EXISTING_FILE_QUESTION"),
        StringUtil.changeFirstLetterCase(resource
          .getString(out.isFile() ? "THE_FILE" : "THE_DIRECTORY"), true,
          false), out.getName()), TOOLTIP_LINE_LENGTH), resource
          .getString("OVERRIDE_EXISTING_FILE_TITLE"), JOptionPane.YES_NO_OPTION,
          JOptionPane.QUESTION_MESSAGE);
  }
  
  /**
   * Recursively looks for the first parent window of the
   * given component and calls the "pack()" method on it.
   * @param parent
   */
  public static void packParentWindow(Component parent) {
    Component c = parent;
    while (c!=null) {
      if (c instanceof Window) {
        ((Window)c).pack();
        //((Window)c).doLayout();
        break; // Dialogs, shown on top of windows. If no break here, window gets also packed.
      }
      c = c.getParent();
    }
  }
  
  /**
   * Replaces two components. Tries to preserve the layout while replacing the
   * two components on the parent of the oldOne.
   * 
   * @param oldOne
   * @param newOne
   */
  public static void replaceComponent(JComponent oldOne, JComponent newOne) {
    if (oldOne == null || oldOne.getParent() == null) {
      // All I can do here is replacing the variables...
      oldOne = newOne;
      return;
    }
    
    Container target = oldOne.getParent();
    LayoutManager lm = target.getLayout();
    
    // Try to replace by setting same layout as old component
    if (lm instanceof BorderLayout) {
      Object c = ((BorderLayout) lm).getConstraints(oldOne);
      lm.removeLayoutComponent(oldOne);
      ((BorderLayout) lm).addLayoutComponent(newOne, c);
      
    } else if (lm instanceof GridBagLayout) {
      Object c = ((GridBagLayout) lm).getConstraints(oldOne);
      lm.removeLayoutComponent(oldOne);
      ((GridBagLayout) lm).addLayoutComponent(newOne, c);
      
    } else {
      // Layouts have no constraints. Just set the correct index.
      boolean replaced = false;
      for (int i = 0; i < target.getComponents().length; i++) {
        if (target.getComponent(i).equals(oldOne)) {
          target.remove(i);
          target.add(newOne, i);
          replaced = true;
          break;
        }
      }
      
      // element not found? still add the new one.
      if (!replaced) {
        target.remove(oldOne);
        target.add(newOne);
      }
      
    }
  }
  
  /**
   * Shows a generic Save file Dialog.
   * 
   * @param parentComp
   * @return null if no {@link File} has been selected for any reason or the
   *         selected {@link File}.
   */
  public static File saveFileDialog(Component parentComp) {
    return saveFileDialog(parentComp, System.getProperty("user.dir"), true,
      false, JFileChooser.FILES_ONLY);
  }
  
  /**
   * 
   * @param parent
   * @param dir
   * @param allFilesAcceptable
   * @param multiSelectionAllowed
   * @param checkFile
   *        decides whether to check if the file is writable and if existing
   *        files are to be overwritten. If false, no such check will be
   *        performed.
   * @param mode  e.g., JFileChooser.FILES_ONLY
   * @param filter
   * @return
   */
  public static File saveFileDialog(Component parent, String dir,
    boolean allFilesAcceptable, boolean multiSelectionAllowed,
    boolean checkFile, int mode, FileFilter... filter) {
    JFileChooser fc = createJFileChooser(dir, allFilesAcceptable,
      multiSelectionAllowed, mode, filter);
    if (fc.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
      File f = fc.getSelectedFile();
      if (f.exists()) {
        if (checkFile && !f.canWrite()) {
          showNowWritingAccessWarning(parent, f);
        } else if (f.isDirectory()
            || (checkFile && GUITools.overwriteExistingFile(parent, f))) {
          return f;
        } else if (!checkFile) { return f; }
      } else {
        return f;
      }
    }
    return null;
  }
  
  /**
   * @param parent
   * @param dir
   * @param allFilesAcceptable
   * @param multiSelectionAllowed
   * @param mode
   * @param filter
   * @return null if no {@link File} has been selected for any reason or the
   *         selected {@link File}.
   */
  public static File saveFileDialog(Component parent, String dir,
    boolean allFilesAcceptable, boolean multiSelectionAllowed, int mode,
    FileFilter... filter) {
    return saveFileDialog(parent, dir, allFilesAcceptable,
      multiSelectionAllowed, true, mode, filter);
  }
  
  /**
   * Searches recursively on "parent" and all components on "parent" for a component of
   * class "searchFor", with a Method (without inputs) called "methodName", that returns an
   * object that equals "retVal".
   * If "methodName" is null, this function will simply return the first instance of "searchFor"
   * on "parent".
   * @param parent
   * @param searchFor
   * @param methodName
   * @param retVal
   * @return the component, if found.
   */
  public static Component searchFor(Container parent, Class<?> searchFor, String methodName, Object retVal) {
    for (int i=0; i<parent.getComponentCount(); i++) {
      Component c = parent.getComponent(i);
      
      // Is c the component we are looking for?
      if (searchFor.isAssignableFrom(c.getClass())) {
        if (methodName!=null) {
          Object ret = Reflect.invokeIfContains(c, methodName);
          if (ret==null && retVal==null || ret.equals(retVal)) {
            return c;
          } else if (ret!=null && retVal!=null && ret instanceof String
              && ((String)ret).equalsIgnoreCase(retVal.toString())) {
            return c;
          }
        } else {
          return c;
        }
      }
      
      // Recurse further.
      if (c instanceof Container) {
        Component c2 = searchFor((Container)c, searchFor, methodName, retVal);
        if (c2!=null) return c2;
      }
    }
    return null;
  }
  
  /**
   * @param c
   * @param color
   */
  public static void setAllBackground(Container c, Color color) {
    c.setBackground(color);
    Component children[] = c.getComponents();
    for (int i = 0; i < children.length; i++) {
      if (children[i] instanceof Container)
        setAllBackground((Container) children[i], color);
      children[i].setBackground(color);
    }
  }
  
  /**
   * @param c
   * @param enabled
   */
  public static void setAllEnabled(Container c, boolean enabled) {
    Component children[] = c.getComponents();
    for (int i = 0; i < children.length; i++) {
      if (children[i] instanceof Container) {
        setAllEnabled((Container) children[i], enabled);
      }
      children[i].setEnabled(enabled);
    }
  }
  
  /**
   * Tries to recursively find instances of {@link AbstractButton} within the
   * given container and sets their enabled status to the given value.
   * 
   * @param state
   * @param c
   * @param command ActionCommand(s) of items to change the enabled state. May NOT be null!
   */
  public static void setEnabled(boolean state, Container c, Object... command) {
    Component inside;
    for (int i = 0; i < c.getComponentCount(); i++) {
      inside = c.getComponent(i);
      if (inside instanceof Container) {
        setEnabled(state, (Container) inside, command);
      } // Don't do "else if" here. JButtons are containers AND buttons itself! 
      if (inside instanceof AbstractButton) {
        String com = ((AbstractButton) inside).getActionCommand();
        if (com==null) continue;
        for (Object cmd: command) {
          if (com.toString().equals(cmd.toString())) {
            inside.setEnabled(state);
            continue;
          }
        }
      }
    }
  }
  
  /**
   * Enables or disables actions that can be performed by SBMLsqueezer, i.e.,
   * all menu items and buttons that are associated with the given actions are
   * enabled or disabled.
   * 
   * @param state
   *        if true buttons, items etc. are enabled, otherwise disabled.
   * @param menuBar
   * @param toolbar
   * @param commands
   */
  public static void setEnabled(boolean state, JMenuBar menuBar,
    JToolBar toolbar, Object... commands) {
    int i, j;
    Set<String> setOfCommands = new HashSet<String>();
    for (Object command : commands) {
      setOfCommands.add(command.toString());
    }
    if (menuBar != null) {
      for (i = 0; i < menuBar.getMenuCount(); i++) {
        JMenu menu = menuBar.getMenu(i);
        for (j = 0; j < menu.getItemCount(); j++) {
          JMenuItem item = menu.getItem(j);
          if (item instanceof JMenu) {
            JMenu m = (JMenu) item;
            boolean containsCommand = false;
            for (int k = 0; k < m.getItemCount(); k++) {
              JMenuItem it = m.getItem(k);
              if (it != null && it.getActionCommand() != null
                  && setOfCommands.contains(it.getActionCommand())) {
                it.setEnabled(state);
                containsCommand = true;
              }
            }
            if (containsCommand) m.setEnabled(state);
          }
          if ((item != null) && (item.getActionCommand() != null)
              && setOfCommands.contains(item.getActionCommand())) {
            item.setEnabled(state);
          }
        }
      }
    }
    if (toolbar != null) for (i = 0; i < toolbar.getComponentCount(); i++) {
      Object o = toolbar.getComponent(i);
      if (o instanceof JButton) {
        JButton b = (JButton) o;
        if (setOfCommands.contains(b.getActionCommand())) {
          b.setEnabled(state);
          // if (b.getIcon() != null
          // && b.getIcon() instanceof CloseIcon)
          // ((CloseIcon) b.getIcon())
          // .setColor(state ? Color.BLACK : Color.GRAY);
        }
      }
    }
  }
  
  /**
   * 
   * @param state
   * @param menuBar
   * @param command
   */
  public static void setEnabled(boolean state, JMenuBar menuBar, Object command) {
    setEnabled(state, menuBar, new Object[] {command});
  }
  
  /**
   * @param state
   * @param menuBar
   * @param commands
   */
  public static void setEnabled(boolean state, JMenuBar menuBar,
    Object... commands) {
    setEnabled(state, menuBar, null, commands);
  }
  
  /**
   * @param state
   * @param toolbar
   * @param commands
   */
  public static void setEnabled(boolean state, JToolBar toolbar,
    Object... commands) {
    setEnabled(state, null, toolbar, commands);
  }
  
  /**
   * Recursively set opaque to a value for p and all JComoponents on p.
   * @param p
   * @param val - false means transparent, true means object has a background.
   */
  public static void setOpaqueForAllElements(JComponent p, boolean val) {
    if (p instanceof JTextField || p instanceof JTextArea) return;
    p.setOpaque(val);
    
    for (Component c: p.getComponents()) {
      if (c instanceof JComponent) {
        setOpaqueForAllElements((JComponent) c, val);
      }
    }
    
  }
  
  /**
   * 
   * @param parent
   * @param message
   */
  public static void showErrorMessage(Component parent, String message) {
    showErrorMessage(parent, null, message);
  }
  
  /**
   * Displays the error message on a {@link JOptionPane}.
   * 
   * @param exc
   */
  public static void showErrorMessage(Component parent, Throwable exc) {
    String msg = getMessage(exc);
    
    if (logger!=null) logger.log(Level.WARNING, msg, exc);
    ValuePair<String, Integer> messagePair = StringUtil
    .insertLineBreaksAndCount(msg, TOOLTIP_LINE_LENGTH, "\n");
    Object message;
    if (messagePair.getB().intValue() > 30) {
      JEditorPane pane = new JEditorPane("text/html", messagePair.getA());
      pane.setEditable(false);
      pane.setPreferredSize(new Dimension(480, 240));
      message = new JScrollPane(pane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    } else {
      message = messagePair.getA();
    }
    Class<? extends Throwable> clazz = exc.getCause() != null ? exc.getCause()
        .getClass() : exc.getClass();
        JOptionPane.showMessageDialog(parent, message, clazz!=null?clazz.getSimpleName():"",
            JOptionPane.ERROR_MESSAGE);
  }


  /**
   * Return a best suited string to describe this {@link Throwable}.
   * Prefers in this order: <ol><li>LocalizedMessage
   * <li>LocalizedMessage of the Cause
   * <li>message<li>toString()<li>NULL</ol>
   * @param exc
   * @return
   */
  private static String getMessage(Throwable exc) {
    if (exc==null) return "NULL";
    String msg = exc.getLocalizedMessage();
    if ((msg == null) && (exc.getCause() != null)) {
      msg = exc.getCause().getLocalizedMessage();
    }
    if (msg == null) msg = exc.getMessage();
    if (msg == null) msg = exc.toString(); // Axis Faults have all messages null
    if (msg == null) msg = "NULL";
    return msg;
  }
  
  /**
   * Shows an error dialog with the given message in case the exception does not
   * provide any detailed message.
   * 
   * @param parent
   * @param exc
   * @param defaultMessage
   */
  public static void showErrorMessage(Component parent, Throwable exc, String defaultMessage) {
    if ((exc == null) || (exc.getLocalizedMessage() == null)
        || (exc.getLocalizedMessage().length() == 0)) {
      
      logger.log(Level.WARNING, defaultMessage, exc);
      String name = (exc!=null?exc.getClass().getSimpleName():"Error");
      JOptionPane.showMessageDialog(parent, defaultMessage, name, JOptionPane.ERROR_MESSAGE);
    } else {
      showErrorMessage(parent, exc);
    }
  }
  
  /**
   * Shows a simple message with a given title and an ok button.
   * 
   * @param message
   * @param title
   * @return
   */
  public static void showMessage(String message, String title) {
    JOptionPane.showMessageDialog(null, StringUtil.toHTML(message,
      TOOLTIP_LINE_LENGTH), title, JOptionPane.INFORMATION_MESSAGE);
  }
  
  
  /**
   * Displays a message on a message dialog window, i.e., an HTML document.
   * 
   * @param path
   *        the URL of an HTML document.
   * @param title
   *        the title of the dialog to be displayed
   * @param owner
   *        the parent of the dialog or null.
   */
  public static void showMessage(URL path, String title, Component owner) {
    showMessage(path, title, owner, null);
  }
  
  /**
   * Show a Question Dialog
   * @param parent may be null
   * @param message question to display
   * @param title dialog title
   * @param choices different choices
   * @return chosen index or JOptionPane static ints
   */
  public static int showQuestionMessage(Component parent, String message, String title, Object... choices) {
    return JOptionPane.showOptionDialog(parent, StringUtil.toHTML(message, TOOLTIP_LINE_LENGTH), 
      title, JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, choices, choices[0]);
  }
  
  /**
   * Show a Question Dialog
   * @param parent may be null
   * @param message question to display
   * @param title dialog title
   * @param optionType e.g. {@link JOptionPane#YES_NO_OPTION}
   * @return an integer indicating the option selected by the user (e.g. {@link JOptionPane#YES_OPTION})
   */
  public static int showQuestionMessage(Component parent, String message, String title, int optionType) {
    return JOptionPane.showConfirmDialog(parent, StringUtil.toHTML(message, TOOLTIP_LINE_LENGTH), 
      title, optionType, JOptionPane.QUESTION_MESSAGE);
  }
  
  /**
   * @param path
   * @param title
   * @param owner
   * @param icon
   */
  public static void showMessage(URL path, String title, Component owner,
    Icon icon) {
    JBrowserPane browser = new JBrowserPane(path);
    browser.removeHyperlinkListener(browser);
    browser.addHyperlinkListener(new SystemBrowser());
    browser.setBorder(BorderFactory.createEtchedBorder());
    
    try {
      File f = new File(OpenFile.doDownload(path.toString()));
      BufferedReader br;
      br = new BufferedReader(new FileReader(f));
      String line;
      int rowCount = 0, maxLine = 0;
      while (br.ready()) {
        line = br.readLine();
        if (line.length() > maxLine) {
          maxLine = line.length();
        }
        rowCount++;
      }
      
      if ((rowCount > 100) || (maxLine > 250)) {
        JScrollPane scroll = new JScrollPane(browser,
          JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
          JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        // TODO: Calculate required size using the font size.
        scroll.setMaximumSize(new Dimension(470, 470));
        Dimension prefered = new Dimension(450, 450);
        browser.setPreferredSize(prefered);
        JOptionPane.showMessageDialog(owner, scroll, title,
          JOptionPane.INFORMATION_MESSAGE, icon);
      } else {
        JOptionPane.showMessageDialog(owner, browser, title,
          JOptionPane.INFORMATION_MESSAGE, icon);
      }
    } catch (IOException exc) {
      showErrorMessage(owner, exc);
    }
  }
  
  
  /**
   * Show the component in an information message context, but does
   * not wait until the user clicks ok, but simply invokes this
   * message dialog in a new thread.
   * 
   * Simply executes {@link javax.swing.JOptionPane#showMessageDialog(Component, Object, String, int)}
   * in a new thread.
   * 
   * @param component - the component to show
   * @param caption - the caption of the dialog
   */
  public static void showMessageDialogInNewThred(final Component component, final String caption) {
    SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
      @Override
      protected Void doInBackground() throws Exception {
        JOptionPane.showMessageDialog(null, component,
          caption, JOptionPane.INFORMATION_MESSAGE);
        return null;
      }
    };
    worker.execute();
    
    // Wait until the dialog is painted, before continuing
    while (worker.getState()==SwingWorker.StateValue.PENDING) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {}
    }
    
  }
  
  
  /**
   * 
   * @param parent
   * @param file
   */
  public static void showNowReadingAccessWarning(Component parent, File file) {
    ResourceBundle resource = ResourceManager
    .getBundle(RESOURCE_LOCATION_FOR_LABELS);
    JOptionPane.showMessageDialog(parent, StringUtil.toHTML(String.format(
      resource.getString("NO_READ_ACCESS_MESSAGE"), resource.getString(file
        .isFile() ? "THE_FILE" : "THE_DIRECTORY"), file.getAbsolutePath()),
        TOOLTIP_LINE_LENGTH), resource.getString("NO_READ_ACCESS_TITLE"),
        JOptionPane.WARNING_MESSAGE);
  }
  
  /**
   * 
   * @param parent
   * @param file
   */
  public static void showNowWritingAccessWarning(Component parent, File file) {
    ResourceBundle resource = ResourceManager
    .getBundle(RESOURCE_LOCATION_FOR_LABELS);
    JOptionPane.showMessageDialog(parent, StringUtil.toHTML(String.format(
      resource.getString("NO_WRITE_ACCESS_MESSAGE"), resource.getString(file
        .isFile() ? "THE_FILE" : "THE_DIRECTORY"), file.getAbsolutePath()),
        TOOLTIP_LINE_LENGTH), resource.getString("NO_WRITE_ACCESS_TITLE"),
        JOptionPane.WARNING_MESSAGE);
  }
  
  /**
   * Shows the output of a process inside a textarea.
   * Autoscrolls as the process genereates output and
   * automatically disables the ok-button as long as
   * the process is running and enables it, as soon
   * as the process terminates.
   * 
   * Hint: Consider setting {@link ProcessBuilder#redirectErrorStream()}
   * to true.
   * 
   * @param p - the process to monitor.
   * @param caption - the caption of the window
   * @param closeWindowAutomaticallyWhenDone - if set to
   * true, the window will be closed, as soon as the
   * process finished.
   * @return JTextArea
   */
  public static JTextArea showProcessOutputInTextArea(final Process p, final String caption,
    final boolean closeWindowAutomaticallyWhenDone) {
    final JTextArea area = new JTextArea();
    SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
      @Override
      protected Void doInBackground() throws Exception {
        
        // Show a JTextArea in an information message in background
        area.setEditable(false);
        JScrollPane pane = new JScrollPane(
          area, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        pane.setPreferredSize(new Dimension(480, 240));
        
        GUITools.showMessageDialogInNewThred(pane, caption);
        
        // Disable the ok-Button of the area
        GUITools.disableOkButton(area);
        
        // Update the window, as the process is executing
        String line;
        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
        while ((line = in.readLine()) != null) {
          area.append(line+"\n");
          // Scroll down
          area.setCaretPosition(area.getDocument().getLength()); 
        }
        
        // As soon as the process is done, enable the ok button again
        Window w = getParentWindow(area);
        if (closeWindowAutomaticallyWhenDone) {
          if (w!=null) w.dispose();
        } else {
          enableOkButtonIfAllComponentsReady(w);
        }
        
        return null;
      }
    };
    worker.execute();
    
    // Wait until the dialog is painted, before continuing
    while (worker.getState()==SwingWorker.StateValue.PENDING) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {}
    }
    
    return area;
  }
  

  /**
   * Build a panel with cancel and ok buttons.
   * When any button is pressed, it will trigger setVisible(false).
   * <p>You can check if ok has been pressed with
   * <pre> ((JButton) ((JPanel) buttonPanel.getComponent(0)).getComponent(0)).isSelected()</pre>
   * @param parentDialog the dialog to close (hide) with ok and cancel.
   * @return
   */
  public static JPanel buildOkCancelButtons(final Component parentDialog) {
    JPanel southPanel = new JPanel(new BorderLayout());

    // Ok Button
    FlowLayout fr = new FlowLayout();
    fr.setAlignment(FlowLayout.RIGHT);
    JPanel se = new JPanel(fr);
    String text = GUITools.getOkButtonText();
    final JButton ok = new JButton(text);
    //if (this.defaultFont!=null) ok.setFont(defaultFont);
    se.add(ok);
    southPanel.add(se, BorderLayout.EAST);

    // Cancel Button
    FlowLayout fl = new FlowLayout();
    fl.setAlignment(FlowLayout.LEFT);
    JPanel sw = new JPanel(fl);
    text = GUITools.getCancelButtonText();
    final JButton cancel = new JButton(text);
    //if (this.defaultFont!=null) cancel.setFont(defaultFont);
    sw.add(cancel);
    southPanel.add(sw, BorderLayout.WEST);
    
    // Set common size
    ok.setPreferredSize(cancel.getPreferredSize()); // new Dimension(75,25)
    
    // Add listeners
    ok.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        ok.setSelected(true);
        cancel.setSelected(false);
        if (parentDialog!=null) parentDialog.setVisible(false);
      }      
    });
    cancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        ok.setSelected(false);
        cancel.setSelected(true);
        if (parentDialog!=null) parentDialog.setVisible(false);
      }      
    });
    
    return southPanel;
  }
  
  
  
  /**
   * Disabled or enables the given components.
   * @param enabled
   * @param components
   */
  public static void setEnabledForAll(boolean enabled, Component...components) {
    for (Component component: components) {
      component.setEnabled(enabled);
    }
  }
  
  /**
   * Show a {@link JPanel} as {@link Dialog}.
   * @param parent optional parent Frame or Dialog
   * @param c the panel to display
   * @param title a title that should be used for the window
   * @param addOkAndCancel if true, will add additional OK and CANCEL buttons
   * at the bottom of the dialog 
   * @return if ok has been pressed. More formarlly: either {@link JOptionPane#OK_OPTION} or
   * {@link JOptionPane#CANCEL_OPTION} or -2 if unknown (external closing, e.g., by your own
   * ok button).
   */
  public static int showAsDialog(Component parent, JPanel c, String title, final boolean addOkAndCancel) {

    // Initialize the dialog
    final JDialog jd;
    if (parent!=null && parent instanceof Frame) {
      jd = new JDialog((Frame)parent, title, true);
    } else if (parent!=null && parent instanceof Dialog) {
      jd = new JDialog((Dialog)parent, title, true);
    } else {
      jd = new JDialog();
      jd.setTitle(title);
      jd.setModal(true);
    }
    jd.setName(""); // The Name encodes the ok and cancel options!
    jd.setLayout(new BorderLayout());
    
    // Create ok and cancel buttons
    final JPanel okAndCancel;
    if (addOkAndCancel) {
      okAndCancel = buildOkCancelButtons(jd);
    } else { 
      okAndCancel = null;
    }
    
    // Initialize the panel
    jd.add(c, BorderLayout.CENTER);
    if (addOkAndCancel) jd.add(okAndCancel, BorderLayout.SOUTH);
    
    // Close dialog with ESC button.
    jd.getRootPane().registerKeyboardAction(new ActionListener() {
      public synchronized void actionPerformed(ActionEvent e) {
        // deselect the OK button
        if (addOkAndCancel) {
          ((JButton) ((JPanel) okAndCancel.getComponent(0)).getComponent(0)).setSelected(false);
        }
        jd.setName(Integer.toString(JOptionPane.CANCEL_OPTION));
        jd.dispose();
      }
    }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    // Close dialog with ENTER button.
    jd.getRootPane().registerKeyboardAction(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // select the OK button
        if (addOkAndCancel) {
          ((JButton) ((JPanel) okAndCancel.getComponent(0)).getComponent(0)).setSelected(true);
        }
        jd.setName(Integer.toString(JOptionPane.OK_OPTION));
        jd.dispose();
      }
    }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    
    // Set close operations
    jd.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        jd.setVisible(false);
      }
    });
    c.addComponentListener(new ComponentListener() {
      public void componentHidden(ComponentEvent e) {
        jd.setVisible(false);
      }
      public void componentMoved(ComponentEvent e) {}
      public void componentResized(ComponentEvent e) {}
      public void componentShown(ComponentEvent e) {}
    });
    
    // Set size
    jd.pack();
    jd.setLocationRelativeTo(parent);
    
    // Set visible and wait until invisible
    jd.setVisible(true);
    
    // Dispose and return reader.
    jd.dispose();
    
    // Get the button/closing method that closed the dialog
    int retVal;
    if (jd.getName()=="") {
      // Exit with a button
      if (addOkAndCancel) {
        // Button from this method
        if (((JButton) ((JPanel) okAndCancel.getComponent(0)).getComponent(0)).isSelected()) {
          retVal = JOptionPane.OK_OPTION;
        } else {
          retVal = JOptionPane.CANCEL_OPTION;
        }
      } else {
        // External closing...
        retVal = -2; // Unknown
      }
    } else {
      // Exit with either Enter or Escape key
      retVal = Integer.parseInt(jd.getName());
    }
    
    return retVal;
  }


  /**
   * Remove all elements from this container and all childs, that
   * have a name that is equal to the given one.
   * @param container
   * @param nameToRemove
   * @return true if at least one element has been removed.
   */
  public static boolean removeAllComponentsWithName(Container container, String nameToRemove) {
    boolean removed = false;
    
    for (Component c: container.getComponents()) {
      String name = c.getName();
      
      if (name!=null && name.equals(nameToRemove)) {
        container.remove(c);
        removed=true;
      } else if (c instanceof Container) {
        // Recurse
        removed|=removeAllComponentsWithName((Container) c,nameToRemove);
      }
    }
    return removed;
  }
  
  
}
