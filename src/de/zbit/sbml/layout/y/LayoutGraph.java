/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2013 by the University of Tuebingen, Germany.
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

import org.sbml.jsbml.ext.layout.NamedSBaseGlyph;

import y.base.Edge;
import y.base.Node;
import y.view.Graph2D;

/**
 * Product type used by {@link YLayoutBuilder}. All maps are built by
 * {@link YLayoutBuilder}.
 * 
 * @author Jakob Matthes
 * @version $Rev$
 */
public class LayoutGraph implements ILayoutGraph {

	/**
	 * 
	 */
	private Map<String, Set<Node>> speciesId2nodes;
	/**
	 * 
	 */
	private Map<String, Set<Node>> compartmentId2nodes;
	/**
	 * 
	 */
	private Map<String, Set<Node>> reactionId2nodes;
	/**
	 * 
	 */
	private Map<String, Set<List<Edge>>> reactionId2edges;
	/**
	 * 
	 */
	private Map<String, Set<String>> speciesId2reactions;

	private Map<Node, NamedSBaseGlyph> node2glyph;

	private Graph2D graph2D;
	
	/**
	 * @param speciesId2nodes
	 * @param compartmentId2nodes
	 * @param reactionId2edges
	 * @param graph2d
	 */
	public LayoutGraph(Map<String, Set<Node>> speciesId2nodes,
			Map<String, Set<Node>> compartmentId2nodes,
			Map<String, Set<Node>> reactionId2nodes,
			Map<String, Set<List<Edge>>> reactionId2edges,
			Map<String, Set<String>> speciesId2reactions, 
			Map<Node, NamedSBaseGlyph> node2glyph,
			Graph2D graph2d) {
		this.speciesId2nodes = speciesId2nodes;
		this.compartmentId2nodes = compartmentId2nodes;
		this.reactionId2edges = reactionId2edges;
		this.reactionId2nodes = reactionId2nodes;
		this.speciesId2reactions = speciesId2reactions;
		this.node2glyph = node2glyph;
		graph2D = graph2d;
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.y.ILayoutGraph#getSpeciesId2nodes()
	 */
	@Override
	public Map<String, Set<Node>> getSpeciesId2nodes() {
		return speciesId2nodes;
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.y.ILayoutGraph#getCompartmentId2nodes()
	 */
	@Override
	public Map<String, Set<Node>> getCompartmentId2nodes() {
		return compartmentId2nodes;
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.y.ILayoutGraph#getReactionId2edges()
	 */
	@Override
	public Map<String, Set<List<Edge>>> getReactionId2edges() {
		return reactionId2edges;
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.y.ILayoutGraph#getGraph2D()
	 */
	@Override
	public Graph2D getGraph2D() {
		return graph2D;
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.y.ILayoutGraph#getReactionId2nodes()
	 */
	@Override
	public Map<String, Set<Node>> getReactionId2nodes() {
		return reactionId2nodes;
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.y.ILayoutGraph#getSpeciesId2reactions()
	 */
	@Override
	public Map<String, Set<String>> getSpeciesId2reactions() {
		return speciesId2reactions;
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.y.ILayoutGraph#node2speciesReferenceGlyph()
	 */
	@Override
	public Map<Node, NamedSBaseGlyph> getNode2glyph() {
		return node2glyph;
	}
	
}
