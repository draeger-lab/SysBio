/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of KEGGtranslator, a program to convert KGML files
 * from the KEGG database into various other formats, e.g., SBML, GML,
 * GraphML, and many more. Please visit the project homepage at
 * <http://www.cogsys.cs.uni-tuebingen.de/software/KEGGtranslator> to
 * obtain the latest version of KEGGtranslator.
 *
 * Copyright (C) 2011-2015 by the University of Tuebingen, Germany.
 *
 * KEGGtranslator is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.graph.io.def;

import java.awt.Color;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.sbml.jsbml.SBO;

import y.view.NodeRealizer;
import y.view.ShapeNodeRealizer;
import de.zbit.graph.sbgn.CompartmentRealizer;
import de.zbit.graph.sbgn.ComplexNode;
import de.zbit.graph.sbgn.DrawingOptions;
import de.zbit.graph.sbgn.EmptySetNode;
import de.zbit.graph.sbgn.NucleicAcidFeatureNode;
import de.zbit.graph.sbgn.PerturbingAgentNode;
import de.zbit.graph.sbgn.ReactionNodeRealizer;
import de.zbit.graph.sbgn.ShapeNodeRealizerSupportingCloneMarker;
import de.zbit.util.prefs.Option;
import de.zbit.util.prefs.SBPreferences;

/**
 * This class stores the sbo terms and the corresponding NodeRealizer classes to
 * visualize SBML documents in SBGN-style.
 * 
 * @author Finja B&uuml;chel
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class SBGNVisualizationProperties {
  
  /**
   * User preferences.
   */
  private static final transient SBPreferences prefs = SBPreferences.getPreferencesFor(DrawingOptions.class);
  
  /**
   * A {@link Logger} for this class.
   */
  public static final transient Logger log = Logger.getLogger(SBGNVisualizationProperties.class.getName());
  
  /**
   * contains all available shapes
   */
  public static Map<Integer, NodeRealizer> sbo2shape;
  
  /**
   * default shape is an Ellipse
   */
  private final static ShapeNodeRealizer defaultShape = new ShapeNodeRealizerSupportingCloneMarker(ShapeNodeRealizer.ELLIPSE);
  
  /**
   * Other SBO terms that should be visualized in the same manner as {@link SBO#getMacromolecule()}.
   */
  private static final int[] macromolecule_synonyms = new int[]{248, 249, 246, 251, 252, 250};
  
  /**
   * Other SBO terms that should be visualized in the same manner as {@link SBO#getSimpleMolecule()}s.
   * 327 = non-macromolecular ion
   * 328 = non-macromolecular radical
   */
  private static final int[] simpleChemical_synonyms = new int[]{327, 328};
  
  /**
   * 
   */
  public static final int materialEntityOfUnspecifiedNature = 285;
  
  /**
   * References to complete maps, e.g., to other pathways.
   */
  public static final int map = 552;
  /**
   * 
   */
  public static final int submap = 395;
  
  /**
   * 
   */
  public static final int process = 375;
  /**
   * 
   */
  public static final int omittedProcess = 397;
  /**
   * 
   */
  public static final int uncertainProcess = 396;
  
  /**
   * Other SBO terms that should be visualized in the same manner as {@link SBO#getNonCovalentComplex()}s.
   * TODO: These complexes below are actually not correct. But Linking them to
   * {@link ComplexNode} is better than the default. But still, the real SBGN-conform
   * specification differs!
   */
  private static final int[] nonCovalentComplex_synonyms = new int[] {
    //macromolecular complex-branch
    296, 420, 543, 297,
    // Multimere branch
    286, 418, 419, 420, 421
  };
  
  /**
   * Static constructor to fill our static maps.
   */
  static {
    init();
  }
  
  /**
   * 
   */
  private static void init() {
    /*
     * NOTE: These are all SBO terms from the SBO:0000240 - "material entity" branch.
     * See http://www.ebi.ac.uk/sbo/main/tree.do?open=240
     */
    sbo2shape = new HashMap<Integer, NodeRealizer>();
    
    sbo2shape.put(SBO.getMacromolecule(), getEnzymeRelizerRaw()); // macromolecule - enzyme
    // Sub-branches in the macromolecule-SBO-tree
    
    for (int sbo:macromolecule_synonyms) {
      sbo2shape.put(sbo, sbo2shape.get(SBO.getMacromolecule()));
    }
    
    sbo2shape.put(SBO.getSimpleMolecule(), new ShapeNodeRealizerSupportingCloneMarker(ShapeNodeRealizer.ELLIPSE)); // simple chemical - simple chemical
    // Sub-branches in the simpleChemical-SBO-tree
    for (int sbo:simpleChemical_synonyms) {
      sbo2shape.put(sbo, sbo2shape.get(SBO.getSimpleMolecule()));
    }
    
    sbo2shape.put(SBO.getGene(), new NucleicAcidFeatureNode()); // nucleic acid feature - gene
    sbo2shape.put(materialEntityOfUnspecifiedNature, new ShapeNodeRealizerSupportingCloneMarker(ShapeNodeRealizer.ELLIPSE)); // unspecified - material entity of unspecified nature
    
    sbo2shape.put(SBO.getEmptySet(), new EmptySetNode()); // empty set
    sbo2shape.put(SBO.getPertubingAgent(), new PerturbingAgentNode()); // perturbing agent
    sbo2shape.put(SBO.getNonCovalentComplex(), new ComplexNode()); // complex - non-covalent complex
    for (int sbo : nonCovalentComplex_synonyms) {
      sbo2shape.put(sbo, sbo2shape.get(SBO.getNonCovalentComplex()));
    }
    
    sbo2shape.put(map, new ShapeNodeRealizerSupportingCloneMarker(ShapeNodeRealizer.RECT)); // unspecified - empty set
    sbo2shape.put(submap, sbo2shape.get(map));
    
    sbo2shape.put(process, new ReactionNodeRealizer());
    ReactionNodeRealizer opr = new ReactionNodeRealizer();
    opr.setLabelText("\\\\");
    sbo2shape.put(omittedProcess, opr);
    ReactionNodeRealizer ucpr = new ReactionNodeRealizer();
    ucpr.setLabelText("?");
    sbo2shape.put(uncertainProcess, opr);
    
    sbo2shape.put(SBO.getPhysicalCompartment(), new CompartmentRealizer());
    sbo2shape.put(SBO.getFunctionalCompartment(), sbo2shape.get(SBO.getPhysicalCompartment()));
    sbo2shape.put(410, sbo2shape.get(SBO.getPhysicalCompartment())); // Implicit Compartment
    
    
    // Sort all synonyms for allowing a binary search later
    Arrays.sort(nonCovalentComplex_synonyms);
    Arrays.sort(simpleChemical_synonyms);
    Arrays.sort(macromolecule_synonyms);
    
    // Make the collection unmodifiable.
    SBGNVisualizationProperties.sbo2shape = Collections.unmodifiableMap(sbo2shape);
  }
  
  /**
   * @param sboTerm
   * @return the color of the appropriate shape
   */
  public static Color getFillColor(int sboTerm) {
    if (SBO.isChildOf(sboTerm, SBO.getNonCovalentComplex())) {
      // DodgerBlue3
      return Option.parseOrCast(Color.class,
        prefs.get(DrawingOptions.NONCOVALENT_COMPLEX_FILL_COLOR));
    } else if (SBO.isChildOf(sboTerm, SBO.getGene())) {
      // Yellow
      return Option.parseOrCast(Color.class,
        prefs.get(DrawingOptions.GENE_FILL_COLOR));
    } else if (SBO.isChildOf(sboTerm, SBO.getMacromolecule())) {
      // Green 3
      return Option.parseOrCast(Color.class,
        prefs.get(DrawingOptions.MACROMOLECULE_FILL_COLOR));
    } else if (SBO.isChildOf(sboTerm, SBO.getSimpleMolecule())) {
      // LightSkyBlue1
      return Option.parseOrCast(Color.class,
        prefs.get(DrawingOptions.SIMPLE_MOLECULE_FILL_COLOR));
    } else if ((sboTerm == map) || (sboTerm == submap)) {
      // azure2
      return new Color(224,238,238);
    } else if (SBO.isChildOf(sboTerm, SBO.getEmptySet())) {
      // Pink
      return Option.parseOrCast(Color.class,
        prefs.get(DrawingOptions.EMPTY_SET_FILL_COLOR));
    } else if (SBO.isChildOf(sboTerm, SBO.getCompartment())) {
      // Some kind of yellowish
      return Option.parseOrCast(Color.class,
        prefs.get(DrawingOptions.COMPARTMENT_FILL_COLOR));
    } else if (SBO.isChildOf(sboTerm, SBO.getPertubingAgent())) {
      // Drug pink
      return Option.parseOrCast(Color.class,
        prefs.get(DrawingOptions.PERTURBING_AGENT_FILL_COLOR));
    } else {
      // LightGreen
      return Option.parseOrCast(Color.class,
        prefs.get(DrawingOptions.DEFAULT_FILL_COLOR));
    }
  }
  
  /**
   * 
   * @param sboTerm
   * @return
   */
  public static Color getLineColor(int sboTerm) {
    SBPreferences prefs = SBPreferences.getPreferencesFor(DrawingOptions.class);
    if (SBO.isChildOf(sboTerm, SBO.getCompartment())) {
      return Option.parseOrCast(Color.class,
        prefs.get(DrawingOptions.COMPARTMENT_LINE_COLOR));
    }
    return Option.parseOrCast(Color.class,
      prefs.get(DrawingOptions.DEFAULT_FILL_COLOR));
  }
  
  /**
   * 
   * @param sboTerm
   * @return the adequate {@link ShapeNodeRealizer}, if for this sboterm no
   *         {@link ShapeNodeRealizer} is available it return the
   *         {@link #defaultShape}
   */
  public static NodeRealizer getNodeRealizer(int sboTerm) {
    if (sbo2shape == null) {
      init();
    }
    NodeRealizer ret = sbo2shape.get(sboTerm);
    if (ret == null) {
      ret = defaultShape;
      log.warning(MessageFormat.format(
        "SBO term: {0,number,integer} could not be assigned to a shape, default shape is used.",
        sboTerm));
    }
    
    // Set a common color
    ret.setFillColor(getFillColor(sboTerm));
    ret.setLineColor(getLineColor(sboTerm));
    
    return ret;
  }
  
  /**
   * Most graphics suites (e.g., yFiles) don't distinct an ellipse
   * and a circle, but SBGN does. If this returns true, a node
   * realizer, even if it is an elliptical realizer, should have
   * the same with and height (resulting in a circle).
   * 
   * @param sboTerm
   * @return
   */
  public static boolean isCircleShape(int sboTerm) {
    return SBO.isChildOf(sboTerm, SBO.getSimpleMolecule())
        || SBO.isChildOf(sboTerm, SBO.getEmptySet());
  }
  
  /**
   * Get a raw (unmodified) realizer for an enzyme.
   * @return {@link ShapeNodeRealizer}, specially made for enzymes.
   */
  private static ShapeNodeRealizer getEnzymeRelizerRaw() {
    return new ShapeNodeRealizerSupportingCloneMarker(ShapeNodeRealizer.ROUND_RECT);
  }
  
}
