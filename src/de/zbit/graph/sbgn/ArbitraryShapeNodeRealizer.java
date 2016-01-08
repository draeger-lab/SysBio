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

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

import y.view.NodeRealizer;
import y.view.ShapeNodeRealizer;
import de.zbit.util.prefs.SBPreferences;

/**
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 */
public class ArbitraryShapeNodeRealizer extends ShapeNodeRealizer {
  
  /**
   * 
   */
  private static final transient SBPreferences prefs = SBPreferences.getPreferencesFor(de.zbit.graph.sbgn.DrawingOptions.class);
  
  /**
   * 
   */
  private double lineWidth = prefs.getDouble(DrawingOptions.GLYPH_LINE_WIDTH);
  
  /**
   * 
   */
  private Path2D path;
  
  /**
   * 
   * @param path
   */
  public ArbitraryShapeNodeRealizer(Path2D path) {
    super();
    this.path = path;
    Rectangle2D bb = this.path.getBounds();
    setX(bb.getX());
    setY(bb.getY());
    setWidth(bb.getWidth());
    setHeight(bb.getHeight());
    setTransparent(false);
    setVisible(true);
  }
  
  /**
   * 
   * @param realizer
   */
  public ArbitraryShapeNodeRealizer(NodeRealizer realizer) {
    super(realizer);
    if (realizer instanceof ArbitraryShapeNodeRealizer) {
      path = (Path2D) ((ArbitraryShapeNodeRealizer) realizer).getPath().clone();
    }
  }
  
  /* (non-Javadoc)
   * @see y.view.NodeRealizer#createCopy()
   */
  @Override
  public NodeRealizer createCopy() {
    return new ArbitraryShapeNodeRealizer(this);
  }
  
  /**
   * 
   * @return
   */
  Path2D getPath() {
    return path;
  }
  
  /* (non-Javadoc)
   * @see y.view.GenericNodeRealizer#createCopy(y.view.NodeRealizer)
   */
  @Override
  public NodeRealizer createCopy(NodeRealizer nr) {
    return new ArbitraryShapeNodeRealizer(nr);
  }
  
  /* (non-Javadoc)
   * @see y.view.GenericNodeRealizer#paint(java.awt.Graphics2D)
   */
  @Override
  public void paintFilledShape(Graphics2D g) {
    //super.paintFilledShape(g);
    g.setColor(getFillColor());
    g.fill(path);
  }
  
  /* (non-Javadoc)
   * @see y.view.NodeRealizer#paint(java.awt.Graphics2D)
   */
  @Override
  public void paint(Graphics2D gfx) {
    paintNode(gfx);
  }
  
  /* (non-Javadoc)
   * @see y.view.ShapeNodeRealizer#paintNode(java.awt.Graphics2D)
   */
  @Override
  protected void paintNode(Graphics2D gfx) {
    // TODO Auto-generated method stub
    super.paintNode(gfx);
  }
  
  /* (non-Javadoc)
   * @see y.view.GenericNodeRealizer#paintSloppy(java.awt.Graphics2D)
   */
  @Override
  public void paintSloppy(Graphics2D g) {
    paintNode(g);
  }
  
  /* (non-Javadoc)
   * @see y.view.ShapeNodeRealizer#paintShapeBorder(java.awt.Graphics2D)
   */
  @Override
  protected void paintShapeBorder(Graphics2D gfx) {
    super.paintShapeBorder(gfx);
    gfx.setStroke(new BasicStroke((float)
      getLineWidth(),
      BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    gfx.setColor(getLineColor());
    gfx.setBackground(getLineColor());
    gfx.draw(path);
  }
  
  /* (non-Javadoc)
   * @see y.view.NodeRealizer#getBoundingBox()
   */
  @Override
  public Rectangle2D.Double getBoundingBox() {
    Rectangle2D r = path.getBounds();
    return new Rectangle2D.Double(r.getX(), r.getY(), r.getWidth(), r.getHeight());
  }
  
  /**
   * @return the lineWidth
   */
  public double getLineWidth() {
    return lineWidth;
  }
  
  /**
   * @param lineWidth the lineWidth to set
   */
  public void setLineWidth(double lineWidth) {
    this.lineWidth = lineWidth;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(getClass().getSimpleName());
    builder.append(" [lineWidth=");
    builder.append(lineWidth);
    builder.append(", path=");
    builder.append(path);
    builder.append("]");
    return builder.toString();
  }
  
}
