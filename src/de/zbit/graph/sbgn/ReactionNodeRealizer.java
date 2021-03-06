/*
 * $Id: ReactionNodeRealizer.java 1407 2017-06-07 20:26:03Z lbuchweitz $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SysBio/trunk/src/de/zbit/graph/sbgn/ReactionNodeRealizer.java $
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2016 by the University of Tuebingen, Germany.
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
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import y.view.NodeRealizer;

/**
 * Realizer for process nodes of type "reaction". Draws the SBGN specified shape
 * (unfilled rectangle).
 *
 * @author Jakob Matthes
 * @version $Rev: 1407 $
 */

public class ReactionNodeRealizer extends ProcessNodeRealizer {
  
  private Boolean whiskers = true;
  /**
   *
   */
  public ReactionNodeRealizer() {
    super();
  }
  
  /**
   *
   */
  public ReactionNodeRealizer(boolean whiskers) {
    super();
    this.whiskers = whiskers;
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
    /*
	  Rectangle2D.Double reactionNode = new Rectangle2D.Double(startOfNode.getX(),
			  startOfNode.getY(), half*resizeFactor, half*resizeFactor);
	  
	  if (whiskers) {
		  //draws whiskers on top and bottom of the reactionGlyph
		  gfx.drawLine((int)(startOfNode.getX()+quart*resizeFactor),
				  	   (int)(startOfNode.getY()),
				  	   (int)(startOfNode.getX()+quart*resizeFactor),
				  	   (int)(startOfNode.getY()-quart*resizeFactor));
		  gfx.drawLine((int)(startOfNode.getX()+quart*resizeFactor),
				  	   (int)(startOfNode.getY()+half*resizeFactor),
				  	   (int)(startOfNode.getX()+quart*resizeFactor),
				  	   (int)(startOfNode.getY()+quart3*resizeFactor));
		   //draws whiskers on left and right of the reactionGlyph
		   gfx.drawLine((int)(startOfNode.getX()-quart*resizeFactor),
				   		(int)(startOfNode.getY()+quart*resizeFactor),
				        (int)(startOfNode.getX()),
				        (int)(startOfNode.getY()+quart*resizeFactor));
		   gfx.drawLine((int)(startOfNode.getX()+half*resizeFactor),
				   		(int)(startOfNode.getY()+quart*resizeFactor),
				   		(int)(startOfNode.getX()+quart3*resizeFactor),
				   		(int)(startOfNode.getY()+quart*resizeFactor));
				   		
	  }
	  
	  Area rxnShape = new Area(reactionNode);
	  gfx.draw(rxnShape);
     */
    
    int extendBesidesBorder = 0;
    int x = (int) getX(); int y = (int) getY();
    //-------------HIER BREITE UND HOEHE DEFINIERT------------------------
    double width = getWidth(), height = getHeight();
    double min = Math.min(width, height);
    double offsetX = (width - min)/2d;
    double offsetY = (height - min)/2d;
    
    gfx.setColor(getLineColor());
    gfx.setStroke(new BasicStroke(lineWidth > 0 ? lineWidth : 1));
    
    int halfHeight = (int) (height/2d);
    
    //---------HIER IST RECHTECK GEMALT----------------
    Rectangle2D.Double rect2d = new Rectangle2D.Double((offsetX + x), (offsetY + y), min, min);
    
    Area rxnShape = new Area(rect2d);
    //    Area rxnShape = new Area(l1);
    //    rxnShape.add(new Area(rect2d));
    //    rxnShape.add(new Area(l2));
    
    //    AffineTransform affineTransform = new AffineTransform();
    //    affineTransform.rotate(Math.toRadians(rotationAngle), rect2d.getCenterX(), rect2d.getCenterY());
    
    //    rxnShape.transform(affineTransform);
    
    //---------HIER ANTENNEN GEMALT-------------------
    if (whiskers) {
      gfx.drawLine((0 + x) - extendBesidesBorder, halfHeight + y, (int) (offsetX + x), halfHeight + y);
      gfx.drawLine((int) (offsetX + min) + x, halfHeight + y, (int) width + x + extendBesidesBorder, halfHeight + y);
    }
    
    gfx.draw(rxnShape);
    
    //    Rectangle2D.Double l1 = new Rectangle2D.Double(0 + x - extendBesidesBorder, halfHeight + y, offsetX, lineWidth > 0 ? lineWidth : 1);
    //    Rectangle2D.Double l2 = new Rectangle2D.Double((offsetX + min) + x, halfHeight + y, offsetX, lineWidth > 0 ? lineWidth : 1);
    
    //    gfx.drawRect((int) (offsetX + x), (int) (offsetY + y), (int) min, (int) min);
    
    // Draw the small reaction lines on both sides, where substrates
    // and products should dock.
    //    gfx.drawLine(0 + x - extendBesidesBorder, halfHeight + y, (int) (offsetX + x), halfHeight + y);
    //    gfx.drawLine((int) (offsetX + min) + x, halfHeight + y, (int) width + x + extendBesidesBorder, halfHeight + y);
    
  }
  
  Point2D startOfNode;
  int resizeFactor;
  double quart;
  double half;
  double quart3;
  
  @Override
  public void defineShape(Graphics2D gfx, int resizeFactor) {
    this.resizeFactor = resizeFactor;
    
    int x = (int) getX();
    int y = (int) getY();
    
    quart = 0.25;
    half = 0.5;
    quart3 = 0.75;
    
    gfx.setColor(getLineColor());
    gfx.setStroke(new BasicStroke(lineWidth > 0 ? lineWidth : 1));
    
    startOfNode = new Point2D.Double(x,y);
  }
  
  /**
   * Returns the value of whiskers
   *
   * @return the value of whiskers
   */
  public Boolean getWhiskers() {
    if (isSetWhiskers()) {
      return whiskers;
    }
    // This is necessary if we cannot return null here.
    return null;
  }
  
  /**
   * Returns whether whiskers is set
   *
   * @return whether whiskers is set
   */
  public boolean isSetWhiskers() {
    return whiskers != null;
  }
  
  /**
   * Sets the value of whiskers
   */
  public void setWhiskers(Boolean whiskers) {
    this.whiskers = whiskers;
  }
  
  /**
   * Unsets the variable whiskers
   *
   * @return {@code true}, if whiskers was set before,
   *         otherwise {@code false}
   */
  public boolean unsetWhiskers() {
    if (isSetWhiskers()) {
      whiskers = null;
      return true;
    }
    return false;
  }
  
}

