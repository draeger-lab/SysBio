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
package de.zbit.sequence;

import de.zbit.util.StringUtil;
import de.zbit.util.Utils;


/**
 * Various utilities, specialized for DNA sequences.
 * 
 * 
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class DNAsequenceUtils {

  /**
   * Returns for a DNA sequence the complement sequence.
   * @param s DNA sequene string
   * @return complement sequence string
   */
  public static String complement(String s) {
    StringBuffer ret = new StringBuffer(s.length());
    char[] a = s.toLowerCase().toCharArray();
    for (int i=0; i<a.length; i++) {
      if (a[i]=='a') ret.append('t');
      else if (a[i]=='c') ret.append('g');
      else if (a[i]=='g') ret.append('c');
      else if (a[i]=='t') ret.append('a');
      else ret.append('n');
    }
    return ret.toString();
  }

  /**
   * Counts for a DNA sequence string the number of xMeres in this string.
   * E.g., for dimeres (xMeres=2), returns an array of 16 objects, containing
   * the number of AA, AC, AG,... dinucleotides. See {@link DNAsequenceUtils#DNA2Num(char)}
   * for array position and sequence link.
   * @see DNAsequenceUtils#DNA2Num(String)
   * @param sequence
   * @param xMeres
   * @return
   */
  public static int[] countNucleotides(String sequence, int xMeres) {
    int counts[] = new int[(int) Math.pow(4, xMeres)];
    for (int i=0; i<sequence.length()-xMeres+1; i++)
      counts[DNAsequenceUtils.DNA2Num(sequence.substring(i, i+xMeres))]++;
    return counts;
  }

  /**
   * Return a unique number for a DNA char.
   * @see Utils#Num2DNA(int) for the reverse function.
   * @param a
   * @return A=0, c=1, g=2, t=3. Else: -1.
   */
  public static int DNA2Num(char a) {
    if (a =='A' || a =='a') return 0;
    if (a =='C' || a =='c') return 1;
    if (a =='G' || a =='g') return 2;
    if (a =='T' || a =='t') return 3;
    
    System.err.println("Unknwon DNA Character: '" + a + "'.");
    return -1;
  }

  /**
   * Return a unique number for a DNA String.
   * Example:
   * AA: 0
   * AC: 1
   * AG: 2
   * AT: 3
   * CA: 4
   * TA: 12
   * TT: 15
   * @see Utils#Num2DNA(int, int) for the reverse function.
   * @param a DNA sequence
   * @return See example above.
   **/
  public static int DNA2Num(String a) {
    int ret = 0;
    char[] arr = StringUtil.reverse(a).toCharArray();
    for(int i=0; i<arr.length; i++)
      ret += (DNA2Num(arr[i])) * Math.pow(4, (i));
    return ret;
  }

  /**
   * Reverse of {@link DNA2Num}. Returns the nucleotide for a number.
   * @see #Num2DNA(int, int) to get the reverse of {@link DNA2Num}!
   * @param a
   * @return
   */
  public static char Num2DNA(int a) {
    if (a==0) return 'A';
    if (a==1) return 'C';
    if (a==2) return 'G';
    if (a==3) return 'T';
    
    System.err.println("To large input parameter on Num2DNA. Use xMeres variant of this function instead." + a);
    return 'N';
  }

  /**
   * Reverse of {@link DNA2Num}
   * @param n
   * @param xMeres
   * @return
   */
  public static String Num2DNA(int n, int xMeres) {
    String ret = "";
    for (int i=xMeres-1; i>0; i--) {
      int k = n/(int)Math.pow(4, (i));
      ret += Num2DNA(k%4);
    }
    int k = n % 4;
    ret += Num2DNA(k);    
    return ret;
  }
  
}
