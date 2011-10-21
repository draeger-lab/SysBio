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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import de.zbit.io.OpenFile;


/**
 * Various utils, which I need quite often.
 * 
 * @author Clemens Wrzodek
 * @version $Rev$
 * @since 1.0
 */
public class Utils {

  /**
   * The natural logarithm of 2.
   */
  private final static double ln2 = Math.log(2);
  
  /**
   * Returns true if and only if arr contains s (case insensitive).
   * @param arr
   * @param s
   * @return
   */
  public static boolean ArrayContains(String[][] arr, String s) {
    s = s.trim();
    for (int i=0; i<arr.length; i++)
      for (int j=0; j<arr[i].length; j++)
        if (arr[i][j].trim().equalsIgnoreCase(s)) return true;
    return false;
  }
  
  /**
   * If there exists one element in arr such that arr[i].equals(s),
   * i (the index) is returned.
   * @param <T>
   * @param arr
   * @param s
   * @return
   */
  public static <T> int arrayIndexOf(T[] arr, T s) {
    for (int i=0; i<arr.length; i++)
        if (arr[i].equals(s)) return i;
    return -1;
  }
  
  /**
   * Returns true if and only if there exists one item in arr
   * such that arr[i].equals(s).
   * @param <T>
   * @param arr
   * @param s
   * @return
   */
  public static <T> boolean ArrayContains(T[] arr, T s) {
    for (int i=0; i<arr.length; i++)
        if (arr[i].equals(s)) return true;
    return false;
  }
  
  /**
   * Returns true if and only if there exists one item in arr
   * such that arr[i].equals(s) OR arr[i].equalsIgnoreCase(s),
   * depending on <code>ignoreCase</code>.
   * @param <T>
   * @param arr
   * @param s
   * @param ignoreCase
   * @return
   */
  public static <T> boolean ArrayContains(String[] arr, String s, boolean ignoreCase) {
    return returnString(arr, s, ignoreCase) != null;
  }
  
  /**
   * Returns the average of all non-NaN and non-infinite values in the given
   * array. Internally, it first tries to use the faster implementation and if
   * that doesn't work, it falls back to the slower alternative.
   * 
   * @param d an array of double values
   * @return the average of all non-NaN and non-infinite values in the given
   *         array.
   */
  public static double average(double[] d){
    double average = average1(d);
    if (Double.isNaN(average) || Double.isInfinite(average)) 
        return average2(d);
    return average;
  }
  
  /**
   * Spaltenweise mittelwertberechnung.
   * Versuchts erst schneller und nimmt sonst den langsameren, aber sicheren Algorithmus.
   */
  public static double[] average(double[][] d){
    double[] average = average1(d);
    if (average == null) return null; // Koomt vor wenn er alle sequenzen nicht mappen kann 
    for (int i=0; i<average.length; i++)
      if (Double.isNaN(average[i]) || average[i]==Double.POSITIVE_INFINITY || average[i]==Double.NEGATIVE_INFINITY)
        return average2(d);
    return average;
  }
  
  /**
   * Returns the average of all non-NaN and non-infinite values in the given
   * array. This implementation first sums up all values and then divides by the
   * number of values. If too many too large values are given, this could lead
   * to a double sum overflow.
   * 
   * @param d an array of double values
   * @return the average of all non-NaN and non-infinite values in the given
   *         array.
   */
  public static double average1(double[] d){ // Schneller
    if (d==null || d.length<1) return Double.NaN;
    double retVal= 0;
    
    int countNonNAN=0;
    for (int i=0; i<d.length; i++) {
      if (Double.isNaN(d[i]) || Double.isInfinite(d[i])) continue;
      countNonNAN++;
      retVal+=d[i];
    }
    
    if (countNonNAN<=0) return Double.NaN;
    return (retVal/countNonNAN);
  }
  
  /**
   * 
   * @param d
   * @return
   */
  public static double[] average1(double[][] d){ // Schneller
    if (d.length<1) return new double[0];
    double[] retVal= null;
    
    int countNonNull = 0;
    for (int i=0; i<d.length; i++) {
      if (d[i] == null) continue; // kommt vor wenn er sequenz i nicht mappen kann
      countNonNull++;
      if (retVal==null) retVal = new double[d[i].length];
      for (int j=0; j<d[i].length; j++)
        retVal[j]+=d[i][j];
    }

    if (retVal==null) return null; // Koomt vor wenn er alle sequenzen nicht mappen kann
    for (int i=0; i<retVal.length; i++)
      retVal[i] /= countNonNull;
    
    
    return retVal;
  }

  /**
   * Returns the average of all non-NaN and non-infinite values in the given
   * array. This implementation performs an iterative calculation of the mean,
   * making it slower and possibly less accurate, but avoid double sum overflows
   * when too many too large values are given.
   * 
   * @param d an array of double values
   * @return the average of all non-NaN and non-infinite values in the given
   *         array.
   */
  public static double average2(double[] d){ // Keine to-large-numbers
    if (d.length<1) return Double.NaN;
    double retVal= 0;

    int countNonNAN=0;
    for (int i=0; i<d.length; i++) {
      if (Double.isNaN(d[i]) || Double.isInfinite(d[i])) continue;
      countNonNAN++;
      
      // retVal[j]=retVal[j] * i/(i+1) + d[i][j] * 1/(i+1);
      retVal=retVal * (countNonNAN-1)/(countNonNAN) + d[i] * 1/(countNonNAN);
    }
    
    // Wenn irgendwo nur NaNs waren, das auch so wiedergeben
    if (countNonNAN<=0) return Double.NaN;
    return retVal;
  }
  
  /**
   * 
   * @param d
   * @return
   */
  public static double[] average2(double[][] d){ // Keine to-large-numbers
    if (d.length<1) return new double[0];
    double[] retVal= null;
    ArrayList<Integer> spaltenCounter = new ArrayList<Integer>(); 
    for (int i=0; i<d.length; i++) {
      if (d[i] == null) continue; // kommt vor wenn er sequenz i nicht mappen kann
      if (retVal==null) retVal = new double[d[i].length];
      for (int j=0; j<d[i].length; j++) {
        if (spaltenCounter.size()<=j) spaltenCounter.add(0);
        if (Double.isNaN(d[i][j])) continue; // Deshalb auch der Spaltencounter: Skip NaN eintr�ge.
        //retVal[j]=retVal[j] * i/(i+1) + d[i][j] * 1/(i+1);
        retVal[j]=retVal[j] * spaltenCounter.get(j)/(spaltenCounter.get(j)+1) + d[i][j] * 1/(spaltenCounter.get(j)+1);
        spaltenCounter.set(j,spaltenCounter.get(j)+1);
      }
    }
    // Wenn irgendwo nur NaNs waren, das auch so wiedergeben
    for (int i=0; i<spaltenCounter.size(); i++)
      if (spaltenCounter.get(i)==0) retVal[i] = Double.NaN;
    return retVal;
  }
  
  /**
   * Calculates the median of the given values.
   * The input array is MODIFIED (sorted).
   * The median of an empty array is defined to be Double.NaN
   * 
   * From Wikipedia:
   * The median of a finite list of numbers can be found by arranging all the
   * observations from lowest value to highest value and picking the middle
   * one. If there is an even number of observations, then there is no single
   * middle value; the median is then usually defined to be the mean of the
   * two middle values.
   * 
   * @param values
   * @return median
   */
  public static double median(double[] values) {
    if (values.length<1) return Double.NaN;
    Arrays.sort(values);
    
    if (values.length%2!=0) {
      return values[values.length/2];
    } else {
      int upper = (int) Math.ceil(values.length/2);
      return (values[upper-1]+(values[upper]-values[upper-1])/2);
    }
  }
  
  /**
   * Calculates the median of the given values.
   * The input list is <b>MODIFIED (sorted)</b>.
   * The median of an empty array is defined to be Double.NaN
   * 
   * @see median
   * @param values
   * @return median
   */
  
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public static double median(List values) {
    if (values.size()<1) return Double.NaN;
    Collections.sort(values);
    
    Object median;
    if (values.size()%2!=0) {
      median = values.get(values.size()/2);
      return getDoubleValue(median);
    } else {
      int upper = (int) Math.ceil(values.size()/2);
      double upperMedian = getDoubleValue(values.get(upper));
      double lowerMedian = getDoubleValue(values.get(upper-1));
      
      return (lowerMedian+(upperMedian-lowerMedian)/2);
    }
    
  }
  
  /**
   * Returns the value at relative (percentage) index <code>quantilke</code>
   * in the sorted list <code>values</code>.
   * @param values
   * @param quantile
   * @param listIsAlreadySorted in doubt, set to false.
   * @return
   */
  public static double quantile(List values, int quantile, boolean listIsAlreadySorted) {
    if (values.size()<1) return Double.NaN;
    if (!listIsAlreadySorted) Collections.sort(values);
    double valIndex = values.size()/100*quantile;
    double valFloor = Math.floor(valIndex);
    // (5 / 100) * 50 = 2.5 => Bei ungrade abrunden
    // (4 / 100) * 50 = 2 => Bei grade mittel aus idx-1 und idx.
    
    if(valFloor != valIndex) {
      // value is NO integer
      Object ret = values.get((int) valFloor);
      return getDoubleValue(ret);
    } else {
      // value is an integer
      double upperMedian = getDoubleValue(values.get((int) valFloor));
      double lowerMedian = getDoubleValue(values.get((int) (valFloor-1)));
      
      return (lowerMedian+(upperMedian-lowerMedian)/2);
    }
    
  }
  
  /**
   * Converts the collection to a list and returns the median.
   * @param values
   * @return
   */
  
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public static double median(Collection values) {
    if (values instanceof List) {
      return median ((List)values);
    } else {
      ArrayList list = new ArrayList(values.size());
      list.addAll(values);
      return median ((List)list);
    }
  }

  /**
   * Returns the double value of any {@link Number}.
   * @param o
   */
  public static double getDoubleValue(Object o) {
    double d;
    if (o instanceof Number || Number.class.isAssignableFrom(o.getClass()) )
      d = ((Number)o).doubleValue();
    else d = Double.parseDouble(o.toString());
    return d;
  }
  
  /**
   * Returns the standard deviation of the given double values.
   * The Standard deviation is the square root of the variance.
   * @param values
   * @return
   */
  public static double standardDeviation(double[] values){
    return Math.sqrt(variance(values));
  }
  
  /**
   * Returns the standard deviation of the given double values.
   * The Standard deviation is the square root of the variance.
   * @param values
   * @param mean -  the precomputed mean value
   * @return
   */
  public static double standardDeviation(double[] values, double mean){
    return Math.sqrt(variance(values, mean));
  }
  
  /**
   * Mittelwertberechnung.
   * Versuchts erst schneller und nimmt sonst den langsameren, aber sicheren Algorithmus.
   */
  @SuppressWarnings("rawtypes")
  public static double average(Collection d){
    double average = average1(d);
    if (Double.isNaN(average) || Double.isInfinite(average)) 
        return average2(d);
    return average;
  }
  @SuppressWarnings("rawtypes")
  private static double average1(Collection doubles){ // Schneller
    if (doubles==null || doubles.size()<1) return Double.NaN;
    double retVal= 0;
    
    int countNonNAN=0;
    Iterator it = doubles.iterator();
    while (it.hasNext()) {
      try  {
        double d=0;
        Object o = it.next();
        d = getDoubleValue(o);
        
        if (Double.isNaN(d) || Double.isInfinite(d)) continue;
        countNonNAN++;
        retVal+=d;        
        
      } catch (Throwable t) {t.printStackTrace();}
    }
    
    if (countNonNAN<=0) return Double.NaN;
    return (retVal/countNonNAN);
  }
  
  @SuppressWarnings("rawtypes")
  private static double average2(Collection doubles){ // Keine to-large-numbers
    if (doubles==null || doubles.size()<1) return Double.NaN;
    double retVal= 0;
    
    int countNonNAN=0;
    Iterator it = doubles.iterator();
    while (it.hasNext()) {
      try  {
        double d=0;
        Object o = it.next();
        d = getDoubleValue(o);
        
        if (Double.isNaN(d) || Double.isInfinite(d)) continue;
        countNonNAN++;
        
        // retVal[j]=retVal[j] * i/(i+1) + d[i][j] * 1/(i+1);
        retVal=retVal * (countNonNAN-1)/(countNonNAN) + d * 1/(countNonNAN);
        
      } catch (Throwable t) {t.printStackTrace();}
    }
    
    // Wenn irgendwo nur NaNs waren, das auch so wiedergeben
    if (countNonNAN<=0) return Double.NaN;
    return retVal;
  }
  
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
   * Empirical Correlation Coefficient computes the correlation coefficient
   * between y (lables) and x (predictions)
   * 
   * @param y
   * @param x
   * @param mean_y
   * @param mean_x
   * @return
   */
  public static double computeCorrelation(double[] y, double[] x, double mean_y, double mean_x) {
    double numerator = 0.0;
    for (int i = 0; i < y.length; i++) {
      numerator = numerator + (x[i] - mean_x) * (y[i] - mean_y);
    }
    numerator = numerator / y.length;
    
    double denominator_x = 0.0;
    double denominator_y = 0.0;
    for (int i = 0; i < y.length; i++) {
      denominator_x = denominator_x + Math.pow((x[i] - mean_x), 2);
    }
    for (int i = 0; i < y.length; i++) {
      denominator_y = denominator_y + Math.pow((y[i] - mean_y), 2);
    }
    
    denominator_x = Math.sqrt(denominator_x / y.length);
    denominator_y = Math.sqrt(denominator_y / y.length);
    
    return numerator / (denominator_x * denominator_y);
  }
  
  
  /**
   * Same as {@link isWord} !!!
   */
  public static boolean containsWord(String containingLine, String containedString) {
    return isWord(containingLine, containedString);
  }
  
  /**
   * Copies a file. Does NOT check if out already exists. Will overwrite out if it already exists.
   * @param in
   * @param out
   * @return success.
   */
  public static boolean copyFile(File in, File out) {
    if (!in.exists()) {System.err.println("File '" + in.getName() + "' does not exist."); return false;}
    boolean success=false;
    try {
      FileChannel inChannel = new FileInputStream(in).getChannel();
      FileChannel outChannel = new FileOutputStream(out).getChannel();
      // magic number for Windows, 64Mb - 32Kb)
      int maxCount = (64 * 1024 * 1024) - (32 * 1024);
      long size = inChannel.size();
      long position = 0;
      while (position < size) {
        position += inChannel.transferTo(position, maxCount, outChannel);
      }
      if (inChannel != null) inChannel.close();
      if (outChannel != null) outChannel.close();
      if (in.length()==out.length()) success=true;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return success;
  }
  
  /**
   * Counts for a DNA sequence string the number of xMeres in this string.
   * E.g., for dimeres (xMeres=2), returns an array of 16 objects, containing
   * the number of AA, AC, AG,... dinucleotides. See {@link #DNA2Num(char)}
   * for array position and sequence link.
   * @see #DNA2Num(String)
   * @param sequence
   * @param xMeres
   * @return
   */
  public static int[] countNucleotides(String sequence, int xMeres) {
    int counts[] = new int[(int) Math.pow(4, xMeres)];
    for (int i=0; i<sequence.length()-xMeres+1; i++)
      counts[DNA2Num(sequence.substring(i, i+xMeres))]++;
    return counts;
  }
  
  /**
   * Cut number at dot. E.g. 1.68 => 1
   * In contrary, decimal format "#" would return 2!
   * @param d
   * @return
   */
  public static String cut(double d) {
    String s = Double.toString(d);
    int ep = s.indexOf(".");
    if (ep<1) ep = s.length();
    return s.substring(0, ep);
  }
  
  /**
   * Divide each element in the first array by the 
   * corresponding element in the second array (same indicies).
   * @param arr1
   * @param arr2
   * @return
   */
  public static double[][] divide (double[][] arr1, double[][] arr2) {
    double[][] ret = new double[arr1.length][];
    for (int i=0; i<arr1.length; i++){
      ret[i] = new double [arr1[i].length];
      for (int j=0; j<arr1[i].length; j++){
        if (arr2[i][j]==0)
          ret[i][j]=Double.NaN;
        else
          ret[i][j] = arr1[i][j]/arr2[i][j];
      }
    }
    return ret;
  }
  
  /**
   * Divide each element in the first array by the 
   * corresponding element in the second array (same indicies).
   * @param arr1
   * @param arr2
   * @return
   */
  public static double[][] divide (int[][] arr1, int[][] arr2) {
    double[][] ret = new double[arr1.length][];
    for (int i=0; i<arr1.length; i++){
      ret[i] = new double [arr1[i].length];
      for (int j=0; j<arr1[i].length; j++){
        if (arr2[i][j]==0)
          ret[i][j]=0;
        else
          ret[i][j] = (double)arr1[i][j]/arr2[i][j];
      }
    }
    return ret;
  }
  
  /**
   * Return a unique number for a DNA char.
   * @see #Num2DNA(int) for the reverse function.
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
   * @see #Num2DNA(int, int) for the reverse function.
   * @param a DNA sequence
   * @return See example above.
   **/
  public static int DNA2Num(String a) {
    int ret = 0;
    char[] arr = reverse(a).toCharArray();
    for(int i=0; i<arr.length; i++)
      ret += (DNA2Num(arr[i])) * Math.pow(4, (i));
    return ret;
  }
  
  /**
   * Ensures that path ends with a slash (for folder processing).
   * @param path
   */
  public static String ensureSlash(String path) {
    if (!path.endsWith("\\") && !path.endsWith("/"))
      if (path.contains("/")) path+="/";
      else if (path.contains("\\")) path+="\\";
      else path+="/";
    return path;
  }
  
  /**
   * 
   * @param s
   * @return
   */
  public static String firstUppercase(String s) {
    if (s==null) return null;
    s = s.trim().toLowerCase();
    if (s.length()==0) return "";
    return Character.toString(s.charAt(0)).toUpperCase() + s.substring(1);
  }
  
  /**
   * Parse unsigned integer number from string.
   * E.g. toParse="X134HA",startAtPos=1
   * would return '134'.
   * @param startAtPos
   * @param toParse
   * @return -1 if failed, else the parsed number.
   */
  public static int getNumberFromString(int startAtPos, String toParse) {
    int i = startAtPos;
    if (i<0 || i>=toParse.length()) return -1;
    
    String ret = "";
    while (i<toParse.length() && Character.isDigit(toParse.charAt(i)))
      ret += toParse.charAt(i++);
    
    return Integer.parseInt(ret);
  }
  
  /**
   * Parse unsigned integer number reverse. Starts at (startAtPos -1).
   * E.g. "Hall-24-xyz" startAtPos = indexOf(x) Return value is 24.
   * WARNING, startPos = indexOf(4) would return 2 !
   * @param startAtPos
   * @param toParse
   * @return -1 if failed, else the parsed number.
   */
  public static int getNumberFromStringRev(int startAtPos, String toParse) {
    return Integer.parseInt(getNumberFromStringRevAsString(startAtPos, toParse));
  }
  
  /**
   * @see #getNumberFromStringRev(int, String)
   * @param startAtPos
   * @param toParse
   * @return number as String (with eventually leading zeros).
   */
  public static String getNumberFromStringRevAsString(int startAtPos, String toParse) {
    int i = startAtPos;
    if (i<=0 || i>toParse.length()) return "-1";
    
    StringBuffer ret = new StringBuffer();
    while (i>0 && Character.isDigit(toParse.charAt(i-1)))
      ret.append( toParse.charAt(--i) );
    
    return ret.length()>0?ret.reverse().toString():"-1";
  }
  
  /**
   * Funzt nur fuer positive, natuerliche Zahlen!
   */
  public static int getNumberFromString(String behindLastIndexOfString, String toParse) {
    int i = toParse.lastIndexOf(behindLastIndexOfString)+1;
    return getNumberFromString(i, toParse);
  }
  
  /**
   * Given the miliseconds elapsed, returns a formatted time string up to a max deph of 3.
   * e.g. "16h 4m 4s" or "2d 16h 4m" or "4s 126dms" 
   * @param miliseconds
   * @return
   */
  public static String getTimeString(long miliseconds) {
    double seconds = (miliseconds/1000.0);
    double minutes = (seconds/60.0);
    double hours = (minutes/60.0);
    double days = hours/24;
    
    String ret;
    if (days>=1) {
      ret = cut(days) + "d " + cut(hours%24.0)  + "h " + cut(minutes%60) + "m";
    } else if (hours>=1) {
      ret = cut(hours%24.0)  + "h " + cut(minutes%60) + "m " + cut(seconds%60) + "s";
    } else if (minutes>=1) {
      ret = cut(minutes%60) + "m " + cut(seconds%60) + "s " + cut(miliseconds%1000.0) + "ms";
    } else if (seconds>=1) {
      ret = cut(seconds%60) + "s " + cut(miliseconds%1000.0) + "ms";
    } else {
      ret = cut(miliseconds%1000.0) + "ms";
    }
    return ret;
  }
  
  /**
   * The same as {@link #getTimeString(long)} but a little less detail,
   * making the resulting string more "pretty".
   * @param miliseconds
   * @return
   * @see #getTimeString(long)
   */
  public static String getPrettyTimeString(long miliseconds) {
    double seconds = (miliseconds/1000.0);
    double minutes = (seconds/60.0);
    double hours = (minutes/60.0);
    double days = hours/24;
    
    String ret;
    if (days>=1) {
      ret = cut(days) + "d " + cut(hours%24.0)  + "h";
    } else if (hours>=1) {
      ret = cut(hours%24.0)  + "h " + cut(minutes%60) + "m";
    } else if (minutes>=2) {
      ret = cut(minutes%60) + "m";
    } else if (minutes>=1) {
      ret = cut(minutes%60) + "m " + cut(seconds%60) + "s";
    } else if (seconds>=1) {
      ret = cut(seconds%60) + "s";
    } else {
      ret = cut(miliseconds%1000.0) + "ms";
    }
    return ret;
  }
  
  /** Nicht ganz korrekt da auch 4.345,2.1 als nummer erkannt wird, aber das reicht mir so. **/
  public static boolean isNumber(String s, boolean onlyDigits) {
    if (s.trim().length()==0) return false;
    char[] a = s.trim().toCharArray();
    boolean atLeastOneDigit=false;
    for (int i=0; i< a.length; i++) {
      if (!atLeastOneDigit && Character.isDigit(a[i])) atLeastOneDigit = true;
      
      if (onlyDigits){
        if (Character.isDigit(a[i])) continue; else return false;
      } else {
        if (Character.isDigit(a[i])) continue;
        else if (i==0 && a[i]=='-') continue;
        else if (a[i]=='.' || a[i]==',') continue;
        else if (a[i]=='E' || a[i]=='e') {
          if (i+1< a.length) {
            // Detect strings like "2.8E+01" or "2.8E-01"
            if (a[i+1]=='+' || a[i+1]=='-') i+=1;
          }
          continue;
        }
        //if (a[i]=='-' || a[i]=='.' || a[i]==',' || a[i]=='E' || a[i]=='e') continue;
        return false;
      }
    }
    if (!atLeastOneDigit) return false; // Only "-" or "..." is no number.
    return true;
  }
  
  /**
   * Returns, wether the containedString does occur somewhere in containingLine as a word.
   * E.g. containingLine = "12.ENOA_MOUSE ABC". "NOA_MOUSE" is no word, but "ENOA_MOUSE"
   * is a word.
   * The function could also be called "containsWord".
   * @param containingLine
   * @param containedString
   * @return true if and only if containedString is contained in containingLine and is not
   * sourrounded by a digit or letter.
   */
  public static boolean isWord(String containingLine, String containedString) {
    return isWord(containingLine, containedString, false);
  }
  
  /**
   * Returns, wether the containedString does occur somewhere in containingLine as a word.
   * E.g. containingLine = "12.ENOA_MOUSE ABC". "NOA_MOUSE" is no word, but "ENOA_MOUSE"
   * is a word.
   * @param containingLine
   * @param containedString
   * @param ignoreDigits - if false, digits will be treated as part of a word (default case).
   * If true, digits will be treated as NOT being part of a word (a word splitter, like a space).
   * @return true if and only if containedString is contained in containingLine as word.
   */
  public static boolean isWord(String containingLine, String containedString, boolean ignoreDigits) {
    // Check if it's a word
    int pos = -1;
    while (true) {
      if (pos+1>=containingLine.length()) break;
      pos = containingLine.indexOf(containedString, pos+1);
      if (pos<0) break;
      
      boolean leftOK = true;
      if (pos>0) {
        char l = containingLine.charAt(pos-1);
        if ((Character.isDigit(l) && !ignoreDigits) || Character.isLetter(l)) leftOK = false;
      }
      boolean rechtsOK = true;
      if (pos+containedString.length()<containingLine.length()) {
        char l = containingLine.charAt(pos+containedString.length());
        if ((Character.isDigit(l) &&!ignoreDigits) || Character.isLetter(l)) rechtsOK = false;
      }
      
      if (rechtsOK && leftOK) return true;
    }
    return false;
  }
  
  /**
   * @param containingLine
   * @param startPosition
   * @param ignoreDigits
   * @return the next word in <code>containingLine</code>, starting from <code>startPosition</code>
   * only including letters and if <code>ignoreDigits</code> is false, also digits.
   */
  public static String getWord(String containingLine, int startPosition, boolean ignoreDigits) {
    // get next word
    int pos = startPosition;
    if (pos<0) return null;
    
    StringBuffer ret = new StringBuffer();
    while (pos<=containingLine.length()) {
      char c = containingLine.charAt(pos);
      if (Character.isLetter(c)) ret.append(c);
      else if (!ignoreDigits && Character.isDigit(c)) ret.append(c);
      else {
        // End of word
        break;
      }
      pos++;
    }
    return ret.toString();
  }
  
  /**
   * Returns the memory, that is currently allocated by the JVM in bytes.
   * Divide it by /1024.0/1024.0 the get the amount in MB.
   * @return
   */
  public double getMemoryUsed() {
    /* Auf 2 Stellen hinter komma in MB:
     * double memory_used = Math.round(((Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/1024.0/1024.0*100.0))/100.0;
     */
    return (((Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory()) ));
  }
  
  /**
   * Returns the maxmimal available memory (set by Xmx, or long.maxValue if no limit is given) to the JVM.
   * Divide it by /1024.0/1024.0 the get the amount in MB.
   * @return
   */
  public double getMemoryMaximum() {
    /* Auf 2 Stellen hinter komma in MB:
     * double memory_max = Math.round((Runtime.getRuntime().maxMemory()/1024.0/1024.0*100.0))/100.0;
     */
    return ((Runtime.getRuntime().maxMemory() ));
  }
  
  /**
   * Reverse of {@link #DNA2Num(char)}. Returns the nucleotide for a number.
   * @see #Num2DNA(int, int) to get the reverse of {@link #DNA2Num(String)}!
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
   * Reverse of {@link #DNA2Num(String)}
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
  
  /**
   * Calculates and returns the variance of the given list of double values
   * 
   * @param d the list of double values
   * @return
   */
  public static double variance(double[] d) {
    double mean = average(d);
    return variance(d, mean);
  }
  
  /**
   * Calculates and returns the variance of the given list of double values.
   * This version of the method also takes the precalculated mean of the values
   * as parameter to prevent redundant calculations.
   * 
   * @param d the list of double values
   * @param mean the mean of the double values
   * @return
   */
  public static double variance(double[] d, double mean) {
    return variance(d, mean, 2);
  }
  
  /**
   * Calculates and returns the variance of the given list of double values.
   * This version of the method also takes the precalculated mean of the values
   * as parameter to prevent redundant calculations.
   * 
   * @param d the list of double values
   * @param mean the mean of the double values
   * @param power standard variance has power 2. Other variances can have other
   * powers.
   * @return
   */
  public static double variance(double[] d, double mean, double power) {
    double sum = 0.0;
    if( d.length <= 1 ) return sum;
    
    for (int i = 0; i < d.length; i++) {
      sum += Math.pow(d[i] - mean, power);
    }
    return sum / (d.length - 1);
  }
  
  /**
   * Usefull for parsing command-line arguments.
   * @param args - Command-line arguments.
   * @param searchForCommand - Command to search for
   * @param hasArgument - Has the command an argument? If yes, the Argument will be returned.
   * @return The Argument, if {@link hasArgument} is set.
   *         Otherwise: "true" if command is available. "false" if not.
   * @throws Exception "Missing argument.". If that's the case.
   */
  public static String parseCommandLine(String[] args, String searchForCommand, boolean hasArgument) throws Exception {
    searchForCommand = searchForCommand.replace("-", "").trim();
    for (int i=0; i<args.length; i++) {
      if (args[i].replace("-", "").trim().equalsIgnoreCase(searchForCommand)) {
        if (hasArgument) {
          if (i==(args.length-1)) throw new Exception ("Missing argument.");
          else return args[i+1];
        } else {
          return "true";
        }
      }
    }
    
    if (!hasArgument) return "false";
    return ""; // Don't return "false". May interfere with hasArgument
  }
  
  /**
   * Outputs the Minimum, Maximum value of the array and the number of
   * {@link Double#NaN} and Infinity values.
   * @param arr
   */
  public static void printMinMaxInfNaN(double[] arr) {
    double min = Double.MAX_VALUE; double max = Double.MIN_VALUE; int nan=0; int inf=0;
    for (double v: arr) {
      if (Double.isInfinite(v)) {inf++; continue;}
      if (Double.isNaN(v)) {nan++; continue;}
      if (v<min) min=v;
      if (v>max) max=v;
    }
    System.out.println("Min: " + min + "\t Max:" + max + "\t Infinity:" + inf + "\t NaN:" + nan);
  }
  
  /**
   * 
   * @param c
   * @param times
   * @return
   */
  public static StringBuffer replicateCharacter(char c, int times) {
    StringBuffer s = new StringBuffer();
    for (int i=0; i<times; i++)
      s.append(c);
    return s;
  }
  
  /**
   * 
   * @param ch
   * @param times
   * @return
   */
  public static String replicateCharacter(String ch, int times) {
    String retval = "";
    for (int i=0; i<times;i++){
      retval += ch;
    }
    return retval;
  }
  
  /**
   * Returns the reverse of a string.
   * @param s
   * @return
   */
  public static String reverse(String s) {
    StringBuffer a = new StringBuffer(s);
    return a.reverse().toString();
  }
  
  /**
   * 
   * @param zahl
   * @param stellen
   * @return
   */
  public static double round(double zahl, int stellen) {
    double d = Math.pow(10, stellen);
    return Math.round( zahl * ((long)d) ) / d;
  }
  
  /**
   * Checks wether all elements of the given array are null.
   * @param <T>
   * @param arr
   * @return
   */
  public static <T> boolean allElementsAreNull(T[] arr) {
    if (arr == null) return true;
    
    for (T e: arr) {
      if (e!=null) return false;
    }
    
    return true;
  }
  
  /**
   * Shutdown the computer. System independent implementation - should work on windows
   * and unix operating systems.
   * @return
   */
  public static boolean shutdownSystem() {
    boolean isWindows = (System.getProperty("os.name").toLowerCase().contains("windows"));
    
    String sysDir = System.getProperty("os.home");
    if (sysDir==null) sysDir = "";
    sysDir = sysDir.trim();
    if (sysDir==null || sysDir.length()==0 && isWindows) sysDir = System.getenv("WinDir").trim();
    if (sysDir.length()!=0 && !sysDir.endsWith("/") && !sysDir.endsWith("\\"))
      sysDir+=File.separator;
    if (isWindows&&sysDir.length()!=0) sysDir += "system32\\";
    
    Runtime run = Runtime.getRuntime();
    if (isWindows) sysDir += "shutdown.exe"; // -s -c "TimeLogger shutdown command." (-f)
    else {
      // Unter linux mit "which" Rscript suchen
      try {
        Process pr = run.exec("which shutdown");
        pr.waitFor();
        pr.getOutputStream();
        
        // read the child process' output
        InputStreamReader r = new InputStreamReader(pr.getInputStream());
        BufferedReader in = new BufferedReader(r);
        String line = in.readLine(); // eine reicht
        if (!line.toLowerCase().contains("no shutdown") && !line.contains(":"))
          sysDir = line;
      } catch(Exception e) {}
      if (sysDir.length()!=0 && !sysDir.endsWith("/") && !sysDir.endsWith("\\"))
        sysDir+=File.separator;
      sysDir += "shutdown";
    }
    
    boolean successValue=false;
    try {
      String dir = sysDir.substring(0, sysDir.lastIndexOf(File.separator));
      String command = sysDir.substring(sysDir.lastIndexOf(File.separator)+1);
      
      String[] commandToRun;
      if (isWindows) commandToRun = new String[] {command, "-s", "-c", "\"TimeLogger shutdown command.\""}; // -f
      else commandToRun = new String[] {command, "-h", "now"}; // Linux
      
      try { // Try to execute in input dir.
        //Process pr = run.exec(cmd, null, new File(inputFolder));
        Process pr = run.exec(commandToRun, null, new File(dir));
        pr.waitFor();
        successValue =(pr.exitValue()==0);
        
      } catch(Exception e) {
        e.printStackTrace();
        try {
          run.exec(new String[] {sysDir}); // don't wait as it may not terminate...
        } catch(Exception ex2) {
          run.exec(new String[]{ "shutdown"}); // don't wait as it may not terminate...
        }
      }
    } catch (Exception e) {e.printStackTrace();}
    
    return successValue;
  }
  
  /**
   * Load a serializable object.
   * @param file
   * @return the loaded object or null if it failed.
   */
  public static Object loadObject(File file) {
    try {
      FileInputStream fileIn = new FileInputStream(file);
      BufferedInputStream bIn = new BufferedInputStream(fileIn);
      Object o = loadObject(bIn);
      fileIn.close();
      return o;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
  
  /**
   * Load a serializable object.
   * Does NOT buffer the stream. Make sure to pipe your
   * stream though a BufferedStream for more performance
   * (e.g. BufferedInputStream). 
   * @param inn
   * @return the loaded object or null if it failed.
   */
  public static Object loadObject(InputStream inn) {
    try {
      ObjectInputStream in = new ObjectInputStream(inn);
      Object ret = in.readObject();
      in.close();
      inn.close();
      return ret;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
  
  /**
   * Load a serializable object.
   * Does NOT buffer the stream. Make sure to pipe your
   * stream though a BufferedStream for more performance
   * (e.g. BufferedInputStream). 
   * @param inn
   * @return the loaded object or null if it failed.
   * @throws IOException 
   * @throws ClassNotFoundException 
   */
  public static Object loadObjectAndThrowExceptions(InputStream inn) throws IOException, ClassNotFoundException {
    ObjectInputStream in = new ObjectInputStream(inn);
    Object ret = in.readObject();
    in.close();
    inn.close();
    return ret;
  }
  
  /**
   * Load a serializable object.
   * @param filename
   * @return the loaded object or null if it failed.
   */
  public static Object loadObject(String filename) {
    return loadObject(new File(filename));
  }
  
  /**
   * Load a serializable object from a gzipped file.
   * @param file (String, File, FileDescriptor or InputStream).
   * @return the loaded object or null if it failed.
   */
  public static Object loadGZippedObject(Object infile) {
    Object ret = null;
    if (infile==null) return ret;
    
    try {
      InputStream fileIn;
      if (infile instanceof String) {
        fileIn = new FileInputStream((String)infile);
      } else if (infile instanceof File) {
        fileIn = new FileInputStream((File)infile);
      } else if (infile instanceof FileDescriptor) {
        fileIn = new FileInputStream((FileDescriptor)infile);
      } else if (infile instanceof InputStream) {
        fileIn = ((InputStream)infile);
      } else {
        throw new IOException("Unsupported input file object: "+infile.getClass().getName());
      }
      BufferedInputStream bIn = new BufferedInputStream(fileIn);
      
      GZIPInputStream gzIn = new GZIPInputStream(bIn);
      
      ret = loadObject((InputStream)gzIn);
      
      bIn.close();
      if (fileIn instanceof Closeable) ((Closeable)fileIn).close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    return ret;
  }
  
  /**
   * Load a serializable object from a gzipped or normal file
   * (is infered automatically).
   * @param file (String, File, FileDescriptor or InputStream).
   * @return the loaded object or null if it failed.
   * @throws IOException 
   */
  public static Object loadObjectAutoDetectZIP(Object serializedFile) throws IOException {
    
    Object ret = null;
    if (serializedFile==null) return ret;
    
    // Create input stream
    InputStream fileIn;
    if (serializedFile instanceof String) {
      fileIn = OpenFile.searchFileAndGetInputStream((String)serializedFile);//new FileInputStream((String)serializedFile);
    } else if (serializedFile instanceof File) {
      fileIn = new FileInputStream((File)serializedFile);
    } else if (serializedFile instanceof FileDescriptor) {
      fileIn = new FileInputStream((FileDescriptor)serializedFile);
    } else if (serializedFile instanceof InputStream) {
      fileIn = ((InputStream)serializedFile);
    } else {
      throw new IOException("Unsupported input file object: "+serializedFile.getClass().getName());
    }
    
    // Create buffered Stream to be able to read and reset the magic bytes
    BufferedInputStream bin = new BufferedInputStream(fileIn);
    bin.mark(20); // Set mark to be able to reset if magic bytes don't match.
    
    // Look if it is an GZipped Object
    InputStream in=null;
    try {
      in = new GZIPInputStream(bin);
    } catch (IOException e) {
      // java.io.IOException: Not in GZIP format
      in=null;
      bin.reset();
    }
    
    // Take directly the source input stream, if it is no GZstream
    if (in==null) in = bin;
    
    try {
      ret = loadObjectAndThrowExceptions(in);
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      ret=null;
    }
    
    bin.close();
    fileIn.close();
    
    return ret;
  }
  
  
  /**
   * Saves and GZipps a serializable object.
   * @param filename - the file to save the object to.
   * Please make sure by yourself, that this filename ends with ".gz".
   * @param obj - the object to save.
   * @return true if and only if saving was succesfull.
   */
  public static boolean saveGZippedObject(String filename, Object obj) {
    try {
      if (filename.contains("/")) {
        new File(filename.substring(0, filename.lastIndexOf('/'))).mkdirs();
      }
    } catch (Throwable t) {};
    try {
      FileOutputStream fileOut = new FileOutputStream(filename);
      GZIPOutputStream gzOut = new GZIPOutputStream(fileOut);
      ObjectOutputStream out = new ObjectOutputStream(gzOut);
      out.writeObject(obj);
      out.close();
      gzOut.close();
      fileOut.close();
      return true;
    } catch(FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return false;
  }
  
  /**
   * The same as {@link #saveGZippedObject(String, Object)} but does not catch
   * any errors.
   * @param filename
   * @param obj
   * @throws IOException 
   */
  public static void saveGZippedObjectAndThrowErrors(String filename, Object obj) throws IOException {
    FileOutputStream fileOut = new FileOutputStream(filename);
    GZIPOutputStream gzOut = new GZIPOutputStream(fileOut);
    ObjectOutputStream out = new ObjectOutputStream(gzOut);
    out.writeObject(obj);
    out.close();
    gzOut.close();
    fileOut.close();
  }
  
  
  /**
   * Saves a serializable object.
   * @param filename - the file to save the object to.
   * @param obj - the object to save.
   * @return true if and only if saving was succesfull.
   */
  public static boolean saveObject(String filename, Object obj) {
    try {
      FileOutputStream fileOut = new FileOutputStream(filename);
      ObjectOutputStream out = new ObjectOutputStream(fileOut);
      out.writeObject(obj);
      out.close();
      fileOut.close();
      return true;
    } catch(FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return false;
  }

  /**
   * Returns the relative path, in which this file is contained.
   * E.g. if fn is "res/home.dat", the return value will be "res/".
   * 
   * It is NOT recommended to use "new File(X).getParent()" because
   * the path can be inside a jar and no real path.
   * 
   * @param fn - any input file and path combination.
   * @return
   */
  public static String getPath(String fn) {
    if (fn.contains("/")) {
      // Exclisive file separator char, can be used in any os.
      return fn.substring(0, fn.lastIndexOf("/")+1);
    } else if (System.getProperty("file.separator").equals("\\") &&
        fn.contains("\\")) {
      // Unix filesystems can use \ to escape e.g. a whitespace. So
      // this one should only be parsed when it is a valud file separator
      return fn.substring(0, fn.lastIndexOf("\\")+1);
    } else {
      return "";
    }
  }

  /**
   * If the given class 'c' is inside a JAR file, this function returns the name
   * of the jar file. Else, if it is a class file, outside of a JAR file, an empty
   * string is returned.
   * If an error occurs, null is returned.
   * @param c - Class to check.
   * @return String (Jarfile name) if Class is in jar, EmptyString if not, null if
   * something went wrong.
   */
  public static String getNameOfJar(Class<?> c) {
    try {
      File moduleFile = new File(c.getProtectionDomain().getCodeSource().getLocation().toURI());
      if (moduleFile.isFile())
        return moduleFile.getName();
      else
        return "";
    } catch (Throwable t) {
      // Not important
    }
    return null;
  }
  
  /**
   * Returns true if and only if the given class is any instance of
   * an integer (german: "Ganzzahlig"). This includes:<ul>
   * <li>AtomicInteger</li>
   * <li>AtomicLong</li>
   * <li>BigInteger</li>
   * <li>Byte</li>
   * <li>Integer</li>
   * <li>Long</li>
   * <li>Short</li>
   * </ul>
   * @param clazz
   * @return
   */
  public static boolean isInteger(Class<?> clazz) {
  	if (!(Number.class.isAssignableFrom(clazz))) return false;
  	if (clazz.equals(AtomicInteger.class)) return true;
  	else if (clazz.equals(AtomicLong.class)) return true;
  	else if (clazz.equals(BigInteger.class)) return true;
  	else if (clazz.equals(Byte.class)) return true;
  	else if (clazz.equals(Integer.class)) return true;
  	else if (clazz.equals(Long.class)) return true;
  	else if (clazz.equals(Short.class)) return true;
  	
  	return false;
  }
  
  /**
   * Performs a deep compareTo on two arrays. If the array contains other arrays as elements,
   * the compareTo is based on their contents and so on, ad infinitum. It is therefore
   * unacceptable to invoke this method on an array that contains itself as an element,
   * either directly or indirectly through one or more levels of arrays.
   * The behavior of such an invocation is undefined.
   * 
   * For any two arrays a and b such that Arrays.deepEquals(a, b), it is also the case that
   * Arrays.deepCompareTo(a) == Arrays.deepCompareTo(b). 
   * 
   * @param arr1 - the first array to comapre to the second one.
   * @param arr2 - the second array to compare to the first one.
   * @return a value, <0 if the first array is "lower" or null, compared to the second one.
   * A value >0 if it is the other way round and 0 if both arrays are deepEqual.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static int deepCompareTo(Object[] arr1, Object[] arr2) {
    if (arr1==null && arr2==null) return 0;
    if (arr1==null) return -1;
    if (arr2==null) return 1;
    
    int min = Math.min(arr1.length, arr2.length);
    for (int i=0; i<min; i++) {
      int ret=0;
      
      // Look if the current object is smaller, greater or eqal
      if (arr1[i]==null && arr2[i]==null) ret =0;
      else if (arr1[i]==null) ret = -1;
      else if (arr2[i]==null) ret = 1;
      
      else if (arr1[i].getClass().isArray() && arr2[i].getClass().isArray()) {
        // Deep array comparison
        ret = deepCompareTo((Object[])arr1[i], (Object[])arr2[i]);
        
      } else if (arr1[i] instanceof Comparable && arr2[i] instanceof Comparable) {
        // Comparables
        ret = ((Comparable)arr1[i]).compareTo(((Comparable)arr2[i]));
        
      } else if (arr1[i] instanceof Number && arr2[i] instanceof Number ) {
        // Numbers
        double diff = (((Number)arr2[i]).doubleValue()-((Number)arr1[i]).doubleValue());
        ret = (int) diff;
        if (ret==0 && !((Number)arr2[i]).equals((Number)arr1[i])) ret = diff<0?-1:1;
        
      } else {
        // String based comparison
        ret = arr1[i].toString().compareTo(arr2[i].toString());
        
      }
      
      // Evaluate results
      if (ret!=0) return ret;
      
      
    }
    
    // They are equal.
    return 0;
  }

  /**
   * @param old_m
   * @param old_cgs
   * @param m
   * @param cgs
   * @return the weighted mean.
   */
  public static double weightedAverage(double val, double weight, double val2, double weight2) {
    return (val*weight/(weight2+weight)) +  (val2 * weight2/(weight2+weight));
  }

  /**
   * This function creates a new file / overwrites an existing one (without warning) and puts
   * the given content into the file. The required directory structure to write the file
   * will be created.
   * @param filename - File path and name.
   * @param content - content to write to file.
   * @throws IOException 
   */
  public static void writeFile(String filename, String content) throws IOException {
    // Create directory
    File parentFolder = new File(new File(filename).getParent());
    if (!parentFolder.exists()) {
      parentFolder.mkdirs();
    }
    
    // Write file
    FileWriter fw = new FileWriter(filename);
    BufferedWriter writer = new BufferedWriter (fw);
    writer.write(content);
    writer.close();
    fw.close();
  }
  
  /**
   * Copy a stream to a file. This enables e.g. copying of
   * resources inside jar-files to files.
   * 
   * Note: It's a good idea to buffer the input stream.
   * 
   * @param is - resource to read
   * @param f - file to write
   * @return true, if everything went fine.
   */
  public static void copyStream(final InputStream is, final File f) throws IOException {
    
      OutputStream os = new BufferedOutputStream(new FileOutputStream(f));
      // Copy from input to output-stream.
      byte[] buffer = new byte[4096];
      int length;
      while ((length = is.read(buffer)) > 0) {
          os.write(buffer, 0, length);
      }
      os.close();
      is.close();

  }

  /**
   * @param values
   * @param onlyDigits
   * @return
   */
  public static boolean isNumber(Iterable<?> values, boolean onlyDigits) {
    boolean ret = true;
    for (Object val: values) {
      try {
        //if (onlyDigits) Long.parseLong(val);
        //else Double.parseDouble(val);
        if (!(Number.class.isAssignableFrom(val.getClass()))) {
          ret = isNumber(val.toString(), onlyDigits);
        }
      } catch (NumberFormatException e) {
        ret = false;
        break;
      }
    }
    return ret;
  }
  
  /**
   * Linear normalize the given value to a distribution from 0 to 1.
   * @see #normalize(double, double, double, double, double)
   * @param value
   * @param minValue
   * @param maxValue
   * @return normalized value
   */
  public static double normalize(double value, double minValue, double maxValue) {
    return normalize(value, minValue, maxValue, 0, 1);
  }
  
  /**
   * Linear normalize the given value to a new distribution.
   * @param value value to normalize
   * @param minValue minimum value of your list
   * @param maxValue maximum value of your list
   * @param targetMinValue desired new minimum value
   * @param targetMaxValue desired new maximum value
   * @return
   */
  public static double normalize(double value, double minValue, double maxValue, double targetMinValue, double targetMaxValue) {
    // Shift new distrubution from 0 to max.
    targetMaxValue-=targetMinValue;
    
    // shift value from old distrubition to 0 to old_max
    double v = value-minValue;
    
    // Normalize v from old_max to new_max
    v*=((targetMaxValue)/(maxValue-minValue));
    
    // Add the new mimum to get to the new distribution
    v+=targetMinValue;
    
    return v;
  }
  
  /**
   * Binomial distribution (German: Binomialverteilung oder "n über m").
   * O(n²) implementation for small values. So better use it with a cache...
   * @param n
   * @param m
   * @return
   */
  public static long binom(int n, int m) {
    long[] b = new long[n+1];
    b[0]=1;
    for (int i=1; i<=n; i++) {
      b[i]=1;
      for (int j=i-1; j>0; j--) {
        b[j]+=b[j-1];
      }
    }
    return b[m];
  }
  
  /**
   * binomialCoefficient that also can calculate bigger values, using a {@link BigInteger}.
   * @param n
   * @param k
   * @return
   */
  public static BigInteger binomialCoefficient(int n, int k){
    return binomialCoefficient(BigInteger.valueOf(n), BigInteger.valueOf(k));
  }
  /**
   * binomialCoefficient that also can calculate bigger values, using a {@link BigInteger}.
   * @param n
   * @param k
   * @return
   */
  public static BigInteger binomialCoefficient(BigInteger n, BigInteger k){
    
    BigInteger n_minus_k=n.subtract(k);
    if(n_minus_k.compareTo(k)<0){
      BigInteger temp=k;
      k=n_minus_k;
      n_minus_k=temp;
    }
    
    BigInteger numerator=BigInteger.ONE;
    BigInteger denominator=BigInteger.ONE;
    
    for(BigInteger j=BigInteger.ONE; j.compareTo(k)<=0; j=j.add(BigInteger.ONE)){
      numerator=numerator.multiply(j.add(n_minus_k));
      denominator=denominator.multiply(j);
      BigInteger gcd=numerator.gcd(denominator);
      numerator=numerator.divide(gcd);
      denominator=denominator.divide(gcd);
    }
    
    return numerator;
  }
  
  /**
   * The hypergeometric distribution, calculated as<br/>
   * <img src="http://upload.wikimedia.org/math/7/e/c/7ecd507bdc33dc636fdae2000d9b31fb.png"/>
   * 
   * @see <a href="http://en.wikipedia.org/wiki/Hypergeometric_distribution">Wikipedia</a>.
   * 
   * @param N total number of objects in urn
   * @param m number of white objects in urn
   * @param n number objects to draw without replacement
   * @param k number of objects to calculate the probability that they are white
   * @return
   */
  public static double hypergeometric_distribution(int N, int m, int n, int k) {
    // Values are getting really really big in here!
    BigDecimal zaehler = new BigDecimal(binomialCoefficient(m,k).multiply(binomialCoefficient(N-m,n-k)));
    BigDecimal nenner  = new BigDecimal(binomialCoefficient(N,n));
    return (zaehler.divide(nenner, 20, RoundingMode.HALF_UP)).doubleValue();
  }
  
  
  /**
   * Calculates a pValue for an enrichment significance (e.g., gene set enrichments
   * in pathways).
   * This is a "Hypergeometric Test".
   * @see <a href="http://nar.oxfordjournals.org/content/37/19/e131.full#disp-formula-1">Publication: "SubpathwayMiner: a software package for flexible identification of pathways"</a>
   * @param m Total number of genes in the genome.
   * @param n Total number of genes in the input set (e.g., in the input gene list)
   * @param t Total number of marked genes (e.g., genes in the current pathway)
   * @param r Number of genes from t that are in n. (e.g., genes from the input set that are in the current pathway).
   * @return
   */
  public static double enrichment_significance(int m, int n, int t, int r) {
    double p = 0;
    
    for (int x=0; x<r; x++) {
      p+=hypergeometric_distribution(m,t,n,x);
    }
    
    return 1-p;
  }


  /**
   * 
   * @param <T>
   * @param values
   * @param onlyDigits
   * @return
   */
  public static<T> boolean isNumber(T[] values, boolean onlyDigits) {
    boolean ret = true;
    for (Object val: values) {
      try {
        //if (onlyDigits) Long.parseLong(val);
        //else Double.parseDouble(val);
        if (!(Number.class.isAssignableFrom(val.getClass()))) {
          ret = isNumber(val.toString(), onlyDigits);
        }
      } catch (NumberFormatException e) {
        ret = false;
        break;
      }
    }
    return ret;
  }

  /**
   * Parses a String that contains multiple numbers (only digits) and returns
   * an array of all those numbers.
   * @param stringWithMultipleNumbers e.g. "HSA: 1026(CDKN1A)\nPTR: 747442(CDKN1A)"
   * @return in the example above, int[]{1026, 1, 747442, 1}
   */
  public static List<Integer> getNumbersFromString(String stringWithMultipleNumbers) {
    return getNumbersFromString(stringWithMultipleNumbers,null,null);
  }
  
  /**
   * Parses a String that contains multiple numbers (only digits) and returns
   * an array of all those numbers.
   * @param stringWithMultipleNumbers e.g. "HSA: 1026(CDKN1A)\nPTR: 747442(CDKN1A)"
   * @param leftOfEachNumber a string that must be left of each number
   * @param rightOfEachNumber a string that must be right of each number
   * @return in the example above, if (<code>leftOfEachNumber</code>=" ") int[]{1026, 747442}
   */
  public static List<Integer> getNumbersFromString(String stringWithMultipleNumbers, String leftOfEachNumber, String rightOfEachNumber) {
    List<Integer> ret = new ArrayList<Integer>();
    StringBuffer curNum = new StringBuffer();
    char[] cArr = stringWithMultipleNumbers.toCharArray();
    boolean jumpToNextNumber = true;
    for (int i=0; i<cArr.length; i++) {
      if (jumpToNextNumber) {
        if (leftOfEachNumber!=null) i = stringWithMultipleNumbers.indexOf(leftOfEachNumber,i);
        if (i<0) break;
        else i+=leftOfEachNumber!=null?leftOfEachNumber.length():0;
        if (i>=cArr.length) break;
      }
      
      char c = cArr[i];
      if (Character.isDigit(c)) {
        jumpToNextNumber=false;
        // append to current number
        curNum.append(c);
      } else if (curNum.length()>0) {
        jumpToNextNumber = true;
        String num = curNum.toString();
        curNum = new StringBuffer();
        if (rightOfEachNumber!=null) {
          if (i+rightOfEachNumber.length()>cArr.length) break;
          if (!new String(cArr, i, rightOfEachNumber.length()).equals(rightOfEachNumber)) {
            continue;
          }
        }
        // Add to list
        ret.add(Integer.parseInt(num));
      }
    }
    if (curNum.length()>0 && rightOfEachNumber==null) {
      ret.add(Integer.parseInt(curNum.toString()));
    }
    
    return ret;
  }

  /**
   * @param values any iterable number collection.
   * @return minimum value in <code>values</code> or {@link Double#NaN}
   * if <code>values</code> is <code>null</code> or contains no numbers.
   */
  public static <T extends Number> double min(Iterable<T> values) {
    if (values == null) return Double.NaN;
    Iterator<T> it = values.iterator();
    if (!it.hasNext()) return Double.NaN;
    double min = it.next().doubleValue();
    while (it.hasNext()) {
      min = Math.min(min, it.next().doubleValue());
    }
    return min;
  }
  
  /**
   * @param values any iterable number collection.
   * @return maximum value in <code>values</code> or {@link Double#NaN}
   * if <code>values</code> is <code>null</code> or contains no numbers.
   */
  public static <T extends Number> double max(Iterable<T> values) {
    if (values == null) return Double.NaN;
    Iterator<T> it = values.iterator();
    if (!it.hasNext()) return Double.NaN;
    double max = it.next().doubleValue();
    while (it.hasNext()) {
      max = Math.max(max, it.next().doubleValue());
    }
    return max;
  }
  
  /**
   * @param <T>
   * @param values
   * @return value with maximum distance to zero.
   */
  public static <T extends Number> double maxDistanceToZero(Iterable<T> values) {
    if (values == null) return Double.NaN;
    Iterator<T> it = values.iterator();
    if (!it.hasNext()) return Double.NaN;
    
    double maxVal = it.next().doubleValue();
    double max = Math.abs(maxVal);
    
    while (it.hasNext()) {
      double val = it.next().doubleValue();
      if (Math.abs(val)>max) {
        max = Math.abs(val);
        maxVal = val;
      }
    }
    return maxVal;
  }
  
  /**
   * Creates a string that summarizes the given collection by
   * returning min, max, median and mean.
   * Example: "Min:0, Mean:4, Median:5, Max:10"
   * @param <T>
   * @param values
   * @return "Min:%s, Mean:%s, Median:%s, Max:%s"
   */
  public static <T extends Number> String summary(Collection<T> values) {
    // Could be realized much more efficient by sorting and getting values manually.
    // All called methods consinder the whole, unsorted collection!
    return(String.format("Min:%s, Mean:%s, Median:%s, Max:%s",
      Utils.min(values), Utils.average(values), Utils.median(values), Utils.max(values) )); 
  }

  /**
   * Performs the nasty comparison of two integers, for usage in CompareTo methods.
   * Performs Nullpointer checks, before performing the actual comparison and
   * returns -1 if only one is <code>NULL</code>, 0 if both are <code>NULL</code>
   * and <pre>a-b</pre> if both are not null.
   * @param a
   * @param b
   * @return
   */
  public static int compareIntegers(Integer a, Integer b) {
    if (a==null) {
      if (b==null) {
        return 0;
      }
      return -1;
    } else if (b==null) {
      return -1;
    } else {
      return a-b;
    }
  }
  
  public static double log2(double val) {
    return Math.log(val)/ln2;
  }
  
  public static <T extends Number> double log2(T val) {
    return Math.log(val.doubleValue())/ln2;
  }

  /**
   * Returns a {@link String} from <code>arr</code> that is
   * equal (or equal ignoring case if <code>ignoreCase</code>
   * is true) to <code>s</code>.
   * <p>Note: This is usefull, either for<br>-replacing a string
   * pointer with another pointer that is already available OR<br>
   * -replace a mixed case string with a defined cases string (e.g.
   * "HaLLo" with "Hallo").
   * @param arr
   * @param s
   * @param ignoreCase
   * @return 
   */
  public static String returnString(String[] arr, String s, boolean ignoreCase) {
    for (int i=0; i<arr.length; i++)
      if ((!ignoreCase && arr[i].equals(s)) || (ignoreCase && arr[i].equalsIgnoreCase(s))) return arr[i];
    return null;
  }
  
}
