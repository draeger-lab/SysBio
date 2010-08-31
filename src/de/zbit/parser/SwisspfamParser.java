/**
 * 
 */
package de.zbit.parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.zbit.io.OpenFile;

/**
 * @author Finja Buechel: finja.buechel@uni-tuebingen.de
 * 
 * 
 * Der Parser erstellt aus der ursprünglichen Swisspfam Datei, eine Datei in folgendem Format:
 * 
 * Protein (uniprot id) \t Domäne (Pfam id) \t startpos im Prot \t endpos im Prot \n
 * Beispiel:
 * 006L_IIV6  PF12299 231 324
 * 006L_IIV6 PF04383 19  126
 * 007R_IIV3 PF10927 181 430
 * 029R_IIV6 PF12299 1 100
 */
public class SwisspfamParser {

  /**
   * @param args
   */
  public static void main(String[] args) {
    try {
      SwisspfamParser sp = new SwisspfamParser();
      sp.parseFile(args[0], args[1]);
    } catch (Exception e) {
      System.err.print("No file entered.");
      e.printStackTrace();
    }
  }

  /**
   * 
   * @param in Eingabedatei von Swisspfam
   * @param out die Datei die der Parser rausschreibt
   */
  private void parseFile(String in, String out) {
    String line = "", protein = "", ac = "", pfamID = "";
    try {
      BufferedReader br = OpenFile.openFile(in);
      BufferedWriter bw = new BufferedWriter(new FileWriter(out));
      int start = -1, end = -1;

      line = br.readLine();
      
      while ((line = br.readLine()) != null) {
        if(!line.trim().isEmpty()){
          if (line.substring(0, 1).equals(">")) {
            // get uniprot id
            start = 0;
            end = line.indexOf(' ', start + 1);
            if (end > 0)
              protein = line.substring(1, end);
            else
              protein = "";

            // get accession id
            start = line.indexOf('|', 0);
            start = line.indexOf('|', start + 1);
            end = line.indexOf('.', start + 1);

            if (start > 0 && end > 0)
              ac = line.substring(start + 2, end);
            else
              ac = "";
          }
          else{
            start = line.indexOf(')', 1);
            end = line.indexOf('.', start+2); // PF id
            if (end < 0)
              end = line.indexOf(' ', start+2); // PB id
//            if((start+2 - end) < 0)
//              end = line.indexOf(' ', end); // PB id

            if (start > 0 && end > 0)
              pfamID = line.substring(start + 2, end);
            else
              pfamID = "";

            while(line.indexOf("  ", end+1) > 0){
              end = line.indexOf("  ", end+1);
            }
            
            List<int[]> number = parseNumbers(end, line);
            for (int[] is : number) {
              String help =protein + "\t" + pfamID + "\t" + is[0] + "\t" + is[1] + "\n"; 
              bw.write(help);
            }
          }
        }
      }

      br.close();
      bw.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    System.out.println("Parsing ready");
  }

  private List<int[]> parseNumbers(int start, String line) {
    List<int[]> list = new ArrayList<int[]>();
    boolean finish = false;

    int domStart = 0, domEnd = 0, end = 0;

    // getting the first numbers
    while (end == 0) {
      start = line.indexOf("  ", start);
      end = line.indexOf('-', start);
      try {
        domStart = Integer.parseInt(line.substring(start, end).trim());
      } catch (Exception e) {
        continue;
      }
    }

    start = end + 1;
    end = line.indexOf(' ', start);
    if (end < 0) {
      end = line.length();
      finish = true;
    }

    domEnd = Integer.parseInt(line.substring(start, end).trim());
    int[] i = new int[] { domStart, domEnd };
    list.add(i);

    while (!finish) {
      start = end;
      end = line.indexOf('-', start);
      domStart = Integer.parseInt(line.substring(start, end).trim());

      start = end + 1;
      end = line.indexOf(' ', start);
      if (end < 0) {
        end = line.length();
        finish = true;
      }

      domEnd = Integer.parseInt(line.substring(start, end).trim());
      i = new int[] { domStart, domEnd };
      list.add(i);
    }
    
    return list;
  }
}
