/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2015 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.gui.prefs;

import static de.zbit.util.Utils.getMessage;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import de.zbit.gui.GUITools;
import de.zbit.util.Reflect;
import de.zbit.util.ResourceManager;
import de.zbit.util.StringUtil;
import de.zbit.util.prefs.KeyProvider;
import de.zbit.util.prefs.Option;

/**
 * A {@link JPanel} containing a {@link JTabbedPane} with several options for
 * the configuration of the {@link Properties} in a GUI.
 * 
 * @author Andreas Dr&auml;ger
 * @author Clemens Wrzodek
 * @since 1.0 (originates from SBMLsqueezer 1.3)
 * @date 2009-09-22
 * @version $Rev$
 */
public class MultiplePreferencesPanel extends AbstractMultiplePreferencesPanel {
  
  /**
   * Load all available {@link PreferencesPanel}s. This array is constructed
   * by querying the current project and the calling project as well.
   * 
   * <p>Access this array by using {@link #getClasses()}!</p>
   */
  private static Class<PreferencesPanel>[] classes = null;
  
  /**
   * Determines, if the {@link #classes} array has been initialized.
   * (Remark: the array may be initialized but still be null!).
   */
  private static boolean isClassesInitialized = false;
  
  /**
   * A {@link Logger} for this class.
   */
  private static final transient Logger logger = Logger.getLogger(MultiplePreferencesPanel.class.getName());
  
  /**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = 3189416350182046246L;
  
  /**
   * @return the classes
   */
  public static Class<PreferencesPanel>[] getClasses() {
    /* Moved the classes initialization to this method. Else, it causes projects using
     * SysBio to have a ~5sec delay, even when they do not use this Class!!
     */
    if (!isClassesInitialized) {
      
      /**
       * It is now possible to predefine the classes, instead of using reflections.
       * Example:<pre>
       * package de.zbit.gui.prefs;
       * public class PreferencePanels {
       *   public static Class<?>[] getPreferencesClasses() {
       *     return new Class<?>[]{
       *         de.zbit.gui.prefs.GeneralOptionPanel.class,
       *         de.zbit.gui.prefs.LaTeXPrefPanel.class,
       *         de.zbit.gui.prefs.MultiplePreferencesPanel.class,
       *         de.zbit.gui.prefs.PreferencesPanelForKeyProvider.class,
       *         de.zbit.gui.prefs.TranslatorPanelOptionPanel.class
       *     };
       *   }
       * }
       * </pre>
       */
      
      // Try to get some predefined classes
      classes = getPredefinedPanels();
      
      // Fallback on reflection
      if (classes == null) {
        String pckName = MultiplePreferencesPanel.class.getPackage().getName();
        /*
         * Remark: user.dir might cause problems when using web start programs;
         * it might become something like C:\Programs\Mozilla Firefox.
         */
        classes = Reflect.getAllClassesInPackage(pckName, true, true, PreferencesPanel.class,
          System.getProperty("user.dir") + File.separatorChar, true);
        logger.finer(MessageFormat.format(
          "Used reflection to find preferences panels: {0}", Arrays
          .deepToString(classes)));
      }
      isClassesInitialized = true;
    }
    
    return classes;
  }
  
  /**
   * Loads a class in the same package as this class, called "PreferencePanels".
   * Looks if it exists and contains some predefined {@link PreferencesPanel}s
   * in a method called <pre>public static Class<?>[] getPreferencesClasses() {}</pre>
   * returns them if this is the case, else, null.
   * 
   * @return
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  protected static Class<PreferencesPanel>[] getPredefinedPanels() {
    String pckName = MultiplePreferencesPanel.class.getPackage().getName();
    String panelClass = pckName + ".PreferencePanels";
    Class<PreferencesPanel>[] foundPanelClasses = null;
    
    // If the panels have been predefined, load them instead of using reflections.
    Object o = null;
    try {
      // Try to get a defined 'panelClass'-class instance.
      Class<?> cl = Class.forName(panelClass);
      Constructor con = cl.getConstructor();
      o = con.newInstance();
    } catch (Exception e1) {
      // Mainly ClassNotFoundException
      logger.finer(MessageFormat.format(
        "No predefined preference panel found for {0}", pckName));
      logger.log(Level.FINEST, MessageFormat.format(
        "{0} not found.", panelClass), e1);
    }
    
    // If a predefined 'panelClass'-instance exits, load the classes array from it.
    if (o != null) {
      try {
        foundPanelClasses = (Class<PreferencesPanel>[]) Reflect.invokeIfContains(o,
            "getPreferencesClasses");
        if (foundPanelClasses != null) {
          logger.finer(MessageFormat.format(
            "Found preferences panels in {0}", panelClass));
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
    return foundPanelClasses;
  }
  
  /**
   * Counts how many tabs can be created inside of this
   * {@link MultiplePreferencesPanel} due to the number of
   * {@link PreferencesPanel} classes found in this package.
   * 
   * @return The number of tabs that will be automatically created and added to
   *         this {@link MultiplePreferencesPanel} when initializing.
   */
  public static int getPossibleTabCount() {
    int tabCount = 0;
    for (int i = 0; i < getClasses().length; i++) {
      if (!getClasses()[i].equals(MultiplePreferencesPanel.class)) {
        try {
          Class<PreferencesPanel> c = getClasses()[i];
          for (Constructor<?> constructor : c.getConstructors()) {
            if (constructor.getParameterTypes().length == 0) {
              tabCount++;
              break;
            }
          }
        } catch (Exception exc) {
          logger.finer(getMessage(exc));
        }
      }
    }
    return tabCount;
  };
  
  /**
   * With this {@link List} it is possible to let this
   * {@link MultiplePreferencesPanel} show only
   * {@link PreferencesPanelForKeyProvider}s defined by the given {@link Option}
   * s.
   */
  private Class<? extends KeyProvider>[] options;
  
  /**
   * Needed to structure this {@link PreferencesPanel}.
   */
  private JTabbedPane tab;
  
  /**
   * @param properties
   * @param defaultProperties
   * @throws IOException
   */
  public MultiplePreferencesPanel() throws IOException {
    super();
    setOpaque(true);
  }
  
  /**
   * 
   * @param kp
   * @throws IOException
   */
  public MultiplePreferencesPanel(Class<? extends KeyProvider>... kp)
      throws IOException {
    super(false);
    setOpaque(true);
    options = kp;
    initializePrefPanel();
  }
  
  /**
   * @return
   */
  public int getSelectedIndex() {
    return tab.getSelectedIndex();
  }
  
  /* (non-Javadoc)
   * @see de.zbit.gui.cfg.PreferencesPanel#getTitle()
   */
  @Override
  public String getTitle() {
    return ResourceManager.getBundle(
      StringUtil.RESOURCE_LOCATION_FOR_LABELS).getString("USER_PREFERENCES");
  }
  
  /* (non-Javadoc)
   * @see de.zbit.gui.cfg.PreferencesPanel#init()
   */
  @Override
  public void init() {
    tab = new JTabbedPane();
    tab.setOpaque(true);
    
    /* Initializes all preferences-tabs according to the following priority:
     * 1. explicitly predefined in a class called de.zbit.gui.prefs.PreferencePanels
     * 2. given as "options"
     * 3. with reflection
     */
    if ((options != null) && (getPredefinedPanels() == null)) {
      for (Class<? extends KeyProvider> provider : options) {
        try {
          addTab(new PreferencesPanelForKeyProvider(provider));
        } catch (IOException exc) {
          GUITools.showErrorMessage(this, exc);
        }
      }
    } else {
      for (int i = 0; i < getClasses().length; i++) {
        if (!getClasses()[i].equals(getClass())) {
          try {
            Class<PreferencesPanel> c = getClasses()[i];
            Constructor<PreferencesPanel> con = c.getConstructor();
            if (c != null) {
              addTab(con.newInstance());
            }
          } catch (NoSuchMethodException exc) {
            logger.finest(getMessage(exc));
          } catch (Exception exc) {
            GUITools.showErrorMessage(this, exc);
          }
        }
      }
    }
    add(tab);
    addItemListener(this);
  }
  
  /**
   * Helper method.
   * 
   * @param settingsPanel
   */
  private void addTab(PreferencesPanel settingsPanel) {
    settingsPanel.setOpaque(true);
    JScrollPane scroll = new JScrollPane(settingsPanel);
    scroll.setOpaque(true);
    tab.addTab(settingsPanel.getTitle(), scroll);
    addPreferencesPanel(settingsPanel);
  }
  
  /* (non-Javadoc)
   * @see javax.swing.JComponent#setPreferredSize(java.awt.Dimension)
   */
  @Override
  public void setPreferredSize(Dimension preferredSize) {
    super.setPreferredSize(preferredSize);
    // Make tab at least as big as this panel.
    if (tab != null) {
      Dimension p1 = tab.getPreferredSize();
      Dimension p2 = preferredSize;
      p1.width = Math.max(p1.width, p2.width);
      p1.height = Math.max(p1.height, p2.height);
      tab.setPreferredSize(p1);
    }
  }
  
  /**
   * Sets the selected {@link PreferencesPanel} to the given index.
   * 
   * @param tab
   *            The index of the {@link PreferencesPanel} to be selected.
   */
  public void setSelectedIndex(int tab) {
    this.tab.setSelectedIndex(tab);
  }
  
}
