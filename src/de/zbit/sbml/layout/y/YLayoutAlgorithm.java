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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.sbml.jsbml.ext.layout.BoundingBox;
import org.sbml.jsbml.ext.layout.CompartmentGlyph;
import org.sbml.jsbml.ext.layout.Curve;
import org.sbml.jsbml.ext.layout.Dimensions;
import org.sbml.jsbml.ext.layout.GraphicalObject;
import org.sbml.jsbml.ext.layout.Layout;
import org.sbml.jsbml.ext.layout.NamedSBaseGlyph;
import org.sbml.jsbml.ext.layout.Point;
import org.sbml.jsbml.ext.layout.Position;
import org.sbml.jsbml.ext.layout.ReactionGlyph;
import org.sbml.jsbml.ext.layout.SpeciesGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph;
import org.sbml.jsbml.ext.layout.TextGlyph;

import y.base.DataMap;
import y.base.Node;
import y.view.Graph2D;
import y.view.NodeRealizer;
import y.view.ShapeNodeRealizer;
import de.zbit.graph.GraphTools;
import de.zbit.graph.io.Graph2Dwriter;
import de.zbit.graph.io.def.GenericDataMap;
import de.zbit.graph.io.def.GraphMLmaps;
import de.zbit.sbml.layout.LayoutAlgorithm;
import de.zbit.sbml.layout.LayoutDirector;

/**
 * @author Jakob Matthes
 * @version $Rev$
 */
public class YLayoutAlgorithm implements LayoutAlgorithm {
	
	/**
	 * Default value for z-coordinates and depth as YFiles works in two
	 * dimensional space but SBML allows three dimensional specifications.
	 */
	private static final double DEFAULT_Z_COORD = 0.0;
	private static final double DEFAULT_DEPTH = 0.0;

	/**
	 * Logger instance for informational output.
	 */
	private static Logger logger = Logger.getLogger(YLayoutAlgorithm.class.toString());
	
	/**
	 * SBML Layout instance.
	 */
	private Layout layout;
	
	/**
	 * Set to hold all layouted glyphs.
	 */
	private Set<GraphicalObject> setOfLayoutedGlyphs;

	/**
	 * Set to hold all unlayouted glyphs;
	 */
	private Set<GraphicalObject> setOfUnlayoutedGlyphs;
	
	/**
	 * Mapping of YFiles nodes to glyphs, containing only nodes/glyphs which
	 * need to be layouted.
	 */
	private Map<Node,GraphicalObject> nodeGlyphMap;
	
	/**
	 * Output of the algorithm, i.e. the autolayouted previously unlayouted
	 * glyphs of the input.
	 */
	private Set<GraphicalObject> output;
	
	/**
	 * Graph2D structure neccessary for using layout algorithms provided by
	 * YFiles.
	 */
	private Graph2D graph2D;
	
	/**
	 * GraphTools instance to handle partial layouting.
	 */
	private GraphTools graphTools;
	
	/**
	 * SBML level
	 */
	private static int level;
	
	/**
	 * SBML version
	 */
	private static int version;

	/**
	 * LayoutAlgorithm constructor.
	 */
	public YLayoutAlgorithm() {
		this.setOfLayoutedGlyphs = new HashSet<GraphicalObject>();
		this.setOfUnlayoutedGlyphs = new HashSet<GraphicalObject>();
		this.nodeGlyphMap = new HashMap<Node, GraphicalObject>();
		this.output = new HashSet<GraphicalObject>();
		graph2D = new Graph2D();
		GenericDataMap<DataMap, String> mapDescriptionMap =
			new GenericDataMap<DataMap, String>(Graph2Dwriter.mapDescription);
		graph2D.addDataProvider(Graph2Dwriter.mapDescription, mapDescriptionMap);
		graphTools = new GraphTools(graph2D);
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutAlgorithm#addLayoutedGlyph(org.sbml.jsbml.ext.layout.GraphicalObject)
	 */
	@Override
	public void addLayoutedGlyph(GraphicalObject glyph) {
		logger.info("add layouted glyph id=" + glyph.getId());
		setOfLayoutedGlyphs.add(glyph);
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutAlgorithm#addUnlayoutedGlyph(org.sbml.jsbml.ext.layout.GraphicalObject)
	 */
	@Override
	public void addUnlayoutedGlyph(GraphicalObject glyph) {
		logger.info("add unlayouted glyph id=" + glyph.getId());
		setOfUnlayoutedGlyphs.add(glyph);
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutAlgorithm#getAutolayoutedGlyphs()
	 */
	@Override
	public Set<GraphicalObject> getAutolayoutedGlyphs() {
		autolayout();
		
		for (Entry<Node,GraphicalObject> entry : nodeGlyphMap.entrySet()) {
			Node node = entry.getKey();
			NodeRealizer nodeRealizer = graph2D.getRealizer(node);
			GraphicalObject glyph = entry.getValue();
			
			BoundingBox boundingBox = glyph.isSetBoundingBox() ?
					glyph.getBoundingBox() : glyph.createBoundingBox();
			
			if (!LayoutDirector.glyphHasPosition(glyph)) {
				boundingBox.createPosition(nodeRealizer.getX(),
						nodeRealizer.getY(),
						DEFAULT_Z_COORD);
			}
			
			if (!LayoutDirector.glyphHasDimensions(glyph)) {
				boundingBox.createDimensions(nodeRealizer.getWidth(),
						nodeRealizer.getHeight(),
						DEFAULT_DEPTH);
			}
			
			output.add(glyph);
		}
		return output;
	}
	
	/**
	 * 
	 */
	private void autolayout() {
		// Add all layouted glyphs to
		for (GraphicalObject glyph : setOfLayoutedGlyphs) {
			logger.info("adding layouted glyphs");

			// text glyphs which are not independent and do not have a
			// bounding box are considered layouted and thus not processed here
			if (glyph instanceof TextGlyph &&
					!LayoutDirector.textGlyphIsIndependent((TextGlyph) glyph) &&
					!glyph.isSetBoundingBox()) {
				continue;
			}
			
			BoundingBox boundingBox = glyph.getBoundingBox();
			Dimensions dimensions = boundingBox.getDimensions();
			Point position = boundingBox.getPosition();
			int x, y, width, height;
			x = (int) position.getX();
			y = (int) position.getY();
			width = (int) dimensions.getWidth();
			height = (int) dimensions.getHeight();
			
			// a) Graph2D structure, using a simple node realizer
			NodeRealizer nodeRealizer = new ShapeNodeRealizer();
			nodeRealizer.setX(x);
			nodeRealizer.setY(y);
			nodeRealizer.setWidth(width);
			nodeRealizer.setHeight(height);
			Node node = graph2D.createNode(nodeRealizer);
			
			// b) GraphTools helper
			graphTools.setInfo(node, GraphMLmaps.NODE_POSITION, x + "|" + y);
			graphTools.setInfo(node, GraphMLmaps.NODE_SIZE, width + "|" + height);
		}
		
		// Add all unlayouted glyphs to Graph2D structure
		for (GraphicalObject glyph : setOfUnlayoutedGlyphs) {
			logger.info("adding unlayouted glyphs");
			NodeRealizer nodeRealizer = new ShapeNodeRealizer();
			Node node = graph2D.createNode(nodeRealizer);
			
			if (LayoutDirector.glyphHasPosition(glyph)) {
				BoundingBox boundingBox = glyph.getBoundingBox();
				Point position = boundingBox.getPosition();
				int x, y;
				x = (int) position.getX();
				y = (int) position.getY();
				nodeRealizer.setX(x);
				nodeRealizer.setY(y);
				graphTools.setInfo(node, GraphMLmaps.NODE_POSITION, x + "|" + y);
			}
			if (LayoutDirector.glyphHasDimensions(glyph)) {
				BoundingBox boundingBox = glyph.getBoundingBox();
				Dimensions dimensions = boundingBox.getDimensions();
				int width, height;
				width = (int) dimensions.getWidth();
				height = (int) dimensions.getHeight();
				nodeRealizer.setWidth(width);
				nodeRealizer.setHeight(height);
				graphTools.setInfo(node, GraphMLmaps.NODE_SIZE, width + "|" + height);
			}
			
			nodeGlyphMap.put(node, glyph);
		}

		// Set to hold all to-be-layouted YFiles nodes
		Set<Node> setOfUnlayoutedNodes = new HashSet<Node>();
		for (Entry<Node,GraphicalObject> entry : nodeGlyphMap.entrySet()) {
			setOfUnlayoutedNodes.add(entry.getKey());
		}
		
		// Autolayout of subset using GraphTools
		graphTools.layoutNodeSubset(setOfUnlayoutedNodes);
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

	// -------------------------------------------------------------------------
	
	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutAlgorithm#createLayoutDimension()
	 */
	@Override
	public Dimensions createLayoutDimension() {
		// TODO Auto-generated method stub
		return null;
	}

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
	 * @see de.zbit.sbml.layout.LayoutAlgorithm#createCompartmentGlyphDimension(org.sbml.jsbml.ext.layout.CompartmentGlyph)
	 */
	@Override
	public Dimensions createCompartmentGlyphDimension(
			CompartmentGlyph previousCompartmentGlyph) {
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
	 * @see de.zbit.sbml.layout.LayoutAlgorithm#createSpeciesGlyphDimension()
	 */
	@Override
	public Dimensions createSpeciesGlyphDimension() {
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
	 * @see de.zbit.sbml.layout.LayoutAlgorithm#createTextGlyphDimension(org.sbml.jsbml.ext.layout.TextGlyph)
	 */
	@Override
	public Dimensions createTextGlyphDimension(TextGlyph textGlyph) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutAlgorithm#getLayout()
	 */
	@Override
	public Layout getLayout() {
		return this.layout;
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutAlgorithm#isSetLayout()
	 */
	@Override
	public boolean isSetLayout() {
		return (layout != null);
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutAlgorithm#setLayout(org.sbml.jsbml.ext.layout.Layout)
	 */
	@Override
	public void setLayout(Layout layout) {
		this.layout = layout;
		this.level = layout.getLevel();
		this.version = layout.getVersion();
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutAlgorithm#createCompartmentGlyphPosition(org.sbml.jsbml.ext.layout.CompartmentGlyph)
	 */
	@Override
	public Position createCompartmentGlyphPosition(
			CompartmentGlyph previousCompartmentGlyph) {
		return null;
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutAlgorithm#createReactionGlyphPosition(org.sbml.jsbml.ext.layout.ReactionGlyph)
	 */
	@Override
	public Position createReactionGlyphPosition(ReactionGlyph reactionGlyph) {
		return null;
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutAlgorithm#createSpeciesGlyphPosition(org.sbml.jsbml.ext.layout.SpeciesGlyph)
	 */
	@Override
	public Position createSpeciesGlyphPosition(SpeciesGlyph speciesGlyph) {
		return null;
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutAlgorithm#createSpeciesGlyphPosition(org.sbml.jsbml.ext.layout.SpeciesGlyph, org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph)
	 */
	@Override
	public Position createSpeciesGlyphPosition(SpeciesGlyph speciesGlyph,
			SpeciesReferenceGlyph specieReferenceGlyph) {
		return null;
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutAlgorithm#createTextGlyphPosition(org.sbml.jsbml.ext.layout.TextGlyph)
	 */
	@Override
	public Position createTextGlyphPosition(TextGlyph textGlyph) {
		return null;
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutAlgorithm#createSpeciesReferenceGlyphPosition(org.sbml.jsbml.ext.layout.ReactionGlyph, org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph)
	 */
	@Override
	public Position createSpeciesReferenceGlyphPosition(
			ReactionGlyph reactionGlyph,
			SpeciesReferenceGlyph speciesReferenceGlyph) {
		return null;
	}

}
