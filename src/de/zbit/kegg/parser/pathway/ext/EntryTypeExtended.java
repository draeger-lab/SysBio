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
package de.zbit.kegg.parser.pathway.ext;

import de.zbit.kegg.parser.pathway.EntryType;


/**
 * 
 * The {@link EntryTypeExtended} is just an additional more fine-grained
 * specification for the {@link EntryType}.
 * 
 * <p>If the {@link EntryType} is Compound (i.e. small molecule),
 * it is not necessary to set a gene type. Thus, this can be
 * Unknown!</p>
 * 
 * @author Finja B&uuml;chel
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public enum EntryTypeExtended {
  /**
   * protein. Use {@link EntryType#gene} to encode a protein.
   */
  @Deprecated
  protein,
  /**
   * Note that {@link EntryType#gene} represents a protein and thus,
   * we need this enum entry to represent a gene.
   */
  gene,
  /**
   * dna region. E.g., a TFBS or such.
   */
  dna_region,
  /**
   * rna_region
   */
  rna_region,
  /**
   * dna
   */
  dna,
  /**
   * rna
   */
  rna,
  /**
   * defines an empty set
   */
  emptySet,
  /**
   * Actually not required. Same as {@link EntryType#other}.
   */
  unknown;

}
