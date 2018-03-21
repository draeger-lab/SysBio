import java.io.File;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.SimpleSpeciesReference;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.TidySBMLWriter;
import org.sbml.jsbml.ext.layout.Layout;
import org.sbml.jsbml.ext.layout.LayoutConstants;
import org.sbml.jsbml.ext.layout.LayoutModelPlugin;
import org.sbml.jsbml.ext.layout.SpeciesGlyph;
import org.sbml.jsbml.ext.layout.TextGlyph;

/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2017 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */

/**
 * Reads in a file with wrong BiGG ids and corrects them.
 * Very specific to James' RBC model.
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 */
public class FixBiGGid {
  
  /**
   * @param args infile outfile
   * @throws IOException
   * @throws XMLStreamException
   */
  public static void main(String[] args) throws XMLStreamException, IOException {
    SBMLDocument doc = SBMLReader.read(new File(args[0]));
    Model m = doc.getModel();
    LayoutModelPlugin lmp = (LayoutModelPlugin) m.getPlugin(LayoutConstants.shortLabel);
    for (Species s : m.getListOfSpecies()) {
      String id = s.getId();
      if (!id.startsWith("M_")) {
        /*
         * Generate BiGG id
         */
        StringBuilder newId = new StringBuilder("M");
        if (id.charAt(0) != '_') {
          newId.append('_');
        }
        newId.append(id);
        for (String sub : new String[] {"_L", "_D"}) {
          if (id.contains(sub)) {
            newId.insert(newId.indexOf(sub), '_');
          }
        }
        newId.insert(newId.length() - 1, '_');
        
        /*
         * Replace old id with new id
         */
        if ((lmp != null) && (lmp.isSetListOfLayouts())) {
          for (Layout l : lmp.getListOfLayouts()) {
            if (l.isSetListOfSpeciesGlyphs()) {
              for (SpeciesGlyph sg : l.getListOfSpeciesGlyphs()) {
                if (sg.getSpecies().equals(id)) {
                  sg.setSpecies(newId.toString());
                }
              }
            }
            if (l.isSetListOfTextGlyphs()) {
              for (TextGlyph tg : l.getListOfTextGlyphs()) {
                if (tg.getOriginOfText().equals(id)) {
                  tg.setOriginOfText(newId.toString());
                }
              }
            }
          }
        }
        if (m.isSetListOfReactions()) {
          for (Reaction r : m.getListOfReactions()) {
            if (r.isSetListOfReactants()) {
              replaceIdReference(r.getListOfReactants(), id, newId.toString());
            }
            if (r.isSetListOfProducts()) {
              replaceIdReference(r.getListOfProducts(), id, newId.toString());
            }
            if (r.isSetListOfModifiers()) {
              replaceIdReference(r.getListOfModifiers(), id, newId.toString());
            }
          }
        }
        s.setId(newId.toString());
      }
      System.out.println(s.getName() + "\t" + s.getId());
    }
    TidySBMLWriter.write(doc, new File(args[1]), ' ', (short) 2);
  }
  
  /**
   * @param r
   */
  private static void replaceIdReference(ListOf<? extends SimpleSpeciesReference> l, String oldId, String newId) {
    for (SimpleSpeciesReference sr : l) {
      if (sr.getSpecies().equals(oldId)) {
        sr.setSpecies(newId);
      }
    }
  }
  
}
