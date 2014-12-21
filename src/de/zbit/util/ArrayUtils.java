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
package de.zbit.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Contains various utilities for arrays, but also some rare examples
 * for {@link Collection}s or {@link List}s (when e.g. a list shoudl
 * be converted to an array).
 * 
 * @author Clemens Wrzodek
 * @version $Rev$
 * @since 1.0
 */
public class ArrayUtils {

  /**
   * Converts a list of Double values to a {@code double[]} array.
   * 
   * @param list
   * @return
   */
  public static double[] toArray(List<Double> list) {
    double[] d = new double[list.size()];
    for (int i = 0; i < d.length; i++) {
      d[i] = list.get(i);
    }
    return d;
  }
  
  /**
   * Converts any single element into an array of length one.
   * @param <E>
   * @param singleElement
   * @return
   */
  public static <E> E[] toArray(E... singleElements) {
    E[] arr = createArray(singleElements[0], singleElements.length);
    for (int i=1; i<singleElements.length; i++) {
      arr[i] = singleElements[i];
    }
    return arr;
  }
  
  
  /**
   * If there exists one element in arr such that arr[i].equals(s),
   * i (the index) is returned.
   * @param <T>
   * @param arr
   * @param s
   * @return
   */
  public static <T> int indexOf(T[] arr, T s) {
    if (arr==null) return -1;
    for (int i=0; i<arr.length; i++) {
      if (s==null) {
        // Also detect indexOf null
        if (arr[i]==null) return i;
        else continue;
      }
      if (arr[i]==null) continue;
      if (arr[i].equals(s)) return i;
    }
    return -1;
  }
  
  /**
   * If there exists one element in arr such that arr[i].equalsIgnoreCase(s),
   * i (the index) is returned.
   * @param <T>
   * @param arr
   * @param s
   * @return
   */
  public static int indexOfIgnoreCase(String[] arr, String s) {
    if (arr==null) return -1;
    for (int i=0; i<arr.length; i++) {
      if (s==null) {
        // Also detect indexOf null
        if (arr[i]==null) return i;
        else continue;
      }
      if (arr[i]==null) continue;
      if (arr[i].equalsIgnoreCase(s)) return i;
    }
    return -1;
  }
  
  /**
   * @param arr
   * @param s
   * @return true if {@code s} is in {@code arr}.
   */
  public static <T> boolean contains(T[] arr, T s) {
    return indexOf(arr, s)>=0;
  }
  
  /**
   * Returns true if and only if there exists one item in arr
   * such that arr[i].equals(s) OR arr[i].equalsIgnoreCase(s).
   * @param arr
   * @param s
   * @return true if {@code s} is in {@code arr}.
   */
  public static boolean containsIgnoreCase(String[] arr, String s) {
    return indexOfIgnoreCase(arr, s)>=0;
  }
  
  /**
   * Checks if any element from {@code arr}toString()
   * is containe in {@code s}.
   * @param <T>
   * @param arr
   * @param s
   * @param ignoreCase
   * @return
   */
  public static <T> int isContainedIn(T[] arr, String s, boolean ignoreCase) {
    if (arr==null) return s==null?-2:-1;
    if (s==null) return -1;
    if (ignoreCase) s = s.toLowerCase();
    
    String s2;
    int index=0;
    for (T a: arr) {
      s2 = a.toString();
      if (ignoreCase) s2 = s2.toLowerCase();
      if (s.contains(s2)) return index;
      index++;
    }
    return -1;
  }
  
  /**
   * @param multiSourceColumn
   * @param i
   * @return
   */
  public static int indexOf(int[] arr, int s) {
    for (int i=0; i<arr.length; i++)
      if (arr[i]==(s)) return i;
    return -1;
  }
  
  /**
   * Implodes the given array. (e.g. implode(["a", "b"], " - ") would return "a - b".
   * @param <T>
   * @param arr
   * @param sep
   * @return
   */
  public static <T> String implode(T[] arr, String sep) {
  	if (arr==null || sep==null || arr.length<1) return null;
  	
  	StringBuilder ret = new StringBuilder();
  	for (int i=0; i<arr.length-1; i++) {
  		ret.append(arr[i]);
  		ret.append(sep);
  	}
  	ret.append(arr[arr.length-1]);
  	
		return ret.toString();
  }
  
  /**
   * Implodes the given iterable. (e.g. implode(["a", "b"], " - ") would return "a - b".
   * @param arr
   * @param sep
   * @return
   */
  public static <T> String implode(Iterable<T> arr, String sep) {
    return implode(arr, sep, false);
  }
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static <T> String implode(Iterable<T> arr, String sep, boolean sorted) {
    if (arr==null || sep==null || !arr.iterator().hasNext()) return null;
    
    if (sorted) {
      arr = new ArrayList<T>(Utils.iterableToList(arr));
      try {
        Collections.sort((List<Comparable>) arr);
      } catch (Exception e) {
        // We cannot sort non-comparable objects...
      }
    }
    
    StringBuilder ret = new StringBuilder();
    Iterator<T> it = arr.iterator();
    while (true) {
      ret.append(it.next());
      if (it.hasNext()) {
        ret.append(sep);
      } else {
        break;
      }
    }
    
    return ret.toString();
  }


  /**
   * Removes each row (first dimension) that contains only empty or null cells.
   * @param data
   * @return
   */
  public static String[][] removeEmptyRows(String[][] data) {
    List<String[]> ret = new LinkedList<String[]>();
    for (int i=0; i<data.length; i++) {
      for (int j=0; j<data[i].length; j++) {
        if (data[i]!=null && data[i][j].length()>0) {
          ret.add(data[i]); break;
        }
      }
    }
    
    return ret.toArray(new String[0][]);
  }


  /**
   * Creates a new generic array and fills it with the
   * given default value.
   * 
   * @param string
   * @param size
   * @return
   */
  @SuppressWarnings("unchecked")
  public static <T extends Object> T[] createArray(T defaultValue, int size) {
    T[] ret = (T[]) createNewArray(defaultValue,size);
    Arrays.fill(ret, defaultValue);
    
    return ret;
  }
  
  /**
   * Creates a new array, uses reflection methods, so this method is save
   * to use with generics.
   * @param type - the method will infere the class of the object from this
   * given sample. Just give any sample of the class you want to create a new
   * array from. The sample won't be touched.
   * @param size
   * @return new array of the class of the given type, with the given size.
   */
  @SuppressWarnings({ "rawtypes" })
  public static Object createNewArray(Object type, int size) {
    Class elementType=null;
    if (type instanceof Class)
      elementType = (Class) type;
    else if (type.getClass().isArray())
      elementType = type.getClass().getComponentType();
    
    // If oldArray was in fact no array, then elementType==null here.
    if (elementType==null) elementType = type.getClass();
    Object newArray = java.lang.reflect.Array.newInstance(elementType, size);
    
    return newArray;
  }


  /**
   * Simply concatenates each element of the array with a tab.
   * @param line
   */
  public static String toTabbedString(String[] line) {
    return implode(line, "\t");
  }


  /**
   * Convert an Integer array to an int array.
   * @param array
   * @return
   */
  public static int[] toIntArray(Integer[] array) {
    int[] ret = new int[array.length];
    for (int i=0; i<array.length; i++) {
      ret[i]=array[i];
    }
    return ret;
  }
  
  /**
   * Convert any {@link Number} list to an double array.
   * @param list
   * @return
   */
  public static <T extends Number> double[] toDoubleArray(List<T> list) {
    double[] ret = new double[list.size()];
    for (int i=0; i<list.size(); i++) {
      ret[i]=list.get(i).doubleValue();
    }
    return ret;
  }


  /**
   * Merges two arrays or one array and multiple single instances
   * of the same type.
   * @param arr
   * @param arr2
   * @return a new array, containing all elements in the given order.
   */
  @SuppressWarnings("unchecked")
  public static <T> T[] merge(T[] arr, T... element) {
    // Ensure that both are not null.
    if (arr==null) return element;
    if (element==null || element.length==1 && element[0]==null) return arr;
    
    // Copy arrays
    T[] ret = (T[]) createNewArray(element, arr.length+element.length);
    System.arraycopy(arr, 0, ret, 0, arr.length);
    System.arraycopy(element, 0, ret, arr.length, element.length);
    
    return ret;
  }
  
  /**
   * Adds one instance to the front of an array.
   * @param element
   * @param arr
   * @return a new array, containing all elements in the given order.
   */
  @SuppressWarnings("unchecked")
  public static <T> T[] merge(T element, T[] arr) {
    // Ensure that both are not null.
    if (arr==null) return createArray(element, 1);
    if (element==null) return arr;
    
    // Copy arrays
    T[] ret = (T[]) createNewArray(element, arr.length+1);
    ret[0] = element;
    System.arraycopy(arr, 0, ret, 1, arr.length);
    
    return ret;
  }
  
  /**
   * Sums each value in an array.
   * @param arr
   * @return
   */
  public static double sum(int[] arr) {
    double d = 0;
    for (int i: arr)
      d+=i;
    return d;
  }

  
  /**
   * Sums each value in an array.
   * @param arr
   * @return
   */
  public static double sum(double[] arr) {
    double d = 0;
    for (double i: arr)
      d+=i;
    
    return d;
  }
  
  
  /**
   * Sums each value in an array.
   * @param arr
   * @return
   */
  public static BigDecimal sum(BigDecimal[] arr) {
    BigDecimal d = BigDecimal.ZERO;
    
    for (BigDecimal i: arr)
      d = d.add(i);
    
    return d;
  }

  
  /**
   * Reverse the input array. I.e., the first item will be the last and so on.
   * @param hits
   * @return
   */
  public static <T> void reverse(T[] array) {
    int max = array.length/2;
    for (int i=0; i<max; i++) {
      swap(array, i, array.length-(i+1));
    }
  }
  
  /**
   * Swaps the two specified elements in the specified array.
   */
  public static <T> void swap(T[] arr, int i, int j) {
    T tmp = arr[i];
    arr[i] = arr[j];
    arr[j] = tmp;
  }


  /**
   * Returns a (not necessarily new) array of the given size
   * that contains each element of the old array if the inidice was
   * available in the old array and the defaultValue at each other
   * Indices.
   * @see #resize(Object[], int, Object)
   * @param <T> type of array
   * @param array
   * @param size new size
   * @param defaultValue default value to fill in every new indice
   * @param largerSizeAllowed if false, will always return an array that
   * is exactly as long as the given size. If false, an array that is
   * at least as long as the given size will be returned (less modifications).
   * @return
   */
  public static <T> T[] resize(T[] array, int size, T defaultValue, boolean largerSizeAllowed) {
    // Ensure !=null
    if (array==null) {
      return createArray(defaultValue, size);
    } else if (array.length==size) return array;
    
    // Ensure array.length==size
    if (largerSizeAllowed && array.length>=size) {
      return array;
    }
    return Arrays.copyOf(array, size);
  }
  
  /**
   * Returns a (not necessarily new) array of the given size
   * that contains each element of the old array if the inidice was
   * available in the old array and the defaultValue at each other
   * Indices.
   * @see #resize(Object[], int, Object, boolean)
   * @param <T> type of array
   * @param array
   * @param size new size
   * @param defaultValue default value to fill in every new indice
   * @return
   */
  public static <T> T[] resize(T[] array, int size, T defaultValue) {
    return resize(array, size, defaultValue, false);
  }


  /**
   * @param arr
   * @return null if all elements in arr are null, else, all
   * elements that are not null.
   */
  @SuppressWarnings("unchecked")
  public static <T> T[] removeNull(T[] arr) {
    LinkedList<T> nonNull = new LinkedList<T>();
    T sample=null;
    for (T t : arr) {
      if (t!=null) {
        nonNull.add(t);
        sample=t;
      }
    }
    if (nonNull.size()<1) return null;
    return (T[]) nonNull.toArray((T[]) createNewArray(sample, nonNull.size()));
  }

  /**
   * Converts a sub-range of an array to a new array.
   * @param arr array to convert
   * @param rangeStart index to place as first element in the resulting list
   * @param rangeEnde stop placing further items in the list, when this
   * index is reached
   * @return array, containing all elements of {@code add} between
   * {@code rangeStart} and {@code rangeEnde} in preserved order.
   */
  @SuppressWarnings("unchecked")
  public static <T> T[] toSubArray(T[] arr, int rangeStart, int rangeEnde) {
    // Couldn't we just use Arrays.copyOfRange ?
    int length = rangeEnde-rangeStart;
    T[] newArr = (T[]) createNewArray(arr, length);
    System.arraycopy(arr, rangeStart, newArr, 0, length);
    return newArr;
  }
  
  /**
   * Allows to convert a sub-range of an array to a list.
   * @param arr array to convert
   * @param rangeStart index to place as first element in the resulting list
   * @param rangeEnde stop placing further items in the list, when this
   * index is reached
   * @return list, containing all elements of {@code add} between
   * {@code rangeStart} and {@code rangeEnde} in preserved order.
   */
  public static <T> List<T> asList(T[] arr, int rangeStart, int rangeEnde) {
    return Arrays.asList(toSubArray(arr, rangeStart, rangeEnde));
  }
  
  /**
   * Add a key value pair to a {@link Map}, in which each Value is
   * actually a {@link List} of real values (i.e. one key can refer
   * to multiple values - Map&lt;K, List&lt;V&gt;&gt;)!
   * @param <K>
   * @param <V>
   * @param map
   * @param key
   * @param listItem
   */
  public static <K, V> void addToList(Map<K,List<V>> map, K key, V listItem) {
    List<V> list = map.get(key);
    if (list==null) {
      list = new ArrayList<V>();
      map.put(key, list);
    }
    list.add(listItem);
  }

  /**
   * <p>NOTE: THIS IS UNTESTED AND MIGHT NOT WORK.</p>
   * @param <T>
   * @param insertAtPosition
   * @param arr
   * @param element
   * @return
   */
  @SuppressWarnings("unchecked")
  public static <T> T[] insert(int insertAtPosition, T[] arr, T... element) {
    // Ensure that both are not null.
    if (arr==null) return element;
    if (element==null || element.length==1 && element[0]==null) return arr;

    // Copy arrays
    T[] ret = (T[]) createNewArray(element, arr.length+element.length);
    System.arraycopy(arr, 0, ret, 0, insertAtPosition);
    System.arraycopy(element, 0, ret, insertAtPosition, element.length);
    System.arraycopy(arr, insertAtPosition, ret, insertAtPosition+element.length, arr.length-insertAtPosition);

    return ret;
  }

  /**
   * Merges all {@code collections} into {@code addHere}.
   * @param addHere
   * @param collections
   */
  public static void merge(Collection<String> addHere, Collection<String>... collections) {
    for (Collection<String> col: collections) {
      if (col!=null) {
        addHere.addAll(col);
      }
    }
  }

  /**
   * 
   * @param table
   * @return
   */
  public static Object[][] toArray(Collection<? extends Collection<?>> table) {
    Object[][] arr = new Object[table.size()][];
    int i=0;
    for (Collection<?> c : table) {
      arr[i++] = c.toArray();
    }
    
    return arr;
  }

}
