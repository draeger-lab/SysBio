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
 * Interface to define a data structure which contains a Graph2D and useful
 * id-node maps.
 * 
 * @author Jakob Matthes
 * @version $Rev$
 */
public interface ILayoutGraph {

	/**
	 * @return the species id -> set of species nodes map.
	 */
	public Map<String, Set<Node>> getSpeciesId2nodes();
	
	/**
	 * @return the compartment id -> set of compartment nodes map.
	 */
	public Map<String, Set<Node>> getCompartmentId2nodes();
	
	/**
	 * @return the reaction id -> set of process nodes map.
	 */
	
	public Map<String, Set<Node>> getReactionId2nodes();
	
	/**
	 * @return the reaction id -> set of list of edges map.
	 */
	public Map<String, Set<List<Edge>>> getReactionId2edges();
	
	/**
	 * @return the Graph2D visual representation.
	 */
	public Graph2D getGraph2D();

}
