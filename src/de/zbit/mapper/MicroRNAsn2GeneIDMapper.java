/*
 * $Id:  MicroRNAsn2GeneIDMapper.java 14:07:35 wrzodek $
 * $URL: MicroRNAsn2GeneIDMapper.java $
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
import java.util.logging.Level;
import java.util.logging.Logger;

import de.zbit.io.CSVReader;
import de.zbit.util.AbstractProgressBar;
import de.zbit.util.FileTools;
import de.zbit.util.logging.LogUtil;

/**
 * A mapper from microRNA systematic <b>precursor</b> names (e.g., "mmu-mir-30e") to
 * NCBI entrez gene identifiers (e.g., 723836).
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class MicroRNAsn2GeneIDMapper  extends AbstractMapper<String, Integer>{
  private static final long serialVersionUID = 3942767471916571867L;
  public static final Logger log = Logger.getLogger(MicroRNAsn2GeneIDMapper.class.getName());
  
  /**
   * The URL where the file should be downloaded, if it is not available.
   * @return 
   */
  private final static String downloadURL = "ftp://mirbase.org/pub/mirbase/CURRENT/database_files/mirna_database_links.txt.gz";
  

  /**
   * Initialize a modified pre-Mapper that maps miRNA systematic names
   * to an internal miRBase numbering scheme.
   */
  MicroRNAsn2miRBaseAccession preMapper;
  
  /**
   * @param sourceType
   * @param targetType
   * @throws IOException
   */
  public MicroRNAsn2GeneIDMapper() throws IOException {
    this(null);
  }
  
  /**
   * @param sourceType
   * @param targetType
   * @param progress
   * @throws IOException
   */
  public MicroRNAsn2GeneIDMapper(AbstractProgressBar progress) throws IOException {
    super(String.class, Integer.class, progress);
    
    // init a pre-mapper to map source integer to source-miRNA systematic name
    preMapper = new MicroRNAsn2miRBaseAccession(progress) {
      private static final long serialVersionUID = 8588648174187402552L;
      @Override
      public int getSourceColumn(CSVReader r) {return 0;}
      @Override
      public int getTargetColumn(CSVReader r) {return super.getSourceColumn(r);};
    };
  }
  
  /**
   * @param args
   * @throws Exception 
   */
  public static void main(String[] args) throws Exception {
    LogUtil.initializeLogging(Level.FINE);
    MicroRNAsn2GeneIDMapper mapper = new MicroRNAsn2GeneIDMapper();
    System.out.println(mapper.map("mmu-mir-30e"));
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
    return  "res/" + FileTools.getFilename(getRemoteURL());
  }

  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getMappingName()
   */
  @Override
  public String getMappingName() {
    return "miRNA2GeneID";
  }

  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getTargetColumn(de.zbit.io.CSVReader)
   */
  @Override
  public int getTargetColumn(CSVReader r) {
    return 3; // actually 2, but 2 is empty, for some reasons... keep an eye on this property!
  }

  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getSourceColumn(de.zbit.io.CSVReader)
   */
  @Override
  public int getSourceColumn(CSVReader r) {
    return 0;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#skipLine(java.lang.String[])
   */
  @Override
  protected boolean skipLine(String[] line) {
    if (line.length<2) return true;
    return !line[1].toUpperCase().trim().equals("ENTREZGENE");
  }
  
  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#readMappingData()
   */
  @Override
  public boolean readMappingData() throws IOException {
    boolean success = super.readMappingData();
    preMapper = null; // not required anymore.
    return success;
  }
  
  
  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#preProcessSourceID(java.lang.String)
   */
  @Override
  protected String preProcessSourceID(String string) {
    try {
      return preMapper.map(string);
    } catch (Exception e) {
      return null;
    }
  }
  
}
