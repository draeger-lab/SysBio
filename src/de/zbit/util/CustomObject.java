package de.zbit.util;

import java.io.Serializable;

/**
 * Neccessary for extending supertypes. They can not be extended by Object directly.
 * @author wrzodek
 */
public class CustomObject<T> implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 3824104125096076634L;
  /**
   * 
   */
  private T obj;
  
  /**
   * 
   * @param obj
   */
  public CustomObject(T obj) {
    this.obj = obj;
  }
  
  /**
   * 
   * @return
   */
  public T getObject() {
    return obj;
  }
}
