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
import java.util.logging.Level;
import java.util.logging.Logger;

import de.zbit.io.FileTools;
import de.zbit.io.csv.CSVReader;
import de.zbit.util.logging.LogUtil;
import de.zbit.util.progressbar.AbstractProgressBar;

/**
 * Map microRNA (miRNA) systematic <b>precursor</b> names (e.g., "mmu-mir-30e") to
 * miRBase accession numbers (e.g., "MI0000259").
 * 
 * <p><i>REMARK: It would also be possible to make a mapping from the MATURE name here, via
 * ftp://mirbase.org/pub/mirbase/CURRENT/database_files/mirna_mature.txt.gz
 * (Col 0=internalNumber, 1=MatureName, 2=miRBaseAccession). BUT these
 * mature names are sometimes NOT UNIQUE, thus the result would possible be multiple
 * ids for one input id. So I decided to take the mapping for the (unique) precursor
 * identifiers and the user simply gets no value for nun-unique mappings.<br/>This also
 * affects the {@link MicroRNAsn2GeneIDMapper} (no value is returned, instead of
 * returning all values).</i>
 * 
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class MicroRNAsn2miRBaseAccession extends AbstractMapper<String, String> {
  private static final long serialVersionUID = 1095224177906614790L;
  public static final Logger log = Logger.getLogger(MicroRNAsn2miRBaseAccession.class.getName());
  
  /**
   * The URL where the file should be downloaded, if it is not available.
   */
  private final static String downloadURL = "ftp://mirbase.org/pub/mirbase/CURRENT/database_files/mirna.txt.gz";
  
  public MicroRNAsn2miRBaseAccession() throws IOException {
    super(String.class, String.class);
    // do NOT call init here.
  }
  
  public MicroRNAsn2miRBaseAccession(AbstractProgressBar progress) throws IOException {
    super(String.class, String.class, progress);
    // do NOT call init here.
  }
  
  
  /**
   * @param args
   * @throws Exception 
   */
  public static void main(String[] args) throws Exception {
    LogUtil.initializeLogging(Level.FINE);
    MicroRNAsn2miRBaseAccession mapper = new MicroRNAsn2miRBaseAccession();
    System.out.println(mapper.map("mmu-mir-30e"));
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
    return "miRNA2miRBase";
  }

  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getRemoteURL()
   */
  @Override
  public String getRemoteURL() {
    return downloadURL; // not available.
  }

  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getSourceColumn(de.zbit.io.CSVReader)
   */
  @Override
  public int getSourceColumn(CSVReader r) {
    //return 2; // Unused. getMutliSourceColumn is used.
    int col=2;
    try {
      col= r.getColumnByMatchingContent("(.*-)?mir-\\d\\S*");
    } catch (IOException e) {
      e.printStackTrace();
    }
    return col;
  }
  

  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getTargetColumn(de.zbit.io.CSVReader)
   */
  @Override
  public int getTargetColumn(CSVReader r) {
    //return 1;
    int col=1;
    try {
      col= r.getColumnByMatchingContent("MI.*");
    } catch (IOException e) {
      e.printStackTrace();
    }
    return col;
  }

  
}
