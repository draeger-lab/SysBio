/* $Id$
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
package de.zbit.sbml.gui;

import static de.zbit.util.Utils.getMessage;

import java.awt.Component;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ProgressMonitor;
import javax.swing.ProgressMonitorInputStream;
import javax.swing.SwingWorker;
import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;

import de.zbit.gui.GUITools;
import de.zbit.io.OpenedFile;
import de.zbit.sbml.io.SBMLfileChangeListener;
import de.zbit.util.ResourceManager;
import de.zbit.util.Timer;

/**
 * Reads an SBML file and sends the resulting {@link SBMLDocument} wrapped in an
 * {@link OpenedFile} object (together with the {@link File} from which it was
 * read) to assigned {@link PropertyChangeListener}s when done. In case that the
 * reading process takes some more time, a {@link ProgressMonitor} is displayed
 * to the user. If the user cancels the reading process, the resulting
 * {@link SBMLDocument} and hence the {@link OpenedFile} will be {@code null}.
 * It is hence necessary to use this {@link SBMLReadingTask} as follows:
 * 
 * <pre>
 * SBMLReadingTask task = new SBMLReadingTask(file, this);
 * task.execute();
 * task.addPropertyChangeListener(myListener);
 * </pre>
 * 
 * Then, to access the result, please do the following:
 * 
 * <pre>
 * public void propertyChange(PropertyChangeEvent evt) {
 * 	if (evt.getPropertyName().equals(SBMLReadingTask.SBML_READING_SUCCESSFULLY_DONE)) {
 * 		if ((evt.getNewValue() != null)
 * 				&amp;&amp; (evt.getNewValue() instanceof OpenedFile)) {
 *       // call some method here with the OpenedFile containing
 *       // the original File and the resulting SBMLDocument.
 * 		} else {
 * 			// The user has canceled the reading or it was not successful.
 * 		}
 * 	}
 * }
 * </pre>
 * 
 * @author Andreas Dr&auml;ger
 * @since 1.1
 * @version $Rev$
 */
public class SBMLReadingTask extends SwingWorker<SBMLDocument, Void> {
  
  /**
   * For localization support.
   */
  private static final transient ResourceBundle bundle = ResourceManager.getBundle("de.zbit.sbml.locales.Messages");
  
  /**
   * Key used to notify listeners about success.
   */
  public static final String SBML_READING_SUCCESSFULLY_DONE = "SBML_READING_SUCCESSFULLY_DONE";
  
  /**
   * The stream from which the SBML content is to be read.
   */
  private InputStream inputStream;
  
  /**
   * The element from the graphical user interface that invokes this task.
   */
  private Component parent;
  
  /**
   * Monitors the reading operation to the user.
   */
  private ProgressMonitor progressMonitor;
  
  /**
   * Memorize the given SBML file for later access, e.g., in case of successful
   * reading.
   */
  private File sbmlFile;
  
  /**
   * 
   * @param sbmlFile
   * @param parent
   * @throws FileNotFoundException
   */
  public SBMLReadingTask(File sbmlFile, Component parent) throws FileNotFoundException {
    super();
    this.parent = parent;
    this.sbmlFile = sbmlFile;
    ProgressMonitorInputStream pmis = new ProgressMonitorInputStream(parent,
      MessageFormat.format(bundle.getString("READING_SBML_FILE"), sbmlFile.getName()),
      new FileInputStream(sbmlFile));
    progressMonitor = pmis.getProgressMonitor();
    inputStream = new BufferedInputStream(pmis);
  }
  
  /**
   * 
   * @param sbmlFile
   * @param parent
   * @param changeListeners
   * @throws FileNotFoundException
   */
  public SBMLReadingTask(File sbmlFile, Component parent, PropertyChangeListener...changeListeners) throws FileNotFoundException {
    this(sbmlFile, parent);
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
  protected SBMLDocument doInBackground() throws XMLStreamException {
    Logger logger = Logger.getLogger(SBMLReadingTask.class.getName());
    Timer timer = new Timer();
    try {
      SBMLDocument doc = SBMLReader.read(inputStream);
      logger.info(MessageFormat.format(bundle.getString("READING_TIME"), timer.getAndReset(false)));
      return doc;
    } catch (Throwable exc) {
      logger.info(MessageFormat.format(bundle.getString("CANCELING_AT_TIME"), timer.getAndReset(false)));
      logger.log(Level.FINE, getMessage(exc), exc);
      throw new XMLStreamException(exc);
    }
  }
  
  /* (non-Javadoc)
   * @see javax.swing.SwingWorker#done()
   */
  @Override
  protected void done() {
    progressMonitor.close();
    try {
      final OpenedFile<SBMLDocument> openedFile = new OpenedFile<SBMLDocument>(sbmlFile, get());
      if (openedFile.isSetDocument()) {
        openedFile.getDocument().addTreeNodeChangeListener(new SBMLfileChangeListener(openedFile));
      }
      firePropertyChange(SBML_READING_SUCCESSFULLY_DONE, null, openedFile);
    } catch (InterruptedException exc) {
      exc.printStackTrace();
      GUITools.showErrorMessage(parent, getMessage(exc));
    } catch (ExecutionException exc) {
      exc.printStackTrace();
      GUITools.showErrorMessage(parent, getMessage(exc));
    }
  }
  
  /**
   * @return the sbmlFile
   */
  public File getSBMLfile() {
    return sbmlFile;
  }
  
}
