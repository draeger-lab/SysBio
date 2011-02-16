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
 * Corresponding to the possible Kegg Entry-Types (see 
 * {@link http://www.genome.jp/kegg/xml/docs})
 * 
 * "The type attribute specifies the type of this entry.
 * Note that when the pathway map is linked to another map,
 * the linked pathway map is treated as a node, a clickable
 * graphics object (round rectangle) in the KEGG Web service."
 * 
 * @author wrzodek
 * @version $Rev$
 */
public enum EntryType {
	/**
	 * the node is a KO (ortholog group)
	 * Should be trated as 'protein'.
	 */
	ortholog,
	/**
	 * the node is an enzyme
	 * Should be trated as 'protein'.
	 */
	enzyme,
	/**
	 * the node is a gene product (mostly a protein)
	 * Should be trated as 'protein'.
	 */
	gene,
	/**
	 * the node is a complex of gene products (mostly a protein complex)
	 * Should be trated as 'complex'.
	 */
	group,
	/**
	 * the node is a chemical compound (including a glycan)
	 * Should be trated as 'complex'.
	 */
	compound,
	/**
	 * the node is a linked pathway map
	 * Should be trated as 'complex'.
	 */
	map,
	
  /**
   * ?
   * !Added for compatbility for with KeggPathways version <0.7!
   * Should not be used separately.
   * Should be trated as 'protein'.
   */
  genes,
	
	/**
	 * -Custom Enum-
	 */
	other
	// protein Complex
}
