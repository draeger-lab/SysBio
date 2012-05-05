package de.zbit.graph.gui;

import de.zbit.graph.sbgn.ReactionNodeRealizer;
import y.base.Edge;
import y.base.Node;
import y.view.Arrow;
import y.view.CreateEdgeMode;
import y.view.EdgeRealizer;
import y.view.Graph2D;

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
    
    ((EdgeRealizer)graph.getRealizer(e2)).setArrow(Arrow.DELTA);

//    nre.fixLayout(reactants, products, modifier);

    return e2;
  }
	
}
