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

import java.io.Serializable;

/**
 * A basic implementation of the {@link Region} interface, specially for Regions
 * smaller or equal than 127bps in length.
 * 
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class SimpleVeryShortRegion extends AbstractRegion implements Region, Serializable, Cloneable, Comparable<Region> {
  private static final long serialVersionUID = -6799511792201071522L;
  
  /**
   * End position
   */
  private byte length;
  
  
  /**
   * 
   * @param chromosome
   * @param start
   * @param end
   * @throws Exception 
   * @see {@link AbstractRegion#createRegion(String, int, int)}
   */
  public SimpleVeryShortRegion(String chromosome, int start, int end) throws Exception {
    this (ChromosomeTools.getChromosomeByteRepresentation(chromosome), start, end);
  }
  
  /**
   * @param chr as given by {@link ChromosomeTools#getChromosomeByteRepresentation(String)}
   * @param start
   * @param end
   * @throws Exception 
   * @see {@link AbstractRegion#createRegion(byte, int, int)}
   */
  public SimpleVeryShortRegion(byte chr, int start, int end) throws Exception {
    super(chr, Math.min(start, end), Math.max(start, end));
  }
  
  /**
   * Copy constructor
   * @param r
   * @throws Exception 
   * @see {@link AbstractRegion#createRegion(Region)}
   */
  public SimpleVeryShortRegion(Region r) throws Exception {
    super(r);
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#clone()
   */
  @Override
  protected SimpleVeryShortRegion clone() {
    try {
      return new SimpleVeryShortRegion(this);
    } catch (Exception e) {
      // Impossible because length is same as in this class
      // so it must be valid.
      return null;
    }
  }
  
  
  /* (non-Javadoc)
   * @see de.zbit.data.Region#getEnd()
   */
  @Override
  public int getEnd() {
    return getStart() + length;
  }

  /* (non-Javadoc)
   * @see de.zbit.data.Region#setEnd(int)
   */
  @Override
  public void setEnd(int end) throws Exception {
    if (!isSetStart()) throw new Exception("Can not set end position prior to start position.");
    int diff = (end-getStart());
    if (diff<0 || diff>Byte.MAX_VALUE) {
      throw new Exception("Invalid or too long region.");
    }
    this.length = (byte)diff;
  }
  
}
