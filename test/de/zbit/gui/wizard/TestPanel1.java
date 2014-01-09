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
package de.zbit.gui.wizard;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Insets;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

/**
 * This implementation is based on the tutorial for Wizads in Swing, retrieved
 * from http://java.sun.com/developer/technicalArticles/GUI/swing/wizard/ on
 * September 12th, 2011. Author of the tutorial article is Robert Eckstein.
 * 
 * @author Robert Eckstein
 * @author Florian Mittag
 * @version $Rev$
 */
public class TestPanel1 extends JPanel {

  /**
   * 
   */
  private static final long serialVersionUID = -5565084803657209271L;
  private JLabel blankSpace;
  private JLabel jLabel1;
  private JLabel jLabel2;
  private JLabel jLabel3;
  private JLabel jLabel4;
  private JLabel jLabel5;
  private JLabel jLabel6;
  private JLabel jLabel7;
  private JLabel jLabel8;
  private JLabel jLabel9;

  private JLabel welcomeTitle;
  private JPanel contentPanel;

  private JLabel iconLabel;
  private ImageIcon icon;

  public TestPanel1() {

    iconLabel = new JLabel();
    contentPanel = getContentPanel();
    contentPanel.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));

    icon = getImageIcon();

    setLayout(new java.awt.BorderLayout());

    if (icon != null)
      iconLabel.setIcon(icon);

    iconLabel.setBorder(new EtchedBorder(EtchedBorder.RAISED));

    add(iconLabel, BorderLayout.WEST);

    JPanel secondaryPanel = new JPanel();
    secondaryPanel.add(contentPanel, BorderLayout.NORTH);
    add(secondaryPanel, BorderLayout.CENTER);
  }

  private JPanel getContentPanel() {

    JPanel contentPanel1 = new JPanel();
    JPanel jPanel1 = new JPanel();

    welcomeTitle = new JLabel();
    blankSpace = new JLabel();
    jLabel1 = new JLabel();
    jLabel2 = new JLabel();
    jLabel3 = new JLabel();
    jLabel4 = new JLabel();
    jLabel5 = new JLabel();
    jLabel7 = new JLabel();
    jLabel6 = new JLabel();
    jLabel8 = new JLabel();
    jLabel9 = new JLabel();

    contentPanel1.setLayout(new java.awt.BorderLayout());

    welcomeTitle.setFont(new java.awt.Font("MS Sans Serif", Font.BOLD, 11));
    welcomeTitle.setText("Welcome to the Wizard Dialog!");
    contentPanel1.add(welcomeTitle, java.awt.BorderLayout.NORTH);

    jPanel1.setLayout(new java.awt.GridLayout(0, 1));

    jPanel1.add(blankSpace);
    jLabel1.setText("This is an example of a wizard dialog, which allows a user to traverse");
    jPanel1.add(jLabel1);
    jLabel2.setText("a number of panels (while entering data) until the wizard has enough ");
    jPanel1.add(jLabel2);
    jLabel3.setText("information to perform whatever end function is necessary. Note that");
    jPanel1.add(jLabel3);
    jLabel4.setText("panels are not necessarily ordered in a linear fashion, but instead in");
    jPanel1.add(jLabel4);
    jLabel5.setText("a tree-like manner (e.g., there may be more than one panel with a");
    jPanel1.add(jLabel5);
    jLabel7.setText("'Finish' button, and it depends on the user's entries as to how they ");
    jPanel1.add(jLabel7);
    jLabel6.setText("traverse the path). That's not the case with this example, however.");
    jPanel1.add(jLabel6);
    jPanel1.add(jLabel8);
    jLabel9.setText("Press the 'Next' button to continue....");
    jPanel1.add(jLabel9);

    contentPanel1.add(jPanel1, java.awt.BorderLayout.CENTER);

    return contentPanel1;

  }

  private ImageIcon getImageIcon() {
    return (ImageIcon) UIManager.get("UT_BM_Rot_RGB_tr_36x64");
  }

}
