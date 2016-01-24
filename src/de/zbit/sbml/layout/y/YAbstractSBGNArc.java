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

import java.util.logging.Logger;

import org.sbml.jsbml.ext.layout.Curve;
import org.sbml.jsbml.ext.layout.CurveSegment;

import y.view.EdgeRealizer;
import de.zbit.sbml.layout.SBGNArc;

/**
 * Abstract class to represent common features of all SBGN arc classes:
 * <ul>
 * <li>draw(CurveSegment, ...) </li> <!-- is not supported -->
 * <li>draw(Curve, double lineWidth) uses the arc-specific draw(Curve)
 * implementations</li>
 * </ul>
 * 
 * @author Jakob Matthes
 * @version $Rev$
 */
public abstract class YAbstractSBGNArc implements SBGNArc<EdgeRealizer> {
  
  /**
   * A {@link Logger} for this class.
   */
  private static final transient Logger logger = Logger.getLogger(YAbstractSBGNArc.class.getName());
  
  /* (non-Javadoc)
   * @see de.zbit.sbml.layout.SBGNArc#draw(org.sbml.jsbml.ext.layout.CurveSegment, double)
   */
  @Override
  public EdgeRealizer draw(CurveSegment curveSegment, double lineWidth) {
    //logger.warning("Implementation does not support separate drawing of single curve segments!");
    EdgeRealizer edgeRealizer = YLayoutBuilder.drawCurveSegment(curveSegment);
    return RealizerTools.setLineWidth(edgeRealizer, lineWidth);
  }
  
  /* (non-Javadoc)
   * @see de.zbit.sbml.layout.SBGNArc#draw(org.sbml.jsbml.ext.layout.Curve, double)
   */
  @Override
  public EdgeRealizer draw(Curve curve, double lineWidth) {
    EdgeRealizer edgeRealizer = draw(curve);
    return RealizerTools.setLineWidth(edgeRealizer, lineWidth);
  }
  
}
