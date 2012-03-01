/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2012 by the University of Tuebingen, Germany.
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
 * A basic implementation of the {@link Region} interface.
 * 
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class SimpleRegion extends AbstractRegion implements Region, Serializable, Cloneable, Comparable<Region> {
  private static final long serialVersionUID = 833008166151851420L;
  
  /**
   * End position
   */
  private int end;
  
  
  /**
   * @param chromosome
   * @param start
   * @param end
   * @throws Exception see {@link #setEnd(int)}
   * @see {@link AbstractRegion#createRegion(String, int, int)}
   */
  public SimpleRegion(String chromosome, int start, int end) throws Exception {
    this (ChromosomeTools.getChromosomeByteRepresentation(chromosome), start, end);
  }
  
  /**
   * @param chr as given by {@link ChromosomeTools#getChromosomeByteRepresentation(String)}
   * @param start
   * @param end
   * @throws Exception see {@link #setEnd(int)}
   * @see {@link AbstractRegion#createRegion(byte, int, int)}
   */
  public SimpleRegion(byte chr, int start, int end) throws Exception {
    super(chr, Math.min(start, end), Math.max(start, end));
  }
  
  /**
   * Copy constructor
   * @param r
   * @throws Exception see {@link #setEnd(int)}
   * @see {@link AbstractRegion#createRegion(Region)}
   */
  public SimpleRegion(Region r) throws Exception {
    super(r);
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#clone()
   */
  @Override
  protected SimpleRegion clone() {
    try {
      return new SimpleRegion(this);
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
    return end;
  }

  /* (non-Javadoc)
   * @see de.zbit.data.Region#setEnd(int)
   */
  @Override
  public void setEnd(int end) {
    this.end = end;
  }
  
  
  /**
   * Will create a {@link SimpleRegion}.
   * This class does NOT throw exceptions, so you must use the correct inputs
   * @param chr
   * @param start must be &lt; <code>end</code>
   * @param end must be &gt; <code>start</code>
   * @return
   */
  public static SimpleRegion createRegion(byte chr, int start, int end) {
    try {
      return new SimpleRegion(chr, start, end);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
  
}
