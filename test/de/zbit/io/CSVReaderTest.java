package de.zbit.io;

import java.io.IOException;
import java.util.Arrays;

public class CSVReaderTest {

  /**
   * @param args
   * @throws IOException 
   */
  public static void main(String[] args) throws IOException {
    CSVReader a = new CSVReader("C:/Dokumente und Einstellungen/buechel/Eigene Dateien/Downloads/Mapping250K_Nsp.na31.annot.csv");
    a.setDisplayProgress(false); // Optional, set to true, if not sysouting

    String[] line;
    while ((line = a.getNextLine())!=null) {
       System.out.println(Arrays.toString(line));
    }
    
    a.getHeader(); // The header (if available)
    a.getPreamble(); // Everything, before actual table start
  }

}
