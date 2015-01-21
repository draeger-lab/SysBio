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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * This implementation is based on the tutorial for Wizads in Swing, retrieved
 * from http://java.sun.com/developer/technicalArticles/GUI/swing/wizard/ on
 * September 12th, 2011. Author of the tutorial article is Robert Eckstein.
 * 
 * @author Robert Eckstein
 * @author Florian Mittag
 * @version $Rev$
 */
public class TestPanel2Descriptor extends WizardPanelDescriptor implements ActionListener {
    
    public static final String IDENTIFIER = "CONNECTOR_CHOOSE_PANEL";
    
    TestPanel2 panel2;
    
    public TestPanel2Descriptor() {
        
        panel2 = new TestPanel2();
        panel2.addCheckBoxActionListener(this);
        
        setPanelDescriptorIdentifier(IDENTIFIER);
        setPanelComponent(panel2);
        
    }
    
    public Object getNextPanelDescriptor() {
        return TestPanel3Descriptor.IDENTIFIER;
    }
    
    public Object getBackPanelDescriptor() {
        return TestPanel1Descriptor.IDENTIFIER;
    }
    
    
    public void aboutToDisplayPanel() {
        setNextButtonAccordingToCheckBox();
    }    

    public void actionPerformed(ActionEvent e) {
        setNextButtonAccordingToCheckBox();
    }
            
    
    private void setNextButtonAccordingToCheckBox() {
         if (panel2.isCheckBoxSelected())
            getWizard().setNextFinishButtonEnabled(true);
         else
            getWizard().setNextFinishButtonEnabled(false);           
    
    }
}
