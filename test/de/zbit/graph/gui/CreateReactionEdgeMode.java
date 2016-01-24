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
package de.zbit.graph.gui;

import y.base.Edge;
import y.base.Node;
import y.view.Arrow;
import y.view.CreateEdgeMode;
import y.view.EdgeRealizer;
import y.view.Graph2D;
import de.zbit.graph.sbgn.ReactionNodeRealizer;

/**
 * 
 * @version $Rev$
 */
public class CreateReactionEdgeMode extends CreateEdgeMode {
  
  /* (non-Javadoc)
   * @see y.view.CreateEdgeMode#createEdge(y.view.Graph2D, y.base.Node, y.base.Node, y.view.EdgeRealizer)
   */
  @Override
  protected Edge createEdge(Graph2D graph, Node startNode, Node targetNode,
    EdgeRealizer realizer) {
    if (graph.getRealizer(targetNode) instanceof ReactionNodeRealizer) {
      // target is a reaction node
      
      Edge e = super.createEdge(graph, startNode, targetNode, realizer);
      
      
      //      ((ReactionNodeRealizer) graph.getRealizer(targetNode)).fixLayout(reactants, products, modifier);
      
      return e;
    }
    // TODO: What if source is a reaction node?
    
    ReactionNodeRealizer nre = new ReactionNodeRealizer();
    Node reactionNode = graph.createNode(nre);
    
    Edge e1 = graph.createEdge(startNode, reactionNode);
    Edge e2 = graph.createEdge(reactionNode, targetNode);;
    
    nre.setCenter((graph.getRealizer(startNode).getCenterX() + graph.getRealizer(targetNode).getCenterX())/2d, (graph.getRealizer(startNode).getCenterY() + graph.getRealizer(targetNode).getCenterY())/2d);
    
    graph.getRealizer(e2).setArrow(Arrow.DELTA);
    
    //    nre.fixLayout(reactants, products, modifier);
    
    return e2;
  }
  
}
