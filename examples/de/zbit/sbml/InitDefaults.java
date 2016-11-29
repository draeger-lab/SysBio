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
package de.zbit.sbml;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.NamedSBase;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.TidySBMLWriter;
import org.sbml.jsbml.ext.layout.BoundingBox;
import org.sbml.jsbml.ext.layout.Layout;
import org.sbml.jsbml.ext.layout.LayoutConstants;
import org.sbml.jsbml.ext.layout.LayoutModelPlugin;
import org.sbml.jsbml.ext.layout.Point;
import org.sbml.jsbml.ext.layout.ReactionGlyph;
import org.sbml.jsbml.ext.layout.SpeciesGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceRole;
import org.sbml.jsbml.ext.layout.TextGlyph;
import org.sbml.jsbml.util.filters.Filter;

import de.zbit.sbml.layout.SimpleLayoutAlgorithm;

/**
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 */
public class InitDefaults implements Callable<Void> {
  
  private SBMLDocument doc;
  private int level;
  private int version;
  private File targetFile;
  
  /**
   * 
   * @param doc
   * @param level
   * @param version
   * @param targetFile
   */
  public InitDefaults(SBMLDocument doc, int level, int version, File targetFile) {
    this.doc = doc;
    this.level = level;
    this.version = version;
    this.targetFile = targetFile;
  }
  
  /**
   * @param args
   *        input SBML file, desired level/version (as separate numbers) for
   *        initializing default values, output SBML file (absolute paths)
   * @throws IOException
   * @throws XMLStreamException
   */
  public static void main(String[] args) throws SBMLException, NumberFormatException, XMLStreamException, IOException {
    new InitDefaults(SBMLReader.read(new File(args[0])), Integer.parseInt(args[1]), Integer.parseInt(args[2]), new File(args[3])).call();
  }
  
  /**
   * 
   * @param l
   * @param level
   * @param version
   */
  private <T extends SBase> void initDefaults(ListOf<T> l, int level, int version) {
    for (T t : l) {
      initDefaults(t, level, version);
    }
  }
  
  /**
   * @param level
   * @param version
   * @param sbase
   */
  private <T extends SBase> void initDefaults(T sbase, int level, int version) {
    try {
      Method method = sbase.getClass().getDeclaredMethod("initDefaults", int.class, int.class, boolean.class);
      if (method != null) {
        Map<String, String> attributes = sbase.writeXMLAttributes();
        method.invoke(sbase, level, version, true);
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
          String prefix = "";
          String attribute = entry.getKey();
          if (attribute.contains(":")) {
            prefix = attribute.substring(0, attribute.indexOf(':'));
          }
          sbase.readAttribute(entry.getKey(), prefix, entry.getValue());
        }
      }
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }
  
  /* (non-Javadoc)
   * @see java.util.concurrent.Callable#call()
   */
  @Override
  public Void call() throws XMLStreamException, SBMLException, IOException {
    
    updateMIRIAMurns(doc);
    
    if (doc.isSetModel()) {
      Model model = doc.getModel();
      
      LayoutModelPlugin lmp = (LayoutModelPlugin) model.getExtension(LayoutConstants.shortLabel);
      if ((lmp != null) && (lmp.isSetListOfLayouts())) {
        for (Layout layout : lmp.getListOfLayouts()) {
          if (layout.isSetListOfReactionGlyphs()) {
            for (ReactionGlyph rg : layout.getListOfReactionGlyphs()) {
              if (rg.isSetListOfSpeciesReferenceGlyphs()) {
                for (SpeciesReferenceGlyph srg : rg.getListOfSpeciesReferenceGlyphs()) {
                  if (rg.isSetBoundingBox() && !srg.isSetBoundingBox() && !srg.isSetCurve() && srg.isSetSpeciesGlyph()) {
                    SpeciesGlyph sg = srg.getSpeciesGlyphInstance();
                    if ((sg != null) && sg.isSetBoundingBox()) {
                      Point rgPos = rg.getBoundingBox().getPosition();
                      Point sgPos = sg.getBoundingBox().getPosition();
                      if ((rgPos != null) && (sgPos != null)) {
                        double width = Math.abs(sgPos.x() - rgPos.x());
                        double height = Math.abs(sgPos.y() - rgPos.y());
                        double depth = 1d;
                        Point center = null;
                        if (srg.getRole().equals(SpeciesReferenceRole.SUBSTRATE) || srg.getRole().equals(SpeciesReferenceRole.SIDESUBSTRATE)) {
                          center = SimpleLayoutAlgorithm.calculateCenter(sg, level, version);
                        } else {
                          center = SimpleLayoutAlgorithm.calculateCenter(rg, level, version);
                        }
                        double x = center.getX();
                        double y = center.getY();
                        double z = sgPos.z();
                        BoundingBox bb = srg.createBoundingBox(width, height, depth, x, y, z);
                      }
                    }
                  }
                }
              }
            }
          }
          if (layout.isSetListOfSpeciesGlyphs()) {
            for (SpeciesGlyph sg : layout.getListOfSpeciesGlyphs()) {
              NamedSBase nsb = sg.getReferenceInstance();
              // Sync SBO terms
              if (nsb != null) {
                if (nsb.isSetSBOTerm() && !sg.isSetSBOTerm()) {
                  sg.setSBOTerm(nsb.getSBOTerm());
                } else if (sg.isSetSBOTerm() && !nsb.isSetSBOTerm()) {
                  nsb.setSBOTerm(sg.getSBOTerm());
                }
              }
            }
          }
          if (layout.isSetListOfTextGlyphs()) {
            for (TextGlyph tg : layout.getListOfTextGlyphs()) {
              if (!tg.isSetBoundingBox() && tg.isSetGraphicalObject()) {
                tg.setBoundingBox(tg.getGraphicalObjectInstance().getBoundingBox().clone());
              }
            }
          }
        }
      }
    }
    
    doc.filter(new Filter() {
      /* (non-Javadoc)
       * @see org.sbml.jsbml.util.filters.Filter#accepts(java.lang.Object)
       */
      @Override
      public boolean accepts(Object o) {
        if (o instanceof SBase) {
          initDefaults((SBase) o, level, version);
        }
        return false;
      }
    });
    
    TidySBMLWriter.write(doc, targetFile, ' ', (short) 2);
    
    return null;
  }
  
  /**
   * 
   * @param sbase
   */
  public void updateMIRIAMurns(SBase sbase) {
    sbase.filter(new Filter() {
      @Override
      public boolean accepts(Object o) {
        if ((o instanceof SBase) && (((SBase) o).getCVTermCount() > 0)) {
          for (CVTerm term : ((SBase) o).getCVTerms()) {
            for (int i = term.getResourceCount() - 1; i >= 0; i--) {
              String uri = term.getResourceURI(i);
              if (uri.startsWith("urn:miriam:")) {
                term.addResource(i, "http://identifiers.org/" + term.removeResource(i).substring(11).replace(':', '/'));
              }
            }
          }
        }
        return false;
      }
    });
    
  }
  
}
