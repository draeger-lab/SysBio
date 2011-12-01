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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.zbit.util.Utils;

/**
 * A basic implementation of the {@link Region} interface.
 * 
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class BasicRegion implements Region, Serializable, Cloneable, Comparable<Region> {
  private static final long serialVersionUID = 833008166151851420L;
  
  /**
   * Chromosome as byte encoding (see 
   * {@link ChromosomeTools#getChromosomeByteRepresentation(String)})
   */
  byte chr;
  /**
   * Start position 
   */
  int start;
  /**
   * End position
   */
  int end;
  
  /**
   * 
   * @param chromosome
   * @param start
   * @param end
   */
  public BasicRegion(String chromosome, int start, int end) {
    this (ChromosomeTools.getChromosomeByteRepresentation(chromosome), start, end);
  }
  
  /**
   * @param chr as given by {@link ChromosomeTools#getChromosomeByteRepresentation(String)}
   * @param start
   * @param end
   */
  public BasicRegion(byte chr, int start, int end) {
    setStart(Math.min(start, end));
    setEnd(Math.max(start, end));
    setChromosome(chr);
  }
  
  /**
   * Copy constructor
   * @param r
   */
  public BasicRegion(Region r) {
    setStart(r.getStart());
    setEnd(r.getEnd());
    setChromosome(r.getChromosomeAsByteRepresentation());
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#clone()
   */
  @Override
  protected BasicRegion clone() {
    return new BasicRegion(this);
  }

  /* (non-Javadoc)
   * @see de.zbit.data.Chromosome#setChromosome(java.lang.String)
   */
  @Override
  public void setChromosome(String chromosome) {
    setChromosome(ChromosomeTools.getChromosomeByteRepresentation(chromosome));
  }

  /* (non-Javadoc)
   * @see de.zbit.data.Chromosome#setChromosome(byte)
   */
  @Override
  public void setChromosome(byte chromosome) {
    this.chr = chromosome;
  }

  /* (non-Javadoc)
   * @see de.zbit.data.Chromosome#getChromosome()
   */
  @Override
  public String getChromosome() {
    return ChromosomeTools.getChromosomeStringRepresentation(chr);
  }

  /* (non-Javadoc)
   * @see de.zbit.data.Chromosome#getChromosomeAsByteRepresentation()
   */
  @Override
  public byte getChromosomeAsByteRepresentation() {
    return chr;
  }

  /* (non-Javadoc)
   * @see de.zbit.data.Region#getStart()
   */
  @Override
  public int getStart() {
    return start;
  }

  /* (non-Javadoc)
   * @see de.zbit.data.Region#setStart(int)
   */
  @Override
  public void setStart(int probeStart) {
    start = probeStart;
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

  /* (non-Javadoc)
   * @see de.zbit.data.Region#intersects(de.zbit.data.Region)
   */
  @Override
  public boolean intersects(Region other) {
    int start = getStart(); int end = getEnd();
    int start2 = other.getStart(); int end2 = other.getEnd();
    return  (getChromosomeAsByteRepresentation()==other.getChromosomeAsByteRepresentation()) &&
        ((start2 >= start && start2 <= end) || (start >= start2 && start <= end2));
  }

  /* (non-Javadoc)
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(Region o) {
    int r = Utils.compareIntegers((int)getChromosomeAsByteRepresentation(), (int)o.getChromosomeAsByteRepresentation());
    if (r==0) {
      r = Utils.compareIntegers(getStart(), o.getStart());
      if (r==0) {
        r = Utils.compareIntegers(getEnd(), o.getEnd());
      }
    }
    
    return r;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    if (isSetEnd() && isSetStart()) {
      // both
      return String.format("%s:%s-%s", getChromosome(), getStart(),getEnd());
    } else if (isSetStart()) {
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
    return end>-1 && end!=DEFAULT_START;
  }

  /**
   * @return
   */
  public boolean isSetEnd() {
    return end>-1 && end!=DEFAULT_START;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return super.hashCode() + chr + start + end;
  }
  
  /**
   * Get all regions from <code>all</code>, overlapping with <code>intersectWith</code>. 
   * @param all
   * @param intersectWith
   * @return Intersecting regions.
   */
  public static <T extends Region> List<T> getAllIntersections(final Iterable<T> all, Region intersectWith) {
    List<T> ret = new ArrayList<T>();
    if (all==null) return ret;
    
    Iterator<T> l = all.iterator();
    while (l.hasNext()) {
      T ns = l.next();
      if (ns.intersects(intersectWith)) {
        ret.add(ns);
      }
    }
    return ret;
  }
}
