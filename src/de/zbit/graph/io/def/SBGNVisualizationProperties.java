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
 * Copyright (C) 2011 by the University of Tuebingen, Germany.
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.sbml.jsbml.SBO;

import y.view.NodeRealizer;
import y.view.ShapeNodeRealizer;
import de.zbit.graph.sbgn.ComplexNode;
import de.zbit.graph.sbgn.EmptySetNode;
import de.zbit.graph.sbgn.NucleicAcidFeatureNode;
import de.zbit.graph.sbgn.ReactionNodeRealizer;
import de.zbit.graph.sbgn.ShapeNodeRealizerSupportingCloneMarker;

/**
 * This class stores the sbo terms and the corresponding NodeRealizer classes to
 * visualize SBML documents in SBGN-style.
 * 
 * @author Finja B&uuml;chel
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class SBGNVisualizationProperties {

  public static final Logger log = Logger.getLogger(SBGNVisualizationProperties.class.getName());
  
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
  
  public static final int materialEntityOfUnspecifiedNature = 285;
  
  /**
   * References to complete maps, e.g., to other pathways.
   */
  public static final int map = 552;
  public static final int submap = 395;
  
  public static final int process = 375;
  public static final int omittedProcess = 397;
  public static final int uncertainProcess = 396;
  
  /**
   * Other SBO terms that should be visualized in the same manner as {@link SBO#getNonCovalentComplex()}s.
   * TODO: These complexes below are actually not correct. But Linking them to
   * ComplexNode is better than the default. But still, the real SBGN-conform
   * specification differs!
   */
  private static final int[] nonCovalentComplex_synonyms = new int[]{
  //macromolecular complex-branch
  296, 420, 543, 297,
  // Multimere branch
  286, 418, 419, 420, 421};
  
  
  /**
   * Static constructor to fill our static maps.
   */
  {
    init();
  }

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
    
    // Sort all synonyms for allowing a binary search later
    Arrays.sort(nonCovalentComplex_synonyms);
    Arrays.sort(simpleChemical_synonyms);
    Arrays.sort(macromolecule_synonyms);
    
    // Make the collection unmodifiable.
    SBGNVisualizationProperties.sbo2shape = Collections.unmodifiableMap(sbo2shape);
  }
  
  /**
   * @return the color of the appropriate shape
   */
  public static Color getFillColor(int sboTerm) {
    if (SBO.isChildOf(sboTerm, SBO.getNonCovalentComplex())){
      return new Color(24,116,205);    // DodgerBlue3
    } else if (SBO.isChildOf(sboTerm, SBO.getGene())){ 
      return new Color(255,255,0);    // Yellow
    } else if (SBO.isChildOf(sboTerm, SBO.getMacromolecule())){
      return new Color(0,205,0);       // Green 3
    } else if (SBO.isChildOf(sboTerm, SBO.getSimpleMolecule())){
      return new Color(176,226,255);   // LightSkyBlue1
    } else if ((sboTerm == map) || (sboTerm == submap)) {
      return new Color(224,238,238);   // azure2
    } else if (SBO.isChildOf(sboTerm, SBO.getEmptySet())) {
    	return new Color(255, 204, 204); // Pink
    } else if (SBO.isChildOf(sboTerm, SBO.getCompartment())) {
    	return new Color(243, 243, 191);
    } else {
      return new Color(144,238,144);   // LightGreen
    }
  }
  
  /**
   * 
   * @param sboTerm
   * @return
   */
  public static Color getLineColor(int sboTerm) {
  	if (SBO.isChildOf(sboTerm, SBO.getCompartment())) {
  		// dark yellow
  		return new Color(204, 204, 0);
  	}
  	return Color.BLACK;
  }

  /**
   * 
   * @param sboTerm
   * @return the adaquate {@link ShapeNodeRealizer}, if for this sboterm no {@link ShapeNodeRealizer}
   * is available it return the {@link #defaultShape}
   */
  public static NodeRealizer getNodeRealizer(int sboTerm) {
    if (sbo2shape == null) {
      init();
    }
    NodeRealizer ret = sbo2shape.get(sboTerm);
    if (ret == null) {
      ret = defaultShape;
      log.log(Level.WARNING, "sboTerm: " + sboTerm + " couldn't be assigned to a shape, default shape is used");
    }
    
    // Set a common color
    ret.setFillColor(getFillColor(sboTerm));
    
    return ret;
  }

  /**
   * Most graphics suites (e.g. yFiles) don't distinct an elipse
   * and a circle, but SBGN does. If this returns true, a node
   * realizer, even if it is an eliptical realizer, should have
   * the same with and height (resulting in a circle).
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
