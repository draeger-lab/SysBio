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


/**
 * This implementation is based on the tutorial for Wizads in Swing, retrieved
 * from http://java.sun.com/developer/technicalArticles/GUI/swing/wizard/ on
 * September 12th, 2011. Author of the tutorial article is Robert Eckstein.
 * 
 * @author Robert Eckstein
 * @author Florian Mittag
 * @version $Rev$
 */
public class TestPanel3Descriptor extends WizardPanelDescriptor {
    
    public static final String IDENTIFIER = "SERVER_CONNECT_PANEL";
    
    TestPanel3 panel3;
    
    public TestPanel3Descriptor() {
        
        panel3 = new TestPanel3();
        setPanelDescriptorIdentifier(IDENTIFIER);
        setPanelComponent(panel3);
        
    }

    public Object getNextPanelDescriptor() {
        return FINISH;
    }
    
    public Object getBackPanelDescriptor() {
        return TestPanel2Descriptor.IDENTIFIER;
    }
    
    
    public void aboutToDisplayPanel() {
        
        panel3.setProgressValue(0);
        panel3.setProgressText("Connecting to Server...");

        getWizard().setNextFinishButtonEnabled(false);
        getWizard().setBackButtonEnabled(false);
        
    }
    
    public void displayingPanel() {

            Thread t = new Thread() {

            public void run() {

                try {
                    Thread.sleep(2000);
                    panel3.setProgressValue(25);
                    panel3.setProgressText("Server Connection Established");
                    Thread.sleep(500);
                    panel3.setProgressValue(50);
                    panel3.setProgressText("Transmitting Data...");
                    Thread.sleep(3000);
                    panel3.setProgressValue(75);
                    panel3.setProgressText("Receiving Acknowledgement...");
                    Thread.sleep(1000);
                    panel3.setProgressValue(100);
                    panel3.setProgressText("Data Successfully Transmitted");

                    getWizard().setNextFinishButtonEnabled(true);
                    getWizard().setBackButtonEnabled(true);

                } catch (InterruptedException e) {
                    
                    panel3.setProgressValue(0);
                    panel3.setProgressText("An Error Has Occurred");
                    
                    getWizard().setBackButtonEnabled(true);
                }

            }
        };

        t.start();
    }
    
    public void aboutToHidePanel() {
        //  Can do something here, but we've chosen not not.
    }    
            
}
