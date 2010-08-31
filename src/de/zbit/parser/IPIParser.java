/**
 * 
 */
package de.zbit.parser;

import java.io.File;

import org.apache.log4j.Level;

import de.zbit.dbfetch.IPIFetcher;
import de.zbit.util.LogUtil;

/**
 * @author Finja Buechel: finja.buechel@uni-tuebingen.de
 * 
 */
public class IPIParser {

  IPIFetcher IPIManagement = null;

  public IPIParser() {
    try {
      if (new File("ipi.dat").exists())
        IPIManagement = (IPIFetcher) IPIFetcher.loadFromFilesystem("ipi.dat");
    } catch (Throwable e) {
    }
    if (IPIManagement == null)
      IPIManagement = new IPIFetcher(80000);
  }
  


  /**
   * @param ids
   * @return
   */
  private String[][] getUniProtACs(String[] ids) {
    String[][] acs = new String[ids.length][2];
    String[] results = IPIManagement.getInformations(ids);
    
    for( int i = 0; i < results.length; i++) {
      if(results[i]!=null){    
        String[] splitLines = results[i].split("\n");
        for(int j = 0; j<splitLines.length; j++){
          String line = splitLines[j];

          if(line.startsWith("DR   UniProtKB/Swiss-Prot")){
            acs[i][0] = ids[i];
            String[] lineEntries = line.split(";");

            acs[i][1] = lineEntries[1];
          }
        } 
      }
      else{
        acs[i][0] = ids[i];
        acs[i][1] = "null";
      }
    }

    return acs;
  }
  
  /**
   * @param args
   */
  public static void main(String[] args) {
    LogUtil.initializeLogging(Level.INFO);
    
    IPIParser ipiParser = new IPIParser();

    String[] ids = {"IPI00003348","IPI00003865","IPI00021435","IPI00026833","IPI00105598","IPI00114375",
                    "IPI00116074","IPI00116283.1","IPI00117264","IPI00122565","IPI00126072","IPI00128023",
                    "IPI00131695","IPI00132042","IPI00133903","IPI00221402","IPI00223875","IPI00331436",
                    "IPI00381412","IPI00407692","IPI00420349","IPI00420385","IPI00454142","IPI00462072",
                    "IPI00467833"};

    String[][] results = ipiParser.getUniProtACs(ids);
    for (int i = 0; i<results.length; i++) {
      System.out.println(results[i][0] + "\t" + results[i][1]);
    }

}


}
