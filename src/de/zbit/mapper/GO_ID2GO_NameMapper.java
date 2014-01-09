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
import java.util.logging.Level;
import java.util.logging.Logger;

import de.zbit.io.FileTools;
import de.zbit.io.csv.CSVReader;
import de.zbit.util.Species;
import de.zbit.util.logging.LogUtil;
import de.zbit.util.progressbar.AbstractProgressBar;

/**
 * Maps GO_IDs (e.g. "GO:0008219") to names (e.g. "cell death").
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class GO_ID2GO_NameMapper extends AbstractMapper<String, String> {
  private static final long serialVersionUID = -1281677290711361724L;
  public static final Logger log = Logger.getLogger(GO_ID2GO_NameMapper.class.getName());
  

  /**
   * The URL where the file should be downloaded, if it is not available.
   * @return 
   */
  private final static String downloadURL = "ftp://ftp.ncbi.nih.gov/gene/DATA/gene2go.gz";
  
  /**
   * If this is true, will make a  reverse (pathwayName 2 Id) mapping.
   */
  private final boolean reverseMapping;
  
  /**
   * This is required. The NCBI Taxonomy ID to identify the organism.
   * @see Species#getNCBITaxonID()
   */
  private int ncbi_tax_id=0;
  
  /**
   * @param sourceType
   * @param targetType
   * @throws IOException
   */
  public GO_ID2GO_NameMapper() throws IOException {
    this(null);
  }
  
  public GO_ID2GO_NameMapper(AbstractProgressBar progress) throws IOException {
    this(false, progress);
  }
  
  public GO_ID2GO_NameMapper(boolean reverseMapping, AbstractProgressBar progress) throws IOException {
    this(reverseMapping, progress, -1);
  }
  
  public GO_ID2GO_NameMapper(boolean reverseMapping, AbstractProgressBar progress, int filter_for_taxon_id) throws IOException {
    // This constructor is called from every other!
    super(String.class, String.class, progress);
    this.reverseMapping = reverseMapping;
    this.ncbi_tax_id = filter_for_taxon_id;
    init();
  }
  
  public GO_ID2GO_NameMapper(int filter_for_taxon_id) throws IOException {
    this(false, null, filter_for_taxon_id);
  }

  /**
   * TESTS ONLY!
   * @throws Exception 
   */
  public static void main(String[] args) throws Exception {
    LogUtil.initializeLogging(Level.FINE);
    
    GO_ID2GO_NameMapper mapper = new GO_ID2GO_NameMapper();
    System.out.println(mapper.map("GO:0008219"));
    System.out.println(mapper.size());
    
    mapper = new GO_ID2GO_NameMapper(true, null);
    System.out.println(mapper.map("cell death"));
    System.out.println(mapper.size());
  }

  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getLocalFile()
   */
  @Override
  public String getLocalFile() {
    return  "res/" + FileTools.getFilename(getRemoteURL());
  }

  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getMappingName()
   */
  @Override
  public String getMappingName() {
    return reverseMapping?"GO_Name2GO_ID":"GO_ID2GO_Name";
  }

  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getRemoteURL()
   */
  @Override
  public String getRemoteURL() {
    return downloadURL;
  }

  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getSourceColumn(de.zbit.io.CSVReader)
   */
  @Override
  public int getSourceColumn(CSVReader r) {
    //#Format: tax_id GeneID GO_ID Evidence Qualifier GO_term PubMed Category (tab is used as a separator, pound sign - start of a comment)
    return reverseMapping?5:2;
  }

  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getTargetColumn(de.zbit.io.CSVReader)
   */
  @Override
  public int getTargetColumn(CSVReader r) {
    return reverseMapping?2:5;
  }
  
  /**
   * Skip every line with a non-matching taxonomy ID.
   */
  @Override
  protected boolean skipLine(String[] line) {
    // Skip everything thats not from this organism
    if (ncbi_tax_id<0) return false; // If no filter is set, accept all.
    try {
      int taxon_id = Integer.parseInt(line[0]);
      if (taxon_id==(ncbi_tax_id)) return false;
    } catch (Throwable t) {
      log.log(Level.WARNING, "Invalid Taxon identifier in GO mapping file: " + line[0], t);
    }
    return true;
  }

  
}
