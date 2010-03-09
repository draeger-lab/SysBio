package de.zbit.util;

/**
 * Useful for timing certain procedures.
 * @author wrzodek
 */
public class Timer {
  long start=-1;
  
  /**
   * Initilalizes the timer and sets the start to the current time.
   */
  public Timer() {
    reset();
  }
  
  public void reset() {
    start = System.currentTimeMillis();
  }

  /**
   * Returns the seconds since the last Reset/Initialization.
   * Resets the timer.
   * @return s since last reset/initialization.
   */
  public long getAndReset() {
    long ret = (System.currentTimeMillis() - start)/1000;
    reset();
    return ret;
  }
  
  
  /*
   * 
System.out.println("PC" + (System.currentTimeMillis()-lStart)/1000);

   */
  
}
