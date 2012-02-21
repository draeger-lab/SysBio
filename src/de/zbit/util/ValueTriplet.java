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
package de.zbit.util;

import java.io.Serializable;
import java.util.Comparator;

/**
 * A triplet of three values with type parameters. This data object is useful
 * whenever exactly three values are required for a specific task.
 * @see ValuePair
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class ValueTriplet <S extends Comparable<? super S>, T extends Comparable<? super T>, E extends Comparable<? super E>>
  implements Comparable<ValueTriplet<S, T, E>>, Serializable {
  
	/**
	 * Generated serial version identifier.
	 */
  private static final long serialVersionUID = -5702457060946906428L;
  
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
   */
  private E c;

  /**
   * 
   * @param a
   * @param b
   * @param c
   */
  public ValueTriplet(S a, T b, E c) {
    this.setA(a);
    this.setB(b);
    this.setC(c);
  }

  /**
   * 
   * @param ValueTriplet
   */
  public ValueTriplet(ValueTriplet<S, T, E> ValueTriplet) {
    this.a = ValueTriplet.getA();
    this.b = ValueTriplet.getB();
    this.b = ValueTriplet.getB();
  }

  /* (non-Javadoc)
   * @see java.lang.Object#clone()
   */
  @Override
  public ValueTriplet<S, T, E> clone() {
    return new ValueTriplet<S, T, E>(this);
  }

  /* (non-Javadoc)
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(ValueTriplet<S, T, E> v) {
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
        return -2;
      }
      if (!v.isSetB()) {
        return 2;
      }
      comp = getB().compareTo(v.getB());
      if (comp == 0) {
        if (!isSetC()) {
          return -1;
        }
        if (!v.isSetC()) {
          return 1;
        }
        comp = getC().compareTo(v.getC());
      }
    }
    return comp;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @SuppressWarnings("unchecked")
  @Override
  public boolean equals(Object o) {
    if (o instanceof ValueTriplet) {
      try {
        ValueTriplet<S, T, E> v = (ValueTriplet<S, T, E>) o;
        boolean equal = true;
        equal &= isSetA() == v.isSetA();
        equal &= isSetB() == v.isSetB();
        equal &= isSetC() == v.isSetC();
        if (equal && isSetA() && isSetB() && isSetC()) {
          equal &= v.getA().equals(getA()) && v.getB().equals(getB()) && v.getC().equals(getC());
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
  
  /**
   * @return the c
   */
  public E getC() {
    return c;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return a.hashCode() + b.hashCode() + c.hashCode();
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
   * 
   * @return
   */
  public boolean isSetC() {
    return c != null;
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
   * @param c
   *            the c to set
   */
  public void setC(E c) {
    this.c = c;
  }

  /**
   * @return a comparator, that only sorts {@link ValueTriplet}s by {@link #getA()}.
   */
  public Comparator<ValueTriplet<S, T, E>> getComparator_OnlyCompareA() {
    Comparator<ValueTriplet<S, T, E>> sortOnlyByA = new Comparator<ValueTriplet<S, T, E>>() {
      public int compare(ValueTriplet<S, T, E> o1, ValueTriplet<S, T, E> o2) {
        return o1.getA().compareTo(o2.getA());
      }
    };
    
    return sortOnlyByA;
  }
  
  /**
   * @return a comparator, that only sorts {@link ValueTriplet}s by {@link #getB()}.
   */
  public Comparator<ValueTriplet<S, T, E>> getComparator_OnlyCompareB() {
    Comparator<ValueTriplet<S, T, E>> sortOnlyByB = new Comparator<ValueTriplet<S, T, E>>() {
      public int compare(ValueTriplet<S, T, E> o1, ValueTriplet<S, T, E> o2) {
        return o1.getB().compareTo(o2.getB());
      }
    };
    
    return sortOnlyByB;
  }
  
  /**
   * @return a comparator, that only sorts {@link ValueTriplet}s by {@link #getC()}.
   */
  public Comparator<ValueTriplet<S, T, E>> getComparator_OnlyCompareC() {
    Comparator<ValueTriplet<S, T, E>> sortOnlyCyC = new Comparator<ValueTriplet<S, T, E>>() {
      public int compare(ValueTriplet<S, T, E> o1, ValueTriplet<S, T, E> o2) {
        return o1.getC().compareTo(o2.getC());
      }
    };
    
    return sortOnlyCyC;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return String.format("[%s, %s, %s]", getA(), getB(), getC());
  }

}
