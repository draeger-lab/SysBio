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
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.RandomAccess;

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
   * @see de.zbit.sequence.region.Region#getMiddle()
   */
  @Override
  public int getMiddle() {
    return start+((end-start)/2);
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
   * This is for unsorted lists, please use {@link #getAllIntersections(List, Region, boolean)}
   * for sorted lists!
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
  

  /**
   * Searches for intersecting {@link Region}s in a sorted list and returns the intersecting region.
   * Additionaly, it is possible to get the closest region, if no intersecting region could
   * be found.
   * <p>The list must be sorted into ascending order according primary by {@link #getChromosome()}
   * and secondary by {@link #getStart()} prior to making this call.
   * If it is not sorted, the results are undefined.</p>
   * @param <T> Actual implementing class
   * @param <K> May be T, may also be any {@link BasicRegion}
   * @param allRegionsSorted sorted list of all regions
   * @param searchFor {@link Region} to search for
   * @param getClosestIfIntersectionIsEmpty get the single, closest region if no intersecting
   * region could be found.
   * @return 
   * @return List of all intersecting {@link Region}s, or single closest.
   */
  public static <T extends Region & Comparable<? super Region>>  List<T> getAllIntersections(
    List<T> allRegionsSorted, Region searchFor, boolean getClosestIfIntersectionIsEmpty) {
    List<T> ret = new ArrayList<T>();
    if (allRegionsSorted==null || allRegionsSorted.size()<1) return ret;
    
    // Track closest region to searchFor middle position
    int middle = searchFor.getStart()+(searchFor.getEnd()-searchFor.getStart())/2;
    T closest = null;
    int minDistance = Integer.MAX_VALUE;
    
    // Get intersecting cgi
    int pos = Collections.binarySearch(allRegionsSorted, searchFor);
    if (pos<0) { // pos is (-(insertion point) - 1). 
      pos = -(pos+1);
    }
    
    /*
     * Since region is sorted primary by chromosome and secondary by start,
     * we now have to check each end point of all positions on same chromosome
     * with a lower or equal starting point. consider the following
     * 1 -------   <- does intersect
     * 2  -        <- does not intersect
     * 3   --      <- searchFor
     * => Get boundaries for all regions on same chromosome with lower starting
     * positions
     */
    int size = allRegionsSorted.size();
    int lPos = pos, uPos = pos;
    while ((--lPos>0) && allRegionsSorted.get(lPos).getChromosome()==searchFor.getChromosome());
    while ((++uPos<size) && (allRegionsSorted.get(uPos).getChromosome()==searchFor.getChromosome()
        && allRegionsSorted.get(uPos).getStart()<=searchFor.getStart()));
    
    // Now, there is now other way than checking each Region from
    // lPos+1 to uPos-1 if their end is >= searchFors start position+1
    T nextRegionStartingBehindSearchFor=null;
    if (allRegionsSorted instanceof RandomAccess) {
      for (int i=(lPos+1); i<uPos; i++) {
        // Add all elements that end after searchFor starts
        T current = allRegionsSorted.get(i);
        if (current.getEnd()>searchFor.getStart()) {
          ret.add(current);
        }
        if (getClosestIfIntersectionIsEmpty && ret.isEmpty()) {
          // This code assumes, that we have no intersections
          // => current.getEnd() is always <= searchFor.getStart() 
          int distance = middle-current.getEnd();
          if (distance<minDistance) {
            minDistance = distance;
            closest = current;
          }
        }
      }
      if (uPos<size) nextRegionStartingBehindSearchFor = allRegionsSorted.get(uPos);
    } else {
      // Iterate to lPos+1
      Iterator<T> it = allRegionsSorted.iterator();
      int i=0;
      for (; i<(lPos+1);i++) {it.next();}
      // Check all to uPos 
      for (;i<uPos; i++) {
        // Add all elements that end after searchFor starts
        T current = it.next();
        if (current.getEnd()>searchFor.getStart()) {
          ret.add(current);
        }
        if (getClosestIfIntersectionIsEmpty && ret.isEmpty()) {
          // This code assumes, that we have no intersections
          // => current.getEnd() is always <= searchFor.getStart() 
          int distance = middle-current.getEnd();
          if (distance<minDistance) {
            minDistance = distance;
            closest = current;
          }
        }
      }
      if (it.hasNext()) nextRegionStartingBehindSearchFor = it.next();
    }
    

    // Eventually return closest
    if (getClosestIfIntersectionIsEmpty && ret.isEmpty()){
      // If we have to return the closest, we also need to check the
      // next item that starts behind searchFor
      if (nextRegionStartingBehindSearchFor!=null) {
        // This code assumes, that we have no intersections
        // => current.getStart() is always >= searchFor.getEnd() 
        int distance = nextRegionStartingBehindSearchFor.getStart()-middle;
        if (distance<minDistance) {
          minDistance = distance;
          closest = nextRegionStartingBehindSearchFor;
        }
      }
      if (closest!=null) ret.add(closest);
      return ret;
    }
    
    return ret;
  }

  /**
   * @return a {@link Comparator} that compares {@link Region}s,
   * primary by {@link #getChromosome()}, seconday by {@link #getStart()}
   * and tertiary by {@link #getEnd()}.
   */
  public static Comparator<? super Region> getComparator() {
    return new Comparator<Region>() {
      @Override
      public int compare(Region o1, Region o2) {
        int r = Utils.compareIntegers((int)o1.getChromosomeAsByteRepresentation(), (int)o2.getChromosomeAsByteRepresentation());
        if (r==0) {
          r = Utils.compareIntegers(o1.getStart(), o2.getStart());
          if (r==0) {
            r = Utils.compareIntegers(o1.getEnd(), o2.getEnd());
          }
        }
        
        return r;
      }
    };
  }
  
}
