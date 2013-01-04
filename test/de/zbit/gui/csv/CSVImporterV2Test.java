/* $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2013 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.gui.csv;

import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

/**
 * @author Andreas Dr&auml;ger
 * @since 1.1
 * @version $Rev$
 */
public class CSVImporterV2Test {
	
  /**
   * Just for DEMO and testing purposes.
   */
  public static void main(String[] args) {
    // Define the columns we expect to read from the file
    ExpectedColumn[] exp = new ExpectedColumn[3];
    
    exp[0] = new ExpectedColumn("Signal");
    exp[0].type=new String[]{"Pval","Fold change"};
    exp[0].setRegExPatternForInitialSuggestion("\\d+");
    exp[0].renameAllowed=true;
    exp[0].multiSelectionAllowed=true;
    exp[0].multiSelectionOnlyWithDifferentType=true;
    
    exp[1] = new ExpectedColumn("Score");
    exp[1].type=new String[]{"a1","a2","a3"};
    
    exp[2] = new ExpectedColumn("Chromosome");
    exp[2].setRegExPatternForInitialSuggestion("chr.*");
    
    // Graphically import the file and let the user assign the columns
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      JFrame parent = new JFrame();
      parent.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      CSVImporterV2 importer = CSVImporterV2.showDialog(parent,"files/sample2.csv.txt","Data import",exp);
      System.out.println("Button pressed    : " + importer.getButtonPressed());
      System.out.println("Dialog approved?  : " + (importer.getButtonPressed()==JOptionPane.OK_OPTION) );
      System.out.println("Approved CSVReader: " + importer.getApprovedCSVReader());
      if ((importer.getButtonPressed()==JOptionPane.OK_OPTION)) {
        System.out.println("Column assignments: ");
        for (ExpectedColumn e: exp) {
          System.out.println("  " + e.getName() + ": " + Arrays.deepToString(e.getAssignedColumns().toArray()));
          if (e.isSetTypeSelection()) {
            System.out.println("    Types: " + Arrays.deepToString(e.getAssignedTypeForEachColumn().toArray()));
          }
        }
      }
      System.exit(0);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
}
