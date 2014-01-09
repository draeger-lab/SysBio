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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;


import de.zbit.util.prefs.KeyProvider;

/**
 * Automatically build an options panel for multiple {@link KeyProvider}s.
 * 
 * @author Clemens Wrzodek
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 */
public class PreferencesPanelForKeyProviders extends AbstractMultiplePreferencesPanel {
  
  /**
   * Generated serial version identifer.
   */
  private static final long serialVersionUID = -921160153764089395L;
  
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

}
