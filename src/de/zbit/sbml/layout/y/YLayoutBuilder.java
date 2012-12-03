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

import java.awt.Color;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.sbml.jsbml.NamedSBase;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.ext.layout.BoundingBox;
import org.sbml.jsbml.ext.layout.CompartmentGlyph;
import org.sbml.jsbml.ext.layout.CubicBezier;
import org.sbml.jsbml.ext.layout.Curve;
import org.sbml.jsbml.ext.layout.CurveSegment;
import org.sbml.jsbml.ext.layout.Dimensions;
import org.sbml.jsbml.ext.layout.Layout;
import org.sbml.jsbml.ext.layout.NamedSBaseGlyph;
import org.sbml.jsbml.ext.layout.Point;
import org.sbml.jsbml.ext.layout.ReactionGlyph;
import org.sbml.jsbml.ext.layout.SpeciesGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph;
import org.sbml.jsbml.ext.layout.TextGlyph;
import org.sbml.jsbml.util.StringTools;

import y.base.Edge;
import y.base.Node;
import y.geom.OrientedRectangle;
import y.layout.DiscreteEdgeLabelModel;
import y.layout.FreeNodeLabelModel;
import y.view.BezierEdgeRealizer;
import y.view.EdgeLabel;
import y.view.EdgeRealizer;
import y.view.GenericEdgeRealizer;
import y.view.Graph2D;
import y.view.NodeLabel;
import y.view.NodeRealizer;
import y.view.PolyLineEdgeRealizer;
import y.view.ShapeNodeRealizer;
import y.view.hierarchy.HierarchyManager;
import de.zbit.sbml.layout.AbstractLayoutBuilder;
import de.zbit.sbml.layout.Catalysis;
import de.zbit.sbml.layout.Compartment;
import de.zbit.sbml.layout.Consumption;
import de.zbit.sbml.layout.Inhibition;
import de.zbit.sbml.layout.LayoutDirector;
import de.zbit.sbml.layout.Macromolecule;
import de.zbit.sbml.layout.Modulation;
import de.zbit.sbml.layout.NecessaryStimulation;
import de.zbit.sbml.layout.NucleicAcidFeature;
import de.zbit.sbml.layout.PerturbingAgent;
import de.zbit.sbml.layout.ProcessNode;
import de.zbit.sbml.layout.Production;
import de.zbit.sbml.layout.SBGNArc;
import de.zbit.sbml.layout.SBGNNode;
import de.zbit.sbml.layout.SimpleChemical;
import de.zbit.sbml.layout.SimpleLayoutAlgorithm;
import de.zbit.sbml.layout.SourceSink;
import de.zbit.sbml.layout.Stimulation;
import de.zbit.sbml.layout.UnspecifiedNode;
import de.zbit.sbml.layout.SimpleLayoutAlgorithm.RelativePosition;
import de.zbit.util.progressbar.AbstractProgressBar;
import de.zbit.util.progressbar.ProgressListener;

/**
 * @author Jakob Matthes
 * @version $Rev$
 */
public class YLayoutBuilder extends AbstractLayoutBuilder<ILayoutGraph,NodeRealizer,EdgeRealizer> {

	private static Logger logger = Logger.getLogger(YLayoutBuilder.class.toString());

	/**
	 * A YFiles Graph2D is the product of this builder.
	 */
	private Graph2D graph;
	
	/**
	 * List keeping track of ProgressListeners.
	 */
	private List<ProgressListener> progressListeners = new LinkedList<ProgressListener>();

	/**
	 * Indicates whether graph generation has finished or not.
	 */
	private boolean terminated = false;

	/**
	 * Maps SBML identifiers to yFiles nodes.
	 */
	private Map<String, Node> id2node = new HashMap<String, Node>();
	
	/**
	 * Map species id to all yfiles nodes.
	 */
	private Map<String, Set<Node>> speciesId2Node = new HashMap<String, Set<Node>>();
	
	/**
	 * Map compartment id to all yfiles nodes.
	 */
	private Map<String, Set<Node>> compartmentId2Node = new HashMap<String, Set<Node>>();
	
	/**
	 * Map reaction glyph id to set of adjoining edges.
	 */
	private Map<String, Set<Edge>> reactionGlyphId2edges = new HashMap<String, Set<Edge>>();
	
	/**
	 * Set to hold all text glyphs which label a specific node.
	 */
	Set<TextGlyph> labelTextGlyphs;

	/**
	 * The SBML layout object.
	 */
	private Layout layout;

	/**
	 * Method to initialize the graph2d structure.
	 * 
	 * @param layout
	 * @see de.zbit.sbml.layout.LayoutBuilder#builderStart(org.sbml.jsbml.ext.layout.Layout)
	 */
	@Override
	public void builderStart(Layout layout) {
		this.layout = layout;
		graph = new Graph2D();
	    HierarchyManager hm = graph.getHierarchyManager();
	    if (hm == null) {
	      hm = new HierarchyManager(graph);
	      graph.setHierarchyManager(hm);
	    }
		labelTextGlyphs = new HashSet<TextGlyph>();
		// TODO for all p in progressListeners: progress.setNumberOfTotalCalls(xyz);
	}
	
	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutBuilder#addProgressListener(de.zbit.util.progressbar.AbstractProgressBar)
	 */
	@Override
	public void addProgressListener(AbstractProgressBar progress) {
		progressListeners.add(progress);
	}

	/**
	 * Method to add the compartment glyph representation to the graph.
	 * @see de.zbit.sbml.layout.LayoutBuilder#buildCompartment(org.sbml.jsbml.ext.layout.CompartmentGlyph)
	 */
	@Override
	public void buildCompartment(CompartmentGlyph compartmentGlyph) {
		// TODO compartmentGlyph.getSBOTerm() returns -1
		// see below for EPNs, are species sbo copied to species glyph sbo by layoutdirector?
		SBGNNode<NodeRealizer> node = getSBGNNode(SBO.getCompartment());
		
		BoundingBox boundingBox = compartmentGlyph.getBoundingBox();
		Point point = boundingBox.getPosition();
		Dimensions dimension = boundingBox.getDimensions();
		double x = point.getX(), y = point.getY(), z = point.getZ();
		double width = dimension.getWidth(), height = dimension.getHeight(), depth = dimension.getDepth();

		ShapeNodeRealizer nodeRealizer = (ShapeNodeRealizer) node.draw(x, y, z, width, height, depth);
		Node ynode = graph.createNode();
		graph.setRealizer(ynode, nodeRealizer);
		id2node.put(compartmentGlyph.getId(), ynode);
		putInMapSet(compartmentId2Node, compartmentGlyph.getCompartment(), ynode);
		
		logger.fine(String.format("building compartment glyph id=%s\n\tbounding box=%s",
				compartmentGlyph.getId(), nodeRealizer.getBoundingBox()));
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutBuilder#buildEntityPoolNode(org.sbml.jsbml.ext.layout.SpeciesGlyph, boolean)
	 */
	@Override
	public void buildEntityPoolNode(SpeciesGlyph speciesGlyph,
			boolean cloneMarker) {
		SBGNNode<NodeRealizer> node = getSBGNNode(speciesGlyph.getSBOTerm());
		
		if (cloneMarker) {
			node.setCloneMarker();
		}
		
		BoundingBox boundingBox = speciesGlyph.getBoundingBox();
		Point point = boundingBox.getPosition();
		Dimensions dimension = boundingBox.getDimensions();
		double x, y, z, width, height, depth;
		x = point.getX();
		y = point.getY();
		z = point.getZ();
		width = dimension.getWidth();
		height = dimension.getHeight();
		depth = dimension.getDepth();

		ShapeNodeRealizer nodeRealizer = (ShapeNodeRealizer) node.draw(x, y, z, width, height, depth);
		
//		nodeRealizer.setDropShadowColor(new Color(0, 0, 0, 64));
//	    nodeRealizer.setDropShadowOffsetX((byte) 3);
//	    nodeRealizer.setDropShadowOffsetY((byte) 3);
//	    Color fillColor = nodeRealizer.getFillColor();
//	    nodeRealizer.setFillColor2(fillColor.brighter());
//	    nodeRealizer.setFillColor(fillColor);
		
		logger.fine(String.format("building EPN element id=%s sbo=%d (%s)\n\tbounding box= %s %s",
				speciesGlyph.getId(), speciesGlyph.getSBOTerm(), SBO.convertSBO2Alias(speciesGlyph.getSBOTerm()),
				speciesGlyph.getBoundingBox().getPosition(), nodeRealizer.getBoundingBox()));
		
		Node ynode = graph.createNode();
		Node compartmentYNode = id2node.get(((Species) speciesGlyph.getSpeciesInstance()).getCompartment());
		if (compartmentYNode != null) {
			graph.getHierarchyManager().setParentNode(ynode, compartmentYNode);
		}
		graph.setRealizer(ynode, nodeRealizer);
		id2node.put(speciesGlyph.getId(), ynode);
		putInMapSet(speciesId2Node, speciesGlyph.getSpecies(), ynode);
	}

	/**
	 * @param <T>
	 * @param speciesId2Node2
	 * @param species
	 * @param ynode
	 */
	private <T> void putInMapSet(Map<String, Set<T>> id2Nodes,
			String id, T object) {
		if (id2Nodes.get(id) == null) {
			HashSet<T> hashSet = new HashSet<T>();
			hashSet.add(object);
			id2Nodes.put(id, hashSet);
		}
		else {
			HashSet<T> hashSet = (HashSet<T>) id2Nodes.get(id);
			hashSet.add(object);
		}
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutBuilder#buildConnectingArc(org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph)
	 */
	@Override
	public void buildConnectingArc(SpeciesReferenceGlyph speciesReferenceGlyph, double curveWidth) {
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutBuilder#buildConnectingArc(org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph, org.sbml.jsbml.ext.layout.ReactionGlyph)
	 */
	@Override
	public void buildConnectingArc(SpeciesReferenceGlyph srg, ReactionGlyph rg, double curveWidth) {
		logger.fine(String.format("building arc srgId=%s to rgId=%s", srg.getId(), rg.getId()));
		
		Node processNode = id2node.get(rg.getId());
		Node speciesGlyphNode = id2node.get(srg.getSpeciesGlyph());
		assert processNode != null;
		assert speciesGlyphNode != null;
		
		SBGNArc<EdgeRealizer> arc;
		if (srg.isSetSBOTerm()) {
			arc = getSBGNArc(srg.getSBOTerm());
			logger.fine("SRG sbo term " + srg.getSBOTerm());
		} else {
			arc = getSBGNArc(srg.getSpeciesReferenceRole());
			logger.fine("SRG role " + srg.getSpeciesReferenceRole());
		}
		
		EdgeRealizer edgeRealizer = arc.draw(srg.getCurve());
		// TODO EdgeRealizer edgeRealizer = arc.draw(srg.getCurve(), width);
		// also set line width on process node
		edgeRealizer = edgeRealizer.createCopy();
		
		// to dock correctly use
		// edgeRealizer.setSourcePoint(yp);
				
		if (srg.isSetSpeciesReference()) {
			SpeciesReference speciesReference = (SpeciesReference) srg.getSpeciesReferenceInstance();
			if (speciesReference.isSetStoichiometry() && speciesReference.getStoichiometry() != 1) {
				// stoichiometry number
				EdgeLabel edgeLabel = new EdgeLabel(StringTools.toString(speciesReference.getStoichiometry()));

				// TODO maybe create extra EdgeLabel subclass for stoichiometry labels
				// placement
				DiscreteEdgeLabelModel elModel = new DiscreteEdgeLabelModel();  
				elModel.setDistance(0.0);
				edgeLabel.setLabelModel(elModel);
				edgeLabel.setModelParameter(DiscreteEdgeLabelModel.createPositionParameter(DiscreteEdgeLabelModel.TTAIL));
				
				// border
				edgeLabel.setLineColor(Color.BLACK);
				
				edgeRealizer.addLabel(edgeLabel);
			}
		}

		Edge edge = graph.createEdge(processNode, speciesGlyphNode, edgeRealizer);
		putInMapSet(reactionGlyphId2edges, rg.getId(), edge);
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutBuilder#buildCubicBezier(org.sbml.jsbml.ext.layout.CubicBezier)
	 */
	@Override
	public void buildCubicBezier(CubicBezier cubicBezier) {
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutBuilder#buildProcessNode(org.sbml.jsbml.ext.layout.ReactionGlyph, double)
	 */
	@Override
	public void buildProcessNode(ReactionGlyph reactionGlyph, double rotationAngle, double curveWidth) {
		BoundingBox boundingBox = reactionGlyph.getBoundingBox();
		Point point = boundingBox.getPosition();
		Dimensions dimension = boundingBox.getDimensions();
		double x, y, z, width, height, depth;
		x = point.getX();
		y = point.getY();
		z = point.getZ();
		width = dimension.getWidth();
		height = dimension.getHeight();
		depth = dimension.getDepth();
		
		ProcessNode<NodeRealizer> processNode = createProcessNode();
		Point rotationCenter = new Point(x + width / 2d, y + height / 2d, z + depth / 2d);
		NodeRealizer reactionNodeRealizer = processNode.draw(
			x, y, z, width, height, depth, rotationAngle, rotationCenter);
		logger.info(MessageFormat.format("Process node position: {0} rotationAngle: {1} rotationCenter: {2}",
				point, rotationAngle, rotationCenter));
		
		Node processYNode = graph.createNode(reactionNodeRealizer);
		id2node.put(reactionGlyph.getId(), processYNode);
		logger.fine(MessageFormat.format("building PN id={0} bounding box={1} {2}",
				reactionGlyph.getId(), reactionGlyph.getBoundingBox().getPosition(), reactionNodeRealizer.getBoundingBox()));
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutBuilder#buildTextGlyph(org.sbml.jsbml.ext.layout.TextGlyph)
	 */
	@Override
	public void buildTextGlyph(TextGlyph textGlyph) {
		String text = "";
		
		if (textGlyph.isSetText() &&
				!textGlyph.isSetGraphicalObject() &&
				!textGlyph.isSetOriginOfText()) {
			// independent text
			BoundingBox boundingBox = textGlyph.getBoundingBox();
			Point point = boundingBox.getPosition();
			Dimensions dimensions = boundingBox.getDimensions();
			double x, y, width, height;
			x = point.getX();
			y = point.getY();
			width = dimensions.getWidth();
			height = dimensions.getHeight();
			
			text = textGlyph.getText();
			logger.fine(String.format("building text glyph element id=%s\n\tindependent text text='%s'",
					textGlyph.getId(), text));

			Node ynode = graph.createNode();
			// TODO maybe implement a IndependentTextRealizer for better presentation
			NodeRealizer nr = graph.getRealizer(ynode);
			nr.setLabelText(text);
			nr.setFrame(x, y, width, height);
		}
		else if (textGlyph.isSetGraphicalObject() &&
				(textGlyph.isSetOriginOfText() || textGlyph.isSetText())) {
			// label for a graphical object
			// label text glyphs are collected and built as a last step of the builder
			labelTextGlyphs.add(textGlyph);
		}
		else {
			logger.warning(String.format("illegal text glyph id=%s", textGlyph.getId()));
		}
	}

	/**
	 * Realizes a text glyph as a label of an already existing node.
	 * @param textGlyph
	 */
	private void buildTextGlyphAsLabel(TextGlyph textGlyph) {
		Node origin = id2node.get(textGlyph.getGraphicalObject());
		if (origin == null) {
			return;
		}
		NodeRealizer originRealizer = graph.getRealizer(origin);

		// TODO special positions for CompartmentGlyphs
		
		String text = null;
		NamedSBase namedSBase = null;
		if (textGlyph.isSetText()) {
			text = textGlyph.getText();
			logger.fine(String.format("building text glyph element id=%s\n\torigin text overridden text='%s'",
					textGlyph.getId(), text));
		}
		else if (textGlyph.isSetOriginOfText()) {
			namedSBase = textGlyph.getOriginOfTextInstance();
			text = namedSBase.getName();
			logger.fine(String.format("building text glyph element id=%s\n\ttext from origin id=%s text='%s'",
					textGlyph.getId(), namedSBase.getId(), text));
		}
		
		if (textGlyph.isSetBoundingBox() &&
				textGlyph.getBoundingBox().isSetPosition() &&
				textGlyph.getBoundingBox().isSetDimensions()) {
			logger.fine("using nodelabel for textglyph " + textGlyph.getId());
			NodeLabel nodeLabel;
			nodeLabel = new NodeLabel(text);
			nodeLabel.setLabelModel(new FreeNodeLabelModel());
			originRealizer.setLabel(nodeLabel);
			
			// text glyph position
			Point position = textGlyph.getBoundingBox().getPosition();
			double glyphX = position.getX();
			double glyphY = position.getY();
			
			logger.fine(String.format("text position is %.2f,%.2f", glyphX, glyphY));
			
			Dimensions dimensions = textGlyph.getBoundingBox().getDimensions();
			double width = dimensions.getWidth();
			double height = dimensions.getHeight();
			
			OrientedRectangle orientedRectangle =
				new OrientedRectangle(glyphX, glyphY, width, height);
			logger.fine("oriented rectangle is " + orientedRectangle.toString());
			Object param = nodeLabel.getBestModelParameterForBounds(orientedRectangle);
			if (param != null) {
				logger.fine("using extra positioning for textglyph " + textGlyph.getId());
				nodeLabel.setModelParameter(param);
			}
		}
		else if (text != null) {
			originRealizer.setLabelText(text);
		}
	}

	/**
		 * @param curve
		 * @return
		 */
		public static EdgeRealizer createEdgeRealizerFromCurve(Curve curve) {
			EdgeRealizer edgeRealizer = new GenericEdgeRealizer();
			
			// Note: if multiple curve segments (beziers) are specified, the resulting
			// representation will not be standard compliant.

			if (curve != null && curve.isSetListOfCurveSegments()) {
				List<CurveSegment> listOfCurveSegments = curve.getListOfCurveSegments();

				// if all curve segments are beziers, use BezierEdgeRealizer, else use PolyLineEdgeRealizer
				boolean drawBezier = true;
				for (CurveSegment curveSegment : listOfCurveSegments) {
					// TODO there should be a constant for this string
					drawBezier = drawBezier && curveSegment.getType().equals("CubicBezier");
					if (!drawBezier) {
						break;
					}
				}
				
				edgeRealizer = drawBezier ? new BezierEdgeRealizer() : new PolyLineEdgeRealizer();

				for (int i = listOfCurveSegments.size()-1; i >= 0; i--) {
					CurveSegment curveSegment = listOfCurveSegments.get(i);
					
					Point end = curveSegment.getEnd();
					edgeRealizer.addPoint(end.getX(), end.getY());
					if (drawBezier) {
						Point basePoint2 = curveSegment.getBasePoint2();
						edgeRealizer.addPoint(basePoint2.getX(), basePoint2.getY());
						Point basePoint1 = curveSegment.getBasePoint1();
						edgeRealizer.addPoint(basePoint1.getX(), basePoint1.getY());
					}
					Point start = curveSegment.getStart();
					edgeRealizer.addPoint(start.getX(), start.getY());
				}
			}
	
			return edgeRealizer;
		}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutBuilder#builderEnd()
	 */
	@Override
	public void builderEnd() {
		// build label text glyphs
		for (TextGlyph textGlyph : labelTextGlyphs) {
			buildTextGlyphAsLabel(textGlyph);
		}
		
		terminated = true;
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutBuilder#getProduct()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ILayoutGraph getProduct() {
		Map<String, Set<List<Edge>>> reactionId2Edge = new HashMap<String, Set<List<Edge>>>();
		for (Reaction reaction : layout.getModel().getListOfReactions()) {
			String reactionId = reaction.getId();
			Set<List<Edge>> edgeListSet= new HashSet<List<Edge>>();
			@SuppressWarnings("unchecked")
			List<NamedSBaseGlyph> reactionGlyphs = (List<NamedSBaseGlyph>) reaction.getUserObject(LayoutDirector.LAYOUT_LINK);
			if (reactionGlyphs != null) {
				for (NamedSBaseGlyph reactionGlyph : reactionGlyphs) {
					edgeListSet.add(new LinkedList<Edge>(reactionGlyphId2edges.get(reactionGlyph.getId())));
				}
			}
			reactionId2Edge.put(reactionId, edgeListSet);
		}
		ILayoutGraph layoutGraph = new LayoutGraph(speciesId2Node,
				compartmentId2Node,
				reactionId2Edge, 
				graph);
		
		return layoutGraph;
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutBuilder#isProductReady()
	 */
	@Override
	public boolean isProductReady() {
		return terminated;
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutFactory#createProcessNode()
	 */
	@Override
	public ProcessNode<NodeRealizer> createProcessNode() {
		return new YProcessNode();
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutFactory#createMacromolecule()
	 */
	@Override
	public Macromolecule<NodeRealizer> createMacromolecule() {
		return new YMacromolecule();
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutFactory#createSourceSink()
	 */
	@Override
	public SourceSink<NodeRealizer> createSourceSink() {
		return new YSourceSink();
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutFactory#createUnspecifiedNode()
	 */
	@Override
	public UnspecifiedNode<NodeRealizer> createUnspecifiedNode() {
		return new YUnspecifiedNode();
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutFactory#createSimpleChemical()
	 */
	@Override
	public SimpleChemical<NodeRealizer> createSimpleChemical() {
		return new YSimpleChemical();
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutFactory#createNucleicAcidFeature()
	 */
	@Override
	public NucleicAcidFeature<NodeRealizer> createNucleicAcidFeature() {
		return new YNucleicAcidFeature();
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutFactory#createPerturbingAgent()
	 */
	@Override
	public PerturbingAgent<NodeRealizer> createPerturbingAgent() {
		return new YPerturbingAgent();
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutFactory#createCompartment()
	 */
	@Override
	public Compartment<NodeRealizer> createCompartment() {
		return new YCompartment();
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutFactory#createProduction()
	 */
	@Override
	public Production<EdgeRealizer> createProduction() {
		return new YProduction();
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutFactory#createConsumption()
	 */
	@Override
	public Consumption<EdgeRealizer> createConsumption() {
		return new YConsumption();
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutFactory#createCatalysis()
	 */
	@Override
	public Catalysis<EdgeRealizer> createCatalysis() {
		return new YCatalysis();
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutFactory#createInhibition()
	 */
	@Override
	public Inhibition<EdgeRealizer> createInhibition() {
		return new YInhibition();
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutFactory#createModulation()
	 */
	@Override
	public Modulation<EdgeRealizer> createModulation() {
		return new YModulation();
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutFactory#createStimulation()
	 */
	@Override
	public Stimulation<EdgeRealizer> createStimulation() {
		return new YStimulation();
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutFactory#createNecessaryStimulation()
	 */
	@Override
	public NecessaryStimulation<EdgeRealizer> createNecessaryStimulation() {
		return new YNecessaryStimulation();
	}

}
