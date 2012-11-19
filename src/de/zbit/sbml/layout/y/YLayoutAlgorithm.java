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

import java.awt.Rectangle;
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
import org.sbml.jsbml.ext.layout.Point;
import org.sbml.jsbml.ext.layout.Position;
import org.sbml.jsbml.ext.layout.ReactionGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph;
import org.sbml.jsbml.ext.layout.TextGlyph;

import y.base.DataMap;
import y.base.Edge;
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
import de.zbit.util.objectwrapper.ValuePairUncomparable;

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

	private Set<ValuePairUncomparable<SpeciesReferenceGlyph, ReactionGlyph>> setOfUnlayoutedEdges;
	private Set<ValuePairUncomparable<SpeciesReferenceGlyph, ReactionGlyph>> setOfLayoutedEdges;
	
	/**
	 * Mapping of YFiles nodes to glyphs, containing only nodes/glyphs which
	 * need to be layouted.
	 */
	private Map<Node,GraphicalObject> nodeIncompleteGlyphMap;
	
	/**
	 * Mapping of glyph id to the corresponding YFiles node.
	 */
	private Map<String,Node> glyphNodeMap;
	
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
	private int level;
	
	/**
	 * SBML version
	 */
	private int version;

	/**
	 * LayoutAlgorithm constructor.
	 */
	public YLayoutAlgorithm() {
		this.setOfLayoutedGlyphs = new HashSet<GraphicalObject>();
		this.setOfUnlayoutedGlyphs = new HashSet<GraphicalObject>();

		this.setOfUnlayoutedEdges =
			new HashSet<ValuePairUncomparable<SpeciesReferenceGlyph,ReactionGlyph>>();
		this.setOfLayoutedEdges =
			new HashSet<ValuePairUncomparable<SpeciesReferenceGlyph,ReactionGlyph>>();
		
		this.nodeIncompleteGlyphMap = new HashMap<Node, GraphicalObject>();
		this.glyphNodeMap = new HashMap<String, Node>();
		
		this.output = new HashSet<GraphicalObject>();
		
		graph2D = new Graph2D();
		GenericDataMap<DataMap, String> mapDescriptionMap =
			new GenericDataMap<DataMap, String>(Graph2Dwriter.mapDescription);
		graph2D.addDataProvider(Graph2Dwriter.mapDescription, mapDescriptionMap);
		
		graphTools = new GraphTools(graph2D);
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutAlgorithm#addLayoutedEdge(org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph, org.sbml.jsbml.ext.layout.ReactionGlyph)
	 */
	@Override
	public void addLayoutedEdge(SpeciesReferenceGlyph srg, ReactionGlyph rg) {
		// edge creation has to be deferred
		ValuePairUncomparable<SpeciesReferenceGlyph, ReactionGlyph> pair =
			new ValuePairUncomparable<SpeciesReferenceGlyph, ReactionGlyph>(srg, rg);
		setOfLayoutedEdges.add(pair);
		addLayoutedGlyph(rg);
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutAlgorithm#addUnlayoutedEdge(org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph, org.sbml.jsbml.ext.layout.ReactionGlyph)
	 */
	@Override
	public void addUnlayoutedEdge(SpeciesReferenceGlyph srg, ReactionGlyph rg) {
		// edge creation has to be deferred
		ValuePairUncomparable<SpeciesReferenceGlyph, ReactionGlyph> pair =
			new ValuePairUncomparable<SpeciesReferenceGlyph, ReactionGlyph>(srg, rg);
		setOfUnlayoutedEdges.add(pair);
		addUnlayoutedGlyph(rg);
	}
	
	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutAlgorithm#addLayoutedGlyph(org.sbml.jsbml.ext.layout.GraphicalObject)
	 */
	@Override
	public void addLayoutedGlyph(GraphicalObject glyph) {
		logger.info("add layouted glyph id=" + glyph.getId());
		
		// text glyphs which are not independent and do not have a
		// bounding box are considered layouted and thus not processed here
		if (glyph instanceof TextGlyph &&
				!LayoutDirector.textGlyphIsIndependent((TextGlyph) glyph) &&
				!glyph.isSetBoundingBox()) {
			return;
		}
		
		BoundingBox boundingBox = glyph.getBoundingBox();
		Dimensions dimensions = boundingBox.getDimensions();
		Point position = boundingBox.getPosition();
		int x, y, width, height;
		x = (int) position.getX();
		y = (int) position.getY();
		width = (int) dimensions.getWidth();
		height = (int) dimensions.getHeight();
		
		// Graph2D structure
		NodeRealizer nodeRealizer = new ShapeNodeRealizer();
		nodeRealizer.setLocation(x, y);
		nodeRealizer.setSize(width, height);
		Node node = graph2D.createNode(nodeRealizer);
		
		// GraphTools helper
		graphTools.setInfo(node, GraphMLmaps.NODE_POSITION, x + "|" + y);
		graphTools.setInfo(node, GraphMLmaps.NODE_SIZE, width + "|" + height);
		
		setOfLayoutedGlyphs.add(glyph);
		glyphNodeMap.put(glyph.getId(), node);
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutAlgorithm#addUnlayoutedGlyph(org.sbml.jsbml.ext.layout.GraphicalObject)
	 */
	@Override
	public void addUnlayoutedGlyph(GraphicalObject glyph) {
		logger.info("add unlayouted glyph id=" + glyph.getId());
		
		NodeRealizer nodeRealizer = new ShapeNodeRealizer();
		Node node = graph2D.createNode(nodeRealizer);
		
		// partially layouted: position only
		if (LayoutDirector.glyphHasPosition(glyph)) {
			BoundingBox boundingBox = glyph.getBoundingBox();
			Point position = boundingBox.getPosition();
			int x, y;
			x = (int) position.getX();
			y = (int) position.getY();
			nodeRealizer.setLocation(x, y);
			graphTools.setInfo(node, GraphMLmaps.NODE_POSITION, x + "|" + y);
		}
		// partially layouted: dimensions only
		if (LayoutDirector.glyphHasDimensions(glyph)) {
			BoundingBox boundingBox = glyph.getBoundingBox();
			Dimensions dimensions = boundingBox.getDimensions();
			int width, height;
			width = (int) dimensions.getWidth();
			height = (int) dimensions.getHeight();
			nodeRealizer.setSize(width, height);
			graphTools.setInfo(node, GraphMLmaps.NODE_SIZE, width + "|" + height);
		}
		
		nodeIncompleteGlyphMap.put(node, glyph);
		glyphNodeMap.put(glyph.getId(), node);
		setOfUnlayoutedGlyphs.add(glyph);
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutAlgorithm#getAutolayoutedGlyphs()
	 */
	@Override
	public Set<GraphicalObject> completeGlyphs() {
		// Set to hold all to-be-layouted YFiles nodes
		Set<Node> setOfUnlayoutedNodes = new HashSet<Node>();
		for (Entry<Node,GraphicalObject> entry : nodeIncompleteGlyphMap.entrySet()) {
			setOfUnlayoutedNodes.add(entry.getKey());
		}
		
		// Create all edges
		for (ValuePairUncomparable<SpeciesReferenceGlyph, ReactionGlyph> pair : setOfLayoutedEdges) {
			SpeciesReferenceGlyph speciesReferenceGlyph = pair.getA();
			ReactionGlyph reactionGlyph = pair.getB();
			handleEdge(speciesReferenceGlyph, reactionGlyph);
		}
		for (ValuePairUncomparable<SpeciesReferenceGlyph, ReactionGlyph> pair : setOfUnlayoutedEdges) {
			SpeciesReferenceGlyph speciesReferenceGlyph = pair.getA();
			ReactionGlyph reactionGlyph = pair.getB();
			handleEdge(speciesReferenceGlyph, reactionGlyph);
		}
		
		// Autolayout of node subset using GraphTools
		graphTools.layoutNodeSubset(setOfUnlayoutedNodes);
		
		// copy realizer information to glyph bounding box
		for (Entry<Node,GraphicalObject> entry : nodeIncompleteGlyphMap.entrySet()) {
			Node node = entry.getKey();
			NodeRealizer nodeRealizer = graph2D.getRealizer(node);
			GraphicalObject glyph = entry.getValue();
			
			
			// get or create bounding box for glyphs
			BoundingBox boundingBox = glyph.isSetBoundingBox() ?
					glyph.getBoundingBox() : glyph.createBoundingBox();
			
			// copy position
			if (!LayoutDirector.glyphHasPosition(glyph)) {
				logger.info("completing glyph position id=" + glyph.getId());
				boundingBox.createPosition(nodeRealizer.getX(),
						nodeRealizer.getY(),
						DEFAULT_Z_COORD);
			}
			
			// copy dimensions
			if (!LayoutDirector.glyphHasDimensions(glyph)) {
				logger.info("completing glyph dimensions id=" + glyph.getId());
				boundingBox.createDimensions(nodeRealizer.getWidth(),
						nodeRealizer.getHeight(),
						DEFAULT_DEPTH);
			}
			
			output.add(glyph);
		}
		
		return output;
	}

	private void handleEdge(SpeciesReferenceGlyph srg, ReactionGlyph rg) {
		Node processNode = glyphNodeMap.get(rg.getId());
		Node speciesGlyphNode = glyphNodeMap.get(srg.getSpeciesGlyph());
		assert glyphNodeMap.containsKey(rg.getId());
		assert processNode != null;
		assert speciesGlyphNode != null;
		assert graph2D != null;
		
		// create edge
		Edge edge = graph2D.createEdge(processNode, speciesGlyphNode);
		
		// copy edge data to curve of species reference glyph
		if (!srg.isSetCurve()) {
//		if (!srg.isSetCurve() || srg.getCurve().getListOfCurveSegments().isEmpty()) {
//			YPoint source = graph2D.getRealizer(edge).getSourcePoint();
//			Point start = new Point(source.getX(), source.getY(), 0.0);
//			YPoint target = graph2D.getRealizer(edge).getSourcePoint();
//			Point end = new Point(target.getX(), target.getY(), 0.0);

//			logger.info(String.format("add curve for rgId=%s  srgId=%s  start=%s  end=%s",
//					rg.getId(), srg.getId(), start.toString(), end.toString()));

//			CurveSegment curveSegment = new CurveSegment();
//			curveSegment.setStart(start);
//			curveSegment.setEnd(end);

			Curve curve = new Curve();
//			ListOf<CurveSegment> listOfCurveSegments = new ListOf<CurveSegment>();
//			listOfCurveSegments.add(curveSegment);
//			curve.setListOfCurveSegments(listOfCurveSegments);

			srg.setCurve(curve);
		}
		
		if (!output.contains(rg)) {
			output.add(rg);
		}
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutAlgorithm#createGlyphBoundingBox(org.sbml.jsbml.ext.layout.NamedSBaseGlyph, org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph)
	 */
	@Override
	public BoundingBox createGlyphBoundingBox(GraphicalObject glyph,
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
		Rectangle graphBoundingBox = graph2D.getBoundingBox();
		Dimensions dimensions = new Dimensions(graphBoundingBox.getWidth(),
				graphBoundingBox.getHeight(),
				DEFAULT_DEPTH,
				level, version);
		logger.info(dimensions.toString());
		return dimensions;
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
		// TODO Auto-generated method stub
		return null;
	}

}
