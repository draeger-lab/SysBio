/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2012 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.sbml.layout.y;

import org.sbml.jsbml.ext.layout.BoundingBox;
import org.sbml.jsbml.ext.layout.CompartmentGlyph;
import org.sbml.jsbml.ext.layout.Curve;
import org.sbml.jsbml.ext.layout.Dimensions;
import org.sbml.jsbml.ext.layout.Layout;
import org.sbml.jsbml.ext.layout.NamedSBaseGlyph;
import org.sbml.jsbml.ext.layout.Position;
import org.sbml.jsbml.ext.layout.ReactionGlyph;
import org.sbml.jsbml.ext.layout.SpeciesGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph;
import org.sbml.jsbml.ext.layout.TextGlyph;

import de.zbit.sbml.layout.LayoutAlgorithm;

/**
 * @author Jakob Matthes
 * @version $Rev$
 */
public class YLayoutAlgorithm implements LayoutAlgorithm {

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutAlgorithm#calculateReactionGlyphRotationAngle(org.sbml.jsbml.ext.layout.ReactionGlyph)
	 */
	@Override
	public double calculateReactionGlyphRotationAngle(
			ReactionGlyph reactionGlyph) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutAlgorithm#createCompartmentGlyphBoundingBox(org.sbml.jsbml.ext.layout.CompartmentGlyph)
	 */
	@Override
	public BoundingBox createCompartmentGlyphBoundingBox(
			CompartmentGlyph previousCompartmentGlyph) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutAlgorithm#createCompartmentGlyphDimension(org.sbml.jsbml.ext.layout.CompartmentGlyph)
	 */
	@Override
	public Dimensions createCompartmentGlyphDimension(
			CompartmentGlyph previousCompartmentGlyph) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutAlgorithm#createCompartmentGlyphPosition(org.sbml.jsbml.ext.layout.CompartmentGlyph)
	 */
	@Override
	public Position createCompartmentGlyphPosition(
			CompartmentGlyph previousCompartmentGlyph) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutAlgorithm#createCurve(org.sbml.jsbml.ext.layout.ReactionGlyph, org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph)
	 */
	@Override
	public Curve createCurve(ReactionGlyph reactionGlyph,
			SpeciesReferenceGlyph speciesReferenceGlyph) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutAlgorithm#createGlyphBoundingBox(org.sbml.jsbml.ext.layout.NamedSBaseGlyph, org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph)
	 */
	@Override
	public BoundingBox createGlyphBoundingBox(NamedSBaseGlyph glyph,
			SpeciesReferenceGlyph specRefGlyph) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutAlgorithm#createLayoutDimension()
	 */
	@Override
	public Dimensions createLayoutDimension() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutAlgorithm#createReactionGlyphBoundingBox(org.sbml.jsbml.ext.layout.ReactionGlyph)
	 */
	@Override
	public BoundingBox createReactionGlyphBoundingBox(
			ReactionGlyph reactionGlyph) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutAlgorithm#createReactionGlyphDimension(org.sbml.jsbml.ext.layout.ReactionGlyph)
	 */
	@Override
	public Dimensions createReactionGlyphDimension(ReactionGlyph reactionGlyph) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutAlgorithm#createReactionGlyphPosition(org.sbml.jsbml.ext.layout.ReactionGlyph)
	 */
	@Override
	public Position createReactionGlyphPosition(ReactionGlyph reactionGlyph) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutAlgorithm#createSpeciesGlyphBoundingBox(org.sbml.jsbml.ext.layout.SpeciesGlyph)
	 */
	@Override
	public BoundingBox createSpeciesGlyphBoundingBox(SpeciesGlyph speciesGlyph) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutAlgorithm#createSpeciesGlyphBoundingBox(org.sbml.jsbml.ext.layout.SpeciesGlyph, org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph)
	 */
	@Override
	public BoundingBox createSpeciesGlyphBoundingBox(SpeciesGlyph speciesGlyph,
			SpeciesReferenceGlyph speciesReferenceGlyph) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutAlgorithm#createSpeciesGlyphDimension()
	 */
	@Override
	public Dimensions createSpeciesGlyphDimension() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutAlgorithm#createSpeciesGlyphPosition(org.sbml.jsbml.ext.layout.SpeciesGlyph)
	 */
	@Override
	public Position createSpeciesGlyphPosition(SpeciesGlyph speciesGlyph) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutAlgorithm#createSpeciesGlyphPosition(org.sbml.jsbml.ext.layout.SpeciesGlyph, org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph)
	 */
	@Override
	public Position createSpeciesGlyphPosition(SpeciesGlyph speciesGlyph,
			SpeciesReferenceGlyph specieReferenceGlyph) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutAlgorithm#createSpeciesReferenceGlyphBoundingBox(org.sbml.jsbml.ext.layout.ReactionGlyph, org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph)
	 */
	@Override
	public BoundingBox createSpeciesReferenceGlyphBoundingBox(
			ReactionGlyph reactionGlyph,
			SpeciesReferenceGlyph speciesReferenceGlyph) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutAlgorithm#createSpeciesReferenceGlyphDimension(org.sbml.jsbml.ext.layout.ReactionGlyph, org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph)
	 */
	@Override
	public Dimensions createSpeciesReferenceGlyphDimension(
			ReactionGlyph reactionGlyph,
			SpeciesReferenceGlyph speciesReferenceGlyph) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutAlgorithm#createSpeciesReferenceGlyphPosition(org.sbml.jsbml.ext.layout.ReactionGlyph, org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph)
	 */
	@Override
	public Position createSpeciesReferenceGlyphPosition(
			ReactionGlyph reactionGlyph,
			SpeciesReferenceGlyph speciesReferenceGlyph) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutAlgorithm#createTextGlyphBoundingBox(org.sbml.jsbml.ext.layout.TextGlyph)
	 */
	@Override
	public BoundingBox createTextGlyphBoundingBox(TextGlyph textGlyph) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutAlgorithm#createTextGlyphDimension(org.sbml.jsbml.ext.layout.TextGlyph)
	 */
	@Override
	public Dimensions createTextGlyphDimension(TextGlyph textGlyph) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutAlgorithm#createTextGlyphPosition(org.sbml.jsbml.ext.layout.TextGlyph)
	 */
	@Override
	public Position createTextGlyphPosition(TextGlyph textGlyph) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutAlgorithm#getLayout()
	 */
	@Override
	public Layout getLayout() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutAlgorithm#isSetLayout()
	 */
	@Override
	public boolean isSetLayout() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutAlgorithm#setLayout(org.sbml.jsbml.ext.layout.Layout)
	 */
	@Override
	public void setLayout(Layout layout) {
		// TODO Auto-generated method stub
		
	}

}
