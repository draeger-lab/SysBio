/* $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2015 by the University of Tuebingen, Germany.
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

import java.awt.BorderLayout;
import java.awt.Container;
import java.util.logging.Logger;

import javax.swing.JFrame;

/**
 * @author Andreas Dr&auml;ger
 * @since 1.1
 * @version $Rev$
 */
public class StatusBarTest {

	private static final transient Logger log = Logger.getLogger(StatusBarTest.class.getName()); 
	
	/**
	 * JUST FOR TESTING AND DEMONSTRATION
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		JFrame frame = new JFrame();
		frame.setBounds(200, 200, 600, 200);
		frame.setTitle("Status bar test");

		StatusBar bar = new StatusBar();
		Container c = frame.getContentPane();
		c.setLayout(new BorderLayout());
		c.add(bar, BorderLayout.SOUTH);
		
		bar.registerAsIconListenerFor(frame);

		log.warning("Test log message.");

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
	
}
