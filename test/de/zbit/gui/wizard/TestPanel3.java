/*
 * $Id$
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
package de.zbit.gui.wizard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;
import javax.swing.border.EmptyBorder;

/**
 * This implementation is based on the tutorial for Wizads in Swing, retrieved
 * from http://java.sun.com/developer/technicalArticles/GUI/swing/wizard/ on
 * September 12th, 2011. Author of the tutorial article is Robert Eckstein.
 * 
 * @author Robert Eckstein
 * @author Florian Mittag
 * @version $Rev$
 */
public class TestPanel3 extends JPanel {

  private static final long serialVersionUID = -2341666718806512008L;

  private JLabel anotherBlankSpace;
  private JLabel blankSpace;
  private JLabel jLabel1;
  private JPanel jPanel1;
  private JLabel progressDescription;
  private JProgressBar progressSent;
  private JLabel welcomeTitle;
  private JLabel yetAnotherBlankSpace1;

  private JPanel contentPanel;
  private JLabel iconLabel;
  private JSeparator separator;
  private JLabel textLabel;
  private JPanel titlePanel;

  public TestPanel3() {

    super();

    contentPanel = getContentPanel();
    ImageIcon icon = getImageIcon();

    titlePanel = new javax.swing.JPanel();
    textLabel = new javax.swing.JLabel();
    iconLabel = new javax.swing.JLabel();
    separator = new javax.swing.JSeparator();

    setLayout(new java.awt.BorderLayout());

    titlePanel.setLayout(new java.awt.BorderLayout());
    titlePanel.setBackground(Color.gray);

    textLabel.setBackground(Color.gray);
    textLabel.setFont(new Font("MS Sans Serif", Font.BOLD, 14));
    textLabel.setText("Pretending To Connect To Server");
    textLabel.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));
    textLabel.setOpaque(true);

    iconLabel.setBackground(Color.gray);
    if (icon != null)
      iconLabel.setIcon(icon);

    titlePanel.add(textLabel, BorderLayout.CENTER);
    titlePanel.add(iconLabel, BorderLayout.EAST);
    titlePanel.add(separator, BorderLayout.SOUTH);

    add(titlePanel, BorderLayout.NORTH);
    JPanel secondaryPanel = new JPanel();
    secondaryPanel.add(contentPanel, BorderLayout.NORTH);
    add(secondaryPanel, BorderLayout.WEST);

  }

  public void setProgressText(String s) {
    progressDescription.setText(s);
  }

  public void setProgressValue(int i) {
    progressSent.setValue(i);
  }

  private JPanel getContentPanel() {

    JPanel contentPanel1 = new JPanel();

    welcomeTitle = new JLabel();
    jPanel1 = new JPanel();
    blankSpace = new JLabel();
    progressSent = new JProgressBar();
    progressDescription = new JLabel();
    anotherBlankSpace = new JLabel();
    yetAnotherBlankSpace1 = new JLabel();
    jLabel1 = new JLabel();

    contentPanel1.setLayout(new java.awt.BorderLayout());

    welcomeTitle.setText("Now we will pretend to send this data somewhere for approval...");
    contentPanel1.add(welcomeTitle, java.awt.BorderLayout.NORTH);

    jPanel1.setLayout(new java.awt.GridLayout(0, 1));

    jPanel1.add(blankSpace);

    progressSent.setStringPainted(true);
    jPanel1.add(progressSent);

    progressDescription.setFont(new java.awt.Font("MS Sans Serif", 1, 11));
    progressDescription.setText("Connecting to Server...");
    jPanel1.add(progressDescription);

    jPanel1.add(anotherBlankSpace);

    jPanel1.add(yetAnotherBlankSpace1);

    contentPanel1.add(jPanel1, java.awt.BorderLayout.CENTER);

    jLabel1.setText("After the sending is completed, the Back and Finish buttons will enable below.");
    contentPanel1.add(jLabel1, java.awt.BorderLayout.SOUTH);

    return contentPanel1;
  }

  private ImageIcon getImageIcon() {
    return null;
  }

}
