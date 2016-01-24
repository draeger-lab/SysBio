/*
 * $Id$
 * $URL$
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
package de.zbit.sbml.layout.y;

import java.awt.Color;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Map;

import org.sbml.jsbml.ext.layout.Curve;
import org.sbml.jsbml.ext.layout.CurveSegment;
import org.sbml.jsbml.ext.layout.Point;

import y.view.GeneralPathNodePainter;
import y.view.GenericNodeRealizer;
import y.view.LineType;
import y.view.NodeRealizer;
import de.zbit.graph.sbgn.ProcessNodeRealizer;
import de.zbit.graph.sbgn.ReactionNodeRealizer;
import de.zbit.sbml.layout.AbstractSBGNProcessNode;
import de.zbit.sbml.layout.Geometry;
import de.zbit.sbml.layout.ProcessNode;

/**
 * yFiles implementation of process node of type "reaction".
 * 
 * @author Jakob Matthes
 * @version $Rev$
 */
public class YProcessNode extends AbstractSBGNProcessNode<NodeRealizer>
implements ProcessNode<NodeRealizer> {
  
  /**
   * 
   */
  ProcessNodeRealizer processNodeRealizer;
  
  /**
   * 
   */
  public YProcessNode() {
    this(new ReactionNodeRealizer());
  }
  
  /**
   * @param realizer
   *        the specific realizer to be used for drawing this object or its
   *        derivatives.
   */
  YProcessNode(ProcessNodeRealizer realizer) {
    super();
    processNodeRealizer = realizer;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.sbml.layout.SBGNNode#draw(double, double, double, double, double, double)
   */
  @Override
  public NodeRealizer draw(double x, double y, double z, double width,
    double height, double depth) {
    return draw(x, y, z, width, height, depth, 0d, null);
  }
  
  /* (non-Javadoc)
   * @see de.zbit.sbml.layout.AbstractSBGNProcessNode#draw(org.sbml.jsbml.ext.layout.Curve, double, org.sbml.jsbml.ext.layout.Point)
   */
  @Override
  public NodeRealizer draw(Curve curve, double rotationAngle,
    Point rotationCenter) {
    
    //    NodeRealizer nr = new ArbitraryShapeNodeRealizer(Geometry.toGeneralPath(curve));
    
    // Get the factory to register custom styles/configurations.
    
    GeneralPath path = Geometry.toGeneralPath(curve);
    
    GeneralPath gp = Geometry.normalize(path);
    
    GenericNodeRealizer.Factory factory = GenericNodeRealizer.getFactory();
    String configName = "General Path";
    Map<Class<?>, Object> implementationsMap = factory.createDefaultConfigurationMap();
    GeneralPathNodePainter painter = new GeneralPathNodePainter(gp);
    implementationsMap.put(GenericNodeRealizer.Painter.class, painter); //new ShadowNodePainter(
    implementationsMap.put(GenericNodeRealizer.ContainsTest.class, painter);
    factory.addConfiguration(configName, implementationsMap);
    
    //factory.configure(nr, configName);
    GenericNodeRealizer nr = new GenericNodeRealizer(configName);
    
    Rectangle2D bb = path.getBounds2D();
    nr.setFrame(bb.getX(), bb.getY(), bb.getWidth(), bb.getHeight());
    nr.setLineType(LineType.LINE_2);
    nr.setLineColor(Color.BLACK);
    //    nr.setFillColor(Color.BLUE);
    //    nr.setFillColor2(Color.BLACK);
    
    return nr;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.sbml.layout.ProcessNode#draw(double, double, double, double, double, double, double, org.sbml.jsbml.ext.layout.Point)
   */
  @Override
  public NodeRealizer draw(double x, double y, double z, double width,
    double height, double depth, double rotationAngle,
    Point rotationCenter) {
    processNodeRealizer = (ProcessNodeRealizer) processNodeRealizer.createCopy();
    processNodeRealizer.setSize(width, height);
    processNodeRealizer.setLocation(x, y);
    if ((rotationAngle % 180) != 0) {
      processNodeRealizer.setRotationAngle(rotationAngle);
      if (rotationCenter != null) {
        Point2D.Double point = new Point2D.Double();
        point.setLocation(rotationCenter.getX(), rotationCenter.getY());
        processNodeRealizer.setRotationCenter(point);
      }
    }
    return processNodeRealizer;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.sbml.layout.ProcessNode#drawLineSegment(org.sbml.jsbml.ext.layout.CurveSegment, double, org.sbml.jsbml.ext.layout.Point)
   */
  @Override
  public NodeRealizer drawCurveSegment(CurveSegment lineSegment,
    double rotationAngle, Point rotationCenter) {
    // Drawing of single line segments not supported by yFiles implementation.
    return null;
  }
  
}
