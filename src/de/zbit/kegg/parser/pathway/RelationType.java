/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2011 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.kegg.parser.pathway;

/**
 * Corresponding to the possible Kegg Relation Types (see {@link http
 * ://www.genome.jp/kegg/xml/docs/})
 * 
 * @author Clemens Wrzodek
 * @version $Rev$
 * @since 1.0
 */
public enum RelationType {
	/**
	 * enzyme-enzyme relation, indicating two enzymes catalyzing successive
	 * reaction steps
	 */
	ECrel,
	/**
	 * protein-protein interaction, such as binding and modification
	 */
	PPrel,
	/**
	 * gene expression interaction, indicating relation of transcription factor
	 * and target gene product
	 */
	GErel,
	/**
	 * protein-compound interaction
	 */
	PCrel,
	/**
	 * link to another map
	 */
	maplink,
	/**
	 * 
	 */
	other
}
