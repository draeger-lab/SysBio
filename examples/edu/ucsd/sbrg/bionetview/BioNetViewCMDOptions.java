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
package edu.ucsd.sbrg.bionetview;

import java.io.File;
import java.util.ResourceBundle;

import de.zbit.util.ResourceManager;
import de.zbit.util.prefs.KeyProvider;
import de.zbit.util.prefs.Option;

/**
 * Options for the command line that are not usable in the graphical user
 * interface.
 * 
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 */
public interface BioNetViewCMDOptions extends KeyProvider {
  
  /**
   * Localization support.
   */
  public static ResourceBundle bundle = ResourceManager.getBundle(YGraphOptions.class.getPackage().getName() + ".Messages");
  
  /**
   * Specifies the SBML input file.
   */
  public static final Option<File> SBML_IN_FILE = new Option<File>("SBML_IN_FILE", File.class, bundle, null);
  
}
