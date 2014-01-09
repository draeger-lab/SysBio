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

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

import de.zbit.io.FileTools;
import de.zbit.io.csv.CSVReader;
import de.zbit.util.progressbar.AbstractProgressBar;
import de.zbit.util.progressbar.ProgressBar;

/**
 * A mapper to map from rs-dbSNP identifier (see http://www.ncbi.nlm.nih.gov/projects/SNP/)
 * to an NCBI Gene ID (Entrez).
 * @author Clemens Wrzodek
 * @author Finja Büchel
 * @version $Rev$
 */
public class SNPid2GeneIDmapper extends AbstractMapper<Integer, Integer> {
  private static final long serialVersionUID = 8644671525392250381L;

  public static final Logger log = Logger.getLogger(SNPid2GeneIDmapper.class.getName());
  
  /**
   * The URL where the mapping file should be downloaded from.
   */
  private String primaryDownloadURL = null;
  
  /**
   * Alternative URL that contains all organism and should always be available
   */
  private static String fallBackDownloadURL = "ftp://ftp.ncbi.nih.gov/snp/Entrez/eLinks/snp_genes.gz";
  
  /**
   * The download URL to try currently
   */
  private String tryThisDownloadURL = fallBackDownloadURL;
  
  /**
   * Filter data to read for this NCBI Taxon id. If <=0,
   * the whole file will be read.
   */
  private int ncbi_tax_id;
  
  
  

  /**
   * Inintializes the mapper from dbSNP to Gene ids. Downloads and reads the mapping
   * file automatically as required. This constructor is not recommended as it
   * downloads data for all possible organisms!
   * @throws IOException
   */
  public SNPid2GeneIDmapper() throws IOException {
    this(new ProgressBar(0));
  }
  
  /** Inintializes the mapper from dbSNP to Gene ids. Downloads and reads the mapping
   * file automatically as required. This constructor is not recommended as it
   * downloads data for all possible organisms!
   * @param progress a custom progress bar. Can be NULL!
   * @throws IOException
   */
  public SNPid2GeneIDmapper(AbstractProgressBar progress) throws IOException {
    this(progress, -1);
  }
  
  /**
   * Inintializes the mapper from dbSNP to Gene ids. Downloads and reads the mapping
   * file automatically as required.
   * @throws IOException
   */
  public SNPid2GeneIDmapper(int ncbi_tax_id) throws IOException {
    this(new ProgressBar(0), ncbi_tax_id);
  }
  /**
   * Inintializes the mapper from dbSNP to Gene ids. Downloads and reads the mapping
   * file automatically as required.
   * @param progress a custom progress bar. Can be NULL!
   * @throws IOException
   */
  public SNPid2GeneIDmapper(AbstractProgressBar progress, int ncbi_tax_id) throws IOException {
    this(progress, ncbi_tax_id, true);
  }
  
  private SNPid2GeneIDmapper(AbstractProgressBar progress, int ncbi_tax_id, boolean initImmediately) throws IOException {
    super(Integer.class, Integer.class, progress);
    this.ncbi_tax_id = ncbi_tax_id;
    /* Since the entrez file from fallBackDownloadURL is rather old
     * and the official dbSNP mapping file contains many informations
     * for many organisms (what results in a hughe download, and probably
     * a Java out of memory exception (heap space)), we have created
     * custom files with organism-specific filetered information
     * that should be downloaded on demand.
     * 
     * Feel free to Update them! See the main method and generateInputFileForSpecies()
     * for all required stuff to generate novel mapping files.
     */
    if (ncbi_tax_id>0) {
      primaryDownloadURL = String.format("http://cogsys.cs.uni-tuebingen.de/software/InCroMAP/downloads/mappings/%s.SNP2GeneID.gz", ncbi_tax_id);
      tryThisDownloadURL = primaryDownloadURL;
    }
    if (initImmediately) init();
  }
  
  /**
   * If this method has been initialized with a fixed taxon id, this method
   * returns the NCBI Taxon identifier. Else, -1 is returned.
   * @return
   */
  public int getNCBITaxonId() {
    return ncbi_tax_id;
  }
  
  
  /**
   * @param args
   * @throws Exception 
   */
  public static void main(String[] args) throws Exception {
//    generateInputFileForSpecies(9606, 10090, 10116);
  }
  
  /**
   * This class can be used to filter the global dbSNP2GeneID mapping
   * file for a new mapping file, that contains SNPs for a certain
   * species only.
   * @param taxonomiesOfInterest NCBI Taxonomy id to generate a mapping
   * file for.
   * @throws Exception 
   */
  @SuppressWarnings("unchecked")
  public static void generateInputFileForSpecies(Integer... taxonomiesOfInterest) throws Exception {
    // Human mouse and rat
    //Integer[] taxonomiesOfInterest = new Integer[]{9606, 10090, 10116};
    
    // Read all snps to read for every given organism
//    System.out.println("Reading taxonomy Mapping...");
//    CSVReader r = new CSVReader("C:/Users/wrzodek/Downloads/snp_taxonomy.gz");
//    String[] line;
//    Set<Integer>[] snpsToRead = new HashSet[taxonomiesOfInterest.length];
//    for (int i=0; i<taxonomiesOfInterest.length; i++) snpsToRead[i] = new HashSet<Integer>();
//    while ((line=r.getNextLine())!=null) {
//      try { //RS 2 TaxID mapping
//        Integer taxId = Integer.parseInt(line[1]);
//        for (int i=0; i<taxonomiesOfInterest.length; i++) {
//          if (taxId.equals(taxonomiesOfInterest[i])) {
//            snpsToRead[i].add(Integer.parseInt(line[0]));
//          }
//        }
//      } catch (Throwable t) {t.printStackTrace();}
//    }
//    for (int i=0; i<snpsToRead.length; i++) {
//      System.out.println(taxonomiesOfInterest[i] + ": " + snpsToRead[i].size() + " SNPs");
//    }
    /*
     * REQUIRED INPUT FILES CAN BE DOWNLOADED FROM
     * - Entrez Gene FTP Server: ftp://ftp.ncbi.nih.gov/gene/DATA/
     * - dbSNP FTP: ftp://ftp.ncbi.nih.gov/snp/Entrez/eLinks/
     */
    
    System.out.println("Reading taxonomy Mapping...");
    CSVReader r = new CSVReader("C:/Users/wrzodek/Downloads/gene_info.gz");
    String[] line;
    Set<Integer>[] genesToRead = new HashSet[taxonomiesOfInterest.length];
    for (int i=0; i<taxonomiesOfInterest.length; i++) genesToRead[i] = new HashSet<Integer>();
    while ((line=r.getNextLine())!=null) {
      try { //Tax 2 GeneID mapping
        Integer taxId = Integer.parseInt(line[0]);
        for (int i=0; i<taxonomiesOfInterest.length; i++) {
          if (taxId.equals(taxonomiesOfInterest[i])) {
            genesToRead[i].add(Integer.parseInt(line[1]));
          }
        }
      } catch (Throwable t) {t.printStackTrace();}
    }
    for (int i=0; i<genesToRead.length; i++) {
      System.out.println(taxonomiesOfInterest[i] + ": " + genesToRead[i].size() + " Genes");
    }
    
    System.out.println("Filtering all SNP2GeneID...");
    
    // Open a GZIPed output stream for all tax-ids
    BufferedOutputStream[] fw = new BufferedOutputStream[taxonomiesOfInterest.length];
    for (int i=0; i<fw.length; i++) {
      fw[i] = new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(taxonomiesOfInterest[i] + ".SNP2GeneID.gz")));
    }
    
    // Read SNP2GeneID file and process only those, that are in out lists
    SNPid2GeneIDmapper mapper = new SNPid2GeneIDmapper(new ProgressBar(0),-1,false);
    mapper.ensureLocalFileIsAvailable();
    r = new CSVReader(mapper.getLocalFile());
    while ((line=r.getNextLine())!=null) {
      try { //RS 2 GenID mapping
//        Integer rsId = Integer.parseInt(line[0]);
//        for (int i=0; i<snpsToRead.length; i++) {
//          if (snpsToRead[i].contains(rsId)) {
//            fw[i].write(String.format("%s\t%s\n", rsId, Integer.parseInt(line[1])).getBytes());
//          }
//        }
      Integer geneId = Integer.parseInt(line[1]);
      for (int i=0; i<genesToRead.length; i++) {
        if (genesToRead[i].contains(geneId)) {
          fw[i].write(String.format("%s\t%s\n", Integer.parseInt(line[0]), geneId).getBytes());
        }
      }
      } catch (Throwable t) {t.printStackTrace();}
    }
    
    // Close all streams
    System.out.print("Done. Closing streams.");
    for (BufferedOutputStream o: fw) {
      o.close();
    }
    
  }
  
  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getRemoteURL()
   */
  @Override
  public String getRemoteURL() {
    return tryThisDownloadURL;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#ensureLocalFileIsAvailable()
   */
  @Override
  protected boolean ensureLocalFileIsAvailable() {
    // Implemented a fallBack on "fallBackDownloadURL".
    boolean retVal = super.ensureLocalFileIsAvailable();
    if (!retVal) {
      if (!tryThisDownloadURL.equals(fallBackDownloadURL)) {
        tryThisDownloadURL = fallBackDownloadURL;
        retVal = super.ensureLocalFileIsAvailable();
      }
    }
    return retVal;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getLocalFile()
   */
  @Override
  public String getLocalFile() {
    return "res/"+FileTools.getFilename(getRemoteURL());
  }
  
  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getMappingName()
   */
  @Override
  public String getMappingName() {
    return "dbSNP2GeneID";
  }
  
  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getTargetColumn(de.zbit.io.CSVReader)
   */
  @Override
  public int getTargetColumn(CSVReader r) {
    return 1;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getSourceColumn(de.zbit.io.CSVReader)
   */
  @Override
  public int getSourceColumn(CSVReader r) {
    return 0;
  }
  
}
