package de.zbit.util;

import java.io.Serializable;

/**
 * Neccessary for extending supertypes. They can not be extended by Object directly.
 * @author wrzodek
 */
public class CustomObject implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 3824104125096076634L;
  /**
   * 
   */
  private Object obj;
  
  /**
   * 
   * @param obj
   */
  public CustomObject(Object obj) {
    this.obj = obj;
  }
  
  /**
   * 
   * @return
   */
  public Object getObject() {
    return obj;
  }
}
