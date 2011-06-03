/*
 * $Id$
 * $URL$
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.zbit.io.CSVReader;
import de.zbit.parser.Species;
import de.zbit.util.AbstractProgressBar;
import de.zbit.util.Timer;

/**
 * This is a meta-mapper, that actually has no own mapping but
 * merges {@link GeneID2KeggIDMapper}, {@link KeggID2PathwayMapper}
 * and {@link KeggPathwayID2PathwayName} to one mapping.
 * 
 * @author Clemens Wrzodek
 * @version $Rev$
 */
@SuppressWarnings("unchecked")
public class GeneID2ListOfKEGGpathways extends AbstractMapper<Integer, Collection> {
  private static final long serialVersionUID = 2584808521199605644L;
  public static final Logger log = Logger.getLogger(GeneID2ListOfKEGGpathways.class.getName());
  
  /**
   * This is required. (e.g. "mmu" for mouse, or "hsa" for human).
   * @see Species#getKeggAbbr()
   */
  private String organism_kegg_abbr = null;
  
  public GeneID2ListOfKEGGpathways(String speciesKEGGPrefix)
  throws IOException {
    this(speciesKEGGPrefix, null);
  }
  
  public GeneID2ListOfKEGGpathways(Species species) throws IOException {
    this(species.getKeggAbbr());
  }
  
  public GeneID2ListOfKEGGpathways(Species species, AbstractProgressBar progress) throws IOException {
    this(species.getKeggAbbr(), progress);
  }
  
  public GeneID2ListOfKEGGpathways(String speciesKEGGPrefix, AbstractProgressBar progress)
  throws IOException {
    // This constructor is called from every other!
    super(Integer.class, Collection.class, progress);
    organism_kegg_abbr = speciesKEGGPrefix;
    init();
  }
  
  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#readMappingData()
   */
  @Override
  public boolean readMappingData() throws IOException {
    isInizialized=true;
    
    // Build the respective meta-collection
    Timer t = new Timer();
    GeneID2KeggIDMapper map1 = new GeneID2KeggIDMapper(organism_kegg_abbr, progress);
    KeggID2PathwayMapper map2 = new KeggID2PathwayMapper(organism_kegg_abbr, progress);
    //KeggPathwayID2PathwayName map3 = new KeggPathwayID2PathwayName(progress);
    map2.convertPathwayIDsToPathwayNames(); // This results in a loss of information (kegg pw ids).
    
    Set<Entry<Integer, String>> gene_ids = map1.mapping.entrySet();
    for (Entry<Integer, String> entry : gene_ids) {
      Collection c;
      try {
        c = map2.map(entry.getValue());
      } catch (Exception e) {
        log.log(Level.WARNING, "Exception while mapping KEGG gene id 2 KEGG Pathway.", e);
        continue;
      }
      if (c!=null && c.size()>0)
        mapping.put(entry.getKey(), c);
    }
    
    log.config("Parsed " + getMappingName() + " mapping file in " + t.getNiceAndReset()+". Read " + ((mapping!=null)?mapping.size():"0") + " mappings.");
    return (mapping!=null && mapping.size()>0);
  }
  
  /**
   * @param args
   * @throws Exception 
   */
  public static void main(String[] args) throws Exception {
    GeneID2ListOfKEGGpathways mapper = new GeneID2ListOfKEGGpathways("mmu");
    
    Collection c = mapper.map(11576);
    if (c==null) System.out.println("null");
    else System.out.println(Arrays.deepToString(c.toArray(new String[0])));
    System.out.println("=================");
    c = mapper.map(77579); // 3 PWs
    if (c==null) System.out.println("null");
    else System.out.println(Arrays.deepToString(c.toArray(new String[0])));
    System.out.println("=================");
    c = mapper.map(16334);
    if (c==null) System.out.println("null");
    else System.out.println(Arrays.deepToString(c.toArray(new String[0])));
    
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
    return "GeneID2KEGGpathways";
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
