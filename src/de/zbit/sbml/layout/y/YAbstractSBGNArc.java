/*
 * $Id: YAbstractSBGNArc.java 1388 2016-01-24 05:16:09Z draeger $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SysBio/trunk/src/de/zbit/sbml/layout/y/YAbstractSBGNArc.java $
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
import java.util.logging.Logger;

import org.sbml.jsbml.ext.layout.Curve;
import org.sbml.jsbml.ext.layout.CurveSegment;

import de.zbit.graph.sbgn.DrawingOptions;
import de.zbit.sbml.layout.SBGNArc;
import de.zbit.util.prefs.Option;
import de.zbit.util.prefs.SBPreferences;
import y.view.EdgeRealizer;
import y.view.LineType;

/**
 * Abstract class to represent common features of all SBGN arc classes:
 * <ul>
 * <li>draw(CurveSegment, ...) </li> <!-- is not supported -->
 * <li>draw(Curve, double lineWidth) uses the arc-specific draw(Curve)
 * implementations</li>
 * </ul>
 * 
 * @author Jakob Matthes
 * @version $Rev: 1388 $
 */
public abstract class YAbstractSBGNArc implements SBGNArc<EdgeRealizer> {
  
  /**
   * A {@link Logger} for this class.
   */
  private static final transient Logger logger = Logger.getLogger(YAbstractSBGNArc.class.getName());
  // TODO: change method signatures to also pass the line color
  private Color lineColor = Option.parseOrCast(Color.class, SBPreferences.getPreferencesFor(DrawingOptions.class).get(DrawingOptions.EDGE_LINE_COLOR));
  
  /* (non-Javadoc)
   * @see de.zbit.sbml.layout.SBGNArc#draw(org.sbml.jsbml.ext.layout.CurveSegment, double)
   */
  @Override
  public EdgeRealizer draw(CurveSegment curveSegment, double lineWidth) {
    //logger.warning("Implementation does not support separate drawing of single curve segments!");
    EdgeRealizer edgeRealizer = YLayoutBuilder.drawCurveSegment(curveSegment);
    edgeRealizer.setLineColor(lineColor);
    edgeRealizer.setLineType(LineType.createLineType((float) lineWidth, LineType.CAP_ROUND, LineType.JOIN_ROUND, LineType.LINE_1.getMiterLimit(), LineType.LINE_1.getDashArray(), LineType.LINE_1.getDashPhase()));
    return RealizerTools.setLineWidth(edgeRealizer, lineWidth);
  }
  
  /* (non-Javadoc)
   * @see de.zbit.sbml.layout.SBGNArc#draw(org.sbml.jsbml.ext.layout.Curve, double)
   */
  @Override
  public EdgeRealizer draw(Curve curve, double lineWidth) {
    EdgeRealizer edgeRealizer = draw(curve);
    edgeRealizer.setLineColor(lineColor);
    edgeRealizer.setLineType(LineType.createLineType((float) lineWidth, LineType.CAP_ROUND, LineType.JOIN_ROUND, LineType.LINE_1.getMiterLimit(), LineType.LINE_1.getDashArray(), LineType.LINE_1.getDashPhase()));
    return RealizerTools.setLineWidth(edgeRealizer, lineWidth);
  }
  
}
