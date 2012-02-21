/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2012 by the University of Tuebingen, Germany.
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

import de.zbit.util.prefs.KeyProvider;
import de.zbit.util.prefs.SBPreferences;

/**
 * Automatically build an options panel.
 * 
 * @author Clemens Wrzodek
 * @version $Rev$
 * @since 1.0
 */
public class PreferencesPanelForKeyProvider extends PreferencesPanel {
	
	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = -3293205475880841303L;
	
	/**
	 * The KeyProvider, that determines this panel.
	 */
	protected Class<? extends KeyProvider> provider;
	
  /**
   * Title of this panel. Will be used as tab-title.
   */
  private String title;
  
  /**
   * @throws IOException
   */
  public PreferencesPanelForKeyProvider(Class<? extends KeyProvider> provider) throws IOException {
    this(null, provider);
  }
  
  /**
   * 
   * @param title
   * @param provider
   * @throws IOException
   */
  public PreferencesPanelForKeyProvider(String title, Class<? extends KeyProvider> provider) throws IOException {
    super(false); // calls init, before provider is set => many null-pointer-exceptions.
    if (title == null) {
    	title = KeyProvider.Tools.createTitle(provider);
    }
    this.title = title;
    this.provider = provider;
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
	  try {
      return (provider.getField(key.toString())!=null);
    } catch (SecurityException e) {
      return false;
    } catch (NoSuchFieldException e) {
      return false;
    }
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
	public void init() {
		if (provider != null) {
			autoBuildPanel();
		}
	}
	
	/* (non-Javadoc)
	 * @see de.zbit.gui.prefs.PreferencesPanel#loadPreferences()
	 */
	protected SBPreferences loadPreferences() throws IOException {
		return provider == null ? null : SBPreferences.getPreferencesFor(provider);
	}
	
}
