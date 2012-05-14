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
import java.awt.geom.GeneralPath;

import y.view.NodeRealizer;
import y.view.ShapeNodeRealizer;

/**
 * 
 * @author Finja B&uuml;chel
 * @author Stephanie Tscherneck
 * @since 1.1
 * @version $Rev$
 */
public class EmptyNode extends ShapeNodeRealizerSupportingCloneMarker {
	
	/**
	 * 
	 */
	public EmptyNode() {
		super(ShapeNodeRealizer.ELLIPSE);
	}

	/**
	 * 
	 * @param nr
	 */
	public EmptyNode(NodeRealizer nr) {
		super(nr);
		// If the given node realizer is of this type, then apply copy
		// semantics.
		if (nr instanceof EmptyNode) {
//			EmptyNode enr = (EmptyNode) nr;
			// Copy the values of custom attributes (there are none).
		}
		if (nr instanceof CloneMarker) {
			setNodeIsCloned(((CloneMarker) nr).isNodeCloned());
		}
	}

	/* (non-Javadoc)
	 * @see y.view.ShapeNodeRealizer#createCopy(y.view.NodeRealizer)
	 */
	@Override
	public EmptyNode createCopy(NodeRealizer nr) {
		return new EmptyNode(nr);
	}

	/* (non-Javadoc)
	 * @see de.zbit.graph.sbgn.ShapeNodeRealizerSupportingCloneMarker#createCopy()
	 */
	@Override
	public EmptyNode createCopy() {
		return new EmptyNode(this);
	}

	/* (non-Javadoc)
	 * @see y.view.ShapeNodeRealizer#paintShapeBorder(java.awt.Graphics2D)
	 */
	@Override
	protected void paintShapeBorder(Graphics2D gfx) {
		gfx.setColor(getLineColor());
		setWidth(getHeight());
		super.paintShapeBorder(gfx);
	}

	/* (non-Javadoc)
	 * @see y.view.ShapeNodeRealizer#paintFilledShape(java.awt.Graphics2D)
	 */
	@Override
	protected void paintFilledShape(Graphics2D gfx) {
		Color fillColor = getFillColor();
		if (!isTransparent() && (fillColor != null)) {
			setWidth(getHeight());
			
			// Create a filled circle
			gfx.setColor(fillColor);
			super.paintFilledShape(gfx);
			
			// Diagonal element:
			gfx.setColor(getLineColor());
			GeneralPath path = new GeneralPath();
			path.moveTo(getX(), getY() + getHeight());
			path.lineTo(getX() + getHeight(), getY());
			path.closePath();
			gfx.draw(path);
		}
	}

}
