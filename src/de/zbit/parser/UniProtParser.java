/**
 * 
 */
package de.zbit.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import data.Gene;
import de.zbit.dbfetch.UniProtFetcher;
import de.zbit.util.InfoManagement;
import de.zbit.util.SortedArrayList;

/**
 * @author Finja B&uml;chel
 * 
 * Parse information out of the Strings it gets from the UniProtWrapper
 * 
 * 
 */
public class UniProtParser {
  
  public static final Logger log = Logger.getLogger(UniProtParser.class);
  UniProtFetcher UniProtManagement = null;

  public UniProtParser(UniProtFetcher w) {
    super();
    this.UniProtManagement = w;
  }
  
  public UniProtParser() {
    super();
    try {
      if (new File("uniprot.dat").exists())
        UniProtManagement = (UniProtFetcher) UniProtManagement.loadFromFilesystem("uniprot.dat");
    } catch (Throwable e) {
    }
    if (UniProtManagement == null)
      UniProtManagement = new UniProtFetcher(80000);
  }
  
  public SortedArrayList<String> getGeneNames(String block){
    SortedArrayList<String> names = new SortedArrayList<String>();
    String[] lineSplit = block.split("\n");
    
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
                e = e.trim();
                if(!names.contains(e))
                  names.add(e);
              }
            }
          }
        }    
      }
    }
    return names;
  }

  public SortedArrayList<String> getGeneSynonyms(String block){
    SortedArrayList<String> synonyms = new SortedArrayList<String>();
    
    return synonyms;
  }
  
  public SortedArrayList<String> getGeneOrderedLocusNames(String block){
    SortedArrayList<String> orderedLocusNames = new SortedArrayList<String>();
    
    return orderedLocusNames;
  }
  
  public SortedArrayList<String> getGeneOrfNames(String block){
    SortedArrayList<String> orfNames = new SortedArrayList<String>();
    
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
  public ArrayList<String>[] getGeneBlock(String[] identifier) {
    log.info("getGene identifier.length(): " + identifier.length);
    
    String[] proteinBlock = UniProtManagement.getInformations(identifier);
    log.info("proteins.length; " + proteinBlock.length);
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
        if (line.startsWith("GN")){
          line = line.substring(5,line.length());
          line = line.trim().toUpperCase();
          if(line.equals("AND")){
            geneBlocks.add(sb.toString());
            sb.setLength(0);
          }
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

    String[] results = UniProtManagement.getInformations(acs);
    
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
    return UniProtManagement;
  }
}
