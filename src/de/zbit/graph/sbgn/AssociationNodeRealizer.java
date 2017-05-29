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
package de.zbit.graph.sbgn;

import java.awt.BasicStroke;
import java.awt.Graphics2D;

import y.view.NodeRealizer;

/**
 * Realizer for process nodes of type "association". Draws the SBGN specified
 * shape (filled circle).
 * 
 * @author Jakob Matthes
 * @version $Rev$
 */
public class AssociationNodeRealizer extends ProcessNodeRealizer {
  
  /**
   * 
   */
  public AssociationNodeRealizer() {
    super();
  }
  
  /**
   * 
   * @param nr
   */
  public AssociationNodeRealizer(NodeRealizer nr) {
    super(nr);
  }
  
  /* (non-Javadoc)
   * @see y.view.ShapeNodeRealizer#createCopy(y.view.NodeRealizer)
   */
  @Override
  public NodeRealizer createCopy(NodeRealizer nr) {
    return new AssociationNodeRealizer(nr);
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
    
    // Draw a filled circle.
    gfx.fillOval(0, 0, 1, 1);
    setFrame((int) (offsetX + x), (int) (offsetY + y), (int) min, (int) min);
  }

@Override
protected void defineShape(Graphics2D gfx, int resizeFactor) {
	// TODO Auto-generated method stub
	
}
  
}
