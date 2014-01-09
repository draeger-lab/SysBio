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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.zbit.io.csv.CSVReader;
import de.zbit.util.Species;
import de.zbit.util.logging.LogUtil;
import de.zbit.util.progressbar.AbstractProgressBar;

/**
 * Maps KEGG GENES IDs to NCBI Gene IDs (Entrez, previously Lokuslink).
 * The kegg genes id is often simply organism:gene_id, but unfortunately not always.
 * See <a href="http://www.genome.jp/kegg/kegg3.html">http://www.genome.jp/kegg/kegg3.html</a>
 * 
 * <p>Quote:<br/>"Entry names of the KEGG GENES database are usually locus_tags
 * given by the International Nucleotide Sequence Database Collaboration
 * (INSDC). The major sequence databases such as NCBI and UniProt/Swiss-Prot
 * use different sets of gene/protein identifiers."</p>
 * 
 * <p>HINT:<br/>If you can't map something, try to use organism:gene_id as heuristic.
 * This should work in 90% of all cases.</p>
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class KeggGenesID2GeneID extends AbstractMapper<String, Integer> {
  private static final long serialVersionUID = -3452034234430740721L;
  public static final Logger log = Logger.getLogger(KeggGenesID2GeneID.class.getName());

  /**
   * This is required. (e.g. "mmu" for mouse, or "hsa" for human).
   * @see Species#getKeggAbbr()
   */
  private String organism_kegg_abbr = null;
  
  /**
   * @param speciesKEGGPrefix KEGG species prefix, e.g., "mmu"
   * for mouse or "hsa" for human.
   * @throws IOException
   */
  public KeggGenesID2GeneID(String speciesKEGGPrefix)
  throws IOException {
    this(speciesKEGGPrefix, null);
  }
  
  public KeggGenesID2GeneID(Species species) throws IOException {
    this(species.getKeggAbbr());
  }
  
  public KeggGenesID2GeneID(Species species, AbstractProgressBar progress) throws IOException {
    this(species.getKeggAbbr(), progress);
  }
  
  public KeggGenesID2GeneID(String speciesKEGGPrefix, AbstractProgressBar progress)
  throws IOException {
    // This constructor is called from every other!
    super(String.class, Integer.class, progress);
    organism_kegg_abbr = speciesKEGGPrefix;
    init();
  }
  
  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#init()
   */
  @Override
  protected void init() throws IOException {
    // Read GeneID 2 KEGG GENES ID mapping
    GeneID2KeggIDMapper gene2kegg = new GeneID2KeggIDMapper(organism_kegg_abbr, progress);
    gene2kegg.init();
    
    // Reverse this mapping and use for this class.
    reverse(gene2kegg);
    
    isInizialized = true;
  }
  
  /**
   * TESTS ONLY!
   * @throws Exception 
   */
  public static void main(String[] args) throws Exception {
    LogUtil.initializeLogging(Level.FINE);
    KeggGenesID2GeneID mapper = new KeggGenesID2GeneID("mmu");
    System.out.println(mapper.map("mmu:13091")); // CYP2B20
    System.out.println(mapper.map("mmu:11576")); // AFP
  }

  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getLocalFile()
   */
  @Override
  public String getLocalFile() {
    return null; // Not required. See GeneID2KeggIDMapper.java
  }

  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getMappingName()
   */
  @Override
  public String getMappingName() {
    return "KeggGenesID2GeneID";
  }

  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getRemoteURL()
   */
  @Override
  public String getRemoteURL() {
    return null; // Not required. See GeneID2KeggIDMapper.java
  }

  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getSourceColumn(de.zbit.io.CSVReader)
   */
  @Override
  public int getSourceColumn(CSVReader r) {
    return 0; // Not required. See GeneID2KeggIDMapper.java
  }

  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getTargetColumn(de.zbit.io.CSVReader)
   */
  @Override
  public int getTargetColumn(CSVReader r) {
    return 0; // Not required. See GeneID2KeggIDMapper.java
  }
  
}
