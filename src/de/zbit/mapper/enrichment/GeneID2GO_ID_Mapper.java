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
package de.zbit.mapper.enrichment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.zbit.io.CSVReader;
import de.zbit.mapper.AbstractMapper;
import de.zbit.mapper.GO_ID2GO_NameMapper;
import de.zbit.parser.Species;
import de.zbit.util.AbstractProgressBar;
import de.zbit.util.FileTools;
import de.zbit.util.logging.LogUtil;

/**
 * Map a geneID to a {@link Collection} of GO Term Identifier.
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class GeneID2GO_ID_Mapper extends AbstractEnrichmentMapper<Integer, String> {
  private static final long serialVersionUID = -5591596601345734852L;
  public static final Logger log = Logger.getLogger(GeneID2GO_ID_Mapper.class.getName());

  /**
   * The URL where the file should be downloaded, if it is not available.
   * @return 
   */
  private final static String downloadURL = "ftp://ftp.ncbi.nih.gov/gene/DATA/gene2go.gz";

  /**
   * This is required. The NCBI Taxonomy ID to identify the organism.
   * @see Species#getNCBITaxonID()
   */
  private int ncbi_tax_id;
  
  /**
   * This represents the total number of 1:1 mappings (Key2ElementInCollection),
   * whereas {@link AbstractMapper#size()} is the number of 1:many (Key2Collection) size.
   */
  public int sumOfCollectionSizes;
  
  /**
   * This list counts the number of genes in a pathway.
   * Thus, the key is the pathway id and the Integer is the
   * total number of genes in the pathway.
   */
  public Map<Integer, Integer> genesInGoCategory = new HashMap<Integer, Integer>();
  
  
  /**
   * @param sourceType
   * @param targetType
   * @throws IOException
   */
  public GeneID2GO_ID_Mapper(int ncbi_tax_id)
  throws IOException {
    this(ncbi_tax_id, null);
  }
  
  public GeneID2GO_ID_Mapper(Species species) throws IOException {
    this(species.getNCBITaxonID());
  }
  
  public GeneID2GO_ID_Mapper(Species species, AbstractProgressBar progress) throws IOException {
    this(species.getNCBITaxonID(), progress);
  }
  
  @SuppressWarnings("unchecked")
  public GeneID2GO_ID_Mapper(int ncbi_tax_id, AbstractProgressBar progress)
  throws IOException {
    // This constructor is called from every other!
    super(Integer.class, (Class<Collection<String>>) new ArrayList<String>().getClass(), progress);
    this.ncbi_tax_id = ncbi_tax_id;
    init();
  }

  /**
   * @param args
   * @throws Exception 
   */
  @SuppressWarnings("unchecked")
  public static void main(String[] args) throws Exception {
    LogUtil.initializeLogging(Level.FINE);
    GeneID2GO_ID_Mapper mapper = new GeneID2GO_ID_Mapper(3702);
    
    for (int i=0; i<2; i++) {
      Collection c = mapper.map(814629); // GO:0e003676, GO:0005575, GO:0008150, GO:0008270
      if (c==null) System.out.println("null");
      else System.out.println(Arrays.deepToString(c.toArray(new String[0])));
      System.out.println("=================");
      c = mapper.map(814657); // 3 PWs
      if (c==null) System.out.println("null");
      else System.out.println(Arrays.deepToString(c.toArray(new String[0])));
      
      System.out.println("NonUnique: " + mapper.getGenomeSize() + " Unique: " + mapper.size());
      System.out.println(mapper.getEnrichmentClassSize("GO:0008219"));
      System.out.println(mapper.getEnrichmentClassSize("cell death"));
      
      if (i==0)mapper.convertGO_IDsToNames();
    }
    
  }

  /**
   * @return the ncbi taxonomy id this class has been initialized for.
   */
  public int getTaxonomyID() {
    return this.ncbi_tax_id;
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
    return "GeneID2GO_IDs";
  }

  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getRemoteURL()
   */
  @Override
  public String getRemoteURL() {
    return downloadURL;
  }

  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getSourceColumn(de.zbit.io.CSVReader)
   */
  @Override
  public int getSourceColumn(CSVReader r) {
    //#Format: tax_id GeneID GO_ID Evidence Qualifier GO_term PubMed Category (tab is used as a separator, pound sign - start of a comment)
    return 1;
  }

  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getTargetColumn(de.zbit.io.CSVReader)
   */
  @Override
  public int getTargetColumn(CSVReader r) {
    return 2;
  }

  /**
   * Skip every line with a non-matching taxonomy ID.
   */
  @Override
  protected boolean skipLine(String[] line) {
    // Skip everything thats not from this organism
    try {
      int taxon_id = Integer.parseInt(line[0]);
      if (taxon_id==(ncbi_tax_id)) return false;
    } catch (Throwable t) {
      log.log(Level.WARNING, "Invalid Taxon identifier in GO mapping file: " + line[0], t);
    }
    return true;
  }
  
  /**
   * @throws IOException 
   * 
   */
  public void convertGO_IDsToNames() throws IOException {
    GO_ID2GO_NameMapper mapper = new GO_ID2GO_NameMapper(false, progress, this.ncbi_tax_id);
    super.convertIDsToNames(mapper);
  }
  
}
