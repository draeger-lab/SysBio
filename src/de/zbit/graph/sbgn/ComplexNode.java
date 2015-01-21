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

import java.awt.Graphics2D;
import java.awt.Polygon;

import y.view.NodeRealizer;
import y.view.ShapeNodeRealizer;

/**
 * The "Complex"-node is a kind of a <b>group node</b> in SBGN.
 * It is actually a normal rectangle with four cutted edges.
 * See {@link #createPolygon()} for an ASCII-art.
 * 
 * @author Finja B&uuml;chel
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class ComplexNode extends ShapeNodeRealizer implements SimpleCloneMarker {
  
  /**
   * Is this node a cloned node? (I.e. another
   * instance must exist in the same graph).
   */
  private boolean isClonedNode = false;

  /**
   * 
   */
  public ComplexNode() {
    super(ShapeNodeRealizer.ROUND_RECT);
  }
  
  /**
   * 
   * @param nr
   */
  public ComplexNode(NodeRealizer nr) {
    super(nr);
    // If the given node realizer is of this type, then apply copy semantics. 
    if (nr instanceof ComplexNode) {
      ComplexNode fnr = (ComplexNode) nr;
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
    return new ComplexNode(nr);
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
    gfx.draw(createPolygon());
  }
  
  /* (non-Javadoc)
   * @see y.view.ShapeNodeRealizer#paintFilledShape(java.awt.Graphics2D)
   */
  @Override
  protected void paintFilledShape(Graphics2D gfx) {
   if (!isTransparent() && (getFillColor() != null)) {
      gfx.setColor(getFillColor());
      gfx.fill(createPolygon());
      
      CloneMarker.Tools.paintLowerBlackIfCloned(gfx, this, createPolygon());
    }
  }
  
  /**
   * See {@link #createPolygon(double, double, double, double)}
   * @return
   */
  private Polygon createPolygon() {
    return createPolygon(getX(), getY(), getWidth(), getHeight());
  }
  
  /**
   *    * Paints the complex-node.
   * <pre>
   *       1 . . . . . 2
   *     .               .
   *   8                   3
   *   .                   .
   *   .                   .
   *   7                   4
   *    .                 .
   *      6 . . . . . . 5
   * </pre>
   * @param x
   * @param y
   * @param w
   * @param h
   * @return
   */
  public static Polygon createPolygon(double x, double y, double w, double h) {
    int arc = (int) (Math.min(w, h)/5);
    Polygon nodeshape = new Polygon(); 
    nodeshape.addPoint((int)  x+arc,       (int) y);                   // 1
    nodeshape.addPoint((int) (x+w)-arc,    (int) y);                   // 2
    nodeshape.addPoint((int) (x+w),        (int) y+arc);               // 3
    nodeshape.addPoint((int) (x+w),        (int) (y+h-arc));           // 4
    nodeshape.addPoint((int) (x+w)-arc,    (int) (y+h));               // 5
    nodeshape.addPoint((int) (x+arc),      (int) (y+h));               // 6
    nodeshape.addPoint((int)  x,           (int) (y+h-arc));           // 7
    nodeshape.addPoint((int)  x,           (int) (y+arc));             // 8
    nodeshape.addPoint((int)  x+arc,       (int) (y));                 // 1
    
    return nodeshape;    
  }

}
