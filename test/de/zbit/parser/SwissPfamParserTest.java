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
package de.zbit.parser;

/**
 * 
 * @version $Rev$
 */
public class SwissPfamParserTest {
  
  /**
   * @param args
   */
  public static void main(String[] args) {
    try {
      String pfamFile = "J:/swisspfam";
      String outTxt = "H:/dbFiles/prot_dom.txt"; //, outHumanTxt = "J:/prot_dom_HUMAN.txt", outMouseTxt = "J:/prot_dom_MOUSE.txt";
      //      String outZip = "J:/prot_dom.zip", outHumanZip = "prot_dom_HUMAN.zip", outMouseZip = "prot_dom_MOUSE.zip";
      
      
      
      SwisspfamParser sp = new SwisspfamParser();
      sp.parseFile(pfamFile, outTxt, false, true, false);
      if(true)
      {
        return;
        //      String species = "_HUMAN";
        //      sp.getSpeciesProtDomFile(outTxt, outHumanTxt, species);
        //      species = "_MOUSE";
        //      sp.getSpeciesProtDomFile(outTxt, outMouseTxt, species);
        //
        //      SwisspfamParser.reformatProt_Dom_Files(new BufferedReader(new FileReader(outHumanTxt)), outHumanZip);
        //      SwisspfamParser.reformatProt_Dom_Files(new BufferedReader(new FileReader(outMouseTxt)), outMouseZip);
        //      SwisspfamParser.reformatProt_Dom_Files(new BufferedReader(new FileReader(outTxt)), outZip);
      }
      
    } catch (Exception e) {
      System.err.printf("Not able to perform Swisspfam reading files entered.", e);
    }
  }
  
}
