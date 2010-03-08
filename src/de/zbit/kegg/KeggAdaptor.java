package de.zbit.kegg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

import keggapi.Definition;
import keggapi.KEGGLocator;
import keggapi.KEGGPortType;
import keggapi.SSDBRelation;

/**
 * @author wrzodek
 * 
 *         Nicht alle funktionen implementiert! Siehe
 *         http://www.genome.jp/kegg/docs/keggapi_manual.html
 * 
 *         Serializable funktioniert nicht, da Kegg-API nicht serialisierbar.
 */
public class KeggAdaptor implements Serializable {
  private static final long serialVersionUID = 3338264258735043999L;

  public static boolean printEachOutputToScreen = false;

  private KEGGPortType serv;

  /**
   * @param args
   */
  public static void main(String[] args) {
    printEachOutputToScreen = true;

    KeggAdaptor adap = new KeggAdaptor();
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

    // adap.get("GN:hsa");
    if (true)
      return;
    adap.get("cpd:C00338");
    System.out.println("======================");
    adap.get("path:map04010");

    String infos = adap.get("ko:K04349");
    System.out.println(extractInfo(infos, "CLASS"));
    System.out.println(extractInfo(infos, "DEFINITION"));
    System.out.println(extractInfo(infos, "GENES"));

    adap.getGenesForKO("ko:K04349", "hsa");
    adap.get("ko:K04349");
    adap.get("hsa:8491");
    if (true)
      return;

    adap.getDatabases();
    adap.getOrganisms();

    adap.find("genes homer1");

    if (true)
      return;
    // NCBI-GeneID: 8491
    adap.getEntrezIDs("hsa:8491");
    if (true)
      return;

    adap.getGenesForKO("ko:K04349", "hsa"); // RASGRF
    // getBestNeighborsByGene("eco:b0002");

    adap.getDatabases();
    // getOrganisms();
    // getPathways("hsa");

    adap.get("path:map04010");
    if (true)
      return;

    String s = "ko:K04344 ko:K04849 ko:K04850 ko:K04851 ko:K04852 ko:K04853 ko:K04854 ko:K04855 ko:K04856 ko:K04857 ko:K05315 ko:K04858 ko:K04859 ko:K04860 ko:K04861 ko:K05316 ko:K04862 ko:K04863 ko:K04864 ko:K04865 ko:K05317 ko:K04866 ko:K04867 ko:K04868 ko:K04869 ko:K04870 ko:K04871 ko:K04872 ko:K04873";
    for (int i = 0; i < s.split(" ").length; i++) {
      adap.getGenesForKO(s.split(" ")[i], "hsa");
    }

    // adap.get("cpd:C00076");
    // adap.find("map kinase");

  }

  public KeggAdaptor() {
    serv = getServlet();
  }

  public String[] getGenesByPathway(String pathway_id) {
    String[] results = new String[0];
    try {
      results = serv.get_genes_by_pathway(pathway_id);
    } catch (RemoteException e) {
      e.printStackTrace();
    }

    if (printEachOutputToScreen)
      printToScreen(results);
    return results;
  }

  private String last_in_id = "";
  private String[] last_id_results = new String[3]; // Siehe 2 Zeilen tiefer

  private void retrieveVariousIDs(String in_id) { // z.B. "hsa:8491"
    String key[] = new String[] { "NCBI-GeneID:", "UniProt:", "Ensembl:" }; // Siehe
                                                                            // 2
                                                                            // Zeilen
                                                                            // dr�ber

    last_in_id = new String(in_id);
    in_id = in_id.replace(",", " ");
    String[] split = in_id.split(" ");

    for (int k = 0; k < key.length; k++)
      last_id_results[k] = "";

    for (String s : split) { // NCBI-GeneID: 8491
      String r = get(s);
      for (int k = 0; k < key.length; k++) {
        String ke = key[k];
        int pos = r.indexOf(ke);
        if (pos < 0)
          continue;

        String te = r.substring(pos + ke.length(), r.indexOf("\n", pos)).trim();
        if (!te.isEmpty())
          last_id_results[k] += te + ",";
      }
    }

    for (int k = 0; k < key.length; k++)
      if (last_id_results[k].length() > 0 && last_id_results[k].endsWith(","))
        last_id_results[k] = last_id_results[k].substring(0, last_id_results[k]
            .length() - 1);
  }

  private String getVariousIDs(String in_id, int index) {
    if (!in_id.equalsIgnoreCase(last_in_id))
      retrieveVariousIDs(in_id);

    if (printEachOutputToScreen)
      System.out.println(last_id_results[index]);
    return last_id_results[index];
  }

  public String getEntrezIDs(String in_id) { // z.B. "hsa:8491"
    return getVariousIDs(in_id, 0);
  }

  public String getUniprotIDs(String in_id) { // z.B. "hsa:8491"
    return getVariousIDs(in_id, 1);
  }

  public String getEnsEmblIDs(String in_id) { // z.B. "hsa:8491"
    return getVariousIDs(in_id, 2);
  }

  /**
   * z.B."GAPDH; glyceraldehyde-3-phosphate dehydrogenase (EC:1.2.1.12); K00134 glyceraldehyde 3-phosphate dehydrogenase [EC:1.2.1.12]"
   * 
   * @param ko_id
   * @param org
   * @return
   */
  public Definition[] getGenesForKO(String ko_id, String org) {
    Definition[] results = new Definition[0];

    try {
      if (org != null && !org.trim().isEmpty())
        results = serv.get_genes_by_ko(ko_id, org);
      else
        results = serv.get_genes_by_ko(ko_id, "");
    } catch (Exception e) {
      e.printStackTrace();
    }

    if (printEachOutputToScreen)
      printToScreen(results);
    return results;
  }

  public Definition[] getGenesForKO(String ko_id) {
    return getGenesForKO(ko_id, "");
  }

  public String[] getCompounds(String id) {
    String[] results = new String[0];
    try {
      results = serv.get_reactions_by_compound(id);
      if (printEachOutputToScreen)
        printToScreen(results);

      results = serv.get_pathways_by_compounds(new String[] { id });
      if (printEachOutputToScreen)
        printToScreen(results);

      results = serv.get_compounds_by_pathway(id);
      if (printEachOutputToScreen)
        printToScreen(results);

      results = serv.search_compounds_by_name(id);
      if (printEachOutputToScreen)
        printToScreen(results);
    } catch (Exception e) {
      e.printStackTrace();
    }

    // if (printEachOutputToScreen) printToScreen(results);
    return results;
  }

  /**
   * see http://www.genome.jp/dbget-bin/show_man?bget
   * http://www.genome.jp/dbget/dbget_manual.html
   * 
   * @param id
   * @return
   */
  public String get(String id) {
    String results = "";
    int retried = 0;

    // Wenn mehrere Threads/ Instanzen gleichzeitig laufen, kommts zu starken
    // delays. deshalb: 3x probieren.
    while (retried < 3) {
      try {
        results = serv.bget(id); // <--
        break;
      } catch (RemoteException e) {
        retried++;
        if (retried == 3)
          e.printStackTrace();
      }
    }

    if (printEachOutputToScreen)
      System.out.println(results);
    return results;
  }

  public String getWithReturnInformation(String id) throws TimeoutException {
    String results = null;
    int retried = 0;

    // Wenn mehrere Threads/ Instanzen gleichzeitig laufen, kommts zu starken
    // delays. deshalb: 3x probieren.
    while (retried < 3) {
      try {
        results = serv.bget(id); // <--
        break;
      } catch (RemoteException e) {
        retried++;
        if (retried >= 3)
          throw new TimeoutException();
      }
    }

    if (printEachOutputToScreen)
      System.out.println(results);
    return results;
  }

  /**
   * See http://www.genome.jp/dbget-bin/show_man?bfind
   * 
   * @param id
   * @return
   */
  public String find(String id) {
    String results = "";
    try {
      results = serv.bfind(id);
    } catch (RemoteException e) {
      e.printStackTrace();
    }

    if (printEachOutputToScreen)
      System.out.println(results);
    return results;
  }

  public Definition[] getOrganisms() {
    Definition[] results = new Definition[0];
    try {
      results = serv.list_organisms();
    } catch (RemoteException e) {
      e.printStackTrace();
    }

    if (printEachOutputToScreen)
      printToScreen(results);
    return results;
  }

  public String getIdentifier(String id) {
    String s = "";
    try {
      s = serv.btit(id);
    } catch (RemoteException e) {
      e.printStackTrace();
    }
    return s;
  }

  public Definition[] getDatabases() {
    Definition[] results = new Definition[0];
    try {
      results = serv.list_databases();
    } catch (RemoteException e) {
      e.printStackTrace();
    }

    if (printEachOutputToScreen)
      printToScreen(results);
    return results;
  }

  /**
   * 
   * @param geneList
   *          as a string array with kegg identifiers e.g. 'hsa:7529' list size
   *          can be 1
   * @return
   */
  public String[] getPathwaysByGenes(String[] geneList) {
    String[] pathways = new String[0];
    try {
      pathways = serv.get_pathways_by_genes(geneList);
    } catch (RemoteException e) {
      e.printStackTrace();
    }

    return pathways;
  }

  public Definition[] getPathways(String org) {
    Definition[] results = new Definition[0];
    try {
      results = serv.list_pathways(org);
    } catch (RemoteException e) {
      e.printStackTrace();
    }

    if (printEachOutputToScreen)
      printToScreen(results);
    return results;
  }

  public ArrayList<String> getPathwayList(String org) {
    ArrayList<String> pws = new ArrayList<String>();
    Definition[] results = new Definition[0];
    try {
      results = serv.list_pathways(org);
      for (Definition definition : results) {
        System.out.println(definition.getEntry_id());
        pws.add(definition.getEntry_id());
      }
    } catch (RemoteException e) {
      e.printStackTrace();
    }
    
    return pws;
  }
  
  public Definition[] getKoClasses(String class_id) {
    Definition[] results = new Definition[0];
    try {
      results = serv.list_ko_classes(class_id);
    } catch (RemoteException e) {
      e.printStackTrace();
    }

    if (printEachOutputToScreen)
      printToScreen(results);
    return results;
  }

  public SSDBRelation[] getBestNeighborsByGene(String genes_id) {
    return getBestNeighborsByGene(genes_id, 1, 50);
  }

  public SSDBRelation[] getBestNeighborsByGene(String genes_id, int offset,
      int limit) {
    SSDBRelation[] results = new SSDBRelation[0];
    try {
      results = serv.get_best_neighbors_by_gene(genes_id, offset, limit);
    } catch (RemoteException e) {
      e.printStackTrace();
    }

    if (printEachOutputToScreen)
      printToScreen(results);
    return results;
  }

  public SSDBRelation[] getBestBestNeighborsByGene(String genes_id) {
    return getBestNeighborsByGene(genes_id, 1, 50);
  }

  public SSDBRelation[] getBestBestNeighborsByGene(String genes_id, int offset,
      int limit) {
    SSDBRelation[] results = new SSDBRelation[0];
    try {
      results = serv.get_best_best_neighbors_by_gene(genes_id, offset, limit);
    } catch (RemoteException e) {
      e.printStackTrace();
    }

    if (printEachOutputToScreen)
      printToScreen(results);
    return results;
  }

  private static void printToScreen(SSDBRelation[] results) {
    if (results == null) {
      System.out.println("NULL result.");
      return;
    }
    System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXX");
    System.out.println(+results.length + " results by genes:");
    for (int i = 0; i < results.length; i++) {
      String gene1 = results[i].getGenes_id1();
      String gene2 = results[i].getGenes_id2();
      int score = results[i].getSw_score();
      System.out.println(gene1 + " -> " + gene2 + "\t SWscore:" + score);
    }
    System.out.println("=====================\n" + results.length
        + " results by definition:");
    for (int i = 0; i < results.length; i++) {
      String gene1 = results[i].getDefinition1();
      String gene2 = results[i].getDefinition2();
      float score = results[i].getBit_score();
      System.out.println(gene1 + " -> " + gene2 + "\t BitScore:" + score);
    }
    System.out.println("=====================\n" + results.length
        + " results by other attributes:");
    for (int i = 0; i < results.length; i++) {
      System.out.print("Start " + i + ": " + results[i].getStart_position1()
          + " -> " + results[i].getStart_position2());
      System.out.print(" \t|End " + i + ": " + results[i].getEnd_position1()
          + " -> " + results[i].getEnd_position2());
      System.out.print(" \t|Length " + i + ": " + results[i].getLength1()
          + " -> " + results[i].getLength2());
      System.out.println(" \t|Overlap+Identity " + i + ": "
          + results[i].getOverlap() + " --- " + results[i].getIdentity());
    }
    System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXX");
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

  public static String extractInfo(String completeString, String startsWith,
      String endsWith) {
    int pos = completeString.toLowerCase().indexOf(
        "\n" + startsWith.toLowerCase() + 1); // Prefer hits starting in a new
                                              // line. // +1 because of \n
    if (pos < 0) {
      pos = completeString.toLowerCase().indexOf(startsWith.toLowerCase());
      // Pruefen ob zeichen ausser \t und " " zwischen \n und pos. wenn ja =>
      // abort. (Beispiel: "  AUTHOR XYZ" m�glich.)
      if (pos < 0)
        return null;
      int lPos = completeString.lastIndexOf("\n", pos);
      String toCheck = completeString.substring(Math.max(lPos, 0), pos);
      if (!toCheck.replace(" ", "").replace("\t", "").replace("\n", "")
          .isEmpty())
        return null;
    }

    String ret = "";
    if (endsWith == null || endsWith.isEmpty()) {
      int st = completeString.indexOf(" ", pos + startsWith.length());
      if (st < 0)
        st = pos + startsWith.length(); // +1 wegen "\n"+sw
      int nl = completeString.indexOf("\n", pos + startsWith.length());
      if (nl < 0)
        nl = completeString.length();

      try {
        ret = completeString.substring(st, nl).trim();
      } catch (Exception e) {
        System.out.println("St: " + st + " \t" + pos + " "
            + startsWith.length());
        System.out.println("Nl: " + nl + " \t" + completeString.length());
        System.out.println(startsWith);
        System.out.println("--------------\n" + completeString);
        e.printStackTrace();
      }
      while (completeString.length() > (nl + 1)) {
        if (completeString.charAt(nl + 1) == ' ') {
          int nl2 = completeString.indexOf("\n", nl + 1);
          if (nl2 < 0)
            nl2 = completeString.length();
          ret += "\n" + completeString.substring(nl + 1, nl2).trim();
          nl = nl2;
        } else
          break;
      }
    } else {
      // Jump to first non-Whitespace Character. Mind the new lines!
      int sPos = pos+startsWith.length();
      while (Character.isWhitespace(completeString.charAt(sPos)) && completeString.charAt(sPos)!='\n') sPos++;
      
      // Search for end position and trim string.
      int pos2 = completeString.toLowerCase().indexOf(endsWith,sPos);
      if (pos2<=0) return "";
      ret = completeString.substring(sPos, pos2).trim();
    }
    
    return ret;

  }

  private static KEGGPortType getServlet() {
    KEGGLocator locator = new KEGGLocator();
    KEGGPortType serv = null;
    try {
      serv = locator.getKEGGPort();
    } catch (Exception e) {
      System.err.println("Unable to initilize Kegg Servlet.");
      e.printStackTrace();
    }
    return serv;
  }

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

  private static void printToScreen(Definition[] results) {
    if (results == null) {
      System.out.println("NULL result.");
      return;
    }
    System.out.println(results.length + " definition results :");
    for (int i = 0; i < results.length; i++) {
      System.out.println(results[i].getEntry_id() + " => "
          + results[i].getDefinition());
    }
  }

  /**
   * @param gene
   *          symbol, species e.g. 'hsa'
   * @return kegg identifiers in an arrayList e.g. "hsa:7529"
   */
  public ArrayList<String> getKEGGIdentifierForAGeneSymbol(String gene,
      String species) {
    ArrayList<String> identifiers = new ArrayList<String>();
    String s = "";

    try {
      String gg = "genes " + gene;
      s = serv.bfind(gg);
      BufferedReader br = new BufferedReader(new StringReader(s));
      String line;
      while ((line = br.readLine()) != null) {
        if (line.startsWith(species.toLowerCase())) {
          identifiers.add(line.substring(0, line.indexOf(" ")));
        }
      }
    } catch (RemoteException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return identifiers;
  }

}
