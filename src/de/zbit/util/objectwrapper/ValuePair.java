/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2014 by the University of Tuebingen, Germany.
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
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * A pair of two values with type parameters. This data object is useful
 * whenever exactly two values are required for a specific task.
 * 
 * @see ValueTriplet
 * @see ValuePairUncomparable
 * @author Andreas Dr&auml;ger
 * @author Clemens Wrzodek
 * @date 2010-09-01
 * @version $Rev$
 * @since 1.0
 */
public class ValuePair<S extends Comparable<? super S>, T extends Comparable<? super T>>
		implements Comparable<ValuePair<S, T>>, Serializable {

	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = -4230267902609475128L;
	/**
	 * 
	 */
	private S a;
	/**
	 * 
	 */
	private T b;

	/**
	 * 
	 * @param a
	 * @param b
	 */
	public ValuePair(S a, T b) {
		this.setA(a);
		this.setB(b);
	}

	/**
	 * 
	 * @param valuePair
	 */
	public ValuePair(ValuePair<S, T> valuePair) {
		this.a = valuePair.getA();
		this.b = valuePair.getB();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public ValuePair<S, T> clone() {
		return new ValuePair<S, T>(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(ValuePair<S, T> v) {
		if (equals(v)) {
			return 0;
		}
		if (!isSetA()) {
			return Integer.MIN_VALUE;
		}
		if (!v.isSetA()) {
			return Integer.MAX_VALUE;
		}
		int comp = getA().compareTo(v.getA());
		if (comp == 0) {
			if (!isSetB()) {
				return -1;
			}
			if (!v.isSetB()) {
				return 1;
			}
			return getB().compareTo(v.getB());
		}
		return comp;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object o) {
		if (o instanceof ValuePair) {
			try {
				ValuePair<S, T> v = (ValuePair<S, T>) o;
				boolean equal = true;
				equal &= isSetA() == v.isSetA();
				equal &= isSetB() == v.isSetB();
        if (equal && isSetA()) {
          equal &= v.getA().equals(getA());
        }
        if (equal && isSetB()) {
          equal &= v.getB().equals(getB());
        }
				return equal;
			} catch (ClassCastException exc) {
				return false;
			}
		}
		return false;
	}

	/**
	 * @return the a
	 */
	public S getA() {
		return a;
	}

	/**
	 * @return the b
	 */
	public T getB() {
		return b;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return a.hashCode() + b.hashCode();
	}

	/**
	 * 
	 * @return
	 */
	public boolean isSetA() {
		return a != null;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isSetB() {
		return b != null;
	}

	/**
	 * @param a
	 *            the a to set
	 */
	public void setA(S a) {
		this.a = a;
	}

	/**
	 * @param b
	 *            the b to set
	 */
	public void setB(T b) {
		this.b = b;
	}
	
	/**
	 * @return a comparator, that only sorts {@link ValuePair}s by {@link #getA()}.
	 */
	public Comparator<ValuePair<S, T>> getComparator_OnlyCompareA() {
	  Comparator<ValuePair<S, T>> sortOnlyByA = new Comparator<ValuePair<S, T>>() {
	    public int compare(ValuePair<S, T> o1, ValuePair<S, T> o2) {
	      return o1.getA().compareTo(o2.getA());
	    }
	  };
	  
	  return sortOnlyByA;
	}
	
	/**
	 * @return a comparator, that only sorts {@link ValuePair}s by {@link #getB()}.
	 */
	public Comparator<ValuePair<S, T>> getComparator_OnlyCompareB() {
	  Comparator<ValuePair<S, T>> sortOnlyByB = new Comparator<ValuePair<S, T>>() {
	    public int compare(ValuePair<S, T> o1, ValuePair<S, T> o2) {
	      return o1.getB().compareTo(o2.getB());
	    }
	  };
	  
	  return sortOnlyByB;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("[%s, %s]", getA(), getB());
	}

  /**
   * Returns a list, that only contains the {@link #getB()} values of {@link ValuePair}s.
   * @param values
   * @return
   */
	public static <S extends Comparable<? super S>, T extends Comparable<? super T>> 
	List<T> getListOfB(Iterable<ValuePair<S, T>> values) {
	  List<T> ret = new LinkedList<T>();
	  
	  Iterator<ValuePair<S, T>> it = values.iterator();
	  while (it.hasNext()) {
      ret.add(it.next().getB());
    }
    
    return ret;
  }
  
  /**
   * Returns a list, that only contains the {@link #getA()} values of {@link ValuePair}s.
   * @param values
   * @return
   */
  public static <S extends Comparable<? super S>, T extends Comparable<? super T>> 
  List<S> getListOfA(Iterable<ValuePair<S, T>> values) {
    List<S> ret = new LinkedList<S>();
    
    Iterator<ValuePair<S, T>> it = values.iterator();
    while (it.hasNext()) {
      ret.add(it.next().getA());
    }
    
    return ret;
  }
  
}
