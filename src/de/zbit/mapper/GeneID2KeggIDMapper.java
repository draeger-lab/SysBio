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
package de.zbit.mapper;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.zbit.io.FileTools;
import de.zbit.io.csv.CSVReader;
import de.zbit.util.Species;
import de.zbit.util.logging.LogUtil;
import de.zbit.util.progressbar.AbstractProgressBar;

/**
 * Maps NCBI Gene IDs (Entrez, previously Lokuslink) to KEGG ids.
 * This is often simply organism:gene_id, but unfortunately not always.
 * See <a href="http://www.genome.jp/kegg/kegg3.html">http://www.genome.jp/kegg/kegg3.html</a>
 * 
 * <p>Quote:<br/>"Entry names of the KEGG GENES database are usually locus_tags
 * given by the International Nucleotide Sequence Database Collaboration
 * (INSDC). The major sequence databases such as NCBI and UniProt/Swiss-Prot
 * use different sets of gene/protein identifiers."</p>
 * 
 * <p>HINT:<br/>If you can't map something, try to use organism:gene_id as heuristic.
 * This should work in 90% of all cases.</p>
 * 
 * <p>XXX: KEGG Pathway FTP is no more available. You'll have to use
 * cached KEGG mapping files for this class to work.
 * 
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class GeneID2KeggIDMapper extends AbstractMapper<Integer, String> implements KEGGspeciesAbbreviation {
  private static final long serialVersionUID = -4655963680015519436L;

  public static final Logger log = Logger.getLogger(GeneID2KeggIDMapper.class.getName());
  
  /**
   * The URL where the file should be downloaded, if it is not available.
   * @return 
   */
  private final static String getDownloadURL(String kegg_abbr) {
    return String.format("ftp://ftp.genome.jp/pub/kegg/genes/organisms/%s/%s_ncbi-geneid.list", kegg_abbr,kegg_abbr);
  }

  /**
   * This is required. (e.g. "mmu" for mouse, or "hsa" for human).
   * @see Species#getKeggAbbr()
   */
  private String organism_kegg_abbr = null;
  
  /**
   * @param sourceType
   * @param targetType
   * @throws IOException
   */
  public GeneID2KeggIDMapper(String speciesKEGGPrefix)
  throws IOException {
    this(speciesKEGGPrefix, null);
  }
  
  public GeneID2KeggIDMapper(Species species) throws IOException {
    this(species.getKeggAbbr());
  }
  
  public GeneID2KeggIDMapper(Species species, AbstractProgressBar progress) throws IOException {
    this(species.getKeggAbbr(), progress);
  }
  
  public GeneID2KeggIDMapper(String speciesKEGGPrefix, AbstractProgressBar progress)
  throws IOException {
    // This constructor is called from every other!
    super(Integer.class, String.class, progress);
    organism_kegg_abbr = speciesKEGGPrefix;
    init();
  }
  
  /**
   * TESTS ONLY!
   * @throws Exception 
   */
  public static void main(String[] args) throws Exception {
    LogUtil.initializeLogging(Level.FINE);
    GeneID2KeggIDMapper mapper = new GeneID2KeggIDMapper("mmu");
    System.out.println(mapper.map(13091)); // CYP2B20
    System.out.println(mapper.map(11576)); // AFP
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
    return "GeneID2KeggID";
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
    // r.getColumnByMatchingContent("^ncbi-geneid.*");
    return 1;
  }

  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getTargetColumn(de.zbit.io.CSVReader)
   */
  @Override
  public int getTargetColumn(CSVReader r) {
    //r.getColumnByMatchingContent("^" + Pattern.quote(organism_kegg_abbr) + ".*");
    return 0;
  }
  
  /** {@inheritDoc}*/
  @Override
  protected String preProcessSourceID(String source) {
    // Cut the "ncbi-geneid:" prefix
    return source.substring(12);
  }
  
  /**
   * @return the KEGG abbreviation for the current species.
   */
  public String getSpeciesKEGGabbreviation() {
    return organism_kegg_abbr;
  }
  
  
}
