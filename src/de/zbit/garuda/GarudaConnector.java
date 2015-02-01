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
package de.zbit.garuda;

import static de.zbit.util.Utils.getMessage;

import java.awt.Component;
import java.util.logging.Logger;

import jp.sbi.garuda.platform.commons.exception.NetworkException;
import de.zbit.UserInterface;
import de.zbit.gui.GUITools;

/**
 * Initiates the connection to Garuda core.
 * 
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 */
public class GarudaConnector implements Runnable {
  
  /**
   * A {@link Logger} for this class.
   */
  private static final Logger logger = Logger.getLogger(GarudaConnector.class.getName());
  
  private String id;
  private UserInterface ui;
  
  /**
   * 
   * @param id
   *        a unique identifier of the software.
   * @param ui
   *        the user interface.
   */
  public GarudaConnector(String id, UserInterface ui) {
    super();
    this.id = id;
    this.ui = ui;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Runnable#run()
   */
  @Override
  public void run() {
    try {
      GarudaSoftwareBackend garudaBackend = new GarudaSoftwareBackend(id, ui);
      garudaBackend.init();
    } catch (NetworkException exc) {
      if (ui instanceof Component) {
        GUITools.showErrorMessage((Component) ui, exc);
      } else {
        logger.warning(getMessage(exc));
      }
    } catch (Throwable exc) {
      logger.fine(getMessage(exc));
      exc.printStackTrace();
    }
  }
  
}
