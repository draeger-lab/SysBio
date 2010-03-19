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
    return (getAndReset(false));
  }
  
  /**
   * Returns the (mili-)seconds since the last Reset/Initialization.
   * Resets the timer.
   * @param milis - if true, time will be returned in miliseconds. Otherwise in seconds.
   * @return ms or s since last reset/initialization.
   */
  public long getAndReset(boolean milis) {
    long ret = (System.currentTimeMillis() - start);
    if (!milis) ret/=1000;
    reset();
    return ret;
  }
  
}
