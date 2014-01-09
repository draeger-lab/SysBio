/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2014 by the University of Tuebingen, Germany.
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

import java.awt.geom.Point2D;

import org.sbml.jsbml.ext.layout.LineSegment;
import org.sbml.jsbml.ext.layout.Point;

import y.view.NodeRealizer;
import de.zbit.graph.sbgn.DissociationNodeRealizer;
import de.zbit.graph.sbgn.ProcessNodeRealizer;
import de.zbit.sbml.layout.DissociationNode;

/**
 * yFiles implementation of process node of type "dissociation".
 * 
 * @author Jakob Matthes
 * @version $Rev$
 */
public class YDissociationNode extends DissociationNode<NodeRealizer> {

	protected ProcessNodeRealizer processNodeRealizer;

	public YDissociationNode() {
	  super();
	  processNodeRealizer = new DissociationNodeRealizer();
  }
	
	@Override
	public NodeRealizer draw(double x, double y, double z, double width,
			double height, double depth) {
		return draw(x, y, z, width, height, depth, 0d, null);
	}

	@Override
	public NodeRealizer drawLineSegment(LineSegment lineSegment,
			double rotationAngle, Point rotationCenter) {
		// Drawing of single line segments not supported by yFiles implementation. 
		return null;
	}

	@Override
	public NodeRealizer draw(double x, double y, double z, double width,
			double height, double depth, double rotationAngle,
			Point rotationCenter) {
		processNodeRealizer = (DissociationNodeRealizer) processNodeRealizer.createCopy();
		processNodeRealizer.setSize(width, height);
		processNodeRealizer.setLocation(x, y);
		if ((rotationAngle % 180) != 0) {
			processNodeRealizer.setRotationAngle(rotationAngle);
			if (rotationCenter != null) {
				java.awt.geom.Point2D.Double point = new Point2D.Double();
				point.setLocation(rotationCenter.getX(), rotationCenter.getY());
				processNodeRealizer.setRotationCenter(point);
			}
		}
		return processNodeRealizer;
	}

	@Override
	public double getLineWidth() {
		return processNodeRealizer.getLineWidth();
	}

	@Override
	public void setLineWidth(double lineWidth) {
		processNodeRealizer.setLineWidth((float) lineWidth);
	}

}
