/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2012 by the University of Tuebingen, Germany.
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingWorker;

import de.zbit.graph.gui.TranslatorPanel;
import de.zbit.util.progressbar.AbstractProgressBar;

/**
 * A simple {@link SwingWorker} extension that allows you set
 * setup {@link ActionListener}s for the worker and the worker
 * can fire several actions.
 * 
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public abstract class NotifyingWorker <T, V> extends SwingWorker<T, V> implements ActionListener {

  /**
   * 
   */
  AbstractProgressBar progress = null;
  
  /**
   * 
   */
  List<ActionListener> listeners = null;
  
  /**
   * 
   * @param progress
   */
  public void setProgressBar(AbstractProgressBar progress) {
    this.progress = progress;
  }
  
  /**
   * 
   * @return
   */
  public AbstractProgressBar getProgressBar() {
    return progress;
  }
  
  /**
   * 
   * @param listener
   */
  public void addActionListener(ActionListener listener) {
    if (listeners == null) {
      listeners = new ArrayList<ActionListener>();
    }
    listeners.add(listener);
  }
  
  /**
   * @param actionEvent
   */
  protected void fireActionEvent(ActionEvent actionEvent) {
    if (listeners!=null) {
      for (ActionListener listener : listeners) {
        listener.actionPerformed(actionEvent);
      }
    }
  }

  /* (non-Javadoc)
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent e) {
    if (e.getActionCommand()!=null) {
      if (e.getActionCommand().equals(TranslatorPanel.COMMAND_NEW_PROGRESSBAR)) {
        setProgressBar((AbstractProgressBar) e.getSource());
      }
    }
  }

}
