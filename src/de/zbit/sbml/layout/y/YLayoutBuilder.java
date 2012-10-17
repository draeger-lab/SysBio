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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.sbml.jsbml.NamedSBase;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.ext.layout.BoundingBox;
import org.sbml.jsbml.ext.layout.CompartmentGlyph;
import org.sbml.jsbml.ext.layout.CubicBezier;
import org.sbml.jsbml.ext.layout.Dimensions;
import org.sbml.jsbml.ext.layout.Layout;
import org.sbml.jsbml.ext.layout.LineSegment;
import org.sbml.jsbml.ext.layout.Point;
import org.sbml.jsbml.ext.layout.ReactionGlyph;
import org.sbml.jsbml.ext.layout.SpeciesGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph;
import org.sbml.jsbml.ext.layout.TextGlyph;

import y.base.Node;
import y.view.Graph2D;
import y.view.NodeRealizer;
import de.zbit.sbml.layout.AbstractLayoutBuilder;
import de.zbit.sbml.layout.Catalysis;
import de.zbit.sbml.layout.Compartment;
import de.zbit.sbml.layout.Consumption;
import de.zbit.sbml.layout.Inhibition;
import de.zbit.sbml.layout.Macromolecule;
import de.zbit.sbml.layout.Production;
import de.zbit.sbml.layout.SBGNNode;
import de.zbit.sbml.layout.SimpleChemical;
import de.zbit.sbml.layout.SourceSink;
import de.zbit.sbml.layout.UnspecifiedNode;
import de.zbit.util.progressbar.AbstractProgressBar;
import de.zbit.util.progressbar.ProgressListener;

/**
 * @author Jakob Matthes
 * @version $Rev$
 */
public class YLayoutBuilder extends AbstractLayoutBuilder<Graph2D,NodeRealizer> {

	private static Logger logger = Logger.getLogger(YLayoutBuilder.class.toString());

	
	/**
	 * A YFiles Graph is the product of this builder.
	 */
	private Graph2D graph;
	
	/**
	 * List keeping track of ProgressListeners.
	 */
	private List<ProgressListener> progressListeners = new LinkedList<ProgressListener>();

	/**
	 * Indicates whether graph generation has terminated or not.
	 */
	private boolean terminated = false;

	/**
	 * Maps SBML identifiers to yFiles nodes.
	 */
	private Map<String, Node> id2node = new HashMap<String, Node>();

	/**
	 * Method to initialize the graph2d structure.
	 * 
	 * @param layout
	 */
	@Override
	public void builderStart(Layout layout) {
		graph = new Graph2D();
		// TODO for all p in progressListeners: progress.setNumberOfTotalCalls(xyz);
	}
	
	// TODO method signature: argument type should be ProgressListener?
	@Override
	public void addProgressListener(AbstractProgressBar progress) {
		progressListeners.add(progress);
	}

	/**
	 * Method to add the compartment glyph representation to the graph.
	 */
	@Override
	public void buildCompartment(CompartmentGlyph compartmentGlyph) {
		// TODO compartmentGlpyh.getSBOTerm() returns -1
		SBGNNode<NodeRealizer> node = getSBGNNode(SBO.getCompartment());
		
		BoundingBox boundingBox = compartmentGlyph.getBoundingBox();
		Point point = boundingBox.getPosition();
		Dimensions dimension = boundingBox.getDimensions();
		double x, y, z, width, height, depth;
		x = point.getX();
		y = point.getY();
		z = point.getZ();
		width = dimension.getWidth();
		height = dimension.getHeight();
		depth = dimension.getDepth();

		NodeRealizer nodeRealizer = node.draw(x, y, z, width, height, depth);
		Node ynode = graph.createNode();
		graph.setRealizer(ynode, nodeRealizer);
		
		logger.info(String.format("building compartment glyph id=%s\n\tbounding box=%s",
				compartmentGlyph.getId(), nodeRealizer.getBoundingBox()));
		
		
		//new GraphTools (null).setInfo(node, GraphMLmaps.NODE_POSITION, "5|9");
	}

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

		NodeRealizer nodeRealizer = node.draw(x, y, z, width, height, depth);
		
		logger.info(String.format("building EPN element id=%s sbo=%d (%s)\n\tbounding box=%s",
				speciesGlyph.getId(), speciesGlyph.getSBOTerm(), SBO.convertSBO2Alias(speciesGlyph.getSBOTerm()),
				nodeRealizer.getBoundingBox()));
		
		Node ynode = graph.createNode();
		graph.setRealizer(ynode, nodeRealizer);
		logger.info(String.format("put %s=%s", speciesGlyph.getId(), ynode));
		id2node.put(speciesGlyph.getId(), ynode);
	}

	@Override
	public void buildConnectingArc(SpeciesReferenceGlyph speciesReferenceGlyph) {
	}

	@Override
	public void buildLineSegment(LineSegment lineSegment) {
	}

	@Override
	public void buildCubicBezier(CubicBezier cubicBezier) {
	}

	@Override
	public void buildProcessNode(ReactionGlyph reactionGlyph,
			double rotationAngle) {
	}

	@Override
	public void buildTextGlyph(TextGlyph textGlyph) {
		BoundingBox boundingBox = textGlyph.getBoundingBox();
		Point point = boundingBox.getPosition();
		Dimensions dimensions = boundingBox.getDimensions();
		double x, y, width, height;
		x = point.getX();
		y = point.getY();
		width = dimensions.getWidth();
		height = dimensions.getHeight();
		
		String text = "";
		
		/* Possibilities:
		 * (See SBML Layout Extension documentation section "TextLabels")
		 * 1) independent text
		 * 		if only text attribute is given
		 * 2a) label for a graphical object
		 * 		if graphicalObject attributes contains the id of any graphicalObject
		 * 2b) label for a SBML model object
		 * 		if originOfText contains the id of a SBML model object, take text
		 * 		from name attribute of that object
		 * 
		 * text overrides originOfText
		 */
		
		if (textGlyph.isSetText() && !textGlyph.isSetGraphicalObject() && !textGlyph.isSetOriginOfText()) {
			// independent text
			text = textGlyph.getText();
			logger.info(String.format("building text glyph element id=%s\n\tindependent text text='%s'",
					textGlyph.getId(), text));
		}
		else if (textGlyph.isSetGraphicalObject() && textGlyph.isSetOriginOfText()) {
			// label for a graphical object
			Node origin = id2node.get(textGlyph.getGraphicalObject());
			NodeRealizer originRealizer = graph.getRealizer(origin);

			if (textGlyph.isSetText()) {
				text = textGlyph.getText();
				logger.info(String.format("building text glyph element id=%s\n\torigin text overridden text='%s'",
						textGlyph.getId(), text));
			}
			else {
				NamedSBase namedSBase = textGlyph.getOriginOfTextInstance();
				text = namedSBase.getName();
				logger.info(String.format("building text glyph element id=%s\n\ttext from origin id=%s text='%s'",
						textGlyph.getId(), namedSBase.getId(), text));
			}
			originRealizer.setLabelText(text);
		}
		else {
			logger.info(String.format("illegal text glyph id=%s",
					 textGlyph.getId()));
			return;
		}
		
		// TODO
	}

	@Override
	public void builderEnd() {
		terminated = true;
	}

	@Override
	public Graph2D getProduct() {
		return graph;
	}

	@Override
	public boolean isProductReady() {
		// TODO What are the options to detect that the graph is complete?
		return terminated;
	}

	@Override
	public Macromolecule<NodeRealizer> createMacromolecule() {
		return new YMacromolecule();
	}

	@Override
	public SourceSink<NodeRealizer> createSourceSink() {
		return new YSourceSink();
	}

	@Override
	public UnspecifiedNode<NodeRealizer> createUnspecifiedNode() {
		return new YUnspecifiedNode();
	}

	@Override
	public SimpleChemical<NodeRealizer> createSimpleChemical() {
		return new YSimpleChemical();
	}

	@Override
	public Compartment<NodeRealizer> createCompartment() {
		return new YCompartment();
	}

	@Override
	public Production<NodeRealizer> createProduction() {
		return new YProduction();
	}

	@Override
	public Consumption<NodeRealizer> createConsumption() {
		return new YConsumption();
	}

	@Override
	public Catalysis<NodeRealizer> createCatalysis() {
		return new YCatalysis();
	}

	@Override
	public Inhibition<NodeRealizer> createInhibition() {
		return new YInhibition();
	}

}
