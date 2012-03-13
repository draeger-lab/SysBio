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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.zbit.io.FileTools;
import de.zbit.io.csv.CSVReader;
import de.zbit.util.ArrayUtils;
import de.zbit.util.logging.LogUtil;
import de.zbit.util.progressbar.AbstractProgressBar;

/**
 * Maps Ensembl identifiers to NCBI Gene IDs (Entrez).
 * 
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class Ensembl2GeneIDMapper extends AbstractMapper<String, Integer> {
  private static final long serialVersionUID = 8018776534857742817L;

  public static final Logger log = Logger.getLogger(Ensembl2GeneIDMapper.class.getName());
  
  /**
   * If true, the NCBI file "ftp://ftp.ncbi.nih.gov/gene/DATA/gene2ensembl.gz"
   * is used. Else, local files in the {@link #filelist} will be used.
   */
  protected boolean useNCBI_gene2ensembl_file=false;
  
  /**
   * This file is parsed for a list of mapping files to read.
   */
  public static final String filelist = "FILELIST.txt";

  /**
   * This is used to filter the input files.
   */
  private String organism = null;
  
  public Ensembl2GeneIDMapper() throws IOException {
    super(String.class, Integer.class);
    // DO not call init() here. GeneSymbol2GeneIDMapper is using this constructor
    // and has to set the organism before initialization.
  }
  
  public Ensembl2GeneIDMapper(AbstractProgressBar progress) throws IOException {
    super(String.class, Integer.class, progress);
    // DO not call init() here. GeneSymbol2GeneIDMapper is using this constructor
    // and has to set the organism before initialization.
  }

  /**
   * @param organism common name (e.g., "mouse")
   * @throws IOException
   */
  public Ensembl2GeneIDMapper(String organism) throws IOException {
    this();
    // DO not call init() here. GeneSymbol2GeneIDMapper is using this constructor
    // and has to set the organism before initialization.
    this.organism = organism;
  }
  
  /**
   * @param organism common name (e.g., "mouse")
   * @param progress
   * @throws IOException
   */
  public Ensembl2GeneIDMapper(String organism, AbstractProgressBar progress) throws IOException {
    this(progress);
    // DO not call init() here. GeneSymbol2GeneIDMapper is using this constructor
    // and has to set the organism before initialization.
    this.organism = organism;
  }
  
  
  
  /**
   * @param args
   * @throws Exception 
   */
  public static void main(String[] args) throws Exception {
    LogUtil.initializeLogging(Level.FINE);
    Ensembl2GeneIDMapper mapper = new Ensembl2GeneIDMapper();
    System.out.println(mapper.map("ENSG00000160868"));
    System.out.println(mapper.map("ENST00000336411"));
    System.out.println(mapper.map("ENST00000544160"));
    
    System.out.println(mapper.map("ENSMUSG00000021268"));
  }

  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getLocalFile()
   */
  @Override
  public String getLocalFile() {
    return null;
  }
  
  /**
   * @return organism common name (e.g., "mouse"), for which this mapper is initialized
   */
  public String getOrganism() {
    return this.organism;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getLocalFiles()
   */
  @Override
  public String[] getLocalFiles() {
    if (useNCBI_gene2ensembl_file) return new String[]{"res/" + FileTools.getFilename(getRemoteURL())};
    Collection<String> inFiles = new LinkedList<String>();
    
    // All files should be mentioned in the FILELIST.txt in the current package.
    try {
      String line;
      BufferedReader r = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(filelist)));
      while ((line = r.readLine())!=null) {
        if (line.trim().startsWith("#")) continue;
        
        // XXX: Unchecked comparison of oraganism. Take care that both
        // (filename and variable) use names like "human","mouse" and "rat".
        if (organism==null || organism.trim().length()<1) {
          inFiles.add(line);
        } else if (line.toLowerCase().contains(organism.toLowerCase())) {
          inFiles.add(line);
        }
        
      }
    } catch (Exception e) {
      log.log(Level.SEVERE, "Could not read list of mapping files for "+ getMappingName(), e);
    }
    
    return inFiles.toArray(new String[0]);
  }

  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getMappingName()
   */
  @Override
  public String getMappingName() {
    return "Ensembl2GeneID";
  }

  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getRemoteURL()
   */
  @Override
  public String getRemoteURL() {
    if (useNCBI_gene2ensembl_file) return "ftp://ftp.ncbi.nih.gov/gene/DATA/gene2ensembl.gz";
    return null; // not available.
  }

  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getSourceColumn(de.zbit.io.CSVReader)
   */
  @Override
  public int getSourceColumn(CSVReader r) {
    return -1; // Unused. getMutliSourceColumn is used.
  }

  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getMutliSourceColumn(de.zbit.io.CSVReader)
   */
  @Override
  public int[] getMultiSourceColumn(CSVReader r) {
    if (useNCBI_gene2ensembl_file) return new int[]{2,4,6};
    Collection<Integer> sourceCols = new LinkedList<Integer>();
    int c;
    c = r.getColumnContaining("Ensembl", "Gene");
    if (c>=0) sourceCols.add(c);
    c = r.getColumnContaining("Ensembl", "Transcript");
    if (c>=0) sourceCols.add(c);
    c = r.getColumnContaining("Ensembl", "Protein");
    if (c>=0) sourceCols.add(c);
    
    return ArrayUtils.toIntArray(sourceCols.toArray(new Integer[0]));
  }
  

  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getTargetColumn(de.zbit.io.CSVReader)
   */
  @Override
  public int getTargetColumn(CSVReader r) {
    if (useNCBI_gene2ensembl_file) return 1;
    return r.getColumnContaining("Gene", "Entrez");
  }
  
  /** {@inheritDoc}*/
  @Override
  protected String postProcessSourceID(String source) {
    return source.toUpperCase().trim();
  }
  
}
