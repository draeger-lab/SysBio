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
package de.zbit.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import y.base.DataMap;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.EdgeMap;
import y.base.Graph;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeMap;
import y.base.YCursor;
import y.io.IOHandler;
import y.layout.Layouter;
import y.layout.organic.SmartOrganicLayouter;
import y.view.EdgeLabel;
import y.view.Graph2D;
import y.view.Graph2DLayoutExecutor;
import y.view.Graph2DView;
import y.view.HitInfo;
import y.view.NodeLabel;
import y.view.NodeRealizer;
import y.view.Selections;
import y.view.View;
import y.view.hierarchy.GroupLayoutConfigurator;
import y.view.hierarchy.GroupNodeRealizer;
import y.view.hierarchy.HierarchyManager;
import de.zbit.graph.io.Graph2Dwriter;
import de.zbit.graph.io.def.GenericDataMap;
import de.zbit.graph.io.def.GraphMLmaps;

/**
 * Various tools for {@link Graph2D} and also for
 * ineractions with {@link GraphMLmaps}.
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class GraphTools {
  public static final transient Logger log = Logger.getLogger(GraphTools.class.getName());
  
  /**
   * A graph on which operations are performed.
   */
  protected Graph2D graph;
  
  /**
   * A reverse map of {@link Graph#getDataProvider(Object)} where Object is
   * {@link KEGG2yGraph#mapDescription}.
   * <p>In other words, returns directly the map for, e.g. "entrezIds"
   */
  protected Map<String, DataMap> descriptor2Map=null;
  
  public GraphTools(Graph2D graph){
    super();
    this.graph=graph;
    if (this.graph==null) log.warning("Graph is null!");
    init();
  }
  
  @SuppressWarnings("unchecked")
  private void init() {
    if (graph==null) {
      descriptor2Map=null;
      return;
    }
    GenericDataMap<DataMap, String> mapDescriptionMap = (GenericDataMap<DataMap, String>) graph.getDataProvider(Graph2Dwriter.mapDescription);
    descriptor2Map = mapDescriptionMap.createReverseMap();
  }
  
  /**
   * There is an optional extension available for yFiles
   * that allows to write graphs as SVG files. 
   * @return <code>TRUE</code> if the extension is available.
   */
  @SuppressWarnings("unchecked")
  public static boolean isSVGextensionInstalled() {
    try {
      Class<? extends IOHandler> svg = (Class<? extends IOHandler>) Class.forName("yext.svg.io.SVGIOHandler");    
      if (svg!=null) return true;
    } catch (Throwable e) {
      // Extension not installed
    }

    return false;
  }
  

  /**
   * Update the enabled state of all registered views
   * to the given value.
   * @param graph
   * @param state
   * @throws Throwable
   */
  public static void enableViews(Graph2D graph, boolean state) throws Throwable {
    YCursor yc = graph.getViews();
    while (yc.ok()) {
      if (yc.current() instanceof Graph2DView) {
        ((Graph2DView)yc.current()).setEnabled(state);
      } else if (yc.current() instanceof View) { 
        ((View)yc.current()).getComponent().setEnabled(state);
      }
      yc.next();
    }
  }
  

  /**
   * Layout the graph with the given layout.
   * @param layouterClass
   */
  public void layout(Class<? extends Layouter> layouterClass) {
    graph.unselectAll();
    
    // Remember group node sizes and insets
    GroupLayoutConfigurator glc = new GroupLayoutConfigurator(graph);
    glc.prepareAll();
    
    // Create layouter and perform layout
    Layouter layouter;
    try {
      layouter = layouterClass.newInstance();
    } catch (Exception e) {
      log.log(Level.WARNING, "Could not create graph layouter.", e);
      return;
    }

    // Change a few properties to make the result nicer
    if (layouter instanceof SmartOrganicLayouter) {
      SmartOrganicLayouter la = ((SmartOrganicLayouter) layouter);
      la.setMinimalNodeDistance(15);
      la.setCompactness(0.7d);
    }
//    layouter.setSmartComponentLayoutEnabled(true);
//    layouter.setNodeOverlapsAllowed(false);
//    layouter.setConsiderNodeLabelsEnabled(true);
//    layouter.setCompactness(0.7d);
//    layouter.setNodeSizeAware(true);
    
    try {
      Graph2DLayoutExecutor l = new Graph2DLayoutExecutor();
      l.doLayout(graph, layouter);
    } catch (Exception e) {
      log.log(Level.WARNING, "Could not layout graph.", e);
    }
    
    // Restore group node sizes and insets
    glc.restoreAll();
  }
  

  /**
   * Layout the freshly added nodes.
   * @param newNodes nodes to layout
   */
  public void layoutNodeSubset(Set<Node> newNodes) {
    layoutNodeSubset(newNodes, false);
  }
  public void layoutNodeSubset(Set<Node> newNodes, boolean strict) {
    if (newNodes==null || newNodes.size()<1) return;
    graph.unselectAll();
    
    // Create a selection map that contains all new nodes.
    NodeMap dp = Selections.createSelectionNodeMap(graph);
    NodeMap dp2 = graph.createNodeMap();
    HierarchyManager hm = graph.getHierarchyManager();
    List<Node> resetLayout = new ArrayList<Node>();
    List<Node> otherNodes = new ArrayList<Node>();
    for (Node n : graph.getNodeArray()) {
      dp.setBool(n, newNodes.contains(n));
      // Do never layout contents of any group node.
      if (hm!=null && hm.isGroupNode(n)) {
        ((GroupNodeRealizer)graph.getRealizer(n)).updateAutoSizeBounds();
        dp2.set(n, SmartOrganicLayouter.GROUP_NODE_MODE_FIX_CONTENTS);
      }
      
      if (!newNodes.contains(n)){// && hm.getParentNode(n)==null && !hm.isGroupNode(n)) {
        if (n.degree()<1) { // NEW: only store orphans (and actually separate cliques...) 
          resetLayout.add(n);
        } else {
          otherNodes.add(n);
        }
      }
      
    }
    graph.addDataProvider(SmartOrganicLayouter.NODE_SUBSET_DATA, dp);
    graph.addDataProvider(SmartOrganicLayouter.GROUP_NODE_MODE_DATA, dp2);
    
    // Remember group node sizes and insets
    GroupLayoutConfigurator glc = new GroupLayoutConfigurator(graph);
    glc.prepareAll();
    
    // Create layouter and perform layout
    SmartOrganicLayouter layouter = new SmartOrganicLayouter();
    layouter.setScope(SmartOrganicLayouter.SCOPE_SUBSET);
    // If SmartComponentLayoutEnabled is true, all new nodes will
    // simply be put one above the other. If false, they are layouted
    // nicely, BUT orphans are being moved, too :-(
//    layouter.setSmartComponentLayoutEnabled(true);
    layouter.setSmartComponentLayoutEnabled(strict);
    layouter.setNodeOverlapsAllowed(newNodes.size()>75);
    layouter.setConsiderNodeLabelsEnabled(true);
    layouter.setCompactness(0.7d);
    layouter.setNodeSizeAware(true);
    
    
//    OrganicLayouter layouter = new OrganicLayouter();
//    layouter.setSphereOfAction(OrganicLayouter.ONLY_SELECTION);
    
    try {
      Graph2DLayoutExecutor l = new Graph2DLayoutExecutor();
      l.doLayout(graph, layouter);
    }catch (Exception e) {
      log.fine("Layout fallback on manual simple layout.");
      /* With LineNodeRealizer it is possible to get
       * java.lang.IllegalArgumentException: Graph contains nodes with zero width/height.
       * Please enlarge those nodes manually or by using LayoutStage y.layout.MinNodeSizeStage.
       */
      // Since we only have miRNAs here, place on top of first target
      try {
        for (Node n:newNodes) {
          NodeRealizer nr = n!=null?graph.getRealizer(n):null;
          if (nr==null) continue;
          EdgeCursor cursor = n.edges();
          if (cursor.ok()) {
            //cursor.toLast();
            Node target = cursor.edge().opposite(n);
            NodeRealizer targetRealizer = graph.getRealizer(target);
            nr.setCenter(targetRealizer.getCenterX(), targetRealizer.getCenterY()-nr.getHeight()*1.5);
          }
        }
      }catch (Exception e2) {
        e2.printStackTrace();
      }
    }
    
    // If we layout only a subset of nodes, the layout still moves
    // all other nodes by a constant offset! Undo this transformation
    DataMap nodeMap = descriptor2Map.get(GraphMLmaps.NODE_POSITION);
    if (nodeMap!=null) {    
      String splitBy = Pattern.quote("|");
      int anyOldX = 0, anyOldY = 0;
      int anyNewX = 0, anyNewY = 0;
      for (Node n: otherNodes) { // Breaks after the first node is found
        Object pos = nodeMap.get(n);
        if (pos==null) continue;
        // pos is always X|Y
        String[] XY = pos.toString().split(splitBy);
        NodeRealizer nr = graph.getRealizer(n);
        //log.finer(String.format("Resetting layout for %s from %s|%s to %s.", n, nr.getX(), nr.getY(), pos.toString()));
        anyOldX = (Integer.parseInt(XY[0]));
        anyOldY = (Integer.parseInt(XY[1]));
        anyNewX = (int) nr.getX();
        anyNewY = (int) nr.getY();
        break;
      }
      graph.moveNodes(graph.nodes(), anyOldX-anyNewX, anyOldY-anyNewY);
    }
    //---
    
    // Write initial position to node annotations
    for (Node n:newNodes) {
      String orgPos = calculateNodeOriginalPosition(n);
      if (orgPos!=null) {
        this.setInfo(n, GraphMLmaps.NODE_POSITION, orgPos);
      }
      
      // Paint above other nodes.
      graph.moveToLast(n);
    }
    
    // Restore group node sizes and insets
    glc.restoreAll();
    
    // remove selection map after layouting.
    graph.removeDataProvider(SmartOrganicLayouter.NODE_SUBSET_DATA);
    graph.removeDataProvider(SmartOrganicLayouter.GROUP_NODE_MODE_DATA);
    
    // Reset layout, because subset scope doesn't work correctly.
    // Not needed anymore, because of novel shift method (see above)
    // => Only needed for orphans
    // Unfortunately also for not-conntected subgraphs...
    resetLayout(Arrays.asList(graph.getNodeArray())); //resetLayout
  }
  
  /**
   * Returns the actual objects that are contained in <code>clickedObjects</code>.
   * @param clickedObjects
   * @param translateLabelHitsToObjects true to return the node for a click on a
   * nodelabel. Same for edges and labels.
   * @return
   */
  public static Set<Object> getHitEdgesAndNodes(HitInfo clickedObjects, boolean translateLabelHitsToObjects) {
    Set<Object> hitObjects = new HashSet<Object>();
    if (clickedObjects.hasHits()) {
      YCursor c = clickedObjects.allHits();
      while (c.ok()) {
        Object x = c.current();
        if (x instanceof Node) {
          hitObjects.add(x);
        } else if (x instanceof Edge) {
          hitObjects.add(x);
        } else if (translateLabelHitsToObjects && x instanceof EdgeLabel) {
          hitObjects.add(((EdgeLabel)x).getEdge());
        } else if (translateLabelHitsToObjects && x instanceof NodeLabel) {
          hitObjects.add(((NodeLabel)x).getNode());
        }
        c.next();
      }
    }
    
    return hitObjects;
  }
  
  
  /**
   * Actually, a faster and more generic version of {@link #getNodeInfoIDs(Node, String)}.
   * @param node_or_edge and {@link Node} or {@link Edge}
   * @param descriptor a descriptor for a {@link NodeMap} or {@link EdgeMap} in the current
   * {@link #graph}.
   * @return the requested information or null, if not available.
   */
  public Object getInfo(Object node_or_edge, String descriptor) {
    // Get the NodeMap from kegg 2 node.
    DataMap nodeMap = descriptor2Map.get(descriptor);
    if (nodeMap==null) {
      // This method is used without checking if the map has ever been set
      // before. So this warning is really more for debugging purposes.
      log.finest(String.format("Could not find Node to %s mapping.", (descriptor==null?"null":descriptor)));
      return null;
    }
    
    // return kegg id(s)
    Object id = nodeMap.get(node_or_edge);
    return id!=null?id:null;
  }
  
  /**
   * Set information to a map, contained in the current {@link #graph}. If the map given by
   * <code>descriptor</code> does not exist, it will be created.
   * @param node_or_edge any node or edge
   * @param descriptor descriptor of the map (e.g. "entrezIds", see {@link GraphMLmaps})
   * @param value value to set.
   */
  public void setInfo(Object node_or_edge, String descriptor, Object value) {
    // Get the NodeMap for the descriptor.
    DataMap nodeMap = getMap(descriptor);
    if (nodeMap==null && value==null) return; // all ok. Unset in a non-existing map.
    
    if (nodeMap==null) {
      // Create non-existing map automatically
      nodeMap = createMap(descriptor, (node_or_edge instanceof Node) );
      log.finer(String.format("Created not existing Node to %s mapping.", (descriptor==null?"null":descriptor)));
    }
    
    // set / unset Value
    nodeMap.set(node_or_edge, value);
  }
  

  /**
   * Resets the layout to the information stored in the nodes. Usually
   * this is the layout as given directly by kegg. Only affects X and Y
   * positions, NOT width and height of nodes.
   */
  public void resetLayout() {
    resetLayout(Arrays.asList(graph.getNodeArray()));
  }
  
  /**
   * Resets the layout to the information stored in the nodes. Usually
   * this is the layout as given directly by kegg. Only affects X and Y
   * positions, NOT width and height of nodes.
   * @param nodesToReset only reset these nodes.
   */
  public void resetLayout(Iterable<Node> nodesToReset) {
    DataMap nodeMap = descriptor2Map.get(GraphMLmaps.NODE_POSITION);
    if (nodeMap==null) {
      log.severe("Could not find original node positions.");
      return;
    }
    log.fine("Resetting layout for certain nodes.");
    
    String splitBy = Pattern.quote("|");
    for (Node n: nodesToReset) {
      Object pos = nodeMap.get(n);
      if (pos==null) continue;
      // pos is always X|Y
      String[] XY = pos.toString().split(splitBy);
      NodeRealizer nr = graph.getRealizer(n);
      //log.finer(String.format("Resetting layout for %s from %s|%s to %s.", n, nr.getX(), nr.getY(), pos.toString()));
      nr.setX(Integer.parseInt(XY[0]));
      nr.setY(Integer.parseInt(XY[1]));
    }
  }
  

  /**
   * @param nr
   * @return
   */
  private String calculateNodeOriginalPosition(Node n) {
    NodeRealizer nr = n!=null?graph.getRealizer(n):null;
    if (nr==null) return null;
    
    // Look for adjacent node with known position
    Node n2 = null;
    String n2Pos = null;
    int dividerPos = -1;
    for ( NodeCursor nc = n.neighbors(); nc.ok(); nc.next() ) {
      n2 = (Node)nc.current();
      Object p = this.getInfo(n2, GraphMLmaps.NODE_POSITION);
      if (p!=null && (dividerPos = p.toString().indexOf("|"))>0) {
        n2Pos = p.toString();
        break;
      }
    }
    // For Orphans, simply return current position...
    if (n2Pos==null) return (int) nr.getX() + "|" + (int) nr.getY();
    
    // Calculate relative coordinates
    NodeRealizer nro = graph.getRealizer(n2);
    int diffX = (int) (nro.getX() - nr.getX());
    int diffY = (int) (nro.getY() - nr.getY());
    
    int xOther = Integer.parseInt(n2Pos.substring(0, dividerPos));
    int yOther = Integer.parseInt(n2Pos.substring(dividerPos+1));
    
    
    return (xOther-diffX) + "|" + (yOther-diffY);
  }
  

  /**
   * @param descriptor e.g., "keggIds" or "entrezIds".
   * <p>See {@link GraphMLmaps} for a complete list.
   * @return map for the descriptor.
   */
  public DataMap getMap(String descriptor) {
    return descriptor2Map.get(descriptor);
  }
  
  /**
   * Get all maps registered in the graph.
   * @param descriptor
   * @return
   */
  public Collection<DataMap> getMaps() {
    return descriptor2Map.values();
  }
  
  /**
   * Create a new Node- or EdgeMap in the {@link #graph}.
   * @param descriptor descriptor for the map.
   * @param nodeMap if true, a {@link NodeMap} will be created. Else,
   * a {@link EdgeMap} will be created.
   * @return the created map.
   */
  public DataMap createMap(String descriptor, boolean nodeMap) {
    DataMap map;
    if (nodeMap) {
      map = graph.createNodeMap();
    } else {
      map = graph.createEdgeMap();
    }
    
    addMap(descriptor, map);
    
    return map;
  }
  

  /**
   * Registers a map WITHIN THESE TOOLS and linked to the {@link GenericDataMap}
   * <code>mapDescriptionMap</code>. Does not touch the graph itself!
   * 
   * @param descriptor
   * @param map
   */
  @SuppressWarnings("unchecked")
  public void addMap(String descriptor, DataMap map) {
    // Add info about map also to descriptors.
    GenericDataMap<DataMap, String> mapDescriptionMap = (GenericDataMap<DataMap, String>) graph.getDataProvider(Graph2Dwriter.mapDescription);
    mapDescriptionMap.set(map, descriptor);
    descriptor2Map.put(descriptor, map);
  }


  /**
   * @param n a node
   * @param descriptor e.g., "keggIds" or "entrezIds". See {@link GraphMLmaps} for a complete list.
   * @return the string associated with this node.
   */
  @SuppressWarnings("unchecked")
  public static String getNodeInfoIDs(Node n, String descriptor) {
    if (n==null || n.getGraph()==null) return null;
    Graph graph = n.getGraph();
    
    // Get the NodeMap from kegg 2 node.
    GenericDataMap<DataMap, String> mapDescriptionMap = (GenericDataMap<DataMap, String>) graph.getDataProvider(Graph2Dwriter.mapDescription);
    NodeMap nodeMap = null;
    if (mapDescriptionMap==null) return null;
    for (int i=0; i<graph.getRegisteredNodeMaps().length; i++) {
      NodeMap nm = graph.getRegisteredNodeMaps()[i];
      if (mapDescriptionMap.getV(nm).equals(descriptor)) {
        nodeMap = nm;
        break;
      }
    }
    if (nodeMap==null) {
      log.severe(String.format("Could not find Node to %s mapping.", (descriptor==null?"null":descriptor)));
      return null;
    }
    
    // return kegg id(s)
    Object id = nodeMap.get(n);
    return id!=null?id.toString():null;
  }
  

  /**
   * @param n
   * @return kegg ids, separated by a "," for the given node.
   */
  public static String getKeggIDs(Node n) {
    return getNodeInfoIDs(n, GraphMLmaps.NODE_KEGG_ID);
  }
  
  
}
