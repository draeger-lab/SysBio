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
package de.zbit.sequence.region;

import java.io.Serializable;

/**
 * A simple implementation of {@link Region} that only takes one position (only
 * a starting point), to mark a chromosomal point.
 * 
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class ChromosomalPoint implements Region, Serializable, Cloneable, Comparable<Region> {
  private static final long serialVersionUID = -1622563599507652752L;
  
  /**
   * Chromosome as byte encoding (see 
   * {@link ChromosomeTools#getChromosomeByteRepresentation(String)})
   */
  private byte chr;
  /**
   * Start position 
   */
  private int start;
  
  /**
   * 
   * @param chromosome
   * @param start
   * @see {@link AbstractRegion#createRegion(String, int, int)}
   */
  public ChromosomalPoint(String chromosome, int start) {
    this (ChromosomeTools.getChromosomeByteRepresentation(chromosome), start);
  }
  
  /**
   * @param chr as given by {@link ChromosomeTools#getChromosomeByteRepresentation(String)}
   * @param start
   * @see {@link AbstractRegion#createRegion(byte, int, int)}
   */
  public ChromosomalPoint(byte chr, int start) {
    setStart(start);
    setChromosome(chr);
  }
  
  /**
   * Copy constructor
   * @param r
   * @see {@link AbstractRegion#createRegion(Region)}
   */
  public ChromosomalPoint(Region r) {
    setStart(r.getStart());
    setChromosome(r.getChromosomeAsByteRepresentation());
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#clone()
   */
  @Override
  protected ChromosomalPoint clone() {
    return new ChromosomalPoint(this);
  }

  /* (non-Javadoc)
   * @see de.zbit.data.Chromosome#setChromosome(java.lang.String)
   */
  public void setChromosome(String chromosome) {
    setChromosome(ChromosomeTools.getChromosomeByteRepresentation(chromosome));
  }

  /* (non-Javadoc)
   * @see de.zbit.data.Chromosome#setChromosome(byte)
   */
  public void setChromosome(byte chromosome) {
    this.chr = chromosome;
  }

  /* (non-Javadoc)
   * @see de.zbit.data.Chromosome#getChromosome()
   */
  public String getChromosome() {
    return ChromosomeTools.getChromosomeStringRepresentation(chr);
  }
  
  /* (non-Javadoc)
   * @see de.zbit.sequence.region.Region#getMiddle()
   */
  public int getMiddle() {
    return start;
  }

  /* (non-Javadoc)
   * @see de.zbit.data.Chromosome#getChromosomeAsByteRepresentation()
   */
  public byte getChromosomeAsByteRepresentation() {
    return chr;
  }

  /* (non-Javadoc)
   * @see de.zbit.data.Region#getStart()
   */
  public int getStart() {
    return start;
  }

  /* (non-Javadoc)
   * @see de.zbit.data.Region#setStart(int)
   */
  public void setStart(int probeStart) {
    start = probeStart;
  }

  /* (non-Javadoc)
   * @see de.zbit.data.Region#getEnd()
   */
  public int getEnd() {
    return DEFAULT_START;
  }

  /* (non-Javadoc)
   * @see de.zbit.data.Region#setEnd(int)
   */
  public void setEnd(int end) throws Exception {
    throw new Exception("Can not set an end position for a point.");
  }
  
  /* (non-Javadoc)
   * @see de.zbit.data.Region#intersects(de.zbit.data.Region)
   */
  public boolean intersects(Region other) {
    int start = getStart(); int end = getEnd();
    int start2 = other.getStart(); int end2 = other.getEnd();
    return  (getChromosomeAsByteRepresentation()==other.getChromosomeAsByteRepresentation()) &&
        ((start2 >= start && start2 <= end) || (start >= start2 && start <= end2));
  }

  /* (non-Javadoc)
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(Region o) {
    return SimpleRegion.getComparator().compare(this, o);
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    if (isSetStart()) {
      // start only
      return String.format("%s:%s", getChromosome(), getStart());
    } else {
      // chromosome only
      return getChromosome();
    }
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Region) {
      // Also convers other classes, implementing this interface.
      // This is intended in this case!
      return compareTo((Region) obj)==0;
    } else {
      return false;
    }
  }

  /**
   * @return
   */
  public boolean isSetStart() {
    return start>-1 && start!=DEFAULT_START;
  }

  /**
   * @return
   */
  public boolean isSetEnd() {
    return false;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return super.hashCode() + chr * 11+ start * 31;
  }

}
