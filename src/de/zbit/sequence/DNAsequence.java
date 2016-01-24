/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2016 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.sequence;

import java.io.IOException;
import java.util.Arrays;

import javax.xml.stream.events.Characters;


/**
 * Very memory efficient class to store DNA sequences.
 * This class needs only two bits for each nucleotide, whereas a {@link String}
 * needs 16 bits for each nucleotide. That saves us 87.5% RAM (8x longer sequences possible).
 * The drawback is, that you can't use 'N' or other {@link Characters} in the sequence.
 * Only a,c,g or t is permitted.
 * 
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class DNAsequence implements java.io.Serializable, Comparable<DNAsequence>, CharSequence, Appendable {
  private static final long serialVersionUID = 592181105415882342L;

  /**
   * Binary coded A,C,G, or T values.
   * <ul><li>00=A</li>
   * <li>01=C</li>
   * <li>10=G</li>
   * <li>11=T</li></ul>
   * <p>Reason for this:
   * Java memory usage: char 16bits, byte 8bits, boolean 1bit.
   * Thus, we only need 2 bits for a char, instead of 16 when using a {@link String}.
   * That saves us 87.5% RAM (8x longer sequences possible).
   */
  boolean[] sequence;
  
  /**
   * Number of characters in {@link #sequence}. Thus, {@link #sequence} must
   * be at least twice as long as {@link #size}.
   */
  int size;
  
  public DNAsequence() {
    this(16);
  }
  
  public DNAsequence(int initialCapacity) {
    super();
    
    size = 0;
    sequence = new boolean[Math.max(2*initialCapacity, 0)];
  }
  
  /**
   * @param binarySequence WITHOUT blanks! Trimmed to actual size only.
   */
  private DNAsequence(boolean[] binarySequence) {
    super();
    size = binarySequence.length/2;
    sequence = binarySequence;
  }

  public DNAsequence(DNAsequence other) {
    super();
    size = other.size;
    sequence = Arrays.copyOf(other.sequence, size);
  }
  
  public DNAsequence(String sequence) throws IOException {
    this (sequence.length());
    append(sequence);
  }

  /**
   * Trims the capacity of this <tt>DNAsequence</tt> instance to be the
   * current size. An application can use this operation to minimize
   * the storage of an <tt>DNAsequence</tt> instance.
   */
  public void trimToSize() {
    if (size*2 < sequence.length) {
      sequence = Arrays.copyOf(sequence, size*2);
    }
  }
  
  /**
   * Increases the capacity of this <tt>DNAsequence</tt> instance, if
   * necessary, to ensure that it can hold at least the number of elements
   * specified by the minimum capacity argument.
   *
   * @param   minCapacity   the desired minimum capacity
   */
  public void ensureCapacity(int minCapacity) {
    int oldCapacity = sequence.length;
    if (minCapacity*2 > oldCapacity) {
      expandCapacity(minCapacity);
    }
  }
  
  /**
   * This implements the expansion semantics of ensureCapacity with no
   * size check or synchronization.
   */
  void expandCapacity(int minimumCapacity) {
    minimumCapacity*=2;
    int newCapacity = (sequence.length * 3)/2 + 2;
    if (newCapacity < 0) {
      newCapacity = Integer.MAX_VALUE;
    } else if (minimumCapacity > newCapacity) {
      newCapacity = minimumCapacity;
    }
    sequence = Arrays.copyOf(sequence, newCapacity);
  }
  
  
  /**
   * Returns <tt>true</tt> if this sequence contains no elements.
   * @return <tt>true</tt> if this sequence contains no elements
   */
  public boolean isEmpty() {
    return size == 0;
  }

  /* (non-Javadoc)
   * @see java.lang.CharSequence#length()
   */
  public int length() {
    return size;
  }

  /* (non-Javadoc)
   * @see java.lang.CharSequence#charAt(int)
   */
  public char charAt(int index) {
    index*=2;
    return getDNAcharacter(sequence[index], sequence[index+1]);
  }

  /** Reverts binary coded A,C,G, or T values.
   * <ul><li>00=A</li>
   * <li>01=C</li>
   * <li>10=G</li>
   * <li>11=T</li></ul>
   * 
   * @param b first boolean
   * @param c second boolean
   * @return A,C,G or T as depicted in the table above.
   */
  private char getDNAcharacter(boolean first, boolean second) {
    if (first) {
      if (second) return 'T';
      return 'G';
    } else {
      if (second) return 'C';
      return 'A';
    }
  }
  
  /**
   * Reverse method for {@link #getDNAcharacter(boolean, boolean)}.
   * @param dna
   * @return binary pair, coding for given {@code dna} character.
   * @throws IOException if a character other than a,c,g or t occurs.
   */
  private boolean[] getBinaryPair(char dna) throws IOException {
    if (dna=='a' || dna=='A') {
      return new boolean[]{false, false};
    } else if (dna=='c' || dna=='C') {
      return new boolean[]{false, true};
    } else if (dna=='g' || dna=='G') {
      return new boolean[]{true, false};
    } else if (dna=='t' || dna=='T') {
      return new boolean[]{true, true};
    } else {
      throw new IOException(String.format("Unknown DNA character '%s'.", dna));
    }
  }

  /* (non-Javadoc)
   * @see java.lang.CharSequence#subSequence(int, int)
   */
  public CharSequence subSequence(int start, int end) {
    return new DNAsequence(Arrays.copyOfRange(sequence, start*2, end*2));
  }

  /* (non-Javadoc)
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(DNAsequence o) {
    int r = size - o.size;
    if (r!=0) return r;
    for (int i=0; i<sequence.length; i++)
      if (sequence[i]!=o.sequence[i]) return sequence[i]?1:-1;
    return 0;
  }
  
  /**
   * Additional compareTo method for convenience.
   * @param o
   * @return
   */
  public int compareTo(String o) {
    int r = size - o.length();
    if (r!=0) return r;
    
    int doubleSize = size*2;
    for (int i=0; i<doubleSize; i+=2) {
       char c = (getDNAcharacter(sequence[i], sequence[i+1]));
       if (c!=Character.toUpperCase(o.charAt(i/2))) return sequence[i]?1:-1;
    }
    
    return 0;
  }

  /* (non-Javadoc)
   * @see java.lang.Appendable#append(java.lang.CharSequence)
   */
  public Appendable append(CharSequence csq) throws IOException {
    return append(csq, 0, csq.length());
  }

  /* (non-Javadoc)
   * @see java.lang.Appendable#append(java.lang.CharSequence, int, int)
   */
  public Appendable append(CharSequence csq, int start, int end)throws IOException {
    int length = end-start;
    
    int newSize = size+length;
    if (newSize*2 > sequence.length)
        expandCapacity(newSize);
    
    int doubleSize = size*2;
    int position;
    boolean[] pair;
    for (int i=0; i<length; i++) {
      pair = getBinaryPair(csq.charAt(start+i));
      position = doubleSize+(i*2);
      sequence[position] = pair[0];
      sequence[position+1] = pair[1];
    }
    // Set size after parsing all chars, so if an exception occurs, this
    // class still represents the old sequence.
    size+=length;
    return this;
  }

  /* (non-Javadoc)
   * @see java.lang.Appendable#append(char)
   */
  public Appendable append(char c) throws IOException {
    int newSize = size + 1;
    if (newSize*2 > sequence.length)
        expandCapacity(newSize);
    
    boolean[] pair = getBinaryPair(c);
    int position = size*2;
    sequence[position] = pair[0];
    sequence[position+1] = pair[1];
    size++;
    return this;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder(size);
    int doubleSize = size*2;
    for (int i=0; i<doubleSize; i+=2) {
      builder.append(getDNAcharacter(sequence[i], sequence[i+1]));
    }
    return builder.toString();
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (DNAsequence.class.isAssignableFrom(obj.getClass())) {
      return compareTo(((DNAsequence)obj))==0;
    } else if (String.class.isAssignableFrom(obj.getClass())) {
      return compareTo(((String)obj))==0; 
    } else {
      return false;
    }
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    int result = 1;
    int doubleSize = size*2;
    for (int i=0; i<doubleSize; i++) {
      result = 31 * result + (sequence[i] ? 1231 : 1237);
    }
    return result;
  }
  
}
