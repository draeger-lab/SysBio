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

import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Species;

import de.zbit.sbml.layout.ProcessNode;

import y.base.Edge;
import y.base.Node;
import y.view.Graph2D;

/**
 * Interface to define a data structure which contains a {@link Graph2D} and
 * useful id-node maps.
 * 
 * @author Jakob Matthes
 * @version $Rev$
 */
public interface ILayoutGraph {

	/**
	 * @return the {@link Species}' id -> {@link Set} of {@link SpeciesNode}s
	 *         {@link Map}.
	 */
	public Map<String, Set<Node>> getSpeciesId2nodes();
	
	/**
	 * @return the {@link Compartment} id -> set of {@link CompartmentNode}s
	 *         {@link Map}.
	 */
	public Map<String, Set<Node>> getCompartmentId2nodes();
	
	/**
	 * @return the {@link Reaction}'s id -> set of {@link ProcessNode}s
	 *         {@link Map}.
	 */
	
	public Map<String, Set<Node>> getReactionId2nodes();
	
	/**
	 * @return the {@link Reaction}'s id -> set of list of {@link Edge}s
	 *         {@link Map}.
	 */
	public Map<String, Set<List<Edge>>> getReactionId2edges();
	
	/**
	 * @return the {@link Species}' id -> set of {@link Reaction}s in which a
	 *         {@link Species} is involved in
	 */
	public Map<String, Set<String>> getSpeciesId2reactions();
	
	/**
	 * @return the {@link Graph2D} visual representation.
	 */
	public Graph2D getGraph2D();

}
