/**
 * 
 */
package de.zbit.parser;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.DecimalFormat;

import de.zbit.io.CSVReader;
import de.zbit.util.SortedArrayList;
import de.zbit.util.StringPair;

/**
 * 
 * This class reads a UniDomInt.tsv file and the reference file containing iPfam and 3did interactions.
 * Then it recalculates the prediction scores
 * 
 * The format of the file looks like this:
 *             <Column 1>   Interacting Pfam-A Domain One
 *             <Column 2>   Interacting Pfam-A Domain Two
 *             <Column 3>   Interaction present in ME
 *             <Column 4>   Interaction present in HIMAP
 *             <Column 5>   Interaction present in RCDP
 *             <Column 6>   Interaction present in DIMA
 *             <Column 7>   Interaction present in P-value
 *             <Column 8>   Interaction present in DPEA
 *             <Column 9>   Interaction present in RDFF
 *             <Column 10>  Interaction present in Interdom
 *             <Column 11>  Interaction present in LP
 *             <Column 12>  Number Networks the interaction was present in
 *             <Column 13>  Interactions Domains present in ME's domain space
 *             <Column 14>  Interactions Domains present in HIMAP's domain space
 *             <Column 15>  Interactions Domains present in RCDP's domain space
 *             <Column 16>  Interactions Domains present in DIMA's domain space
 *             <Column 17>  Interactions Domains present in P-value's domain space
 *             <Column 18>  Interactions Domains present in DPEA's domain space
 *             <Column 19>  Interactions Domains present in RDFF's domain space
 *             <Column 20>  Interactions Domains present in Interdom's domain space
 *             <Column 21>  Interactions Domains present in LP's domain space
 *             <Column 22>  Number of Networks domain spaces the interacting domains was present in
 *             <Column 23>  Interaction present in Ipfam or 3DID (1 = Yes, 0 = No)
 *             <Column 24>  Reliability score for the interaction
 *             
 * @author Finja B&uml;chel
 */


public class UniDomIntParser {

  private static SortedArrayList<String> D_ref;
  private static SortedArrayList<StringPair> I_ref;
  private static SortedArrayList<StringPair> I_me;
  private static SortedArrayList<StringPair> I_himap;
  private static SortedArrayList<StringPair> I_rcdp;
  private static SortedArrayList<StringPair> I_dima;
  private static SortedArrayList<StringPair> I_pValue;
  private static SortedArrayList<StringPair> I_dpea;
  private static SortedArrayList<StringPair> I_rdff;
  private static SortedArrayList<StringPair> I_inter;
  private static SortedArrayList<StringPair> I_lp;

  double w_me;
  double w_himap;
  double w_rcdp;
  double w_dima;
  double w_pValue;
  double w_dpea;
  double w_rdff;
  double w_inter;
  double w_lp;

  double sum_ofAllw; 


  public UniDomIntParser(String referenceFile, String interactionFile){
    init(referenceFile, interactionFile);
  }


  /**
   * @param referenceFile 
   * @param interactionFile = uniDomIntFile
   */
  private void init(String referenceFile, String interactionFile) {
    readAndSetReferenceNetwork(referenceFile);

    SortedArrayList[] me = readNetworkFile    (interactionFile, "me",     2, 12);
    I_me = me[1];
    SortedArrayList[] himap = readNetworkFile (interactionFile, "himap",  3, 13);
    I_himap = himap[1];
    SortedArrayList[] rcdp = readNetworkFile  (interactionFile, "rcdp",   4, 14);
    I_rcdp = rcdp[1];
    SortedArrayList[] dima = readNetworkFile  (interactionFile, "dima",   5, 15);
    I_dima = dima[1];
    SortedArrayList[] pValue = readNetworkFile(interactionFile, "pValue", 6, 16);
    I_pValue = pValue[1];
    SortedArrayList[] dpea = readNetworkFile  (interactionFile, "dpea",   7, 17);
    I_dpea = dpea[1];
    SortedArrayList[] rdff = readNetworkFile  (interactionFile, "rdff",   8, 18);
    I_rdff = rdff[1];
    SortedArrayList[] inter = readNetworkFile (interactionFile, "inter",  9, 19);
    I_inter = inter[1];
    SortedArrayList[] lp = readNetworkFile    (interactionFile, "lp",    10, 20);
    I_lp = lp[1];

    w_me     = calculateW(me, "ME");
    w_himap  = calculateW(himap, "HIMAP");
    w_rcdp   = calculateW(rcdp, "RCDP");
    w_dima   = calculateW(dima, "DIMA");
    w_pValue = calculateW(pValue, "P-value");
    w_dpea   = calculateW(dpea, "DPEA");
    w_rdff   = calculateW(rdff, "RDFF");
    w_inter  = calculateW(inter, "Interdom");
    w_lp     = calculateW(lp, "LP");

    sum_ofAllw = w_me + w_himap  + w_rcdp + w_dima + w_pValue + w_dpea + w_rdff + w_inter + w_lp;
  }

  /**
   * The class reads the file and saves the domains and interactions in an SortedArray
   * @param fileName of the reference file containing domain interactions of iPfam and 3did
   */

  private void readAndSetReferenceNetwork(String fileName) {
    CSVReader reader = new CSVReader(fileName);
    String[][] data = reader.getData();
    D_ref = new SortedArrayList<String>();
    I_ref = new SortedArrayList<StringPair>();

    //    BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
    for(int i=1;i<data.length; i++){
      if(!D_ref.contains(data[i][0]))
        D_ref.add(data[i][0]);
      if(!D_ref.contains(data[i][1]))
        D_ref.add(data[i][1]);
      StringPair sb = new StringPair(data[i][0], data[i][1]);
      if(!I_ref.contains(sb)){
        I_ref.add(sb);
      }
    }

    //    System.out.println("D_ref:\t" + D_ref.size() + "\tI_ref:\t" + I_ref.size());
  }

  /**
   * 
   * @param fileName of the unidomint file
   * @param networkname
   * @param pos1, position where to find the interaction
   * @param pos2, position where to find if both domains occur in the network
   * @return new SortedArrayList[]{D_ref, I_ref};
   */
  private SortedArrayList[] readNetworkFile(String fileName, String networkname, int pos1, int pos2) {
    CSVReader reader = new CSVReader(fileName);
    String[][] data = reader.getData();
    SortedArrayList<String> D = new SortedArrayList<String>();
    SortedArrayList<StringPair> I = new SortedArrayList<StringPair>();

    SortedArrayList<StringPair> temp = new SortedArrayList<StringPair>();

    for(int i=0;i<data.length; i++){
      StringPair sb = new StringPair(data[i][0], data[i][1]);
      if(!temp.contains(sb))
        temp.add(sb);
      else
        System.out.println("Double occurrence of domain pair " + data[i][0] + " " + data[i][1]);


      if(data[i][pos2].equals("1")){
        if(!D.contains(data[i][0]))
          D.add(data[i][0]);

        if(!D.contains(data[i][1]))
          D.add(data[i][1]);


        if(data[i][pos1].equals("1")){
          if(!I.contains(sb)){
            I.add(sb);
          }
          else{
            System.out.println("Double occurrence of interaction " + data[i][0] + " " + data[i][1]);
          }
        }
      }
    }

    return new SortedArrayList[]{D, I};
  }

  /** @param ref SortedArrayList[]{D, I}
   * @param me SortedArrayList[]{D, I}
   * @return
   */
  private double calculateW(SortedArrayList[] me, String networkName) {
    SortedArrayList<String> D_me  = me[0];
    SortedArrayList<StringPair> I_me  = me[1];

    SortedArrayList<String>   D_ref_D_me = new SortedArrayList<String>();
    SortedArrayList<StringPair> I_ref_me = new SortedArrayList<StringPair>();
    SortedArrayList<StringPair> I_me_ref = new SortedArrayList<StringPair>();
    SortedArrayList<StringPair> intersection_ref_me = new SortedArrayList<StringPair>();

    // create intersection of domain spaces
    for (String domain : D_me) {
      if(D_ref.contains(domain) && !D_ref_D_me.contains(domain))
        D_ref_D_me.add(domain);
    }

    // create potentially shared interactions
    for (int i = 0; i < D_ref_D_me.size(); i++) {
      for (int j = 0; j < D_ref_D_me.size(); j++) {
        StringPair sb = new StringPair(D_ref_D_me.get(i), D_ref_D_me.get(j));

        if(I_ref.contains(sb)){
          if (!I_ref_me.contains(sb))
            I_ref_me.add(sb);
        }
        if(I_me.contains(sb)){
          if (!I_me_ref.contains(sb))
            I_me_ref.add(sb);
        }
      }
    }

    // create intersection of interaction spaces
    for (StringPair sb : I_ref) {
      if(I_me.contains(sb) && !intersection_ref_me.contains(sb))
        intersection_ref_me.add(sb);
    }

    // calculation of the weighted overlap wo
    double numerator   = (double)(2*(intersection_ref_me.size())); 
    double denominator = (double)(I_ref_me.size() + I_me_ref.size());
    double wo = numerator/denominator;

    double precision = (double)((double)intersection_ref_me.size()/(double)I_me.size());

    DecimalFormat df = new DecimalFormat("0.00");
    //    System.out.println(df.format(1.1257444d));

    System.out.println( networkName + "\n" +
              "I_"+networkName+":"                + "\t" + I_me.size() + "\n" +  
              "D_"+networkName+":"                + "\t" + D_me.size() + "\n" + 
              "D_ref_"+networkName+":"            + "\t" + D_ref_D_me.size() + "\n" +
              "I_intersection:"                   + "\t" + intersection_ref_me.size() + "\n" + 
              "I_ref_"+networkName+":"            + "\t" + I_ref_me.size() + "\n" + 
              "I_"+networkName+"_ref:"            + "\t" + I_me_ref.size() + "\n" + 
              "w_"+networkName+":"                + "\t" + df.format(wo*100) + "\n" +
              "precision:"                        + "\t" + df.format(precision*100) + "\n");

    return wo;
  }

  /**
   * @param domain1
   * @param domain2
   */
  private double getreliabilityScore(String domain1, String domain2) {
    StringPair sb = new StringPair(domain1, domain2);
    double sum = 0.0;
    if(I_me.contains(sb)){
      sum+= w_me;
    }
    if(I_himap.contains(sb)){
      sum+= w_himap;
    }
    if(I_rcdp.contains(sb)){
      sum+= w_rcdp;
    }
    if(I_dima.contains(sb)){
      sum+= w_dima;
    }
    if(I_pValue.contains(sb)){
      sum+= w_pValue;
    }
    if(I_dpea.contains(sb)){
      sum+= w_dpea;
    }
    if(I_rdff.contains(sb)){
      sum+= w_rdff;
    }
    if(I_inter.contains(sb)){
      sum+= w_inter;
    }
    if(I_lp.contains(sb)){
      sum+= w_lp;
    }
    
    return sum/sum_ofAllw;
  }


  /**
   * @param interactionFile
   * @param outputFileName, will look like
   * domain1 \t domain2 \t iPfam3did \t ME \t HIMAP \t RCDP \t DIMA \t pValue \t DPEA \t RDFF \t Inter \t LP \t predictionScore
   */
  public void writeUniDomIntFileWithNewPredictionScores(String domainFileName, String outputFileName) {
    CSVReader reader = new CSVReader(domainFileName);
    String[][] data = reader.getData();
    try{
      BufferedWriter bw = new BufferedWriter(new FileWriter(outputFileName));
      for(int i=0;i<data.length; i++){
        String domain1 = data[i][0];
        String domain2 = data[i][1];

        bw.append(domain1 + "\t" + domain2 + "\t");
        bw.append(data[i][22] + "\t");
        bw.append(data[i][2] + "\t");
        bw.append(data[i][3] + "\t");
        bw.append(data[i][4] + "\t");
        bw.append(data[i][5] + "\t");
        bw.append(data[i][6] + "\t");
        bw.append(data[i][7] + "\t");
        bw.append(data[i][8] + "\t");
        bw.append(data[i][9] + "\t");
        bw.append(data[i][10] + "\t");
        bw.append(String.valueOf(getreliabilityScore(domain1, domain2)));
        
        bw.append("\n");
      }
      bw.close();
    }
    catch(Exception e){
      System.err.print("Error while writing file: " + outputFileName);
      e.printStackTrace();
    }
  }


}
