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

import java.io.File;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.Species;

/**
 * @author Stephanie Tscherneck
 * @version $Rev$
 */
public class SetInitialvalueNaN {
  
  /**
   * 
   * @param input
   * @param output
   * @throws XMLStreamException
   * @throws IOException
   */
  public SetInitialvalueNaN(File input, String output) throws XMLStreamException, IOException{
    SBMLDocument doc = SBMLReader.read(input);
    zeroToNaN(doc, output);
  }
  
  /**
   * 
   * @param doc
   * @param output
   * @throws SBMLException
   * @throws XMLStreamException
   * @throws IOException
   */
  private void zeroToNaN(SBMLDocument doc, String output) throws SBMLException, XMLStreamException, IOException {
    Model m = doc.getModel();
    if (m.isSetListOfSpecies()) {
      for (Species s : m.getListOfSpecies()) {
        if (!s.isSetValue() || (s.getValue() == 0d)) {
          s.setInitialConcentration(Double.NaN);
        }
        s.setHasOnlySubstanceUnits(false);
      }
    }
    SBMLWriter w = new SBMLWriter();
    w.write(doc, output);
  }
  
  /**
   * @param args
   * @throws IOException
   * @throws XMLStreamException
   */
  public static void main(String[] args) throws XMLStreamException, IOException {
    File file = new File(args[0]);
    new SetInitialvalueNaN(file, args[1]);
  }
  
}
