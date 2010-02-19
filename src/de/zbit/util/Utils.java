package de.zbit.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

/**
 * Various utils, which I need quite often.
 * 
 * @author wrzodek
 */
public class Utils {

  public static StringBuffer replicateCharacter(char c, int times) {
    StringBuffer s = new StringBuffer();
    for (int i=0; i<times; i++)
      s.append(c);
    return s;
  }
  
  /**
   * Kann auch als Synonym für "containsWord" gebraucht werden.
   * @param containingLine
   * @param containedString
   * @return
   */
  public static boolean isWord(String containingLine, String containedString) {
    return isWord(containingLine, containedString, false);
  }
  
  public static boolean isWord(String containingLine, String containedString, boolean ignoreDigits) {
    // Check if it's a word
    int pos = -1;
    while (true) {
      if (pos+1>=containedString.length()) break;
      pos = containingLine.indexOf(containedString, pos+1);
      if (pos<0) break;
      
      boolean linksOK = true;
      if (pos>0) {
        char l = containingLine.charAt(pos-1);
        if ((Character.isDigit(l) && !ignoreDigits) || Character.isLetter(l)) linksOK = false;
      }
      boolean rechtsOK = true;
      if (pos+containedString.length()<containingLine.length()) {
        char l = containingLine.charAt(pos+containedString.length());
        if ((Character.isDigit(l) &&!ignoreDigits) || Character.isLetter(l)) rechtsOK = false;
      }
      
      if (rechtsOK && linksOK) return true;
    }
    return false;
  }
  
  /** Nicht ganz korrekt da auch 4.345,2.1 als nummer erkannt wird, aber das reicht mir so. **/
  public static boolean isNumber(String s, boolean onlyDigits) {
    char[] a = s.toCharArray();
    for (int i=0; i< a.length; i++) {
      if (onlyDigits){
        if (Character.isDigit(a[i])) continue; else return false;
      } else {
        if (Character.isDigit(a[i])) continue;
        if (a[i]=='-' || a[i]=='.' || a[i]==',' || a[i]=='E' || a[i]=='e') continue;
        return false;
      }
    }
    return true;
  }

  /**
   * Funzt nur für positive, natürliche Zahlen!
   */
  public static int getNumberFromString(String behindLastIndexOfString, String toParse) {
    int i = toParse.lastIndexOf(behindLastIndexOfString)+1;
    return getNumberFromString(i, toParse);
  }
  public static int getNumberFromString(int startAtPos, String toParse) {
    int i = startAtPos;
    if (i<=0) return -1; // Schlechte Rückgabe... aber was sonst? Exception throwen ist scheiße
    
    String ret = "";
    while (Character.isDigit(toParse.charAt(i)))
      ret += toParse.charAt(i++);
    
    return Integer.parseInt(ret);
  }
  
  
  public static String reverse(String s) {
    StringBuffer a = new StringBuffer(s);
    return a.reverse().toString();
  }
  public static String complement(String s) {
    StringBuffer ret = new StringBuffer(s.length());
    char[] a = s.toLowerCase().toCharArray();
    for (int i=0; i<a.length; i++) {
      if (a[i]=='a') ret.append('t');
      if (a[i]=='c') ret.append('g');
      if (a[i]=='g') ret.append('c');
      if (a[i]=='t') ret.append('a');
    }
    return ret.toString();
  }
  
  
  /**
   * Example:
   * AA: 0
   * AC: 1
   * AG: 2
   * AT: 3
   * CA: 4
   * TA: 12
   * TT: 15
   **/
  public static int DNA2Num(String a) {
    int ret = 0;
    char[] arr = reverse(a).toCharArray();
    for(int i=0; i<arr.length; i++)
      ret += (DNA2Num(arr[i])) * Math.pow(4, (i));
    return ret;
  }
  public static int DNA2Num(char a) {
    if (a =='A' || a =='a') return 0;
    if (a =='C' || a =='c') return 1;
    if (a =='G' || a =='g') return 2;
    if (a =='T' || a =='t') return 3;
    
    System.err.println("Unknwon DNA Character: '" + a + "'.");
    return -1;
  }
  
  public static char Num2DNA(int a) {
    if (a==0) return 'A';
    if (a==1) return 'C';
    if (a==2) return 'G';
    if (a==3) return 'T';
    
    System.err.println("To large input parameter on Num2DNA. Use xMeres variant of this function instead." + a);
    return 'N';
  }
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
  public static int[] countNucleotides(String sequence, int xMeres) {
    int counts[] = new int[(int) Math.pow(4, xMeres)];
    for (int i=0; i<sequence.length()-xMeres+1; i++)
      counts[DNA2Num(sequence.substring(i, i+xMeres))]++;
    return counts;
  }
  
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
  public static boolean ArrayContains(String[][] arr, String s) {
    s = s.toLowerCase().trim();
    for (int i=0; i<arr.length; i++)
      for (int j=0; j<arr[i].length; j++)
        if (arr[i][j].toLowerCase().trim().equals(s)) return true;
    return false;
  }
  public static String replicateCharacter(String ch, int times) {
    String retval = "";
    for (int i=0; i<times;i++){
      retval += ch;
    }
    return retval;
  }
  
  public static double round(double zahl, int stellen) {
    double d = Math.pow(10, stellen);
    return Math.round( zahl * ((long)d) ) / d;
  }
  
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
  public static double[] average2(double[][] d){ // Keine to-large-numbers
    if (d.length<1) return new double[0];
    double[] retVal= null;
    ArrayList<Integer> spaltenCounter = new ArrayList<Integer>(); 
    for (int i=0; i<d.length; i++) {
      if (d[i] == null) continue; // kommt vor wenn er sequenz i nicht mappen kann
      if (retVal==null) retVal = new double[d[i].length];
      for (int j=0; j<d[i].length; j++) {
        if (spaltenCounter.size()<=j) spaltenCounter.add(0);
        if (Double.isNaN(d[i][j])) continue; // Deshalb auch der Spaltencounter: Skip NaN einträge.
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
   * Mittelwertberechnung.
   * Versuchts erst schneller und nimmt sonst den langsameren, aber sicheren Algorithmus.
   */
  public static double average(double[] d){
    double average = average1(d);
    if (Double.isNaN(average) || Double.isInfinite(average)) 
        return average2(d);
    return average;
  }
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
  public static Object loadObject(String filename) {
    try {
      FileInputStream fileIn = new FileInputStream(filename);
      ObjectInputStream in = new ObjectInputStream(fileIn);
      Object ret = in.readObject();
      in.close();
      fileIn.close();
      return ret;
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch(FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

}
