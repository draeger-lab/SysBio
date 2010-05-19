package de.zbit.util;

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
}
