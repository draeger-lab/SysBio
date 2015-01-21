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
package de.zbit.mapper.enrichment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import de.zbit.io.FileTools;
import de.zbit.io.csv.CSVReader;
import de.zbit.mapper.KEGGspeciesAbbreviation;
import de.zbit.mapper.KeggPathwayID2PathwayName;
import de.zbit.util.Species;
import de.zbit.util.logging.LogUtil;
import de.zbit.util.progressbar.AbstractProgressBar;

/**
 * Maps the given KEGG pathway id (e.g., "path:hsa00010") to
 * a {@link Collection} of KEGG Gene IDs (e.g., "hsa:8789")
 * which are all contained in this pathway.
 * 
 * <p>XXX: KEGG Pathway FTP is no more available. You'll have to use
 * cached KEGG mapping files for this class to work.
 * 
 * @author Clemens Wrzodek
 * @version $Rev$
 */
@SuppressWarnings("unchecked")
public class KeggPathway2KEGGGeneIDs extends AbstractEnrichmentMapper<String, String> implements KEGGspeciesAbbreviation {
  private static final long serialVersionUID = -3011095040303247890L;
  public static final Logger log = Logger.getLogger(KeggPathway2KEGGGeneIDs.class.getName());
  
  /**
   * The URL where the file should be downloaded, if it is not available.
   * @return
   */
  private final static String getDownloadURL(String kegg_abbr) {
    // Col 0 = KG_Gene, Col 1 = KG_Pathway
    //return String.format("ftp://ftp.genome.jp/pub/kegg/genes/organisms/%s/%s_pathway.list", kegg_abbr,kegg_abbr);
    
    // Alternative URL (More Information!):
    // Col 0 = KG_Pathway, Col 1 = KG_Gene
    return String.format("ftp://ftp.genome.jp/pub/kegg/pathway/organisms/%s/%s.list", kegg_abbr,kegg_abbr);
  }
  
  /**
   * This is required. (e.g. "mmu" for mouse, or "hsa" for human).
   * @see Species#getKeggAbbr()
   */
  private String organism_kegg_abbr = null;
  
  /**
   * Cache the sourceColumn
   */
  private int sourceCol=-1;
  
  /**
   * @param sourceType
   * @param targetType
   * @throws IOException
   */
  public KeggPathway2KEGGGeneIDs(String speciesKEGGPrefix)
      throws IOException {
    this(speciesKEGGPrefix, null);
  }
  
  public KeggPathway2KEGGGeneIDs(Species species) throws IOException {
    this(species.getKeggAbbr());
  }
  
  public KeggPathway2KEGGGeneIDs(Species species, AbstractProgressBar progress) throws IOException {
    this(species.getKeggAbbr(), progress);
  }
  
  public KeggPathway2KEGGGeneIDs(String speciesKEGGPrefix, AbstractProgressBar progress)
      throws IOException {
    // This constructor is called from every other!
    super(String.class, (Class<Collection<String>>) ((Collection<String>) new ArrayList<String>()).getClass(), progress);
    organism_kegg_abbr = speciesKEGGPrefix;
    init();
  }
  
  
  /**
   * TESTS ONLY!
   * @throws Exception
   */
  @SuppressWarnings("rawtypes")
  public static void main(String[] args) throws Exception {
    LogUtil.initializeLogging(Level.FINE);
    KeggPathway2KEGGGeneIDs mapper = new KeggPathway2KEGGGeneIDs("mmu");
    
    for (int i=0; i<2; i++) {
      Collection c = mapper.map("mmu:11576");
      if (c==null) {
        System.out.println("null");
      } else {
        System.out.println(Arrays.deepToString(c.toArray(new String[0])));
      }
      System.out.println("=================");
      c = mapper.map("mmu:77579"); // 3 PWs
      if (c==null) {
        System.out.println("null");
      } else {
        System.out.println(Arrays.deepToString(c.toArray(new String[0])));
      }
      System.out.println("=================");
      c = mapper.map("mmu:16334");
      if (c==null) {
        System.out.println("null");
      } else {
        System.out.println(Arrays.deepToString(c.toArray(new String[0])));
      }
      
      System.out.println("NonUnique: " + mapper.getTotalSumOfEntitiesInAllClasses() + " Unique: " + mapper.size());
      System.out.println(mapper.getEnrichmentClassSize("path:mmu04530"));
      System.out.println(mapper.getEnrichmentClassSize("Tight junction"));
      
      if (i==0) {
        mapper.convertPathwayIDsToPathwayNames();
      }
    }
  }
  
  /**
   * @return the KEGG abbreviation for the current species.
   */
  @Override
  public String getSpeciesKEGGabbreviation() {
    return organism_kegg_abbr;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getLocalFile()
   */
  @Override
  public String getLocalFile() {
    return  "res/" + FileTools.getFilename(getRemoteURL());
  }
  
  /*
   * (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getEncryptedLocalFile()
   */
  @Override
  public String getEncryptedLocalFile() {
    return getLocalFile()+".dat";
  }
  
  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getMappingName()
   */
  @Override
  public String getMappingName() {
    return "KEGGPathway2KEGGGeneIDs";
  }
  
  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getRemoteURL()
   */
  @Override
  public String getRemoteURL() {
    return getDownloadURL(organism_kegg_abbr);
  }
  
  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getSourceColumn(de.zbit.io.CSVReader)
   */
  @Override
  public int getSourceColumn(CSVReader r) {
    try {
      return r.getColumnByMatchingContent("^" + Pattern.quote("path:"+organism_kegg_abbr) + ".*");
    } catch (IOException e) {
      log.log(Level.SEVERE,"Could not infere target column.", e);
      return 0;
    }
  }
  
  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getTargetColumn(de.zbit.io.CSVReader)
   */
  @Override
  public int getTargetColumn(CSVReader r) {
    int sourceCol=this.sourceCol;
    if (sourceCol<0) {
      try {
        sourceCol = r.getColumnByMatchingContent("^" + Pattern.quote(organism_kegg_abbr) + ".*");
      } catch (IOException e) {
        log.log(Level.SEVERE,"Could not infere source column.", e);
        sourceCol=1;
      }
    }
    this.sourceCol=sourceCol;
    return sourceCol;
  }
  
  
  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#skipLine(java.lang.String[])
   */
  @Override
  protected boolean skipLine(String[] line) {
    // Skip everything thats not a gene (e.g., compounds starting with "CPD:*")
    if (!line[getTargetColumn(null)].startsWith(organism_kegg_abbr)) {
      return true;
    }
    return false;
  }
  
  /**
   * Uses the {@link KeggPathwayID2PathwayName} mapper to map all IDs in
   * this mapping to the pathway name (e.g., "Tight junction" instead
   * of "path:mmu04530").
   * @throws IOException
   */
  public void convertPathwayIDsToPathwayNames() throws IOException {
    KeggPathwayID2PathwayName mapper = new KeggPathwayID2PathwayName();
    super.convertIDsToNames(mapper);
  }
  
  
}
