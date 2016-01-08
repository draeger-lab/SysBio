/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2015 by the University of Tuebingen, Germany.
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
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
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
public class EmptySetNode extends ShapeNodeRealizerSupportingCloneMarker {
  
  /**
   * 
   */
  public EmptySetNode() {
    super(ShapeNodeRealizer.ELLIPSE);
  }
  
  /**
   * 
   * @param nr
   */
  public EmptySetNode(NodeRealizer nr) {
    super(nr);
    // If the given node realizer is of this type, then apply copy
    // semantics.
    if (nr instanceof EmptySetNode) {
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
  public EmptySetNode createCopy(NodeRealizer nr) {
    return new EmptySetNode(nr);
  }
  
  /* (non-Javadoc)
   * @see de.zbit.graph.sbgn.ShapeNodeRealizerSupportingCloneMarker#createCopy()
   */
  @Override
  public EmptySetNode createCopy() {
    return new EmptySetNode(this);
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
      double diameter = Math.max(getWidth(), getHeight());
      setWidth(diameter);
      setHeight(diameter);
      
      // Create a filled circle
      gfx.setColor(fillColor);
      super.paintFilledShape(gfx);
      CloneMarker.Tools.paintLowerBlackIfCloned(gfx, this,
        new Ellipse2D.Double(getX(), getY(), getWidth(), getHeight()));
      
      // Diagonal element:
      gfx.setColor(getLineColor());
      gfx.draw(createDiagonal());
    }
  }
  
  /**
   * 
   * @return
   */
  private Shape createDiagonal() {
    double x = getX(), y = getY(), width = getWidth(), height = getHeight();
    double diameter = Math.max(width, height);
    GeneralPath path = new GeneralPath();
    path.moveTo(x, y + diameter);
    path.lineTo(x + diameter, y);
    path.closePath();
    return path;
  }
  
  /* (non-Javadoc)
   * @see y.view.NodeRealizer#paintSloppy(java.awt.Graphics2D)
   */
  @Override
  public void paintSloppy(Graphics2D g) {
    double diameter = Math.max(getWidth(), getHeight());
    Color fillColor = Color.BLUE;
    if (!isTransparent() && (fillColor != null)) {
      g.setColor(getFillColor());
      g.fill(new Ellipse2D.Double(getX(), getY(), diameter, diameter));
    }
    g.setColor(getLineColor());
    g.draw(new Ellipse2D.Double(getX(), getY(), diameter, diameter));
    //g.draw(createDiagonal());
  }
  
}
