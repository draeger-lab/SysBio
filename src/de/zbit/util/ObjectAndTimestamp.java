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
package de.zbit.util;

import java.io.Serializable;

/**
 * Stores an object together with a timestamp. Updates the timestamp
 * whenever this information is accessed.
 * @author Clemens Wrzodek
 * @version $Rev$
 * @param <INFOtype>
 */
public class ObjectAndTimestamp<INFOtype> implements Comparable, Serializable {
  private static final long serialVersionUID = 7597343480723045939L;
  
  /**
   * The information itself.
   */
  private INFOtype information;
  /**
   * Datestamp, when this information has been accessed the last time.
   */
  private long lastUsage = System.currentTimeMillis();
  
  
  /**
   * Construct a new element.
   * 
   * @param information Content
   */
  public ObjectAndTimestamp(INFOtype information) {
    super();
    this.information = information;
  }
  
  /**
   * Returns the information content of this object.
   * 
   * Do NOT call this function internally (e.g. for sorting), because
   * it affects the caching behavior (it stores last usage informations).
   * Use {@link #getInformation(boolean)} instead.
   * 
   * @return
   */
  public INFOtype getInformation() {
    return getInformation(true);
  }
  
  /**
   * Returns the information content of this object.
   * @param updateTimestamp - if true, the timestamp of this
   * object will be set to currentTimeMillis().
   * @return
   */
  protected INFOtype getInformation(boolean updateTimestamp) {
    if (updateTimestamp) {
      resetTimestamp();
    }
    return information;
  }

  /**
   * Sets the timestamp to 'now'.
   */
  public void resetTimestamp() {
    lastUsage = System.currentTimeMillis();
  }
  
  /**
   * @return date stamp, when this information has been accessed
   * the last time.
   */
  public long getLastUsage() {
    return lastUsage;
  }
  
  /**
   * Sets the information and resets the timestamp.
   * @param information
   */
  public void setInformation(INFOtype information) {
    this.information = information;
    resetTimestamp();
  }
  
  /*
   * (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return (information.hashCode());
  }
  
  
  /*
   * (non-Javadoc)
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public int compareTo(Object o) {
    int r = getComparable(this).compareTo(getComparable(o));
    if (r==0 && o instanceof ObjectAndTimestamp) {
      // Compare timestamp
      long t = (lastUsage - ((ObjectAndTimestamp)o).lastUsage);
      if (t==0) r = 0;
      else r = t<0?-1:1;
    }
    return r;
  }
  
  
  @SuppressWarnings("rawtypes")
  private Comparable getComparable(Object o) {
    Comparable other;
    if (o instanceof ObjectAndTimestamp) {
      Object oi = ((ObjectAndTimestamp)o).information;
      if (oi instanceof Comparable) {
        other = (Comparable) oi;
      } else {
        other = oi.toString();
      }
    } else {
      other = o.toString();
    }
    return other;
  }
  
  
  /**
   * Compares just the object, not the timestamp!
   * @param o
   * @return
   */
  public boolean equals(ObjectAndTimestamp<INFOtype> o) {
    if (this.information==null) {
      return o.information==null;
    } else {
      if (o.information==null) return false;
      return (o.information.equals(this.information));
    }
  }
  
  /*
   * (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Override
  public boolean equals(Object obj) {
    try {
      if (obj instanceof ObjectAndTimestamp)
        return equals((ObjectAndTimestamp) obj); // do NOT add <?> => Stack overflow.
    } catch (Exception e) {
    } // Other subtypes. Wrong cast!
    return super.equals(obj);
  }
  
  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return String.format("[@Object '%s' timestamp '%s']", information.toString(), lastUsage);
  }  
}
