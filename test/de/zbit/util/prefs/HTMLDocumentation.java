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
package de.zbit.util.prefs;

import java.util.List;

import de.zbit.Launcher;


/**
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 * @since 1.0
 */
public class HTMLDocumentation {
	
	/**
	 * @param args
	 * @throws ClassNotFoundException 
	 */
  @SuppressWarnings("unchecked")
  public static void main(String[] args) throws ClassNotFoundException {
    Class<?> providers[] = new Class<?>[args.length];
    for (int i = 0; i < args.length; i++) {
      providers[i] = Class.forName(args[i]);
    }
    writeDocumentation((Class<? extends KeyProvider>[]) providers);
  }
  
  /**
   * 
   * @param launcher
   */
  @SuppressWarnings("unchecked")
  public static void writeDocumentation(Launcher launcher) {
    List<Class<? extends KeyProvider>> opts = launcher.getCmdLineOptions();
    Class<?> providers[] = new Class<?>[opts.size()];
    for (int i = 0; i < opts.size(); i++) {
      providers[i] = opts.get(i);
    }
    writeDocumentation((Class<? extends KeyProvider>[]) providers);
  }
  
  /**
   * 
   * @param providers
   */
  public static void writeDocumentation(
    Class<? extends KeyProvider>... providers) {
    System.out.println(KeyProvider.Tools.createDocumentation(providers));
  }
	
}
