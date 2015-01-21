/*
 * $Id: CompoundID2ListOfKEGGpathways.java 1272 2013-05-27 15:48:11Z wrzodek $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SysBio/trunk/src/de/zbit/mapper/enrichment/CompoundID2ListOfKEGGpathways.java $
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
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.zbit.io.csv.CSVReader;
import de.zbit.mapper.KEGGspeciesAbbreviation;
import de.zbit.mapper.KeggPathwayID2PathwayName;
import de.zbit.mapper.MappingUtils.IdentifierClass;
import de.zbit.mapper.compounds.InChIKey2KeggCompoundMapper;
import de.zbit.util.Species;
import de.zbit.util.progressbar.AbstractProgressBar;

/**
 * This is a meta-mapper, that actually has no own mapping but
 * merges {@link InChIKey2KeggCompoundMapper}, {@link KeggID2PathwayMapper}
 * and {@link KeggPathwayID2PathwayName} to one mapping.
 * So you must specify an organism in the constructor.
 * 
 * @author Clemens Wrzodek
 * @version $Rev: 1272 $
 */
public class InChIKey2ListOfKEGGpathways extends AbstractEnrichmentMapper<String, String> implements KEGGspeciesAbbreviation {
  private static final long serialVersionUID = 6686165001064072220L;
  public static final Logger log = Logger.getLogger(InChIKey2ListOfKEGGpathways.class.getName());
  
  /**
   * This is required. (e.g. "mmu" for mouse, or "hsa" for human).
   * @see Species#getKeggAbbr()
   */
  private String organism_kegg_abbr = null;
  
  public InChIKey2ListOfKEGGpathways(Species species) throws IOException {
    this(species.getKeggAbbr());
  }
  
  public InChIKey2ListOfKEGGpathways(String speciesKEGGPrefix) throws IOException {
    this(speciesKEGGPrefix, null);
  }
  
  public InChIKey2ListOfKEGGpathways(Species species, AbstractProgressBar progress) throws IOException {
    this(species.getKeggAbbr(), progress);
  }
  
  @SuppressWarnings("unchecked")
  public InChIKey2ListOfKEGGpathways(String speciesKEGGPrefix, AbstractProgressBar progress)
  throws IOException {
    // This constructor is called from every other!
    super(String.class, (Class<Collection<String>>) ((Collection<String>) new ArrayList<String>()).getClass(), progress);
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
    InChIKey2KeggCompoundMapper map1 = new InChIKey2KeggCompoundMapper(progress);
    KeggID2PathwayMapper map2 = new KeggID2PathwayMapper(organism_kegg_abbr, progress, IdentifierClass.Compound);
    
    for (Entry<String, Set<String>> entry : map1.getMapping().entrySet()) {
      //An inchi key might contain mappings to multiple KeggIDs due to
    	//compound ontologies
    	for(String keggid: entry.getValue()){
	    	Collection<String> c=null;
	      try {
	        c = map2.map(standardizeKeggCompoundID(keggid));
	      } catch (Exception e) {
	        log.log(Level.WARNING, "Exception while mapping KEGG compound id 2 KEGG Pathway.", e);
	        continue;
	      }
	      if (c!=null && c.size()>0) {
	      	Collection<String> pws = getMapping().get(entry.getKey());
	      	if(pws != null){
	      		pws.addAll(c);
	      	}else{
	      		getMapping().put(entry.getKey(), c); 
	      	}
	      }
    	}
    }

    
    // Grab the additional Enrichment information from map2
    this.sumOfCollectionSizes = map2.getTotalSumOfEntitiesInAllClasses();
    this.entitiesInPathway = map2.entitiesInPathway;
    
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
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public static void main(String[] args) throws Exception {
    // TEST Compounds only
    InChIKey2ListOfKEGGpathways mapper = new InChIKey2ListOfKEGGpathways("mmu");
    
    for (int i=0; i<2; i++) {
      Collection c = mapper.map("NBSCHQHZLSJFNQ-GASJEMHNSA-N"); // Glucose-6-phosphate
      if (c==null) System.out.println("null");
      else System.out.println(Arrays.deepToString(c.toArray(new String[0])));
      System.out.println("=================");
      c = mapper.map("NBSCHQHZLSJFNQ-GASJEMHNSA-N"); // Glucose-6-phosphate
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
  
  private static String standardizeKeggCompoundID(String id){
  	if(id==null) return null;
  	
  	String newID=id;
  	// "XXX:C00523" => "C00523"
    if (id.length()>6) {
      newID = id.substring(id.length()-6);
    }
  	
  	if (newID.charAt(0)=='c') {
      newID = newID.toUpperCase();
    }
	  
	  return "cpd:"+newID;
    
  }
  

}
