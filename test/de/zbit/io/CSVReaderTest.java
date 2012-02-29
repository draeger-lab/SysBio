package de.zbit.io;

import java.io.IOException;
import java.util.Arrays;

public class CSVReaderTest {

  /**
   * @param args
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {
    if (args != null && args.length == 1) {
      CSVReader a = new CSVReader(args[0]);
      a.setDisplayProgress(false); // Optional, set to true, if not sysouting

      String[] line;
      while ((line = a.getNextLine()) != null) {
        System.out.println(Arrays.toString(line));
      }

      a.getHeader(); // The header (if available)
      a.getPreamble(); // Everything, before actual table start

    } else {
      System.out.println("It is necessary to enter a csv file!");
    }

  }

}
