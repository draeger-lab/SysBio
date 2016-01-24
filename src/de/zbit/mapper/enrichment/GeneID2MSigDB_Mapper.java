/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2016 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.mapper.enrichment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.zbit.io.FileTools;
import de.zbit.io.csv.CSVReader;
import de.zbit.mapper.GeneSymbol2GeneIDMapper;
import de.zbit.util.Species;
import de.zbit.util.logging.LogUtil;
import de.zbit.util.progressbar.AbstractProgressBar;
import de.zbit.util.progressbar.ProgressBar;

/**
 * Enrichment Mapper for any Gene Set Enrichment from MSigDB
 * ("Molecular Signatures Database"). Tested with v3.0.
 * 
 * <p>Simply give the URL of any GMT file with either gene
 * symbols or entrez gene IDs as input file. The class will
 * automatically redirect to an url that requires no login, or
 * will take the file directly, if it is on disk or directly
 * downloadable.
 * 
 * <p>With this class, you can use any of the MSigDB defined
 * gene sets for gene enrichment analysis!
 * 
 * <p>See http://www.broadinstitute.org/gsea/msigdb/collections.jsp#C1
 * for a list and download links for gene sets AND
 * http://www.broadinstitute.org/cancer/software/gsea/wiki/index.php/License_info
 * for license information.
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class GeneID2MSigDB_Mapper extends AbstractEnrichmentMapper<Integer, String> {
  private static final long serialVersionUID = -8622792124892108115L;

  public static final Logger log = Logger.getLogger(GeneID2MSigDB_Mapper.class.getName());
  
  /**
   * The URL where the file should be downloaded, if it is not available.
   * @return 
   */
  private String downloadURL = null;
  
  /**
   * This is eventually required (depends on input file)
   * to convert gene symbols to entrez ids.
   */
  private Species species;
  
  /**
   * May be initialized, if input contains gene symbols.
   */
  private GeneSymbol2GeneIDMapper tempMapper = null;
  
  public GeneID2MSigDB_Mapper(String inputFileOrURL, Species species) throws IOException {
    this(inputFileOrURL, species, null);
  }
  
  @SuppressWarnings("unchecked")
  public GeneID2MSigDB_Mapper(String inputFileOrURL, Species species, AbstractProgressBar progress) throws IOException {
    // This constructor is called from every other!
    super(Integer.class, (Class<Collection<String>>) ((Collection<String>) new ArrayList<String>()).getClass(), progress);
    this.downloadURL = inputFileOrURL;
    this.species = species;
    init();
  }
  
  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#init()
   */
  @Override
  protected void init() throws IOException {
    String file = getLocalFile();
    if (file.contains("symbols") && !file.contains("entrez")) {
      tempMapper = new GeneSymbol2GeneIDMapper(species.getCommonName(), progress);
    }
    
    super.init();
    tempMapper = null; // Not required anymore.
  }
  
  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#preProcessSourceID(java.lang.String)
   */
  @Override
  protected String preProcessSourceID(String string) {
    if (tempMapper==null) {
      return super.preProcessSourceID(string);
    } else {
      Integer entrez=null;
      try {
        entrez = tempMapper.map(string);
      } catch (Exception e) {}
      if (entrez!=null) return Integer.toString(entrez);
      else return "0";
    }
  }
  
  /**
   * @param args
   * @throws Exception 
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static void main(String[] args) throws Exception {
    LogUtil.initializeLogging(Level.FINE);
    Species species = new Species("Mus musculus", "_MOUSE", "mouse", "mmu", 10090);
    
    GeneID2MSigDB_Mapper mapper = new GeneID2MSigDB_Mapper("http://www.broadinstitute.org/gsea/msigdb/download_file.jsp?filePath=/resources/msigdb/3.0/c2.cp.biocarta.v3.0.entrez.gmt", species, new ProgressBar(0));
    Collection c = mapper.map(5175);
    if (c==null) System.out.println("null");
    else System.out.println(Arrays.deepToString(c.toArray(new String[0])));
    System.out.println("NonUnique: " + mapper.getTotalSumOfEntitiesInAllClasses() + " Unique: " + mapper.size());
    System.out.println(mapper.getEnrichmentClassSize("BIOCARTA_SRCRPTP_PATHWAY"));
    
    System.out.println("=================");
    mapper = new GeneID2MSigDB_Mapper("http://www.broadinstitute.org/gsea/resources/msigdb/3.0/c3.tft.v3.0.symbols.gmt", species, new ProgressBar(0));
    c = mapper.map(121021); // ="CSPG4"
    if (c==null) System.out.println("null");
    else System.out.println(Arrays.deepToString(c.toArray(new String[0])));
    System.out.println("NonUnique: " + mapper.getTotalSumOfEntitiesInAllClasses() + " Unique: " + mapper.size());
    System.out.println(mapper.getEnrichmentClassSize("V$SP1_Q6_01"));
    
  }
  
  /**
   * SuperClass should NOT count enrichment sizes here.
   * This is intentionally overwritten and almost blank.
   * <p>The {@link #skipLine(String[])} method counts
   * the genes in enrichment class!
   */
  @SuppressWarnings("rawtypes")
  @Override
  protected Collection postProcessTargetID(Collection target) {
    return target;
  }
  
  /**
   * We do no postProcessing here, but fill our private variables.
   * 
   * <p>We count the genome size in {@link #sumOfCollectionSizes}
   * and the Genes in an Enrichment class {@link #genesInPathway}.
   * 
   * <p>Methods, overriding this method should at any cost make
   * a reference to this super method!
   */
  protected boolean skipLine(String[] line) {
    // col 0 = target, 1 = URL, 2-X = genes
    entitiesInPathway.put(line[getTargetColumn(null)], line.length-2);
    sumOfCollectionSizes+=line.length-2;
    return super.skipLine(line);
  }
  
  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#configureReader(de.zbit.io.CSVReader)
   */
  @Override
  protected void configureReader(CSVReader r) {
    r.setSeparatorChar('\t');
    r.setContainsHeaders(false);
    r.setSkipLines(0);
    r.setAutoDetectContentStart(false);
  }
  
  
  /* (non-Javadoc)
   * @see de.zbit.mapper.Mapper#getMappingName()
   */
  @Override
  public String getMappingName() {
    return "GeneID2MSigDB";
  }
  
  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getRemoteURL()
   */
  @Override
  public String getRemoteURL() {
    // Perform conversion
    // http://www.broadinstitute.org/gsea/msigdb/download_file.jsp?filePath=/resources/msigdb/3.0/c2.cp.biocarta.v3.0.entrez.gmt
    // to http://www.broadinstitute.org/gsea/resources/msigdb/3.0/c2.cp.biocarta.v3.0.entrez.gmt 
    if (downloadURL.contains("filePath=/resources/msigdb/")) {
      String subPath = "filePath=";
      downloadURL = String.format("http://www.broadinstitute.org/gsea%s", 
        downloadURL.substring(downloadURL.indexOf(subPath)+subPath.length(), downloadURL.length()));
    }
    return downloadURL;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getLocalFile()
   */
  @Override
  public String getLocalFile() {
    if (downloadURL.toLowerCase().trim().startsWith("http://") ||
        downloadURL.toLowerCase().trim().startsWith("ftp://")) {
      return "res/" + FileTools.getFilename(getRemoteURL());
    } else {
      return downloadURL;
    }
  }
  
  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getTargetColumn(de.zbit.io.CSVReader)
   */
  @Override
  public int getTargetColumn(CSVReader r) {
    return 0;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getMultiSourceColumn(de.zbit.io.CSVReader)
   */
  @Override
  public int[] getMultiSourceColumn(CSVReader r) {
    // From the second to the end of the line
    return new int[]{2, Integer.MAX_VALUE};
  }
  
  
  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getSourceColumn(de.zbit.io.CSVReader)
   */
  @Override
  public int getSourceColumn(CSVReader r) {
    return 2; // Never called if getMultiSourceColumn() is implemented.
  }
}
