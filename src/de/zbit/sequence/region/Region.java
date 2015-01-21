/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2015 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.sequence.region;

/**
 * Interface for genome regions.
 * 
 * Please see {@link AbstractRegion} for various very helpful tools
 * and an abstract implementation.
 * 
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public interface Region extends Chromosome {
  
  /**
   * Any default number to use for "unknown"
   */
  public final static int DEFAULT_START = -1;
  
  /**
   * Probe start is added to {@link NameAndSignals#addData(String, Object)} with this key.
   */
  public final static String startKey = "Probe_position_start";
  
  /**
   * Probe end is added to {@link NameAndSignals#addData(String, Object)} with this key.
   */
  public final static String endKey = "Probe_position_end";
  
  
  
  /**
   * @return the start coordinate (or {@link #DEFAULT_START} if none).
   */
  public int getStart();
  
  /**
   * Set the (probe) starting position.
   */
  public void setStart(int start);
  
  /**
   * @return the end coordinate (or {@link #DEFAULT_START} if none).
   */
  public int getEnd();
  
  /**
   * Set the (probe) starting position.
   * @throws Exception in some implementations, region length is
   * limited to a certain number of base pairs and end may not be
   * set prior to start. In these cases, an exception is thrown.
   * You might want to use {@link AbstractRegion#createRegion(String, int, int)} to avoid this issue.
   */
  public void setEnd(int end) throws Exception;
  
  /**
   * Performs the simple calculation:
   * <pre>return start+((end-start)/2);</pre>
   * @return middle position
   */
  public int getMiddle();
  
  /**
   * Used to determine if two regions overlap each other.
   * You may use the following code:
   * <pre>
   * int start = getStart(); int end = getEnd();
   * int start2 = other.getStart(); int end2 = other.getEnd();
   * return  (getChromosomeAsByteRepresentation()==other.getChromosomeAsByteRepresentation()) &&
   *    ((start2 >= start && start2 <= end) || (start >= start2 && start <= end2));
   * </pre>
   * @param other
   * @return {@code true} if this region intersects {@code other}.
   */
  public boolean intersects(Region other);
  
  
}
