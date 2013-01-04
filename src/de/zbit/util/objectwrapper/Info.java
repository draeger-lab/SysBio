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
package de.zbit.util.objectwrapper;

import java.io.Serializable;

/**
 * Improves data Management when data is associated to an id or descriptor.
 * This class represents one element in the cache.
 * <p> In a way, this class is similar to {@link ValuePair}, but {@link Comparable}
 * is only required for the identifier and comparisons are also
 * performed on identifiers.
 * 
 * <p>In addition to the Identifier and Info, a {@link #lastUsage} timestamp
 * is stored for every object.
 * 
 * @author Clemens Wrzodek
 * 
 * @param <IDtype>
 *            Type of the id (int or string,...) must be comparable.
 * @param <INFOtype>
 *            Type of information (arbitrary)
 * @version $Rev$
 * @since 1.0
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class Info<IDtype extends Comparable, INFOtype> implements Comparable, Serializable {
	private static final long serialVersionUID = 3592331552130670620L;
	/**
	 * Identifier for the stored information.
	 */
	private IDtype identifier;
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
	 * @param identifier
	 * @param information - Content
	 */
	public Info(IDtype identifier, INFOtype information) {
		this.identifier = identifier;
		this.information = information;
	}
	
	/**
	 * Returns the identifier for this object.
	 * @return
	 */
	public IDtype getIdentifier() {
		return identifier;
	}
	
	/**
	 * Returns the information content of this object.
	 * 
   * Do NOT call this function internaly (e.g. for sorting), because
   * it affects the caching behaviour (it stores last usage informations).
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
	    lastUsage = System.currentTimeMillis();
	  }
	  return information;
	}
	
	/**
	 * @return date stamp, when this information has been accessed
	 * the last time.
	 */
	public long getLastUsage() {
	  return lastUsage;
	}
	
  /**
   * 
   * @param information
   */
  public void setInformation(INFOtype information) {
    this.information = information;
  }
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return getIdentifier().hashCode() + information.hashCode();
	}
	

  /*
   * (non-Javadoc)
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(Object o) {
    if (o instanceof Info) {
      return identifier.compareTo(((Info) o).getIdentifier());
    } else if (o instanceof Comparable) {
      try {
        return identifier.compareTo((IDtype) o);
      } catch (Exception e) {
      } // Invalid cast
      return identifier.compareTo((Comparable) o);
    }
    System.err.println("Cannot compare Info to " + o);
    return 0;
  }

  /**
   * 
   * @param o
   * @return
   */
  public boolean equals(Info<IDtype, INFOtype> o) {
    if (o.getIdentifier().equals(this.getIdentifier())
        && o.information.equals(this.information))
      return true;
    return false;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    try {
      if (obj instanceof Info)
        return equals((Info) obj); // do NOT add <?> => Stack overflow.
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
		return "[@Info ID: '" + getIdentifier().toString()
				+ "' Information: '" + information.toString() + "']";
	}

}
