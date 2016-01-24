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
package de.zbit.util.progressbar.gui;

import java.text.MessageFormat;

import javax.swing.JProgressBar;

import de.zbit.util.ResourceManager;
import de.zbit.util.Utils;
import de.zbit.util.progressbar.AbstractProgressBar;

/**
 * ProgressBarSwing - a swing implementation that wraps the
 * AbstractProgressBar around a {@link JProgressBar}.
 * @author Clemens Wrzodek
 * @version $Rev$
 * @since 1.0
 */
public class ProgressBarSwing extends AbstractProgressBar {
  
  /**
   * Generated serial version identifier
   */
  private static final long serialVersionUID = 2754375775367568812L;
  
  /**
   * 
   */
  private static final String REMAINING_TIME = ResourceManager.getBundle("de.zbit.locales.Labels").getString("REMAINING_TIME");
  
  /**
   * The actual component that is used to draw the progress.
   */
  private JProgressBar progressBar;
  
  /**
   * Create a new instance of {@link AbstractProgressBar} on the
   * given {@link JProgressBar}.
   * @param progressBar
   */
  public ProgressBarSwing(JProgressBar progressBar) {
    super();
    setProgressBar(progressBar);
  }
  
  /*=====================================
   * Getters and Setters
   *=====================================*/
  
  /**
   * 
   */
  public JProgressBar getProgressBar() {
    return progressBar;
  }
  
  /**
   * 
   * @param progressBar
   */
  public synchronized void setProgressBar(JProgressBar progressBar) {
    this.progressBar = progressBar;
    if (progressBar != null) {
      initProgressBar();
    }
  }
  
  /*=====================================
   * ProgressBar
   *=====================================*/
  
  /**
   * 
   */
  public void initProgressBar() {
    // initializes Progress bar
    if ((progressBar != null) && (progressBar instanceof JProgressBar)) {
      JProgressBar jprogressbar = progressBar;
      jprogressbar.setMaximum(100);
      jprogressbar.setMinimum(0);
      jprogressbar.setValue(0);
      jprogressbar.setStringPainted(true);
    }
  }
  
  /* (non-Javadoc)
   * @see de.zbit.util.AbstractProgressBar#drawProgressBar(int, double, java.lang.String)
   */
  @Override
  public void drawProgressBar(final int percent, final double miliSecondsRemaining, final String additionalText) {
    if (progressBar instanceof JProgressBar) {
      JProgressBar jprogressbar = progressBar;
      jprogressbar.setValue(percent);
      
      String s = percent + " %";
      if (miliSecondsRemaining > 0) {
        s += ' ' + MessageFormat.format(REMAINING_TIME, Utils.getPrettyTimeString((long) miliSecondsRemaining));
      }
      if ((additionalText != null) && (additionalText.length() > 0)) {
        s += " - " + additionalText;
      }
      
      jprogressbar.setString(s);
    }
  }
  
  /* (non-Javadoc)
   * @see de.zbit.util.AbstractProgressBar#finished_impl()
   */
  @Override
  public void finished_impl() {
    // Set Progressbar to 100%
    drawProgressBar(100, 0, "");
  }
  
}
