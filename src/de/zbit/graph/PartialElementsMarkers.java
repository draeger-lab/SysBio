/*
 * $Id$ $URL:
 * PartialElementsMarkers.java $
 * --------------------------------------------------------------------- This
 * file is part of the SysBio API library.
 * 
 * Copyright (C) 2009-2013 by the University of Tuebingen, Germany.
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation. A copy of the license agreement is provided in the file
 * named "LICENSE.txt" included with this software distribution and also
 * available online as <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.graph;

import java.awt.Color;

import y.base.DataProvider;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Node;
import y.base.NodeCursor;
import y.layout.partial.PartialLayouter;
import y.util.DataProviderAdapter;
import y.view.Graph2D;
import y.view.Selections;

/**
 * Provides methods to mark nodes and/or edges as <em>partial</em> (with regards
 * to {@link PartialLayouter#PARTIAL_NODES_DPKEY} and
 * {@link PartialLayouter#PARTIAL_EDGES_DPKEY} according to either their color
 * or their selection state (with regards to
 * {@link y.view.Graph2D#isSelected(y.base.Edge)} and
 * {@link y.view.Graph2D#isSelected(y.base.Node)}).
 * 
 * <p>
 * Originates from a template from yFiles examples!
 * 
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class PartialElementsMarkers {
  DataProvider odpSelectedNodes;
  DataProvider odpSelectedEdges;

  /**
   * Adds data providers to the given graph for keys
   * {@link PartialLayouter#PARTIAL_NODES_DPKEY} and {@link PartialLayouter#PARTIAL_EDGES_DPKEY}
   * that reflect the selection state (with regards to
   * {@link y.view.Graph2D#isSelected(y.base.Edge)} and
   * {@link y.view.Graph2D#isSelected(y.base.Node)}) of nodes and edges.
   * @param graph the graph for which selection markers are created.
   * @return {@code true} if the specified graph has selected nodes
   * and/or selected edges and {@code false} otherwise.
   */
  boolean markBySelection(final Graph2D graph) {
    //store the old data provider
    odpSelectedNodes = graph.getDataProvider(PartialLayouter.PARTIAL_NODES_DPKEY);
    odpSelectedEdges = graph.getDataProvider(PartialLayouter.PARTIAL_EDGES_DPKEY);

    //register dp
    graph.addDataProvider(PartialLayouter.PARTIAL_NODES_DPKEY, Selections.createSelectionNodeMap(graph));
    graph.addDataProvider(PartialLayouter.PARTIAL_EDGES_DPKEY, Selections.createSelectionEdgeMap(graph));

    return (graph.selectedNodes().ok() || graph.selectedEdges().ok());
  }

  /**
   * Adds data providers to the given graph for keys
   * {@link PartialLayouter#PARTIAL_NODES_DPKEY} and {@link PartialLayouter#PARTIAL_EDGES_DPKEY}
   * that report an edge as <em>partial</em> if its state color equals
   * the specified partial edge color and a node if its state color equals
   * the specified partial node color.
   * @param graph the graph for which selection markers are created.
   * @return {@code true} if the specified graph has nodes and/or edges
   * with the appropriate state colors and {@code false} otherwise.
   */
  boolean markByColor(final Graph2D graph, final Color c) {
    //store the old data provider
    odpSelectedNodes = graph.getDataProvider(PartialLayouter.PARTIAL_NODES_DPKEY);
    odpSelectedEdges = graph.getDataProvider(PartialLayouter.PARTIAL_EDGES_DPKEY);
      
    //Determine partial nodes/edges by color:
    DataProviderAdapter isPartialNode = new DataProviderAdapter() {
      public boolean getBool(Object dataHolder) {
        Node n = (Node) dataHolder;
        if (graph.getHierarchyManager().isGroupNode(n) ||
            graph.getHierarchyManager().isFolderNode(n)) {
          return c.equals(graph.getRealizer(n).getLabel().getBackgroundColor());
        } else {
          return c.equals(graph.getRealizer(n).getFillColor());
        }
      }
    };

    DataProviderAdapter isPartialEdge = new DataProviderAdapter() {
      public boolean getBool(Object dataHolder) {
        return c.equals(graph.getRealizer((Edge) dataHolder).getLineColor());
      }
    };

    graph.addDataProvider(PartialLayouter.PARTIAL_NODES_DPKEY, isPartialNode);
    graph.addDataProvider(PartialLayouter.PARTIAL_EDGES_DPKEY, isPartialEdge);

    for (NodeCursor nodeCursor = graph.nodes(); nodeCursor.ok(); nodeCursor.next()) {
      Node node = nodeCursor.node();
      if (isPartialNode.getBool(node)) {
        return true;
      }
    }
    for (EdgeCursor edgeCursor = graph.edges(); edgeCursor.ok(); edgeCursor.next()) {
      Edge edge = edgeCursor.edge();
      if (isPartialEdge.getBool(edge)) {
        return true;
      }
    }

    return false;
  }
  
  void resetMarkers(Graph2D graph) {
    //reset data provider
    graph.removeDataProvider(PartialLayouter.PARTIAL_NODES_DPKEY);
    if (odpSelectedNodes != null) {
      //set the old data provider
      graph.addDataProvider(PartialLayouter.PARTIAL_NODES_DPKEY, odpSelectedNodes);
    }

    graph.removeDataProvider(PartialLayouter.PARTIAL_EDGES_DPKEY);
    if (odpSelectedEdges != null) {
      //set the old data provider
      graph.addDataProvider(PartialLayouter.PARTIAL_EDGES_DPKEY, odpSelectedEdges);
    }
  }
}
