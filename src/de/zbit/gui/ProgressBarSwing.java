/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2011 by the University of Tuebingen, Germany.
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

import javax.swing.JProgressBar;

import de.zbit.util.AbstractProgressBar;
import de.zbit.util.Utils;

/**
 * ProgressBarSwing - a swing implementation that wraps the
 * AbstractProgressBar around a JProgressBar.
 * @author Clemens Wrzodek
 * @version $Rev$
 * @since 1.0
 */
public class ProgressBarSwing extends AbstractProgressBar {
  private static final long serialVersionUID = 2754375775367568812L;
  
  /**
   * The actual component that is used to draw the progress.
   */
  private JProgressBar progressBar;
  
  /**
   * Create a new instance of AbstractProgressBar on the
   * given JProgressBar.
   * @param progressBar
   */
  public ProgressBarSwing(JProgressBar progressBar) {
    super();
    setProgressBar(progressBar);
  }
  
  /*=====================================
   * Getters and Setters
   *=====================================*/
  

  public JProgressBar getProgressBar() {
    return progressBar;
  }
  public void setProgressBar(JProgressBar progressBar) {
    this.progressBar = progressBar;
    if (progressBar!=null) initProgressBar();
  }
  
  /*=====================================
   * ProgressBar
   *=====================================*/
  
  public void initProgressBar() {
    // initializes Progress bar
    if (progressBar!=null && progressBar instanceof JProgressBar) {
      ((JProgressBar)progressBar).setMaximum(100);
      ((JProgressBar)progressBar).setMinimum(0);
      ((JProgressBar)progressBar).setValue(0);
      ((JProgressBar)progressBar).setStringPainted(true);
    }
  }
  
  /* (non-Javadoc)
   * @see de.zbit.util.AbstractProgressBar#drawProgressBar(int, double, java.lang.String)
   */
  @Override
  protected void drawProgressBar(int percent, double miliSecondsRemaining, String additionalText) {
    if (progressBar instanceof JProgressBar) {
      ((JProgressBar)progressBar).setValue(percent);
      
      String s = percent + "%";
      if (miliSecondsRemaining>0) {
        s +=" ETR: "+ Utils.getPrettyTimeString((long) miliSecondsRemaining);
      }
      if (additionalText!=null && additionalText.length()>0) {
        s += " - " + additionalText;
      }
      
      ((JProgressBar)progressBar).setString(s);
    }
  }

  /* (non-Javadoc)
   * @see de.zbit.util.AbstractProgressBar#finished_impl()
   */
  @Override
  public void finished_impl() {
    // Set Progressbar to 100%
    drawProgressBar(100,0,"");
  }

}
