/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2015 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.gui;

import java.awt.Component;
import java.awt.HeadlessException;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JToolBar;

import de.zbit.util.logging.LogUtil;

/**
 * @author Andreas Dr&auml;ger
 * @date 10:48:25
 * @since 1.1
 * @version $Rev$
 */
public class BaseFrameExample extends BaseFrame {
  
  /**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = -8203209532268944180L;
  
  /**
   * A {@link Logger} for this class.
   */
  private static final Logger logger = Logger.getLogger(BaseFrameExample.class.getName());

  /**
   * @throws HeadlessException
   */
  public BaseFrameExample() throws HeadlessException {
    super();
  }
  
  /* (non-Javadoc)
   * @see de.zbit.UserInterface#closeFile()
   */
  @Override
  public boolean closeFile() {
    logger.info("closeFile");
    return false;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.UserInterface#getURLAboutMessage()
   */
  @Override
  public URL getURLAboutMessage() {
    // TODO Auto-generated method stub
    return null;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.UserInterface#getURLLicense()
   */
  @Override
  public URL getURLLicense() {
    // TODO Auto-generated method stub
    return null;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.UserInterface#getURLOnlineHelp()
   */
  @Override
  public URL getURLOnlineHelp() {
    // TODO Auto-generated method stub
    return null;
  }
  
  /* (non-Javadoc)
   * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
   */
  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    logger.info("property changed: " + evt);
  }
  
  /* (non-Javadoc)
   * @see de.zbit.gui.BaseFrame#createJToolBar()
   */
  @Override
  protected JToolBar createJToolBar() {
    return createDefaultToolBar();
  }
  
  /* (non-Javadoc)
   * @see de.zbit.gui.BaseFrame#createMainComponent()
   */
  @Override
  protected Component createMainComponent() {
    // TODO Auto-generated method stub
    return null;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.gui.BaseFrame#openFile(java.io.File[])
   */
  @Override
  protected File[] openFile(File... files) {
    logger.info("openFile " + Arrays.toString(files));
    return null;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.gui.BaseFrame#saveFileAs()
   */
  @Override
  public File saveFileAs() {
    logger.info("saveFileAs");
    return null;
  }
  
  /**
   * @param args
   */
  public static void main(String[] args) {
    LogUtil.initializeLogging(Level.INFO, "de.zbit");
    BaseFrameExample frame = new BaseFrameExample();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
  
}
