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
package de.zbit.math;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import de.zbit.util.Utils;

/**
 * Contains various mathematic utilities.
 * 
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class MathUtils {
  
  /**
   * The natural logarithm of 2.
   */
  final static double ln2 = Math.log(2);
  
  /**
   * Returns the average of all non-NaN and non-infinite values in the given
   * array. Internally, it first tries to use the faster implementation and if
   * that doesn't work, it falls back to the slower alternative.
   * 
   * @param d an array of double values
   * @return the average of all non-NaN and non-infinite values in the given
   *         array.
   */
  public static double mean(double... d) {
    double average = mean1(d);
    if (Double.isNaN(average) || Double.isInfinite(average)) {
      return mean2(d);
    }
    return average;
  }
  
  /**
   * Spaltenweise mittelwertberechnung.
   * Versuchts erst schneller und nimmt sonst den langsameren, aber sicheren Algorithmus.
   */
  public static double[] mean(double[][] d) {
    double[] average = mean1(d);
    if (average == null)
    {
      return null; // Koomt vor wenn er alle sequenzen nicht mappen kann
    }
    for (int i=0; i<average.length; i++) {
      if (Double.isNaN(average[i]) || average[i]==Double.POSITIVE_INFINITY || average[i]==Double.NEGATIVE_INFINITY) {
        return mean2(d);
      }
    }
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
  private static double mean1(double[] d) { // Schneller
    if (d==null || d.length<1) {
      return Double.NaN;
    }
    double retVal= 0;
    
    int countNonNAN=0;
    for (int i=0; i<d.length; i++) {
      if (Double.isNaN(d[i]) || Double.isInfinite(d[i])) {
        continue;
      }
      countNonNAN++;
      retVal+=d[i];
    }
    
    if (countNonNAN<=0) {
      return Double.NaN;
    }
    return (retVal/countNonNAN);
  }
  
  /**
   * 
   * @param d
   * @return
   */
  private static double[] mean1(double[][] d) { // Schneller
    if (d.length<1) {
      return new double[0];
    }
    double[] retVal= null;
    
    int countNonNull = 0;
    for (int i=0; i<d.length; i++) {
      if (d[i] == null)
      {
        continue; // kommt vor wenn er sequenz i nicht mappen kann
      }
      countNonNull++;
      if (retVal==null) {
        retVal = new double[d[i].length];
      }
      for (int j=0; j<d[i].length; j++) {
        retVal[j]+=d[i][j];
      }
    }
    
    if (retVal==null)
    {
      return null; // Koomt vor wenn er alle sequenzen nicht mappen kann
    }
    for (int i=0; i<retVal.length; i++) {
      retVal[i] /= countNonNull;
    }
    
    
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
  private static double mean2(double[] d) { // Keine to-large-numbers
    if (d.length<1) {
      return Double.NaN;
    }
    double retVal= 0;
    
    int countNonNAN=0;
    for (int i=0; i<d.length; i++) {
      if (Double.isNaN(d[i]) || Double.isInfinite(d[i])) {
        continue;
      }
      countNonNAN++;
      
      // retVal[j]=retVal[j] * i/(i+1) + d[i][j] * 1/(i+1);
      retVal=retVal * (countNonNAN-1)/(countNonNAN) + d[i] * 1/(countNonNAN);
    }
    
    // Wenn irgendwo nur NaNs waren, das auch so wiedergeben
    if (countNonNAN<=0) {
      return Double.NaN;
    }
    return retVal;
  }
  
  /**
   * 
   * @param d
   * @return
   */
  private static double[] mean2(double[][] d) { // Keine to-large-numbers
    if (d.length<1) {
      return new double[0];
    }
    double[] retVal= null;
    ArrayList<Integer> spaltenCounter = new ArrayList<Integer>();
    for (int i=0; i<d.length; i++) {
      if (d[i] == null)
      {
        continue; // kommt vor wenn er sequenz i nicht mappen kann
      }
      if (retVal==null) {
        retVal = new double[d[i].length];
      }
      for (int j=0; j<d[i].length; j++) {
        if (spaltenCounter.size()<=j) {
          spaltenCounter.add(0);
        }
        if (Double.isNaN(d[i][j]))
        {
          continue; // Deshalb auch der Spaltencounter: Skip NaN eintraege.
        }
        //retVal[j]=retVal[j] * i/(i+1) + d[i][j] * 1/(i+1);
        retVal[j]=retVal[j] * spaltenCounter.get(j)/(spaltenCounter.get(j)+1) + d[i][j] * 1/(spaltenCounter.get(j)+1);
        spaltenCounter.set(j,spaltenCounter.get(j)+1);
      }
    }
    // Wenn irgendwo nur NaNs waren, das auch so wiedergeben
    for (int i=0; i<spaltenCounter.size(); i++) {
      if (spaltenCounter.get(i)==0) {
        retVal[i] = Double.NaN;
      }
    }
    return retVal;
  }
  
  /**
   * Mittelwertberechnung.
   * Versuchts erst schneller und nimmt sonst den langsameren, aber sicheren Algorithmus.
   */
  @SuppressWarnings("rawtypes")
  public static double mean(Collection d) {
    double average = mean1(d);
    if (Double.isNaN(average) || Double.isInfinite(average)) {
      return mean2(d);
    }
    return average;
  }
  
  @SuppressWarnings("rawtypes")
  private static double mean1(Collection doubles) { // Schneller
    if (doubles==null || doubles.size()<1) {
      return Double.NaN;
    }
    double retVal= 0;
    
    int countNonNAN=0;
    Iterator it = doubles.iterator();
    while (it.hasNext()) {
      try  {
        double d=0;
        Object o = it.next();
        d = Utils.getDoubleValue(o);
        
        if (Double.isNaN(d) || Double.isInfinite(d)) {
          continue;
        }
        countNonNAN++;
        retVal+=d;
        
      } catch (Throwable t) {t.printStackTrace();}
    }
    
    if (countNonNAN<=0) {
      return Double.NaN;
    }
    return (retVal/countNonNAN);
  }
  
  @SuppressWarnings("rawtypes")
  private static double mean2(Collection doubles) { // Keine to-large-numbers
    if (doubles==null || doubles.size()<1) {
      return Double.NaN;
    }
    double retVal= 0;
    
    int countNonNAN=0;
    Iterator it = doubles.iterator();
    while (it.hasNext()) {
      try  {
        double d=0;
        Object o = it.next();
        d = Utils.getDoubleValue(o);
        
        if (Double.isNaN(d) || Double.isInfinite(d)) {
          continue;
        }
        countNonNAN++;
        
        // retVal[j]=retVal[j] * i/(i+1) + d[i][j] * 1/(i+1);
        retVal=retVal * (countNonNAN-1)/(countNonNAN) + d * 1/(countNonNAN);
        
      } catch (Throwable t) {t.printStackTrace();}
    }
    
    // Wenn irgendwo nur NaNs waren, das auch so wiedergeben
    if (countNonNAN<=0) {
      return Double.NaN;
    }
    return retVal;
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
   * Binomial distribution (German: Binomialverteilung oder "n &uuml;ber m").
   * O(n&sup2;) implementation for small values. So better use it with a cache...
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
  public static BigInteger binomialCoefficient(int n, int k) {
    return binomialCoefficient(BigInteger.valueOf(n), BigInteger.valueOf(k));
  }
  
  /**
   * binomialCoefficient that also can calculate bigger values, using a {@link BigInteger}.
   * @param n
   * @param k
   * @return
   */
  public static BigInteger binomialCoefficient(BigInteger n, BigInteger k) {
    
    BigInteger n_minus_k=n.subtract(k);
    if(n_minus_k.compareTo(k)<0) {
      BigInteger temp=k;
      k=n_minus_k;
      n_minus_k=temp;
    }
    
    BigInteger numerator=BigInteger.ONE;
    BigInteger denominator=BigInteger.ONE;
    
    for(BigInteger j=BigInteger.ONE; j.compareTo(k)<=0; j=j.add(BigInteger.ONE)) {
      numerator=numerator.multiply(j.add(n_minus_k));
      denominator=denominator.multiply(j);
      BigInteger gcd=numerator.gcd(denominator);
      numerator=numerator.divide(gcd);
      denominator=denominator.divide(gcd);
    }
    
    return numerator;
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
    for (int i=0; i<arr1.length; i++) {
      ret[i] = new double [arr1[i].length];
      for (int j=0; j<arr1[i].length; j++) {
        if (arr2[i][j]==0) {
          ret[i][j]=Double.NaN;
        } else {
          ret[i][j] = arr1[i][j]/arr2[i][j];
        }
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
    for (int i=0; i<arr1.length; i++) {
      ret[i] = new double [arr1[i].length];
      for (int j=0; j<arr1[i].length; j++) {
        if (arr2[i][j]==0) {
          ret[i][j]=0;
        } else {
          ret[i][j] = (double)arr1[i][j]/arr2[i][j];
        }
      }
    }
    return ret;
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
    if (values.length<1) {
      return Double.NaN;
    }
    Arrays.sort(values);
    
    if (values.length %2 !=0) {
      return values[values.length/2];
    } else {
      int upper = (int) Math.ceil(values.length/2);
      return (values[upper - 1] + (values[upper] - values[upper - 1])/2);
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
    if (values.size()<1) {
      return Double.NaN;
    }
    Collections.sort(values);
    
    Object median;
    if (values.size()%2!=0) {
      median = values.get(values.size()/2);
      return Utils.getDoubleValue(median);
    } else {
      int upper = (int) Math.ceil(values.size()/2);
      double upperMedian = Utils.getDoubleValue(values.get(upper));
      double lowerMedian = Utils.getDoubleValue(values.get(upper-1));
      
      return (lowerMedian+(upperMedian-lowerMedian)/2);
    }
    
  }
  
  /**
   * Returns the value at relative (percentage) index {@code quantilke}
   * in the sorted list {@code values}.
   * @param values
   * @param quantile
   * @param listIsAlreadySorted in doubt, set to false.
   * @return
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static double quantile(List values, int quantile, boolean listIsAlreadySorted) {
    if (values.size()<1) {
      return Double.NaN;
    }
    if (!listIsAlreadySorted) {
      Collections.sort(values);
    }
    double valIndex = values.size()/100*quantile;
    double valFloor = Math.floor(valIndex);
    // (5 / 100) * 50 = 2.5 => Bei ungrade abrunden
    // (4 / 100) * 50 = 2 => Bei grade mittel aus idx-1 und idx.
    
    if(valFloor != valIndex) {
      // value is NO integer
      Object ret = values.get((int) valFloor);
      return Utils.getDoubleValue(ret);
    } else {
      // value is an integer
      double upperMedian = Utils.getDoubleValue(values.get((int) valFloor));
      double lowerMedian = Utils.getDoubleValue(values.get(Math.max((int) (valFloor-1), 0)));
      
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
      return median (list);
    }
  }
  
  /**
   * Returns the standard deviation of the given double values.
   * The Standard deviation is the square root of the variance.
   * @param values
   * @return
   */
  public static double standardDeviation(double[] values) {
    return Math.sqrt(variance(values));
  }
  
  /**
   * Returns the standard deviation of the given double values.
   * The Standard deviation is the square root of the variance.
   * @param values
   * @param mean -  the precomputed mean value
   * @return
   */
  public static double standardDeviation(double[] values, double mean) {
    return Math.sqrt(variance(values, mean));
  }
  
  /**
   * Calculates and returns the variance of the given list of double values
   * 
   * @param d the list of double values
   * @return
   */
  public static double variance(double[] d) {
    double mean = mean(d);
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
    if( d.length <= 1 ) {
      return sum;
    }
    
    for (int i = 0; i < d.length; i++) {
      sum += Math.pow(d[i] - mean, power);
    }
    return sum / (d.length - 1);
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
   * @param values any iterable number collection.
   * @return minimum value in {@code values} or {@link Double#NaN}
   * if {@code values} is {@code null} or contains no numbers.
   */
  public static <T extends Number> double min(Iterable<T> values) {
    if (values == null) {
      return Double.NaN;
    }
    Iterator<T> it = values.iterator();
    if (!it.hasNext()) {
      return Double.NaN;
    }
    double min = it.next().doubleValue();
    while (it.hasNext()) {
      min = Math.min(min, it.next().doubleValue());
    }
    return min;
  }
  
  /**
   * @param values any iterable number collection.
   * @return maximum value in {@code values} or {@link Double#NaN}
   * if {@code values} is {@code null} or contains no numbers.
   */
  public static <T extends Number> double max(Iterable<T> values) {
    if (values == null) {
      return Double.NaN;
    }
    Iterator<T> it = values.iterator();
    if (!it.hasNext()) {
      return Double.NaN;
    }
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
    if (values == null) {
      return Double.NaN;
    }
    Iterator<T> it = values.iterator();
    if (!it.hasNext()) {
      return Double.NaN;
    }
    
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
  
  public static double log2(double val) {
    return Math.log(val)/ln2;
  }
  
  public static <T extends Number> double log2(T val) {
    return Math.log(val.doubleValue())/ln2;
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
   * Returns a weighted mean of two values.
   * @param old_m
   * @param old_cgs
   * @param m
   * @param cgs
   * @return the weighted mean.
   */
  public static double weightedMean(double val, double weight, double val2, double weight2) {
    return (val*weight/(weight2+weight)) +  (val2 * weight2/(weight2+weight));
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
      min(values), mean(values), median(values), max(values) ));
  }
  
  /**
   * Creates a string that summarizes the given collection by
   * returning min, max, median and mean.
   * Example: "Min:0, Mean:4, Median:5, Max:10"
   * @param <T>
   * @param values
   * @param roundToDigits round given values to this number of digits
   * @return "Min:%s, Mean:%s, Median:%s, Max:%s"
   */
  public static <T extends Number> String summary(Collection<T> values, int roundToDigits) {
    // Could be realized much more efficient by sorting and getting values manually.
    // All called methods consinder the whole, unsorted collection!
    return(String.format("Min:%s, Mean:%s, Median:%s, Max:%s",
      round(min(values), roundToDigits), round(mean(values), roundToDigits),
      round(median(values), roundToDigits), round(max(values),roundToDigits) ));
  }
  
}
