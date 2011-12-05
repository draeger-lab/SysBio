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
package de.zbit.gui.prefs;

import java.awt.Dimension;
import java.awt.event.ItemListener;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeListener;

import de.zbit.gui.GUITools;
import de.zbit.util.Reflect;
import de.zbit.util.ResourceManager;
import de.zbit.util.prefs.KeyProvider;
import de.zbit.util.prefs.Option;
import de.zbit.util.prefs.SBPreferences;

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
public class MultiplePreferencesPanel extends PreferencesPanel {

	/**
	 * Load all available {@link PreferencesPanel}s. This array is constructed
	 * by querying the current project and the calling project as well.
	 * 
	 * <p>Access this array by using {@link #getClasses()}!</p>
	 */
	private static Class<PreferencesPanel>[] classes = null;
	
	/**
	 * With this {@link List} it is possible to let this
	 * {@link MultiplePreferencesPanel} show only
	 * {@link PreferencesPanelForKeyProvider}s defined by the given {@link Option}
	 * s.
	 */
	private Class<? extends KeyProvider>[] options;
	
	/**
	 * Determines, if the {@link #classes} array has been initialized.
	 * (Remark: the array may be initialized but still be null!).
	 */
	private static boolean isClassesInitialized = false;

	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = 3189416350182046246L;
	
	/**
	 * 
	 */
	private static final transient Logger logger = Logger.getLogger(MultiplePreferencesPanel.class.getName());

	/**
	 * @return the classes
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
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
	    
	    String pckName = MultiplePreferencesPanel.class.getPackage().getName();
	    String panelClass = pckName + ".PreferencePanels";
	    
	    // If the panels have been predefined, load them instead of using reflections.
	    Object o = null;
      try {
        // Try to get a defined 'panelClass'-class instance.
        Class<?> cl = Class.forName(panelClass);
        Constructor con = cl.getConstructor();
        o = con.newInstance();
      } catch (Exception e1) {
        // Mainly ClassNotFoundException
        logger.finer(String.format(
          "%s not found. Using reflections to get preferences panels.",
          panelClass));
      }
      // If a predefined 'panelClass'-instance exits, load the classes array from it.
	    if (o != null) {
	      try {
          classes = (Class<PreferencesPanel>[]) Reflect.invokeIfContains(o,
            "getPreferencesClasses");
          if (classes != null) {
            logger.finer(String.format("Found preferences panels in %s",
              panelClass));
          }
	      } catch (Exception e) {
	        e.printStackTrace();
	      }
	    }
	    
	    if (classes == null) {
        /*
         * Remark: user.dir might cause problems when using web start programs;
         * it might become something like C:\Programs\Mozilla Firefox.
         */
	      classes = Reflect.getAllClassesInPackage(pckName, true, true, PreferencesPanel.class,
	        System.getProperty("user.dir") + File.separatorChar, true);
        logger.finer(String.format(
          "Used reflection to find preferences panels: %s", Arrays
              .deepToString(classes)));
	    }
	    isClassesInitialized = true;
	  }
	  
		return classes;
	};

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
					logger.finer(exc.getLocalizedMessage());
				}
			}
		}
		return tabCount;
	}

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
	}

  /**
   * 
   * @param kp
   * @throws IOException
   */
  public MultiplePreferencesPanel(Class<? extends KeyProvider>... kp)
    throws IOException {
    super(false);
    this.options = kp;
    initializePrefPanel();
  }

  /*
	 * (non-Javadoc)
	 * 
	 * @see de.zbit.gui.cfg.PreferencesPanel#accepts(java.lang.Object)
	 */
	public boolean accepts(Object key) {
		return false;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.zbit.gui.cfg.PreferencesPanel#addChangeListener(javax.swing.event.
	 * ChangeListener)
	 */
	@Override
	public void addChangeListener(ChangeListener listener) {
		for (int i = 0; i < tab.getTabCount(); i++) {
			getPreferencesPanel(i).addChangeListener(listener);
		}
	}

	/* (non-Javadoc)
	 * @see
	 * de.zbit.gui.cfg.PreferencesPanel#addItemListener(java.awt.event.ItemListener)
	 */
	@Override
	public void addItemListener(ItemListener listener) {
		for (int i = 0; i < tab.getTabCount(); i++) {
			getPreferencesPanel(i).addItemListener(listener);
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.Component#addKeyListener(java.awt.event.KeyListener)
	 */
	@Override
	public void addKeyListener(KeyListener listener) {
		for (int i = 0; i < tab.getTabCount(); i++) {
			getPreferencesPanel(i).addKeyListener(listener);
		}
	}

	/**
	 * @param index
	 * @return
	 */
	public PreferencesPanel getPreferencesPanel(int index) {
		return (PreferencesPanel) ((JScrollPane) tab.getComponentAt(index))
				.getViewport().getComponent(0);
	}

	/**
	 * Gives the number of {@link PreferencesPanel}s displayed on this element.
	 * 
	 * @return 0 if there is no panel or if this element has not yet been
	 *         initialized properly.
	 */
	public int getPreferencesPanelCount() {
		return tab == null ? 0 : tab.getTabCount();
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
	public String getTitle() {
		return ResourceManager.getBundle(GUITools.RESOURCE_LOCATION_FOR_LABELS)
				.getString("USER_PREFERENCES");
	}
	
	/* (non-Javadoc)
	 * @see de.zbit.gui.cfg.PreferencesPanel#init()
	 */
	public void init() {
		tab = new JTabbedPane();
		PreferencesPanel settingsPanel;
		if (options != null) {
			for (Class<? extends KeyProvider> provider : options) {
				try {
					settingsPanel = new PreferencesPanelForKeyProvider(provider);
					tab.addTab(settingsPanel.getTitle(), new JScrollPane(settingsPanel));
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
							settingsPanel = con.newInstance();
							tab.addTab(settingsPanel.getTitle(), new JScrollPane(
								settingsPanel));
						}
					} catch (NoSuchMethodException exc) {
						logger.finest(exc.getLocalizedMessage());
					} catch (Exception exc) {
						GUITools.showErrorMessage(this, exc);
					}
				}
			}
		}
		this.add(tab);
		addItemListener(this);
	}

	/* (non-Javadoc)
	 * @see de.zbit.gui.cfg.PreferencesPanel#isDefaultConfiguration()
	 */
	@Override
	public boolean isDefaultConfiguration() {
		for (int i = 0; i < getPreferencesPanelCount(); i++) {
			if (!getPreferencesPanel(i).isDefaultConfiguration()) {
				return false;
			}
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see de.zbit.gui.cfg.PreferencesPanel#isUserConfiguration()
	 */
	@Override
	public boolean isUserConfiguration() {
		for (int i = 0; i < getPreferencesPanelCount(); i++) {
			if (!getPreferencesPanel(i).isUserConfiguration()) {
				return false;
			}
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see de.zbit.gui.cfg.PreferencesPanel#loadPreferences()
	 */
	protected SBPreferences loadPreferences() throws IOException {
		// This class only gathers other preferences panels.
		// It therefore does not have any own preferences.
		return null;
	}

	/* (non-Javadoc)
	 * @see de.zbit.gui.cfg.PreferencesPanel#persist()
	 */
	@Override
	public void persist() throws BackingStoreException {
		for (int i = 0; i < tab.getComponentCount(); i++) {
			getPreferencesPanel(i).persist();
		}
	}

	/* (non-Javadoc)
	 * @see de.zbit.gui.cfg.PreferencesPanel#restoreDefaults()
	 */
	@Override
	public void restoreDefaults() {
		for (int i = 0; i < tab.getComponentCount(); i++) {
			getPreferencesPanel(i).restoreDefaults();
		}
		validate();
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
