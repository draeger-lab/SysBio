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
package edu.ucsd.sbrg.bionetview;

import java.awt.Component;

import javax.swing.SwingWorker;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.ext.layout.LayoutConstants;
import org.sbml.jsbml.ext.layout.LayoutModelPlugin;

import de.zbit.sbml.layout.GlyphCreator;

/**
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 */
public class GlyphCreationTask extends SwingWorker<LayoutModelPlugin, Void> {
  
  private SBMLDocument doc;
  private Component parent;
  
  /**
   * 
   * @param doc
   * @param parent
   */
  public GlyphCreationTask(SBMLDocument doc, Component parent) {
    this.doc = doc;
    this.parent = parent;
  }
  
  /* (non-Javadoc)
   * @see javax.swing.SwingWorker#doInBackground()
   */
  @Override
  protected LayoutModelPlugin doInBackground() throws Exception {
    Model model = doc.getModel();
    GlyphCreator creator = new GlyphCreator(model);
    creator.create();
    return (LayoutModelPlugin) model.getExtension(LayoutConstants.getNamespaceURI(doc.getLevel(), doc.getVersion()));
  }
  
  
  
}
