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
package de.zbit.sbml.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.AbstractSBase;
import org.sbml.jsbml.Annotation;
import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.CVTerm.Qualifier;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;

public class ModelMerging {
  /**
   * 
   * @param modelFiles
   * @throws XMLStreamException
   * @throws IOException
   * @throws SBMLException
   */
  public static void mergeModels(String[] modelFiles)
    throws XMLStreamException, IOException, SBMLException {
    if (modelFiles.length == 0) { return; }
    
    SBMLDocument[] docs = new SBMLDocument[modelFiles.length];
    for (int i = 0; i != modelFiles.length; i++) {
      docs[i] = (new SBMLReader()).readSBML(modelFiles[i]);
    }
    
    int level = docs[0].getLevel();
    int version = docs[0].getVersion();
    SBMLDocument newDoc = new SBMLDocument(level, version);
    newDoc.createModel("newModel");
    
    Map<String, List<AbstractSBase>> compartmentAnnotationMap = new HashMap<String, List<AbstractSBase>>();
    Map<String, List<AbstractSBase>> speciesAnnotationMap = new HashMap<String, List<AbstractSBase>>();
    Map<String, List<AbstractSBase>> reactionAnnotationMap = new HashMap<String, List<AbstractSBase>>();
    
    for (int j = 0; j < docs.length; j++) {
      Model currentModel = docs[j].getModel();
      
      //compartments
      for (int n = 0; n != currentModel.getCompartmentCount(); n++) {
        Compartment c = currentModel.getCompartment(n);
        List<CVTerm> cvTerms = c.getCVTerms();
        c.setLevel(level);
        c.setVersion(version);
        c.setId("C" + j + "_" + n + "_" + c.getId());
        c.setMetaId(c.getId());
        
        newDoc.getModel().addCompartment(c);
        
        if (cvTerms.size() != 0) {
          c.setAnnotation(new Annotation());
        }
        
        for (CVTerm current : cvTerms) {
          c.addCVTerm(current);
          if (current.getBiologicalQualifierType().equals(Qualifier.BQB_IS)) {
            for (String s : current.getResources()) {
              List<AbstractSBase> list = compartmentAnnotationMap.get(s);
              if (list == null) {
                list = new LinkedList<AbstractSBase>();
              }
              list.add(c);
              compartmentAnnotationMap.put(s, list);
            }
          }
        }
        
      }
      
      //species
      for (int n = 0; n != currentModel.getSpeciesCount(); n++) {
        Species sp = currentModel.getSpecies(n);
        List<CVTerm> cvTerms = sp.getCVTerms();
        sp.setLevel(level);
        sp.setVersion(version);
        sp.setId("S" + j + "_" + n + "_" + sp.getId());
        sp.setMetaId(sp.getId());
        newDoc.getModel().addSpecies(sp);
        
        if (cvTerms.size() != 0) {
          sp.setAnnotation(new Annotation());
        }
        
        for (CVTerm current : cvTerms) {
          sp.addCVTerm(current);
          if (current.getBiologicalQualifierType().equals(Qualifier.BQB_IS)) {
            
            for (String s : current.getResources()) {
              List<AbstractSBase> list = speciesAnnotationMap.get(s);
              if (list == null) {
                list = new LinkedList<AbstractSBase>();
              }
              list.add(sp);
              speciesAnnotationMap.put(s, list);
            }
          }
        }
        
      }
      
      //reactions
      for (int n = 0; n != currentModel.getReactionCount(); n++) {
        Reaction r = currentModel.getReaction(n);
        List<CVTerm> cvTerms = r.getCVTerms();
        r.setLevel(level);
        r.setVersion(version);
        r.setId("C" + j + "_" + n + "_" + r.getId());
        r.setMetaId(r.getId());
        
        newDoc.getModel().addReaction(r);
        if (cvTerms.size() != 0) {
          r.setAnnotation(new Annotation());
        }
        
        for (CVTerm current : cvTerms) {
          r.addCVTerm(current);
          if (current.getBiologicalQualifierType().equals(Qualifier.BQB_IS)) {
            
            for (String s : current.getResources()) {
              List<AbstractSBase> list = reactionAnnotationMap.get(s);
              if (list == null) {
                list = new LinkedList<AbstractSBase>();
              }
              list.add(r);
              reactionAnnotationMap.put(s, list);
            }
          }
        }
      }
      
    }
    
    //remove elements with the same annotation and SBOTerm
    
    //species
    removeDuplicateElements(newDoc, speciesAnnotationMap);
    
    //reactions
    removeDuplicateElements(newDoc, reactionAnnotationMap);
    SBMLWriter w = new SBMLWriter();
    w.write(newDoc, "files/mergedModel.xml");
    
  }
  
  /**
   * 
   * @param modelFiles
   * @throws XMLStreamException
   * @throws IOException
   * @throws SBMLException
   */
  public static void mergeCompartments(String modelFile)
    throws XMLStreamException, IOException, SBMLException {
    
    SBMLDocument doc = (new SBMLReader()).readSBML(modelFile);
    
    SBMLDocument newDoc = new SBMLDocument(doc.getLevel(), doc.getVersion());
    newDoc.createModel("newModel");
    
    Map<String, List<AbstractSBase>> speciesAnnotationMap = new HashMap<String, List<AbstractSBase>>();
    Map<String, List<AbstractSBase>> reactionAnnotationMap = new HashMap<String, List<AbstractSBase>>();
    
    Model model = doc.getModel();
    Map<String, String> ids = new HashMap<String, String>();
    
    Compartment c = newDoc.getModel().createCompartment("compartment");
    //species
    if (model.isSetListOfSpecies()) {
    	for (Species sp : model.getListOfSpecies()) {
    		
    		List<CVTerm> cvTerms = sp.getCVTerms();
    		String name = sp.getName();
    		String oldId = sp.getId();
    		sp.setId("_"
    				+ name.replace(",", "_").replace("(", "_").replace(")", "_")
    				.replace("-", "_").replace("+", "plus"));
    		sp.setMetaId(sp.getId());
    		sp.setCompartment(c);
    		ids.put(oldId, sp.getId());
    		newDoc.getModel().addSpecies(sp);
    		if (cvTerms.size() != 0) {
    			sp.setAnnotation(new Annotation());
    		}
    		
    		for (CVTerm current : cvTerms) {
    			sp.addCVTerm(current);
    			if (current.getBiologicalQualifierType().equals(Qualifier.BQB_IS)) {
    				
    				for (String s : current.getResources()) {
    					List<AbstractSBase> list = speciesAnnotationMap.get(s);
    					if (list == null) {
    						list = new LinkedList<AbstractSBase>();
    					}
    					list.add(sp);
    					speciesAnnotationMap.put(s, list);
    				}
    			}
    		}
    		
    	}
    }
    //reactions
    if (model.isSetListOfReactions()) {
    	for (Reaction r : model.getListOfReactions()) {
    		if (r.getSBOTerm() == 185) {
    			continue;
    		}
    		if (r.isSetListOfProducts()) {
    			for (SpeciesReference sr : r.getListOfProducts()) {
    				String newSpeciesId = ids.get(sr.getSpecies());
    				sr.setSpecies(newSpeciesId);
    			}
    		}
    		if (r.isSetListOfModifiers()) {
    			for (ModifierSpeciesReference sr : r.getListOfModifiers()) {
    				String newSpeciesId = ids.get(sr.getSpecies());
    				sr.setSpecies(newSpeciesId);
    			}
    		}
    		if (r.isSetListOfReactants()) {
    			for (SpeciesReference sr : r.getListOfReactants()) {
    				String newSpeciesId = ids.get(sr.getSpecies());
    				sr.setSpecies(newSpeciesId);
    			}
    		}
    		
    		newDoc.getModel().addReaction(r);
    		if (r.getCVTermCount() > 0) {
    			r.setAnnotation(new Annotation());
    			
    			for (CVTerm current : r.getCVTerms()) {
    				r.addCVTerm(current);
    				if (current.getBiologicalQualifierType().equals(Qualifier.BQB_IS)) {
    					
    					for (String s : current.getResources()) {
    						if (s.contains("urn:miriam:kegg.reaction")) {
    							List<AbstractSBase> list = reactionAnnotationMap.get(s);
    							if (list == null) {
    								list = new LinkedList<AbstractSBase>();
    							}
    							list.add(r);
    							reactionAnnotationMap.put(s, list);
    						}
    					}
    				}
    			}
    		}
    	}
    }
    //remove elements with the same annotation and SBOTerm
    
    //species
    removeDuplicateElements(newDoc, speciesAnnotationMap);
    
    //reactions
    removeDuplicateElements(newDoc, reactionAnnotationMap);
    SBMLWriter w = new SBMLWriter();
    w.write(newDoc, "files/oneCompartmentModel.xml");
    
  }
  
  private static void removeDuplicateElements(SBMLDocument doc,
    Map<String, List<AbstractSBase>> annotationMap) {
    
    for (String term : annotationMap.keySet()) {
      List<AbstractSBase> elements = annotationMap.get(term);
      if (elements.size() > 1) {
        for (int n1 = 0; n1 != elements.size(); n1++) {
          for (int n2 = n1 + 1; n2 != elements.size(); n2++) {
            if (checkEquality(elements.get(n1), elements.get(n2))) {
              merge(elements.get(n1), elements.get(n2), doc);
            }
          }
        }
      }
    }
  }
  
  private static void merge(AbstractSBase abstractSBase,
    AbstractSBase abstractSBase2, SBMLDocument doc) {
  	Model model = doc.getModel();
    if ((abstractSBase instanceof Reaction)
        && (abstractSBase2 instanceof Reaction)) {
      Reaction r = (Reaction) abstractSBase2;
      model.removeReaction(r);
      
    } else if ((abstractSBase instanceof Species)
        && (abstractSBase2 instanceof Species)) {
      Species s1 = (Species) abstractSBase;
      Species s2 = (Species) abstractSBase2;
      if (model.isSetListOfReactions()) {
      	for (Reaction r : model.getListOfReactions()) {
      		if (r.isSetListOfProducts()) {
      			for (SpeciesReference sr : r.getListOfProducts()) {
      				if (sr.getSpeciesInstance() == s2) {
      					if (r.getId().equals("r0422")) {
      						System.out.println();
      					}
      					sr.setSpecies(s1);
      				}
      			}
      		}
      		if (r.isSetListOfReactants()) {
      			for (SpeciesReference sr : r.getListOfReactants()) {
      				if (sr.getSpeciesInstance() == s2) {
      					if (r.getId().equals("r0422")) {
      						System.out.println();
      					}
      					sr.setSpecies(s1);
      				}
      			}
      		}
      		if (r.isSetListOfModifiers()) {
      			for (ModifierSpeciesReference sr : r.getListOfModifiers()) {
      				if (sr.getSpeciesInstance() == s2) {
      					sr.setSpecies(s1);
      				}
      			}
      		}
      	}
      }
      model.removeSpecies(s2);
    } else if ((abstractSBase instanceof Compartment)
        && (abstractSBase2 instanceof Compartment)) {
      Compartment c1 = (Compartment) abstractSBase;
      Compartment c2 = (Compartment) abstractSBase2;
      if (model.isSetListOfSpecies()) {
      	for (Species s : model.getListOfSpecies()) {
      		if (s.getCompartmentInstance() == c2) {
      			s.setCompartment(c1);
      		}
      	}
      }
      if (model.isSetListOfReactions()) {
      	for (Reaction r : model.getListOfReactions()) {
      		if (r.getCompartmentInstance() != null
      				&& r.getCompartmentInstance() == c2) {
      			r.setCompartment(c1);
      		}
      	}
      }
      model.removeCompartment(c2.getId());
    }
  }
  
  private static boolean checkEquality(AbstractSBase abstractSBase,
    AbstractSBase abstractSBase2) {
    
    if (abstractSBase.getSBOTerm() == abstractSBase2.getSBOTerm()) {
      if ((abstractSBase instanceof Reaction)
          && (abstractSBase2 instanceof Reaction)) {
        Reaction r1 = (Reaction) abstractSBase;
        Reaction r2 = (Reaction) abstractSBase2;
        if (r1.getId().equals("r0422") || r2.getId().equals("r0422")) {
          System.out.println();
        }
        if (r1.getId().split("_")[0].equals(r2.getId().split("_")[0])) { return false; }
        
        //determine compartment of reaction 1
        Compartment c1 = r1.getCompartmentInstance();
        if (c1 == null) {
          if (r1.getReactantCount() > 0) {
            Species s = r1.getReactant(0).getSpeciesInstance();
            if (s != null) {
              c1 = s.getCompartmentInstance();
            }
          }
        }
        if (c1 == null) {
          if (r1.getProductCount() > 0) {
            Species s = r1.getProduct(0).getSpeciesInstance();
            if (s != null) {
              c1 = s.getCompartmentInstance();
            }
          }
        }
        //determine compartment of reaction 2
        Compartment c2 = r2.getCompartmentInstance();
        if (c2 == null) {
          if (r2.getReactantCount() > 0) {
            Species s = r2.getReactant(0).getSpeciesInstance();
            if (s != null) {
              c2 = s.getCompartmentInstance();
            }
          }
        }
        if (c2 == null) {
          if (r2.getProductCount() > 0) {
            Species s = r2.getProduct(0).getSpeciesInstance();
            if (s != null) {
              c2 = s.getCompartmentInstance();
            }
          }
        }
        if (c1 == c2) { return true; }
        
      } else if ((abstractSBase instanceof Species)
          && (abstractSBase2 instanceof Species)) {
        Species s1 = (Species) abstractSBase;
        Species s2 = (Species) abstractSBase2;
        if (!(s1.getId().startsWith("_"))
            && (s1.getId().split("_")[0].equals(s2.getId().split("_")[0]))) { return false; }
        if ((s1.getId().startsWith("_"))
            && !(s1.getId().split("_")[1].equals(s2.getId().split("_")[1]))) { return false; }
        if ((s1.getSBOTerm() == s2.getSBOTerm())
            && (s1.getCompartmentInstance() == s2.getCompartmentInstance())) { return true; }
      } else if ((abstractSBase instanceof Compartment)
          && (abstractSBase2 instanceof Compartment)) {
        if (((Compartment) abstractSBase).getId().split("_")[0]
            .equals(((Compartment) abstractSBase2).getId().split("_")[0])) {
          return false;
        } else {
          return true;
        }
      }
    }
    return false;
  }
  
  public static void main(String[] args) throws XMLStreamException,
    IOException, SBMLException {
    String file = "files/Modell_Hofmann.xml";
    mergeCompartments(file);
  }
  
}
