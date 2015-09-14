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
package de.zbit.sbml;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.Species;

import de.zbit.sbml.util.AnnotationUtils;

/**
 * 
 * @author draeger
 *
 */
public class SBMLAnnotationTest {
  
  /**
   * 
   * @param args
   * @throws SBMLException
   * @throws XMLStreamException
   */
  public static void main(String args[]) throws SBMLException, XMLStreamException {
    SBMLDocument doc = new SBMLDocument(3, 1);
    Model m = doc.createModel("m1");
    Compartment c = m.createCompartment("c1");
    Species s = m.createSpecies("s1", c);
    s.addCVTerm(new CVTerm(CVTerm.Qualifier.BQB_IS, AnnotationUtils.convertURN2URI("urn:miriam:obo.chebi:CHEBI:15377")));
    SBMLWriter.write(doc, System.out, ' ', (short) 2);
  }
  
}
