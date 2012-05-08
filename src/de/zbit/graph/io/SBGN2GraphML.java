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
 * Copyright (C) 2010-2012 by the University of Tuebingen, Germany.
 *
 * KEGGtranslator is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.graph.io;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.sbgn.bindings.Arc;
import org.sbgn.bindings.Bbox;
import org.sbgn.bindings.Glyph;
import org.sbgn.bindings.Port;
import org.sbgn.bindings.Sbgn;

import y.base.Node;
import y.view.Arrow;
import y.view.EdgeRealizer;
import y.view.GenericEdgeRealizer;
import y.view.Graph2D;
import de.zbit.graph.io.def.SBGNProperties;
import de.zbit.graph.io.def.SBGNProperties.ArcType;
import de.zbit.graph.io.def.SBGNProperties.GlyphType;
import de.zbit.graph.sbgn.ReactionNodeRealizer;

/**
 * This class creates a {@link Graph2D} document (GraphML) to visualize
 * SBGN documents. Currently, many glyphes and arcs are supported, but
 * this class could still be enhanced to support all of them.
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class SBGN2GraphML extends SB_2GraphML<Sbgn> {
  public static final Logger log = Logger.getLogger(SBGN2GraphML.class.getName());
  
  /**
   * This map helps to enhance the reactionNode-layout, after the graph
   * is completely built and layouted.
   */
  Map<Glyph, ReactionNodeRealizer> reaction2node=null;
  
  /**
   * True if and only if any glyph added as node contained x/y information.
   */
  private boolean anyNodeContainedLayout = false;
  
  
  /* (non-Javadoc)
   * @see de.zbit.kegg.io.SB_2GraphML#createNodesAndEdges(java.lang.Object)
   */
  @Override
  protected void createNodesAndEdges(Sbgn document) {
    reaction2node = new HashMap<Glyph, ReactionNodeRealizer>();
    
    // Check if we have anything to visualize
    if (document==null || document.getMap()==null) return;
    
    // Add all glyphs to the graph
    addGlyphsToGraph(document);
    
    // Add all arcs to the graph
    addArcsToGraph(document);
    
    return;
  }

  /**
   * @param document
   */
  private void addArcsToGraph(Sbgn document) {
    for (Arc a : document.getMap().getArc()) {
      // Get source and target node
      Node source = null, target = null;
      if (a.getSource()!=null) {
        if (a.getSource() instanceof Glyph) {
          source = (Node) id2node.get(((Glyph)a.getSource()).getId());
        } else if (a.getSource() instanceof Port) {
          source = id2node.get(((Port) a.getSource()).getId());
        }
      }
      
      // TODO: These methods require glyphs to have an unique ID. But
      // the specification says, that one should identify glyphs by instance
      // and NOT by id...
      // TODO: Same ports should be translated to yFiles y.view.Port
      // so that all dock at the same positions.
      
      if (a.getTarget()!=null) {
        if (a.getTarget() instanceof Glyph) {
          target = (Node) id2node.get(((Glyph)a.getTarget()).getId());
        } else if (a.getTarget() instanceof Port) {
          target = id2node.get(((Port) a.getTarget()).getId());
        }
      }
      if (source==null || target==null) {
        log.warning(String.format("Missing %s glyph for arc %s.", source==null?"source":"target", a));
        continue;
      }
      
      if (simpleGraph.containsEdge(source, target)) {
        log.fine("Skippind duplicate arc-edge.");
        continue;
      } 
      
      // TODO: Also consider the arc's glyph (i.e. special reaction nodes, etc)
      
      EdgeRealizer er = new GenericEdgeRealizer();
      if (a.getClazz().equalsIgnoreCase(ArcType.consumption.toString())) {
        er.setTargetArrow(Arrow.NONE);
      } else if (a.getClazz().equalsIgnoreCase(ArcType.production.toString())) {
        er.setTargetArrow(Arrow.DELTA);
      } else if (a.getClazz().equalsIgnoreCase(ArcType.modulation.toString())) {
        er.setTargetArrow(Arrow.WHITE_DIAMOND);
      } else if (a.getClazz().equalsIgnoreCase(ArcType.stimulation.toString())) {
        er.setTargetArrow(Arrow.WHITE_DELTA);
      } else if (a.getClazz().equalsIgnoreCase(ArcType.catalysis.toString())) {
        er.setTargetArrow(Arrow.TRANSPARENT_CIRCLE);
      } else if (a.getClazz().equalsIgnoreCase(ArcType.inhibition.toString())) {
        er.setTargetArrow(Arrow.T_SHAPE);
      } else {
        log.warning(String.format("Please implement an arrow shape for '%s'.", a.getClazz()));
      }
      
      // TODO: Write the getStart() and getEnd() strings at the start/end of the edge.
      
      simpleGraph.createEdge(source, target, er);
    }
  }
  
  /**
   * @param document
   */
  private void addGlyphsToGraph(Sbgn document) {
    anyNodeContainedLayout=false;
    for (Glyph g : document.getMap().getGlyph()) {
      // Get the SBO-term (defining the shape and color)
      int sboTerm=0;
      GlyphType gType=null;
      if (g.getClazz()!=null) {
        gType = SBGNProperties.GlyphType.valueOfString(g.getClazz());
        sboTerm = gType.getSBOterm();
      }
      

      // Initialize default layout variables
      double x=Double.NaN;
      double y=Double.NaN;
      double w=46;
      double h=17;
      if (gType!=null && gType == GlyphType.process) {
        w = h = 8;
      }
      
      // Get information from the layout extension
      Bbox box = g.getBbox();
      if (box!=null) {
        if (isValid(box.getW()) && isValid(box.getH())) {
          w = box.getW();
          h = box.getH();
        }
        if (isValid(box.getX()) || isValid(box.getY())) {
          x = box.getX();
          y = box.getY();
          anyNodeContainedLayout = true;
        }
      }
      
      String label = null;
      if (g.getLabel()!=null) {
        label = g.getLabel().getText();
      } else {
        label = g.getId();
      }
      
      // Now create the real node
      Node n = createNode(g.getId(), label, sboTerm, x, y, w, h);
      if (simpleGraph.getRealizer(n) instanceof ReactionNodeRealizer) {
        reaction2node.put(g, (ReactionNodeRealizer)simpleGraph.getRealizer(n));
      }
      
      // Associtate ports with this node
      List<Port> ports = g.getPort();
      for (Port p: ports) {
        if (!id2node.containsKey(p.getId())) {
          id2node.put(p.getId(), n);
        }
      }
      
      // TODO: Draw the state (and also other attributes)
      //g.getState().getVariable() = "value @ variable" (e.g. "P@253".)
    }
  }
  
  /**
   * Fix ReactionNode nodes (determines 90° rotatable node orientation).
   * @param reaction2node
   */
  @Override
  protected void improveReactionNodeLayout() {
    if (reaction2node==null) return;
    for (Map.Entry<Glyph,ReactionNodeRealizer> en : reaction2node.entrySet()) {
      // TODO Iterate through all ARCS, inspect source and target and classify into:
      Set<Node> reactants = new HashSet<Node>();
      Set<Node> products = new HashSet<Node>();
      Set<Node> modifier = new HashSet<Node>();
      // See SBML2GraphML for an example
      en.getValue().fixLayout(reactants, products, modifier);
    }
  }

  /**
   * @param number
   * @return
   */
  private boolean isValid(float number) {
    // Also ignore 0|0 positions. They're due to default values
    return !Double.isNaN(number) && number!=0f;
  }

  /* (non-Javadoc)
   * @see de.zbit.kegg.io.SB_2GraphML#isAnyLayoutInformationAvailable()
   */
  @Override
  protected boolean isAnyLayoutInformationAvailable() {
    return anyNodeContainedLayout;
  }
  
  
}
