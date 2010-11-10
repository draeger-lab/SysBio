package de.zbit.util;

import java.util.LinkedList;
import java.util.List;

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
  
}
