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
package de.zbit.gui.prefs;

import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import de.zbit.gui.GUITools;
import de.zbit.gui.layout.LayoutHelper;
import de.zbit.io.filefilter.SBFileFilter;

/**
 * @author Andreas Dr&auml;ger
 * @date 12:07:01
 * @since 1.1
 * @version $Rev$
 */
public class FileSelectorTest {
	
	/**
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		GUITools.initLaF("FileSelector test");
		JPanel p = new JPanel();
		FileSelector selectors[] = FileSelector.createOpenSavePanel(new LayoutHelper(p), System
				.getProperty("user.dir"), false, new SBFileFilter[] { SBFileFilter
				.createSBMLFileFilter() }, System.getProperty("user.dir"), false,
			new SBFileFilter[] { SBFileFilter.createTeXFileFilter() });
		if (JOptionPane.showConfirmDialog(null, p, "Test",
			JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
			for (FileSelector fs : selectors) {
				try {
					System.out.println(fs.getSelectedFile());
				} catch (IOException exc) {
					GUITools.showErrorMessage(null, exc);
				}
			}
		}
		
	}
	
}
