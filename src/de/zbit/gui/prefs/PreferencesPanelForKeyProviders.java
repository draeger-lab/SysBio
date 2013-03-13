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
package de.zbit.gui.prefs;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;

import de.zbit.util.prefs.KeyProvider;
import de.zbit.util.prefs.SBPreferences;
import de.zbit.util.prefs.SBProperties;

/**
 * Automatically build an options panel for multiple {@link KeyProvider}s.
 * 
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class PreferencesPanelForKeyProviders extends PreferencesPanel {
  
  /**
   * Generated serial version identifer.
   */
  private static final long serialVersionUID = -921160153764089395L;
  
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
  private String title;

  /**
   * 
   * @param provider
   * @throws IOException
   */
  public PreferencesPanelForKeyProviders(Class<? extends KeyProvider>... provider) throws IOException {
    this(Arrays.asList(provider));
  }
  
  /**
   * 
   * @param provider
   * @throws IOException
   */
  public PreferencesPanelForKeyProviders(List<Class<? extends KeyProvider>> provider) throws IOException {
    this(null, provider);
  }
  
  /**
   * 
   * @param title
   * @param provider
   * @throws IOException
   */
  public PreferencesPanelForKeyProviders(String title, Class<? extends KeyProvider>... provider) throws IOException {
    this(title, Arrays.asList(provider));
  }
  
  /**
   * 
   * @param title
   * @param provider
   * @throws IOException
   */
  public PreferencesPanelForKeyProviders(String title, List<Class<? extends KeyProvider>> provider) throws IOException {
    super(false); // calls init, before provider is set => many null-pointer-exceptions.
    this.providers = provider;
    if (title == null) {
      title = KeyProvider.Tools.createTitle(providers.get(0));
    }
    this.title = title;
    initializePrefPanel();
  }

  /* (non-Javadoc)
   * @see de.zbit.gui.prefs.PreferencesPanel#accepts(java.lang.Object)
   */
  public boolean accepts(Object key) {
    //return preferences.keySetFull().contains(key);
    // Preferences keyset contains all options from the package.
    // Better read fields of actual key-provider. These are the
    // Keys we want to have in the properties!!
    
    for (Class<? extends KeyProvider> provider: providers) {
      try {
        boolean b = (provider.getField(key.toString())!=null);
        if (b) return true;
      } catch (SecurityException e) {
      } catch (NoSuchFieldException e) {}
    }
    return false;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.gui.prefs.PreferencesPanel#getTitle()
   */
  public String getTitle() {
    return title;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.gui.prefs.PreferencesPanel#init()
   */
  @SuppressWarnings("unchecked")
  public void init() {
    if ((providers != null) && (providers.size() > 0)) {
      autoBuildPanel((Class<? extends KeyProvider>[]) providers.toArray(new Class[0]));
    }
  }
  
  /* (non-Javadoc)
   * @see de.zbit.gui.prefs.PreferencesPanel#initializePrefPanel()
   */
  protected void initializePrefPanel() throws IOException {
  	String k;
    properties = new SBProperties(new SBProperties());
    for (Class<? extends KeyProvider> provider: providers) {
    	preferences = SBPreferences.getPreferencesFor(provider);
    	// TODO: In cases that there is an old configuration of preferences in memory it might be necessary to call SBPreferences.getPreferencesFor(myKeyProvider).flush() before all options can be displayed. The reason is that here all keys are taken from the preferences, instead using the actual options from the keyprovider!
    	for (Object key : preferences.keySetFull()) {
    		if (accepts(key)) {
    			// Accept only key from KeyProvider!
    			// Don't put all keys in properties (also not in preferences) here!
    			k = key.toString();
    			properties.put(k, preferences.get(key));
    			properties.getDefaults().put(k, preferences.getDefault(key));
    		} else {
    			log.finer(MessageFormat.format("Rejecting key: {0}", key));
    		}
    	}
    }
    preferences = null;
    init();
  }
  
  /* (non-Javadoc)
   * @see de.zbit.gui.prefs.PreferencesPanel#persist()
   */
  @Override
  public void persist() throws BackingStoreException {
    SBPreferences prefs = null;
    for (Class<? extends KeyProvider> provider: providers) {
      prefs = SBPreferences.getPreferencesFor(provider);
      
      boolean changes = false;
      for (Map.Entry<? extends Object, ? extends Object> entry : properties.entrySet()) {
        if (prefs.containsKey(entry.getKey()) || KeyProvider.Tools.providesOption(provider, entry.getKey().toString())) {
        	// TODO: this is a very simple check. What if we have a new key-value pair? Null values??
          prefs.put(entry.getKey(), entry.getValue());
          changes = true;
          logger.finest("Storing key-value pair (" + entry.getKey() + ", " + entry.getValue() + ") for KeyProvider " + provider.getName());
        } else {
        	logger.finest("Could not store key-value pair (" + entry.getKey() + ", " + entry.getValue() + ") for KeyProvider " + provider.getName());
        }
      }
      
      if (changes) {
        prefs.flush();
      }
    }
  }
  
  /* (non-Javadoc)
   * @see de.zbit.gui.prefs.PreferencesPanel#loadPreferences()
   */
  protected SBPreferences loadPreferences() throws IOException {
    return null;
  }

}
