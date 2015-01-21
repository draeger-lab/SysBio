/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBML Editor.
 *
 * Copyright (C) 2012-2015 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.graph.io;

import java.util.logging.Logger;

import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.ext.layout.BoundingBox;
import org.sbml.jsbml.ext.layout.CompartmentGlyph;
import org.sbml.jsbml.ext.layout.Dimensions;
import org.sbml.jsbml.ext.layout.Layout;
import org.sbml.jsbml.ext.layout.Point;
import org.sbml.jsbml.ext.layout.ReactionGlyph;
import org.sbml.jsbml.ext.layout.SpeciesGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceRole;
import org.sbml.jsbml.ext.layout.TextGlyph;

import y.base.Node;
import y.view.EditMode;
import y.view.GenericEdgeRealizer;
import de.zbit.sbml.layout.y.SBMLCreateEdgeMode;

/**
 * Creates yFiles graph information from a JSBML layout and draws it on the graph.
 * 
 * @author Andreas Dr&aum;ger
 * @author Alexander Diamantikos
 * @author Jakob Matthes
 * @author Eugen Netz
 * @author Jan Rudolph
 * @version $Rev$
 */
public class Layout2GraphML extends SB_2GraphML<Layout> {
	
	private class Constants {
	  public static final String GLYPH_NODE_KEY = "GLYPH_NODE_KEY";
	  public static final String GRAPHOBJECT_TEXTGLYPH_KEY = "GRAPHOBJECT_TEXTGLYPH_KEY";
	}
  
  private Logger logger = Logger.getLogger(Layout2GraphML.class.getName());
  
  private EditMode editMode;
	/**
	 * Constructor.
	 */
	public Layout2GraphML(EditMode editMode) {
		super();
		this.editMode = editMode;
	}

	/**
	 * Creates nodes and edges for all the CompartmentGlyphs, SpeciesGlyph, ReactionGlyphs and TextGlyphs in the layout.
	 * @param layout
	 */
	@Override
	protected void createNodesAndEdges(Layout layout) {
    if (layout == null
        || layout.getSBMLDocument() == null
        || !layout.getSBMLDocument().isSetModel()) {
      return;
    }
		initCompartments(layout);
		initTextGlyphs(layout);
		initSpeciesGlyphs(layout);
		initReactionGlyphs(layout);
		initReactionModifiers(layout);
		initTextGlyphs(layout);
		logger.info("Layout Information Initialized.");
	}

	/**
	 * Creates the edges for ReactionModifiers.
	 * @param layout
	 */
	private void initReactionModifiers(Layout layout) {
    SBMLCreateEdgeMode createEdgeMode = (SBMLCreateEdgeMode) this.editMode.getCreateEdgeMode();
    if (layout.isSetListOfReactionGlyphs()) {
    	for (ReactionGlyph r : layout.getListOfReactionGlyphs()) {
    		if (r.isSetListOfSpeciesReferencesGlyphs()) {
    			for (SpeciesReferenceGlyph sRef : r.getListOfSpeciesReferenceGlyphs()) {
    				if ((sRef.getSpeciesReferenceRole() != SpeciesReferenceRole.PRODUCT) && (sRef.getSpeciesReferenceRole() != SpeciesReferenceRole.SUBSTRATE)) {
    					Node source = (Node) sRef.getSpeciesGlyphInstance().getUserObject(Constants.GLYPH_NODE_KEY);
    					Node target = (Node) r.getUserObject(Constants.GLYPH_NODE_KEY);
    					if ((source != null) && (target != null)) {
    						createEdgeMode.createEdge(this.simpleGraph, source, target,
    							new GenericEdgeRealizer(), sRef.getSBOTerm());
    					}
    				}
    			}
    		}
    	}
    }
  }

  /**
   * Initializes the TextGlyphs.
	 * @param layout
	 */
	private void initTextGlyphs(Layout layout) {
		if (layout.isSetListOfTextGlyphs()) {
			for (TextGlyph textGlyph : layout.getListOfTextGlyphs()) {
				SpeciesGlyph s = layout.getSpeciesGlyph(textGlyph.getGraphicalObject());
				if (s != null) {
					s.putUserObject(Constants.GRAPHOBJECT_TEXTGLYPH_KEY, textGlyph);
				}
			}
		}
	}

	/**
	 * Creates the nodes for ReactionGlyphs and the corresponding edges.
	 * @param layout
	 */
	private void initReactionGlyphs(Layout layout) {
		if (layout.isSetListOfReactionGlyphs()) {
			for (ReactionGlyph r : layout.getListOfReactionGlyphs()) {
				
				Reaction reaction = (Reaction) r.getReactionInstance();
				
				Node source = null;
				Node target = null;
				if (r.isSetListOfSpeciesReferencesGlyphs()) {
					for (SpeciesReferenceGlyph sRef : r.getListOfSpeciesReferenceGlyphs()) {
						if (sRef.getSpeciesReferenceRole() == SpeciesReferenceRole.SUBSTRATE) {
							source = (Node) sRef.getSpeciesGlyphInstance().getUserObject(Constants.GLYPH_NODE_KEY);
						}
						if (sRef.getSpeciesReferenceRole() == SpeciesReferenceRole.PRODUCT) {
							target = (Node) sRef.getSpeciesGlyphInstance().getUserObject(Constants.GLYPH_NODE_KEY);
						}
					}
				}
				SBMLCreateEdgeMode createEdgeMode = (SBMLCreateEdgeMode) this.editMode.getCreateEdgeMode();
				Node n = createEdgeMode.createEdgeNode(this.simpleGraph, source, target,
					new GenericEdgeRealizer(), reaction.getReversible());	
				
				if (r.isSetBoundingBox()) {
					this.simpleGraph.setLocation(n, r.getBoundingBox().getPosition().getX(), r.getBoundingBox().getPosition().getY());
				}
				r.putUserObject(Constants.GLYPH_NODE_KEY, n);
			}
		}
	}

	/**
	 * Creates the nodes for SpeciesGlyphs.
	 * @param layout
	 */
	private void initSpeciesGlyphs(Layout layout) {
		if (layout.isSetListOfSpeciesGlyphs()) {
			for (SpeciesGlyph glyph : layout.getListOfSpeciesGlyphs()) {
				
				String speciesId = glyph.getSpecies();
				Species species = layout.getModel().getSpecies(speciesId);
				if (species == null) {
					continue;
				}
				
				String name = "(" + species.getName() + ")";
				TextGlyph textGlyph = (TextGlyph) glyph.getUserObject(Constants.GRAPHOBJECT_TEXTGLYPH_KEY);
				if ((textGlyph != null) && textGlyph.isSetReference()) {
					String namedSBase = textGlyph.getReference();
					Species originSpecies = textGlyph.getModel().getSpecies(namedSBase);
					if (originSpecies != null && originSpecies.isSetName()) {
						name = originSpecies.getName();
					}
				}
				
				Node n;
				if (glyph.isSetBoundingBox() && glyph.getBoundingBox().isSetPosition()
						&& glyph.getBoundingBox().isSetDimensions()) {
					BoundingBox bb = glyph.getBoundingBox();
					Dimensions dimensions = bb.getDimensions();
					Point point = bb.getPosition();
					n = createNode(speciesId, name, species.getSBOTerm(), point.getX(), point.getY(), dimensions.getWidth(), dimensions.getHeight());
				}
				else {
					n = createNode(speciesId, name, species.getSBOTerm());
				}
				glyph.putUserObject(Constants.GLYPH_NODE_KEY, n);
			}
		}
	}

	/**
	 * Creates the nodes for CompartmentGlyphs.
	 * @param layout
	 */
	private void initCompartments(Layout layout) {
		if (layout.isSetListOfCompartmentGlyphs()) {
			// TODO
			// String outmostCompartmentId = getOutmostCompartmentId(compartments);
			String outmostCompartmentId = "outmost";
			for (CompartmentGlyph c : layout.getListOfCompartmentGlyphs()) {
				// TODO order: outmost compartment -> innermost compartment
				if (c.getId().equals(outmostCompartmentId)) {
					continue;
				}
				Node n;
				if (c.isSetBoundingBox() && c.getBoundingBox().isSetDimensions()
						&& c.getBoundingBox().isSetPosition()) {
					BoundingBox bb = c.getBoundingBox();
					Dimensions dimensions = bb.getDimensions();
					Point point = bb.getPosition();
					n = createNode(c.getId(), c.getName(), c.getSBOTerm(),
						point.getX(), point.getY(), dimensions.getWidth(), dimensions.getHeight());
				}
				else {
					n = createNode(c.getId(), c.getName(), c.getSBOTerm());
				}
				c.putUserObject(Constants.GLYPH_NODE_KEY, n);
			}
		}
	}
	
	
	/**
	 * @param a
	 * @param b
	 * @return whether {@link BoundingBox} a contains {@link BoundingBox} b 
	 */
	private static boolean containsBoundingBox(BoundingBox a, BoundingBox b) {
	  // TODO Auto-generated method stub
	  return false;
	}

	
	@Override
	protected boolean isAnyLayoutInformationAvailable() {
		// TODO Auto-generated method stub
		return false;
	} 

}
