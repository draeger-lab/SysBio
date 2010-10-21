package de.zbit.util;

import java.io.Serializable;

/**
 * Improves data Management when data is associated to an id or descriptor.
 * This class represents one element in the cache.
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
        return equals((Info) obj);
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
