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
package de.zbit;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;

import de.zbit.util.StringUtil;
import de.zbit.util.prefs.KeyProvider;
import de.zbit.util.prefs.Option;
import de.zbit.util.prefs.SBPreferences;
import de.zbit.util.prefs.SBProperties;

/**
 * This object encapsulates information about the configuration of a program,
 * i.e., an application.
 * 
 * @author Andreas Dr&auml;ger
 * @author Stephanie Tscherneck
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class AppConf implements Cloneable, Serializable {
  
  /**
   * A {@link Logger} for this class.
   */
  private static final transient Logger logger = Logger.getLogger(AppConf.class.getName());
  
  /**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = -2339428729026820588L;

  /**
   * The name of a program.
   */
  private String applicationName;
  
  /**
   * 
   */
  private SBProperties cmdArgs;
  
  /**
   * 
   */
  private Class<? extends KeyProvider>[] cmdOptions, interactiveOptions;
  
  /**
   * 
   */
  private URL licenceFile;
  
  /**
   * 
   */
  private URL onlineUpdate;
  
  /**
   * 
   */
  private String versionNumber;
  
  /**
   * 
   */
  private short yearOfProjectStart;
  
  /**
   * 
   */
  private short yearOfRelease;
  
  /**
   * 
   * @param pc
   */
  public AppConf(AppConf pc) {
    super();
    if (pc.getApplicationName() != null) {
      this.applicationName = new String(pc.getApplicationName());
    }
    if (pc.getCmdArgs() != null) {
      this.cmdArgs = pc.getCmdArgs().clone();
    }
    if (pc.getCmdOptions() != null) {
      this.cmdOptions = pc.getCmdOptions().clone();
    }
    if (pc.getInteractiveOptions() != null) {
      this.interactiveOptions = pc.getInteractiveOptions().clone();
    }
    try {
      if (pc.getLicenceFile() != null) {
        this.licenceFile = new URL(pc.getLicenceFile().toString());
      }
      if (pc.getOnlineUpdate() != null) {
        this.onlineUpdate = new URL(pc.getOnlineUpdate().toString());
      }
    } catch (MalformedURLException e) {
      // can never happen...
    }
    if (pc.getVersionNumber() != null) {
      this.versionNumber = new String(pc.getVersionNumber());
    }
    this.yearOfRelease = pc.getYearOfRelease();
    this.yearOfProjectStart = pc.getYearOfProjectStart();
  }
  
  /**
   * 
   * @param applicationName
   * @param versionNumber
   * @param yearOfRelease
   * @param yearOfProjectStart
   * @param cmdOptions
   * @param cmdArgs
   * @param interactiveOptions
   * @param licenseFile
   * @param onlineUpdate
   */
  public AppConf(String applicationName, String versionNumber,
    short yearOfRelease, short yearOfProjectStart, Class<? extends KeyProvider> cmdOptions[],
    SBProperties cmdArgs, Class<? extends KeyProvider> interactiveOptions[],
    URL licenseFile, URL onlineUpdate) {
    this(applicationName, versionNumber, yearOfRelease, yearOfProjectStart, cmdOptions, cmdArgs,
      licenseFile);
    this.interactiveOptions = interactiveOptions;
    this.onlineUpdate = onlineUpdate;
  }

  /**
   * 
   * @param applicationName
   * @param versionNumber
   * @param yearOfRelease
   * @param cmdOptions
   * @param cmdArgs
   * @param licenseFile
   */
  public AppConf(String applicationName, String versionNumber,
    short yearOfRelease, short yearOfProjectStart, Class<? extends KeyProvider> cmdOptions[],
    SBProperties cmdArgs, URL licenseFile) {
    super();
    this.applicationName = applicationName;
    this.versionNumber = versionNumber;
    this.yearOfRelease = yearOfRelease;
    this.yearOfProjectStart = yearOfProjectStart;
    this.cmdOptions = cmdOptions;
    this.cmdArgs = cmdArgs;
    this.licenceFile = licenseFile;
  }

  /**
   * 
   * @param applicationName
   * @param versionNumber
   * @param yearOfProgramRelease
   * @param yearOfProjectStart
   * @param commandLineOptions
   * @param commandLineArgs
   * @param interactiveOptions
   * @param urLlicenseFile
   * @param urlOnlineUpdate
   */
  @SuppressWarnings("unchecked")
  public AppConf(String applicationName, String versionNumber,
    short yearOfProgramRelease, short yearOfProjectStart,
    List<Class<? extends KeyProvider>> commandLineOptions,
    SBProperties commandLineArgs,
    List<Class<? extends KeyProvider>> interactiveOptions, URL urLlicenseFile,
    URL urlOnlineUpdate) {
    this(applicationName, versionNumber, yearOfProgramRelease, yearOfProjectStart,
      (Class<? extends KeyProvider>[]) (commandLineOptions != null ? commandLineOptions
          .toArray(new Class<?>[0]) : null),
      commandLineArgs,
      (Class<? extends KeyProvider>[]) (interactiveOptions != null ? interactiveOptions
          .toArray(new Class<?>[0]) : null), urLlicenseFile, urlOnlineUpdate);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#clone()
   */
  @Override
  protected Object clone() throws CloneNotSupportedException {
    return new AppConf(this);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    return obj.getClass().equals(getClass())
        && toMap().equals(((AppConf) obj).toMap());
  }

  /**
   * @return the applicationName
   */
  public String getApplicationName() {
    return applicationName;
  }

  /**
   * @return the cmdArgs
   */
  public SBProperties getCmdArgs() {
    return cmdArgs;
  }

  /**
   * @return the cmdOptions
   */
  public Class<? extends KeyProvider>[] getCmdOptions() {
    return cmdOptions;
  }

  /**
   * @return the interactiveOptions
   */
  public Class<? extends KeyProvider>[] getInteractiveOptions() {
    return interactiveOptions;
  }

  /**
   * @return the licenceFile
   */
  public URL getLicenceFile() {
    return licenceFile;
  }

  /**
   * @return the onlineUpdate
   */
  public URL getOnlineUpdate() {
    return onlineUpdate;
  }
  
  /**
   * @return the versionNumber
   */
  public String getVersionNumber() {
    return versionNumber;
  }

  /**
   * @return the year when the project started
   */
  public short getYearOfProjectStart() {
    return yearOfProjectStart;
  }
  
  /**
   * @return the yearOfRelease
   */
  public short getYearOfRelease() {
    return yearOfRelease;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final short prime = (short) 613;
    int hashCode = getClass().getName().hashCode();
    hashCode += cmdArgs != null ? prime * cmdArgs.hashCode() : 0;
    hashCode += cmdOptions != null ? prime * cmdOptions.hashCode() : 0;
    hashCode += interactiveOptions != null ? prime * interactiveOptions.hashCode() : 0;
    hashCode += licenceFile != null ? prime * licenceFile.hashCode() : 0;
    hashCode += onlineUpdate != null ? prime * onlineUpdate.hashCode() : 0;
    hashCode += applicationName != null ? prime * applicationName.hashCode() : 0;
    hashCode += versionNumber != null ? prime * versionNumber.hashCode() : 0;
    hashCode += prime * Short.valueOf(yearOfRelease).hashCode();
    hashCode += prime * Short.valueOf(yearOfProjectStart).hashCode();
    return hashCode;
  }

	/**
	 * If command line arguments are stored in this object, this method makes all
	 * those {@link Option}-value pairs belonging to the
	 * {@link #interactiveOptions} persistent in corresponding
	 * {@link SBPreferences}.
	 * 
	 * @throws BackingStoreException
	 */
	@SuppressWarnings("rawtypes")
	public void persistCmdArgs(Class<? extends KeyProvider>... keyProvider) throws BackingStoreException {
		if (keyProvider == null) {
			return;
		}
		SBProperties props = getCmdArgs();
		if (props.size() > 0) {
			for (Class<? extends KeyProvider> provider : keyProvider) {
				SBPreferences prefs = SBPreferences.getPreferencesFor(provider);
				Iterator<Option> options = KeyProvider.Tools.optionIterator(provider);
				Option<?> option;
				while (options.hasNext()) {
					option = options.next();
					if (props.containsKey(option)) {
						prefs.put(option, props.get(option));
					}
				}
				prefs.flush();
			}
		}
	}

  /**
   * Creates and returns a {@link Map} representation of all fields in this
   * {@link AppConf}.
   * 
   * @return
   */
  public Map<String, Object> toMap() {
    Map<String, Object> map = new Hashtable<String, Object>();
    for (Field f : getClass().getDeclaredFields()) {
      if (!Modifier.isStatic(f.getModifiers())) {
        try {
          map.put(StringUtil.createKeyFromField(f.getName()), f.get(this));
        } catch (IllegalArgumentException exc) {
          logger.log(Level.FINE, exc.getLocalizedMessage(), exc);
        } catch (IllegalAccessException exc) {
          logger.log(Level.FINE, exc.getLocalizedMessage(), exc);
        }
      }
    }
    return map;
  }

	/* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return toMap().toString();
  }
  
}
