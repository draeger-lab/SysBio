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
package de.zbit.mapper.probes;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.zbit.io.csv.CSVReader;
import de.zbit.mapper.AbstractMapper;
import de.zbit.util.StringUtil;
import de.zbit.util.logging.LogUtil;
import de.zbit.util.progressbar.AbstractProgressBar;
import de.zbit.util.progressbar.ProgressBar;

/**
 * This class downloads or loads (if available) mapping data
 * to map Probe Identifier (Affymetrx, Agilent, Illumina, etc) 
 * to NCBI Gene IDs (Entrez).
 * 
 * @author buechel
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class ProbeID2GeneIDMapper extends AbstractMapper<String, Integer> {
  private static final long serialVersionUID = -4951755727304781666L;

  public static final Logger log = Logger.getLogger(ProbeID2GeneIDMapper.class.getName());
  
  /**
   * The Base url where all mapping files are (must end with a slash).
   */
  private static String downloadBaseURL = "http://www.cogsys.cs.uni-tuebingen.de/software/InCroMAP/downloads/mappings/";
  
  /**
   * Different array vendors, for which we have mapping files available.
   * @author Clemens Wrzodek
   * @version $Rev$
   */
  public static enum Manufacturer {
    Affymetrix,
    Agilent,
    Illumina
  }
  
  public String mappingFile;
  

  /**
   * 
   * @param speciesCommonName
   * @throws IOException
   */
  public ProbeID2GeneIDMapper(Manufacturer man, String speciesCommonName) throws IOException {
    this(new ProgressBar(0), man, speciesCommonName);
  }
  /**
   * @param progress - a custom progress bar. Can be NULL!
   * @throws IOException
   * @see {@link ProbeID2GeneIDMapper#GeneIDMapper()}
   */
  public ProbeID2GeneIDMapper(AbstractProgressBar progress, Manufacturer man, String speciesCommonName) throws IOException {
    super(String.class, Integer.class, progress);
    mappingFile = man.toString() + StringUtil.firstLetterUpperCase(speciesCommonName.toLowerCase().trim()) + ".txt";
  }
  
  
  public static void main (String[] args) throws Exception {
    LogUtil.initializeLogging(Level.FINE);
    ProbeID2GeneIDMapper mapper = new ProbeID2GeneIDMapper(Manufacturer.Affymetrix, "human");
    System.out.println(mapper.map("70171_at")); // 7037
    System.out.println(mapper.map("43668_at")); // 284904
    
  }


  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getLocalFile()
   */
  @Override
  public String getLocalFile() {
    return "res/"+mappingFile;
  }


  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getMappingName()
   */
  @Override
  public String getMappingName() {
    return "ProbeID2GeneID";
  }


  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getRemoteURL()
   */
  @Override
  public String getRemoteURL() {
    return downloadBaseURL+mappingFile;
  }


  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getSourceColumn()
   */
  @Override
  public int getSourceColumn(CSVReader r) {
    return 0;
  }


  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getTargetColumn()
   */
  @Override
  public int getTargetColumn(CSVReader r) {
    return 1;
  }

}
