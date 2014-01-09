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
package de.zbit.gui;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

/**
 * Displays the output of some external process in a {@link JTextArea} that is
 * displayed on a {@link JDialog}.
 * 
 * @author Andreas Dr&auml;ger
 * @author Clemens Wrzodek
 * @date 14:27:04
 * @since 1.1
 * @version $Rev$
 */
public class ProcessObservationWorker extends SwingWorker<Void, String> {
	
	private JTextArea area;
	private boolean closeWindowAutomaticallyWhenDone;
	private JScrollPane pane;
	private Process process;

	/**
	 * @param builder 
	 * @param area 
	 * @param parent 
	 * @param closeWindowAutomaticallyWhenDone 
	 * 
	 */
	public ProcessObservationWorker(Process process, JTextArea area, Frame parent, boolean closeWindowAutomaticallyWhenDone) {
		super();
		this.process = process;
		this.area = area;
		this.closeWindowAutomaticallyWhenDone = closeWindowAutomaticallyWhenDone;
		
    area.setEditable(false);
    pane = new JScrollPane(area);
    pane.setPreferredSize(new Dimension(480, 240));
    JOptionPane optPane = new JOptionPane(pane, JOptionPane.INFORMATION_MESSAGE);
    JDialog dialog = optPane.createDialog(parent, parent.getTitle());
    dialog.setModal(false);
    GUITools.disableOkButton(area);
    dialog.setVisible(true);
    dialog.setModal(true);
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	@Override
	protected Void doInBackground() throws Exception {
    // Update the window, as the process is executing
    String line;
    BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
    while ((line = in.readLine()) != null) {
      publish(line);
    }
    return null;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#done()
	 */
	@Override
	protected void done() {
    // As soon as the process is done, enable the ok button again
    Window w = GUITools.getParentWindow(area);
    if (closeWindowAutomaticallyWhenDone) {
      if (w != null) {
      	w.dispose();
      }
    } else {
      GUITools.enableOkButtonIfAllComponentsReady(w);
    }
	}

	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#process(java.util.List)
	 */
  @Override
	protected void process(List<String> chunks) {
		for (String line : chunks) {
			area.append(line + '\n');
		}
		// Scroll down
    area.setCaretPosition(area.getDocument().getLength());
	}
	
}
