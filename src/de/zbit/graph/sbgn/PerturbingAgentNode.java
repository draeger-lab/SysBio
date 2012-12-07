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

import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;

import y.view.NodeRealizer;
import y.view.ShapeNodeRealizer;

/**
 * @author Andreas Dr&auml;ger
 * @date 08:26:35
 * @since 1.1
 * @version $Rev$
 */
public class PerturbingAgentNode extends ShapeNodeRealizerSupportingCloneMarker {
	
	/**
	 * 
	 */
	public PerturbingAgentNode() {
		super();
	}
	
	/**
	 * @param type
	 * @param x
	 * @param y
	 * @param label
	 */
	public PerturbingAgentNode(byte type, double x, double y, String label) {
		super(type, x, y, label);
	}
	
	/**
	 * @param type
	 */
	public PerturbingAgentNode(byte type) {
		super(type);
	}
	
	/**
	 * @param argNodeRealizer
	 */
	public PerturbingAgentNode(NodeRealizer pan) {
		super(pan);
		if (pan instanceof CloneMarker) {
			setNodeIsCloned(((CloneMarker) pan).isNodeCloned());
		}
	}
	
  /* (non-Javadoc)
   * @see y.view.ShapeNodeRealizer#paintShapeBorder(java.awt.Graphics2D)
   */
  @Override
  protected void paintShapeBorder(Graphics2D gfx) {
    gfx.setColor(getLineColor());
    gfx.draw(getPath());
  }
  
  /* (non-Javadoc)
   * @see y.view.ShapeNodeRealizer#paintFilledShape(java.awt.Graphics2D)
   */
  @Override
  protected void paintFilledShape(Graphics2D gfx) {
   if (!isTransparent() && (getFillColor() != null)) {
      gfx.setColor(getFillColor());
      gfx.fill(getPath());
      
      CloneMarker.Tools.paintLowerBlackIfCloned(gfx, this, getPath());
    }
  }

  @Override
  public NodeRealizer createCopy(NodeRealizer nr) {
  	return new PerturbingAgentNode(nr);
  }
  
  /* (non-Javadoc)
	 * @see de.zbit.graph.sbgn.ShapeNodeRealizerSupportingCloneMarker#createCopy()
	 */
	@Override
	public NodeRealizer createCopy() {
		return new PerturbingAgentNode(this);
	}

	/**
   * @return
   */
  protected GeneralPath getPath() {
    GeneralPath path = new GeneralPath();
    double x = getX(), y = getY(), w = getWidth(), h = getHeight(), h_2 = h/2d;
    int a = (int) (w/10d);
    path.moveTo(x, y);
    path.lineTo(x + w, y);
    path.lineTo(x + w - a, y + h_2);
    path.lineTo(x + w, y + h);
    path.lineTo(x, y + h);
    path.lineTo(x + a, y + h_2);
    path.lineTo(x, y);
    path.closePath();
    return path;
  }
	
}
