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
package de.zbit.gui.wizard;

import de.zbit.gui.GUITools;

/**
 * This implementation is based on the tutorial for Wizads in Swing, retrieved
 * from http://java.sun.com/developer/technicalArticles/GUI/swing/wizard/ on
 * September 12th, 2011. Author of the tutorial article is Robert Eckstein.
 * 
 * @author Robert Eckstein
 * @author Florian Mittag
 * @version $Rev$
 */
public class Main {

  public static void main(String[] args) {

    GUITools.initLaF("Wizard Test");

    Wizard wizard = new Wizard();
    wizard.getDialog().setTitle("Test Wizard Dialog");
    wizard.setWarningVisible(false);

    WizardPanelDescriptor descriptor1 = new TestPanel1Descriptor();
    wizard.registerWizardPanel(TestPanel1Descriptor.IDENTIFIER, descriptor1);

    WizardPanelDescriptor descriptor2 = new TestPanel2Descriptor();
    wizard.registerWizardPanel(TestPanel2Descriptor.IDENTIFIER, descriptor2);

    WizardPanelDescriptor descriptor3 = new TestPanel3Descriptor();
    wizard.registerWizardPanel(TestPanel3Descriptor.IDENTIFIER, descriptor3);

    wizard.setCurrentPanel(TestPanel1Descriptor.IDENTIFIER);

    int ret = wizard.showModalDialog();

    System.out.println("Dialog return code is (0=Finish,1=Cancel,2=Error): " + ret);
    System.out.println("Second panel selection is: " + (((TestPanel2) descriptor2.getPanelComponent()).getRadioButtonSelected()));

    System.exit(0);

  }

}
