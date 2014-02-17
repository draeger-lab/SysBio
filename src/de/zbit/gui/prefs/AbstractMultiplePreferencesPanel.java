/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2014 by the University of Tuebingen, Germany.
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

import java.awt.event.ItemListener;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.PreferenceChangeListener;

import javax.swing.event.ChangeListener;

import de.zbit.gui.layout.LayoutHelper;
import de.zbit.util.prefs.KeyProvider;
import de.zbit.util.prefs.SBPreferences;

/**
 * 
 * @author Andreas Dr&auml;ger
 * @since 1.1
 * @version $Rev$
 */
public abstract class AbstractMultiplePreferencesPanel extends PreferencesPanel {
  
  /**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = 8781298411082354285L;
  
  /**
   * A {@link Logger} for this class.
   */
  private static final Logger logger = Logger.getLogger(PreferencesPanelForKeyProviders.class.getName());
  /**
   * The KeyProvider, that determines this panel.
   */
  protected List<Class<? extends KeyProvider>> providers;
  /**
   * Title of this panel. Will be used as tab-title.
   */
  protected String title;
  /**
   * 
   */
  private LayoutHelper layoutHelper;
  /**
   * 
   */
  private List<PreferencesPanel> listOfPanels = new LinkedList<PreferencesPanel>();
  
  /**
   * 
   * @throws IOException
   */
  public AbstractMultiplePreferencesPanel() throws IOException {
    super();
  }
  
  /**
   * 
   * @param initPanel
   * @throws IOException
   */
  public AbstractMultiplePreferencesPanel(boolean initPanel) throws IOException {
    this(initPanel, null);
  }
  
  /**
   * 
   * @param initPanel
   * @param ignore
   * @throws IOException
   */
  public AbstractMultiplePreferencesPanel(boolean initPanel, Set<String> ignore) throws IOException {
    super(initPanel);
    ignoreOptions = ignore;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.gui.prefs.PreferencesPanel#accepts(java.lang.Object)
   */
  @Override
  public boolean accepts(Object key) {
    //return preferences.keySetFull().contains(key);
    // Preferences keyset contains all options from the package.
    // Better read fields of actual key-provider. These are the
    // Keys we want to have in the properties!!
    if (providers != null) {
      for (Class<? extends KeyProvider> provider: providers) {
        try {
          if (KeyProvider.Tools.providesOption(provider, key.toString())) {
            return true;
          }
        } catch (SecurityException e) {
          logger.log(Level.FINER, e.getMessage(), e);
        }
      }
    }
    return false;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.gui.prefs.PreferencesPanel#getTitle()
   */
  @Override
  public String getTitle() {
    return title;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.gui.prefs.PreferencesPanel#isDefaultConfiguration()
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
   * @see de.zbit.gui.prefs.PreferencesPanel#isUserConfiguration()
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
   * @see de.zbit.gui.prefs.PreferencesPanel#persist()
   */
  @Override
  public void persist() throws BackingStoreException {
    for (int i = 0; i < getPreferencesPanelCount(); i++) {
      getPreferencesPanel(i).persist();
    }
  }
  
  /* (non-Javadoc)
   * @see de.zbit.gui.prefs.PreferencesPanel#init()
   */
  @Override
  public void init() {
    layoutHelper = new LayoutHelper(this);
    if ((providers != null) && (providers.size() > 0)) {
      for (Class<? extends KeyProvider> provider : providers) {
        try {
          PreferencesPanelForKeyProvider settingsPanel = new PreferencesPanelForKeyProvider(provider, ignoreOptions);
          addPreferencesPanel(settingsPanel);
          settingsPanel.setOpaque(true);
          layoutHelper.add(settingsPanel);
        } catch (IOException exc) {
          logger.fine(exc.getLocalizedMessage());
        }
      }
    }
    addItemListener(this);
  }
  
  /* (non-Javadoc)
   * @see de.zbit.gui.prefs.PreferencesPanel#addChangeListener(javax.swing.event.ChangeListener)
   */
  @Override
  public void addChangeListener(ChangeListener listener) {
    for (int i = 0; i < getPreferencesPanelCount(); i++) {
      getPreferencesPanel(i).addChangeListener(listener);
    }
  }
  
  /* (non-Javadoc)
   * @see de.zbit.gui.prefs.PreferencesPanel#addItemListener(java.awt.event.ItemListener)
   */
  @Override
  public void addItemListener(ItemListener listener) {
    for (int i = 0; i < getPreferencesPanelCount(); i++) {
      getPreferencesPanel(i).addItemListener(listener);
    }
  }
  
  /* (non-Javadoc)
   * @see java.awt.Component#addKeyListener(java.awt.event.KeyListener)
   */
  @Override
  public void addKeyListener(KeyListener listener) {
    for (int i = 0; i < getPreferencesPanelCount(); i++) {
      getPreferencesPanel(i).addKeyListener(listener);
    }
  }
  
  /**
   * Register the given panel as a sub-component of this composed element.
   * 
   * @param settingsPanel
   */
  protected void addPreferencesPanel(PreferencesPanel settingsPanel) {
    listOfPanels.add(settingsPanel);
  }
  
  /* (non-Javadoc)
   * @see de.zbit.gui.prefs.PreferencesPanel#addPreferenceChangeListener(java.util.prefs.PreferenceChangeListener)
   */
  @Override
  public void addPreferenceChangeListener(PreferenceChangeListener listener) {
    for (int i = 0; i < getPreferencesPanelCount(); i++) {
      getPreferencesPanel(i).addPreferenceChangeListener(listener);
    }
  }
  
  /**
   * @param index
   * @return
   */
  public PreferencesPanel getPreferencesPanel(int index) {
    return listOfPanels.get(index);
  }
  
  /**
   * Gives the number of {@link PreferencesPanel}s displayed on this element.
   * 
   * @return 0 if there is no panel or if this element has not yet been
   *         initialized properly.
   */
  public int getPreferencesPanelCount() {
    return listOfPanels.size();
  }
  
  /* (non-Javadoc)
   * @see de.zbit.gui.prefs.PreferencesPanel#removeChangeListener(javax.swing.event.ChangeListener)
   */
  @Override
  public boolean removeChangeListener(ChangeListener listener) {
    boolean removed = false;
    for (int i = 0; i < getPreferencesPanelCount(); i++) {
      removed |= getPreferencesPanel(i).removeChangeListener(listener);
    }
    return removed;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.gui.prefs.PreferencesPanel#removeItemListener(java.awt.event.ItemListener)
   */
  @Override
  public boolean removeItemListener(ItemListener listener) {
    boolean removed = false;
    for (int i = 0; i < getPreferencesPanelCount(); i++) {
      removed |= getPreferencesPanel(i).removeItemListener(listener);
    }
    return removed;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.gui.prefs.PreferencesPanel#removePreferenceChangeLisener(java.util.prefs.PreferenceChangeListener)
   */
  @Override
  public void removePreferenceChangeLisener(PreferenceChangeListener listener) {
    for (int i = 0; i < getPreferencesPanelCount(); i++) {
      getPreferencesPanel(i).removePreferenceChangeLisener(listener);
    }
  }
  
  /* (non-Javadoc)
   * @see de.zbit.gui.prefs.PreferencesPanel#restoreDefaults()
   */
  @Override
  public void restoreDefaults() {
    for (int i = 0; i < getPreferencesPanelCount(); i++) {
      getPreferencesPanel(i).restoreDefaults();
    }
    validate();
  }
  
  /* (non-Javadoc)
   * @see de.zbit.gui.prefs.PreferencesPanel#loadPreferences()
   */
  @Override
  protected SBPreferences loadPreferences() throws IOException {
    // This class only gathers other preferences panels.
    // It therefore does not have any own preferences.
    return null;
  }
  
}
