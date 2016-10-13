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
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

import org.sbml.jsbml.ext.layout.Layout;

import de.zbit.graph.RestrictedEditMode;
import de.zbit.sbml.layout.LayoutDirector;
import de.zbit.util.Utils;
import y.view.Graph2D;
import y.view.Graph2DView;
import y.view.Graph2DViewMouseWheelZoomListener;

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
    
    Graph2D graph2d = director.getProduct().getGraph2D();
    Graph2DView activeView = new Graph2DView(graph2d);
    // TODO: Add tooltip support
    /*TooltipMode tm = new YGraphTooltip(director);
    activeView.addViewMode(tm);*/
    
    Rectangle box = activeView.getGraph2D().getBoundingBox();
    Dimension dim = box.getSize();
    activeView.setSize(dim);
    // activeView.zoomToArea(box.getX() - 10, box.getY() - 10, box.getWidth() + 20, box.getHeight() + 20);
    int WINDOW_HEIGHT = 720;
    Dimension minimumSize = new Dimension(
      (int) Math.max(activeView.getMinimumSize().getWidth(), 100),
      (int) Math.max(activeView.getMinimumSize().getHeight(), WINDOW_HEIGHT/2d));
    activeView.setMinimumSize(minimumSize);
    activeView.setPreferredSize(new Dimension(100, (int) Math.max(WINDOW_HEIGHT * 0.6d, 50d)));
    activeView.setOpaque(false);
    
    activeView.getCanvasComponent().addMouseWheelListener(new Graph2DViewMouseWheelZoomListener());
    try {
      activeView.fitContent(true);
    } catch (Throwable t) {
      // Not really a problem
      logger.finest(Utils.getMessage(t));
    }
    RestrictedEditMode.addOverviewAndNavigation(activeView);
    activeView.addViewMode(new RestrictedEditMode());
    activeView.setFitContentOnResize(true);
    
    return activeView;
  }
  
  /* (non-Javadoc)
   * @see javax.swing.SwingWorker#process(java.util.List)
   */
  @Override
  protected void process(List<LayoutDirector<ILayoutGraph>> chunks) {
    progressMonitor.close();
  }
  
}
