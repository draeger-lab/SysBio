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
package de.zbit.graph.io;

import java.util.logging.Level;
import java.util.logging.Logger;

import y.io.IOHandler;
import y.view.Graph2DView;
import de.zbit.kegg.io.KEGGtranslator;

/**
 * This class should be used whenever any yFiles data
 * structure should be written to a file. No matter if
 * JPG or GraphML is the destination format.
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class Graph2Dwriter extends Graph2DExporter {
  
  /**
   * A {@link Logger} for this class.
   */
  private static final transient Logger logger = Logger.getLogger(Graph2Dwriter.class.getName());
  
  protected KEGGtranslator<?> translator = null;
  
  /**
   * 
   * @param outputHandler2
   * @param translator
   */
  public Graph2Dwriter(IOHandler outputHandler2, KEGGtranslator<?> translator) {
    this(outputHandler2);
    this.translator = translator;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.graph.io.Graph2DExporter#processBackgroundImage(y.view.Graph2DView, boolean)
   */
  @Override
  protected void processBackgroundImage(Graph2DView view, boolean waitUntilComplete) {
    try {
      if (isSetBackgroundImageProvider()) {
        // translator should be ALLOWED to be null, must be considered in
        // the GraphBachgroundImageProviders... (NOT HERE!!!)
        getBackgroundImageProvider().addBackgroundImage(view, getTranslator(), waitUntilComplete);
      }
    } catch (Exception exc) { // NullPointer or MalformedURLException
      logger.log(Level.WARNING, "Could not setup background image for output file.", exc);
    }
  }
  
  
  
  /**
   * @param f
   * @throws Exception
   */
  public Graph2Dwriter(WriteableFileExtensions f, KEGGtranslator<?> translator) throws Exception {
    this(getIOHandler(f), translator);
  }
  
  /**
   * @param outputHandler2
   */
  public Graph2Dwriter(IOHandler outputHandler2) {
    super();
    setOutputHandler(outputHandler2);
  }
  
  /**
   * @param f
   * @throws Exception
   */
  public Graph2Dwriter(WriteableFileExtensions f) throws Exception {
    this(getIOHandler(f));
  }
  
  /**
   * @return the translator
   */
  public KEGGtranslator<?> getTranslator() {
    return translator;
  }
  
  /**
   * @param translator the translator to set
   */
  public void setTranslator(KEGGtranslator<?> translator) {
    this.translator = translator;
  }
  
}
