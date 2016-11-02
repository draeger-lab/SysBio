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
package de.zbit.sbml.layout.y;

import java.awt.Component;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

import org.sbml.jsbml.ext.layout.Layout;

import de.zbit.sbml.layout.LayoutDirector;

/**
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 */
public class LayoutDirectionTask extends SwingWorker<Component, LayoutDirector<ILayoutGraph>> {
  
  /**
   * A {@link Logger} for this class.
   */
  private static final transient Logger logger = Logger.getLogger(LayoutDirectionTask.class.getName());
  
  /**
   * The layout for which a view is to be created.
   */
  private Layout layout;
  
  /**
   * This tracks the progress of the layout building procedure.
   */
  private ProgressMonitor progressMonitor;
  
  /**
   * 
   * @param layout
   * @param parent
   */
  public LayoutDirectionTask(Layout layout, Component parent) {
    this.layout = layout;
    progressMonitor = new ProgressMonitor(parent, "Initializing Layout", "", 0, 100);
    progressMonitor.setMillisToDecideToPopup(0);
    progressMonitor.setMillisToPopup(0);
  }
  
  /* (non-Javadoc)
   * @see javax.swing.SwingWorker#doInBackground()
   */
  @Override
  protected Component doInBackground() throws Exception {
    LayoutDirector<ILayoutGraph> director = new LayoutDirector<ILayoutGraph>(
        layout, new YLayoutBuilder(), new YLayoutAlgorithm());
    progressMonitor.setProgress(30);
    progressMonitor.setNote("Generating layout.");
    director.run();
    progressMonitor.setProgress(90);
    
    publish(director);
    logger.fine("Passing generated graph layout to 2D view...");
    return Graph2DViewConfigurator.configureGraph2DView(director.getProduct().getGraph2D(), 100, 720);
  }
  
  /* (non-Javadoc)
   * @see javax.swing.SwingWorker#process(java.util.List)
   */
  @Override
  protected void process(List<LayoutDirector<ILayoutGraph>> chunks) {
    progressMonitor.close();
  }
  
}
