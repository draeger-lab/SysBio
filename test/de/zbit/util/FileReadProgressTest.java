/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2016 by the University of Tuebingen, Germany.
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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.zbit.util.progressbar.FileReadProgress;


/**
 * NOTE: To run this from command line, go to the "bin" directory and execute:
 * 
 * <pre>java -cp ../lib/junit-4.3.1.jar:. org.junit.runner.JUnitCore de.zbit.util.FileReadProgressTest</pre>
 * 
 * @author Florian Mittag
 * @version $Rev$
 * @since 1.0
 */
public class FileReadProgressTest {

  public static FileReadProgress frp;
  
  @Before
  public void setUp() throws Exception {
    frp = new FileReadProgress(150);
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testProgressSameLine() throws InterruptedException {
    frp.setPrintProgessInSameLine(true);
    for( int i = 0; i < 150; i++ ) {
      frp.progress(1);
      Thread.sleep(30);
    }
    System.out.println();
  }

  @Test
  public void testProgressNewLine() throws InterruptedException {
    frp.setPrintProgessInSameLine(false);
    for( int i = 0; i < 150; i++ ) {
      frp.progress(1);
      Thread.sleep(30);
    }
    System.out.println();
  }
}
