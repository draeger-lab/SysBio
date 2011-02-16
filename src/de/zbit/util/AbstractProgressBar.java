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

import java.io.Serializable;

/**
 * <p>
 * General class for progress bars.
 * This class does the management and computations. The visualization
 * should be done in implementing classes.
 * </p>
 * 
 * <p>
 * The principle is, setting a number of "calls" and calling this class
 * exactly that often.
 * 
 * <br /><b>Example:</b></p>
 * <pre>
 * setNumberOfTotalCalls(5)
 * for (int i=0; i<5; i++) {
 *   DisplayBar();
 * }
 * </pre>
 * 
 * @author wrzodek 
 * @version $Rev$
 */
public abstract class AbstractProgressBar implements Serializable {
  private static final long serialVersionUID = 6447054832080673569L;
  
  /*
   * Set these values.
   */
  private long totalCalls=0;
  private boolean estimateTime=false;
  
  /*
   * Internal variables (not to set by user).
   */
  private long callNr=0;
  
  /*
   * for time duration estimations
   */
  private long measureTime = 0;
  private int numMeasurements = 0;
  private long lastCallTime=0;//System.currentTimeMillis();
  private boolean callNumbersInSyncWithTimeMeasurements=true;
  
  public void reset() {
    callNr=0;
    measureTime = 0;
    numMeasurements = 0;
    lastCallTime=0;//System.currentTimeMillis();
    callNumbersInSyncWithTimeMeasurements=true;
  }
  
  public void setNumberOfTotalCalls(long totalCalls) {
    this.totalCalls = totalCalls;
    reset(); // Reset when changing number of total calls.
  }
  
  public long getNumberOfTotalCalls() {
    return this.totalCalls;
  }
  
  public void setEstimateTime(boolean estimateTime) {
    this.estimateTime = estimateTime;
    if (estimateTime) lastCallTime = 0;//System.currentTimeMillis();
  }
  
  /**
   * @return Should the class calculate the remaining time?
   */
  public boolean getEstimateTime() {
    return estimateTime;
  }
  
  
  /**
   * @return How often the DisplayBar method has been called.
   */
  public long getCallNumber() {
    return callNr;
  }
  
  /**
   * Call this function, to set the counter one step further to totalCalls.
   * Paints automatically the progress bar.
   */
  public synchronized void DisplayBar() {
    DisplayBar(null);
  }
  
  /**
   * If using the time estimate counter, this function will reset the last
   * call time (start time) to now.
   */
  public synchronized void resetLastCallTime() {
    lastCallTime = System.currentTimeMillis();
  }
  
  /**
   * If using the time estimate counter, this function will return the
   * System.currentTimeMillis() time of the last DisplayBar call.
   * @return 
   */
  public long getLastCallTime() {
    return lastCallTime;
  }
  
  /**
   * Only use this if you know exactly what you're doing!
   * Adds time to measureTime, but does NOT increas
   * numMeasurements.
   * @param timespan
   */
  public synchronized void addTime(long timespan) {
    measureTime += timespan;
  }
  
  /**
   * Call this function, to set the counter one step further to totalCalls.
   * Paints automatically the progress bar and adds an @param additionalText.
   * 
   * This function should be called exactly as often as defined in the constructor. 
   * It will draw or update a previously drawn progressBar.
   * @param additionalText - Any additional text (e.g. "Best item found so far XYZ")
   */
  public synchronized void DisplayBar(String additionalText) {
    DisplayBar(additionalText, false);
  }
  /**
   * See {@link #DisplayBar(String)}.
   * @param omitTimeCount - If true, increases call number, but does not include this call
   * into the ETA estimation. Does also NOT reset the timer, if true.
   */
  public synchronized void DisplayBar(String additionalText, boolean omitTimeCount) {
    callNr++;
    
    // Calculate percentage
    int perc = Math.min((int)(((double)callNr/(double)totalCalls)*100.0), 100);
    
    // Calculate time remaining
    double miliSecsRemaining = -1;
    if (estimateTime && !omitTimeCount) {
      // Increment
      if (lastCallTime>0) {
        measureTime += System.currentTimeMillis() - lastCallTime;
        numMeasurements++;
      }
      
      // Calculate
      if (numMeasurements>0) {
        double ScansRemaining = (totalCalls - (callNr+1)); // /(double)MLIBSVMSettings.runs;
        if (callNumbersInSyncWithTimeMeasurements) {
          miliSecsRemaining = ScansRemaining * ((measureTime/(double)numMeasurements)) ;
        } else {
          miliSecsRemaining = ScansRemaining * ((measureTime/(double)callNr)) ;
        }
      }
      
      // Reset (intended not to reset if omitTimeCount!)
      lastCallTime = System.currentTimeMillis();
    }
    
    // Draw the bar
    drawProgressBar(perc, miliSecsRemaining, additionalText);
    
    // Eventually, call the finishing method.
    if (callNr == totalCalls) finished();
  }
  
  /**
   * This method is called automatically, when all calculations are finished (callNr=TotalCalls).
   * Please implement it SYNCHRONIZED. Only call it manually, when you finish before reaching 100%.
   */
  public abstract void finished();
  
  /**
   * Implement this function. Avoid calling it manually.
   * Please, set it to SYNCHRONIZED.
   * @param percent - The percentage of the bar.
   * @param miliSecondsRemaining - If available, miliseconds remaining until 100%. If NOT available, -1.
   * @param additionalText - If available, additional text to display. , If NOT available, null.
   */
  protected abstract void drawProgressBar(int percent, double miliSecondsRemaining, String additionalText);

  /**
   * @param callNr
   */
  public void setCallNr(long callNr) {
    // Remember this change when estimating the eta.
    callNumbersInSyncWithTimeMeasurements=false;
    
    this.callNr = callNr;
  }
  
}
