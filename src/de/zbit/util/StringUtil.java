package de.zbit.util;

import java.util.Arrays;
import java.util.List;

/**
 * 
 * @author mittag
 * @author wrzodek
 */
public class StringUtil {
  
  /**
   * Returns the concatenated strings of the array separated with the given
   * delimiter.
   * 
   * @param ary
   * @param delim
   * @return
   */
  public static String implode(String[] ary, String delim) {
    String out = "";
    for(int i=0; i<ary.length; i++) {
      if(i!=0) { out += delim; }
      out += ary[i];
    }
    return out;
  }
  
  /**
   * Returns the concatenated strings of the array separated with the given
   * delimiter.
   * 
   * @param ary
   * @param delim
   * @return
   */
  public static String implode(List<String> list, String delim) {
    String[] ary = new String[list.size()];
    list.toArray(ary);
    return implode(ary, delim);
  }
  
  public static String fill(String input, int len, char fill, boolean prepend) {
    if( input == null ) {
      input = "";
    }
    if( len <= input.length() ) {
      return input;
    }
    
    char[] cs = new char[len - input.length()];
    Arrays.fill(cs, fill);
    
    return prepend ? (new String(cs) + input) : input + (new String(cs));
  }
  
  public static String getLongestCommonPrefix(String a, String b) {
    int i;
    for (i=0; i<Math.min(a.length(), b.length()); i++) {
      if (a.charAt(i)!=b.charAt(i)) break;
    }
    return a.substring(0, i);
  }
  public static String getLongestCommonSuffix(String a, String b) {
    int i;
    for (i=1; i<=Math.min(a.length(), b.length()); i++) {
      if (a.charAt(a.length()-i)!=b.charAt(b.length()-i)) break;
    }
    return a.substring(a.length()-i+1, a.length());
  }
  public static String getLongestCommonPrefix(String a, String[] b, boolean ignoreEmptyStrings) {
    int i;
    
    // Get minimum String length in b
    int minLength=a.length();
    minLength = Math.min(getMinimumLength(b, ignoreEmptyStrings), minLength);
    
    // Iterate through all positions, until it does not match.
    boolean breakIt=false;
    for (i=0; i<minLength; i++) {
      char c = a.charAt(i);
      for (int l=0; l<b.length; l++) {
        // !ignoreEmptyStrings already handled.
        if (b[l]==null || b[l].length()<1) continue;
        
        if (c!=b[l].charAt(i)) {
          breakIt=true;
          break;
        }
      }
      if (breakIt) break;
    }
    
    return a.substring(0, i);
  }
  public static String getLongestCommonSuffix(String a, String[] b, boolean ignoreEmptyStrings) {
    int i;
    
    // Get minimum String length in b
    int minLength=a.length();
    minLength = Math.min(getMinimumLength(b, ignoreEmptyStrings), minLength);
    
    // Iterate through all positions, until it does not match.
    boolean breakIt=false;
    for (i=1; i<=minLength; i++) {
      char c = a.charAt(a.length()-i);
      for (int l=0; l<b.length; l++) {
        // !ignoreEmptyStrings already handled.
        if (b[l]==null || b[l].length()<1) continue;
        
        if (c!=b[l].charAt(b[l].length()-i)) {
          breakIt=true;
          break;
        }
      }
      if (breakIt) break;
    }
    
    return a.substring(a.length()-i+1, a.length());
  }
  public static String getLongestCommonSuffix(String[] b, boolean ignoreEmptyStrings) {
    int i;
    
    // Get minimum String length in b
    int minLength = getMinimumLength(b, ignoreEmptyStrings);
    
    // Iterate through all positions, until it does not match.
    int nonEmptyId=-1;
    boolean breakIt=false;
    for (i=1; i<=minLength; i++) {
      char c = '\u0000';
      for (int l=0; l<b.length; l++) {
        // !ignoreEmptyStrings already handled.
        if (b[l]==null || b[l].length()<1) continue;
        
        // Set current char
        if (c == '\u0000') {
          nonEmptyId = l;
          c=b[l].charAt(b[l].length()-i);
          continue;
        }
        
        if (c!=b[l].charAt(b[l].length()-i)) {
          breakIt=true;
          break;
        }
      }
      if (breakIt) break;
    }
    
    return nonEmptyId<0?"":b[nonEmptyId].substring(b[nonEmptyId].length()-i+1, b[nonEmptyId].length());
  }
  public static String getLongestCommonPrefix(String[] b, boolean ignoreEmptyStrings) {
    int i;
    
    // Get minimum String length in b
    int minLength = getMinimumLength(b, ignoreEmptyStrings);
    
    // Iterate through all positions, until it does not match.
    int nonEmptyId=-1;
    boolean breakIt=false;
    for (i=0; i<minLength; i++) {
      char c = '\u0000';
      for (int l=0; l<b.length; l++) {
        // !ignoreEmptyStrings already handled.
        if (b[l]==null || b[l].length()<1) continue;
        
        // Set current char
        if (c == '\u0000') {
          nonEmptyId = l;
          c=b[l].charAt(i);
          continue;
        }
        
        if (c!=b[l].charAt(i)) break;
      }
      if (breakIt) break;
    }
    
    return nonEmptyId<0?"":b[nonEmptyId].substring(0,i);
  }
  
  /**
   * Returns the longest common length and the number of it's occurences.
   * If multiple lengths occure equally often, prefres the longer length.
   * Null strings are treated as length=0.
   * @param b
   * @return int[2].
   * [0]=The longest common String length
   * [1]=The number of strings in b with that length.
   */
  public static int[] getLongestCommonLength(String[] b) {
    /*
     * Can be implemented in o(n*n) runtime and o(1) memory usage OR
     * o(n) runtime and o(n) memory usage. This is the later implementation.
     */
    SortedArrayList<int[]> LengthAndCounts = new SortedArrayList<int[]>();
    int maxLength=0; int maxLengthOcc=0;
    for (int l=0; l<b.length; l++) {
      int length=0;
      if (b[l]!=null) {
        length = b[l].length();
      }
      
      // Create new element or increment by one
      int pos = LengthAndCounts.indexOf(length);
      int newCounter=1;
      if (pos<0) LengthAndCounts.add(new int[]{length, newCounter});
      else {
        newCounter = (LengthAndCounts.get(pos)[1]+1);
        LengthAndCounts.set(pos, new int[]{length, newCounter});
      }
      
      // Remember maximum occuring length
      if (newCounter>maxLengthOcc || 
          newCounter==maxLengthOcc && length > maxLength) {
        maxLengthOcc = newCounter;
        maxLength = length;
      }
      
    }
    
    return new int[]{maxLength, maxLengthOcc};
  }
  
  /**
   * 
   * @param b
   * @param ignoreEmptyStrings - Ignore empty or null elements.
   * @return Minimum String length in b
   */
  public static int getMinimumLength(String[] b, boolean ignoreEmptyStrings) {
    int minLength=Integer.MAX_VALUE; boolean atLeastOneNonNull=false;
    for (int l=0; l<b.length; l++) {
      if (b[l]==null || b[l].length()<1) {
        if (ignoreEmptyStrings) continue; else return 0;
      } else {
        minLength = Math.min(minLength, b[l].length());
        atLeastOneNonNull = true;
      }
    }
    
    if (b.length==0 || !atLeastOneNonNull) return 0;
    return minLength;
  }
  
  /**
   * Returns the given column (O(n) implementation).
   * @param data[rows][cols]
   * @param col - col number to return
   * @return
   */
  public static String[] getColumn(String[][] data, int col) {
    String[] ret = new String[data.length];
    for (int i=0; i<data.length; i++)
      if (data[i]==null || data[i].length<=col)
        ret[i]=null;
      else
        ret[i] = data[i][col];
    return ret;
  }
  
}
