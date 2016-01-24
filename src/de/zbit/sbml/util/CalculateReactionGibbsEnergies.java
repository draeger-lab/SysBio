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

package de.zbit.sbml.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.SpeciesReference;


/**
 * @author Stephanie Tscherneck
 * @version $Rev$
 */
public class CalculateReactionGibbsEnergies {
  
  private static final transient Logger logger = Logger.getLogger(CalculateReactionGibbsEnergies.class.getName());
  
  private HashMap<String, Double> gibbsMap = new HashMap<String, Double>();
  
  /**
   * calculates the Gibbs energies for all reactions of a given sbml model file,
   *   using metabolites Gibbs energies, which are provided in another file
   * 
   * @param model
   * @param metabolitesGibbsFile
   * @param output
   * @throws XMLStreamException
   * @throws IOException
   */
  public CalculateReactionGibbsEnergies(File model, String metabolitesGibbsFile, String output) throws XMLStreamException, IOException {
    this(SBMLReader.read(model), metabolitesGibbsFile, output);
  }
  
  /**
   * calculates the Gibbs energies for all reactions of a given sbml document,
   *   using metabolites Gibbs energies, which are provided in another file
   * 
   * @param doc
   * @param metabolitesGibbsFile
   * @param output
   * @throws IOException
   */
  public CalculateReactionGibbsEnergies(SBMLDocument doc, String metabolitesGibbsFile, String output) throws IOException{
    FileWriter fw = openFile(output, false);
    fillGibbsMap(metabolitesGibbsFile);
    
    fw.write("id\t[kJ/mol]\n");
    //get the list of reactions
    Model m = doc.getModel();
    
    //for each reaction get the ListOf reactants and products incl. stoichiometry,
    //then calculate the Gibbs energy,
    //then write it to the outputfile
    if (m.isSetListOfReactions()) {
      for (Reaction r : m.getListOfReactions()) {
        
        String[] reactants = getListOfReactants(r);
        String[] products = getListOfProducts(r);
        
        Double gEnergy = calculateGibbsEnergies(reactants, products);
        fw.write(r.getId() + "\t" + gEnergy + "\n");
      }
    }
    logger.info("output is written to: " + output);
    closeFile(fw);
  }
  
  /**
   * 
   * @param reactants
   * @param products
   * @return
   */
  private Double calculateGibbsEnergies(String[] reactants, String[] products) {
    Double leftSum = 0.0;
    Double rightSum = 0.0;
    
    // example of s: "1.0 \t HC00032_i"
    for (String s : reactants) {
      String[] helper = s.split("\t");
      if (gibbsMap.get(helper[1]) != null) {
        leftSum += (Double.valueOf(helper[0]) * gibbsMap.get(helper[1]));
      }
      else {
        System.out.println("no value for: " + helper[1]);
      }
    }
    for (String s : products) {
      String[] helper = s.split("\t");
      if (gibbsMap.get(helper[1]) != null) {
        rightSum += (Double.valueOf(helper[0]) * gibbsMap.get(helper[1]));
      }
      else {
        System.out.println("no value for: " + helper[1]);
      }
    }
    return (rightSum - leftSum);
  }
  
  /**
   * 
   * @param gibbsFile
   * @throws IOException
   */
  private void fillGibbsMap(String gibbsFile) throws IOException{
    String line;
    BufferedReader input = new BufferedReader(new FileReader(gibbsFile));
    boolean flagHeader = true;
    int colId = 1000000;
    int colGibbs = 1000000;
    
    while ((line = input.readLine()) != null) {
      String[] help = line.split("[ \t]");
      if ((colId != 1000000) && (colGibbs != 1000000) && (colId != colGibbs)) {
        gibbsMap.put(help[colId], Double.valueOf(help[colGibbs]));
      }
      // must be placed as second, otherwise the first row will tried to put in the @param gibbsMap
      if (flagHeader) {
        boolean flagID = false;
        boolean flagkJperMole = false;
        for (int i=0; i<help.length; i++) {
          if (help[i].contains("id")) {
            colId = i;
            flagID = true;
          }
          else if (help[i].equals("[kJ/mol]")) {
            colGibbs = i;
            flagkJperMole = true;
          }
        }
        if (!flagID || !flagkJperMole) {
          logger.info("columns should named \"id\" and \"[kJ/mol]\"");
          System.exit(0);
        }
        flagHeader = false;
      }
    }
    input.close();
  }
  
  /**
   * 
   * @param r
   * @return speciesIds
   */
  private String[] getListOfProducts(Reaction r) {
    String[] speciesIds = new String[0];
    if (r.isSetListOfProducts()) {
      for (SpeciesReference specRef : r.getListOfProducts()) {
        speciesIds = addEntry(speciesIds, (specRef.getStoichiometry() + "\t" + specRef.getSpeciesInstance().getId()));
      }
    }
    return speciesIds;
  }
  
  /**
   * 
   * @param r
   * @return speciesIds
   */
  private String[] getListOfReactants(Reaction r) {
    String[] speciesIds = new String[0];
    if (r.isSetListOfReactants()) {
      for (SpeciesReference specRef : r.getListOfReactants()) {
        speciesIds = addEntry(speciesIds, (specRef.getStoichiometry() + "\t" + specRef.getSpeciesInstance().getId()));
      }
    }
    return speciesIds;
  }
  
  /**
   * 
   * @param old
   * @param newPart
   * @return
   */
  private String[] addEntry(String[] old, String newPart) {
    String[] newer = new String[(old.length + 1)];
    System.arraycopy(old, 0, newer, 0, (old.length));
    newer[old.length] = newPart;
    return newer;
  }
  
  /**
   * 
   * @param outputfile
   * @param enlarge
   * @return
   * @throws IOException
   */
  private FileWriter openFile(String file, boolean enlarge) throws IOException{
    FileWriter fw = new FileWriter(file ,enlarge);
    return fw;
  }
  
  /**
   * 
   * @param fw
   * @throws IOException
   */
  private void closeFile(FileWriter fw) throws IOException{
    fw.close();
  }
  
  /**
   * @param args
   * @throws IOException
   * @throws XMLStreamException
   */
  public static void main(String[] args) throws XMLStreamException, IOException {
    // what we need:
    // as first: the model itself,
    // as second: a file with the gibbs energies of the metabolites, e.g. Information taken from BioPath,
    // as third: the possibility to name a result file,
    // otherwise taking a default file in the same directory like the model
    
    File model = new File(args[0]);
    String gibbsEnergies = args[1];
    String result = new File(gibbsEnergies).getParent() + "/gibbsEnergyResult";
    if (args.length == 3) {
      result = args[2];
    }
    new CalculateReactionGibbsEnergies(model, gibbsEnergies, result);
  }
  
}
