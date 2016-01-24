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

import y.view.EdgeRealizer;
import y.view.LineType;

/**
 * Tools to facilitate modification of yFiles realizers (NodeRealizer,
 * EdgeRealizer).
 * 
 * @author Jakob Matthes
 * @version $Rev$
 */
public class RealizerTools {
  
  /**
   * Set the line width of an edge realizer without changing any other
   * attribute.
   * 
   * @param edgeRealizer the edge realizer to work on
   * @param lineWidth the line width to set
   * @return edge realizer with modified line width
   */
  public static EdgeRealizer setLineWidth(EdgeRealizer edgeRealizer,
    double lineWidth) {
    LineType currentLineType = edgeRealizer.getLineType();
    LineType lineType = LineType.createLineType((float) lineWidth,
      currentLineType.getEndCap(),
      currentLineType.getLineJoin(),
      currentLineType.getMiterLimit(),
      currentLineType.getDashArray(),
      currentLineType.getDashPhase());
    edgeRealizer.setLineType(lineType);
    return edgeRealizer;
  }
  
}
