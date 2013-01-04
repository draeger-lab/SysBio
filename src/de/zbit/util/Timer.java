/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2013 by the University of Tuebingen, Germany.
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

/**
 * Useful for timing certain procedures.
 * @author Clemens Wrzodek
 * @version $Rev$
 * @since 1.0
 */
public class Timer {
  /**
   * 
   */
  long start = -1;
  
  /**
   * Initilalizes the timer and sets the start to the current time.
   */
  public Timer() {
    reset();
  }
  
  /**
   * Returns the seconds since the last Reset/Initialization.
   * Resets the timer.
   * @return s since last reset/initialization.
   */
  public long getAndReset() {
    long ret = System.currentTimeMillis() - start;
    reset();
    return ret;
  }

  /**
   * Returns the (mili-)seconds since the last Reset/Initialization.
   * Resets the timer.
   * @param milis if true, time will be returned in miliseconds. Otherwise in seconds.
   * @return ms or s since last reset/initialization.
   */
  public double getAndReset(boolean milis) {
  	double ret = getAndReset();
  	if (!milis) {
    	ret /= 1000d;
    }
  	return ret;
  }
  
  /**
   * Returns the (mili-)seconds since the last Reset/Initialization.
   * Does NOT reset the timer.
   * @param milis if true, time will be returned in miliseconds. Otherwise in seconds.
   * @return ms or s since last reset/initialization.
   */
  public long get(boolean milis) {
    long ret = System.currentTimeMillis() - start;
    if (!milis) {
    	ret /= 1000;
    }
    return ret;
  }
  
  /**
   * 
   */
  public void reset() {
    start = System.currentTimeMillis();
  }

  /**
   * Returns a nice string representation of the passed time.
   * @return
   * @see Utils#getPrettyTimeString(long)
   */
  public String getNiceAndReset() {
    long milis = getAndReset();
    return Utils.getPrettyTimeString(milis);
  }
  
}
