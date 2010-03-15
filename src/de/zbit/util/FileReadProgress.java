package de.zbit.util;

import java.io.File;
import java.io.IOException;

/**
 * <p>When reading in files and processing each line (files line by line),
 * this class is handy to output the percentage of how much is done.</p>
 * 
 * <p>NOTE: Theoretically, this class can be used for displaying more types of
 * progress than just reading in files.</p>
 * 
 * @author Clemens Wrzodek
 * @author Florian Mittag
 */
public class FileReadProgress {

  public static final String anim= "|/-\\";

  
  private long fileLength=0;
  private long bytesRead=0;

  private boolean outputPercentage=true;
  private boolean printProgressInSameLine = true;

  private int lastOutputtedPercentage = -1;
  private int progressCounter = 0;
  
  /**
   * Creates a new FileReadProgress object with the file size retrieved from
   * the given file. Additionally, it has to be specified if the progress should
   * be written in the same line (default) or a new line for each percentage.
   * 
   * @param filepath  the path of the file
   * @param printProgessInSameLine if <code>true</code>, the output will always be in the same line
   */
  public FileReadProgress(String filepath, boolean printProgessInSameLine) {
    this(filepath);
    this.printProgressInSameLine = printProgessInSameLine;
  }
  
  /**
   * Creates a new FileReadPrograss object for the given file.
   * 
   * @param filepath the file to be read from
   */
  public FileReadProgress(String filepath) {
    this(new File(filepath));
  }
  
  /**
   * Creates a new FileReadPrograss object for the given file.
   * 
   * @param file the file to be read from
   */
  public FileReadProgress(File file) {
    this(file.length());
  }
  
  /**
   * Creates a new FileReadProgress object with the given length to be 100%.
   * 
   * @param length the length of the file
   */
  public FileReadProgress(long length) {
    fileLength = length;
  }

  /**
   * Returns if the current percentage should be put out every time it is updated.
   * 
   * @return <code>true</code>, if the current percentage should be put out
   *         every time it is updated
   */
  public boolean getOutputPercentage() {
    return outputPercentage;
  }
  
  /**
   * Specify whether the current percentage should be put out every time it is
   * updated. <code>true</code> means, the percentage will be put out every time
   * it is updated, <code>false</code> means, no output will be automatically
   * generated, but {@link DiplayBar} has to be called explicitly.
   * 
   * @param outputPercentage
   */
  public void setOutputPercentage(boolean outputPercentage) {
    this.outputPercentage = outputPercentage;
  }
  /**
   * Tries to establish a "command line" progress bar. Does not work correctly
   * if output goes to a file.
   */
  public void setPrintProgessInSameLine(boolean b) {
    printProgressInSameLine = b;
  }
  
  /**
   * Resets all counters back to zero.
   */
  public void reset() {
    lastOutputtedPercentage = -1;
    bytesRead=0;
    progressCounter = 0;
  }
  
  /**
   * Reports that the given line has been read from the file. This implicitly
   * adds 1 to the length of the given string to account for newline characters.
   * 
   * @param curLine
   */
  public void progress(String curLine) {
    if (curLine != null) {
      progress(curLine.length() + 1); // +1 for \n. For Windows possibly +2...
    } else {
      progress(0);
    }
  }
  
  public void progress(long bytesRead) {
    this.bytesRead += bytesRead;
    progressCounter++;
    
    // if percentage output should be generated
    if (outputPercentage) {
      int perc = getPercentage();   // get the current percentage
      if ( printProgressInSameLine ) {
        try {
          System.out.write(getDisplayBarString(progressCounter, perc).getBytes());
        } catch (IOException e) {
          e.printStackTrace();
        }
      } else {
        if (perc != lastOutputtedPercentage) {
          System.out.println(perc + "%");
          lastOutputtedPercentage = perc;
        }
      }
    }
  }
  
  protected boolean canPrintInSameLine() {
    return printProgressInSameLine
           && (System.console() != null)
           && (System.console().writer() != null);
  }
  
  public int getPercentage() {
    return (int)Math.round((double)bytesRead/fileLength*100.0);
  }
  
  
  public static String getDisplayBarString(int counter, int percent) {
    // when not in normal console, do not try to use carriage return
    if (System.console()==null || System.console().writer()==null) {
      if( percent % 10 == 0 ) {
        return percent+"%";
      } else {
        return ".";
      }
    }
    
    StringBuilder sb = new StringBuilder();
    
    int x = percent / 2;
    sb.append("\r[");
    for (int k = 0; k < 50; k++)
      sb.append(((x <= k) ? " " : "="));
    sb.append("] " + anim.charAt(counter % anim.length())  + " " + percent + "%");
    
    return sb.toString();    
  }  
}
