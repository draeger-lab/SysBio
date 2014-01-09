/* $Id$
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
package de.zbit.gui;

import java.util.Arrays;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.UIManager;

/**
 * @author Andreas Dr&auml;ger
 * @since 1.1
 * @version $Rev$
 */
public class JLabeledComponentTest {
	
	/**
	 * A {@link Logger} for this class.
	 */
	private static final transient Logger logger = Logger.getLogger(JLabeledComponentTest.class.getName()); 
	
  /**
   * Just for testing purposes.
   */
  public static void main(String[] args) {
    String[][] sug = new String[][]{{"A","B","C"}, {"A","D","E"}, {"A","F","G"}};
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      JFrame parent = new JFrame();
      parent.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      String[] ret = JLabeledComponent.showDialog(parent,"My title", new String[]{"1.", "2.", "3."}, sug, false);
      if (ret == null) {
      	logger.info("Cancelled");
      } else {
      	logger.fine(Arrays.deepToString(ret));
      }
      System.exit(0);
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    /*JFrame frame = new JFrame();
      frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
      try {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        JLabeledComponent c = new JLabeledComponent("test", false, new String[]{"header1", "header2"});
        frame.getContentPane().add(c);
      } catch (Exception e) {
        e.printStackTrace();
      }
      frame.pack();
      frame.setVisible(true);*/
  }
  
}
