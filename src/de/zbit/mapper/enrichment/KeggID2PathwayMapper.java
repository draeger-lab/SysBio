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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import de.zbit.io.CSVReader;
import de.zbit.mapper.AbstractMapper;
import de.zbit.mapper.GeneID2KeggIDMapper;
import de.zbit.mapper.KeggPathwayID2PathwayName;
import de.zbit.parser.Species;
import de.zbit.util.AbstractProgressBar;
import de.zbit.util.FileTools;
import de.zbit.util.Utils;
import de.zbit.util.ValuePair;
import de.zbit.util.logging.LogUtil;
import de.zbit.util.prefs.Range;

/**
 * Maps the given KEGG gene id (see {@link GeneID2KeggIDMapper}) to
 * a {@link Collection} of KEGG Pathways, in which this gene occurs.
 * @author Clemens Wrzodek
 * @version $Rev$
 */
@SuppressWarnings("unchecked")
public class KeggID2PathwayMapper extends AbstractEnrichmentMapper<String, String> {
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
   * @param sourceType
   * @param targetType
   * @throws IOException
   */
  public KeggID2PathwayMapper(String speciesKEGGPrefix)
  throws IOException {
    this(speciesKEGGPrefix, null);
  }
  
  public KeggID2PathwayMapper(Species species) throws IOException {
    this(species.getKeggAbbr());
  }
  
  public KeggID2PathwayMapper(Species species, AbstractProgressBar progress) throws IOException {
    this(species.getKeggAbbr(), progress);
  }
  
  public KeggID2PathwayMapper(String speciesKEGGPrefix, AbstractProgressBar progress)
  throws IOException {
    // This constructor is called from every other!
    super(String.class, (Class<Collection<String>>) new ArrayList<String>().getClass(), progress);
    this.organism_kegg_abbr = speciesKEGGPrefix;
    init();
  }
  
  /**
   * TESTS ONLY!
   * @throws Exception 
   */
  public static void main(String[] args) throws Exception {
    LogUtil.initializeLogging(Level.FINE);
//    KeggID2PathwayMapper mapper = new KeggID2PathwayMapper("mmu");
//    
//    for (int i=0; i<2; i++) {
//      Collection c = mapper.map("mmu:11576");
//      if (c==null) System.out.println("null");
//      else System.out.println(Arrays.deepToString(c.toArray(new String[0])));
//      System.out.println("=================");
//      c = mapper.map("mmu:77579"); // 3 PWs
//      if (c==null) System.out.println("null");
//      else System.out.println(Arrays.deepToString(c.toArray(new String[0])));
//      System.out.println("=================");
//      c = mapper.map("mmu:16334");
//      if (c==null) System.out.println("null");
//      else System.out.println(Arrays.deepToString(c.toArray(new String[0])));
//      
//      System.out.println("NonUnique: " + mapper.getGenomeSize() + " Unique: " + mapper.size());
//      System.out.println(mapper.getEnrichmentClassSize("path:mmu04530"));
//      System.out.println(mapper.getEnrichmentClassSize("Tight junction"));
//      
//      if (i==0)mapper.convertPathwayIDsToPathwayNames();
//    }
    generateSmallCrypticSerializedFile("../Integrator/res/rno.list","../Integrator/res/mmu.list","../Integrator/res/hsa.list");
    generateSmallCrypticSerializedFile2("../Integrator/res/rno.list","../Integrator/res/mmu.list","../Integrator/res/hsa.list");
    generateSmallCrypticSerializedFile3("../Integrator/res/rno.list","../Integrator/res/mmu.list","../Integrator/res/hsa.list");
  }
  
  private static void generateSmallCrypticSerializedFile(String... listFiles) throws IOException {
    /* Initialize a mapping from kegg_Organism (e.g., "hsa") to
     * ValuePair<PathwayNumber, GenesInPathway (asRanges)>.
     */
    Map<String, List<ValuePair<Integer, Range<Integer>>>> orgPwGeneMap = new HashMap<String, List<ValuePair<Integer, Range<Integer>>>>();
    for (String currentFile: listFiles) {
      String organism_kegg_abbr = FileTools.trimExtension(new File(currentFile).getName()).toLowerCase().trim();
      Map<Integer, List<Integer>> pwToGene = new HashMap<Integer, List<Integer>>();
      // Create short file
      CSVReader r = new CSVReader(currentFile);
      String[] line;
      while ((line=r.getNextLine())!=null) {
        // Example for first two columns: path:rno04530 rno:85420
        // Skip everything thats not a gene (e.g., compounds starting with "CPD:*")
        if (!line[1].startsWith(organism_kegg_abbr)) continue;
        
        // Get pathway and gene number
        int pathwayNumber = Utils.getNumberFromStringRev(line[0].length(), line[0]);
        int geneNumber = Utils.getNumberFromStringRev(line[1].length(), line[1]);
        if (pathwayNumber<=0 || geneNumber<=0) continue;
        List<Integer> geneIds = pwToGene.get(pathwayNumber);
        if (geneIds==null) {
          geneIds = new ArrayList<Integer>();
          pwToGene.put(pathwayNumber, geneIds);
        }
        geneIds.add(geneNumber);
      }
      
      // We now have a mapping from pwNumber to GeneIDs in that pw
      List<ValuePair<Integer, Range<Integer>>> groupedPWgenes = new ArrayList<ValuePair<Integer, Range<Integer>>>(pwToGene.size());
      int sumOfGenes=0;
      for (Integer key : pwToGene.keySet()) {
        groupedPWgenes.add(new ValuePair<Integer, Range<Integer>>(key, Range.toIntegerRange(pwToGene.get(key))));
        sumOfGenes+=pwToGene.get(key).size();
      }
      
      // Add to global mapping file
      orgPwGeneMap.put(organism_kegg_abbr, groupedPWgenes);
      log.fine(String.format("Added %s pathways, mapping to %s genes for organism %s.", groupedPWgenes.size(), sumOfGenes, organism_kegg_abbr));
    }
    // Save as gzipped serialized file
    Utils.saveGZippedObject("kg2pw.dat", orgPwGeneMap);
  }
  
  private static void generateSmallCrypticSerializedFile2(String... listFiles) throws IOException {
    /* Initialize a mapping from kegg_Organism (e.g., "hsa") to
     * ValuePair<PathwayNumber, GenesInPathway (asRanges)>.
     */
    
    for (String currentFile: listFiles) {
      Map<Integer, List<Integer>> orgPwGeneMap = new HashMap<Integer, List<Integer>>();
      String organism_kegg_abbr = FileTools.trimExtension(new File(currentFile).getName()).toLowerCase().trim();
      // Create short file
      CSVReader r = new CSVReader(currentFile);
      String[] line;
      int sumOfGenes=0;
      while ((line=r.getNextLine())!=null) {
        // Example for first two columns: path:rno04530 rno:85420
        // Skip everything thats not a gene (e.g., compounds starting with "CPD:*")
        if (!line[1].startsWith(organism_kegg_abbr)) continue;
        
        // Get pathway and gene number
        int pathwayNumber = Utils.getNumberFromStringRev(line[0].length(), line[0]);
        int geneNumber = Utils.getNumberFromStringRev(line[1].length(), line[1]);
        if (pathwayNumber<=0 || geneNumber<=0) continue;
        List<Integer> geneIds = orgPwGeneMap.get(pathwayNumber);
        if (geneIds==null) {
          geneIds = new ArrayList<Integer>();
          orgPwGeneMap.put(pathwayNumber, geneIds);
        }
        sumOfGenes++;
        geneIds.add(geneNumber);
      }
      
      // // Save as gzipped serialized file
      Utils.saveGZippedObject(String.format("kg2pw_%s.dat", organism_kegg_abbr), orgPwGeneMap);
      log.fine(String.format("Added %s pathways, mapping to %s genes for organism %s.", orgPwGeneMap.size(), sumOfGenes, organism_kegg_abbr));
    }
  }
  
  private static void generateSmallCrypticSerializedFile3(String... listFiles) throws IOException {
    /* Initialize a mapping from kegg_Organism (e.g., "hsa") to
     * ValuePair<PathwayNumber, GenesInPathway (asRanges)>.
     */
    
    for (String currentFile: listFiles) {
      Map<Integer, Range<Integer>> orgPwGeneMap = new HashMap<Integer, Range<Integer>>();
      String organism_kegg_abbr = FileTools.trimExtension(new File(currentFile).getName()).toLowerCase().trim();
      // Create short file
      CSVReader r = new CSVReader(currentFile);
      String[] line;
      int sumOfGenes=0;
      Map<Integer, List<Integer>> pwToGene = new HashMap<Integer, List<Integer>>();
      while ((line=r.getNextLine())!=null) {
        // Example for first two columns: path:rno04530 rno:85420
        // Skip everything thats not a gene (e.g., compounds starting with "CPD:*")
        if (!line[1].startsWith(organism_kegg_abbr)) continue;
        
        // Get pathway and gene number
        int pathwayNumber = Utils.getNumberFromStringRev(line[0].length(), line[0]);
        int geneNumber = Utils.getNumberFromStringRev(line[1].length(), line[1]);
        if (pathwayNumber<=0 || geneNumber<=0) continue;
        List<Integer> geneIds = pwToGene.get(pathwayNumber);
        if (geneIds==null) {
          geneIds = new ArrayList<Integer>();
          pwToGene.put(pathwayNumber, geneIds);
        }
        geneIds.add(geneNumber);
        sumOfGenes++;
      }
      
      // We now have a mapping from pwNumber to GeneIDs in that pw
      for (Integer key : pwToGene.keySet()) {
        orgPwGeneMap.put(key, Range.toIntegerRange(pwToGene.get(key)));
      }
      
      // // Save as gzipped serialized file
      Utils.saveGZippedObject(String.format("kg2pw_%s_range.dat", organism_kegg_abbr), orgPwGeneMap);
      log.fine(String.format("Added %s pathways, mapping to %s genes for organism %s.", orgPwGeneMap.size(), sumOfGenes, organism_kegg_abbr));
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
    // Skip everything thats not a gene (e.g., compounds starting with "CPD:*")
    if (!line[getSourceColumn(null)].startsWith(organism_kegg_abbr)) return true;
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
  public int getGenomeSize() {
    return super.getGenomeSize();
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
