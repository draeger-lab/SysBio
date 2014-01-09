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
package de.zbit.gui.csv;

import javax.swing.JFrame;
import javax.swing.UIManager;

/**
 * @author Andreas Dr&auml;ger
 * @since 1.1
 * @version $Rev$
 */
public class CSVReaderOptionPanelTest {
  /**
   * Just for testing purposes.
   */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			JFrame parent = new JFrame();
			parent.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			System.out.println(CSVReaderOptionPanel.showDialog(parent,"files/sample.csv.txt",
				CSVReaderOptionPanel.getCSVOptionsString()).getNumberOfDataLines());
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
