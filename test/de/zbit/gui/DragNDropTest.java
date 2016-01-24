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
package de.zbit.gui;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.net.URL;
import java.util.Arrays;

import javax.swing.JToolBar;

/**
 * 
 * @version $Rev$
 */
public class DragNDropTest extends BaseFrame {
  
  private static final long serialVersionUID = -2668467238882169768L;
  
  public DragNDropTest() {
    super();
  }
  
  @Override
  public boolean closeFile() {
    return false;
  }
  
  @Override
  protected JToolBar createJToolBar() {
    return null;
  }
  
  @Override
  protected Component createMainComponent() {
    return null;
  }
  
  @Override
  public URL getURLAboutMessage() {
    return null;
  }
  
  @Override
  public URL getURLLicense() {
    return null;
  }
  
  @Override
  public URL getURLOnlineHelp() {
    return null;
  }
  
  @Override
  protected File[] openFile(File... files) {
    // TODO: Test method!!
    System.out.println(Arrays.toString(files));
    return null;
  }
  
  @Override
  public File saveFileAs() {
    return null;
  }
  
  public static void main(String args[]) {
    DragNDropTest test = new DragNDropTest();
    test.setVisible(true);
  }
  
  /* (non-Javadoc)
   * @see de.zbit.gui.BaseFrame#saveFile()
   */
  @Override
  public File saveFile() {
    return null;
  }
  
  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    // TODO Auto-generated method stub
    
  }
  
}
