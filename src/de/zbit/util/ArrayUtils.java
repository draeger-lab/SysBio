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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * 
 * @version $Rev$
 * @since 1.0
 */
public class ArrayUtils {

  /**
   * Converts a list of Double values to a <code>double[]</code> array.
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
   * Implodes the given array. (e.g. implode(["a", "b"], " - ") would return "a - b".
   * @param <T>
   * @param arr
   * @param sep
   * @return
   */
  public static <T> String implode(T[] arr, String sep) {
  	if (arr==null || sep==null || arr.length<1) return null;
  	
  	StringBuffer ret = new StringBuffer();
  	for (int i=0; i<arr.length-1; i++) {
  		ret.append(arr[i]);
  		ret.append(sep);
  	}
  	ret.append(arr[arr.length-1]);
  	
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
  @SuppressWarnings("unchecked")
  private static Object createNewArray(Object type, int size) {
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
  
}
