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
package de.zbit.sbml.layout.y;

import org.sbml.jsbml.ext.layout.LineSegment;
import org.sbml.jsbml.ext.layout.Point;

import y.view.NodeRealizer;
import de.zbit.graph.sbgn.ReactionNodeRealizer;
import de.zbit.sbml.layout.ProcessNode;

/**
 * @author Jakob Matthes
 * @version $Rev$
 */
public class YProcessNode extends ProcessNode<NodeRealizer> {

	/**
	 * 
	 */
	private ReactionNodeRealizer reactionNodeRealizer;

	/**
	 * 
	 */
	public YProcessNode() {
		reactionNodeRealizer = new ReactionNodeRealizer();
	}
	
	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.SBGNNode#draw(double, double, double, double, double, double)
	 */
	@Override
	public NodeRealizer draw(double x, double y, double z, double width,
			double height, double depth) {
		return draw(x, y, z, width, height, depth, 0d, null);
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.ProcessNode#drawLineSegment(org.sbml.jsbml.ext.layout.LineSegment, double, org.sbml.jsbml.ext.layout.Point)
	 */
	@Override
	public String drawLineSegment(LineSegment lineSegment,
			double rotationAngle, Point rotationCenter) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.ProcessNode#draw(double, double, double, double, double, double, double, org.sbml.jsbml.ext.layout.Point)
	 */
	@Override
	public NodeRealizer draw(double x, double y, double z, double width,
			double height, double depth, double rotationAngle,
			Point rotationCenter) {
		reactionNodeRealizer = (ReactionNodeRealizer) reactionNodeRealizer.createCopy();
		reactionNodeRealizer.setSize(width, height);
		reactionNodeRealizer.setLocation(x, y);
		if ((rotationAngle % 180) != 0) {
			reactionNodeRealizer.setRotationAngle(rotationAngle);
			if (rotationCenter != null) {
				java.awt.Point point = new java.awt.Point();
				point.setLocation(rotationCenter.getX(), rotationCenter.getY());
				reactionNodeRealizer.setRotationCenter(point);
			}
		}
		return reactionNodeRealizer;
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.ProcessNode#getLineWidth()
	 */
	@Override
	public double getLineWidth() {
		return reactionNodeRealizer.getLineWidth();
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.ProcessNode#setLineWidth(double)
	 */
	@Override
	public void setLineWidth(double lineWidth) {
		reactionNodeRealizer.setLineWidth((float) lineWidth);
	}

}
