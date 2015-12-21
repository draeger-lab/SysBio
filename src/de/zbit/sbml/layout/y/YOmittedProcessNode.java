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

import y.view.NodeRealizer;
import de.zbit.graph.sbgn.OmittedProcessNodeRealizer;
import de.zbit.sbml.layout.OmittedProcessNode;

/**
 * yFiles implementation of process node of type "omitted process".
 * 
 * @author Jakob Matthes
 * @version $Rev$
 */
public class YOmittedProcessNode extends YProcessNode implements OmittedProcessNode<NodeRealizer> {
  
  /**
   * 
   */
  public YOmittedProcessNode() {
    super(new OmittedProcessNodeRealizer());
  }
  
}
