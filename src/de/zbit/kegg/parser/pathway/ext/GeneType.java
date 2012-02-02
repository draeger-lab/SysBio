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
package de.zbit.kegg.parser.pathway.ext;


/**
 * @author Finja B&uuml;chel
 * @version $Rev$
 */
public enum GeneType{
  /**
   * protein
   */
  protein,
  /**
   * dna region
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
   * unknown
   */
  unknown;

}
