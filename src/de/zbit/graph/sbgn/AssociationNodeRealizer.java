/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2013 by the University of Tuebingen, Germany.
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

import y.view.NodeRealizer;
import y.view.ShapeNodeRealizer;

public class AssociationNodeRealizer extends ProcessNodeRealizer {

	public AssociationNodeRealizer(NodeRealizer nr) {
		super(nr);
		// If the given node realizer is of this type, then apply copy semantics. 
		if (nr instanceof AssociationNodeRealizer) {
			ReactionNodeRealizer fnr = (ReactionNodeRealizer) nr;
			lineWidth = fnr.lineWidth;
			rotationAngle = fnr.rotationAngle;
			rotationCenter = fnr.rotationCenter;
		}
	}

	/* (non-Javadoc)
	 * @see y.view.ShapeNodeRealizer#createCopy(y.view.NodeRealizer)
	 */
	public NodeRealizer createCopy(NodeRealizer nr) {
		return new AssociationNodeRealizer(nr);
	}

	public AssociationNodeRealizer() {
		super(ShapeNodeRealizer.RECT);
		setHeight(10);
		setWidth(getHeight() * 2);
	}

	@Override
  protected void drawShape(Graphics2D gfx) {
	  // TODO draw filled circle
	  
  }

}