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

package de.zbit.sbml.layout;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.NamedSBase;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.SimpleSpeciesReference;
import org.sbml.jsbml.ext.SBasePlugin;
import org.sbml.jsbml.ext.layout.AbstractReferenceGlyph;
import org.sbml.jsbml.ext.layout.CompartmentGlyph;
import org.sbml.jsbml.ext.layout.CurveSegment;
import org.sbml.jsbml.ext.layout.GraphicalObject;
import org.sbml.jsbml.ext.layout.Layout;
import org.sbml.jsbml.ext.layout.LayoutConstants;
import org.sbml.jsbml.ext.layout.LayoutModelPlugin;
import org.sbml.jsbml.ext.layout.ReactionGlyph;
import org.sbml.jsbml.ext.layout.SpeciesGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceRole;
import org.sbml.jsbml.ext.layout.TextGlyph;
import org.sbml.jsbml.util.StringTools;

import de.zbit.graph.sbgn.DrawingOptions;
import de.zbit.io.csv.CSVReader;
import de.zbit.sbml.util.SBMLtools;
import de.zbit.util.prefs.SBPreferences;

/**
 * {@link LayoutDirector} produces a graphical representation of a layout of an
 * {@link SBMLDocument}.
 *
 * @param <P>
 *            Type of the product.
 *
 * @author Mirjam Gutekunst
 * @version $Rev$
 */
public class LayoutDirector<P> implements Runnable {
  
  /**
   * User preferences.
   */
  private static final transient SBPreferences prefs = SBPreferences.getPreferencesFor(DrawingOptions.class);
  
  /**
   * 
   */
  public static final String SPECIAL_ROTATION_NEEDED = "SPECIAL_ROTATION_NEEDED";
  
  /**
   * 
   */
  public static final String NO_WHISKERS = "NO_WHISKERS";
  
  /**
   *
   */
  private static final double DEFAULT_CURVE_WIDTH = prefs.getDouble(DrawingOptions.EDGE_LINE_WIDTH);
  
  /**
   * A {@link Logger} for this class
   */
  private static final transient Logger logger = Logger.getLogger(LayoutDirector.class.getName());
  
  /**
   * Constant for use as the key for flux values.
   */
  public static final String KEY_FOR_FLUX_VALUES = "fluxValue";
  
  /**
   * Constant for use as the key for layout links. A layout link connects a
   * core object (e.g., a species) with a all of its glyphs in the given
   * layout.
   */
  public static final String LAYOUT_LINK = "LAYOUT_LINK";
  
  /**
   * Constant for use as the key for compartment links, also see
   * {@link #LAYOUT_LINK}.
   */
  public static final String COMPARTMENT_LINK = "COMPARTMENT_LINK";
  
  /**
   * Constant for use as the key for relative docking points. A relative
   * docking point is a property stored in the user objects of a
   * {@link ReactionGlyph} to denote the relative docking position of the
   * connecting arcs.
   */
  public static final String PN_RELATIVE_DOCKING_POINT = "PN_RELATIVE_DOCKING_POINT";
  
  /**
   * Constant for use as the key for relative docking points for a species,
   * also see {@link #PN_RELATIVE_DOCKING_POINT}.
   */
  public static final String SPECIES_RELATIVE_DOCKING_POINT = "SPECIES_RELATIVE_DOCKING_POINT";
  
  public static final Map<String,Double> rotationAngles = new HashMap<String,Double>();
  /**
   * checks, if the nodes are drawn again, to get the rotationAngles again
   */
  public static boolean fromRearrangeCycle = false;  
  /**
   * LayoutAlgorithm instance to compute missing information
   */
  private LayoutAlgorithm algorithm;
  
  /**
   * LayoutBuilder instance to build the layout
   */
  private LayoutBuilder<P> builder;
  
  /**
   * SBML model of the given document
   */
  private Model model;
  
  /**
   * index of the layout for which to produce the graphical representation
   */
  private int layoutIndex;
  
  /**
   * map of fluxes for the given SBML document
   */
  private Map<String, Double> mapOfFluxes;
  
  // TODO: Would be nice to pass in options class
  
  /**
   * boolean value to be passed into layout glyph creation algorithms to add whiskers or not
   */
  private Boolean addWhiskers = true;
  
  
  /**
   * @param inputFile file containing the SBML document
   * @param builder LayoutBuilder instance for producing the output
   * @param algorithm LayoutAlgorithm instance for layouting elements
   * @throws XMLStreamException
   * @throws IOException
   */
  public LayoutDirector(File inputFile, LayoutBuilder<P> builder,
    LayoutAlgorithm algorithm) throws XMLStreamException, IOException {
    this(SBMLReader.read(inputFile), builder, algorithm);
  }
  
  /**
   * @param inputFile file containing the SBML document
   * @param builder LayoutBuilder instance for producing the output
   * @param algorithm LayoutAlgorithm instance for layouting elements
   * @param fluxFile file containing the flux information
   * @throws XMLStreamException
   * @throws IOException
   */
  public LayoutDirector(File inputFile, LayoutBuilder<P> builder,
    LayoutAlgorithm algorithm, File fluxFile)
        throws XMLStreamException, IOException {
    this(SBMLReader.read(inputFile), builder, algorithm);
    mapOfFluxes = new HashMap<String, Double>();
    
    CSVReader csvReader = new CSVReader(fluxFile.getAbsolutePath());
    String[][] data = csvReader.read();
    csvReader.close();
    for (int i = 0; i < data.length; i++) {
      mapOfFluxes.put(data[i][0], Double.parseDouble(data[i][1]));
    }
  }
  
  /**
   * @param doc the SBMLdocument
   * @param builder LayoutBuilder instance for producing the output
   * @param algorithm LayoutAlgorithm instance for layouting elements
   */
  public LayoutDirector(SBMLDocument doc, LayoutBuilder<P> builder,
    LayoutAlgorithm algorithm) {
    this(doc, builder, algorithm, 0);
  }
  
  /**
   * @param doc the SBMLdocument
   * @param builder LayoutBuilder instance for producing the output
   * @param algorithm LayoutAlgorithm instance for layouting elements
   * @param index the index of the layout for which to produce the ouput
   */
  public LayoutDirector(SBMLDocument doc, LayoutBuilder<P> builder,
    LayoutAlgorithm algorithm, int index) {
    this(((LayoutModelPlugin) doc.getModel().getExtension(
      LayoutConstants.getNamespaceURI(doc.getLevel(), doc.getVersion())))
      .getLayout(index), builder, algorithm);
  }
  
  /**
   * @param layout the SBML layout for which to produce the output
   * @param builder LayoutBuilder instance for producing the output
   * @param algorithm LayoutAlgorithm instance for layouting elements
   */
  public LayoutDirector(Layout layout, LayoutBuilder<P> builder,
    LayoutAlgorithm algorithm) {
    this.model = layout.getModel();
    this.builder = builder;
    this.algorithm = algorithm;
    LayoutModelPlugin ext = (LayoutModelPlugin) model.getExtension(LayoutConstants.getNamespaceURI(layout.getLevel(), layout.getVersion()));
    if ((ext != null) && (ext.isSetListOfLayouts())) {
      this.layoutIndex = ext.getListOfLayouts().indexOf(layout);
    } else {
      this.layoutIndex = -1;
    }
  }
  
  
  /**
   * Returns the value of addWhiskers
   *
   * @return the value of addWhiskers
   */
  public Boolean getAddWhiskers() {
    //TODO: if variable is boolean, create an additional "isVar"
    //TODO: return primitive data type if possible (e.g. int instead of Integer)
    if (isSetAddWhiskers()) {
      return addWhiskers;
    }
    return null;
  }
  
  
  /**
   * Returns whether addWhiskers is set
   *
   * @return whether addWhiskers is set
   */
  public Boolean isSetAddWhiskers() {
    return this.addWhiskers != null;
  }
  
  
  /**
   * Sets the value of addWhiskers
   */
  public void setAddWhiskers(Boolean addWhiskers) {
    this.addWhiskers = addWhiskers;
  }
  
  
  /**
   * Unsets the variable addWhiskers
   *
   * @return {@code true}, if addWhiskers was set before,
   *         otherwise {@code false}
   */
  public boolean unsetAddWhiskers() {
    if (isSetAddWhiskers()) {
      this.addWhiskers = null;
      return true;
    }
    return false;
  }
  
  /**
   * @return the layoutIndex
   */
  public int getLayoutIndex() {
    return layoutIndex;
  }
  
  /**
   * @param layoutIndex the index of the layout to be set
   */
  public void setLayoutIndex(int layoutIndex) {
    
    if (layoutIndex < 0) {
      throw new IndexOutOfBoundsException(MessageFormat.format(
        "{0,number,integer} < 0", layoutIndex));
    }
    
    SBasePlugin extension = model.getExtension(LayoutConstants
      .getNamespaceURI(model.getLevel(), model.getVersion()));
    
    if ((extension != null) &&
        (layoutIndex >= ((LayoutModelPlugin) extension).getLayoutCount())) {
      throw new IndexOutOfBoundsException(MessageFormat.format(
        "{0,number,integer} > {1,number,integer}", layoutIndex,
        ((LayoutModelPlugin) extension).getLayoutCount()));
    }
    
    this.layoutIndex = layoutIndex;
  }
  
  /**
   * Builds the product for the specified layout:
   * 1. All glyphs are added to the input of the layout algorithm.
   * 2. The layout algorithm completes all missing information.
   * 3. All glyphs are built with the layout builder.
   * 4. The dimensions of the whole layout are computed.
   *
   * @param layout the layout for which to produce the output
   */
  private void buildLayout(Layout layout) {
    
    algorithm.setLayout(layout);
    builder.builderStart(layout);
    
    // Render package preprocessing: Building links form elements to their local styles
    RenderProcessor.preprocess(layout);
    
    // Compartment glyphs
    ListOf<CompartmentGlyph> compartmentGlyphList = null;
    List<CompartmentGlyph> sortedCompartmentGlyphList = null;
    if (layout.isSetListOfCompartmentGlyphs()) {
      compartmentGlyphList = layout.getListOfCompartmentGlyphs();
      createLayoutLinks(compartmentGlyphList);
      sortedCompartmentGlyphList = getSortedCompartmentGlyphList();
    }
    // Species glyphs
    ListOf<SpeciesGlyph> speciesGlyphList = null;
    if (layout.isSetListOfSpeciesGlyphs()) {
      speciesGlyphList = layout.getListOfSpeciesGlyphs();
      createLayoutLinks(speciesGlyphList);
    }
    
    // Reaction glyphs
    ListOf<ReactionGlyph> reactionGlyphList = null;
    if (layout.isSetListOfReactionGlyphs()) {
      reactionGlyphList = layout.getListOfReactionGlyphs();
      createLayoutLinks(reactionGlyphList);
    }
    
    // Text glyphs
    ListOf<TextGlyph> textGlyphList = layout.isSetListOfTextGlyphs() ? textGlyphList = layout.getListOfTextGlyphs() : null;
    
    // add all glyphs to algorithm input
    
    // 1. compartment glyphs
    if (compartmentGlyphList != null) {
      for (CompartmentGlyph compartmentGlyph : compartmentGlyphList) {
        if (glyphIsLayouted(compartmentGlyph)) {
          algorithm.addLayoutedGlyph(compartmentGlyph);
        } else {
          algorithm.addUnlayoutedGlyph(compartmentGlyph);
        }
      }
    }
    
    // 2. species glyphs + texts
    if (speciesGlyphList != null) {
      
      for (SpeciesGlyph speciesGlyph : speciesGlyphList) {
        if (glyphIsLayouted(speciesGlyph)) {
          algorithm.addLayoutedGlyph(speciesGlyph);
        } else {
          algorithm.addUnlayoutedGlyph(speciesGlyph);
        }
      }
    }
    if (textGlyphList != null) {
      for (TextGlyph textGlyph : textGlyphList) {
        if (glyphIsLayouted(textGlyph)) {
          algorithm.addLayoutedGlyph(textGlyph);
        } else {
          algorithm.addUnlayoutedGlyph(textGlyph);
        }
      }
    }
    
    // 3. reaction glyphs: create edges (srg, rg)
    if (reactionGlyphList != null) {
      for (ReactionGlyph reactionGlyph : reactionGlyphList) {
        if (!addWhiskers){
          reactionGlyph.putUserObject(NO_WHISKERS, true);
        }
        // add reaction glyph to algorithm input
        if (glyphIsLayouted(reactionGlyph)) {
          algorithm.addLayoutedGlyph(reactionGlyph);
          if (reactionGlyphHasCurves(reactionGlyph)) {
            reactionGlyph.putUserObject(SPECIAL_ROTATION_NEEDED, reactionGlyph);
          }
        } else {
          algorithm.addUnlayoutedGlyph(reactionGlyph);
        }
        if (reactionGlyph.isSetListOfSpeciesReferenceGlyphs()) {
          ListOf<SpeciesReferenceGlyph> speciesReferenceGlyphs =
              reactionGlyph.getListOfSpeciesReferenceGlyphs();
          // add all (srg, rg) pairs to algorithm input
          for (SpeciesReferenceGlyph srg : speciesReferenceGlyphs) {
            if (edgeIsLayouted(reactionGlyph, srg)) {
              algorithm.addLayoutedEdge(srg, reactionGlyph);
            } else {
              algorithm.addUnlayoutedEdge(srg, reactionGlyph);
            }
          }
        }
      }
    }
    
    // 2. let algorithm complete all glyphs
    algorithm.completeGlyphs();
    
    // 3. build all glyphs
    if (sortedCompartmentGlyphList != null) {
      handleCompartmentGlyphs(sortedCompartmentGlyphList);
    }
    if (speciesGlyphList != null) {
      handleSpeciesGlyphs(speciesGlyphList);
    }
    if (reactionGlyphList != null) {
      handleReactionGlyphs(reactionGlyphList);
    }
    if (textGlyphList != null) {
      handleTextGlyphs(textGlyphList);
    }
    
    // 4. set layout dimensions
    // TODO this is the second call (see above)
    layout.setDimensions(algorithm.createLayoutDimension());
    
    builder.builderEnd();
  }
  
  /**
   * Check if there are {@link CurveSegment}s given for any of the species
   * reference glyphs of the given {@link ReactionGlyph}.
   *
   * @param reactionGlyph
   * @return whether there exists a curve
   */
  private boolean reactionGlyphHasCurves(ReactionGlyph reactionGlyph) {
    boolean hasCurves = false;
    if (reactionGlyph.isSetListOfSpeciesReferenceGlyphs()) {
      for (SpeciesReferenceGlyph speciesReferenceGlyph :
        reactionGlyph.getListOfSpeciesReferenceGlyphs()) {
        if (speciesReferenceGlyph.isSetCurve()) {
          hasCurves = true;
          break;
        }
      }
    }
    return hasCurves;
  }
  
  /**
   * Check if the incoming edge is layouted, i.e. if it has a curve.
   *
   * @param reactionGlyph process node of the edge
   * @param speciesReferenceGlyph species node of the edge
   * @return whether the edge is layouted
   */
  public static boolean edgeIsLayouted(ReactionGlyph reactionGlyph,
    SpeciesReferenceGlyph speciesReferenceGlyph) {
    return speciesReferenceGlyph.isSetCurve();
  }
  
  /**
   * Check if a glyph as complete layout information (i.e. if it as both
   * dimensions and position).
   *
   * @param glyph
   * @return
   */
  public static boolean glyphIsLayouted(GraphicalObject glyph) {
    return glyph.isSetBoundingBox()
        && glyph.getBoundingBox().isSetDimensions()
        && glyph.getBoundingBox().isSetPosition()
        && (glyph.getBoundingBox().getPosition().getX() != 0d)
        && (glyph.getBoundingBox().getPosition().getY() != 0d)
        && (glyph.getBoundingBox().getDimensions().getHeight() != 0d)
        && (glyph.getBoundingBox().getDimensions().getWidth() != 0d);
  }
  
  /**
   * Check if a glyph has dimensions.
   *
   * @param glyph
   * @return
   */
  public static boolean glyphHasDimensions(GraphicalObject glyph) {
    return glyph.isSetBoundingBox()
        && glyph.getBoundingBox().isSetDimensions()
        && (glyph.getBoundingBox().getDimensions().getWidth() != 0)
        && (glyph.getBoundingBox().getDimensions().getHeight() != 0);
  }
  
  /**
   * Check if a glyph has a position.
   *
   * @param glyph
   * @return
   */
  public static boolean glyphHasPosition(GraphicalObject glyph) {
    return glyph.isSetBoundingBox()
        && glyph.getBoundingBox().isSetPosition()
        && (glyph.getBoundingBox().getPosition().getX() != 0)
        && (glyph.getBoundingBox().getPosition().getY() != 0);
  }
  
  /**
   * Check if a species reference glyph is a substrate. Both species reference
   * roles (higher priority) and SBO terms are used.
   *
   * @param speciesReferenceGlyph
   * @return
   */
  public static boolean isSubstrate(SpeciesReferenceGlyph speciesReferenceGlyph) {
    if (speciesReferenceGlyph.isSetSpeciesReferenceRole()) {
      return speciesReferenceGlyph.getSpeciesReferenceRole().equals(
        SpeciesReferenceRole.SUBSTRATE)
          || speciesReferenceGlyph.getSpeciesReferenceRole().equals(
            SpeciesReferenceRole.SIDESUBSTRATE);
    } else if (speciesReferenceGlyph.isSetSBOTerm()) {
      return SBO.isChildOf(speciesReferenceGlyph.getSBOTerm(), 394);
    }
    return false;
  }
  
  /**
   * Check if a species reference glyph is a product. Both species reference
   * roles (higher priority) and SBO terms are used.
   *
   * @param speciesReferenceGlyph
   * @return
   */
  public static boolean isProduct(SpeciesReferenceGlyph speciesReferenceGlyph) {
    if (speciesReferenceGlyph.isSetSpeciesReferenceRole()) {
      return speciesReferenceGlyph.getSpeciesReferenceRole().equals(
        SpeciesReferenceRole.PRODUCT)
          || speciesReferenceGlyph.getSpeciesReferenceRole().equals(
            SpeciesReferenceRole.SIDEPRODUCT);
    } else if (speciesReferenceGlyph.isSetSBOTerm()) {
      return SBO.isChildOf(speciesReferenceGlyph.getSBOTerm(), 393);
    }
    return false;
  }
  
  /**
   * Method calls the corresponding method
   * {@link LayoutBuilder#buildCompartment} of the builder.
   *
   * @param compartmentGlyphList
   */
  private void handleCompartmentGlyphs(
    List<CompartmentGlyph> compartmentGlyphList) {
    CompartmentGlyph previousCompartmentGlyph = null;
    for (int i = 0; i < compartmentGlyphList.size(); i++) {
      CompartmentGlyph compartmentGlyph = compartmentGlyphList.get(i);
      if ((previousCompartmentGlyph != null) &&
          previousCompartmentGlyph.isSetReference() &&
          compartmentGlyph.isSetReference()) {
        Compartment previousCompartment = (Compartment) previousCompartmentGlyph.getReferenceInstance();
        if (previousCompartment.getUserObject(COMPARTMENT_LINK) instanceof List<?>) {
          @SuppressWarnings("unchecked")
          List<Compartment> containedCompartments =
          (List<Compartment>) previousCompartment.getUserObject(COMPARTMENT_LINK);
          if (!containedCompartments.contains(compartmentGlyph.getReferenceInstance())) {
            previousCompartment = null;
          }
        }
      }
      
      previousCompartmentGlyph = compartmentGlyph;
      builder.buildCompartment(compartmentGlyph);
    }
  }
  
  /**
   * Handle a list of species glyphs. Method calls
   * {@link LayoutDirector#handleSpeciesGlyph} for every {@link SpeciesGlyph}
   * of the given list.
   *
   * @param speciesGlyphList
   */
  private void handleSpeciesGlyphs(ListOf<SpeciesGlyph> speciesGlyphList) {
    for (SpeciesGlyph sg : speciesGlyphList) {
      handleSpeciesGlyph(sg);
    }
  }
  
  /**
   * Build a species glyph. Method calls the corresponding method
   * {@link LayoutBuilder#buildEntityPoolNode} of the builder.
   *
   * @param speciesGlyph
   */
  @SuppressWarnings("unchecked")
  private void handleSpeciesGlyph(SpeciesGlyph speciesGlyph) {
    boolean cloneMarker = false;
    
    if (speciesGlyph.isSetReference()) {
      NamedSBase species = speciesGlyph.getReferenceInstance();
      
      if (!speciesGlyph.isSetSBOTerm()) {
        if (!species.isSetSBOTerm()) {
          SBMLtools.setSBOTerm(speciesGlyph, SBO.getUnknownMolecule());
        } else {
          SBMLtools.setSBOTerm(speciesGlyph, species.getSBOTerm());
        }
      }
      
      List<AbstractReferenceGlyph> glyphList = null; //new ArrayList<AbstractReferenceGlyph>();
      if (species.getUserObject(LAYOUT_LINK) instanceof List<?>) {
        glyphList = (List<AbstractReferenceGlyph>) species.getUserObject(LAYOUT_LINK);
      }
      cloneMarker = (glyphList != null) && (glyphList.size() > 1);
      /*(speciesGlyph.getSBOTerm() == SBO.getSideProduct()) ||
          (speciesGlyph.getSBOTerm() == SBO.getSideSubstrate());*/
      
    }
    builder.buildEntityPoolNode(speciesGlyph, cloneMarker);
  }
  
  /**
   * Handle a list of reaction glyphs. Method calls
   * {@link LayoutDirector#handleReactionGlyph} for every
   * {@link ReactionGlyph} of the given list.
   *
   * @param reactionGlyphList
   */
  private void handleReactionGlyphs(ListOf<ReactionGlyph> reactionGlyphList) {
    for (ReactionGlyph rg : reactionGlyphList) {
      handleReactionGlyph(rg);
    }
  }
  
  /**
   * Build a species glyph. Method calls the corresponding methods
   * {@link LayoutBuilder#buildProcessNode} and
   * {@link LayoutBuilder#buildConnectingArc} of the builder.
   *
   * @param reactionGlyph the object to be drawn
   */
  private void handleReactionGlyph(ReactionGlyph reactionGlyph) {
    
    double curveWidth = DEFAULT_CURVE_WIDTH;
    
    Object userObject = reactionGlyph.getUserObject(KEY_FOR_FLUX_VALUES);
    if (userObject != null) {
      curveWidth = (Double) userObject;
    }
    
    // The responsible classes for rotation are LayoutBuilder and the ProcessNodeRealizier.
    // The first one only calculates the rotation angle, while the second one performs drawing.
    double rgRotationAngle = algorithm.calculateReactionGlyphRotationAngle(reactionGlyph);
    builder.buildProcessNode(reactionGlyph, rgRotationAngle, curveWidth);  
    
    if (reactionGlyph.isSetListOfSpeciesReferenceGlyphs()) {
      for (SpeciesReferenceGlyph srg : reactionGlyph.getListOfSpeciesReferenceGlyphs()) {
        try {
          // copy SBO term of species reference to species reference glyph
          NamedSBase nsb = srg.getReferenceInstance();
          if (!(nsb instanceof SimpleSpeciesReference) && (nsb != null)) {
            logger.warning(MessageFormat.format(
              "Expecting simpleSpeciesReference, but found {0} with id ''{1}'' in {2} with id ''{3}''.",
              nsb.getElementName(), nsb.getId(), srg.getElementName(), srg.getId()));
            srg.setSBOTerm(SBO.getConsumption());
          } else {
            SimpleSpeciesReference speciesReference = (SimpleSpeciesReference) nsb;
            if ((speciesReference == null) || !speciesReference.isSetSBOTerm()) {
              if (!srg.isSetSpeciesReferenceRole()) {
                logger.warning(MessageFormat.format(
                  "Undefined participant role for species glyph ''{0}'' in reaction glyph ''{1}'', assuming consumption.",
                  srg.getId(), reactionGlyph.getId()));
                // sets consumption (straight line as default)
                srg.setSBOTerm(SBO.getConsumption());
              }
            } else {
              srg.setSBOTerm(speciesReference.getSBOTerm());
            }
          }
          
          builder.buildConnectingArc(srg, reactionGlyph, curveWidth);
        } catch (ClassCastException exc) {
          logger.fine("tried to access object wiht id = " + srg.getReference());
          throw exc;
        }
      }
    }
  }
  
  /**
   * Handle a list of text glyphs. Method calls
   * {@link LayoutDirector#handleTextGlyph} for every {@link TextGlyph} of the
   * given list.
   *
   * @param textGlyphList
   */
  private void handleTextGlyphs(ListOf<TextGlyph> textGlyphList) {
    for (TextGlyph textGlyph : textGlyphList) {
      handleTextGlyph(textGlyph);
    }
  }
  
  /**
   * Build a text glyph. Method calls the corresponding method
   * {@link LayoutBuilder#buildTextGlyph} of the builder.
   *
   * @param textGlyph
   */
  private void handleTextGlyph(TextGlyph textGlyph) {
    builder.buildTextGlyph(textGlyph);
  }
  
  /**
   * Check whether a text glyph represents an independent text (i.e. it is not
   * associated with any other graphical object or species.
   *
   * @param textGlyph
   * @return
   */
  public static boolean textGlyphIsIndependent(TextGlyph textGlyph) {
    return textGlyph.isSetText() &&
        !textGlyph.isSetGraphicalObject() &&
        !textGlyph.isSetOriginOfText();
  }
  
  /**
   * Connect a component with its corresponding glyph. It creates an user
   * object with key LAYOUT_LINK for every species of the given list of
   * species glyphs.
   *
   * @param listOfAbstractReferenceGlyphs list of species glyphs
   */
  @SuppressWarnings("unchecked")
  private void createLayoutLinks(ListOf<? extends AbstractReferenceGlyph> listOfAbstractReferenceGlyphs) {
    for (AbstractReferenceGlyph glyph : listOfAbstractReferenceGlyphs) {
      if (glyph.isSetReference()) {
        NamedSBase correspondingSBase = glyph.getReferenceInstance();
        if (correspondingSBase == null) {
          logger.warning(MessageFormat.format(
            "Removing incorrect link from glyph {0} to an non existing element {1}",
            glyph, glyph.getReference()));
          glyph.unsetReference();
        } else {
          List<AbstractReferenceGlyph> listOfGlyphs = null;
          if (correspondingSBase.getUserObject(LAYOUT_LINK) instanceof List<?>) {
            listOfGlyphs = (List<AbstractReferenceGlyph>) correspondingSBase.getUserObject(LAYOUT_LINK);
          } else {
            listOfGlyphs = new ArrayList<AbstractReferenceGlyph>();
            correspondingSBase.putUserObject(LAYOUT_LINK, listOfGlyphs);
          }
          listOfGlyphs.add(glyph);
        }
      }
    }
  }
  
  /* Build the layout and start the actual drawing of the elements. (non-Javadoc)
   * @see java.lang.Runnable#run()
   */
  @Override
  public void run() {
    SBasePlugin extension = model.getExtension(LayoutConstants
      .getNamespaceURI(model.getLevel(), model.getVersion()));
    if (extension != null) {
      LayoutModelPlugin layoutModel = (LayoutModelPlugin) extension;
      if (layoutModel.getLayoutCount() > layoutIndex) {
        // TODO: remove this!
        if (mapOfFluxes != null) {
          // If flux values are present, set the flux values as an
          // user object of the reaction glyph.
          for (String reactionId : mapOfFluxes.keySet()) {
            ReactionGlyph reactionGlyph = layoutModel.getLayout(layoutIndex).getReactionGlyph(reactionId);
            if (reactionGlyph != null) {
              reactionGlyph.putUserObject(KEY_FOR_FLUX_VALUES,
                mapOfFluxes.get(reactionId));
            } else {
              throw new IllegalArgumentException(MessageFormat.format(
                "{0} is no legal ReactionGlyph ID for this model.",
                reactionId));
            }
          }
        }
        buildLayout(layoutModel.getLayout(layoutIndex));
      }
    } else {
      logger.info("Method LayoutDirector.run failed: No model extension available for this model.");
    }
  }
  
  /**
   * @param builder the builder to be set
   */
  public void setBuilder(LayoutBuilder<P> builder) {
    this.builder = builder;
  }
  
  /**
   * @return the builder
   */
  public LayoutBuilder<P> getBuilder() {
    return builder;
  }
  
  /**
   * @param algorithm the algorithm to be set
   */
  public void setAlgorithm(LayoutAlgorithm algorithm) {
    this.algorithm = algorithm;
  }
  
  /**
   * @return the algorithm
   */
  public LayoutAlgorithm getAlgorithm() {
    return algorithm;
  }
  
  /**
   * Return the product of the building process.
   *
   * @return the product
   */
  public P getProduct() {
    if (builder.isProductReady()) {
      return builder.getProduct();
    }
    return null;
  }
  
  /**
   * Order of the compartments from outside to inside.
   *
   * @return a List<CompartmentGlyph> with the compartment glyphs ordered from
   *         outside to inside
   */
  @SuppressWarnings("unchecked")
  private List<CompartmentGlyph> getSortedCompartmentGlyphList() {
    createCompartmentLinks();
    List<CompartmentGlyph> sortedGlyphList = new ArrayList<CompartmentGlyph>();
    if (model.isSetListOfCompartments()) {
      for (Compartment compartment : model.getListOfCompartments()) {
        List<CompartmentGlyph> compartmentGlyphList = new ArrayList<CompartmentGlyph>();
        if (compartment.getUserObject(LAYOUT_LINK) instanceof List<?>) {
          compartmentGlyphList = (List<CompartmentGlyph>) compartment.getUserObject(LAYOUT_LINK);
        }
        if (!compartment.isSetOutside()) {
          sortedGlyphList.addAll(compartmentGlyphList);
          sortedGlyphList.addAll(getContainedCompartmentGlyphs(compartment));
        }
      }
    } else {
      // Draw all compartment glyphs as given in the layout
      sortedGlyphList.addAll(((LayoutModelPlugin) model.getPlugin(LayoutConstants.shortLabel)).getLayout(layoutIndex).getListOfCompartmentGlyphs());
    }
    return sortedGlyphList;
  }
  
  /**
   * Find the contained compartments of a compartment using its user object with
   * the key {@link #COMPARTMENT_LINK}.
   *
   * @param compartment
   *        for which to find the contained compartments
   * @return a {@code List<CompartmentGlyph>} with the compartments the given
   *         compartment contains
   */
  @SuppressWarnings("unchecked")
  private List<CompartmentGlyph> getContainedCompartmentGlyphs(Compartment compartment) {
    List<CompartmentGlyph> containedList = new LinkedList<CompartmentGlyph>();
    Object userObject = compartment.getUserObject(LAYOUT_LINK);
    if (userObject instanceof List<?>) {
      List<CompartmentGlyph> directlyContainedCompartmentGlyphs = (List<CompartmentGlyph>) userObject;
      containedList.addAll(directlyContainedCompartmentGlyphs);
      for (CompartmentGlyph containedCompartmentGlyph : directlyContainedCompartmentGlyphs) {
        Compartment comp = (Compartment) containedCompartmentGlyph.getReferenceInstance();
        if (comp != compartment) {
          // Condition prevents this recursion from becoming an infinite loop
          containedList.addAll(getContainedCompartmentGlyphs(comp));
        }
      }
    }
    return containedList;
  }
  
  /**
   * Create a list of contained compartments for every compartment and set it
   * as a user object with the key COMPARTMENT_LINK.
   */
  @SuppressWarnings("unchecked")
  private void createCompartmentLinks() {
    if (model.isSetListOfCompartments()) {
      ListOf<Compartment> compartmentList = model.getListOfCompartments();
      for (Compartment compartment : compartmentList) {
        if (compartment.isSetOutsideInstance()) {
          Compartment outside = compartment.getOutsideInstance();
          LinkedList<Compartment> userObject = new LinkedList<Compartment>();
          if (outside.getUserObject(COMPARTMENT_LINK) instanceof LinkedList<?>) {
            userObject = (LinkedList<Compartment>) outside.getUserObject(COMPARTMENT_LINK);
          }
          userObject.add(compartment);
          outside.putUserObject(COMPARTMENT_LINK, userObject);
        }
      }
    }
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return StringTools.concat(getClass().getSimpleName(),
      " [algorithm=", algorithm,
      ", builder=", builder,
      ", model=", model,
      ", layoutIndex=", layoutIndex,
      ", mapOfFluxes=", mapOfFluxes,
      ", addWhiskers=", addWhiskers, "]").toString();
  }
  
}
