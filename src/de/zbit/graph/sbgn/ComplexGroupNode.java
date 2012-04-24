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
 * Copyright (C) 2011 by the University of Tuebingen, Germany.
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

import java.awt.Graphics2D;
import java.awt.Polygon;

import y.view.NodeRealizer;
import y.view.hierarchy.GroupNodeRealizer;

/**
 * The "Complex"-node is a kind of a <b>group node</b> in SBGN.
 * This implementation extends {@link GroupNodeRealizer} and
 * uses {@link ComplexNode} to draw it's content.
 * <p>Thus, in yFiles terms, this is now a real group node.
 * 
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class ComplexGroupNode extends GroupNodeRealizer implements SimpleCloneMarker {
  
  /**
   * Is this node a cloned node? (I.e. another
   * instance must exist in the same graph).
   */
  private boolean isClonedNode=false;

  public ComplexGroupNode() {
    super(new ComplexNode());
  }
  
  public ComplexGroupNode(NodeRealizer nr) {
    super(nr);
    // If the given node realizer is of this type, then apply copy semantics. 
    if (nr instanceof ComplexGroupNode) {
      ComplexGroupNode fnr = (ComplexGroupNode) nr;
      // Copy the values of custom attributes (there are none). 
    }
    if (nr instanceof CloneMarker) {
      setNodeIsCloned(((CloneMarker) nr).isNodeCloned());
    }
  }
  
  public NodeRealizer createCopy(NodeRealizer nr) {
    ComplexGroupNode cgr = new ComplexGroupNode(nr);
    cgr.setNodeIsCloned(isClonedNode);
    return cgr;
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
  

  /* (non-Javadoc)
   * @see y.view.ShapeNodeRealizer#paintShapeBorder(java.awt.Graphics2D)
   */
  @Override
  protected void paintShapeBorder(Graphics2D gfx) {
    gfx.setColor(getLineColor());
    gfx.draw(getPolygon());
  }
  
  /* (non-Javadoc)
   * @see y.view.ShapeNodeRealizer#paintFilledShape(java.awt.Graphics2D)
   */
  @Override
  protected void paintFilledShape(Graphics2D gfx) {
   if (!isTransparent() && getFillColor()!=null) {
      gfx.setColor(getFillColor());
      gfx.fill(getPolygon());
      
      CloneMarker.Tools.paintLowerBlackIfCloned(gfx, this, getPolygon());
    }
  }
  
  /**
   * See {@link ComplexNode#getPolygon(double, double, double, double)}
   * @return
   */
  private Polygon getPolygon() {
    return ComplexNode.getPolygon(getX(), getY(), getWidth(), getHeight());
  }
  
}
