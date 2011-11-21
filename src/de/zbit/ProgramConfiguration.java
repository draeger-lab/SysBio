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
package de.zbit;

import java.net.URL;
import java.util.logging.Level;

import de.zbit.util.prefs.KeyProvider;

/**
 * @author Andreas Dr&auml;ger
 * @author Stephanie Tscherneck
 * @version $Rev$
 */
public class ProgramConfiguration {
  
  /**
   * 
   */
  private Class<KeyProvider>[] cmdOptions, interactiveOptions;
  
  /**
   * 
   */
  private URL licenceFile;
  
  /**
   * 
   */
  private Level logLevel;

  /**
   * 
   */
  private String logPackages[];
  
  /**
   * 
   */
  private URL onlineUpdate;
  
  /**
   * The name of a program.
   */
  private String programName;
  
  /**
   * 
   */
  private String versionNumber;
  
  /**
   * 
   */
  private short yearOfRelease;
  
  /**
   * 
   */
  public ProgramConfiguration(Class<KeyProvider> cmdOptions[]) {
    super();
    this.cmdOptions = cmdOptions;
  }

  /**
   * @return the cmdOptions
   */
  public Class<KeyProvider>[] getCmdOptions() {
    return cmdOptions;
  }

  /**
   * @return the interactiveOptions
   */
  public Class<KeyProvider>[] getInteractiveOptions() {
    return interactiveOptions;
  }

  /**
   * @return the licenceFile
   */
  public URL getLicenceFile() {
    return licenceFile;
  }

  /**
   * @return the logLevel
   */
  public Level getLogLevel() {
    return logLevel;
  }

  /**
   * @return the logPackages
   */
  public String[] getLogPackages() {
    return logPackages;
  }

  /**
   * @return the onlineUpdate
   */
  public URL getOnlineUpdate() {
    return onlineUpdate;
  }

  /**
   * @return the programName
   */
  public String getProgramName() {
    return programName;
  }

  /**
   * @return the versionNumber
   */
  public String getVersionNumber() {
    return versionNumber;
  }

  /**
   * @return the yearOfRelease
   */
  public short getYearOfRelease() {
    return yearOfRelease;
  }
  
}
