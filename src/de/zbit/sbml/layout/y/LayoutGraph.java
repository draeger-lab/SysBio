/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2012 by the University of Tuebingen, Germany.
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

import java.util.List;
import java.util.Map;
import java.util.Set;

import y.base.Edge;
import y.base.Node;
import y.view.Graph2D;

/**
 * Product type used by YLayoutBuilder. All maps are built by YLayoutBuilder.
 * 
 * @author Jakob Matthes
 * @version $Rev$
 */
public class LayoutGraph implements ILayoutGraph {

	private Map<String, Set<Node>> speciesId2nodes;
	private Map<String, Set<Node>> compartmentId2nodes;
	private Map<String, Set<List<Edge>>> reactionId2edges;
	private Graph2D graph2D;
	
	/**
	 * @param speciesId2nodes
	 * @param compartmentId2nodes
	 * @param reactionId2edges
	 * @param graph2d
	 */
	public LayoutGraph(Map<String, Set<Node>> speciesId2nodes,
			Map<String, Set<Node>> compartmentId2nodes,
			Map<String, Set<List<Edge>>> reactionId2edges, Graph2D graph2d) {
		this.speciesId2nodes = speciesId2nodes;
		this.compartmentId2nodes = compartmentId2nodes;
		this.reactionId2edges = reactionId2edges;
		graph2D = graph2d;
	}

	/**
	 * @return the speciesId2nodes
	 */
	public Map<String, Set<Node>> getSpeciesId2nodes() {
		return speciesId2nodes;
	}

	/**
	 * @return the compartmentId2nodes
	 */
	public Map<String, Set<Node>> getCompartmentId2nodes() {
		return compartmentId2nodes;
	}

	/**
	 * @return the reactionId2edges
	 */
	public Map<String, Set<List<Edge>>> getReactionId2edges() {
		return reactionId2edges;
	}

	/**
	 * @return the graph2D
	 */
	public Graph2D getGraph2D() {
		return graph2D;
	}
	
}
