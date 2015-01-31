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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

import y.view.NodeRealizer;

/**
 * Realizer for process nodes of type "dissociation". Draws the SBGN specified
 * shape (two unfilled circles).
 * 
 * @author Jakob Matthes
 * @version $Rev$
 */
public class DissociationNodeRealizer extends ProcessNodeRealizer {
  
  /**
   * 
   */
  public DissociationNodeRealizer() {
    super();
  }
  
  /**
   * 
   * @param nr
   */
  public DissociationNodeRealizer(NodeRealizer nr) {
    super(nr);
  }
  
  /* (non-Javadoc)
   * @see y.view.ShapeNodeRealizer#createCopy(y.view.NodeRealizer)
   */
  @Override
  public NodeRealizer createCopy(NodeRealizer nr) {
    return new DissociationNodeRealizer(nr);
  }
  
  /* (non-Javadoc)
   * @see de.zbit.graph.sbgn.ProcessNodeRealizer#drawShape(java.awt.Graphics2D)
   */
  @Override
  protected void drawShape(Graphics2D gfx) {
    double min = Math.min(width, height);
    double offsetX = (width - min)/2d;
    double offsetY = (height - min)/2d;
    
    gfx.setColor(getLineColor());
    gfx.setStroke(new BasicStroke(lineWidth > 0 ? lineWidth : 1));
    
    // Draw a circle and a smaller circle in the center of the first.
    // TODO The smaller circle is not positioned correctly, possibly due to
    // rounding errors/int casting.
    gfx.drawOval((int) (offsetX + x), (int) (offsetY + y), (int) min, (int) min);
    
    gfx.setColor(Color.RED);
    gfx.drawRect((int) (offsetX + x), (int) (offsetY + y), (int) min, (int) min);
    gfx.setColor(getLineColor());
    
    double diameter = min * .75d;
    double diff = min - diameter;
    int innerX = (int) (offsetX + x  +  diff/2d);
    int innerY = (int) (offsetY + y  +  diff/2d);
    gfx.drawOval(innerX, innerY, (int) diameter, (int) diameter);
    
    gfx.setColor(Color.RED);
    gfx.drawRect(innerX, innerY, (int) diameter, (int) diameter);
    gfx.setColor(getLineColor());
  }
  
}
