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
package de.zbit.util;


/**
 * Implementations of various sorting algorithms
 * 
 * @author Clemens Wrzodek
 * @version $Rev$
 * @since 1.0
 */
@SuppressWarnings("unchecked")
public class Mergesort{
  
  /*
   * Quicksort
   */
  
  public static void qsort(Comparable[] c,int start,int end) {
    if(end <= start) return;
    Comparable comp = c[start];
    int i = start,j = end + 1;
    for(;;) {
      do i++; while(i<end && c[i].compareTo(comp)<0);
      do j--; while(j>start && c[j].compareTo(comp)>0);
      if(j <= i)   break;
      Comparable tmp = c[i];
      c[i] = c[j];
      c[j] = tmp;
    }
    c[start] = c[j];
    c[j] = comp;
    qsort(c,start,j-1);
    qsort(c,j+1,end);
  }
  
  public static void qsort(Comparable[] c) {
    qsort(c,0,c.length-1);
  }
  
  public static <T extends Comparable> void qsortTabString(Comparable<String>[][] c, int indexToSort, T DataTypeToSort, int start,int end) {
    //String DataType = DataTypeToSort.toLowerCase();
    if(end <= start) return;
    Comparable<T> comp = (T)c[start][indexToSort];
    Comparable<String>[] tmp2 = c[start];
    
    /*if (DataType == "string")
      comp = (String)comp;
    else if (DataType == "int" || DataType == "integer")
      comp = Integer.parseInt((String)comp);
    else if (DataType == "double")
      comp = Double.parseDouble((String)comp);*/
    
    int i = start,j = end + 1;
    for(;;) {
      /*if (DataType == "string") {
        do i++; while(i<end && (c[i][indexToSort]).compareTo(comp)<=0);
        do j--; while(j>start && (c[j][indexToSort]).compareTo(comp)>=0);
      } else if (DataType == "int" || DataType == "integer") {
        do i++; while(i<end && ((Comparable)Integer.parseInt((String)c[i][indexToSort])).compareTo(comp)<=0);
        do j--; while(j>start && ((Comparable)Integer.parseInt((String)c[j][indexToSort])).compareTo(comp)>=0);
      } else if (DataType == "double") {
        do i++; while(i<end && ((Comparable)Double.parseDouble((String)c[i][indexToSort])).compareTo(comp)<=0);
        //do j--; while(j>start && ((Comparable<Double>)c[j][indexToSort]).compareTo(comp)>=0);
      } else {*/
      do i++; while(i<end && ((T)c[i][indexToSort]).compareTo(comp)<=0);
      do j--; while(j>start && ((T)c[j][indexToSort]).compareTo(comp)>=0);
      //}
      
      if(j <= i)   break;
      Comparable<String>[] tmp = c[i];
      c[i] = c[j];
      c[j] = tmp;
    }
    c[start] = c[j];
    c[j] = tmp2;
    qsortTabString(c,indexToSort, DataTypeToSort, start,j-1);
    qsortTabString(c,indexToSort, DataTypeToSort, j+1,end);
  }
  
  public static void qsortTabString(Comparable[][] c, int indexToSort, String DataTypeToSort) {
    qsortTabString(c,indexToSort, DataTypeToSort, 0,c.length-1);
  }
  
  
  
  
  
  
  
  
  
  
  /*
   * MergeSort
   */
  
  /***********************************************************************
   *  Merge the subarrays a[lo] .. a[mid-1] and a[mid] .. a[hi-1] into
   *  a[lo] .. a[hi-1] using the auxilliary array aux[] as scratch space.
   *
   *  Precondition:   the two subarrays are in ascending order
   *  Postcondition:  a[lo] .. a[hi-1] is in ascending order
   *
   ***********************************************************************/
  private static void merge(Comparable[][] a, Comparable[][] aux, int lo, int mid, int hi, int spalte, boolean numeric) {
    int i = lo, j = mid;
    for (int k = lo; k < hi; k++) {
      if      (i == mid)                 aux[k] = a[j++];
      else if (j == hi)                  aux[k] = a[i++];
      else if (((!numeric) && a[j][spalte].compareTo(a[i][spalte]) < 0) ||
          (numeric)&&Double.parseDouble(a[j][spalte]+"")<Double.parseDouble(a[i][spalte]+""))
        aux[k] = a[j++];
      else                               aux[k] = a[i++];
    }
    
    // copy back
    for (int k = lo; k < hi; k++)
      a[k] = aux[k];
  }
  
  
  /***********************************************************************
   *  Mergesort the subarray a[lo] .. a[hi-1], using the
   *  auxilliary array aux[] as scratch space.
   ***********************************************************************/
  public static void MergeSort(Comparable[][] a, Comparable[][] aux, int lo, int hi, int spalte, boolean numeric) {
    
    // base case
    if (hi - lo <= 1) return;
    
    // sort each half, recursively
    int mid = lo + (hi - lo) / 2;
    MergeSort(a, aux, lo, mid, spalte, numeric);
    MergeSort(a, aux, mid, hi, spalte, numeric);
    
    // merge back together
    merge(a, aux, lo, mid, hi, spalte, numeric);
  }
  
  
  /***********************************************************************
   *  Sort the array a using mergesort
   ***********************************************************************/
  public static void MergeSort(Comparable[][] a, int spalte, boolean numeric) {
    Comparable[][] aux = new Comparable[a.length][];
    MergeSort(a, aux, 0, a.length, spalte, numeric);
  }
  
  
  /***********************************************************************
   *  Check if array is sorted - useful for debugging
   ***********************************************************************/
  public static boolean isSorted(Comparable<String>[][] a, int spalte, boolean numeric) {
    for (int i = 2; i < a.length; i++) {
      if (!numeric) {
        if (a[i][spalte].compareTo(a[i-1][spalte]+"") < 0) {
          System.out.println("TEST");
          return false;
        }
      } else {
        if ((Double.parseDouble(a[i][spalte]+"")) < (Double.parseDouble(a[i-1][spalte]+""))) return false;
        
      }
    }
    return true;
  }
  
  
  
  private static void mergeDescending(Comparable[][] a, Comparable[][] aux, int lo, int mid, int hi, int spalte, boolean numeric) {
    int i = lo, j = mid;
    for (int k = lo; k < hi; k++) {
      if      (i == mid)                 aux[k] = a[j++];
      else if (j == hi)                  aux[k] = a[i++];
      else if (((!numeric) && a[j][spalte].compareTo(a[i][spalte]) > 0) ||
          (numeric)&&Double.parseDouble(a[j][spalte]+"")>Double.parseDouble(a[i][spalte]+""))
        aux[k] = a[j++];
      else                               aux[k] = a[i++];
    }
    
    // copy back
    for (int k = lo; k < hi; k++)
      a[k] = aux[k];
  }
  
  
  /***********************************************************************
   *  Mergesort the subarray a[lo] .. a[hi-1], using the
   *  auxilliary array aux[] as scratch space.
   ***********************************************************************/
  public static void MergeSortDescending(Comparable[][] a, Comparable[][] aux, int lo, int hi, int spalte, boolean numeric) {
    
    // base case
    if (hi - lo <= 1) return;
    
    // sort each half, recursively
    int mid = lo + (hi - lo) / 2;
    MergeSortDescending(a, aux, lo, mid, spalte, numeric);
    MergeSortDescending(a, aux, mid, hi, spalte, numeric);
    
    // merge back together
    mergeDescending(a, aux, lo, mid, hi, spalte, numeric);
  }
  
  
  /***********************************************************************
   *  Sort the array a using mergesort
   ***********************************************************************/
  public static void MergeSortDescending(Comparable[][] a, int spalte, boolean numeric) {
    Comparable[][] aux = new Comparable[a.length][];
    MergeSortDescending(a, aux, 0, a.length, spalte, numeric);
  }
  
  
}
