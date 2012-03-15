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
package de.zbit.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.sun.imageio.plugins.common.ImageUtil;

import de.zbit.gui.GUITools;
import de.zbit.io.FileTools;
import de.zbit.math.MathUtils;
import de.zbit.sequence.DNAsequenceUtils;


/**
 * Various utils, which I need quite often.
 * 
 * <p>When searching or adding utilities, please
 * first consider the more specific classes:
 * <ul>
 * <li>{@link StringUtil}
 * <li>{@link ConsoleTools}</li>
 * <li>{@link ArrayUtils}</li>
 * <li>{@link MathUtils}</li>
 * <li>{@link GUITools}</li>
 * <li>{@link ImageUtil}</li>
 * <li>{@link DNAsequenceUtils}</li>
 * <li>{@link FileTools}</li>
 * <li>...</li>
 * </ul>
 * 
 * @author Clemens Wrzodek
 * @version $Rev$
 * @since 1.0
 */
public class Utils {
  
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
   * Cut number at dot. E.g., 1.68 => 1
   * In contrary, decimal format "#" would return 2!
   * @param d
   * @return
   */
  public static String cut(double d) {
    String s = Double.toString(Math.floor(d));
    int ep = s.indexOf('.');
    if (ep < 1) {
    	ep = s.length();
    }
    return s.substring(0, ep);
  }
  
  /**
   * Ensures that path ends with a slash (for folder processing).
   * @param path
   */
  public static String ensureSlash(String path) {
    if (!path.endsWith("\\") && !path.endsWith("/")) {
      if (path.contains("/")) {
      	path += '/';
      } else if (path.contains("\\")) {
      	path += '\\';
      } else {
      	path += '/';
      }
    }
    return path;
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
    double seconds = miliseconds / 1000d;
    double minutes = seconds / 60d;
    double hours = minutes / 60d;
    double days = hours / 24;
    
    String ret;
    if (days >= 1) {
      ret = cut(days) + "d " + cut(hours % 24d)  + "h " + cut(minutes % 60) + "m";
    } else if (hours >= 1) {
      ret = cut(hours % 24d)  + "h " + cut(minutes % 60) + "m " + cut(seconds % 60) + "s";
    } else if (minutes >= 1) {
      ret = cut(minutes % 60) + "m " + cut(seconds % 60) + "s " + cut(miliseconds % 1000d) + " ms";
    } else if (seconds >= 1) {
      ret = cut(seconds % 60) + " s " + cut(miliseconds%1000.0) + " ms";
    } else {
      ret = cut(miliseconds % 1000d) + " ms";
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
    double seconds = miliseconds / 1000d;
    double minutes = seconds / 60d;
    double hours = minutes / 60d;
    double days = hours / 24d;
    
    String ret;
    if (days >= 1) {
      ret = cut(days) + " d " + cut(hours % 24d)  + " h";
    } else if (hours >= 1) {
      ret = cut(hours % 24d)  + " h " + cut(minutes % 60) + " m";
    } else if (minutes >= 2) {
      ret = cut(minutes % 60d) + " m";
    } else if (minutes >= 1) {
      ret = cut(minutes % 60) + " m " + cut(seconds % 60) + " s";
    } else if (seconds >= 1) {
      ret = cut(seconds % 60) + " s";
    } else {
      ret = cut(miliseconds % 1000d) + " ms";
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
   * Outputs the Minimum, Maximum value of the array and the number of
   * {@link Double#NaN} and Infinity values.
   * @param arr
   */
  public static void showMinMaxInfNaN(double[] arr) {
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
   * This function creates a new file / overwrites an existing one (without warning) and puts
   * the given content into the file. The required directory structure to write the file
   * will be created.
   * @param filename File path and name.
   * @param content content to write to file.
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
  
  /**
   * Returns a {@link String} from <code>arr</code> that is
   * equal (or equal ignoring case if <code>ignoreCase</code>
   * is true) to <code>s</code>.
   * <p>Note: This is usefull, either for
   * <br>-replacing a string pointer with another pointer
   * that is already available OR
   * <br>-replace a mixed case string with a defined
   * cases string (e.g. "HaLLo" with "Hallo").
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
  
  /**
   * Converts a {@link Collection} to a {@link List} with the
   * most effective available method.
   * <p><i>NOTE: This is a wrapper method that simply calls
   * {@link #iterableToList(Iterable)}.</i></p>
   * @param <T>
   * @param col
   * @return
   */
  public static <T> List<T> collectionToList(Collection<T> col) {
    // People where searching for "collectionToList" and not for
    // "iterableToList". Thus, to enhance visibility, this method
    // has been added.
    return iterableToList(col);
  }
  
  /**
   * Converts an {@link Iterable} to a {@link List} with the
   * most effective available method. 
   * @param <T>
   * @param it
   * @return
   */
  public static <T> List<T> iterableToList(Iterable<T> it) {
    List<T> l;
    if (it instanceof List) {
      // is already a list
      l = (List<T>) it;
    } else if (it instanceof Collection) {
      // ArrayList simply wraps the collection => O(1) here!
      l = new ArrayList<T>((Collection<T>)it);
    } else {
      // Worst Case... O(n) here.
      l = new ArrayList<T>();
      Iterator<T> i = it.iterator();
      while (i.hasNext()) {
        l.add(i.next());
      }
    }
    
    return l;
  }

  
}
