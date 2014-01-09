/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2014 by the University of Tuebingen, Germany.
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

import de.zbit.io.FileTools;
import de.zbit.io.csv.CSVReader;
import de.zbit.util.Species;
import de.zbit.util.progressbar.AbstractProgressBar;

/**
 * A mapper for UniProt Accessions AND UniProt Identifiers to Entrez Gene IDs.
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class UniProt2GeneIDmapper extends AbstractMapper<String, Integer> {
  private static final long serialVersionUID = -5865500118907958668L;
  public static final Logger log = Logger.getLogger(UniProt2GeneIDmapper.class.getName());
  
  /**
   * The Base url to get the directory listing and search for a file "X2geneid.gz"
   */
  private static String downloadBaseURL = "ftp://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/idmapping/by_organism/";
  
  /**
   * The URL where the file should be downloaded, if it is not available.
   */
  private String downloadURL;
  
  /**
   * The downloaded and cached local mapping file.
   * MUST contain a folder.
   */
  private String localFile;
  
  /**
   * Filter data to read for this NCBI Taxon id. If <=0,
   * the whole file will be read.
   */
  private Species species;
  

  /**
   * Initialize a new mapper from UniProt Accessions AND Identifiers to Entrez Gene IDs.
   * @param spec Required (with fields uniprot, common name, ncbi Taxon ID).
   * @param progress Optional
   * @throws IOException
   */
  public UniProt2GeneIDmapper(Species spec, AbstractProgressBar progress) throws IOException {
    super(String.class, Integer.class, progress);
    this.species = spec;
    setupFileURLs();
    init();
  }
  
  /**
   * Uses {@link #species} to fill {@link #downloadURL} and {@link #localFile}.
   * @throws IOException
   */
  private void setupFileURLs() throws IOException {
    if (species==null) {
      throw new IOException("Could not initialize UniProt mapping for unknown species.");
    }
    String UniProtOrganism = species.getUniprotExtension();
    if (UniProtOrganism==null) {
      UniProtOrganism = species.getCommonName();
    }
    if (UniProtOrganism!=null && UniProtOrganism.startsWith("_")) {
      UniProtOrganism = UniProtOrganism.substring(1);
    }
    if (UniProtOrganism==null || !species.isSetTaxonomyId()) {
      throw new IOException("Could not initialize UniProt mapping for unknown UniProtOrganism or missing taxon id.");
    }
    
    downloadURL = String.format("%s%s_%s_idmapping_selected.tab.gz", downloadBaseURL, UniProtOrganism.toUpperCase().trim(), species.getNCBITaxonID());
    localFile = "res/" + FileTools.getFilename(downloadURL);
  }
  
  
  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getRemoteURL()
   */
  @Override
  public String getRemoteURL() {
    return downloadURL;
  }
  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getLocalFile()
   */
  @Override
  public String getLocalFile() {
    return localFile;
  }
  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getMappingName()
   */
  @Override
  public String getMappingName() {
    return "UniProt2GeneID";
  }
  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getTargetColumn(de.zbit.io.csv.CSVReader)
   */
  @Override
  public int getTargetColumn(CSVReader r) {
    return 2;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#preProcessTargetID(java.lang.String)
   */
  @Override
  protected String preProcessTargetID(String string) {
    // Uniprot sometimes maps to muliple ids, e.g., "801; 805; 808".
    // As there is no easy perfect solution (and luckily this is a rare case), simply take the first number.
    int p = string.indexOf(';');
    if (p>0) {
      return string.substring(0, p);
    }
    return string;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getSourceColumn(de.zbit.io.csv.CSVReader)
   */
  @Override
  public int getSourceColumn(CSVReader r) {
    return 0; // Unused. getMutliSourceColumn is used to include both, UniProt AC and ID.
  }

  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getMutliSourceColumn(de.zbit.io.CSVReader)
   */
  @Override
  public int[] getMultiSourceColumn(CSVReader r) {
    // 0 = UniProt Accession
    // 1 = UniProt Identifier
    // => Read both for convenience.
    return new int[]{0,1};
  }  
}
