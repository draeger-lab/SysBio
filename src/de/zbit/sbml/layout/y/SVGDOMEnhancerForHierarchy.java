/*
 * $Id: SVGDOMEnhancerForHierarchy.java 1388 2016-01-24 05:16:09Z draeger $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SysBio/trunk/examples/de/zbit/sbml/layout/y/SVGDOMEnhancerForHierarchy.java $
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

import java.util.HashMap;
import java.util.Map;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.w3c.dom.Element;

import y.base.Edge;
import y.base.Node;
import yext.svg.io.SVGDOMEnhancer;

/**
 * 
 * @version $Rev: 1388 $
 */
public class SVGDOMEnhancerForHierarchy extends SVGDOMEnhancer {
  
  private Model model;
  private Map<String, Reaction> rxnNameToReaction;
  
  public SVGDOMEnhancerForHierarchy(Model model) {
    super();
    this.model = model;
    rxnNameToReaction = new HashMap<String, Reaction>();
    
    for (Reaction rxn : model.getListOfReactions()) {
      rxnNameToReaction.put(rxn.getName(), rxn);
    }
    
  }
  
  /* (non-Javadoc)
   * @see yext.svg.io.SVGDOMEnhancer#nodeAddedToDOM(y.base.Node, org.w3c.dom.Element)
   */
  @Override
  protected void nodeAddedToDOM(Node n1, Element element) {
    
    Reaction reaction;
    if (rxnNameToReaction.get(n1.toString()) != null) {
      reaction = rxnNameToReaction.get(n1.toString());
      element.setAttribute("reaction", reaction.getId());
    }
    
    super.nodeAddedToDOM(n1, element);
  }
  
  /* (non-Javadoc)
   * @see yext.svg.io.SVGDOMEnhancer#edgeAddedToDOM(y.base.Edge, org.w3c.dom.Element)
   */
  @Override
  protected void edgeAddedToDOM(Edge edge, Element element) {
    
    Node n1 = edge.source();
    Node n2 = edge.target();
    
    Reaction reaction;
    Node n;
    
    if (rxnNameToReaction.get(n1.toString()) != null) {
      reaction = rxnNameToReaction.get(n1.toString());
      n = n1;
      element.setAttribute("reaction", reaction.getId());
    } else if (rxnNameToReaction.get(n2.toString()) != null) {
      reaction = rxnNameToReaction.get(n2.toString());
      n = n2;
      element.setAttribute("reaction", reaction.getId());
    } else {
      System.out.println("No reaction...?");
      element.setAttribute("reaction", "NoRxn");
    }
    
    super.edgeAddedToDOM(edge, element);
  }
  
}