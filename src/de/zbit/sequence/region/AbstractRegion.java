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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.RandomAccess;

import de.zbit.util.Utils;

/**
 * A basic, abstract implementation of the {@link Region} interface. This allows other
 * classes to set a variable type for saving the end position. 
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public abstract class AbstractRegion extends ChromosomalPoint implements Region, Serializable, Cloneable, Comparable<Region> {
  private static final long serialVersionUID = 5027672293870790223L;
  
  /**
   * A comparator to compare two {@link Region}s.
   */
  private static Comparator<Region> regionComparator = new Comparator<Region>() {
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
  
  
  /**
   * 
   * @param chromosome
   * @param start
   * @param end
   * @throws Exception see {@link #setEnd(int)}
   * @see {@link AbstractRegion#createRegion(String, int, int)}
   */
  public AbstractRegion(String chromosome, int start, int end) throws Exception {
    this (ChromosomeTools.getChromosomeByteRepresentation(chromosome), start, end);
  }
  
  /**
   * @param chr as given by {@link ChromosomeTools#getChromosomeByteRepresentation(String)}
   * @param start
   * @param end
   * @throws Exception see {@link #setEnd(int)}
   * @see {@link AbstractRegion#createRegion(byte, int, int)}
   */
  public AbstractRegion(byte chr, int start, int end) throws Exception {
    super(chr, Math.min(start, end));
    setEnd(Math.max(start, end));
  }
  
  /**
   * Copy constructor
   * @param r
   * @throws Exception see {@link #setEnd(int)}
   * @see {@link AbstractRegion#createRegion(Region)}
   */
  public AbstractRegion(Region r) throws Exception {
    super(r);
    setEnd(r.getEnd());
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#clone()
   */
  @Override
  protected abstract AbstractRegion clone();
  
  /* (non-Javadoc)
   * @see de.zbit.sequence.region.Region#getMiddle()
   */
  @Override
  public int getMiddle() {
    return getStart()+(getLength()/2);
  }
  
  /**
   * @return length of this region.
   */
  public int getLength() {
    if (!isSetEnd()) return 0;
    else return getEnd()-getStart();
  }
  
  /* (non-Javadoc)
   * @see de.zbit.data.Region#getEnd()
   */
  @Override
  public abstract int getEnd();

  /* (non-Javadoc)
   * @see de.zbit.data.Region#setEnd(int)
   */
  @Override
  public abstract void setEnd(int end) throws Exception;

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
    return regionComparator.compare(this, o);
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    if (isSetEnd() && isSetStart()) {
      // both
      return String.format("%s:%s-%s", getChromosome(), getStart(), getEnd());
    } else {
      // Is a point
      return super.toString();
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
   * @return <code>TRUE</code> if end is set.
   */
  public boolean isSetEnd() {
    int end = getEnd();
    return end>-1 && end!=DEFAULT_START;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return super.hashCode() + getEnd()*7;
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
   * @param <K> May be T, may also be any {@link AbstractRegion}
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
    return regionComparator;
  }
  
  /**
   * A factory to get any {@link Region} implementation with minimal memory needs.
   * @param chromosome
   * @param start
   * @param end
   * @return
   */
  public static Region createRegion(String chromosome, int start, int end) {
    return createRegion(ChromosomeTools.getChromosomeByteRepresentation(chromosome), start, end);
  }

  /**
   * A factory to get any {@link Region} implementation with minimal memory needs.
   * @param chr as given by {@link ChromosomeTools#getChromosomeByteRepresentation(String)}
   * @param start
   * @param end
   * @return
   */
  public static Region createRegion(byte chr, int start, int end) {
    // Is any end position set?
    if (end<0 || end==DEFAULT_START || end==start) {
      return new ChromosomalPoint(chr, start);
    } else {
      int diff = end-start;
      try {
        if (diff<=Byte.MAX_VALUE) {
          return new SimpleVeryShortRegion(chr, start, end);
        } else if (diff<=Short.MAX_VALUE) {
          return new SimpleShortRegion(chr, start, end);
        } else {
          return new SimpleRegion(chr, start, end);
        }
      } catch (Exception e) {
        // Impossible, beacause exception is always when
        // end<0 or too long and since we check this here,
        // this exception is impossible.
        return null;
      }
    }
  }


  /**
   * Create a new simple region, based on the given {@link Region}.
   * This will create a region with minimal memory needs.
   * @param other
   * @return
   */
  public static Region createRegion(Region other) {
    return createRegion(other.getChromosomeAsByteRepresentation(), other.getStart(), other.getEnd());
  }
  

}
