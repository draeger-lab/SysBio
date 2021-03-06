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
import java.util.regex.Pattern;

import de.zbit.io.FileTools;
import de.zbit.io.csv.CSVReader;
import de.zbit.mapper.AbstractMapper;
import de.zbit.mapper.GeneID2KeggIDMapper;
import de.zbit.mapper.KEGGspeciesAbbreviation;
import de.zbit.mapper.KeggPathwayID2PathwayName;
import de.zbit.mapper.MappingUtils.IdentifierClass;
import de.zbit.util.Species;
import de.zbit.util.logging.LogUtil;
import de.zbit.util.progressbar.AbstractProgressBar;

/**
 * Maps the given KEGG gene id (see {@link GeneID2KeggIDMapper}) to
 * a {@link Collection} of KEGG Pathways, in which this gene/compound occurs.
 * 
 * <p>XXX: KEGG Pathway FTP is no more available. You'll have to use
 * cached KEGG mapping files for this class to work.
 * 
 * @author Clemens Wrzodek
 * @version $Rev$
 */
@SuppressWarnings("unchecked")
public class KeggID2PathwayMapper extends AbstractEnrichmentMapper<String, String> implements KEGGspeciesAbbreviation {
  private static final long serialVersionUID = -3011095040303247890L;
  public static final Logger log = Logger.getLogger(KeggID2PathwayMapper.class.getName());
  
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
   * What should be considerer? Genes, compounds or both?
   * A user MUST decide via the constructors. Setting a default might generate errors
   */
  private IdentifierClass dataType;
  
  /**
   * @param sourceType
   * @param targetType
   * @throws IOException
   */
  public KeggID2PathwayMapper(String speciesKEGGPrefix, IdentifierClass dataType)
  throws IOException {
    this(speciesKEGGPrefix, null, dataType);
  }
  
  public KeggID2PathwayMapper(Species species, IdentifierClass dataType) throws IOException {
    this(species.getKeggAbbr(),dataType);
  }
  
  public KeggID2PathwayMapper(Species species, AbstractProgressBar progress, IdentifierClass dataType) throws IOException {
    this(species.getKeggAbbr(), progress, dataType);
  }
    
  /**
   * Read KEGG_IDs and map them to pathways. Note: KEGG IDs are always saved WITH PREFIX.
   * So be sure to add "cpd:" to your compounds and, e.g., "hsa:" to your gene IDs!
   * @param speciesKEGGPrefix NOT required for compounds only.
   * @param progress
   * @param dataType
   *        specify what to Include. {@link IdentifierClass#Gene} for genes
   *        only, {@link IdentifierClass#Compound} for compounds only or
   *        {@code NULL} for both.
   * @throws IOException
   */
  public KeggID2PathwayMapper(String speciesKEGGPrefix, AbstractProgressBar progress, IdentifierClass dataType)
  throws IOException {
    // This constructor is called from every other!
    super(String.class, (Class<Collection<String>>) ((Collection<String>) new ArrayList<String>()).getClass(), progress);
    this.organism_kegg_abbr = speciesKEGGPrefix;
    this.dataType = dataType;
    if (dataType!=null && dataType.equals(IdentifierClass.Compound) && organism_kegg_abbr==null) {
      organism_kegg_abbr = "hsa"; // Compounds are equal for all organisms. Just use any.
    }
    init();
  }
  
  /**
   * TESTS ONLY!
   * @throws Exception 
   */
  @SuppressWarnings("rawtypes")
  public static void main(String[] args) throws Exception {
    LogUtil.initializeLogging(Level.FINE);
    KeggID2PathwayMapper mapper = new KeggID2PathwayMapper("mmu",IdentifierClass.Gene);
    
    for (int i=0; i<2; i++) {
      Collection c = mapper.map("mmu:11576");
      if (c==null) System.out.println("null");
      else System.out.println(Arrays.deepToString(c.toArray(new String[0])));
      System.out.println("=================");
      c = mapper.map("mmu:77579"); // 3 PWs
      if (c==null) System.out.println("null");
      else System.out.println(Arrays.deepToString(c.toArray(new String[0])));
      System.out.println("=================");
      c = mapper.map("mmu:16334");
      if (c==null) System.out.println("null");
      else System.out.println(Arrays.deepToString(c.toArray(new String[0])));
      
      System.out.println("NonUnique: " + mapper.getTotalSumOfEntitiesInAllClasses() + " Unique: " + mapper.size());
      System.out.println(mapper.getEnrichmentClassSize("path:mmu04530"));
      System.out.println(mapper.getEnrichmentClassSize("Tight junction"));
      
      if (i==0)mapper.convertPathwayIDsToPathwayNames();
    }
  }
  
  /**
   * @return the KEGG abbreviation for the current species.
   */
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
    return "KeggID2Pathway";
  }

  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getRemoteURL()
   */
  @Override
  public String getRemoteURL() {
    return getDownloadURL(this.organism_kegg_abbr);
  }

  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getSourceColumn(de.zbit.io.CSVReader)
   */
  @Override
  public int getSourceColumn(CSVReader r) {
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
   * @see de.zbit.mapper.AbstractMapper#getTargetColumn(de.zbit.io.CSVReader)
   */
  @Override
  public int getTargetColumn(CSVReader r) {
    try {
      return r.getColumnByMatchingContent("^" + Pattern.quote("path:"+organism_kegg_abbr) + ".*");
    } catch (IOException e) {
      log.log(Level.SEVERE,"Could not infere target column.", e);
      return 0;
    }
  }
  
  
  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#skipLine(java.lang.String[])
   */
  @Override
  protected boolean skipLine(String[] line) {
    if (dataType==null) return false; // Read all data
    
    if (dataType.equals(IdentifierClass.Gene)) {
      // Skip everything thats not a gene (e.g., compounds starting with "CPD:*")
      if (!line[getSourceColumn(null)].startsWith(organism_kegg_abbr)) return true;
    } else if (dataType.equals(IdentifierClass.Compound)) {
      if (!line[getSourceColumn(null)].startsWith("cpd")) return true;
    }
    
    return false;
  }
  
  /**
   * A better name: getSumOfGenesInAllPathways()
   * <p>Total number of genes, occuring in all pathways
   * including multiples (genes occuring in multiple
   * pathways).
   * <p>Use {@link AbstractMapper#size()} to get number of
   * unique genes, occuring in all pathways.
   * @return
   */
  public int getTotalSumOfEntitiesInAllClasses() {
    return super.getTotalSumOfEntitiesInAllClasses();
  }
  
  /**
   * Return the number of genes in a pathway (getPathwaySize()).
   * @param pathway
   * @return number of genes in the given pathway.
   */
  public int getEnrichmentClassSize(String pathway) {
    return super.getEnrichmentClassSize(pathway);
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
