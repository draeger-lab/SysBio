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

import y.view.GenericNodeRealizer;
import y.view.NodeRealizer;
import de.zbit.util.prefs.SBPreferences;

/**
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 */
public class ArbitraryShapeNodeRealizer extends GenericNodeRealizer {
  
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
  public void paint(Graphics2D g) {
    g.setStroke(new BasicStroke((float)
      getLineWidth(),
      BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    g.setColor(getLineColor());
    g.setBackground(getLineColor());
    g.draw(path);
  }
  
  /* (non-Javadoc)
   * @see y.view.GenericNodeRealizer#paintSloppy(java.awt.Graphics2D)
   */
  @Override
  public void paintSloppy(Graphics2D g) {
    paint(g);
  }
  
  /* (non-Javadoc)
   * @see y.view.GenericNodeRealizer#paintNode(java.awt.Graphics2D)
   */
  @Override
  public void paintNode(Graphics2D g) {
    paint(g);
    super.paintNode(g);
  }
  
  /* (non-Javadoc)
   * @see y.view.NodeRealizer#getBoundingBox()
   */
  @Override
  public Rectangle2D.Double getBoundingBox() {
    return (java.awt.geom.Rectangle2D.Double) path.getBounds2D();
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
  
}
