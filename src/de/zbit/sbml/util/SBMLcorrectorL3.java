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

import org.sbml.jsbml.Annotation;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.Unit;
import org.sbml.jsbml.UnitDefinition;
import org.sbml.jsbml.ext.layout.AbstractReferenceGlyph;
import org.sbml.jsbml.ext.layout.GraphicalObject;
import org.sbml.jsbml.ext.layout.Layout;
import org.sbml.jsbml.ext.layout.LayoutConstants;
import org.sbml.jsbml.ext.layout.LayoutModelPlugin;
import org.sbml.jsbml.ext.layout.SpeciesGlyph;
import org.sbml.jsbml.ext.layout.TextGlyph;

import de.zbit.util.logging.LogUtil;

/**
 * @author Andreas Dr&auml;ger
 * @date 07:22:27
 * @since 1.1
 * @version $Rev$
 */
public class SBMLcorrectorL3 {
  
  private static final Logger logger = Logger.getLogger(SBMLcorrectorL3.class.getName());
  
  /**
   * @param args
   * @throws IOException
   * @throws XMLStreamException
   */
  public static void main(String[] args) throws XMLStreamException, IOException {
    LogUtil.initializeLogging(SBMLcorrectorL3.class.getPackage().toString());
    SBMLDocument doc = SBMLReader.read(new File(args[0]));
    if (doc.isSetModel()) {
      Model m = doc.getModel();
      if (m.isSetListOfUnitDefinitions()) {
        for (UnitDefinition ud : m.getListOfUnitDefinitions()) {
          correctUnits(ud);
        }
      }
      if (m.isSetListOfCompartments()) {
        for (Compartment c : m.getListOfCompartments()) {
          correctCompartment(c);
        }
      }
      if (m.isSetListOfSpecies()) {
        for (Species s : m.getListOfSpecies()) {
          correctSpecies(s);
        }
      }
      if (m.isSetListOfReactions()) {
        for (Reaction r : m.getListOfReactions()) {
          correctReaction(r);
        }
      }
      String namespace = LayoutConstants.getNamespaceURI(m.getLevel(), m.getVersion());
      if (m.getExtension(namespace) != null) {
        LayoutModelPlugin layoutPlugin = (LayoutModelPlugin) m.getExtension(namespace);
        int i = 1;
        if (layoutPlugin.isSetListOfLayouts()) {
          for (Layout layout : layoutPlugin.getListOfLayouts()) {
            if (!layout.isSetId()) {
              layout.setId("layout_" + SBO.sboNumberString(i));
              logger.info("Created id for layout " + layout.getId());
            }
            correctTextGlyphs(layout);
            //findExtremeSpeciesGlyphs(layout);
            //					for (ReactionGlyph rg : layout.getListOfReactionGlyphs()) {
            //						correctReactionGlyph(rg);
            //					}
          }
        }
      }
    }
    SBMLWriter.write(doc, new File(args[1]), ' ', (short) 2);
  }
  
  //	private static void correctReactionGlyph(ReactionGlyph rg) {
  //		if (rg.isSetId() && !rg.isSetReaction()) {
  //			String id = rg.getId();
  //			int pos = id.lastIndexOf('_');
  //			if ((id.length() > pos - 1) && (pos > 0)) {
  //				id = id.substring(pos + 1);
  //				rg.getModel().containsReaction(id);
  //				logger.info("Setting reaction " + id + " in ReactionGlyph " + rg.getId());
  //				rg.setReaction(id);
  //			}
  //		}
  //	}
  
  public static void removeCellDesignerAnnotation(SBase sbase) throws XMLStreamException {
    if (sbase.isSetAnnotation()) {
      Annotation annotation = sbase.getAnnotation();
      String nonRDF = annotation.getNonRDFannotation().toXMLString();
      if ((nonRDF != null) && (nonRDF.length() > 0) && nonRDF.contains("<celldesigner:extension>")) {
        StringBuilder sb = new StringBuilder();
        sb.append(nonRDF.substring(0, nonRDF.indexOf("<celldesigner:extension>")));
        sb.append(nonRDF.substring(nonRDF.indexOf("</celldesigner:extension>") + 25, nonRDF.length()));
        String newAnnotation = sb.toString().trim();
        if (newAnnotation.length() == 0) {
          annotation.unsetNonRDFannotation();
          if (annotation.isEmpty()) {
            logger.info("Removed the complete annotation from " + sbase);
            sbase.unsetAnnotation();
          } else {
            logger.info("Removed CellDesigner annotation from " + sbase);
          }
        } else if (newAnnotation.length() != nonRDF.length()) {
          logger.info("Removed CellDesigner annotation from " + sbase);
          annotation.setNonRDFAnnotation(newAnnotation);
        }
      }
    }
  }
  
  /**
   * 
   * @param r
   * @throws XMLStreamException
   */
  public static void correctReaction(Reaction r) throws XMLStreamException {
    if (!r.isSetReversible()) {
      logger.info("Setting reversible on " + r);
      r.setReversible(false);
    }
    if (!r.isSetFast()) {
      logger.info("Setting fast on " + r);
      r.setFast(false);
    }
    if (r.isSetListOfReactants()) {
      for (SpeciesReference specRef : r.getListOfReactants()) {
        correctSpeciesReference(specRef);
      }
    }
    if (r.isSetListOfProducts()) {
      for (SpeciesReference specRef : r.getListOfProducts()) {
        correctSpeciesReference(specRef);
      }
    }
    if (r.isSetListOfModifiers()) {
      for (ModifierSpeciesReference specRef : r.getListOfModifiers()) {
        correctModifierSpeciesReference(specRef);
      }
    }
    removeCellDesignerAnnotation(r);
  }
  
  /**
   * 
   * @param specRef
   * @throws XMLStreamException
   */
  public static void correctModifierSpeciesReference(
    ModifierSpeciesReference specRef) throws XMLStreamException {
    removeCellDesignerAnnotation(specRef);
  }
  
  /**
   * 
   * @param specRef
   * @throws XMLStreamException
   */
  public static void correctSpeciesReference(SpeciesReference specRef) throws XMLStreamException {
    if (!specRef.isSetConstant()) {
      logger.info("Setting constant on " + specRef);
      specRef.setConstant(true);
    }
    if (!specRef.isSetStoichiometry() && !specRef.isSetStoichiometryMath()) {
      logger.info("Setting stoichiometry of " + specRef + " to 1.0");
      specRef.setStoichiometry(1d);
    }
    removeCellDesignerAnnotation(specRef);
  }
  
  /**
   * 
   * @param s
   * @throws XMLStreamException
   */
  public static void correctSpecies(Species s) throws XMLStreamException {
    if (!s.isSetHasOnlySubstanceUnits()) {
      logger.info("Setting hasOnlySubstanceUnits on " + s);
      s.setHasOnlySubstanceUnits(true);
    }
    if (!s.isSetBoundaryCondition()) {
      logger.info("Setting boundaryCondition on " + s);
      s.setBoundaryCondition(false);
    }
    if (!s.isSetConstant()) {
      logger.info("Setting constant on " + s);
      s.setConstant(false);
    }
    removeCellDesignerAnnotation(s);
  }
  
  /**
   * 
   * @param c
   */
  public static void correctCompartment(Compartment c) {
    if (!c.isSetConstant()) {
      logger.info("Had to set compartment " + c + " to constant");
      c.setConstant(true);
    }
    if (c.isSetOutside()) {
      logger.info("Had to remove outside attribute on " + c);
      c.unsetOutside();
    }
  }
  
  /**
   * 
   * @param ud
   * @throws XMLStreamException
   */
  public static void correctUnits(UnitDefinition ud) throws XMLStreamException {
    if (ud.isSetListOfUnits()) {
      for (Unit u : ud.getListOfUnits()) {
        boolean change = false;
        if (!u.isSetExponent()) {
          logger.info("Setting exponent to 1d");
          u.setExponent(1d);
          change = true;
        }
        if (!u.isSetScale()) {
          logger.info("Setting scale to 0");
          u.setScale(0);
          change = true;
        }
        if (!u.isSetMultiplier()) {
          logger.info("Setting multiplier to 1d");
          u.setMultiplier(1d);
          change = true;
        }
        if (change) {
          logger.info("Had to correct unit in " + ud);
        }
        removeCellDesignerAnnotation(u);
      }
      removeCellDesignerAnnotation(ud);
    }
  }
  
  /**
   * 
   * @param layout
   */
  public static void findExtremeSpeciesGlyphs(Layout layout) {
    SpeciesGlyph extreme[] = new SpeciesGlyph[4];
    final int leftMost = 0, topMost = 1, rightMost = 2, bottomMost = 3;
    if (layout.isSetListOfSpeciesGlyphs()) {
      for (SpeciesGlyph sg : layout.getListOfSpeciesGlyphs()) {
        for (int i = 0; i < extreme.length; i++) {
          if (extreme[i] == null) {
            extreme[i] = sg;
          } else if (sg.isSetBoundingBox() && sg.getBoundingBox().isSetPosition()) {
            double x, y, z;
            switch (i) {
              case leftMost:
                x = Double.MAX_VALUE;
                if (extreme[i].isSetBoundingBox() && extreme[i].getBoundingBox().isSetPosition()) {
                  x = extreme[i].getBoundingBox().getPosition().getX();
                }
                if (sg.getBoundingBox().getPosition().getX() < x) {
                  extreme[i] = sg;
                }
                break;
              case topMost:
                y = Double.MAX_VALUE;
                if (extreme[i].isSetBoundingBox() && extreme[i].getBoundingBox().isSetPosition()) {
                  y = extreme[i].getBoundingBox().getPosition().getY();
                }
                if (sg.getBoundingBox().getPosition().getY() < y) {
                  extreme[i] = sg;
                }
                break;
              case rightMost:
                x = Double.MIN_VALUE;
                if (extreme[i].isSetBoundingBox() && extreme[i].getBoundingBox().isSetPosition()) {
                  x = extreme[i].getBoundingBox().getPosition().getX();
                }
                if (sg.getBoundingBox().getPosition().getX() > x) {
                  extreme[i] = sg;
                }
                break;
              case bottomMost:
                y = -Double.MAX_VALUE;
                if (extreme[i].isSetBoundingBox() && extreme[i].getBoundingBox().isSetPosition()) {
                  y = extreme[i].getBoundingBox().getPosition().getY();
                }
                if (sg.getBoundingBox().getPosition().getY() > y) {
                  extreme[i] = sg;
                }
                break;
              default:
                break;
            }
          }
        }
      }
    }
    System.out.println("Extreme species in following order: left, top, right, bottom");
    for (SpeciesGlyph sgl : extreme) {
      System.out.println(sgl.getBoundingBox().getPosition());
    }
  }
  
  /**
   * 
   * @param layout
   */
  public static void correctTextGlyphs(Layout layout) {
    if (layout.isSetListOfTextGlyphs()) {
      for (TextGlyph tg : layout.getListOfTextGlyphs()) {
        if (tg.isSetOriginOfText() && tg.isSetGraphicalObject() && tg.getOriginOfText().equals(tg.getGraphicalObject())) {
          GraphicalObject go = tg.getGraphicalObjectInstance();
          if (go instanceof AbstractReferenceGlyph) {
            AbstractReferenceGlyph nsbg = (AbstractReferenceGlyph) go;
            if (nsbg.isSetReference()) {
              tg.setOriginOfText(nsbg.getReference());
            }
          }
        }
      }
    }
  }
  
}
