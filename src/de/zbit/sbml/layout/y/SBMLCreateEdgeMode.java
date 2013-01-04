/*
 * $Id:  SBMLCreateEdgeMode.java 21:02:39 Eugen Netz$
 * $URL: SBMLCreateEdgeMode.java$
 * ---------------------------------------------------------------------
 * This file is part of SBML Editor.
 *
 * Copyright (C) 2012-2013 by the University of Tuebingen, Germany.
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

import org.sbml.jsbml.SBO;

import y.base.Edge;
import y.base.Node;
import y.view.Arrow;
import y.view.CreateEdgeMode;
import y.view.EdgeRealizer;
import y.view.Graph2D;
import de.zbit.graph.sbgn.ReactionNodeRealizer;


/**
 * Offers methods to draw reaction nodes and edges.
 * 
 * @author Alexander Diamantikos
 * @author Jakob Matthes
 * @author Eugen Netz
 * @author Jan Rudolph
 * @version $Rev$
 */
public class SBMLCreateEdgeMode extends CreateEdgeMode {
	
	/**
	 * A {@link Logger} for this class.
	 */
	private static final Logger logger = Logger.getLogger(SBMLCreateEdgeMode.class.getName());
	
	/**
	 * Creates a reaction node and the edges to and from that node.
	 * 
	 * @param graph
	 * @param start
	 * @param target
	 * @param realizer
	 * @param reversible
	 * @return the created node
	 */
	public Node createEdgeNode(Graph2D graph, Node start, Node target, EdgeRealizer realizer, boolean reversible) {
		ReactionNodeRealizer nre = new ReactionNodeRealizer();
		Node reactionNode = graph.createNode(nre);
		
		Edge e1 = graph.createEdge(reactionNode, start);
		Edge e2 = graph.createEdge(reactionNode, target);;
		
		nre.setCenter((graph.getRealizer(start).getCenterX() + graph.getRealizer(target).getCenterX())/2d, (graph.getRealizer(start).getCenterY() + graph.getRealizer(target).getCenterY())/2d);
		((EdgeRealizer)graph.getRealizer(e2)).setArrow(Arrow.DELTA);
		
		if (reversible) {
			((EdgeRealizer) graph.getRealizer(e1)).setArrow(Arrow.DELTA);
		}
		return reactionNode;
	}
	
	/**
	 * Creates an edge between the start SpeciesGlyph node and the target ReactionGlyph node for a modifier.
	 * If the SBO-term for the modifier isn't Inhibitor or Catalyst, the edge has a CONCAVE arrow by default.
	 * 
	 * @param graph
	 * @param start
	 * @param target
	 * @param realizer
	 * @param sbo
	 */
	public void createEdge(Graph2D graph, Node start, Node target, EdgeRealizer realizer, int sbo) {
		logger.info("SBO: " + SBO.convertSBO2Alias(sbo));
		if (graph.getRealizer(target) instanceof ReactionNodeRealizer) {
			Edge e = super.createEdge(graph, start, target, realizer);
			if (sbo == SBO.getCatalyst()) {
				// FIXME never entered -> SBO: MODULATION
				graph.getRealizer(e).setArrow(Arrow.TRANSPARENT_CIRCLE);
			} else if (sbo == SBO.getInhibitor()) {
				graph.getRealizer(e).setArrow(Arrow.T_SHAPE);
			} else {
				graph.getRealizer(e).setArrow(Arrow.CONCAVE);
			}
		}
	}

}
