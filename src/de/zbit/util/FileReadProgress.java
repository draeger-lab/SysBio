/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2011 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.util;

import java.io.BufferedReader;
import java.io.File;
import java.util.logging.Logger;

import de.zbit.io.OpenFile;

/**
 * <p>When reading in files and processing each line (files line by line),
 * this class is handy to output the percentage of how much is done.</p>
 * 
 * <p>NOTE: Theoretically, this class can be used for displaying more types of
 * progress than just reading in files.</p>
 * 
 * @author Clemens Wrzodek
 * @author Florian Mittag
 * @version $Rev$
 * @since 1.0
 */
public class FileReadProgress {
  public static final transient Logger log = Logger.getLogger(FileReadProgress.class.getName());

  /**
   * Total file length
   */
  private long fileLength=0;
  /**
   * Bytes that have currently been read from the file
   */
  private long bytesRead=0;
  /**
   * If true, output should only take one line
   */
  private boolean printProgressInSameLine = false;
  
  /**
   * true if the file has \r\n line breaks. False, if the
   * input file has \n line breaks.
   * @see #fileUsesWindowsLinebreaks(String)
   */
  private boolean fileUsesWindowsLinebreaks=false;
  
  /**
   * This enables a custom progress bar to use.
   * For example, you may set this to a swing progress bar implementation.
   */
  private AbstractProgressBar progressBar;
  
  /**
   * Creates a new FileReadPrograss object for the given file.
   * 
   * @param file the file to be read from
   */
  public FileReadProgress(File file) {
    this(file.length());
    fileUsesWindowsLinebreaks = fileUsesWindowsLinebreaks(file.getPath());
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
    super();
    // This is called from every other constructor.
    setFileLength(length);
    if (progressBar==null) setProgressBar(new ProgressBar(length));
  }
  
  /**
   * Creates a new FileReadPrograss object for the given file.
   * 
   * @param filepath the file to be read from
   */
  public FileReadProgress(String filepath) {
    this (getFileLength(filepath));
    fileUsesWindowsLinebreaks = fileUsesWindowsLinebreaks(filepath);
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
    if (a==null) return;
    progressBar = a;
    configureProgressBar(progressBar);
  }

  /**
   * Configures the current progress bar. This includes
   * setting the total number of bytes to read, current
   * status, setting estimate time to true and setting
   * the {@link #printProgressInSameLine} flag.
   */
  private void configureProgressBar(AbstractProgressBar progressBar) {
    if (progressBar!=null) {
      progressBar.setNumberOfTotalCalls(fileLength);
      progressBar.setCallNr(bytesRead);
      progressBar.setEstimateTime(true);
      if (progressBar instanceof ProgressBar) {
        ((ProgressBar) progressBar).setPrintInOneLine(printProgressInSameLine);
      }
    }
  }
  
  /**
   * Does override the currently set total file length.
   * Does NOT reset the current status (bytes already read).
   */
  public void setFileLength(long l) {
    if (l<0) {
      // Finer, because message is issued for many compressed archives, e.g. tar files.
      log.finer(String.format("Negative file length of %s for %s.", l, getClass().getSimpleName()));
      l=0;
    }
    this.fileLength = l;
    if (progressBar!=null) {
      progressBar.setNumberOfTotalCalls(l);
    }
  }
  
  /**
   * @return total length of the file
   */
  public long getFileLength() {
    return this.fileLength;
  }
  
  /**
   * Set the file length. Automatically detected uncompressed
   * file sizes for ZIP or GZIP files and sets the respective
   * value.
   * @param inFileName
   */
  public void setFileLength(String inFileName) {
    setFileLength(getFileLength(inFileName));
  }

  /**
   * Returns the uncompressed (for zip, gzip) or raw length
   * of the file, denoted by the given inFileName. 
   * @param inFileName
   * @return
   */
  private static long getFileLength(String inFileName) {
    return OpenFile.getFileSize(inFileName);
  }
  
  /**
   * Returns the current percentage ({@link #bytesRead} of {@link #fileLength}).
   */
  public int getPercentage() {
    return Math.min((int)(((double)bytesRead/(double)fileLength)*100.0), 100);
  }
  
  /**
   * This should be called every time, bytes have been read from the input stream
   * @param bytesRead number of bytes that have been read
   */
  public void progress(long bytesRead) {
    this.bytesRead += bytesRead;
    
    // if percentage output should be generated
    progressBar.setCallNr(this.bytesRead-1);
    progressBar.DisplayBar(); // Increases counter by 1.
  }
  
  /**
   * Reports that the given line has been read from the file. This implicitly
   * adds 1 or 2 to the length of the given string to account for newline characters.
   * 
   * @param curLine
   */
  public void progress(String curLine) {
    if (curLine != null) {
      // +1 for \n. For Windows possibly +2...
      progress(curLine.length() + (fileUsesWindowsLinebreaks?2:1) );
    } else {
      progress(0);
    }
  }
  
  /**
   * Because using the file length is only an approximation, calling this method
   * after the whole file has been read may be a good idea.
   * It will display the 100% mark and set all variables to "file fully read".
   */
  public void finished() {
    long missing = (fileLength-bytesRead);
    if (missing>0) progress(missing);
    progressBar.finished();
  }
  
  /**
   * Resets all counters back to zero.
   */
  public void reset() {
    bytesRead=0;
    progressBar.reset();
  }
  
  /**
   * If true, the whole progress bar will be painted in one line.
   */
  public void setPrintProgessInSameLine(boolean b) {
    printProgressInSameLine = b;
    if (progressBar instanceof ProgressBar) {
      ((ProgressBar) progressBar).setPrintInOneLine(printProgressInSameLine);
    }
  }
  
  /**
   * Determines if a file uses windows (\r\n) or Unix (\n) line brakes.
   * @return true for windows, false for Unix line brakes.
   */
  private static boolean fileUsesWindowsLinebreaks(String file) {
    // OpenFile.openFile may be replaced by BufferedReader(new FileReader(file))
    try {
      // Read line with predefined method
      boolean oldVerbose = OpenFile.verbose;
      OpenFile.verbose = false;
      BufferedReader inStream = OpenFile.openFile(file);
      OpenFile.verbose = oldVerbose;
      if (inStream==null || !inStream.ready()) return false;
      String line = inStream.readLine();
      inStream.close();
      if (line==null) return false;
      
      // Custom read line
      inStream = OpenFile.openFile(file);
      char[] buff = new char[2];
      inStream.skip(line.length());
      inStream.read(buff);
      inStream.close();
      if (buff[buff.length-1]=='\n' && buff[buff.length-2]=='\r') return true;
      else return false;
    
    } catch (Throwable t) {return false;}
  }
  
}
