/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2011 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.kegg;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import de.zbit.kegg.parser.pathway.Entry;
import de.zbit.kegg.parser.pathway.EntryType;
import de.zbit.kegg.parser.pathway.Pathway;
import de.zbit.kegg.parser.pathway.Reaction;
import de.zbit.kegg.parser.pathway.ReactionComponent;
import de.zbit.kegg.parser.pathway.Relation;
import de.zbit.kegg.parser.pathway.SubType;

/**
 * @author wrzodek
 * @version $Rev$
 */
public class KeggTools {
  
  /**
   * Retrieves all Equations and Enzymes for all reactions in the pathway.
   * Looks, which of them is already contained in the pathway and adds all
   * missing reactants and enzymes to the pathway.
   * @param p
   * @param manager
   */
  public static void autocompleteReactions(Pathway p, KeggInfoManagement manager) {
    int newEntrysId = p.getMaxEntryId();
    
    for (Reaction r : p.getReactions()) {
      for (String ko_id : r.getName().split(" ")) {
        
        // Get the complete reaction from Kegg
        KeggInfos infos = new KeggInfos(ko_id, manager);
        if (infos.queryWasSuccessfull()) {
          
          // Add missing reactants
          if (infos.getEquation()!=null) {
            String eq = infos.getEquation();
            int dividerPos = eq.indexOf("<=>");
            eq = eq.replace("<=>", "+");
            
            int curPos = eq.indexOf("+");
            int lastPos = 0;
            while (lastPos>=0) {
              String reactant = eq.substring(lastPos, curPos>=0?curPos:eq.length()).trim();
              boolean isSubstrate = (lastPos<dividerPos);
              if (reactant.contains(" ")) { // e.g. "2 C00103"
                reactant = reactant.substring(reactant.indexOf(" ")+1);
              }
              if (reactant.charAt(0)!='C') {
                System.err.println("Warning: non-compound reactat: " + reactant);
              } else if (!reactant.contains(":")) {
                reactant = "cpd:" + reactant;
              }
              
              Entry found = p.getEntryForName(reactant);
              if (found == null) {
                // Create a new entry
                newEntrysId++;
                Entry entry = new Entry(p, newEntrysId,reactant);
                
                autocompleteLinkAndAddToPathway(p, entry);
                
                if (isSubstrate) r.addSubstrate(new ReactionComponent(entry.getName()));
                else r.addProduct(new ReactionComponent(entry.getName()));
              }
              
              lastPos = curPos<0?curPos:curPos+1;
              curPos = eq.indexOf("+", curPos+1);
            }
          }
          
          
          // Add missing enzymes
          if (infos.getEnzymes()!=null) {
            // Get all Enzymes, that are already contained in the pathway.
            List<Entry> modifier = p.getReactionModifiers(r.getName());
            List<String> contained_enzymes = new LinkedList<String>();
            if (modifier!=null && modifier.size()>0) {
              for (Entry mod : modifier) {
                contained_enzymes.addAll(getKeggEnzymeNames(mod, manager));
              }
            }
            // remark: contained_enzymes contains doubles. But this doesn't matter
            
            // Iterate through all enzymes in the reaction
            String[] enzymes = infos.getEnzymes().trim().replaceAll("\\s+", " ").split(" ");
            for (String enzyme: enzymes) {
              boolean isContained=contained_enzymes.contains(enzyme);
              if (!isContained) { // Add the enzyme
                // Create a new entry
                newEntrysId++;
                Entry entry = new Entry(p, newEntrysId,"EC:"+enzyme,EntryType.enzyme);
                entry.setReaction(r.getName());
                
                autocompleteLinkAndAddToPathway(p, entry);
              }
              
            }
          }
          
          
        }
      }
      
    }
    
  }
  
  /**
   * Builds the cache for all reactions and entries in the pathway. This is much
   * faster than fetching each entry one by one.
   * @param p - The source pathway
   * @param manager - The cache
   * @param autocompleteReactions - Set to true, if you plan to call
   * {@link #autocompleteReactions(Pathway, KeggInfoManagement)} afterwars.
   */
  public static void preFetchInformation(Pathway p, KeggInfoManagement manager, boolean autocompleteReactions) {
    // PreFetch infos. Enormous performance improvement!
    ArrayList<String> preFetchIDs = new ArrayList<String>();
    preFetchIDs.add("GN:" + p.getOrg());
    preFetchIDs.add(p.getName());
    for (Entry entry : p.getEntries()) {
      for (String ko_id : entry.getName().split(" ")) {
        if (ko_id.trim().equalsIgnoreCase("undefined") || entry.hasComponents())
          continue; // "undefined" = group node, which contains "Components"
        preFetchIDs.add(ko_id);
      }
    }
    for (Reaction r : p.getReactions()) {
      for (String ko_id : r.getName().split(" ")) {
        preFetchIDs.add(ko_id);
      }
    }
    manager.precacheIDs(preFetchIDs.toArray(new String[preFetchIDs.size()]));
    
    
    if (autocompleteReactions) {
      // Also prefetch all Enzymes, and reactants of every reaction, even if
      // it's not in the KGML document.
      preFetchIDs.clear();
      for (Reaction r : p.getReactions()) {
        for (String ko_id : r.getName().split(" ")) {
          KeggInfos infos = new KeggInfos(ko_id, manager);

          if (infos.getEquation()!=null) {
            String[] reactants = infos.getEquation().replace("<=>", "+").trim().split(Pattern.quote("+"));
            for (String string : reactants) {
              string = string.trim();
              if (string.contains(" ")) { // e.g. "2 C00103"
                string = string.substring(string.indexOf(" ")+1).trim();
              }
              
              String toAdd = "cpd:"+string;
              if (!preFetchIDs.contains(toAdd)) preFetchIDs.add(toAdd);
            }
          }
          if (infos.getEnzymes()!=null) {
            String[] enzymes = infos.getEnzymes().trim().replaceAll("\\s+", " ").split(" ");
            for (String string : enzymes) {
              String toAdd = "EC:"+string;
              if (!preFetchIDs.contains(toAdd)) preFetchIDs.add(toAdd);
            }
          }
          
        }
      }
      manager.precacheIDs(preFetchIDs.toArray(new String[preFetchIDs.size()]));
    }
    
    // TODO: Add relations?
    // -------------------------
  }
  
  /**
   * Retrieves the Kegg Enzyme IDs for the given entry.
   * You should precache this in the manager!
   * @param manager
   * @return List<String> (empty list if none available).
   */
  public static List<String> getKeggEnzymeNames(Entry entry, KeggInfoManagement manager) {
    List<String> modifier = new LinkedList<String>();
    
    // Add all KEGG Enzyme ids to the modifier
    if (entry.getType().equals(EntryType.enzyme)) {
      modifier.add(entry.getName().contains(":")?
          entry.getName().substring(entry.getName().indexOf(":")+1):entry.getName());
    } else {
      for (String ko_id : entry.getName().split(" ")) {
        KeggInfos infos = new KeggInfos(ko_id, manager);
        if (infos.queryWasSuccessfull() && infos.getDefinition()!=null) {
          String def = infos.getDefinition().replace("]", ")");
          //e.g. "aldehyde dehydrogenase 9 family, member A1 (EC:1.2.1.3 1.2.1.19\n1.2.1.47)"
          
          int pos = def.indexOf("EC:");
          String ECString = pos>0?def.substring(pos+3, def.indexOf(")",pos)):null;
          if (ECString!=null) {
            String[] splitt = ECString.split("\\s");
            for (String string : splitt) {
              if (!modifier.contains(string)) modifier.add(string);
            }
          }
        }
      }
    }
    
    return modifier;
  }


  /**
   * If entry has no link, a default link is set. Afterwards, this item
   * is added to the given pathway.
   * @param p
   * @param entry
   */
  public static void autocompleteLinkAndAddToPathway(Pathway p, Entry entry) {
    if (entry.getLink()==null || entry.getLink().length()<1) {
      entry.setLink("http://www.kegg.jp/dbget-bin/www_bget?"+entry.getName());
    }
    p.addEntry(entry);
  }
  
  
  /**
   * Parses a Kegg Pathway and returns the maximum x and y coordinates
   * 
   * @param p - Kegg Pathway Object
   * @return int[]{x,y}
   */
  public static int[] getMaxCoords(Pathway p) {
    int x = 0, y = 0;
    for (Entry e : p.getEntries()) {
      if (e.hasGraphics()) {
        x = Math.max(x, e.getGraphics().getX()
            + e.getGraphics().getWidth());
        y = Math.max(y, e.getGraphics().getY()
            + e.getGraphics().getHeight());
      }
    }
    return new int[] { x, y };
  }
  
  

  /**
   * Remove all white nodes from the pathway. More specific: removes a entry,
   * if it has graphics information, if the bgcolor endsWith ffffff AND
   * the entryType is gene or ortholog.
   * 
   * Background: Kegg colors in organism specific pathways all nodes white,
   * that do not occur in this organism.
   * 
   * @param p
   */
  public static void removeWhiteNodes(Pathway p) {
    for (int i=0; i<p.getEntries().size(); i++) {
      Entry entry = p.getEntries().get(i);
      if (entry.hasGraphics() && entry.getGraphics().isBGcolorSet() &&          
          entry.getGraphics().getBgcolor().toLowerCase().trim().endsWith("ffffff")
          && (entry.getType() == EntryType.gene || entry.getType() == EntryType.ortholog)) {
        p.removeEntry(i);
        i--;
      }
    }
  }
  
  /**
   * Iterates through all entries and removes all nodes that are
   * not contained in any reaction.
   * <p> For graphical outputs you should set considerRelations to true and
   * considerReactions to false. For function output via versa.</p>
   * @param p - Parent pathway.
   * @param considerRelations - if true, only removes the node if
   * it is also not contained in any relation. If false, relations
   * are getting ignored.
   * @param considerReactions - if true, reactions will be considered
   * as well.
   */
  public static void removeOrphans(Pathway p, boolean considerRelations, boolean considerReactions) {
    for (int i=0; i<p.getEntries().size(); i++) {
      Entry entry = p.getEntries().get(i);
      
      
      // Remove it
      if (isOrphan(p, entry, considerRelations, considerReactions)) {
        p.removeEntry(i);
        i--;
      }
    }
    
  }
  
  /**
   * Returns true if and only if entry is an orphan in p.
   * @param p
   * @param entry
   * @param considerRelations
   * @param considerReactions
   * @return
   */
  public static boolean isOrphan(Pathway p, Entry entry, boolean considerRelations, boolean considerReactions) {
    
    // Look if it is an enzyme (or other reaction modifier)
    if (considerReactions && entry.getReaction() != null && entry.getReaction().length() > 0) {
      return false;
    }
    
    // Loop through all reactions and look for the node.
    boolean found = false;
    if (considerReactions) {
      for (Reaction r : p.getReactions()) {
        for (ReactionComponent rc : r.getProducts())
          if (rc.getName().equalsIgnoreCase(entry.getName())) {
            found = true;
            break;
          }
        if (!found) {
          for (ReactionComponent rc : r.getSubstrates())
            if (rc.getName().equalsIgnoreCase(entry.getName())) {
              found = true;
              break;
            }
        }
        if (found) break;
      }
    }
    
    // Loop through all relations and look for the node.
    if (considerRelations && !found) {
      for (Relation r : p.getRelations()) {
        if (r.getEntry1() == entry.getId() || r.getEntry2() == entry.getId()) {
          found = true;
          break;
        }
        for (SubType st : r.getSubtypes()) {
          try {
            if (Integer.parseInt(st.getValue()) == entry.getId()) {
              found = true;
              break;
            }
          } catch (Exception e) {}
        }
        if (found) break;
      }
    }
    
    // It is an orphan!
    if (!found) {
      boolean orphan=true;
      
      // Look if it is an component (i.e. a member of a group which is not an orphan)!
      for (Entry e2:p.getEntries()) {
        if (e2.hasComponents() && e2.getComponents().contains(entry.getId())) {
          // Look if the parent group node itself is an orphan.
          if (!isOrphan(p, e2, considerRelations, considerReactions)) {
            orphan=false;
            break;
          }
        }
      }
      
      // Make an exception for pathway titles
      // Do not remove name="path:*" & graphics.name="TITLE:*" node (Pathway description)
      try {
        if (entry.getName().startsWith("path:") && entry.getGraphics().getName().startsWith("TITLE:")) {
          orphan = false;
        }
      } catch (Exception e) {
        // Entry has no graphics or such. Not important.
      }
      
      return orphan;
      
    } else {
      // The node has been found in another reaction or relation.
      return false;
    }
  }

  
}
