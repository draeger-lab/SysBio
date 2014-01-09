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
package de.zbit.util;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import de.zbit.util.objectwrapper.ValuePair;

/**
 * @author Finja B&uml;chel
 * @version $Rev$
 * @since 1.0
 */
public class ValuePairTest {
  ValuePair<String, String> r1, r2, r3;
  
  @Before
  public void setUP() {
    r1 = new ValuePair<String, String>("path:hsa0010", "YWHAB");
    r2 = new ValuePair<String, String>("path:hsa0020", "YWHAB");
    r3 = new ValuePair<String, String>("path:hsa0010", "YWHAB");
  }
  
  
  @Test
  public void pwRankingsEqual() {
    assertEquals(r1.equals(r2), false);
    assertEquals(r1.equals(r3), true);
  }
  
  
}
