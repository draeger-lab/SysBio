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
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.SpeciesReference;

public class SetStoichiometry {
  
  private static final transient Logger logger = Logger.getLogger(SetStoichiometry.class.getName());
  private SBMLDocument doc;
  
  public SetStoichiometry(File modelFile) throws XMLStreamException, IOException {
    this(SBMLReader.read(modelFile));
  }
  
  public SetStoichiometry(SBMLDocument doc) {
    this.doc = doc;
    Model m = doc.getModel();
    checkAndSetStoichiometry(m);
  }
  
  private void checkAndSetStoichiometry(Model m) {
    if (m.isSetListOfReactions()) {
      for (Reaction r : m.getListOfReactions()) {
        if (r.isSetListOfReactants()) {
          for (SpeciesReference sr : r.getListOfReactants()) {
            if ((!sr.isSetStoichiometry()) && (m.getLevel() < 3)) {
              sr.setStoichiometry(1d);
            }
          }
        }
        if (r.isSetListOfProducts()) {
          for (SpeciesReference sr : r.getListOfProducts()) {
            if ((!sr.isSetStoichiometry()) && (m.getLevel() < 3)) {
              sr.setStoichiometry(1d);
            }
          }
        }
      }
    }
  }
  
  private void writeDocument(File modelFile) throws SBMLException, XMLStreamException, IOException {
    SBMLWriter.write(doc, modelFile, ' ', (short) 2);
    logger.info("stoichiometry is set");
  }
  
  public static void main(String[] args) throws XMLStreamException, IOException {
    logger.info(args[0]);
    
    File modelFile = new File(args[0]);
    SetStoichiometry setter = new SetStoichiometry(modelFile);
    setter.writeDocument(modelFile);
  }
  
}
