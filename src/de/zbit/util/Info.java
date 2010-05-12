package de.zbit.util;

import java.io.Serializable;

/**
 * Improves data Management when data is associated to an id or descriptor.
 * 
 * @author wrzodek
 * 
 * @param <IDtype>
 *            Type of the id (int or string,...) must be comparable.
 * @param <INFOtype>
 *            Type of information (arbitrary)
 */
@SuppressWarnings("unchecked")
public class Info<IDtype extends Comparable, INFOtype> implements Comparable, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3592331552130670620L;
	/**
	 * 
	 */
	private IDtype identifier;
	/**
	 * 
	 */
	private INFOtype information;
    /**
     * 
     */
	private int timesInfoAccessed = 0;
	/**
	 * Remebering the time, the fetching of this information took (in s, (Minium of 1!!!))
	 */
	private float timeForFetchingInfo=(float) 1.0;

	/**
	 * Construct a new element.
	 * 
	 * @param identifier
	 * @param information
	 *            - Content
	 */
	public Info(IDtype identifier, INFOtype information) {
		this.identifier = identifier;
		this.information = information;
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
				return equals((Info<IDtype, INFOtype>) obj);
		} catch (Exception e) {
		} // Other subtypes. Wrong cast!
		return super.equals(obj);
	}
	
	/**
	 * 
	 * @return
	 */
	public IDtype getIdentifier() {
		return identifier;
	}
	
	/**
	 * 
	 * @return
	 */
	public INFOtype getInformation() {
		/*
		 * Do NOT call this function internaly, since it remembers how often you
		 * use it.
		 */
		if (timesInfoAccessed != Integer.MAX_VALUE)
			timesInfoAccessed++;
		return information;
	}

	/**
	 * Returns the time, the fetching of this information took.
	 * In seconds with minimum of 1!
	 * @return
	 */
	public float getTimeForFetchingInfo() {
	  return (float) Math.max(1.0, timeForFetchingInfo);
	}

	/**
	 * 
	 * @return
	 */
	public int getTimesInfoAccessed() {
		return timesInfoAccessed;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return getIdentifier().hashCode() + information.hashCode();
	}

	/**
	 * 
	 */
	public void resetTimesInfoAccessed() {
		timesInfoAccessed = 0;
	}

	/**
	 * 
	 * @param information
	 */
	public void setInformation(INFOtype information) {
		this.information = information;
	}

	/**
   * Sets the time, the fetching of this information took.
   * In seconds with minimum of 1!
   * 
   * If you give this information, items which take long to retrieve have less probability
   * to get remove from the queue if it's full.
   */
	public void setTimeForFetchingInfo(float f) {
	  this.timeForFetchingInfo = Math.max(f,(float)1.0);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "@InfoObject. ID: '" + getIdentifier().toString()
				+ "'. Information: '" + information.toString() + "'.";
	}

}
