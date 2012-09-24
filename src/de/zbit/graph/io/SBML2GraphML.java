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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.sbml.jsbml.AbstractNamedSBase;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.NamedSBase;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.SimpleSpeciesReference;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.ext.SBasePlugin;
import org.sbml.jsbml.ext.groups.Group;
import org.sbml.jsbml.ext.groups.GroupModel;
import org.sbml.jsbml.ext.layout.BoundingBox;
import org.sbml.jsbml.ext.layout.CompartmentGlyph;
import org.sbml.jsbml.ext.layout.Dimensions;
import org.sbml.jsbml.ext.layout.ExtendedLayoutModel;
import org.sbml.jsbml.ext.layout.Layout;
import org.sbml.jsbml.ext.layout.LayoutConstants;
import org.sbml.jsbml.ext.layout.Point;
import org.sbml.jsbml.ext.layout.ReactionGlyph;
import org.sbml.jsbml.ext.layout.SpeciesGlyph;
import org.sbml.jsbml.ext.qual.Input;
import org.sbml.jsbml.ext.qual.Output;
import org.sbml.jsbml.ext.qual.QualConstant;
import org.sbml.jsbml.ext.qual.QualitativeModel;
import org.sbml.jsbml.ext.qual.Sign;
import org.sbml.jsbml.ext.qual.Transition;
import org.sbml.jsbml.xml.parsers.GroupsParser;

import y.base.Edge;
import y.base.Node;
import y.view.Arrow;
import y.view.EdgeRealizer;
import y.view.Graph2D;
import y.view.LineType;
import y.view.NodeRealizer;
import y.view.hierarchy.HierarchyManager;
import de.zbit.graph.io.def.SBGNVisualizationProperties;
import de.zbit.graph.sbgn.CloneMarker;
import de.zbit.graph.sbgn.CompartmentRealizer;
import de.zbit.graph.sbgn.ReactionNodeRealizer;
import de.zbit.math.MathUtils;
import de.zbit.util.Utils;
import de.zbit.util.objectwrapper.ValuePair;

/**
 * This class creates a {@link Graph2D} document (GraphML) to visualize
 * SBML documents in SBGN-style. Currently, in addition to the core, the
 * following extensions are supported:
 * <ul><li>Core</li><li>Qual</li><li>Layout</li></ul>
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class SBML2GraphML extends SB_2GraphML<SBMLDocument> {
  public static final Logger log = Logger.getLogger(SBML2GraphML.class.getName());
  
  /**
   * If false, shows the normal, quantitative SBML model.
   * If true, shows the qual model.
   */
  private boolean showQualModel = false;
  
  /**
   * This is NO option that can be set. It is used as internal
   * status variable to determine, if layout infomation is available!
   */
  private boolean useLayoutExtension;
  
  /**
   * If layout information is available (see {@link #useLayoutExtension})
   * this map, maps every id (of {@link org.sbml.jsbml.Species} or
   * {@link Reaction}) to the corresponding {@link BoundingBox}.
   * TODO: Actually, one species can have multiple glyphs. This is not considered here.
   */
  private Map<String, Collection<BoundingBox>> id2layoutMap = null;
  
  /**
   * This map helps to enhance the reactionNode-layout, after the graph
   * is completely built and layouted.
   */
  private Map<Reaction, ReactionNodeRealizer> reaction2node=null;
  
  /**
   * The namespace URI of the qual extension.
   */
  private final static String qualNamespace = QualConstant.namespaceURI;
  /**
   * The namespace URI of the layout extension.
   */
  private final static String layoutNamespace = LayoutConstants.namespaceURI;
  /**
   * The namespace URI of the layout extension.
   */
  private final static String groupNamespace = GroupsParser.namespaceURI;
  
  /**
   * map from reaction id to corresponding edge in graph
   */
  private Map<String, LinkedList<Edge>> id2edge = new HashMap<String, LinkedList<Edge>>();
  
  /**
   * 
   */
  public SBML2GraphML() {
    super();
  }
  
  public SBML2GraphML(boolean showQualModel) {
    super();
    this.showQualModel = showQualModel;
  }
  
  /**
   * Returns mapping from reaction ID to all edges of this reaction.
   * @return
   */
  public Map<String, LinkedList<Edge>> getId2edge(){
    return id2edge;
  }
  
  /**
   * @return {@code true} if the qual model is shown.
   */
  public boolean isQualModel() {
    return showQualModel;
  }
  
  /**
   * @param showQualModel {@code true} if the qual model should
   * be converted, instead of the metabolic model.
   */
  public void setShowQualModel(boolean showQualModel) {
    this.showQualModel = showQualModel;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.kegg.io.SB_2GraphML#createNodesAndEdges(java.lang.Object)
   */
  protected void createNodesAndEdges(SBMLDocument document) {
    
    // Check if we have anything to visualize
    if ((document == null) || !document.isSetModel()) {
    	return;
    }
    
    // Get list of species
    List<? extends AbstractNamedSBase> species;
    if (showQualModel) {
      SBasePlugin qm = document.getModel().getExtension(qualNamespace);
      if (qm!=null && qm instanceof QualitativeModel) {
        QualitativeModel q = (QualitativeModel) qm;
        if (!q.isSetListOfQualitativeSpecies()) return;
        species = q.getListOfQualitativeSpecies();
      } else {
        log.warning("SBMLDocument contains no qual-model.");
        return;
      }
    } else {
      species = document.getModel().getListOfSpecies();
    }
    
    // Eventually get the layout extension
    parseLayoutInformation(document, species);
    
    // Support for the groups extension
    species = parseGroupInformation(document, species);
    
    // Create the yFiles nodes
    addSpeciesToGraph(species, document);
    
    
    if (showQualModel) {
      /*
       * RELATIONS
       */
      QualitativeModel qm = ((QualitativeModel)document.getModel().getExtension(qualNamespace));
      for (Transition t : qm.getListOfTransitions()) {
        createRelation(t);
      }
    } else {
      /*
       * REACTIONS
       */
      reaction2node = createReactions(document);
    }
    
    createCompartments(document);
    
    return;
  }
  
  
  private void createCompartments(SBMLDocument doc) {
  	/*
     *  TODO: Add COMPARTMENTS
     */
    HierarchyManager hm = simpleGraph.getHierarchyManager();
    if (hm == null) {
      hm = new HierarchyManager(simpleGraph);
      simpleGraph.setHierarchyManager(hm);
    }
    Map<String, List<String>> mapOfChildren = new HashMap<String, List<String>>();
    if (doc.isSetModel()) {
    	Model model = doc.getModel();
    	SBasePlugin extension = model.getExtension(LayoutConstants.getNamespaceURI(model.getLevel(), model.getVersion()));
    	if (extension != null) {
    		ExtendedLayoutModel layoutModel = (ExtendedLayoutModel) extension;
    		int layoutIndex = 0;
    		if (layoutModel.isSetListOfLayouts()) {
    			Layout layout = layoutModel.getLayout(layoutIndex);
    			for (int i = 0; i < layout.getCompartmentGlyphCount(); i++) {
    				CompartmentGlyph cg = layout.getCompartmentGlyph(i);
    				NamedSBase nsb = cg.getNamedSBaseInstance();
    				if (nsb != null) {
    					nsb.putUserObject("GLYPH", cg);
    				}
    			}
    		}
    	}
    	// Find children
    	List<String> children;
    	for (Species s : model.getListOfSpecies()) {
    		Compartment c = s.getCompartmentInstance();
    		if ((c != null) && s.isSetId()) {
    			addChild(c.getId(), s.getId(), mapOfChildren);
    		}
    	}
    	for (Reaction r : model.getListOfReactions()) {
    		Compartment c = r.getCompartmentInstance();
    		if (c != null) {
    			if (r.isSetId()) {
    				addChild(c.getId(), r.getId(), mapOfChildren);
    			}
    		} else {
    			Set<Compartment> compartments = findCompartments(r.getListOfReactants());
    			compartments.addAll(findCompartments(r.getListOfModifiers()));
    			compartments.addAll(findCompartments(r.getListOfProducts()));
    			if (compartments.size() == 1) {
    				addChild(compartments.iterator().next().getId(), r.getId(), mapOfChildren);
    			}
    		}
    	}
    	
    	// Sort compartments from outside to inside
    	LinkedList<Compartment> compList = new LinkedList<Compartment>();
    	LinkedList<Compartment> temp = new LinkedList<Compartment>(model.getListOfCompartments());
    	while (temp.size() > 0) {
    		Iterator<Compartment> it = temp.iterator();
    		while (it.hasNext()) {
    			Compartment curr = it.next();
    			if (!curr.isSetOutside()) {
    				compList.addFirst(curr);
    				it.remove();
    			} else {
						Compartment outside = curr.getOutsideInstance();
						if (outside == null) {
							compList.add(curr);
							it.remove();
						} else {
							int pi = compList.indexOf(outside);
							if (pi >= 0) { // contains
								compList.addLast(curr);
								it.remove();
							} else if (!temp.contains(outside)) {
								compList.add(curr);
								it.remove();
							}
						}
    			}
    		}
    	}
    	temp = null;
    	
    	for (int i = 0; i < compList.size(); i++) {
    		Compartment c = compList.get(i);
    		String id = c.getId();
    		double x = Double.NaN, y = Double.NaN, w = 25d, h = 25d;
    		if (c.containsUserObjectKey("GLYPH") || c.isSetOutside()) {
    			if (c.containsUserObjectKey("GLYPH")) {
    				CompartmentGlyph cg = (CompartmentGlyph) c.getUserObject("GLYPH");
    				if (cg.isSetBoundingBox()) {
    					BoundingBox bb = cg.getBoundingBox();
    					if (bb.isSetPosition()) {
    						Point p = bb.getPosition();
    						x = p.getX();
    						y = p.getY();
    					}
    					if (bb.isSetDimensions()) {
    						Dimensions d = bb.getDimensions();
    						w = d.getWidth();
    						h = d.getHeight();
    					}
    				}
    				id = cg.getId();
    			} else {
    				id = c.getId();
    			}
    			Node n = createNode(id, c.isSetName() ? c.getName() : c.getId(), SBO.getCompartment(), x, y, w, h);
    			CompartmentRealizer nr = new CompartmentRealizer();
    			nr.setFillColor(null);
    			nr.setInterFillColor(SBGNVisualizationProperties.getFillColor(SBO.getCompartment()));
    			nr.setLineColor(SBGNVisualizationProperties.getLineColor(SBO.getCompartment()));
    			simpleGraph.setRealizer(n, nr);
    	    hm.convertToGroupNode(n);
    	    if (c.isSetOutsideInstance()) {
    	    	Node outside = id2node.get(c.getOutside());
    	    	if (outside != null) {
        	    simpleGraph.getHierarchyManager().setParentNode(n, outside);
    	    	}
    	    }
    	    children = mapOfChildren.get(c.getId());
    	    if ((children != null) && (children.size() > 0)) {
    	    	addChildren(n, children.toArray(new String[] {}));
    	    }
    		}
    	}
    }
	}

	/**
   * Fix ReactionNode nodes (determines 90° rotatable node orientation).
   * @param reaction2node
   */
  @Override
  public void improveReactionNodeLayout() {
    if (reaction2node == null) {
    	return; // QUAL-Model
    }
    for (Map.Entry<Reaction,ReactionNodeRealizer> en : reaction2node.entrySet()) {
      Set<Node> reactants = new HashSet<Node>();
      Set<Node> products = new HashSet<Node>();
      Set<Node> modifier = new HashSet<Node>();
      for (SimpleSpeciesReference sr : en.getKey().getListOfReactants()) {
        reactants.add((Node) id2node.get(sr.getSpecies()));
      }
      for (SimpleSpeciesReference sr : en.getKey().getListOfProducts()) {
        products.add((Node) id2node.get(sr.getSpecies()));
      }
      for (SimpleSpeciesReference sr : en.getKey().getListOfModifiers()) {
        modifier.add((Node) id2node.get(sr.getSpecies()));
      }
      en.getValue().fixLayout(reactants, products, modifier);      
    }
  }
  
  
  
  /**
   * Creates edges and pseude-reaction nodes (using {@link ReactionNodeRealizer})
   * and enzymes (also cloning them), etc. to visualize
   * metabolic reactions.
   * @param document
   * @return a map from every SBML-{@link Reaction} to the corresponding
   * {@link ReactionNodeRealizer} of the reaction node (small box).
   */
  private Map<Reaction, ReactionNodeRealizer> createReactions(SBMLDocument document) {
    Map<Reaction, ReactionNodeRealizer> reaction2node = new HashMap<Reaction, ReactionNodeRealizer>();
    
    // Add all reactions to the graph
    Set<Node> usedEnzymes = new HashSet<Node>();
    
    for (Reaction r : document.getModel().getListOfReactions()) {
      // List all edges corresponding to the same Reaction
      LinkedList<Edge> listOfEdges = new LinkedList<Edge>();
      
      if (r.isSetListOfReactants() && r.isSetListOfProducts()) {
        
        // Create the reaction node
        NodeRealizer nr = new ReactionNodeRealizer();
        reaction2node .put(r, (ReactionNodeRealizer) nr);
        Node rNode = simpleGraph.createNode(nr);
        GraphElement2SBid.put(rNode, r.getId());
        id2node.put(r.getId(), rNode);
        
        // Get information from the layout extension
        double x = Double.NaN;
        double y = Double.NaN;
        if (useLayoutExtension) {
          // reactions can not have mutliple glyphs... take first!
          Collection<BoundingBox> layout = id2layoutMap.get(r.getId());
          BoundingBox g = (layout != null) && (layout.size() > 0) ? layout.iterator().next() : null;
          if (g != null) {
            if (g.isSetDimensions()) {
              nr.setWidth(g.getDimensions().getWidth());
              nr.setHeight(g.getDimensions().getHeight());
            }
            if (g.isSetPosition()) {
              // Ignore 0|0 positions. They're due to default values
              if ((g.getPosition().getX() != 0d) || (g.getPosition().getY() != 0d)) {
                x = g.getPosition().getX();
                y = g.getPosition().getY();
              }
            }
          }
        }
        
        // Adding them to the to-be-layouted list will lead to position
        // them as freely as possible by yFiles, which is WRONG for these
        // nodes. Actually, they should always be between products and
        // substrate... needing special layouting!
        //unlayoutedNodes.add(rNode);
        if (Double.isNaN(x) || Double.isNaN(y)) {
          ValuePair<Double, Double> xy = calculateMeanCoords(r.getListOfReactants(), r.getListOfProducts(), id2node, simpleGraph);
          x = (xy.getA());
          y = (xy.getB());
        }
        nr.setCenterX(x);
        nr.setCenterY(y);
        
        // TODO: Add stoichiometry to edges (docked to corresponding node):
        // subtrate on substrate node
        // product on product node
        
        // Add edges to the reaction node
        for (SpeciesReference sr : r.getListOfReactants()) {
          Node source = id2node.get(sr.getSpecies());
          if (source!=null) {
            Edge e = simpleGraph.createEdge(source, rNode);
            GraphElement2SBid.put(e, r.getId());
            listOfEdges.add(e);
            EdgeRealizer er = simpleGraph.getRealizer(e);
            if (r.isReversible()) {
              er.setSourceArrow(Arrow.STANDARD);
            } else {
              er.setSourceArrow(Arrow.NONE);
            }
            er.setArrow(Arrow.NONE);
            
            
          }
        }
        
        for (SpeciesReference sr : r.getListOfProducts()) {
          Node target = id2node.get(sr.getSpecies());
          if (target!=null) {
            Edge e = simpleGraph.createEdge(rNode, target);
            GraphElement2SBid.put(e, r.getId());
            listOfEdges.add(e);
            EdgeRealizer er = simpleGraph.getRealizer(e);
            er.setArrow(Arrow.STANDARD);
            er.setSourceArrow(Arrow.NONE);
          }
        }
        
        for (ModifierSpeciesReference sr : r.getListOfModifiers()) {
          Node source = id2node.get(sr.getSpecies());
          if (source!=null) {
            if (splitEnzymesToOnlyOccurOnceInAnyReaction) {
              // Split enzymes to have a nicer visualization. 
              if (usedEnzymes.contains(source)) {
                Node oldSource = source;
                source = oldSource.createCopy(simpleGraph);
                NodeRealizer realizer = simpleGraph.getRealizer(source);
                if (realizer instanceof CloneMarker) {
                  ((CloneMarker) realizer).setNodeIsCloned(true);
                  ((CloneMarker) simpleGraph.getRealizer(oldSource)).setNodeIsCloned(true);
                } else {
                  log.warning("Can not setup clone marker on " + realizer.getClass().getSimpleName());
                }
                unlayoutedNodes.add(source);
                GraphElement2SBid.put(source, sr.getSpecies());
              }
            }
            Edge e = simpleGraph.createEdge(source, rNode);
            GraphElement2SBid.put(e, r.getId());
            listOfEdges.add(e);
            EdgeRealizer er = simpleGraph.getRealizer(e);
            er.setArrow(Arrow.TRANSPARENT_CIRCLE);
            er.setLineType(LineType.LINE_1);
            er.setSourceArrow(Arrow.NONE);
            usedEnzymes.add(source);
          }
        }
        
      }
      
      id2edge.put(r.getId(), listOfEdges);
    }
    
    return reaction2node;
  }
  
  
  /**
   * @param document
   * @param species
   */
  private void addSpeciesToGraph(List<? extends AbstractNamedSBase> species, SBMLDocument document) {
    // Create a list of species with enzymatic activity.
    Set<String> enzymeSpeciesIDs = document != null ? getListOfEnzymes(document) : null;
    
    for (AbstractNamedSBase s : species) {
      // Get the SBO-term (defining the shape and color)
      int sboTerm = 0;
      /*
       * IMPORTANT ReactionModifiers (referencing to species)
       * should get the enzyme shape, even if the species is a gene!
       * The picture will otherwise show genes, catalyzing reactions
       * instead of enzymes!!!!!
       */
      if ((enzymeSpeciesIDs != null) && enzymeSpeciesIDs.contains(s.getId())) {
        sboTerm = SBO.getMacromolecule();
      } else if (s.isSetSBOTerm()) {
        sboTerm = s.getSBOTerm();
      }
      
      
      // Eventually create multiple copies
      List<BoundingBox> layouts = null;
      boolean multipleCopiesAvailable=false;
      
      if (useLayoutExtension) {
        Collection<BoundingBox> layoutsTemp = id2layoutMap.get(s.getId());
        multipleCopiesAvailable = layoutsTemp!=null && layoutsTemp.size()>1;
        /*
         * Feature Deactivated until problems are resolved:
         * - id2node and GraphElement2SBid maps (in SB_2GraphML) only take one node
         * for each species
         * - when visualization reactions/ relations, we don't know which of the
         * multiple glyphs to use!
         * 
         * If these two problems are completely solved, we can reactive
         * multiple glyphs for one species.
         */
        multipleCopiesAvailable = false;
        if (layoutsTemp != null) {
          layouts = new ArrayList<BoundingBox>(layoutsTemp); // Convert to list
          
        }
      }
      
      for (int aktNode = 0; aktNode < (multipleCopiesAvailable?layouts.size():1); aktNode++) {
        
        // Initialize default layout variables
        double x = Double.NaN;
        double y = Double.NaN;
        double w = 46;
        double h = 17;
        
        // Get information from the layout extension
        if (useLayoutExtension && layouts!=null) {
          BoundingBox g = layouts.get(aktNode);
          if (g!=null) {
            if (g.isSetDimensions()) {
              w = g.getDimensions().getWidth();
              h = g.getDimensions().getHeight();
            }
            if (g.isSetPosition()) {
              // Ignore 0|0 positions. They're due to default values
              if ((g.getPosition().getX() != 0d) || g.getPosition().getY() != 0d) {
                x = g.getPosition().getX();
                y = g.getPosition().getY();
              }
            }
          }
          
        }
        
        // Now create the real node
        Node n;
        if (s instanceof Group && ((Group) s).isSetListOfMembers()) {
          // Create a group node (SBML-groups extension)
          int size = ((Group) s).getListOfMembers().size();
          String[] groupMembers = new String[size];
          for (int i = 0; i < size; i++) {
            groupMembers[i] = ((Group) s).getMember(i).getSymbol();
          }
          n = createGroupNode(s.getId(), s.getName(), sboTerm, x, y, w, h, groupMembers);
          
        } else {
          // A simple, normal node.
          n = createNode(s.getId(), s.isSetName() ? s.getName() : s.getId(), sboTerm, x, y, w, h);
        }
        
        // Eventually setup the clone marker
        if (multipleCopiesAvailable) {
          NodeRealizer nr = simpleGraph.getRealizer(n);
          if (nr instanceof CloneMarker) {
            ((CloneMarker) nr).setNodeIsCloned(true);
          }
        }
      }
    }
  }
  
  /**
   * 
   * @param listOfReactionParticipants
   * @return
   */
  private Set<Compartment> findCompartments(ListOf<? extends SimpleSpeciesReference> listOfReactionParticipants) {
  	Set<Compartment> setOfComp = new HashSet<Compartment>();
		for (int i = 0; i < listOfReactionParticipants.size(); i++) {
			SimpleSpeciesReference ref = listOfReactionParticipants.get(i);
			Species s = ref.getSpeciesInstance();
			if (s != null) {
				Compartment curr = s.getCompartmentInstance();
				if (curr != null) {
					setOfComp.add(curr);
				}
			}
		}
		return setOfComp;
	}

	/**
   * 
   * @param compartmentID
   * @param elementID
   * @param mapOfChildren
   */
  private void addChild(String compartmentID, String elementID, Map<String, List<String>> mapOfChildren) {
  	List<String> children = mapOfChildren.get(compartmentID);
		if (children == null) {
			children = new ArrayList<String>();
			children.add(elementID);
			mapOfChildren.put(compartmentID, children);
		} else {
			children.add(elementID);
		}
	}

	/**
   * Parses the layout information from the given {@code document}.
   * This will set the {@link #useLayoutExtension} and
   * {@link #id2layoutMap} variables.
   * @param document
   */
  private void parseLayoutInformation(SBMLDocument document) {
    parseLayoutInformation(document, null);
  }
  
  /**
   * Parses the layout information from the given {@code document}.
   * This will set the {@link #useLayoutExtension} and
   * {@link #id2layoutMap} variables.
   * @param document
   * @param species list of all species to draw. This helps identifying
   * the correct layout.
   */
  private void parseLayoutInformation(SBMLDocument document, List<? extends AbstractNamedSBase> species) {
    SBasePlugin layoutExtension = document.getModel().getExtension(layoutNamespace);
    useLayoutExtension = layoutExtension!=null;
    id2layoutMap = null;
    if (useLayoutExtension) {
      if (((ExtendedLayoutModel)layoutExtension).isSetListOfLayouts()) {
        // TODO: For generic releases, it would be nice to have a JList
        // that let's the user choose the layout.
        
        // In many applications (KEGGtranslator, SBVC, etc.) there is one layout
        // for core and one for qual => identify the required one.
        ListOf<Layout> layouts = ((ExtendedLayoutModel)layoutExtension).getListOfLayouts();
        Layout l = layouts.iterator().next(); // First one
        for (int i=0; i<layouts.size(); i++) {
          Layout toTest = ((ExtendedLayoutModel)layoutExtension).getLayout(i);
          if (toTest.isSetListOfSpeciesGlyphs()) {
            String anyID = toTest.getListOfSpeciesGlyphs().iterator().next().getSpecies();
            if (species!=null && speciesListContainsID(species, anyID)) {
              // we have a corresponding layout
              l = toTest;
              break;              
            }
            if (!showQualModel && document.getModel().getSpecies(anyID)!=null) {
              // we have a core layout
              l = toTest;
              break;
            } else {
              // TODO: Implement the same getSpecies check also for QUAL.
            }
          }
          
        }
        
        // Parse layout and establish internal maps
        id2layoutMap = new HashMap<String, Collection<BoundingBox>>();
        for (SpeciesGlyph sg: l.getListOfSpeciesGlyphs()) {
          if (sg.isSetBoundingBox()) {
            Utils.addToMapOfSets(id2layoutMap, sg.getSpecies(), sg.getBoundingBox());
          }
        }
        for (ReactionGlyph sg: l.getListOfReactionGlyphs()) {
          if (sg.isSetBoundingBox()) {
            Utils.addToMapOfSets(id2layoutMap, sg.getReaction(), sg.getBoundingBox());
          }
        }
        useLayoutExtension = id2layoutMap.size()>0;
      } else {
        useLayoutExtension = false;
      }
    }
  }
  
  /**
   * Parses the groups information from the given {@code document}.
   * This will set the {@link #useLayoutExtension} and
   * {@link #id2layoutMap} variables.
   * @param document
   * @param species list of all species to draw. This helps identifying
   * the correct layout.
   * @return 
   */
  private List<? extends AbstractNamedSBase> parseGroupInformation(SBMLDocument document, List<? extends AbstractNamedSBase> species) {
    SBasePlugin groupExtension = document.getModel().getExtension(groupNamespace);
    boolean useGroupExtension = groupExtension!=null;
    if (useGroupExtension) {
      if (((GroupModel)groupExtension).isSetListOfGroups()) {
        ListOf<Group> groups = ((GroupModel)groupExtension).getListOfGroups();
        
        // Add all groups to our list
        List<AbstractNamedSBase> speciesNew = new ArrayList<AbstractNamedSBase>(species);
        // It is important to add all groups TO THE END of the list.
        speciesNew.addAll(groups);
        
        return speciesNew;
      } else {
        useGroupExtension = false;
      }
    }
    
    return species;
  }
  
  
  /**
   * Look for a species with a specific id.
   * <p>Note: this is a standard-slow iterative approach.</p>
   * @param species
   * @param anyID
   * @return {@code true} if {@code species} contains
   * any {@link AbstractNamedSBase} with ID {@code anyID}.
   */
  private boolean speciesListContainsID(
    List<? extends AbstractNamedSBase> species, Object anyID) {
    for(AbstractNamedSBase s : species) {
      if (s.getId().equals(anyID)) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * @param t
   */
  private void createRelation(Transition t) {
    // TODO: Actually, instead of making n*m edges
    // make n edges to a fake-node, that branches off to m-nodes
    // (correct SBML/SBGN-Style).
    if (!t.isSetListOfInputs() || !t.isSetListOfOutputs()) {
      return;
    }
    
    for (Input i: t.getListOfInputs()) {
      for (Output o: t.getListOfOutputs()) {
        
        // You should use a predefined map here, since the source
        // or target could also be a group!
        Node source = id2node.get(i.getQualitativeSpecies());
        Node target = id2node.get(o.getQualitativeSpecies());
        if (source==null || target==null) continue;
        
        Edge e = simpleGraph.createEdge(source, target);
        GraphElement2SBid.put(e, t.getId());
        
        if (i.isSetSign()) {
          if (i.getSign().equals(Sign.positive)) {
            simpleGraph.getRealizer(e).setArrow(Arrow.STANDARD);
          } else if (i.getSign().equals(Sign.negative)) {
            simpleGraph.getRealizer(e).setArrow(Arrow.T_SHAPE);
          } else if (i.getSign().equals(Sign.dual)) {
            // Diamond is used in SBGN-PD as "modulation".
            simpleGraph.getRealizer(e).setArrow(Arrow.DIAMOND);
          }
        }
        
      }
    }
  }
  
  
  
  /* (non-Javadoc)
   * @see de.zbit.kegg.io.SB_2GraphML#isAnyLayoutInformationAvailable()
   */
  @Override
  protected boolean isAnyLayoutInformationAvailable() {
    return useLayoutExtension;
  }
  
  
  
  /**
   * First, calculates the mean of all x/y coordinates of all
   * products and substrates separately. Then, calculates
   * the mean of those means. The result should be X/Y
   * coordinates that are perfectly between the given 
   * products and substrates.
   * @param <T>
   * @param listOfSubstrates
   * @param listOfProducts
   * @param species2node
   * @param simpleGraph
   * @return
   */
  protected <T extends SimpleSpeciesReference> ValuePair<Double, Double> calculateMeanCoords(Iterable<T> listOfSubstrates,
    Iterable<T> listOfProducts, Map<String, Node> species2node, Graph2D simpleGraph) {
    
    ValuePair<Double, Double> subs = calculateMeanCoords(listOfSubstrates, species2node, simpleGraph);
    ValuePair<Double, Double> prod = calculateMeanCoords(listOfProducts, species2node, simpleGraph);
    
    return new ValuePair<Double, Double>(MathUtils.mean(subs.getA(), prod.getA()), MathUtils.mean(subs.getB(), prod.getB()));
  }
  
  /**
   * Calculates the mean x and y coordinates.
   * @param <T>
   * @param listOfReactants
   * @param species2node
   * @param simpleGraph
   * @return
   */
  private <T extends SimpleSpeciesReference> ValuePair<Double, Double> calculateMeanCoords(Iterable<T> listOfReactants,
    Map<String, Node> species2node, Graph2D simpleGraph) {
    List<Double> xes = new ArrayList<Double>();
    List<Double> yes = new ArrayList<Double>();
    
    Iterator<T> it = listOfReactants.iterator();
    while (it.hasNext()) {
      T s = it.next();
      if (s.isSetSpecies()) {
        Node n = species2node.get(s.getSpecies());
        if (n!=null) {
          NodeRealizer nr = simpleGraph.getRealizer(n);
          xes.add(nr.getX());
          yes.add(nr.getY());
        }
      }
    }
    
    return new ValuePair<Double, Double>(MathUtils.mean(xes), MathUtils.mean(yes));
  }
  
  
  /**
   * Create a list of species (by identifier) that 
   * show an enzymatic activity. 
   * @param document
   * @return List of species identifiers that are in the
   * list list of {@link ModifierSpeciesReference}s.
   */
  public static Set<String> getListOfEnzymes(SBMLDocument document) {
    // Create a list of all species IDs that act as enzymes.
    Set<String> enzymeSpeciesIDs = new HashSet<String>();
    {
      Set<ModifierSpeciesReference> ref = document.getModel().getModifierSpeciesReferences();
      for (ModifierSpeciesReference msr : ref) {
        if ( msr.isSetSpecies() && msr.getSpecies().length()>0) {
          enzymeSpeciesIDs.add(msr.getSpecies());
        }
      }
    }
    return enzymeSpeciesIDs;
  }
  
}
