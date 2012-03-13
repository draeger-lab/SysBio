/**
 *
 * @author Clemens Wrzodek
 */
package de.zbit.io;

import java.io.IOException;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.UIManager;

import de.zbit.gui.csv.CSVReaderOptionPanel;
import de.zbit.io.csv.CSVReader;

/**
 * @author Clemens Wrzodek
 *
 */
public class CSVReaderDemonstration {
  
  /**
   * @param args
   */
  public static void main(String[] args) {
    
    // Test 1:
    CSVReader r = new CSVReader("files/sample.csv.txt");
    demoOutput(r);
    //showOptionsPanel(r);
    //if (true) return;
    
    r = new CSVReader("files/small2.txt");
    demoOutput(r);
    
    r = new CSVReader("files/small3.txt");
    demoOutput(r);
    
    r = new CSVReader("files/test.txt");
    demoOutput(r);
    
    r = new CSVReader("files/pair.gz");
    demoOutput(r);
    
    r = new CSVReader("files/sample.csv.txt");
    demoOutput(r);
    
  }
  
  public static void demoOutput(CSVReader r) {
    System.out.println("=============================\n" + r.getFilename());
    System.out.println("Headers: " + r.getContainsHeaders());
    System.out.println("NumCols: " + r.getNumberOfColumns());
    System.out.println("ContentStart: " + r.getContentStartLine());
    
    String sep = Character.toString(r.getSeparatorChar());
    if (r.getSeparatorChar()=='\u0001') sep = "[AnyWhitespaceChar]";
    System.out.println("Separator: '" + sep + "'");
    System.out.println("TreatMultiAsOne: " + r.getTreatMultipleConsecutiveSeparatorsAsOne());
    
    try {
      int locationCol = r.getColumnByMatchingContent("^chr.{1,2}:\\d+-\\d+", Pattern.CASE_INSENSITIVE,100);
      System.out.println("Location col: " + locationCol);
    } catch (IOException e1) {
      e1.printStackTrace();
    }
    
    // Headers
    System.out.println("Headers: " + r.getContainsHeaders());
    if (r.getContainsHeaders()) {
      System.out.println("\nHeaders:");
      for (String s : r.getHeader())
        System.out.print(s + " | ");
      System.out.println();
    }
    
    // First 5 data lines
    System.out.println("\nData:");
    String[] line;
    int i=0;
    try {
      while (((line=r.getNextLine())!=null) && i<5) {
        
        for (String s : line)
          System.out.print(s + " | ");
        System.out.println();
        i++;
      }
    } catch (IOException e) {e.printStackTrace();}

  }
  
  public static void showOptionsPanel(CSVReader r) {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      JFrame parent = new JFrame();
      parent.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      
      CSVReader ret = CSVReaderOptionPanel.showDialog(parent,r, "CSV Options");
      
      System.exit(0);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
}
