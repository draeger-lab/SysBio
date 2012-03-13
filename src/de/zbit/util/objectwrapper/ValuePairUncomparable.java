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
package de.zbit.util.objectwrapper;

import java.io.Serializable;

/**
 * A pair of two values with type parameters. This data object is useful
 * whenever exactly two values are required for a specific task and
 * data must not be comparable.
 * <p><i>Note:<br/>This is actually the same as {@link ValuePair} but
 * it does <b>NOT</b> implement the {@link Comparable} interface and
 * thus, requires values inside the ValuePair to not having to
 * implement the {@link Comparable} interface.</i></p>
 * 
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class ValuePairUncomparable<S, T> implements Serializable {
  
  /**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = -8545896964735957097L;
  
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
  public ValuePairUncomparable(S a, T b) {
    this.setA(a);
    this.setB(b);
  }
  
  /**
   * 
   * @param ValuePairUncomparable
   */
  public ValuePairUncomparable(ValuePairUncomparable<S, T> ValuePairUncomparable) {
    this.a = ValuePairUncomparable.getA();
    this.b = ValuePairUncomparable.getB();
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#clone()
   */
  @Override
  public ValuePairUncomparable<S, T> clone() {
    return new ValuePairUncomparable<S, T>(this);
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @SuppressWarnings("unchecked")
  @Override
  public boolean equals(Object o) {
    if (o instanceof ValuePairUncomparable) {
      try {
        ValuePairUncomparable<S, T> v = (ValuePairUncomparable<S, T>) o;
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
  
  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return String.format("[%s, %s]", getA(), getB());
  }
  
}

