/*
 * $Id$
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
package de.zbit.kegg;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.ext.fbc.FBCSpeciesPlugin;

import de.zbit.kegg.api.KeggInfos;
import de.zbit.kegg.api.cache.KeggInfoManagement;
import de.zbit.kegg.parser.pathway.Pathway;
import de.zbit.kegg.parser.pathway.Reaction;
import de.zbit.kegg.parser.pathway.ReactionComponent;

/**
 * Static class to check atom balances of KEGG reactions.
 * @author Clemens Wrzodek
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 */
public final class AtomBalanceCheck {
  
  /**
   * A {@link Logger} for this class.
   */
  private static final transient Logger logger = Logger.getLogger(AtomBalanceCheck.class.getName());
  
  /**
   * A defined level to output the results of the atom balance check
   * in the logger.
   */
  public static Level level = Level.FINE;
  
  /**
   * 
   * @param logLevel
   */
  public static void setLogLevel(Level logLevel) {
    level = logLevel;
  }
  
  /**
   * A class used to improve return values of contained methods
   * @author Clemens Wrzodek
   * @version $Rev$
   */
  public static final class AtomCheckResult<R> {
    final R r;
    final Map<String, Integer> atomsLeft;
    final Map<String, Integer> atomsRight;
    final Map<String, Integer> defects;
    
    /**
     * @param r
     * @param atomsLeft
     * @param atomsRight
     * @param defects
     */
    public AtomCheckResult(R r, Map<String, Integer> atomsLeft,
      Map<String, Integer> atomsRight, Map<String, Integer> defects) {
      super();
      this.r = r;
      this.atomsLeft = atomsLeft;
      this.atomsRight = atomsRight;
      this.defects = defects;
    }
    
    /**
     * @return the reaction
     */
    public R getReaction() {
      return r;
    }
    
    /**
     * @return the atomsLeft
     */
    public Map<String, Integer> getAtomsLeft() {
      return atomsLeft;
    }
    
    /**
     * @return the atomsRight
     */
    public Map<String, Integer> getAtomsRight() {
      return atomsRight;
    }
    
    /**
     * @return the defects
     */
    public Map<String, Integer> getDefects() {
      return defects;
    }
    
    /**
     * Get all atoms occuring in this reaction
     * @return list of atoms
     */
    public Collection<String> getAtoms() {
      Set<String> atoms = new TreeSet<String>();
      atoms.addAll(atomsLeft.keySet());
      atoms.addAll(atomsRight.keySet());
      return atoms;
    }
    
    /**
     * Convenient method to generate a HTML table showing all results.
     * @return
     */
    public String getResultsAsHTMLtable() {
      Collection<String> atoms = getAtoms();
      if (atoms.size() < 1) {
        return "";
      }
      StringBuilder sb = new StringBuilder("<table style=\"border:1px solid black;\"><tr align=\"right\"><th>&#160;</th>");
      
      // All atoms
      for (String atom : atoms) {
        sb.append(String.format("<th>%s</th>", atom));
      }
      sb.append("</tr>");
      
      // Substrates
      if (atomsLeft.size() > 1) {
        addHTMLtableRow(sb, "Substrate side", atoms, atomsLeft);
      }
      
      // Products
      if (atomsRight.size() > 1) {
        addHTMLtableRow(sb, "Product side", atoms, atomsRight);
      }
      
      // Defects
      if (defects.size() != 0) {
        addHTMLtableRow(sb, "Defects", atoms, defects);
      }
      
      sb.append("</table>");
      return sb.toString();
    }
    
    /**
     * @param sb
     * @param rowName
     * @param atoms
     */
    private static void addHTMLtableRow(StringBuilder sb, String rowName,
      Collection<String> atoms, Map<String, Integer> atomList) {
      sb.append(String.format("<tr align=\"right\"><th align=\"left\">%s</th>", rowName));
      for (String atom: atoms) {
        Integer value = atomList.get(atom);
        if ((value == null) || (value == 0)) {
          sb.append("<td>&#160;</td>");
        } else {
          sb.append(String.format("<td>%s</td>", value));
        }
      }
      sb.append("</tr>");
    }
    
    /**
     * @return {@code true} if this equation is not balanced, i.e.
     * has some missing atoms.
     */
    public boolean hasDefects() {
      return (defects != null) && (defects.size() != 0);
    }
    
  }
  
  
  /**
   * Check atom balances of all reactions in a pathway.
   * @param manager a cache to fetch eqations, formulas, etc. from
   * @param p
   * @param replacement number to be used as a replacement of "n" in empirical formulas.
   * @return
   * @see #checkAtomBalance(KeggInfoManagement, Reaction, int)
   */
  public static List<AtomCheckResult> checkAtomBalance(KeggInfoManagement manager, Pathway p,
    int replacement) {
    
    /* TODO:
     * Option in KEGGtranslator fuer "correct minor deficiencies"
     * (einzelne atome wie H+ selbststaendig ergaenzen).
     * -  Dann aber unbedingt note in reaction dass und was korigiert wurde
     * 
     */
    
    
    if (p.getReactions() != null) {
      List<AtomCheckResult> ret = new ArrayList<AtomCheckResult>(p.getReactions().size());
      
      for (Reaction r : p.getReactions()) {
        ret.add(checkAtomBalance(manager, r, replacement));
      }
      
      return ret;
    }
    
    return null;
  }
  
  
  /**
   * Check atom balance of reaction r
   * @param manager a cache to fetch reactions, formulas, etc. from
   * @param r
   * @param replacement
   *            number to be used as a replacement of "n" in empirical
   *            formulas.
   * @return null if no check is possible. Otherwise an hash with the Atom
   *         defects.
   */
  public static AtomCheckResult<Reaction> checkAtomBalance(KeggInfoManagement manager, Reaction r,
    int replacement) {
    Map<String, Integer> atomsLeft = countAtoms(manager, r.getSubstrates(), replacement);
    Map<String, Integer> atomsRight = countAtoms(manager, r.getProducts(), replacement);
    if (((atomsLeft == null) || (atomsLeft.size() == 0))
        || ((atomsRight == null) || (atomsRight.size() == 0))) {
      logger.log(level, MessageFormat.format("Couldn't check atom balance of reaction {0}.", r.getName()));
      return null;
    }
    Map<String, Integer> defect = calculateDefect(r.getName(), atomsLeft, atomsRight);
    
    return new AtomCheckResult<Reaction>(r, atomsLeft, atomsRight, defect);
  }
  
  
  /**
   * 
   * @param reactionIdentifier
   * @param atomsLeft
   * @param atomsRight
   * @return
   */
  private static Map<String, Integer> calculateDefect(String reactionIdentifier,
    Map<String, Integer> atomsLeft, Map<String, Integer> atomsRight) {
    Map<String, Integer> defect;
    defect = new TreeMap<String, Integer>();
    for (String key : atomsLeft.keySet()) {
      if (!atomsRight.containsKey(key)) {
        defect.put(key, atomsLeft.get(key));
      } else {
        int left = atomsLeft.get(key).intValue();
        int right = atomsRight.get(key);
        if (left != right) {
          defect.put(key, Integer.valueOf((left - right)));
        }
      }
    }
    for (String key : atomsRight.keySet()) {
      if (!atomsLeft.containsKey(key)) {
        defect.put(key, atomsRight.get(key));
      } else {
        int left = atomsLeft.get(key).intValue(), right = atomsRight.get(key);
        if (left != right) {
          defect.put(key, Integer.valueOf((left - right)));
        }
      }
    }
    
    if (defect.size() > 0) {
      //      System.out.println(String.format("Detected incorrect atom balance in reaction %s:", r.getName()));
      //      System.out.println(String.format(
      //          "%s\natoms left:  %s\natoms right:  %s\ndefect:  %s\n",
      //          r.getEquation(), atomsLeft.toString(), atomsRight
      //              .toString(), defect.toString()));
      logger.log(level, MessageFormat.format("Detected incorrect atom balance in reaction ''{0}'': {1}", reactionIdentifier, defect.toString()));
    }
    return defect;
  }
  
  /**
   * 
   * @param r
   * @param replacement
   * @return
   */
  public static AtomCheckResult<org.sbml.jsbml.Reaction> checkAtomBalance(org.sbml.jsbml.Reaction r, int replacement) {
    Map<String, Integer> atomsLeft = countAtoms(r.getListOfReactants(), replacement);
    Map<String, Integer> atomsRight = countAtoms(r.getListOfProducts(), replacement);
    if (((atomsLeft == null) || (atomsLeft.size() == 0))
        || ((atomsRight == null) || (atomsRight.size() == 0))) {
      logger.log(level, MessageFormat.format("Couldn't check atom balance of reaction {0}.", r.getId()));
      return null;
    }
    Map<String, Integer> defect = calculateDefect(r.getName(), atomsLeft, atomsRight);
    return new AtomCheckResult<org.sbml.jsbml.Reaction>(r, atomsLeft, atomsRight, defect);
  }
  
  /**
   * 
   * @param manager
   * @param listOfSpecRefs
   * @param replacement
   *            number to be used as a replacement if "n" occurs in an
   *            empirical formula.
   * @return
   */
  public static Map<String, Integer> countAtoms(KeggInfoManagement manager,
    List<ReactionComponent> listOfSpecRefs, int replacement) {
    /* TODO: Does this work correctly for (n+1)?
     * Consider, e.g. rn:R04241 "C00002 + C03541(n) + C00025 <=> C00008 + C00009 + C03541(n+1)"!
     * 
     */
    Map<String, Integer> atomCount = new TreeMap<String, Integer>();
    for (ReactionComponent component : listOfSpecRefs) {
      KeggInfos infos = KeggInfos.get(KeggInfos.appendPrefix(component.getName()), manager);
      
      
      if ((infos == null) || !infos.queryWasSuccessfull()) {
        atomCount.clear();
        break;
      }
      
      // Component.getName() might be a glycan and the chemical formula is only given for compounds
      // => Look if we have synonym identifers for KEGG compound and refetch
      String formula = infos.getFormulaDirectOrFromSynonym(manager);
      if (formula != null) {
        countAtoms(replacement, atomCount, component.getStoichiometry() == null ? 1d : component.getStoichiometry().doubleValue(), formula);
      } else {
        atomCount.clear();
        break;
      }
      
    }
    // if (atomCount.containsKey("R"))
    // atomCount.clear();
    return atomCount;
  }
  
  
  /**
   * 
   * @param replacement
   * @param atomCount
   * @param stoichiometricCoefficient
   * @param formula
   */
  private static void countAtoms(int replacement,
    Map<String, Integer> atomCount, double stoichiometricCoefficient, String formula) {
    // TODO: consider better replacement.
    Map<String, Integer> count = countAtoms(stoichiometricCoefficient, formula, replacement);
    for (String key : count.keySet()) {
      if (!atomCount.containsKey(key)) {
        atomCount.put(key, Integer.valueOf(0));
      }
      atomCount.put(key, Integer.valueOf(atomCount.get(key).intValue() + count.get(key).intValue()));
    }
  }
  
  /**
   * 
   * @param listOfSpeciesReferences
   * @param replacement
   * @return
   */
  public static Map<String, Integer> countAtoms(ListOf<SpeciesReference> listOfSpeciesReferences, int replacement) {
    Model model = listOfSpeciesReferences.getModel();
    Map<String, Integer> atomCount = new TreeMap<String, Integer>();
    for (SpeciesReference specRef : listOfSpeciesReferences) {
      Species species = model.getSpecies(specRef.getSpecies());
      if (species != null) {
        // getExtension does not create the extension (in contrast to getPlugin).
        FBCSpeciesPlugin specPlug = (FBCSpeciesPlugin) species.getExtension("fbc");
        if ((specPlug != null) && (specPlug.isSetChemicalFormula()) && !Double.isNaN(specRef.getStoichiometry())) {
          countAtoms(replacement, atomCount, specRef.getStoichiometry(), specPlug.getChemicalFormula().trim());
        } else {
          atomCount.clear();
          break;
        }
      } else {
        logger.severe(MessageFormat.format("No species set for speciesReference ''{0}''.", specRef.getId()));
        atomCount.clear();
        break;
      }
    }
    return atomCount;
  }
  
  /**
   * 
   * @param stoichiometry
   * @param formula
   * @param replacement
   *            used if "n" occurs in the formula (as number of atoms).
   * @return
   */
  public static Map<String, Integer> countAtoms(double stoichiometry,
    String formula, int replacement) {
    Map<String, Integer> atomCount = new TreeMap<String, Integer>();
    StringBuilder name = new StringBuilder(), number = new StringBuilder();
    StringBuilder newFormula = new StringBuilder();
    boolean brackets = false;
    boolean digitAfter = false;
    for (int i = 0; i < formula.length(); i++) {
      char c = formula.charAt(i);
      if (Character.isLetterOrDigit(c)) {
        if (brackets) {
          name.append(c);
        } else {
          if (digitAfter) {
            if (Character.isUpperCase(c)) {
              if (number.length() == 0) {
                number.append(1);
              }
              Map<String, Integer> inlay = countAtoms(1,
                name.toString(), replacement);
              for (String key : inlay.keySet()) {
                if (!atomCount.containsKey(key)) {
                  atomCount.put(key, Integer.valueOf(0));
                }
                int mult = number.toString().contains("n") ? replacement
                    : Integer.parseInt(number.toString());
                int val = (int) (stoichiometry
                    * inlay.get(key).intValue() * mult);
                atomCount.put(key, Integer.valueOf(val));
              }
              digitAfter = false;
              // Do not output something here... if it is important, log it below info!
              //System.out.printf("cut:\t%s\t%s", name, number);
            } else {
              number.append(c);
            }
          }
          if (!digitAfter) {
            newFormula.append(c);
          }
        }
      } else if (c == '(') {
        brackets = true;
        digitAfter = false;
        name = new StringBuilder();
        number = new StringBuilder();
      } else if (c == ')') {
        brackets = false;
        digitAfter = true;
      }
    }
    for (char c : newFormula.toString().toCharArray()) {
      if (Character.isLetterOrDigit(c)) {
        if (Character.isUpperCase(c)) {
          if (name.length() > 0) {
            if (number.length() == 0) {
              number.append(1);
            }
            if (digitAfter) {
              Map<String, Integer> inlay = countAtoms(1,
                name.toString(), replacement);
              for (String key : inlay.keySet()) {
                if (!atomCount.containsKey(key)) {
                  atomCount.put(key, Integer.valueOf(0));
                }
                int mult = number.toString().contains("n") ? replacement
                    : Integer.parseInt(number.toString());
                int val = (int) (stoichiometry
                    * inlay.get(key).intValue() * mult);
                atomCount.put(key, Integer.valueOf(val));
              }
              digitAfter = false;
            } else {
              String key = name.toString();
              if (atomCount.containsKey(key)) {
                atomCount.put(key, (int) (atomCount.get(key)
                    .intValue() + stoichiometry
                    * Integer.parseInt(number.toString())));
              } else {
                int num = number.toString().contains("n") ? num = replacement
                    : Integer.parseInt(number.toString());
                atomCount.put(key, (int) (stoichiometry * num));
              }
            }
          }
          name = new StringBuilder(Character.toString(c));
          number = new StringBuilder();
        } else if (Character.isDigit(c)) {
          number.append(Character.toString(c));
        } else {
          name.append(c);
        }
      }
    }
    if (name.length() > 0) {
      String key = name.toString();
      if (number.length() == 0) {
        number.append(1);
      }
      if (atomCount.containsKey(key)) {
        atomCount.put(key, atomCount.get(key).intValue()
          + (int) (stoichiometry * Integer.parseInt(number
            .toString())));
      } else {
        int num = number.toString().contains("n") ? num = replacement
            : Integer.parseInt(number.toString());
        atomCount.put(key, (int) (stoichiometry * num));
      }
    }
    return atomCount;
  }
  
}
