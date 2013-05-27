/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2013 by the University of Tuebingen, Germany.
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
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.zbit.io.csv.CSVReader;
import de.zbit.mapper.GeneID2KeggIDMapper;
import de.zbit.mapper.KEGGspeciesAbbreviation;
import de.zbit.mapper.KeggPathwayID2PathwayName;
import de.zbit.mapper.MappingUtils.IdentifierClass;
import de.zbit.mapper.compounds.CompoundID2KeggCompoundMapper;
import de.zbit.util.Species;
import de.zbit.util.progressbar.AbstractProgressBar;

/**
 * This is a meta-mapper, that actually has no own mapping but
 * merges {@link CompoundID2KeggCompoundMapper}, {@link KeggID2PathwayMapper}
 * and {@link KeggPathwayID2PathwayName} to one mapping.
 * <p>Please note: despite compounds not being organism specific, pathways are.
 * So you must specify an organism in the constructor.
 * 
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class CompoundID2ListOfKEGGpathways extends AbstractEnrichmentMapper<Integer, String> implements KEGGspeciesAbbreviation {
  private static final long serialVersionUID = 6686165001064072220L;
  public static final Logger log = Logger.getLogger(CompoundID2ListOfKEGGpathways.class.getName());
  
  /**
   * This is required. (e.g. "mmu" for mouse, or "hsa" for human).
   * @see Species#getKeggAbbr()
   */
  private String organism_kegg_abbr = null;
  
  /**
   * If false, only compound information will be read.
   */
  private boolean readGenesANDCompounds = false;

  public CompoundID2ListOfKEGGpathways(Species species) throws IOException {
    this(species.getKeggAbbr());
  }
  
  public CompoundID2ListOfKEGGpathways(String speciesKEGGPrefix) throws IOException {
    this(false, speciesKEGGPrefix, null);
  }
  
  public CompoundID2ListOfKEGGpathways(boolean readCompoundANDGenes, Species species) throws IOException {
    this(readCompoundANDGenes, species.getKeggAbbr());
  }
  
  public CompoundID2ListOfKEGGpathways(boolean readCompoundANDGenes, String speciesKEGGPrefix)
  throws IOException {
    this(readCompoundANDGenes, speciesKEGGPrefix, null);
  }
  
  public CompoundID2ListOfKEGGpathways(boolean readCompoundANDGenes, Species species, AbstractProgressBar progress) throws IOException {
    this(readCompoundANDGenes, species.getKeggAbbr(), progress);
  }
  
  public CompoundID2ListOfKEGGpathways(boolean readCompoundANDGenes, String speciesKEGGPrefix, AbstractProgressBar progress)
  throws IOException {
    // This constructor is called from every other!
    super(Integer.class, (Class<Collection<String>>) ((Collection<String>) new ArrayList<String>()).getClass(), progress);
    organism_kegg_abbr = speciesKEGGPrefix;
    this.readGenesANDCompounds = readCompoundANDGenes; 
    init();
  }
  
  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#readMappingData()
   */
  @SuppressWarnings("rawtypes")
  @Override
  public boolean readMappingData() throws IOException {
    isInizialized=true;
    
    // Build the respective meta-collection
    GeneID2KeggIDMapper map1 = readGenesANDCompounds?new GeneID2KeggIDMapper(organism_kegg_abbr, progress):null;
    CompoundID2KeggCompoundMapper mapC = new CompoundID2KeggCompoundMapper(progress);
    KeggID2PathwayMapper map2 = new KeggID2PathwayMapper(organism_kegg_abbr, progress, readGenesANDCompounds?null:IdentifierClass.Compound);
    
    // Create an internal mapping from CompoundID 2 PathwayList (merge mapC and map2).
    // NOTE: All CompoundIDs are added as NEGATIVE values to avoid overlaps with GeneIDs!!!
    for (Entry<Integer, String> entry : mapC.getMapping().entrySet()) {
      Collection c=null;
      try {
        c = map2.map("cpd:"+ entry.getValue());
      } catch (Exception e) {
        log.log(Level.WARNING, "Exception while mapping KEGG compound id 2 KEGG Pathway.", e);
        continue;
      }
      if (c!=null && c.size()>0) {
        getMapping().put(entry.getKey()*-1, c); // add as NEGATIVE value
      }
    }
    // Maybe also add the GENE_IDs
    if (readGenesANDCompounds) {
      for (Entry<Integer, String> entry : map1.getMapping().entrySet()) {
        Collection c=null;
        try {
          c = map2.map(entry.getValue());
        } catch (Exception e) {
          log.log(Level.WARNING, "Exception while mapping KEGG gene id 2 KEGG Pathway.", e);
          continue;
        }
        if (c!=null && c.size()>0) {
          getMapping().put(entry.getKey(), c);
        }
      }
    }

    
    // Grab the additional Enrichment information from map2
    this.sumOfCollectionSizes = map2.getGenomeSize();
    this.genesInPathway = map2.genesInPathway;
    
    log.config("Parsed " + getMappingName() + " mapping file. Read " + ((getMapping()!=null)?getMapping().size():"0") + " mappings.");
    return (getMapping()!=null && getMapping().size()>0);
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
  
  /**
   * @param args
   * @throws Exception 
   */
  @SuppressWarnings("rawtypes")
  public static void main(String[] args) throws Exception {
    // TEST Compounds only
    CompoundID2ListOfKEGGpathways mapper = new CompoundID2ListOfKEGGpathways(false, "mmu");
    
    for (int i=0; i<2; i++) {
      Collection c = mapper.map(1401); // Glucose-6-phosphate
      if (c==null) System.out.println("null");
      else System.out.println(Arrays.deepToString(c.toArray(new String[0])));
      System.out.println("=================");
      c = mapper.map(-1401); // Glucose-6-phosphate
      if (c==null) System.out.println("null");
      else System.out.println(Arrays.deepToString(c.toArray(new String[0])));
      System.out.println("=================");

      mapper.convertPathwayIDsToPathwayNames();
    }
    
    // TEST with genes
    mapper = new CompoundID2ListOfKEGGpathways(true, "mmu");
    for (int i=0; i<2; i++) {
      Collection c = mapper.map(-1401); // Glucose-6-phosphate
      if (c==null) System.out.println("null");
      else System.out.println(Arrays.deepToString(c.toArray(new String[0])));
      System.out.println("=================");
      c = mapper.map(77579); // Myh10 myosin; 3 PWs
      if (c==null) System.out.println("null");
      else System.out.println(Arrays.deepToString(c.toArray(new String[0])));
      System.out.println("=================");
      mapper.convertPathwayIDsToPathwayNames();
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
    return null;
  }

  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getMappingName()
   */
  @Override
  public String getMappingName() {
    return "CompoundID2KEGGpathways";
  }

  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getRemoteURL()
   */
  @Override
  public String getRemoteURL() {
    return null;
  }

  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getSourceColumn(de.zbit.io.CSVReader)
   */
  @Override
  public int getSourceColumn(CSVReader r) {
    return 0;
  }

  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getTargetColumn(de.zbit.io.CSVReader)
   */
  @Override
  public int getTargetColumn(CSVReader r) {
    return 0;
  }
  

}
