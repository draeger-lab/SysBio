package de.zbit.util;

import java.io.File;
import java.io.IOException;

/**
 * When reading in files and processing each line (files line by line),
 * this class is handy to give a % how much is done.
 * @author wrzodek
 */
public class FileReadProgress {
  private long FileLength=0;
  private long BytesRead=0;
  private boolean outputPercentage=true;
  private int lastOutputtedPercentage = -1;
  private boolean printProgressInSameLine = true;
  
  public FileReadProgress(String filepath, boolean printProgessInSameLine) {
    this(filepath);
    this.printProgressInSameLine = printProgessInSameLine;
  }
  public FileReadProgress(String filepath) {
    this(new File(filepath));
  }
  public FileReadProgress(File file) {
    this(file.length());
  }
  public FileReadProgress(long length) {
    FileLength = length;
  }

  public boolean getOutputPercentage() {
    return outputPercentage;
  }
  public void setOutputPercentage(boolean outputPercentage) {
    this.outputPercentage = outputPercentage;
  }
  /**
   * Tries to establish a "command line" progress bar. Does not work correctly if output goes to a file.
   */
  public void setPrintProgessInSameLine(boolean b) {
    printProgressInSameLine = b;
  }
  
  public void reset() {
    lastOutputtedPercentage = -1;
    BytesRead=0;
  }
  
  public void progress(String curLine) {
    progress(curLine.length()+1); // +1 fuer \n. Unter windows eigentlich sogar +2...
  }
  public void progress(long bytesRead) {
    BytesRead+=bytesRead;
    if (outputPercentage) {
      int perc = getPercentage();
      if (perc!=lastOutputtedPercentage) {
        if (!printProgressInSameLine)
          System.out.println(perc + "%");
        else
          try {
            System.out.write(DisplayBar(perc).getBytes());
            if (perc>=100 || System.console()==null) System.out.println();
          } catch (IOException e) {e.printStackTrace();}
        lastOutputtedPercentage = perc;
      }
    }
  }
  
  public int getPercentage() {
    return (int)Math.round((double)BytesRead/FileLength*100.0);
  }
  
  private static String DisplayBar(int perc) {
    // Wenn nicht in normaler Konsole laeuft, nur % angabe.
    if (System.console()==null || System.console().writer()==null) {
      return perc+"%";
    }
    
    String anim= "|/-\\";
    StringBuilder sb = new StringBuilder();
    
    int x = perc / 2;
    sb.append("\r[");
    for (int k = 0; k < 50; k++)
      sb.append(((x <= k) ? " " : "="));
    sb.append("] " + anim.charAt(perc % anim.length())  + " " + perc + "%");
    
    return sb.toString();
  }
  
}
