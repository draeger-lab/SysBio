/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of KEGGtranslator, a program to convert KGML files
 * from the KEGG database into various other formats, e.g., SBML, GML,
 * GraphML, and many more. Please visit the project homepage at
 * <http://www.cogsys.cs.uni-tuebingen.de/software/KEGGtranslator> to
 * obtain the latest version of KEGGtranslator.
 *
 * Copyright (C) 2010-2015 by the University of Tuebingen, Germany.
 *
 * KEGGtranslator is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.graph.sbgn;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

import org.sbml.jsbml.util.StringTools;

import y.view.NodeRealizer;
import y.view.ShapeNodeRealizer;

/**
 * An extension of {@link ShapeNodeRealizer} supporting a
 * {@link CloneMarker}.
 * 
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class ShapeNodeRealizerSupportingCloneMarker extends ShapeNodeRealizer
    implements SimpleCloneMarker {
  
  /**
   * Is this node a cloned node (i.e., another
   * instance must exist in the same graph)?
   */
  private boolean isClonedNode = false;
  
  
  /**
   * 
   */
  public ShapeNodeRealizerSupportingCloneMarker() {
    super();
  }

  /**
   * @param type
   */
  public ShapeNodeRealizerSupportingCloneMarker(byte type) {
    super(type);
  }

  /**
   * @param type
   * @param x
   * @param y
   * @param label
   */
  public ShapeNodeRealizerSupportingCloneMarker(byte type, double x, double y,
    String label) {
    super(type, x, y, label);
  }

  /**
   * @param argNodeRealizer
   */
  public ShapeNodeRealizerSupportingCloneMarker(NodeRealizer argNodeRealizer) {
    super(argNodeRealizer);
    if (argNodeRealizer instanceof CloneMarker) {
      setNodeIsCloned(((CloneMarker) argNodeRealizer).isNodeCloned());
    }
  }
  
  /* (non-Javadoc)
   * @see y.view.NodeRealizer#createCopy()
   */
  @Override
  public NodeRealizer createCopy() {
    return new ShapeNodeRealizerSupportingCloneMarker(this);
  }
  
  /* (non-Javadoc)
   * @see de.zbit.graph.CloneMarker#isNodeCloned()
   */
  public boolean isNodeCloned() {
    return isClonedNode;
  }

  /* (non-Javadoc)
   * @see y.view.ShapeNodeRealizer#paintFilledShape(java.awt.Graphics2D)
   */
  @Override
  protected void paintFilledShape(Graphics2D gfx) {
  	super.paintFilledShape(gfx);
  	if (!isTransparent() && (getFillColor() != null)) {
  		CloneMarker.Tools.paintLowerBlackIfCloned(gfx, this, shape);
  	}
  }
  
  /* (non-Javadoc)
	 * @see y.view.NodeRealizer#paintSloppy(java.awt.Graphics2D)
	 */
	@Override
	public void paintSloppy(Graphics2D g) {
		Color color = getFillColor();
		if (color != null) {
			g.setColor(color);
		}
		switch (getShapeType()) {
			case ELLIPSE:
				Ellipse2D ellipse = new Ellipse2D.Double(getX(), getY(), getWidth(), getHeight());
				g.fill(ellipse);
				color = getLineColor();
				if (color != null) {
					g.setColor(color);
					g.draw(ellipse);
				}
				break;
			case ROUND_RECT:
				double x = getX(), y = getY(), width = getWidth(), height = getHeight();
				RoundRectangle2D rectangle = new RoundRectangle2D.Double(
					x, y, width, height, width * .25d, height * .25d);
				g.fill(rectangle);
				color = getLineColor();
				if (color != null) {
					g.setColor(color);
					g.draw(rectangle);
				}
				break;
			default:
				super.paintSloppy(g);
				break;
		}
	}

	/* (non-Javadoc)
   * @see de.zbit.graph.CloneMarker#setNodeIsCloned(boolean)
   */
  public void setNodeIsCloned(boolean b) {
    isClonedNode = b;
  }

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return StringTools.concat(getClass().getSimpleName(), " [isClonedNode=",
			isClonedNode, ", x=", x, ", y=", y, ", width=", width, ", height=",
			height, ", fill=", getFillColor(), ", line=", getLineColor(),
			"]").toString();
	}

}
