package de.zbit.util;

/**
 * General class for progress bars.
 * This class does the management and computations. The visualization
 * should be done in implementing classes.
 * 
 * The principle is, setting a number of "calls" and calling this class
 * exactly that often. Example:
 * setNumberOfTotalCalls(5)
 * for (int i=0; i<5; i++) {
 *   DisplayBar();
 * }
 * 
 * @author wrzodek 
 */
public abstract class aProgressBar {
  
  /**
   * Set these values.
   */
  private int totalCalls=0;
  private boolean estimateTime=false;
  
  /** 
   * Internal variables (not to set by user).
   */
  private int callNr=0;
  
  /** 
   * for time duration estimations
   */
  private long measureTime = 0;
  private int numMeasurements = 0;
  private long lastCallTime=System.currentTimeMillis();
  
  
  
  public void setNumberOfTotalCalls(int totalCalls) {
    this.totalCalls = totalCalls;
  }
  
  public void setEstimateTime(boolean estimateTime) {
    this.estimateTime = estimateTime;
    if (estimateTime) lastCallTime = System.currentTimeMillis();
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
  public int getCallNumber() {
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
    int perc = Math.min((int)((double)callNr/(double)totalCalls*100), 100);
    
    // Calculate time remaining
    double miliSecsRemaining = -1;
    if (estimateTime && !omitTimeCount) {
      // Increment
      measureTime += System.currentTimeMillis() - lastCallTime;
      numMeasurements++;
      
      // Calculate
      double ScansRemaining = (totalCalls - (callNr+1)); // /(double)MLIBSVMSettings.runs;
      miliSecsRemaining = ScansRemaining * ((measureTime/(double)numMeasurements)) ;
      
      // Reset (intended not to reset if omitTimeCount)
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
  
}
