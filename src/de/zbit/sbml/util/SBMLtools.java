/*
 * $Id:  SBMLtools.java 17:32:00 draeger$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2015 by the University of Tuebingen, Germany.
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

import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.logging.Logger;

import javax.swing.tree.TreeNode;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.MathContainer;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.NamedSBase;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.Unit;
import org.sbml.jsbml.UnitDefinition;
import org.sbml.jsbml.util.filters.NameFilter;

import de.zbit.util.ResourceManager;

/**
 * Useful tools for working with SBML.
 * 
 * @author Andreas Dr&auml;ger
 * @author Sarah Rachel M&uuml;ller vom Hagen
 * @author Sebastian Nagel
 * @author Clemens Wrzodek
 * @version $Rev$
 * @since 1.1
 */
public class SBMLtools extends org.sbml.jsbml.util.SBMLtools {
  
  /**
   * A {@link Logger} for this class.
   */
  private static final Logger logger = Logger.getLogger(SBMLtools.class.getName());
  
  /**
   * 
   * @param <T>
   * @param listOf
   * @param element
   */
  public static final <T extends NamedSBase> void addOrReplace(ListOf<T> listOf, T element) {
    T prev = listOf.firstHit(new NameFilter(element.getId()));
    if (prev != null) {
      listOf.remove(prev);
    }
    listOf.add(element);
  }
  
  /**
   * Returns a name associated to this given {@link NamedSBase}. It first tries
   * the name, then the id, finally, the element's name.
   * 
   * @param nsb
   * @return a {@link String} describing the given element.
   */
  public static String getName(NamedSBase nsb) {
    if (nsb == null) {
      return "";
    }
    if (nsb.isSetName()) {
      return nsb.getName();
    }
    if (nsb.isSetId()) {
      return nsb.getId();
    }
    return nsb.getElementName();
  }
  
  /**
   * 
   * @param sbase
   * @param term
   */
  public static final void setSBOTerm(SBase sbase, int term) {
    if (-1 < sbase.getLevelAndVersion().compareTo(Integer.valueOf(2),
      Integer.valueOf(2))) {
      sbase.setSBOTerm(term);
    } else {
      ResourceBundle bundle = ResourceManager.getBundle("de.zbit.sbml.locales.Messages");
      logger.warning(MessageFormat.format(
        bundle.getString("COULD_NOT_SET_SBO_TERM"),
        SBO.sboNumberString(term), sbase.getElementName(), sbase.getLevel(), sbase.getVersion()));
    }
  }
  
  /**
   * 
   * @param node
   * @param unit
   */
  public static final void setUnits(ASTNode node, UnitDefinition unit) {
    setUnits(node, unit.getId());
  }
  
  /**
   * 
   * @param node
   * @param unit
   */
  public static final void setUnits(ASTNode node, Unit.Kind unit) {
    setUnits(node, unit.toString().toLowerCase());
  }
  
  /**
   * 
   * @param node
   * @param unit
   */
  public static final void setUnits(ASTNode node, String unit) {
    MathContainer container = node.getParentSBMLObject();
    if ((container != null) && (container.getLevel() > 2)) {
      node.setUnits(unit);
    }
  }
  
  /**
   * 
   * @param sbase
   * @param doc
   */
  public static void updateAnnotation(SBase sbase, SBMLDocument doc) {
    if (sbase.isSetMetaId()) {
      sbase.setMetaId(doc.nextMetaId());
    }
    for (int i = 0; i < sbase.getChildCount(); i++) {
      TreeNode child = sbase.getChildAt(i);
      if (child instanceof SBase) {
        updateAnnotation((SBase) child, doc);
      }
    }
  }
  
  /**
   * 
   * @param sbase
   * @return
   */
  public static String createHTMLfromNotes(SBase sbase) {
    String text = toXML(sbase.getNotes());
    if (text.startsWith("<notes") && text.endsWith("notes>")) {
      text = text.substring(toXML(sbase.getNotes()).indexOf('>') + 1,
        toXML(sbase.getNotes()).lastIndexOf('/') - 1);
    }
    text = text.trim().replace("/>", ">");
    if (!text.startsWith("<body") && !text.endsWith("</body>")) {
      text = "<body>" + text + "</body>";
    }
    text = "<html><head></head>" + text + "</html>";
    return text;
  }
  
  
  /**
   * Appends "_&lt;number&gt;" to a given String. &lt;number&gt; is being set to
   * the next free number, so that this sID is unique in this
   * {@link SBMLDocument}. Should only be called from {@link #nameToSId(String)}.
   * 
   * @return
   */
  private static String incrementSIdSuffix(String prefix, SBMLDocument doc) {
    int i = 1;
    String aktString = prefix + "_" + i;
    Model model = doc.getModel();
    while (model.containsUniqueNamedSBase(aktString)) {
      aktString = prefix + "_" + (++i);
    }
    return aktString;
  }
  
  /**
   * Generates a valid SId from a given name. If the name already is a valid
   * SId, the name is returned. If the SId already exists in this document,
   * "_&lt;number>" will be appended and the next free number is being assigned.
   * => See SBML L2V4 document for the Definition of SId. (Page 12/13)
   * 
   * @param name
   * @return SId
   */
  public static String nameToSId(String name, SBMLDocument doc) {
    /*
     * letter = a-z,A-Z; digit = 0-9; idChar = (letter | digit | _ );
     * SId = ( letter | _ ) idChar*
     */
    String ret;
    if ((name == null) || (name.trim().length() == 0)) {
      ret = incrementSIdSuffix("SId", doc);
    } else {
      // Make unique
      ret = toSId(name);
      Model model = doc.getModel();
      if (model.containsUniqueNamedSBase(ret)) {
        ret = incrementSIdSuffix(ret, doc);
      }
    }
    
    return ret;
  }
  
  /**
   * 
   * @param name
   * @return
   */
  public static String toSId(String name) {
    name = name.trim();
    StringBuilder id = new StringBuilder(name.length() + 4);
    char c = name.charAt(0);
    
    // Must start with letter or '_'.
    if (!(isLetter(c) || c == '_')) {
      id.append("_");
    }
    
    // May contain letters, digits or '_'
    for (int i = 0; i < name.length(); i++) {
      c = name.charAt(i);
      if (c == ' ') {
        c = '_'; // Replace spaces with "_"
      }
      
      if (isLetter(c) || Character.isDigit(c) || (c == '_')) {
        id.append(c);
      } else if ((c == '-') || (c == '(') || (c == ')')) {
        if (i < name.length() - 1) {
          id.append('_');
        }
      } // else: skip other invalid characters
    }
    return id.toString();
  }
  
  /**
   * Returns true if c is out of A-Z or a-z.
   * @param c
   * @return
   */
  private static boolean isLetter(char c) {
    // Unfortunately Character.isLetter also accepts ??, but SBML doesn't.
    // a-z or A-Z
    return ((c >= 97) && (c <= 122)) || ((c >= 65) && (c <= 90));
  }
  
  /**
   * Generate a valid SBML identifier using UUID.
   * 
   * @param model
   * @return
   */
  public static String nextId(Model model) {
    String idOne;
    do {
      idOne = UUID.randomUUID().toString().replace("-", "_");
      if (Character.isDigit(idOne.charAt(0))) {
        // Add an underscore at the beginning of the new id only if
        // necessary.
        idOne = '_' + idOne;
      }
    } while (model.findNamedSBase(idOne) != null);
    return idOne;
  }
  
}
