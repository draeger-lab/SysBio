/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2016 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.garuda;

import java.awt.Component;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.swing.JFrame;

import jp.sbi.garuda.client.backend.BackendAlreadyInitializedException;
import jp.sbi.garuda.client.backend.BackendNotInitializedException;
import jp.sbi.garuda.client.backend.GarudaClientBackend;
import jp.sbi.garuda.client.backend.listeners.GarudaBackendPropertyChangeSupport;
import jp.sbi.garuda.client.backend.ui.GarudaControlPanelFactory;
import jp.sbi.garuda.client.backend.ui.GarudaGlassPanel;
import jp.sbi.garuda.platform.commons.Gadget;
import jp.sbi.garuda.platform.commons.exception.NetworkException;
import jp.sbi.garuda.platform.commons.net.GarudaConnectionNotInitializedException;
import de.zbit.UserInterface;
import de.zbit.gui.GUITools;
import de.zbit.io.FileTools;
import de.zbit.util.ResourceManager;

/**
 * An easily usable backend for Garuda support.
 * 
 * @author Andreas Dr&auml;ger
 * @date 17:42:09
 * @since 1.1
 * @version $Rev$
 */
public class GarudaSoftwareBackend {
  
  public static final String GARUDA_ACTIVATED = "de.zbit.garuda.GarudaSoftwareBackend.garudaActivated";
  public static final String SOFTWARE_REGISTERED_ERROR = "Protocol Error: SoftwareRegistered";
  
  /**
   * The actual backend.
   */
  private GarudaClientBackend backend;
  
  /**
   * A {@link Logger} for this class.
   */
  private static final Logger logger = Logger.getLogger(GarudaSoftwareBackend.class.getName());
  
  /**
   * Localization support.
   */
  private static final ResourceBundle bundle = ResourceManager.getBundle("de.zbit.garuda.locales.Labels");
  
  /**
   * 
   */
  private UserInterface parent;
  
  /**
   * 
   */
  private GarudaBackendPropertyChangeSupport pcs;
  /**
   * 
   */
  private Gadget gadget;
  
  /**
   * Creates a connection to the Garuda core.
   * 
   * @param uid
   *        a unique identifier for your gadget
   * @param parent
   *        the user interface of the gadget
   */
  public GarudaSoftwareBackend(String uid, UserInterface parent) {
    gadget = new Gadget();
    gadget.setID(uid);
    gadget.setName(parent.getApplicationName());
    
    this.parent = parent;
    pcs = new GarudaBackendPropertyChangeSupport(this.parent);
    
    addPropertyChangeListener(this.parent);
  }
  
  /**
   * 
   * @param pcl
   */
  public void addPropertyChangeListener(PropertyChangeListener pcl) {
    pcs.addPropertyChangeListener(pcl);
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof GarudaSoftwareBackend)) {
      return false;
    }
    GarudaSoftwareBackend other = (GarudaSoftwareBackend) obj;
    if (parent == null) {
      if (other.parent != null) {
        return false;
      }
    } else if (!parent.equals(other.parent)) {
      return false;
    }
    if (gadget == null) {
      if (other.gadget != null) {
        return false;
      }
    } else if (!gadget.equals(other.gadget)) {
      return false;
    }
    return true;
  }
  
  /**
   * 
   * @param propertyName
   * @param newValue
   */
  public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
    pcs.firePropertyChange(propertyName, oldValue, newValue);
  }
  
  /**
   * 
   * @param propertyName
   * @param newValue
   */
  public void firePropertyChange(String propertyName, Object newValue) {
    firePropertyChange(propertyName, null, newValue);
  }
  
  /**
   * @return the listOfCompatibleSoftware
   */
  public List<Gadget> getCompatibleGadgetList() {
    return backend.getCompatibleGadgetList();
  }
  
  /**
   * 
   * @return
   */
  public UserInterface getParent() {
    return parent;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((parent == null) ? 0 : parent.hashCode());
    result = prime * result + ((gadget == null) ? 0 : gadget.hashCode());
    return result;
  }
  
  /**
   * 
   */
  public void init() {
    backend = new GarudaClientBackend(gadget.getID(), gadget.getName()); /*,
      iconPath, listOfOutputFileFormats, listOfInputFileFormats,
      listOfCategories, provider, description, listOfScreenshots);*/
    UserInterface ui = getParent();
    if (ui instanceof JFrame) {
      backend.setParentFrame((JFrame) ui);
    }
    logger.fine(bundle.getString("TRYING_TO_INITIALIZE_GARUDA_CORE"));
    backend.setForceCloseOnDisconnect(true);
    backend.setShowHelpOnStart(false);
    backend.addGarudaChangeListener(new GarudaSoftwareListener(this));
    try {
      backend.initialize();
    } catch (GarudaConnectionNotInitializedException exc) {
      firePropertyChange(GarudaClientBackend.CONNECTION_NOT_INITIALIZED_ID, null);
    } catch (BackendAlreadyInitializedException exc) {
      firePropertyChange(SOFTWARE_REGISTERED_ERROR, null);
    }
  }
  
  /**
   * 
   */
  public void setGlassPane() {
    if (parent instanceof java.awt.Component) {
      java.awt.Component component = (java.awt.Component) parent;
      if (component instanceof javax.swing.JFrame) {
        backend.setParentFrame((javax.swing.JFrame) component);
      }
      try {
        GarudaGlassPanel glassPane = (GarudaGlassPanel) GarudaControlPanelFactory.getGarudaGlassPanel(backend);
        if (component instanceof javax.swing.JComponent) {
          ((javax.swing.JComponent) component).getRootPane().setGlassPane(glassPane);
        } else if (component instanceof javax.swing.JFrame) {
          ((javax.swing.JFrame) component).setGlassPane(glassPane);
        }
        glassPane.setKeyboardListeners();
      } catch (NoClassDefFoundError exc) {
        logger.fine(exc.getMessage());
      }
    }
  }
  
  /**
   * 
   * @return
   */
  public boolean isSetGlassPane() {
    return getGlassPane() != null;
  }
  
  /**
   * @return the initialized
   */
  public boolean isInitialized() {
    return backend.isInitialized();
  }
  
  /**
   * 
   * @param files
   * @return
   */
  public File[] openFile(File... files) {
    return parent.openFileAndLogHistory(files);
  }
  
  /**
   * 
   * @throws NetworkException
   * @throws BackendNotInitializedException
   * @throws GarudaConnectionNotInitializedException
   * @throws BackendNotInitializedException
   */
  public void registedSoftwareToGaruda() throws NetworkException,
  BackendNotInitializedException, GarudaConnectionNotInitializedException, BackendNotInitializedException {
    if (!backend.isInitialized()) {
      throw new BackendNotInitializedException();
    }
    File parentFolder = new File(System.getProperty("user.dir"));
    logger.fine(MessageFormat.format(bundle.getString("PATH_FOR_SOFTWARE"), parentFolder.getAbsolutePath()));
    //The path of the software that the core will use in order to launch it. Note that this
    //is the command that someone would use in order to launch the software. In this case we are
    //using the command to launch a jar file.
    //      String filePath = "javaw -jar " + parentFolder.getAbsolutePath().toString()  +  "/NewTestSoftwareA.jar";
    
    String filePath = GarudaSoftwareBackend.class.getProtectionDomain().getCodeSource().getLocation().getPath();
    
    try {
      filePath = URLDecoder.decode(filePath, "UTF-8");
    } catch (UnsupportedEncodingException exc) {
      showErrorMessage(exc);
    }
    
    filePath = filePath.substring(1);
    
    //      String filePath = null;
    
    String os_name = System.getProperty("os.name").toLowerCase();
    
    if (os_name.indexOf("mac") >= 0) {
      filePath = "open \"" + filePath + "\"";
    } else if (os_name.contains("windows")) {
      filePath = "cmd /c start javaw.exe -jar \"" + filePath + "\"";
    } else {
      filePath = "java -jar " + filePath;
    }
    backend.registerGadgetToGaruda(filePath);
    logger.fine(MessageFormat.format(bundle.getString("REGISTERED_SOFTWARE_TO_CORE"), parentFolder.getAbsolutePath()));
  }
  
  /**
   * @param exc
   */
  private void showErrorMessage(Throwable exc) {
    if (parent instanceof Component) {
      GUITools.showErrorMessage((Component) parent, exc);
    } else {
      exc.printStackTrace();
    }
  }
  
  /**
   * 
   * @param pcl
   */
  public void removePropertyChangeListener(PropertyChangeListener pcl) {
    pcs.removePropertyChangeListener(pcl);
  }
  
  /**
   * 
   * @param selectedFile
   * @throws NetworkException
   * @throws IllegalStateException
   * @throws GarudaConnectionNotInitializedException
   */
  public void requestForLoadableGadgets(File selectedFile, String fileType) throws NetworkException,
  IllegalStateException, GarudaConnectionNotInitializedException {
    if (selectedFile != null) {
      backend.requestForLoadableGadgets(fileType, selectedFile);
      logger.fine(MessageFormat.format(bundle.getString("REQUESTED_COMPATIBLE_SOFTWARE"), selectedFile));
    } else {
      throw new IllegalStateException(bundle.getString("NO_FILE_SELECTED"));
    }
  }
  
  /**
   * 
   * @param selectedFile
   * @param indexOfSoftware
   * @throws NetworkException
   * @throws IllegalStateException
   * @throws GarudaConnectionNotInitializedException
   */
  public void sentFileToSoftware(File selectedFile, int indexOfSoftware)
      throws NetworkException, IllegalStateException, GarudaConnectionNotInitializedException {
    if ((selectedFile == null) || !selectedFile.exists()) {
      throw new IllegalStateException(MessageFormat.format(bundle.getString("FILE_DOES_NOT_EXIST"), selectedFile));
    }
    List<Gadget> listOfCompatibleGadgets = backend.getCompatibleGadgetList();
    if ((listOfCompatibleGadgets == null)
        || listOfCompatibleGadgets.isEmpty()
        || (listOfCompatibleGadgets.get(indexOfSoftware) == null)) {
      throw new IllegalStateException(MessageFormat.format(
        bundle.getString("NO_COMPATIBLE_SOFTWARE_FOUND"),
        FileTools.getExtension(selectedFile.getName())));
    }
    backend.sentFileToGadget(backend.getCompatibleGadgetList().get(indexOfSoftware), selectedFile);
    logger.fine(MessageFormat.format(
      bundle.getString("FILE_LOADING_REQUEST"),
      selectedFile.getAbsolutePath(),
      listOfCompatibleGadgets.get(indexOfSoftware).getName()));
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(getClass().getSimpleName());
    builder.append(" [softwareID=");
    builder.append(gadget.getID());
    builder.append(", initialized=");
    builder.append((backend != null) && backend.isInitialized() ? "true" : "faslse");
    builder.append(']');
    return builder.toString();
  }
  
  /**
   * 
   * @return
   */
  public java.awt.Component getGlassPane() {
    if (parent instanceof javax.swing.JComponent) {
      return ((javax.swing.JComponent) parent).getRootPane().getGlassPane();
    }
    return null;
  }
  
}
