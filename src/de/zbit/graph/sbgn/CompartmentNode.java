/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2012 by the University of Tuebingen, Germany.
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

import java.awt.Color;
import java.awt.Graphics2D;

import y.view.NodeRealizer;
import y.view.ShapeNodeRealizer;

/**
 * @author Andreas Dr&auml;ger
 * @date 16:27:56
 * @since 1.1
 * @version $Rev$
 */
public class CompartmentNode extends ShapeNodeRealizer {
	
	/**
	 * 
	 */
	public CompartmentNode() {
		super(ShapeNodeRealizer.ROUND_RECT);
	}
	
	/**
	 * @param compartment
	 */
	public CompartmentNode(NodeRealizer nr) {
		super(nr);
		// If the given node realizer is of this type, then apply copy semantics. 
    if (nr instanceof CompartmentNode) {
    	CompartmentNode fnr = (CompartmentNode) nr;
      // Copy the values of custom attributes (there are none). 
    }
	}
	
	/* (non-Javadoc)
   * @see y.view.ShapeNodeRealizer#createCopy(y.view.NodeRealizer)
   */
  @Override
  public NodeRealizer createCopy(NodeRealizer nr) {
    return new CompartmentNode(nr);
  }
  
  /* (non-Javadoc)
   * @see y.view.ShapeNodeRealizer#paintShapeBorder(java.awt.Graphics2D)
   */
  @Override
  protected void paintShapeBorder(Graphics2D gfx) {
    gfx.setColor(getLineColor());
    gfx.draw(createCompartmentShape());
  }
  
  private CompartmentShape createCompartmentShape() {
		return createCompartmentShape(getX(), getY(), getWidth(), getHeight());
	}

	/* (non-Javadoc)
   * @see y.view.ShapeNodeRealizer#paintFilledShape(java.awt.Graphics2D)
   */
  @Override
  protected void paintFilledShape(Graphics2D gfx) {
  	if (!isTransparent() && getFillColor() != null) {
  		CompartmentShape shape = createCompartmentShape();
  		gfx.setColor(getFillColor());
  		gfx.fill(shape.getOuterShape());
  		gfx.setColor(Color.WHITE);
  		gfx.fill(shape.getInnerShape());
  	}
  }
  
  /**
   * 
   * @param x
   * @param y
   * @param w
   * @param h
   * @return
   */
  public static CompartmentShape createCompartmentShape(double x, double y, double w, double h) {
    return new CompartmentShape(x, y, w, h);
  }

}
