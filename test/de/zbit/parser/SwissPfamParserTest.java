package de.zbit.parser;

import java.io.BufferedReader;
import java.io.FileReader;



public class SwissPfamParserTest {
  
  /**
   * @param args
   */
  public static void main(String[] args) {    
    try {
      String pfamFile = "J:/swisspfam";
      String outTxt = "J:/prot_dom.txt", outHumanTxt = "J:/prot_dom_HUMAN.txt", outMouseTxt = "J:/prot_dom_MOUSE.txt";
      String outZip = "J:/prot_dom.zip", outHumanZip = "prot_dom_HUMAN.zip", outMouseZip = "prot_dom_MOUSE.zip";
      
      
      
      SwisspfamParser sp = new SwisspfamParser();
      sp.parseFile(pfamFile, outTxt, false, false);
      
      String species = "_HUMAN";
      sp.getSpeciesProtDomFile(outTxt, outHumanTxt, species);
      species = "_MOUSE";
      sp.getSpeciesProtDomFile(outTxt, outMouseTxt, species);
      
      SwisspfamParser.reformatProt_Dom_Files(new BufferedReader(new FileReader(outHumanTxt)), outHumanZip);
      SwisspfamParser.reformatProt_Dom_Files(new BufferedReader(new FileReader(outMouseTxt)), outMouseZip);
      SwisspfamParser.reformatProt_Dom_Files(new BufferedReader(new FileReader(outTxt)), outZip);
      
    } catch (Exception e) {
      System.err.printf("Not able to perform Swisspfam reading files entered.", e);
    }
  }

}
