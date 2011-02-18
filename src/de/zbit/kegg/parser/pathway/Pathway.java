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
package de.zbit.kegg.parser.pathway;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Main Kegg document. Corresponding to the Kegg Pathway class
 * (see {@link http://www.genome.jp/kegg/xml/docs/})
 * 
 * @author wrzodek
 * @version $Rev$
 * @since 1.0
 */
public class Pathway {
	/* see http://www.genome.jp/kegg/xml/docs/ */
	/**
	 * keggid.type the KEGGID of this pathway map
	 */
	private String name = "";
	/**
	 * maporg.type ko/ec/[org prefix]
	 */
	private String org = "";
	/**
	 * mapnumber.type the map number of this pathway map (5 digit integer)
	 */
	private int number = 0;
	/**
	 * string.type the title of this pathway map
	 */
	private String title = "";
	/**
	 * url.type the resource location of the image file of this pathway map
	 */
	private String image = "";
	/**
	 * url.type the resource location of the information about this pathway map
	 */
	private String link = "";
	/**
	 * All entries of the pathway
	 */
	private ArrayList<Entry> entries = new ArrayList<Entry>();
	/**
	 * All reactions of the pathway
	 */
	private ArrayList<Reaction> reactions = new ArrayList<Reaction>();
	/**
	 * All relations of the pathway
	 */
	private ArrayList<Relation> relations = new ArrayList<Relation>();

	/*
	 *  Custom Variables, not in the KGML specification
	 */
	
	/**
	 * Comment of the source KGML file.
	 */
	private String comment = null;
	/**
	 * Version number of the source KGML file
	 */
	private double version=0;
	
	/*
	 * Speed improvements
	 */

	/**
	 * Contains ids of entrys and the entry itself.
	 */
	private Map<Integer, Entry> idMap = new HashMap<Integer, Entry>();
	
  /**
   * Contains names of entrys and the entry itself.
   */
  private Map<String, Entry> nameMap = new HashMap<String, Entry>();

  /**
   * Contains names of reactions and entrys that modify this reactions (usually enzymes).
   */
  private Map<String, List<Entry>> reactionModifiers = new HashMap<String, List<Entry>>();
	
  /**
   * The maxmimum id number of an contained entry.
   */
  private int maxId=0;
  
	/**
	 * 
	 */
	private Pathway() {
		super();
	}

	/**
	 * 
	 * @param name
	 * @param org
	 * @param number
	 */
	public Pathway(String name, String org, int number) {
		this();
		setName(name);
		setOrg(org);
		setNumber(number);
	}

	/**
	 * 
	 * @param name
	 * @param org
	 * @param number
	 * @param title
	 * @param image
	 * @param link
	 */
	public Pathway(String name, String org, int number, String title,
			String image, String link) {
		this(name, org, number);
		setTitle(title);
		setImage(image);
		setLink(link);
	}

	/**
	 * 
	 * @param e
	 */
	public void addEntry(Entry e) {
	  idMap.put(e.getId(), e);
	  nameMap.put(e.getName(), e);
	  addReactionModifier(e);
	  
	  maxId=Math.max(maxId, e.getId());
	  
		entries.add(e);
	}
	
	/**
	 * If and only if e is a reactionModifier, it is
	 * added to the list.
	 * @param e
	 */
	private void addReactionModifier(Entry e) {
	  addReactionModifier(e, e.getReaction());
	}
	/**
   * If and only if e is a reactionModifier, it is
   * added to the list.
	 * @param e
	 * @param reactionName
	 */
	private void addReactionModifier(Entry e, String reactionName) {
    if (reactionName!=null && reactionName.length()>0) {
      List<Entry> l = reactionModifiers.get(reactionName);
      if (l==null) {
        l = new LinkedList<Entry>();
        reactionModifiers.put(reactionName, l);
      }
      if (!l.contains(e)) l.add(e);
    }
	}
	
  /**
   * Removes the given entry from the {@link #reactionModifiers}
   * list.
   * @param e
   */
  private void removeReactionModifier(Entry entry) {
    if (entry.getReaction()!=null && entry.getReaction().length()>0) {
      List<Entry> l = reactionModifiers.get(entry.getReaction());
      if (l!=null) l.remove(entry);
    }
  }

	/**
	 * 
	 * @param r
	 */
	public void addReaction(Reaction r) {
		reactions.add(r);
	}

	/**
	 * 
	 * @param r
	 */
	public void addRelation(Relation r) {
		relations.add(r);
	}

	/**
	 * Returns the complete entry list.
	 * 
	 * You should NOT modify this list, but use the provided methods
	 * (e.g. {@link #addEntry(Entry)}), else the id and name references
	 * are getting corrupt. 
	 * @return
	 */
	public ArrayList<Entry> getEntries() {
		return entries;
	}
	
	/**
	 * Iteratively parsing through all entries and
	 * returning the maximum entry id.
	 * @return
	 */
	public int getMaxEntryId() {
	  return maxId;
	}

	/**
	 * Iterates through all entries all returns the entry
	 * with the given id if it exists, or null.
	 * @param id
	 * @return
	 */
	public Entry getEntryForId(int id) {
		/*for (int i = 0; i < entries.size(); i++)
			if (entries.get(i).getId() == id)
				return entries.get(i); 
		return null;*/
	  return idMap.get(id);
	}

	/**
	 * Returns the entry that has a name that
	 * equalsIgnoreCase the given name.
	 * If no entry with this contidion can be found,
	 * returns null.
	 * @param name
	 * @return
	 */
	public Entry getEntryForName(String name) {
		/*for (int i = 0; i < entries.size(); i++)
			if (entries.get(i).getName().equalsIgnoreCase(name))
				return entries.get(i);
		return null;*/
	  return nameMap.get(name);
	}
	
	/**
	 * Returns all modifiers (usually enzymes) for the given
	 * reaction, as noted in the KGML document.
	 * @param reactionName - e.g. "rn:R01662"
	 * @return List<Entry> or null
	 */
	public List<Entry> getReactionModifiers(String reactionName) {
	  return reactionModifiers.get(reactionName);
	}

	/**
	 * 
	 * @return
	 */
	public String getImage() {
		return image;
	}

	/**
	 * 
	 * @return
	 */
	public String getLink() {
		return link;
	}

	/**
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * Remember: Pathway number is an 5-digit integer. So if size < 5 you need
	 * to make a prefix of 0's. You'd better use getNumberReal() instead.
	 */
	public int getNumber() {
		return number;
	}

	/**
	 * 
	 * @return
	 */
	public String getNumberReal() {
		String ret = Integer.toString(number);
		while (ret.length() < 5)
			ret = "0" + ret;
		return ret;
	}

	/**
	 * 
	 * @return
	 */
	public String getOrg() {
		return org;
	}

	/**
	 * 
	 * @return
	 */
	public ArrayList<Reaction> getReactions() {
		return reactions;
	}

	/**
	 * 
	 * @return
	 */
	public ArrayList<Relation> getRelations() {
		return relations;
	}

	/**
	 * 
	 * @return
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * 
	 * @param image
	 */
	public void setImage(String image) {
		this.image = image;
	}

	/**
	 * 
	 * @param link
	 */
	public void setLink(String link) {
		this.link = link;
	}

	/**
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 
	 * @param number
	 */
	public void setNumber(int number) {
		this.number = number;
	}

	/**
	 * 
	 * @param org
	 */
	public void setOrg(String org) {
		this.org = org;
	}

	/**
	 * 
	 * @param title
	 */
	public void setTitle(String title) {
		this.title = title;
	}

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public double getVersion() {
    return version;
  }

  public void setVersion(double version) {
    this.version = version;
  }

  /**
   * This function has to be called by entries, before their
   * ids change. It does NOT perform the change itself.
   * @param entry - Entry object BEFORE THE CHANGE
   * @param id - new id.
   */
  protected void idChange(Entry entry, int id) {
    // Change the maxId
    resetMaxId(entry);
    maxId=Math.max(maxId, id);
    
    // Change the idMap
    idMap.remove(entry.getId());
    idMap.put(id, entry);
  }
  
  /**
   * Reset the maxId by iterating through all elements
   * @param entry - entry to IGNORE if it occurs. Set to
   * null to disable this feature.
   */
  private void resetMaxId(Entry entry) {
    if (entry.getId() == maxId) {
      maxId=0;
      for (Entry e:getEntries()) {
        if (entry!=null && e.equals(entry)) continue;
        maxId = Math.max(maxId, e.getId());
      }
    }
  }

  /**
   * This function has to be called by entries, before their
   * names change. It does NOT perform the change itself.
   * @param entry - Entry object BEFORE THE CHANGE
   * @param name - the new name.
   */
  protected void nameChange(Entry entry, String name) {
    // Change the nameMap
    nameMap.remove(entry.getName());
    nameMap.put(name, entry);
  }
  
  /**
   * This function has to be called by entries, before their
   * reaction changes. It does NOT perform the change itself.
   * @param entry - Entry object BEFORE THE CHANGE
   * @param reaction - the new reaction.
   */
  protected void reactionChange(Entry entry, String reaction) {
    // Remove the old entry
    removeReactionModifier(entry);
    addReactionModifier(entry, reaction);
  }
  
  /**
   * Remove the given entry from this pathway.
   * @param e
   */
  public void removeEntry(Entry e) {
    removeEntry(entries.indexOf(e));
  }
  /**
   * Remove the entry at the given index in the {@link #entries} list.
   * @param index - index of the entry to remove.
   */
  public void removeEntry(int index) {
    // Comment [wrzodek 2010-07-01]: Entry may still be in other entries compound (group node) list.
    if (index<0) return;
    Entry e = entries.get(index);
    
    idMap.remove(e.getId());
    nameMap.remove(e.getName());
    removeReactionModifier(e);
    if (maxId==e.getId()) resetMaxId(e);
    
    entries.remove(index);
    
    e.setName(Entry.removedNodeName);
    e=null;
  }

	
}
