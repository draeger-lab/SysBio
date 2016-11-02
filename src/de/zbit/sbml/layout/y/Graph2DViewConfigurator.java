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

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.logging.Logger;

import de.zbit.graph.RestrictedEditMode;
import de.zbit.util.Utils;
import y.view.Graph2D;
import y.view.Graph2DView;
import y.view.Graph2DViewMouseWheelZoomListener;

/**
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 */
public class Graph2DViewConfigurator {
  
  /**
   * A {@link Logger} for this class.
   */
  private static final Logger logger = Logger.getLogger(Graph2DViewConfigurator.class.getName());
  
  /**
   * 
   */
  private Graph2DViewConfigurator() {
  }
  
  /**
   * 
   * @param graph2d
   * @param minWidth
   * @param windowHeight
   * @return
   */
  public static Graph2DView configureGraph2DView(Graph2D graph2d, int minWidth, int windowHeight) {
    Graph2DView activeView = new Graph2DView(graph2d);
    // TODO: Add tooltip support
    /*TooltipMode tm = new YGraphTooltip(director);
    activeView.addViewMode(tm);*/
    
    Rectangle box = activeView.getGraph2D().getBoundingBox();
    Dimension dim = box.getSize();
    activeView.setSize(dim);
    // activeView.zoomToArea(box.getX() - 10, box.getY() - 10, box.getWidth() + 20, box.getHeight() + 20);
    
    Dimension minimumSize = new Dimension(
      (int) Math.max(activeView.getMinimumSize().getWidth(), minWidth),
      (int) Math.max(activeView.getMinimumSize().getHeight(), windowHeight/2d));
    activeView.setMinimumSize(minimumSize);
    activeView.setPreferredSize(new Dimension(minWidth, (int) Math.max(windowHeight * 0.6d, 50d)));
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
  
}
