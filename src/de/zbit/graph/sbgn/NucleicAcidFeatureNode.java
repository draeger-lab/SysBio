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
 * Copyright (C) 2011-2015 by the University of Tuebingen, Germany.
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
import java.awt.geom.GeneralPath;

import y.view.NodeRealizer;
import y.view.ShapeNodeRealizer;

/**
 * Node for a nucleic acid feature. This is basically
 * a rectangle with normal upper corners and rounded
 * lower corners.
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class NucleicAcidFeatureNode extends ShapeNodeRealizer implements SimpleCloneMarker {
  /**
   * Is this node a cloned node? (I.e. another
   * instance must exist in the same graph).
   */
  private boolean isClonedNode = false;
  
  public NucleicAcidFeatureNode() {
    super(ShapeNodeRealizer.RECT);
  }
  
  public NucleicAcidFeatureNode(NodeRealizer nr) {
    super(nr);
    // If the given node realizer is of this type, then apply copy semantics. 
    if (nr instanceof NucleicAcidFeatureNode) {
      NucleicAcidFeatureNode fnr = (NucleicAcidFeatureNode) nr;
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
  public NodeRealizer createCopy(NodeRealizer nr) {
    return new NucleicAcidFeatureNode(nr);
  }
  
  /* (non-Javadoc)
   * @see y.view.ShapeNodeRealizer#paintShapeBorder(java.awt.Graphics2D)
   */
  @Override
  protected void paintShapeBorder(Graphics2D gfx) {
    gfx.setColor(getLineColor());
    gfx.draw(createPath());
  }
  
  /* (non-Javadoc)
   * @see y.view.ShapeNodeRealizer#paintFilledShape(java.awt.Graphics2D)
   */
  @Override
  protected void paintFilledShape(Graphics2D gfx) {
   if (!isTransparent() && (getFillColor() != null)) {
      gfx.setColor(getFillColor());
      gfx.fill(createPath());
      CloneMarker.Tools.paintLowerBlackIfCloned(gfx, this, createPath());
    }
  }

  /**
   * @return
   */
  protected GeneralPath createPath() {
  	double x = getX(), y = getY(), width = getWidth(), height = getHeight();
    int arc = (int) (width/10d);
    GeneralPath path = new GeneralPath();
    path.moveTo(x, y);
    path.lineTo(x + width, y);
    path.lineTo(x + width, y + height - arc);
    path.quadTo(x + width, y + height, x + width - arc, y + height);
    path.lineTo(x + arc, y + height);
    path.quadTo(x, y + height, x, y + height - arc);
    path.closePath();
    return path;
  }
  
  /* (non-Javadoc)
	 * @see y.view.NodeRealizer#paintSloppy(java.awt.Graphics2D)
	 */
	@Override
	public void paintSloppy(Graphics2D g) {
		GeneralPath path = createPath();
		Color color = getFillColor();
		if (color != null) {
			g.setColor(color);
		}
		g.fill(path);
		color = getLineColor();
		if (color != null) {
			g.setColor(color);
		}
		g.draw(path);
	}

	/* (non-Javadoc)
   * @see de.zbit.graph.CloneMarker#setNodeIsCloned(boolean)
   */
  public void setNodeIsCloned(boolean b) {
    isClonedNode = b;
  }

  /* (non-Javadoc)
   * @see de.zbit.graph.CloneMarker#isNodeCloned()
   */
  public boolean isNodeCloned() {
    return isClonedNode;
  }

}
