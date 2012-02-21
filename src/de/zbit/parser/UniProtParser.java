/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2012 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

import de.zbit.dbfetch.UniProtFetcher;
import de.zbit.util.AbstractProgressBar;
import de.zbit.util.SortedArrayList;

/**
 * @author Finja B&uml;chel
 * 
 * Parse information out of the Strings it gets from the UniProtFetcher
 * 
 * @version $Rev$
 * @since 1.0
 */
public class UniProtParser {
  
  protected static final Logger log = Logger.getLogger(UniProtParser.class.getName());
  UniProtFetcher UniProtFetcher = null;

  public UniProtParser(UniProtFetcher w) {
    super();
    this.UniProtFetcher = w;
  }
  
  public UniProtParser() {
    super();
    if (UniProtFetcher == null) {
      log.fine("Initialize new and empty UniprotFetcher cache.");
      UniProtFetcher = new UniProtFetcher(80000);
    }
  }
  
  /**
   * 
   * @param geneblock, containing the whole gene block of the uniprot entry, parsed by  {@link #getGeneBlocks(String[])} 
   * @return the offical gene name of a protein
   */
  public String getGeneSymbolName(String geneBlock){
    String name = "";
    String[] lineSplit = geneBlock.split("\n");
    
    for (String line : lineSplit) {
      if(line!=null && line.length()>0){
        String[] geneSplit = line.split(";");
        for (String gs : geneSplit) {
          gs = gs.trim();
          String[] split = gs.split("=");
          if(split.length==2){          
            String[] entry = split[1].trim().split(",");

            if(split[0].trim().equals("NAME")){
              for (String e : entry) {
                name = e.trim();
              }
            }
          }
        }    
      }
    }
    return name;
  }

  public SortedArrayList<String> getGeneSynonyms(String block){
    SortedArrayList<String> synonyms = new SortedArrayList<String>();
    String[] lineSplit = block.split("\n");
    
    for (String line : lineSplit) {
      if(line!=null && line.length()>0){
        String[] geneSplit = line.split(";");
        for (String gs : geneSplit) {
          gs = gs.trim();
          String[] split = gs.split("=");
          if(split.length==2){          
            String[] entry = split[1].trim().split(",");

            if(split[0].trim().equals("SYNONYMS")){
              for (String e : entry) {
                e = e.trim();
                if(!synonyms.contains(e))
                  synonyms.add(e);
              }
            }
          }
        }    
      }
    }
    return synonyms;
  }
  
  public SortedArrayList<String> getGeneOrderedLocusNames(String block){
    SortedArrayList<String> orderedLocusNames = new SortedArrayList<String>();
    String[] lineSplit = block.split("\n");
    
    for (String line : lineSplit) {
      if(line!=null && line.length()>0){
        String[] geneSplit = line.split(";");
        for (String gs : geneSplit) {
          gs = gs.trim();
          String[] split = gs.split("=");
          if(split.length==2){          
            String[] entry = split[1].trim().split(",");

            if(split[0].trim().equals("ORDEREDLOCUSNAMES")){
              for (String e : entry) {
                e = e.trim();
                if(!orderedLocusNames.contains(e))
                  orderedLocusNames.add(e);
              }
            }
          }
        }    
      }
    }
    return orderedLocusNames;
  }
  
  public SortedArrayList<String> getGeneOrfNames(String block){
    SortedArrayList<String> orfNames = new SortedArrayList<String>();
    String[] lineSplit = block.split("\n");
    
    for (String line : lineSplit) {
      if(line!=null && line.length()>0){
        String[] geneSplit = line.split(";");
        for (String gs : geneSplit) {
          gs = gs.trim();
          String[] split = gs.split("=");
          if(split.length==2){          
            String[] entry = split[1].trim().split(",");

            if(split[0].trim().equals("ORFNAMES")){
              for (String e : entry) {
                e = e.trim();
                if(!orfNames.contains(e))
                  orfNames.add(e);
              }
            }
          }
        }    
      }
    }
    return orfNames;
  }
  
  /**
   * @param array of UniProt ids 
   * @return a list of arrays with gene identifiers
   *             [0] = protein id (SortedArrayList<String> 
   *             [1] = names (SortedArrayList<String>)
   *             [2] = synonyms (SortedArrayList<String>)
   *             [3] = orderedLocusNames (SortedArrayList<String>)
   *             [4] = orfNames (SortedArrayList<String>)
   *  
   */
  public ArrayList<String>[] getGeneBlocks(String[] identifier) {
    log.fine("getGene identifier.length(): " + identifier.length);
    
    String[] proteinBlock = UniProtFetcher.getInformations(identifier);
    log.fine("proteins.length; " + proteinBlock.length);
    ArrayList<String>[] result = new ArrayList[identifier.length];
    
    
    for (int i=0; i<proteinBlock.length; i++) {
      String block = proteinBlock[i];

      if (block==null) {
        continue;
      }

      ArrayList<String> geneBlocks = new ArrayList<String>();
      StringBuffer sb = new StringBuffer(); 
      
      String[] splitLines = block.split("\n");  // separates lines
      for (String line : splitLines) {
        if (line.startsWith("GN   ")){
          line = line.substring(5,line.length());
          line = line.trim().toUpperCase();
          if(line.equals("AND")){
            geneBlocks.add(sb.toString());
            sb.setLength(0);
          }
          else
            sb.append(line + "\n");
        }
      }
      geneBlocks.add(sb.toString());
      
      result[i] = geneBlocks;
    }

    return result;
  }

  /**
   * @param uniprot acs (necessary, that the ac doesn't contain "-"
   * @return list of [uniprot ac][uniprot id]
   */
  public ArrayList<String[]> getUniProtID(String[] acs) {
    
    ArrayList<String[]> ids = new ArrayList<String[]>();
    String[] uniProtBlockLine, splitLine;

    String[] results = UniProtFetcher.getInformations(acs);
    
    if(results!=null){
      for (int i=0; i<results.length; i++) {
        String uniProtBlock = results[i];

        if (uniProtBlock==null) {
          continue;
        }

        while (uniProtBlock.contains("   ")) {
          uniProtBlock = uniProtBlock.replace("   ", "  ");
        }

        uniProtBlock = uniProtBlock.replace("  ", "\t");
        uniProtBlockLine = uniProtBlock.split("\n");
        
        String id = "";
        for (String line : uniProtBlockLine) {
          line = line.trim().toUpperCase();
          splitLine = line.split("\t");

          if (splitLine[0].equals("ID")) {
            id = splitLine[1].trim();
            if (id.contains(" "))
              id = id.substring(0, id.indexOf(" "));
            break;
          }

          /*if (splitLine[0].equals("AC")) {
            String[] splitACLine = splitLine[1].split(";");
            for(int j=0; j<splitACLine.length; j++){ 
              if(splitACLine[j].trim().startsWith(acs[i])){
                ids.add(new String[]{acs[i], id});
              }
            }
          }*/
          
        }
        ids.add(new String[]{acs[i], id});
      }
    }
    

    return ids;
  }

  /**
   * @return
   */
  public UniProtFetcher getUniprotManager() {
    return UniProtFetcher;
  }

  /**
   * in the DR line the NCBI/RefSeq gene identifier is safed
   * Format: 
   * DR   RESOURCE_ABBREVIATION; RESOURCE_IDENTIFIER; OPTIONAL_INFORMATION_1[; OPTIONAL_INFORMATION_2][; OPTIONAL_INFORMATION_3].
   * RESOURCE_ABBREVIATION = GeneID;
   * 
   * @return gene id, defined by NCBI, RefSeq; default = -1;
   */
  public int getGeneID(String uniProtID) {
    int geneID = -1;
    
    String result = UniProtFetcher.getInformation(uniProtID);
    String[] split = result.split("\n");
    for (String line : split) {
      if(line.startsWith("DR   GeneID")){
        String[] splitID = line.split(";");
        geneID = Integer.parseInt(splitID[1].trim());
      }
    }
    
    return geneID;
  }
  
  /**
   * in the DR line the NCBI/RefSeq gene identifier is safed
   * Format: 
   * DR   RESOURCE_ABBREVIATION; RESOURCE_IDENTIFIER; OPTIONAL_INFORMATION_1[; OPTIONAL_INFORMATION_2][; OPTIONAL_INFORMATION_3].
   * RESOURCE_ABBREVIATION = GeneID;
   * 
   * @return gene id, defined by NCBI, RefSeq; default = -1;
   */
  public int[] getGeneIDs(String[] identifier, AbstractProgressBar progress) {
    log.fine("getGene identifier.length(): " + identifier.length);
    
    String[] proteinBlock = UniProtFetcher.getInformations(identifier, progress);
    log.fine("proteins.length; " + proteinBlock.length);
    int[] geneIDs = new int[identifier.length];
    Arrays.fill(geneIDs, -1);
    
    for (int i=0; i<proteinBlock.length; i++) {
      if (proteinBlock[i]==null) continue;
      
      String[] split = proteinBlock[i].split("\n");
      for (String line : split) {
        if(line.startsWith("DR   GeneID")){
          String[] splitID = line.split(";");
          geneIDs[i] = Integer.parseInt(splitID[1].trim());
          continue;
        }
      }
      
    }
    
    return geneIDs;
  }
 

  
}
