/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2018 by the University of Tuebingen, Germany.
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

import java.io.IOException;
import java.text.MessageFormat;
import java.util.logging.Logger;

import org.sbml.jsbml.Model;

import y.io.ViewPortConfigurator;
import y.view.Graph2D;
import y.view.Graph2DView;
import yext.svg.io.SVGIOHandler;

/**
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 */
public class YImageTools {
  
  private static final transient Logger logger = Logger.getLogger(YImageTools.class.getSimpleName());
  
  /**
   * 
   * @param model
   * @param graph
   * @param outFile
   */
  public static void writeSVGImage(Model model, Graph2D graph, String outFile) {
    writeSVGImage(model, graph, outFile, 0, 0);
  }
  
  /**
   * Write svg image file
   * @param graph
   * 
   * @param outFile path of the output file
   */
  public static void writeSVGImage(Model model, Graph2D graph, String outFile, int w, int h) {
    SVGIOHandler svgio = new SVGIOHandler();
    SVGDOMEnhancerForHierarchy svgEFH = new SVGDOMEnhancerForHierarchy(model);
    svgEFH.setDrawEdgesFirst(false);
    svgio.setSVGGraph2DRenderer(svgEFH);
    
    //unselect objects
    graph.unselectAll();
    //save current view
    Graph2DView originalViewPort = (Graph2DView) graph.getCurrentView();
    Graph2DView imageView = svgio.createDefaultGraph2DView(graph);
    //use original render settings
    imageView.setGraph2DRenderer(originalViewPort.getGraph2DRenderer());
    imageView.setRenderingHints(originalViewPort.getRenderingHints());
    //set dedicated view
    graph.setCurrentView(imageView);
    if ((w > 0) && (h > 0)) {
      //settings for dedicated view
      ViewPortConfigurator vpc = new ViewPortConfigurator();
      vpc.setGraph2D(imageView.getGraph2D());
      //do not cut off anything not in view
      vpc.setClipType(ViewPortConfigurator.CLIP_GRAPH);
      //scale image to video size
      vpc.setSizeType(ViewPortConfigurator.SIZE_USE_CUSTOM_WIDTH);
      vpc.setCustomWidth(w);
      //configure dedicated view with settings
      vpc.configure(imageView);
    }
    
    try {
      svgio.write(graph, outFile);
      logger.info(MessageFormat.format("Image written to ''{0}''.", outFile));
      
      //restore original view
      graph.removeView(graph.getCurrentView());
      graph.setCurrentView(originalViewPort);
      
    } catch (IOException e) {
      logger.warning("Could not write image: ImageWriter not available.");
    }
  }
  
}
