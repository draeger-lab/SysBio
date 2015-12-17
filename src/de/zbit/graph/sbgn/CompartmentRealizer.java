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
import java.awt.Color;
import java.awt.Graphics2D;

import y.geom.YInsets;
import y.layout.DiscreteNodeLabelModel;
import y.view.NodeLabel;
import y.view.NodeRealizer;
import y.view.hierarchy.GroupNodeRealizer;
import de.zbit.util.prefs.SBPreferences;

/**
 * @author Andreas Dr&auml;ger
 * @date 13:28:25
 * @since 1.1
 * @version $Rev$
 */
public class CompartmentRealizer extends GroupNodeRealizer {
  
  /**
   * 
   */
  private static final transient SBPreferences prefs = SBPreferences.getPreferencesFor(de.zbit.graph.sbgn.DrawingOptions.class);
  
  /**
   * 
   */
  private Color interFillColor;
  
  /**
   * @return the interFillColor
   */
  public Color getInterFillColor() {
    return interFillColor;
  }
  
  /**
   * @param interFillColor the interFillColor to set
   */
  public void setInterFillColor(Color interFillColor) {
    this.interFillColor = interFillColor;
  }
  
  /**
   * 
   */
  public CompartmentRealizer() {
    super();
    setGroupClosed(false);
    setTransparent(false);
    
    // Eliminate the expanding/ collapsing icons
    setClosedGroupIcon(null);
    setOpenGroupIcon(null);
    
    setMinimalInsets(new YInsets(15, 15, 15, 15)); // top, left, bottom, right
    setAutoBoundsEnabled(true);
    NodeLabel label = getLabel();
    // TODO DiscreteNodeLabelModel does not work as intended
    label.setLabelModel(new DiscreteNodeLabelModel(DiscreteNodeLabelModel.BOTTOM, 5d));
    label.setBackgroundColor(null);
    label.setTextColor(Color.BLACK);
  }
  
  /**
   * @param nr
   */
  public CompartmentRealizer(NodeRealizer nr) {
    super(nr);
    // If the given node realizer is of this type, then apply copy semantics.
    if (nr instanceof CompartmentRealizer) {
      CompartmentRealizer fnr = (CompartmentRealizer) nr;
      // Copy the values of custom attributes (there are none).
    }
  }
  
  /* (non-Javadoc)
   * @see y.view.hierarchy.GroupNodeRealizer#createCopy(y.view.NodeRealizer)
   */
  @Override
  public CompartmentRealizer createCopy(NodeRealizer nr) {
    return new CompartmentRealizer(this);
  }
  
  /* (non-Javadoc)
   * @see y.view.ShapeNodeRealizer#paintShapeBorder(java.awt.Graphics2D)
   */
  @Override
  protected void paintShapeBorder(Graphics2D gfx) {
    CompartmentShape shape = createCompartmentShape();
    gfx.setColor(getLineColor());
    gfx.draw(shape);
    gfx.setColor(getInterFillColor());
    gfx.fill(shape);
  }
  
  /* (non-Javadoc)
   * @see y.view.ShapeNodeRealizer#paintFilledShape(java.awt.Graphics2D)
   */
  @Override
  protected void paintFilledShape(Graphics2D gfx) {
    if (!isTransparent() && (getFillColor() != null) &&
        prefs.getBoolean(DrawingOptions.COMPARTMENT_FILLED)) {
      CompartmentShape shape = createCompartmentShape();
      gfx.setColor(getFillColor());
      gfx.fill(shape.getInnerArea());
    }
  }
  
  /* (non-Javadoc)
   * @see y.view.hierarchy.GroupNodeRealizer#paintSloppy(java.awt.Graphics2D)
   */
  @Override
  public void paintSloppy(Graphics2D g) {
    Color color = getFillColor();
    if (!isTransparent() && (color != null)) {
      g.setColor(color);
      CompartmentShape shape = createCompartmentShape();
      if (prefs.getBoolean(DrawingOptions.COMPARTMENT_FILLED)) {
        g.fill(shape.getInnerArea());
      }
      color = getLineColor();
      if (color != null) {
        g.setStroke(new BasicStroke((float)
          prefs.getDouble(DrawingOptions.COMPARTMENT_LINE_WIDTH),
          BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(color);
        g.draw(shape.getInnerArea());
      }
    } else {
      super.paintSloppy(g);
    }
  }
  
  /**
   * 
   * @return
   */
  private CompartmentShape createCompartmentShape() {
    return new CompartmentShape(getX(), getY(), getWidth(), getHeight());
  }
  
}
