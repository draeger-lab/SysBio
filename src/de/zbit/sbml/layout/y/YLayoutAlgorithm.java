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

import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.ext.layout.BoundingBox;
import org.sbml.jsbml.ext.layout.CompartmentGlyph;
import org.sbml.jsbml.ext.layout.Curve;
import org.sbml.jsbml.ext.layout.Dimensions;
import org.sbml.jsbml.ext.layout.GraphicalObject;
import org.sbml.jsbml.ext.layout.Point;
import org.sbml.jsbml.ext.layout.Position;
import org.sbml.jsbml.ext.layout.ReactionGlyph;
import org.sbml.jsbml.ext.layout.SpeciesGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph;
import org.sbml.jsbml.ext.layout.TextGlyph;

import y.base.DataMap;
import y.base.Edge;
import y.base.Node;
import y.view.Graph2D;
import y.view.NodeRealizer;
import y.view.ShapeNodeRealizer;
import y.view.hierarchy.GroupNodeRealizer;
import y.view.hierarchy.HierarchyManager;
import de.zbit.graph.GraphTools;
import de.zbit.graph.io.Graph2Dwriter;
import de.zbit.graph.io.def.GenericDataMap;
import de.zbit.graph.io.def.GraphMLmaps;
import de.zbit.graph.sbgn.ReactionNodeRealizer;
import de.zbit.sbml.layout.LayoutDirector;
import de.zbit.sbml.layout.SimpleLayoutAlgorithm;
import de.zbit.util.objectwrapper.ValuePairUncomparable;

/**
 * @author Jakob Matthes
 * @version $Rev$
 */
public class YLayoutAlgorithm extends SimpleLayoutAlgorithm {
	
	/**
	 * Default value for z-coordinates and depth as YFiles works in two
	 * dimensional space but SBML allows three dimensional specifications.
	 */
	private static final double DEFAULT_Z_COORD = 0.0d;
	private static final double DEFAULT_DEPTH = 0.0d;

	/**
	 * Logger instance for informational output.
	 */
	private static Logger logger = Logger.getLogger(YLayoutAlgorithm.class.toString());

	/**
	 * 
	 */
	private Set<ValuePairUncomparable<SpeciesReferenceGlyph, ReactionGlyph>> setOfUnlayoutedEdges;

	/**
	 * 
	 */
	private Set<ValuePairUncomparable<SpeciesReferenceGlyph, ReactionGlyph>> setOfLayoutedEdges;
	
	/**
	 * 
	 */
	private Map<String, Node> compartmentGlyphMap;
	
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
	 * Set to hold all reaction glyphs which need positioning.
	 */
	private Set<ReactionGlyph> reactionNodes;
	
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
	 * LayoutAlgorithm constructor.
	 */
	public YLayoutAlgorithm() {
		super();
		
		this.setOfUnlayoutedEdges =
			new HashSet<ValuePairUncomparable<SpeciesReferenceGlyph,ReactionGlyph>>();
		this.setOfLayoutedEdges =
			new HashSet<ValuePairUncomparable<SpeciesReferenceGlyph,ReactionGlyph>>();
		
		this.nodeIncompleteGlyphMap = new HashMap<Node, GraphicalObject>();
		this.glyphNodeMap = new HashMap<String, Node>();
		this.reactionNodes = new HashSet<ReactionGlyph>();
		this.compartmentGlyphMap = new HashMap<String, Node>();
		this.output = new HashSet<GraphicalObject>();
		
		graph2D = new Graph2D();
		GenericDataMap<DataMap, String> mapDescriptionMap =
			new GenericDataMap<DataMap, String>(Graph2Dwriter.mapDescription);
		graph2D.addDataProvider(Graph2Dwriter.mapDescription, mapDescriptionMap);
		
		HierarchyManager hm = graph2D.getHierarchyManager();
		if (hm == null) {
			hm = new HierarchyManager(graph2D);
			graph2D.setHierarchyManager(hm);
		}
		
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
		logger.fine("add layouted glyph id=" + glyph.getId());
		
		// text glyphs (non-indepentend) without bounding box are considered
		// layouted (positioning in center of graphical object)
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
		NodeRealizer nodeRealizer = (glyph instanceof CompartmentGlyph) ? new GroupNodeRealizer() : new ShapeNodeRealizer();
		nodeRealizer.setSize(width, height);
		nodeRealizer.setLocation(x, y);
		Node node = graph2D.createNode(nodeRealizer);
		
		logger.fine(String.format("%d,%d %dx%d", x, y, width, height));
			
		handleHierarchy(glyph, node);
		
		// GraphTools helper
		graphTools.setInfo(node, GraphMLmaps.NODE_POSITION, x + "|" + y);
		graphTools.setInfo(node, GraphMLmaps.NODE_SIZE, width + "|" + height);
		
		setOfLayoutedGlyphs.add(glyph);
		glyphNodeMap.put(glyph.getId(), node);
	}

	/**
	 * @param glyph
	 * @param node
	 */
	private void handleHierarchy(GraphicalObject glyph, Node node) {
		HierarchyManager hm = graph2D.getHierarchyManager();
		if (glyph instanceof CompartmentGlyph) {
			hm.convertToGroupNode(node);
			CompartmentGlyph compartmentGlyph = (CompartmentGlyph) glyph;
			logger.info(compartmentGlyph.getCompartment());
			compartmentGlyphMap.put(compartmentGlyph.getCompartment(), node);
		}
		else if (glyph instanceof ReactionGlyph) {
			Reaction r = (Reaction) ((ReactionGlyph) glyph).getReactionInstance();
			if (r.isSetCompartment()) {
				Node comp = compartmentGlyphMap.get(r.getCompartment());
				if (comp != null) {
					hm.setParentNode(node, comp);
				}
			}
		}
		else if (glyph instanceof SpeciesGlyph) {
			Species s = (Species) ((SpeciesGlyph) glyph).getSpeciesInstance();
			if (s.isSetCompartment()) {
				logger.info(s.getId() + " " + s.getCompartment());
				Node comp = compartmentGlyphMap.get(s.getCompartment());
				if(comp != null) {
					hm.setParentNode(node, comp);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutAlgorithm#addUnlayoutedGlyph(org.sbml.jsbml.ext.layout.GraphicalObject)
	 */
	@Override
	public void addUnlayoutedGlyph(GraphicalObject glyph) {
		logger.fine("add unlayouted glyph id=" + glyph.getId());
		
		// text glyphs (non-indepentend) without bounding box are considered
		// layouted (positioning in center of graphical object)
		if ((glyph instanceof TextGlyph) &&
				!LayoutDirector.textGlyphIsIndependent((TextGlyph) glyph) &&
				!glyph.isSetBoundingBox()) {
			return;
		}
		
		NodeRealizer nodeRealizer;
		Node node;
		if (!(glyph instanceof ReactionGlyph)) {
			nodeRealizer = (glyph instanceof CompartmentGlyph) ? new GroupNodeRealizer() : new ShapeNodeRealizer();
			node = graph2D.createNode(nodeRealizer);

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
			

			setOfUnlayoutedGlyphs.add(glyph);
			nodeIncompleteGlyphMap.put(node, glyph);
		}
		
		// handle reaction glyphs differently
		else {
			// glyph is ReactionGlyph
			nodeRealizer = new ReactionNodeRealizer();
			node = graph2D.createNode(nodeRealizer);
			
			if (!LayoutDirector.glyphHasDimensions(glyph)) {
				Dimensions dimensions = createReactionGlyphDimension((ReactionGlyph) glyph);
				int width, height;
				width = (int) dimensions.getWidth();
				height = (int) dimensions.getHeight();
				nodeRealizer.setSize(width, height);
				graphTools.setInfo(node, GraphMLmaps.NODE_SIZE, width + "|" + height);
				if (glyph.isSetBoundingBox()) {
					glyph.getBoundingBox().setDimensions(dimensions);
				}
				else {
					glyph.createBoundingBox(dimensions);
				}
			}
			reactionNodes.add((ReactionGlyph) glyph);
		}

		glyphNodeMap.put(glyph.getId(), node);
		
		handleHierarchy(glyph, node);
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
		
		// (1) create all edges 
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
		
		// (2) autolayout of node subset using GraphTools
		graphTools.layoutNodeSubset(setOfUnlayoutedNodes);
		
		// (3) copy realizer information to glyph bounding box
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
		
		// position process nodes
		for (ReactionGlyph reactionGlyph : reactionNodes) {
			assert glyphNodeMap.get(reactionGlyph.getId()) != null;
			ReactionNodeRealizer reactionNodeRealizer = (ReactionNodeRealizer) graph2D.getRealizer(glyphNodeMap.get(reactionGlyph.getId()));
			
//			double rotationAngle = calculateReactionGlyphRotationAngle(reactionGlyph);
//			reactionGlyph.putUserObject("ROTATION", rotationAngle);
			Position position = createReactionGlyphPositionNew(reactionGlyph);
			reactionNodeRealizer.setLocation(position.getX(), position.getY());
			
			BoundingBox rgBoundingBox = reactionGlyph.isSetBoundingBox() ?
					reactionGlyph.getBoundingBox() : reactionGlyph.createBoundingBox();
			rgBoundingBox.setPosition(position);
			
			// TODO calculate docking points for each SpeciesReferenceGlyph of
			// this reaction
//			Reaction reaction = (Reaction) reactionGlyph.getReactionInstance();
//			for (SpeciesReferenceGlyph sRG : reactionGlyph.getListOfSpeciesReferenceGlyphs()) {
//				logger.info("now calculate docking positions for " + sRG.getSpeciesGlyph());
//				Point speciesDocking = calculateReactionGlyphDockingPointForSpecies(reactionGlyph, rotationAngle, sRG);
//				logger.info(speciesDocking.toString());
//			}
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
//
//			logger.info(String.format("add curve for rgId=%s  srgId=%s  start=%s  end=%s",
//					rg.getId(), srg.getId(), start.toString(), end.toString()));
//
//			CurveSegment curveSegment = new CurveSegment();
//			curveSegment.setStart(start);
//			curveSegment.setEnd(end);

			Curve curve = new Curve();
//			ListOf<CurveSegment> listOfCurveSegments = new ListOf<CurveSegment>();
//			listOfCurveSegments.add(curveSegment);
//			curve.setListOfCurveSegments(listOfCurveSegments);

			srg.setCurve(curve);
		}
		
		output.add(rg);
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutAlgorithm#createGlyphBoundingBox(org.sbml.jsbml.ext.layout.NamedSBaseGlyph, org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph)
	 */
	@Override
	public BoundingBox createGlyphBoundingBox(GraphicalObject glyph,
			SpeciesReferenceGlyph specRefGlyph) {
		 if (glyph instanceof ReactionGlyph) {
			 BoundingBox boundingBox = createBoundingBoxWithLevelAndVersion();
			ReactionGlyph reactionGlyph = (ReactionGlyph) glyph;
			if (specRefGlyph != null) {
				boundingBox.setDimensions(createSpeciesReferenceGlyphDimension(reactionGlyph, specRefGlyph));
				boundingBox.setPosition(createSpeciesReferenceGlyphPosition(reactionGlyph, specRefGlyph));
			} else {
				boundingBox.setDimensions(createReactionGlyphDimension(reactionGlyph));
				boundingBox.setPosition(createReactionGlyphPosition(reactionGlyph));
			}
			return boundingBox;
		 }
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
		logger.fine(dimensions.toString());
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
		return new Dimensions(20, 10, 0, level, version);
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
	 * @see de.zbit.sbml.layout.LayoutAlgorithm#createCompartmentGlyphPosition(org.sbml.jsbml.ext.layout.CompartmentGlyph)
	 */
	@Override
	public Position createCompartmentGlyphPosition(
			CompartmentGlyph previousCompartmentGlyph) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutAlgorithm#createSpeciesGlyphDimension()
	 */
	@Override
	public Dimensions createSpeciesGlyphDimension() {
		return new Dimensions(100, 100, 0, level, version);
	}

}
