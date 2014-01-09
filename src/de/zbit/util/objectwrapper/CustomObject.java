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
package de.zbit.util.objectwrapper;

import java.io.Serializable;

/**
 * Neccessary for extending supertypes. They can not be extended by Object directly.
 * <p>Furthermore, this class implements the {@link Serializable} interface and can thus
 * be used in every class that requires a {@link Serializable} object.
 * 
 * @author Clemens Wrzodek
 * @version $Rev$
 * @since 1.0
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
