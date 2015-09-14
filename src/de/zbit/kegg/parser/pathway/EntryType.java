/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2015 by the University of Tuebingen, Germany.
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
 * Corresponding to the possible KEGG Entry-Types (see
 * {@link http://www.genome.jp/kegg/xml/docs})
 * 
 * "The type attribute specifies the type of this entry.
 * Note that when the pathway map is linked to another map,
 * the linked pathway map is treated as a node, a clickable
 * graphics object (round rectangle) in the KEGG Web service."
 * 
 * @author Clemens Wrzodek
 * @author Finja B&uuml;chel
 * @version $Rev$
 * @since 1.0
 */
public enum EntryType {
  /**
   * The node is a KO (ortholog group)<br/>
   * Should be treated as 'ortholog'/'protein'.
   */
  ortholog,
  /**
   * The node is an enzyme<br/>
   * Should be treated as 'protein'.
   */
  enzyme,
  /**
   * Since KGML 7.1
   * Some kind of a reaction node... KGML says: "the node is a reaction".
   * <p><b>AS this is HIGHLIGH INCONSISTENT, please do NOT USE IT!</b>
   */
  reaction,
  /**
   * The node is a gene product (mostly a protein)
   * Should be treated as 'protein'.
   * <br/><b>THIS IS NOT A GENE</b> we need to call it gene though,
   * because the KGML specification does! But this does not
   * represent a gene!
   */
  gene,
  /**
   * the node is a complex of gene products (mostly a protein complex)
   * <br/>Should be treated as 'complex'.
   */
  group,
  /**
   * the node is a chemical compound (including a glycan)
   * <br/>Should be treated as 'small molecule'.
   */
  compound,
  /**
   * The node is a linked pathway map.
   * Should be treated as 'other', because it represents a
   * whoe pathway.
   */
  map,
  
  /**
   * !Added for compatibility for with KeggPathways version <0.7!
   * <p><b>Should not be used separately.
   * <p><b>Has been renamed to {@link #group} now, so it
   * should be treated as 'complex'!
   */
  genes,
  
  /**
   * -Custom Enum-
   */
  other
}
