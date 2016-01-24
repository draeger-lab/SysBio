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
package de.zbit.sbml.gui;

import static de.zbit.util.Utils.getMessage;

import java.awt.Component;
import java.beans.PropertyChangeListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLWriter;

import de.zbit.gui.GUITools;
import de.zbit.io.OpenedFile;

/**
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 */
public class SBMLWritingTask extends SwingWorker<File, Void> {
  
  public static final String SBML_WRITING_SUCCESSFULLY_DONE = SBMLWritingTask.class.getName() + ".SBML_WRITING_SUCCESSFULLY_DONE";
  
  private Component parent;
  private OpenedFile<SBMLDocument> sbmlFile;
  
  /**
   * 
   * @param sbmlFile
   * @param parent
   * @param changeListeners
   * @throws FileNotFoundException
   */
  public SBMLWritingTask(OpenedFile<SBMLDocument> sbmlFile, Component parent, PropertyChangeListener...changeListeners) throws FileNotFoundException {
    super();
    this.parent = parent;
    this.sbmlFile = sbmlFile;
    if ((changeListeners != null) && (changeListeners.length > 0)) {
      for (PropertyChangeListener listener : changeListeners) {
        addPropertyChangeListener(listener);
      }
    }
  }
  
  /* (non-Javadoc)
   * @see javax.swing.SwingWorker#doInBackground()
   */
  @Override
  protected File doInBackground() throws Exception {
    BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(sbmlFile.getFile()));
    SBMLWriter.write(sbmlFile.getDocument(), outputStream,
      System.getProperty("app.name"), System.getProperty("app.version"));
    outputStream.close();
    return sbmlFile.getFile();
  }
  
  /* (non-Javadoc)
   * @see javax.swing.SwingWorker#done()
   */
  @Override
  protected void done() {
    try {
      firePropertyChange(SBML_WRITING_SUCCESSFULLY_DONE, null, get());
    } catch (InterruptedException exc) {
      GUITools.showErrorMessage(parent, getMessage(exc));
    } catch (ExecutionException exc) {
      GUITools.showErrorMessage(parent, getMessage(exc));
    }
  }
  
}
