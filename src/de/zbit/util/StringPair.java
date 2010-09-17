package de.zbit.util;

import java.io.Serializable;
/**
 * 
 */

/**
 * Class for saving two strings.
 * 
 * @author Finja B&uml;chel
 */
public class StringPair implements Comparable, Serializable{

  private static final long serialVersionUID = 6045589200635964019L;
  private String string1, string2;
  
  /**
   * 
   */
  public StringPair(String s1, String s2) {
    if (s1.compareTo(s2) < 0) {
      this.string1 = s1;
      this.string2 = s2;
    }
    else{
      this.string1 = s2;
      this.string2 = s1;
    }
    
  }
  
  public String toString(){
    return string1 + "\t" + string2;
  }

  public boolean equals(Object o) {
    boolean b = false;
    if(o instanceof StringPair){
      if((((StringPair) o).getString1().equals(string1) && ((StringPair) o).getString2().equals(string2))||
          (((StringPair) o).getString2().equals(string1) && ((StringPair) o).getString1().equals(string2))){
        b = true;
      }
      else
        b = false;
    }
    
    return b;
  }
  
  public int hashCode(){
    return string1.hashCode() + string2.hashCode();
  }
  
  public String getString1(){
    return string1;
  }
  
  public String getString2(){
    return string2;
  }

  
  public int compareTo(Object o) {
    
    if(o instanceof StringPair){
        int a = string1.compareTo(((StringPair) o).getString1());
        if (a==0) return string2.compareTo(((StringPair) o).getString2());
        else return a;
    }
    
    return this.toString().compareTo(o.toString());
  }
}
