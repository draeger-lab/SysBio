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
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;

import y.view.NodeRealizer;


/**
 * Realizer for process nodes of type "reaction". Draws the SBGN specified shape
 * (unfilled rectangle).
 * 
 * @author Jakob Matthes
 * @version $Rev$
 */
public class ReactionNodeRealizer extends ProcessNodeRealizer {

  /**
   * 
   */
  public ReactionNodeRealizer() {
    super();
  }

  /**
   * 
   * @param nr
   */
  public ReactionNodeRealizer(NodeRealizer nr) {
    super(nr);
  }

  /* (non-Javadoc)
   * @see y.view.ShapeNodeRealizer#createCopy(y.view.NodeRealizer)
   */
  @Override
  public NodeRealizer createCopy(NodeRealizer nr) {
    return new ReactionNodeRealizer(nr);
  }

  /* (non-Javadoc)
   * @see de.zbit.graph.sbgn.ProcessNodeRealizer#drawShape(java.awt.Graphics2D)
   */
  @Override
  protected void drawShape(Graphics2D gfx) {

    int extendBesidesBorder = 0;
    int x = (int) getX(); int y = (int) getY();
    double width = getWidth(), height = getHeight();
    double min = Math.min(width, height);
    double offsetX = (width - min)/2d;
    double offsetY = (height - min)/2d;

    gfx.setColor(getLineColor());
    gfx.setStroke(new BasicStroke(lineWidth > 0 ? lineWidth : 1));

    int halfHeight = (int) (height/2d);

    Rectangle2D.Double rect2d = new Rectangle2D.Double((offsetX + x), (offsetY + y), min, min);
    //    Rectangle2D.Double l1 = new Rectangle2D.Double(0 + x - extendBesidesBorder, halfHeight + y, offsetX, lineWidth > 0 ? lineWidth : 1);
    //    Rectangle2D.Double l2 = new Rectangle2D.Double((offsetX + min) + x, halfHeight + y, offsetX, lineWidth > 0 ? lineWidth : 1);

    Area rxnShape = new Area(rect2d);
    //    Area rxnShape = new Area(l1);
    //    rxnShape.add(new Area(rect2d));
    //    rxnShape.add(new Area(l2));

    AffineTransform affineTransform = new AffineTransform();
    //    affineTransform.rotate(Math.toRadians(rotationAngle), rect2d.getCenterX(), rect2d.getCenterY());

    rxnShape.transform(affineTransform);
    gfx.draw(rxnShape);



    //    Rectangle2D.Double l1 = new Rectangle2D.Double(0 + x - extendBesidesBorder, halfHeight + y, offsetX, lineWidth > 0 ? lineWidth : 1);
    //    Rectangle2D.Double l2 = new Rectangle2D.Double((offsetX + min) + x, halfHeight + y, offsetX, lineWidth > 0 ? lineWidth : 1);

    //    gfx.drawRect((int) (offsetX + x), (int) (offsetY + y), (int) min, (int) min);





    // Draw the small reaction lines on both sides, where substrates
    // and products should dock.
    //    gfx.drawLine(0 + x - extendBesidesBorder, halfHeight + y, (int) (offsetX + x), halfHeight + y);
    //    gfx.drawLine((int) (offsetX + min) + x, halfHeight + y, (int) width + x + extendBesidesBorder, halfHeight + y);




  }

}
