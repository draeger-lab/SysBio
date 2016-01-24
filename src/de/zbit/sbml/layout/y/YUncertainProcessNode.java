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

import y.view.NodeRealizer;
import de.zbit.graph.sbgn.UncertainProcessNodeRealizer;
import de.zbit.sbml.layout.UncertainProcessNode;

/**
 * yFiles implementation of process node of type "uncertain process".
 * 
 * @author Jakob Matthes
 * @version $Rev$
 */
public class YUncertainProcessNode extends YProcessNode implements UncertainProcessNode<NodeRealizer> {
  
  /**
   * 
   */
  public YUncertainProcessNode() {
    super(new UncertainProcessNodeRealizer());
  }
  
}
