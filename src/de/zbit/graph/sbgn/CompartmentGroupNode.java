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
package de.zbit.graph.sbgn;

import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;

import y.geom.YInsets;
import y.layout.DiscreteNodeLabelModel;
import y.view.NodeRealizer;
import y.view.hierarchy.GroupNodeRealizer;

/**
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class CompartmentGroupNode extends GroupNodeRealizer {
  
  /**
   * 
   */
  public CompartmentGroupNode() {
    super(new ComplexNode());
    
    initDefaults();
  }
  
  /**
   * 
   */
  private void initDefaults() {
    setGroupClosed(false);
    setTransparent(false); // we always fill the border and never the content
    
    // Eliminate the expanding/ collapsing icons
    setClosedGroupIcon(null);
    setOpenGroupIcon(null);
    
    setMinimalInsets(new YInsets(7, 7, 7, 7)); // top, left, bottom, right
    setAutoBoundsEnabled(true);
    getLabel().setLabelModel(new DiscreteNodeLabelModel(DiscreteNodeLabelModel.BOTTOM));
    getLabel().setBackgroundColor(null);
  }
  
  /**
   * 
   * @param nr
   */
  public CompartmentGroupNode(NodeRealizer nr) {
    super(nr);
    // If the given node realizer is of this type, then apply copy semantics.
    if (nr instanceof CompartmentGroupNode) {
      CompartmentGroupNode fnr = (CompartmentGroupNode) nr;
      // Copy the values of custom attributes (there are none).
    }
  }
  
  /* (non-Javadoc)
   * @see y.view.hierarchy.GroupNodeRealizer#createCopy(y.view.NodeRealizer)
   */
  @Override
  public NodeRealizer createCopy(NodeRealizer nr) {
    return new CompartmentGroupNode(nr);
  }
  
  
  /* (non-Javadoc)
   * @see y.view.ShapeNodeRealizer#paintShapeBorder(java.awt.Graphics2D)
   */
  @Override
  protected void paintShapeBorder(Graphics2D gfx) {
    gfx.setColor(getLineColor());
    gfx.draw(createPolygon());
  }
  
  /* (non-Javadoc)
   * @see y.view.ShapeNodeRealizer#paintFilledShape(java.awt.Graphics2D)
   */
  @Override
  protected void paintFilledShape(Graphics2D gfx) {
    if (getFillColor()!=null) {
      gfx.setColor(getFillColor());
      gfx.fill(createPolygon());
    }
  }
  
  
  /**
   * See {@link #createPolygon(double, double, double, double)}
   * @return
   */
  private GeneralPath createPolygon() {
    return createPolygon(getX(), getY(), getWidth(), getHeight());
  }
  
  /**
   * Creates a {@link GeneralPath} that draws the shape depicted below.
   * All corners are round cornes (i.e., from 2 to 3 is a rounded corner
   * not a direkt lines).
   * <pre>
   *       1 . . . . . 2
   *     .               .
   *   8                   3
   *   .                   .
   *   .                   .
   *   7                   4
   *    .                 .
   *      6 . . . . . . 5
   * </pre>
   * @param x
   * @param y
   * @param w
   * @param h
   * @return
   */
  public static GeneralPath createRoundedRectanglePath(double x, double y, double w, double h) {
    int arc = (int) (w/10); // Determines rounding of the edges
    
    GeneralPath path = new GeneralPath();
    // outer path
    path.moveTo(x + arc, y); // fly to 1
    path.lineTo(x + w - arc, y); // line to 2
    path.quadTo(x + w, y, x + w, y + arc); // curve to 3
    path.lineTo(x + w, y + h - arc); // line to 4
    path.quadTo(x + w, y + h, x + w - arc, y + h); // curve to 5
    path.lineTo(x + arc, y + h); // line to 6
    path.quadTo(x, y + h, x, y + h - arc); // curve to 7
    path.lineTo(x, y + arc); // line to 8
    path.quadTo(x, y, x + arc, y); // curve to 1
    
    path.closePath(); // should have no effect.
    
    return path;
  }
  
  /**
   * 
   * @param x
   * @param y
   * @param w
   * @param h
   * @return
   */
  public static GeneralPath createPolygon(double x, double y, double w, double h) {
    int halfBorderWidth = (int)(w / 10d); // Width of the border
    
    /*
     * TODO Thick bold solid line
     */
    
    // Create outer and inner parts
    GeneralPath outer = createRoundedRectanglePath(x, y, w, h);
    GeneralPath inner = createRoundedRectanglePath(x + halfBorderWidth, y + halfBorderWidth, w - 2 * halfBorderWidth, h - 2 * halfBorderWidth);
    
    // Join both parts
    outer.append(inner, false);
    
    return outer;
  }
  
}
