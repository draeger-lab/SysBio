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

package de.zbit.sbml.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Logger;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.ext.layout.BoundingBox;
import org.sbml.jsbml.ext.layout.CompartmentGlyph;
import org.sbml.jsbml.ext.layout.Curve;
import org.sbml.jsbml.ext.layout.CurveSegment;
import org.sbml.jsbml.ext.layout.Dimensions;
import org.sbml.jsbml.ext.layout.GraphicalObject;
import org.sbml.jsbml.ext.layout.Layout;
import org.sbml.jsbml.ext.layout.LayoutConstants;
import org.sbml.jsbml.ext.layout.LayoutModelPlugin;
import org.sbml.jsbml.ext.layout.LineSegment;
import org.sbml.jsbml.ext.layout.Point;
import org.sbml.jsbml.ext.layout.ReactionGlyph;
import org.sbml.jsbml.ext.layout.SpeciesGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceRole;
import org.sbml.jsbml.ext.layout.TextGlyph;

import de.zbit.sbml.util.SBMLtools;

/**
 * @author Stephanie Tscherneck
 * @version $Rev$
 */
public class CellDesignerAnnotationParser implements Runnable {
  
  /**
   * A {@link Logger} for this class.
   */
  private static final transient Logger logger = Logger.getLogger(CellDesignerAnnotationParser.class.getName());
  
  /**
   * Direct link to the layout.
   */
  private Layout layout;
  
  /**
   * The document for which CellDesigner information should be parsed.
   */
  private SBMLDocument sbmlDocument;
  
  /**
   * The corresponding model for the {@link SBMLDocument}
   */
  private Model model;
  
  /**
   * 
   * @param inputFile
   * @throws XMLStreamException
   * @throws IOException
   */
  public CellDesignerAnnotationParser(File xmlFile) throws XMLStreamException, IOException {
    sbmlDocument = SBMLReader.read(xmlFile);
  }
  
  /**
   * 
   * @param doc
   */
  public CellDesignerAnnotationParser(SBMLDocument doc) {
    sbmlDocument = doc;
  }
  
  /**
   * @return the sbmlDocument
   */
  public SBMLDocument getSBMLDocument() {
    return sbmlDocument;
  }
  
  /**
   * 
   * @param doc
   */
  private void initializeLayout(SBMLDocument doc) {
    model = doc.getModel();
    if ((model != null) && (model.getExtension(LayoutConstants.namespaceURI) == null)) {
      LayoutModelPlugin layoutExt = new LayoutModelPlugin(model);
      model.addExtension(LayoutConstants.namespaceURI, layoutExt);
      layout = layoutExt.createLayout("l1");
    }
    // need to set level 3 version 1, necessary for layout extension
    sbmlDocument.setLevel(3);
    sbmlDocument.setVersion(1);
  }
  
  /**
   * 
   * @param inputStream
   * @throws XMLStreamException
   */
  private void readCDLayout(BufferedReader inputStream) throws XMLStreamException {
    initializeLayout(sbmlDocument);
    if (!sbmlDocument.isSetModel() || (sbmlDocument.getModel().getExtension(LayoutConstants.namespaceURI) == null)) {
      logger.info("SBMLDocument didn't contain any model.");
      return;
    }
    
    boolean newSpeciesAlias = false;
    boolean newCompartmentAlias = false;
    Double actualX = null;
    Double actualY = null;
    Double actualHeight = null;
    Double actualWidth = null;
    String actualId = null;
    String aliasID = null;
    XMLInputFactory inputFactory = XMLInputFactory.newInstance();
    XMLStreamReader streamReader = inputFactory.createXMLStreamReader(inputStream);
    
    while (streamReader.hasNext()) {
      
      if (streamReader.getEventType() == XMLStreamConstants.START_ELEMENT) {
        logger.fine(streamReader.getLocalName());
        if (streamReader.getLocalName().equals("speciesAlias")) {
          // reset everything
          newCompartmentAlias = false;
          actualX = null;
          actualY = null;
          actualHeight = null;
          actualWidth = null;
          actualId = null;
          aliasID = null;
          
          // setting the newer values
          newSpeciesAlias = true;
          for (int i = 0; i < streamReader.getAttributeCount(); i++) {
            logger.finer(streamReader.getAttributeLocalName(i) + ": " + streamReader.getAttributeValue(i));
            if (streamReader.getAttributeLocalName(i).equals("species")) {
              actualId = streamReader.getAttributeValue(i);
            }
            else if (streamReader.getAttributeLocalName(i).equals("id")) {
              aliasID = streamReader.getAttributeValue(i);
            }
          }
          logger.fine("species alias " + actualId);
        }
        else if (streamReader.getLocalName().equals("compartmentAlias")) {
          // reset everything
          newSpeciesAlias = false;
          actualX = null;
          actualY = null;
          actualHeight = null;
          actualWidth = null;
          actualId = null;
          aliasID = null;
          
          // setting the newer values
          newCompartmentAlias = true;
          for (int i = 0; i < streamReader.getAttributeCount(); i++) {
            logger.finer(streamReader.getAttributeLocalName(i) + ": " + streamReader.getAttributeValue(i));
            if (streamReader.getAttributeLocalName(i).equals("compartment")) {
              actualId = streamReader.getAttributeValue(i);
            }
            else if (streamReader.getAttributeLocalName(i).equals("id")) {
              aliasID = streamReader.getAttributeValue(i);
            }
          }
          logger.fine("compartment alias " + actualId);
        }
        else if (newCompartmentAlias && streamReader.getLocalName().equals("namePoint")) {
          //					Double x = null;
          //					Double y = null;
          //					for (int i = 0; i < streamReader.getAttributeCount(); i++) {
          //						if (streamReader.getAttributeLocalName(i).equals("x")) {
          //							x = Double.valueOf(streamReader.getAttributeValue(i));
          //						}
          //						else if (streamReader.getAttributeLocalName(i).equals("y")) {
          //							y = Double.valueOf(streamReader.getAttributeValue(i));
          //						}
          //					}
          createTextGlyph(actualId, aliasID);
        }
        else if ((newSpeciesAlias || newCompartmentAlias) && streamReader.getLocalName().equals("bounds")) {
          for (int i = 0; i < streamReader.getAttributeCount(); i++) {
            logger.finer(streamReader.getAttributeLocalName(i) + ": " + streamReader.getAttributeValue(i));
            if (streamReader.getAttributeLocalName(i).equals("h")) {
              actualHeight = Double.valueOf(streamReader.getAttributeValue(i));
            }
            else if (streamReader.getAttributeLocalName(i).equals("w")) {
              actualWidth = Double.valueOf(streamReader.getAttributeValue(i));
            }
            else if (streamReader.getAttributeLocalName(i).equals("x")) {
              actualX = Double.valueOf(streamReader.getAttributeValue(i));
            }
            else if (streamReader.getAttributeLocalName(i).equals("y")) {
              actualY = Double.valueOf(streamReader.getAttributeValue(i));
            }
          }
          logger.fine("writing layout");
          if (newCompartmentAlias) {
            createCompartmentGlyph(aliasID, actualId, actualX, actualY, actualWidth, actualHeight);
          }
          else if (newSpeciesAlias) {
            createSpeciesGlyph(aliasID, actualId, actualX, actualY, actualWidth, actualHeight);
            newSpeciesAlias = false;
          }
        }
        else if (streamReader.getLocalName().equals("modelDisplay")) {
          Double modelWidth = 0.0;
          Double modelHeigth = 0.0;
          for (int i = 0; i < streamReader.getAttributeCount(); i++) {
            if (streamReader.getAttributeLocalName(i).equals("sizeX")) {
              modelWidth = Double.valueOf(streamReader.getAttributeValue(i));
            }
            else if (streamReader.getAttributeLocalName(i).equals("sizeY")) {
              modelHeigth = Double.valueOf(streamReader.getAttributeValue(i));
            }
          }
          layout.setDimensions(new Dimensions(modelWidth, modelHeigth, 0, layout.getLevel(), layout.getVersion()));
        }
      }
      streamReader.next();
    }
  }
  
  /**
   * 
   * @param annotationBlock
   * @return
   */
  private String getAnnotationBlockWithNamespace (String annotationBlock) {
    
    return ("<?xml version='1.0' encoding='UTF-8' standalone='no'?>" +
        "<annotation xmlns:celldesigner=\"http://www.sbml.org/2001/ns/celldesigner\">" +
        annotationBlock +
        "</annotation>");
  }
  
  /**
   * 
   * @throws XMLStreamException
   */
  private void readReactionLayout() throws XMLStreamException {
    if (model.isSetListOfReactions()) {
      for (Reaction r : model.getListOfReactions()) {
        createGlyphsForReaction(r, getAnnotationBlockWithNamespace(r.getAnnotationString()));
      }
    }
  }
  
  /**
   * 
   * @param reaction
   * @param annotationBlockOfReaction
   * @throws XMLStreamException
   */
  private void createGlyphsForReaction(Reaction reaction, String annotationBlockOfReaction) throws XMLStreamException {
    String rgID = "reaction_" + reaction.getId();
    String specRefGlyphIDbasis = "specRef_" + reaction.getId() + "_";
    ReactionGlyph rg = new ReactionGlyph(rgID, layout.getLevel(), layout.getVersion());
    rg.setReaction(reaction);
    
    XMLInputFactory inputFactory = XMLInputFactory.newInstance();
    XMLStreamReader streamReader = inputFactory.createXMLStreamReader(new BufferedReader(new StringReader(annotationBlockOfReaction)));
    
    while (streamReader.hasNext()) {
      
      if (streamReader.getEventType() == XMLStreamConstants.START_ELEMENT) {
        logger.fine(streamReader.getLocalName());
        if (streamReader.getLocalName().equals("baseReactant")) {
          for (int i = 0; i < streamReader.getAttributeCount(); i++) {
            if (streamReader.getAttributeLocalName(i).equals("alias")) {
              SpeciesReferenceGlyph srg = rg.createSpeciesReferenceGlyph((specRefGlyphIDbasis + streamReader.getAttributeValue(i)), streamReader.getAttributeValue(i));
              srg.setRole(SpeciesReferenceRole.SUBSTRATE);
            }
          }
        }
        else if (streamReader.getLocalName().equals("reactantLink")) {
          for (int i = 0; i < streamReader.getAttributeCount(); i++) {
            if (streamReader.getAttributeLocalName(i).equals("alias")) {
              SpeciesReferenceGlyph srg = rg.createSpeciesReferenceGlyph((specRefGlyphIDbasis + streamReader.getAttributeValue(i)), streamReader.getAttributeValue(i));
              srg.setRole(SpeciesReferenceRole.SIDESUBSTRATE);
            }
          }
        }
        else if (streamReader.getLocalName().equals("baseProduct")) {
          for (int i = 0; i < streamReader.getAttributeCount(); i++) {
            if (streamReader.getAttributeLocalName(i).equals("alias")) {
              SpeciesReferenceGlyph srg = rg.createSpeciesReferenceGlyph((specRefGlyphIDbasis + streamReader.getAttributeValue(i)), streamReader.getAttributeValue(i));
              srg.setRole(SpeciesReferenceRole.PRODUCT);
            }
          }
        }
        else if (streamReader.getLocalName().equals("productLink")) {
          for (int i = 0; i < streamReader.getAttributeCount(); i++) {
            if (streamReader.getAttributeLocalName(i).equals("alias")) {
              SpeciesReferenceGlyph srg = rg.createSpeciesReferenceGlyph((specRefGlyphIDbasis + streamReader.getAttributeValue(i)), streamReader.getAttributeValue(i));
              srg.setRole(SpeciesReferenceRole.SIDEPRODUCT);
            }
          }
        }
        else if (streamReader.getLocalName().equals("modification")) {
          SpeciesReferenceGlyph srg = null;
          for (int i = 0; i < streamReader.getAttributeCount(); i++) {
            if (streamReader.getAttributeLocalName(i).equals("aliases")) {
              srg = rg.createSpeciesReferenceGlyph((specRefGlyphIDbasis + streamReader.getAttributeValue(i)), streamReader.getAttributeValue(i));
            }
            else if (streamReader.getAttributeLocalName(i).equals("type")) {
              if (srg != null) {
                if (streamReader.getAttributeValue(i).equals("INHIBITION")) {
                  srg.setRole(SpeciesReferenceRole.INHIBITOR);
                }
                // TODO die anderen ELSE IF's
              }
            }
          }
        }
      }
      streamReader.next();
    }
    layout.addReactionGlyph(rg);
  }
  
  /**
   * 
   * @param direction (e.g. NW, SE, ...)
   * @param x
   * @param y
   * @param width
   * @param height
   * @return
   */
  private Point getCoordinates (String direction, Double x, Double y, Double width, Double height) {
    Point p = new Point(x, y, 0);
    if (direction.equals("N")) {
      p.setX(x + (width / 2));
    }
    return p;
  }
  
  /**
   * 
   * @param originGlyphID
   * @param text
   */
  private void createTextGlyph(String originID, String originGlyphID) {
    String tgID = "text_" + originGlyphID;
    TextGlyph tg = new TextGlyph(tgID, layout.getLevel(), layout.getVersion());
    tg.setGraphicalObject(originGlyphID);
    tg.setOriginOfText(originID);
    layout.addTextGlyph(tg);
  }
  
  /**
   * 
   * @param glyphID
   * @param compID
   * @param x
   * @param y
   * @param width
   * @param height
   */
  private void createCompartmentGlyph (String glyphID, String compID, Double x, Double y, Double width, Double height) {
    CompartmentGlyph cg = new CompartmentGlyph(glyphID, layout.getLevel(), layout.getVersion());
    cg.setCompartment(compID);
    layout.addCompartmentGlyph(cg);
    setBoundingBox(cg,x,y,width,height);
  }
  
  /**
   * 
   * @param glyphID
   * @param speciesID
   * @param x
   * @param y
   * @param width
   * @param height
   * @return
   */
  private SpeciesGlyph createSpeciesGlyph (String glyphID, String speciesID, Double x, Double y, Double width, Double height) {
    SpeciesGlyph sg = new SpeciesGlyph(glyphID, layout.getLevel(), layout.getVersion());
    sg.setSpecies(speciesID);
    layout.addSpeciesGlyph(sg);
    setBoundingBox(sg,x,y,width,height);
    createTextGlyph(speciesID, sg.getId());
    return sg;
  }
  
  /**
   * 
   * @param go
   * @param x
   * @param y
   * @param width
   * @param height
   */
  private void setBoundingBox(GraphicalObject go, Double x, Double y,	Double width, Double height) {
    if (go != null) {
      BoundingBox bb = go.createBoundingBox(width, height, 0);
      bb.setPosition(new Point(x, y, 0));
    }
  }
  
  /**
   * creates a simple reaction incl. species-, speciesreference- and reactionglyph and curvesegments
   */
  public void createTestReaction() {
    Species s1 = new Species("test1", "test1", model.getLevel(), model.getVersion());
    s1.setName("test1");
    s1.setSBOTerm(SBO.getSimpleMolecule());
    Species s2 = new Species("test2", "test2", model.getLevel(), model.getVersion());
    s2.setName("test2");
    s2.setSBOTerm(SBO.getSimpleMolecule());
    Reaction r1 = new Reaction("rtest", model.getLevel(), model.getVersion());
    r1.addReactant(new SpeciesReference(s1));
    r1.addProduct(new SpeciesReference(s2));
    model.addSpecies(s1);
    model.addSpecies(s2);
    model.addReaction(r1);
    SpeciesGlyph speciesGlyph1 = new SpeciesGlyph("spec_s1", model.getLevel(), model.getVersion());
    SpeciesGlyph speciesGlyph2 = new SpeciesGlyph("spec_s2", model.getLevel(), model.getVersion());
    speciesGlyph1.setSpecies(s1);
    speciesGlyph2.setSpecies(s2);
    layout.addSpeciesGlyph(speciesGlyph1);
    layout.addSpeciesGlyph(speciesGlyph2);
    setBoundingBox(speciesGlyph1, 0.0, 0.0, 70.0, 20.0);
    setBoundingBox(speciesGlyph2, 200.0, 0.0, 70.0, 20.0);
    SpeciesGlyph sg1 = createSpeciesGlyph("spec_s1", s1.getId(), 0.0, 0.0, 70.0, 20.0);
    SpeciesGlyph sg2 = createSpeciesGlyph("spec_s2", s2.getId(), 100.0, 0.0, 70.0, 20.0);
    
    ReactionGlyph rg = new ReactionGlyph("react_r1", model.getLevel(), model.getVersion());
    SpeciesReferenceGlyph srg1 = rg.createSpeciesReferenceGlyph("srg_r1_s1", sg1.getId());
    SpeciesReferenceGlyph srg2 = rg.createSpeciesReferenceGlyph("srg_r1_s2", sg2.getId());
    srg1.setRole(SpeciesReferenceRole.SUBSTRATE);
    srg2.setRole(SpeciesReferenceRole.PRODUCT);
    setBoundingBox(rg, 100.0, 0.0, 10.0, 10.0);
    
    LineSegment cs1 = new LineSegment();
    cs1.setStart(new Point(35.0, 10.0, 0.0));
    cs1.setEnd(new Point(100.0, 10.0, 0.0));
    Curve c = new Curve();
    ListOf<CurveSegment> csList = new ListOf<CurveSegment>();
    csList.add(cs1);
    c.setListOfCurveSegments(csList);
    srg1.setCurve(c);
    
    LineSegment cs2 = new LineSegment();
    cs2.createStart(110.0, 10.0, 0.0);
    cs2.createEnd(235.0, 10.0, 0.0);
    Curve c2 = new Curve();
    ListOf<CurveSegment> csList2 = new ListOf<CurveSegment>();
    csList2.add(cs2);
    c2.setListOfCurveSegments(csList2);
    
    BoundingBox bb2 = srg2.createBoundingBox();
    bb2.setPosition(new Point(110.0, 10.0, 0.0));
    bb2.setDimensions(new Dimensions(200.0, 10.0, 0, model.getLevel(), model.getVersion()));
    srg2.setCurve(c2);
    
    layout.addReactionGlyph(rg);
  }
  
  /**
   * @param args
   * @throws IOException
   * @throws XMLStreamException
   */
  public static void main(String[] args) throws XMLStreamException, IOException {
    logger.info("Reading file: " + args[0]);
    CellDesignerAnnotationParser parser = new CellDesignerAnnotationParser(new File(args[0]));
    parser.run();
    if (args.length > 1) {
      SBMLWriter.write(parser.getSBMLDocument(), new File(args[1]), ' ', (short) 2);
      logger.info("Document written to: " + args[1]);
    } else {
      SBMLWriter.write(parser.getSBMLDocument(), System.out, ' ', (short) 2);
    }
  }
  
  /* (non-Javadoc)
   * @see java.lang.Runnable#run()
   */
  @Override
  public void run() {
    if ((sbmlDocument != null) && (sbmlDocument.isSetModel())) {
      String annotation = getAnnotationBlockWithNamespace(
        SBMLtools.toXML(
          sbmlDocument.getModel().getAnnotation().getNonRDFannotation()));
      try {
        readCDLayout(new BufferedReader(new StringReader(annotation)));
        readReactionLayout();
        //				createTestReaction();
      } catch (XMLStreamException exc) {
        throw new RuntimeException(exc);
      }
    }
  }
  
}
