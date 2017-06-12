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
package de.zbit.sbml.layout.y;

import static de.zbit.graph.sbgn.DrawingOptions.FONT;
import static de.zbit.graph.sbgn.DrawingOptions.FONT_COLOR;

import java.awt.Color;
import java.awt.Font;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.NamedSBase;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.SimpleSpeciesReference;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.ext.layout.AbstractReferenceGlyph;
import org.sbml.jsbml.ext.layout.BoundingBox;
import org.sbml.jsbml.ext.layout.CompartmentGlyph;
import org.sbml.jsbml.ext.layout.CubicBezier;
import org.sbml.jsbml.ext.layout.Curve;
import org.sbml.jsbml.ext.layout.CurveSegment;
import org.sbml.jsbml.ext.layout.Dimensions;
import org.sbml.jsbml.ext.layout.Layout;
import org.sbml.jsbml.ext.layout.LineSegment;
import org.sbml.jsbml.ext.layout.Point;
import org.sbml.jsbml.ext.layout.ReactionGlyph;
import org.sbml.jsbml.ext.layout.SpeciesGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceRole;
import org.sbml.jsbml.ext.layout.TextGlyph;
import org.sbml.jsbml.util.StringTools;

import de.zbit.graph.sbgn.DrawingOptions;
import de.zbit.graph.sbgn.FillLevelNodeRealizer;
import de.zbit.graph.sbgn.ReactionNodeRealizer;
import de.zbit.graph.sbgn.ShapeNodeRealizerSupportingCloneMarker;
import de.zbit.sbml.layout.AbstractLayoutBuilder;
import de.zbit.sbml.layout.AssociationNode;
import de.zbit.sbml.layout.Catalysis;
import de.zbit.sbml.layout.Compartment;
import de.zbit.sbml.layout.Consumption;
import de.zbit.sbml.layout.DissociationNode;
import de.zbit.sbml.layout.Inhibition;
import de.zbit.sbml.layout.LayoutDirector;
import de.zbit.sbml.layout.Macromolecule;
import de.zbit.sbml.layout.Modulation;
import de.zbit.sbml.layout.NecessaryStimulation;
import de.zbit.sbml.layout.NucleicAcidFeature;
import de.zbit.sbml.layout.OmittedProcessNode;
import de.zbit.sbml.layout.PerturbingAgent;
import de.zbit.sbml.layout.ProcessNode;
import de.zbit.sbml.layout.Production;
import de.zbit.sbml.layout.RenderProcessor;
import de.zbit.sbml.layout.ReversibleConsumption;
import de.zbit.sbml.layout.SBGNArc;
import de.zbit.sbml.layout.SBGNNode;
import de.zbit.sbml.layout.SBGNNodeWithCloneMarker;
import de.zbit.sbml.layout.SBGNProcessNode;
import de.zbit.sbml.layout.SimpleChemical;
import de.zbit.sbml.layout.SourceSink;
import de.zbit.sbml.layout.Stimulation;
import de.zbit.sbml.layout.UncertainProcessNode;
import de.zbit.sbml.layout.UnspecifiedNode;
import de.zbit.util.prefs.Option;
import de.zbit.util.prefs.SBPreferences;
import de.zbit.util.progressbar.AbstractProgressBar;
import de.zbit.util.progressbar.ProgressListener;
import y.base.Edge;
import y.base.Node;
import y.geom.OrientedRectangle;
import y.geom.YPoint;
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

/**
 * @author Jakob Matthes
 * @version $Rev$
 */
public class YLayoutBuilder extends AbstractLayoutBuilder<ILayoutGraph, NodeRealizer, EdgeRealizer> {
  
  /**
   * User preferences.
   */
  private static final transient SBPreferences prefs = SBPreferences.getPreferencesFor(DrawingOptions.class);
  
  /**
   * curve segment type cubic bezier
   */
  private static final String CURVESEGMENT_CUBICBEZIER = "CubicBezier";
  
  /**
   * A {@link Logger} for this class.
   */
  private static final transient Logger logger = Logger.getLogger(YLayoutBuilder.class.toString());
  
  /**
   * A YFiles Graph2D is the product of this builder.
   */
  private Graph2D graph;
  
  /**
   * List keeping track of ProgressListeners.
   */
  private List<ProgressListener> progressListeners = new LinkedList<ProgressListener>();
  
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
   * Map reaction id to all yfiles process nodes.
   */
  private Map<String, Set<Node>> reactionId2Node = new HashMap<String, Set<Node>>();
  
  /**
   * Map reaction glyph id to set of adjoining edges.
   */
  private Map<String, Set<Edge>> reactionGlyphId2edges = new HashMap<String, Set<Edge>>();
  
  /**
   * Map a species id to the set of reactions the species is involved in.
   * For each species only the reactions where species is necessary are listed.
   */
  private Map<String, Set<String>> speciesId2reactions = new HashMap<String, Set<String>>();
  
  /**
   * Maps yfiles node to the represented SRG
   * TODO merge / replace with id2node map
   */
  private Map<Node, AbstractReferenceGlyph> node2glyph = new HashMap<Node, AbstractReferenceGlyph>();
  
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
    // TODO: grant access to the graph to listeners.
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
    node2glyph.put(ynode, compartmentGlyph);
    logger.fine(MessageFormat.format("building compartment glyph id={0}\n\tbounding box={1}",
      compartmentGlyph.getId(), nodeRealizer.getBoundingBox()));
  }
  
  /* (non-Javadoc)
   * @see de.zbit.sbml.layout.LayoutBuilder#buildEntityPoolNode(org.sbml.jsbml.ext.layout.SpeciesGlyph, boolean)
   */
  @Override
  public void buildEntityPoolNode(SpeciesGlyph speciesGlyph,
    boolean cloneMarker) {
    SBGNNode<NodeRealizer> node = getSBGNNode(speciesGlyph.getSBOTerm());
    
    if (node instanceof SBGNNodeWithCloneMarker) {
      ((SBGNNodeWithCloneMarker<?>) node).setCloneMarker(cloneMarker);
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
    
    nodeRealizer.setLineColor(Option.parseOrCast(Color.class, prefs.get(DrawingOptions.DEFAULT_LINE_COLOR)));
    
    //		nodeRealizer.setDropShadowColor(new Color(0, 0, 0, 64));
    //	    nodeRealizer.setDropShadowOffsetX((byte) 3);
    //	    nodeRealizer.setDropShadowOffsetY((byte) 3);
    Color fillColor = RenderProcessor.getRenderFillColor(speciesGlyph);
    if (fillColor == null) {
      fillColor = nodeRealizer.getFillColor();
      /*      nodeRealizer.setFillColor2(fillColor.brighter());
    } else {
      nodeRealizer.setFillColor2(fillColor.darker());*/
    }
    nodeRealizer.setFillColor(fillColor);
    
    logger.fine(MessageFormat.format("building EPN element id={0} sbo={1} (%s)\n\tbounding box= {2} {3}",
      speciesGlyph.getId(), speciesGlyph.getSBOTerm(), SBO.convertSBO2Alias(speciesGlyph.getSBOTerm()),
      speciesGlyph.getBoundingBox().getPosition(), nodeRealizer.getBoundingBox()));
    
    Node ynode = graph.createNode();
    if (speciesGlyph.isSetSpecies()) {
      Node compartmentYNode = id2node.get(((Species) speciesGlyph.getSpeciesInstance()).getCompartment());
      if (compartmentYNode != null) {
        graph.getHierarchyManager().setParentNode(ynode, compartmentYNode);
      }
    }
    graph.setRealizer(ynode, nodeRealizer);
    id2node.put(speciesGlyph.getId(), ynode);
    putInMapSet(speciesId2Node, speciesGlyph.getSpecies(), ynode);
    node2glyph.put(ynode, speciesGlyph);
  }
  
  /**
   * @param <T>
   * @param speciesId2Node2
   * @param species
   * @param ynode
   */
  private <T> void putInMapSet(Map<String, Set<T>> id2Nodes,
    String id, T object) {
    if (!id2Nodes.containsKey(id)) {
      Set<T> hashSet = new HashSet<T>();
      hashSet.add(object);
      id2Nodes.put(id, hashSet);
    } else {
      Set<T> hashSet = id2Nodes.get(id);
      hashSet.add(object);
    }
  }
  
  /* (non-Javadoc)
   * @see de.zbit.sbml.layout.LayoutBuilder#buildConnectingArc(org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph, org.sbml.jsbml.ext.layout.ReactionGlyph)
   */
  @Override
  public void buildConnectingArc(SpeciesReferenceGlyph srg, ReactionGlyph reactionGlyph, double curveWidth) {
    
    Node processNode = id2node.get(reactionGlyph.getId());
    SpeciesGlyph speciesGlyph = srg.getSpeciesGlyphInstance();
    String speciesId = speciesGlyph.getSpecies();
    Node speciesGlyphNode = id2node.get(srg.getSpeciesGlyph());
    assert processNode != null;
    assert speciesGlyphNode != null;
    
    SBGNArc<EdgeRealizer> arc = createArc(srg, reactionGlyph);
    
    EdgeRealizer edgeRealizer = arc.draw(srg.getCurve(), curveWidth);
    
    // dock correctly at process node
    Point relativeDockingAtPN = (Point) srg.getUserObject(LayoutDirector.PN_RELATIVE_DOCKING_POINT);
    logger.fine(MessageFormat.format("srg={0} rg={1} dock relative at PN {2}", srg.getId(), reactionGlyph.getId(), relativeDockingAtPN));
    if (relativeDockingAtPN != null) {
      double x = relativeDockingAtPN.getX();
      double y = relativeDockingAtPN.getY();
      edgeRealizer.setSourcePoint(new YPoint(x, y));
    }
    else {
      logger.fine(MessageFormat.format(
        "Algorithm did not calculate relative docking position at process node for species reference glyph ''{0}''",
        srg.getId()));
    }
    
    // docking at species works automatically, YFiles points the edge towards the center of the node
    
    // display stoichiometry labels
    if (srg.isSetSpeciesReference()) {
      NamedSBase nsb = srg.getSpeciesReferenceInstance();
      if (nsb == null) {
        logger.fine(MessageFormat.format(
          "Encountered undefined identifier ''{0}'' in speciesReferenceGlyph ''{1}''.",
          srg.getSpeciesReference(), srg.getId()));
      } else {
        if (!(nsb instanceof SimpleSpeciesReference)) {
          logger.warning(MessageFormat.format(
            "Expecting simple species reference, but found {0} in {1}.",
            nsb.getElementName(), srg));
        } else {
          SimpleSpeciesReference speciesReference = (SimpleSpeciesReference) nsb;
          if (speciesReference instanceof SpeciesReference) {
            SpeciesReference specRef = (SpeciesReference) speciesReference;
            if (specRef.isSetStoichiometry() && (specRef.getStoichiometry() != 1)) {
              String value = StringTools.toString(specRef.getStoichiometry());
              EdgeLabel edgeLabel = new StoichiometryLabel(value);
              edgeRealizer.addLabel(edgeLabel);
            }
          }
        }
      }
    }
    
    Edge edge = graph.createEdge(processNode, speciesGlyphNode, edgeRealizer);
    putInMapSet(reactionGlyphId2edges, reactionGlyph.getId(), edge);
    if (neccessary(srg)) {
      putInMapSet(speciesId2reactions, speciesId, reactionGlyph.getReaction());
    }
    node2glyph.put(processNode, reactionGlyph);
  }
  
  /**
   * checks if given {@link SpeciesReferenceGlyph} is necessary for
   * the reaction to take place
   * @param srg
   * @return
   */
  private boolean neccessary(SpeciesReferenceGlyph srg) {
    // TODO Roles need higher priority
    if (srg.isSetSBOTerm()) {
      int sbo = srg.getSBOTerm();
      if (SBO.isChildOf(sbo, SBO.getProduct())
          || SBO.isChildOf(sbo, SBO.getReactant())
          || SBO.isChildOf(sbo, SBO.getEssentialActivator())) {
        return true;
      }
      return false;
    }
    else {
      SpeciesReferenceRole role = srg.getSpeciesReferenceRole();
      switch (role) {
        case INHIBITOR: return false;
        case ACTIVATOR: return false; //TODO not sure
        default:
          break;
      }
      return true;
    }
  }
  
  /* (non-Javadoc)
   * @see de.zbit.sbml.layout.LayoutBuilder#buildCubicBezier(org.sbml.jsbml.ext.layout.CubicBezier)
   */
  @Override
  public void buildCubicBezier(CubicBezier cubicBezier, double lineWidth) {
    // TODO partial edge drawing is not supported
  }
  
  /* (non-Javadoc)
   * @see de.zbit.sbml.layout.LayoutBuilder#buildProcessNode(org.sbml.jsbml.ext.layout.ReactionGlyph, double)
   */
  @Override
  public void buildProcessNode(ReactionGlyph reactionGlyph, double rotationAngle, double curveWidth) {
    NodeRealizer processNodeRealizer;
    
    SBGNProcessNode<NodeRealizer> processNode; // = createProcessNode();
    if (reactionGlyph.isSetReaction()) {
      processNode = getSBGNReactionNode(reactionGlyph.getReactionInstance().getSBOTerm());
    } else {
      processNode = getSBGNReactionNode(reactionGlyph.getSBOTerm());
    }
    assert processNode != null;
    
    processNode.setLineWidth(prefs.getDouble(DrawingOptions.GLYPH_LINE_WIDTH));
    
    if (reactionGlyph.isSetCurve()) {
      processNodeRealizer = processNode.draw(reactionGlyph.getCurve(), 0d, null);
      
    } else {
      BoundingBox boundingBox = reactionGlyph.getBoundingBox();
      Point point = boundingBox.getPosition();
      Dimensions dimension = boundingBox.getDimensions();
      double x = 0d, y = 0d, z = 0d, width = 1d, height = 1d, depth = 1d;
      if (point != null) {
        x = point.getX();
        y = point.getY();
        z = point.getZ();
      } else {
        logger.warning(MessageFormat.format(
          "No position given for reaction glyph {0} - using default values",
          reactionGlyph));
      }
      if (dimension != null) {
        width = dimension.getWidth();
        height = dimension.getHeight();
        depth = dimension.getDepth();
      } else {
        logger.warning(MessageFormat.format(
          "No dimension given for reaction glyph {0} - using default values",
          reactionGlyph));
      }
      
      Point rotationCenter = new Point(x + (width / 2d), y + (height / 2d), z + (depth / 2d));
      logger.fine(MessageFormat.format("Process node position: {0} rotationAngle: {1} rotationCenter: {2}",
        point, rotationAngle, rotationCenter));
      
      processNodeRealizer = processNode.draw(
        x, y, z, width, height, depth, rotationAngle, rotationCenter);
    }
    
    if ((processNodeRealizer instanceof ReactionNodeRealizer)
        && (reactionGlyph.isSetUserObjects())
        && (reactionGlyph.getUserObject(LayoutDirector.NO_WHISKERS) != null)) {
      ((ReactionNodeRealizer) processNodeRealizer).setWhiskers(false);
      logger.fine(MessageFormat.format("building PN id={0} bounding box={1} {2}",
        reactionGlyph.getId(), reactionGlyph.getBoundingBox().getPosition(), processNodeRealizer.getBoundingBox()));
    }
    Node processYNode = graph.createNode(processNodeRealizer);
    id2node.put(reactionGlyph.getId(), processYNode);
    putInMapSet(reactionId2Node, reactionGlyph.getReaction(), processYNode);
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
      logger.fine(MessageFormat.format("building text glyph element id={0}\n\tindependent text text='{1}'",
        textGlyph.getId(), text));
      
      Node ynode = graph.createNode();
      IndependentTextRealizer textRealizer =
          new IndependentTextRealizer(x, y, width, height,text);
      textRealizer.setLineColor(Option.parseOrCast(Color.class, prefs.get(FONT_COLOR)));
      Font font = FONT.parseOrCast(prefs.get(FONT));
      // TODO: this is a quick hack.. get font information from Render if possible.
      Font f = new Font(font.getFontName(), font.getStyle(), font.getSize() * 3);
      textRealizer.setFont(f);
      graph.setRealizer(ynode, textRealizer);
    }
    else if (textGlyph.isSetGraphicalObject() &&
        (textGlyph.isSetOriginOfText() || textGlyph.isSetText())) {
      // label for a graphical object
      // label text glyphs are collected and built as a last step of the builder
      labelTextGlyphs.add(textGlyph);
    }
    else {
      logger.warning(MessageFormat.format("illegal text glyph id={0}", textGlyph.getId()));
    }
  }
  
  /**
   * Realizes a text glyph as a label of an already existing node.
   * @param textGlyph
   */
  private void buildTextGlyphAsLabel(TextGlyph textGlyph) {
    // TODO: We need some option for the default font size!
    int fontSize = 18;
    NamedSBase namedSBase = null;
    if (textGlyph.isSetOriginOfText()) {
      namedSBase = textGlyph.getOriginOfTextInstance();
    }
    Node origin = id2node.get(textGlyph.getGraphicalObject());
    if (origin == null) {
      return;
    }
    NodeRealizer originRealizer = graph.getRealizer(origin);
 
    String text = null;
    if (textGlyph.isSetText()) {
      text = textGlyph.getText();
      logger.fine(MessageFormat.format(
        "Building text glyph element id={0}\n\torigin text overridden text='{1}'",
        textGlyph.getId(), text));
    }
    else if (textGlyph.isSetOriginOfText()) {
      namedSBase = textGlyph.getOriginOfTextInstance();
      if (namedSBase != null) {
        text = namedSBase.getName();
        logger.fine(MessageFormat.format(
          "Building text glyph element id={0}\n\ttext from origin id={1} text='{2}'",
          textGlyph.getId(), namedSBase.getId(), text));
      } else {
        text = textGlyph.getOriginOfText();
        logger.warning(MessageFormat.format(
          "No such element defined ''{0}'', using reference id as label.",
          textGlyph.getOriginOfText()));
      }
    } else {
      text = "";
      logger.warning(MessageFormat.format(
        "Encountered text label ''{0}'' with undefined text.",
        textGlyph.getId()));
    }
    
    if (textGlyph.isSetBoundingBox() &&
        textGlyph.getBoundingBox().isSetPosition() &&
        textGlyph.getBoundingBox().isSetDimensions() &&
        !((textGlyph.getBoundingBox().getDimensions().getWidth() == 0) &&
            (textGlyph.getBoundingBox().getDimensions().getHeight() == 0) &&
            (textGlyph.getBoundingBox().getPosition().getX() == 0) &&
            (textGlyph.getBoundingBox().getPosition().getY() == 0))) {
      logger.fine("using nodelabel for textglyph " + textGlyph.getId());
      NodeLabel nodeLabel;
      nodeLabel = new NodeLabel(text);
      nodeLabel.setLabelModel(new FreeNodeLabelModel());
      nodeLabel.setFontSize(fontSize);
      originRealizer.setLabel(nodeLabel);
      
      // text glyph position
      Point position = textGlyph.getBoundingBox().getPosition();
      double glyphX = position.getX();
      double glyphY = position.getY();
      
      logger.fine(MessageFormat.format("text position is {0,number},{1,number}", glyphX, glyphY));
      
      Dimensions dimensions = textGlyph.getBoundingBox().getDimensions();
      double width = dimensions.getWidth();
      double height = dimensions.getHeight();
      
      OrientedRectangle orientedRectangle =
          new OrientedRectangle(glyphX, glyphY + height, width, height, 0f, -1f);
      logger.fine("oriented rectangle is " + orientedRectangle.toString());
      Object param = nodeLabel.getBestModelParameterForBounds(orientedRectangle);
      if (param != null) {
        logger.fine("using extra positioning for textglyph " + textGlyph.getId());
        nodeLabel.setModelParameter(param);
      }
            
    } else if (text != null) {
      if (namedSBase != null) {
        NodeLabel nodeLabel;
        nodeLabel = new NodeLabel(text);
        if (namedSBase instanceof org.sbml.jsbml.Compartment) {
          nodeLabel.setModel(NodeLabel.INTERNAL);
          nodeLabel.setPosition(NodeLabel.TOP_RIGHT);
          nodeLabel.setDistance(20d);
        } else if (namedSBase instanceof org.sbml.jsbml.Reaction) {
          nodeLabel = new NodeLabel(text);
          nodeLabel.setModel(NodeLabel.CORNERS);
          nodeLabel.setPosition(NodeLabel.SE);
        } else if (namedSBase instanceof org.sbml.jsbml.Species) {
          nodeLabel.setModel(NodeLabel.INTERNAL);
          
          //          SmartNodeLabelModel nlabel = new SmartNodeLabelModel();
          //          nodeLabel.setLabelModel(nlabel,nlabel.getDefaultParameter());
          
          //          if (originRealizer.getWidth() < nodeLabel.getWidth()) {
          //            originRealizer.getBoundingBox();
          //            originRealizer.setWidth(nodeLabel.getWidth());
          //          }
          //          if (originRealizer.getHeight() < nodeLabel.getHeight()) {
          //            originRealizer.setHeight(nodeLabel.getHeight());
          //          }
          
          
        }
        nodeLabel.setFontSize(fontSize);
        originRealizer.setLabel(nodeLabel);
      } else {
        originRealizer.setLabelText(text);
        NodeLabel nodeLabel = originRealizer.getLabel();
        nodeLabel.setFontSize(fontSize);
        
      }
    }
  }
  
  /**
   * @param curve
   * @return
   */
  public static EdgeRealizer createEdgeRealizerFromCurve(Curve curve) {
    return createEdgeRealizerFromCurve(curve, true);
  }
  
  /**
   * 
   * @param curve
   * @param forward
   * @return
   */
  public static EdgeRealizer createEdgeRealizerFromCurve(Curve curve,
    boolean forward) {
    EdgeRealizer edgeRealizer = new GenericEdgeRealizer();
    
    // Note: if multiple curve segments (beziers) are specified, the resulting
    // representation will not be standard compliant.
    
    if ((curve != null) && curve.isSetListOfCurveSegments()) {
      List<CurveSegment> listOfCurveSegments = curve.getListOfCurveSegments();
      
      // if at least one curve segment is a Beziér, use BezierEdgeRealizer, else use PolyLineEdgeRealizer
      boolean drawBezier = false;
      for (CurveSegment curveSegment : listOfCurveSegments) {
        //        drawBezier = drawBezier && curveSegment.isSetType() && curveSegment.getType().equals(CURVESEGMENT_CUBICBEZIER);
        //        if (!drawBezier) {
        //          break;
        //        }
        if (curveSegment instanceof CubicBezier) {
          drawBezier = true;
          break;
        }
      }
      
      edgeRealizer = drawBezier ? new BezierEdgeRealizer() : new PolyLineEdgeRealizer();
      
      if (forward) {
        for (int i = 0; i < listOfCurveSegments.size(); i++) {
          CurveSegment curveSegment = listOfCurveSegments.get(i);
          drawCurveSegment(edgeRealizer, curveSegment, forward);
        }
      } else {
        for (int i = listOfCurveSegments.size() - 1; i >= 0; i--) {
          CurveSegment curveSegment = listOfCurveSegments.get(i);
          drawCurveSegment(edgeRealizer, curveSegment, forward);
        }
      }
    }
    
    return edgeRealizer;
  }
  
  /**
   * 
   * @param curveSegment
   * @return
   */
  public static EdgeRealizer drawCurveSegment(CurveSegment curveSegment) {
    return drawCurveSegment(curveSegment instanceof CubicBezier ? new BezierEdgeRealizer() : new PolyLineEdgeRealizer(), curveSegment);
  }
  
  /**
   * @param edgeRealizer
   * @param drawBezier
   * @param curveSegment
   * @return
   */
  private static EdgeRealizer drawCurveSegment(EdgeRealizer edgeRealizer, CurveSegment curveSegment) {
    return drawCurveSegment(edgeRealizer, curveSegment, true);
  }
  
  /**
   * 
   * @param edgeRealizer
   * @param curveSegment
   * @param forward
   * @return
   */
  private static EdgeRealizer drawCurveSegment(EdgeRealizer edgeRealizer, CurveSegment curveSegment, 
		  boolean forward) {
    LineSegment ls = (LineSegment) curveSegment;
    
    Point start = forward ? ls.getStart() : ls.getEnd();
    edgeRealizer.addPoint(start.getX(), start.getY());
    if ((edgeRealizer instanceof BezierEdgeRealizer) && (curveSegment instanceof CubicBezier)) {
      CubicBezier cb = (CubicBezier) curveSegment;
      BezierEdgeRealizer ber = (BezierEdgeRealizer) edgeRealizer;
      if (forward) {
        if (cb.isSetBasePoint1()) {
          Point basePoint1 = cb.getBasePoint1();
          ber.appendBend(basePoint1.getX(), basePoint1.getY());
        }
        if (cb.isSetBasePoint2()) {
          Point basePoint2 = cb.getBasePoint2();
          ber.appendBend(basePoint2.getX(), basePoint2.getY());
        }
      } else {
        if (cb.isSetBasePoint2()) {
          Point basePoint2 = cb.getBasePoint2();
          ber.appendBend(basePoint2.getX(), basePoint2.getY());
        }
        if (cb.isSetBasePoint1()) {
          Point basePoint1 = cb.getBasePoint1();
          ber.appendBend(basePoint1.getX(), basePoint1.getY());
        }
      }
    }
    Point end = forward ? ls.getEnd() : ls.getStart();
    edgeRealizer.addPoint(end.getX(), end.getY());
    
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
    Model model = layout.getModel();
    if (model.isSetListOfReactions()) {
      for (Reaction reaction : model.getListOfReactions()) {
        String reactionId = reaction.getId();
        Set<List<Edge>> edgeListSet = new HashSet<List<Edge>>();
        List<AbstractReferenceGlyph> reactionGlyphs = (List<AbstractReferenceGlyph>) reaction.getUserObject(LayoutDirector.LAYOUT_LINK);
        if (reactionGlyphs != null) {
          for (AbstractReferenceGlyph reactionGlyph : reactionGlyphs) {
            String id = reactionGlyph.getId();
            Set<Edge> setOfEdges = reactionGlyphId2edges.get(id);
            if (setOfEdges != null) {
              edgeListSet.add(new LinkedList<Edge>(setOfEdges));
            }
          }
        }
        reactionId2Edge.put(reactionId, edgeListSet);
      }
    }
    ILayoutGraph layoutGraph = new LayoutGraph(speciesId2Node,
      compartmentId2Node,
      reactionId2Node,
      reactionId2Edge,
      speciesId2reactions,
      node2glyph,
      graph);
    
    return layoutGraph;
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
   * @see de.zbit.sbml.layout.LayoutFactory#createReversibleConsumption()
   */
  @Override
  public ReversibleConsumption<EdgeRealizer> createReversibleConsumption() {
    return new YReversibleConsumption();
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
  
  /* (non-Javadoc)
   * @see de.zbit.sbml.layout.LayoutFactory#createProcessNode()
   */
  @Override
  public ProcessNode<NodeRealizer> createProcessNode() {
    return new YProcessNode();
  }
  
  /* (non-Javadoc)
   * @see de.zbit.sbml.layout.LayoutFactory#createAssociationNode()
   */
  @Override
  public AssociationNode<NodeRealizer> createAssociationNode() {
    return new YAssociationNode();
  }
  
  /* (non-Javadoc)
   * @see de.zbit.sbml.layout.LayoutFactory#createDissociationNode()
   */
  @Override
  public DissociationNode<NodeRealizer> createDissociationNode() {
    return new YDissociationNode();
  }
  
  /* (non-Javadoc)
   * @see de.zbit.sbml.layout.LayoutFactory#createOmittedProcessNode()
   */
  @Override
  public OmittedProcessNode<NodeRealizer> createOmittedProcessNode() {
    return new YOmittedProcessNode();
  }
  
  /* (non-Javadoc)
   * @see de.zbit.sbml.layout.LayoutFactory#createUncertainProcessNode()
   */
  @Override
  public UncertainProcessNode<NodeRealizer> createUncertainProcessNode() {
    return new YUncertainProcessNode();
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("YLayoutBuilder [layout=");
    builder.append(layout);
    builder.append("]");
    return builder.toString();
  }
  
}
