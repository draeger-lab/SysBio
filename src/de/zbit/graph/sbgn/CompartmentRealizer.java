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

import y.geom.YInsets;
import y.view.NodeRealizer;
import y.view.hierarchy.GroupNodeRealizer;

/**
 * @author Andreas Dr&auml;ger
 * @date 13:28:25
 * @since 1.1
 * @version $Rev$
 */
public class CompartmentRealizer extends GroupNodeRealizer {
	
	/**
	 * 
	 */
	public CompartmentRealizer() {
		super();
    setGroupClosed(false);
    setTransparent(false);
    
    // Eliminate the expanding/ collapsing icons
    setClosedGroupIcon(null);
    setOpenGroupIcon(null);
    
    setMinimalInsets(new YInsets(7, 7, 7, 7)); // top, left, bottom, right
    setAutoBoundsEnabled(true);
	}
	
	/**
	 * @param nr
	 */
	public CompartmentRealizer(NodeRealizer nr) {
		super(nr);
		// If the given node realizer is of this type, then apply copy semantics. 
    if (nr instanceof CompartmentRealizer) {
    	CompartmentRealizer fnr = (CompartmentRealizer) nr;
      // Copy the values of custom attributes (there are none). 
    }
	}
	
	/* (non-Javadoc)
	 * @see y.view.hierarchy.GroupNodeRealizer#createCopy(y.view.NodeRealizer)
	 */
	@Override
	public CompartmentRealizer createCopy(NodeRealizer nr) {
		return new CompartmentRealizer(this);
	}

  /* (non-Javadoc)
   * @see y.view.ShapeNodeRealizer#paintShapeBorder(java.awt.Graphics2D)
   */
  @Override
  protected void paintShapeBorder(Graphics2D gfx) {
    gfx.setColor(getLineColor());
    gfx.draw(createCompartmentShape());
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
   * @return
   */
  private CompartmentShape createCompartmentShape() {
    return CompartmentNode.createCompartmentShape(getX(), getY(), getWidth(), getHeight());
  }

}
