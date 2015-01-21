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
package de.zbit.kegg.api;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import keggapi.Definition;
import de.zbit.io.FileDownload;
import de.zbit.kegg.api.cache.KeggFunctionManagement;
import de.zbit.kegg.api.cache.KeggInfoManagement;
import de.zbit.util.StringUtil;

/**
 * A Kegg Adaptor, that directly retrieves informations from the
 * Kegg database.
 * You should not use this class directly, but the cached derivates
 * of this class e.g. {@link KeggInfoManagement} for general queries
 * or {@link KeggFunctionManagement} for directly accessing certain
 * methods.
 * 
 * <p>Since 2013, the old SOAP API has been deactivated (the api JAR is
 * NOT required anymore) and information is retrieved with a simple
 * REST (URL-download-based) interface.</p>
 * 
 * See http://www.kegg.jp/kegg/rest/keggapi.html
 * 
 * <p>NOTE: As a backup system, there is an URL based mirror at
 * http://togows.dbcls.jp
 * That means, the URL determines what is being retrieved.
 * E.g.
 * http://togows.dbcls.jp/entry/kegg-reaction/R02750
 * http://togows.dbcls.jp/entry/kegg-reaction/R02750/equation
 * </p>
 * 
 * @author Clemens Wrzodek
 * @version $Rev$
 * @since 1.0
 */
public class KeggAdaptor {
  /**
   * If true, all queried objects will be printed to the console (for debugging).
   */
  public static boolean printEachOutputToScreen = false;

  /**
   * KEE API is accessed with:
   * <pre>
   * http://rest.kegg.jp/(operation)/(argument)/(argument2 or option)
   * 
   * (operation) = info | list | find | get | conv | link
   * (argument) = (database) | (dbentries)
   * </pre>
   */
  protected final static String KEGG_API_REST_PREFIX = "http://rest.kegg.jp/";
  

  /**
   * @param args
   */
  public static void main(String[] args) {
    printEachOutputToScreen = true;
    KeggAdaptor adap = new KeggAdaptor();

    
    try {
      adap.getWithReturnInformation("mmu:436049");
    } catch (TimeoutException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    if (true) return;
    
    String ret = adap.get("rn:R07618");
    System.out.println("---\n"+KeggAdaptor.extractInfo(ret, "PATHWAY")+"\n---");
    
    String ttt = adap.get("path:hsa04010");
    System.out.println(ttt+"\n======================");
    
    String[] genesRaw = adap.getGenesByPathway("path:hsa04010");
    for (String s: genesRaw) {
      System.out.println(s);
    }
    System.out.println("======================");
    String genesString = adap.getIdentifier("hsa:9693 hsa:994 hsa:9965 hsa:998");
    System.out.println(genesString);
    System.out.println("======================");
    String weatt = adap.get("hsa:9965 hsa:998");
    System.out.println(extractInfo(weatt, "NAME", " "));
    if (true) return;

    
    adap.getPathwayList("hsa");
    adap.getOrganisms();
    
    String weat = adap.get("cpd:C00103 hsa:8491 rn:R05964 q:243 glycan:G00181");
    System.out.println(extractInfo(weat, "ENTRY", " "));
    if (true) return;
    
    //adap.get("hsa:8491");
    //System.out.println("======================");
    //adap.get("path:map00603");

    adap.getGenesByPathway("path:hsa04010");
    System.out.println("======================");
    String test = adap.get("hsa:4893");
    System.out.println(extractInfo(test, "MOTIF"));
    System.out.println(extractInfo(test, "Ensembl:", "\n").trim());
    if (true)
      return;

    // adap.get("hsa:8491");
    // System.out.println("======================");
    // adap.get("path:map00603");
    adap.get("glycan:G00181");
    System.out.println("======================");
    adap.get("ec:2.4.1.-");
    System.out.println("======================");
    adap.get("ko:K01204");
    System.out.println("======================");
    adap.get("cpd:C00031");
    System.out.println("======================");
    adap.get("dr:D00694");

    // adap.get("gn:hsa");
    if (true)
      return;
    adap.get("cpd:C00338");
    System.out.println("======================");
    adap.get("path:map04010");

    String infos = adap.get("ko:K04349");
    System.out.println(extractInfo(infos, "CLASS"));
    System.out.println(extractInfo(infos, "DEFINITION"));
    System.out.println(extractInfo(infos, "GENES"));

    adap.get("ko:K04349");
    adap.get("hsa:8491");
    if (true)
      return;

    adap.getOrganisms();

    adap.find("genes homer1");
    
  }
  
  
  
  /**
   * Extrahiert infos aus einem String. Beispiel: NAME MAP4K3 DEFINITION
   * mitogen-activated protein kinase kinase kinase kinase 3 (EC:2.7.11.1)
   * ORTHOLOGY KO: K04406 mitogen-activated protein kinase kinase kinase kinase
   * 3 [EC:2.7.11.1] => Hier waere startWith "NAME" oder "DEFINITION". (Case
   * insensitive)
   * 
   * @param completeString
   * @param startsWith
   * @return
   */
  public static String extractInfo(String completeString, String startsWith) {
    return extractInfo(completeString, startsWith, null);
  }
  
  /**
   * 
   * @param completeString
   * @param startsWith
   * @param endsWith
   * @return
   * @see #extractInfo(String, String)
   */
  public static String extractInfo(String completeString, String startsWith, String endsWith) {
    return extractInfoCaseSensitive(completeString, completeString.toUpperCase(), startsWith.toUpperCase(), endsWith!=null?endsWith.toUpperCase():null);
  }

  /**
   * Considering the case once and than calling this method internally is faster
   * than always performing the case-conversion.
   * @param completeOriginalString original cased complete string
   * @param uCaseComplete upper-cased {@code completeOriginalString}
   * @param startsWith upper-cased startswith string
   * @param endsWith upper-cased endswith string
   * @return String from {@code completeOriginalString} between {@code startsWith}
   * and {@code startsWith} (or to the end, if startsWith is null).
   * @see #extractInfo(String, String)
   */
  static String extractInfoCaseSensitive(String completeOriginalString, String uCaseComplete, String startsWith, String endsWith) {
    // Prefer hits starting in a new line. +1 because of \n
    int pos = uCaseComplete.indexOf('\n' + startsWith) + 1;
    
    if (pos <= 0) { // <=0 because of +1 in line above.
      pos = uCaseComplete.indexOf(startsWith);
      // Pruefen ob zeichen ausser \t und " " zwischen \n und pos. wenn ja =>
      // abort. (Beispiel: "  AUTHOR XYZ" moeglich.)
      if (pos < 0 || pos>=completeOriginalString.length())
        return null;
      int lPos = completeOriginalString.lastIndexOf('\n', pos);
      if (endsWith!=null) {
        // We do not want a complete block, but a sub-section of an existing block.
        lPos = Math.max(lPos, completeOriginalString.lastIndexOf("  ", pos));
      }
      String toCheck = completeOriginalString.substring(Math.max(lPos, 0), pos);
      // Wenn was zwischen unserem Treffer und newLine steht => abort.
      if (toCheck.replaceAll("\\s", "").length()>0)
        return null;
      // Wenn unser Treffer nicht von einem Whitespace char gefolgt wird => abort.
      if (!Character.isWhitespace(completeOriginalString.charAt(pos+startsWith.length())))
        return null;
    }
    if (pos + startsWith.length()>=completeOriginalString.length()) return "";

    String ret = "";
    if (endsWith == null || endsWith.length()<1) {
      int st = completeOriginalString.indexOf(' ', pos + startsWith.length());
      if (st < 0)
        st = pos + startsWith.length(); // +1 wegen "\n"+sw
      int nl = completeOriginalString.indexOf('\n', pos + startsWith.length());
      if (nl < 0)
        nl = completeOriginalString.length();

      try {
        ret = completeOriginalString.substring(st, nl).trim();
      } catch (Exception e) {
        System.out.println("St: " + st + " \t" + pos + " "
            + startsWith.length());
        System.out.println("Nl: " + nl + " \t" + completeOriginalString.length());
        System.out.println(startsWith);
        System.out.println("--------------\n");
        e.printStackTrace();
      }
      while (completeOriginalString.length() > (nl + 1)) {
        if (completeOriginalString.charAt(nl + 1) == ' ') {
          int nl2 = completeOriginalString.indexOf('\n', nl + 1);
          if (nl2 < 0)
            nl2 = completeOriginalString.length();
          ret += "\n" + completeOriginalString.substring(nl + 1, nl2).trim();
          nl = nl2;
        } else
          break;
      }
    } else {
      // Jump to first non-Whitespace Character. Mind the new lines!
      int sPos = pos+startsWith.length();
      while (sPos<completeOriginalString.length() &&
          Character.isWhitespace(completeOriginalString.charAt(sPos)) && completeOriginalString.charAt(sPos)!='\n') sPos++;
      if (sPos>=completeOriginalString.length()) return "";
      
      // Search for end position and trim string.
      int pos2 = uCaseComplete.indexOf(endsWith,sPos);
      if (pos2<=0) return "";
      ret = completeOriginalString.substring(sPos, pos2).trim();
    }
    
    return ret;
  }

  
  /**
   * 
   * @param results
   */
  private static void printToScreen(String[] results) {
    if (results == null) {
      System.out.println("NULL result.");
      return;
    }
    System.out.println(results.length + " string results :");
    for (int i = 0; i < results.length; i++) {
      System.out.println(results[i]);
    }
  }
  
  /**
   * This is the core method of the KEGG API. See
   * http://www.kegg.jp/kegg/rest/keggapi.html for instructions how to use.
   * 
   * @param operation
   *        one of info, list, find, get, conv, link
   * @param arguments
   *        database or dbentries
   * @return answer from the KEGG database.
   * @throws IOException
   */
  public String get(String operation, String... arguments ) throws IOException {
    // Assemble the KEGG API URL
    StringBuffer address = new StringBuffer(KEGG_API_REST_PREFIX);
    address.append(operation);
    if (arguments!=null) {
      for (String argument : arguments) {
        address.append('/');
        address.append(argument);
      }
    }
    
    // Get the requested information
    ByteArrayOutputStream bs = new ByteArrayOutputStream();
    BufferedOutputStream out = new BufferedOutputStream(bs);
    FileDownload.download(address.toString(), out, false);
    
    // Return the result
    return bs.toString();
  }


  /**
   * See http://www.genome.jp/dbget-bin/show_man?bfind
   * 
   * @param id format: "%database% %id1%+%id2%+...".
   * @return
   */
  public String find(String id) {
    String results = "";
    
    int pos = id.indexOf(' ');
    String db = id.substring(0, pos);
    String query = id.substring(pos+1, id.length()).replace(' ', '+');
      
    try {
      results = get("find", db,query);
    } catch (Exception e) {
      e.printStackTrace();
    }

    if (printEachOutputToScreen)
      System.out.println(results);
    return results;
  }

  /**
   * see http://www.genome.jp/dbget-bin/show_man?bget
   * http://www.genome.jp/dbget/dbget_manual.html
   * 
   * @param id MAXIMUM 10 identifiers, since 2013-01-01!
   * @return
   */
  public String get(String id) {
    String results = "";
    int retried = 0;

    // In a previous release of the KEGG API, multiple ids had to be separated with a space. Now it is a plus.
    id = id.replace(' ', '+');
    // Furthermore, since 2013-01-01, the prefixes (hsa:, ko:, ec:, etc.) need to be lowercased!
    id = id.toLowerCase();
    
    // Wenn mehrere Threads/ Instanzen gleichzeitig laufen, kommts zu starken
    // delays. deshalb: 3x probieren.
    while (retried < 3) {
      try {
        results = get("get", id);
        break;
      } catch (Exception e) {
        retried++;
        if (retried == 3) {
          e.printStackTrace();
        }
      }
    }

    if (printEachOutputToScreen)
      System.out.println(results);
    
    return results;
  }
  
  /**
   * 
   * @param pathway_id e.g. 'path:hsa00650'
   * @return String array with all genes of the pathway, e.g. 'hsa:10327', 'hsa:123'
   */
  public String[] getGenesByPathway(String pathway_id) {
    
    String content = get(pathway_id);
    String genesInPW = extractInfo(content, "GENE");
    /* genesInPW now contains lines like:
     * "5214  PFKP; phosphofructokinase, platelet [KO:K00850] [EC:2.7.1.11]" */
    
    // Extract the organism prefix from "path:hsa00650" or "hsa00650"
    String organismPrefix = "";
    int pos = pathway_id.indexOf(":");
    if (pos<0) pos = 0;
    while (pos<pathway_id.length()) {
      char c = pathway_id.charAt(pos);
      if (Character.isLetter(c)) {
        organismPrefix+=c;
      } else {
        break;
      }
      pos++;
    }
    
    // Create return array;
    String[] list = genesInPW.split("\n");
    String[] returns = new String[list.length];
    int i=0;
    for (String line : list) {
      int num = de.zbit.util.Utils.getNumberFromString(0, line.trim());
      returns[i++] = String.format("%s:%s", organismPrefix, num);
    }
    
    if (printEachOutputToScreen)
      printToScreen(returns);
    
    return returns;
  }

  /**
   * 
   * @param a string with all hsa-numbers('hsa:103') separated with a blank
   * @return KG-Number, GeneSymbol, Gene Description, one per line. e.g.:
   *   hsa:9693 RAPGEF2; Rap guanine nucleotide exchange factor (GEF) 2; K08018 Rap guanine nucleotide exchange factor (GEF) 2
   *   hsa:994 CDC25B; cell division cycle 25 homolog B (S. pombe) (EC:3.1.3.48); K05866 cell division cycle 25B [EC:3.1.3.48]
   */
  public String getIdentifier(String id) {
    String s = "";
    try {
      s = get("list", id.replace(' ', '+'));
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    if (printEachOutputToScreen)
      if (s!=null && s.length()>0) System.out.println(s);
    
    return s;
  }
  

  /**
   * @param gene symbol
   * @param species - optional. E.g. "hsa". Set to null for all.
   * @return kegg identifiers in an arrayList e.g. "hsa:7529"
   */
  public ArrayList<String> getKEGGIdentifierForAGeneSymbol(String gene, String species) {
    ArrayList<String> identifiers = new ArrayList<String>();
    String s = "";

    try {
      String gg = "genes " + gene;
      s = find(gg);
      BufferedReader br = new BufferedReader(new StringReader(s));
      String line;
      while ((line = br.readLine()) != null) {
        if (species==null || species.length()<1 ||
        		line.startsWith(species.toLowerCase())) {
          identifiers.add(line.substring(0, line.indexOf(" ")));
          if (printEachOutputToScreen) System.out.println(line.substring(0, line.indexOf(" ")));
        }
      }
    } catch (RemoteException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return identifiers;
  }
  
  /**
   * Retrieves a list of available organisms from KEGG.
   * 
   * @return a list if valuepairs with KEGG abbreviation (e.g., mmu) and full
   *         name (e.g., "mus musculus"). Returns {@code null} if an error
   *         occurs.
   */
  public Definition[] getOrganisms() {
    String results = null;
    
    try {
      results = get("list", "organism");
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    if (results == null) return null;
    if (printEachOutputToScreen) {
      System.out.println(results);
    }
    
    // results is now a single string consisting of lines that look like:
    // T00727 min Methylacidiphilum infernorum  Prokaryotes;Bacteria;Verrucomicrobia;Methylacidiphilum
    List<Definition> org = splitMatrixStringToDefinition(results, 1, 2);
    
    return org.toArray(new Definition[0]);
  }



  /**
   * @param results
   * @return
   */
  private List<Definition> splitMatrixStringToDefinition(String results, int col_entry_id, int col_definition) {
    if (results == null) return null;
    List<Definition> org = new ArrayList<Definition>();
    for (String line : results.split("\n")) {
      String[] cells = line.split("\t");
      org.add(new Definition(cells[col_entry_id], cells[col_definition]));
    }
    return org;
  }

  /**
   * 
   * @param org
   * @return
   */
  public ArrayList<String> getPathwayList(String org) {
    ArrayList<String> pws = new ArrayList<String>();
    
    try {
      String result = get("list", "pathway", org);
      if (result == null) {
        return null;
      }
      
      int pos = 0;
      while(pos>=0) {
        int ePos = result.indexOf('\t', pos+1);
        if (pos>=0 && ePos > pos) {
          pws.add(result.substring(pos, ePos).trim());
        }
        
        pos = result.indexOf('\n', pos+1);
      }
      
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    if (printEachOutputToScreen) {
      for (String s : pws) {
        System.out.println(s);
      }
    }
    
    return pws;
  }

  /**
   * 
   * @param org
   * @return
   */
  public Definition[] getPathways(String org) {
    String result=null;
    try {
      result = get("list", "pathway", org);
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    if (result == null) {
      return null;
    }
    

    if (printEachOutputToScreen)
      System.out.println(result);
    
    return splitMatrixStringToDefinition(result, 0, 1).toArray(new Definition[0]);
  }

  /**
   * 
   * @param geneList
   *          as a string array with kegg identifiers e.g. 'hsa:7529' list size
   *          can be 1
   * @return pathways identifiers (e.g. 'path:hsa04110') in a String array 
   */
  public String[] getPathwaysByGenes(String... geneList) {
    String[] pathways = new String[0];
    
    String result;
    try {
      result = get("link", "pathway", StringUtil.implode(geneList, "+"));
      
      // result is a matrix with two columns. We need an array of the second one.
      ArrayList<String> pws = new ArrayList<String>();
      int pos = 0;
      while(pos>=0) {
        int ePos = result.indexOf('\t', pos+1);
        pos = result.indexOf('\n', pos+1);
        
        // Don't forget the last entry
        if (ePos>0 && pos<0) {
          pos = result.length();
        }
        if (pos>=0 && ePos < pos) {
          pws.add(result.substring(ePos, pos).trim());
        }
      }
      pathways = pws.toArray(new String[0]);
      
    } catch (Exception e) {
      e.printStackTrace();
    }

    if (printEachOutputToScreen)
      printToScreen(pathways);
    
    return pathways;
  }

  /**
   * 
   * @param id MAXIMUM 10 identifiers, since 2013-01-01!
   * @return
   * @throws TimeoutException
   */
  public String getWithReturnInformation(String id) throws TimeoutException {
    String results = "";
    int retried = 0;

    // In a previous release of the KEGG API, multiple ids had to be separated with a space. Now it is a plus.
    id = id.replace(' ', '+');
    // Furthermore, since 2013-01-01, the prefixes (hsa:, ko:, ec:, etc.) need to be lowercased!
    id = id.toLowerCase();
    
    // Wenn mehrere Threads/ Instanzen gleichzeitig laufen, kommts zu starken
    // delays. deshalb: 3x probieren.
    while (retried < 3) {
      try {
        results = get("get", id);
        break;
      } catch (Exception e) {
        retried++;
        if (retried <= 3) {
          throw new TimeoutException(e.getMessage());
        }
      }
    }

    if (printEachOutputToScreen)
      System.out.println(results);
    
    return results;
  }



  /**
   * Convert from an external identifier to a KEGG identifier.
   * @param keggDB "genes" or "compound" 
   * @param id "chebi:16761" or "ncbi-geneid:1644"
   * @return Tabbed mapping from original_ID to KEGG_ID.
   * @throws IOException 
   */
  public String convert(String keggDB, String id) throws IOException {
    return get("conv", keggDB.toLowerCase(), id.toLowerCase());
  }

}
