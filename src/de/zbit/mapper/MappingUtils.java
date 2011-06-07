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
package de.zbit.mapper;

import java.io.IOException;

import de.zbit.parser.Species;
import de.zbit.util.AbstractProgressBar;

/**
 * This class contains utilities for {@link AbstractMapper}s.
 * 
 * <p>It mainly contains a list of supported Identifiers ({@link IdentifierType})
 * and can initialize an {@link AbstractMapper}, based on a list element
 * ({@link #initialize2GeneIDMapper(IdentifierType, AbstractProgressBar, String)}).
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class MappingUtils {
  
  
  /**
   * A enumeration of different gene identifiers.
   * <b>All these identifiers should be mappable to GeneID.</b>
   * <p>Note: If you change this list, please also change the
   * {@link MappingUtils#initialize2GeneIDMapper(IdentifierType, AbstractProgressBar, String)}
   * method.
   * @author Clemens Wrzodek
   */
  public static enum IdentifierType {
    Unknown, GeneID, RefSeq, Ensembl, KeggGenes, Symbol;
  }
  
  
  /**
   * Initializes an X to GeneID mapper.
   * @param targetIDtype - see {@link IdentifierType}
   * @param progress - Optional progress bar, used for downloading or reading the mapping flatfile. May be null.
   * @param speciesCommonName - For Ensembl or GeneSymbol, the species common name (e.g., "human") is required.
   * @return
   * @throws IOException
   */
  public static AbstractMapper<String, Integer> initialize2GeneIDMapper(IdentifierType targetIDtype, AbstractProgressBar progress, Species species) throws IOException {
    // Init mapper based on targetIDtype
    AbstractMapper<String, Integer> mapper = null;
    if (targetIDtype.equals(IdentifierType.RefSeq)) {
      mapper = new RefSeq2GeneIDMapper(progress);
    } else if (targetIDtype.equals(IdentifierType.Ensembl)) {
      mapper = new Ensembl2GeneIDMapper(species.getCommonName(), progress);
    } else if (targetIDtype.equals(IdentifierType.Symbol)) {
      mapper = new GeneSymbol2GeneIDMapper(species.getCommonName(), progress);
    } else if (targetIDtype.equals(IdentifierType.KeggGenes)) {
      mapper = new KeggGenesID2GeneID(species.getKeggAbbr(), progress);
    }
    return mapper;
  }
  
}
