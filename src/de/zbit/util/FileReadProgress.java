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

  /**
   * 
   */
  public static final String anim= "|/-\\";
  /**
   * 
   */
  private long fileLength=0;
  /**
   * 
   */
  private long bytesRead=0;
  /**
   * 
   */
  private boolean outputPercentage=true;
  /**
   * 
   */
  private boolean printProgressInSameLine = false;
  /**
   * 
   */
  private int lastOutputtedPercentage = -1;
  /**
   * 
   */
  private int progressCounter = 0;
  /**
   * 
   */
  private boolean isWindows;
  
  /**
   * 
   */
  protected boolean useSimpleStyle = useSimpleStyle();
  
  /**
   * This enables a custom progress bar to use.
   * For example, you may set this to a swint progress bar implementation.
   */
  private AbstractProgressBar progressBar;
  
  /**
   * Creates a new FileReadPrograss object for the given file.
   * 
   * @param file the file to be read from
   */
  public FileReadProgress(File file) {
    this(file.length());
  }

  /**
   * Creates a new FileReadPrograss object for the given file.
   * 
   * @param file the file to be read from
   * @param printProgressInSameLine if <code>true</code>, the output will always be in the same line
   */
  public FileReadProgress(File file, boolean printProgressInSameLine) {
    this(file);
    this.printProgressInSameLine = printProgressInSameLine;
  }

  /**
   * Creates a new FileReadProgress object with the given length to be 100%.
   * 
   * @param length the length of the file
   */
  public FileReadProgress(long length) {
    fileLength = length;
    isWindows = (System.getProperty("os.name").toLowerCase().contains("windows"))?true:false;
    if (progressBar==null) setProgressBar(new ProgressBar(length));
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
   * Creates a new FileReadProgress object with the file size retrieved from
   * the given file. Additionally, you may specify a custom progress bar.
   * @param filepath
   * @param progressBar
   */
  public FileReadProgress(String filepath, AbstractProgressBar progressBar) {
    this(filepath);
    setProgressBar(progressBar);
  }

  /**
   * Creates a new FileReadProgress object with the file size retrieved from
   * the given file. Additionally, it has to be specified if the progress should
   * be written in the same line (default) or a new line for each percentage.
   * 
   * @param filepath  the path of the file
   * @param printProgessInSameLine if <code>true</code>, the output will always be in the same line
   */
  public FileReadProgress(String filepath, boolean printProgressInSameLine) {
    this(filepath);
    this.printProgressInSameLine = printProgressInSameLine;
  }
  
  /**
   * Enables to set a custom progress bar. E.g. for GUI integration.
   * @param a
   */
  public void setProgressBar(AbstractProgressBar a) {
    progressBar = a;
    progressBar.setNumberOfTotalCalls(fileLength);
    progressBar.setCallNr(bytesRead);
    progressBar.setEstimateTime(true);
  }
  
  /**
   * Does override the currently set total file length.
   * Does NOT reset the current status (bytes already read).
   */
  public void setFileLength(long l) {
    this.fileLength = l;
  }
  
  /**
   * 
   * @param counter
   * @param percent
   * @return
   */
  public String getDisplayBarString(int counter, int percent) {
    // when not in normal console, do not try to use carriage return
    if (useSimpleStyle) {
      if( lastOutputtedPercentage == percent ) {
        return "";
      } else if( percent % 10 == 0 ) {
        return percent+"%" + (percent==100?"\n":"");
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
   * 
   */
  public int getPercentage() {
    return (int)Math.round((double)bytesRead/fileLength*100.0);
  }
  
  /**
   * 
   * @param bytesRead
   */
  public void progress(long bytesRead) {
    this.bytesRead += bytesRead;
    progressCounter++;
    
    // if percentage output should be generated
    if (outputPercentage) {
      int perc = getPercentage();   // get the current percentage
      if ( printProgressInSameLine && (useSimpleStyle||progressBar==null)) {
        // If NOT useSimpleStyle, then the progress is written in one line anyways.
        try {
          System.out.write(getDisplayBarString(progressCounter, perc).getBytes());
          lastOutputtedPercentage = perc;
        } catch (IOException e) {
          e.printStackTrace();
        }
      } else {
        if (perc != lastOutputtedPercentage) {
          if (progressBar!=null) {
            progressBar.setCallNr(this.bytesRead-1);
            progressBar.DisplayBar(); // Increases counter by 1.
          } else {
            System.out.println(perc + "%");
          }
          lastOutputtedPercentage = perc;
        }
      }
    }
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
  
  /*
   * Kommentiert. Methode macht kein Sinn! Man kann immer (ob console oder nicht) in selbe Zeile Printen!
  protected boolean canPrintInSameLine() {
    return printProgressInSameLine
           && (System.console() != null)
           && (System.console().writer() != null);
  }
  */
  
  /**
   * Resets all counters back to zero.
   */
  public void reset() {
    lastOutputtedPercentage = -1;
    bytesRead=0;
    progressCounter = 0;
    progressBar.reset();
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
   * Determins if ANSI compliance console commands can be used, based on java version, os type and outputStream Type.
   * @return
   */
  protected boolean useSimpleStyle() {
    boolean useSimpleStyle = false;
    if (isWindows) {
      useSimpleStyle = true; // MS Windows has (by default) no ANSI capabilities.
      return useSimpleStyle;
    }
    
    // is TTY Check is only available for java 1.6. So a wrapper to determine java version is needed for Java 1.5 compatibility.
    String v = System.getProperty("java.version");
    if (v!=null && v.length()>2) {
      try {
        double d = Double.parseDouble(v.substring(0, 3));
        if (d<1.6) useSimpleStyle = true;
        else useSimpleStyle = !isTTY_Java16only.isTty();
      } catch (Throwable e) {
        useSimpleStyle = true;
      }
    }
    
    return useSimpleStyle;
  }
  
}
