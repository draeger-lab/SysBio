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
package de.zbit.mapper;

import java.io.IOException;
import java.util.logging.Logger;

import de.zbit.util.DatabaseIdentifiers;
import de.zbit.util.Species;
import de.zbit.util.progressbar.AbstractProgressBar;

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
  private static final transient Logger log = Logger.getLogger(MappingUtils.class.getName());
  
  
  /**
   * An enumeration of different gene identifiers.
   * <b>All these identifiers should be mappable to GeneID.</b>
   * <p>Note: If you change this list, please also change the
   * {@link MappingUtils#initialize2GeneIDMapper(IdentifierType, AbstractProgressBar, String)}
   * method.
   * @author Clemens Wrzodek
   */
  public static enum IdentifierType {
    // We can not use the "DatabaseIdentifiers" class here, because
    // we need a subset of identifiers that are mappabla to geneID
    // and have an actual 2GeneID implementation here.
    Unknown, NCBI_GeneID, RefSeq, Ensembl, KeggGenes, GeneSymbol;
  }
  
  /**
   * Get a regular expression for each {@link IdentifierType}.
   * @return regular expressions for each {@link IdentifierType} in
   * the same ordering as {@link IdentifierType#values()}.
   */
  public static String[] getRegularExpressionsForAllIdentifierTypes() {
    IdentifierType[] values = IdentifierType.values();
    String[] ret = new String[values.length];
    
    for (int i=0; i<values.length; i++) {
      ret[i] = getRegularExpressionForIdentifier(values[i]);
    }
    
    return ret;
  }
  
  /**
   * Return a regular expression to identify a certain identifier.
   * @param identifier
   * @return 
   */
  public static String getRegularExpressionForIdentifier(IdentifierType identifier) {
    // Use the new class "DatabaseIdentifiers" for this method.
    switch (identifier) {
      case NCBI_GeneID:
        return DatabaseIdentifiers.getRegularExpressionForIdentifier(
          DatabaseIdentifiers.IdentifierDatabases.EntrezGene, false);
      case RefSeq:
        return DatabaseIdentifiers.getRegularExpressionForIdentifier(
          DatabaseIdentifiers.IdentifierDatabases.RefSeq, false);
      case Ensembl:
        return DatabaseIdentifiers.getRegularExpressionForIdentifier(
          DatabaseIdentifiers.IdentifierDatabases.Ensembl, false);
      case KeggGenes:
        return DatabaseIdentifiers.getRegularExpressionForIdentifier(
          DatabaseIdentifiers.IdentifierDatabases.KEGG_Genes, false);
      case GeneSymbol:
        return DatabaseIdentifiers.getRegularExpressionForIdentifier(
          DatabaseIdentifiers.IdentifierDatabases.GeneSymbol, false);
      default:
        return null;
    }
  }
  
  
  /**
   * Initializes an X to GeneID mapper.
   * @param sourceIDtype - see {@link IdentifierType}
   * @param progress - Optional progress bar, used for downloading or reading the mapping flatfile. May be null.
   * @param speciesCommonName - For Ensembl or GeneSymbol, the species common name (e.g., "human") is required.
   * @return
   * @throws IOException
   */
  public static AbstractMapper<String, Integer> initialize2GeneIDMapper(IdentifierType sourceIDtype, AbstractProgressBar progress, Species species) throws IOException {
    log.info("Initializing 2GeneID mapper...");
    // Init mapper based on targetIDtype
    AbstractMapper<String, Integer> mapper = null;
    if (sourceIDtype.equals(IdentifierType.RefSeq)) {
      mapper = new RefSeq2GeneIDMapper(progress, species.getNCBITaxonID());
    } else if (sourceIDtype.equals(IdentifierType.Ensembl)) {
      mapper = new Ensembl2GeneIDMapper(species.getCommonName(), progress);
    } else if (sourceIDtype.equals(IdentifierType.GeneSymbol)) {
      mapper = new GeneSymbol2GeneIDMapper(species.getCommonName(), progress);
    } else if (sourceIDtype.equals(IdentifierType.KeggGenes)) {
      mapper = new KeggGenesID2GeneID(species.getKeggAbbr(), progress);
    }
    return mapper;
  }
  
}
