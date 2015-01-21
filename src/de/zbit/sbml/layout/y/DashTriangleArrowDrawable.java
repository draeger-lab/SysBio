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
package de.zbit.sbml.layout.y;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;

import y.view.Arrow;
import y.view.Drawable;
import y.view.ShapeDrawable;

/**
 * Drawable subclass for custom arrow for use with the static yFiles Arrow
 * class, needed for the representation of a necessary stimulation arc.
 * 
 * @author Jakob Matthes
 * @version $Rev$
 */
public class DashTriangleArrowDrawable implements Drawable {
  
  /**
   * Name for the custom arrow --|> to use with y.view.Arrow.
   */
  public static final String DASH_TRIANGLE = "dashTriangle";
  
  private static Arrow baseArrow;
  private static Drawable decoration;
  
  /**
   * Create a dash-triangle arrowhead drawable.
   */
  public DashTriangleArrowDrawable() {
    baseArrow = Arrow.DELTA;
    // TODO: wrong decoration
    decoration = new ShapeDrawable(
      new Ellipse2D.Double(-baseArrow.getArrowLength() - 10 - 1, -5, 10, 10),
      Color.GREEN);
  }
  
  /* (non-Javadoc)
   * @see y.view.Drawable#getBounds()
   */
  @Override
  public Rectangle getBounds() {
    //Rectangle union = decoration.getBounds().union(
    //		new Rectangle(-1, -1, 1, 1));
    Rectangle union = baseArrow.getShape().getBounds().union(
      decoration.getBounds());
    return union;
  }
  
  /* (non-Javadoc)
   * @see y.view.Drawable#paint(java.awt.Graphics2D)
   */
  @Override
  public void paint(Graphics2D g) {
    baseArrow.paint(g, 0d, 0d, 0d, 0d);
    decoration.paint(g);
  }
  
}
