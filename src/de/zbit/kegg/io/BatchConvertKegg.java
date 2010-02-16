package de.zbit.kegg.io;

import java.io.File;
import java.util.ArrayList;

import de.zbit.kegg.parser.KeggParser;
import de.zbit.kegg.parser.pathway.Pathway;
import de.zbit.util.DirectoryParser;



public class BatchConvertKegg {
  private static String changeOutdirTo = "";
  private static String orgOutdir = "";
  
  /**
   * @param args
   */
  public static void main(String[] args) {
    if (args!=null && args.length>0) {
      orgOutdir = args[0];
      if (args.length>1) changeOutdirTo = args[1];
      parseDirAndSubDir(orgOutdir);
      return;
    }
    orgOutdir = "C:\\Dokumente und Einstellungen\\wrzodek\\Desktop\\KEGG\\KEGG Daten\\kgml";
    changeOutdirTo =  "C:\\Dokumente und Einstellungen\\wrzodek\\Desktop\\KEGG\\KEGG Daten\\kgml\\gml";
    parseDirAndSubDir(orgOutdir);
  }
  
  private static void parseDirAndSubDir(String dir) {
    if (!dir.endsWith("/") && !dir.endsWith("\\"))
      if (dir.contains("\\")) dir+="\\"; else dir +="/";
    System.out.println("Parsing directory " + dir);
    
    DirectoryParser dp = new DirectoryParser(dir);
    while (dp.hasNext()) {
      String fn = dp.next();
      
      //if (fn.equals("gml")|| fn.equals("metabolic")) continue;
      
      if (new File(dir+fn).isDirectory()) {
        parseDirAndSubDir(dir + fn);
      } else if (fn.toLowerCase().trim().endsWith(".xml")) {
        // Test if outFile already exists. Assumes: 1 Pathway per file. (should be true for all files... not crucial if assumption is wrong)
        String myDir = getAndCreateOutDir(dir);
        String outFileTemp = myDir + fn.trim().substring(0, fn.trim().length()-4) + ".graphML";
        if (new File(outFileTemp).exists()) continue; // Skip already converted files.

        // Parse and convert all Pathways in XML file.
        ArrayList<Pathway> pw=null;
        try {
          pw = de.zbit.kegg.parser.KeggParser.parse(dir+fn);
        } catch (Throwable t) {t.printStackTrace();} // Show must go on...
        if (pw==null || pw.size()<1) continue;
        
        boolean appendNumber=(pw.size()>1);
        for (int i=0; i<pw.size(); i++) {
          String outFile = myDir + fn.trim().substring(0, fn.trim().length()-4) + (appendNumber?"-"+(i+1):"") + ".graphML";
          if (new File(outFile).exists()) continue; // Skip already converted files.
          
          // XXX: Main Part
          KEGG2GraphML.KEGG2GraphML(pw.get(i), outFile);
          
          if (KEGG2GraphML.lastFileWasOverwritten) { // Datei war oben noch nicht da, sp�ter aber schon => ein anderer prezess macht das selbe bereits.
            System.out.println("It looks like another instance is processing the same files. Going to next subfolder.");
            return;
          }
        }
        
        
      }
    }
  }

  private static String getAndCreateOutDir(String dir) {
    String myDir = dir;
    if (changeOutdirTo!=null && !changeOutdirTo.isEmpty()) {
      myDir = changeOutdirTo + myDir.substring(orgOutdir.length());
      try {
        new File(myDir).mkdirs();
      } catch (Exception e) {} // Gibts schon...
    }
    return myDir;
  }
  
}
