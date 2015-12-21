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
package de.zbit.graph.sbgn;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.Set;

import org.sbml.jsbml.util.StringTools;

import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Node;
import y.geom.YPoint;
import y.view.EdgeRealizer;
import y.view.Graph2D;
import y.view.NodeRealizer;
import y.view.ShapeNodeRealizer;
import de.zbit.math.MathUtils;

/**
 * Superclass for all reaction nodes. Subclasses decide which specific shape to
 * draw.
 *
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public abstract class ProcessNodeRealizer extends ShapeNodeRealizer {
  
  /**
   * line width of reaction node
   */
  protected float lineWidth;
  protected double rotationAngle;
  protected Point2D.Double rotationCenter;
  
  /**
   *
   */
  public ProcessNodeRealizer() {
    super(ShapeNodeRealizer.RECT);
    setHeight(10);
    setWidth(getHeight() * 2);
  }
  
  /**
   *
   * @param nr
   */
  public ProcessNodeRealizer(NodeRealizer nr) {
    super(nr);
    // If the given node realizer is of this type, then apply copy semantics.
    if (nr instanceof ProcessNodeRealizer) {
      ProcessNodeRealizer fnr = (ProcessNodeRealizer) nr;
      lineWidth = fnr.lineWidth;
      rotationAngle = fnr.rotationAngle;
      rotationCenter = fnr.rotationCenter;
    }
  }
  
  /**
   * 
   * @param shape
   */
  public ProcessNodeRealizer(byte shape) {
    super(shape);
  }
  
  /* (non-Javadoc)
   * @see y.view.ShapeNodeRealizer#paintShapeBorder(java.awt.Graphics2D)
   */
  @Override
  protected void paintShapeBorder(Graphics2D gfx) {
    int extendBesidesBorder = 0;
    int x = (int) getX(); int y = (int) getY();
    double width = getWidth(), height = getHeight();
    double min = Math.min(width, height);
    double offsetX = (width - min)/2d;
    double offsetY = (height - min)/2d;
    
    //    rotate(gfx, rotationAngle, rotationCenter);
    
    drawShape(gfx);
    
    int halfHeight = (int) (height/2d);
    
    // Draw the small reaction lines on both sides, where substrates
    // and products should dock.
    // TODO: Deal with Whiskers!!!
    //        gfx.drawLine((0 + x) - extendBesidesBorder, halfHeight + y, (int) (offsetX + x), halfHeight + y);
    //        gfx.drawLine((int) (offsetX + min) + x, halfHeight + y, (int) width + x + extendBesidesBorder, halfHeight + y);
    
    //    rotate(gfx, -rotationAngle, rotationCenter);
  }
  
  /**
   * Draw the shape of the process node. Each subclass provides its own shape.
   *
   * @param gfx
   */
  protected abstract void drawShape(Graphics2D gfx);
  
  /**
   * Configures the orientation of this node and the docking point
   * for all adjacent edges to match the given parameters.
   * <p>Still, you should make sure that all reactants are on one side
   * of the node and all products on the other one!
   */
  public void fixLayout(Set<Node> reactants, Set<Node> products, Set<Node> modifier) {
    Node no = getNode();
    Graph2D graph = (Graph2D) no.getGraph();
    
    // Determine location of adjacent nodes
    Integer[] cases = new Integer[4];
    double[] meanDiff = new double[2];
    Arrays.fill(cases, 0);
    Arrays.fill(meanDiff, 0); // [0] = X, [1] = Y
    //	    int reactantsLeft=0;
    //	    int reactantsAbove=1;
    //	    int productsLeft=2;
    //	    int productsAbove=3;
    
    for (EdgeCursor ec = no.edges(); ec.ok(); ec.next()) {
      Edge v = ec.edge();
      Node other = v.opposite(no);
      NodeRealizer nr = graph.getRealizer(other);
      
      double distanceX = Math.abs(getCenterX() - nr.getCenterX());
      double distanceY = Math.abs(getCenterY() - nr.getCenterY());
      if (reactants.contains(other)) {
        meanDiff[0]+= distanceX;
        meanDiff[1]+= distanceY;
        
        // Count cases, but require at least 5 pixels difference
        if ((distanceX>5) && (nr.getCenterX()<getCenterX())) {
          cases[0]++;
        }
        if ((distanceY>5) && (nr.getCenterY()<getCenterY())) {
          cases[1]++;
        }
      } else if (products.contains(other)) {
        meanDiff[0]+= distanceX;
        meanDiff[1]+= distanceY;
        
        // Count cases, but require at least 5 pixels difference
        if ((distanceX>5) && (nr.getCenterX()<getCenterX())) {
          cases[2]++;
        }
        if ((distanceY>5) && (nr.getCenterY()<getCenterY())) {
          cases[3]++;
        }
      }
    }
    
    // Determine orientation of this node
    double max = MathUtils.max(Arrays.asList(cases));
    boolean horizontal = isHorizontal();
    if (((cases[0] == max) && (cases[1] == max)) ||
        ((cases[2] == max) && (cases[3] == max))) {
      // If same number is above and left, let the distance decide the orientation.
      if (meanDiff[0]>meanDiff[1]) { // X-Distance larger
        if (!horizontal) {
          rotateNode();
        }
      } else { // Y-Distance larger
        if (horizontal) {
          rotateNode();
        }
      }
    }
    else if ((cases[0] == max) && !horizontal) {
      rotateNode();
    } else if ((cases[1] == max) && horizontal) {
      rotateNode();
    } else if ((cases[2] == max) && !horizontal) {
      rotateNode();
    } else if ((cases[3] == max) && horizontal) {
      rotateNode();
    }
    
    // Dock all edges to the correct side
    if (isHorizontal()) {
      if (cases[0]>cases[2]) {
        // Reactants are left of this node
        setEdgesToDockOnLeftSideOfNode(reactants);
        setEdgesToDockOnRightSideOfNode(products);
      } else {
        setEdgesToDockOnLeftSideOfNode(products);
        setEdgesToDockOnRightSideOfNode(reactants);
      }
    } else {
      if (cases[1] > cases[3]) {
        // Reactants are above this node;
        setEdgesToDockOnUpperSideOfNode(reactants);
        setEdgesToDockOnLowerSideOfNode(products);
      } else {
        setEdgesToDockOnUpperSideOfNode(products);
        setEdgesToDockOnLowerSideOfNode(reactants);
      }
    }
    // TODO: reaction modifiers dock on square
    
  }
  
  /**
   *
   * @return
   */
  public float getLineWidth() {
    return lineWidth;
  }
  
  /**
   * True if the node is painted in a horizontal layout.
   * Use {@link #rotateNode()} to change the rotation.
   */
  public boolean isHorizontal() {
    return getWidth() >= getHeight();
  }
  
  /**
   * @param adjacentNodes
   * @param dockToPoint
   */
  protected void letAllEdgesDockToThisPointOnThisNode(Set<Node> adjacentNodes,
    YPoint dockToPoint) {
    Node no = getNode();
    Graph2D graph = (Graph2D) no.getGraph();
    
    for (EdgeCursor ec = no.edges(); ec.ok(); ec.next()) {
      Edge v = ec.edge();
      Node other = v.opposite(no);
      EdgeRealizer er = graph.getRealizer(v);
      boolean isSource = v.source().equals(no);
      
      if (adjacentNodes.contains(other)) {
        if (isSource) {
          er.setSourcePoint(dockToPoint);
        } else {
          er.setTargetPoint(dockToPoint);
        }
      }
    }
  }
  
  /* (non-Javadoc)
   * @see y.view.ShapeNodeRealizer#paintFilledShape(java.awt.Graphics2D)
   */
  @Override
  protected void paintFilledShape(Graphics2D gfx) {
    // Do NOT fill it.
  }
  
  /* (non-Javadoc)
   * @see y.view.NodeRealizer#paintSloppy(java.awt.Graphics2D)
   */
  @Override
  public void paintSloppy(Graphics2D g) {
    paintShapeBorder(g);
    //			g.draw(new Rectangle(
    //				new Point((int) Math.round(getX()), (int) Math.round(getY())),
    //				new Dimension((int) Math.round(getWidth()), (int) Math.round(getHeight()))));
  }
  
  /**
   *
   * @param gfx
   * @param rotationAngle
   * @param rotationCenter
   */
  protected void rotate(Graphics2D gfx, double rotationAngle, java.awt.geom.Point2D.Double rotationCenter) {
    if ((rotationAngle % 180) != 0) {
      if (rotationCenter != null) {
        gfx.rotate(Math.toRadians(rotationAngle), rotationCenter.getX(), rotationCenter.getY());
      } else {
        gfx.rotate(Math.toRadians(rotationAngle));
      }
    }
  }
  
  /**
   * Rotates the node by 90&deg;.
   */
  public void rotateNode() {
    double width = getWidth(), height = getHeight();
    setWidth(height);
    setHeight(width);
  }
  
  /**
   * @param adjacentNodes
   */
  private void setEdgesToDockOnLeftSideOfNode(Set<Node> adjacentNodes) {
    YPoint dockToPoint = new YPoint((getWidth()/2)*-1,0);
    letAllEdgesDockToThisPointOnThisNode(adjacentNodes, dockToPoint);
  }
  
  /**
   * @param adjacentNodes
   */
  private void setEdgesToDockOnLowerSideOfNode(Set<Node> adjacentNodes) {
    YPoint dockToPoint = new YPoint(0,getHeight()/2);
    letAllEdgesDockToThisPointOnThisNode(adjacentNodes, dockToPoint);
  }
  
  /**
   * @param adjacentNodes
   */
  private void setEdgesToDockOnRightSideOfNode(Set<Node> adjacentNodes) {
    YPoint dockToPoint = new YPoint(getWidth()/2,0);
    letAllEdgesDockToThisPointOnThisNode(adjacentNodes, dockToPoint);
  }
  
  /**
   * @param adjacentNodes
   */
  private void setEdgesToDockOnUpperSideOfNode(Set<Node> adjacentNodes) {
    YPoint dockToPoint = new YPoint(0,(getHeight()/2)*-1);
    letAllEdgesDockToThisPointOnThisNode(adjacentNodes, dockToPoint);
  }
  
  /**
   * sets line width of {@link ReactionNodeRealizer}. If not set, line
   * width will be 1.
   *
   * @param lineWidth
   */
  public void setLineWidth(float lineWidth) {
    this.lineWidth = lineWidth;
  }
  
  /**
   *
   * @param rotationAngle
   */
  public void setRotationAngle(double rotationAngle) {
    this.rotationAngle = rotationAngle;
  }
  
  /**
   *
   * @param rotationCenter
   */
  public void setRotationCenter(java.awt.geom.Point2D.Double rotationCenter) {
    this.rotationCenter = rotationCenter;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return StringTools.concat(getClass().getSimpleName(),
      " [x=", x,
      ", y=", y,
      ", width=", width,
      ", height=", height,
      ", rotationCenter=", rotationCenter,
      ", rotationAngle=", rotationAngle,
      ", fill=", getFillColor(), ", line=", getLineColor(),
        "]").toString();
  }
  
  
}
